package com.cy.ns.raft.node;

import static java.util.Arrays.sort;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cy.ns.common.constants.RaftConstants;
import com.cy.ns.raft.command.Command;
import com.cy.ns.raft.config.NacosConfiguration;
import com.cy.ns.raft.config.RabbitMQConfiguration;
import com.cy.ns.raft.config.RaftConfiguration;
import com.cy.ns.raft.entity.po.ClusterLogPO;
import com.cy.ns.raft.entity.po.FilePO;
import com.cy.ns.raft.entity.po.NodeInfoPO;
import com.cy.ns.raft.enums.LogType;
import com.cy.ns.raft.enums.RaftRole;
import com.cy.ns.raft.log.LogEntry;
import com.cy.ns.raft.netty.channel.RaftChannel;
import com.cy.ns.raft.netty.msg.AppendLogEntriesMsg;
import com.cy.ns.raft.netty.msg.AppendLogEntriesResMsg;
import com.cy.ns.raft.netty.msg.HeartbeatMsg;
import com.cy.ns.raft.netty.msg.Message;
import com.cy.ns.raft.netty.msg.PullFileRequestMsg;
import com.cy.ns.raft.netty.msg.PullFileResMsg;
import com.cy.ns.raft.netty.msg.RequestVoteMsg;
import com.cy.ns.raft.service.ClusterLogService;
import com.cy.ns.raft.service.FileService;
import com.cy.ns.raft.service.NodeInfoService;
import com.cy.ns.rs.utils.RSCodingUtil;
import io.netty.channel.Channel;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @author Haechi
 * @date 2025/3/16
 */
@Data
@Component
@Slf4j
public class RaftServerNode {

    private final ReentrantReadWriteLock stateLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock logLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock applyLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock storageLock = new ReentrantReadWriteLock();
    private volatile  Boolean initStorage = false;


    /**
     * 本节点的ip
     */
    @Value("${raft.server.ip:localhost}")
    private String ip;
    /**
     * 端口
     */
    @Value("${raft.server.port:8081}")
    private int port;

    private int id;
    /**
     * 任期
     */
    private Long currentTerm;

     /**
     * 投票给谁
     */
    private int voteFor;

    /**
     * 角色
     */
    private volatile RaftRole role;

    /**
     * 服务端通道
     */
    private Channel channel;

    private int leaderId;

    /**
     * 提交索引
     */
    private volatile int commitIndex;

    /**
     * 应用索引 当前日志执行到哪， 日志先提交才能执行
     */
    private volatile int lastApplied;

    /**
     * 下一个索引
     */
    private volatile Map<Integer,Integer> nextIndex;

    /**
     * 匹配索引
     */
    private volatile Map<Integer,Integer> matchIndex;

    /**
     * 日志列表
     */
    private volatile List<LogEntry> logs;


    /**
     * 最后一个日志索引和任期
     */
    private AtomicInteger lastLogIndex = new AtomicInteger();
    private volatile Long lastLogTerm;

    /**
     * 最后一个快照索引和任期
     */
    private int lastSnapshotIndex;
    private  Long lastSnapshotTerm;

    public static Map<Integer, RaftClientNode> raftNodeMap;

    /**
     * 用于跟踪日志提交的监听器
     */
    private volatile Map<Long, CompletableFuture<Long>> commitListeners = new ConcurrentHashMap<>();
    private volatile Map<String, CompletableFuture<Boolean>> recoveryListeners = new ConcurrentHashMap<>();
    private volatile Map<String, CompletableFuture<Boolean>> deleteListeners = new ConcurrentHashMap<>();

    private ScheduledExecutorService serverSchedule = Executors.newSingleThreadScheduledExecutor();

    private ScheduledExecutorService electionSchedule = Executors.newSingleThreadScheduledExecutor();

    private ScheduledExecutorService logAppendSchedule = Executors.newSingleThreadScheduledExecutor();



    @Resource
    @Lazy
    private RaftChannel raftChannel;

    @Resource
    private NodeInfoService nodeInfoService;

    @Resource
    private ClusterLogService clusterLogService;

    @Resource
    private FileService fileService;

    @Resource
    private NacosConfiguration nacosConfiguration;

    @Resource
    private RabbitMQConfiguration rabbitMQConfiguration;


    /**
     * 选举结果集
     */
    private Map<Integer, Boolean> voteResultMap;


    /**
     * 选举票数
     */
    private int voteCount;

    public void start() {
        // 初始化
        channel = raftChannel.createServer( port );
        raftNodeMap = new HashMap<>();
        voteResultMap = new HashMap<>();
        nextIndex = new HashMap<>();
        matchIndex = new HashMap<>();
        logs = new ArrayList<>();
        id = RaftConfiguration.id;

        initLog(id);

        // 建立其他节点的连接
        RaftConfiguration.raftClientNodes.forEach( raftClientNode -> {
           raftClientNode.setChannel(raftChannel.createClient( raftClientNode.getIp(), raftClientNode.getPort() ));
           raftNodeMap.put( raftClientNode.getId(), raftClientNode );
        } );

        log.info( "Node {} start success.", id );
    }

    @PreDestroy
    public void stop() {

        if ( channel != null ) {
            channel.close();
        }
        serverSchedule.shutdownNow();
        RaftConfiguration.raftClientNodes.forEach( raftClientNode -> {
            if(raftClientNode.getChannel() != null){
                raftClientNode.getChannel().close();
            }
        } );
        log.info( "server stop time: {}", new Date().getTime() );
        log.info( "Node {} stop success.", id );
    }

    private void initLog(int logNodeId){
        // 从数据库中获取日志
        NodeInfoPO nodeInfoPO = nodeInfoService.getById( logNodeId );
        System.out.println(nodeInfoPO);

        commitIndex = nodeInfoPO.getApplyIndex();
        currentTerm = nodeInfoPO.getApplyTerm();
        lastLogIndex.set(nodeInfoPO.getApplyIndex());
        lastLogTerm = nodeInfoPO.getApplyTerm();
        lastApplied = nodeInfoPO.getApplyIndex();
        lastSnapshotIndex = nodeInfoPO.getApplyIndex();
        lastSnapshotTerm = nodeInfoPO.getApplyTerm();
    }

    /**
     * 开始选举
     */
    public void startElection() {
        log.info( "Node {} start election.", id );
        // 遍历所有节点，发送投票请求
        // 选举超时时间内没有收到大多数节点的投票，重新开始选举
        // 收到大多数节点的投票，成为leader
        becomeCandidate();
        serverSchedule.scheduleWithFixedDelay( voteTask(), 0, 5, TimeUnit.SECONDS );
    }


    /**
     * 投票任务
     * @return Runnable
     */
    private Runnable voteTask() {
        return () -> {
            RequestVoteMsg requestVoteMsg = new RequestVoteMsg();
            requestVoteMsg.setTerm( currentTerm );
            requestVoteMsg.setCandidateId( id );
            requestVoteMsg.setLastLogTerm( lastLogTerm );
            requestVoteMsg.setLastLogIndex( lastLogIndex.get() );
            // 遍历map，发送消息
            for ( Map.Entry<Integer, RaftClientNode> entry : raftNodeMap.entrySet() ) {
                // 如果已经投过票，不再发送
                if(voteResultMap.get( entry.getValue().getId())!=null){
                    continue;
                }
                sendMsgToNode( entry.getValue(), requestVoteMsg);
            }
            clusterLogService.save( createClusterLogPO( LogType.INFO.getCode(), "timeout to start election and send vote request ." ) );
        };
    }


    /**
     * 增加选票
     */
    private void increaseVoteCount() {
        voteCount++;
        if ( voteCount >= RSCodingUtil.DATA_SHARDS){
            becomeLeader();
        }
    }


    /**
     * 成为候选人
     */
    public void becomeCandidate() {
        if(role == RaftRole.LEADER) {
            log.error( "This node is already a leader." );
            return;
        }

        // 清空选举结果
        serverSchedule.shutdownNow();
        serverSchedule = Executors.newSingleThreadScheduledExecutor();
        voteResultMap.clear();
        voteCount = 1;
        leaderId = -1;

        // 变为候选者： 任期增加，身份改变，为自己投票
        currentTerm++;
        role = RaftRole.CANDIDATE;
        voteFor = id;
        log.info( "Node {} become candidate, term is {}", id, currentTerm);
        nacosConfiguration.stop();
    }
    /**
     * 成为领导者
     */
    public void becomeLeader() {
        if ( role != RaftRole.CANDIDATE ) {
            log.error( "This node is not a candidate." );
            return;
        }
        role = RaftRole.LEADER;
        log.info( "vote end time: {}", new Date().getTime() );
        log.info( "Node {} become leader, term is {}", id, currentTerm );

        // 设置nextIndex和matchIndex
        for ( Map.Entry<Integer, RaftClientNode> entry : raftNodeMap.entrySet() ) {
            nextIndex.put( entry.getKey(), lastLogIndex.get() + 1 );
            matchIndex.put( entry.getKey(), -1 );
        }

        // 停止选举任务，开始发送心跳
        serverSchedule.shutdownNow();
        serverSchedule = Executors.newSingleThreadScheduledExecutor();
        serverSchedule.scheduleAtFixedRate( heartbeatTask(), 0, 10, TimeUnit.SECONDS );
        logAppendSchedule = Executors.newSingleThreadScheduledExecutor();
        logAppendSchedule.scheduleAtFixedRate( logAppendTask(), 0, 10, TimeUnit.SECONDS );
        nacosConfiguration.start();
        rabbitMQConfiguration.startListeners();
        clusterLogService.save( createClusterLogPO( LogType.INFO.getCode(), " become leader." ) );
        if(!initStorage){
            storageLock.writeLock().lock();
            try {
                if(!initStorage){
                    initStorage = true;
                }
            }finally {
                storageLock.writeLock().unlock();
            }
        }

    }



    /**
     * 成为追随者
     */
    public void becomeFollower(Long term, int candidateId) {
        if ( term < currentTerm ) {
            log.error( "The term is less than the current term." );
            return;
        }
        role = RaftRole.FOLLOWER;
        log.info( "Node {} become follower, term is {}", id, term );

        currentTerm = term;
        voteFor = candidateId;
        serverSchedule.shutdownNow();
        logAppendSchedule.shutdownNow();
        nacosConfiguration.stop();
        rabbitMQConfiguration.stopListeners();

    }

    /**
     * 处理投票响应
     * @param voteGranted 是否投票
     * @param term 任期
     */
    public void solveVoteResponse(Boolean voteGranted, Long term) {
        // 如果已经是leader或者follower，不做处理
        if ( role == RaftRole.LEADER || role == RaftRole.FOLLOWER) {
            return;
        }
        if ( voteGranted ) {
            log.info( "get one vote");
            voteResultMap.put( id, true );
            increaseVoteCount();
        } else {
            log.info( "Node {} refuse vote for me, term is {}", id, term );
            voteResultMap.put( id, false );
        }
    }

    public Boolean appendLogEntries(AppendLogEntriesMsg appendLogEntriesMsg) {
        if(appendLogEntriesMsg.getPrevLogIndex() == RaftConstants.RELOAD_SNAPSHOT_INDEX ){
            // 重新读取快照
            initLog( appendLogEntriesMsg.getLeaderId() );
            log.info( "Reload Snap" );
            return true;
        }
        // 如果日志不匹配，拒绝
        if (appendLogEntriesMsg.getPrevLogIndex()!=-1){
            if(appendLogEntriesMsg.getPrevLogIndex() > lastLogIndex.get() ||
            appendLogEntriesMsg.getPrevLogIndex()< lastSnapshotIndex ) {
                log.info( "Refuse append log for leader {} because of prevLogIndex less than lastSnapshotIndex", appendLogEntriesMsg.getLeaderId() );
                return false;
            }
            if(appendLogEntriesMsg.getPrevLogIndex()== lastSnapshotIndex && !Objects.equals( appendLogEntriesMsg.getPrevLogTerm(), lastSnapshotTerm ) ){
                log.info( "Refuse append log for leader {} because of lastSnapshotTerm unEqual", appendLogEntriesMsg.getLeaderId() );
                return false;
            }
            if(appendLogEntriesMsg.getPrevLogIndex()> lastSnapshotIndex && !Objects.equals( logs.get( idx(appendLogEntriesMsg.getPrevLogIndex()) ).getTerm(), appendLogEntriesMsg.getPrevLogTerm() )){
                log.info( "Refuse append log for leader {} because of lastLogTerm unEqual", appendLogEntriesMsg.getLeaderId() );
                return false;
            }
        }
        // 匹配日志
        if (appendLogEntriesMsg.getPrevLogIndex()<lastLogIndex.get()) {
            logs.subList( idx( appendLogEntriesMsg.getPrevLogIndex()), logs.size() ).clear();
        }

        logs.addAll( appendLogEntriesMsg.getEntries() );
        lastLogIndex.set( size() - 1 );
        lastLogTerm = logs.get( idx( lastLogIndex.get() )).getTerm();
        log.info( "Append log for leader {}", appendLogEntriesMsg.getLeaderId() );

        return true;

    }

    /**
     * 追加日志并返回一个 Future，在日志提交时完成
     */
    public CompletableFuture<Long> appendAndWaitCommit(LogEntry entry) {
        logLock.writeLock().lock();
        try {
            // 1. 追加日志到本地（Leader）
            long logIndex = appendLogEntry(entry);

            // 2. 创建与该日志索引关联的 Future
            CompletableFuture<Long> future = new CompletableFuture<>();
            commitListeners.put(logIndex, future);
            System.out.println("获得future");

            return future;
        }finally {
            logLock.writeLock().unlock();
        }

    }

    public Integer appendLogEntry(LogEntry logEntry) {
        logLock.writeLock().lock();
        try {
            if(role != RaftRole.LEADER){
                log.error("This node is not a leader.");
                return -1;
            }
            logEntry.setTerm( currentTerm );
            logEntry.setIndex( lastLogIndex.incrementAndGet());
            logs.add( logEntry );
            lastLogTerm = logEntry.getTerm();
            log.info( "append log start time: {}", new Date().getTime() );
            sendAppendLogMsg();
            return logEntry.getIndex();
        }finally {
            logLock.writeLock().unlock();
        }

    }

    public void updateLogIndex(AppendLogEntriesResMsg appendLogEntriesResMsg) {
        stateLock.writeLock().lock();
        try {
            if (appendLogEntriesResMsg.getPrevLogIndex() < lastSnapshotIndex ||
                    appendLogEntriesResMsg.getIsSuccess() ||
                    appendLogEntriesResMsg.getPrevLogIndex()== -1||
                    Objects.equals( logs.get( idx( appendLogEntriesResMsg.getPrevLogIndex()) ).getTerm(), appendLogEntriesResMsg.getPrevLogTerm() ) ) {
                nextIndex.put( appendLogEntriesResMsg.getNodeId(), appendLogEntriesResMsg.getPrevLogIndex() + 1 );
                matchIndex.put( appendLogEntriesResMsg.getNodeId(), appendLogEntriesResMsg.getPrevLogIndex() );
                refreshCommitIndex();
            } else {
                // 遍历找到第一个小于prevLogIndex的日志index
                for (int i = nextIndex.get( appendLogEntriesResMsg.getNodeId() )-1; i >= 0; i--) {
                    if( logs.get( idx( i ) ).getTerm() < appendLogEntriesResMsg.getPrevLogTerm()){
                        nextIndex.put( appendLogEntriesResMsg.getNodeId(), i + 1 );
                        break;
                    }
                }
            }
            log.info( "Update log nextIndex for client {} to {}", appendLogEntriesResMsg.getNodeId(), nextIndex.get( appendLogEntriesResMsg.getNodeId() ) );
        }finally {
            stateLock.writeLock().unlock();
        }

    }

    public void checkCommitIndex(int leaderCommitIndex){
        if(leaderCommitIndex > commitIndex){
            log.info("Update commit index to {}", leaderCommitIndex);
            commitIndex = leaderCommitIndex;
        }
        if(commitIndex > lastApplied){
            applyLogs();
        }
    }

    private void applyLogs(){
        applyLock.writeLock().lock();
        try{
            while(lastApplied < commitIndex){
                if(lastApplied + 1 >= size()){
                    log.info("Log is not enough to apply.");
                    break;
                }
                LogEntry logEntry = logs.get(idx( lastApplied + 1));
                //执行日志
                applyLogEntry( logEntry );
                lastApplied++;
                nodeInfoService.updateById( new NodeInfoPO(id,ip,port,lastApplied,logs.get( idx( lastApplied )).getTerm()) );
            }
        }finally {
            applyLock.writeLock().unlock();
        }
    }

    private void applyLogEntry(LogEntry logEntry){
        // 执行日志
        System.out.println(logEntry);
        switch ( logEntry.getCommand().getCommandType() ){
            case UPLOAD -> {
                if(role == RaftRole.LEADER){
                    // 通知所有等待该索引的 Future
                    CompletableFuture<Long> future = commitListeners.remove( (long) logEntry.getIndex() );
                    if (future != null) {
                        future.complete((long) logEntry.getIndex());
                    }
                }else{
                    // 拉取文件到本地
                    PullFileRequestMsg pullFileRequestMsg = new PullFileRequestMsg();
                    pullFileRequestMsg.setTerm( currentTerm );
                    pullFileRequestMsg.setNodeId( id );
                    BeanUtils.copyProperties( logEntry.getCommand(), pullFileRequestMsg );

                    sendMsgToNode( raftNodeMap.get( leaderId ), pullFileRequestMsg);
                }
            }
            case DELETE -> {
                // 文件的绝对路径
                String filePath ="/"+logEntry.getCommand().getUserId()+
                        "/"+logEntry.getCommand().getBucketId()+"/"+logEntry.getCommand().getFileName();

                try {
                    // 删除文件
                    Path path = Paths.get( RaftConfiguration.path + filePath );
                    if(Files.exists( path )){
                        Files.delete(path );
                        log.info( "delete file success" );
                    }else{
                        log.info( "file is already not in this node" );
                    }

                } catch (Exception e) {
                    log.error( "delete file failed, error {}",e.getMessage() );
                }
                if(role == RaftRole.LEADER){
                    // 通知等待的future
                    CompletableFuture<Boolean> future = deleteListeners.remove( filePath );
                    if (future != null) {
                        future.complete(true);
                    }
                }
            }
        }

    }

    private void sendMsgToNode(RaftClientNode raftClientNode, Message msg) {
        Channel channel = raftClientNode.getChannel();
        if ( channel != null && channel.isActive()) {
            if( msg instanceof PullFileResMsg temp ){
                log.info("Send to client file res msg {}: {}",raftClientNode.getId(),
                        "/"+temp.getBucketId()+"/"+temp.getBucketId()+"/"+temp.getFileName()+" file size: "+temp.getFileData().length);
            }else{
                log.info("Send to client {}: {}",raftClientNode.getId(), msg.toString() );
            }
            channel.writeAndFlush( msg );
        }else {
            channel = raftChannel.createClient( raftClientNode.getIp(), raftClientNode.getPort() );
            if(channel != null){
                raftClientNode.setChannel( channel );
                // 重新设置日志索引
                nextIndex.put( raftClientNode.getId(), lastLogIndex.get() == -1 ? 0 : lastLogIndex.get() );
                channel.writeAndFlush( msg );
            }else{
                clusterLogService.save( createClusterLogPO( LogType.ERROR.getCode(), "can't connect to client "+raftClientNode.getId() ) );
            }
        }
    }

    public void startVoteLoop() {
        // 随机选取一个时间作为选举超时时间
        electionSchedule.scheduleWithFixedDelay(electionTask(), 12, 20, TimeUnit.SECONDS);
    }

    public void resetElectionTimeout() {
        log.info("Reset election timeout.");
        electionSchedule.shutdownNow();
        electionSchedule = Executors.newSingleThreadScheduledExecutor();
        startVoteLoop();
    }

    /**
     * 心跳任务
     * @return Runnable
     */
    private Runnable heartbeatTask() {
        return () -> {
            HeartbeatMsg heartbeatMsg = new HeartbeatMsg();
            heartbeatMsg.setTerm( currentTerm );
            heartbeatMsg.setLeaderId( id );
            heartbeatMsg.setNodeId( id );
            heartbeatMsg.setCommitIndex( commitIndex );

            clusterLogService.save( createClusterLogPO( LogType.INFO.getCode(), " send heartbeat." ) );
            // 遍历map，发送心跳消息
            for ( Map.Entry<Integer, RaftClientNode> entry : raftNodeMap.entrySet() ) {
                sendMsgToNode( entry.getValue(), heartbeatMsg );
            }
        };
    }

    private Runnable electionTask () {
        return () -> {
            try {
                // 随机生成5-10秒的间隔
                int randomInterval = new Random().nextInt( RaftConfiguration.electionTimeoutMax - RaftConfiguration.electionTimeoutMin + 1 ) + RaftConfiguration.electionTimeoutMin;
                Thread.sleep(randomInterval);

                if(role != RaftRole.LEADER) {
                    // 执行任务内容
                    log.info("Timeout and Start vote.");
                    startElection();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
    }

    private Runnable logAppendTask () {
        return this::sendAppendLogMsg;
    }

    private void sendAppendLogMsg(){
        AppendLogEntriesMsg appendLogEntriesMsg = new AppendLogEntriesMsg();

        appendLogEntriesMsg.setLeaderId( id );
        appendLogEntriesMsg.setNodeId( id );
        appendLogEntriesMsg.setTerm( currentTerm );

        // 遍历map，发送日志消息
        for ( Map.Entry<Integer, Integer> entry : nextIndex.entrySet() ) {

            // 比较nextIndex和lastLogIndex
            if ( entry.getValue() <= lastLogIndex.get() ) {
                if(entry.getValue()< lastSnapshotIndex + 1){
                    appendLogEntriesMsg.setPrevLogIndex( RaftConstants.RELOAD_SNAPSHOT_INDEX );
                    sendMsgToNode( raftNodeMap.get( entry.getKey() ), appendLogEntriesMsg );
                    continue;
                }
                // 发送日志
                List<LogEntry> entries = new ArrayList<>();
                appendLogEntriesMsg.setPrevLogIndex( entry.getValue() - 1 );
                long prevLogTerm = idx(entry.getValue()) == 0 ? logs.get( 0 ).getTerm() : logs.get( idx( entry.getValue() - 1) ).getTerm();
                if(entry.getValue() == lastSnapshotIndex + 1){
                    prevLogTerm = lastSnapshotTerm;
                }

                appendLogEntriesMsg.setPrevLogTerm( prevLogTerm );
                for (int i = entry.getValue(); i <= lastLogIndex.get(); i++) {

                    entries.add( logs.get( idx( i ) ) );
                }
                appendLogEntriesMsg.setEntries( entries );
                sendMsgToNode( raftNodeMap.get( entry.getKey() ), appendLogEntriesMsg );
            }
        }
    }

    private void refreshCommitIndex() {
        // 遍历matchIndexMap，计算commitIndex
        int[] matchIndexArray = new int[matchIndex.size()];
        int i = 0;
        for ( Map.Entry<Integer, Integer> entry : matchIndex.entrySet() ) {
            matchIndexArray[i++] = entry.getValue();
        }
        // 排序
        sort( matchIndexArray );
        int n = matchIndexArray[RSCodingUtil.DATA_SHARDS-1];
        if ( n > commitIndex && Objects.equals( logs.get( idx( n ) ).getTerm(), currentTerm ) ) {
            commitIndex = n;
            log.info( "append end start time: {}", new Date().getTime() );
            log.info( "Update commit index to {}", commitIndex );
            applyLogs();
            if(commitIndex>1000 && commitIndex%1000 == 0){
                // 每100条日志生成快照
                generateSnapshot();
            }
        }else{
            log.info( "Not need to update commit index." );
        }
    }
    @AllArgsConstructor
    @Data
    static class FileRecode{
        private int existCnt;
        private int unExistCnt;
        private List<PullFileResMsg> fileList;
    }

    Map<String,FileRecode> fileRecodeMap = new ConcurrentHashMap<>();
    Map<String,ReentrantReadWriteLock> fileRecodeLockMap = new ConcurrentHashMap<>();


    /**
     * 恢复文件
     */
    public CompletableFuture<Boolean> recoverFileAndWait(PullFileRequestMsg fileMsg) {

        clusterLogService.save( createClusterLogPO( LogType.ERROR.getCode(), "notfound or incomplete user: "+fileMsg.getUserId()+" bucket: "+fileMsg.getBucketId()+" file: " + fileMsg.getFileName() ) );
        clusterLogService.save( createClusterLogPO( LogType.INFO.getCode(), "recover user: "+fileMsg.getUserId()+" bucket: "+fileMsg.getBucketId()+" file: " + fileMsg.getFileName() ) );
        // 恢复文件
        fileMsg.setTerm( currentTerm );
        fileMsg.setNodeId( id );
        String filePathKey = "/"+ fileMsg.getUserId() +"/"+ fileMsg.getBucketId() +"/"+ fileMsg.getFileName();
        if(recoveryListeners.get( filePathKey )!=null){
            log.info( "{} is recovering now",filePathKey );
            return recoveryListeners.get( filePathKey );
        }
        FileRecode fileRecode = new FileRecode( 0, 0 , new ArrayList<>() );
        Path path = Path.of(RaftConfiguration.path + filePathKey );
        try {
            if(Files.exists( path ) && Files.size( path )< fileMsg.getFileSize() ) {
                // 文件存在，但是是文件块
                fileRecode.setExistCnt( 1 );
                PullFileResMsg fileRecodeMsg = new PullFileResMsg();
                fileRecodeMsg.setFileData( Files.readAllBytes( path ) );
                fileRecode.getFileList().add( fileRecodeMsg );
            }else{
                // 文件不存在
                fileRecode.setUnExistCnt( 1 );
            }
            fileRecodeMap.put( filePathKey,  fileRecode);
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
        for ( Map.Entry<Integer, RaftClientNode> entry : raftNodeMap.entrySet() ) {
            if(entry.getKey() == id){
                continue;
            }
            sendMsgToNode( entry.getValue(), fileMsg );
        }
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        recoveryListeners.put( filePathKey, future );
        ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
        fileRecodeLockMap.put( filePathKey, reentrantReadWriteLock );
        return future;
    }


    /**
     * 删除文件
     */
    public CompletableFuture<Boolean> deleteFileAndWait( Command command ){
        LogEntry logEntry = new LogEntry();
        logEntry.setCommand( command );
        logEntry.setCommandValid( true );
        appendLogEntry( logEntry );
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        String filePathKey = "/"+ command.getUserId() +"/"+ command.getBucketId() +"/"+ command.getFileName();
        deleteListeners.put( filePathKey, future );
        return future;
    }

    public void recoverFile( PullFileResMsg fileMsg ){
        String filePathKey = "/"+ fileMsg.getUserId() +"/"+ fileMsg.getBucketId() +"/"+ fileMsg.getFileName();
        ReentrantReadWriteLock reentrantReadWriteLock = fileRecodeLockMap.get( filePathKey );
        if(reentrantReadWriteLock != null){
            reentrantReadWriteLock.writeLock().lock();
        }else{
            return;
        }
        try {
            FileRecode fileRecode = fileRecodeMap.get( filePathKey );
            if(fileRecode == null){
                log.info( "File recover end" );
                if(fileMsg.getFileData() == null || fileMsg.getFileData().length == 0){
                    PullFileResMsg msg = new PullFileResMsg();
                    BeanUtils.copyProperties(fileMsg, msg);
                    msg.setTerm( currentTerm );
                    msg.setNodeId( id );
                    Path path = Path.of(RaftConfiguration.path + filePathKey );
                    try {
                        msg.setFileData( RSCodingUtil.getBytesByIndex(  Files.readAllBytes( path ), fileMsg.nodeId -1) );
                        sendMsgToNode(raftNodeMap.get( fileMsg.nodeId ) , msg);
                    } catch ( IOException e ) {
                        log.error( "get file error, msg: "+e.getMessage() );
                    }
                }
                return;
            }
            fileRecode.getFileList().add( fileMsg );
            if(fileMsg.getFileData() != null && fileMsg.getFileData().length > 0) {
                fileRecode.setExistCnt( fileRecode.getExistCnt() + 1 );
            }else{
                fileRecode.setUnExistCnt( fileRecode.getUnExistCnt() + 1 );
            }
            if(fileRecode.existCnt >= RSCodingUtil.DATA_SHARDS ){
                // 恢复文件
                byte [][] fileData = new byte[RSCodingUtil.DATA_SHARDS][];
                int cnt =0;
                for ( PullFileResMsg pullFileResMsg : fileRecode.getFileList() ) {
                    if ( pullFileResMsg.getFileData() != null && pullFileResMsg.getFileData().length > 0 ) {
                        fileData[ cnt++ ] = pullFileResMsg.getFileData();
                        log.info("data shard length: {}", pullFileResMsg.getFileData().length );
                        if(cnt == RSCodingUtil.DATA_SHARDS){
                            break;
                        }
                    }
                }
                try {
                    byte [] recoverFileData = RSCodingUtil.getFileByteArr( fileData );
                    // 写入文件
                    Path path = Path.of(RaftConfiguration.path + filePathKey );

                    Files.write( path, recoverFileData );
                    log.info( "File recover success" );
                    clusterLogService.save( createClusterLogPO( LogType.INFO.getCode(), " recover user: "+fileMsg.getUserId()+" bucket: "+fileMsg.getBucketId()+" file: "+fileMsg.getFileName()+"  success" ) );
                    recoveryListeners.remove( filePathKey ).complete( true );
                    for ( PullFileResMsg pullFileResMsg : fileRecode.getFileList() ) {
                        if ( pullFileResMsg.getFileData() == null || pullFileResMsg.getFileData().length == 0 ) {
                            PullFileResMsg msg = new PullFileResMsg();
                            BeanUtils.copyProperties(pullFileResMsg, msg);
                            msg.setTerm( currentTerm );
                            msg.setNodeId( id );
                            msg.setFileData( RSCodingUtil.getBytesByIndex(  Files.readAllBytes( path ), pullFileResMsg.nodeId -1) );
                            sendMsgToNode(raftNodeMap.get( pullFileResMsg.nodeId ) , msg);
                        }
                    }
                } catch ( Exception e ) {
                    log.error( "File recover failed, msg: "+e.getMessage() );
                    clusterLogService.save( createClusterLogPO( LogType.ERROR.getCode(), " recover user: "+fileMsg.getUserId()+" bucket: "+fileMsg.getBucketId()+" file: "+fileMsg.getFileName()+"  failed" ) );
                    recoveryListeners.remove( filePathKey ).complete( false );
                }finally {
                    fileRecodeMap.remove( filePathKey );
                    fileRecodeLockMap.remove( filePathKey );
                }
            } else if ( fileRecode.unExistCnt > RSCodingUtil.PARITY_SHARDS ) {
                fileRecodeMap.remove( filePathKey );
                // 恢复失败
                log.info( "File recover failed because less shards" );
                clusterLogService.save( createClusterLogPO( LogType.ERROR.getCode(), " recover user: "+fileMsg.getUserId()+" bucket: "+fileMsg.getBucketId()+" file: "+fileMsg.getFileName()+"  failed" ) );
                recoveryListeners.remove( filePathKey ).complete( false );
            }
        }finally {
            reentrantReadWriteLock.writeLock().unlock();
        }

    }

    /**
     * 生成快照
     */
    private void generateSnapshot(){
        // 生成快照
        int temp = idx(commitIndex);
        lastSnapshotTerm = logs.get( idx(commitIndex) ).getTerm();

        lastSnapshotIndex = commitIndex;
        // 清除快照之前的日志
        logs.subList( 0, temp + 1 ).clear();

    }

    public void initStorageFiles(){
        if(!initStorage){
            storageLock.writeLock().lock();
            try {
                if(!initStorage){
                    clusterLogService.save( createClusterLogPO( LogType.INFO.getCode(), "start init file storage") );
                    QueryWrapper<FilePO> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq( "is_del",0 );
                    List<FilePO> allFiles = fileService.list(queryWrapper);
                    Set<String> dbFileMap = buildDbFileMap(allFiles);
                    Set<String> actualFilePaths = getActualFilePaths();

                    deleteOrphanedFiles(actualFilePaths, dbFileMap);
                    reportMissingFiles(allFiles, actualFilePaths);
                    initStorage = true;
                    clusterLogService.save( createClusterLogPO( LogType.INFO.getCode(), "init file storage end") );
                }
            }catch ( Exception e ){
                clusterLogService.save( createClusterLogPO( LogType.ERROR.getCode(), "init file storage failed, error msg: "+e.getMessage()) );
            }
            finally {
                storageLock.writeLock().unlock();
            }

        }
    }

    private Set<String> buildDbFileMap(List<FilePO> files) {
        Set<String> set = new HashSet<>();
        for (FilePO file : files) {
            String realPath = "/"+file.getUserId()+"/"+file.getBucketId()+"/"+file.getName();
            Path absolutePath = Paths.get(RaftConfiguration.path, realPath).normalize();
            set.add(absolutePath.toString());
        }
        return set;
    }

    private Set<String> getActualFilePaths() {
        Set<String> paths = new HashSet<>();
        try ( Stream<Path> stream = Files.walk(Paths.get(RaftConfiguration.path))) {
            stream.filter(Files::isRegularFile)
                    .map(path -> path.normalize().toString())
                    .forEach(paths::add);
        } catch (IOException e) {
            log.error("遍历文件失败：", e);
        }
        return paths;
    }

    private void deleteOrphanedFiles(Set<String> actualPaths, Set<String> dbMap) {
        actualPaths.stream()
                .filter(path -> !dbMap.contains(path))
                .forEach(this::deleteFile);
    }
    private void deleteFile(String path) {
        try {
            Files.delete(Path.of(path));
            log.info("已删除文件：{}", path);
        } catch (IOException e) {
            log.error("删除文件失败：{}", path, e);
        }
    }

    private void reportMissingFiles(List<FilePO> dbFiles, Set<String> actualPaths) {
        // 创建包含路径与实体映射的临时Map（用于快速查找）
        Map<String, FilePO> validFileMap = dbFiles.stream()
                .collect(Collectors.toMap(
                        file -> Paths.get(RaftConfiguration.path,
                                "/"+file.getUserId()+"/"+file.getBucketId()+"/"+file.getName()
                        ).normalize().toString(),
                        Function.identity()
                ));

        // 通过比对找出缺失的实体
        List<FilePO> missingEntities = validFileMap.entrySet().stream()
                .filter(entry -> !actualPaths.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .toList();

        if (!missingEntities.isEmpty()) {
            missingEntities.forEach( filePO -> {
                // 拉取文件到本地
                PullFileRequestMsg pullFileRequestMsg = new PullFileRequestMsg();
                pullFileRequestMsg.setTerm( currentTerm );
                pullFileRequestMsg.setNodeId( id );
                pullFileRequestMsg.setUserId( filePO.getUserId() );
                pullFileRequestMsg.setBucketId( filePO.getBucketId() );
                pullFileRequestMsg.setFileName( filePO.getName() );
                pullFileRequestMsg.setFileMd5( filePO.getMd5() );
                pullFileRequestMsg.setFileSize( filePO.getFileSize());
                sendMsgToNode( raftNodeMap.get( leaderId ), pullFileRequestMsg);
            } );
        }

    }

    private int idx(int logicIndex){
        if(logicIndex <= lastSnapshotIndex || logicIndex > size() - 1){
            return -1;
        }
        return logicIndex - lastSnapshotIndex -1;
    }

    private int size(){
        return logs.size() + lastSnapshotIndex +1;
    }


    private ClusterLogPO createClusterLogPO(int type, String msg) {
        ClusterLogPO clusterLog = new ClusterLogPO();
        clusterLog.setType( type );
        clusterLog.setLogTime( LocalDateTime.now() );
        clusterLog.setNodeId( id );
        clusterLog.setMsg( msg );
        return clusterLog;
    }


}

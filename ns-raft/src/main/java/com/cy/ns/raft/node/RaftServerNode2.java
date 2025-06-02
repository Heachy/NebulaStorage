//package com.cy.ns.raft.node;
//
//import static java.util.Arrays.sort;
//
//import com.cy.ns.common.constants.RaftConstants;
//import com.cy.ns.raft.config.NacosConfiguration;
//import com.cy.ns.raft.config.RaftConfiguration;
//import com.cy.ns.raft.entity.po.NodeInfoPO;
//import com.cy.ns.raft.enums.RaftRole;
//import com.cy.ns.raft.log.LogEntry;
//import com.cy.ns.raft.netty.channel.RaftChannel;
//import com.cy.ns.raft.netty.msg.AppendLogEntriesMsg;
//import com.cy.ns.raft.netty.msg.AppendLogEntriesResMsg;
//import com.cy.ns.raft.netty.msg.HeartbeatMsg;
//import com.cy.ns.raft.netty.msg.Message;
//import com.cy.ns.raft.netty.msg.RequestVoteMsg;
//import com.cy.ns.raft.service.NodeInfoService;
//import io.netty.channel.Channel;
//import jakarta.annotation.PreDestroy;
//import jakarta.annotation.Resource;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//import java.util.Random;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.stereotype.Component;
//
///**
// * @author Haechi
// * @date 2025/3/16
// */
//@Data
//@Component
//@Slf4j
//public class RaftServerNode {
//
//    /**
//     * 本节点的ip
//     */
//    @Value("${raft.server.ip:localhost}")
//    private String ip;
//    /**
//     * 端口
//     */
//    @Value("${raft.server.port:8081}")
//    private int port;
//    /**
//     * id
//     */
//    @Value("${raft.server.id:1}")
//    private int id;
//    /**
//     * 任期
//     */
//    private Long currentTerm;
//
//     /**
//     * 投票给谁
//     */
//    private int voteFor;
//
//    /**
//     * 角色
//     */
//    private volatile RaftRole role;
//
//    /**
//     * 服务端通道
//     */
//    private Channel channel;
//
//    private int leaderId;
//
//    /**
//     * 提交索引
//     */
//    private int commitIndex;
//
//    /**
//     * 应用索引 当前日志执行到哪， 日志先提交才能执行
//     */
//    private int lastApplied;
//
//    /**
//     * 下一个索引
//     */
//    private Map<Integer,Integer> nextIndex;
//
//    /**
//     * 匹配索引
//     */
//    private Map<Integer,Integer> matchIndex;
//
//    /**
//     * 日志列表
//     */
//    private volatile List<LogEntry> logs;
//
//
//    /**
//     * 最后一个日志索引和任期
//     */
//    private int lastLogIndex;
//    private Long lastLogTerm;
//
//    /**
//     * 最后一个快照索引和任期
//     */
//    private int lastSnapshotIndex;
//    private  Long lastSnapshotTerm;
//
//    public static Map<Integer, RaftClientNode> raftNodeMap;
//
//    /**
//     * 用于跟踪日志提交的监听器
//     */
//    private volatile Map<Long, CompletableFuture<Long>> commitListeners = new ConcurrentHashMap<>();
//
//    private ScheduledExecutorService serverSchedule = Executors.newSingleThreadScheduledExecutor();
//
//    private ScheduledExecutorService electionSchedule = Executors.newSingleThreadScheduledExecutor();
//
//    private ScheduledExecutorService logAppendSchedule = Executors.newSingleThreadScheduledExecutor();
//
//
//
//    @Resource
//    @Lazy
//    private RaftChannel raftChannel;
//
//    @Resource
//    private NodeInfoService nodeInfoService;
//
//    @Resource
//    private NacosConfiguration nacosConfiguration;
//
//
//    /**
//     * 选举结果集
//     */
//    private Map<Integer, Boolean> voteResultMap;
//
//
//    /**
//     * 选举票数
//     */
//    private int voteCount;
//
//    public void start() {
//        // 初始化
//        channel = raftChannel.createServer( port );
//        raftNodeMap = new HashMap<>();
//        voteResultMap = new HashMap<>();
//        nextIndex = new HashMap<>();
//        matchIndex = new HashMap<>();
//        logs = new ArrayList<>();
//
//        initLog(id);
//
//        // 建立其他节点的连接
//        RaftConfiguration.raftClientNodes.forEach( raftClientNode -> {
//           raftClientNode.setChannel(raftChannel.createClient( raftClientNode.getIp(), raftClientNode.getPort() ));
//           raftNodeMap.put( raftClientNode.getId(), raftClientNode );
//        } );
//
//        log.info( "Node {} start success.", id );
//    }
//
//    @PreDestroy
//    public void stop() {
//        if ( channel != null ) {
//            channel.close();
//        }
//        serverSchedule.shutdownNow();
//        RaftConfiguration.raftClientNodes.forEach( raftClientNode -> {
//            if(raftClientNode.getChannel() != null){
//                raftClientNode.getChannel().close();
//            }
//        } );
//        log.info( "Node {} stop success.", id );
//    }
//
//    private void initLog(int logNodeId){
//        // 从数据库中获取日志
//        NodeInfoPO nodeInfoPO = nodeInfoService.getById( logNodeId );
//        System.out.println(nodeInfoPO);
//
//        commitIndex = nodeInfoPO.getApplyIndex();
//        currentTerm = nodeInfoPO.getApplyTerm();
//        lastLogIndex = nodeInfoPO.getApplyIndex();
//        lastLogTerm = nodeInfoPO.getApplyTerm();
//        lastApplied = nodeInfoPO.getApplyIndex();
//        lastSnapshotIndex = nodeInfoPO.getApplyIndex();
//        lastSnapshotTerm = nodeInfoPO.getApplyTerm();
//    }
//
//    /**
//     * 开始选举
//     */
//    public void startElection() {
//        log.info( "Node {} start election.", id );
//        // 遍历所有节点，发送投票请求
//        // 选举超时时间内没有收到大多数节点的投票，重新开始选举
//        // 收到大多数节点的投票，成为leader
//        becomeCandidate();
//        serverSchedule.scheduleWithFixedDelay( voteTask(), 0, 5, TimeUnit.SECONDS );
//    }
//
//
//    /**
//     * 投票任务
//     * @return Runnable
//     */
//    private Runnable voteTask() {
//        return () -> {
//            RequestVoteMsg requestVoteMsg = new RequestVoteMsg();
//            requestVoteMsg.setTerm( currentTerm );
//            requestVoteMsg.setCandidateId( id );
//            requestVoteMsg.setLastLogTerm( lastLogTerm );
//            requestVoteMsg.setLastLogIndex( lastLogIndex );
//            // 遍历map，发送消息
//            for ( Map.Entry<Integer, RaftClientNode> entry : raftNodeMap.entrySet() ) {
//                // 如果已经投过票，不再发送
//                if(voteResultMap.get( entry.getValue().getId())!=null){
//                    continue;
//                }
//                sendMsgToNode( entry.getValue(), requestVoteMsg);
//            }
//        };
//    }
//
//
//    /**
//     * 增加选票
//     */
//    private void increaseVoteCount() {
//        voteCount++;
//        if ( voteCount > (raftNodeMap.size()+1) / 2 ) {
//            becomeLeader();
//        }
//    }
//
//
//    /**
//     * 成为候选人
//     */
//    public void becomeCandidate() {
//        if(role == RaftRole.LEADER) {
//            log.error( "This node is already a leader." );
//            return;
//        }
//
//        // 清空选举结果
//        serverSchedule.shutdownNow();
//        serverSchedule = Executors.newSingleThreadScheduledExecutor();
//        voteResultMap.clear();
//        voteCount = 1;
//        leaderId = -1;
//
//        // 变为候选者： 任期增加，身份改变，为自己投票
//        currentTerm++;
//        role = RaftRole.CANDIDATE;
//        voteFor = id;
//        log.info( "Node {} become candidate, term is {}", id, currentTerm);
//        nacosConfiguration.stop();
//    }
//    /**
//     * 成为领导者
//     */
//    public void becomeLeader() {
//        if ( role != RaftRole.CANDIDATE ) {
//            log.error( "This node is not a candidate." );
//            return;
//        }
//        role = RaftRole.LEADER;
//        log.info( "Node {} become leader, term is {}", id, currentTerm );
//
//        // 设置nextIndex和matchIndex
//        for ( Map.Entry<Integer, RaftClientNode> entry : raftNodeMap.entrySet() ) {
//            nextIndex.put( entry.getKey(), lastLogIndex + 1 );
//            matchIndex.put( entry.getKey(), -1 );
//        }
//
//        // 停止选举任务，开始发送心跳
//        serverSchedule.shutdownNow();
//        serverSchedule = Executors.newSingleThreadScheduledExecutor();
//        serverSchedule.scheduleAtFixedRate( heartbeatTask(), 0, 10, TimeUnit.SECONDS );
//        logAppendSchedule = Executors.newSingleThreadScheduledExecutor();
//        logAppendSchedule.scheduleAtFixedRate( logAppendTask(), 0, 10, TimeUnit.SECONDS );
//        nacosConfiguration.start();
//    }
//
//
//
//    /**
//     * 成为追随者
//     */
//    public void becomeFollower(Long term, int candidateId) {
//        if ( term < currentTerm ) {
//            log.error( "The term is less than the current term." );
//            return;
//        }
//        role = RaftRole.FOLLOWER;
//        log.info( "Node {} become follower, term is {}", id, term );
//
//        currentTerm = term;
//        voteFor = candidateId;
//        serverSchedule.shutdownNow();
//        logAppendSchedule.shutdownNow();
//        nacosConfiguration.stop();
//
//    }
//
//    /**
//     * 处理投票响应
//     * @param voteGranted 是否投票
//     * @param term 任期
//     */
//    public void solveVoteResponse(Boolean voteGranted, Long term) {
//        // 如果已经是leader或者follower，不做处理
//        if ( role == RaftRole.LEADER || role == RaftRole.FOLLOWER) {
//            return;
//        }
//        if ( voteGranted ) {
//            log.info( "get one vote");
//            voteResultMap.put( id, true );
//            increaseVoteCount();
//        } else {
//            log.info( "Node {} refuse vote for me, term is {}", id, term );
//            voteResultMap.put( id, false );
//        }
//    }
//
//    public Boolean appendLogEntries(AppendLogEntriesMsg appendLogEntriesMsg) {
//        if(appendLogEntriesMsg.getPrevLogIndex() == RaftConstants.RELOAD_SNAPSHOT_INDEX ){
//            // 重新读取快照
//            initLog( appendLogEntriesMsg.getLeaderId() );
//            log.info( "Reload Snap" );
//            return true;
//        }
//        // 如果日志不匹配，拒绝
//        if (appendLogEntriesMsg.getPrevLogIndex()!=-1){
//            if(appendLogEntriesMsg.getPrevLogIndex() > lastLogIndex ||
//            appendLogEntriesMsg.getPrevLogIndex()< lastSnapshotIndex ) {
//                log.info( "Refuse append log for leader {} because of prevLogIndex less than lastSnapshotIndex", appendLogEntriesMsg.getLeaderId() );
//                return false;
//            }
//            if(appendLogEntriesMsg.getPrevLogIndex()== lastSnapshotIndex && !Objects.equals( appendLogEntriesMsg.getPrevLogTerm(), lastSnapshotTerm ) ){
//                log.info( "Refuse append log for leader {} because of lastSnapshotTerm unEqual", appendLogEntriesMsg.getLeaderId() );
//                return false;
//            }
//            if(appendLogEntriesMsg.getPrevLogIndex()> lastSnapshotIndex && !Objects.equals( logs.get( idx(appendLogEntriesMsg.getPrevLogIndex()) ).getTerm(), appendLogEntriesMsg.getPrevLogTerm() )){
//                log.info( "Refuse append log for leader {} because of lastLogTerm unEqual", appendLogEntriesMsg.getLeaderId() );
//                return false;
//            }
//        }
//        // 匹配日志
//        if (appendLogEntriesMsg.getPrevLogIndex()<lastLogIndex) {
//            logs.subList( idx( appendLogEntriesMsg.getPrevLogIndex()), logs.size() ).clear();
//        }
//
//        logs.addAll( appendLogEntriesMsg.getEntries() );
//        lastLogIndex = size() - 1;
//        lastLogTerm = logs.get( idx( lastLogIndex )).getTerm();
//        log.info( "Append log for leader {}", appendLogEntriesMsg.getLeaderId() );
//
//        return true;
//
//    }
//
//    /**
//     * 追加日志并返回一个 Future，在日志提交时完成
//     */
//    public CompletableFuture<Long> appendAndWaitCommit(LogEntry entry) {
//        // 1. 追加日志到本地（Leader）
//        long logIndex = appendLogEntry(entry);
//
//        // 2. 创建与该日志索引关联的 Future
//        CompletableFuture<Long> future = new CompletableFuture<>();
//        commitListeners.put(logIndex, future);
//        System.out.println("获得future");
//
//        return future;
//    }
//
//    public Integer appendLogEntry(LogEntry logEntry) {
//        if(role != RaftRole.LEADER){
//            log.error("This node is not a leader.");
//            return -1;
//        }
//        logEntry.setTerm( currentTerm );
//        logEntry.setIndex( lastLogIndex + 1 );
//        logs.add( logEntry );
//        lastLogIndex++;
//        lastLogTerm = logEntry.getTerm();
//        sendAppendLogMsg();
//        return logEntry.getIndex();
//    }
//
//    public void updateLogIndex(AppendLogEntriesResMsg appendLogEntriesResMsg) {
//        if (appendLogEntriesResMsg.getPrevLogIndex() < lastSnapshotIndex || appendLogEntriesResMsg.getIsSuccess() || appendLogEntriesResMsg.getPrevLogIndex()== -1|| Objects.equals( logs.get( idx( appendLogEntriesResMsg.getPrevLogIndex()) ).getTerm(), appendLogEntriesResMsg.getPrevLogTerm() ) ) {
//            nextIndex.put( appendLogEntriesResMsg.getNodeId(), appendLogEntriesResMsg.getPrevLogIndex() + 1 );
//            matchIndex.put( appendLogEntriesResMsg.getNodeId(), appendLogEntriesResMsg.getPrevLogIndex() );
//            refreshCommitIndex();
//        } else {
//            // 遍历找到第一个小于prevLogIndex的日志index
//            for (int i = nextIndex.get( appendLogEntriesResMsg.getNodeId() )-1; i >= 0; i--) {
//                if( logs.get( idx( i ) ).getTerm() < appendLogEntriesResMsg.getPrevLogTerm()){
//                    nextIndex.put( appendLogEntriesResMsg.getNodeId(), i + 1 );
//                    break;
//                }
//            }
//        }
//        log.info( "Update log nextIndex for client {} to {}", appendLogEntriesResMsg.getNodeId(), nextIndex.get( appendLogEntriesResMsg.getNodeId() ) );
//    }
//
//    public void checkCommitIndex(int leaderCommitIndex){
//        if(leaderCommitIndex > commitIndex){
//            log.info("Update commit index to {}", leaderCommitIndex);
//            commitIndex = leaderCommitIndex;
//        }
//        if(commitIndex > lastApplied){
//            applyLogs();
//        }
//    }
//
//    private void applyLogs(){
//        while(lastApplied < commitIndex){
//            if(lastApplied + 1 >= size()){
//                log.info("Log is not enough to apply.");
//                break;
//            }
//            LogEntry logEntry = logs.get(idx( lastApplied + 1));
//            //执行日志
//            applyLogEntry( logEntry );
//            lastApplied++;
//            System.out.println("lastApplied: " + lastApplied);
//            System.out.println("commitIndex: " + commitIndex);
//            nodeInfoService.updateById( new NodeInfoPO(id,ip,port,lastApplied,logs.get( idx( lastApplied )).getTerm()) );
//        }
//    }
//
//    private void applyLogEntry(LogEntry logEntry){
//        // 执行日志
//        System.out.println(logEntry);
//        if(role == RaftRole.LEADER){
//            // 通知所有等待该索引的 Future
//            CompletableFuture<Long> future = commitListeners.remove( (long) logEntry.getIndex() );
//            if (future != null) {
//                future.complete((long) logEntry.getIndex());
//            }
//        }
//    }
//
//    private void sendMsgToNode(RaftClientNode raftClientNode, Message msg) {
//        Channel channel = raftClientNode.getChannel();
//        if ( channel != null && channel.isActive()) {
//            log.info("Send to client {}: {}",raftClientNode.getId(), msg.toString() );
//            channel.writeAndFlush( msg );
//        }else {
//            channel = raftChannel.createClient( raftClientNode.getIp(), raftClientNode.getPort() );
//            if(channel != null){
//                raftClientNode.setChannel( channel );
//                // 重新设置日志索引
//                nextIndex.put( raftClientNode.getId(), lastLogIndex == -1 ? 0 : lastLogIndex );
//                channel.writeAndFlush( msg );
//            }
//        }
//    }
//
//    public void startVoteLoop() {
//        // 随机选取一个时间作为选举超时时间
//        electionSchedule.scheduleWithFixedDelay(electionTask(), 12, 20, TimeUnit.SECONDS);
//    }
//
//    public void resetElectionTimeout() {
//        log.info("Reset election timeout.");
//        electionSchedule.shutdownNow();
//        electionSchedule = Executors.newSingleThreadScheduledExecutor();
//        startVoteLoop();
//    }
//
//    /**
//     * 心跳任务
//     * @return Runnable
//     */
//    private Runnable heartbeatTask() {
//        return () -> {
//            HeartbeatMsg heartbeatMsg = new HeartbeatMsg();
//            heartbeatMsg.setTerm( currentTerm );
//            heartbeatMsg.setLeaderId( id );
//            heartbeatMsg.setNodeId( id );
//            heartbeatMsg.setCommitIndex( commitIndex );
//
//            // 遍历map，发送心跳消息
//            for ( Map.Entry<Integer, RaftClientNode> entry : raftNodeMap.entrySet() ) {
//                sendMsgToNode( entry.getValue(), heartbeatMsg );
//            }
//        };
//    }
//
//    private Runnable electionTask () {
//        return () -> {
//            try {
//                // 随机生成5-10秒的间隔
//                int randomInterval = new Random().nextInt( RaftConfiguration.electionTimeoutMax - RaftConfiguration.electionTimeoutMin + 1 ) + RaftConfiguration.electionTimeoutMin;
//                Thread.sleep(randomInterval);
//
//                if(role != RaftRole.LEADER) {
//                    // 执行任务内容
//                    log.info("Timeout and Start vote.");
//                    startElection();
//                }
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//        };
//    }
//
//    private Runnable logAppendTask () {
//        return this::sendAppendLogMsg;
//    }
//
//    private void sendAppendLogMsg(){
//        AppendLogEntriesMsg appendLogEntriesMsg = new AppendLogEntriesMsg();
//
//        appendLogEntriesMsg.setLeaderId( id );
//        appendLogEntriesMsg.setNodeId( id );
//        appendLogEntriesMsg.setTerm( currentTerm );
//
//        // 遍历map，发送日志消息
//        for ( Map.Entry<Integer, Integer> entry : nextIndex.entrySet() ) {
//
//            // 比较nextIndex和lastLogIndex
//            if ( entry.getValue() <= lastLogIndex ) {
//                if(entry.getValue()< lastSnapshotIndex + 1){
//                    appendLogEntriesMsg.setPrevLogIndex( RaftConstants.RELOAD_SNAPSHOT_INDEX );
//                    sendMsgToNode( raftNodeMap.get( entry.getKey() ), appendLogEntriesMsg );
//                    continue;
//                }
//                // 发送日志
//                List<LogEntry> entries = new ArrayList<>();
//                appendLogEntriesMsg.setPrevLogIndex( entry.getValue() - 1 );
//                long prevLogTerm = idx(entry.getValue()) == 0 ? logs.get( 0 ).getTerm() : logs.get( idx( entry.getValue() - 1) ).getTerm();
//                if(entry.getValue() == lastSnapshotIndex + 1){
//                    prevLogTerm = lastSnapshotTerm;
//                }
//
//                appendLogEntriesMsg.setPrevLogTerm( prevLogTerm );
//                for (int i = entry.getValue(); i <= lastLogIndex; i++) {
//                    entries.add( logs.get( idx( i ) ) );
//                }
//                appendLogEntriesMsg.setEntries( entries );
//                sendMsgToNode( raftNodeMap.get( entry.getKey() ), appendLogEntriesMsg );
//            }
//        }
//    }
//
//    private void refreshCommitIndex() {
//        // 遍历matchIndexMap，计算commitIndex
//        int[] matchIndexArray = new int[matchIndex.size()];
//        int i = 0;
//        for ( Map.Entry<Integer, Integer> entry : matchIndex.entrySet() ) {
//            matchIndexArray[i++] = entry.getValue();
//        }
//        // 排序
//        sort( matchIndexArray );
//        // TODO 换为纠删码可备份数量
//        int n = matchIndexArray[2];
//        if ( n > commitIndex && Objects.equals( logs.get( idx( n ) ).getTerm(), currentTerm ) ) {
//            commitIndex = n;
//            log.info( "Update commit index to {}", commitIndex );
//            applyLogs();
//            if(commitIndex>10 && commitIndex%10 == 0){
//                // 每100条日志生成快照
//                generateSnapshot();
//            }
//        }else{
//            log.info( "Not need to update commit index." );
//        }
//    }
//
//    /**
//     * 生成快照
//     */
//    private void generateSnapshot(){
//        // 生成快照
//        lastSnapshotTerm = logs.get( idx(commitIndex) ).getTerm();
//
//        lastSnapshotIndex = commitIndex;
//
//
//
//    }
//
//    private int idx(int logicIndex){
//        if(logicIndex <= lastSnapshotIndex || logicIndex > size() - 1){
//            return -1;
//        }
//        return logicIndex - lastSnapshotIndex -1;
//    }
//
//    private int size(){
//        return logs.size() + lastSnapshotIndex +1;
//    }
//
//}

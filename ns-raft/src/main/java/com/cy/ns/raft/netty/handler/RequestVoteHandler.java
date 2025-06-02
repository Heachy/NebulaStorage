package com.cy.ns.raft.netty.handler;

import com.cy.ns.raft.enums.RaftRole;
import com.cy.ns.raft.netty.msg.HeartbeatMsg;
import com.cy.ns.raft.netty.msg.RequestVoteMsg;
import com.cy.ns.raft.netty.msg.RequestVoteResponseMsg;
import com.cy.ns.raft.node.RaftServerNode;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import jakarta.annotation.Resource;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Haechi
 * @date 2025/3/22
 */
@ChannelHandler.Sharable
@Component
@Slf4j
@AllArgsConstructor
public class RequestVoteHandler  extends SimpleChannelInboundHandler<RequestVoteMsg> {


    @Resource
    RaftServerNode raftServerNode;

    @Override
    protected void channelRead0( ChannelHandlerContext channelHandlerContext, RequestVoteMsg requestVoteMsg ) throws Exception {
        RequestVoteResponseMsg responseMsg = new RequestVoteResponseMsg( false );
        responseMsg.setNodeId( raftServerNode.getId() );
        responseMsg.setTerm(raftServerNode.getCurrentTerm());
        if (requestVoteMsg.getTerm() < raftServerNode.getCurrentTerm()) {
            log.info("Refuse vote for candidate {} because of term {} < {}", requestVoteMsg.getCandidateId(), requestVoteMsg.getTerm(), raftServerNode.getCurrentTerm());
        }else if (requestVoteMsg.getTerm() > raftServerNode.getCurrentTerm()) {
            if(isNewer( requestVoteMsg )){
                raftServerNode.becomeFollower( requestVoteMsg.getTerm(), requestVoteMsg.getCandidateId() );
                responseMsg.setVoteGranted( true );
                log.info( "Vote for candidate {}", requestVoteMsg.getCandidateId() );
            }else{
                log.info( "Refuse vote for candidate {} because of log", requestVoteMsg.getCandidateId() );
            }

        }else {
            if(raftServerNode.getVoteFor() == requestVoteMsg.getCandidateId()) {
                responseMsg.setVoteGranted( true );
                raftServerNode.setVoteFor( requestVoteMsg.getCandidateId() );
                log.info( "Already vote for candidate" );
            }else{
                log.info( "Refuse vote for candidate {} because of already vote for {}", requestVoteMsg.getCandidateId(), raftServerNode.getVoteFor() );
            }
        }
        channelHandlerContext.writeAndFlush(responseMsg);
    }

    /**
     * 判断候选者的日志是否比自己新
     */
    public Boolean isNewer( RequestVoteMsg requestVoteMsg ) {
        log.info( "Compare last log, me: index {}; term {}, candidate: index {}; term {}", raftServerNode.getLastLogIndex(), raftServerNode.getLastLogTerm(), requestVoteMsg.getLastLogIndex(), requestVoteMsg.getLastLogTerm() );
        if( !Objects.equals( requestVoteMsg.getLastLogTerm(), raftServerNode.getLastLogTerm() ) ){
            return requestVoteMsg.getLastLogTerm()>raftServerNode.getLastLogTerm();
        }

        return requestVoteMsg.getLastLogIndex()>=raftServerNode.getLastLogIndex().get();
    }

}
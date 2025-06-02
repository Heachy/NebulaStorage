package com.cy.ns.raft.netty.handler;

import com.cy.ns.raft.netty.msg.HeartbeatMsg;
import com.cy.ns.raft.netty.msg.Message;
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
public class RequestVoteResHandler  extends SimpleChannelInboundHandler<RequestVoteResponseMsg> {


    RaftServerNode raftServerNode;


    @Override
    protected void channelRead0( ChannelHandlerContext channelHandlerContext, RequestVoteResponseMsg requestVoteResponseMsg ) throws Exception {
        if ( Objects.equals( requestVoteResponseMsg.getTerm(), raftServerNode.getCurrentTerm() ) ) {
            log.info( "Receive vote response from client {} with term {} and voteGranted {}", requestVoteResponseMsg.getNodeId(), requestVoteResponseMsg.getTerm(), requestVoteResponseMsg.getVoteGranted() );
            raftServerNode.solveVoteResponse(requestVoteResponseMsg.getVoteGranted(),requestVoteResponseMsg.getTerm());
        }else if ( requestVoteResponseMsg.getTerm() > raftServerNode.getCurrentTerm() ) {
            log.info( "error term" );
            raftServerNode.becomeFollower( requestVoteResponseMsg.getTerm(), -1 );
        }
    }

}
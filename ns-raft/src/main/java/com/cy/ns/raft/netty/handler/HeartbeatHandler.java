package com.cy.ns.raft.netty.handler;

import com.cy.ns.raft.netty.msg.HeartbeatMsg;
import com.cy.ns.raft.node.RaftServerNode;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Haechi
 * @date 2025/3/16
 */
@ChannelHandler.Sharable
@Component
@Slf4j
public class HeartbeatHandler extends SimpleChannelInboundHandler<HeartbeatMsg> {

    @Resource
    RaftServerNode raftServerNode;

    @Override
    protected void channelRead0( ChannelHandlerContext channelHandlerContext, HeartbeatMsg heartbeatMsg ) throws Exception {
        if(raftServerNode.getCurrentTerm() > heartbeatMsg.getTerm()) {
            log.info("Refuse heartbeat from server {} because of term {} > {}", heartbeatMsg.getNodeId(), heartbeatMsg.getTerm(), raftServerNode.getCurrentTerm());
            return;
        }
        if(raftServerNode.getCurrentTerm() < heartbeatMsg.getTerm() || raftServerNode.getLeaderId() != heartbeatMsg.getLeaderId()) {
            log.info("Receive heartbeat from server {} with term {}", heartbeatMsg.getNodeId(), heartbeatMsg.getTerm());
            raftServerNode.becomeFollower(heartbeatMsg.getTerm(), -1);
            raftServerNode.setLeaderId( heartbeatMsg.getLeaderId() );
        }
        raftServerNode.resetElectionTimeout();
        raftServerNode.checkCommitIndex( heartbeatMsg.getCommitIndex() );
        raftServerNode.initStorageFiles();
    }

}

package com.cy.ns.raft.netty.handler;

import com.cy.ns.raft.netty.msg.AppendLogEntriesResMsg;
import com.cy.ns.raft.node.RaftServerNode;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Haechi
 * @date 2025/3/29
 */
@ChannelHandler.Sharable
@Component
@Slf4j
public class AppendLogEntriesResHandler extends SimpleChannelInboundHandler<AppendLogEntriesResMsg> {

    @Resource
    RaftServerNode raftServerNode;

    @Override
    protected void channelRead0( ChannelHandlerContext channelHandlerContext, AppendLogEntriesResMsg appendLogEntriesResMsg ) throws Exception {

        if(appendLogEntriesResMsg.getTerm() > raftServerNode.getCurrentTerm()) {
            log.info("Refuse append log entries response from server {} because of term {} > {}", appendLogEntriesResMsg.getLeaderId(), appendLogEntriesResMsg.getTerm(), raftServerNode.getCurrentTerm());
            raftServerNode.becomeFollower(appendLogEntriesResMsg.getTerm(), -1);
            raftServerNode.setLeaderId( appendLogEntriesResMsg.getLeaderId() );
            raftServerNode.resetElectionTimeout();
        }else{
            log.info("Receive append log entries response from server {} with term {} and isSuccess {}", appendLogEntriesResMsg.getLeaderId(), appendLogEntriesResMsg.getTerm(), appendLogEntriesResMsg.getIsSuccess());
            raftServerNode.updateLogIndex(appendLogEntriesResMsg);
        }
    }

}

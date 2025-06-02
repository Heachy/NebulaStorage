package com.cy.ns.raft.netty.handler;

import com.cy.ns.raft.netty.msg.AppendLogEntriesMsg;
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
public class AppendLogEntriesHandler extends SimpleChannelInboundHandler<AppendLogEntriesMsg> {

    @Resource
    RaftServerNode raftServerNode;

    @Override
    protected void channelRead0( ChannelHandlerContext channelHandlerContext, AppendLogEntriesMsg appendLogEntriesMsg ) throws Exception {

        AppendLogEntriesResMsg responseMsg = new AppendLogEntriesResMsg( );
        responseMsg.setIsSuccess( false );
        responseMsg.setTerm( raftServerNode.getCurrentTerm() );
        responseMsg.setNodeId( raftServerNode.getId() );
        if(raftServerNode.getCurrentTerm() > appendLogEntriesMsg.getTerm()) {
            log.info("Refuse append log entries from server {} because of term {} > {}", appendLogEntriesMsg.getLeaderId(), appendLogEntriesMsg.getTerm(), raftServerNode.getCurrentTerm());
            responseMsg.setLeaderId( raftServerNode.getLeaderId() );
        }else{
            log.info("Receive append log entries from server {} with term {}", appendLogEntriesMsg.getLeaderId(), appendLogEntriesMsg.getTerm());
            if(raftServerNode.getCurrentTerm()<appendLogEntriesMsg.getTerm() || raftServerNode.getLeaderId() == -1) {
                raftServerNode.setLeaderId( appendLogEntriesMsg.getLeaderId() );
                raftServerNode.becomeFollower(appendLogEntriesMsg.getTerm(), appendLogEntriesMsg.getLeaderId());
            }
            if(raftServerNode.appendLogEntries(appendLogEntriesMsg)) {
                responseMsg.setIsSuccess( true );
            }
        }

        responseMsg.setPrevLogIndex( raftServerNode.getLastLogIndex().get());
        responseMsg.setPrevLogTerm( raftServerNode.getLastLogTerm());
        channelHandlerContext.writeAndFlush(responseMsg);
    }

}


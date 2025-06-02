package com.cy.ns.raft.netty.handler;


import com.cy.ns.raft.config.RaftConfiguration;
import com.cy.ns.raft.enums.RaftRole;
import com.cy.ns.raft.netty.msg.PullFileRequestMsg;
import com.cy.ns.raft.netty.msg.PullFileResMsg;
import com.cy.ns.raft.node.RaftServerNode;
import com.cy.ns.rs.utils.RSCodingUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import jakarta.annotation.Resource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * @author Haechi
 * @date 2025/4/17
 */
@ChannelHandler.Sharable
@Component
@Slf4j
public class PullFileMsgHandler extends SimpleChannelInboundHandler<PullFileRequestMsg> {

    @Resource
    RaftServerNode raftServerNode;
    @Override
    protected void channelRead0( ChannelHandlerContext channelHandlerContext, PullFileRequestMsg pullFileRequestMsg ) throws Exception {
        PullFileResMsg pullFileResMsg = new PullFileResMsg();
        BeanUtils.copyProperties(pullFileRequestMsg, pullFileResMsg);
        pullFileResMsg.setTerm( raftServerNode.getCurrentTerm() );
        pullFileResMsg.setNodeId( raftServerNode.getId() );
        String realPath = RaftConfiguration.path + "/"+ pullFileRequestMsg.getUserId() +
                "/" +pullFileRequestMsg.getBucketId() + "/" + pullFileRequestMsg.getFileName();

        // 文件实际路径
        Path path = Path.of(realPath);
        // 如果文件存在
        if(path.toFile().exists()) {
            if(raftServerNode.getRole() == RaftRole.LEADER){
                if(Files.size( path ) < pullFileRequestMsg.getFileSize()){
                    raftServerNode.recoverFileAndWait(pullFileRequestMsg);
                }else{
                    pullFileResMsg.setFileData( RSCodingUtil.getBytesByIndex(  Files.readAllBytes( path ), pullFileRequestMsg.nodeId -1) );
                }
            }else{
                if(Files.size( path ) < pullFileRequestMsg.getFileSize()){
                    pullFileResMsg.setFileData( Files.readAllBytes( path ) );
                }else{
                    pullFileResMsg.setFileData( RSCodingUtil.getBytesByIndex(  Files.readAllBytes( path ), raftServerNode.getId()-1) );
                }
            }
            channelHandlerContext.writeAndFlush(pullFileResMsg);
        } else {
            log.error("file not found, file path: {}", realPath);

            if(raftServerNode.getRole() == RaftRole.LEADER){
                // 从节点拉取文件->文件不存在
                raftServerNode.recoverFileAndWait(pullFileRequestMsg);
                log.error(" nodeId: {} need fileName: {}, but no exist", pullFileRequestMsg.getNodeId(), pullFileRequestMsg.getFileName());
            }else{
                // 主节点拉取文件->文件不存在
                // 返回空数据
                pullFileResMsg.setFileData( new byte[0] );
                channelHandlerContext.writeAndFlush(pullFileResMsg);
            }

        }
    }

}

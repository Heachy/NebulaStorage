package com.cy.ns.raft.netty.handler;

import com.cy.ns.raft.config.RaftConfiguration;
import com.cy.ns.raft.enums.RaftRole;
import com.cy.ns.raft.netty.msg.PullFileResMsg;
import com.cy.ns.raft.node.RaftServerNode;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import jakarta.annotation.Resource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Haechi
 * @date 2025/4/17
 */
@ChannelHandler.Sharable
@Component
@Slf4j

public class PullFileMsgResHandler extends SimpleChannelInboundHandler<PullFileResMsg> {

    @Resource
    RaftServerNode raftServerNode;
    @Override
    protected void channelRead0( ChannelHandlerContext channelHandlerContext, PullFileResMsg pullFileResMsg ) throws Exception {
        if(raftServerNode.getRole() == RaftRole.LEADER){
            // 主节点在恢复文件
            raftServerNode.recoverFile( pullFileResMsg );
        }else{
            // 从节点复制文件
            String realPath = RaftConfiguration.path + "/"+ pullFileResMsg.getUserId() +
                    "/" +pullFileResMsg.getBucketId();

            if (pullFileResMsg.getFileData() != null && pullFileResMsg.getFileData().length > 8) {
                log.info("file: {}", Arrays.toString( Arrays.copyOfRange(pullFileResMsg.getFileData(), 0, 8)));
            }

            Path dirPath = Paths.get( realPath ).toAbsolutePath().normalize();
            Files.createDirectories( dirPath );
            Path path = dirPath.resolve( Objects.requireNonNull( pullFileResMsg.getFileName() ) );
            Files.write( path, pullFileResMsg.getFileData() );

            log.info( "upload current time: {}", new Date().getTime() );
        }
    }

}

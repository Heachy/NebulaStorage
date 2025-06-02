package com.cy.ns.raft.netty.channel;

import com.cy.ns.raft.netty.code.MessageCodec;
import com.cy.ns.raft.netty.handler.AppendLogEntriesHandler;
import com.cy.ns.raft.netty.handler.AppendLogEntriesResHandler;
import com.cy.ns.raft.netty.handler.HeartbeatHandler;
import com.cy.ns.raft.netty.handler.PullFileMsgHandler;
import com.cy.ns.raft.netty.handler.PullFileMsgResHandler;
import com.cy.ns.raft.netty.handler.RequestVoteHandler;
import com.cy.ns.raft.netty.handler.RequestVoteResHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Haechi
 * @date 2025/3/12
 */
@Data
@AllArgsConstructor
@Component
@Slf4j
public class RaftChannel {
    MessageCodec messageCodec;

    HeartbeatHandler heartbeatHandler;

    RequestVoteHandler requestVoteHandler;

    RequestVoteResHandler requestVoteResHandler;

    AppendLogEntriesHandler appendLogEntriesHandler;

    AppendLogEntriesResHandler appendLogEntriesResHandler;

    PullFileMsgHandler pullFileMsgHandler;

    PullFileMsgResHandler pullFileMsgResHandler;


    /**
     * 创建服务端
     * @param port 端口
     * @return 服务端channel
     */
    public Channel createServer(Integer port) {
        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        NioEventLoopGroup worker = new NioEventLoopGroup(2);
        Channel channel = null;
        try {
            channel = new ServerBootstrap()
                    .group(boss, worker)
                    .channel( NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new LengthFieldBasedFrameDecoder(1024 * 1024 * 1024, 22, 4, 0, 0))
                                    .addLast(messageCodec)
                                    .addLast(heartbeatHandler)
                                    .addLast(requestVoteHandler)
                                    .addLast( requestVoteResHandler )
                                    .addLast( appendLogEntriesHandler )
                                    .addLast( appendLogEntriesResHandler )
                                    .addLast( pullFileMsgHandler )
                                    .addLast( pullFileMsgResHandler )
                                    ;
                        }
                    })
                    .bind(port)
                    .sync()
                    .channel();
            ChannelFuture closeFuture = channel.closeFuture();
            closeFuture.addListener((ChannelFutureListener) channelFuture -> {
                boss.shutdownGracefully();
                worker.shutdownGracefully();
            });
            log.info("server start success");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return channel;
    }

    /**
     * 创建客户端
     * @param ip ip地址
     * @param port 端口
     * @return 客户端channel
     */
    public Channel createClient(String ip, Integer port) {
            EventLoopGroup group = new NioEventLoopGroup(2);
            Channel channel = null;
            try {
                channel = new Bootstrap()
                        .group(group)
                        .handler(new ChannelInitializer<NioSocketChannel>() {
                            @Override
                            protected void initChannel(NioSocketChannel ch) throws Exception {
                                ch.pipeline()
                                        .addLast(new LengthFieldBasedFrameDecoder(1024 * 1024 * 1024, 22, 4, 0, 0))
                                        .addLast(messageCodec)
                                        .addLast(heartbeatHandler)
                                        .addLast(requestVoteResHandler)
                                        .addLast( requestVoteResHandler )
                                        .addLast( appendLogEntriesHandler )
                                        .addLast( appendLogEntriesResHandler )
                                        .addLast( pullFileMsgHandler )
                                        .addLast( pullFileMsgResHandler )
                                ;
                            }
                        })
                        .channel(NioSocketChannel.class).connect(ip, port)
                        .sync()
                        .channel();
                ChannelFuture closeFuture = channel.closeFuture();
                closeFuture.addListener((ChannelFutureListener) channelFuture -> group.shutdownGracefully() );
                log.info("client start success");
            } catch (Exception e) {
                log.error( e.getMessage() );
            }

            return channel;
    }
}

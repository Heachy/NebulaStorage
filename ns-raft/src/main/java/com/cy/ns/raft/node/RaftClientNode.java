package com.cy.ns.raft.node;

import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Haechi
 * @date 2025/3/12
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RaftClientNode {
    private String ip;
    private int port;
    private int id;
    private Channel channel;
}

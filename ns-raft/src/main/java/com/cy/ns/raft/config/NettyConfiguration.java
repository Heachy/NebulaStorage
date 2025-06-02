package com.cy.ns.raft.config;

import org.springframework.context.annotation.Configuration;

/**
 * @author Haechi
 * @date 2025/3/16
 */
@Configuration
public class NettyConfiguration {
    public static String magicNum = "G.E.M.";
    public static String serializable = "json";
    public static Integer maxSend = 262144;
    public static int TCPPort = 9001;
}

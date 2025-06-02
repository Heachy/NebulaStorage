package com.cy.ns.raft.run;

import com.cy.ns.raft.node.RaftServerNode;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author Haechi
 * @date 2025/3/16
 */
@Component
@Slf4j
public class RaftRunner implements CommandLineRunner {

    @Resource
    RaftServerNode raftServerNode;


    @Override
    public void run( String... args ) throws Exception {
        log.info( "RaftRunner is running" );

        raftServerNode.start();

        raftServerNode.startVoteLoop();

    }



}

package com.cy.ns.raft.config;

import com.cy.ns.raft.node.RaftClientNode;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Haechi
 * @date 2025/3/20
 */
@Configuration
@ConfigurationProperties(prefix = "raft")
public class RaftConfiguration {

    public static int electionTimeoutMin = 11;

    public static int electionTimeoutMax = 1;

    public static int replicateInterval = 2;

    public static List<RaftClientNode> raftClientNodes;

    public static String path;

    public static int id;



    public void setElectionTimeoutMin( int electionTimeoutMin ) {
        RaftConfiguration.electionTimeoutMin = electionTimeoutMin;
    }


    public void setElectionTimeoutMax( int electionTimeoutMax ) {
        RaftConfiguration.electionTimeoutMax = electionTimeoutMax;
    }

    public void setReplicateInterval( int replicateInterval ) {
        RaftConfiguration.replicateInterval = replicateInterval;
    }

    public void setRaftClientNodes( List<RaftClientNode> raftClientNodes ) {
        RaftConfiguration.raftClientNodes = raftClientNodes;
    }
    public void setPath( String path ) {
        RaftConfiguration.path = path;
    }
    public void setId( int id ) {
        RaftConfiguration.id = id;
    }
}

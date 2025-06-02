package com.cy.ns.raft.netty.msg;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Haechi
 * @date 2025/3/15
 * 消息基类
 */
@Data
@NoArgsConstructor
public abstract class Message {
    private static final Map<Integer, Class<?>> MSG_CLASSES = new HashMap<>();
    protected static final int HEARTBEAT_MSG = 0;
    protected static final int REQUEST_VOTE_MSG = 1;
    protected static final int REQUEST_VOTE_RESPONSE_MSG = 2;
    protected static final int APPEND_LOG_ENTRIES_MSG = 3;
    protected static final int APPEND_LOG_ENTRIES_RESPONSE_MSG = 4;
    protected static final int PULL_FILE_REQUEST_MSG = 5;
    protected static final int PULL_FILE_RES_MSG = 6;


    protected String randomStr;


    /**
     * 任期
     */
    public Long term;

    /**
     * 节点id
     */
    public   int nodeId;
    public abstract Integer getType();

    public abstract Integer getSequenceId();

    public static Class<?> getMessageClass(int messageType) {
        return MSG_CLASSES.get(messageType);
    }

    static {
        MSG_CLASSES.put( HEARTBEAT_MSG, HeartbeatMsg.class);
        MSG_CLASSES.put( REQUEST_VOTE_MSG, RequestVoteMsg.class);
        MSG_CLASSES.put( REQUEST_VOTE_RESPONSE_MSG, RequestVoteResponseMsg.class);
        MSG_CLASSES.put( APPEND_LOG_ENTRIES_MSG, AppendLogEntriesMsg.class);
        MSG_CLASSES.put( APPEND_LOG_ENTRIES_RESPONSE_MSG, AppendLogEntriesResMsg.class);
        MSG_CLASSES.put( PULL_FILE_REQUEST_MSG, PullFileRequestMsg.class);
        MSG_CLASSES.put( PULL_FILE_RES_MSG, PullFileResMsg.class);

    }
}

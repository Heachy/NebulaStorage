package com.cy.ns.raft.netty.msg;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Haechi
 * @date 2025/3/15
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HeartbeatMsg extends Message implements Serializable {

    private int leaderId;

    private int commitIndex;

    @Override
    public Integer getType() {
        return Message.HEARTBEAT_MSG;
    }


    @Override
    public Integer getSequenceId() {
        return -1;
    }

}

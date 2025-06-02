package com.cy.ns.raft.netty.msg;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Haechi
 * @date 2025/3/29
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppendLogEntriesResMsg extends Message implements Serializable {

    private Boolean isSuccess;

    private int prevLogIndex;
    private Long prevLogTerm;
    private int leaderId;

    @Override
    public Integer getType() {
        return Message.APPEND_LOG_ENTRIES_RESPONSE_MSG;
    }


    @Override
    public Integer getSequenceId() {
        return -1;
    }

}

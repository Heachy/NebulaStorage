package com.cy.ns.raft.netty.msg;

import com.cy.ns.raft.log.LogEntry;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Haechi
 * @date 2025/3/27
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppendLogEntriesMsg extends Message implements Serializable {
    private int prevLogIndex;
    private Long prevLogTerm;
    private int leaderId;

    private List<LogEntry> entries;

    private int leaderCommit;


    @Override
    public Integer getType() {
        return Message.APPEND_LOG_ENTRIES_MSG;
    }


    @Override
    public Integer getSequenceId() {
        return -1;
    }

}

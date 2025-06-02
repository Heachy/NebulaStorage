package com.cy.ns.raft.netty.msg;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Haechi
 * @date 2025/3/15
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestVoteMsg extends Message implements Serializable {

    private int candidateId;

    private int lastLogIndex;

    private Long lastLogTerm;

    @Override
    public Integer getType() {
        return Message.REQUEST_VOTE_MSG;
    }


    @Override
    public Integer getSequenceId() {
        return -1;
    }

}

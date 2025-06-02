package com.cy.ns.raft.netty.msg;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Haechi
 * @date 2025/3/15
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class RequestVoteResponseMsg extends Message implements Serializable {

    private Boolean voteGranted;

    @Override
    public Integer getType() {
        return Message.REQUEST_VOTE_RESPONSE_MSG;
    }


    @Override
    public Integer getSequenceId() {
        return -1;
    }

}

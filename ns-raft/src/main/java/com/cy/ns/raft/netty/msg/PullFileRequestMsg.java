package com.cy.ns.raft.netty.msg;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Haechi
 * @date 2025/4/17
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PullFileRequestMsg extends Message implements Serializable {
    Long bucketId;
    Long userId;
    String fileMd5;
    String fileName;
    Long fileSize;

    @Override
    public Integer getType() {
        return Message.PULL_FILE_REQUEST_MSG;
    }


    @Override
    public Integer getSequenceId() {
        return -1;
    }

}

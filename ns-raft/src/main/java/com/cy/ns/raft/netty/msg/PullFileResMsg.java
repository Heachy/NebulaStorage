package com.cy.ns.raft.netty.msg;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Haechi
 * @date 2025/4/17
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PullFileResMsg extends Message implements Serializable {
    private String fileMd5;
    private String fileName;
    private Long bucketId;
    private Long userId;
    private byte[] fileData;

    @Override
    public Integer getType() {
        return Message.PULL_FILE_RES_MSG;
    }


    @Override
    public Integer getSequenceId() {
        return -1;
    }

}

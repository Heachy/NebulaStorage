package com.cy.ns.raft.command;

import com.cy.ns.raft.enums.CommandType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Haechi
 * @date 2025/3/26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Command {
    private CommandType commandType;
    private String fileMd5;
    private Long userId;
    private Long bucketId;
    private String fileName;
    private Long fileSize;
}

package com.cy.ns.raft.log;

import com.cy.ns.raft.command.Command;
import java.io.Serializable;
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
public class LogEntry implements Serializable {
    private int index;
    private Long term;
    private Command command;
    private Boolean commandValid =true;



}

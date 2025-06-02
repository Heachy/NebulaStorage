package com.cy.ns.generate.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Haechi
 * @date 2025/4/20
 */
@AllArgsConstructor
@Data
@NoArgsConstructor
public class RaftNodeVO {
    private String ip;
    private int port;
    private int id;
    private Boolean isActive;
    private int logIndex;
    private int nextIndex;
    private Long term;
}

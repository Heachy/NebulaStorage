package com.cy.ns.generate.entity.vo;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Haechi
 * @date 2025/4/13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BucketVO {
    private Long id;
    private String name;
    private Long memoryUsage;
    private Long maxMemory;
    private Integer authority;
    private LocalDateTime createTime;
}

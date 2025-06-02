package com.cy.ns.generate.entity.param;

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
public class BucketParam {
    private Long id;
    private String name;
    private Integer authority;
}

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
public class FileParam {
    private Long id;
    private String name;
    private Long bucketId;
    private Integer page = 1;
    private Integer pageSize = 10;
    private Integer authority;

}

package com.cy.ns.generate.entity.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Haechi
 * @date 2025/4/22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NsConfigParam {
    /**
     * 默认bucket对外权限
     */
    private Integer defaultBucketAuthority;

    /**
     * 默认文件权限
     */
    private Integer defaultFileAuthority;
}

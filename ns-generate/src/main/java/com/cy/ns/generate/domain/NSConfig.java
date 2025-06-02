package com.cy.ns.generate.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author Haechi
 * @since 2025-04-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("ns_config")
public class NSConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 最大bucket数
     */
    private Integer maxBucketCount;

    /**
     * 默认bucket大小
     */
    private Long defaultBucketMemory;

    /**
     * 默认bucket对外权限
     */
    private Integer defaultBucketAuthority;

    /**
     * 默认文件权限
     */
    private Integer defaultFileAuthority;

    /**
     * 默认文件删除保存时间
     */
    private Integer defaultFileSave;


}

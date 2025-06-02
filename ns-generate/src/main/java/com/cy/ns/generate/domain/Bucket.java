package com.cy.ns.generate.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serial;
import java.time.LocalDateTime;
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
public class Bucket implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * bucket名称
     */
    private String name;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 当前bucket大小
     */
    private Long memoryUsage;

    /**
     * 占用内存最大值
     */
    private Long maxMemory;

    /**
     * 对外权限
     */
    private Integer authority;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    private Boolean isDel;


}

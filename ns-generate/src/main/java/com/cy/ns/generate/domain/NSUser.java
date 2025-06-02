package com.cy.ns.generate.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("ns_user")
public class NSUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名称
     */
    private String name;

    /**
     * 电话
     */
    private String phone;

    /**
     * 密码
     */
    private String password;

    /**
     * 头像url
     */
    private String avatarUrl;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 创建时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    private Boolean isDel;


}

package com.cy.ns.raft.entity.po;

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
@TableName("file")
public class FilePO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文件名
     */
    private String name;

    /**
     * 文件大小 以kb为单位
     */
    private Long fileSize;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * bucket_id
     */
    private Long bucketId;

    /**
     * md5编码
     */
    private String md5;

    /**
     * 文件对外权限
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

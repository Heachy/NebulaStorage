package com.cy.ns.raft.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Haechi
 * @date 2025/4/21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("cluster_log")
public class ClusterLogPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private int type;
    private String msg;
    private int nodeId;
    private LocalDateTime logTime;
}

package com.cy.ns.raft.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Haechi
 * @date 2025/4/1
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("node_info")
public class NodeInfoPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private int id;
    private String ip;
    private int port;
    private int applyIndex;
    private Long applyTerm;

}

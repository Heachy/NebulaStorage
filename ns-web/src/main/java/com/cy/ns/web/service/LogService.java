package com.cy.ns.web.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cy.ns.generate.entity.param.LogParam;
import com.cy.ns.raft.entity.po.ClusterLogPO;

/**
 * @author Haechi
 * @date 2025/4/21
 */
public interface LogService {
    Page<ClusterLogPO> getLogList( LogParam logParam );
}

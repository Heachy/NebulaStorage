package com.cy.ns.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cy.ns.generate.entity.param.LogParam;
import com.cy.ns.raft.entity.po.ClusterLogPO;
import com.cy.ns.raft.service.ClusterLogService;
import com.cy.ns.web.service.LogService;
import org.springframework.stereotype.Service;

/**
 * @author Haechi
 * @date 2025/4/21
 */
@Service
public class LogServiceImpl implements LogService {

    ClusterLogService clusterLogService;

    public LogServiceImpl( ClusterLogService clusterLogService ) {
        this.clusterLogService = clusterLogService;
    }

    @Override
    public Page<ClusterLogPO> getLogList( LogParam logParam ) {
        QueryWrapper<ClusterLogPO> queryWrapper = new QueryWrapper<>();

        if(logParam.getNodeId()!= null) {
            queryWrapper.eq("node_id", logParam.getNodeId());
        }
        if(logParam.getType()!= null) {
            queryWrapper.eq("type", logParam.getType());
        }
        if(logParam.getStartTime()!= null){
            queryWrapper.ge("log_time", logParam.getStartTime());
        }
        if(logParam.getEndTime()!= null){
            queryWrapper.le("log_time", logParam.getEndTime());
        }
        if(logParam.getPage() == null) {
            logParam.setPage(1);
        }
        if(logParam.getPageSize() == null) {
            logParam.setPageSize(10);
        }
        queryWrapper.orderByDesc( "log_time" );
        // 创建分页对象，参数：当前页、每页大小
        Page<ClusterLogPO> page = new Page<>(logParam.getPage(), logParam.getPageSize());

        return clusterLogService.getBaseMapper().selectPage( page, queryWrapper );

    }

}

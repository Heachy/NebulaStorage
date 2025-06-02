package com.cy.ns.raft.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.ns.raft.entity.po.ClusterLogPO;
import com.cy.ns.raft.mapper.ClusterLogMapper;
import com.cy.ns.raft.service.ClusterLogService;
import org.springframework.stereotype.Service;

/**
 * @author Haechi
 * @date 2025/4/21
 */
@Service
public class ClusterLogServiceImpl extends ServiceImpl<ClusterLogMapper, ClusterLogPO> implements ClusterLogService {

}

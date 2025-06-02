package com.cy.ns.raft.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.ns.raft.entity.po.NodeInfoPO;
import com.cy.ns.raft.mapper.NodeInfoMapper;
import com.cy.ns.raft.service.NodeInfoService;
import org.springframework.stereotype.Service;

/**
 * @author Haechi
 * @date 2025/4/1
 */
@Service
public class NodeInfoServiceImpl extends ServiceImpl<NodeInfoMapper, NodeInfoPO> implements NodeInfoService {

}

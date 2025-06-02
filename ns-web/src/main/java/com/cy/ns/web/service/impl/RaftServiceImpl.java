package com.cy.ns.web.service.impl;

import com.cy.ns.generate.entity.vo.RaftNodeVO;
import com.cy.ns.generate.entity.vo.RaftVO;
import com.cy.ns.raft.node.RaftClientNode;
import com.cy.ns.raft.node.RaftServerNode;
import com.cy.ns.web.service.RaftService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * @author Haechi
 * @date 2025/4/20
 */
@Service
public class RaftServiceImpl implements RaftService {
    RaftServerNode raftServerNode;

    public RaftServiceImpl(RaftServerNode raftServerNode) {
        this.raftServerNode = raftServerNode;
    }

    @Override
    public RaftVO getRaftInfo() {
        RaftVO raftVO = new RaftVO();
        List<RaftNodeVO> list = new ArrayList<>();
        RaftServerNode.raftNodeMap.forEach((k, v) -> {
            RaftNodeVO raftNodeVO = new RaftNodeVO();
            BeanUtils.copyProperties(v, raftNodeVO);
            raftNodeVO.setIsActive( v.getChannel().isActive());
            raftNodeVO.setNextIndex( raftServerNode.getNextIndex().get( raftNodeVO.getId() ) );
            list.add( raftNodeVO );
        });
        RaftNodeVO raftNodeVO = new RaftNodeVO();
        raftNodeVO.setTerm( raftServerNode.getCurrentTerm() );
        raftNodeVO.setLogIndex( raftServerNode.getLastLogIndex().get() );
        raftNodeVO.setIp( raftServerNode.getIp() );
        raftNodeVO.setPort( raftServerNode.getPort() );
        raftNodeVO.setId( raftServerNode.getId() );
        raftNodeVO.setIsActive( raftServerNode.getChannel().isActive() );
        raftVO.setLeader( raftNodeVO );
        raftVO.setNodes( list );
        return raftVO;
    }
}

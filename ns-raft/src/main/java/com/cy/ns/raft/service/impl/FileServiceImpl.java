package com.cy.ns.raft.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.ns.raft.entity.po.FilePO;
import com.cy.ns.raft.mapper.FileMapper;
import com.cy.ns.raft.service.FileService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Haechi
 * @since 2025-04-08
 */
@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, FilePO> implements FileService {

}

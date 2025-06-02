package com.cy.ns.web.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cy.ns.raft.entity.po.FilePO;
import com.cy.ns.generate.entity.param.FileParam;
import com.cy.ns.generate.entity.vo.FileVO;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Haechi
 * @date 2025/4/5
 */
public interface NsFileService {

    Page<FileVO> fileList( FileParam fileParam);

    Page<FileVO> deleteFileList( FileParam fileParam);

    FileVO fileInfo( Long fileId);

    String uploadFile( MultipartFile file, Long bucketId ) throws IOException, ExecutionException, InterruptedException;


    FilePO downloadFile(Long fileId);

    String deleteFile(Long fileId);

    String deleteFilePermanent(Long fileId);

    String recoverFile(Long fileId);

    String updateFileAuthority(Long fileId, Integer authority);

}

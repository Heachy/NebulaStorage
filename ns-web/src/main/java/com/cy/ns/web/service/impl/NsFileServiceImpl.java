package com.cy.ns.web.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cy.ns.common.constants.NsConstants;
import com.cy.ns.common.enums.AuthorityEnums;
import com.cy.ns.generate.domain.Bucket;
import com.cy.ns.raft.entity.po.FilePO;
import com.cy.ns.generate.domain.NSConfig;
import com.cy.ns.generate.entity.param.FileParam;
import com.cy.ns.generate.entity.vo.FileVO;
import com.cy.ns.generate.service.BucketService;
import com.cy.ns.raft.service.FileService;
import com.cy.ns.raft.config.RaftConfiguration;
import com.cy.ns.raft.node.RaftServerNode;
import com.cy.ns.web.handler.FileHandler;
import com.cy.ns.web.service.ConfigService;
import com.cy.ns.web.service.NsFileService;
import com.cy.ns.web.util.FileUtil;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Haechi
 * @date 2025/4/5
 */
@Service
@Slf4j
public class NsFileServiceImpl implements NsFileService  {

    FileService fileService;

    BucketService bucketService;

    FileHandler fileHandler;

    RaftServerNode raftServerNode;

    ConfigService configService;

    DeleteFileQueService deleteFileQueService;

    public NsFileServiceImpl( FileService fileService,
                               BucketService bucketService,
                               FileHandler fileHandler,
                               RaftServerNode raftServerNode,
                               ConfigService configService,
                               DeleteFileQueService deleteFileQueService) {
        this.fileService = fileService;
        this.bucketService = bucketService;
        this.fileHandler = fileHandler;
        this.raftServerNode = raftServerNode;
        this.configService = configService;
        this.deleteFileQueService = deleteFileQueService;
    }

    @Override
    public Page<FileVO> fileList( FileParam fileParam ) {
        // 分页查询
        QueryWrapper<FilePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq( "bucket_id", fileParam.getBucketId() )
                .eq( "is_del",0 );
        if(fileParam.getName()!=null && !fileParam.getName().isEmpty()){
            queryWrapper.like( "name", fileParam.getName() );
        }
        queryWrapper.select( "id, name, file_size, md5, authority, create_time, update_time" );
        // 创建分页对象，参数：当前页、每页大小
        Page<FilePO> page = new Page<>(fileParam.getPage(), fileParam.getPageSize());
        // 可选：设置是否查询总记录数（默认true）
        page.setSearchCount(true);

        // 执行分页查询
        Page<FilePO> selectPage = fileService.getBaseMapper().selectPage( page, queryWrapper );

        // 转换为VO对象
        List<FileVO> fileVOList = selectPage.getRecords().stream().map( file -> {
            FileVO fileVO = new FileVO();
            BeanUtils.copyProperties( file, fileVO );
            return fileVO;
        } ).toList();

        // 创建新的Page对象，设置查询结果和总记录数
        Page<FileVO> fileVOPage = new Page<>();
        fileVOPage.setRecords( fileVOList );
        fileVOPage.setTotal( selectPage.getTotal() );
        fileVOPage.setCurrent( selectPage.getCurrent() );
        fileVOPage.setSize( selectPage.getSize());

        return fileVOPage;

    }


    @Override
    public Page<FileVO> deleteFileList( FileParam fileParam ) {
        // 分页查询
        QueryWrapper<FilePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq( "is_del",1 )
                .eq( "user_id",StpUtil.getLoginIdAsLong() );
        queryWrapper.select( "id, name, file_size, md5, authority, create_time, update_time" );
        // 创建分页对象，参数：当前页、每页大小
        Page<FilePO> page = new Page<>(fileParam.getPage(), fileParam.getPageSize());
        // 可选：设置是否查询总记录数（默认true）
        page.setSearchCount(true);

        // 执行分页查询
        Page<FilePO> selectPage = fileService.getBaseMapper().selectPage( page, queryWrapper );

        // 转换为VO对象
        List<FileVO> fileVOList = selectPage.getRecords().stream().map( file -> {
            FileVO fileVO = new FileVO();
            BeanUtils.copyProperties( file, fileVO );
            return fileVO;
        } ).toList();

        // 创建新的Page对象，设置查询结果和总记录数
        Page<FileVO> fileVOPage = new Page<>();
        fileVOPage.setRecords( fileVOList );
        fileVOPage.setTotal( selectPage.getTotal() );
        fileVOPage.setCurrent( selectPage.getCurrent() );
        fileVOPage.setSize( selectPage.getSize());

        return fileVOPage;
    }


    @Override
    public FileVO fileInfo( Long fileId ) {
        // 检验文件对外权限
        QueryWrapper<FilePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq( "id", fileId )
                .eq( "is_del", 0 );
        FilePO file = fileService.getOne( queryWrapper );

        if( file == null ){
            return null;
        }
        if(!hasReadPermission( file )){
            return null;
        }
        FileVO fileVO = new FileVO();
        BeanUtils.copyProperties( file, fileVO );

        return fileVO;
    }


    @Override
    public String uploadFile( MultipartFile file, Long bucketId ) throws ExecutionException, InterruptedException {

        // 检查文件是否为空
        if (file.isEmpty()) {
            return "上传文件不能为空";
        }

        // 获取安全处理的文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            return "文件名无效";
        }

        // 检查文件名是否重复
        QueryWrapper<FilePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq( "bucket_id", bucketId )
                .eq( "name", originalFilename )
                .eq( "is_del", 0 );
        if ( fileService.count( queryWrapper ) > 0 ) {
            return "文件名已存在";
        }

        // 检查文件大小
        Bucket bucket = bucketService.getById( bucketId );
        bucket.setMemoryUsage( bucket.getMemoryUsage() + file.getSize() );
        if ( bucket.getMemoryUsage() > bucket.getMaxMemory() ) {
            return "文件大小超过限制";
        }
        bucketService.updateById( bucket );

        Long userId = StpUtil.getLoginIdAsLong();

        NSConfig config = configService.getConfig();

        return fileHandler.handleUpload( file, bucketId )
                .thenApply( result -> {
                    if ( result.getStatus() == 200L ) {
                        System.out.println("日志同步成功" );
                        // 保存文件信息到数据库
                        FilePO nsFile = new FilePO();
                        nsFile.setUserId( userId );
                        nsFile.setName( originalFilename );
                        nsFile.setFileSize( file.getSize() );
                        nsFile.setBucketId( bucketId );
                        if ( config.getDefaultFileAuthority()==AuthorityEnums.EXTEND.getCode()){
                            nsFile.setAuthority( bucket.getAuthority() );
                        }else{
                            nsFile.setAuthority( config.getDefaultFileAuthority() );
                        }
                        nsFile.setCreateTime( LocalDateTime.now() );
                        nsFile.setUpdateTime( LocalDateTime.now() );
                        nsFile.setMd5( FileUtil.getMd5( file ) );
                        fileService.save( nsFile );

                        return NsConstants.UPLOAD_SUCCESS;
                    } else {
                        return result.getMessage();
                    }
                } ).get();
    }


    @Override
    public FilePO downloadFile( Long fileId) {
        QueryWrapper<FilePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq( "id", fileId ).eq( "is_del", 0 );
        FilePO file = fileService.getOne( queryWrapper );
        if ( file == null ) {
            return null;
        }
        if(!hasReadPermission( file )){
            return null;
        }

        String realPath = RaftConfiguration.path + "/"+ file.getUserId() +
                "/" +file.getBucketId() + "/" + file.getName();

        // 文件实际路径
        Path path = Path.of(realPath);

        // 如果文件存在
        try {
            if(!path.toFile().exists()|| Files.size( path )< file.getFileSize() ) {
                log.error("file not found, file path: {}", realPath);
                log.info( "start recover: {}", new Date().getTime() );
                if(fileHandler.handledDownload( file ).get()){
                    log.info( "end recover: {}", new Date().getTime() );
                    log.info("file recover success, file path: {}", realPath);
                }else{
                    log.error("file recover failed, file path: {}", realPath);
                    return null;
                }
            }
        } catch ( Exception e ) {
            log.error( e.getMessage() );
            return null;
        }

        return file;
    }


    @Override
    public String deleteFile( Long fileId ) {
        QueryWrapper<FilePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq( "id", fileId ).eq( "is_del", 0 );
        FilePO file = fileService.getOne( queryWrapper );
        if ( file == null ) {
            return "无此文件";
        }

        NSConfig config = configService.getConfig();

        try {
            deleteFileQueService.putMsg( fileId.toString() ,config.getDefaultFileSave()*24*60*60*1000);
            UpdateWrapper<FilePO> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq( "id", fileId )
                    .set( "is_del", 1 )
                    .set( "update_time", LocalDateTime.now() );
            if ( fileService.update( updateWrapper )){
                return NsConstants.DELETE_SUCCESS;
            }
        } catch ( Exception e ) {
            log.error( e.getMessage() );
            return "删除失败";
        }
        return "删除失败";
    }


    @Override
    public String deleteFilePermanent( Long fileId ) {
        QueryWrapper<FilePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq( "id", fileId ).eq( "is_del", 1 );
        FilePO file = fileService.getOne( queryWrapper );
        if ( file == null ) {
            return "无此文件";
        }


        try {
            if(fileHandler.handledDelete( file ).get()){
                if(fileService.removeById( fileId )) {
                    return NsConstants.DELETE_SUCCESS;
                }
            }
        } catch ( Exception e ) {
            log.error( e.getMessage() );
            return "删除失败";
        }
        return "删除失败";
    }


    @Override
    public String recoverFile( Long fileId ) {
        QueryWrapper<FilePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq( "id", fileId ).eq( "is_del", 1 );
        FilePO file = fileService.getOne( queryWrapper );
        if ( file == null ) {
            return "无此文件, 或者文件未删除";
        }


        try {
            UpdateWrapper<FilePO> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq( "id", fileId )
                    .set( "is_del", 0 )
                    .set( "update_time", LocalDateTime.now() );
            if ( fileService.update( updateWrapper )){
                return NsConstants.RECOVER_SUCCESS;
            }
        } catch ( Exception e ) {
            log.error( e.getMessage() );
            return "恢复失败";
        }
        return "恢复失败";
    }


    @Override
    public String updateFileAuthority( Long fileId, Integer authority ) {
        if ( fileId == null || authority == null ) {
            return "参数不能为空";
        }
        UpdateWrapper<FilePO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq( "id", fileId )
                .set( "authority", authority )
                .set( "update_time", LocalDateTime.now() );
        return fileService.update( updateWrapper ) ? NsConstants.UPDATE_SUCCESS : NsConstants.UPDATE_FAIL;
    }

    private Boolean hasReadPermission( FilePO file) {
        // 如果是访客
        if ( StpUtil.hasRole( "guest" ) || StpUtil.getLoginIdAsLong() != file.getUserId()){
            return file.getAuthority() != AuthorityEnums.FORBIDDEN.getCode();
        }
        return true;
    }

}

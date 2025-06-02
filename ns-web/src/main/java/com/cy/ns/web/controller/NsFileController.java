package com.cy.ns.web.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cy.ns.common.api.CommonResult;
import com.cy.ns.common.constants.NsConstants;
import com.cy.ns.common.utils.OssManagerUtil;
import com.cy.ns.raft.entity.po.FilePO;
import com.cy.ns.generate.entity.param.FileParam;
import com.cy.ns.generate.entity.vo.FileVO;
import com.cy.ns.raft.config.RaftConfiguration;
import com.cy.ns.web.service.NsFileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Haechi
 * @date 2025/4/5
 */
@RestController
@RequestMapping("/file")
public class NsFileController {

    NsFileService nsFileService;


    public NsFileController( NsFileService nsFileService ) {
        this.nsFileService = nsFileService;
    }


    /**
     * 上传文件
     *
     * @return 文件名
     */
    @RequestMapping("/upload/single")
    public CommonResult<String> upload(@RequestParam("file") MultipartFile file, @RequestParam("bucketId")Long bucketId ) throws IOException, ExecutionException, InterruptedException {
        String msg = nsFileService.uploadFile( file, bucketId );
        if ( msg != null && msg.equals( NsConstants.UPLOAD_SUCCESS )) {
            return CommonResult.success( msg );
        }
        return CommonResult.failed( msg );
    }

    /**
     * 批量上传文件
     *
     * @return 文件名
     */
    @RequestMapping("/upload/multiple")
    public CommonResult<List<String>> uploadMultiple( @RequestParam("files") MultipartFile[] files ) throws IOException {
        List<String> list = new ArrayList<>();
        for ( MultipartFile file : files ) {
            list.add( OssManagerUtil.getUrl( Objects.requireNonNull( file.getOriginalFilename() ),file.getInputStream() ));
        }
        return CommonResult.success( list );
    }

    @PostMapping("/delete/{id}")
    public CommonResult<String> deleteFile(@PathVariable("id") Long id) {
        // 删除文件
        String msg = nsFileService.deleteFile( id );
        if ( msg != null && msg.equals( NsConstants.DELETE_SUCCESS )) {
            return CommonResult.success( msg );
        }
        return CommonResult.failed( msg );
    }

    @PostMapping("/delete/permanent/{id}")
    public CommonResult<String> deleteFilePermanent(@PathVariable("id") Long id) {
        // 删除文件
        String msg = nsFileService.deleteFilePermanent( id );
        if ( msg != null && msg.equals( NsConstants.DELETE_SUCCESS )) {
            return CommonResult.success( msg );
        }
        return CommonResult.failed( msg );
    }

    @PostMapping("/recover/{id}")
    public CommonResult<String> recoverFile(@PathVariable("id") Long id) {
        // 恢复删除文件
        String msg = nsFileService.recoverFile( id );
        if ( msg != null && msg.equals( NsConstants.RECOVER_SUCCESS )) {
            return CommonResult.success( msg );
        }
        return CommonResult.failed( msg );
    }



    /**
     * 文件列表
     * @param fileParam 参数
     * @return 文件列表
     */
    @PostMapping("/list")
    public CommonResult<Page<FileVO>> fileList(@RequestBody FileParam fileParam) {
        // 分页查询
        return CommonResult.success( nsFileService.fileList( fileParam ) );
    }

    @PostMapping("/delete/list")
    public CommonResult<Page<FileVO>> deleteFileList(@RequestBody FileParam fileParam) {
        // 分页查询
        return CommonResult.success( nsFileService.deleteFileList( fileParam ) );
    }

    /**
     * 文件信息
     * @param fileId 文件ID
     * @return 文件信息
     */
    @GetMapping("/info/{fileId}")
    public CommonResult<FileVO> fileInfo(@PathVariable("fileId") Long fileId) {
        // 查询文件信息
        FileVO file = nsFileService.fileInfo( fileId );
        if ( file != null ) {
            return CommonResult.success( file );
        }
        return CommonResult.failed( "文件不存在或者没有访问权限" );
    }

    @PostMapping("/authority/update")
    public CommonResult<String> updateFileAuthority(@RequestBody FileParam fileParam) {
        // 更新文件权限
        String msg =nsFileService.updateFileAuthority( fileParam.getId(),fileParam.getAuthority() );
        if ( msg != null && msg.equals( NsConstants.UPDATE_SUCCESS )) {
            return CommonResult.success( msg );
        }
        return CommonResult.failed( msg );
    }


    @GetMapping("/download/{fileId}")
    public void download(@PathVariable("fileId") Long fileId, HttpServletResponse response){
        try {
            // 从Service获取文件元信息
            FilePO file = nsFileService.downloadFile(fileId);
            if (file == null) {
                CommonResult<String> result = CommonResult.failed("无法获取文件信息");
                writeJsonResponse(response, result);
                return;
            }

            Path path = Paths.get( RaftConfiguration.path +
                    "/" + StpUtil.getLoginId() + "/" + file.getBucketId() + "/" ).resolve( file.getName() ).normalize();

            // 设置响应头（强制下载）
            response.setContentType("application/octet-stream");
            response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,HttpHeaders.CONTENT_DISPOSITION);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename="+URLEncoder.encode(file.getName(), StandardCharsets.UTF_8 ));

            // 流式传输文件内容
            Files.copy(path, response.getOutputStream());
            response.flushBuffer();

        } catch ( FileNotFoundException e) {
            // 封装错误信息到CommonResult
            CommonResult<String> result = CommonResult.failed("File not found");
            writeJsonResponse(response, result);

        } catch (IOException e) {
            CommonResult<String> result = CommonResult.failed("Download failed");
            writeJsonResponse(response, result);
        }
    }

    // 工具方法：将CommonResult转为JSON响应
    private void writeJsonResponse(HttpServletResponse response, CommonResult<?> result) {
        try {
            response.setStatus( HttpServletResponse.SC_OK );
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(new ObjectMapper().writeValueAsString(result));
            response.getWriter().flush();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to write JSON response", ex);
        }
    }



}

package com.cy.ns.web.handler;

import cn.dev33.satoken.stp.StpUtil;
import com.cy.ns.common.api.CommonResult;
import com.cy.ns.raft.entity.po.FilePO;
import com.cy.ns.raft.command.Command;
import com.cy.ns.raft.config.RaftConfiguration;
import com.cy.ns.raft.enums.CommandType;
import com.cy.ns.raft.log.LogEntry;
import com.cy.ns.raft.netty.msg.PullFileRequestMsg;
import com.cy.ns.raft.node.RaftServerNode;
import com.cy.ns.web.util.FileUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Haechi
 * @date 2025/4/15
 */
@Component
@Slf4j
public class FileHandler {

    RaftServerNode raftServerNode;

    public FileHandler( RaftServerNode raftServerNode ) {
        this.raftServerNode = raftServerNode;
    }

    public CompletableFuture<CommonResult<String>> handleUpload( MultipartFile file, Long bucketId) {
        // 1. 生成临时存储路径
        Path tempPath;

        // 2. 保存文件到本地
        try {
            // 保存文件到本地
            String realPath = RaftConfiguration.path + "/" + StpUtil.getLoginId() + "/" + bucketId;
            Path targetDir = Paths.get( realPath ).toAbsolutePath().normalize();
            Files.createDirectories( targetDir );
            tempPath = targetDir.resolve( Objects.requireNonNull( file.getOriginalFilename() ) );
            file.transferTo(tempPath);
        } catch ( IOException e) {
            return CompletableFuture.failedFuture(e);
        }

        // 3. 创建日志条目（不包含文件内容，只含元数据）
        LogEntry logEntry = new LogEntry();
        Command command = new Command();
        command.setCommandType( CommandType.UPLOAD );
        command.setFileMd5( FileUtil.getMd5( file ) );
        command.setFileName( file.getOriginalFilename() );
        command.setBucketId( bucketId );
        command.setUserId( Long.valueOf( (String) StpUtil.getLoginId() ) );
        command.setFileSize( file.getSize() );
        logEntry.setCommand( command );
        logEntry.setCommandValid( true );
        System.out.println("保存完temp");

        // 4. 提交日志并等待提交
        CompletableFuture<Long> commitFuture = raftServerNode.appendAndWaitCommit(logEntry);

        // 5. 日志提交后触发文件持久化和响应
        return commitFuture.thenApplyAsync(commitIndex -> {
            // 返回客户端响应
            return CommonResult.success( "File uploaded successfully");

        }).exceptionally(ex -> {
            // 失败处理：删除临时文件
            FileUtil.cleanupTempFile(tempPath);
            return CommonResult.failed("File upload failed: " + ex.getMessage());
        });
    }


    public CompletableFuture<Boolean> handledDownload( FilePO file ) {

        // 提交恢复任务
        return raftServerNode.recoverFileAndWait( new PullFileRequestMsg(
                file.getBucketId(),file.getUserId(),file.getMd5(),file.getName(),file.getFileSize()) );
    }

    public CompletableFuture<Boolean> handledDelete( FilePO file ) {

        // 提交恢复任务
        return raftServerNode.deleteFileAndWait( new Command(CommandType.DELETE,file.getMd5(),file.getUserId(),file.getBucketId(),file.getName(),file.getFileSize()) );
    }


}

package com.cy.ns.web.util;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Haechi
 * @date 2025/4/14
 */
@Slf4j
public class FileUtil {

    /**
     * 获取上传文件的md5
     * @param file 上传的文件
     * @return md5
     */
    public static String getMd5( MultipartFile file) {
        try {
            //获取文件的byte信息
            byte[] uploadBytes = file.getBytes();
            // 拿到一个MD5转换器
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(uploadBytes);
            //转换为16进制
            return new BigInteger(1, digest).toString(16);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * 清理临时文件，处理所有可能的异常场景
     * @param tempPath 需要清理的临时文件路径
     */
    public static void cleanupTempFile( Path tempPath) {
        if (tempPath == null) {
            log.warn("Attempted to clean up null temporary file path");
            return;
        }

        try {
            // 1. 检查文件是否存在
            if (!Files.exists(tempPath)) {
                log.debug("Temporary file does not exist: {}", tempPath);
                return;
            }

            // 2. 尝试删除文件
            boolean deleted = Files.deleteIfExists(tempPath);

            // 3. 处理删除结果
            if (deleted) {
                log.info("Successfully deleted temporary file: {}", tempPath);
            } else {
                // 可能发生的竞态条件：文件在检查存在后突然消失
                log.warn("File disappeared before deletion: {}", tempPath);
            }
        } catch (SecurityException e) {
            log.error("Security manager blocked file deletion: {}", tempPath, e);
        } catch ( IOException e) {
            // 细化异常类型处理
            if (e instanceof AccessDeniedException ) {
                log.error("Delete permission denied for: {}", tempPath, e);
            } else if (e instanceof DirectoryNotEmptyException ) {
                log.error("Cannot delete non-empty directory: {}", tempPath, e);
            } else {
                log.error("Failed to delete temporary file: {}", tempPath, e);
            }

            // 4. 强制删除尝试（慎用）
            attemptForceDelete(tempPath);
        }
    }

    /**
     * 强制删除文件（处理被占用的文件）
     */
    private static void attemptForceDelete(Path tempPath) {
        try {
            // 使用 NIO 的 deleteOnExit 作为最后手段
            tempPath.toFile().deleteOnExit();
            log.warn("Marked file for deletion on JVM exit: {}", tempPath);
        } catch (Exception e) {
            log.error("Failed to mark file for deletion: {}", tempPath, e);
        }
    }


}

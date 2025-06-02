package com.cy.ns.common.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import java.io.IOException;
import java.io.InputStream;

/**
 * 阿里Oss对象存储工具类
 */
public class OssManagerUtil {

    private static final String ENDPOINT = "ENDPOINT";

    private static final String ACCESS_KEY_ID = "ACCESS_KEY_ID";

    private static final String ACCESS_KEY_SECRET = "ACCESS_KEY_SECRET";

    private static final String BUCKET = "BUCKET";

    private static final OSS CLIENT;

    static {
        CLIENT = new OSSClientBuilder().build(ENDPOINT, ACCESS_KEY_ID, ACCESS_KEY_SECRET);
    }
    /**
     * 上传图片
     * @param fileName 图片名称
     * @param length   图片大小
     * @param content  输入流
     */
    public static String uploadImage(String fileName, long length, InputStream content) {

        uploadBucketImage(BUCKET, fileName, length, content);

        return "https://" + BUCKET + "." + ENDPOINT + "/" + fileName;
    }


    /**
     * 上传文件
     * @param bucket   存储空间名
     * @param fileName 文件名
     * @param length   流的长度
     * @param content  输入流
     */
    public static void uploadBucketImage(String bucket, String fileName, long length, InputStream content) {

        // 创建上传Object的Metadata
        ObjectMetadata meta = new ObjectMetadata();

        // 必须设置ContentLength
        meta.setContentLength(length);

        // 上传Object.
        CLIENT.putObject(bucket, fileName, content, meta);
    }


    /**
     * 删除文件
     * @param url 图片的url
     */
    public static void deleteImage(String url) {

        String filename=url.replace("https://" + BUCKET + "." + ENDPOINT + "/","");

        //执行删除
        CLIENT.deleteObject(BUCKET, filename);

    }


    /**
     * 获得上传文件后的url
     * @param fileName 文件名
     * @param inputStream 输入流
     */
    public static String getUrl(String fileName, InputStream inputStream){

        assert fileName != null;

        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);

        fileName = java.util.UUID.randomUUID().toString().replace("-", "") + "." + suffix;

        String url = null;

        try {
            url = OssManagerUtil.uploadImage(fileName,inputStream.available(),inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return url;
    }
}


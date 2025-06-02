package com.cy.ns.rs.utils;

import com.cy.ns.rs.config.RrsManager;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Haechi
 */

@Slf4j
public class RSCodingUtil {
    public static final int DATA_SHARDS = RrsManager.config.getDataShards();
    public static final int PARITY_SHARDS = RrsManager.config.getParityShards();
    public static final int TOTAL_SHARDS = DATA_SHARDS + PARITY_SHARDS;
    private static final int BYTES_IN_INT = 4;
    private static final ReedSolomon REED_SOLOMON = ReedSolomon.create(DATA_SHARDS, PARITY_SHARDS);

    /**
     * 编码
     * @param data 原文件的字节数组
     * @return 包含数据块序号的数据块
     */
    public static byte[][] encodeByteArr(byte[] data , int needIndex) {
        // 检测data长度
        if(data.length<DATA_SHARDS){
            throw new RuntimeException("data长度不足以进行编码");
        }
        int shardSize = (int) Math.ceil((double) data.length / DATA_SHARDS);

        shardSize+=BYTES_IN_INT;

        byte[][] shards = new byte[TOTAL_SHARDS][shardSize];

        int index=0;

        // 给每个数据块添加序号
        for(int i=0;i<DATA_SHARDS-1;i++){

            System.arraycopy(intToBytes(i),0,shards[i],0,BYTES_IN_INT);

            System.arraycopy(data, index, shards[i], BYTES_IN_INT,shardSize-BYTES_IN_INT);

            index+=shardSize-BYTES_IN_INT;
        }

        // 检测最后一个数据块是否填充完毕
        if(index+shardSize>data.length) {
            System.arraycopy(intToBytes(DATA_SHARDS-1),0,shards[DATA_SHARDS-1],0,BYTES_IN_INT);

            System.arraycopy(data, index, shards[DATA_SHARDS-1], BYTES_IN_INT, data.length-index);

            // 填充不足的数据
            for(int i=BYTES_IN_INT+data.length-index;i<shardSize;i++){
                shards[DATA_SHARDS-1][i]=0;
            }
        }
        for(int i=DATA_SHARDS;i<TOTAL_SHARDS;i++){
            System.arraycopy(intToBytes(i),0,shards[i],0,BYTES_IN_INT);
        }

        // 创建校验码
        REED_SOLOMON.encodeParity(shards, BYTES_IN_INT, shardSize-BYTES_IN_INT, needIndex);

        return shards;
    }

    public static byte[] getBytesByIndex(byte[] data, int index) {
        if(index<0||index>=TOTAL_SHARDS){
            throw new RuntimeException("数据块序号错误");
        }
        int shardSize = (int) Math.ceil((double) data.length / DATA_SHARDS);

        shardSize+=BYTES_IN_INT;

        byte[] res = new byte[shardSize];
        System.arraycopy(intToBytes(index),0,res,0,BYTES_IN_INT);

        if(index<DATA_SHARDS){

            if(index == DATA_SHARDS -1){
                System.arraycopy(data, index*(shardSize-BYTES_IN_INT), res, BYTES_IN_INT,data.length-index*(shardSize-BYTES_IN_INT));
                if(DATA_SHARDS*(shardSize-BYTES_IN_INT)>data.length){
                    // 填充不足的数据
                    for(int i=BYTES_IN_INT+data.length-((DATA_SHARDS-1)*(shardSize-BYTES_IN_INT));i<shardSize;i++){
                        res[i]=0;
                    }
                }
            }else{
                System.arraycopy(data, index*(shardSize-BYTES_IN_INT), res, BYTES_IN_INT,shardSize-BYTES_IN_INT);
            }
            return res;
        }
        return encodeByteArr(data , index)[index];
    }

    /**
     * 编码
     * @param file 原文件
     * @return 包含数据块序号的数据块
     */
    public static byte[][] encodeFile(MultipartFile file){
        try {
            return encodeByteArr(file.getBytes(), -1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 编码
     * @param inputStream 原文件的输入流
     * @return 包含数据块序号的数据块
     */
    public static byte[][] encodeInputStream(InputStream inputStream){
        try {
            return encodeByteArr(inputStream.readAllBytes(), -1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 解码
     * @param data 缺损的数据块
     * @return 包含数据块序号的数据块数组
     */
    public static byte[][] decodeByteArr(byte [][]data){
        byte[][] shards = new byte[TOTAL_SHARDS][];
        int shardSize = -1;
        boolean[] shardPresent = new boolean[TOTAL_SHARDS];

        int availableShards = 0;

        for (byte[] datum : data) {
            if (datum != null && datum.length > BYTES_IN_INT) {
                int shardIndex = bytesToInt(Arrays.copyOfRange(datum, 0, BYTES_IN_INT));

                if (shardIndex >= TOTAL_SHARDS || shardIndex < 0) {
                    continue;
                }

                if (shardPresent[shardIndex]) {
                    continue;
                }

                shards[shardIndex] = Arrays.copyOfRange(datum, 0, datum.length);

                if (shardSize == -1) {
                    shardSize = shards[shardIndex].length;
                }
                shardPresent[shardIndex] = true;

                availableShards++;
            }
        }

        if (availableShards < DATA_SHARDS) {
            throw new RuntimeException("不够数据块复原文件");
        }

        for (int i = 0; i < TOTAL_SHARDS; i++) {
            if(shards[i]==null){
                shards[i]=new byte[shardSize];
            }
        }

        // 解码
        REED_SOLOMON.decodeMissing(shards, shardPresent, BYTES_IN_INT, shardSize-BYTES_IN_INT);

        return shards;
    }


    /**
     * 获得原文件的字节数组
     * @param data 缺损的数据块
     * @return 原文件的字节数组
     */
    public static byte[] getFileByteArr(byte [][] data){
        // 打印前每个数组的前8个字节
        for (byte[] datum : data) {
            if (datum != null && datum.length > 8) {
                log.info("data: {}", Arrays.toString(Arrays.copyOfRange(datum, 0, 8)));
            }
        }

        byte[][] shards = decodeByteArr(data);

        int shardSize = shards[0].length-BYTES_IN_INT;

        byte[] result=new byte[ shardSize*DATA_SHARDS];

        int index=0;

        for(int i=0;i<DATA_SHARDS;i++){
            System.arraycopy(shards[i],BYTES_IN_INT,result,index,shardSize);
            index+=shardSize;
        }

        return result;
    }

    /**
     * 获得原文件
     * @param files 缺损的数据文件
     * @return 原文件字节数组
     */
    public static byte[] getFileByFiles(MultipartFile []files){
        byte[][] shards = getByteArrByFile(files);

        return getFileByteArr(shards);
    }

    /**
     * 对文件数组进行复原
     * @param files 缺损的数据文件
     * @return 包含数据块序号的数据块数组
     */
    public static byte[][] decodeByFiles(MultipartFile []files){
        byte[][] shards = getByteArrByFile(files);

        return decodeByteArr(shards);
    }

    /**
     * 获得原文件
     * @param inputStreams 缺损的数据文件
     * @return 原文件字节数组
     */
    public static byte[] getFileInputStreams(InputStream []inputStreams){
        byte[][] shards = getByteArrByInputStream(inputStreams);

        return getFileByteArr(shards);
    }

    /**
     * 对输入流数组进行复原
     * @param inputStreams 输入流数组
     * @return 包含数据块序号的数据块数组
     */
    public static byte[][] decodeByInputStreams(InputStream []inputStreams){
        byte[][] shards = getByteArrByInputStream(inputStreams);

        return decodeByteArr(shards);
    }

    /**
     * 获得文件的字节数组
     * @param files 文件数组
     * @return 文件的字节数组
     */
    public static byte[][] getByteArrByFile(MultipartFile []files){
        byte[][] shards = new byte[TOTAL_SHARDS][];

        for (int i = 0; i < files.length; i++) {
            if (!files[i].isEmpty()) {
                try {
                    shards[i] = files[i].getBytes();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return shards;
    }

    /**
     * 获得输入流的字节数组
     * @param inputStreams 输入流
     * @return 字节数组
     */
    public static byte[][] getByteArrByInputStream(InputStream []inputStreams){
        byte[][] shards = new byte[TOTAL_SHARDS][];

        for (int i = 0; i < inputStreams.length; i++) {
            if (inputStreams[i]!=null) {
                try {
                    shards[i] = inputStreams[i].readAllBytes();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return shards;
    }

    /**
     * int转byte数组
     * @param i int
     * @return byte数组
     */
    private static byte[] intToBytes(int i) {
        byte[] result = new byte[4];
        result[0] = (byte) (i >> 24);
        result[1] = (byte) (i >> 16);
        result[2] = (byte) (i >> 8);
        result[3] = (byte) i;
        return result;
    }

    /**
     * byte数组转int
     * @param bytes byte数组
     * @return int
     */
    private static int bytesToInt(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16) | ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
    }

}

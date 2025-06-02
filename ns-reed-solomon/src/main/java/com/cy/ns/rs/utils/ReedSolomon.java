package com.cy.ns.rs.utils;


import com.cy.ns.rs.base.CodingLoop;
import com.cy.ns.rs.base.Galois;
import com.cy.ns.rs.base.InputOutputByteTableCodingLoop;
import com.cy.ns.rs.base.Matrix;
import lombok.Getter;

/**
 * @author Haechi
 */
@Getter
public class ReedSolomon {

    /**
     * 数据块数
     */
    private final int dataShardCount;

    /**
     * 冗余块数
     */
    private final int parityShardCount;

    /**
     * 总块数
     */
    private final int totalShardCount;

    /**
     * 编码矩阵
     */
    private final Matrix matrix;

    /**
     * 编码循环算法
     */
    private final CodingLoop codingLoop;

    /**
     * 冗余编码行
     */
    private final byte [] [] parityRows;

    /**
     * 以InputOutputByteTableCodingLoop为默认循环编码算法
     */
    public static ReedSolomon create(int dataShardCount, int parityShardCount) {
        return new ReedSolomon(dataShardCount, parityShardCount, new InputOutputByteTableCodingLoop());
    }

    public ReedSolomon(int dataShardCount, int parityShardCount, CodingLoop codingLoop) {
        // 总共最多可以有256个分片，因为任何更多的分片都会导致Vandermonde矩阵中的重复行
        // 这将导致下面构建的矩阵中的重复行。那么包含重复行的任何子集都是奇异的
        if ( Galois.FIELD_SIZE < dataShardCount + parityShardCount) {
            throw new IllegalArgumentException("太多的数据块 - 最大："+Galois.FIELD_SIZE);
        }

        // 属性初始化
        this.dataShardCount = dataShardCount;
        this.parityShardCount = parityShardCount;
        this.codingLoop = codingLoop;
        this.totalShardCount = dataShardCount + parityShardCount;

        //获得编码矩阵
        matrix = buildMatrix(dataShardCount, this.totalShardCount);

        //初始化冗余矩阵
        parityRows = new byte [parityShardCount] [];

        for (int i = 0; i < parityShardCount; i++) {
            parityRows[i] = matrix.getRow(dataShardCount + i);
        }
    }

    /**
     * 生成编码矩阵
     * @param dataShards 数据块数
     * @param totalShards 总块数
     * @return 编码矩阵
     */
    private static Matrix buildMatrix(int dataShards, int totalShards) {
        // 从范德蒙矩阵开始。从理论上讲，这个矩阵可以工作，但不具有编码后数据分片不变的属性。
        Matrix vandermonde = Matrix.vandermonde(totalShards, dataShards);

        // 乘以矩阵最上面平方的逆矩阵。这将使最上面的平方成为单位矩阵，但保留了行的任何平方子集可逆的性质。
        Matrix top = vandermonde.submatrix(0, 0, dataShards, dataShards);

        return vandermonde.times(top.invert());
    }

    /**
     * 检测输入的数据和大小合不合法
     * @param shards 数据块
     * @param offset 偏移量
     * @param byteCount 字节数
     */
    private void checkBuffersAndSizes(byte [] [] shards, int offset, int byteCount) {
        // 传入的shards的块数需要等于总块数
        if (shards.length != totalShardCount) {
            throw new IllegalArgumentException("错误的数据块数: " + shards.length);
        }

        // 所有数据块的大小需要一致
        int shardLength = shards[0].length;
        for (int i = 1; i < shards.length; i++) {
            if (shards[i].length != shardLength) {
                throw new IllegalArgumentException("数据块长度不一致");
            }
        }

        // offset和byteCount必须是非负的，并且适合缓冲区。
        if (offset < 0) {
            throw new IllegalArgumentException("偏移量为负: " + offset);
        }
        if (byteCount < 0) {
            throw new IllegalArgumentException("字节数为负: " + byteCount);
        }//防止越界
        if (shardLength < offset + byteCount) {
            throw new IllegalArgumentException("数据块长度不足: " + byteCount + offset);
        }
    }

    /**
     * 进行冗余编码
     * @param shards 数据块
     * @param offset 偏移量
     * @param byteCount 字节数
     */
    public void encodeParity(byte[][] shards, int offset, int byteCount ,int needIndex) {
        // 先检查参数
        checkBuffersAndSizes(shards, offset, byteCount);

        // 创建冗余矩阵的缓冲区，其实就是本来的
        byte [] [] outputs = new byte [parityShardCount] [];

        // 由于arraycopy是浅拷贝，所以后面编码循环的算法会直接改变原数组的值
        System.arraycopy(shards, dataShardCount, outputs, 0, parityShardCount);

        // 调用编码函数
        codingLoop.codeSomeShards(
                parityRows,
                shards, dataShardCount,
                outputs, parityShardCount,
                offset, byteCount,
                needIndex
        );
    }

    /**
     * 复原数据块
     * @param shards 数据块
     * @param shardPresent 数据块是否存在
     * @param offset 偏移量
     * @param byteCount 字节数
     */
    public void decodeMissing(byte [] [] shards,
                              boolean [] shardPresent,
                              final int offset,
                              final int byteCount) {
        // 调用检测方法检测每个矩阵的大小
        checkBuffersAndSizes(shards, offset, byteCount);

        int numberPresent = 0;

        for (int i = 0; i < totalShardCount; i++) {
            if (shardPresent[i]) {
                numberPresent += 1;
            }
        }
        // 如果所有的数据块都存在，则不需要做什么
        if (numberPresent == totalShardCount) {
            return;
        }
        // 如果数据块不足dataShardCount则抛出异常
        if (numberPresent < dataShardCount) {
            throw new IllegalArgumentException("没有足够的数据块来恢复数据");
        }

        // 获得剩下来的数据块对应的编码矩阵行
        Matrix subMatrix = new Matrix(dataShardCount, dataShardCount);

        // 获得前dataShardCount个数据块; 因为只需要dataShardCount个数据块即可还原数据，减少计算
        byte [] [] subShards = new byte [dataShardCount] [];
        {
            int subMatrixRow = 0;

            for (int matrixRow = 0; matrixRow < totalShardCount && subMatrixRow < dataShardCount; matrixRow++) {
                if (shardPresent[matrixRow]) {
                    for (int c = 0; c < dataShardCount; c++) {
                        subMatrix.set(subMatrixRow, c, matrix.get(matrixRow, c));
                    }
                    subShards[subMatrixRow] = shards[matrixRow];
                    subMatrixRow += 1;
                }
            }
        }
        // 将编码矩阵求逆，为后面的复原数据块做准备
        Matrix dataDecodeMatrix = subMatrix.invert();

        // 再生出那些缺失的数据段
        // 只需要传入缺失数据块对应的编码行即可，已有的数据无需再算一遍，减少计算量
        // 因为是直接= shards[iShard]，所以后面计算出来的output的值可以直接同步到shards上，因为是直接引用对应的空间
        byte [] [] outputs = new byte [parityShardCount] [];
        byte [] [] matrixRows = new byte [parityShardCount] [];
        int outputCount = 0;
        for (int iShard = 0; iShard < dataShardCount; iShard++) {
            if (!shardPresent[iShard]) {
                outputs[outputCount] = shards[iShard];

                matrixRows[outputCount] = dataDecodeMatrix.getRow(iShard);

                outputCount += 1;
            }
        }
        //做个检测判断，能缩短一定的时间 有缺失数据块才进行计算
        if(outputCount != 0) {
            codingLoop.codeSomeShards(
                    matrixRows,
                    subShards, dataShardCount,
                    outputs, outputCount,
                    offset, byteCount, -1);
        }

        // 同样，再生出那些冗余数据段
        // 一样的只需要传入缺失冗余数据块对应的编码行即可，已有的数据无需再算一遍，减少计算量
        // 因为是直接= shards[iShard]，所以后面计算出来的output的值可以直接同步到shards上，因为是直接引用对应的空间
        outputCount = 0;
        for (int iShard = dataShardCount; iShard < totalShardCount; iShard++) {
            if (!shardPresent[iShard]) {
                outputs[outputCount] = shards[iShard];
                //这边就是对应冗余数据块对应的冗余编码矩阵的行
                matrixRows[outputCount] = parityRows[iShard - dataShardCount];

                outputCount += 1;
            }
        }
        //做个检测判断，能缩短一定的时间 有缺失数据块才进行计算
        if(outputCount != 0) {
            codingLoop.codeSomeShards(
                    matrixRows,
                    shards, dataShardCount,
                    outputs, outputCount,
                    offset, byteCount, -1);
        }
    }


}

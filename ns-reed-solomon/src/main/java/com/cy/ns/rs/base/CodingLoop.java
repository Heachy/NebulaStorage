package com.cy.ns.rs.base;

/**
 * 有限域上的计算方法
 * @author Haechi
 */
public interface CodingLoop {


    /**
     * 所有的可用的编码循环算法
     * 根据适合的情况选择最优的算法
     * 由于经过检测，InputOutputByteTableCodingLoop是最快的，所以只有这一个
     */
    CodingLoop[] ALL_CODING_LOOPS =
            new CodingLoop[] {
                    new InputOutputByteTableCodingLoop()
            };

    /** 生成冗余矩阵
     * @param matrixRows 编码矩阵
     * @param inputs 数据矩阵
     * @param inputCount 数据矩阵的行数
     * @param outputs 冗余矩阵
     * @param outputCount 冗余矩阵的行数
     * @param offset 偏移量
     * @param byteCount 字节数
     */
    void codeSomeShards(final byte [] [] matrixRows,
                        final byte [] [] inputs,
                        final int inputCount,
                        final byte [] [] outputs,
                        final int outputCount,
                        final int offset,
                        final int byteCount,
                        final int needIndex);

    /**检查冗余矩阵是否正确
     * @param matrixRows 编码矩阵
     * @param inputs 数据矩阵
     * @param inputCount 数据矩阵的行数
     * @param toCheck 需要检验的矩阵
     * @param checkCount 需要检验的矩阵的行数
     * @param offset 偏移量
     * @param byteCount 字节数
     * @param tempBuffer 临时缓冲区 用来存储生成的冗余矩阵
     * @return 是否检验成功
     */
    boolean checkSomeShards(final byte [] [] matrixRows,
                            final byte [] [] inputs,
                            final int inputCount,
                            final byte [] [] toCheck,
                            final int checkCount,
                            final int offset,
                            final int byteCount,
                            final byte [] tempBuffer);
}

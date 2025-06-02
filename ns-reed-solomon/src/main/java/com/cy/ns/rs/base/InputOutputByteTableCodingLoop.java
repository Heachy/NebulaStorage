package com.cy.ns.rs.base;

/**
 * @author Haechi
 * @apiNote 数据输入输出比特查表的循环结构
 */
public class InputOutputByteTableCodingLoop extends CodingLoopBase{
    @Override
    public void codeSomeShards(
            byte[][] matrixRows,
            byte[][] inputs, int inputCount,
            byte[][] outputs, int outputCount,
            int offset, int byteCount,
            int needIndex) {

        final byte [] [] table = Galois.MULTIPLICATION_TABLE;

        //用于创建一个局部作用域（block），这个作用域可以用来限定变量的作用范围。
        {
            //对输出数据块进行初始化赋值，减少异或次数，提高速度
            final int iInput = 0;

            final byte[] inputShard = inputs[iInput];

            for (int iOutput = 0; iOutput < outputCount; iOutput++) {
                if(needIndex > -1 && needIndex - inputCount != iOutput){
                    continue;
                }
                final byte[] outputShard = outputs[iOutput];

                final byte[] matrixRow = matrixRows[iOutput];

                final byte[] multiTableRow = table[matrixRow[iInput] & 0xFF];

                for (int iByte = offset; iByte  < offset + byteCount; iByte++) {
                    outputShard[iByte] = multiTableRow[inputShard[iByte] & 0xFF];
                }
            }
        }

        for (int iInput = 1; iInput < inputCount; iInput++) {
            final byte[] inputShard = inputs[iInput];

            for (int iOutput = 0; iOutput < outputCount; iOutput++) {
                if(needIndex > -1 && needIndex - inputCount != iOutput){
                    continue;
                }
                final byte[] outputShard = outputs[iOutput];

                final byte[] matrixRow = matrixRows[iOutput];

                final byte[] multiTableRow = table[matrixRow[iInput] & 0xFF];

                for (int iByte = offset; iByte < offset + byteCount; iByte++) {
                    outputShard[iByte] ^= multiTableRow[inputShard[iByte] & 0xFF];
                }
            }
        }
    }
    @Override
    public boolean checkSomeShards(
            byte[][] matrixRows,
            byte[][] inputs, int inputCount,
            byte[][] toCheck, int checkCount,
            int offset, int byteCount,
            byte[] tempBuffer) {

        if (tempBuffer == null) {
            return super.checkSomeShards(matrixRows, inputs, inputCount, toCheck, checkCount, offset, byteCount, null);
        }

        // 利用outputInputByteTableCodingLoop的循环结构，使用临时缓冲区来存储生成的冗余矩阵
        final byte [] [] table = Galois.MULTIPLICATION_TABLE;

        for (int iOutput = 0; iOutput < checkCount; iOutput++) {
            final byte [] outputShard = toCheck[iOutput];

            // 生成冗余矩阵 感觉都可以调用codeSomeShards方法了哈哈
            final byte[] matrixRow = matrixRows[iOutput];
            {
                final int iInput = 0;

                final byte [] inputShard = inputs[iInput];

                final byte [] multiTableRow = table[matrixRow[iInput] & 0xFF];

                for (int iByte = offset; iByte < offset + byteCount; iByte++) {
                    tempBuffer[iByte] = multiTableRow[inputShard[iByte] & 0xFF];
                }
            }
            for (int iInput = 1; iInput < inputCount; iInput++) {
                final byte [] inputShard = inputs[iInput];

                final byte [] multiTableRow = table[matrixRow[iInput] & 0xFF];

                for (int iByte = offset; iByte < offset + byteCount; iByte++) {
                    tempBuffer[iByte] ^= multiTableRow[inputShard[iByte] & 0xFF];
                }
            }
            // 检验传入的冗余矩阵是否正确
            for (int iByte = offset; iByte < offset + byteCount; iByte++) {
                if (tempBuffer[iByte] != outputShard[iByte]) {
                    return false;
                }
            }
        }

        return true;
    }
}

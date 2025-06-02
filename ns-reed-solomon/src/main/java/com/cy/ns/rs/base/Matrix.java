package com.cy.ns.rs.base;

import java.util.Arrays;

/**
 * @author Haechi
 */
public class Matrix {
    /**
     * 行
     */
    private final int rows;

    /**
     * 列
     */
    private final int columns;

    /**
     * 数据数组
     */
    private final byte[][] data;

    /**
     * 生成一个空的矩阵
     */
    public Matrix(int initRows, int initColumns) {
        this.rows = initRows;
        this.columns = initColumns;
        this.data = new byte[rows][columns];
    }

    /**
     * 通过二维数组生成矩阵
     */
    public Matrix(byte[][] initData) {
        this.rows = initData.length;
        this.columns = initData[0].length;
        data = new byte [rows] [];
        for (int r = 0; r < rows; r++) {
            // 检测是否每一行的列数都相同
            if (initData[r].length != columns) {
                throw new IllegalArgumentException("每一行的列数必须相同");
            }
            data[r] = new byte[columns];
            System.arraycopy(initData[r], 0, data[r], 0, columns);
        }
    }

    /**
     * 获得行数
     */
    public int getRows(){
        return rows;
    }

    /**
     * 获得列数
     */
    public int getColumns(){
        return columns;
    }

    /**
     * 获得指定行列的值
     */
    public byte get(int r,int c){
        if (r < 0 || rows <= r) {
            throw new IllegalArgumentException("行下标越界: " + r);
        }
        if (c < 0 || columns <= c) {
            throw new IllegalArgumentException("列下标越界: " + c);
        }
        return data[r][c];
    }

    /**
     * 设置指定行列的值
     */
    public void set(int r,int c,byte value){
        if (r < 0 || rows <= r) {
            throw new IllegalArgumentException("行下标越界: " + r);
        }
        if (c < 0 || columns <= c) {
            throw new IllegalArgumentException("列下标越界: " + c);
        }
        data[r][c]=value;
    }

    /**
     * 获得指定行
     */
    public byte [] getRow(int row) {
        byte [] result = new byte [columns];
        for (int c = 0; c < columns; c++) {
            result[c] = get(row, c);
        }
        return result;
    }

    /**
     * 交换两行
     */
    public void swapRows(int r1, int r2) {
        if (r1 < 0 || rows <= r1 || r2 < 0 || rows <= r2) {
            throw new IllegalArgumentException("Row index out of range");
        }
        byte [] tmp = data[r1];
        data[r1] = data[r2];
        data[r2] = tmp;
    }

    /**
     * 挨个去比较相不相等
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Matrix)) {
            return false;
        }
        for (int r = 0; r < rows; r++) {
            if (!Arrays.equals(data[r], ((Matrix)other).data[r])) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获得单位矩阵
     */
    public static Matrix identity(int size) {
        Matrix result = new Matrix(size, size);
        for (int i = 0; i < size; i++) {
            result.set(i, i, (byte) 1);
        }
        return result;
    }

    /**
     * 矩阵相乘
     */
    public Matrix times(Matrix right){
        // 判断是否能运算，左边的列数要等于右边的行数
        if (getColumns() != right.getRows()) {
            throw new IllegalArgumentException(
                    "左侧矩阵的列数 (" + getColumns() +") " +
                            "不等于右侧矩阵的行数 (" + right.getRows() + ")");
        }

        Matrix result = new Matrix(getRows(), right.getColumns());

        for(int r=0;r<getRows();r++){
            for (int c = 0; c < right.getColumns(); c++) {
                byte value = 0;
                for (int i = 0; i < getColumns(); i++) {
                    //使用有限域的加和成法来进行计算
                    value ^= Galois.multiply(get(r, i), right.get(i, c));
                }
                result.set(r, c, value);
            }
        }

        return result;
    }

    /**
     * 矩阵拼接
     */
    public Matrix augment(Matrix right){
        //如果两个矩阵行数不同，无法进行链接
        if (rows != right.rows) {
            throw new IllegalArgumentException("两个矩阵的行数不同");
        }
        Matrix result = new Matrix(rows, columns + right.columns);

        for (int r = 0; r < rows; r++) {
            System.arraycopy(data[r], 0, result.data[r], 0, columns);

            System.arraycopy(right.data[r], 0, result.data[r], columns, right.columns);
        }

        return result;
    }

    /**
     * 提取部分矩阵
     */
    public Matrix submatrix(int rmin, int cmin, int rmax, int cmax) {
        Matrix result = new Matrix(rmax - rmin, cmax - cmin);
        for (int r = rmin; r < rmax; r++) {
            System.arraycopy(data[r], cmin, result.data[r - rmin], 0, cmax - cmin);
        }
        return result;
    }

    /**
     * 高斯消元法
     */
    private void gaussianElimination() {
        // 使得对角线为1，下半部分为0
        for (int r = 0; r < rows; r++) {
            // 如果对角线上的元素为0，找到下面的一行非0的并进行交换
            if (data[r][r] == (byte) 0) {
                for (int rowBelow = r + 1; rowBelow < rows; rowBelow++) {
                    if (data[rowBelow][r] != 0) {
                        swapRows(r, rowBelow);
                        break;
                    }
                }
            }
            // 如果没有找到非0，说明是奇异矩阵，可以有多个解的
            if (data[r][r] == (byte) 0) {
                throw new IllegalArgumentException("Matrix is singular");
            }
            // 归一化
            if (data[r][r] != (byte) 1) {
                // 归一化要相乘的系数，通过有限域的计算获得
                byte scale = Galois.divide((byte) 1, data[r][r]);
                for (int c = 0; c < columns; c++) {
                    //将一整行都乘上这个系数
                    data[r][c] = Galois.multiply(data[r][c], scale);
                }
            }
            // 循环使得对角线下半部分为0，通过减去scale*当前行(归一化处理)
            for (int rowBelow = r + 1; rowBelow < rows; rowBelow++) {
                if (data[rowBelow][r] != (byte) 0) {
                    byte scale = data[rowBelow][r];
                    for (int c = 0; c < columns; c++) {
                        data[rowBelow][c] ^= Galois.multiply(scale, data[r][c]);
                    }
                }
            }
        }

        // 循环使得对角线上半部分为0
        for (int d = 0; d < rows; d++) {
            for (int rowAbove = 0; rowAbove < d; rowAbove++) {
                if (data[rowAbove][d] != (byte) 0) {
                    byte scale = data[rowAbove][d];
                    for (int c = 0; c < columns; c++) {
                        data[rowAbove][c] ^= Galois.multiply(scale, data[d][c]);
                    }

                }
            }
        }
    }

    /**
     * 矩阵求逆
     */
    public Matrix invert() {
        // 判断是否为方形矩阵
        if (rows != columns) {
            throw new IllegalArgumentException("Only square matrices can be inverted");
        }

        // 右边拼接一个单位矩阵
        Matrix work = augment(identity(rows));

        // 利用高斯消元法获得逆矩阵
        work.gaussianElimination();

        // 提取逆矩阵
        return work.submatrix(0, rows, columns, columns * 2);
    }

    /**
     * 生成范德蒙矩阵
     */
    public static Matrix vandermonde(int rows, int cols) {
        Matrix result = new Matrix(rows, cols);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                // 用有限域上的计算
                result.set(r, c, Galois.exp((byte) r, c));
            }
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append('[');
        for (int r = 0; r < rows; r++) {
            if (r != 0) {
                result.append(",\n");
            }
            result.append('[');
            for (int c = 0; c < columns; c++) {
                if (c != 0) {
                    result.append(", ");
                }
                result.append(data[r][c] & 0xFF);
            }
            result.append(']');
        }
        result.append(']');
        return result.toString();
    }
}

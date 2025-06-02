package com.cy.ns.rs.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Haechi
 * @apiNote 有限域上的计算方法
 */
public final class Galois {

    /**
     * 有限域大小
     */
    public static final int FIELD_SIZE =256 ;

    /**
     * 生成多项式
     */
    public static final int GENERATING_POLYNOMIAL = 29;

    /**
     * 对数表
     * 可以通过generateLogTable方法生成
     */
    public static final short[] LOG_TABLE = generateLogTable(GENERATING_POLYNOMIAL);

    /**
     * 幂值表
     */
    public static final byte [] EXP_TABLE = generateExpTable();

    /**
     * 乘法表
     */
    public static byte [] [] MULTIPLICATION_TABLE = generateMultiplicationTable();

    /**
     * 生成对数表
     * @param polynomial 生成多项式
     * @return 对数表
     */
    public static short[] generateLogTable(int polynomial){

        // result中存放的是查询一个值的对数是多少 result[查询的值]= 对数->几次方;以2为底
        short[] result = new short[FIELD_SIZE];

        // 标记是否已经有值
        Arrays.fill(result, (short) -1);

        // 从1开始，因为0没有对数
        int b= 1;

        for(int log=0;log<FIELD_SIZE-1;log++){

            // 检测是否会重复
            if(result[b]!=-1){
                throw new RuntimeException("该生成多项式不符合要求");
            }

            result[b] = (short) log;

            b =(b << 1);

            // 保证b处于有限域内且不重复
            if(b >= FIELD_SIZE){
                b = (b-FIELD_SIZE)^polynomial;
            }
        }
        return result;
    }

    /**
     * 所有生成多项式的值
     */
    public static Integer[] allPossiblePolynomials(){
        List<Integer> list =new ArrayList<>();
        for(int i=1;i<FIELD_SIZE;i++){
            // 通过检测没有异常去获得生成多项式
            // 生成多项式的值通常是经过研究和测试确定的，而不是通过某种公式计算得出的。
            try {
                generateLogTable(i);
                list.add(i);
            }catch (Exception ignored) {
            }
        }
        return list.toArray(new Integer[0]);
    }

    /**
     * 生成幂值表
     */
    public static byte[] generateExpTable(){
        // result存放的是查询一个对应对数的幂值的result[查询值]=2^(查询值) 次幂运算
        // 设置两倍的大小，方便乘法查值的时候不用考虑范围
        byte [] result = new byte[FIELD_SIZE*2-2];

        for(int i=1;i<FIELD_SIZE;i++){

            // 获得i的对数
            // 可以理解为查表，也可以理解为遍历
            int log = LOG_TABLE[i];

            result[log] = (byte) i;

            result[log+FIELD_SIZE-1] = (byte) i;
        }

        return result;
    }

    /**乘法
     * @param a 乘数a
     * @param b 乘数b
     * @return 结果
     */
    public static byte multiply(byte a,byte b){
        // 如果有一个为0，那么结果为0
        if(a==0||b==0){
            return 0;
        }else{
            // 指数相加运算
            // 通过对数表查找对应的幂值
            int logA = LOG_TABLE[a & 0xFF];
            int logB = LOG_TABLE[b & 0xFF];
            int logResult = logA+logB;
            return EXP_TABLE[logResult];
        }
    }

    /**除法
     * @param a 被除数a
     * @param b 除数b
     * @return 结果
     */
    public static byte divide(byte a,byte b){
        // 被除数为0，结果为0
        if (a==0){
            return 0;
        }else if(b==0){
            // 除数为0的异常
            throw new IllegalArgumentException("Argument 'divisor' is 0");
        }else{
            // 指数相减运算
            // 通过对数表查找对应的幂值
            int logA = LOG_TABLE[a & 0xFF];
            int logB = LOG_TABLE[b & 0xFF];
            int logResult = logA-logB;
            if(logResult<0){
                logResult+=FIELD_SIZE-1;
            }
            return EXP_TABLE[logResult];
        }
    }

    /**
     * 幂运算
     * @param a 底数
     * @param n 幂数
     * @return 结果
     */
    public static byte exp(byte a,int n){
        // 0的任何次幂都是1
        if (n == 0) {
            return 1;
        }// 0的任何>1次幂都是0
        else if (a == 0) {
            return 0;
        }else{
            // 利用对数和查表简化运算
            // a & 0xFF是为了防止a为负数
            // byte是有符号的，所以需要转换为无符号的
            int logA = LOG_TABLE[a & 0xFF];
            int logResult = logA * n;
            logResult %= FIELD_SIZE-1;
            return EXP_TABLE[logResult];
        }
    }

    /**
     * 生成乘法表
     */
    public static byte[][] generateMultiplicationTable(){
        byte [] [] result = new byte [FIELD_SIZE] [FIELD_SIZE];
        for (int a = 0; a < FIELD_SIZE; a++) {
            for (int b = 0; b < FIELD_SIZE; b++) {
                result[a][b] = multiply((byte) a, (byte) b);
            }
        }
        return result;
    }
}

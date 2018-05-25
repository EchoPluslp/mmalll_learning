package com.mmall.util;

import java.math.BigDecimal;

/**
 * Bigdecimal的工具类
 *
 * @author Liupeng
 * @createTime 2018-05-11 10:07
 *
 *注意：当使用商业计算时，需要使用BigDecimal的String类型的构造器方法。
 * 科研计算可以使用float或者double计算的数据类型
 **/
public class BigDecimalUtil {
    private BigDecimalUtil(){

    }

    //加法

    public static BigDecimal add(double v1,double v2){
        BigDecimal bigDecimal1 = new BigDecimal(Double.toString(v1));
        BigDecimal bigDecimal2 = new BigDecimal(Double.toString(v2));
        return  bigDecimal1.add(bigDecimal2);
    }
    //减法
    public static BigDecimal sub(double v1,double v2){
        BigDecimal bigDecimal1 = new BigDecimal(Double.toString(v1));
        BigDecimal bigDecimal2 = new BigDecimal(Double.toString(v2));
        return  bigDecimal1.subtract(bigDecimal2);
    }
    //乘法
    public static BigDecimal mul(double v1,double v2){
        BigDecimal bigDecimal1 = new BigDecimal(Double.toString(v1));
        BigDecimal bigDecimal2 = new BigDecimal(Double.toString(v2));
        return  bigDecimal1.multiply(bigDecimal2);
    }
    //除法
    public static BigDecimal div(double v1,double v2){
        BigDecimal bigDecimal1 = new BigDecimal(Double.toString(v1));
        BigDecimal bigDecimal2 = new BigDecimal(Double.toString(v2));
        return  bigDecimal1.divide(bigDecimal2,2,BigDecimal.ROUND_HALF_UP);
        //除不尽的情况下，保留2为小数，并采用四舍五入的形式
    }
}
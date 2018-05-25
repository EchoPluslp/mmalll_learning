package com.mall.test;

import org.junit.Test;

import java.math.BigDecimal;

/**
 * BigDecimal测试类
 *
 * @author Liupeng
 * @createTime 2018-05-11 9:49
 *
 * 0.060000000000000005
0.5800000000000001
10.625
1.2329999999999999
0.06000000000000000298372437868010820238851010799407958984375
0.06
 打印结果为上面，
    可以得出，如果进行商业计算精度问题，需要使用Bigdecimal的String类型的构造器方法！！！
 **/
public class BigDecimalTest {
    @Test
    public void test1(){
        System.out.println(0.05+0.01);
        System.out.println(1.0-0.42);
        System.out.println(4.25*2.5);
        System.out.println(123.3/100);
    }
    @Test
    public void test2(){
        BigDecimal bigDecimal1 = new BigDecimal(0.05);
        BigDecimal bigDecimal2 = new BigDecimal(0.01);
        System.out.println(bigDecimal1.add(bigDecimal2));
    }

    @Test
    public void test3(){
        BigDecimal bigDecimal1 = new BigDecimal("0.05");
        BigDecimal bigDecimal2 = new BigDecimal("0.01");
        System.out.println(bigDecimal1.add(bigDecimal2));
    }
}
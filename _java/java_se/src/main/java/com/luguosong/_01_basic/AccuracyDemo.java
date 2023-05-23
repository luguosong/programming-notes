package com.luguosong._01_basic;

import java.math.BigDecimal;

/**
 * 浮点数精度问题
 *
 * @author luguosong
 */
public class AccuracyDemo {
    public static void main(String[] args) {
        //浮点数计算存在精度问题
        System.out.println(0.1 + 0.2); //0.30000000000000004

        //可以使用java.math.BigDecimal类来解决精度问题
        System.out.println(new BigDecimal("0.1").add(new BigDecimal("0.2"))); //0.3
    }
}

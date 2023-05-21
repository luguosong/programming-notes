package com.luguosong._01_basic;

import com.luguosong.util.TimeTool;

/**
 * 求斐波那契数
 *
 * @author luguosong
 */
public class Fibonacci {
    public static void main(String[] args) {
        TimeTool.check("使用迭代计算斐波那契数", () -> System.out.println(getFibonacci1(40)));
        TimeTool.check("使用迭代计算斐波那契数", () -> System.out.println(getFibonacci2(40)));
    }

    /**
     * 递归方式求斐波那契数
     * 代码复杂度：O(2^n)
     *
     * @param n
     * @return
     */
    public static int getFibonacci1(int n) {
        if (n <= 1)
            return n;
        return getFibonacci1(n - 1) + getFibonacci1(n - 2);
    }

    /**
     * 代码复杂度： O(n)
     *
     * @param n
     * @return
     */
    public static int getFibonacci2(int n) {
        if (n <= 1)
            return n;
        int a = 0;
        int b = 1;
        int c = 0;
        for (int i = 2; i <= n; i++) {
            c = a + b;
            a = b;
            b = c;
        }
        return c;
    }
}

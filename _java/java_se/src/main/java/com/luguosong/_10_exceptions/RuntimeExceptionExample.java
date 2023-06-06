package com.luguosong._10_exceptions;

/**
 * 运行时异常
 *
 * @author luguosong
 */
public class RuntimeExceptionExample {
    public static void main(String[] args) {
        //即使存在异常，也可以编译通过
        int[] ints = {1, 2, 3, 4, 5};
        System.out.println(ints[10]);

        //运行时异常，不需要显示处理
        test();
    }

    /**
     * 抛出运行时异常
     */
    public static void test() {
        throw new RuntimeException("test");
    }
}

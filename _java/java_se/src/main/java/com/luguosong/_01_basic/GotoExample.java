package com.luguosong._01_basic;

/**
 * @author luguosong
 */
public class GotoExample {
    public static void main(String[] args) {
        boolean condition = true;

        outerLoop:
        for (int i = 1; i <= 3; i++) {
            for (int j = 1; j <= 3; j++) {
                System.out.println("i = " + i + ", j = " + j);
                if (condition) {
                    break outerLoop;
                }
            }
        }

        System.out.println("循环结束");
    }
}


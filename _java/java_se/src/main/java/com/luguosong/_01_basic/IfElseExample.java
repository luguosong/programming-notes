package com.luguosong._01_basic;

/**
 * if-else
 *
 * @author luguosong
 */
public class IfElseExample {
    public static void main(String[] args) {
        int number = 10;

        if (number > 0) {
            System.out.println("该数字是正数。");
        } else if (number < 0) {
            System.out.println("该数字是负数。");
        } else {
            System.out.println("该数字是零。");
        }
    }
}

package com.luguosong._02_arrays;

import java.util.Arrays;

/**
 * 数组复制
 *
 * @author luguosong
 */
public class ArrayCopyDemo {
    public static void main(String[] args) {
        String[] copyFrom = {
                "Affogato", "Americano", "Cappuccino", "Corretto", "Cortado",
                "Doppio", "Espresso", "Frappucino", "Freddo", "Lungo", "Macchiato",
                "Marocchino", "Ristretto"};

        /*
         * 复制方法一
         *
         * 参数一：源数组
         * 参数二：源数组开始位置
         * 参数三：目标数组
         * 参数四：目标数组开始位置
         * 参数五：复制长度
         * */
        String[] copyTo1 = new String[7];
        System.arraycopy(copyFrom, 2, copyTo1, 0, 7);
        for (String coffee : copyTo1) {
            System.out.print(coffee + " ");
        }

        System.out.println(" ");

        /*
         * 复制方法二
         *
         * 参数一：源数组
         * 参数二：源数组开始位置
         * 参数三：目标数组
         * 返回值：复制后的数组
         * */
        String[] copyTo2 = Arrays.copyOfRange(copyFrom, 2, 9);
        for (String coffee : copyTo2) {
            System.out.print(coffee + " ");
        }
    }
}

package com.luguosong._02_arrays;

/**
 * @author luguosong
 */
public class ArrayExample2 {
    public static void main(String[] args) {
        int[] array = {1, 2, 3, 4};

        /*
         * 元素赋值
         * */
        array[0] = 10;
        array[1] = 20;

        /*
         * 元素调用
         * */
        System.out.println("第一个元素：" + array[0]);
        System.out.println("第二个元素：" + array[1]);

        /*
         * 数组长度
         * */
        System.out.println("数组长度：" + array.length);

        /*
         * 遍历数组
         * */
        for (int i = 0; i < array.length; i++) {
            System.out.println("第" + (i + 1) + "个元素：" + array[i]);
        }
    }
}

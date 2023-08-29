package com.luguosong._02_basic;

/**
 * @author luguosong
 */
public class ArrayExample {
    public static void main(String[] args) {
        // 数组的声明
        int[] arr1 = new int[10];
        int[] arr2 = {1, 2, 3, 4, 5};
        int[] arr3 = new int[]{1, 2, 3, 4, 5};

        // 数组元素调用
        System.out.println(arr2[0]); // 1
        System.out.println(arr1[0]); // 默认值为0

        // 数组的长度
        System.out.println(arr2.length); // 5

        // 数组的遍历
        for (int j : arr2) {
            System.out.println(j);
        }
    }
}

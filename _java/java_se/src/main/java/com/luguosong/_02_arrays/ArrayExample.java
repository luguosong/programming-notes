package com.luguosong._02_arrays;

/**
 * 数组初始化和声明
 *
 * @author luguosong
 */
public class ArrayExample {
    public static void main(String[] args) {
        /*
         * 数组声明+初始化
         *
         * 一旦初始化完成，其长度就确定了，且不可更改
         * */
        int[] array1 = new int[5]; //方式一
        int[] array2 = new int[]{1, 2, 3, 4, 5}; //方式二
        int[] array3 = {1, 2, 3, 4, 5}; //方式三：类型推断：这种方式只有在声明时可以使用
        //array3 = {1, 2, 3}; //报错，不能在赋值时使用

        int array4[] = new int[5]; //使用c样式声明，不推荐👎
    }
}

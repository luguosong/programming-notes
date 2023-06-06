package com.luguosong._02_arrays;

/**
 * 二维数组
 *
 * @author luguosong
 */
public class TwoDimensionalArrayExample {

    public static void main(String[] args) {
        /*
         * 二维数组的声明
         * */
        //{% raw %}
        int[][] array1 = new int[][]{{1, 2, 3}, {4, 5, 6}, {7, 8, 9}, {10, 11, 12}};
        int[][] array2 = new int[4][3]; //{{0,0,0},{0,0,0},{0,0,0},{0,0,0}}
        int[][] array3 = new int[4][]; //{null,null,null,null}
        int[][] array4 = {{1, 2, 3}, {4, 5, 6}, {7, 8}, {9, 10, 11, 12}};
        //{% endraw %}

        //遍历数组
        for (int[] ints : array4) {
            for (int anInt : ints) {
                System.out.print(anInt + " ");
            }
            System.out.println();
        }
    }

}

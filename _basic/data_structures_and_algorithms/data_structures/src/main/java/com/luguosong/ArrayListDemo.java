package com.luguosong;

import com.luguosong.util.ArrayList;
import com.luguosong.util.TimeTool;

import java.util.Random;

/**
 * @author luguosong
 */
public class ArrayListDemo {
    public static void main(String[] args) {
        ArrayList<Integer> array = new ArrayList<>();

        TimeTool.check("添加元素", () -> {
            for (int i = 0; i < 100; i++) {
                array.add(new Random().nextInt(100));
            }
            System.out.println(array);
        });

        TimeTool.check("删除元素", () -> {
            array.remove(99);
            array.remove(50);
            array.remove(0);
            System.out.println(array);
        });



        TimeTool.check("清空元素", () -> {
            array.clear();
            System.out.println(array);
        });
    }
}

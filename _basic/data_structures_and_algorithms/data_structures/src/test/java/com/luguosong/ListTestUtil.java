package com.luguosong;


import com.luguosong.util.TimeTool;

import java.util.List;

/**
 * @author luguosong
 */
public class ListTestUtil {
    public static void run(List<Integer> list) {
        /*
         * 增加测试
         * */
        //先初始化一些元素进去
        for (int i = 0; i < 100000; i++) {
            list.add(0);
        }

        TimeTool.check("向末尾增加元素", () -> {
            list.add(22);
            System.out.println("元素个数:" + list.size());
        });


        TimeTool.check("向中间增加元素", () -> {
            list.add(5, 11);
            System.out.println("元素个数:" + list.size());
        });

        /*
         * 查询测试
         * */
        System.out.println("是否包含11：" + (list.contains(11) ? "是" : "否"));
        System.out.println("是否为空：" + (list.isEmpty() ? "是" : "否"));
        System.out.println("元素个数：" + list.size());
        System.out.println("获取第五个元素：" + list.get(5));
        System.out.println("11是第几个元素：" + list.indexOf(11));



        /*
         * 修改测试
         * */
        TimeTool.check("修改元素", () -> {
            list.set(5, 33);
        });

        /*
         * 删除测试
         * */
        TimeTool.check("删除元素", () -> {
            list.remove(5);
        });
        TimeTool.check("清空元素", list::clear);
    }
}

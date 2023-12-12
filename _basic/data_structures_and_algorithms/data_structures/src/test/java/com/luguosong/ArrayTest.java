package com.luguosong;

import org.junit.jupiter.api.Test;

/**
 * 数组测试
 *
 * @author luguosong
 */
public class ArrayTest {
    @Test
    public void test(){
        int[] arrays = new int[]{111, 222, 333};

        //增:数组在创建后长度就固定了，无法再增加元素

        //删：数组在创建后长度就固定了，无法再删除元素

        //改：根据下标修改元素
        arrays[1] = 555;

        //查：根据下标查询元素
        System.out.println(arrays[1]);
    }
}

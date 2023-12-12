package com.luguosong;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

/**
 * 动态数组测试
 *
 * @author luguosong
 */
public class ArrayListTest {
    /**
     * 测试JDK自带的动态数组
     */
    @Test
    public void testJavaLangArrayList(){
        ListTestUtil.run(new ArrayList<>());
    }

    /**
     * 测试自定义的动态数组
     */
    @Test
    public void testMyArrayList(){
        ListTestUtil.run(new com.luguosong.util.list.ArrayList<>());
    }

}

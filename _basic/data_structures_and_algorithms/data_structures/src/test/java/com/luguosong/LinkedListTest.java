package com.luguosong;

import com.luguosong.util.list.LinkedList;
import org.junit.jupiter.api.Test;

/**
 * @author luguosong
 */
public class LinkedListTest {

    @Test
    public void test() {
        LinkedList<Integer> list = new LinkedList<Integer>();
        list.add(11);
        list.add(33);
        list.add(1, 22);
        list.remove(1);
        System.out.println(list.size());
        System.out.println(list.indexOf(11));
        System.out.println(list);
    }

    /**
     * 测试JDK自带的链表
     */
    @Test
    public void testJavaLangLinkedList() {
        ListTestUtil.run(new java.util.LinkedList<Integer>());
    }

    /**
     * 测试自己实现的链表
     */
    @Test
    public void testLinkedList() {
        ListTestUtil.run(new LinkedList<Integer>());
    }
}

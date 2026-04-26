package com.example.behavioral.iterator;

import java.util.ArrayList;
import java.util.List;

/**
 * 迭代器模式 - 反例
 * 问题：客户端需要了解集合内部结构（数组索引 vs 链表 next），换数据结构则遍历代码全改
 */
public class IteratorBadExample {
    public static void main(String[] args) {
        // ❌ 数组：用下标遍历
        String[] arrayBooks = {"设计模式", "重构", "代码整洁之道"};
        System.out.println("数组遍历（依赖内部结构）：");
        for (int i = 0; i < arrayBooks.length; i++) { // 必须知道是数组 ❌
            System.out.println("  " + arrayBooks[i]);
        }

        // ❌ LinkedList：必须用 java.util.Iterator（不同接口）
        java.util.LinkedList<String> linkedBooks = new java.util.LinkedList<>();
        linkedBooks.add("敏捷软件开发");
        linkedBooks.add("领域驱动设计");
        java.util.Iterator<String> it = linkedBooks.iterator();
        System.out.println("链表遍历（依赖 Iterator 接口）：");
        while (it.hasNext()) {
            System.out.println("  " + it.next());
        }
        // 如果把 arrayBooks 换成 LinkedList，上面的 for(int i...) 代码全部要改 ❌
    }
}

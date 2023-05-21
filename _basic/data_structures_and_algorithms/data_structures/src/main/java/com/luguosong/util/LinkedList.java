package com.luguosong.util;

/**
 * 链表实现
 *
 * @author luguosong
 */
public class LinkedList<E> implements List<E>{

    private int size;
    private Node<E> firstNode;

    /**
     * 添加元素到最后面
     *
     * @param e 元素
     */
    @Override
    public void add(E e) {

    }

    /**
     * 往index位置添加元素
     *
     * @param index 索引
     * @param e     元素
     */
    @Override
    public void add(int index, E e) {

    }

    /**
     * 删除index位置对应的元素
     *
     * @param index 索引
     * @return 被删除的元素
     */
    @Override
    public E remove(int index) {
        return null;
    }

    /**
     * 清除所有元素
     */
    @Override
    public void clear() {

    }

    /**
     * 设置index位置的元素
     *
     * @param index   索引
     * @param element 元素
     * @return 被替换的元素
     */
    @Override
    public E set(int index, int element) {
        return null;
    }

    /**
     * 返回index位置对应元素
     *
     * @param index 索引
     * @return 元素
     */
    @Override
    public E get(int index) {
        return null;
    }

    /**
     * 查看元素的索引
     *
     * @param element 元素
     * @return 索引
     */
    @Override
    public int indexOf(E element) {
        return 0;
    }

    /**
     * 是否包含某个元素
     *
     * @param element 元素
     * @return 是否包含
     */
    @Override
    public Boolean contains(E element) {
        return null;
    }

    /**
     * 元素数量
     *
     * @return 元素数量
     */
    @Override
    public int size() {
        return 0;
    }

    /**
     * 是否为空
     *
     * @return 是否为空
     */
    @Override
    public Boolean isEmpty() {
        return null;
    }

    /**
     * 链表节点
     */
    private static class Node<E> {
        E element;
        Node<E> next;

        Node(E element, Node<E> next) {
            this.element = element;
            this.next = next;
        }
    }
}

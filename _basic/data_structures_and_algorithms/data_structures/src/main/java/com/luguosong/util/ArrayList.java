package com.luguosong.util;

import java.util.Arrays;

/**
 * 动态数组实现
 *
 * @author luguosong
 */
public class ArrayList<E> implements List<E> {

    /**
     * 元素数量
     * 注意：size并非elements的数组容量，而是数组中实际有多少个元素
     */
    private int size;

    /**
     * 所有元素
     */
    private Object[] elements;

    /**
     * 默认容量
     */
    private static final int DEFAULT_CAPACITY = 10;

    /**
     * 有参构造
     *
     * @param capacity 容量
     */
    public ArrayList(int capacity) {
        //容量小于默认容量，使用默认容量
        if (capacity < DEFAULT_CAPACITY)
            capacity = DEFAULT_CAPACITY;
        elements = new Object[capacity];
    }

    /**
     * 无参构造
     */
    public ArrayList() {
        this(DEFAULT_CAPACITY);
    }


    /**
     * 添加元素到最后面
     *
     * @param e 元素
     */
    @Override
    public void add(E e) {
        add(size, e);
    }


    /**
     * 往index位置添加元素
     *
     * @param index 索引
     * @param e     元素
     */
    @Override
    public void add(int index, E e) {
        //判断index是否越界
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("Index:" + index + ",Size:" + size);
        //判断是否需要扩容
        ensureCapacity(size + 1);
        //将index位置后面的元素都往后移动一位
        for (int i = size - 1; i >= index; i--) {
            elements[i + 1] = elements[i];
        }
        //将index位置设置为e
        elements[index] = e;
        //元素数量+1
        size++;
    }


    /**
     * 删除index位置对应的元素
     *
     * @param index 索引
     * @return 被删除的元素
     */
    @Override
    public E remove(int index) {
        //判断index是否越界
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("Index:" + index + ",Size:" + size);
        //获取index位置对应的元素
        E old = (E) elements[index];
        //将index位置后面的元素都往前移动一位
        for (int i = index + 1; i < size; i++) {
            elements[i - 1] = elements[i];
        }
        //将最后一位元素置空
        elements[--size] = null;
        return old;
    }


    /**
     * 清除所有元素
     */
    @Override
    public void clear() {
        //将所有元素置空
        Arrays.fill(elements, null);
        //元素数量置为0
        size = 0;
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
        //判断index是否越界
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("Index:" + index + ",Size:" + size);
        //设置index位置的元素
        E old = (E) elements[index];
        elements[index] = element;
        return old;
    }


    /**
     * 返回index位置对应元素
     *
     * @param index 索引
     * @return 元素
     */
    @Override
    public E get(int index) {
        //判断index是否越界
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("Index:" + index + ",Size:" + size);
        //返回index位置对应元素
        return (E) elements[index];
    }


    /**
     * 查看元素的索引
     *
     * @param element 元素
     * @return 索引
     */
    @Override
    public int indexOf(E element) {
        //
        if (elements == null) {
            for (int i = 0; i < size; i++) {
                if (element == null)
                    return i;
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (elements[i].equals(element))
                    return i;
            }
        }
        return -1;
    }

    /**
     * 是否包含某个元素
     *
     * @param element 元素
     * @return 是否包含
     */
    @Override
    public Boolean contains(E element) {
        return indexOf(element) != -1;
    }


    /**
     * 元素数量
     *
     * @return
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * 是否为空
     *
     * @return 是否为空
     */
    @Override
    public Boolean isEmpty() {
        return size == 0;
    }

    /**
     * 扩容
     *
     * @param size 数组容量
     */
    private void ensureCapacity(int size) {
        if (elements.length < size) {
            int oldLength = elements.length;
            //创建新数组,容量为旧数组的1.5倍
            Object[] newElements = new Object[elements.length + (elements.length >> 1)];
            //将旧数组中的值转移到新数组
            System.arraycopy(elements, 0, newElements, 0, elements.length);
            //将elements指向新数组
            elements = newElements;
            System.out.println("数组从" + oldLength + "扩容到" + elements.length);
        }
    }

    /**
     * 重写toString方法
     *
     * @return 字符串
     */
    @Override
    public String toString() {
        return "ArrayList{" +
                "size=" + size +
                ", elements=" + Arrays.toString(elements) +
                '}';
    }
}

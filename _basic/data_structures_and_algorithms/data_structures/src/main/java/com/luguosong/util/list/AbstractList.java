package com.luguosong.util.list;

/**
 * 实现线性表中的相同代码
 *
 * @author luguosong
 */
public abstract class AbstractList<E> extends UnimplementedList<E> {

    /**
     * 实际元素数量
     */
    protected int size;

    /**
     * 获取元素数量
     *
     * @return 此列表中元素的个数
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * 判断是否为空
     *
     * @return 如果不是空返回true
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * 判断是否包含指定元素
     *
     * @param o 元素
     * @return 是否包含
     */
    @Override
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

    /**
     * 向末尾添加元素
     *
     * @param e 元素
     * @return 返回true
     */
    @Override
    public boolean add(E e) {
        add(size, e);
        return true;
    }

}

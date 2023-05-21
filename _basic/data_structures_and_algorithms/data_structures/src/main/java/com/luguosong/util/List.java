package com.luguosong.util;

/**
 * @author luguosong
 */
public interface List<E> {

    /**
     * 添加元素到最后面
     *
     * @param e 元素
     */
    public void add(E e);


    /**
     * 往index位置添加元素
     *
     * @param index 索引
     * @param e     元素
     */
    public void add(int index, E e);


    /**
     * 删除index位置对应的元素
     *
     * @param index 索引
     * @return 被删除的元素
     */
    public E remove(int index);


    /**
     * 清除所有元素
     */
    public void clear();

    /**
     * 设置index位置的元素
     *
     * @param index   索引
     * @param element 元素
     * @return 被替换的元素
     */
    public E set(int index, int element);


    /**
     * 返回index位置对应元素
     *
     * @param index 索引
     * @return 元素
     */
    public E get(int index);


    /**
     * 查看元素的索引
     *
     * @param element 元素
     * @return 索引
     */
    public int indexOf(E element);

    /**
     * 是否包含某个元素
     *
     * @param element 元素
     * @return 是否包含
     */
    public Boolean contains(E element);


    /**
     * 元素数量
     *
     * @return 元素数量
     */
    public int size();

    /**
     * 是否为空
     *
     * @return 是否为空
     */
    public Boolean isEmpty();

}

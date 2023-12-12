package com.luguosong.util.list;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * 过滤掉自定义List不实现的接口
 * 让关键代码更清爽
 *
 * @author luguosong
 */
public abstract class UnimplementedList<E> implements List<E> {
    @Override
    public Iterator<E> iterator() {
        throw new RuntimeException("该接口未实现");
    }

    @Override
    public Object[] toArray() {
        throw new RuntimeException("该接口未实现");
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new RuntimeException("该接口未实现");
    }


    @Override
    public boolean remove(Object o) {
        throw new RuntimeException("该接口未实现");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new RuntimeException("该接口未实现");
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new RuntimeException("该接口未实现");
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        throw new RuntimeException("该接口未实现");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new RuntimeException("该接口未实现");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new RuntimeException("该接口未实现");
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new RuntimeException("该接口未实现");
    }

    @Override
    public ListIterator<E> listIterator() {
        throw new RuntimeException("该接口未实现");
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        throw new RuntimeException("该接口未实现");
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        throw new RuntimeException("该接口未实现");
    }
}

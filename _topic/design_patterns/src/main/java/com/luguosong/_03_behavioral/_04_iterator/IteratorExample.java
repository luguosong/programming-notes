package com.luguosong._03_behavioral._04_iterator;

/**
 * 迭代器模式
 *
 * @author luguosong
 */
public class IteratorExample {
    public static void main(String[] args) {
        String[] names = {"Alice", "Bob", "Charlie", "Dave"};
        Collection<String> collection = new ConcreteCollection<>(names);
        Iterator<String> iterator = collection.createIterator();

        while (iterator.hasNext()) {
            String name = iterator.next();
            System.out.println(name);
        }
    }

    // 迭代器接口
    static interface Iterator<T> {
        boolean hasNext();

        T next();
    }

    // 具体迭代器类
    static class ConcreteIterator<T> implements Iterator<T> {
        private final T[] collection;
        private int position;

        public ConcreteIterator(T[] collection) {
            this.collection = collection;
            this.position = 0;
        }

        public boolean hasNext() {
            return position < collection.length;
        }

        public T next() {
            T item = collection[position];
            position++;
            return item;
        }
    }

    // 集合接口
    static interface Collection<T> {
        Iterator<T> createIterator();
    }

    // 具体集合类
    static class ConcreteCollection<T> implements Collection<T> {
        private final T[] collection;

        public ConcreteCollection(T[] collection) {
            this.collection = collection;
        }

        public Iterator<T> createIterator() {
            return new ConcreteIterator<>(collection);
        }
    }
}

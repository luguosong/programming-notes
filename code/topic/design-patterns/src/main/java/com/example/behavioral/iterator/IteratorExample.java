package com.example.behavioral.iterator;

import java.util.ArrayList;
import java.util.List;

/**
 * 迭代器模式 - 正例
 * 自定义 Iterator 接口，客户端通过统一接口遍历，不依赖集合内部结构
 */
public class IteratorExample {
    public static void main(String[] args) {
        ProductCatalog catalog = new ProductCatalog();
        catalog.add(new Product("设计模式", 59.9));
        catalog.add(new Product("重构",     69.9));
        catalog.add(new Product("代码整洁之道", 49.9));

        // ✅ 客户端只依赖 Iterator 接口，不关心内部是数组还是链表
        Iterator<Product> it = catalog.iterator();
        System.out.println("商品列表：");
        while (it.hasNext()) {
            Product p = it.next();
            System.out.println("  " + p.getName() + " - ¥" + p.getPrice());
        }

        // ✅ 内部换成 LinkedList，客户端代码不用改（多态）
    }
}

// 产品领域对象
class Product {
    private final String name;
    private final double price;
    public Product(String name, double price) { this.name = name; this.price = price; }
    public String getName()  { return name;  }
    public double getPrice() { return price; }
}

// 自定义迭代器接口（与 java.util.Iterator 同名但在此包下）
interface Iterator<T> {
    boolean hasNext();
    T       next();
}

// 自定义可迭代接口
interface Iterable<T> {
    Iterator<T> iterator();
}

// 具体集合：商品目录
class ProductCatalog implements Iterable<Product> {
    private final List<Product> products = new ArrayList<>();

    public void add(Product product) { products.add(product); }

    @Override
    public Iterator<Product> iterator() {
        return new ProductIterator(products);
    }

    // 内部迭代器实现
    private static class ProductIterator implements Iterator<Product> {
        private final List<Product> list;
        private int index = 0;

        ProductIterator(List<Product> list) { this.list = list; }

        @Override public boolean   hasNext() { return index < list.size(); }
        @Override public Product   next()    { return list.get(index++);   }
    }
}

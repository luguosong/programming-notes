package com.luguosong._01_creational._01_simple_factory;

/**
 * @author luguosong
 */
public class ConcreteProductA implements Product {
    @Override
    public void operation() {
        System.out.println("具体产品A的操作");
    }
}

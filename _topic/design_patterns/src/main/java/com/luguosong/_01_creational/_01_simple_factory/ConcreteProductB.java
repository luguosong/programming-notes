package com.luguosong._01_creational._01_simple_factory;

/**
 * @author luguosong
 */
public class ConcreteProductB implements Product {
    /**
     * 具体产品角色B
     */
    @Override
    public void operation() {
        System.out.println("具体产品B的操作");
    }
}

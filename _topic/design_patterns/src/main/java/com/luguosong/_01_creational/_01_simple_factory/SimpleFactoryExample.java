package com.luguosong._01_creational._01_simple_factory;

/**
 * @author luguosong
 */
public class SimpleFactoryExample {
    public static void main(String[] args) {
        // 使用工厂方法创建具体产品对象
        Product productA = Factory.factoryMethod("projectA");
        Product productB = Factory.factoryMethod("projectB");

        // 调用产品对象的方法
        productA.operation();
        productB.operation();
    }

    // 抽象产品角色
    static interface Product {
        public abstract void operation();
    }

    // 具体产品角色A
    static class ConcreteProductA implements Product {
        @Override
        public void operation() {
            System.out.println("具体产品A的操作");
        }
    }

    // 具体产品角色B
    static class ConcreteProductB implements Product {
        @Override
        public void operation() {
            System.out.println("具体产品B的操作");
        }
    }

    // 简单工厂角色
    static class Factory {

        /**
         * 静态工厂方法
         * 一般简单工厂的方法是静态的，所以也称为静态工厂方法
         *
         * @param type
         * @return
         */
        public static Product factoryMethod(String type) {
            if (type.equals("projectA")) {
                return new ConcreteProductA();
            } else if (type.equals("projectB")) {
                return new ConcreteProductB();
            }
            throw new IllegalArgumentException("无效的产品类型");
        }
    }
}

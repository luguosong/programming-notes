package com.luguosong._01_creational._04_factory_method;

/**
 * @author luguosong
 */
public class FactoryMethodExample {
    public static void main(String[] args) {
        // 创建具体创建者A对象
        Creator creatorA = new ConcreteCreatorA();
        Product product = creatorA.createProduct();
        product.operation();
        
        // 创建具体创建者B对象
        Creator creatorB = new ConcreteCreatorB();
        creatorB.someOperation();
    }

    // 产品接口
    static interface Product {
        void operation();
    }

    // 具体产品实现
    static class ConcreteProductA implements Product {
        @Override
        public void operation() {
            System.out.println("Concrete Product A operation");
        }
    }

    static class ConcreteProductB implements Product {
        @Override
        public void operation() {
            System.out.println("Concrete Product B operation");
        }
    }

    // 创建者抽象类
    static abstract class Creator {
        // 工厂方法，返回产品对象
        public abstract Product createProduct();

        // 其他操作
        public void someOperation() {
            // 创建产品对象
            Product product = createProduct();

            // 使用产品对象进行操作
            product.operation();
        }
    }

    // 具体创建者实现
    static class ConcreteCreatorA extends Creator {
        @Override
        public Product createProduct() {
            // 返回具体产品A对象
            return new ConcreteProductA();
        }
    }

    static class ConcreteCreatorB extends Creator {
        @Override
        public Product createProduct() {
            // 返回具体产品B对象
            return new ConcreteProductB();
        }
    }
}

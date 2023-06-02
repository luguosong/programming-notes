package com.luguosong._01_creational._02_abstract_factory;

/**
 * @author luguosong
 */
public class AbstractFactoryExample {
    public static void main(String[] args) {
        AbstractFactory factory1 = new ConcreteFactory1();
        ProductA productA1 = factory1.createProductA();
        ProductB productB1 = factory1.createProductB();
        productA1.operationA();
        productB1.operationB();

        AbstractFactory factory2 = new ConcreteFactory2();
        ProductA productA2 = factory2.createProductA();
        ProductB productB2 = factory2.createProductB();
        productA2.operationA();
        productB2.operationB();
    }

    static interface ProductA {
        void operationA();
    }

    static interface ProductB {
        void operationB();
    }

    static class ConcreteProductA1 implements ProductA {
        @Override
        public void operationA() {
            System.out.println("产品族1：产品A");
        }
    }

    static class ConcreteProductA2 implements ProductA {
        @Override
        public void operationA() {
            System.out.println("产品族2：产品A");
        }
    }

    static class ConcreteProductB1 implements ProductB {
        @Override
        public void operationB() {
            System.out.println("产品族1：产品B");
        }
    }

    static class ConcreteProductB2 implements ProductB {
        @Override
        public void operationB() {
            System.out.println("产品族2：产品B");
        }
    }

    static interface AbstractFactory {
        ProductA createProductA();

        ProductB createProductB();
    }

    static class ConcreteFactory1 implements AbstractFactory {
        @Override
        public ProductA createProductA() {
            return new ConcreteProductA1();
        }

        @Override
        public ProductB createProductB() {
            return new ConcreteProductB1();
        }
    }

    static class ConcreteFactory2 implements AbstractFactory {
        @Override
        public ProductA createProductA() {
            return new ConcreteProductA2();
        }

        @Override
        public ProductB createProductB() {
            return new ConcreteProductB2();
        }
    }

}

package com.luguosong._02_structural._02_bridge;

/**
 * @author luguosong
 */
public class BridgeExample {
    public static void main(String[] args) {
        // 创建具体实现对象1
        Implementation implementation1 = new ConcreteImplementation1();
        // 创建抽象对象，并将具体实现对象1传递给它
        Abstraction abstraction = new RefinedAbstraction(implementation1);
        // 调用抽象对象的执行动作方法
        abstraction.executeAction();

        // 创建具体实现对象2
        Implementation implementation2 = new ConcreteImplementation2();
        // 将具体实现对象2传递给抽象对象
        abstraction = new RefinedAbstraction(implementation2);
        // 调用抽象对象的执行动作方法
        abstraction.executeAction();
    }

    // 实现部分的接口
    static interface Implementation {
        void performAction();
    }

    // 具体实现1
    static class ConcreteImplementation1 implements Implementation {
        @Override
        public void performAction() {
            System.out.println("具体实现1");
        }
    }

    // 具体实现2
    static class ConcreteImplementation2 implements Implementation {
        @Override
        public void performAction() {
            System.out.println("具体实现2");
        }
    }

    // 抽象部分的基类
    static abstract class Abstraction {
        protected Implementation implementation;

        public Abstraction(Implementation implementation) {
            this.implementation = implementation;
        }

        public abstract void executeAction();
    }

    // 精确抽象
    static class RefinedAbstraction extends Abstraction {
        public RefinedAbstraction(Implementation implementation) {
            super(implementation);
        }

        @Override
        public void executeAction() {
            System.out.println("精确抽象部分1");
            implementation.performAction();
        }
    }
}

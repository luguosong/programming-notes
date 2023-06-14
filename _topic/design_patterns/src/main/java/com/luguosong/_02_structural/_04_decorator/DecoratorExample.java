package com.luguosong._02_structural._04_decorator;

/**
 * @author luguosong
 */
public class DecoratorExample {
    public static void main(String[] args) {
        // 创建具体组件
        ConcreteComponent component = new ConcreteComponent();

        //对具体组件进行装饰
        Decorator decoratorA = new ConcreteDecoratorA(component);
        Decorator decoratorB = new ConcreteDecoratorB(decoratorA);

        //执行操作
        decoratorB.operation();
    }


    /**
     * 部件（Component）
     */
    static interface Component {
        void operation();
    }


    /**
     * 具体部件（Concrete Component）
     */
    static class ConcreteComponent implements Component {
        @Override
        public void operation() {
            System.out.println("执行具体组件的操作");
        }
    }


    /**
     * 基础装饰（Base Decorator）
     */
    static abstract class Decorator implements Component {
        protected Component component;

        public Decorator(Component component) {
            this.component = component;
        }

        @Override
        public void operation() {
            component.operation();
        }
    }

    /**
     * 具体装饰类A（Concrete Decorators）
     */
    static class ConcreteDecoratorA extends Decorator {
        public ConcreteDecoratorA(Component component) {
            super(component);
        }

        @Override
        public void operation() {
            super.operation();
            //⭐⭐⭐这是整个装饰者模式的关键，通过super.operation()调用被装饰者的方法，然后再执行自己的额外行为
            addBehavior();
        }

        private void addBehavior() {
            System.out.println("装饰者A添加的额外行为");
        }
    }


    /**
     * 具体装饰类B（Concrete Decorators）
     */
    static class ConcreteDecoratorB extends Decorator {
        public ConcreteDecoratorB(Component component) {
            super(component);
        }

        @Override
        public void operation() {
            super.operation();
            //⭐⭐⭐这是整个装饰者模式的关键，通过super.operation()调用被装饰者的方法，然后再执行自己的额外行为
            addBehavior();
        }

        private void addBehavior() {
            System.out.println("装饰者B添加的额外行为");
        }
    }
}

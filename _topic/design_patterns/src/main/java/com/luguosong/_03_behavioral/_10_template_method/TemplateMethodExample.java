package com.luguosong._03_behavioral._10_template_method;

/**
 * @author luguosong
 */
public class TemplateMethodExample {
    public static void main(String[] args) {
        AbstractClass class1 = new ConcreteClass1();
        class1.templateMethod();

        System.out.println("--------------------");

        AbstractClass class2 = new ConcreteClass2();
        class2.templateMethod();
    }

    // 抽象类
    static abstract class AbstractClass {
        // ⭐模板方法，定义了算法的骨架
        public final void templateMethod() {
            step1();
            step2();
            step3();
        }

        // 抽象方法，算法的步骤1
        protected abstract void step1();

        // 抽象方法，算法的步骤2
        protected abstract void step2();

        // 默认实现，算法的步骤3
        protected void step3() {
            System.out.println("AbstractClass: 执行默认的step3实现。");
        }
    }

    // 具体类1
    static class ConcreteClass1 extends AbstractClass {
        @Override
        protected void step1() {
            System.out.println("ConcreteClass1: 执行step1。");
        }

        @Override
        protected void step2() {
            System.out.println("ConcreteClass1: 执行step2。");
        }
    }

    // 具体类2
    static class ConcreteClass2 extends AbstractClass {
        @Override
        protected void step1() {
            System.out.println("ConcreteClass2: 执行step1。");
        }

        @Override
        protected void step2() {
            System.out.println("ConcreteClass2: 执行step2。");
        }

        @Override
        protected void step3() {
            System.out.println("ConcreteClass2: 执行自定义的step3实现。");
        }
    }
}

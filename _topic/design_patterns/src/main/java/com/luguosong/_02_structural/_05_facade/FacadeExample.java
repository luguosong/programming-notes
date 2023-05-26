package com.luguosong._02_structural._05_facade;

/**
 * 外观模式示例
 *
 * @author luguosong
 */
public class FacadeExample {
    public static void main(String[] args) {
        // 使用外观类来简化操作
        Facade facade = new Facade();
        facade.operation();

        // 可以直接调用外观类的其他方法...
    }

    // 子系统类1
    static class Subsystem1 {
        public void operation1() {
            System.out.println("Subsystem 1: Operation 1");
        }

        // 其他操作和方法...
    }

    // 子系统类2
    static class Subsystem2 {
        public void operation2() {
            System.out.println("Subsystem 2: Operation 2");
        }

        // 其他操作和方法...
    }

    // 外观类
    static class Facade {
        private Subsystem1 subsystem1;
        private Subsystem2 subsystem2;

        public Facade() {
            subsystem1 = new Subsystem1();
            subsystem2 = new Subsystem2();
        }

        public void operation() {
            subsystem1.operation1();
            subsystem2.operation2();
        }

        // 其他操作和方法...
    }
}

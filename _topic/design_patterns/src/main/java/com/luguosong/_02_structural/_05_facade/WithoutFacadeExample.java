package com.luguosong._02_structural._05_facade;

/**
 * 不使用外观的情况
 *
 * @author luguosong
 */
public class WithoutFacadeExample {
    public static void main(String[] args) {
        // 直接使用子系统类来完成操作，没有外观模式的封装
        Subsystem1 subsystem1 = new Subsystem1();
        Subsystem2 subsystem2 = new Subsystem2();

        subsystem1.operation1();
        subsystem2.operation2();

        // 可以直接调用子系统类的其他方法...
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
}

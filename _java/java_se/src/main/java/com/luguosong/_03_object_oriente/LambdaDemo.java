package com.luguosong._03_object_oriente;

/**
 * @author luguosong
 */
public class LambdaDemo {
    public static void main(String[] args) {
        // 使用Lambda表达式创建一个接口实例
        MyInterface myInterface = () -> {
            System.out.println("Lambda表达式实现的doSomething方法");
        };

        // 调用Lambda表达式实现的方法
        myInterface.doSomething();
    }

    // 定义一个接口
    interface MyInterface {
        void doSomething();
    }
}



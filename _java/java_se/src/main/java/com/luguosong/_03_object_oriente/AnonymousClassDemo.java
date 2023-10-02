package com.luguosong._03_object_oriente;

/**
 * @author luguosong
 */
public class AnonymousClassDemo {
    public static void main(String[] args) {
        // 创建一个接口实例，并使用匿名类实现该接口
        MyInterface myInterface = new MyInterface() {
            @Override
            public void doSomething() {
                System.out.println("匿名类实现的doSomething方法");
            }
        };

        // 调用匿名类实现的方法
        myInterface.doSomething();
    }

    // 定义一个接口
    interface MyInterface {
        void doSomething();
    }
}



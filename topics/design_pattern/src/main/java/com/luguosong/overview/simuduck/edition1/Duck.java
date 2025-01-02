package com.luguosong.overview.simuduck.edition1;

/**
 * 鸭子超类
 *
 * @author luguosong
 */
public abstract class Duck {
    public void quack() {
        System.out.println("嘎嘎叫");
    }

    public void swim() {
        System.out.println("游泳");
    }

    /*
    * display() 方法是抽象的，因为所有鸭子子类型的外观都不同。
    * */
    public abstract void display();
}

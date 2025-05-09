package com.luguosong.overview.simuduck.edition2;

/**
 * 鸭子超类
 *
 * @author luguosong
 */
public abstract class Duck {

    public void swim() {
        System.out.println("游泳");
    }

    /*
    * display() 方法是抽象的，因为所有鸭子子类型的外观都不同。
    * */
    public abstract void display();
}

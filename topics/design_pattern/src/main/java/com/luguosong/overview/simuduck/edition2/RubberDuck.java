package com.luguosong.overview.simuduck.edition2;

/**
 * @author luguosong
 */
public class RubberDuck extends Duck implements Quackable {
    @Override
    public void display() {
        System.out.println("我是一只橡皮鸭");
    }

    @Override
    public void quack() {
        System.out.println("橡皮鸭呱呱叫");
    }
}

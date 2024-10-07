package com.luguosong.overview.simuduck.edition2;

/**
 * @author luguosong
 */
public class MallardDuck extends Duck implements Flyable, Quackable {
    @Override
    public void display() {
        System.out.println("我是一只绿头鸭");
    }

    @Override
    public void fly() {
        System.out.println("绿头鸭会飞");
    }

    @Override
    public void quack() {
        System.out.println("绿头鸭呱呱叫");
    }
}

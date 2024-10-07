package com.luguosong.overview.simuduck.edition2;

/**
 * @author luguosong
 */
public class RedheadDuck extends Duck implements Flyable, Quackable {
    @Override
    public void display() {
        System.out.println("我是一只红头鸭");
    }

    @Override
    public void fly() {
        System.out.println("红头鸭会飞");
    }

    @Override
    public void quack() {
        System.out.println("红头鸭嘎嘎叫");
    }
}

package com.luguosong.behavioral.strategy.use_strategy;

/**
 * 橡皮鸭
 *
 * @author luguosong
 */
public class RubberDuck extends Duck {

	public RubberDuck() {
		flyBehavior = new FlyNoWay(); // 橡皮鸭不会飞
		quackBehavior = new Squeak(); // 橡皮鸭会发出吱吱声
	}

	public void display() {
		System.out.println("我是只橡皮鸭。");
	}
}

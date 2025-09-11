package com.luguosong.behavioral.strategy.use_strategy;

/**
 * 诱饵鸭
 *
 * @author luguosong
 */
public class DecoyDuck extends Duck {
	public DecoyDuck() {
		setFlyBehavior(new FlyNoWay()); // 诱饵鸭不会飞
		setQuackBehavior(new MuteQuack()); // 诱饵鸭不会叫
	}

	public void display() {
		System.out.println("我是一个诱饵鸭。");
	}
}

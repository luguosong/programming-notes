package com.luguosong.behavioral.strategy.use_strategy;

/**
 * 不会飞的鸭子的飞行行为实现（像橡皮鸭和诱饵鸭）
 *
 * @author luguosong
 */
public class FlyNoWay implements FlyBehavior {
	public void fly() {
		System.out.println("我不会飞");
	}
}

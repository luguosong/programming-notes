package com.luguosong.behavioral.strategy.use_strategy;

/**
 * 红头鸭
 *
 * @author luguosong
 */
public class RedHeadDuck extends Duck {

	public RedHeadDuck() {
		flyBehavior = new FlyWithWings(); // 确实能飞的鸭子
		quackBehavior = new Quack(); // 呱呱叫
	}

	public void display() {
		System.out.println("我是真正的红头鸭。");
	}
}

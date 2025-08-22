package com.luguosong.behavioral.strategy.use_strategy;

/**
 * 绿头鸭
 *
 * @author luguosong
 */
public class MallardDuck extends Duck {

	public MallardDuck() {
		quackBehavior = new Quack(); // 呱呱叫
		flyBehavior = new FlyWithWings(); // 确实能飞的鸭子
	}

	public void display() {
		System.out.println("我是一只真正的绿头鸭。");
	}
}

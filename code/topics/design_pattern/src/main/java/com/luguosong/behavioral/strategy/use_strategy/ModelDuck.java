package com.luguosong.behavioral.strategy.use_strategy;

/**
 * 模型鸭
 *
 * @author luguosong
 */
public class ModelDuck extends Duck {
	public ModelDuck() {
		flyBehavior = new FlyNoWay(); // 模型鸭不会飞
		quackBehavior = new Quack(); // 模型鸭呱呱叫
	}

	public void display() {
		System.out.println("我是模型鸭。");
	}
}

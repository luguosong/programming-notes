package com.luguosong.behavioral.strategy.inheritance;

/**
 * @author luguosong
 */
public abstract class Duck {
	// 所有鸭子都会嘎嘎叫
	void quack() {
		System.out.println("嘎嘎叫");
	}

	// 所有鸭子都会游泳
	void swim() {
		System.out.println("游泳");
	}

	// 不同鸭子看起来不一样
	abstract void display();
}

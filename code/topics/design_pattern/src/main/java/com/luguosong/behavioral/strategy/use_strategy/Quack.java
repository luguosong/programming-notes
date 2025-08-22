package com.luguosong.behavioral.strategy.use_strategy;

/**
 * 呱呱叫
 *
 * @author luguosong
 */
public class Quack implements QuackBehavior {
	public void quack() {
		System.out.println("Quack");
	}
}

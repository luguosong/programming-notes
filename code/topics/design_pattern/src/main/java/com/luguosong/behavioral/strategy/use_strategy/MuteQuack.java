package com.luguosong.behavioral.strategy.use_strategy;

/**
 * 静音
 *
 * @author luguosong
 */
public class MuteQuack implements QuackBehavior {
	public void quack() {
		System.out.println("<< Silence >>");
	}
}


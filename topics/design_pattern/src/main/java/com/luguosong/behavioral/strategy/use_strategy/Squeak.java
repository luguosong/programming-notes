package com.luguosong.behavioral.strategy.use_strategy;

/**
 * 吱吱声
 *
 * @author luguosong
 */
public class Squeak implements QuackBehavior {
	public void quack() {
		System.out.println("Squeak");
	}
}

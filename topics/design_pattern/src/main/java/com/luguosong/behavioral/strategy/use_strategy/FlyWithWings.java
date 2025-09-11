package com.luguosong.behavioral.strategy.use_strategy;

/**
 * 确实能飞的鸭子的飞行行为实现
 * <p>
 * 具体策略
 *
 * @author luguosong
 */
public class FlyWithWings implements FlyBehavior {
	@Override
	public void fly() {
		System.out.println("我正在飞行");
	}
}

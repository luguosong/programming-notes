package com.luguosong.behavioral.strategy.use_strategy;

/**
 * 上下文
 *
 * @author luguosong
 */
public abstract class Duck {

	/*
	 * 为行为接口类型声明两个引用变量。
	 * 所有鸭子子类都继承它们
	 * */
	FlyBehavior flyBehavior;
	QuackBehavior quackBehavior;

	public Duck() {
	}

	// 动态设置行为
	public void setFlyBehavior(FlyBehavior fb) {
		flyBehavior = fb;
	}

	// 动态设置行为
	public void setQuackBehavior(QuackBehavior qb) {
		quackBehavior = qb;
	}

	abstract void display();

	// 委托给行为类
	public void performFly() {
		flyBehavior.fly();
	}

	// 委托给行为类
	public void performQuack() {
		quackBehavior.quack();
	}

	public void swim() {
		System.out.println("All ducks float, even decoys!");
	}
}

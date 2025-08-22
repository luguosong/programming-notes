package com.luguosong.behavioral.strategy.use_strategy;

/**
 * @author luguosong
 */
public class MiniDuckSimulator {

	public static void main(String[] args) {

		Duck mallard = new MallardDuck();
		Duck rubberDuck = new RubberDuck();
		Duck decoy = new DecoyDuck();

		Duck model = new ModelDuck();

		mallard.performQuack(); // Quack
		rubberDuck.performQuack(); // Squeak
		decoy.performQuack(); // << Silence >>

		model.performFly(); // 我不会飞
		//可以动态改变行为
		model.setFlyBehavior(new FlyRocketPowered());
		model.performFly(); // 我正在乘火箭飞行
	}
}

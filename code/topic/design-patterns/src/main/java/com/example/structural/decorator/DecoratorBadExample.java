package com.example.structural.decorator;

/**
 * 装饰器模式 - 反例
 * 问题：用继承组合功能，导致子类爆炸（N种饮料 × M种配料 = N×M个类）
 */
public class DecoratorBadExample {
    public static void main(String[] args) {
        // ❌ 每种组合都是一个独立子类
        BeverageBad e = new EspressoBad();
        System.out.println(e.getDescription() + " - ¥" + e.cost());

        BeverageBad em = new EspressoWithMilkBad();
        System.out.println(em.getDescription() + " - ¥" + em.cost());

        BeverageBad ems = new EspressoWithMilkAndSugarBad();
        System.out.println(ems.getDescription() + " - ¥" + ems.cost());

        // 如果加一种新配料（例如香草），需要新增所有组合的子类 ❌
    }
}

abstract class BeverageBad {
    public abstract String getDescription();
    public abstract double cost();
}

class EspressoBad extends BeverageBad {
    @Override public String getDescription() { return "浓缩咖啡"; }
    @Override public double cost()           { return 10.0; }
}

// ❌ 为每种组合创建子类
class EspressoWithMilkBad extends BeverageBad {
    @Override public String getDescription() { return "浓缩咖啡 + 牛奶"; }
    @Override public double cost()           { return 10.0 + 2.0; }
}

class EspressoWithMilkAndSugarBad extends BeverageBad {
    @Override public String getDescription() { return "浓缩咖啡 + 牛奶 + 糖"; }
    @Override public double cost()           { return 10.0 + 2.0 + 0.5; }
}

// 美式咖啡系列（同样要重复所有组合 ❌）
class AmericanoBad extends BeverageBad {
    @Override public String getDescription() { return "美式咖啡"; }
    @Override public double cost()           { return 8.0; }
}
// AmericanoWithMilkBad, AmericanoWithSugarBad, AmericanoWithMilkAndSugarBad ... 无限增长 ❌

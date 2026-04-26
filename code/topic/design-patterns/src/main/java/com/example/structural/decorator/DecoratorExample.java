package com.example.structural.decorator;

/**
 * 装饰器模式 - 正例
 * 装饰器动态叠加功能，新增配料只需一个新装饰器类，不产生子类爆炸
 */
public class DecoratorExample {
    public static void main(String[] args) {
        // ✅ 用装饰器动态叠加：浓缩咖啡 + 牛奶 + 糖
        Beverage espresso = new Espresso();
        espresso = new MilkDecorator(espresso);
        espresso = new SugarDecorator(espresso);
        System.out.println(espresso.getDescription() + " - ¥" + espresso.cost());
        // 输出: 浓缩咖啡, 牛奶, 糖 - ¥12.5

        // ✅ 美式咖啡 + 双份糖，只需重新组合，不用新增类
        Beverage americano = new Americano();
        americano = new SugarDecorator(americano);
        americano = new SugarDecorator(americano); // 双份糖
        System.out.println(americano.getDescription() + " - ¥" + americano.cost());
    }
}

// 饮料组件接口
abstract class Beverage {
    public abstract String getDescription();
    public abstract double cost();
}

// 具体组件：浓缩咖啡
class Espresso extends Beverage {
    @Override public String getDescription() { return "浓缩咖啡"; }
    @Override public double cost()           { return 10.0; }
}

// 具体组件：美式咖啡
class Americano extends Beverage {
    @Override public String getDescription() { return "美式咖啡"; }
    @Override public double cost()           { return 8.0; }
}

// 抽象装饰器：持有被装饰对象的引用
abstract class CondimentDecorator extends Beverage {
    protected final Beverage wrapped;
    public CondimentDecorator(Beverage beverage) { this.wrapped = beverage; }
}

// ✅ 具体装饰器：牛奶（只需一个类，可叠加到任何饮料）
class MilkDecorator extends CondimentDecorator {
    public MilkDecorator(Beverage beverage) { super(beverage); }

    @Override public String getDescription() { return wrapped.getDescription() + ", 牛奶"; }
    @Override public double cost()           { return wrapped.cost() + 2.0; }
}

// ✅ 具体装饰器：糖
class SugarDecorator extends CondimentDecorator {
    public SugarDecorator(Beverage beverage) { super(beverage); }

    @Override public String getDescription() { return wrapped.getDescription() + ", 糖"; }
    @Override public double cost()           { return wrapped.cost() + 0.5; }
}

package com.example.behavioral.strategy;

/**
 * 策略模式 - 正例
 * 每种折扣策略独立封装，新增策略不修改已有代码（开闭原则）
 */
public class StrategyExample {
    public static void main(String[] args) {
        OrderService service = new OrderService();

        // ✅ 运行时切换策略
        service.setDiscountStrategy(new VipDiscount());
        System.out.println("VIP价格: " + service.calculatePrice(100.0));      // 80.0

        service.setDiscountStrategy(new NewUserDiscount());
        System.out.println("新用户价格: " + service.calculatePrice(100.0));   // 90.0

        service.setDiscountStrategy(new NoDiscount());
        System.out.println("原价: " + service.calculatePrice(100.0));         // 100.0

        // ✅ 新增满减策略：只需新建 FullReductionDiscount，不改任何已有代码
    }
}

// 策略接口
interface DiscountStrategy {
    double apply(double originalPrice);
}

// 具体策略：VIP 八折
class VipDiscount implements DiscountStrategy {
    @Override public double apply(double price) { return price * 0.8; }
}

// 具体策略：新用户减 10 元
class NewUserDiscount implements DiscountStrategy {
    @Override public double apply(double price) { return Math.max(0, price - 10); }
}

// 具体策略：无折扣
class NoDiscount implements DiscountStrategy {
    @Override public double apply(double price) { return price; }
}

// 上下文：持有策略引用，可运行时替换
class OrderService {
    private DiscountStrategy discountStrategy = new NoDiscount(); // 默认无折扣

    public void setDiscountStrategy(DiscountStrategy strategy) {
        this.discountStrategy = strategy;
    }

    public double calculatePrice(double originalPrice) {
        return discountStrategy.apply(originalPrice); // 委托给策略
    }
}

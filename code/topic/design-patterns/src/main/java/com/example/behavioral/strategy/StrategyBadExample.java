package com.example.behavioral.strategy;

/**
 * 策略模式 - 反例
 * 问题：calculatePrice 方法包含所有折扣逻辑，新增折扣类型必须修改原方法
 */
public class StrategyBadExample {
    public static void main(String[] args) {
        StrategyBadExample demo = new StrategyBadExample();
        System.out.println(demo.calculatePrice(100.0, "vip"));
        System.out.println(demo.calculatePrice(100.0, "new_user"));
        System.out.println(demo.calculatePrice(100.0, "normal"));
        // 新增"满减"折扣？要改这个方法的 if-else ❌
    }

    // ❌ 所有折扣逻辑堆在一个方法里
    public double calculatePrice(double price, String userType) {
        if ("vip".equals(userType)) {
            return price * 0.8; // VIP 八折
        } else if ("new_user".equals(userType)) {
            return price - 10;  // 新用户减10元
        } else {
            return price;       // 无折扣
        }
        // 新增满减？这里再加 else if ❌
    }
}

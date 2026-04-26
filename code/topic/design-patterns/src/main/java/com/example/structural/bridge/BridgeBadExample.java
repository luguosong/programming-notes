package com.example.structural.bridge;

/**
 * 桥接模式 - 反例
 * 问题：用继承实现"通知类型 × 发送渠道"的组合，导致类爆炸
 */
public class BridgeBadExample {
    public static void main(String[] args) {
        // ❌ 每种组合都是一个独立类
        new UrgentEmailNotification().send("服务器宕机！");
        new UrgentSmsNotification().send("服务器宕机！");
        new NormalEmailNotification().send("每日报告");
        // 新增一种渠道（如微信），要为所有通知类型各新增一个子类 ❌
    }
}

// ❌ 类爆炸：通知类型 × 渠道 = 每种组合一个类
abstract class NotificationBad {
    public abstract void send(String message);
}

class UrgentEmailNotification extends NotificationBad {
    @Override
    public void send(String message) {
        System.out.println("[紧急][Email] " + message);
    }
}

class UrgentSmsNotification extends NotificationBad {
    @Override
    public void send(String message) {
        System.out.println("[紧急][SMS] " + message);
    }
}

class NormalEmailNotification extends NotificationBad {
    @Override
    public void send(String message) {
        System.out.println("[普通][Email] " + message);
    }
}

class NormalSmsNotification extends NotificationBad {
    @Override
    public void send(String message) {
        System.out.println("[普通][SMS] " + message);
    }
}

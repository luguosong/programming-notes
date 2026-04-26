package com.example.creational.factory_method;

/**
 * 工厂方法模式 - 反例
 * 问题1：直接 new，与具体类强耦合
 * 问题2：if-else 扩展需要修改原有代码，违反开闭原则
 */
public class FactoryMethodBadExample {
    public static void main(String[] args) {
        // 方式1：直接 new，与具体类强耦合 ❌
        AlipayNotificationBad alipay = new AlipayNotificationBad();
        alipay.send("支付宝订单：123");

        // 方式2：字符串 type 控制分支，新增类型必须改这里 ❌
        OrderServiceBad service = new OrderServiceBad();
        service.completeOrder("alipay",  1L);
        service.completeOrder("wechat",  2L);
        service.completeOrder("unknown", 3L);
    }
}

// 通知方式接口
interface NotificationBad {
    void send(String msg);
}

// 支付宝通知（具体类）
class AlipayNotificationBad implements NotificationBad {
    @Override
    public void send(String msg) { System.out.println("[支付宝通知] " + msg); }
}

// 微信通知（具体类）
class WechatNotificationBad implements NotificationBad {
    @Override
    public void send(String msg) { System.out.println("[微信通知] " + msg); }
}

// ❌ 直接在业务代码里写 if-else 判断类型
class OrderServiceBad {
    public void completeOrder(String paymentType, Long orderId) {
        // 处理业务逻辑...
        NotificationBad notification;
        if ("alipay".equals(paymentType)) {
            notification = new AlipayNotificationBad(); // ❌ 强耦合
        } else if ("wechat".equals(paymentType)) {
            notification = new WechatNotificationBad(); // ❌ 强耦合
        } else {
            throw new IllegalArgumentException("不支持的支付类型");
        }
        notification.send("订单 " + orderId + " 已完成");
    }
}

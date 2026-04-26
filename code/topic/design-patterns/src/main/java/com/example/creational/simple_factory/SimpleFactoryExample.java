package com.example.creational.simple_factory;

/**
 * 简单工厂模式 - 正例
 * 工厂类集中管理 new 操作，客户端只依赖接口 NotificationSender
 */
public class SimpleFactoryExample {
    public static void main(String[] args) {
        // ✅ 调用方只依赖接口，不依赖具体类
        NotificationSender sender = SenderFactory.create("email");
        sender.send("验证码：123456"); // [Email] 验证码：123456

        sender = SenderFactory.create("sms");
        sender.send("验证码：123456"); // [SMS] 验证码：123456

        sender = SenderFactory.create("push");
        sender.send("验证码：123456"); // [Push] 验证码：123456
    }
}

// 产品接口
interface NotificationSender {
    void send(String message);
}

// 具体产品：邮件
class EmailSender implements NotificationSender {
    @Override
    public void send(String message) { System.out.println("[Email] " + message); }
}

// 具体产品：短信
class SmsSender implements NotificationSender {
    @Override
    public void send(String message) { System.out.println("[SMS] " + message); }
}

// 具体产品：推送
class PushSender implements NotificationSender {
    @Override
    public void send(String message) { System.out.println("[Push] " + message); }
}

// 工厂类：集中管理对象创建
class SenderFactory {
    public static NotificationSender create(String type) {
        return switch (type) {
            case "email" -> new EmailSender();
            case "sms"   -> new SmsSender();
            case "push"  -> new PushSender();
            default      -> throw new IllegalArgumentException("未知渠道：" + type);
        };
    }
}

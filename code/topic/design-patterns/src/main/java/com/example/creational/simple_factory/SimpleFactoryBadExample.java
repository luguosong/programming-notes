package com.example.creational.simple_factory;

/**
 * 简单工厂模式 - 反例
 * 问题：客户端直接依赖具体类，新增通知渠道需要找到所有调用点逐一修改
 */
public class SimpleFactoryBadExample {
    public static void main(String[] args) {
        SimpleFactoryBadExample demo = new SimpleFactoryBadExample();
        demo.notify("email", "验证码：123456");
        demo.notify("sms",   "验证码：123456");
        demo.notify("push",  "验证码：123456");
        // 新增微信通知？每个 notify 方法都要改 ❌
    }

    // ❌ 客户端直接依赖具体类，新增通知渠道需要找到所有调用点逐一修改
    public void notify(String type, String msg) {
        if ("email".equals(type)) {
            new EmailSenderBad().send(msg);
        } else if ("sms".equals(type)) {
            new SmsSenderBad().send(msg);
        } else if ("push".equals(type)) {
            new PushSenderBad().send(msg);
        }
        // 新增微信通知？每个 notify 方法都要改 ❌
    }
}

class EmailSenderBad {
    public void send(String message) { System.out.println("[Email] " + message); }
}

class SmsSenderBad {
    public void send(String message) { System.out.println("[SMS] " + message); }
}

class PushSenderBad {
    public void send(String message) { System.out.println("[Push] " + message); }
}

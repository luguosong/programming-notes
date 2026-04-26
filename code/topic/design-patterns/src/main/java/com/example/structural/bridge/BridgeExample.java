package com.example.structural.bridge;

/**
 * 桥接模式 - 正例
 * 将"通知类型"与"发送渠道"解耦：两个维度独立扩展，互不影响
 */
public class BridgeExample {
    public static void main(String[] args) {
        // ✅ 自由组合：不同通知类型 × 不同渠道
        Notification urgent = new UrgentNotification(new EmailChannel());
        urgent.send("服务器宕机！");

        Notification urgentSms = new UrgentNotification(new SmsChannel());
        urgentSms.send("服务器宕机！");

        Notification normal = new NormalNotification(new EmailChannel());
        normal.send("每日报告");

        // ✅ 新增微信渠道：只需加一个 WechatChannel，不需要修改任何 Notification 类
    }
}

// 实现维度接口：发送渠道
interface MessageChannel {
    void sendMessage(String prefix, String message);
}

// 具体实现：邮件
class EmailChannel implements MessageChannel {
    @Override
    public void sendMessage(String prefix, String message) {
        System.out.println("[Email][" + prefix + "] " + message);
    }
}

// 具体实现：短信
class SmsChannel implements MessageChannel {
    @Override
    public void sendMessage(String prefix, String message) {
        System.out.println("[SMS][" + prefix + "] " + message);
    }
}

// 抽象维度：通知类型（持有渠道引用，这就是"桥"）
abstract class Notification {
    protected final MessageChannel channel; // 桥接点

    public Notification(MessageChannel channel) { this.channel = channel; }

    public abstract void send(String message);
}

// 具体抽象：紧急通知
class UrgentNotification extends Notification {
    public UrgentNotification(MessageChannel channel) { super(channel); }

    @Override
    public void send(String message) {
        channel.sendMessage("紧急", "⚠ " + message); // 委托给渠道
    }
}

// 具体抽象：普通通知
class NormalNotification extends Notification {
    public NormalNotification(MessageChannel channel) { super(channel); }

    @Override
    public void send(String message) {
        channel.sendMessage("普通", message);
    }
}

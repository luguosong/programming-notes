package com.example.structural.adapter;

/**
 * 适配器模式 - 反例
 * 问题：业务代码直接依赖第三方 SDK 类，SDK 升级或更换时需要修改所有调用点
 */
public class AdapterBadExample {
    public static void main(String[] args) {
        // ❌ 直接 new 第三方 SDK，业务代码与 SDK 强耦合
        OrderServiceBad service = new OrderServiceBad();
        service.pay(99.9, "order-001");
    }
}

// ❌ 业务服务直接依赖第三方 SDK
class OrderServiceBad {
    // 字段直接是第三方 SDK 类型
    private final AlipaySDKBad alipaySDK = new AlipaySDKBad("appId-xxx", "privateKey-xxx");

    public void pay(double amount, String orderId) {
        // 业务逻辑与 SDK API 混在一起 ❌
        boolean success = alipaySDK.doAlipayRequest(amount, "CNY", orderId, "order_" + orderId);
        if (success) {
            System.out.println("✅ 支付成功");
        } else {
            System.out.println("❌ 支付失败");
        }
        // 假设现在要换成微信支付——所有用到 AlipaySDKBad 的地方都要改！
    }
}

// 模拟第三方支付宝 SDK（命名和参数与我们的业务接口不一致）
class AlipaySDKBad {
    private final String appId;
    private final String privateKey;

    public AlipaySDKBad(String appId, String privateKey) {
        this.appId      = appId;
        this.privateKey = privateKey;
        System.out.println("AlipaySDK 初始化，appId=" + appId);
    }

    // ❌ SDK 方法签名和参数顺序与我们的业务接口不一致
    public boolean doAlipayRequest(double totalAmount, String currency,
                                   String outTradeNo, String subject) {
        System.out.println("[支付宝SDK] 发起支付 outTradeNo=" + outTradeNo
                + ", amount=" + totalAmount + " " + currency);
        return true;
    }
}

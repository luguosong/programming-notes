package com.example.structural.adapter;

/**
 * 适配器模式 - 正例
 * AlipayAdapter 将第三方 SDK 包装为统一的 PaymentGateway 接口
 */
public class AdapterExample {
    public static void main(String[] args) {
        // ✅ OrderService 只依赖 PaymentGateway 接口
        PaymentGateway alipay = new AlipayAdapter(new AlipaySDK("appId-xxx", "key-xxx"));
        OrderService service  = new OrderService(alipay);

        service.pay(99.9, "order-001");

        // 换成微信支付只需换一个适配器，OrderService 代码不变 ✅
        // PaymentGateway wechat = new WechatAdapter(...);
        // OrderService service2 = new OrderService(wechat);
    }
}

// 业务目标接口（我们自己定义）
interface PaymentGateway {
    boolean charge(String orderId, double amountYuan);
}

// 支付请求值对象
record PaymentRequest(String orderId, double amount) {}

// 第三方支付宝 SDK（不可修改，参数顺序和命名与我们的接口不同）
class AlipaySDK {
    private final String appId;
    private final String privateKey;

    public AlipaySDK(String appId, String privateKey) {
        this.appId      = appId;
        this.privateKey = privateKey;
        System.out.println("AlipaySDK 初始化，appId=" + appId);
    }

    // SDK 方法签名与 PaymentGateway 不同
    public boolean doAlipayRequest(double totalAmount, String currency,
                                   String outTradeNo, String subject) {
        System.out.println("[支付宝SDK] outTradeNo=" + outTradeNo
                + ", amount=" + totalAmount + " " + currency);
        return true;
    }
}

// ✅ 适配器：将 AlipaySDK 的接口转换为 PaymentGateway 接口
class AlipayAdapter implements PaymentGateway {
    private final AlipaySDK sdk;

    public AlipayAdapter(AlipaySDK sdk) { this.sdk = sdk; }

    @Override
    public boolean charge(String orderId, double amountYuan) {
        // 适配：把我们的参数转换为 SDK 需要的格式
        return sdk.doAlipayRequest(
                amountYuan,          // 参数映射
                "CNY",               // 补充 SDK 需要的额外参数
                orderId,
                "order_" + orderId
        );
    }
}

// ✅ 业务服务：只依赖 PaymentGateway 接口，与具体 SDK 解耦
class OrderService {
    private final PaymentGateway paymentGateway;

    public OrderService(PaymentGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    public void pay(double amount, String orderId) {
        boolean success = paymentGateway.charge(orderId, amount);
        System.out.println(success ? "✅ 支付成功" : "❌ 支付失败");
    }
}

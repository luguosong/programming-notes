package com.example.creational.factory_method;

/**
 * 工厂方法模式 - 正例
 * 工厂方法由子类决定创建哪种产品，新增支付方式只需添加新子类，不修改已有代码
 */
public class FactoryMethodExample {
    public static void main(String[] args) {
        // ✅ 程序只依赖抽象 PaymentFactory，新增支付方式不改原有代码
        PaymentFactory alipayFactory = new AlipayFactory();
        PaymentResult r1 = alipayFactory.processPayment(99.9);
        System.out.println("支付宝：" + r1.status() + " - " + r1.message());

        PaymentFactory wechatFactory = new WechatPayFactory();
        PaymentResult r2 = wechatFactory.processPayment(99.9);
        System.out.println("微信支付：" + r2.status() + " - " + r2.message());
    }
}

// 支付结果状态
enum PaymentStatus { SUCCESS, FAILED }

// 支付结果值对象
record PaymentResult(PaymentStatus status, String message) {}

// 产品接口
interface PaymentProcessor {
    PaymentResult pay(double amount);
}

// 具体产品：支付宝
class AlipayProcessor implements PaymentProcessor {
    @Override
    public PaymentResult pay(double amount) {
        System.out.println("[支付宝] 扣款 " + amount + " 元");
        return new PaymentResult(PaymentStatus.SUCCESS, "支付宝支付成功");
    }
}

// 具体产品：微信支付
class WechatPayProcessor implements PaymentProcessor {
    @Override
    public PaymentResult pay(double amount) {
        System.out.println("[微信支付] 扣款 " + amount + " 元");
        return new PaymentResult(PaymentStatus.SUCCESS, "微信支付成功");
    }
}

// 抽象工厂：定义工厂方法
abstract class PaymentFactory {
    // 工厂方法：由子类决定创建哪种 PaymentProcessor
    protected abstract PaymentProcessor createProcessor();

    // 模板方法：完整的支付流程
    public PaymentResult processPayment(double amount) {
        PaymentProcessor processor = createProcessor(); // 调用工厂方法
        return processor.pay(amount);
    }
}

// 具体工厂：支付宝
class AlipayFactory extends PaymentFactory {
    @Override
    protected PaymentProcessor createProcessor() {
        return new AlipayProcessor(); // ✅ 子类决定创建哪种产品
    }
}

// 具体工厂：微信支付
class WechatPayFactory extends PaymentFactory {
    @Override
    protected PaymentProcessor createProcessor() {
        return new WechatPayProcessor(); // ✅ 子类决定创建哪种产品
    }
}

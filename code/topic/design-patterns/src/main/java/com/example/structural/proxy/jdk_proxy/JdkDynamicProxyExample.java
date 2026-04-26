package com.example.structural.proxy.jdk_proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * JDK 动态代理示例
 * <p>
 * 使用 java.lang.reflect.Proxy 在运行时自动生成代理类，无需为每个接口手动编写代理类。
 * 核心：实现 InvocationHandler，所有方法调用都汇聚到 invoke() 统一处理。
 * <p>
 * 限制：被代理对象必须实现接口（代理类是接口的实现，不是目标类的子类）。
 */
public class JdkDynamicProxyExample {
    public static void main(String[] args) {
        OrderService real = new RealOrderService();

        // ✅ 同一个 LoggingHandler 可代理任意接口，无需为每个接口单独写代理类
        OrderService service = (OrderService) Proxy.newProxyInstance(
                real.getClass().getClassLoader(),    // 使用目标类的类加载器
                new Class[]{OrderService.class},     // 代理的接口列表
                new LoggingHandler(real)             // 方法调用委托给 InvocationHandler
        );

        service.createOrder("商品B", 3);
        service.cancelOrder(2001L);
    }
}

interface OrderService {
    Long createOrder(String product, int quantity);
    void cancelOrder(Long orderId);
}

class RealOrderService implements OrderService {
    private long nextId = 2000L;

    @Override
    public Long createOrder(String product, int quantity) {
        System.out.printf("[业务] 创建订单：%s x%d%n", product, quantity);
        return ++nextId;
    }

    @Override
    public void cancelOrder(Long orderId) {
        System.out.println("[业务] 取消订单：" + orderId);
    }
}

// ✅ 通用拦截器：持有 Object 引用，可为任意类型的目标对象生成代理
class LoggingHandler implements InvocationHandler {
    private final Object target;

    public LoggingHandler(Object target) { this.target = target; }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 前置日志
        System.out.printf("[日志] → %s(%s)%n", method.getName(), formatArgs(args));
        // 反射调用真实方法
        Object result = method.invoke(target, args);
        // 后置日志
        System.out.printf("[日志] ← %s 返回: %s%n", method.getName(), result);
        return result;
    }

    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) return "无参数";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(args[i]);
        }
        return sb.toString();
    }
}

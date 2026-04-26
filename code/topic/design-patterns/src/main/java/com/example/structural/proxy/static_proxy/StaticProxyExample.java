package com.example.structural.proxy.static_proxy;

/**
 * 静态代理示例
 * <p>
 * 代理类在编译期就已手动编写完成，每个接口都需要一个独立的代理类。
 * 优点：实现简单、直观；缺点：接口越多代理类越多，维护成本随之增加。
 */
public class StaticProxyExample {
    public static void main(String[] args) {
        // 静态代理：编译期已确定代理类，运行时直接 new
        OrderService service = new LoggingOrderProxy(new RealOrderService());
        service.createOrder("商品A", 2);
        service.cancelOrder(1001L);
    }
}

interface OrderService {
    Long createOrder(String product, int quantity);
    void cancelOrder(Long orderId);
}

// 真实业务类，只专注于核心逻辑
class RealOrderService implements OrderService {
    private long nextId = 1000L;

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

// ✅ 静态代理：手动编写，实现相同接口，织入日志横切逻辑
class LoggingOrderProxy implements OrderService {
    private final OrderService target;

    public LoggingOrderProxy(OrderService target) { this.target = target; }

    @Override
    public Long createOrder(String product, int quantity) {
        System.out.printf("[日志] → createOrder(%s, %d)%n", product, quantity);
        Long id = target.createOrder(product, quantity);
        System.out.println("[日志] ← createOrder 返回 orderId=" + id);
        return id;
    }

    @Override
    public void cancelOrder(Long orderId) {
        System.out.println("[日志] → cancelOrder(" + orderId + ")");
        target.cancelOrder(orderId);
        System.out.println("[日志] ← cancelOrder 完成");
    }
}

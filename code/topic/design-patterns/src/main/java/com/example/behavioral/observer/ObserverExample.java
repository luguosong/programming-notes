package com.example.behavioral.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * 观察者模式 - 正例
 * OrderService 只发布事件，各监听器独立订阅，互不依赖
 */
public class ObserverExample {
    public static void main(String[] args) {
        OrderService orderService = new OrderService();

        // ✅ 注册监听器（新增监听方只需注册，不改 OrderService）
        orderService.addListener(new PointListener());
        orderService.addListener(new InventoryListener());
        orderService.addListener(new SmsListener());

        orderService.completeOrder(1001L);
        System.out.println("---");
        // 取消积分监听
        orderService.completeOrder(1002L);
    }
}

// 订单领域对象
class Order {
    private final Long   orderId;
    private final Long   userId;
    private       String status;

    public Order(Long orderId, Long userId) {
        this.orderId = orderId;
        this.userId  = userId;
        this.status  = "PENDING";
    }

    public Long   getOrderId() { return orderId; }
    public Long   getUserId()  { return userId;  }
    public String getStatus()  { return status;  }
    public void   setStatus(String status) { this.status = status; }
}

// 观察者接口
interface OrderEventListener {
    void onOrderCompleted(Order order);
}

// 具体观察者：积分服务
class PointListener implements OrderEventListener {
    @Override
    public void onOrderCompleted(Order order) {
        System.out.println("✅ [积分] 订单 " + order.getOrderId() + " 增加积分");
    }
}

// 具体观察者：库存服务
class InventoryListener implements OrderEventListener {
    @Override
    public void onOrderCompleted(Order order) {
        System.out.println("✅ [库存] 订单 " + order.getOrderId() + " 扣减库存");
    }
}

// 具体观察者：短信服务
class SmsListener implements OrderEventListener {
    @Override
    public void onOrderCompleted(Order order) {
        System.out.println("✅ [短信] 向用户 " + order.getUserId() + " 发送通知");
    }
}

// 被观察者：OrderService 只管发布事件
class OrderService {
    private final List<OrderEventListener> listeners = new ArrayList<>();

    public void addListener(OrderEventListener listener)    { listeners.add(listener);    }
    public void removeListener(OrderEventListener listener) { listeners.remove(listener); }

    public void completeOrder(Long orderId) {
        Order order = new Order(orderId, 100L);
        order.setStatus("COMPLETED");
        System.out.println("订单 " + orderId + " 完成");
        // ✅ 只发布事件，不关心谁在监听
        listeners.forEach(l -> l.onOrderCompleted(order));
    }
}

package com.example.behavioral.observer;

/**
 * 观察者模式 - 反例
 * 问题：OrderServiceBad 直接调用所有下游服务，耦合度高，新增监听方需修改该类
 */
public class ObserverBadExample {
    public static void main(String[] args) {
        OrderServiceBad service = new OrderServiceBad();
        service.completeOrder(1001L);
    }
}

// ❌ 直接依赖所有下游服务
class OrderServiceBad {
    private final PointServiceBad     pointService     = new PointServiceBad();
    private final InventoryServiceBad inventoryService = new InventoryServiceBad();
    private final MessageServiceBad   messageService   = new MessageServiceBad();

    public void completeOrder(long orderId) {
        System.out.println("订单 " + orderId + " 完成");
        // ❌ 业务逻辑与通知逻辑混杂，新增监听方要改这里
        pointService.addPoints(orderId);
        inventoryService.deduct(orderId);
        messageService.sendSms(orderId);
    }
}

class PointServiceBad     { public void addPoints(long orderId) { System.out.println("积分：订单 " + orderId + " 增加积分"); } }
class InventoryServiceBad { public void deduct(long orderId)    { System.out.println("库存：订单 " + orderId + " 扣减库存"); } }
class MessageServiceBad   { public void sendSms(long orderId)   { System.out.println("短信：订单 " + orderId + " 发送通知"); } }

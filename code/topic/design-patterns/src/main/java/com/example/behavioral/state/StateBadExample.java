package com.example.behavioral.state;

/**
 * 状态模式 - 反例
 * 问题：订单状态逻辑全部堆在 OrderBad 内，每次新增状态都要修改整个类
 */
public class StateBadExample {
    public static void main(String[] args) {
        OrderBad order = new OrderBad();
        order.pay();     // PENDING -> PAID
        order.ship();    // PAID -> SHIPPED
        order.complete();// SHIPPED -> COMPLETED
        order.pay();     // ❌ 已完成订单不能再支付
    }
}

// ❌ 所有状态迁移逻辑都堆在一个类里
class OrderBad {
    private String status = "PENDING"; // 用字符串表示状态

    public void pay() {
        if ("PENDING".equals(status)) {
            System.out.println("支付成功，状态: PENDING -> PAID");
            status = "PAID";
        } else {
            System.out.println("❌ 当前状态[" + status + "]不能支付");
        }
    }

    public void ship() {
        if ("PAID".equals(status)) {
            System.out.println("已发货，状态: PAID -> SHIPPED");
            status = "SHIPPED";
        } else {
            System.out.println("❌ 当前状态[" + status + "]不能发货");
        }
    }

    public void complete() {
        if ("SHIPPED".equals(status)) {
            System.out.println("订单完成，状态: SHIPPED -> COMPLETED");
            status = "COMPLETED";
        } else {
            System.out.println("❌ 当前状态[" + status + "]不能完成");
        }
    }
    // 新增"退款"状态？要在每个方法里加 if-else ❌
}

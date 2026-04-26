package com.example.behavioral.state;

/**
 * 状态模式 - 正例
 * 每个状态独立封装，新增状态只需添加新类，不修改 Order
 */
public class StateExample {
    public static void main(String[] args) {
        Order order = new Order();
        order.pay();      // PENDING -> PAID   ✅
        order.ship();     // PAID -> SHIPPED   ✅
        order.complete(); // SHIPPED -> COMPLETED ✅
        order.pay();      // ❌ 已完成，拒绝操作
    }
}

// 状态接口
interface OrderState {
    void pay(Order order);
    void ship(Order order);
    void complete(Order order);
}

// 状态：待支付
class PendingState implements OrderState {
    @Override
    public void pay(Order order) {
        System.out.println("✅ 支付成功 PENDING -> PAID");
        order.setState(new PaidState());
    }
    @Override public void ship(Order order)    { System.out.println("❌ 待支付订单不能发货");   }
    @Override public void complete(Order order){ System.out.println("❌ 待支付订单不能完成");   }
}

// 状态：已支付
class PaidState implements OrderState {
    @Override public void pay(Order order)     { System.out.println("❌ 已支付，不能重复支付"); }
    @Override
    public void ship(Order order) {
        System.out.println("✅ 发货成功 PAID -> SHIPPED");
        order.setState(new ShippedState());
    }
    @Override public void complete(Order order){ System.out.println("❌ 已支付订单须先发货");   }
}

// 状态：已发货
class ShippedState implements OrderState {
    @Override public void pay(Order order)     { System.out.println("❌ 已发货，不能支付");     }
    @Override public void ship(Order order)    { System.out.println("❌ 已发货，不能重复发货"); }
    @Override
    public void complete(Order order) {
        System.out.println("✅ 订单完成 SHIPPED -> COMPLETED");
        order.setState(new CompletedState());
    }
}

// 状态：已完成（终态）
class CompletedState implements OrderState {
    @Override public void pay(Order order)     { System.out.println("❌ 已完成订单不能支付");   }
    @Override public void ship(Order order)    { System.out.println("❌ 已完成订单不能发货");   }
    @Override public void complete(Order order){ System.out.println("❌ 已完成订单不能再完成"); }
}

// 上下文：Order 持有当前状态，所有操作委托给状态对象
class Order {
    private OrderState state = new PendingState(); // 初始状态：待支付

    public void setState(OrderState state) { this.state = state; }

    // 委托给当前状态处理
    public void pay()      { state.pay(this);      }
    public void ship()     { state.ship(this);     }
    public void complete() { state.complete(this); }
}

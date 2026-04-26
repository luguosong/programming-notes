package com.example.creational.singleton;

/**
 * 单例模式 - 反例
 * 问题：没有任何限制，每次使用都创建新的连接池实例，导致资源浪费
 */
public class SingletonBadExample {
    public static void main(String[] args) {
        UserServiceBad userService  = new UserServiceBad();
        OrderServiceBad orderService = new OrderServiceBad();
        System.out.println(userService.findUser(1L));
        System.out.println(orderService.findOrder(1L));
        // 控制台会打印两次"连接池初始化"，说明创建了两个实例 ❌
    }
}

// ❌ 没有限制：每次都创建新的连接池
class UserServiceBad {
    public String findUser(Long id) {
        ConnectionPoolBad pool = new ConnectionPoolBad(); // 每次都重新初始化！
        String conn = pool.getConnection();
        return conn + ":user" + id;
    }
}

class OrderServiceBad {
    public String findOrder(Long id) {
        ConnectionPoolBad pool = new ConnectionPoolBad(); // 又一个连接池实例！
        String conn = pool.getConnection();
        return conn + ":order" + id;
    }
}

// 模拟一个重量级的连接池对象
class ConnectionPoolBad {
    public ConnectionPoolBad() {
        // 模拟耗时初始化
        System.out.println("❌ 连接池初始化（耗时操作）！");
    }

    public String getConnection() {
        return "Connection";
    }
}

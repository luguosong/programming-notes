package com.example.behavioral.chain_of_responsibility;

/**
 * 责任链模式 - 反例
 * 问题：所有处理逻辑堆在一个方法里，新增处理环节需要修改原方法
 */
public class ChainOfResponsibilityBadExample {
    public static void main(String[] args) {
        ChainOfResponsibilityBadExample demo = new ChainOfResponsibilityBadExample();
        demo.handle("user-123", "/api/orders", "192.168.1.1");
        demo.handle(null,       "/api/orders", "192.168.1.1"); // 认证失败
        demo.handle("user-123", "/api/orders", "1.2.3.4");     // 限流触发
    }

    // ❌ 所有处理步骤都堆在一个方法里
    public void handle(String userId, String path, String ip) {
        // 第1步：认证
        if (userId == null) {
            System.out.println("❌ 认证失败，拒绝请求");
            return;
        }
        // 第2步：限流
        if ("1.2.3.4".equals(ip)) { // 模拟该IP超出限制
            System.out.println("❌ IP " + ip + " 已超出请求限制");
            return;
        }
        // 第3步：业务处理
        System.out.println("✅ 处理请求: userId=" + userId + ", path=" + path);
        // 新增日志、权限校验步骤？继续往这里加 ❌
    }
}

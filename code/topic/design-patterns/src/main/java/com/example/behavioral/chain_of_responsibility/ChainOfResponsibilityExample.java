package com.example.behavioral.chain_of_responsibility;

import java.util.Map;

/**
 * 责任链模式 - 正例
 * 每个处理器独立封装，链式传递，新增处理器只需插入链中
 */
public class ChainOfResponsibilityExample {
    public static void main(String[] args) {
        // ✅ 组装责任链
        RequestHandler chain = new AuthHandler();
        chain.setNext(new RateLimitHandler())
             .setNext(new BusinessHandler());

        System.out.println("--- 正常请求 ---");
        chain.handle(Map.of("userId", "user-123", "ip", "192.168.1.1", "path", "/api/orders"));

        System.out.println("--- 认证失败 ---");
        chain.handle(Map.of("ip", "192.168.1.1", "path", "/api/orders"));

        System.out.println("--- 限流触发 ---");
        chain.handle(Map.of("userId", "user-123", "ip", "1.2.3.4", "path", "/api/orders"));
    }
}

// 抽象处理器
abstract class RequestHandler {
    private RequestHandler next;

    public RequestHandler setNext(RequestHandler next) {
        this.next = next;
        return next; // 返回 next 便于链式调用
    }

    public abstract void handle(Map<String, String> request);

    // 模板方法：传递给下一个处理器
    protected void passToNext(Map<String, String> request) {
        if (next != null) {
            next.handle(request);
        }
    }
}

// 具体处理器：认证
class AuthHandler extends RequestHandler {
    @Override
    public void handle(Map<String, String> request) {
        if (!request.containsKey("userId")) {
            System.out.println("❌ [AuthHandler] 认证失败，拒绝请求");
            return; // 拦截，不向后传递
        }
        System.out.println("✅ [AuthHandler] 认证通过");
        passToNext(request); // 通过，交给下一个处理器
    }
}

// 具体处理器：限流
class RateLimitHandler extends RequestHandler {
    private static final String BLOCKED_IP = "1.2.3.4"; // 模拟黑名单 IP

    @Override
    public void handle(Map<String, String> request) {
        if (BLOCKED_IP.equals(request.get("ip"))) {
            System.out.println("❌ [RateLimitHandler] IP " + request.get("ip") + " 已超出限制");
            return;
        }
        System.out.println("✅ [RateLimitHandler] 限流检查通过");
        passToNext(request);
    }
}

// 具体处理器：业务逻辑
class BusinessHandler extends RequestHandler {
    @Override
    public void handle(Map<String, String> request) {
        System.out.println("✅ [BusinessHandler] 处理业务: path=" + request.get("path")
                + ", userId=" + request.get("userId"));
    }
}

package com.example.creational.prototype;

/**
 * 原型模式 - 反例
 * 问题：每次都重新从数据库加载配置，忽略了对象创建成本
 */
public class PrototypeBadExample {
    public static void main(String[] args) {
        // ❌ 每次都重新加载配置，耗时且浪费资源
        TenantConfigBad configA = new TenantConfigBad("tenant-A");
        TenantConfigBad configB = new TenantConfigBad("tenant-A"); // 又加载一次！
        System.out.println("是同一次加载吗？" + (configA == configB)); // false，重复初始化了 ❌
    }
}

// ❌ 每次创建都执行耗时初始化
class TenantConfigBad {
    private final String tenantId;
    private final String dbUrl;
    private final int    maxConnections;

    public TenantConfigBad(String tenantId) {
        this.tenantId = tenantId;
        // 模拟从数据库加载（耗时 500ms）
        System.out.println("❌ 从数据库加载租户配置：" + tenantId);
        try { Thread.sleep(10); } catch (InterruptedException ignored) {} // 模拟耗时
        this.dbUrl          = "jdbc:mysql://db/" + tenantId;
        this.maxConnections = 20;
    }

    @Override
    public String toString() {
        return "TenantConfig{tenantId=" + tenantId + ", dbUrl=" + dbUrl + "}";
    }
}

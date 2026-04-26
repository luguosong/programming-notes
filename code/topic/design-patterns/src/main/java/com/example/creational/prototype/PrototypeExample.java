package com.example.creational.prototype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 原型模式 - 正例
 * 已创建的对象通过 clone() 复制，避免重复的昂贵初始化操作
 */
public class PrototypeExample {
    public static void main(String[] args) {
        ConfigRegistry registry = new ConfigRegistry();

        // 首次加载（慢）
        TenantConfig original = registry.get("tenant-A");

        // 克隆（快：直接内存复制）✅
        TenantConfig clone1 = original.clone();
        TenantConfig clone2 = original.clone();
        clone1.setMaxConnections(50); // 单独修改，互不影响

        System.out.println("原始配置: " + original);
        System.out.println("克隆1:   " + clone1);
        System.out.println("克隆2:   " + clone2);
        System.out.println("clone1 和 original 是同一对象？" + (original == clone1)); // false，但数据相同

        // ConfigService 演示：同一租户多次请求直接用克隆
        ConfigService service = new ConfigService(registry);
        TenantConfig c1 = service.getConfig("tenant-A");
        TenantConfig c2 = service.getConfig("tenant-A");
        System.out.println("两次获取配置是否独立？" + (c1 != c2)); // true，每次都是新克隆 ✅
    }
}

// 原型接口
interface Prototype<T> {
    T clone();
}

// 租户配置（实现 Prototype）
class TenantConfig implements Prototype<TenantConfig> {
    private final  String       tenantId;
    private        String       dbUrl;
    private        int          maxConnections;
    private final  List<String> features;       // 深拷贝示例字段

    // 主构造器：模拟耗时初始化
    public TenantConfig(String tenantId) {
        this.tenantId = tenantId;
        System.out.println("✅ 首次从数据库加载租户配置：" + tenantId);
        try { Thread.sleep(10); } catch (InterruptedException ignored) {}
        this.dbUrl          = "jdbc:mysql://db/" + tenantId;
        this.maxConnections = 20;
        this.features       = new ArrayList<>(List.of("feature-a", "feature-b"));
    }

    // 深拷贝构造器（供 clone() 使用）
    private TenantConfig(TenantConfig source) {
        this.tenantId       = source.tenantId;
        this.dbUrl          = source.dbUrl;
        this.maxConnections = source.maxConnections;
        this.features       = new ArrayList<>(source.features); // 深拷贝集合
    }

    @Override
    public TenantConfig clone() {
        return new TenantConfig(this); // ✅ 用私有拷贝构造器，跳过耗时初始化
    }

    public void setMaxConnections(int max) { this.maxConnections = max; }

    @Override
    public String toString() {
        return "TenantConfig{tenantId=" + tenantId + ", dbUrl=" + dbUrl
             + ", maxConn=" + maxConnections + ", features=" + features + "}";
    }
}

// 配置注册中心：缓存已创建的对象
class ConfigRegistry {
    private final Map<String, TenantConfig> cache = new HashMap<>();

    public TenantConfig get(String tenantId) {
        return cache.computeIfAbsent(tenantId, TenantConfig::new);
    }
}

// 应用服务：每次请求拿到的是克隆，修改互不影响
class ConfigService {
    private final ConfigRegistry registry;

    public ConfigService(ConfigRegistry registry) { this.registry = registry; }

    public TenantConfig getConfig(String tenantId) {
        return registry.get(tenantId).clone(); // ✅ 返回克隆，而非原始对象
    }
}

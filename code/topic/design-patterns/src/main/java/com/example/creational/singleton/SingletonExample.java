package com.example.creational.singleton;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 单例模式 - 四种常见实现方式
 */
public class SingletonExample {
    public static void main(String[] args) {
        // 方式一：饿汉式
        AppConfig cfg1 = AppConfig.getInstance();
        AppConfig cfg2 = AppConfig.getInstance();
        System.out.println("✅ 饿汉式同一实例：" + (cfg1 == cfg2)); // true

        // 方式二：双重检查锁（懒加载）
        ConnectionPool p1 = ConnectionPool.getInstance();
        ConnectionPool p2 = ConnectionPool.getInstance();
        System.out.println("✅ DCL 同一连接池：" + (p1 == p2)); // true

        // 方式三：静态内部类
        Logger.getInstance().log("测试日志");

        // 方式四：枚举
        RedisCache.INSTANCE.put("user:1", "张三");
        System.out.println("✅ 枚举缓存：" + RedisCache.INSTANCE.get("user:1"));
    }
}

// 方式一：饿汉式（类加载时创建，JVM 保证线程安全）
class AppConfig {
    // 类加载时立即创建，JVM 保证线程安全
    private static final AppConfig INSTANCE = new AppConfig();

    private String dbUrl;
    private int    maxPoolSize;

    private AppConfig() {
        // 模拟从配置文件加载
        this.dbUrl       = "jdbc:mysql://localhost:3306/app";
        this.maxPoolSize = 20;
        System.out.println("配置中心初始化完成");
    }

    public static AppConfig getInstance() { return INSTANCE; }

    public String getDbUrl()   { return dbUrl;       }
    public int    getMaxPool() { return maxPoolSize;  }
}

// 方式二：双重检查锁（懒加载 + 线程安全，推荐）
class ConnectionPool {
    // volatile：防止 JVM 指令重排序导致其他线程拿到"半初始化"的对象
    private static volatile ConnectionPool instance;

    private ConnectionPool() {
        System.out.println("连接池初始化，预建立 10 条连接...");
    }

    public static ConnectionPool getInstance() {
        if (instance == null) {                         // 第一次检查：避免每次加锁
            synchronized (ConnectionPool.class) {
                if (instance == null) {                 // 第二次检查：防止并发时重复创建
                    instance = new ConnectionPool();
                }
            }
        }
        return instance;
    }

    public String getConnection() { return "Connection"; }
}

// 方式三：静态内部类（懒加载 + 线程安全，最优雅）
class Logger {
    private Logger() {}

    // Holder 类只有在 getInstance() 被调用时才会被加载，JVM 类加载天然线程安全
    private static class Holder {
        static final Logger INSTANCE = new Logger();
    }

    public static Logger getInstance() { return Holder.INSTANCE; }

    public void log(String message) {
        System.out.println("[LOG] " + message);
    }
}

// 方式四：枚举（最简洁，额外防御反序列化和反射破坏）
enum RedisCache {
    INSTANCE;

    private final Map<String, Object> store = new ConcurrentHashMap<>();

    public void put(String key, Object value) { store.put(key, value);         }
    public Object get(String key)             { return store.get(key);         }
    public boolean contains(String key)       { return store.containsKey(key); }
}

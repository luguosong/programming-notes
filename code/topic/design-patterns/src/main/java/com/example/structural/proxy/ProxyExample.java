package com.example.structural.proxy;

import java.util.HashMap;
import java.util.Map;

/**
 * 代理模式 - 正例
 * CachingUserServiceProxy 在不修改 UserServiceImpl 的情况下添加缓存能力
 */
public class ProxyExample {
    public static void main(String[] args) {
        // ✅ 代理透明地为真实服务添加缓存
        UserService service = new CachingUserServiceProxy(new UserServiceImpl());

        System.out.println(service.findUser(1L)); // 第一次：查数据库
        System.out.println(service.findUser(1L)); // 第二次：命中缓存 ✅
        System.out.println(service.findUser(2L)); // 不同 ID：查数据库
    }
}

// 用户领域对象
class User {
    private final Long   id;
    private final String name;
    public User(Long id, String name) { this.id = id; this.name = name; }
    @Override public String toString() { return "User{id=" + id + ", name=" + name + "}"; }
}

// 服务接口
interface UserService {
    User findUser(Long id);
}

// 真实对象：只专注于业务逻辑
class UserServiceImpl implements UserService {
    @Override
    public User findUser(Long id) {
        System.out.println("[数据库] SELECT * FROM user WHERE id = " + id);
        return new User(id, "张三" + id);
    }
}

// ✅ 代理：在不修改 UserServiceImpl 的前提下添加缓存
class CachingUserServiceProxy implements UserService {
    private final UserService        target; // 持有真实对象的引用
    private final Map<Long, User>    cache = new HashMap<>();

    public CachingUserServiceProxy(UserService target) { this.target = target; }

    @Override
    public User findUser(Long id) {
        // 前置处理：检查缓存
        if (cache.containsKey(id)) {
            System.out.println("[缓存] 命中 user:" + id + " ✅");
            return cache.get(id);
        }
        // 委托真实对象处理
        User user = target.findUser(id);
        // 后置处理：写入缓存
        cache.put(id, user);
        System.out.println("[缓存] 写入 user:" + id);
        return user;
    }
}

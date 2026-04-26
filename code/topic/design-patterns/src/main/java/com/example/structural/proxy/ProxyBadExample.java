package com.example.structural.proxy;

/**
 * 代理模式 - 反例
 * 问题：UserServiceImpl 混杂了缓存、鉴权、日志等横切关注点，职责不单一
 */
public class ProxyBadExample {
    public static void main(String[] args) {
        UserServiceImplBad service = new UserServiceImplBad();
        System.out.println(service.findUser(1L));
        System.out.println(service.findUser(1L)); // 没有缓存，再次查数据库 ❌
    }
}

class UserBad {
    private final Long   id;
    private final String name;
    public UserBad(Long id, String name) { this.id = id; this.name = name; }
    @Override public String toString() { return "User{id=" + id + ", name=" + name + "}"; }
}

// ❌ 业务逻辑与缓存/日志/鉴权混在一起
class UserServiceImplBad {
    public UserBad findUser(Long id) {
        // ❌ 缓存逻辑混入业务代码
        System.out.println("[缓存检查] 无缓存（每次都要检查）");
        // ❌ 日志混入业务代码
        System.out.println("[日志] 开始查询用户 " + id);
        long start = System.currentTimeMillis();

        // 模拟数据库查询
        UserBad user = queryFromDb(id);

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("[日志] 查询完成，耗时 " + elapsed + "ms");
        return user;
    }

    private UserBad queryFromDb(Long id) {
        System.out.println("[数据库] SELECT * FROM user WHERE id = " + id);
        return new UserBad(id, "张三");
    }
}

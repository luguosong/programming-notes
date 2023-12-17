---
layout: note
title: Spring Security
nav_order: 30
parent: 认证和鉴权
create_time: 2023/9/26
---

# 功能

- 身份认证
- 操作授权
- 防止攻击

# Hello World

导入依赖：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

编写测试接口：

```java
@RestController
public class TestController {

    /**
     * 测试接口
     *
     * @return
     */
    @GetMapping("/test")
    public String test() {
        return "hello world";
    }
}
```

启动程序，此时接口访问需要用户名密码：

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202309260953507.png)

默认用户名为`user`, 密码在启动日志中：

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202309260954512.png)

可以通过配置文件对用户名密码进行修改：

```yaml
spring:
  security:
    user:
      name: admin
      password: 123456
```

访问`/logout`接口可以退出登录，再次访问`/test`接口需要重新登录。


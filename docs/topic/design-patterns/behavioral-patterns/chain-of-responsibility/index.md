---
title: 责任链模式
---

# 责任链模式

## 🔍 定义

责任链模式（Chain of Responsibility）将请求的发送者和接收者解耦：沿着处理者链传递请求，链上的每个处理者决定是否处理该请求，以及是否将请求传递给下一个处理者。

## ⚠️ 不使用责任链存在的问题

HTTP 请求处理需要依次进行认证、限流、日志记录，写在一个方法里导致逻辑混杂：

``` java title="ChainOfResponsibilityBadExample.java"
--8<-- "code/topic/design-patterns/src/main/java/com/example/behavioral/chain_of_responsibility/ChainOfResponsibilityBadExample.java"
```

## 🏗️ 设计模式结构说明

```mermaid
classDiagram
    class RequestHandler {
        <<abstract>>
        #next: RequestHandler
        +setNext(handler) RequestHandler
        +handle(request) Response
    }
    class AuthHandler {
        +handle(request) Response
    }
    class RateLimitHandler {
        +handle(request) Response
    }
    class BusinessHandler {
        +handle(request) Response
    }
    RequestHandler <|-- AuthHandler
    RequestHandler <|-- RateLimitHandler
    RequestHandler <|-- BusinessHandler
    RequestHandler --> RequestHandler
```

## 💻 设计模式举例说明

``` java title="ChainOfResponsibilityExample.java"
--8<-- "code/topic/design-patterns/src/main/java/com/example/behavioral/chain_of_responsibility/ChainOfResponsibilityExample.java"
```

## ⚖️ 优缺点

**优点：**

- 解耦请求发送者和处理者，客户端不需要知道链的结构
- 符合**开闭原则**：新增处理步骤只需新增处理者类并插入链中
- 可以灵活地动态组合处理链

**缺点：**

- 请求可能到达链尾仍未被处理（需要有默认处理者或明确的终止逻辑）
- 链过长时调试困难，难以追踪请求流转

## 🔗 与其它模式的关系

**相似模式防混淆：**

| 模式 | 处理者数量 | 是否继续传递 |
|------|----------|------------|
| 责任链（Chain） | 多个，按顺序 | 每个处理者决定是否传递 |
| 命令（Command） | 1个接收者 | 不传递，直接执行 |
| 装饰器（Decorator） | 多个，全部执行 | 总是调用下一层 |

## 🗂️ 应用场景

- 多个对象都可能处理同一请求，在运行时决定由谁处理
- 需要按顺序对请求执行多项检查（认证 → 限流 → 日志 → 业务）
- Servlet `Filter` 链（`FilterChain.doFilter()`）
- Spring Security 的 `SecurityFilterChain`
- Netty 的 `ChannelPipeline`

## 工业视角

### 两种实现风格：链表 vs 数组

GoF 经典定义中，Handler 持有 `next` 形成链表，但这种实现有一个隐患：每个子类都必须在业务逻辑后手动调用 `successor.handle()`，稍有遗漏就是 bug。更健壮的做法是结合**模板方法**将传链逻辑提升到抽象父类，子类只需实现自己的业务：

``` java title="链表实现 + 模板方法加固"
public abstract class Handler {
    protected Handler successor;

    public final void handle() {           // final：不允许子类覆盖传链逻辑
        boolean handled = doHandle();
        if (!handled && successor != null) {
            successor.handle();
        }
    }

    protected abstract boolean doHandle(); // 子类只实现业务，无需关心传链
}
```

第二种实现使用 `List<Handler>` 在 `HandlerChain` 内部统一遍历，Handler 只返回 `boolean` 表示是否已处理。这种方式更简洁，也更接近 Spring MVC `HandlerExecutionChain` 的真实设计。

### Servlet Filter 的递归传链与双向拦截

Tomcat `ApplicationFilterChain` 用数组保存所有 Filter，以 `pos` 游标推进。其核心技巧在于 `doFilter()` 是**递归**调用——Filter 在 `chain.doFilter()` 之前的代码拦截**请求**，之后的代码拦截**响应**，一个方法实现双向拦截：

``` java title="Tomcat ApplicationFilterChain 核心逻辑（简化）"
public void doFilter(ServletRequest request, ServletResponse response) {
    if (pos < n) {
        Filter filter = filters[pos++].getFilter();
        filter.doFilter(request, response, this); // 递归：this 就是 chain
    } else {
        servlet.service(request, response);       // 所有 Filter 执行完毕
    }
}
```

!!! tip "Servlet Filter vs Spring Interceptor 的实现差异"

    Servlet Filter 的双向拦截写在同一个 `doFilter()` 中（before → `chain.doFilter()` → after），依赖递归展开，可以在同一调用栈中 try-catch 包裹整个请求生命周期；Spring MVC `HandlerExecutionChain` 将拦截拆成 `preHandle()`、`postHandle()`、`afterCompletion()` 三个独立钩子，逻辑更直观，但三段代码不在同一调用栈中，无法用单个 try-catch 统一处理异常。

### 职责链作为框架扩展点

职责链在框架中的核心价值不只是"处理一个请求"，而是**向外暴露扩展点**：Servlet Filter、Spring Interceptor、Dubbo Filter、Netty ChannelPipeline、MyBatis Plugin，无一不是这个思路——框架固化核心执行流，将横切关注点（鉴权、限流、日志、加密）的扩展权交给使用者，且使用者无需修改框架源码，完全符合开闭原则。

!!! warning "Filter / Interceptor / AOP 选型参考"

    - **Servlet Filter**：最早介入，可拿到原始 HTTP 请求/响应，但拿不到 Spring Bean 和 Controller 信息。适合跨域、编解码、全局限流。
    - **Spring Interceptor**：在 DispatcherServlet 之后，能拿到 Handler（Controller 方法），适合登录校验、操作日志。
    - **Spring AOP**：粒度最细，能拿到方法参数和返回值，但无法直接拿到 `HttpServletRequest`。适合事务、方法级权限、性能埋点。

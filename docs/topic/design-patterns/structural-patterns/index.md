---
title: 结构型模式
---

# 结构型模式

结构型模式关注**如何将类和对象组合成更大的结构**，同时保持结构的灵活性和高效性。

GoF 定义了 7 种结构型模式，每种模式都有独立的详细笔记：

| 模式 | 一句话总结 | 核心手段 |
|------|-----------|---------|
| [外观（Facade）](facade/index.md) | 为复杂子系统提供简单接口 | 聚合子系统，暴露高层接口 |
| [适配器（Adapter）](adapter/index.md) | 让不兼容的接口协同工作 | 包装不兼容类，实现目标接口 |
| [代理（Proxy）](proxy/index.md) | 控制对目标对象的访问 | 实现相同接口 + 持有真实对象 |
| [装饰器（Decorator）](decorator/index.md) | 运行时动态添加功能 | 实现相同接口 + 持有被装饰对象 |
| [桥接（Bridge）](bridge/index.md) | 抽象与实现分离，独立扩展 | 组合代替继承，两维度各自扩展 |
| [组合（Composite）](composite/index.md) | 用树形结构表示整体-部分关系 | 叶子和容器统一接口 |
| [享元（Flyweight）](flyweight/index.md) | 共享细粒度对象减少内存占用 | 分离内/外部状态，工厂缓存共享对象 |

## 易混淆模式快速区分

适配器、装饰器、代理三者都是"包装"对象，区别在意图：

| 模式 | 接口变化？ | 主要意图 |
|------|----------|---------|
| 适配器 | ✅ 改变接口 | 兼容不兼容的接口 |
| 装饰器 | ❌ 接口不变 | 动态增强功能 |
| 代理 | ❌ 接口不变 | 控制/延迟访问 |

三者的结构差异一图对比：

```mermaid
%%{init: {'themeVariables': {'noteBkgColor': 'transparent', 'noteBorderColor': '#768390'}}}%%
classDiagram
    classDef default fill:transparent,stroke:#768390
    class Target {
        <<interface>>
        +request() void
    }
    class Adaptee {
        +specificRequest() void
    }
    class Adapter {
        -adaptee: Adaptee
        +request() void
    }
    class Component {
        <<interface>>
        +operation() void
    }
    class ConcreteComponent {
        +operation() void
    }
    class Decorator {
        -component: Component
        +operation() void
    }
    class Subject {
        <<interface>>
        +request() void
    }
    class RealSubject {
        +request() void
    }
    class Proxy {
        -realSubject: RealSubject
        +request() void
    }
    Target <|.. Adapter : 实现目标接口
    Adapter --> Adaptee : 持有引用
    Component <|.. ConcreteComponent : 实现
    Component <|.. Decorator : 实现（接口不变）
    Decorator o--> Component : 包装
    Subject <|.. RealSubject : 实现
    Subject <|.. Proxy : 实现（接口不变）
    Proxy o--> RealSubject : 持有
    note for Adapter "改变接口\n让不兼容的类协作"
    note for Decorator "接口不变\n动态叠加功能"
    note for Proxy "接口不变\n控制对真实对象的访问"
```

## 模式选型参考

```mermaid
graph TD
    A[需要组合/包装对象] --> B{需要改变\n对外接口？}
    B -- 是 --> C[适配器模式]
    B -- 否 --> D{需要动态\n叠加功能？}
    D -- 是 --> E[装饰器模式]
    D -- 否 --> F{需要控制\n对象访问？}
    F -- 是 --> G[代理模式]
    F -- 否 --> H{构建树形\n整体-部分结构？}
    H -- 是 --> I[组合模式]
    H -- 否 --> J{简化复杂\n子系统接口？}
    J -- 是 --> K[外观模式]
    J -- 否 --> L{两个维度\n独立扩展？}
    L -- 是 --> M[桥接模式]
    L -- 否 --> N[享元模式\n（大量相似对象共享）]
```


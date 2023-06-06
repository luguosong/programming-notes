---
layout: note
title: 设计原则
nav_order: 0
parent: 设计模式
create_time: 2023/5/30
---

# 类之间的关系

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/diagrams/design-pattern/uml-simple.svg)

# 面向对象的 S.O.L.I.D 原则

## 开闭原则

{: .note-title}
> 开闭原则（Open Closed Principle,OCP）
>
> 对扩展开放，对修改关闭

- 使用接口或抽象类
- 使用策略模式
- 使用装饰模式

## 单一职责原则

{: .note-title}
> 单一职责原则（Single Responsibility Principle，SRP）
>
> 一个类或者模块只负责完成一个职责（或者功能）

## 接口隔离原则

{: .note-title}
> 接口隔离原则（Interface Segregation Principle，ISP）
>
> 接口中不应该定义太多的方法，以防止实现类在实现接口时，被迫实现不需要的方法。

## 里氏代换原则

{: .note-title}
> 里氏替换原则（Liskov Substitution Principle，LSP）
>
> 子类对象应该能够在不破坏程序正确性的前提下替换父类对象。

- 子类可以实现父类的抽象方法，但`不能覆盖父类的非抽象方法`
- 子类`可以增加自己特有的方法`
- 当子类的方法重载父类的方法时，方法的前置条件（即方法的输入/入参）要比父类方法的输入参数更宽松

## 依赖反转原则

{: .note-title}
> 依赖反转原则(Dependency Inversion Principle,DIP)
>
> 高层模块（high-level modules,调用者）不要依赖低层模块（low-level，具体被调用者）,高层模块和低层模块应该通过抽象（abstractions）来互相依赖。
>
> 抽象（abstractions）不要依赖具体实现细节（details，具体被调用者）：这样可以确保抽象的稳定性和可复用性，使得模块可以在不改变抽象的情况下进行修改或替换。
>
> 具体实现细节（details,具体被调用者）依赖抽象（abstractions）：这样可以确保模块的实现符合抽象定义的规范

换句话说，就是`面向接口编程，而不是具体实现`。

# 迪米特法则

不要和陌生人说话

对于对象`O`中一个方法`M`，M应该只能够访问以下对象中的方法：

- 对象 O；
- 与 O 直接相关的 Component Object；
- 由方法 M 创建或者实例化的对象；
- 作为方法 M 的参数的对象。

# KISS原则

Keep It Simple, Stupid

尽量保持简单，不要过度设计

- 尽量减少使用别人不懂的代码，比如复杂的正则表达式
- 不要重复造轮子
- 不要过度优化

# YAGNI原则

You Ain’t Gonna Need It

你不会需要它

不要去设计当前用不到的代码，不要去编写当前用不到的功能

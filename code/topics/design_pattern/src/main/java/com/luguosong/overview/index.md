# 设计模式概述

## 好代码的批判标准

- 可维护性（maintainability）
- 可读性（readability）
- 可扩展性（extensibility）
- 灵活性（flexibility）
- 简洁性（simplicity）
- 可复用性（reusability）
- 可测试性（testability）

## SOLID原则

### SRP 单一职责原则

### OCP 开闭原则

### LSP 里式替换原则

### ISP 接口隔离原则

### DIP 依赖注入原则

## DRY 原则

## KISS 原则

## YAGNI 原则

## LOD 法则

## SimUDuck应用程序

乔就职于一家开发非常成功的鸭子池塘模拟游戏SimUDuck的公司。这个游戏可以展示各种鸭子品种`游泳`和`发出嘎嘎叫声`。

### 第一版:使用继承

系统的初始设计者采用了标准的`面向对象`技术，创建了一个`鸭子超类`，所有其他鸭子类型都从这个超类继承。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409060916576.png){ loading=lazy }
  <figcaption>第一版</figcaption>
</figure>

``` java title="Duck.java"
--8<-- "code/topics/design_pattern/src/main/java/com/luguosong/overview/simuduck/edition1/Duck.java"
```

``` java title="MallardDuck.java"
--8<-- "code/topics/design_pattern/src/main/java/com/luguosong/overview/simuduck/edition1/MallardDuck.java"
```

``` java title="RedheadDuck.java"
--8<-- "code/topics/design_pattern/src/main/java/com/luguosong/overview/simuduck/edition1/RedheadDuck.java"
```

### 第二版:添加接口

现在需要给鸭子添加飞行`功能`。

在`鸭子超类`中编写`fly`方法，这样所有鸭子都会飞了。但也有些鸭子是不会飞的，比如橡皮鸭。当然，子类可以重写`fly`方法，来改变鸭子的飞行行为，但这样让开发变得复杂。

我们可以部分行为使用`接口`替代。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409060950737.png){ loading=lazy }
  <figcaption>第二版</figcaption>
</figure>

❌但这样仍然存在问题，我们知道，并不是所有子类都应该具有飞行或呱呱叫的行为，因此继承并不是正确的解决方案。然而，让子类实现 Flyable 和/或 Quackable 虽然解决了部分问题（例如橡皮鸭不应该飞），但却完全破坏了这些行为的`代码复用`，从而造成了另一种`维护上的麻烦`。当然，即使在会飞的鸭子中，也可能存在不止一种飞行行为。

### 第三版

识别应用程序中会变化的部分，并将其与保持不变的部分分离。

这是理解这一原则的另一种方式：`将变化的部分封装起来，这样以后就可以在不影响不变部分的情况下，修改或扩展变化的部分`。

尽管这个概念很简单，但它构成了几乎所有设计模式的基础。所有模式都提供了一种方法，使系统的某个部分可以独立于其他部分进行变化。

据我们所知，除了 `fly()` 和 `quack()` 方法的问题外，Duck 类运行良好，其他部分似乎没有频繁变化。因此，除了做一些小的调整外，我们基本上会保持 Duck 类不变。

现在，为了`将变化的部分与不变的部分分开`，我们将创建两组类（完全独立于Duck），一组用于飞行，一组用于叫声。每组类将包含各自行为的所有实现。例如，我们可能有一个实现呱呱叫的类，另一个实现吱吱叫的类，还有一个实现静音的类。

!!! note

    我们知道，fly() 和 quack() 是鸭子类中因鸭子不同而变化的部分。

    为了将这些行为从鸭子类中分离出来，我们将把这两个方法从鸭子类中提取出来，并为每种行为创建一组新的类。

<figure markdown="span">
  ![](https://edrawcloudpubliccn.oss-cn-shenzhen.aliyuncs.com/viewer/self/1059758/share/2024-9-6/1725602055/main.svg){ loading=lazy }
  <figcaption>将变化的部分分离</figcaption>
</figure>

那么，我们将如何设计实现飞行和呱呱叫行为的一组类呢？

我们希望保持灵活性；毕竟，正是鸭子行为的僵化让我们一开始陷入了麻烦。我们知道，我们想要为鸭子的实例分配行为。例如，我们可能想要实例化一个新的绿头鸭实例，并用特定类型的飞行行为进行初始化。同时，为什么不确保我们可以动态地改变鸭子的行为呢？换句话说，我们应该`在鸭子类中包含行为设置方法`，以便我们可以在运行时更改绿头鸭的飞行行为。

我们将使用接口来表示每种行为——例如，FlyBehavior 和 QuackBehavior——每种行为的实现都将实现其中一个接口。



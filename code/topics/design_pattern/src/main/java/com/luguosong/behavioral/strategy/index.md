# 策略

## 定义

策略模式是一种行为设计模式， 它能让你定义一系列算法， 并将每种算法分别放入独立的类中， 以使算法的对象能够相互替换。

## 结构

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202506181113060.png){ loading=lazy }
  <figcaption>策略模式</figcaption>
</figure>

1. `上下文（Context）` 维护指向具体策略的引用， 且仅通过策略接口与该对象进行交流。
2. `策略（Strategy）` 接口是所有具体策略的通用接口， 它声明了一个上下文用于执行策略的方法。
3. `具体策略（ConcreteStrategies）` 实现了上下文所用算法的各种不同变体。
4. 当上下文需要运行算法时，它会在其已连接的策略对象上调用执行方法。上下文不清楚其所涉及的策略类型与算法的执行方式。
5. 客户端（Client）会创建一个特定策略对象并将其传递给上下文。上下文则会提供一个设置器以便客户端在运行时替换相关联的策略。

## 实例分析

### 使用继承或实现

#### 示例

``` java title="Duck.java"
--8<-- "code/topics/design_pattern/src/main/java/com/luguosong/behavioral/strategy/inheritance/Duck.java"
```

``` java title="MallarDuck.java"
--8<-- "code/topics/design_pattern/src/main/java/com/luguosong/behavioral/strategy/inheritance/MallarDuck.java"
```

``` java title="RedheadDuck.java"
--8<-- "code/topics/design_pattern/src/main/java/com/luguosong/behavioral/strategy/inheritance/RedheadDuck.java"
```

#### 存在的问题

如果Duck需要增加新的行为，比如飞行。

- 如果直接在父类Duck中实现fly()方法，那么所有子类鸭子都将继承飞行，但并非所有鸭子都会飞行，这显然不对。
- 可以将fly()方法定义为抽象方法，但这会导致所有子类都必须实现该方法，增加了代码的复杂性。
- 也可以在不可以飞行的子类鸭子中覆写fly()方法，但这会导致代码重复，冗余。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202506181442675.png){ loading=lazy }
  <figcaption>代码冗余</figcaption>
</figure>

- 使用接口来定义飞行行为，虽然可以解决问题，但会导致代码的复杂性增加。每次增加新的行为，都需要修改对应子类的实现。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202506181438087.png){ loading=lazy }
  <figcaption>❗使用接口来定义行为，每当增加行为，所有相关子类都需要重新改写</figcaption>
</figure>

### 使用策略模式

#### Duck类：上下文

``` java title="Duck.java"
--8<-- "code/topics/design_pattern/src/main/java/com/luguosong/behavioral/strategy/use_strategy/Duck.java"
```

#### 飞行行为

##### 策略

``` java title="FlyBehavior.java"
--8<-- "code/topics/design_pattern/src/main/java/com/luguosong/behavioral/strategy/use_strategy/FlyBehavior.java"
```

##### 具体策略

``` java title="FlyWithWings.java"
--8<-- "code/topics/design_pattern/src/main/java/com/luguosong/behavioral/strategy/use_strategy/FlyWithWings.java"
```

``` java title="FlyNoWay.java"
--8<-- "code/topics/design_pattern/src/main/java/com/luguosong/behavioral/strategy/use_strategy/FlyNoWay.java"
```

``` java title="FlyRocketPowered.java"
--8<-- "code/topics/design_pattern/src/main/java/com/luguosong/behavioral/strategy/use_strategy/FlyRocketPowered.java"
```

#### 叫声行为

##### 策略

``` java title="QuackBehavior.java"
--8<-- "code/topics/design_pattern/src/main/java/com/luguosong/behavioral/strategy/use_strategy/QuackBehavior.java"
```

##### 具体策略

``` java title="Quack.java"
--8<-- "code/topics/design_pattern/src/main/java/com/luguosong/behavioral/strategy/use_strategy/Quack.java"
```

``` java title="MuteQuack.java"
--8<-- "code/topics/design_pattern/src/main/java/com/luguosong/behavioral/strategy/use_strategy/MuteQuack.java"
```

``` java title="Squeak.java"
--8<-- "code/topics/design_pattern/src/main/java/com/luguosong/behavioral/strategy/use_strategy/Squeak.java"
```

#### 在具体鸭子类中设置行为

``` java title="MallardDuck.java"
--8<-- "code/topics/design_pattern/src/main/java/com/luguosong/behavioral/strategy/use_strategy/MallardDuck.java"
```

``` java title="RedHeadDuck.java"
--8<-- "code/topics/design_pattern/src/main/java/com/luguosong/behavioral/strategy/use_strategy/RedHeadDuck.java"
```

``` java title="RubberDuck.java"
--8<-- "code/topics/design_pattern/src/main/java/com/luguosong/behavioral/strategy/use_strategy/RubberDuck.java"
```

``` java title="DecoyDuck.java"
--8<-- "code/topics/design_pattern/src/main/java/com/luguosong/behavioral/strategy/use_strategy/DecoyDuck.java"
```

``` java title="ModelDuck.java"
--8<-- "code/topics/design_pattern/src/main/java/com/luguosong/behavioral/strategy/use_strategy/ModelDuck.java"
```

#### 测试代码

``` java title="MiniDuckSimulator.java"
--8<-- "code/topics/design_pattern/src/main/java/com/luguosong/behavioral/strategy/use_strategy/MiniDuckSimulator.java"
```

## 设计模式分析

## 实际使用场景

# JUnit5

## 入门案例

- 引入依赖

```xml

<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

- 编写测试类

``` java title="HelloTest.java"
--8<-- "code/java/test/junit/junit5-demo/src/test/java/com/luguosong/junit5demo/HelloTest.java"
```

## 生命周期方法

``` java title="LifecycleTest.java"
--8<-- "code/java/test/junit/junit5-demo/src/test/java/com/luguosong/junit5demo/LifecycleTest.java"
```

执行结果：

```text
执行BeforeAll方法

执行BeforeEach方法
执行测试方法1
执行AfterEach方法

执行BeforeEach方法
执行测试方法2
执行AfterEach方法

执行AfterAll方法
```

## 自定义显示名称

``` java title="DisplayNameTest.java"
--8<-- "code/java/test/junit/junit5-demo/src/test/java/com/luguosong/junit5demo/DisplayNameTest.java"
```

## 断言

### 概述

!!! note

	断言失败，后续代码不会继续执行。

### assertEquals

assertEquals用于断言两个值是否相等。

``` java title="AssertEqualsTest.java"
--8<-- "code/java/test/junit/junit5-demo/src/test/java/com/luguosong/junit5demo/assertions/AssertEqualsTest.java"
```

### assertTrue

assertTrue用于断言一个布尔值为true。

``` java title="AssertTrueTest.java"
--8<-- "code/java/test/junit/junit5-demo/src/test/java/com/luguosong/junit5demo/assertions/AssertTrueTest.java"
```

### assertThrows

如果抛出的异常类型与 expectedType 相同或属于其子类型，则断言通过。如果没有抛出异常，或抛出的异常类型不同，该方法将失败。

``` java title="AssertThrowsTest.java"
--8<-- "code/java/test/junit/junit5-demo/src/test/java/com/luguosong/junit5demo/assertions/AssertThrowsTest.java"
```

### assertTimeout

断言提供的executable的执行在给定的timeout超时之前完成。

!!! warning

	executable将与调用代码在同一线程中执行。因此，如果超时被超过，executable的执行不会被抢占性终止。

``` java title="AssertTimeoutTest.java"
--8<-- "code/java/test/junit/junit5-demo/src/test/java/com/luguosong/junit5demo/assertions/AssertTimeoutTest.java"
```

### assertTimeoutPreemptively

断言提供的executable执行完毕，并且未超过给定的timeout。

``` java title="AssertTimeoutPreemptivelyTest.java"
--8<-- "code/java/test/junit/junit5-demo/src/test/java/com/luguosong/junit5demo/assertions/AssertTimeoutPreemptivelyTest.java"
```

## 假设

假设和断言的区别是：假设如果不成立，测试被标记为`终止`，而不是失败。断言如果不成立测试则会被标记为`失败`。

### assumeTrue

``` java title="AssumeTrueTest.java"
--8<-- "code/java/test/junit/junit5-demo/src/test/java/com/luguosong/junit5demo/assume/AssumeTrueTest.java"
```

### assumingThat

仅在假设成立时执行代码。

``` java title="AssumingThatTest.java"
--8<-- "code/java/test/junit/junit5-demo/src/test/java/com/luguosong/junit5demo/assume/AssumingThatTest.java"
```

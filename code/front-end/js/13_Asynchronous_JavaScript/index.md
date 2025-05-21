# 异步编程

## 使用回调的异步编程

### 延时任务

`setTimeout`用于在指定的时间后执行一次函数。它接受两个参数：要执行的函数和延迟的时间（以毫秒为单位）。

``` html
--8<-- "code/front-end/js/13_Asynchronous_JavaScript/timeout.html"
```

<iframe loading="lazy" src="timeout.html"></iframe>

### 定时任务

`setInterval`用于在指定的时间间隔内重复执行一次函数。它接受两个参数：要执行的函数和间隔时间（以毫秒为单位）。

可以使用`clearInterval`来停止定时任务。

``` html
--8<-- "code/front-end/js/13_Asynchronous_JavaScript/interval.html"
```

<iframe loading="lazy" src="interval.html"></iframe>

### 事件

通过`addEventListener`来注册回调函数，当指定事件发时会调用这些函数。

``` html
--8<-- "code/front-end/js/13_Asynchronous_JavaScript/event.html"
```

<iframe loading="lazy" src="event.html"></iframe>

### 网络请求

``` html
--8<-- "code/front-end/js/13_Asynchronous_JavaScript/request.html"
```

<iframe loading="lazy" src="request.html"></iframe>

## 期约(Promise)

### 回调地狱问题

- 基于`回调`的异步编程会出现回调`多层嵌套`的情况，导致代码缩进过多难以阅读。
- 回调函数中发生异常，异步的发起者无法获取和处理异常。

``` html
--8<-- "code/front-end/js/13_Asynchronous_JavaScript/callback_hell.html"
```

<iframe loading="lazy" src="callback_hell.html"></iframe>

### 期约概述

`期约(Promise)`是为了简化异步编程。

期约是一个对象，表示异步操作的结果。

相比于回调处理异步操作，期约做出如下改进：

- 期约可以让回调地狱以一种更线性的`期约链`形式表达出来。
- 期约标准化了异常处理，通过期约链提供了一种让错误正确传播的途径。

!!! warning

    不能使用期约替代`setInterval`。期约不能表示重复的异步计算，这并不是期约所考虑的用例。

    也不应该使用期约替代事件，事件往往可以重复触发，不需要等待上一条事件执行完毕。

### 示例

### 期约链

期约链更容易表达一连串异步操作。

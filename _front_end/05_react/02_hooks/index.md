---
layout: note
title: hooks
nav_order: 20
parent: React
create_time: 2023/7/13
---

# 概述

`ReactHooks`是React16.8版本中提出的新特性，它可以让你在不编写class的情况下使用state以及其他的React特性。它的目的是解决常年以来在class组件中存在的各种问题，实现更高效的编写react组件。

# useState-状态

{% highlight react %}
{% include_relative hooks-example/src/UseStateExample.js %}
{% endhighlight %}

# useEffect-副作用

`useEffect`允许您在函数组件中执行副作用。副作用是指那些不属于组件渲染的操作，例如数据获取、手动更改 DOM、设置订阅和计时器等。

- 参数1：回调函
- 数参数2：依赖项数组，当数组中的值发生变化时，才会执行回调函数

useEffect可以模拟生命周期中的`componentDidMount`，`componentDidUpdate`，`componentWillUnmount`三个生命周期函数。

{% highlight react %}
{% include_relative hooks-example/src/UseEffectExample.js %}
{% endhighlight %}

# useCallback-记忆函数

作用：使得函数只在依赖项更新时更新

{% highlight react %}
{% include_relative hooks-example/src/UseCallbackExample.js %}
{% endhighlight %}

# useMemo-缓存计算的结果

类似于Vue中的计算属性，当依赖项更新时，才会重新计算

{: .warning}
> useCallback用于缓存函数，而useMemo用于缓存计算结果。

{% highlight react %}
{% include_relative hooks-example/src/UseMemoExample.js %}
{% endhighlight %}

# useRef-获取DOM元素

{% highlight react %}
{% include_relative hooks-example/src/UseRefExample.js %}
{% endhighlight %}

# useContext-跨层级传递数据

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202307181457275-react-useContext.png)

# useReducer-替代redux

在组件外部进行状态管理。

{% highlight react %}
{% include_relative hooks-example/src/UseReducerExample.js %}
{% endhighlight %}

useReducer和useContext搭配使用：

{% highlight react %}
{% include_relative hooks-example/src/UseReducerAndUseContextExample.js %}
{% endhighlight %}

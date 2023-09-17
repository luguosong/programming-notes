---
layout: note
title: 数组
nav_order: 20
parent: JavaSE
latex: true
create_time: 2023/9/15
---

# 概述

数组是一个`容器对象`，它可以容纳`固定数量`的`同一类型`的值。数组的`长度在创建数组时确定`，创建后其`长度是固定的`。

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202309171620224.gif)

数组中的每个项目称为一个`元素`，每个元素通过其数字`索引`访问。如前面的示例所示，编号从0开始。因此，第9个元素将在索引8处访问。

```java
class ArrayDemo {
    public static void main(String[] args) {
        //可以使用简化的语法来创建和初始化数组
        int[] anArray1 = {
                100, 200, 300,
                400, 500, 600,
                700, 800, 900, 1000
        };

        // 声明一个整数数组
        int[] anArray;

        // 为10个整数分配内存空间
        anArray = new int[10];

        // 初始化第一个元素
        anArray[0] = 100;
        // 初始化第二个元素
        anArray[1] = 200;
        // 依此类推
        anArray[2] = 300;
        anArray[3] = 400;
        anArray[4] = 500;
        anArray[5] = 600;
        anArray[6] = 700;
        anArray[7] = 800;
        anArray[8] = 900;
        anArray[9] = 1000;

        System.out.println("索引 0 处的元素: " + anArray[0]);
        System.out.println("索引 1 处的元素: " + anArray[1]);
        System.out.println("索引 2 处的元素: " + anArray[2]);
        System.out.println("索引 3 处的元素: " + anArray[3]);
        System.out.println("索引 4 处的元素: " + anArray[4]);
        System.out.println("索引 5 处的元素: " + anArray[5]);
        System.out.println("索引 6 处的元素: " + anArray[6]);
        System.out.println("索引 7 处的元素: " + anArray[7]);
        System.out.println("索引 8 处的元素: " + anArray[8]);
        System.out.println("索引 9 处的元素: " + anArray[9]);
    }
}
```

输出结果为：

```shell
索引 0 处的元素: 100
索引 1 处的元素: 200
索引 2 处的元素: 300
索引 3 处的元素: 400
索引 4 处的元素: 500
索引 5 处的元素: 600
索引 6 处的元素: 700
索引 7 处的元素: 800
索引 8 处的元素: 900
索引 9 处的元素: 1000
```

# 数组复制

代码示例：

{% highlight java %}
{% include_relative ArrayCopyDemo.java %}
{% endhighlight %}

输出结果为：

```shell
Cappuccino Corretto Cortado Doppio Espresso Frappucino Freddo  
Cappuccino Corretto Cortado Doppio Espresso Frappucino Freddo 
```

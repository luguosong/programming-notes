---
layout: note
title: 面向对象
nav_order: 30
parent: JavaSE
latex: true
create_time: 2023/9/15
---

# 什么是对象

软件对象在概念上类似于现实世界中的对象：它们也由`状态`和`相关行为`组成。

一个对象将其`状态`存储在`字段`中（在某些编程语言中为变量），并通过`方法`（在某些编程语言中为函数）暴露其行为。

`⭐封装`：`方法`操作对象的内部`状态`，并作为对象之间通信的主要机制。`隐藏内部状态`并要求所有交互都通过对象的`方法`
执行被称为`数据封装`，这是面向对象编程的基本原则之一。

将代码打包成独立的软件对象提供了许多优点，包括：

- `模块化`：对象的源代码可以独立编写和维护，与其他对象的源代码无关。一旦创建，对象可以在系统内部轻松传递。
- `信息隐藏`：通过仅与对象的`方法`进行交互，对象`内部实现的细节对外部世界保持隐藏`。
- `代码重用`：如果某个对象已经存在（可能是由另一位软件开发者编写），你可以在自己的程序中使用该对象。这允许专家实现/测试/调试复杂的、特定任务的对象，然后你可以信任它们在你自己的代码中运行。
- `可插拔性和便于调试`：如果某个特定对象出现问题，你可以简单地将其从应用程序中移除，并插入另一个对象作为替代。这类似于在现实世界中修复机械问题。如果螺栓断了，你只需要替换它，而不是整个机器。

# 什么是类

`类`是创建单个对象的蓝图。

# 类的声明

一般而言，类的声明可以包括以下组成部分，按顺序排列：

- `修饰符`，例如 public、private，以及其他一些稍后会遇到的修饰符。（但需要注意的是，private修饰符只能应用于嵌套类。）
- `类名`，按照约定以大写字母开头。
- `父类`（超类）的名称（如果有的话），前面加上关键字 extends。一个类只能继承（子类化）一个父类。
- 由关键字 implements 开始的`接口`名称列表，如果有的话。一个类可以实现多个接口。
- `类体`，由大括号 {} 包围起来。

```java
/*
 * MyClass 是 MySuperClass 的子类，并且实现了 YourInterface 接口。
 * */
public class MyClass extends MySuperClass implements YourInterface {
    // field, constructor, and
    // method declarations
}
```

# 成员变量

字段声明由三个组成部分组成，按顺序排列：

- 零个或多个`修饰符`，如 public 或 private。
- 字段的`类型`。
- 字段的`名称`。

{: .note}
> 为了`封装的原则`，通常会将字段设置为私有。然而，我们仍然需要访问这些值。可以通过添加公共方法来间接地获取字段的值。

# 方法

更一般地说，方法声明包括六个组成部分，按顺序排列如下：

- `修饰符` - 例如 public、private 等，还有一些稍后会学到的修饰符。
- `返回类型` - 方法返回的值的数据类型，如果方法不返回值，则为 void。
- `方法名称` - 方法名称遵循字段名称的规则，但约定略有不同。
- `参数列表在括号内` - 由逗号分隔的输入参数列表，前面是它们的数据类型，括在括号 () 内。如果没有参数，必须使用空括号。
- `异常列表` - 将在以后讨论。
- `方法体` - 在大括号中包围的方法代码，包括局部变量的声明等。

{: .note}
> 方法声明的两个组成部分构成了`方法签名`,即`方法的名称`和`参数类型`。

Java编程语言支持`方法重载`
，Java能够区分具有不同方法签名的方法。这意味着在同一个类中的方法如果具有不同的参数列表，它们可以具有相同的名称。应尽量少用`重载方法`
，因为它们会大大降低代码的可读性。

# 构造方法

类包含`构造函数`，用于从类蓝图创建对象。构造函数的声明看起来像方法声明，只是它们`使用类的名称`，并且`没有返回类型`。

当不手动编写构造函数，编译器会`自动`为任何没有构造函数的类提供一个`无参数的默认构造函数`
。这个默认构造函数将`调用超类的无参数构造函数`。在这种情况下，`如果超类没有无参数构造函数，编译器将会报错`
，因此您必须确认它确实有这个构造函数。如果您的类没有显式的超类，那么它具有隐式的超类Object，而Object类确实具有一个无参数构造函数。

# 参数传递

`传递基本数据类型`：通过`值`传递到方法中。这意味着对参数值的任何更改仅存在于方法的范围内。当方法返回时，参数将被销毁，对它们的任何更改都会丢失。不会影响调用方法的代码中的原始变量。

`传递引用数据类型`：通过`引用`传递到方法中。这意味着当对参数进行更改时，调用代码中的原始变量将反映出这些更改。

# this关键字

使用关键字 "this" 的最常见原因是因为字段被方法或构造函数参数遮蔽：

```java
public class Point {
    public int x = 0;
    public int y = 0;

    //constructor
    //当参数名称与字段名称相同时，可以使用关键字 this 来引用字段。
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
```

在构造函数内部，您还可以使用关键字 "this" 来调用同一类中的另一个构造函数。这样做被称为`显式构造函数调用`：

```java
public class Rectangle {
    private int x, y;
    private int width, height;

    public Rectangle() {
        //调用另一个构造函数
        this(0, 0, 1, 1);
    }

    public Rectangle(int width, int height) {
        //调用另一个构造函数
        this(0, 0, width, height);
    }

    public Rectangle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    //...
}
```

# 控制访问类成员

## 修饰类

- `public`：类是公共的，可以在任何地方被访问。
- `无修饰符`:类是包私有的，它只能被同一个包中的代码访问。

NoModifierClass类不使用public进行修饰：

{% highlight java %}
{% include_relative controlling_access/class_demo/class_package/NoModifierClass.java %}
{% endhighlight %}

同一个包下的类可以访问NoModifierClass类：

{% highlight java %}
{% include_relative controlling_access/class_demo/class_package/NoModifierClassTest.java %}
{% endhighlight %}

不同包下的类无法访问NoModifierClass类：

{% highlight java %}
{% include_relative controlling_access/class_demo/NoModifierClassTest.java %}
{% endhighlight %}

## 修饰成员

- `public`：成员是公共的，可以在任何地方被访问。
- `protected`：只能在其自己的包内访问，还可以被`另一个包`中的其类的`子类`访问。
- `无修饰符`：只能在其自己的包内访问。
- `private`：成员仅在其声明的类中可见。

⭐主要注意`protected`和`无修饰符`的区别。

# 类成员（静态成员）

## 静态变量

具有`static修饰符`的字段称为静态字段或类变量。它们`与类相关联`，而不是与任何对象相关联。类的每个实例`共享一个类变量`
，该变量位于内存中的一个固定位置。

任何对象都可以更改类变量的值，但也可以在不创建类的实例的情况下操作类变量。

类变量由`类名`本身引用，不建议使用对象引用，因为这样会让人误以为它是实例变量。

## 静态方法

拥有static修饰符的静态方法在声明中，应该使用类名来调用，无需创建类的实例。

同理，也可以使用对象引用来引用静态方法，但这种做法并不可取，因为它没有明确说明它们是类方法。

静态方法的一个常见用途是`访问静态字段`。

{: .warning}
> 类方法不能直接访问实例变量或实例方法--它们必须使用对象引用。此外，类方法不能使用 this 关键字，因为 this 没有实例可以引用。

## 常量

`static修饰符`与`final修饰符`结合使用用于定义常量。 `final修饰符`表示该字段的值`不能更改`。

常量值的名称用大写字母拼写。如果名称由多个单词组成，则使用下划线字符`_`将它们分开。

```java
class Demo {
    static final double PI = 3.141592653589793;
}
```

# 字段初始化

静态字段初始化：

- `声明时初始化`
- `静态代码块`：类加载时执行，且仅执行一次。一般用于初始化静态变量。
- `私有静态方法初始化`：如果以后需要重新初始化类变量，可以重复使用这些方法。

实例字段初始化：

- `声明时初始化`
- `构造函数初始化`：用于处理复杂逻辑的初始化
- `实例代码块`：Java编译器将初始化块复制到每个构造函数中。因此，这种方法可以用于在多个构造函数之间共享一段代码。
- `final方法`:final方法不能在子类中被覆盖。这样的方法可以安全地用于初始化类变量。

```java
class Whatever {
    private varType myVar = initializeInstanceVariable();

    // final方法进行代码初始化
    protected final varType initializeInstanceVariable() {

        // initialization code goes here
    }
}
```

# 嵌套类

`非静态嵌套类（内部类）`可以访问外层类的其他成员，即使它们被声明为私有。由于`内部类`
与实例相关联，因此它本身`不能定义任何静态成员`。

`静态嵌套类`不能访问外层类的其他成员。

```java
class OuterClass {
    //内部类
    class InnerClass {
        //...
    }

    //静态嵌套类
    static class StaticNestedClass {
        //...
    }
}

class Demo {
    public static void main(String[] args) {
        //创建内部类实例对象
        OuterClass outerObject = new OuterClass(); //必须先创建外部类对象
        OuterClass.InnerClass innerObject = outerObject.new InnerClass();

        //创建静态嵌套类实例对象
        OuterClass.StaticNestedClass nestedObject = new OuterClass.StaticNestedClass();
    }
}
```

嵌套类同名情况：

```java
public class ShadowTest {

    public int x = 0;

    class FirstLevel {

        public int x = 1;

        void methodInFirstLevel(int x) {
            System.out.println("x = " + x);
            System.out.println("this.x = " + this.x);
            System.out.println("ShadowTest.this.x = " + ShadowTest.this.x);
        }
    }

    public static void main(String... args) {
        ShadowTest st = new ShadowTest();
        ShadowTest.FirstLevel fl = st.new FirstLevel();
        fl.methodInFirstLevel(23);
    }
}
```

执行结果：

```shell
x = 23
this.x = 1
ShadowTest.this.x = 0
```

{: .warning}
> 强烈不推荐对内部类进行`序列化`，包括局部类和匿名类。
>
> 当Java编译器编译某些结构，如内部类时，它会创建合成结构；这些结构包括类、方法、字段和其他在源代码中没有对应构造的结构。合成结构使Java编译器能够实现新的Java语言特性，而无需更改JVM。
>
> 然而，合成结构在不同的Java编译器实现之间可能会有所不同，这意味着不同实现之间的.class文件也可能会有所不同。因此，如果您对内部类进行序列化，然后在不同的JRE实现中对其进行反序列化，可能会出现兼容性问题。

# 局部类

`局部类`是在`块`中定义的类，该块是位于成对大括号之间的零个或多个语句的组。通常情况下，你会在`方法体`中定义局部类。

`局部类`可以访问其`外层类`的成员（`字段`和`方法`），并且可以对字段进行`修改`。

`局部类`可以访问`局部变量`。然而，局部类只能访问声明为`final`的局部变量。从Java SE
8开始，这些变量和参数要么是final的，要么是有效地final的。一个值在初始化后永远不会改变的变量或参数是有效地final的。

{: .note-title}
> 局部类使用的局部变量为什么需要是有效final的？
>
> 这是因为局部类的生命周期可以超出包含它的方法的生命周期，因此局部类可能在方法执行完毕后仍然存在，而它所访问的局部变量在方法执行完毕后可能已经被销毁。
>
>当一个局部变量被声明为final时，它的值不能被修改。这使得编译器可以在`编译时确定这个变量的值`
> ，并且可以将这个值存储在局部类的实例中，以便在方法执行完毕后局部变量仍然可以被访问。
> 如果局部变量不是final或者等效于final，那么在方法执行完毕后，它的值可能会被销毁或者发生变化，这将导致局部类访问到不确定的值，可能会引发错误或不一致的行为。

{% highlight java %}
{% include_relative LocalClassesDemo.java %}
{% endhighlight %}

# 匿名类

`匿名类`允许您使代码更加简洁。它们允许您同时`声明`和`实例化`一个类。它们类似于`局部类`
，但没有名称。如果您`只需要在一个地方使用局部类`，可以使用`匿名类`。

匿名类表达式由以下部分组成：

- `new运算符`：表示要创建一个新的对象。
- `要实现的接口的名称或要扩展的类的名称`：在这个示例中，匿名类实现了接口HelloWorld。
- `括号`：包含传递给构造函数的参数，就像普通的类实例创建表达式一样。请注意：当实现接口时，通常没有构造函数，所以您使用一个空的括号对，就像这个示例中一样。
- `类体`：类体是一个类声明的主体。更具体地说，在类体中允许方法声明，但不允许语句。

{% highlight java %}
{% include_relative AnonymousClassDemo.java %}
{% endhighlight %}

# Lambda表达式

`匿名类`的一个问题是，如果匿名类的实现非常简单，比如一个只包含一个方法的接口，那么匿名类的语法可能会显得笨重和不清晰。

对于只有一个方法的类来说,可以使用`Lambda表达式`来代替匿名类。

{% highlight java %}
{% include_relative LambdaDemo.java %}
{% endhighlight %}

# 枚举类型（Enum）

## 手写枚举类

JavaSE5.0之前，需要自己手写枚举类。

{% highlight java %}
{% include_relative EnumClassDemo.java %}
{% endhighlight %}

## enum关键字创建枚举

简单的枚举类：

{% highlight java %}
{% include_relative EnumDemo2.java %}
{% endhighlight %}

自定义构造函数：

{% highlight java %}
{% include_relative EnumDemo.java %}
{% endhighlight %}

{: .warning}
> 所有枚举类型都隐式地扩展了`java.lang.Enum`。因为一个类只能扩展一个父类（参见声明类），Java语言不支持状态的多重继承（参见状态、实现和类型的多重继承），因此枚举不能扩展其他任何内容。

# 注解

## 概述

{: .note-title}
> 注解
> 
> `注解`是`Java 5`的一项重要语言更新。
> 
> 又叫作`元数据`,使我们可以用正式的方式为代码添加信息，这样就可以在将来方便地使用这些数据。

可以使用注解的地方：
- `声明`：注解可以应用于类、字段、方法和其他程序元素的声明。
- `类型使用`：截至`Java SE 8`发布版本，注解也可以应用于类型的使用。

作用：
- `编译器的信息`：注解可用于由编译器检测错误或抑制警告。
- `编译时和部署时处理`：软件工具可以处理注解信息以生成代码、XML文件等。
- `运行时处理`：一些注解可在运行时进行检查。

## 常见注解

- `@Deprecated`：标记过时的方法。
- `@Override`：标记方法覆盖。
- `@SuppressWarnings`：抑制编译器警告。

# 创建对象

创建对象分为以下三个部分：

- `声明（Declaration）`：将变量名与对象类型关联起来。
- `实例化（Instantiation）`：new 关键字是Java中创建对象的操作符。
- `初始化（Initialization）`：new 操作符后面跟着对构造函数的调用，用于初始化新对象。

```java
class Demo {
    Point originOne = new Point(23, 94);
    Rectangle rectOne = new Rectangle(originOne, 100, 200);
    Rectangle rectTwo = new Rectangle(50, 100);
}
```

# 什么是继承

面向对象编程允许类从其他类继承常用的`状态`和`行为`。

在Java编程语言中，每个类允许拥有`一个直接的超类`(单继承)，而每个超类有潜力拥有无限数量的子类：

```java
class MountainBike extends Bicycle {
    // 定义新的字段和方法，用于描述
}
```

# 什么是接口

对象通过它们所暴露的`方法`来定义与外部世界的交互。`方法`构成了对象与外部世界的`接口`。

实现一个`接口`使得一个`类对其承诺提供的行为更加明确`。接口在类和外部世界之间形成了一份`契约`，而这份契约由编译器在构建时强制执行。

如果你的类声称实现了一个`接口`，那么在类成功编译之前，该接口定义的所有方法必须出现在类的源代码中。

# 什么是软件包

`包（Package）`是一个命名空间，用来组织一组相关的类和接口。从概念上来说，你可以将包类比于计算机上的不同文件夹。

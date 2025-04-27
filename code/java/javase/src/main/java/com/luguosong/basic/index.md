# 语言基础

## 变量(Variables)

### 变量概述

对象将其状态存储在`字段`中。

``` java
int cadence = 0;
int speed = 0;
int gear = 1;
```

在Java编程语言中，`字段`和`变量`这两个术语都会使用，这常常让新开发者感到困惑，因为它们似乎都指的是同一件事。

Java 编程语言定义了以下几种变量：

- `实例变量（Instance Variables,非静态字段）`:
  从技术上讲，对象将其各自的状态存储在“非静态字段”中，即那些没有用static关键字声明的字段。非静态字段也被称为实例变量，因为它们的值对于类的每个实例（换句话说，对于每个对象）都是独一无二的；一辆自行车的currentSpeed与另一辆自行车的currentSpeed是相互独立的。
- `类变量（Class Variables,静态字段）`:
  类变量是任何用static修饰符声明的字段；这告诉编译器，无论类被实例化多少次，这个变量都只有一个副本存在。定义特定类型自行车齿轮数量的字段可以标记为static，因为从概念上讲，相同的齿轮数量适用于所有实例。代码static
  int numGears = 6;将创建这样一个静态字段。此外，可以添加关键字final来表示齿轮数量永远不会改变。
- `局部变量(Local Variables)`:类似于对象在字段中存储其状态，方法通常会在局部变量中存储其临时状态。声明局部变量的语法与声明字段类似（例如，int
  count = 0;）。没有特殊的关键字将变量指定为局部变量；这种判断完全取决于变量声明的位置——即在方法的开括号和闭括号之间。因此，局部变量仅对声明它们的方法可见；它们无法从类的其他部分访问。
- `参数(Parameters)`：主方法的签名是public static void main(String[] args)
  。这里，args变量就是这个方法的参数。重要的是要记住，参数始终被归类为“变量”而不是“字段”。这一点同样适用于其他接受参数的结构（如构造函数和异常处理器）。

话虽如此，本教程余下部分在讨论字段和变量时将遵循以下一般指南。如果我们在谈论“字段”（不包括局部变量和参数），我们可能会简单地说“字段”。如果讨论适用于“以上所有”，我们可能会简单地说“变量”。如果需要区分，我们将根据需要使用具体术语（静态字段、局部变量等）。您可能还会偶尔看到使用“成员”这个术语。一个类型的字段、方法和嵌套类型统称为其成员。

### 变量命名规范

每种编程语言都有其特定的规则和约定来规定可以使用的名称类型，Java编程语言也不例外。命名变量的规则和约定可以总结如下：

- 变量名是`区分大小写`的。变量名可以是任何合法的标识符——一个以字母、美元符号`$`或下划线`_`开头的Unicode字母和数字的无限长度序列。然而，惯例是始终以
  `字母开头`，而不是`$`或`_`。此外，按照惯例，美元符号通常不使用。你可能会在某些情况下发现自动生成的名称中包含美元符号，但你的变量名应始终避免使用它。对于下划线字符也有类似的惯例；虽然从技术上讲可以用
  `_`开头，但这种做法不被推荐。`空白字符是不允许的`。
-

后续字符可以是字母、数字、美元符号或下划线字符。惯例（以及常识）同样适用于此规则。在为变量选择名称时，使用完整的单词而不是晦涩的缩写。这样做会使代码更易于阅读和理解。在许多情况下，这也会使代码具有自我文档化的功能；例如，命名为cadence、speed和gear的字段，比缩写版本如s、c和g更直观。此外，请记住，您选择的名称
`不能是关键字或保留字`。

- 如果您选择的名称仅由一个单词组成，请将该单词全部用`小写字母拼写`。如果由多个单词组成，请将每个`后续单词的首字母大写`
  。名称如gearRatio和currentGear就是这种命名惯例的典型例子。如果您的变量存储一个常量值，例如
  `static final int NUM_GEARS = 6`，惯例会稍有变化，将`每个字母大写`，并用`下划线`字符分隔后续单词。按照惯例，下划线字符不会在其他地方使用。

## 基本数据类型

### 概述

Java 编程语言是静态类型的，这意味着所有变量在使用之前必须先声明。这包括声明变量的类型和名称，如您所见：

``` java
int gear = 1;
```

这样做会告诉你的程序存在一个名为“gear”的字段，它保存数值数据，并且初始值为“1”。变量的数据类型决定了它可以包含的值以及可以对其执行的操作。除了int之外，Java编程语言还支持其他七种基本数据类型。基本类型是由语言预定义的，并由保留关键字命名。基本值不与其他基本值共享状态。Java编程语言支持的八种基本数据类型是：

- `byte`
  ：byte数据类型是一个8位有符号的二进制补码整数。其最小值为-128，最大值为127（包括127）。在大型数组中，byte数据类型可以用于节省内存，当内存节省确实重要时，这种类型非常有用。它们也可以替代int使用，当其限制有助于使代码更清晰时；变量范围有限的事实可以作为一种文档说明。
- `short`：short数据类型是一个16位有符号的二进制补码整数。其最小值为-32,768，最大值为32,767（包含）。与byte类型相同的原则适用：在内存节省确实重要的大型数组中，可以使用short来节省内存。
- `int`：默认情况下，int数据类型是一个32位有符号的二进制补码整数，最小值为$-2^{31}$，最大值为$2^{31}-1$。在Java SE
  8及更高版本中，可以使用int数据类型表示无符号32位整数，其最小值为0，最大值为$2^{32}-1$。要将int数据类型用作无符号整数，可以使用
  `Integer类`。为了支持无符号整数的算术运算，Integer类中添加了`compareUnsigned`、`divideUnsigned`等静态方法。
- `long`：long数据类型是一个64位的二进制补码整数。带符号的长整型最小值为$-2^{63}$，最大值为$2^{63}-1$。在Java SE
  8及更高版本中，可以使用long数据类型来表示无符号的64位长整型，其最小值为0，最大值为$2^{64}-1$
  。当需要比int提供的范围更大的数值时，可以使用这种数据类型。`Long类`还包含`compareUnsigned`、`divideUnsigned`
  等方法，以支持无符号长整型的算术操作。
- `float`：float数据类型是单精度的32位IEEE
  754浮点数。其数值范围超出了本次讨论的范围，但在Java语言规范的[浮点类型、格式和数值部分](https://docs.oracle.com/javase/specs/jls/se7/html/jls-4.html#jls-4.2.3)
  中有详细说明。与对byte和short的建议类似，如果需要在大型浮点数数组中节省内存，应使用float（而不是double）。这种数据类型不应用于精确数值，例如货币。对于这种情况，你需要使用
  `java.math.BigDecimal`类。Java平台提供的Numbers和Strings部分涵盖了BigDecimal和其他有用的类。
- `double`：double数据类型是双精度64位IEEE
  754浮点数。其数值范围超出了本次讨论的范围，但在Java语言规范的[浮点类型、格式和数值部分](https://docs.oracle.com/javase/specs/jls/se7/html/jls-4.html#jls-4.2.3)
  中有详细说明。对于十进制数值，这种数据类型通常是默认选择。如上所述，这种数据类型不应用于精确数值，例如货币。
- `boolean`：boolean数据类型只有两个可能的值：`true` 和 `false`。使用这种数据类型来表示简单的标志，用于跟踪真/假条件。该数据类型代表一位信息，但其
  `大小`并没有精确定义。
- `char`：char数据类型是一个16位的单一Unicode字符。其最小值为`\u0000`（或0），最大值为`\uffff`（或65,535，包含在内）。

除了上述八种基本数据类型外，Java 编程语言还通过 `java.lang.String` 类为字符字符串提供了特殊支持。将字符字符串用双引号括起来会自动创建一个新的
String 对象，例如，`String s = "this is a string"`;。String 对象是不可变的，这意味着一旦创建，其值就不能更改。虽然 String
类在技术上不是一种基本数据类型，但考虑到语言对其提供的特殊支持，你可能会倾向于将其视为基本数据类型。在简单数据对象中，你将进一步了解
String 类。

### 字段默认值

在声明字段时，不一定总是需要赋值。那些声明但未初始化的`字段`
将由编译器设置为一个合理的默认值。一般来说，这个默认值会是零或空值，具体取决于数据类型。然而，依赖这样的默认值通常被认为是不良的编程风格。

下表总结了上述数据类型的默认值。

| 数据类型      | 默认值        |
|:----------|:-----------|
| `byte`    | `0`        |
| `short`   | `0`        |
| `int`     | `0`        |
| `long`    | `0L`       |
| `float`   | `0.0f`     |
| `double`  | `0.0d`     |
| `char`    | `'\u0000'` |
| `String`  | `null`     |
| `boolean` | `false`    |

`局部变量`略有不同；编译器不会为未初始化的局部变量分配默认值。如果无法在声明时初始化局部变量，请确保在使用之前为其赋值。访问未初始化的局部变量将导致编译时错误。

## 字面量

您可能已经注意到，在初始化基本类型变量时不使用 `new 关键字`
。基本类型是内置于语言中的特殊数据类型，它们不是从类创建的对象。字面量是固定值的源代码表示；字面量直接在代码中表示，无需计算。如下所示，可以将字面量赋值给基本类型的变量：

``` java
boolean result = true;
char capitalC = 'C';
byte b = 100;
short s = 10000;
int i = 100000;
```

### 整数字面量

如果一个整数字面量以字母`L`或`l`结尾，则其类型为`long`；否则，其类型为`int`。建议使用大写字母`L`，因为小写字母`l`与数字`1`
难以区分。

可以通过整型字面量创建整型类型的值，包括 `byte`、`short`、`int` 和 `long`。超出 int 范围的 long 类型值可以通过 long
字面量创建。整数字面量可以用以下数字系统表示：

- `十进制`：以10为基数，其数字由0到9组成；这是你每天使用的数字系统。
- `十六进制`：以16为基数，其数字由0到9和字母A到F组成
- `二进制`：以2为基数，其数字由0和1组成（在Java SE 7及更高版本中可以创建二进制字面量）

对于通用编程，十进制系统可能是您唯一会使用的数字系统。然而，如果您需要使用其他数字系统，以下示例展示了正确的语法。前缀`0x`
表示十六进制，`0b`表示二进制：

``` java
// 十进制中的数字26
int decVal = 26;
//  数字26，用十六进制表示
int hexVal = 0x1a;
// 数字26的二进制表示
int binVal = 0b11010;
```

### 浮点数字面量

如果一个浮点字面量以字母F或f结尾，则其类型为float；否则，其类型为double，并且可以选择以字母D或d结尾。

浮点类型（float 和 double）也可以使用 E 或 e（科学计数法）、F 或 f（32位浮点字面量）以及 D 或 d（64位双精度字面量；这是默认值，通常省略）来表示。

``` java
double d1 = 123.4;
// 与d1相同的值，但以科学计数法表示
double d2 = 1.234e2;
float f1 = 123.4f;
```

### 字符和字符串字面量

字符和字符串类型的字面量可以包含任何Unicode（UTF-16）字符。如果你的编辑器和文件系统支持，你可以直接在代码中使用这些字符。如果不支持，你可以使用
`Unicode转义字符`，例如`\u0108`（带抑扬符的大写C），或`S\u00ED Se\u00F1or`（西班牙语中的`Sí Señor`）。字符字面量请始终使用`单引号`
，字符串字面量请使用`双引号`。Unicode转义序列可以在程序的其他地方使用（例如字段名称），不仅限于字符或字符串字面量。

Java 编程语言还支持一些用于字符和字符串字面量的特殊转义序列：`\b`（退格）、`\t`（制表符）、`\n`（换行）、`\f`（换页）、`\r`（回车）、
`\"`（双引号）、`\'`（单引号）和 `\\`（反斜杠）。

### 空值字面量

还有一个特殊的空值字面量，可以用作任何引用类型的值。`null` 可以赋给任何变量，除了基本类型的变量。除了检测其存在之外，null
值几乎没有其他用途。因此，null 常常在程序中用作标记，以指示某个对象不可用。

### 类字面量

最后，还有一种特殊的字面量称为类字面量，它是通过在类型名称后加上`.class`形成的；例如:`String.class`。这指的是表示该类型本身的对象（类型为
`Class`）。

### 数字字面量中使用下划线

在 Java SE 7 及更高版本中，数字字面量中的任意位置都可以插入任意数量的下划线字符:`_`。这一特性使您能够在数字字面量中分隔数字组，从而提高代码的可读性。

例如，如果你的代码中包含多位数字，可以使用下划线字符将数字按三位一组分隔，类似于使用逗号或空格作为分隔符。

以下示例展示了在数字字面量中使用下划线的其他方法：

``` java
long creditCardNumber = 1234_5678_9012_3456L;
long socialSecurityNumber = 999_99_9999L;
float pi = 3.14_15F;
long hexBytes = 0xFF_EC_DE_5E;
long hexWords = 0xCAFE_BABE;
long maxLong = 0x7fff_ffff_ffff_ffffL;
byte nybbles = 0b0010_0101;
long bytes = 0b11010010_01101001_10010100_10010010;
```

您只能在数字之间放置下划线；不能在以下位置放置下划线：

- 在数字的开头或结尾
- 浮点字面量中紧邻小数点
- 在 F 或 L 后缀之前
- 在需要一串数字的位置

以下示例展示了数字字面量中下划线的有效和无效放置（已突出显示）：

``` java
// ❌无效：不能在小数点旁边使用下划线
float pi1 = 3_.1415F;
// ❌无效：不能在小数点旁边放置下划线
float pi2 = 3._1415F;
// ❌无效：不能在L后缀前加下划线
long socialSecurityNumber1 = 999_99_9999_L;

// OK（十进制字面量）
int x1 = 5_2;
// ❌无效：文字末尾不能加下划线
int x2 = 52_;
// OK（十进制字面量）
int x3 = 5_______2;

// ❌无效：不能在 0x 前缀中使用下划线
int x4 = 0_
x52;
// ❌无效：数字开头不能加下划线
int x5 = 0x_52;
// OK（十六进制字面量）
int x6 = 0x5_2;
// ❌无效：数字末尾不能加下划线
int x7 = 0x52_;
```

## 数组

### 数组概述

数组是一种容器对象，用于存储固定数量的单一类型的值。数组的长度在创建时确定，创建后其长度是固定的。在`Hello World!`
应用程序的主方法中，你已经见过数组的例子。本节将更详细地讨论数组。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202412101715066.png){ loading=lazy }
  <figcaption>一个包含10个元素的数组。</figcaption>
</figure>

数组中的每个项目称为元素，每个元素通过其数字索引进行访问。如前图所示，编号从0开始。例如，第9个元素将通过索引8进行访问。

以下程序 ArrayDemo 创建了一个整数数组，将一些值放入数组中，并将每个值打印到标准输出。

``` java title="ArrayDemo.java"
--8<-- "code/java/javase/src/main/java/com/luguosong/basic/ArrayDemo.java"
```

该程序的输出是：

```shell
索引0处的元素: 100
索引1处的元素: 200
索引2处的元素: 300
索引3处的元素: 400
索引4处的元素: 500
索引5处的元素: 600
索引6处的元素: 700
索引7处的元素: 800
索引8处的元素: 900
索引9处的元素: 1000
```

在实际编程中，你可能会使用支持的循环结构来遍历数组中的每个元素，而不是像前面的例子那样逐行编写。不过，这个例子清楚地展示了数组的语法。你将在控制流部分学习各种循环结构（如
`for`、`while` 和 `do-while`）。

### 声明一个变量以引用数组

前面的程序通过以下代码行声明了一个数组（名为 anArray）：

``` java
// 声明一个整数数组 
int[] anArray;
```

与其他类型的变量声明类似，数组声明也有两个组成部分：`数组的类型`和`数组的名称`。

- `数组的类型`写作 `type[]`，其中 `type` 是所包含元素的数据类型；方括号是特殊符号，表示该变量存储的是一个数组。数组的大小不是其类型的一部分（这就是为什么方括号是空的）。
- `数组的名称`可以是任何你想要的，只要它遵循命名部分中讨论的规则和惯例。与其他类型的变量一样，声明并不会实际创建数组；它只是告诉编译器这个变量将存储一个指定类型的数组。

同样，你也可以声明其他类型的数组：

``` java
byte[] anArrayOfBytes;
short[] anArrayOfShorts;
long[] anArrayOfLongs;
float[] anArrayOfFloats;
double[] anArrayOfDoubles;
boolean[] anArrayOfBooleans;
char[] anArrayOfChars;
String[] anArrayOfStrings;
```

您也可以将方括号放在数组名称之后：

``` java
// 不建议使用此格式。
float anArrayOfFloats[];
```

不过，惯例不鼓励这种形式；方括号用于标识数组类型，应该与类型说明一起出现。

### 创建、初始化和访问数组

一种创建数组的方法是使用 `new 运算符`。在 ArrayDemo 程序中，下一条语句分配了一个具有足够内存以容纳 10 个整数元素的数组，并将该数组赋值给变量
anArray。

``` java
// 创建一个整数数组
anArray =new int[10];
```

如果缺少此语句，编译器将打印如下错误，并且编译失败：

```shell
ArrayDemo.java:4: 变量 anArray 可能尚未初始化。
```

接下来的几行代码为数组的每个元素赋值：

``` java
anArray[0]=100; // initialize first element
anArray[1]=200; // initialize second element
anArray[2]=300; // and so forth
```

每个数组元素通过其数字索引进行访问：

``` java
System.out.println("索引0处的元素: "+anArray[0]);
System.out.

println("索引1处的元素: "+anArray[1]);
System.out.

println("索引2处的元素: "+anArray[2]);
```

或者，您可以使用快捷语法来创建和初始化数组：

``` java
int[] anArray = {
		100, 200, 300,
		400, 500, 600,
		700, 800, 900, 1000
};
```

这里数组的长度由大括号中提供的值的数量决定，值之间用逗号分隔。

最后，您可以使用内置的 length 属性来确定任何数组的大小。以下代码将数组的大小打印到标准输出：

``` java
System.out.println(anArray.length);
```

### 复制数组

System 类有一个 arraycopy 方法，可以用来高效地将数据从一个数组复制到另一个数组：

``` java
public static void arraycopy(Object src,
							 int srcPos,
							 Object dest,
							 int destPos,
							 int length);
```

两个对象参数分别指定要复制的`源数组`和`目标数组`。三个整数参数分别指定`源数组的起始位置`、`目标数组的起始位置`，以及要
`复制的数组元素数量`。

``` java title="ArrayCopyDemo.java"
--8<-- "code/java/javase/src/main/java/com/luguosong/basic/ArrayCopyDemo.java"
```

输出结果为：

```shell
Cappuccino Corretto Cortado Doppio Espresso Frappucino Freddo 
```

以下程序 ArrayCopyDemo 声明了一个字符串元素数组。它使用 System.arraycopy 方法将数组中的一个子序列复制到第二个数组中：

### java.util.Arrays工具类

数组是编程中一个强大且有用的概念。Java SE 提供了一些方法来执行与数组相关的常见操作。例如，ArrayCopyDemo 示例使用 System 类的
arraycopy 方法，而不是手动遍历源数组的元素并将每个元素放入目标数组。这些操作在后台完成，使开发者只需一行代码即可调用该方法。

为了方便起见，Java SE 在 `java.util.Arrays` 类中提供了几种用于执行数组操作的方法（常见任务，如复制、排序和搜索数组）。例如，可以修改之前的示例以使用
java.util.Arrays 类的 `copyOfRange 方法`，正如您在 ArrayCopyOfDemo 示例中所看到的。不同之处在于，使用 copyOfRange
方法不需要在调用方法之前创建目标数组，因为目标数组是由该方法返回的：

``` java title="ArrayCopyOfDemo2.java"
--8<-- "code/java/javase/src/main/java/com/luguosong/basic/ArrayCopyOfDemo2.java"
```

正如你所见，这个程序的输出是相同的，尽管它需要的代码行更少。注意，copyOfRange方法的第二个参数是要复制范围的初始索引（包含），而第三个参数是要复制范围的最终索引（不包含）。在这个例子中，要复制的范围不包括索引为9的数组元素（其中包含字符串Lungo）。

java.util.Arrays 类中的方法提供了一些其他有用的操作：

- 在数组中搜索特定值以获取其所在的索引位置（`binarySearch 方法`）。
- 比较两个数组以确定它们是否相等（`equals方法`）。
- 填充数组以在每个索引位置放置特定值（`fill 方法`）。
- 将数组按升序排序。这可以通过顺序方式使用`sort方法`来完成，也可以通过Java SE 8引入的`parallelSort方法`
  并发完成。在多处理器系统上，对大型数组进行并行排序比顺序排序更快。
- 创建一个以数组为源的流（使用 `stream 方法`）。例如，以下语句以与前一个示例相同的方式打印 copyTo 数组的内容：

  `java.util.Arrays.stream(copyTo).map(coffee -> coffee + " ").forEach(System.out::print);  `

- 将数组转换为字符串。toString 方法将数组的每个元素转换为字符串，用逗号分隔，然后用方括号括起来。例如，以下语句将 copyTo
  数组转换为字符串并打印出来：

  `System.out.println(java.util.Arrays.toString(copyTo)); `

  此语句打印如下内容：

  `[Cappuccino, Corretto, Cortado, Doppio, Espresso, Frappucino, Freddo] `

### 多维数组

您还可以通过使用两个或多个括号来声明一个数组的数组（也称为多维数组），例如 `String[][] names`。因此，每个元素必须通过相应数量的索引值来访问。

在 Java 编程语言中，`多维数组`是其元素本身也是数组的数组。这与 C 或 Fortran 中的数组不同。其结果是，数组的行可以有不同的长度，如以下的
MultiDimArrayDemo 程序所示：

``` java title="MultiDimArrayDemo.java"
--8<-- "code/java/javase/src/main/java/com/luguosong/basic/MultiDimArrayDemo.java"
```

该程序的输出是：

```shell
Mr. Smith
Ms. Jones
```

## 运算符(Operators)概述

### 运算符概述

现在你已经学会了如何声明和初始化变量，可能想知道如何对它们进行操作。学习 Java
编程语言的运算符是一个不错的起点。运算符是执行特定操作的特殊符号，可以作用于一个、两个或三个操作数，然后返回结果。

在我们探索 Java
编程语言的运算符时，提前了解哪些运算符具有最高优先级可能会对你有所帮助。下表中的运算符是按照优先级顺序列出的。越靠近表格顶部的运算符，其优先级越高。优先级较高的运算符会在优先级相对较低的运算符之前进行计算。同一行的运算符具有相同的优先级。当相同优先级的运算符出现在同一表达式中时，必须有规则来决定哪个先计算。除赋值运算符外，所有二元运算符都从左到右计算；赋值运算符则从右到左计算。

| 运算符   | 优先级                                     |
|-------|-----------------------------------------|
| 后缀    | expr++ expr--                           |
| 一元运算符 | ++expr --expr +expr -expr ~ !           |
| 乘法    | * / %                                   |
| 加法    | + -                                     |
| 位移    | << >> >>>                               |
| 关系    | < > <= >= instanceof                    |
| 等式    | == !=                                   |
| 按位与   | &                                       |
| 按位异或  | ^                                       |
| 按位或   | \|                                      |
| 逻辑与   | &&                                      |
| 逻辑或   | \|\|                                    |                              |
| 三元运算符 | ? :                                     |
| 赋值    | = += -= *= /= %= &= ^= \|= <<= >>= >>>= |

在通用编程中，某些运算符比其他运算符出现得更频繁；例如，赋值运算符“=”比无符号右移运算符“>>>
”常见得多。考虑到这一点，以下讨论首先关注您最有可能经常使用的运算符，最后关注那些不太常见的运算符。每个讨论都附有可以编译和运行的示例代码。研究其输出将有助于巩固您刚刚学到的知识。

### 简单赋值运算符

你会经常遇到的一个最常见的运算符是简单赋值运算符 "="。你在自行车类中已经见过这个运算符；它将右侧的值赋给左侧的操作数：

``` java
 int cadence = 0;
int speed = 0;
int gear = 1;
```

正如在创建对象中所讨论的，这个运算符也可以用于对象上以分配对象引用。

### 算术运算符

Java 编程语言提供了用于执行加法、减法、乘法和除法的运算符。你很可能会通过它们在基础数学中的对应符号认出它们。唯一可能对你来说比较新的符号是“%”，它用于将一个操作数除以另一个操作数，并返回余数作为结果。

| 操作符 | 描述              |
|-----|-----------------|
| +   | 加法运算符（也用于字符串连接） |
| -   | 减法运算符           |
| *   | 乘法运算符           |
| /   | 除法运算符           |
| %   | 余数运算符           |

以下程序 ArithmeticDemo 测试算术运算符。

``` java title="ArithmeticDemo.java"
--8<-- "code/java/javase/src/main/java/com/luguosong/basic/ArithmeticDemo.java"
```

该程序打印以下内容：

```shell
1 + 2 = 3
3 - 1 = 2
2 * 2 = 4
4 / 2 = 2
2 + 8 = 10
10 % 7 = 3
```

您还可以将算术运算符与简单赋值运算符结合使用，创建复合赋值。例如，x+=1; 和 x=x+1; 都会使 x 的值增加 1。

`+ 运算符`也可以用于连接（合并）两个字符串，如以下 ConcatDemo 程序所示：

``` java title="ConcatDemo.java"
--8<-- "code/java/javase/src/main/java/com/luguosong/basic/ConcatDemo.java"
```

在该程序结束时，变量 thirdString 包含 "This is a concatenated string."，并将其打印到标准输出。

### 一元运算符

一元运算符只需一个操作数；它们执行各种操作，如将值增加/减少一、取反表达式或反转布尔值。

| 操作符 | 说明                            |
|-----|-------------------------------|
| +   | 一元加号运算符；表示正值（即使没有这个符号，数字也是正的） |
| -   | 一元负号运算符；用于取反表达式               |
| ++  | 递增运算符；将值增加1                   |
| --  | 递减运算符；将值减去1                   |
| !   | 逻辑补运算符：反转布尔值                  |

以下程序 UnaryDemo 测试了一元运算符：

``` java title="UnaryDemo.java"
--8<-- "code/java/javase/src/main/java/com/luguosong/basic/UnaryDemo.java"
```

增量/减量运算符可以在操作数之前（`前缀`）或之后（`后缀`）应用。代码 `result++;` 和 `++result;` 都会使 result
增加一。唯一的区别在于，前缀版本（++result）会计算为增加后的值，而后缀版本（result++）会计算为原始值。如果你只是进行简单的增量/减量操作，选择哪个版本并不重要。但如果你在更大的表达式中使用这个运算符，选择的版本可能会产生显著的差异。

以下程序 PrePostDemo 演示了前缀/后缀单目增量运算符：

``` java title="PrePostDemo.java"
--8<-- "code/java/javase/src/main/java/com/luguosong/basic/PrePostDemo.java"
```

### 相等与关系运算符

等号和关系运算符用于判断一个操作数是否大于、小于、等于或不等于另一个操作数。大多数这些运算符你可能都很熟悉。请注意，当测试两个基本类型的值是否相等时，必须使用
`==`而不是`=`。

| 操作符 | 描述   |
|-----|------|
| ==  | 等于   |
| !=  | 不等于  |
| >   | 大于   |
| >=  | 大于等于 |
| <   | 小于   |
| <=  | 小于等于 |

以下程序ComparisonDemo测试比较运算符：

``` java title="ComparisonDemo.java"
--8<-- "code/java/javase/src/main/java/com/luguosong/basic/ComparisonDemo.java"
```

### 条件运算符

`&&` 和 `||` 运算符对两个布尔表达式执行条件与和条件或操作。这些运算符表现出`短路`行为，也就是说，只有在需要时才会对第二个操作数进行求值。

| 操作符  | 说明  |
|------|-----|
| &&   | 条件与 |
| \|\| | 条件或 |

以下程序 ConditionalDemo1 测试这些运算符：

``` java title="ConditionalDemo1.java"
--8<-- "code/java/javase/src/main/java/com/luguosong/basic/ConditionalDemo1.java"
```

### 三元运算符

另一个条件运算符是` ?:`，可以被视为 `if-then-else` 语句的简写（在本课的控制流语句部分讨论）。这个运算符也被称为`三元运算符`
，因为它使用三个操作数。在下面的例子中，这个运算符应被理解为：
`如果 someCondition 为真，则将 value1 的值赋给 result。否则，将 value2 的值赋给 result。`

以下程序 ConditionalDemo2 测试了三元运算符：

``` java title="ConditionalDemo2.java"
--8<-- "code/java/javase/src/main/java/com/luguosong/basic/ConditionalDemo2.java"
```

由于 someCondition 为真，该程序会在屏幕上打印“1”。如果使用 ?: 运算符能让代码更易读，则应优先使用，而不是 if-then-else
语句；例如，当表达式简洁且没有副作用（如赋值）时。

### 类型比较运算符instanceof

`instanceof 运算符`用于将对象与指定类型进行比较。你可以用它来测试一个对象是否是某个类的实例、某个子类的实例，或者是实现了特定接口的类的实例。

以下程序 InstanceofDemo 定义了一个父类（名为 Parent）、一个简单的接口（名为 MyInterface），以及一个继承该父类并实现该接口的子类（名为
Child）。

``` java title="InstanceofDemo.java"
--8<-- "code/java/javase/src/main/java/com/luguosong/basic/InstanceofDemo.java"
```

!!! warning

    使用 instanceof 运算符时，请记住 null 不是任何东西的实例。

### 位运算符和位移运算符

Java 编程语言还提供了一些运算符，可以对整数类型执行按位和位移操作。本节讨论的运算符使用频率较低，因此只做简要介绍，旨在让您知道这些运算符的存在。

一元按位取反运算符`~`用于反转位模式；它可以应用于任何整数类型，将每个`0`变为`1`，每个`1`变为`0`。例如，一个字节包含8位；对位模式为
`00000000`的值应用此运算符会将其模式变为`11111111`。

有符号左移运算符 `<<` 将位模式向左移动，有符号右移运算符 `>>` 将位模式向右移动。位模式由左侧操作数给出，移动的位数由右侧操作数决定。无符号右移运算符
`>>>` 会在最左边的位置填入零，而 `>>` 后的最左边位置则取决于符号扩展。

按位 `&` 运算符执行按位与操作。

按位 `^` 运算符执行按位异或操作。

按位 `|` 运算符执行按位或操作。

以下程序 BitDemo 使用按位与运算符将数字“2”打印到标准输出。

``` java title="BitDemo.java"
--8<-- "code/java/javase/src/main/java/com/luguosong/basic/BitDemo.java"
```

## 表达式、语句和代码块

既然你已经了解了`变量`和`运算符`，现在是时候学习`表达式`、`语句`和`代码块`了。

`运算符`可以用于构建`表达式`，从而计算出数值；

`表达式`是`语句`的核心组成部分；`语句`可以组合成`代码块`。

### 表达式

表达式是由变量、运算符和方法调用构成的结构，根据语言的语法构建，最终计算出一个单一的值。你已经见过表达式的例子，下面用局部代码展示：

int `cadence` = 0;

`anArray[0] = 100`;

System.out.println(`"Element 1 at index 0: " + anArray[0]`);

int `result = 1 + 2`; // result is now 3

if (`value1 == value2`)

System.out.println(`value1 == value2`);

表达式返回值的数据类型取决于表达式中使用的元素。表达式 `cadence = 0` 返回一个 int
类型的值，因为赋值运算符返回的值与其左侧操作数的数据类型相同；在这个例子中，cadence 是一个 int
类型。正如你从其他表达式中看到的，表达式也可以返回其他类型的值，比如 boolean 或 String。

Java 编程语言允许您将多个较小的表达式组合成复合表达式，只要表达式中各部分所需的数据类型相匹配即可。以下是一个复合表达式的示例：

``` java
1 * 2 * 3
```

在这个特定例子中，表达式的计算顺序并不重要，因为乘法的结果与顺序无关；无论你以何种顺序进行乘法运算，结果总是相同的。然而，并非所有表达式都是如此。例如，以下表达式的结果会有所不同，这取决于你是先进行加法还是先进行除法运算：

``` java
x + y / 100    // 模棱两可
```

您可以使用成对的括号 `(` 和 `)` 精确指定一个表达式的计算方式。例如，为了使之前的表达式不产生歧义，您可以这样写：

``` java
(x + y) / 100  // 明确的，推荐👍🏻
```

如果你没有明确指明操作的执行顺序，那么顺序将由表达式中使用的运算符的优先级决定。优先级较高的运算符会先被计算。例如，除法运算符的优先级高于加法运算符。因此，以下两个语句是等价的：

``` java
x + y / 100 


x + (y / 100)
```

在编写复合表达式时，要明确指出哪些运算符应首先计算，并使用括号标明。这种做法使代码更易于阅读和维护。

### 语句

语句大致相当于自然语言中的句子。一个语句构成一个完整的执行单元。以下类型的表达式可以通过在表达式末尾加上分号（;）来构成语句。

- 赋值表达式
- 任何使用++或--
- 方法调用
- 对象创建表达式

这些语句称为`表达式语句`。以下是一些表达式语句的示例。

``` java
// 赋值语句
aValue = 8933.234;
// 自增语句
aValue++;
// 方法调用语句
System.out.println("Hello World!");
// 对象创建语句
Bicycle myBike = new Bicycle();
```

除了`表达式语句`，还有两种其他类型的语句：`声明语句`和`控制流语句`。声明语句用于声明变量。您已经见过许多声明语句的例子：

``` java
// 声明陈述
double aValue = 8933.234;
```

最后，控制流语句用于调节语句执行的顺序。您将在下一节`控制流语句`中学习有关控制流语句的内容。

### 块

块是由零个或多个语句组成的，并用成对的大括号括起来，可以在任何允许单个语句的地方使用。以下示例 BlockDemo 展示了块的用法：

``` java title="BlockDemo.java"
--8<-- "code/java/javase/src/main/java/com/luguosong/basic/BlockDemo.java"
```

## 控制流语句

源文件中的语句通常按照它们出现的顺序从上到下执行。然而，控制流语句通过决策、循环和分支来打破执行流程，使程序能够有条件地执行特定的代码块。本节介绍Java编程语言支持的决策语句（if-then、if-then-else、switch）、循环语句（for、while、do-while）以及分支语句（break、continue、return）。

### if-then

`if-then` 语句是所有控制流语句中最基本的。它指示程序仅在特定条件为真时执行某段代码。例如，Bicycle
类可以设置只有在自行车已经在运动时，刹车才能降低自行车的速度。applyBrakes 方法的一种可能实现如下：

``` java
void applyBrakes() {
	// if 条件：自行车必须在移动中
	if (isMoving) {
		// “then” 子句：降低当前速度
		currentSpeed--;
	}
}
```

如果此测试结果为假（意味着自行车没有在运动），控制将跳转到 if-then 语句的末尾。

此外，如果`then`子句中仅包含一个语句，则大括号是可选的：

``` java
void applyBrakes() {
    // same as above, but without braces 
    if (isMoving)
        currentSpeed--;
}
```

决定何时省略大括号是个人喜好的问题。省略大括号可能会使代码变得更脆弱。如果后来在`then`
子句中添加了第二条语句，常见的错误是忘记添加新需要的大括号。编译器无法捕捉到这种错误；你只会得到错误的结果。

### if-then-else

`if-then-else` 语句在 `if`子句判断为假时提供了一个备用的执行路径。你可以在 applyBrakes 方法中使用 `if-then-else`
语句，以便在自行车未运动时刹车被应用时采取某些措施。在这种情况下，措施就是简单地打印一条错误信息，说明自行车已经停下。

``` java
void applyBrakes() {
    if (isMoving) {
        currentSpeed--;
    } else {
        System.err.println("The bicycle has already stopped!");
    } 
}
```

以下程序 IfElseDemo 根据测试分数的值分配等级：90%或以上为A，80%或以上为B，依此类推。

``` java title="IfElseDemo.java"
--8<-- "code/java/javase/src/main/java/com/luguosong/basic/IfElseDemo.java"
```

您可能已经注意到，testscore 的值可以满足复合语句中的多个表达式：76 >= 70 和 76 >=
60。然而，一旦某个条件满足，相关的语句（grade = 'C';）就会被执行，剩余的条件将不再被评估。

### switch

与 `if-then` 和 `if-then-else` 语句不同，`switch` 语句可以有多个可能的执行路径。switch 可以与 byte、short、char 和 int
基本数据类型一起使用。它也可以与枚举类型（在枚举类型中讨论）、String 类以及一些包装特定基本类型的特殊类一起使用：Character、Byte、Short
和 Integer（在数字和字符串中讨论）。

以下代码示例 SwitchDemo 声明了一个名为 month 的整数，其值代表一个月份。代码使用 switch 语句根据 month 的值显示月份名称。

``` java title="SwitchDemo.java"
--8<-- "code/java/javase/src/main/java/com/luguosong/basic/SwitchDemo.java"
```

switch 语句的主体称为 switch 块。switch 块中的语句可以用一个或多个 case 或 default 标签标记。switch 语句会对其表达式进行求值，然后执行与匹配的
case 标签后面的所有语句。

您还可以使用 if-then-else 语句显示月份名称：

``` java
int month = 8;
if (month == 1) {
    System.out.println("January");
} else if (month == 2) {
    System.out.println("February");
}
...  // and so on
```

决定使用`if-then-else`语句还是`switch`语句，取决于可读性和语句所测试的表达式。`if-then-else`语句可以测试基于数值范围或条件的表达式，而
`switch`语句仅测试基于单个整数、枚举值或字符串对象的表达式。

另一个值得注意的点是 `break` 语句。每个 break 语句都会终止所包含的 switch 语句。控制流将继续执行 switch 块后的第一个语句。break
语句是必要的，因为如果没有它们，switch 块中的语句会贯穿：在遇到 break 语句之前，所有在匹配的 case 标签之后的语句都会按顺序执行，而不管后续
case 标签的表达式如何。程序 SwitchDemoFallThrough 展示了在 switch 块中贯穿的语句。该程序显示与整数月份对应的月份以及一年中随后的月份：

``` java title="SwitchDemoFallThrough.java"
--8<-- "code/java/javase/src/main/java/com/luguosong/basic/SwitchDemoFallThrough.java"
```

从技术上讲，最后的 break 并不是必需的，因为流程会自然退出 switch 语句。建议使用 break，这样修改代码更容易且不易出错。default
部分处理所有未被任何 case 部分明确处理的值。

以下代码示例 SwitchDemo2 展示了一个语句如何可以有多个 case 标签。该代码示例计算特定月份的天数：

``` java title="SwitchDemo2.java"
--8<-- "code/java/javase/src/main/java/com/luguosong/basic/SwitchDemo2.java"
```

在 Java SE 7 及更高版本中，你可以在 switch 语句的表达式中使用 String 对象。以下代码示例 StringSwitchDemo 根据名为 month
的字符串值显示月份的数字：

``` java title="StringSwitchDemo.java"
--8<-- "code/java/javase/src/main/java/com/luguosong/basic/StringSwitchDemo.java"
```

在 switch 表达式中，字符串会与每个 case 标签相关联的表达式进行比较，就像使用 String.equals 方法一样。为了让
StringSwitchDemo 示例能够接受任何大小写形式的月份，month 会被转换为小写（使用 toLowerCase 方法），并且所有与 case
标签相关联的字符串也都是小写。

!!! note

    此示例检查 switch 语句中的表达式是否为 null。请确保任何 switch 语句中的表达式都不为 null，以防止抛出 NullPointerException。

### while和do-while

当特定条件为真时，while 语句会不断执行一段语句块。其语法可以表示为：

``` java
while (expression) {
     statement(s)
}
```

while 语句会对表达式进行求值，该表达式必须返回一个布尔值。如果表达式求值为 true，while 语句就会执行 while 块中的语句。while
语句会持续测试表达式并执行其块，直到表达式求值为 false。可以通过以下 WhileDemo 程序使用 while 语句打印从 1 到 10 的值：

``` java title="WhileDemo.java"
--8<-- "code/java/javase/src/main/java/com/luguosong/basic/WhileDemo.java"
```

您可以使用 while 语句来实现一个无限循环，如下所示：

``` java
while (true){
    // 你的代码在这里
}
```

Java 编程语言还提供了一个 do-while 语句，可以表示如下：

``` java
do {
     statement(s)
} while (expression);
```

do-while 和 while 的区别在于，do-while 在循环底部而不是顶部对其表达式进行求值。因此，do 块中的语句总会至少执行一次，如以下的
DoWhileDemo 程序所示：

``` java title="DoWhileDemo.java"
--8<-- "code/java/javase/src/main/java/com/luguosong/basic/DoWhileDemo.java"
```

### for

for 语句提供了一种简洁的方法来遍历一系列值。程序员通常称之为`for 循环`，因为它会反复执行，直到满足特定条件。for
语句的一般形式可以表示如下：

``` java
for (initialization; termination; increment) {
    statement(s)
}
```

使用此版本的 for 语句时，请记住：

- `initialization`表达式用于初始化循环；它在循环开始时执行一次。
- 当`termination`表达式计算结果为假时，循环终止。
- 在每次循环迭代后调用`increment` 表达式；该表达式可以递增或递减一个值，这是完全可以接受的。

以下程序 ForDemo 使用 for 语句的一般形式将数字 1 到 10 输出到标准输出：

``` java title="ForDemo.java"
--8<-- "code/java/javase/src/main/java/com/luguosong/basic/ForDemo.java"
```

请注意代码如何在初始化表达式中声明一个变量。该变量的作用域从声明开始一直延续到由for语句控制的代码块结束，因此也可以在终止和增量表达式中使用。如果for语句控制的变量在循环外不需要使用，最好在初始化表达式中声明该变量。i、j和k这些名称常用于控制for循环；在初始化表达式中声明它们可以限制其生命周期并减少错误。

for 循环的三个表达式都是可选的，可以通过以下方式创建一个无限循环：

``` java
// 无限循环
for ( ; ; ) {
    
    // 代码
}
```

for 语句还有另一种形式，专为遍历集合和数组而设计。这种形式有时被称为增强型 for 语句，可以使循环更简洁易读。为了演示这一点，考虑以下数组，其中包含数字
1 到 10：

``` java
int[] numbers = {1,2,3,4,5,6,7,8,9,10};
```

以下程序 EnhancedForDemo 使用增强型 for 循环遍历数组：

``` java title="EnhancedForDemo.java"
--8<-- "code/java/javase/src/main/java/com/luguosong/basic/EnhancedForDemo.java"
```

我们建议在可能的情况下使用这种形式的 for 语句，而不是通用形式。

### 分支语句

#### break

break语句有两种形式：带标签和不带标签。在前面的switch语句讨论中，你已经见过不带标签的形式。你也可以使用不带标签的break来终止for、while或do-while循环，如以下的BreakDemo程序所示：

``` java title="BreakDemo.java"
--8<-- "code/java/javase/src/main/java/com/luguosong/basic/BreakDemo.java"
```

该程序在数组中搜索数字12。break语句在找到该值时终止for循环。控制流随后转移到for循环后的语句。

一个无标签的 break 语句会终止最内层的 switch、for、while 或 do-while 语句，而一个有标签的 break 语句则会`终止外层的语句`
。以下程序 BreakWithLabelDemo 类似于前一个程序，但使用嵌套的 for 循环在一个二维数组中搜索一个值。当找到该值时，有标签的
break 语句会终止外层的 for 循环（标签为 "search"）：

``` java title="BreakWithLabelDemo.java"
--8<-- "code/java/javase/src/main/java/com/luguosong/basic/BreakWithLabelDemo.java"
```

break 语句终止标记语句；它不会将控制流转移到该标记。控制流会转移到紧接在标记（已终止）语句之后的语句。

#### continue

continue 语句会跳过 for、while 或 do-while 循环的当前迭代。未标记的形式会跳到最内层循环体的末尾，并评估控制循环的布尔表达式。以下程序 ContinueDemo 遍历一个字符串，计算字母 "p" 出现的次数。如果当前字符不是 "p"，continue 语句会跳过循环的其余部分并继续下一个字符。如果是 "p"，程序会增加字母计数。

``` java title="ContinueDemo.java"
--8<-- "code/java/javase/src/main/java/com/luguosong/basic/ContinueDemo.java"
```

要更清楚地看到这个效果，试着去掉 continue 语句并重新编译。再次运行程序时，计数将会出错，显示找到了 35 个 p，而不是 9 个。

带标签的 continue 语句会跳过带有指定标签的外层循环的当前迭代。以下示例程序 ContinueWithLabelDemo 使用嵌套循环在一个字符串中搜索子字符串。需要两个嵌套循环：一个用于迭代子字符串，另一个用于迭代被搜索的字符串。以下程序 ContinueWithLabelDemo 使用带标签的 continue 形式来跳过外层循环中的一次迭代。

``` java title="ContinueWithLabelDemo.java"
--8<-- "code/java/javase/src/main/java/com/luguosong/basic/ContinueWithLabelDemo.java"
```

#### return

最后一种分支语句是 return 语句。return 语句用于`退出当前方法`，并将控制流返回到调用该方法的地方。return 语句有两种形式：一种是返回一个值，另一种是不返回值。要返回一个值，只需在 return 关键字后面放置该值（或计算该值的表达式）。

``` java
return ++count;
```

返回值的数据类型必须与方法声明的返回值类型匹配。当方法声明为void时，使用不返回值的return形式。

``` java
return;
```



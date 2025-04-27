# 面向对象

## 类(Class)

### 什么是类

在现实世界中，你经常会发现许多相同类型的个体对象。可能有成千上万辆相同品牌和型号的自行车存在。每辆自行车都是根据同一套蓝图制造的，因此包含相同的组件。在面向对象的术语中，我们说你的自行车是被称为自行车的对象类的一个实例。
`类是创建个体对象的蓝图`。

以下是自行车类的一种可能实现：

``` java
class Bicycle {

	int cadence = 0;
	int speed = 0;
	int gear = 1;

	void changeCadence(int newValue) {
		cadence = newValue;
	}

	void changeGear(int newValue) {
		gear = newValue;
	}

	void speedUp(int increment) {
		speed = speed + increment;
	}

	void applyBrakes(int decrement) {
		speed = speed - decrement;
	}

	void printStates() {
		System.out.println("cadence:" +
				cadence + " speed:" +
				speed + " gear:" + gear);
	}
}
```

Java 编程语言的语法可能对你来说比较新颖，但这个类的设计基于之前关于自行车对象的讨论。`字段` cadence、speed 和 gear 代表对象的
`状态`，而`方法`（如 changeCadence、changeGear、speedUp 等）定义了它与外界的`交互`。

您可能已经注意到，Bicycle 类中没有包含 `main 方法`。这是因为它不是一个完整的应用程序；它只是一个用于应用程序中的自行车的蓝图。创建和使用新的
Bicycle 对象的职责属于您应用程序中的其他类。

这是一个BicycleDemo类，用于创建两个独立的Bicycle对象并调用它们的方法：

``` java
class BicycleDemo {
	public static void main(String[] args) {

		// 创建两个不同的自行车对象
		Bicycle bike1 = new Bicycle();
		Bicycle bike2 = new Bicycle();

		// 对这些对象调用方法。
		bike1.changeCadence(50);
		bike1.speedUp(10);
		bike1.changeGear(2);
		bike1.printStates();

		bike2.changeCadence(50);
		bike2.speedUp(10);
		bike2.changeGear(2);
		bike2.changeCadence(40);
		bike2.speedUp(10);
		bike2.changeGear(3);
		bike2.printStates();
	}
}
```

此测试的输出将打印两辆自行车的终点踏频、速度和档位：

```shell
cadence:50 speed:10 gear:2
cadence:40 speed:20 gear:3
```

### 什么是继承(Inheritance)

不同种类的物体通常彼此之间有一些共同点。例如，山地自行车、公路自行车和双人自行车都具有自行车的特征（当前速度、当前踏频、当前齿轮）。然而，每种自行车也有其独特的特征：双人自行车有两个座位和两套车把；公路自行车有下弯车把；一些山地自行车有额外的链轮，从而提供更低的齿轮比。

面向对象编程`允许类从其他类继承常用的状态和行为`。在这个例子中，Bicycle 现在成为 MountainBike、RoadBike 和 TandemBike 的超类。在
Java 编程语言中，每个类允许有一个直接超类，并且每个超类可以拥有无限数量的子类：

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202412070938124.png){ loading=lazy }
  <figcaption>自行车类别的层级。</figcaption>
</figure>

创建子类的语法很简单。在类声明的开头，使用 `extends 关键字`，后接要继承的类名：

``` java
class MountainBike extends Bicycle {

	// 定义山地自行车的新字段和方法将在此处添加

}
```

这使得MountainBike拥有与Bicycle相同的字段和方法，同时可以专注于其独特的功能。这使得子类的代码易于阅读。然而，你必须注意正确记录每个超类定义的状态和行为，因为这些代码不会出现在每个子类的源文件中。

### 声明类

您已经见过以下方式定义的类：

``` java
class MyClass {
    // 字段、构造函数和方法声明
}
```

这是一个类的声明。`类体`（大括号之间的区域）包含了所有为从该类创建的对象提供生命周期的代码：用于初始化新对象的`构造函数`
、提供类及其对象状态的`字段`声明，以及实现类及其对象行为的`方法`。

前面的类声明是一个最简化的版本。它仅包含类声明中必需的组成部分。你可以在类声明的开头提供更多关于类的信息，比如它的`父类`
名称、是否实现了某些`接口`等。例如，

``` java
class MyClass extends MySuperClass implements YourInterface {
    // 字段、构造函数和方法声明
}
```

意味着 MyClass 是 MySuperClass 的子类，并实现了 YourInterface 接口。

您还可以在最开始添加修饰符，如 public 或 private，因此您可以看到类声明的开头行可能会变得相当复杂。修饰符 public 和 private
用于确定哪些其他类可以访问 MyClass，这将在本课后面讨论。关于接口和继承的课程将解释在类声明中如何以及为何使用 extends 和
implements 关键字。目前，您无需担心这些额外的复杂性。

通常，类声明可以按顺序包含以下组件：

1. 修饰符包括public、private以及其他一些你之后会遇到的修饰符。（不过，请注意private修饰符只能用于嵌套类。）
2. 类名，按照惯例首字母大写。
3. 类的父类（超类）的名称（如果有的话），前面加上关键字 extends。一个类只能继承（子类化）一个父类。
4. 由类实现的接口列表（如果有），用逗号分隔，并以关键字implements开头。一个类可以实现多个接口。
5. 类主体由大括号 {} 包围。

### 声明成员变量(字段)

有几种类型的变量：

- 类中的成员变量——这些被称为`字段`。
- 方法或代码块中的变量——这些称为`局部变量`。
- 方法声明中的变量——这些称为`参数`。

字段声明由三个部分按顺序组成：

- 零个或多个修饰符，例如 public 或 private。
- 字段的类型。
- 字段的名称。

成员字段的`修饰符`用于控制其他类对其访问权限。目前，只需考虑 public 和 private。其他访问修饰符将在后面讨论。

- `public` 修饰符——该字段可被所有类访问。
- `private` 修饰符——字段仅在其所属类内可访问。

为了体现封装的原则，通常会将字段设为私有。这意味着它们只能在Bicycle类中被直接访问。然而，我们仍然需要访问这些值。这可以通过添加公共方法来间接实现，这些方法可以获取字段的值：

```java
public class Bicycle {

	private int cadence;
	private int gear;
	private int speed;

	public Bicycle(int startCadence, int startSpeed, int startGear) {
		gear = startGear;
		cadence = startCadence;
		speed = startSpeed;
	}

	public int getCadence() {
		return cadence;
	}

	public void setCadence(int newValue) {
		cadence = newValue;
	}

	public int getGear() {
		return gear;
	}

	public void setGear(int newValue) {
		gear = newValue;
	}

	public int getSpeed() {
		return speed;
	}

	public void applyBrake(int decrement) {
		speed -= decrement;
	}

	public void speedUp(int increment) {
		speed += increment;
	}
}
```

所有变量都必须有一个`类型`。你可以使用基本类型，如 int、float、boolean 等，或者使用引用类型，如字符串、数组或对象。

所有变量，无论是字段、局部变量还是参数，都遵循在语言基础课程中`变量命名`部分所讲述的相同命名规则和惯例。

!!! note

	在本课中，请注意`方法`和`类名`使用相同的命名规则和约定，除了

	- 类名的首字母应大写
	- 方法名称中的第一个（或唯一）单词应为动词。

### 声明方法

以下是一个典型的方法声明示例：

``` java
public double calculateAnswer(double wingSpan, int numberOfEngines,
                              double length, double grossTons) {
    //在此进行计算
}
```

方法声明的唯一必需元素是方法的返回类型、名称、一对括号 `()`，以及用大括号 `{}` 包围的主体。

更广泛地说，方法声明由六个部分按顺序组成：

- 修饰符——例如 public、private 以及其他你将稍后学习的内容。
- 返回类型——方法返回值的数据类型，如果方法不返回值，则为void。
- 方法名称——字段名称的规则同样适用于方法名称，但惯例略有不同。
- 括号中的参数列表是一个以逗号分隔的输入参数列表，参数前需注明数据类型，并用括号括起来 ()。如果没有参数，必须使用空括号。
- 异常列表
- 方法体位于大括号之间——方法的代码，包括局部变量的声明，都在这里。

#### 方法签名

方法声明的两个组成部分构成了`方法签名`——`方法名称`和`参数类型`。

上述方法的签名是：

`calculateAnswer(double, int, double, double)`

#### 方法命名

尽管方法名称可以是任何合法的标识符，但代码规范对方法名称有一定的限制。按照惯例，方法名称应为小写动词，或以小写动词开头的多词名称，后接形容词、名词等。在多词名称中，第二个及后续单词的首字母应大写。以下是一些示例：

``` java
run
runFast
getBackground
getFinalData
compareTo
setX
isEmpty
```

通常，一个方法在其类中具有唯一的名称。然而，由于方法重载，一个方法可能与其他方法同名。

#### 方法重载

Java 编程语言支持方法重载，并且 Java 可以区分具有不同方法签名的方法。这意味着在一个类中，如果`方法的参数列表不同，它们可以拥有相同的名称`
（关于这一点的一些限定条件将在名为“接口和继承”的课程中讨论）。

假设你有一个可以使用书法绘制各种类型数据（如字符串、整数等）的类，并且包含一个用于绘制每种数据类型的方法。为每个方法使用一个新名称是很麻烦的——例如，drawString、drawInteger、drawFloat等等。在Java编程语言中，你可以为所有绘图方法使用相同的名称，但为每个方法传递不同的参数列表。因此，这个数据绘制类可能会声明四个名为draw的方法，每个方法都有不同的参数列表。

``` java
public class DataArtist {
    ...
    public void draw(String s) {
        ...
    }
    public void draw(int i) {
        ...
    }
    public void draw(double f) {
        ...
    }
    public void draw(int i, double f) {
        ...
    }
}
```

重载方法通过传入方法的参数数量和类型来区分。在代码示例中，draw(String s) 和 draw(int i) 是不同且独特的方法，因为它们需要不同类型的参数。

您不能声明多个具有相同名称以及相同数量和类型参数的方法，因为编译器无法区分它们。

编译器在区分方法时不考虑返回类型，因此即使返回类型不同，也不能声明两个具有相同签名的方法。

!!! note

	重载方法应谨慎使用，因为它们可能会降低代码的可读性。

### 为类提供构造函数

一个类包含构造函数，用于根据类的蓝图创建对象。构造函数的声明看起来像方法声明——但它们使用类的名称且没有返回类型。例如，Bicycle
类有一个构造函数：

``` java
public Bicycle(int startCadence, int startSpeed, int startGear) {
    gear = startGear;
    cadence = startCadence;
    speed = startSpeed;
}
```

要创建一个名为 myBike 的新自行车对象，需要通过 new 运算符调用构造函数：

``` java
Bicycle myBike = new Bicycle(30, 0, 8);
```

`new Bicycle(30, 0, 8)`在内存中为对象创建空间并初始化其字段。

尽管自行车类只有一个构造函数，但它可以有其他构造函数，包括一个无参数的构造函数：

``` java
public Bicycle() {
    gear = 1;
    cadence = 10;
    speed = 0;
}
```

`Bicycle yourBike = new Bicycle();` 调用了无参构造函数来创建一个名为 yourBike 的新 Bicycle 对象。

这两个构造函数可以在 Bicycle 类中声明，因为它们的参数列表不同。与方法一样，Java
平台根据参数列表中的参数数量和类型来区分构造函数。你不能为同一个类编写两个具有相同数量和类型参数的构造函数，因为平台无法区分它们。这会导致编译时错误。

你不必为你的类提供任何构造函数，但在这样做时必须小心。编译器会`自动为没有构造函数的类提供一个无参数的默认构造函数`
。这个默认构造函数会调用超类的无参数构造函数。在这种情况下，如果超类没有无参数构造函数，编译器会报错，因此你必须
`确认超类确实有无参数构造函数`。如果你的类没有显式的超类，那么它会有一个`隐式的超类Object`，而Object是有无参数构造函数的。

您可以自行使用超类构造函数。本课开头的MountainBike类就是这样做的。这将在后面的接口和继承课程中进行讨论。

您可以在构造函数的声明中使用访问修饰符来控制哪些其他类可以调用该构造函数。

!!! note

	如果另一个类无法调用 MyClass 的构造函数，那么它就不能直接创建 MyClass 对象。

### 将信息传递给方法或构造函数

方法或构造函数的声明会指明该方法或构造函数的参数数量和类型。例如，以下是一个计算房贷月供的方法，其基于贷款金额、利率、贷款期限（期数）以及贷款的未来价值：

``` java
public double computePayment(
                  double loanAmt,
                  double rate,
                  double futureValue,
                  int numPeriods) {
    double interest = rate / 100.0;
    double partial1 = Math.pow((1 + interest), 
                    - numPeriods);
    double denominator = (1 - partial1) / interest;
    double answer = (-loanAmt / denominator)
                    - ((futureValue * partial1) / denominator);
    return answer;
}
```

该方法有四个参数：贷款金额、利率、未来价值和期数。前三个参数是双精度浮点数，第四个参数是整数。这些参数在方法体中使用，并在运行时接受传入的实参值。

!!! note

	参数是指方法声明中的变量列表。参数值是调用方法时传递的实际值。当你调用一个方法时，所用的参数值必须在类型和顺序上与声明中的参数相匹配。

#### 参数类型

您可以为方法或构造函数的参数使用任何数据类型。这包括基本数据类型，如 double、float 和 integer，就像您在 computePayment
方法中看到的那样，以及引用数据类型，如对象和数组。

以下是一个接受数组作为参数的方法示例。在这个示例中，该方法创建了一个新的Polygon对象，并通过一个Point对象数组进行初始化（假设Point是一个表示x,
y坐标的类）：

``` java
public Polygon polygonFrom(Point[] corners) {
    // 方法主体在此处编写
}
```

!!! note

	如果你想将一个方法传递给另一个方法，那么可以使用 `lambda 表达式`或`方法引用`。

#### 任意数量的参数

您可以使用一种称为`可变参数（varargs）`
的结构来向方法传递任意数量的值。当您不知道将传递给方法的特定类型参数的数量时，可以使用可变参数。这是一种手动创建数组的快捷方式（之前的方法本可以使用可变参数而不是数组）。

要使用可变参数，您需要在最后一个参数的类型后面加上省略号（三个点，...），然后是一个空格和参数名称。这样的方法可以用任意数量的该参数调用，包括不传递任何参数。

``` java
public Polygon polygonFrom(Point... corners) {
    int numberOfSides = corners.length;
    double squareOfSide1, lengthOfSide1;
    squareOfSide1 = (corners[1].x - corners[0].x)
                     * (corners[1].x - corners[0].x) 
                     + (corners[1].y - corners[0].y)
                     * (corners[1].y - corners[0].y);
    lengthOfSide1 = Math.sqrt(squareOfSide1);

    // 后续的方法主体代码将创建并返回一个连接这些点的多边形
}
```

您可以看到，在方法内部，corners 被视为一个数组。该方法可以用数组或一系列参数来调用。在这两种情况下，方法体中的代码都会将参数视为数组。

您最常在打印方法中看到可变参数，例如这个 printf 方法：

``` java
public PrintStream printf(String format, Object... args)
```

允许您打印任意数量的对象。可以这样调用：

``` java
System.out.printf("%s: %d, %s%n", name, idnum, address);
```

或者这样

``` java
System.out.printf("%s: %d, %s, %s, %s%n", name, idnum, address, phone, email);
```

或使用不同数量的参数。

#### 参数名称

当你为方法或构造函数声明一个参数时，你需要为该参数提供一个名称。在方法体内，这个名称用于引用传入的参数。

参数的名称在其作用域内必须是唯一的。它不能与同一方法或构造函数中的其他参数名称相同，也不能与方法或构造函数中的局部变量名称相同。

一个参数可以与类的字段同名。如果出现这种情况，就称`该参数遮蔽了字段`。遮蔽字段可能会使代码难以阅读，通常仅在构造函数和设置特定字段的方法中使用。例如，考虑以下
Circle 类及其 setOrigin 方法：

``` java
public class Circle {
    private int x, y, radius;
    public void setOrigin(int x, int y) {
        ...
    }
}
```

Circle 类有三个字段：x、y 和 radius。setOrigin 方法有两个参数，每个参数的名称与其中一个字段相同。每个方法参数都会遮蔽与其同名的字段。因此，在方法体内使用简单名称
x 或 y 指的是参数，而不是字段。要访问字段，必须使用限定名称。这将在本课后面的使用 `this 关键字`部分中讨论。

#### 传递原始数据类型参数

像 int 或 double 这样的基本类型参数是通过`值`传递给方法的。这意味着对参数值的任何更改仅存在于方法的作用域内。当方法返回时，参数就消失了，对它们的任何更改也会丢失。以下是一个示例：

``` java
public class PassPrimitiveByValue {

    public static void main(String[] args) {
           
        int x = 3;
           
        // 使用 x 作为参数调用 passMethod()
        passMethod(x);
           
        // 打印 x 以查看其值是否已更改 => 3
        System.out.println("After invoking passMethod, x = " + x);
           
    }
        
    // 更改 passMethod() 中的参数
    public static void passMethod(int p) {
        p = 10;
    }
}
```

#### 传递引用数据类型参数

引用数据类型参数，例如对象，也通过`值`传递给方法。这意味着当方法返回时，传入的引用仍然指向与之前相同的对象。然而，如果对象的字段具有适当的访问级别，则可以在方法中更改其值。

例如，考虑一个任意类中用于移动圆形对象的方法：

``` java
public void moveCircle(Circle circle, int deltaX, int deltaY) {
    // 将圆心移动到 x+deltaX, y+deltaY 的代码
    circle.setX(circle.getX() + deltaX);
    circle.setY(circle.getY() + deltaY);
        
    // 代码为圆形分配一个新的引用
    circle = new Circle(0, 0);
}
```

在方法内部，circle最初指的是myCircle。该方法将circle引用的对象（即myCircle）的x和y坐标分别更改为23和56。这些更改在方法返回后仍然有效。然后，circle被赋予一个新的Circle对象的引用，其x和y都为0。然而，这种重新赋值没有持久性，因为引用是通过值传递的，无法更改。在方法内部，circle指向的对象已更改，但当方法返回时，myCircle仍然引用与方法调用前相同的Circle对象。

### 从方法返回值

当一个方法执行完毕时，它会返回到调用它的代码处。

- 完成方法中的所有语句，
- 到达 return 语句，或
- 抛出异常（稍后会详细介绍）

以先发生者为准。

在方法声明中声明方法的返回类型。在方法体内，使用return语句返回值。

任何声明为 void 的方法都不返回值。它不需要包含 return 语句，但可以包含。在这种情况下，return 语句可以用于跳出控制流块并退出方法，使用方式如下：

``` java
return;
```

如果尝试从声明为 void 的方法中返回一个值，会导致编译错误。

任何未声明为 void 的方法必须包含一个带有相应返回值的 return 语句，如下所示：

``` java
return returnValue; 
```

返回值的数据类型必须与方法声明的返回类型一致；不能从声明返回布尔值的方法中返回整数值。

#### 返回类或接口

当一个方法使用类名作为其返回类型时，返回对象的类型必须是返回类型的`子类`或`与返回类型完全相同的类`。假设你有一个类层次结构，其中
ImaginaryNumber 是 java.lang.Number 的子类，而 java.lang.Number 又是 Object 的子类，如下图所示。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202412131622472.png){ loading=lazy }
  <figcaption>ImaginaryNumber 的类层次结构</figcaption>
</figure>

现在假设你有一个声明为返回 Number 的方法：

``` java
public Number returnANumber() {
    ...
}
```

returnANumber 方法可以返回一个 ImaginaryNumber，但不能返回一个 Object。ImaginaryNumber 是 Number 的子类，因此它是一个
Number。然而，Object 不一定是 Number——它可能是一个 String 或其他类型。

您可以重写一个方法，并将其定义为返回原始方法的子类，如下所示：

``` java
public ImaginaryNumber returnANumber() {
    ...
}
```

这种称为协变返回类型的技术意味着返回类型可以随着子类的变化而变化。

!!! note

	您也可以使用接口名称作为返回类型。在这种情况下，返回的对象必须实现指定的接口。

### this关键字

在实例方法或构造函数中，this 是对当前对象的引用——即正在调用其方法或构造函数的对象。在实例方法或构造函数中，可以使用 this
来引用当前对象的任何成员。

#### 与字段一起使用

使用 this 关键字最常见的原因是字段被方法或构造函数参数遮蔽。

例如，Point 类是这样编写的：

``` java
public class Point {
    public int x = 0;
    public int y = 0;
        
    //constructor
    public Point(int a, int b) {
        x = a;
        y = b;
    }
}
```

但它本可以这样写：

``` java
public class Point {
    public int x = 0;
    public int y = 0;
        
    //constructor
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
```

构造函数的每个参数都会遮蔽对象的一个字段——在构造函数内部，x 是构造函数第一个参数的局部副本。要引用 Point 字段 x，构造函数必须使用
this.x。

#### 在构造函数中使用 this

在构造函数内部，你也可以使用 this 关键字来调用同一类中的另一个构造函数。这种做法称为显式构造函数调用。下面是另一个
Rectangle 类，其实现方式与对象部分中的不同。

``` java
public class Rectangle {
    private int x, y;
    private int width, height;
        
    public Rectangle() {
        this(0, 0, 1, 1);
    }
    public Rectangle(int width, int height) {
        this(0, 0, width, height);
    }
    public Rectangle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    ...
}
```

这个类包含一组构造函数。每个构造函数初始化矩形的一些或全部成员变量。对于没有通过参数提供初始值的成员变量，构造函数会提供一个默认值。例如，无参数构造函数会在坐标0,0处创建一个1x1的矩形。两个参数的构造函数调用四个参数的构造函数，传入宽度和高度，但始终使用0,0坐标。与之前一样，编译器根据参数的数量和类型来确定调用哪个构造函数。

如果存在，调用另一个构造函数必须是构造函数中的第一行。

### 控制类成员的访问

`访问级别修饰符`决定了其他类是否可以使用特定的字段或调用特定的方法。访问控制有两个级别：

- 在顶层——public，或无修饰符（包私有）。
- 在成员级别——public、private、protected或无修饰符（包私有）。

一个`类`可以用修饰符`public`声明，这样该类对所有地方的所有类都是可见的。如果一个类`没有修饰符`（默认情况下，也称为包私有），那么它
`仅在其所属的包内可见`。

在成员级别，你可以像对顶级类一样使用 `public` 修饰符或`无修饰符`（包私有），并且含义相同。对于成员，还有两个额外的访问修饰符：
`private` 和 `protected`。`private` 修饰符指定成员只能在其自身的类中访问。`protected`
修饰符指定成员只能在其自身的包内访问（与包私有相同），此外，还可以被其他包中其类的子类访问。

下表显示了每个修饰符允许的成员访问权限。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202412131658586.png){ loading=lazy }
  <figcaption>访问级别</figcaption>
</figure>

第一列数据表示类本身是否可以访问由访问级别定义的成员。如你所见，类总是可以访问其自身的成员。第二列表示与该类在同一包中的类（无论其继承关系如何）是否可以访问该成员。第三列表示在此包外声明的该类的子类是否可以访问该成员。第四列表示所有类是否可以访问该成员。

访问级别对您有两方面的影响。首先，当您使用来自其他来源的类时，例如 Java
平台中的类，访问级别决定了您的类可以使用这些类的哪些成员。其次，当您编写一个类时，您需要决定类中的每个成员变量和方法应具有什么样的访问级别。

让我们来看一组类，看看访问级别如何影响可见性。下图展示了这个示例中的四个类及其关系。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202412131700564.png){ loading=lazy }
  <figcaption>用于说明访问级别的示例类和包</figcaption>
</figure>

以下表格显示了在每种可应用的访问修饰符下，Alpha类成员的可见性。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202412131702028.png){ loading=lazy }
  <figcaption>可见性</figcaption>
</figure>

!!! note "选择访问级别的提示："

	如果其他程序员使用你的类，你希望确保不会因误用而产生错误。访问级别可以帮助你做到这一点。

	- 对特定成员使用最严格的访问级别。除非有充分理由，否则应使用private。
	- 避免使用public字段，除非是常量。（教程中的许多示例使用了公共字段，这可能有助于简洁地说明某些要点，但不建议在生产代码中使用。）公共字段往往会将你绑定到特定的实现上，限制你更改代码的灵活性。

### 类成员(静态成员)

在本节中，我们将讨论使用static关键字来创建属于类的字段和方法，而不是类的实例。

#### 类变量(静态字段)

当从同一个类蓝图创建多个对象时，它们各自拥有自己独立的实例变量副本。以自行车类为例，实例变量包括踏频、齿轮和速度。每个自行车对象都有这些变量的独立值，存储在不同的内存位置。

有时候，你可能希望拥有对所有对象都通用的变量。这可以通过使用`static修饰符`
来实现。在声明中带有static修饰符的字段称为静态字段或类变量。它们与类相关联，而不是与任何对象相关联。类的每个实例共享一个类变量，该变量在内存中位于一个固定位置。任何对象都可以更改类变量的值，但类变量也可以在不创建类实例的情况下进行操作。

例如，假设你想创建多个自行车对象，并为每个对象分配一个序列号，从第一个对象开始编号为1。这个ID号对每个对象都是唯一的，因此是一个实例变量。同时，你需要一个字段来记录已经创建了多少个自行车对象，以便知道下一个对象应该分配什么ID。这样的字段与任何单个对象无关，而是与整个类相关。为此，你需要一个类变量，numberOfBicycles，如下所示：

``` java
public class Bicycle {
        
    private int cadence;
    private int gear;
    private int speed;
        
    // 为对象ID添加一个实例变量。
    private int id;
    
    // 添加一个类变量，用于记录已实例化的自行车对象数量
    private static int numberOfBicycles = 0;
        ...
}
```

类变量通过类名本身进行引用，例如

``` java
Bicycle.numberOfBicycles
```

这清楚地表明它们是类变量。

!!! warning

	您也可以使用对象引用来访问静态字段，例如

	``` java
	myBike.numberOfBicycles
	```

	但不建议这样做，因为这无法明确表明它们是类变量。

您可以使用 Bicycle 构造函数来设置 id 实例变量，并增加 numberOfBicycles 类变量的值：

``` java
public class Bicycle {
        
    private int cadence;
    private int gear;
    private int speed;
    private int id;
    private static int numberOfBicycles = 0;
        
    public Bicycle(int startCadence, int startSpeed, int startGear){
        gear = startGear;
        cadence = startCadence;
        speed = startSpeed;

        // 增加自行车数量并分配编号。
        id = ++numberOfBicycles;
    }

    // 返回ID实例变量的新方法
    public int getID() {
        return id;
    }
        ...
}
```

#### 类方法(静态方法)

Java 编程语言支持静态方法和静态变量。带有 static 修饰符的静态方法应通过类名调用，无需创建类的实例，例如：

``` java
ClassName.methodName(args)
```

!!! warning

	您也可以使用对象引用来调用静态方法，例如

	``` java
	instanceName.methodName(args)
	```

	但不建议这样做，因为这样无法明确表明它们是类方法。

静态方法的常见用途是访问静态字段。例如，我们可以在Bicycle类中添加一个静态方法来访问numberOfBicycles静态字段：

``` java
public static int getNumberOfBicycles() {
    return numberOfBicycles;
}
```

#### 相互访问

并非所有实例变量、类变量和方法的组合都是允许的：

- ✔️`实例方法`可以直接访问`实例变量`和`实例方法`。
- ✔️`实例方法`可以直接访问`类变量`和`类方法`。
- ✔️`类方法`可以直接访问`类变量`和`类方法`。
- ❌`类方法`不能直接访问`实例变量`或`实例方法`——它们必须使用对象引用。此外，类方法不能使用this关键字，因为没有实例可供this引用。

#### 常量(Constants)

`static` 修饰符与 `final` 修饰符结合使用时，也用于定义常量。

- `final` 修饰符表示该字段的值不可更改。
- `static`使得常量是类级别的，而不依赖于类的实例。如果不加`static`，每个实例都会拥有一份常量的副本，虽然它的值不变，但这并不符合常量的定义。

例如，以下变量声明定义了一个名为 PI 的常量，其值是圆周率（圆的周长与直径之比）的近似值：

``` java
static final double PI = 3.141592653589793;
```

以这种方式定义的常量不能被重新赋值，如果程序尝试这样做，将会导致编译时错误。按照惯例，常量的名称使用大写字母拼写。如果名称由多个单词组成，单词之间用下划线（_
）分隔。

!!! note

	如果一个基本类型或字符串被定义为常量，并且其值在编译时已知，编译器会在代码中将常量名替换为其值。这被称为`编译时常量`。如果外部世界中常量的值发生变化（例如，如果法律规定圆周率实际上应该是3.975），那么你需要重新编译使用该常量的所有类以获取当前值。

### 初始化字段

正如您所见，您通常可以在字段声明时提供一个初始值：

``` java
public class BedAndBreakfast {

    // 初始化为 10
    public static int capacity = 10;

    // 初始化为 false
    private boolean full = false;
}
```

当初始化值可用且可以在一行中完成初始化时，这种方法效果很好。然而，由于其简单性，这种初始化形式存在局限性。如果初始化需要一些逻辑（例如，错误处理或使用
for 循环填充复杂数组），简单的赋值就不够用了。实例变量可以在`构造函数`中初始化，在那里可以使用错误处理或其他逻辑。为了为`类变量`
提供相同的功能，Java 编程语言引入了`静态初始化块`。

!!! note

	在类定义的开头声明字段并不是必须的，尽管这是最常见的做法。只需确保在使用之前声明并初始化它们即可。

#### 静态初始化块

静态初始化块是一个用大括号 { } 包围的普通代码块，并且前面有 static 关键字。以下是一个示例：

``` java
static {
    // 初始化所需的代码放在这里
}
```

一个类可以有任意数量的静态初始化块，并且它们可以出现在类主体的任何位置。运行时系统保证静态初始化块按照它们在源代码中出现的顺序被调用。

有一种替代静态代码块的方法——你可以编写一个私有静态方法：

``` java
class Whatever {
    public static varType myVar = initializeClassVariable();
        
    private static varType initializeClassVariable() {

        // 初始化代码在此处
    }
}
```

私有静态方法的优点在于，如果需要重新初始化类变量，它们可以被重复使用。

#### 初始化实例成员

通常，你会在构造函数中编写代码来初始化实例变量。除了使用构造函数，还有两种替代方法可以初始化实例变量：初始化块和final 方法。

实例变量的初始化块看起来与静态初始化块相似，但没有 static 关键字：

``` java
{
    // 初始化所需的代码在此处编写
}
```

Java 编译器会将初始化块复制到每个构造函数中。因此，这种方法可以用于在多个构造函数之间共享一段代码。

在子类中，final 方法不能被重写。这在接口和继承的课程中有讨论。以下是使用 final 方法初始化实例变量的一个示例：

``` java
class Whatever {
    private varType myVar = initializeInstanceVariable();
        
    protected final varType initializeInstanceVariable() {

        // 初始化代码在此处
    }
}
```

这特别有用，如果子类可能想要重用初始化方法。该方法是final的，因为在实例初始化期间调用非final方法可能会导致问题。

## 对象(Object)

### 什么是对象

要理解面向对象技术，关键在于对象。环顾四周，你会发现许多现实世界中的对象：你的狗、你的书桌、你的电视机、你的自行车。

现实世界的对象具有两个特征：它们都有`状态`和`行为`。

- 狗有`状态`（名字、颜色、品种、饥饿）和`行为`（吠叫、取物、摇尾巴）。
- 自行车也有`状态`（当前档位、当前踏频、当前速度）和`行为`（换档、改变踏频、刹车）。

识别现实世界对象的`状态`和`行为`是开始面向对象编程思维的一个好方法。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409060858570.png){ loading=lazy }
  <figcaption>软件对象</figcaption>
</figure>

软件对象在概念上类似于现实世界的对象：它们也由`状态`和相关`行为`
组成。对象在`字段`（某些编程语言中的变量）中存储其状态，并通过`方法`（某些编程语言中的函数）展示其行为。`方法`对对象的`内部状态`
进行操作，并作为对象间通信的主要机制。`隐藏内部状态`并要求所有交互通过对象的`方法`进行，这被称为数据`封装`——面向对象编程的基本原则。

以自行车为例：

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202412062139053.png){ loading=lazy }
  <figcaption>作为软件对象建模的自行车。</figcaption>
</figure>

通过设置状态（当前速度、当前踏频和当前档位）并提供改变状态的方法，该对象能够控制外界如何使用它。例如，如果自行车只有6个档位，那么改变档位的方法可以拒绝任何小于1或大于6的值。

将代码打包成单独的软件对象带来了许多好处，包括：

1. `模块化`：一个对象的源代码可以独立于其他对象的源代码编写和维护。一旦创建，一个对象可以在系统内部轻松传递。
2. `信息隐藏`：通过仅与对象的方法交互，其内部实现的细节对外界保持隐藏。
3. `代码重用`：如果一个对象已经存在（可能由其他软件开发人员编写），你可以在你的程序中使用该对象。这使得专家可以实现、测试和调试复杂的、特定任务的对象，然后你可以放心地在自己的代码中运行它们。
4. `可插拔性和调试便利性`：如果某个特定对象出现问题，你可以简单地将其从应用程序中移除，并插入一个不同的对象作为替换。这类似于在现实世界中修复机械问题。如果一个螺栓断了，你只需更换它，而不是整个机器。

### 创建对象

正如你所知，类为对象提供了蓝图；你可以从类中创建对象。以下每个语句都创建了一个对象，并将其赋值给一个变量：

``` java
Point originOne = new Point(23, 94);
Rectangle rectOne = new Rectangle(originOne, 100, 200);
Rectangle rectTwo = new Rectangle(50, 100);
```

第一行创建了一个 Point 类的对象，第二行和第三行分别创建了一个 Rectangle 类的对象。

这些陈述中的每一个都包含三个部分（详见下文）：

- `声明(Declaration)`：将变量名与对象类型关联起来。
- `实例化(Instantiation)`：new 关键字是一个 Java 运算符，用于创建对象。
- `初始化(Initialization)`：new 运算符后面跟随对构造函数的调用，用于初始化新对象。

#### 声明一个变量以引用对象

之前，您了解到要声明一个变量，您需要这样写：

``` java
type name;
```

这通知编译器，你将使用名称来引用类型为type的数据。对于一个基本变量，这个声明还为变量预留了适当的内存空间。

您也可以在单独一行上声明一个引用变量。例如：

``` java
Point originOne;
```

如果你像这样声明 originOne，那么在实际创建并赋值给它之前，它的值将是不确定的。仅仅声明一个引用变量并不会创建对象。为此，你需要使用
new 操作符，具体将在下一节中描述。在使用 originOne 之前，必须先为其赋值一个对象，否则会出现编译错误。

处于这种状态的变量当前不引用任何对象，可以如下图示（变量名为originOne，加上一个指向空的引用）：

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202412121450333.png){ loading=lazy }
  <figcaption></figcaption>
</figure>

#### 实例化类

`new 运算符`通过为新对象分配内存并返回该内存的引用来实例化一个类。new 运算符还会调用对象的构造函数。

!!! note

	“实例化一个类”与“创建一个对象”是同一个意思。当你创建一个对象时，你是在创建一个类的“实例”，因此就是在“实例化”一个类。

new 运算符需要一个单一的后缀参数：对构造函数的调用。构造函数的名称提供了要实例化的类的名称。

new 操作符返回对其创建的对象的引用。这个引用通常被分配给一个合适类型的变量，例如：

``` java
Point originOne = new Point(23, 94);
```

由 new 运算符返回的引用不必赋值给变量。它也可以直接用于表达式中。例如：

``` java
int height = new Rectangle().height;
```

#### 初始化对象

这是 Point 类的代码：

``` java
public class Point {
    public int x = 0;
    public int y = 0;
    //constructor
    public Point(int a, int b) {
        x = a;
        y = b;
    }
}
```

这个类包含一个单一的构造函数。你可以通过其声明与类名相同且没有返回类型来识别构造函数。Point类中的构造函数接受两个整数参数，如代码所声明的 (
int a, int b)。以下语句为这些参数提供了23和94作为值：

``` java
Point originOne = new Point(23, 94);
```

执行此语句的结果可以在下图中说明：

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202412131036080.png){ loading=lazy }
  <figcaption>初始化对象</figcaption>
</figure>

以下是 Rectangle 类的代码，其中包含四个构造函数：

``` java
public class Rectangle {
    public int width = 0;
    public int height = 0;
    public Point origin;

    // four constructors
    public Rectangle() {
        origin = new Point(0, 0);
    }
    public Rectangle(Point p) {
        origin = p;
    }
    public Rectangle(int w, int h) {
        origin = new Point(0, 0);
        width = w;
        height = h;
    }
    public Rectangle(Point p, int w, int h) {
        origin = p;
        width = w;
        height = h;
    }

    // a method for moving the rectangle
    public void move(int x, int y) {
        origin.x = x;
        origin.y = y;
    }

    // a method for computing the area of the rectangle
    public int getArea() {
        return width * height;
    }
}
```

每个构造函数都允许您为矩形的起点、宽度和高度提供初始值，可以使用基本类型和引用类型。如果一个类有多个构造函数，它们必须具有不同的签名。Java编译器根据参数的数量和类型来区分构造函数。当Java编译器遇到以下代码时，它知道要调用Rectangle类中需要一个Point参数和两个整数参数的构造函数：

``` java
Rectangle rectOne = new Rectangle(originOne, 100, 200);
```

这将调用 Rectangle 的一个构造函数，将起点初始化为 originOne。同时，构造函数将宽度设置为 100，高度设置为 200。现在有两个引用指向同一个
Point 对象——一个对象可以有多个引用，如下图所示：

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202412131057311.png){ loading=lazy }
  <figcaption>一个对象可以有多个引用</figcaption>
</figure>

以下代码行调用了需要两个整数参数的矩形构造函数，这两个参数为宽度和高度提供初始值。如果你查看构造函数中的代码，你会发现它创建了一个新的点对象，其
x 和 y 值被初始化为 0：

``` java
Rectangle rectTwo = new Rectangle(50, 100);
```

以下语句中使用的矩形构造函数不接受任何参数，因此称为无参数构造函数：

``` java
Rectangle rect = new Rectangle();
```

所有类至少有一个构造函数。如果一个类没有显式声明任何构造函数，Java编译器会自动提供一个无参构造函数，称为默认构造函数。这个默认构造函数会调用类父类的无参构造函数，如果该类没有其他父类，则调用Object的构造函数。如果父类没有构造函数（Object类是有的），编译器将拒绝该程序。

### 使用对象

一旦创建了一个对象，你可能会想要用它来做些事情。你可能需要使用它某个字段的值，修改某个字段，或者调用它的方法来执行某个操作。

#### 引用对象的字段

对象字段通过其名称进行访问。您必须使用明确的名称。

在类内部，你可以为字段使用简单的名称。例如，我们可以在 Rectangle 类中添加一个语句来打印宽度和高度：

``` java
System.out.println("Width and height are: " + width + ", " + height);
```

在这种情况下，宽度和高度是简单的名称。

在对象类之外的代码必须使用对象引用或表达式，后接点号（.）运算符，再接简单字段名称，如下所示：

``` java
objectReference.fieldName 
```

例如，CreateObjectDemo 类中的代码位于 Rectangle 类的代码之外。因此，要引用名为 rectOne 的 Rectangle 对象中的 origin、width 和
height 字段，CreateObjectDemo 类必须分别使用 rectOne.origin、rectOne.width 和 rectOne.height 这些名称。程序使用其中两个名称来显示
rectOne 的宽度和高度：

``` java
System.out.println("Width of rectOne: "  + rectOne.width);
System.out.println("Height of rectOne: " + rectOne.height);
```

尝试在 CreateObjectDemo 类的代码中使用简单名称 width 和 height 是没有意义的——这些字段仅存在于对象内部——并会导致编译错误。

稍后，程序使用类似的代码来显示关于 rectTwo 的信息。同类型的对象拥有相同实例字段的各自副本。因此，每个 Rectangle 对象都有名为
origin、width 和 height 的字段。当通过对象引用访问实例字段时，您引用的是该特定对象的字段。在 CreateObjectDemo 程序中，两个对象
rectOne 和 rectTwo 拥有不同的 origin、width 和 height 字段。

要访问字段，您可以使用对对象的命名引用，就像前面的例子中那样，或者您可以使用任何返回对象引用的表达式。请记住，new
操作符会返回一个对象的引用。因此，您可以使用 new 返回的值来访问新对象的字段：

``` java
int height = new Rectangle().height;
```

这条语句创建了一个新的矩形对象，并立即获取其高度。本质上，这条语句计算了矩形的默认高度。请注意，在这条语句执行后，程序不再持有对创建的矩形的引用，因为程序从未将该引用存储在任何地方。该对象没有被引用，其资源可以被
Java 虚拟机回收。

#### 调用对象的方法

您还可以使用对象引用来调用对象的方法。将方法的简单名称附加到对象引用上，中间使用点运算符（.）。同时，在括号内提供方法的任何参数。如果方法不需要任何参数，则使用空括号。

``` java
objectReference.methodName(argumentList);

// 或

objectReference.methodName();
```

Rectangle 类有两个方法：getArea() 用于计算矩形的面积，move() 用于改变矩形的原点。以下是调用这两个方法的 CreateObjectDemo
代码：

``` java
System.out.println("Area of rectOne: " + rectOne.getArea());
...
rectTwo.move(40, 72);
```

第一句调用了 rectOne 的 getArea() 方法并显示结果。第二句移动了 rectTwo，因为 move() 方法为对象的 origin.x 和 origin.y
赋予了新值。

与实例字段一样，objectReference必须是一个对象的引用。你可以使用变量名，但也可以使用任何返回对象引用的表达式。new操作符返回一个对象引用，因此你可以使用new返回的值来调用新对象的方法：

``` java
new Rectangle(100, 50).getArea()
```

表达式 new Rectangle(100, 50) 返回一个指向 Rectangle 对象的对象引用。如图所示，你可以使用点符号调用新 Rectangle 的
getArea() 方法来计算新矩形的面积。

一些方法，例如 getArea()，会返回一个值。对于返回值的方法，你可以在表达式中调用这些方法。你可以将返回值赋给一个变量，用于决策，或者控制循环。以下代码将
getArea() 返回的值赋给变量 areaOfRectangle：

``` java
int areaOfRectangle = new Rectangle(100, 50).getArea();
```

请记住，在特定对象上调用方法就像向该对象发送消息。在这种情况下，调用 getArea() 的对象是由构造函数返回的矩形。

#### 垃圾回收器

某些面向对象的语言要求你跟踪所有创建的对象，并在不再需要时显式销毁它们。手动管理内存既繁琐又容易出错。Java平台允许你根据系统的承受能力创建任意数量的对象，而无需担心销毁它们。Java运行时环境会在
`确定对象不再被使用时自动删除它们`，这个过程称为`垃圾回收`。

当一个对象不再有任何引用时，它就有资格进行垃圾回收。通常，当变量超出其作用域时，变量中持有的引用会被丢弃。或者，你可以通过将变量设置为特殊值
null 来显式地丢弃对象引用。请记住，一个程序可以对同一个对象有多个引用；必须丢弃所有对该对象的引用后，该对象才有资格进行垃圾回收。

Java 运行时环境具有一个垃圾收集器，它会定期释放不再被引用的对象所占用的内存。垃圾收集器会在认为合适的时机自动执行其任务。

## 接口(Interface)

### 什么是接口

正如你已经了解到的，对象通过它们公开的方法来定义与外界的交互。方法构成了对象与外界的接口；例如，电视机前面的按钮就是你与其塑料外壳另一侧的电线之间的接口。你按下
`电源`按钮来打开和关闭电视。

在最常见的形式中，接口是一组相关的方法，这些方法没有具体实现。如果将自行车的行为定义为接口，可能会如下所示：

``` java
interface Bicycle {

	//  wheel revolutions per minute
	void changeCadence(int newValue);

	void changeGear(int newValue);

	void speedUp(int increment);

	void applyBrakes(int decrement);
}
```

要实现这个接口，您的类名需要更改（例如，改为某个特定品牌的自行车，如ACMEBicycle），并在类声明中使用`implements关键字`：

``` java
class ACMEBicycle implements Bicycle {

	int cadence = 0;
	int speed = 0;
	int gear = 1;

	// 编译器现在将要求实现方法 
	// changeCadence、changeGear、speedUp 和 applyBrakes。
	// 如果这个类中缺少这些方法，编译将失败。

	void changeCadence(int newValue) {
		cadence = newValue;
	}

	void changeGear(int newValue) {
		gear = newValue;
	}

	void speedUp(int increment) {
		speed = speed + increment;
	}

	void applyBrakes(int decrement) {
		speed = speed - decrement;
	}

	void printStates() {
		System.out.println("cadence:" +
				cadence + " speed:" +
				speed + " gear:" + gear);
	}
}
```

实现接口使类对其承诺提供的行口在类与外部世界之间形成了一种契约，这种契约在构建时由编译器强制执行。如果一个类声称实现了某个接口，那么该接口定义的所有方法都必须出现在类的源代码中，否则类将无法成功编译。

!!! note

	要实际编译ACMEBicycle类，您需要在实现的接口方法开头添加`public关键字`。关于这样做的原因，您将在后续的类与对象、接口与继承课程中学习到。

## 包(Package)

### 什么是包

包是一个`命名空间`
，用于组织一组相关的类和接口。从概念上讲，你可以将包视为类似于计算机上的不同文件夹。你可能会将HTML页面放在一个文件夹中，图像放在另一个文件夹中，脚本或应用程序放在另一个文件夹中。由于用Java编程语言编写的软件可能由数百或数千个独立的类组成，因此通过将相关的类和接口放入包中来保持组织性是合理的。

Java 平台提供了一个庞大的类库（即一组包），适用于您自己的应用程序。这个类库被称为“应用程序编程接口”，简称“API”。这些包代表了与通用编程最常相关的任务。例如，String
对象包含字符字符串的状态和行为；File 对象使程序员能够轻松地在文件系统上创建、删除、检查、比较或修改文件；Socket
对象允许创建和使用网络套接字；各种 GUI 对象控制按钮和复选框以及其他与图形用户界面相关的元素。实际上有成千上万的类可供选择。这使得程序员可以专注于特定应用程序的设计，而不是其运行所需的基础设施。

Java平台API规范包含Java SE平台提供的所有包、接口、类、字段和方法的完整列表。在浏览器中加载该页面并将其加入书签。作为程序员，这将成为您最重要的参考文档。

## 嵌套类(Nested Classes)

Java 编程语言允许您在一个类中定义另一个类。这样的类称为嵌套类，示例如下：

``` java
class OuterClass {
    ...
    class NestedClass {
        ...
    }
}
```

`嵌套类`分为两类：`非静态`和`静态`。非静态嵌套类称为`内部类`。声明为静态的嵌套类称为`静态嵌套类`。

``` java
class OuterClass {
    ...
    class InnerClass {
        ...
    }
    static class StaticNestedClass {
        ...
    }
}
```

嵌套类是其外部类的成员。非静态嵌套类（内部类）可以访问外部类的其他成员，即使这些成员被声明为私有。
`静态嵌套类则无法访问外部类的其他成员`。

作为OuterClass的成员，嵌套类可以被声明为private, public, protected或包私有。（请记住，外部类只能被声明为public 或包私有。）

### 为什么使用嵌套类？

使用嵌套类的充分理由包括以下几点：

- 这是一种逻辑上将仅在一个地方使用的类进行分组的方法：如果一个类仅对另一个类有用，那么将其嵌入到该类中并将两者放在一起是合乎逻辑的。嵌套这样的
  `辅助类`可以使它们的包更加简洁。
- 它增加了`封装性`：考虑两个顶级类，A 和 B，其中 B 需要访问 A 的成员，而这些成员本来会被声明为私有。通过将类 B 隐藏在类 A
  内部，A 的成员可以声明为私有，而 B 可以访问它们。此外，B 本身也可以对外界隐藏。
- 这可以使代码更易读和维护：将小类嵌套在顶级类中，使代码更接近其使用位置。

### 内部类

与实例方法和变量一样，内部类与其外部类的实例相关联，并可以`直接访问该对象的方法和字段`。此外，由于内部类与实例相关联，因此它
`不能定义任何静态成员`。

内部类的实例对象存在于外部类的实例中。请考虑以下类：

``` java
class OuterClass {
    ...
    class InnerClass {
        ...
    }
}
```

InnerClass的实例只能存在于OuterClass的实例中，并且可以直接访问其外部实例的方法和字段。

要实例化一个内部类，必须先实例化外部类。然后，使用以下语法在外部对象中创建内部对象：

``` java
OuterClass outerObject = new OuterClass();
OuterClass.InnerClass innerObject = outerObject.new InnerClass();
```

### 静态嵌套类

与类方法和变量类似，静态嵌套类与其外部类相关联。与静态类方法一样，静态嵌套类不能直接引用其封闭类中定义的实例变量或方法：它只能通过对象引用来使用它们。

!!! note

	静态嵌套类与其外部类（以及其他类）的实例成员的交互方式与任何其他顶级类相同。实际上，静态嵌套类在行为上是一个顶级类，只是为了方便打包而嵌套在另一个顶级类中。

实例化静态嵌套类的方式与实例化顶级类相同：

``` java
StaticNestedClass staticNestedObject = new StaticNestedClass();
```

### 变量遮蔽问题

如果在特定作用域（例如内部类或方法定义）中声明的类型（如成员变量或参数名）与外部作用域中的另一个声明同名，那么该声明会遮蔽外部作用域的声明。你不能仅通过名称来引用被遮蔽的声明。以下示例，ShadowTest，演示了这一点：

``` java
public class ShadowTest {

    public int x = 0;

    class FirstLevel {

        public int x = 1;

        void methodInFirstLevel(int x) {
        	// => 23
            System.out.println("x = " + x); 
            // => 1
            System.out.println("this.x = " + this.x); 
			// => 0
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

此示例定义了三个名为 x 的变量：`类 ShadowTest 的成员变量`、`内部类 FirstLevel 的成员变量`，以及`方法 methodInFirstLevel 的参数`
。在方法 methodInFirstLevel 中定义为参数的变量 x 会`遮蔽`内部类 FirstLevel 的变量。因此，当在方法 methodInFirstLevel 中使用变量
x 时，它指的是方法参数。要引用内部类 FirstLevel 的成员变量，请使用关键字 this 来表示封闭范围：

``` java
System.out.println("this.x = " + this.x);
```

通过类名引用包含更大范围的成员变量。例如，以下语句从方法 methodInFirstLevel 访问类 ShadowTest 的成员变量：

``` java
System.out.println("ShadowTest.this.x = " + ShadowTest.this.x);
```

### 序列化问题

强烈建议不要对内部类进行序列化，包括局部类和匿名类。当Java编译器编译某些结构（如内部类）时，会创建合成结构；这些是类、方法、字段和其他在源代码中没有对应结构的构造。合成结构使Java编译器能够在不更改JVM的情况下实现新的Java语言特性。然而，不同的Java编译器实现之间的合成结构可能会有所不同，这意味着.class文件在不同的实现之间也可能有所不同。因此，如果你序列化一个内部类，然后在不同的JRE实现中反序列化，可能会出现兼容性问题。有关编译内部类时生成的合成结构的更多信息，请参阅获取方法参数名称部分中的隐式和合成参数部分。

### 内部类示例

要查看内部类的使用，首先考虑一个数组。在下面的例子中，你创建一个数组，用整数值填充它，然后仅按升序输出数组中偶数索引的值。

以下是 DataStructure.java 示例的组成部分：

- DataStructure外部类，包括一个构造函数，用于创建一个包含连续整数值数组（0, 1, 2, 3等）的DataStructure实例，以及一个打印数组中偶数索引元素的方法
- EvenIterator内部类实现了DataStructureIterator接口，而DataStructureIterator接口又扩展了Iterator<Integer>
  接口。迭代器用于遍历数据结构，通常具有测试是否为最后一个元素、获取当前元素以及移动到下一个元素的方法。
- 一个main方法实例化一个DataStructure对象（ds），然后调用printEven方法打印数组arrayOfInts中索引值为偶数的元素。

``` java
public class DataStructure {
    
    // 创建一个数组
    private final static int SIZE = 15;
    private int[] arrayOfInts = new int[SIZE];
    
    public DataStructure() {
        // 用递增的整数值填充数组
        for (int i = 0; i < SIZE; i++) {
            arrayOfInts[i] = i;
        }
    }
    
    public void printEven() {
        
        // 打印数组中偶数下标的值
        DataStructureIterator iterator = this.new EvenIterator();
        while (iterator.hasNext()) {
            System.out.print(iterator.next() + " ");
        }
        System.out.println();
    }
    
    interface DataStructureIterator extends java.util.Iterator<Integer> { } 

    // 内部类实现了DataStructureIterator接口，
    // 该接口继承了Iterator<Integer>接口
    
    private class EvenIterator implements DataStructureIterator {
        
        // 从数组的开始位置开始遍历
        private int nextIndex = 0;
        
        public boolean hasNext() {
            
            // 检查当前元素是否是数组中的最后一个元素
            return (nextIndex <= SIZE - 1);
        }        
        
        public Integer next() {
            
            // 记录数组中偶数下标的值
            Integer retValue = Integer.valueOf(arrayOfInts[nextIndex]);
            
            // 获取下一个偶数下标的元素
            nextIndex += 2;
            return retValue;
        }
    }
    
    public static void main(String s[]) {
        
        // 填充数组并只打印出偶数下标的值
        DataStructure ds = new DataStructure();
        ds.printEven();
    }
}
```

输出结果为：

``` shell
0 2 4 6 8 10 12 14 
```

请注意，EvenIterator 类直接引用了 DataStructure 对象的 arrayOfInts 实例变量。

您可以使用内部类来实现辅助类，例如本例中所示的类。要处理用户界面事件，您必须了解如何使用内部类，因为事件处理机制广泛使用了它们。

## 局部类(Local Classes)

### 声明局部类

您可以在任何代码块中定义局部类。例如，您可以在方法体、for 循环或 if 语句中定义局部类。

以下示例 LocalClassExample 用于验证两个电话号码。它在方法 validatePhoneNumber 中定义了局部类 PhoneNumber：

``` java
public class LocalClassExample {

	static String regularExpression = "[^0-9]";

	public static void validatePhoneNumber(
			String phoneNumber1, String phoneNumber2) {

		final int numberLength = 10;

		// JDK 8及以后版本有效：

		// int numberLength = 10;

		class PhoneNumber {

			String formattedPhoneNumber = null;

			PhoneNumber(String phoneNumber) {
				// numberLength = 7;
				String currentNumber = phoneNumber.replaceAll(
						regularExpression, "");
				if (currentNumber.length() == numberLength)
					formattedPhoneNumber = currentNumber;
				else
					formattedPhoneNumber = null;
			}

			public String getNumber() {
				return formattedPhoneNumber;
			}

			// JDK 8及以后版本有效：

//            public void printOriginalNumbers() {
//                System.out.println("原始电话号码是 " + phoneNumber1 +
//                    " 和 " + phoneNumber2);
//            }
		}

		PhoneNumber myNumber1 = new PhoneNumber(phoneNumber1);
		PhoneNumber myNumber2 = new PhoneNumber(phoneNumber2);

		// JDK 8及以后版本有效：

//        myNumber1.printOriginalNumbers();

		if (myNumber1.getNumber() == null)
			System.out.println("第一个电话号码无效");
		else
			System.out.println("第一个电话号码是 " + myNumber1.getNumber());
		if (myNumber2.getNumber() == null)
			System.out.println("第二个电话号码无效");
		else
			System.out.println("第二个电话号码是 " + myNumber2.getNumber());

	}

	public static void main(String... args) {
		validatePhoneNumber("123-456-7890", "456-7890");
	}
}
```

该示例通过首先移除电话号码中除数字0到9以外的所有字符来验证电话号码。之后，它检查电话号码是否正好包含十位数字（北美电话号码的长度）。

### 访问外围类的成员

局部类可以访问其外围类的成员。在前面的例子中，`PhoneNumber 构造函数`访问了成员 `LocalClassExample.regularExpression`。

此外，局部类可以访问`局部变量`。然而，局部类`只能访问被声明为 final 的局部变量`。当局部类访问封闭块的局部变量或参数时，它会捕获该变量或参数。例如，PhoneNumber 构造函数可以访问局部变量 numberLength，因为它被声明为 final；numberLength 是一个被捕获的变量。

从 Java SE 8 开始，局部类可以访问封闭块中那些`是 final` 或`有效 final `的局部变量和参数。一个变量或参数在初始化后其值从未改变，则被视为`有效 final`。例如，假设变量 numberLength 没有声明为 final，并且你在 PhoneNumber 构造函数中添加了高亮的赋值语句，将有效电话号码的长度更改为 7 位：

``` java title="❌" hl_lines="2"
PhoneNumber(String phoneNumber) {
    numberLength = 7;
    String currentNumber = phoneNumber.replaceAll(
        regularExpression, "");
    if (currentNumber.length() == numberLength)
        formattedPhoneNumber = currentNumber;
    else
        formattedPhoneNumber = null;
}
```

由于这个赋值语句，变量 numberLength 不再是有效的最终变量。因此，Java 编译器会生成类似于`内部类引用的局部变量必须是final的或有效final的`这样的错误信息，这是因为内部类 PhoneNumber 尝试访问 numberLength 变量。

``` java
if (currentNumber.length() == numberLength)
```

从 Java SE 8 开始，如果在方法中声明局部类，该类可以访问方法的参数。例如，您可以在 PhoneNumber 局部类中定义以下方法：

``` java
public void printOriginalNumbers() {
    System.out.println("Original numbers are " + phoneNumber1 +
        " and " + phoneNumber2);
}
```

方法 printOriginalNumbers 访问方法 validatePhoneNumber 的参数 phoneNumber1 和 phoneNumber2。

!!! "变量遮蔽和局部类"

    在局部类中声明的类型（例如变量）会遮蔽在外部作用域中具有相同名称的声明。

### 局部类类似于内部类

局部类与内部类相似，因为它们`不能定义或声明任何静态成员`。在静态方法中的局部类，例如在静态方法validatePhoneNumber中定义的类PhoneNumber，只能引用外部类的静态成员。例如，如果你没有将成员变量regularExpression定义为静态的，那么Java编译器会生成类似于`无法从静态上下文中引用非静态变量regularExpression`的错误。

局部类是非静态的，因为它们可以访问封闭块的实例成员。因此，它们不能包含大多数类型的静态声明。

您不能在代码块中声明接口；接口本质上是静态的。例如，以下代码片段无法编译，因为接口 HelloThere 被定义在方法 greetInEnglish 的主体内：




## 对象之间关系及UML表示

### 依赖

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409270853756.png){ loading=lazy }
  <figcaption>UML 图中的依赖。教授依赖于课程资料。</figcaption>
</figure>

依赖是类之间关系中最基本且最弱的一种类型。

如果一个类的定义发生变化可能导致另一个类需要修改，那么这两个类之间就存在`依赖关系`。

依赖通常发生在代码中使用具体类名时。比如：

- 方法形参中的类名
- 返回值中的类名

通过让代码依赖于`接口`或`抽象类`而不是具体类，可以使依赖关系更弱。

!!! note

	通常情况下，UML图不会展示所有依赖——它们在真实代码中的数量太多了。为了不让依赖关系破坏UML图，你必须对其进行精心选择，仅展示那些对于沟通你的想法来说重要的依赖关系。

### 关联

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409270855555.png){ loading=lazy }
  <figcaption>UML 图中的关联。教授与学生进行交流。</figcaption>
</figure>

`关联`是一个对象使用另一对象或与另一对象进行交互的关系。

`关联`可视为一种特殊类型的`依赖`，即一个对象总是拥有访问与其交互的对象的权限，而简单的依赖关系并不会在对象间建立永久性的联系。

举例说明：

- 字段
- getter方法

!!! note

	双向关联也是完全正常的，这种情况就用双向箭头来表示。

``` java
public class Professor {
	/*
	 * 学生（Student）类是教授类的依赖,如果remember（记住）方法被修改，教授的代码也将崩溃。
	 * 由于教授的所有方法总能访问student成员变量，所以学生类就不仅是依赖，而也是关联
	 * */
	private Student student;


	/*
	 * 接收一个来自课程（Course）类的参数。
	 * 如果有人修改了课程类的getKnowledge（获取知识）方法（修改方法名或添加一些必须的参数等），
	 * 代码将会崩溃。这就是依赖关系。
	 * */
	void teach(Course c) {
		this.student.remember(c.getKnowledge());
	}
}
```

### 聚合

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409271014763.png){ loading=lazy }
  <figcaption>UML 图中的聚合。院系包含教授。</figcaption>
</figure>

`聚合`是一种特殊类型的`关联`，用于表示多个对象之间的`一对多`、`多对多`或`整体对部分`的关系。

通常在聚合关系中，一个对象`拥有`一组其他对象，并扮演着容器或集合的角色。

`组件可以独立于容器存在`，也可以同时连接多个容器。

### 组合

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409271452730.png){ loading=lazy }
  <figcaption>UML 图中的组合。大学由院系构成。</figcaption>
</figure>

`组合`是一种特殊类型的`聚合`，其中一个对象由一个或多个其他对象实例`构成`。

!!! warning

	注意，许多人常常在实际想说聚合和组合时使用`组合`这个术语。 其中最恶名昭彰的例子是著名的`组合优于继承`原则。这并不是因为人们不清楚它们之间的差别，而是因为`组合（例如‘对象组合’）`说起来更顺口。

### 小结

- `依赖`：对类B进行修改会影响到类A。
- `关联`：对象A知道对象B。类A依赖于类B。
- `聚合`：对象A知道对象B且由B构成。类A依赖于类B。
- `组合`：对象A知道对象B、由B构成而且管理着B的生命周期。类A依赖于类B。
- `实现`：类A定义的方法由接口B声明。对象A可被视为对象B。类A依赖于类B。
- `继承`：类A继承类B的接口和实现，但是可以对其进行扩展。对象A可被视为对象B。类A依赖于类B。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409271516685.png){ loading=lazy }
  <figcaption>对象和类之间的关系：从弱到强。</figcaption>
</figure>

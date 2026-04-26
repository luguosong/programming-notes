---
title: 设计原则
---

# 设计原则

设计模式是"套路"，而设计原则是"道理"。先理解原则背后的思想，才能在实际项目中判断什么时候用哪种模式，而不是生搬硬套。

## SOLID 原则

SOLID 是五条面向对象设计原则的缩写，由 Robert C. Martin（"Uncle Bob"）整理推广：

| 字母 | 原则 | 一句话总结 |
|------|------|-----------|
| S | 单一职责原则（SRP） | 一个类只做一件事 |
| O | 开闭原则（OCP） | 对扩展开放，对修改关闭 |
| L | 里氏替换原则（LSP） | 子类可以无缝替换父类 |
| I | 接口隔离原则（ISP） | 接口要细化，不强迫依赖不需要的方法 |
| D | 依赖倒置原则（DIP） | 依赖抽象，不依赖具体实现 |

### 单一职责原则

当一个类承担的职责越多，它被修改的理由就越多，耦合就越高——任何一处改动都可能引发意外的连锁反应。

``` java title="违反 SRP"
// ❌ User 类承担了三件事：数据存储、发邮件、写日志
public class User {
    private String name;
    private String email;

    // 职责一：业务数据
    public void setName(String name) { this.name = name; }

    // 职责二：发邮件（和用户数据无关）
    public void sendWelcomeEmail() {
        // 调用邮件服务...
    }

    // 职责三：记录日志（和用户数据无关）
    public void logUserCreation() {
        // 写日志文件...
    }
}
```

``` java title="遵循 SRP"
// ✅ 职责拆分后，每个类只做一件事
public class User {
    private String name;
    private String email;
    // 只管数据
}

public class EmailService {
    public void sendWelcomeEmail(User user) {
        // 只管发邮件
    }
}

public class UserLogger {
    public void logCreation(User user) {
        // 只管日志
    }
}
```

!!! tip "判断技巧"

    判断是否违反 SRP，可以问：「这个类为什么会被修改？」如果有多个答案，就需要拆分。

### 开闭原则

每次修改已有代码都有引入新 bug 的风险，而扩展新代码只会增加，不会破坏已有逻辑。

``` java title="违反 OCP"
// ❌ 每次新增折扣类型，都要修改这个方法
public class OrderService {
    public double calculateDiscount(Order order, String discountType) {
        if ("VIP".equals(discountType)) {
            return order.getAmount() * 0.8;
        } else if ("STUDENT".equals(discountType)) {
            return order.getAmount() * 0.9;
        } else if ("EMPLOYEE".equals(discountType)) { // ← 新增时要改这里
            return order.getAmount() * 0.7;
        }
        return order.getAmount();
    }
}
```

``` java title="遵循 OCP"
// ✅ 定义折扣策略接口，新增折扣类型只需新增实现类，无需改已有代码
public interface DiscountStrategy {
    double calculate(double amount);
}

public class VipDiscount implements DiscountStrategy {
    @Override
    public double calculate(double amount) {
        return amount * 0.8; // VIP 八折
    }
}

public class StudentDiscount implements DiscountStrategy {
    @Override
    public double calculate(double amount) {
        return amount * 0.9; // 学生九折
    }
}

// 新增员工折扣：只需加这个类，其他代码不动
public class EmployeeDiscount implements DiscountStrategy {
    @Override
    public double calculate(double amount) {
        return amount * 0.7; // 员工七折
    }
}
```

!!! tip "实践提示"

    "关闭修改"不等于"永不修改"——bug 修复和重构仍然需要修改代码。OCP 的重点是设计扩展点，让新需求通过"加法"而非"改法"来实现。

### 里氏替换原则

里氏替换原则（Liskov Substitution Principle）由 Barbara Liskov 在 1987 年提出：**子类对象能够替换其父类对象，并且程序行为不变。**

经典的反例：`Square`（正方形）继承 `Rectangle`（矩形）。

``` java title="违反 LSP"
// ❌ Square 继承 Rectangle，但行为不一致
public class Rectangle {
    protected int width;
    protected int height;

    public void setWidth(int width)   { this.width = width; }
    public void setHeight(int height) { this.height = height; }
    public int area() { return width * height; }
}

public class Square extends Rectangle {
    @Override
    public void setWidth(int width) {
        this.width = width;
        this.height = width; // ← 修改了高度！违背矩形约定
    }

    @Override
    public void setHeight(int height) {
        this.width  = height;
        this.height = height;
    }
}

// 调用方按矩形理解使用，结果出错
void useRectangle(Rectangle r) {
    r.setWidth(5);
    r.setHeight(3);
    System.out.println(r.area()); // 期望 15，但 Square 输出 9！ ❌
}
```

``` java title="修复方案"
// ✅ Rectangle 和 Square 各自独立，不存在继承关系
public interface Shape {
    int area();
}

public class Rectangle implements Shape { ... }
public class Square    implements Shape { ... }
```

!!! tip "实践提示"

    不是所有"IS-A"的语义关系都适合用继承——正方形在数学上是矩形，但在代码里不一定是。用继承前先问：「子类是否完全满足父类的所有契约？」

### 接口隔离原则

一个接口方法越多，实现它的类就越可能被迫实现"用不到"的方法，导致空实现或异常抛出。

``` java title="违反 ISP"
// ❌ 胖接口：Dog 被迫实现 fly()
public interface Animal {
    void eat();
    void fly();   // 狗不会飞！
    void swim();
}

public class Dog implements Animal {
    @Override public void eat()  { System.out.println("吃饭"); }
    @Override public void fly()  { throw new UnsupportedOperationException("狗不会飞"); } // ❌ 被迫的空实现
    @Override public void swim() { System.out.println("游泳"); }
}
```

``` java title="遵循 ISP"
// ✅ 细粒度接口：按行为能力拆分
public interface Eatable  { void eat(); }
public interface Flyable  { void fly(); }
public interface Swimmable { void swim(); }

// Dog 只实现它能做到的
public class Dog implements Eatable, Swimmable {
    @Override public void eat()  { System.out.println("吃饭"); }
    @Override public void swim() { System.out.println("游泳"); }
}

// Bird 实现飞行和进食
public class Bird implements Eatable, Flyable {
    @Override public void eat() { System.out.println("啄食"); }
    @Override public void fly() { System.out.println("飞翔"); }
}
```

!!! tip "实践提示"

    接口不是越细越好——过度拆分会导致接口爆炸。合理的粒度是"按角色"或"按能力维度"划分，而非"一个方法一个接口"。

### 依赖倒置原则

高层模块（业务逻辑）不应该直接依赖低层模块（数据库、文件系统等具体实现），两者都应该依赖抽象（接口或抽象类）。

``` java title="违反 DIP"
// ❌ 高层模块直接依赖低层具体类
public class UserService {
    private MySQLUserRepository repository; // ← 直接依赖 MySQL 实现

    public UserService() {
        this.repository = new MySQLUserRepository(); // ← 硬编码创建
    }
}
// 想换成 MongoDB？UserService 必须修改——这正是耦合的危害
```

``` java title="遵循 DIP"
// ✅ 依赖抽象接口，具体实现通过构造函数注入
public interface UserRepository {
    User findById(Long id);
    void save(User user);
}

public class MySQLUserRepository implements UserRepository { /* MySQL 实现 */ }
public class MongoUserRepository  implements UserRepository { /* MongoDB 实现 */ }

// UserService 只知道 UserRepository 接口，不关心具体实现
public class UserService {
    private final UserRepository repository;

    // 通过构造函数注入（Spring 的 @Autowired 就是这个原理）
    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public User findById(Long id) {
        return repository.findById(id);
    }
}
```

!!! tip "实践提示"

    DIP 是 Spring IoC（控制反转）的理论基础。你写 `@Autowired UserRepository repository` 时，就是在践行依赖倒置原则。

## 其他常用原则

### DRY（Don't Repeat Yourself）

不要重复你自己。每一份知识（逻辑、数据）都应该在系统中有且只有一处权威表示。

重复代码带来的问题：修改一处逻辑时，必须找到所有副本逐一修改——遗漏一处就是 bug。

``` java title="违反 DRY"
// ❌ 两处都写了相同的邮箱校验逻辑
public class UserController {
    public void register(String email) {
        if (!email.contains("@") || email.length() < 5) { // ← 重复
            throw new IllegalArgumentException("邮箱格式不正确");
        }
    }
}

public class AdminController {
    public void addAdmin(String email) {
        if (!email.contains("@") || email.length() < 5) { // ← 重复
            throw new IllegalArgumentException("邮箱格式不正确");
        }
    }
}
```

``` java title="遵循 DRY"
// ✅ 提取为公共工具类
public class EmailValidator {
    public static boolean isValid(String email) {
        return email.contains("@") && email.length() >= 5;
    }
}
```

### KISS（Keep It Simple, Stupid）

保持简单，不要做不必要的复杂化。最简单的、能完成需求的方案往往是最好的方案。

!!! warning "警惕过度设计"

    刚学会某个模式的程序员往往会过度使用它，看什么都像钉子。如果简单的 if-else 就能解决问题，就不需要策略模式。

### YAGNI（You Aren't Gonna Need It）

你不会需要它的。不要为当前需求之外"可能用到"的功能提前设计——这会增加不必要的复杂度和维护负担。

!!! tip "YAGNI 与 OCP 的区别"

    两者看似矛盾——一个说"不要预留扩展点"，一个说"设计扩展点"。区别在于：**已知会变化的点**（如折扣类型、支付方式）应用 OCP；**臆想可能变化的点**应用 YAGNI。

### 迪米特法则

迪米特法则（Law of Demeter，LoD）也叫"最少知识原则"：一个对象应当尽可能少地了解其他对象的内部结构。

**常见表述**：只和你的"直接朋友"说话——方法的参数、成员变量、方法内创建的对象、方法的返回值，这四类是"直接朋友"；其他的都是"陌生人"，不要直接调用。

``` java title="违反迪米特法则"
// ❌ 深层链式调用，暴露了对象内部结构
public class OrderService {
    public String getCityName(Order order) {
        // 需要了解 Order → Customer → Address → City 的内部结构
        return order.getCustomer().getAddress().getCity().getName();
    }
}
```

``` java title="遵循迪米特法则"
// ✅ 通过封装隐藏内部结构
public class Order {
    public String getCustomerCityName() {
        return customer.getCityName();
    }
}

public class Customer {
    public String getCityName() {
        return address.getCityName();
    }
}
```

!!! tip "实践提示"

    链式调用 `a.getB().getC().getD()` 通常是迪米特法则的警报信号。但 Builder 模式的链式调用（`builder.setA().setB().build()`）和流式 API（`stream.filter().map().collect()`）是例外——它们都作用于同一个对象。

### 合成复用原则

优先使用组合（Composition）而不是继承（Inheritance）来复用代码。

继承复用的缺点：子类与父类高度耦合，父类的修改直接影响所有子类；继承关系在编译时固定，运行时无法切换行为。

``` java title="用继承复用（不推荐）"
// ❌ 用继承复用日志功能：与 LoggableService 强绑定
public class LoggableService {
    protected void log(String msg) { System.out.println("[LOG] " + msg); }
}

public class UserService extends LoggableService {
    public void createUser() {
        log("创建用户");
    }
}
```

``` java title="用组合复用（推荐）"
// ✅ Logger 作为成员变量注入，耦合度更低
public class Logger {
    public void log(String msg) { System.out.println("[LOG] " + msg); }
}

public class UserService {
    private final Logger logger;

    public UserService(Logger logger) { this.logger = logger; }

    public void createUser() {
        logger.log("创建用户");
    }
}
```

!!! tip "实践提示"

    "继承"适合真正的 IS-A 关系（`Dog IS-A Animal`），"组合"适合 HAS-A 关系（`Car HAS-A Engine`）。实践中，组合比继承用得更多，因为它更灵活、耦合度更低。

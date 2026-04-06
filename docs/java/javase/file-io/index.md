# 文件和 IO 流

你的 Java 程序运行在内存中，但数据往往存储在硬盘文件里。当你需要读取一个配置文件、保存用户上传的图片、或者把日志写入磁盘时，就需要 IO（Input/Output）流来充当内存与外部世界之间的「桥梁」。

**🎯 本文你会学到**：

- 📁 如何用 `File` 类在代码中表示文件和目录
- ⚡ 什么是节点流——直接对接数据源的底层流
- 🎨 装饰器模式如何让 IO 流具备了「即插即用」的扩展能力
- 🔧 包装流如何在节点流基础上叠加缓冲、编码转换、对象序列化等高级功能
- 🛡️ 如何用 `try-with-resources` 优雅地管理资源

## 🗂️ IO 流解决什么问题？

当你的程序需要和外部环境交换数据时（读文件、写网络、接收键盘输入），直接操作操作系统的文件描述符既繁琐又容易出错。Java 用「流」（Stream）这个抽象概念把数据的读写统一为：**打开 → 读/写 → 关闭**，不管数据来自文件、网络还是内存，操作方式都是一致的。

### 按方向分——输入流与输出流

方向是站在**内存（程序）**的视角来看的：

| 方向 | 含义 | 类比 |
|------|------|------|
| `输入流`（Input） | 数据从外部 → 内存 | 水龙头往杯子里倒水 |
| `输出流`（Output） | 数据从内存 → 外部 | 杯子往水池里倒水 |

### 按数据单位分——字节流与字符流

| 类型 | 数据单位 | 适用场景 | 顶级抽象类 |
|------|---------|---------|-----------|
| `字节流` | 8 位字节（byte） | 所有文件（图片、视频、二进制） | `InputStream` / `OutputStream` |
| `字符流` | 字符（char），按编码处理 | 文本文件（.txt、.java、.xml） | `Reader` / `Writer` |

⚠️ 如果用字节流读文本文件，一个中文汉字占多个字节（UTF-8 下占 3 字节），读取不完整就会出现**乱码**。所以读写文本时优先选择字符流。

### 按功能分——节点流与包装流

这是理解 IO 流体系的**最关键分类**，也是本文的主线：

| 类型 | 特点 | 类比 |
|------|------|------|
| `节点流`（Node Stream） | 直接连接数据源或目标 | 水管直接接在水龙头上 |
| `包装流`（Processing Stream） | 包裹在节点流外面，增强功能 | 给水管加上过滤器、加压器 |

→ 这种「在已有功能上套一层新功能」的设计，正是`装饰器模式`的经典应用。后文会详细展开。

### 四大顶级抽象类

Java 的所有 IO 流都继承自这四个抽象类：

``` mermaid
graph TD
    IO["Java IO 流体系"] --> ByteIn["InputStream\n字节输入流"]
    IO --> ByteOut["OutputStream\n字节输出流"]
    IO --> CharIn["Reader\n字符输入流"]
    IO --> CharOut["Writer\n字符输出流"]

    classDef root fill:transparent,stroke:#539bf5,color:#adbac7,stroke-width:2px
    classDef node fill:transparent,stroke:#768390,color:#adbac7,stroke-width:1px
    class IO root
    class ByteIn,ByteOut,CharIn,CharOut node
```

它们都实现了 `Closeable` 接口（因此可以用 `try-with-resources` 自动关闭），输出流还额外实现了 `Flushable` 接口（将缓冲区数据强制写出）。

## 📁 File 类——如何在代码中表示文件？

在使用 IO 流之前，你通常需要先用 `File` 类来指定要操作的文件或目录。`File` 是文件路径的**抽象表示**——它可以指向一个文件，也可以指向一个目录，但它本身不负责读写数据。

### 创建 File 对象

``` java title="创建 File 对象的三种方式"
--8<-- "code/java/io/io-file/src/test/java/com/luguosong/io/FileTest.java:create_file_object"
```

### 常用方法速查

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `exists()` | `boolean` | 文件或目录是否存在 |
| `isFile()` | `boolean` | 是否为文件 |
| `isDirectory()` | `boolean` | 是否为目录 |
| `getName()` | `String` | 获取文件名 |
| `getAbsolutePath()` | `String` | 获取绝对路径 |
| `createNewFile()` | `boolean` | 创建新文件（父目录必须已存在） |
| `mkdir()` | `boolean` | 创建单级目录 |
| `mkdirs()` | `boolean` | 创建多级目录（推荐） |
| `delete()` | `boolean` | 删除文件或**空**目录 |
| `listFiles()` | `File[]` | 列出目录下的所有文件和子目录 |

### 文件路径的三种写法

| 路径类型 | 示例 | 说明 |
|---------|------|------|
| 绝对路径 | `D:/test/hello.txt` | 从盘符或根目录开始的完整路径 |
| 相对路径 | `test/hello.txt` | 相对于项目根目录（IDEA 中是 Project 根目录，不是 Module 根目录） |
| 类路径 | 通过 `ClassLoader` 获取 | 相对于编译后的 `classes` 目录 |

获取类路径的方式：

``` java title="获取类路径根目录"
--8<-- "code/java/io/io-file/src/test/java/com/luguosong/io/FileTest.java:classpath_resource"
```

## ⚡ 节点流——如何直接读写数据源？

节点流是 IO 体系中最底层的流，它们**直接连接数据源**（文件、内存数组、管道等）。没有节点流，包装流就无从「包装」。

### FileInputStream——按字节读取文件

`FileInputStream` 是最基础的文件读取流，每次从文件中读取一个或多个`字节`。

`一次读取一个字节`

``` java title="FileInputStream：逐字节读取"
--8<-- "code/java/io/io-node-stream/src/test/java/com/luguosong/io/NodeStreamTest.java:file_input_stream_single_byte"
```

!!! warning "read() 返回 int 而非 byte"
    `read()` 返回 `int` 类型（0~255），用 `-1` 表示文件读完。如果返回 `byte`，就无法区分「数据字节 -1」和「文件结束标志」了。

`一次读取一个字节数组`（推荐）

``` java title="FileInputStream：按数组读取（推荐）"
--8<-- "code/java/io/io-node-stream/src/test/java/com/luguosong/io/NodeStreamTest.java:file_input_stream_byte_array"
```

### FileOutputStream——按字节写入文件

`FileOutputStream` 用于将字节数据写入文件。构造方法的第二个参数 `append` 决定是覆盖还是追加：

``` java title="FileOutputStream：覆盖写入与追加写入"
--8<-- "code/java/io/io-node-stream/src/test/java/com/luguosong/io/NodeStreamTest.java:file_output_stream_write"
```

### 实战：用字节流拷贝文件

节点流最典型的应用——**文件拷贝**。这种方式对任何类型的文件都有效（图片、视频、压缩包等）：

``` java title="字节流实现文件拷贝"
--8<-- "code/java/io/io-node-stream/src/test/java/com/luguosong/io/NodeStreamTest.java:file_copy"
```

### ByteArrayInputStream / ByteArrayOutputStream——内存中的流

当数据源不是文件而是**内存中的字节数组**时，就用字节数组流。它不涉及磁盘操作，常用于单元测试、数据转换等场景。

``` java title="ByteArray 流基本使用"
--8<-- "code/java/io/io-node-stream/src/test/java/com/luguosong/io/NodeStreamTest.java:byte_array_stream"
```

💡 `ByteArrayOutputStream` 配合 `ObjectOutputStream` 可以实现**对象的深克隆**——先序列化到内存字节数组，再反序列化回来，得到一个完全独立的副本。

### PipedInputStream / PipedOutputStream——线程间通信

管道流用于**两个线程之间**的数据传递：一个线程通过 `PipedOutputStream` 写入，另一个线程通过 `PipedInputStream` 读取。

``` java title="管道流线程间通信"
--8<-- "code/java/io/io-node-stream/src/test/java/com/luguosong/io/NodeStreamTest.java:piped_stream"
```

⚠️ 管道流必须在**不同线程**中使用，否则会死锁。实际开发中更常用 `BlockingQueue` 等并发工具替代。

## 🎨 装饰器模式——包装流的设计思想

在学习包装流之前，先来理解它背后的设计模式。否则你可能会疑惑：为什么要把一个流「套」在另一个流外面，而不是直接继承一个功能更强的流？

### 如果只用继承会怎样？

假设我们需要给 `FileInputStream` 添加缓冲功能和加密功能：

``` text
FileInputStream
├── BufferedFileInputStream          （加缓冲）
├── EncryptedFileInputStream         （加加密）
├── BufferedEncryptedFileInputStream （缓冲 + 加密）
└── ...
```

每多一种功能组合，就要新建一个子类。如果再加上其他节点流（`ByteArrayInputStream`、`PipedInputStream`…），子类数量就会**爆炸式增长**——这就是所谓的「类爆炸」问题。

### 装饰器模式如何解决？

装饰器模式的核心思想：**不通过继承，而是通过「包裹」来扩展功能**。

``` mermaid
graph LR
    A["节点流\nFileInputStream"] --> B["装饰器 1\nBufferedInputStream"]
    B --> C["装饰器 2\nDataInputStream"]

    classDef base fill:transparent,stroke:#539bf5,color:#adbac7,stroke-width:2px
    classDef deco fill:transparent,stroke:#e3b341,color:#adbac7,stroke-width:1px
    class A base
    class B,C deco
```

就像套娃一样——最里面是节点流（数据来源），外面一层一层套上包装流（增强功能），想要什么功能就套什么，自由组合：

``` java
// 自由组合：文件流 → 缓冲 → 按行读取
BufferedReader br = new BufferedReader(   // 第二层：缓冲 + 按行读取
    new InputStreamReader(                 // 第一层：字节转字符（指定编码）
        new FileInputStream("data.txt"),   // 最底层：节点流
        StandardCharsets.UTF_8
    )
);
```

### IO 流中装饰器模式的体现

在 Java IO 源码中，所有包装流的构造方法都接收一个**同类型的父类流对象**——这正是装饰器的标志：

``` java
// BufferedInputStream 的构造方法——接收一个 InputStream
public BufferedInputStream(InputStream in) { ... }

// InputStreamReader 的构造方法——接收一个 InputStream
public InputStreamReader(InputStream in, Charset cs) { ... }

// DataInputStream 的构造方法——接收一个 InputStream
public DataInputStream(InputStream in) { ... }
```

这意味着**任何** `InputStream` 的子类（不管是 `FileInputStream`、`ByteArrayInputStream` 还是另一个包装流）都能被传入，实现灵活组合。

!!! tip "关闭包装流会自动关闭底层流"
    关闭最外层的包装流时，它会沿着链条依次关闭内部的流。所以你只需要关闭最外层即可，不必手动逐层关闭。

## 🔧 包装流——如何给节点流添加超能力？

理解了装饰器模式后，下面逐一介绍 Java IO 中最常用的几类包装流。

### 转换流——如何解决中文乱码？

当你用 `FileInputStream`（字节流）读取中文文本时，经常遇到乱码。这是因为字节流不关心编码，而中文在不同编码（UTF-8、GBK）下占的字节数不同。

`转换流`的作用就是在**字节流和字符流之间架桥**，同时指定编码：

``` mermaid
graph LR
    A["字节流\nInputStream"] -->|"指定编码\nUTF-8"| B["InputStreamReader\n字节→字符"]
    B --> C["程序得到 char"]

    D["程序写出 char"] --> E["OutputStreamWriter\n字符→字节"]
    E -->|"指定编码\nUTF-8"| F["字节流\nOutputStream"]

    classDef byte fill:transparent,stroke:#539bf5,color:#adbac7,stroke-width:2px
    classDef convert fill:transparent,stroke:#e3b341,color:#adbac7,stroke-width:1px
    classDef text fill:transparent,stroke:#57ab5a,color:#adbac7,stroke-width:1px
    class A,F byte
    class B,E convert
    class C,D text
```

#### InputStreamReader——读取时指定解码字符集

``` java title="InputStreamReader 指定编码读取"
--8<-- "code/java/io/io-wrapper-stream/src/test/java/com/luguosong/io/WrapperStreamTest.java:input_stream_reader"
```

#### OutputStreamWriter——写入时指定编码字符集

OutputStreamWriter 在上面的示例中已经展示——它在写入时指定编码字符集，将字符转为指定编码的字节写入底层 `OutputStream`。

#### FileReader / FileWriter——转换流的简化版

`FileReader` 和 `FileWriter` 本质上就是 `InputStreamReader` 和 `OutputStreamWriter` 的快捷写法，使用平台默认编码：

``` java title="FileReader / FileWriter 基本使用"
--8<-- "code/java/io/io-wrapper-stream/src/test/java/com/luguosong/io/WrapperStreamTest.java:file_reader_writer"
```

!!! warning "FileReader / FileWriter 的局限"
    它们使用平台默认编码，无法手动指定字符集。如果文件编码与平台不一致（比如在 Windows GBK 环境读 UTF-8 文件），仍然会乱码。此时必须用 `InputStreamReader` / `OutputStreamWriter` 显式指定编码。

    Java 18 起，`FileReader` 和 `FileWriter` 新增了接收 `Charset` 参数的构造方法，可以直接指定编码。

### 缓冲流——如何提升读写性能？

节点流每次 `read()` / `write()` 都会触发一次系统调用（磁盘 IO），频率太高性能很差。缓冲流在内部维护一个**缓冲区**（默认 8KB），攒够一批数据后再一次性读写，大幅减少磁盘交互次数。

| 缓冲流类 | 包装对象 | 特殊能力 |
|---------|---------|---------|
| `BufferedInputStream` | `InputStream` | 缓冲读取 |
| `BufferedOutputStream` | `OutputStream` | 缓冲写入 |
| `BufferedReader` | `Reader` | 缓冲读取 + `readLine()` 按行读取 |
| `BufferedWriter` | `Writer` | 缓冲写入 + `newLine()` 跨平台换行 |

#### BufferedInputStream / BufferedOutputStream

``` java title="缓冲流文件拷贝"
--8<-- "code/java/io/io-wrapper-stream/src/test/java/com/luguosong/io/WrapperStreamTest.java:buffered_stream_copy"
```

#### BufferedReader / BufferedWriter

`BufferedReader` 最常用的方法是 `readLine()`——按行读取文本，返回 `null` 表示读完：

``` java title="BufferedReader 按行读取"
--8<-- "code/java/io/io-wrapper-stream/src/test/java/com/luguosong/io/WrapperStreamTest.java:buffered_reader_readline"
```

`BufferedReader` 还支持 `mark()` 和 `reset()`——在流中做标记，之后可以回退到标记位置重新读取：

``` java title="mark / reset 回退读取"
--8<-- "code/java/io/io-wrapper-stream/src/test/java/com/luguosong/io/WrapperStreamTest.java:buffered_reader_mark_reset"
```

### 数据流——如何读写基本数据类型？

普通的字节流只能读写 `byte[]`，如果你想把 `int`、`double`、`boolean` 等基本数据类型直接写入文件，就需要 `DataInputStream` / `DataOutputStream`。

⚠️ **读取的顺序必须与写入的顺序完全一致**，否则数据会错乱。

``` java title="数据流读写基本类型"
--8<-- "code/java/io/io-wrapper-stream/src/test/java/com/luguosong/io/WrapperStreamTest.java:data_stream"
```

### 对象流——如何保存 Java 对象？

数据流只能处理基本类型，如果你需要把整个 Java 对象保存到文件（或通过网络传输），就需要`对象流`——`ObjectOutputStream`（序列化）和 `ObjectInputStream`（反序列化）。

#### 序列化与反序列化

- `序列化`（Serialization）：将 Java 对象 → 字节序列，写入文件或网络
- `反序列化`（Deserialization）：将字节序列 → Java 对象，从文件或网络还原

#### 对象必须实现 Serializable 接口

``` java title="可序列化的实体类"
--8<-- "code/java/io/io-wrapper-stream/src/test/java/com/luguosong/io/User.java"
```

#### ObjectOutputStream——将对象写入文件 / ObjectInputStream——从文件还原对象

``` java title="对象的序列化与反序列化"
--8<-- "code/java/io/io-wrapper-stream/src/test/java/com/luguosong/io/WrapperStreamTest.java:object_stream"
```

#### serialVersionUID——版本兼容的关键

当你序列化一个对象写入文件后，如果后续修改了这个类（比如添加了新字段），反序列化时可能抛出 `InvalidClassException`。

这是因为编译器会根据类的结构自动生成 `serialVersionUID`，类一改，版本号就变了。解决方法是**手动指定版本号**：

``` java
public class User implements Serializable {
    @Serial // Java 14+ 注解，帮助编译器检查序列化相关声明
    private static final long serialVersionUID = 1L;
    // ...
}
```

手动指定后，即使类结构发生变化，只要 `serialVersionUID` 不变，反序列化就不会报错（新增的字段取默认值，删除的字段被忽略）。

!!! tip "`transient` 关键字"
    被 `transient` 修饰的字段**不会参与序列化**。适用于敏感信息（密码）或不需要持久化的临时数据。

### 打印流——System.out.println() 的真面目

你每天都在用的 `System.out.println()` 里的 `out` 其实就是一个 `PrintStream` 对象。打印流是一种特殊的输出包装流，提供了方便的 `print()` / `println()` 方法。

| 特性 | `PrintStream`（字节） | `PrintWriter`（字符） |
|------|---------------------|---------------------|
| 支持输出各种数据类型 | ✅ | ✅ |
| 自动换行（`println`） | ✅ | ✅ |
| 自动编码 | ✅ | ✅ |
| 自动刷新 | ✅（默认） | ❌（需手动 `flush()` 或构造时开启） |
| 构造参数 | `OutputStream` | `OutputStream` 或 `Writer` |

``` java title="PrintStream 输出重定向"
--8<-- "code/java/io/io-wrapper-stream/src/test/java/com/luguosong/io/WrapperStreamTest.java:print_stream"
```

### 压缩流——如何压缩和解压文件？

Java 内置了对 GZIP 和 ZIP 格式的支持，通过包装流实现：

#### GZIP 压缩与解压

``` java title="GZIP 压缩与解压"
--8<-- "code/java/io/io-wrapper-stream/src/test/java/com/luguosong/io/WrapperStreamTest.java:gzip_stream"
```

#### ZIP 格式

ZIP 比 GZIP 更复杂，它支持多个文件条目（`ZipEntry`），可以把多个文件打包成一个 `.zip` 文件。使用 `ZipInputStream` / `ZipOutputStream` 配合 `ZipEntry` 操作。

## 🖥️ 标准流——System.in 和 System.out 是什么？

Java 预定义了三个标准流，它们在程序启动时就已初始化：

| 标准流 | 类型 | 默认目标 | 说明 |
|-------|------|---------|------|
| `System.in` | `InputStream` | 键盘（控制台输入） | 标准输入流 |
| `System.out` | `PrintStream` | 控制台 | 标准输出流 |
| `System.err` | `PrintStream` | 控制台 | 标准错误流 |

### 从控制台读取输入

``` java
// 方式一：直接使用 System.in（底层，不方便）
try (BufferedReader br = new BufferedReader(
        new InputStreamReader(System.in))) {
    System.out.print("请输入你的名字：");
    String name = br.readLine();
    System.out.println("你好，" + name);
}

// 方式二：使用 Scanner（更方便，底层也是包装 System.in）
Scanner scanner = new Scanner(System.in);
System.out.print("请输入一个整数：");
int num = scanner.nextInt();
```

### 重定向标准流

标准流的目标可以被修改——这就是「重定向」：

``` java
// 将标准输出重定向到文件
System.setOut(new PrintStream(new FileOutputStream("output.log")));
System.out.println("这句话不会出现在控制台，而是写入 output.log");

// 将标准输入重定向为文件
System.setIn(new FileInputStream("input.txt"));
// 此后 System.in.read() 从文件读取，而非键盘
```

## 🛡️ Try-With-Resources——如何优雅关闭资源？

IO 流使用完毕后必须关闭，否则会导致资源泄漏（文件句柄耗尽、内存泄漏等）。传统写法需要在 `finally` 中手动关闭，代码冗长且容易遗漏。

Java 7 引入的 `try-with-resources` 语法可以**自动关闭资源**——只要资源实现了 `AutoCloseable`（或其子接口 `Closeable`）：

``` java title="try-with-resources 自动关闭"
--8<-- "code/java/io/io-wrapper-stream/src/test/java/com/luguosong/io/WrapperStreamTest.java:try_with_resources"
```

💡 可以在 `try()` 中声明多个资源，用分号分隔，它们会按照**声明的逆序**依次关闭：

``` java
try (FileInputStream fis = new FileInputStream("src.txt");
     FileOutputStream fos = new FileOutputStream("dest.txt")) {
    // fos 先关闭，fis 后关闭
}
```

所有 IO 流都实现了 `Closeable` 接口，因此都可以使用 `try-with-resources`。

## 📋 Properties 文件——如何读取配置文件？

`.properties` 文件是 Java 中最常用的配置文件格式。`java.util.Properties` 类可以方便地读写这种 `key=value` 格式的文件。

### 方式一：Properties + FileReader

``` java title="Properties + FileReader 读取配置"
--8<-- "code/java/io/io-wrapper-stream/src/test/java/com/luguosong/io/WrapperStreamTest.java:properties_file_reader"
```

### 方式二：ResourceBundle（类路径读取）

`ResourceBundle` 专门用于读取类路径下的 `.properties` 文件，更适合读取打包在 JAR 中的配置：

``` java title="ResourceBundle 读取配置"
--8<-- "code/java/io/io-wrapper-stream/src/test/java/com/luguosong/io/WrapperStreamTest.java:properties_resource_bundle"
```

## 🧩 IO 流选型指南

面对这么多 IO 流，该怎么选？按照以下决策路径即可：

``` mermaid
graph TD
    Start["需要读写数据"] --> Q1{"数据类型？"}
    Q1 -->|"二进制\n（图片/视频/压缩包）"| Q2{"需要缓冲？"}
    Q1 -->|"文本"| Q3{"需要指定编码？"}

    Q2 -->|"是"| A1["BufferedInputStream\nBufferedOutputStream"]
    Q2 -->|"否"| A2["FileInputStream\nFileOutputStream"]

    Q3 -->|"是"| A3["InputStreamReader\nOutputStreamWriter"]
    Q3 -->|"否（默认编码即可）"| Q4{"需要按行读写？"}

    Q4 -->|"是"| A4["BufferedReader\nBufferedWriter"]
    Q4 -->|"否"| A5["FileReader\nFileWriter"]

    classDef question fill:transparent,stroke:#e3b341,color:#adbac7,stroke-width:1px
    classDef answer fill:transparent,stroke:#539bf5,color:#adbac7,stroke-width:2px
    classDef start fill:transparent,stroke:#57ab5a,color:#adbac7,stroke-width:2px
    class Start start
    class Q1,Q2,Q3,Q4 question
    class A1,A2,A3,A4,A5 answer
```

📝 **简单记忆**：

| 场景 | 推荐组合 |
|------|---------|
| 拷贝任意文件 | `BufferedInputStream` + `BufferedOutputStream` |
| 读写文本（已知编码） | `BufferedReader`(包装 `InputStreamReader`) |
| 读写文本（默认编码） | `BufferedReader`(包装 `FileReader`) |
| 保存/读取基本类型 | `DataInputStream` / `DataOutputStream` |
| 保存/读取 Java 对象 | `ObjectInputStream` / `ObjectOutputStream` |
| 日志输出 | `PrintStream` / `PrintWriter` |
| 文件压缩 | `GZIPOutputStream` / `ZipOutputStream` |

# 文件操作

Java 程序经常需要和文件系统打交道：读写文件内容、创建目录、遍历文件树、复制移动文件……Java 先后提供了两套文件操作 API：

- **`java.io.File`**（JDK 1.0）——老牌 API，功能有限，错误处理靠返回 `boolean`
- **`java.nio.file`**（Java 7+）——现代 API，功能强大，通过异常报告错误，与 Stream API 无缝集成

**🎯 本文你会学到**：

- 📁 `File` 类的基本用法——旧 API 快速上手
- ⚡ `Path` 类——更优雅地表示文件路径
- 🔧 `Files` 工具类——一行代码读写文件、遍历目录
- 🗂️ 目录遍历与文件查找——`walk()`、`walkFileTree()`、`PathMatcher`
- 🔄 `File` 与 `Path` 的对比与互转——新旧 API 如何共存
- 👁️ `WatchService`——监控目录中的文件变化

## 📁 java.io.File——旧版文件 API

`File` 类是 Java 最早的文件系统抽象——它可以指向一个文件，也可以指向一个目录，但它本身**不负责读写数据**（读写是 IO 流的职责，详见「IO 流」）。

### 如何创建 File 对象？

``` java title="创建 File 对象的三种方式"
--8<-- "code/java/javase/file/file-basic/src/test/java/com/luguosong/file/FileTest.java:create_file_object"
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

### 文件路径有哪些写法？

| 路径类型 | 示例 | 说明 |
|---------|------|------|
| 绝对路径 | `D:/test/hello.txt` | 从盘符或根目录开始的完整路径 |
| 相对路径 | `test/hello.txt` | 相对于项目根目录（IDEA 中是 Project 根目录，不是 Module 根目录） |
| 类路径 | 通过 `ClassLoader` 获取 | 相对于编译后的 `classes` 目录 |

获取类路径的方式：

``` java title="获取类路径根目录"
--8<-- "code/java/javase/file/file-basic/src/test/java/com/luguosong/file/FileTest.java:classpath_resource"
```

### File 有什么不足？

`File` 在 JDK 1.0 就存在了，随着时间推移，它的设计缺陷越来越明显：

| 问题 | 说明 |
|------|------|
| ❌ 错误只返回 `boolean` | `delete()` 失败时不知道原因——是权限不够？还是文件不存在？ |
| ❌ 无法原子操作 | 没有 `move()` 方法，重命名不保证原子性 |
| ❌ 性能差 | `listFiles()` 一次性返回所有结果，大目录下内存占用高 |
| ❌ 不支持符号链接 | 无法区分真实文件和符号链接 |
| ❌ 路径操作笨拙 | 拼接路径靠字符串拼接，容易出错 |

→ 这些问题催生了 Java 7 的 NIO.2 文件 API。

## ⚡ java.nio.file.Path——现代路径表示

Java 7 引入的 `Path` 接口是 `File` 的现代替代品。它**只表示路径**（文件或目录的地址），不关心文件是否存在——就像一个门牌号，不管房子在不在。

### 如何创建 Path 对象？

``` java title="创建 Path 对象的多种方式"
--8<-- "code/java/javase/file/nio-file/src/test/java/com/luguosong/file/NioFileTest.java:path_create"
```

!!! tip "`Path.of()` vs `Paths.get()`"
    两者完全等价。`Path.of()` 是 Java 11 新增的语法糖，更简洁。Java 7~10 只能用 `Paths.get()`。

### Path 的路径操作

`Path` 提供了丰富的路径操作方法，告别字符串拼接：

``` java title="Path 的路径拼接与解析"
--8<-- "code/java/javase/file/nio-file/src/test/java/com/luguosong/file/NioFileTest.java:path_operations"
```

**常用路径操作方法**：

| 方法 | 作用 | 示例 |
|------|------|------|
| `resolve(other)` | 拼接子路径 | `src.resolve("main")` → `src/main` |
| `resolveSibling(other)` | 替换为兄弟路径 | `src/main.resolveSibling("test")` → `src/test` |
| `relativize(other)` | 计算相对路径 | 从 A 到 B 要怎么走 |
| `normalize()` | 消除 `.` 和 `..` | `a/../b/./c` → `b/c` |
| `toAbsolutePath()` | 转为绝对路径 | 补全当前工作目录前缀 |
| `getFileName()` | 获取最后一段（文件名） | `a/b/c.txt` → `c.txt` |
| `getParent()` | 获取父路径 | `a/b/c.txt` → `a/b` |
| `getNameCount()` | 路径片段数 | `a/b/c` → `3` |

### 如何获取文件属性信息？

``` java title="获取 Path 的各种属性信息"
--8<-- "code/java/javase/file/nio-file/src/test/java/com/luguosong/file/NioFileTest.java:path_info"
```

## 🔧 Files 工具类——一行代码搞定文件操作

`java.nio.file.Files` 是 NIO.2 的核心工具类，几乎所有文件操作都通过它的**静态方法**完成。与 `File` 类每个操作都返回 `boolean` 不同，`Files` 在失败时抛出明确的异常（如 `NoSuchFileException`、`FileAlreadyExistsException`），你能清楚知道出了什么问题。

### 读写文件——告别繁琐的流操作

对于「小文件」（能一次性放进内存的），`Files` 提供了极简的一行式读写方法：

``` java title="Files 工具类的一行式读写"
--8<-- "code/java/javase/file/nio-file/src/test/java/com/luguosong/file/NioFileTest.java:files_read_write"
```

**读写方法速查**：

| 方法 | 版本 | 作用 |
|------|------|------|
| `Files.readString(path)` | Java 11+ | 读取整个文件为 `String` |
| `Files.writeString(path, str)` | Java 11+ | 将 `String` 写入文件 |
| `Files.readAllLines(path)` | Java 7+ | 按行读取为 `List<String>` |
| `Files.write(path, lines)` | Java 7+ | 将 `Iterable<String>` 按行写入 |
| `Files.readAllBytes(path)` | Java 7+ | 读取全部字节 |
| `Files.write(path, bytes)` | Java 7+ | 写入字节数组 |

### 大文件怎么办？——Files.lines() 流式读取

上面的方法会一次性把整个文件读进内存，大文件可能导致 `OutOfMemoryError`。`Files.lines()` 返回一个 `Stream<String>`，按需逐行读取，读多少加载多少：

``` java title="Files.lines() 流式懒加载按行读取"
--8<-- "code/java/javase/file/nio-file/src/test/java/com/luguosong/file/NioFileTest.java:files_lines_stream"
```

!!! warning "必须关闭 Stream"
    `Files.lines()` 返回的 `Stream` 持有底层文件句柄，**必须用 try-with-resources 包裹**，否则会泄漏文件描述符。

### 创建目录与临时文件

``` java title="目录和临时文件的创建"
--8<-- "code/java/javase/file/nio-file/src/test/java/com/luguosong/file/NioFileTest.java:files_dir_create"
```

### 复制、移动与删除

``` java title="文件的复制、移动与删除"
--8<-- "code/java/javase/file/nio-file/src/test/java/com/luguosong/file/NioFileTest.java:files_copy_move_delete"
```

## 🗂️ 目录遍历——如何查找文件？

### Files.walk()——递归遍历目录树

`Files.walk()` 返回一个 `Stream<Path>`，深度优先遍历整个目录树。配合 `filter()`、`map()` 等 Stream 操作，可以轻松实现各种查找需求：

``` java title="Files.walk() 递归遍历目录树"
--8<-- "code/java/javase/file/nio-file/src/test/java/com/luguosong/file/NioFileTest.java:files_walk"
```

### Files.walkFileTree()——访问者模式遍历

当你需要在遍历过程中做**复杂操作**（如递归删除整个目录树），`Files.walkFileTree()` 配合 `SimpleFileVisitor` 更合适：

``` java title="walkFileTree 递归删除目录"
--8<-- "code/java/javase/file/nio-file/src/test/java/com/luguosong/file/NioFileTest.java:files_walk_file_tree"
```

`SimpleFileVisitor` 提供了四个可重写的方法：

| 方法 | 调用时机 |
|------|---------|
| `preVisitDirectory()` | 进入目录**前** |
| `visitFile()` | 访问每个**文件**时 |
| `visitFileFailed()` | 文件**无法访问**时 |
| `postVisitDirectory()` | 离开目录**后**（子文件已全部访问） |

💡 递归删除的诀窍：在 `visitFile()` 中删文件，在 `postVisitDirectory()` 中删（已空的）目录。

### PathMatcher——用 glob 模式匹配文件

如果你只想找特定类型的文件，`PathMatcher` 比手动 `endsWith()` 更优雅：

``` java title="PathMatcher 用 glob 模式查找文件"
--8<-- "code/java/javase/file/nio-file/src/test/java/com/luguosong/file/NioFileTest.java:path_matcher"
```

**常用 glob 语法**：

| 模式 | 含义 | 示例 |
|------|------|------|
| `*` | 匹配任意字符（不跨目录） | `*.java` 匹配 `App.java` |
| `**` | 匹配任意层级目录 | `**/*.txt` 匹配所有 `.txt` 文件 |
| `?` | 匹配单个字符 | `?.txt` 匹配 `a.txt`，不匹配 `ab.txt` |
| `{a,b}` | 匹配 a 或 b | `*.{java,md}` 匹配 `.java` 或 `.md` |

## 🔄 File vs Path——对比与互转

### 功能对比

| 维度 | `java.io.File` | `java.nio.file.Path` + `Files` |
|------|----------------|-------------------------------|
| 引入版本 | JDK 1.0 | Java 7 |
| 错误处理 | 返回 `boolean`，失败原因未知 | 抛异常，错误原因明确 |
| 路径操作 | 字符串拼接 | `resolve()`、`relativize()`、`normalize()` |
| 读写文件 | 需配合 IO 流 | `Files.readString()`、`Files.write()` 一行搞定 |
| 目录遍历 | `listFiles()` 一次性返回，大目录吃内存 | `Files.walk()` 返回 Stream，惰性加载 |
| Stream API | ❌ 不支持 | ✅ 无缝集成 |
| 符号链接 | ❌ 不支持 | ✅ 支持 |
| 文件属性 | 有限 | 丰富（权限、时间戳、所有者等） |

📌 **结论**：新代码一律用 `Path` + `Files`。只在与旧 API 交互时才通过互转方法衔接。

### 相互转换

遗留代码中大量使用 `File`，好在它们可以轻松互转：

``` java title="File 与 Path 的相互转换"
--8<-- "code/java/javase/file/nio-file/src/test/java/com/luguosong/file/NioFileTest.java:file_vs_path"
```

| 转换方向 | 方法 |
|---------|------|
| `Path` → `File` | `path.toFile()` |
| `File` → `Path` | `file.toPath()` |

## 👁️ WatchService——监控目录变化

需要监控某个目录下的文件创建、修改、删除事件？Java 7 提供了 `WatchService` API，底层利用操作系统的文件事件通知机制（Linux inotify、macOS kqueue、Windows ReadDirectoryChangesW），**无需轮询**即可感知文件变化。

### 使用流程

```mermaid
graph LR
    A["创建 WatchService"] --> B["注册目录 + 事件类型"]
    B --> C["循环 take() 等待事件"]
    C --> D["遍历 pollEvents()"]
    D --> E["处理事件"]
    E --> F["reset() 重置 Key"]
    F --> C
```

### 事件类型

| 事件 | 含义 |
|------|------|
| `ENTRY_CREATE` | 目录中新建了文件或子目录 |
| `ENTRY_MODIFY` | 文件被修改 |
| `ENTRY_DELETE` | 文件或子目录被删除 |
| `OVERFLOW` | 事件丢失（系统来不及处理时触发） |

### 使用示例

``` java title="WatchService 监控目录变化"
--8<-- "code/java/javase/file/nio-file/src/test/java/com/luguosong/file/NioFileTest.java:watch_service"
```

⚠️ **注意事项**：

- `WatchService` 只能监控**直接子项**（非递归），监控子目录需要对每个子目录分别注册
- `take()` 是阻塞方法，会一直等到有事件发生；`poll()` 和 `poll(timeout)` 是非阻塞/限时版本
- 每次处理完事件后必须调用 `key.reset()`，否则不会再收到后续事件
- `reset()` 返回 `false` 表示该 Key 已失效（如监控的目录被删除了），此时应退出监听循环

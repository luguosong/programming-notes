---
title: 入门案例
description: 使用 Maven 创建 Java 工程和 Web 工程的完整步骤，构建命令速查
---

# 入门案例

上一节我们安装好了 `Maven`，环境已经就绪。但工具装好了不代表会用——你可能马上会遇到一个问题：**我的第一个 Java 项目怎么创建？**

如果你之前手动建过 Java 工程，一定经历过这些痛苦：手动建目录、手动下载 jar 包放到 classpath、手动写编译脚本……`Maven` 的 `archetype`（项目模板）机制就是为了解决这个问题的。

## 🏗️ 创建 Java 工程

### 使用 archetype 生成项目

📂 你可以把 `archetype` 理解成项目模板——就像用 Word 模板创建文档一样，选好模板、填上你的信息，一个标准项目就自动生成了。

`Maven` 通过插件来执行具体任务。这里用到的 `archetype` 是一个插件（plugin），`generate` 是这个插件提供的一个目标（goal）。合在一起就是：

``` bash title="交互式创建项目"
mvn archetype:generate
```

执行后会进入交互式流程：

1. **选择模板**：终端会列出所有可用的 `archetype`，默认推荐 `maven-archetype-quickstart`（适合普通 Java 项目），直接回车即可
2. **填写 GAV 坐标**：依次输入 `groupId`、`artifactId`、`version`、`package`
3. **确认生成**：确认信息无误后按回车，项目目录自动创建

!!! tip "跳过交互，一步到位"
    交互式选择模板要等网络加载列表，而且每次都要手动输入。如果你已经知道要用哪个模板，可以直接在命令中指定所有参数：

    ``` bash title="非交互式创建"
    mvn archetype:generate \
      -DgroupId=com.example \
      -DartifactId=my-app \
      -DarchetypeArtifactId=maven-archetype-quickstart \
      -DinteractiveMode=false
    ```

    这样一条命令就能生成完整的项目骨架，不需要任何交互。

### GAV 坐标说明

你可能会好奇，命令里那三个参数 `groupId`、`artifactId`、`version` 到底是什么？在 `Maven` 的世界里，它们合称为 **GAV 坐标**。

🎯 类比一下：GAV 坐标就像图书馆的索书号——每本书在图书馆中都有唯一的编号，凭编号就能精确找到它。`Maven` 用同样的方式标识每一个 jar 包。

| 坐标 | 含义 | 类比 | 示例 |
|------|------|------|------|
| `groupId` | 组织/公司标识 | 出版社名称 | `com.example`、`org.springframework` |
| `artifactId` | 项目/模块名称 | 书名 | `my-app`、`spring-core` |
| `version` | 版本号 | 版次 | `1.0-SNAPSHOT`、`6.1.5` |

几点补充说明：

- `groupId` 通常使用域名反写（如 `com.example`），这样天然保证了全局唯一性
- `artifactId` 就是你的项目名，也是最终生成的 jar/war 包的文件名前缀
- `version` 中的 `SNAPSHOT` 后缀表示开发版本（快照版），`Maven` 会区别对待快照版和正式版（Release）

### 项目目录结构

模板生成完毕后，你会得到一个标准的 `Maven` 项目结构。`Maven` 强烈建议所有项目都遵循这个约定——这就是「约定优于配置」的核心思想。

```
my-app/
├── pom.xml                          ← 项目配置文件（核心）
├── src/
│   ├── main/
│   │   ├── java/                    ← 源代码
│   │   │   └── com/example/App.java
│   │   └── resources/               ← 资源文件（配置文件、静态资源等）
│   └── test/
│       ├── java/                    ← 测试代码
│       │   └── com/example/AppTest.java
│       └── resources/               ← 测试资源
└── target/                          ← 构建产物（自动生成，勿手动修改）
    ├── classes/
    ├── test-classes/
    └── my-app-1.0-SNAPSHOT.jar
```

几个关键点：

- `pom.xml` 是整个项目的灵魂，所有配置都写在这里（详见「POM 详解」）
- `src/main/java` 放源代码，`src/test/java` 放测试代码——源码和测试严格分离
- `target/` 目录由 `Maven` 自动管理，每次构建时会清空重建，永远不要手动修改里面的内容

### 编写代码并构建

模板已经帮我们生成了一个最小的可运行示例。来看看生成的代码：

``` java title="src/main/java/com/example/App.java"
package com.example;

public class App {

    public static void main(String[] args) {
        System.out.println("Hello World!");
    }
}
```

``` java title="src/test/java/com/example/AppTest.java"
package com.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AppTest {

    @Test
    void testAppHasAGreeting() {
        App classUnderTest = new App();
        assertNotNull(classUnderTest.getGreeting(),
            "app should have a greeting");
    }
}
```

现在进入项目目录，执行完整的构建流程：

``` bash title="编译 → 测试 → 打包"
cd my-app

# 1️⃣ 编译源代码
mvn compile

# 2️⃣ 运行测试
mvn test

# 3️⃣ 打包为 jar
mvn package
```

构建成功后，`target/` 目录下会出现 `my-app-1.0-SNAPSHOT.jar`——这就是你的项目产物。可以用以下命令验证：

``` bash title="运行 jar 包"
java -jar target/my-app-1.0-SNAPSHOT.jar
```

!!! info "关于 jar 包运行"
    直接用 `java -jar` 运行需要一个前提：`pom.xml` 中配置了主类（Main Class）。如果没有配置，会报 "no main manifest attribute" 错误。后续在「POM 详解」中会讲到如何配置。

## 📋 构建命令速查

🔧 上面的构建流程中我们用了 `compile`、`test`、`package` 三个命令。实际上 `Maven` 提供了一整套标准化的构建命令，这里做一个完整汇总：

| 命令 | 作用 | 触发的生命周期阶段 |
|------|------|-----------------|
| `mvn clean` | 删除 `target/` 目录 | `clean` |
| `mvn compile` | 编译主程序源代码 | `compile` |
| `mvn test-compile` | 编译测试程序源代码 | `test-compile` |
| `mvn test` | 运行测试 | `test` |
| `mvn package` | 打包为 jar/war | `package` |
| `mvn install` | 安装到本地仓库 | `install` |
| `mvn deploy` | 部署到远程仓库 | `deploy` |
| `mvn clean package` | 清理后重新打包 | `clean` + `package` |

!!! warning "阶段是累积执行的"
    执行某个阶段时，`Maven` 会自动执行该阶段之前的所有阶段。比如你执行 `mvn package`，实际执行链是：

    `compile` → `test-compile` → `test` → `package`

    所以平时不需要手动一条一条执行，直接 `mvn package` 就能完成从编译到打包的全部工作。

还有一个细节值得注意：`mvn package` 打出的 jar 包**只包含 `src/main/` 下的代码和资源**，不会把测试代码打进去。测试代码只在构建过程中运行，不会出现在最终产物中。

## 🌐 创建 Web 工程

### 使用 webapp archetype

前面我们创建的是普通的 Java 工程（打 jar 包）。如果你要开发一个 Web 应用（打 war 包），项目结构就不一样了——你需要 `WEB-INF/web.xml`、`webapp/` 目录等 Web 特有的东西。

`Maven` 为此提供了专门的 `maven-archetype-webapp` 模板：

``` bash title="创建 Web 工程"
mvn archetype:generate \
  -DgroupId=com.example \
  -DartifactId=my-webapp \
  -DarchetypeGroupId=org.apache.maven.archetypes \
  -DarchetypeArtifactId=maven-archetype-webapp \
  -DarchetypeVersion=1.4 \
  -DinteractiveMode=false
```

生成的目录结构如下：

```
my-webapp/
├── pom.xml
└── src/
    └── main/
        ├── resources/
        └── webapp/
            ├── WEB-INF/
            │   └── web.xml        ← Web 应用部署描述符
            └── index.jsp          ← 默认首页
```

对比 Java 工程你会发现：没有 `src/main/java/` 目录，也没有 `src/test/` 目录。这个模板只生成了最基础的 Web 结构，你需要手动补全 `java/` 和测试目录。

!!! tip "手动补全缺失目录"
    模板不生成的目录不代表不能用。你可以手动创建以下目录：

    - `src/main/java/` — 放 Servlet 等 Java 源码
    - `src/test/java/` — 放测试代码

    `Maven` 识别目录靠的是约定，不是模板——只要路径正确，`Maven` 就能找到。

### 添加 Servlet 依赖

Web 应用离不开 Servlet API。你需要在 `pom.xml` 中添加依赖，但注意：Servlet API 由 Tomcat 等容器在运行时提供，所以 `scope`（依赖范围）必须设为 `provided`——意思是"开发时用，打包时不带"。

``` xml title="pom.xml - 添加 Servlet 依赖"
<dependencies>
    <!-- Servlet API -->
    <dependency>
        <groupId>jakarta.servlet</groupId>
        <artifactId>jakarta.servlet-api</artifactId>
        <version>6.0.0</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

!!! info "javax 还是 jakarta？"
    Servlet API 经历了一次重大的包名迁移：

    - **Jakarta EE 9+**（新项目推荐）：`jakarta.servlet:jakarta.servlet-api`，包名为 `jakarta.servlet.*`
    - **Java EE 8 及更早**（老项目维护）：`javax.servlet:javax.servlet-api`，包名为 `javax.servlet.*`

    如果你用的是 Tomcat 10+，选 Jakarta 版本；Tomcat 9 及以下用 javax 版本。新项目一律推荐 Jakarta。

### 打包与部署

依赖配好之后，执行打包命令：

``` bash title="打包 Web 工程"
mvn package
```

构建成功后，`target/` 目录下会出现 `my-webapp.war` 文件。

部署方式非常直接——把 war 包放到 Tomcat 的 `webapps/` 目录下，Tomcat 启动时会自动解压并部署：

``` bash title="部署到 Tomcat"
cp target/my-webapp.war /path/to/tomcat/webapps/
```

启动 Tomcat 后，访问 `http://localhost:8080/my-webapp/` 即可看到页面。

!!! tip "war 包名称与访问路径"
    war 包的文件名（去掉 `.war` 后缀）就是应用的上下文路径（Context Path）。比如 `my-webapp.war` 对应 `/my-webapp`。如果你想直接用根路径 `/` 访问，把 war 包重命名为 `ROOT.war` 即可。

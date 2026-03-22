# JDBC Demo 模块拆分实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 `code/java/database/jdbc-demo` 单模块拆分为 6 个按功能分组的独立 Maven 子模块，使代码结构与文档章节清晰对应。

**Architecture:** 在 `code/java/database/` 目录下新建聚合 POM，其下创建 6 个子模块：`jdbc-connection`、`jdbc-statement`、`jdbc-preparedstatement`、`jdbc-transaction-batch`、`jdbc-pool`、`jdbc-metadata`。每个子模块只包含对应主题的测试类，依赖与原 `jdbc-demo` 完全相同。文档 `docs/java/database/jdbc/index.md` 中所有 `--8<--` 代码引用路径同步更新。原 `jdbc-demo` 目录最终删除。

**Tech Stack:** Java 17, Maven (multi-module), Spring Boot 3.5.0 (spring-boot-starter-parent), H2, HikariCP, Druid, JUnit 5

---

## 文件变更清单

| 操作 | 路径 |
|------|------|
| 新建 | `code/java/database/pom.xml`（database 聚合 POM） |
| 修改 | `code/java/pom.xml`（子模块路径 `database/jdbc-demo` → `database`） |
| 新建 | `code/java/database/jdbc-connection/pom.xml` |
| 新建 | `code/java/database/jdbc-connection/src/test/java/com/luguosong/jdbc/ConnectionTest.java` |
| 新建 | `code/java/database/jdbc-statement/pom.xml` |
| 新建 | `code/java/database/jdbc-statement/src/test/java/com/luguosong/jdbc/StatementTest.java` |
| 新建 | `code/java/database/jdbc-statement/src/test/java/com/luguosong/jdbc/ResultSetTest.java` |
| 新建 | `code/java/database/jdbc-preparedstatement/pom.xml` |
| 新建 | `code/java/database/jdbc-preparedstatement/src/test/java/com/luguosong/jdbc/PreparedStatementTest.java` |
| 新建 | `code/java/database/jdbc-transaction-batch/pom.xml` |
| 新建 | `code/java/database/jdbc-transaction-batch/src/test/java/com/luguosong/jdbc/TransactionTest.java` |
| 新建 | `code/java/database/jdbc-transaction-batch/src/test/java/com/luguosong/jdbc/BatchProcessingTest.java` |
| 新建 | `code/java/database/jdbc-pool/pom.xml` |
| 新建 | `code/java/database/jdbc-pool/src/test/java/com/luguosong/jdbc/ConnectionPoolTest.java` |
| 新建 | `code/java/database/jdbc-metadata/pom.xml` |
| 新建 | `code/java/database/jdbc-metadata/src/test/java/com/luguosong/jdbc/MetaDataTest.java` |
| 修改 | `docs/java/database/jdbc/index.md`（更新所有 `--8<--` 引用路径） |
| 删除 | `code/java/database/jdbc-demo/`（整个目录） |

---

## Task 1：创建 `database` 聚合 POM

**Files:**
- 新建：`code/java/database/pom.xml`
- 修改：`code/java/pom.xml`

> ⚠️ `code/java/database/` 目录已存在（内含 `jdbc-demo`），**无需 `mkdir` 父目录**，直接在其中新建 `pom.xml` 即可。

- [ ] **Step 1：创建 `code/java/database/pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.luguosong</groupId>
    <artifactId>java-database-examples</artifactId>
    <version>1.0.0</version>
    <packaging>pom</packaging>
    <name>Java 数据库知识点演示合集</name>

    <modules>
        <module>jdbc-connection</module>
        <module>jdbc-statement</module>
        <module>jdbc-preparedstatement</module>
        <module>jdbc-transaction-batch</module>
        <module>jdbc-pool</module>
        <module>jdbc-metadata</module>
    </modules>
</project>
```

- [ ] **Step 2：修改 `code/java/pom.xml`，将旧模块路径改为新聚合 POM**

将：
```xml
<module>database/jdbc-demo</module>
```
改为：
```xml
<module>database</module>
```

> ⚠️ **不在此处提交**。聚合 POM 注册了 6 个子模块目录，若此时提交，那次 commit 将处于无法构建的中间状态（子模块目录尚未创建）。聚合 POM 的变更统一在 Task 7 完成后随最后一次 `mvn test` 验证通过后提交。

---

## Task 2：创建 `jdbc-connection` 子模块

**Files:**
- 新建：`code/java/database/jdbc-connection/pom.xml`
- 新建：`code/java/database/jdbc-connection/src/test/java/com/luguosong/jdbc/ConnectionTest.java`（从 `jdbc-demo` 复制）

- [ ] **Step 1：创建目录结构**

```bash
mkdir -p "E:/code-note/programming-notes/code/java/database/jdbc-connection/src/test/java/com/luguosong/jdbc"
```

- [ ] **Step 2：创建 `jdbc-connection/pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.0</version>
        <relativePath/>
    </parent>
    <groupId>com.luguosong</groupId>
    <artifactId>jdbc-connection</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>jdbc-connection</name>
    <description>JDBC 连接管理演示</description>
    <properties>
        <java.version>17</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

> 注意：`jdbc-connection` 只用 H2 和 spring-boot-starter-test，不需要 HikariCP / Druid。

- [ ] **Step 3：复制 `ConnectionTest.java`**

将 `code/java/database/jdbc-demo/src/test/java/com/luguosong/jdbc/ConnectionTest.java` 完整复制到 `code/java/database/jdbc-connection/src/test/java/com/luguosong/jdbc/ConnectionTest.java`（package 声明保持 `com.luguosong.jdbc` 不变）。

- [ ] **Step 4：运行测试验证**

```bash
cd E:/code-note/programming-notes/code/java/database/jdbc-connection
mvn test --no-transfer-progress
```
期望输出：`Tests run: 7, Failures: 0, Errors: 0`（ConnectionTest 共 7 个测试方法）

- [ ] **Step 5：提交**

```bash
cd E:/code-note/programming-notes
git add code/java/database/jdbc-connection/
git commit -m "feat: add jdbc-connection sub-module with ConnectionTest"
```

---

## Task 3：创建 `jdbc-statement` 子模块

**Files:**
- 新建：`code/java/database/jdbc-statement/pom.xml`
- 新建：`code/java/database/jdbc-statement/src/test/java/com/luguosong/jdbc/StatementTest.java`
- 新建：`code/java/database/jdbc-statement/src/test/java/com/luguosong/jdbc/ResultSetTest.java`

- [ ] **Step 1：创建目录结构**

```bash
mkdir -p "E:/code-note/programming-notes/code/java/database/jdbc-statement/src/test/java/com/luguosong/jdbc"
```

- [ ] **Step 2：创建 `jdbc-statement/pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.0</version>
        <relativePath/>
    </parent>
    <groupId>com.luguosong</groupId>
    <artifactId>jdbc-statement</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>jdbc-statement</name>
    <description>JDBC Statement 与 ResultSet 演示</description>
    <properties>
        <java.version>17</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 3：复制 `StatementTest.java` 和 `ResultSetTest.java`**

从 `jdbc-demo/src/test/java/com/luguosong/jdbc/` 复制 `StatementTest.java` 和 `ResultSetTest.java` 到 `jdbc-statement/src/test/java/com/luguosong/jdbc/`。

- [ ] **Step 4：运行测试验证**

```bash
cd E:/code-note/programming-notes/code/java/database/jdbc-statement
mvn test --no-transfer-progress
```
期望输出：两个测试类全部通过，`Failures: 0, Errors: 0`。

- [ ] **Step 5：提交**

```bash
cd E:/code-note/programming-notes
git add code/java/database/jdbc-statement/
git commit -m "feat: add jdbc-statement sub-module with StatementTest and ResultSetTest"
```

---

## Task 4：创建 `jdbc-preparedstatement` 子模块

**Files:**
- 新建：`code/java/database/jdbc-preparedstatement/pom.xml`
- 新建：`code/java/database/jdbc-preparedstatement/src/test/java/com/luguosong/jdbc/PreparedStatementTest.java`

- [ ] **Step 1：创建目录结构**

```bash
mkdir -p "E:/code-note/programming-notes/code/java/database/jdbc-preparedstatement/src/test/java/com/luguosong/jdbc"
```

- [ ] **Step 2：创建 `jdbc-preparedstatement/pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.0</version>
        <relativePath/>
    </parent>
    <groupId>com.luguosong</groupId>
    <artifactId>jdbc-preparedstatement</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>jdbc-preparedstatement</name>
    <description>JDBC PreparedStatement 演示（含防 SQL 注入、BLOB）</description>
    <properties>
        <java.version>17</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 3：复制 `PreparedStatementTest.java`**

从 `jdbc-demo/src/test/java/com/luguosong/jdbc/PreparedStatementTest.java` 复制到 `jdbc-preparedstatement/src/test/java/com/luguosong/jdbc/PreparedStatementTest.java`。

- [ ] **Step 4：运行测试验证**

```bash
cd E:/code-note/programming-notes/code/java/database/jdbc-preparedstatement
mvn test --no-transfer-progress
```
期望输出：`Failures: 0, Errors: 0`。

- [ ] **Step 5：提交**

```bash
cd E:/code-note/programming-notes
git add code/java/database/jdbc-preparedstatement/
git commit -m "feat: add jdbc-preparedstatement sub-module with PreparedStatementTest"
```

---

## Task 5：创建 `jdbc-transaction-batch` 子模块

**Files:**
- 新建：`code/java/database/jdbc-transaction-batch/pom.xml`
- 新建：`code/java/database/jdbc-transaction-batch/src/test/java/com/luguosong/jdbc/TransactionTest.java`
- 新建：`code/java/database/jdbc-transaction-batch/src/test/java/com/luguosong/jdbc/BatchProcessingTest.java`

- [ ] **Step 1：创建目录结构**

```bash
mkdir -p "E:/code-note/programming-notes/code/java/database/jdbc-transaction-batch/src/test/java/com/luguosong/jdbc"
```

- [ ] **Step 2：创建 `jdbc-transaction-batch/pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.0</version>
        <relativePath/>
    </parent>
    <groupId>com.luguosong</groupId>
    <artifactId>jdbc-transaction-batch</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>jdbc-transaction-batch</name>
    <description>JDBC 事务管理与批处理演示</description>
    <properties>
        <java.version>17</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 3：复制测试文件**

从 `jdbc-demo/src/test/java/com/luguosong/jdbc/` 复制 `TransactionTest.java` 和 `BatchProcessingTest.java` 到 `jdbc-transaction-batch/src/test/java/com/luguosong/jdbc/`。

- [ ] **Step 4：运行测试验证**

```bash
cd E:/code-note/programming-notes/code/java/database/jdbc-transaction-batch
mvn test --no-transfer-progress
```
期望输出：`Failures: 0, Errors: 0`。

- [ ] **Step 5：提交**

```bash
cd E:/code-note/programming-notes
git add code/java/database/jdbc-transaction-batch/
git commit -m "feat: add jdbc-transaction-batch sub-module with TransactionTest and BatchProcessingTest"
```

---

## Task 6：创建 `jdbc-pool` 子模块

**Files:**
- 新建：`code/java/database/jdbc-pool/pom.xml`
- 新建：`code/java/database/jdbc-pool/src/test/java/com/luguosong/jdbc/ConnectionPoolTest.java`

- [ ] **Step 1：创建目录结构**

```bash
mkdir -p "E:/code-note/programming-notes/code/java/database/jdbc-pool/src/test/java/com/luguosong/jdbc"
```

- [ ] **Step 2：创建 `jdbc-pool/pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.0</version>
        <relativePath/>
    </parent>
    <groupId>com.luguosong</groupId>
    <artifactId>jdbc-pool</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>jdbc-pool</name>
    <description>JDBC 连接池演示（HikariCP + Druid）</description>
    <properties>
        <java.version>17</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.zaxxer</groupId>
            <artifactId>HikariCP</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid</artifactId>
            <version>1.2.23</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 3：复制 `ConnectionPoolTest.java`**

从 `jdbc-demo/src/test/java/com/luguosong/jdbc/ConnectionPoolTest.java` 复制到 `jdbc-pool/src/test/java/com/luguosong/jdbc/ConnectionPoolTest.java`。

- [ ] **Step 4：运行测试验证**

```bash
cd E:/code-note/programming-notes/code/java/database/jdbc-pool
mvn test --no-transfer-progress
```
期望输出：`Failures: 0, Errors: 0`。

- [ ] **Step 5：提交**

```bash
cd E:/code-note/programming-notes
git add code/java/database/jdbc-pool/
git commit -m "feat: add jdbc-pool sub-module with ConnectionPoolTest"
```

---

## Task 7：创建 `jdbc-metadata` 子模块

**Files:**
- 新建：`code/java/database/jdbc-metadata/pom.xml`
- 新建：`code/java/database/jdbc-metadata/src/test/java/com/luguosong/jdbc/MetaDataTest.java`

- [ ] **Step 1：创建目录结构**

```bash
mkdir -p "E:/code-note/programming-notes/code/java/database/jdbc-metadata/src/test/java/com/luguosong/jdbc"
```

- [ ] **Step 2：创建 `jdbc-metadata/pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.0</version>
        <relativePath/>
    </parent>
    <groupId>com.luguosong</groupId>
    <artifactId>jdbc-metadata</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>jdbc-metadata</name>
    <description>JDBC 数据库元数据演示</description>
    <properties>
        <java.version>17</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 3：复制 `MetaDataTest.java`**

从 `jdbc-demo/src/test/java/com/luguosong/jdbc/MetaDataTest.java` 复制到 `jdbc-metadata/src/test/java/com/luguosong/jdbc/MetaDataTest.java`。

- [ ] **Step 4：运行测试验证**

```bash
cd E:/code-note/programming-notes/code/java/database/jdbc-metadata
mvn test --no-transfer-progress
```
期望输出：`Failures: 0, Errors: 0`。

- [ ] **Step 5：全量验证 6 个子模块**

所有子模块已就绪，从 database 聚合根执行全量构建：

```bash
cd E:/code-note/programming-notes/code/java/database
mvn test --no-transfer-progress
```
期望输出：6 个模块全部 `BUILD SUCCESS`，`Failures: 0, Errors: 0`。

- [ ] **Step 6：统一提交聚合 POM 变更 + jdbc-metadata 子模块**

```bash
cd E:/code-note/programming-notes
git add code/java/pom.xml code/java/database/pom.xml code/java/database/jdbc-metadata/
git commit -m "build: add database aggregator pom and jdbc-metadata sub-module; all 6 modules build green"
```

---

## Task 8：更新文档 `--8<--` 引用路径

**Files:**
- 修改：`docs/java/database/jdbc/index.md`

文档中所有代码片段引用形如：
```
--8<-- "code/java/database/jdbc-demo/src/test/java/com/luguosong/jdbc/XxxTest.java:tag_name"
```
需按下表批量替换为新路径：

| 原路径前缀 | 新路径前缀 |
|-----------|-----------|
| `code/java/database/jdbc-demo/…/ConnectionTest.java` | `code/java/database/jdbc-connection/…/ConnectionTest.java` |
| `code/java/database/jdbc-demo/…/StatementTest.java` | `code/java/database/jdbc-statement/…/StatementTest.java` |
| `code/java/database/jdbc-demo/…/ResultSetTest.java` | `code/java/database/jdbc-statement/…/ResultSetTest.java` |
| `code/java/database/jdbc-demo/…/PreparedStatementTest.java` | `code/java/database/jdbc-preparedstatement/…/PreparedStatementTest.java` |
| `code/java/database/jdbc-demo/…/TransactionTest.java` | `code/java/database/jdbc-transaction-batch/…/TransactionTest.java` |
| `code/java/database/jdbc-demo/…/BatchProcessingTest.java` | `code/java/database/jdbc-transaction-batch/…/BatchProcessingTest.java` |
| `code/java/database/jdbc-demo/…/ConnectionPoolTest.java` | `code/java/database/jdbc-pool/…/ConnectionPoolTest.java` |
| `code/java/database/jdbc-demo/…/MetaDataTest.java` | `code/java/database/jdbc-metadata/…/MetaDataTest.java` |

- [ ] **Step 1：执行批量替换**

在 PowerShell 中运行（一次性替换所有引用）：

> ⚠️ 使用 `[System.IO.File]::WriteAllText` 而非 `Set-Content`，避免 PowerShell 5.x 写入 UTF-8 BOM 破坏文档。

```powershell
$file = "E:\code-note\programming-notes\docs\java\database\jdbc\index.md"
$content = Get-Content $file -Raw -Encoding UTF8

# 替换各文件路径（保留 --8<-- 前缀和 :tag_name 后缀）
$content = $content -replace 'jdbc-demo/src/test/java/com/luguosong/jdbc/ConnectionTest\.java', 'jdbc-connection/src/test/java/com/luguosong/jdbc/ConnectionTest.java'
$content = $content -replace 'jdbc-demo/src/test/java/com/luguosong/jdbc/StatementTest\.java', 'jdbc-statement/src/test/java/com/luguosong/jdbc/StatementTest.java'
$content = $content -replace 'jdbc-demo/src/test/java/com/luguosong/jdbc/ResultSetTest\.java', 'jdbc-statement/src/test/java/com/luguosong/jdbc/ResultSetTest.java'
$content = $content -replace 'jdbc-demo/src/test/java/com/luguosong/jdbc/PreparedStatementTest\.java', 'jdbc-preparedstatement/src/test/java/com/luguosong/jdbc/PreparedStatementTest.java'
$content = $content -replace 'jdbc-demo/src/test/java/com/luguosong/jdbc/TransactionTest\.java', 'jdbc-transaction-batch/src/test/java/com/luguosong/jdbc/TransactionTest.java'
$content = $content -replace 'jdbc-demo/src/test/java/com/luguosong/jdbc/BatchProcessingTest\.java', 'jdbc-transaction-batch/src/test/java/com/luguosong/jdbc/BatchProcessingTest.java'
$content = $content -replace 'jdbc-demo/src/test/java/com/luguosong/jdbc/ConnectionPoolTest\.java', 'jdbc-pool/src/test/java/com/luguosong/jdbc/ConnectionPoolTest.java'
$content = $content -replace 'jdbc-demo/src/test/java/com/luguosong/jdbc/MetaDataTest\.java', 'jdbc-metadata/src/test/java/com/luguosong/jdbc/MetaDataTest.java'

# 写回无 BOM 的 UTF-8（兼容 PowerShell 5.x 和 7.x）
[System.IO.File]::WriteAllText($file, $content, [System.Text.UTF8Encoding]::new($false))
```

- [ ] **Step 2：验证替换结果**

确认文档中不再有 `jdbc-demo` 的引用：

```powershell
Select-String -Path "E:\code-note\programming-notes\docs\java\database\jdbc\index.md" -Pattern "jdbc-demo"
```
期望输出：无任何匹配行。

- [ ] **Step 3：提交**

```bash
cd E:/code-note/programming-notes
git add docs/java/database/jdbc/index.md
git commit -m "docs: update jdbc index.md code snippet paths to new sub-module layout"
```

---

## Task 9：删除旧 `jdbc-demo` 目录并最终验证

**Files:**
- 删除：`code/java/database/jdbc-demo/`

- [ ] **Step 1：删除 `jdbc-demo` 目录**

```bash
rm -rf "E:/code-note/programming-notes/code/java/database/jdbc-demo"
```

- [ ] **Step 2：验证聚合构建全部通过**

从 `code/java/database/` 执行全量构建，确认 6 个子模块测试均绿：

```bash
cd E:/code-note/programming-notes/code/java/database
mvn test --no-transfer-progress
```
期望输出：6 个模块全部 `BUILD SUCCESS`，总计 `Failures: 0, Errors: 0`。

- [ ] **Step 3：验证文档中无悬挂引用**

```powershell
Select-String -Path "E:\code-note\programming-notes\docs\java\database\jdbc\index.md" -Pattern "jdbc-demo"
```
期望：无输出。

- [ ] **Step 4：暂存删除并提交**

```bash
cd E:/code-note/programming-notes
git rm -r code/java/database/jdbc-demo
git commit -m "chore: remove legacy jdbc-demo module after split into 6 sub-modules"
```

---

## 完成标志

- [ ] `code/java/database/` 下存在 6 个子模块目录，每个均有 `pom.xml` 和对应测试类
- [ ] `code/java/database/pom.xml` 聚合 POM 注册了全部 6 个子模块
- [ ] `code/java/pom.xml` 指向 `database`（聚合 POM），不再指向 `database/jdbc-demo`
- [ ] 文档 `docs/java/database/jdbc/index.md` 不含 `jdbc-demo` 路径
- [ ] `code/java/database/jdbc-demo/` 目录已删除
- [ ] `mvn test` 在 `code/java/database/` 下全绿

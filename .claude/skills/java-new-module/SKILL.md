---
name: java-new-module
description: 创建新的 Java 示例模块，包含 Maven 目录结构、pom.xml、聚合 POM 更新和基础测试类。触发词：新建模块、new module、创建模块、add module。
---

# 新建 Java 示例模块

在 `code/java/` 下创建新的 Maven 示例模块，确保目录结构、pom.xml、聚合 POM 更新一步到位。

## 执行流程

### 第一步：确认模块信息

使用 `AskUserQuestion` 确认以下信息：

1. **模块名称**：自描述功能边界的名称（如 `jdbc-connection`、`nio-file`），遵循一个模块只演示一个功能维度的原则
2. **所属聚合 POM**：
   - `code/java/database/pom.xml`（数据库相关）
   - `code/java/javase/` 下的子聚合（如 `file/pom.xml`、`io/pom.xml`）
   - 或其他已有的聚合 POM
3. **额外依赖**：除了基础依赖（H2 + spring-boot-starter-test）外是否需要额外依赖

### 第二步：创建模块目录

创建标准 Maven 目录结构：

```
code/java/{聚合路径}/{模块名}/
├── pom.xml
└── src/
    └── test/
        └── java/
            └── com/
                └── luguosong/
                    └── {模块名}/
```

### 第三步：创建 pom.xml

先读取父聚合 POM 获取 `groupId`、`version` 和 `artifactId`，然后创建：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>{父 groupId}</groupId>
        <artifactId>{父 artifactId}</artifactId>
        <version>{父 version}</version>
    </parent>

    <artifactId>{模块名}</artifactId>
    <description>{模块功能描述}</description>

    <dependencies>
        <!-- 按需添加额外依赖，基础依赖已在父 POM 中管理 -->
    </dependencies>

</project>
```

### 第四步：更新聚合 POM

在父聚合 POM 的 `<modules>` 中添加新模块目录名：

```xml
<modules>
    ...已有模块...
    <module>{新模块名}</module>
</modules>
```

### 第五步：验证

1. 在模块目录执行 `mvn validate` 验证 POM 配置正确
2. 确认聚合 POM 能识别新模块

## 模块拆分原则

- **一个模块只演示一个功能维度**
- ✅ `jdbc-connection`（只放连接相关）
- ❌ `jdbc-demo`（把连接、查询、事务全堆在一起）
- 模块名称自描述功能边界（如 `jdbc-transaction-batch` 而非 `jdbc-advanced`）
- 测试类间无共享依赖时应拆分

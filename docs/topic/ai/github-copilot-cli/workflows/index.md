# 开发工作流

Copilot CLI 不仅是一个问答工具，更是覆盖完整开发周期的助手。本页介绍如何将 Copilot 融入代码审查、重构、调试、测试和 Git 工作流中。

---

## 代码审查

### 基础审查

``` text
# 审查当前工作区的所有变更
/review

# 审查特定文件
> @src/auth.py 审查这个文件的代码质量和安全性

# 聚焦审查特定方面
> @src/api/routes.py 只关注安全性问题：SQL 注入、XSS、认证绕过
```

### /diff 命令

查看当前工作区与最近提交之间的差异，并请求审查：

``` text
# 查看变更
/diff

# 审查变更
> /diff 然后审查所有变更，重点关注错误处理
```

### 自动化审查（Pre-commit Hook）

在 Git 提交前自动运行 Copilot 审查：

``` bash title=".git/hooks/pre-commit"
#!/bin/bash

# 获取暂存的文件
STAGED=$(git diff --cached --name-only --diff-filter=ACM | grep -E '\.py$')

if [ -n "$STAGED" ]; then
  echo "Running Copilot review on staged files..."
  for file in $STAGED; do
    REVIEW=$(timeout 60 copilot --allow-all -p "Quick security review of @$file - critical issues only" 2>/dev/null)
    if [ $? -eq 124 ]; then
      echo "Warning: Review timed out for $file (skipping)"
      continue
    fi
    if echo "$REVIEW" | grep -qi "CRITICAL"; then
      echo "Critical issues found in $file:"
      echo "$REVIEW"
      exit 1
    fi
  done
  echo "Review passed"
fi
```

!!! note "内置 Hook 替代方案"

    Copilot CLI 提供了内置的 Hook 系统（`copilot hooks`），比手动 Git Hook 更简单。详见「插件与 Hook」页面。

---

## 代码重构

### 渐进式重构

``` text
# 第一步：理解现有代码
> @src/legacy/user_manager.py 分析这个类的职责，识别违反单一职责原则的地方

# 第二步：制定重构计划
> /plan 将 UserManager 拆分为 UserAuthService 和 UserProfileService

# 第三步：执行重构
# 确认计划后，Copilot 执行拆分

# 第四步：验证
> 运行测试确认重构没有破坏现有功能
```

### 设计模式应用

``` text
# 提取策略模式
> @src/payment.py 这里的 if/elif 链太长了，帮我重构为策略模式

# 引入依赖注入
> @src/services/ 将硬编码的依赖改为构造函数注入
```

---

## 调试

### 错误分析

``` text
# 直接粘贴错误信息
> 运行测试时出现以下错误：
> TypeError: 'NoneType' object is not subscriptable
> 在 src/data_processor.py 第 42 行

# 引用相关文件辅助分析
> @src/data_processor.py 上面这个 TypeError 的根本原因是什么？
```

### 结合日志分析

``` bash
# 管道传入日志文件
cat error.log | copilot -p "分析这些错误日志，找出最频繁的错误模式和根本原因"

# 引用日志文件
> @logs/app.log 最近的错误有什么规律？
```

### 逐步调试

``` text
# 让 Copilot 帮助设置断点
> @src/api/handler.py 请求处理函数在并发场景下偶尔返回 500。
> 帮我分析可能的竞态条件，并建议调试策略。
```

---

## 测试生成

### 为现有代码生成测试

``` text
# 基础测试生成
> @src/calculator.py 为这个计算器模块生成单元测试

# 指定测试框架和风格
> @src/user_service.py 使用 pytest 生成测试，包含：
> - 正常场景
> - 边界条件
> - 异常场景
> - 使用 mock 隔离外部依赖
```

### 结合已有测试上下文

``` text
# 引用已有测试作为风格参考
> @src/auth.py @tests/test_auth.py
> 参考已有测试的风格，为 auth.py 中新增的 reset_password 函数生成测试
```

### TDD 工作流

``` text
# 先写测试
> /plan 我要实现一个 BookCollection 类，支持按年份范围搜索。先帮我写失败测试。

# 实现代码让测试通过
> @tests/test_books.py 现在实现代码让这些测试通过

# 重构
> @src/books.py @tests/test_books.py 在保持测试通过的前提下重构代码
```

---

## Git 集成

### 生成提交信息

``` text
# 基于暂存更改生成 commit message
> 为暂存的更改生成一个符合 Conventional Commit 规范的提交信息

# 使用 Programmatic 模式
copilot -p "查看 git diff --staged，生成 Conventional Commit 格式的提交信息"
```

### PR 操作

``` text
# 查看 PR
/pr

# 生成 PR 描述
> 根据当前分支的所有 commit，生成 PR 描述，包含：
> - 变更摘要
> - 改动详情
> - 测试说明
```

### PR 描述生成器（脚本化）

``` bash
# 自动生成 PR 描述
BRANCH=$(git branch --show-current)
COMMITS=$(git log main..$BRANCH --oneline)

copilot -p "Generate a PR description for:
Branch: $BRANCH
Commits:
$COMMITS

Include: Summary, Changes Made, Testing Done"
```

---

## 综合工作流示例

### 从 Idea 到合并 PR

完整的端到端工作流，展示如何组合使用各项能力：

``` text
# 1️⃣ 收集上下文
> @src/ 项目的整体架构是什么？

# 2️⃣ 规划
> /plan 实现"按年份搜索图书"功能

# 3️⃣ 实施（确认计划后）
# Copilot 根据计划逐步实现

# 4️⃣ 生成测试
> @src/books.py @tests/test_books.py
> 为新功能生成测试，包括边界条件

# 5️⃣ 审查
> /review

# 6️⃣ 更新文档
> @README.md 添加新功能的使用说明

# 7️⃣ 提交
> 生成 commit message 并提交

# 8️⃣ 创建 PR
> /pr
```

### 新项目上手

加入新项目时的快速上手工作流：

``` text
# 1️⃣ 了解全局
> @src/ 这个项目的整体架构和技术栈是什么？

# 2️⃣ 理解特定流程
> @src/api/routes.py @src/services/user.py
> 用户注册的完整流程是怎样的？

# 3️⃣ 使用 Agent 深度分析
> /agent
# 选择合适的 Agent（如 code-review）
> @src/auth/ 认证模块有哪些设计问题需要改进？

# 4️⃣ 查找任务（通过 MCP 访问 GitHub）
> 列出标记为 "good first issue" 的 Issue

# 5️⃣ 开始贡献
> 选择最简单的 Issue，制定修复计划
```

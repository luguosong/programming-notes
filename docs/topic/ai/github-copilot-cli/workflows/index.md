---
title: 开发工作流
description: 用 Copilot CLI 完成规划、审查、重构、调试、测试和 Git 操作的完整工作流
---

# 开发工作流

**本文你会学到**：
- 🔄 如何将 Copilot 融入完整的开发周期
- 📐 智能规划：从 Issue 到实现蓝图
- 🔍 代码审查、重构、调试、测试的 AI 辅助工作流
- 🔀 Git 集成：提交信息生成、PR 操作
- 🚀 端到端的综合工作流示例

Copilot CLI 不仅是一个问答工具，更是覆盖完整开发周期的助手。前面的章节介绍了 Copilot 的各项能力，本页则展示如何在实际开发工作中`组合使用`这些能力。

!!! tip "工作流思维"

    使用 Copilot CLI 最高效的方式不是把它当搜索引擎用，而是把它当作开发流程中的`每个环节`的加速器——规划、编码、测试、审查、提交，都可以借助 AI 提效。

---

## 📐 智能规划：从 Issue 到实现蓝图

Plan 模式（详见「交互模式」）不仅是任务清单，更是防止 AI 产生"幻觉（Hallucination）"的防火墙。在大型项目中，确保 AI 理解现有架构一致性是实施的前提。

### 交互式计划生成流程

1. **Issue 深度分析**：利用 `/research` 命令对 GitHub Issue 进行深度分析，评估设计选择并给出实现步骤
2. **迭代完善计划**：使用 `/plan` 进入 Plan 模式，通过对话细化方案。例如要求 AI"添加对特定环境变量的支持"或"考虑向后兼容性"
3. **强制验证逻辑**：在计划中明确指出如何验证每个阶段的产出（如特定的单元测试断言或 API 调用验证）

``` text
# 第一步：研究 Issue 背景
> /research GitHub Issue #42 涉及的技术栈和可能的实现方案

# 第二步：生成计划
> /plan 实现 Issue #42：为 API 添加分页支持，需兼容现有客户端

# 第三步：细化验证步骤
> 在计划中为每个阶段添加验证方法，确保可以通过测试确认完成
```

### 标准 AI 计划书组成

一份高质量的 AI 计划书应包含以下要素——你可以把它理解为 AI 版的"技术方案评审文档"：

- **工作阶段（Work Phases）**：逻辑任务拆解（如：Schema 变更 → 核心逻辑 → API 暴露 → 测试补充）
- **文件影响清单（File Impact）**：预测将被创建或修改的文件路径
- **验证方法（Validation）**：明确的端到端或单元测试路径

!!! tip "计划即防火墙"

    经验表明，最有效的计划必须包含**验证条目**。没有验证步骤的计划等同于没有测试的代码——看似完成，实则无法保证正确性。

---

## 🔍 代码审查

Copilot 可以作为你的"第一道审查关卡"——在你提交 PR 之前先让 AI 看一遍，发现明显的问题。这不会取代人工审查，但能大幅减少审查轮次。

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

查看当前工作区与最近提交之间的差异，并请求审查。`/diff` 支持 17 种编程语言的语法高亮（1.0.5 新增），在备用屏幕模式下支持 ++home++ / ++end++ 和 ++page-up++ / ++page-down++ 导航（1.0.15 新增）：

``` text
# 查看变更
/diff

# 审查变更
> /diff 然后审查所有变更，重点关注错误处理
```

### CodeRabbit 协同审查

[CodeRabbit](https://coderabbit.ai/) 是 AI 驱动的 PR 审查工具，可以与 Copilot CLI 形成"执行-审查"闭环：

- **自动 PR 总结**：利用 LLM 快速提炼变更重点
- **三级风险评估**：自动标注 `Critical`（严重）、`Major`（主要）、`Minor`（次要）问题
- **Committable Suggestions**：直接在 GitHub PR 界面提供可一键提交的修复建议

跨工具协同修复工作流：

``` bash
# 1. 获取 CodeRabbit 的审查建议
code-rabbit --prompt-only > /tmp/review-fixes.txt

# 2. 将修复指令传递给 Copilot CLI 执行
copilot -p "$(cat /tmp/review-fixes.txt) 请根据以上审查建议修复代码"
```

### 多模型协同审查

通过 Copilot CLI 同时调度多个 AI 模型并行评审同一份代码，消除单一模型的逻辑盲区：

``` bash
# 使用 -p 获取非交互式输出，便于脚本比对
copilot --agent code-review -p "@src/auth.py 审查安全性" > /tmp/review-default.txt

# 切换模型进行对比审查
copilot --agent code-review --model opus -p "@src/auth.py 审查安全性" > /tmp/review-opus.txt
copilot --agent code-review --model haiku -p "@src/auth.py 审查安全性" > /tmp/review-haiku.txt

# 人工比对不同模型的审查结果，综合判断
diff /tmp/review-default.txt /tmp/review-opus.txt
```

!!! info "多模型审查的价值"

    不同模型各有所长：某些模型在安全审查上更严苛，另一些在逻辑推理上更精准。通过交叉审查可以发现单一模型遗漏的问题。

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

    Copilot CLI 提供了内置的 Hook 系统，比手动 Git Hook 更简单。详见「Hook 扩展」和「插件系统」页面。

---

## 🔧 代码重构

重构是最适合 AI 辅助的场景之一——AI 擅长机械性的模式识别和批量修改，而这正是重构的核心工作。关键是`先规划再执行`，不要让 AI 直接动手改代码。

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

## 🐛 调试

AI 擅长快速定位常见错误模式——把错误信息或日志贴给它，通常能比搜索引擎更快地找到原因。但对于复杂的系统级问题（如并发竞态），AI 的建议可能不够深入，此时还需要结合实际调试工具。

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

## 🧪 测试生成

写测试是 AI 最擅长的任务之一——给定业务代码，AI 能快速覆盖正常路径、边界条件和异常场景。关键是：`给 AI 足够的上下文`（业务代码 + 已有测试的风格参考），它才能生成风格一致的测试。

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

## 🔀 Git 集成

### 生成提交信息

``` text
# 基于暂存更改生成 commit message
> 为暂存的更改生成一个符合 Conventional Commit 规范的提交信息

# 使用 Programmatic 模式
copilot -p "查看 git diff --staged，生成 Conventional Commit 格式的提交信息"
```

### PR 操作

`/pr` 命令提供完整的 PR 工作流（1.0.5 新增）：创建和查看 PR、修复 CI 失败、处理 Review 反馈、解决合并冲突。

``` text
# 查看 PR
/pr

# 查看本地或 Web 上的 PR
/pr view local    # 本地查看
/pr view web      # 浏览器打开

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

### 导出会话（/share）

使用 `/share html` 将当前会话和研究报告导出为自包含的交互式 HTML 文件（1.0.15 新增），方便在团队内分享 AI 辅助的调查过程和结论。

---

## 🚀 综合工作流示例

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

---

## 💡 进阶技巧

### 预防"懒惰代理"

AI 代理有时会倾向于给出简化的 TODO 注释或占位符代码，而非完整的实现——就像一个偷懒的实习生，把工作推到"下次再做"。应对策略：

1. **明确完成标准**：在 prompt 中指明"不要使用 TODO 注释，请完整实现所有逻辑"
2. **循环验证**：在 Autopilot 模式中搭配测试命令，持续驱动代理直至任务完全通过验证
3. **计划锚定**：使用 Plan 模式先制定详细计划，Autopilot 按计划执行时更不容易偷懒

``` text
# 在 prompt 中明确要求
> 实现完整的用户注册功能。要求：
> - 不使用任何 TODO 或占位符
> - 每个函数都有完整的错误处理
> - 包含输入验证逻辑
> - 生成对应的单元测试并确保通过
```

!!! tip "验证优先"

    最有效的防懒策略是要求 AI 在计划中包含自动化测试，然后通过 Autopilot 模式运行测试来验证实现的完整性。测试失败会自动触发修复循环。

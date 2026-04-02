# 上下文与会话管理

Copilot CLI 的效果直接取决于你提供的上下文质量。本页介绍如何使用 `@` 语法精确引用文件，以及如何管理会话和上下文窗口。

---

## @ 语法：引用文件与目录

在 prompt 中使用 `@` 前缀引用文件或目录，让 Copilot 读取其内容作为上下文。

### 基本引用

``` text
# 引用单个文件
> @src/auth.py 解释这段认证逻辑

# 引用目录（递归包含所有文件）
> @src/models/ 这些模型之间的关系是什么？

# 引用多个文件
> @src/auth.py @src/middleware.py 这两个文件如何协作？
```

### Glob 模式匹配

支持 glob 通配符批量引用文件：

``` text
# 所有 Python 文件
> @**/*.py 项目中有哪些数据类？

# 特定目录下的所有测试
> @tests/**/*.test.ts 测试覆盖率如何？

# 所有配置文件
> @**/*.{json,yaml,toml} 列出所有配置
```

### 引用 URL

可以引用 URL 让 Copilot 获取网页内容：

``` text
> @https://api.example.com/openapi.json 根据这个 API 规范生成客户端
```

### 图片上下文（实验性）

支持引用本地图片文件作为视觉上下文：

``` text
# 引用图片
> @design/mockup.png 根据这个设计稿实现页面布局

# 引用截图辅助调试
> @screenshot.png 页面渲染结果与预期不符，帮我分析原因
```

!!! warning "图片支持限制"

    图片上下文为实验性功能，支持 PNG、JPEG、GIF、WebP 格式。效果取决于模型的视觉能力。

!!! tip "邻近标签页技巧（VS Code）"

    在 VS Code 中使用 Copilot 时，打开相关的上下文文件作为标签页，Copilot 会自动扫描这些打开的标签页以增强生成的准确性。在 CLI 中，等价操作是通过 `@` 语法显式引用相关文件。

---

## 跨文件智能分析

Copilot 不仅读取引用的文件，还会理解文件之间的关系。这使得跨文件分析特别强大：

``` text
# 追踪数据流
> @src/api/routes.py @src/services/user.py @src/models/user.py
> 追踪用户注册请求从 API 入口到数据库存储的完整流程

# 依赖分析
> @package.json @src/ 哪些依赖在代码中实际被使用了？

# 一致性检查
> @src/api/ @tests/api/ API 路由和测试文件的覆盖情况如何？有哪些路由缺少测试？
```

---

## 会话管理

Copilot CLI 的每次对话都是一个`会话`（Session）。会话数据自动持久化到本地磁盘，支持恢复和管理。

### 会话存储结构

``` text
~/.copilot/session-state/{session-id}/
├── events.jsonl      # 完整会话历史
├── workspace.yaml    # 会话元数据
├── plan.md           # 实现计划（Plan 模式生成）
├── checkpoints/      # 上下文压缩历史
└── files/            # 会话产物（不提交到 git）
```

### 恢复会话

``` bash
# 恢复最近的会话
copilot --resume

# 恢复指定会话
copilot --resume <session-id>

# 从特定检查点恢复
copilot --continue <checkpoint-id>
```

!!! tip "会话命名"

    使用 `/rename` 给会话起一个有意义的名称，方便后续查找：

    ``` text
    /rename oauth-feature
    ```

### 查看会话信息

``` text
# 查看当前会话详情
/session
```

输出内容包括：会话 ID、创建时间、已使用的 token 数、会话文件路径等。

---

## 上下文窗口管理

AI 模型有固定的上下文窗口大小（token 限制）。当对话变长时，需要主动管理上下文以保持效果。

### /compact：压缩上下文

当对话过长导致上下文接近饱和时，使用 `/compact` 压缩历史：

``` text
# 压缩当前上下文
/compact

# 带提示地压缩（告诉 Copilot 保留什么信息）
/compact 保留关于认证模块的所有讨论，其他可以压缩
```

`/compact` 会将之前的对话历史总结为一个精简摘要，释放 token 空间。这类似于"存档并继续"。

### /clear：清除上下文

``` text
# 完全清除对话历史，从头开始
/clear
```

!!! warning "清除 vs 压缩"

    - `/compact`：保留对话摘要，适合同一任务需要更多空间时使用
    - `/clear`：完全清除，适合切换到全新任务时使用

### 最佳实践：保持会话聚焦

``` text
# ✅ 好习惯：每个功能一个会话
copilot
> /rename auth-feature
# 专注于认证功能的开发
> /exit

copilot
> /rename csv-export
# 专注于 CSV 导出功能
> /exit

# ❌ 坏习惯：一个会话做所有事情
# 上下文越来越混乱，Copilot 的回答质量会下降
```

---

## 上下文相关命令速查

| 命令 | 功能 |
|------|------|
| `@file` | 引用文件作为上下文 |
| `@dir/` | 引用整个目录 |
| `@**/*.ext` | 使用 glob 模式批量引用 |
| `/session` | 查看当前会话信息 |
| `/rename <name>` | 重命名当前会话 |
| `/compact` | 压缩上下文历史 |
| `/clear` | 清除对话历史 |
| `/context` | 查看当前上下文中的文件 |
| `--resume` | 启动时恢复上次会话 |
| `--continue` | 从特定检查点恢复 |

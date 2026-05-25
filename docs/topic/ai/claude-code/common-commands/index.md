---
title: 常用命令
---

# 常用命令

Claude Code 提供了丰富的斜杠命令（`/command`）和键盘快捷键，覆盖会话管理、项目配置、代码审查等日常工作流。本文按使用场景分类整理最常用的命令，方便快速查阅。

本文你会学到：

- 按场景查找需要的斜杠命令
- 常用键盘快捷键速查
- 命令行启动参数的典型用法

## 会话管理

日常对话中最常用的一组命令，控制对话的开始、恢复、压缩和导出。

### /compact 与 /clear：清空与压缩

当上下文变长导致响应变慢，或者需要换个话题时：

```shell
# 压缩当前对话，释放上下文空间
# 可选传入总结指令，让 Claude 聚焦关键信息
/compact
/compact 保留所有 bug 修复相关的讨论

# 完全清空对话，开始新会话
# 之前的对话可通过 /resume 恢复
/clear
```

`/compact` 和 `/clear` 的区别：`/compact` 保留对话摘要继续聊，`/clear` 彻底清空重新开始。当你发现 Claude 开始"遗忘"早期内容，先用 `/compact`；想换一个完全独立的话题，用 `/clear`。

### /resume 与 /branch：恢复与分支

```shell
# 恢复之前的对话（打开选择器）
/resume

# 按名称恢复特定会话
/resume my-session-name

# 在当前对话点创建分支，原始对话不受影响
# 可通过 /resume 返回主分支
/branch
/branch try-refactor
```

### /export 与 /copy：导出与复制

```shell
# 导出当前对话为纯文本
/export
/export session-notes.txt

# 复制最后一条回复到剪贴板
/copy
# 复制倒数第 2 条回复
/copy 2
```

### /rename：重命名会话

```shell
# 重命名当前会话（不带名称则自动生成）
/rename
/rename auth-bug-fix
```

## 项目与环境

### /init：项目初始化

进入一个新项目时，先用 `/init` 生成 `CLAUDE.md` 项目说明文件：

```shell
# 交互式生成 CLAUDE.md（推荐）
# 设置环境变量启用交互式流程
CLAUDE_CODE_NEW_INIT=1
/init
```

`CLAUDE.md` 相当于给 Claude 的项目"入职手册"——告诉它技术栈、目录结构、编码规范等，后续每次对话自动加载。

### /config：主题与模型设置

```shell
# 打开设置界面：主题、模型、输出样式等
/config
```

### /doctor：环境诊断

```shell
# 诊断安装和环境问题，按 f 自动修复
/doctor
```

### /add-dir：添加工作目录

```shell
# 添加额外的工作目录（访问其他项目文件）
/add-dir ../another-project
```

### /context：上下文占用查看

```shell
# 查看当前上下文占用（彩色网格可视化）
/context
```

## 权限与外部工具

### /permissions：权限管理

当你频繁被权限提示打断时：

```shell
# 打开权限管理界面
# 可添加允许/拒绝规则，按作用域查看
/permissions
```

### /fewer-permission-prompts：减少权限弹窗

```shell
# 自动扫描会话记录，将常用只读操作加入允许列表
# 减少 "允许 Claude 执行此操作？" 的弹窗
/fewer-permission-prompts
```

### /mcp：MCP 服务器

```shell
# 管理 MCP（Model Context Protocol）服务器连接
# 添加、移除、查看已配置的外部工具服务器
/mcp
```

## 模型与推理

### /model：切换模型

```shell
# 打开模型选择器
/model

# 直接指定模型
/model claude-sonnet-4-6
```

### /effort：调整推理力度

当你觉得 Claude 回复"太浅"或"太慢"时，可以调整推理努力级别：

```shell
# 打开交互式滑块
/effort

# 直接设置级别：low / medium / high / xhigh / max
/effort high
# 恢复模型默认
/effort auto
```

`low` 适合简单问答（快），`high`/`xhigh` 适合复杂架构决策（深思熟虑），日常编码用 `medium` 即可。

### /fast：快速模式

```shell
# 切换快速模式（使用更快的模型变体，仅 Opus 4.6 可用）
/fast
/fast on
/fast off
```

## 代码与审查

### /diff：查看变更

```shell
# 打开交互式 diff 查看器
# 左右箭头切换 git diff / 单轮对话 diff
# 上下箭头浏览文件
/diff
```

### /review 与 /security-review：代码审查

```shell
# 在当前会话中审查 PR（本地）
# 自动检测当前分支关联的 PR
/review
# 指定 PR
/review 42

# 安全审查：分析待提交变更的安全漏洞
/security-review
```

### /autofix-pr：PR 自动修复

```shell
# 监视当前分支的 PR，CI 失败或收到评论时自动推送修复
# 需要先 gh pr view 关联 PR
/autofix-pr
# 附加自定义指令
/autofix-pr 只修复 lint 和类型错误
```

### /plan：计划模式

进入计划模式，先分析再动手。Claude 只读取和分析代码，不会修改文件。

```shell
/plan
/plan 修复认证模块的 bug
```

## 跨会话知识

### /memory：编辑持久化知识

```shell
# 编辑 CLAUDE.md 内存文件
# 可启用/禁用自动记忆、查看已有的记忆条目
/memory
```

`/memory` 管理的是跨会话持久化的知识——比如你告诉 Claude "这个项目用中文注释"，它以后每次对话都会记住。

## 系统与账户

### /status 与 /usage：状态与用量

```shell
# 查看版本、模型、账户状态
/status

# 查看会话费用和使用量统计
/usage
```

### /login 与 /logout：账户登录

```shell
# 登录 Anthropic 账户
/login

# 登出 Anthropic 账户
/logout
```

### /release-notes：更新日志

```shell
# 查看 Claude Code 更新日志
/release-notes
```

## 界面与辅助

### /theme：切换主题

```shell
# 切换颜色主题（支持 auto 跟随终端）
/theme
```

### /voice：语音听写

```shell
# 切换语音输入模式
/voice
/voice hold
/voice tap
/voice off
```

### /focus：精简视图

精简显示模式，只显示提示、工具调用摘要和最终响应。

```shell
/focus
```

### /desktop：桌面应用

```shell
# 在桌面应用中继续当前会话（仅 macOS 和 Windows）
/desktop
```

### /feedback：提交反馈

```shell
# 提交反馈或报告 Bug
/feedback
/feedback 压缩对话后上下文丢失
```

### /skills：技能管理

```shell
# 列出已安装的技能
/skills
```

### /tasks：后台任务

```shell
# 查看后台任务列表
/tasks
```

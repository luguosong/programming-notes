---
title: 交互模式
description: 掌握 Interactive、Plan、Autopilot、Programmatic 四种交互模式的适用场景
---

# 交互模式

**本文你会学到**：

- 🎛️ Copilot CLI 的四种交互模式及其适用场景
- ⚡ 每种模式的特点和进入方式
- 🎯 如何选择最适合当前任务的模式
- 📤 任务委派（`/delegate`）的使用方法

打个比方：`Interactive` 模式像是你坐在副驾驶，每一步都要你点头才能继续；`Plan` 模式像是先让导航规划路线，你看完路线再出发；`Autopilot` 模式像是告诉司机目的地，然后你靠在座位上等到达。

Copilot CLI 提供三种交互模式，通过 ++shift+tab++ 循环切换。状态栏左侧会显示当前所处的模式。此外还有一个非交互式的 Programmatic 模式用于脚本集成。

!!! tip "什么时候用什么模式？"

    - `日常编码、调试、问答` → Interactive
    - `涉及多文件的复杂变更` → 先 Plan 再 Interactive
    - `重复性高、目标明确的大任务` → Plan → Autopilot
    - `写脚本、CI/CD 自动化` → Programmatic

!!! info "自定义状态栏（1.0.30 新增）"

    `/statusline` 命令（别名 `/footer`）可自定义状态栏显示的项目，包括工作目录、Git 分支、reasoning effort、上下文窗口使用率、配额等。如果你觉得默认状态栏信息不够或太拥挤，可以用它来定制。

    1.0.36 新增了 `changes` 状态栏项，可显示当前会话中新增/删除的行数，方便直观地感知 AI 产生的代码变更量。

---

## 💬 Interactive 模式（默认）

这是最常用的模式，也是你启动 Copilot CLI 后默认进入的模式。每次你提交 prompt 后，Copilot 会执行操作，然后在需要你审批时暂停等待确认——就像带着一个做事积极的助手，他每做一步都会先问你"这样可以吗？"。

``` text
> 在 src/utils.py 中添加一个日期格式化函数

Copilot will edit src/utils.py
Allow? [Y/n]
```

适合日常编码、调试、问答等需要逐步控制的场景。

---

## 📋 Plan 模式

### 为什么需要先规划？

直接让 AI 写代码，就像让装修队不看图纸就开工——结果可能是墙敲了发现该留门。Plan 模式让你先和 AI 协作制定实现计划，确保方向正确后再动手。

适用场景：多文件变更、大规模重构、新功能实现——简单来说，`只要你觉得"这个任务有点复杂"就应该先用 Plan 模式`。

### 进入方式

=== "快捷键进入"

    按 ++shift+tab++ 切换到 Plan 模式（状态栏显示 `plan`）。

=== "斜杠命令"

    ``` text
    /plan 添加 OAuth2 身份验证，支持 Google 和 GitHub 提供商
    ```

=== "命令行参数"

    ``` bash
    # 直接以 Plan 模式启动（1.0.23 新增）
    copilot --plan

    copilot -p "[[PLAN]] 重构数据库层为 Repository 模式"
    ```

    在 prompt 前加上 `[[PLAN]]` 前缀可直接进入 Plan 模式。

### 规划流程

1. `分析请求`：分析你的 prompt 和代码库结构
2. `澄清问题`：提出澄清问题，确认需求和方法
3. `创建计划`：生成带复选框的结构化实现计划（保存为 `plan.md`）
4. `等待审批`：在实施之前等待你的确认

!!! tip "编辑计划"

    按 ++ctrl+y++ 可在默认编辑器中查看和编辑计划的 Markdown 文件。你可以删除不需要的步骤、调整优先级或添加细节，Copilot 会按修改后的计划执行。

    在 plan 模式下输入多行 prompt 时，多行内容现在会被完整保留（1.0.32 修复），不再被截断。

### 审批后的选项

计划生成后，你可以选择：

- `确认执行`：直接在当前模式下实施
- `切换到 Autopilot`：选择 "Accept plan and build on autopilot" 让 Copilot 自主完成
- `修改计划`：提出修改意见让 Copilot 调整计划
- `取消`：放弃当前计划

---

## 🚀 Autopilot 模式（实验性）

!!! warning "实验性功能"

    需要先启用实验模式：启动时添加 `--experimental` 参数，或在会话中执行 `/experimental`。启用后会持久化，后续无需重复设置。

Autopilot 模式让 Copilot 自主完成任务，无需在每个步骤后提供输入。打个比方：Interactive 模式是你和 AI 一问一答，Autopilot 模式是你交代完任务后，AI 自己干活直到完成或遇到无法解决的问题。

- Agent 判断任务已完成
- 出现无法继续的问题
- 你按 ++ctrl+c++ 手动停止
- 达到最大续行次数限制（如已设置）

### 进入方式

=== "交互式切换"

    按 ++shift+tab++ 循环到 autopilot（状态栏显示 `autopilot`），然后输入 prompt。

=== "命令行启动"

    ``` bash
    copilot --autopilot --yolo --max-autopilot-continues 10 -p "YOUR PROMPT HERE"
    ```

    - `--autopilot`：启用 Autopilot 模式
    - `--yolo`：跳过所有权限确认
    - `--max-autopilot-continues N`：限制最大自动续行次数

### 权限选择

进入 Autopilot 模式时，如果尚未授予全部权限，会提示你选择：

1. `Enable all permissions`（推荐）
2. Continue with limited permissions
3. Cancel

!!! tip "推荐工作流：Plan → Autopilot"

    最高效的使用方式是先 Plan 后 Autopilot：

    1. 用 Plan 模式制定详细计划，确保方向正确
    2. 确认计划后，选择 "Accept plan and build on autopilot"
    3. Copilot 按照已审批的计划自主完成所有实施步骤

### 子代理并行处理

Autopilot 模式在处理复杂的多文件任务时，会将任务拆解并调度多个子代理（Sub-agents）并行处理。例如，一个涉及多模块重构的任务可能被拆解为：

- `explore` 代理并行扫描不同模块的依赖关系
- `task` 代理并行运行不同模块的测试套件
- `general-purpose` 代理处理复杂的跨文件逻辑变更

这种并行化处理使得传统需要逐文件手动修改的重构任务可以在几分钟内完成。

!!! warning "YOLO 模式安全准则"

    `--yolo` 标志允许 AI 跳过所有人工确认直接执行工具调用。使用时务必遵守以下准则：

    - **严禁**在生产分支开启 YOLO 模式
    - **仅在**受控的临时分支、原型开发阶段或高度重复且有测试覆盖的任务中使用
    - **建议**搭配 `--max-autopilot-continues N` 限制自动续行次数，防止失控
    - **始终**在使用后检查 Git diff，确认所有变更符合预期

!!! tip "限流时自动降级模型（1.0.35 新增）"

    在配置中设置 `continueOnAutoMode: true` 后，当遇到限流（rate limit）时，Copilot 会自动切换到 auto 模型继续工作，而非暂停等待。适合长时间运行的 Autopilot 任务，避免因限流导致工作流中断。

---

## 🤖 Programmatic 模式

如果你不想和 Copilot 交互式对话，只是想"给它一个任务，拿回一个结果"，那就用 Programmatic 模式。它通过 `-p` 参数执行单条指令后立即退出，非常适合写进脚本或 CI/CD 流水线。

``` bash
# 单次执行
copilot -p "解释 src/auth.py 中的认证逻辑"

# 结合管道使用
cat error.log | copilot -p "分析这个错误日志，找出根本原因"

# 在 CI/CD 中生成代码审查
copilot -p "审查 @src/api.py 的安全性" > review.md
```

!!! note "与交互模式的区别"

    Programmatic 模式不是 ++shift+tab++ 循环的一部分。它是一个独立的非交互命令行选项，执行完毕后立即退出。

### 实用标志

| 标志 | 功能 |
|------|------|
| `-p "prompt"` | 执行单条指令后退出 |
| `--mode <mode>` | 以指定模式启动 CLI（`interactive`/`plan`/`autopilot`）（1.0.23 新增） |
| `--plan` | 直接以 Plan 模式启动（1.0.23 新增） |
| `--autopilot` | 直接以 Autopilot 模式启动（1.0.23 新增） |
| `--allow-all` | 自动批准所有权限（适合无人值守脚本） |
| `--yolo` | 同 `--allow-all`，跳过所有确认 |
| `--agent <name>` | 指定使用的 Agent |
| `--reasoning-effort <level>` | 设置推理努力级别（1.0.4 新增），简写 `--effort` |
| `--model <model>` | 指定使用的模型，如 `claude-opus-4.7`（1.0.29 新增 Claude Opus 4.7 支持）；选 `auto` 让 Copilot 自动挑选最佳模型（1.0.32 新增） |
| `--connect <session-id>` | 直接连接到指定的远程会话（1.0.32 新增） |
| `--print-debug-info` | 打印版本、终端能力和环境变量，便于排障（1.0.32 新增） |
| `--session-idle-timeout <秒>` | 配置会话空闲超时（默认禁用，1.0.32 新增） |
| `--name <名称>` | 启动时命名会话，配合 `--resume=<名称>` 按名称恢复（1.0.35 新增） |

---

## 📤 任务委派（/delegate）

有时候任务太复杂，或者你想让 Copilot 在你关掉终端后继续工作——这时可以用 `/delegate` 把任务推送到 GitHub 云端的 Copilot coding agent。就像你把一份待办清单交给同事，他去完成后再通知你检查成果。

``` text
/delegate 完成 API 集成测试并修复所有失败的边界用例
```

也可以用 `&` 前缀快速委派：

``` text
& 完成 API 集成测试并修复所有失败的边界用例
```

!!! info "委派流程"

    1. Copilot 将未暂存的更改提交到新分支
    2. 在 GitHub 上创建 Draft PR
    3. Copilot coding agent 在云端继续工作
    4. 完成后发送通知，请求你的 Review

## 🖥️ 远程会话控制

除了委派到云端，你也可以连接并控制**已在另一台机器上运行的本地 Copilot 会话**（1.0.25 新增）。适合在服务器上启动会话后，通过笔记本远程操控：

```bash
# 启动时附加到远程会话
copilot --remote

# 会话内随时切换到远程控制
/remote
```

你也可以在 `--resume` 会话选择器中直接连接到远程控制会话（1.0.28 新增），无需手动输入命令。如果你已经知道远程会话 ID，可以用 `copilot --connect <session-id>` 直接连接（1.0.32 新增），跳过选择器。

与 `/delegate` 的区别：`/delegate` 把任务推送到 GitHub 云端代理；`--remote`/`/remote` 是连接到你自己机器上正在运行的会话，适合在多设备间切换工作环境。

`/remote` 命令现在可以显示当前状态，并通过 `/remote on` 和 `/remote off` 切换远程控制（1.0.36 改进），比之前更直观。1.0.39 进一步改进了 `/remote` 的状态输出——为每个连接状态都显示可操作的提示信息，让你一眼就知道下一步该怎么做（如"正在连接中，请等待"或"连接断开，请检查网络"）。

---

## 📊 模式对比

| 特性 | Interactive | Plan | Autopilot | Programmatic |
|------|:-----------:|:----:|:---------:|:------------:|
| 每步需要确认 | ✅ | ✅ | ❌ | N/A |
| 先规划后执行 | ❌ | ✅ | ❌（可配合 Plan） | ❌ |
| 自主连续执行 | ❌ | ❌ | ✅ | ❌ |
| 交互式对话 | ✅ | ✅ | ✅ | ❌ |
| 适合场景 | 日常编码、调试 | 复杂多文件变更 | 明确的大型任务 | 脚本/自动化 |
| 进入方式 | 默认 | ++shift+tab++ / `/plan` | ++shift+tab++ | `copilot -p` |

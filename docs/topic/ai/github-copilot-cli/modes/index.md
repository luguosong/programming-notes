# 交互模式

Copilot CLI 提供三种交互模式，通过 ++shift+tab++ 循环切换。状态栏左侧会显示当前所处的模式。此外还有一个非交互式的 Programmatic 模式用于脚本集成。

---

## Interactive 模式（默认）

默认的对话模式。你提交 prompt 后，Copilot 执行操作，然后等待你的下一条指令。每个需要审批的工具调用（如文件写入、命令执行）都会暂停并征求你的许可。

``` text
> 在 src/utils.py 中添加一个日期格式化函数

Copilot will edit src/utils.py
Allow? [Y/n]
```

适合日常编码、调试、问答等需要逐步控制的场景。

---

## Plan 模式

在编写代码之前先与 Copilot 协作制定实现计划，适用于复杂的多文件变更、大规模重构和新功能实现。

### 进入方式

=== "快捷键进入"

    按 ++shift+tab++ 切换到 Plan 模式（状态栏显示 `plan`）。

=== "斜杠命令"

    ``` text
    /plan 添加 OAuth2 身份验证，支持 Google 和 GitHub 提供商
    ```

=== "命令行参数"

    ``` bash
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

### 审批后的选项

计划生成后，你可以选择：

- `确认执行`：直接在当前模式下实施
- `切换到 Autopilot`：选择 "Accept plan and build on autopilot" 让 Copilot 自主完成
- `修改计划`：提出修改意见让 Copilot 调整计划
- `取消`：放弃当前计划

---

## Autopilot 模式（实验性）

!!! warning "实验性功能"

    需要先启用实验模式：启动时添加 `--experimental` 参数，或在会话中执行 `/experimental`。启用后会持久化，后续无需重复设置。

Autopilot 模式让 Copilot 自主完成任务，无需在每个步骤后提供输入。给出初始指令后，Copilot 会持续工作直到：

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

---

## Programmatic 模式

使用 `-p` 参数可以非交互式地执行单条指令，适合脚本集成和自动化场景：

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
| `--allow-all` | 自动批准所有权限（适合无人值守脚本） |
| `--yolo` | 同 `--allow-all`，跳过所有确认 |
| `--agent <name>` | 指定使用的 Agent |

---

## 任务委派（/delegate）

使用 `/delegate` 命令可以将当前会话推送到 GitHub 上的 Copilot coding agent，让它在云端继续完成工作：

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

---

## 模式对比

| 特性 | Interactive | Plan | Autopilot | Programmatic |
|------|:-----------:|:----:|:---------:|:------------:|
| 每步需要确认 | ✅ | ✅ | ❌ | N/A |
| 先规划后执行 | ❌ | ✅ | ❌（可配合 Plan） | ❌ |
| 自主连续执行 | ❌ | ❌ | ✅ | ❌ |
| 交互式对话 | ✅ | ✅ | ✅ | ❌ |
| 适合场景 | 日常编码、调试 | 复杂多文件变更 | 明确的大型任务 | 脚本/自动化 |
| 进入方式 | 默认 | ++shift+tab++ / `/plan` | ++shift+tab++ | `copilot -p` |

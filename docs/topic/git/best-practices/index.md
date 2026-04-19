---
title: 提交规范与最佳实践
---

# 提交规范与最佳实践

**本文你会学到：**

- 好的提交信息究竟长什么样（header + body + footer 完整结构）
- 分支命名约定：如何让分支名自我说明
- commit early, commit often：原子性提交的价值
- GUI 客户端推荐：什么时候用图形界面比命令行更高效
- 实用 Git 配置技巧：让 Git 更顺手的进阶设置

## ✉️ 提交信息的解剖

提交信息是写给未来（自己和团队）的"时间胶囊"。一条糟糕的提交信息，会在几个月后让你完全想不起那次改动的目的；一条好的提交信息，则能像 README 一样精确传达意图。

《Head First Git》把提交信息的质量视为最重要的 Git 习惯之一——好消息是，它有相当成熟的格式约定可以直接套用。

### Conventional Commits 完整结构

``` mermaid
graph TD
    MSG["完整提交信息"]
    H["Header（必须）\ntype(scope): subject"]
    BODY["Body（可选）\n解释为什么，而非是什么"]
    FOOTER["Footer（可选）\nBREAKING CHANGE / Closes #N"]
    TYPE["type\nfeat / fix / docs / style\nrefactor / test / chore / perf"]
    SCOPE["scope（可选）\n改动影响的模块或功能"]
    SUBJECT["subject\n祈使句，≤ 72 字符，不加句号"]
    MSG --> H & BODY & FOOTER
    H --> TYPE & SCOPE & SUBJECT
    classDef root fill:transparent,stroke:#f57c00,color:#adbac7,stroke-width:2px
    classDef required fill:transparent,stroke:#d32f2f,color:#adbac7,stroke-width:2px
    classDef optional fill:transparent,stroke:#768390,color:#adbac7,stroke-width:1px
    classDef detail fill:transparent,stroke:#539bf5,color:#adbac7,stroke-width:1px
    class MSG root
    class H required
    class BODY,FOOTER optional
    class TYPE,SCOPE,SUBJECT detail
```

```
<type>(<scope>): <subject>
                              ← 空一行
[body]
                              ← 空一行（可选）
[footer(s)]
```

#### Header（必须）

```
feat(auth): 添加 JWT 刷新 token 机制
├── type ──┤      ├────── subject ───────┤
           ↑
         scope（可选）
```

**type 类型速查：**

| type | 用途 |
|------|------|
| `feat` | 新功能（对应语义化版本的 MINOR） |
| `fix` | Bug 修复（对应 PATCH） |
| `docs` | 文档变更（不影响代码逻辑）|
| `style` | 代码格式调整（不影响功能，如缩进、分号）|
| `refactor` | 既不是新功能也不是 bug 修复的代码重构 |
| `test` | 添加或修改测试 |
| `chore` | 构建脚本、依赖更新等杂项（不影响源码） |
| `perf` | 性能优化 |
| `ci` | CI/CD 配置变更 |
| `revert` | 撤销某个历史提交 |

**subject 写作规则：**

- 使用祈使句（"添加 X"而非"添加了 X"，或英文用"Add X"而非"Added X"）
- 不超过 72 个字符（方便 `git log --oneline` 展示）
- 结尾**不加句号**

#### Body（可选但推荐）

Body 解释 **"为什么"** 做这个改动，而不是"做了什么"（代码本身说明了做了什么）。

```
fix(payment): 修复优惠码在移动端无法应用的问题

原因：移动端请求头中 User-Agent 包含特殊字符，导致
优惠码校验的正则表达式出现误判，将部分合法请求标记
为机器人行为后直接拒绝。

修复方式：在校验前先对 User-Agent 进行 URL 解码，
再进行正则匹配。
```

```bash title="在编辑器中写多行提交信息"
# 不加 -m，Git 会打开编辑器让你写完整信息
git commit

# 或者：通过 -m 多次使用来分段
git commit -m "fix(payment): 修复优惠码在移动端无法应用的问题" \
           -m "原因：移动端 User-Agent 特殊字符导致正则误判" \
           -m "Closes #234"
```

#### Footer（可选）

Footer 记录**破坏性变更**或**关联 Issue**：

```
feat(api)!: 用户 ID 从整型改为 UUID

迁移指南：所有调用方需要更新接收用户 ID 的参数类型。
数据库迁移脚本见 migrations/20240315_user_id_to_uuid.sql

BREAKING CHANGE: user.id 字段类型由 Integer 变为 String
Closes #301
Co-authored-by: 李四 <lisi@company.com>
```

!!! tip "BREAKING CHANGE"

    在 subject 末尾加 `!`，或在 Footer 写 `BREAKING CHANGE:`，表示这是**不兼容的变更**，会触发语义化版本的 MAJOR 版本号升级。

### ❌ 反面教材

```bash
# 这些提交信息完全没有价值：
git commit -m "update"
git commit -m "fix bug"
git commit -m "aaa"
git commit -m "test"
git commit -m "修改"
git commit -m "2024/3/15 更新"

# 这些虽然有点内容，但还不够好：
git commit -m "修复了登录时候的一个关于 token 的 bug"   # ❌ 没用 type，描述不精确
git commit -m "feat: 添加了很多东西"                     # ❌ subject 太模糊
```

## 🌿 分支命名约定

好的分支名能让团队成员一眼看出这个分支在做什么、属于哪个类型、可能关联哪个 Issue。

### 推荐格式

```
<type>/<short-description>
<type>/<issue-id>-<short-description>    ← 关联 Issue 时
```

**type 前缀：**

| 前缀 | 含义 |
|------|------|
| `feat/` | 新功能开发 |
| `fix/` | Bug 修复 |
| `hotfix/` | 生产环境紧急修复 |
| `release/` | 发布准备 |
| `chore/` | 杂项（升级依赖、配置变更）|
| `docs/` | 文档更新 |
| `refactor/` | 重构 |
| `test/` | 测试相关 |
| `experiment/` | 实验性功能（可能不合并）|

**示例：**

```bash
# ✅ 好的分支名
feat/user-authentication
fix/login-token-expiry
hotfix/payment-null-pointer
release/v2.1.0
chore/upgrade-spring-boot-3.5
feat/301-user-profile-avatar   # 关联 Issue #301

# ❌ 糟糕的分支名
my-branch
test1
john-stuff
fix
dev
```

### 命名规则

- 使用**连字符**（`-`）分隔单词，不用下划线或驼峰
- 全部**小写**
- 简洁但自描述：3-5 个单词足够
- 避免个人名字（`john-feature`）和日期（`20240315-update`）——这些信息 Git 历史里已经有了

## 📦 commit early, commit often

"提交早、提交频繁"是资深 Git 用户共同遵守的原则，原因很简单：

**小提交的好处：**

- **更容易 code review**：每次改动聚焦一件事，审查者理解成本低
- **更容易撤销**：改错了只需要 `revert` 一个小提交，不影响其他功能
- **更容易 bisect**：每次提交改动少，bisect 找到的"问题提交"更精确
- **更完整的历史**：项目演进脉络清晰，新人上手更快

**原子性提交的判断标准**：

> 一次提交，只做一件事。如果你写提交信息时需要用"以及"连接两件事，那就应该分成两次提交。

``` mermaid
graph LR
    subgraph "❌ 非原子提交"
    BIG["一个提交\n添加用户模块＋修复登录 bug\n＋更新 README"]
    end
    subgraph "✅ 原子性提交"
    A1["feat(user): 添加用户注册接口"]
    A2["fix(auth): 修复登录 token 未刷新"]
    A3["docs: 更新 README 认证说明"]
    end
    classDef bad fill:transparent,stroke:#d32f2f,color:#adbac7,stroke-width:2px
    classDef good fill:transparent,stroke:#388e3c,color:#adbac7,stroke-width:2px
    class BIG bad
    class A1,A2,A3 good
```

```bash
# ❌ 一个提交做了太多事
git commit -m "feat: 添加用户模块，修复了登录 bug，更新了 README"

# ✅ 拆分为三个原子提交
git commit -m "feat(user): 添加用户注册和个人信息接口"
git commit -m "fix(auth): 修复登录时 token 未刷新导致的 401"
git commit -m "docs: 更新 README 中的 API 认证说明"
```

**WIP 提交的清理**：

在功能分支上开发时，难免会有"保存进度"的 WIP 提交。推送 PR 前，用交互式 rebase 整理一下：

```bash
# 开发过程中的提交历史：
# WIP: 用户接口骨架
# WIP: 添加校验逻辑
# fix: 修改变量名拼写错误
# WIP: 完善错误处理

# PR 前整理为一个干净的提交：
git rebase -i HEAD~4    # 合并最近4个提交
# 在交互编辑器中把后3个改为 squash/fixup
```

## 🖥️ GUI 客户端：命令行之外的选择

命令行是 Git 的原生界面，但 GUI 客户端在某些场景下效率更高，二者并不矛盾。

### 哪些操作用 GUI 更高效？

| 操作 | 推荐工具 |
|------|---------|
| 查看分支历史图谱 | GUI > 命令行 |
| 解决合并冲突 | GUI 三栏视图 > 手动编辑 |
| 精细化选择要暂存的行（`git add -p`） | GUI > 命令行 |
| 查看文件 diff | GUI > 命令行（颜色、行号更直观）|
| cherry-pick 操作 | GUI 拖拽 > 命令行 |
| 学习 Git 时可视化理解 | GUI > 命令行 |

### 主流 GUI 客户端推荐

=== "VS Code（内置）"

    VS Code 的 Source Control 面板内置 Git 支持，适合日常提交、查看 diff、解决冲突。配合以下扩展更强大：
    - **GitLens**：blame、历史追踪、分支对比（强烈推荐）
    - **Git Graph**：可视化分支历史图谱

    ```bash
    # 用 VS Code 打开某个文件的 diff（命令行调用）
    git difftool --tool=vscode -- src/app.py
    git config --global diff.tool vscode
    git config --global difftool.vscode.cmd 'code --wait --diff $LOCAL $REMOTE'
    ```

=== "IntelliJ IDEA / Android Studio"

    JetBrains IDE 内置 Git 集成极为完善：
    - 三栏冲突解决界面（行业标杆）
    - 提交前可以 review 所有改动
    - 交互式 rebase 的图形界面

=== "GitHub Desktop"

    GitHub 官方出品，界面极简，零学习成本。适合：
    - Git 初学者
    - 只用 GitHub 的团队
    - macOS / Windows 用户

=== "GitKraken"

    功能最完整的独立 Git GUI，分支图谱最漂亮。适合需要管理多个仓库的开发者。

### 与命令行配合的最佳姿势

GUI 和命令行不是非此即彼的关系——很多开发者两者混用：

```bash
# 用命令行创建分支、push、写提交信息（快）
git switch -c feat/payment && git push -u origin feat/payment

# 用 GUI 查看 diff、解决冲突、做 code review（直观）
code .    # 打开 VS Code 查看 Source Control
```

## ⚙️ 进阶配置技巧

### 全局 .gitconfig 速查

```ini title="~/.gitconfig 常用配置"
[user]
    name = 你的名字
    email = you@example.com

[core]
    editor = code --wait       # 默认编辑器（VS Code）
    pager = less -FX           # 输出少时不进入分页模式

[alias]
    lg = log --oneline --graph --all --decorate
    st = status
    co = checkout
    br = branch
    unstage = restore --staged
    last = log -1 HEAD --stat

[pull]
    rebase = true              # git pull 默认使用 rebase

[fetch]
    prune = true               # fetch 时自动清理陈旧的远程跟踪分支

[push]
    default = current          # git push 默认推送到同名远程分支

[rerere]
    enabled = true             # 记住冲突解决方案，相同冲突自动解决

[diff]
    tool = vscode

[merge]
    tool = vscode
    conflictstyle = diff3      # 显示三路冲突（更多上下文）
```

### rerere：记住冲突解决方案

当你在长期功能分支上频繁 rebase 时，同一个冲突可能反复出现。`rerere`（Reuse Recorded Resolution）让 Git 记住你的解决方案，下次自动应用：

```bash
# 开启 rerere
git config --global rerere.enabled true

# 之后每次解决冲突后 add，Git 会自动记录
git add conflicted-file.py
# 记录保存在 .git/rr-cache/

# 下次出现相同冲突时，Git 自动解决并提示：
# Resolved 'conflicted-file.py' using previous resolution.
```

### 有用的 Git 别名

```bash title="一次性配置的实用别名"
# 彩色图形日志（强烈推荐）
git config --global alias.lg "log --color --graph --pretty=format:'%Cred%h%Creset -%C(yellow)%d%Creset %s %Cgreen(%cr) %C(bold blue)<%an>%Creset' --abbrev-commit"

# 快速查看上一次提交
git config --global alias.last "log -1 HEAD --stat"

# 撤销暂存区
git config --global alias.unstage "restore --staged"

# 列出所有别名
git config --global alias.aliases "config --get-regexp alias"
```

## 小结

| 主题 | 关键建议 |
|------|---------|
| 提交信息 | 使用 `type(scope): subject` 格式；body 解释"为什么" |
| 分支命名 | `feat/`、`fix/`、`hotfix/` 等前缀 + 连字符描述 |
| 提交频率 | 原子性提交；一次提交只做一件事 |
| GUI vs 命令行 | diff/冲突用 GUI，日常操作用命令行 |
| 配置 | 开启 `rerere`、设置 `fetch.prune`、配置 `pull.rebase` |

下一篇「重写历史」将讲解如何用 `amend`、`rebase -i` 和 `filter-repo` 整理已有的提交历史。

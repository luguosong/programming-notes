---
title: 撤销时光机
---

# 撤销时光机：restore / reset / revert

**本文你会学到：**

- 三种撤销场景及对应工具
- `git restore`：丢弃工作区/暂存区的修改
- `git commit --amend`：修改最近一次提交
- `git reset`：移动 HEAD 指针（软/混合/硬）
- `git revert`：安全撤销已发布的提交
- 如何选择：什么时候用哪个？

## ⚠️ 先建立"反悔"的心智模型

在 Git 里，"撤销"并不是一个单一操作——根据你的代码**到达了哪个阶段**，对应不同的命令：

``` mermaid
graph LR
    W["🗂️ 工作区\n（还没 add）"] 
    S["📋 暂存区\n（已 add）"] 
    C["🗄️ 仓库\n（已 commit）"]
    W -->|"git add"| S
    S -->|"git commit"| C
    W -->|"git restore <file>"| W
    S -->|"git restore --staged <file>"| S
    C -->|"git reset / git revert"| C
    style W fill:transparent,stroke:#539bf5,color:#adbac7,stroke-width:2px
    style S fill:transparent,stroke:#f57c00,color:#adbac7,stroke-width:2px
    style C fill:transparent,stroke:#388e3c,color:#adbac7,stroke-width:2px
```

---

## 1️⃣ 撤销工作区的修改：git restore

### 场景

你修改了一个文件，但改错了，想恢复成上次 `git add` 或 `git commit` 时的样子。

```bash title="丢弃工作区的修改"
git restore app.py           # 把 app.py 恢复到暂存区的版本
git restore .                # 丢弃工作区所有未暂存的修改

# 从某个提交还原文件（不影响历史）
git restore --source HEAD~2 app.py   # 把 app.py 恢复到2个提交之前
```

!!! danger "注意：这是不可逆的"

    `git restore <file>` 会**直接丢弃工作区的修改**，没有回收站，确认再执行。如果有重要改动，先 `git stash` 保存再说。

### 撤销暂存区（从暂存区移出）

你用 `git add` 暂存了文件，但后悔了，想把它移出暂存区（保留工作区修改）：

```bash title="把文件从暂存区移出"
git restore --staged app.py     # 从暂存区移出，但工作区修改保留
git restore --staged .          # 移出所有暂存文件

# 等效的旧写法（见于旧教程）：
git reset HEAD app.py           # 效果相同，但语义不直观
```

---

## 2️⃣ 修改最近一次提交：--amend

### 场景

刚提交完，发现提交信息写错了，或者漏加了一个文件：

```bash title="修补最近一次提交"
# 只修改提交信息（不改文件）
git commit --amend -m "fix: 修复登录时 token 未刷新的问题"

# 漏加了文件，补救：
git add forgotten_file.py
git commit --amend --no-edit    # --no-edit 表示不改提交信息

# 重新打开编辑器修改提交信息
git commit --amend
```

!!! warning "amend 会改变 commit SHA"

    `--amend` 实际上是**用新的提交替换旧提交**（SHA 会变化）。如果这个提交已经 `push` 到远程仓库，`--amend` 后再 `push` 需要加 `--force-with-lease`，且会影响其他人的工作。**未 push 的提交随便 amend，已 push 的提交慎用。**

---

## 3️⃣ 移动 HEAD：git reset

`git reset` 是更强大的工具——它可以让当前分支指针回退到某个历史提交。有三种模式：

``` mermaid
graph LR
    C1["提交A"] --> C2["提交B"] --> C3["提交C（HEAD）"]
    C2 -.->|"git reset 到B"| H["HEAD 移到这里"]
    classDef commit fill:transparent,stroke:#768390,color:#adbac7,stroke-width:1px
    classDef head fill:transparent,stroke:#f57c00,color:#adbac7,stroke-width:2px
    class C1,C2,C3 commit
    class H head
```

### 三种模式对比

| 模式 | 命令 | 提交记录 | 暂存区 | 工作区 |
|------|------|---------|--------|--------|
| 软重置 | `--soft` | 回退 | **保留** | 保留 |
| 混合重置（默认）| `--mixed` | 回退 | **清空** | 保留 |
| 硬重置 | `--hard` | 回退 | 清空 | **清空** |

```bash title="git reset 三种模式示例"
# 假设当前在 C 提交，想回到 A

# --soft：只移动 HEAD，暂存区和工作区都保留
# 使用场景：把最近几次提交"合并"成一次
git reset --soft HEAD~2        # 回退2个提交，改动保留在暂存区
git commit -m "feat: 整合用户系统改动"

# --mixed（默认）：移动 HEAD + 清空暂存区，工作区保留
# 使用场景：后悔 add 了什么，重新选择要提交的内容
git reset HEAD~1               # 等同于 git reset --mixed HEAD~1
git reset HEAD~1 app.py        # 只把 app.py 从暂存区移出

# --hard：彻底回退，改动全部丢失 ⚠️
# 使用场景：完全放弃最近几次提交和所有改动
git reset --hard HEAD~3        # 完全回退3个提交
git reset --hard origin/main   # 强制同步到远程最新状态
```

!!! danger "--hard 很危险"

    `--hard` 会丢失工作区和暂存区的所有修改，且**不可找回**（除非你记住了 SHA，可以用 `git reflog` 补救）。操作前务必确认。

### 引用方式速查

```bash
HEAD       # 当前提交
HEAD~1     # 上一个提交（等同于 HEAD^）
HEAD~3     # 往前3个提交
abc1234    # 具体的 SHA（可以只写前几位）
main       # 某个分支名
v1.2.0     # 某个 tag
```

---

## 4️⃣ 安全撤销已发布提交：git revert

### reset vs revert 的本质区别

`git reset` 是"**改写历史**"，而 `git revert` 是"**新增一个反向提交**"：

``` mermaid
graph LR
    subgraph "reset 方式（危险）"
    A1["A"] --> B1["B（被删除）"] --> C1["C（HEAD）"]
    A1 -.->|"reset 后 HEAD 指向A"| A1
    end
    subgraph "revert 方式（安全）"
    A2["A"] --> B2["B"] --> C2["C"] --> R2["revert-B\n（消除B的改动）"]
    end
    classDef c fill:transparent,stroke:#768390,color:#adbac7,stroke-width:1px
    classDef danger fill:transparent,stroke:#d32f2f,color:#adbac7,stroke-width:2px
    classDef safe fill:transparent,stroke:#388e3c,color:#adbac7,stroke-width:2px
    class A1,B1,C1 danger
    class A2,B2,C2,R2 safe
```

```bash title="git revert 用法"
# 撤销最近一次提交（会打开编辑器写 revert 提交信息）
git revert HEAD

# 撤销指定提交
git revert abc1234

# 撤销但不自动提交（先看效果）
git revert --no-commit abc1234
git revert --no-commit HEAD~2..HEAD   # 撤销一段范围

# 多个提交的批量 revert
git revert HEAD~3..HEAD    # 撤销最近3个提交（生成3个 revert 提交）
```

### 什么时候用 reset，什么时候用 revert？

| 情况 | 推荐 | 原因 |
|------|------|------|
| 提交**还没有 push** | `git reset` | 本地历史随便改 |
| 提交**已经 push** 到共享分支 | `git revert` | 不破坏他人的历史 |
| 只是想撤销一个中间提交 | `git revert` | reset 会连带丢失之后的提交 |

---

## 5️⃣ 后悔药：git reflog

以为 `reset --hard` 之后数据没了？别怕，只要你没有做 GC，Git 会在 `reflog` 里保留最近 90 天的所有操作记录：

```bash title="用 reflog 找回"丢失"的提交"
git reflog
# HEAD@{0}: reset: moving to HEAD~3
# HEAD@{1}: commit: feat: 添加支付功能
# HEAD@{2}: commit: feat: 购物车优化
# HEAD@{3}: commit: fix: 修复订单状态

# 恢复到某个之前的状态
git reset --hard HEAD@{2}     # 回到"feat: 购物车优化"那个状态
git checkout HEAD@{1}         # 以游离 HEAD 模式查看
```

!!! tip "reflog 是你的最终保险"

    只要提交曾经存在过（哪怕被 reset 掉了），90天内都能通过 reflog 找回。这就是为什么 Git 几乎不会真正"丢失"数据。

---

## 🔁 撤销合并提交：revert -m 1

当一个错误的 PR 已经合并到 `main`，需要撤销整个合并时，普通的 `git revert` 不够——合并提交有两个父提交，Git 需要知道"以哪个父提交为主线"：

```bash
# 查看合并提交的结构
git log --merges --oneline -5
# a1b2c3d Merge pull request #42 from feature/broken-payment

git show a1b2c3d
# commit a1b2c3d
# Merge: b3c4a5b d6e7f8a     ← 两个父提交
#   parent 1: b3c4a5b (main 主线)
#   parent 2: d6e7f8a (feature 分支)

# 用 -m 1 表示"以第1个父提交（main）为主线"
git revert -m 1 a1b2c3d
# 这会产生一个新的 revert 提交，完全抵消 PR 带来的改动
```

!!! warning "revert 合并提交后，重新合并同一分支需要注意"

    用 `revert -m 1` 撤销了合并后，如果想重新合并修复后的 feature 分支，需要先 revert 那个 revert 提交（让 Git 重新认为 feature 的改动是"新的"）：
    ```bash
    git revert <revert-of-merge-sha>  # revert 那个 revert
    git merge feature/fixed-payment   # 现在可以重新合并
    ```

## 🧪 实战演练：完整撤销场景

以下是一个典型的混合撤销场景，综合运用本篇所有工具：

```bash title="场景：开发途中发现改错了方向"
# 当前状态：
# commit C: WIP - 走错了方向
# commit B: fix: 修复登录验证
# commit A: feat: 添加搜索功能
# （在 push 之前）

# 方案1：保留 A、B，丢弃 C（软重置，保留 C 的代码改动在工作区）
git reset --soft HEAD~1      # HEAD 退回到 B，C 的改动保留在暂存区
git restore --staged .       # 移出暂存区（可选）
# 现在可以重新组织代码

# 方案2：保留 A、B，完全丢弃 C 的代码
git reset --hard HEAD~1      # 彻底回到 B，C 的改动消失

# 方案3：不动历史，只是"标记"C 的反操作
git revert HEAD              # 生成 D（抵消 C），A→B→C→D
```

```bash title="场景：不小心把密码提交了"
# 刚提交了包含 API KEY 的文件，还没 push

# 步骤1：立刻从工作区删除密码
rm config/secrets.env
echo "secrets.env" >> .gitignore
git add .

# 步骤2：修补上一条提交（覆盖掉包含密码的提交）
git commit --amend --no-edit

# 步骤3：验证密码已不在最新提交中
git show HEAD -- config/secrets.env   # 应该看不到内容了

# ⚠️ 如果已经 push 了，密码已经泄露，必须立刻吊销 API KEY！
# 然后用 git filter-repo 清理历史（见「重写历史」篇）
```

## 小结：如何选择撤销工具？

``` mermaid
graph TD
    Q1{"改动到了哪一步？"}
    Q1 -->|"只在工作区，还没 add"| A1["git restore <file>"]
    Q1 -->|"已 add，在暂存区"| A2["git restore --staged <file>"]
    Q1 -->|"已 commit（未 push）"| Q2{"要怎么处理改动？"}
    Q1 -->|"已 commit 且已 push"| A5["git revert（安全）"]
    Q2 -->|"改动保留在暂存区"| A3["git reset --soft"]
    Q2 -->|"改动保留在工作区"| A4["git reset --mixed（默认）"]
    Q2 -->|"改动全部丢弃"| A6["git reset --hard ⚠️"]
    classDef action fill:transparent,stroke:#539bf5,color:#adbac7,stroke-width:2px
    classDef danger fill:transparent,stroke:#d32f2f,color:#adbac7,stroke-width:2px
    classDef question fill:transparent,stroke:#768390,color:#adbac7,stroke-width:1px
    class A1,A2,A3,A4,A5 action
    class A6 danger
    class Q1,Q2 question
```

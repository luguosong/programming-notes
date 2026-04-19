---
title: 远程协作
---

# 远程协作：fetch / pull / push

**本文你会学到：**

- remote 的概念和管理（`git remote`）
- 远程跟踪分支（`origin/main`）是什么
- `fetch` vs `pull` 的区别
- `push` 与上游分支设置
- 多人协作的典型场景

## 🌐 remote：远程仓库是什么？

本地仓库 + 远程仓库的关系，就像你的本地笔记和云端同步盘：
- **本地仓库**：你随时工作的地方，不需要网络
- **远程仓库**（remote）：团队共享的中央存储，需要 push/pull 同步

```bash title="管理远程仓库"
# 查看远程仓库
git remote -v
# origin  git@github.com:user/repo.git (fetch)
# origin  git@github.com:user/repo.git (push)

# 添加远程仓库（clone 时自动添加 origin）
git remote add origin git@github.com:user/repo.git
git remote add upstream git@github.com:original/repo.git  # fork 时添加上游

# 查看远程仓库详情
git remote show origin

# 重命名 / 删除
git remote rename origin github
git remote remove upstream
```

!!! info "`origin` 只是个约定名称"

    `origin` 只是 `git clone` 时默认给远程仓库起的名字，你完全可以改成其他名字（如 `github`、`company`）。一个本地仓库可以有多个 remote。

## 📡 远程跟踪分支：origin/main

当你 clone 或 fetch 之后，Git 会在本地保存远程分支的状态，称为**远程跟踪分支**：

```
本地分支：   main                  ← 你工作的地方
远程跟踪：   origin/main           ← 上次同步时远程的状态（只读快照）
```

``` mermaid
graph LR
    L["main（本地）\nHEAD 在这里"]
    R["origin/main（远程跟踪）\n上次 fetch 时的状态"]
    Remote["GitHub: main\n（可能比 origin/main 更新）"]
    L -.->|"本地新提交"| L
    Remote -.->|"git fetch"| R
    R -.->|"git merge / git rebase"| L
    classDef local fill:transparent,stroke:#539bf5,color:#adbac7,stroke-width:2px
    classDef track fill:transparent,stroke:#f57c00,color:#adbac7,stroke-width:2px
    classDef remote fill:transparent,stroke:#388e3c,color:#adbac7,stroke-width:2px
    class L local
    class R track
    class Remote remote
```

```bash
# 查看所有分支（含远程跟踪）
git branch -a
# * main
#   feature/login
#   remotes/origin/main
#   remotes/origin/develop

# 查看本地 vs 远程的差距
git log origin/main..main      # 本地比远程多的提交
git log main..origin/main      # 远程比本地多的提交
```

### 远程跟踪分支的4大作用

《Head First Git》专门用一整节来阐述远程跟踪分支存在的意义，这4个理由很值得记住：

**作用1：告诉你推送到哪里**

当你运行 `git push` 时，Git 通过远程跟踪分支知道"往哪个远程、哪个分支推送"——这就是为什么首次 push 必须设置上游（`-u`），之后才能省略。

**作用2：获取并整合同事的更新**

`git fetch` 更新远程跟踪分支，然后你可以查看变化，选择时机再合并：

```bash
git fetch origin
git log origin/main --oneline    # 看看同事提交了什么
git diff main origin/main        # 看看具体改了哪些
git merge origin/main            # 满意后再合并
```

**作用3：告诉你是否需要推送**

`git branch -vv` 输出的 `[ahead N]` / `[behind N]` 信息，正是靠远程跟踪分支来计算的：

```bash
git branch -vv
# * main  a1b2c3d [origin/main: ahead 2, behind 1] feat: 搜索功能
#   ↑ 本地比远程多2个提交（需要 push），远程比本地多1个（需要先 pull）
```

**作用4：合并前的"预检区"**

clone 一个新仓库后，远程的其他分支并不自动创建为本地分支——但你可以通过远程跟踪分支先查看再决定是否需要：

```bash
git branch -a                    # 看到 remotes/origin/feature/payment
git switch feature/payment       # Git 自动基于远程跟踪分支创建本地分支
```

## 📥 fetch vs pull

这是初学者最常见的困惑点：

| | `git fetch` | `git pull` |
|-|-------------|------------|
| 做什么 | 下载远程数据，**不修改**本地分支 | fetch + merge（或 rebase）二合一 |
| 工作区是否变化 | ❌ 不变 | ✅ 会变（合并后） |
| 安全性 | 总是安全 | 可能产生冲突 |
| 适用场景 | 先看看远程有什么变化 | 直接同步到本地 |

``` mermaid
sequenceDiagram
    participant L as 本地 main
    participant T as origin/main（跟踪分支）
    participant R as 远程 GitHub
    Note over L,R: 方式① 推荐：fetch + merge（分步执行，有预检机会）
    R->>T: git fetch origin（只下载，不动本地分支）
    Note over L: 可先 git log / git diff 查看变化
    T->>L: git merge origin/main（确认后手动整合）
    Note over L,R: 方式② git pull（自动合并，无预检）
    R->>T: 下载新提交
    T->>L: 自动 merge（没有机会先检查）
```

!!! tip "推荐：用 `git fetch` + `git merge`，少用 `git pull`"

    `git pull` 表面上方便，但它把"下载"和"合并"捆绑在一起，让你没有机会在合并前检查远程带来了什么。《Head First Git》明确建议：

    > **"Use git fetch + git merge. Avoid git pull."**

    下面展示推荐的工作方式：

```bash title="推荐的同步方式：fetch + 手动 merge"
# 第一步：下载远程更新（不影响工作区）
git fetch origin

# 第二步：查看远程带来了什么变化
git log origin/main --oneline    # 新增了哪些提交？
git diff main origin/main        # 具体改了哪些内容？

# 第三步：确认没问题后，再整合
git merge origin/main            # 合并（保留分支历史）
# 或
git rebase origin/main           # 变基（保持线性历史）
```

```bash title="git pull 的用法（了解即可）"
git pull                   # 拉取当前分支的上游，并 merge
git pull origin main       # 明确指定远程和分支
git pull --rebase          # 拉取后用 rebase 而不是 merge（保持线性历史）

# 如果你习惯用 pull，至少配置为 rebase 模式
git config --global pull.rebase true
```

## 📤 push：把本地提交推送到远程

```bash title="push 的各种用法"
# 基本 push（需要先设置上游分支）
git push

# 首次 push 功能分支，设置上游并推送
git push -u origin feature/login
# -u 等同于 --set-upstream，以后直接 git push 即可

# 推送到指定远程的指定分支
git push origin main
git push origin feature/login:feature/login

# 删除远程分支（两种写法）
git push origin --delete feature/old-feature
git push origin :feature/old-feature       # 旧写法（推送空分支到远程）

# 推送所有本地分支
git push --all origin

# 强制推送（危险！覆盖远程历史）
git push --force-with-lease    # 推荐：先检查远程有没有新提交
git push -f                    # 不安全：直接覆盖，可能丢失他人工作
```

### 上游分支（upstream）

```bash
# 查看当前分支追踪的远程分支
git branch -vv
# * main      a1b2c3d [origin/main: ahead 2] feat: 添加搜索
# feature     e4f5g6h [origin/feature] WIP

# "ahead 2" 表示本地比远程多 2 个提交（需要 push）
# "behind 3" 表示远程比本地多 3 个提交（需要 pull）

# 修改当前分支的上游追踪
git branch --set-upstream-to=origin/main main
```

## 🤝 多人协作典型场景

### 场景一：同步主线到功能分支

```bash
# 你在 feature/login 分支，main 已经有新提交
git fetch origin               # 先下载（不影响工作区）
git rebase origin/main         # 把功能分支移植到最新 main 之上
# 或：git merge origin/main    # 产生合并提交
```

### 场景二：同事 push 了，你 push 失败

```bash
git push
# ! [rejected] main -> main (non-fast-forward)
# hint: Updates were rejected because the remote contains work you don't have

# 解决：先拉取同事的提交
git pull --rebase             # 拉取并 rebase（推荐）
git push                      # 再 push
```

### 场景三：Fork 仓库同步上游

```bash
# 首次设置上游
git remote add upstream https://github.com/original/repo.git

# 同步上游最新
git fetch upstream
git switch main
git rebase upstream/main      # 或 merge
git push origin main          # 更新自己 fork 的 main
```

## 🧹 清理陈旧的远程跟踪分支

功能分支合并并删除后，本地的远程跟踪分支（如 `remotes/origin/feature/login`）不会自动消失。定期清理可以让 `git branch -a` 的输出更整洁：

```bash title="清理陈旧的远程跟踪分支"
# 查看哪些远程跟踪分支已经"过期"（对应的远程分支已被删除）
git remote show origin
# 会列出 "stale" 状态的远程跟踪分支

# 清除所有已在远程删除的跟踪分支（只影响本地的 remotes/origin/* 记录）
git fetch --prune
# 或：git remote prune origin

# 配置为每次 fetch 自动清理
git config --global fetch.prune true

# 删除本地遗留的已合并功能分支（合并进 main 后）
git branch --merged main | grep -v "main" | xargs git branch -d
```



| 命令 | 用途 |
|------|------|
| `git remote -v` | 查看远程仓库 |
| `git fetch` | 下载远程更新（不影响工作区）|
| `git fetch --prune` | 下载并清理已删除的远程跟踪分支 |
| `git pull --rebase` | 拉取并 rebase |
| `git push -u origin <branch>` | 首次推送并设置上游 |
| `git push --force-with-lease` | 安全强推 |
| `git branch -vv` | 查看本地/远程分支跟踪关系 |

下一篇「贮藏与清理」将讲解临时保存工作现场的利器 `git stash`。

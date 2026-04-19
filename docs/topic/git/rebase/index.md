---
title: 变基详解
---

# 变基详解：rebase vs merge

**本文你会学到：**

- rebase 的核心原理（"改写提交的父节点"）
- merge vs rebase 的视觉差异
- 交互式 rebase：整理杂乱的提交历史
- rebase 的黄金法则：什么情况下绝对不能 rebase

## 🤔 为什么会有 rebase？

当你在功能分支工作了几天，main 分支已经向前走了好几步。你想把 main 的最新代码合进来，有两种方式：

``` mermaid
graph LR
    subgraph "初始状态"
    A["E1"] --> A2["E2（main）"]
    A --> A3["F1"] --> A4["F2（feature，HEAD）"]
    end
    classDef commit fill:transparent,stroke:#768390,color:#adbac7,stroke-width:1px
    class A,A2,A3,A4 commit
```

**方式一（merge）**：产生一个额外的合并提交 M：

``` mermaid
graph TD
    subgraph "merge（保留分叉历史）"
    E1["E1"] --> E2["E2（main）"]
    E1 --> F1["F1"] --> F2["F2（feature）"]
    E2 --> M["M（合并提交）"]
    F2 --> M
    end
    subgraph "rebase（线性历史）"
    E3["E1"] --> E4["E2（main）"] --> F3["F1'"] --> F4["F2'（feature）"]
    end
    classDef commit fill:transparent,stroke:#768390,color:#adbac7,stroke-width:1px
    classDef merge fill:transparent,stroke:#388e3c,color:#adbac7,stroke-width:2px
    classDef newc fill:transparent,stroke:#539bf5,color:#adbac7,stroke-width:2px
    class E1,E2,F1,F2,E3,E4 commit
    class M merge
    class F3,F4 newc
```

**方式二（rebase）**：把 F1、F2 "移植"到 E2 之后，历史变成一条直线：

```
E1 → E2 → F1' → F2'（feature 重新基于 E2）
```

## 🔧 rebase 的工作原理

`git rebase main` 的实际步骤：

1. 找到 feature 和 main 的**共同祖先**（E1）
2. 把 E1 之后 feature 上的提交（F1、F2）暂存起来（成为"补丁"）
3. 把 feature 的起点移到 main 的最新提交（E2）
4. 依次把补丁重新应用上去，生成 **F1'、F2'**（内容相同，但 SHA 不同）

``` mermaid
graph LR
    A["E1"] --> B["E2（main）"] --> C["F1'"] --> D["F2'（feature，HEAD）"]
    classDef commit fill:transparent,stroke:#768390,color:#adbac7,stroke-width:1px
    classDef new fill:transparent,stroke:#388e3c,color:#adbac7,stroke-width:2px
    class A,B commit
    class C,D new
```

```bash title="基本 rebase 用法"
git switch feature
git rebase main              # 把 feature 变基到 main 最新提交之上

# 如果有冲突（逐个提交地处理）：
# 解决冲突 → git add . → git rebase --continue
# 放弃 rebase：git rebase --abort

# rebase 完成后，切回 main 合并（此时是 fast-forward）
git switch main
git merge feature            # 一条直线，非常干净
```

## ⚖️ merge vs rebase 对比

| | merge | rebase |
|-|-------|--------|
| 历史形状 | 有分叉 + 合并提交，保留真实历史 | 线性，干净整洁 |
| 是否改写 SHA | ❌ 不改 | ✅ 改（生成新的提交） |
| 冲突处理 | 一次性解决所有 | 逐提交解决（可能更清晰）|
| 适合场景 | 保留功能分支历史、公共分支合并 | 功能分支同步主线、清理本地提交 |
| 安全性 | 总是安全 | 已推送的分支使用危险 |

**什么时候用哪个**：
- 想保留完整分支历史 → `merge`
- 想保持线性历史（PR 前整理） → `rebase`
- 公共分支（main/develop）上 → **永远用 merge，不用 rebase**

## ✏️ 交互式 rebase：整理提交历史

`git rebase -i`（interactive）是最强大的历史整理工具。它允许你在一系列提交上执行：**合并、拆分、修改、重排、删除**。

### 典型场景：合并零碎提交

开发过程中常常会有这样的提交历史：

```
a1b2c3d WIP: 先提交一版
e4f5g6h fix typo
h7i8j9k 完成了，但忘了加注释
k1l2m3n 加了注释
```

用 `rebase -i` 整理成一个干净的提交：

```bash title="交互式 rebase 整理提交"
git rebase -i HEAD~4    # 整理最近4个提交

# 会打开编辑器，显示：
# pick a1b2c3d WIP: 先提交一版
# pick e4f5g6h fix typo
# pick h7i8j9k 完成了，但忘了加注释
# pick k1l2m3n 加了注释

# 把后面的 pick 改为：
# pick a1b2c3d WIP: 先提交一版    ← 保留，作为基础
# squash e4f5g6h fix typo         ← 合并到上一个提交
# squash h7i8j9k 完成了，但忘了加注释
# squash k1l2m3n 加了注释

# 保存退出后，Git 会弹出新的编辑器让你写合并后的提交信息
```

### rebase -i 的所有操作指令

| 指令 | 缩写 | 含义 |
|------|------|------|
| `pick` | `p` | 保留提交（不变） |
| `reword` | `r` | 保留提交，但修改提交信息 |
| `edit` | `e` | 暂停，允许你修改这个提交 |
| `squash` | `s` | 合并到前一个提交，并合并提交信息 |
| `fixup` | `f` | 合并到前一个提交，**丢弃**本次提交信息 |
| `drop` | `d` | 直接删除这个提交 |
| `exec` | `x` | 执行一条 shell 命令 |

```bash title="常用组合示例"
# 修改某个提交的内容（不只是信息）
git rebase -i HEAD~3
# 把目标提交的 pick 改为 edit，保存退出
# Git 会停在那个提交，你可以修改文件
git add changed_file.py
git commit --amend --no-edit   # 修改提交内容
git rebase --continue          # 继续后续提交

# 删除某个提交
git rebase -i HEAD~5
# 把目标提交的 pick 改为 drop（或直接删除那一行）
```

## ⚠️ 黄金法则：rebase 的禁区

!!! danger "绝对不要 rebase 已经推送到公共仓库的提交"

    rebase 会改写提交的 SHA。如果你 rebase 了已经 `push` 到 `main` 或团队共享分支的提交，其他人的本地仓库里还有旧的 SHA，下次他们 pull 时会造成**历史分叉混乱**，Git 记录会变成两份相同改动的"平行历史"。

**安全边界**：
- ✅ 可以 rebase：**只在本地、还没推送**的提交
- ✅ 可以 rebase：**自己独享的功能分支**（没人基于它工作）
- ❌ 禁止 rebase：`main`、`develop` 等公共分支
- ❌ 禁止 rebase：已经有 PR 且其他人看过的提交（会让 review 历史失效）

**如果已经推送了，强制 push 的代价**：

```bash
# 不得已时才用，且必须通知所有协作者
git push --force-with-lease   # 比 --force 更安全（检查远程有没有新提交）
```

## 🎯 rebase --onto：精确控制变基目标

当你需要把一段提交从一个地方"搬"到完全不同的地方时，`--onto` 参数非常有用。

**场景**：你在 `feature-b`（基于 `feature-a`）上开发，但 `feature-a` 被砍掉了，需要把 `feature-b` 的提交直接接到 `main` 上。

```
原始历史：
main → A → B
             └── feature-a → C → D
                                  └── feature-b → E → F（HEAD）

目标：
main → A → B → E' → F'（feature-b 直接接到 main）
```

```bash title="--onto 用法"
# 语法：git rebase --onto <新基底> <旧基底> <要移动的分支>
git rebase --onto main feature-a feature-b

# 解读：把 feature-b 中，在 feature-a 之后的提交（E、F）
#       移到 main 之后
```

```bash title="另一个常见场景：只保留部分提交"
# 把最近 3 个提交中的最后 2 个移走，只留第一个
git rebase --onto HEAD~3 HEAD~2
# 等效于删除倒数第 3 个提交（保留倒数 1、2 两个）
```

## ⚡ rebase 实用技巧

### 自动 stash（--autostash）

rebase 前要求工作区是干净的，`--autostash` 可以自动帮你 stash/unstash：

```bash
git rebase --autostash main
# 自动：git stash
# 执行 rebase
# 自动：git stash pop
```

### 冲突时的三个选择

rebase 遇到冲突时，你有三个选择：

```bash
# 解决冲突后继续
git add .
git rebase --continue

# 跳过当前这个有冲突的提交（丢弃它）
git rebase --skip

# 完全放弃，恢复到 rebase 之前的状态
git rebase --abort
```

### PR 前的标准整理流程

```bash title="提 PR 前的完整整理流程"
# 1. 同步主线最新代码
git fetch origin
git rebase origin/main          # 变基到最新 main

# 2. 如果有冲突，逐个解决
# git add . && git rebase --continue

# 3. 交互式整理（压缩 WIP 提交）
git rebase -i origin/main       # 整理所有功能提交

# 4. 推送（需要强推，因为改变了历史）
git push --force-with-lease origin feature/login
```

## 🎯 最佳实践总结

```
本地开发时 → rebase 整理提交（提 PR 前） ✅
同步主线到功能分支 → git rebase main    ✅
功能分支合入主线 → merge --no-ff        ✅（保留分支痕迹）
公共分支 → 永远 merge，永远不 rebase    ✅
需要精确控制 → git rebase --onto        ✅
```

下一篇「远程协作」将讲解如何与团队成员通过远程仓库协同工作。

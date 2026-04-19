---
title: 贮藏与清理
---

# 贮藏与清理：stash / clean

**本文你会学到：**

- `git stash` 保存和恢复工作现场
- stash 的各种选项（含未跟踪文件）
- 从 stash 创建新分支
- `git clean` 清理未跟踪的文件和目录

## 🛑 工作被打断怎么办？

场景：你正在 `feature/payment` 分支写代码，写到一半，产品说主线有个紧急 bug 需要立刻修复。

此时问题来了：
- 代码改了一半，不能直接提交（破坏功能）
- 不切换分支的话，又没法去修 bug

**`git stash` 就是你的抽屉**——把当前改动临时塞进去，切换去做别的，回来再取出来。

``` mermaid
graph LR
    W["🛠️ feature/payment\n开发到一半（未完成）"]
    ST["📦 stash 抽屉\n临时保存现场"]
    M["🔥 main 分支\n修紧急 bug"]
    W -->|"git stash push"| ST
    ST -->|"git switch main"| M
    M -->|"修完，git switch back"| ST
    ST -->|"git stash pop"| W
    classDef work fill:transparent,stroke:#539bf5,color:#adbac7,stroke-width:2px
    classDef stash fill:transparent,stroke:#f57c00,color:#adbac7,stroke-width:2px
    classDef hotfix fill:transparent,stroke:#d32f2f,color:#adbac7,stroke-width:2px
    class W work
    class ST stash
    class M hotfix
```

## 📦 基础用法

```bash title="stash 的基本操作"
# 保存工作现场（默认只保存已跟踪文件的修改）
git stash
# 等同于 git stash push

# 保存时附加描述，便于后续识别
git stash push -m "WIP: 支付模块接口对接"

# 查看所有 stash
git stash list
# stash@{0}: On feature/payment: WIP: 支付模块接口对接
# stash@{1}: WIP on main: a1b2c3 fix: 修复登录问题

# 恢复最新的 stash（恢复后 stash 记录仍保留）
git stash apply
git stash apply stash@{1}   # 恢复指定的 stash

# 恢复并删除 stash 记录（最常用）
git stash pop
git stash pop stash@{1}     # 恢复并删除指定的 stash

# 删除 stash
git stash drop stash@{0}    # 删除指定
git stash clear              # 删除所有 stash（慎用！）
```

## 🔍 查看 stash 内容

```bash
# 查看最新 stash 的改动摘要
git stash show
# git stash show -p 查看完整 diff
git stash show -p stash@{0}

# 输出示例：
# src/payment.js | 12 ++++++------
# 1 file changed, 6 insertions(+), 6 deletions(-)
```

## 📁 包含未跟踪文件

默认情况下，`git stash` 只处理**已跟踪文件**的修改，新创建的文件（未 `git add` 的）不会被贮藏：

```bash
# 贮藏时包含未跟踪文件（新建但未 add 的文件）
git stash push -u
# -u 等同于 --include-untracked

# 贮藏时包含所有文件（含 .gitignore 里的）
git stash push -a
# -a 等同于 --all（谨慎：会包含编译产物等）
```

```bash title="实际场景演示"
# 当前状态
git status
# Changes not staged for commit:
#   modified:   src/payment.js   ← 已跟踪但未暂存
# Untracked files:
#   src/payment-helper.js        ← 新建文件，未跟踪

git stash push -u -m "支付模块：接口+辅助工具"

git status
# nothing to commit, working tree clean  ← 干净了！

git switch main                          # 去修紧急 bug
# ... 修好提交 ...
git switch feature/payment

git stash pop                            # 回来取回现场
```

## 🌱 从 stash 创建新分支

如果 stash 存了很久，apply 时可能和当前代码冲突。这时可以从 stash 的基础提交直接创建新分支：

```bash
# 从 stash@{0} 创建新分支，自动 apply 并删除 stash
git stash branch feature/payment-resume stash@{0}

# 等同于：
# git switch -c feature/payment-resume <stash 的 base commit>
# git stash apply stash@{0}
# git stash drop stash@{0}
```

## 🧹 git clean：清理未跟踪文件

`git stash` 处理的是**修改**，而 `git clean` 处理的是**未跟踪的文件和目录**：

```bash
# 预览会被删除哪些文件（dry-run，不真正删除）
git clean -n

# 删除未跟踪文件
git clean -f

# 删除未跟踪文件 + 目录
git clean -fd

# 删除未跟踪文件 + 目录 + .gitignore 中忽略的文件
git clean -fdx

# 交互模式（逐项确认删除）
git clean -i
```

!!! warning "git clean 不可撤销"

    `git clean` 删除的文件**无法通过 git 恢复**，因为它们从未被 Git 跟踪。执行前务必先用 `-n`（dry-run）预览，确认无误再操作。

### clean 的典型场景

```bash title="清理构建产物"
# 你 clone 了一个项目，跑了 npm build，产生了 dist/ 目录
# 想还原到 clone 时的干净状态
git clean -fd           # 删除所有未跟踪的文件和目录

# 完全重置（修改 + 未跟踪文件一起清理）
git reset --hard HEAD
git clean -fd
```

## 对比总结

| | `git stash` | `git clean` |
|-|-------------|-------------|
| 处理对象 | 已跟踪文件的修改 | 未跟踪的文件/目录 |
| 是否可恢复 | ✅ 可以 pop/apply | ❌ 不可恢复 |
| 主要场景 | 临时保存工作现场 | 清理构建产物/临时文件 |
| 快速入口 | `git stash push/pop` | `git clean -fd` |

下一篇「高级工具」将介绍 `git cherry-pick`、`git bisect` 等进阶命令。

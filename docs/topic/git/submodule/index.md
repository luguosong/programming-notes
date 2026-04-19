---
title: 子模块
---

# 子模块：管理多仓库依赖

**本文你会学到：**

- 什么时候需要子模块
- 添加、克隆、更新子模块
- 子模块的常见陷阱
- 删除子模块的正确方式

## 🤔 什么时候需要子模块？

场景：你的前端项目 `webapp` 需要依赖一个内部 UI 组件库 `ui-components`，这个库由另一个团队维护，有自己的 Git 仓库。

**选项对比**：

| 方案 | 优点 | 缺点 |
|------|------|------|
| 直接复制代码 | 简单 | 无法追踪上游更新，重复维护 |
| npm 包 | 版本管理清晰 | 需要发布流程，调试麻烦 |
| Git 子模块 | 锁定特定提交版本，保留完整 Git 历史 | 操作相对复杂 |

**子模块适合**：
- 多项目共享的内部库（但不想走包管理器）
- 需要锁定到特定提交（而不是"最新版本"）
- 有时需要直接修改依赖库代码

## ➕ 添加子模块

```bash title="将 ui-components 作为子模块添加"
# 在 webapp 项目中执行
git submodule add https://github.com/team/ui-components.git
# 默认放在 ./ui-components/ 目录

# 指定放置路径
git submodule add https://github.com/team/ui-components.git libs/ui

# 添加后，会生成两个变化：
# 1. .gitmodules 文件（记录子模块配置）
# 2. 一个指向子模块特定提交的"gitlink"条目

cat .gitmodules
# [submodule "libs/ui"]
#     path = libs/ui
#     url = https://github.com/team/ui-components.git

# 提交这两个变化
git commit -m "chore: 添加 ui-components 子模块"
```

!!! info "子模块记录的是「提交 SHA」不是分支"

    父仓库中记录的是子模块某个**特定提交的 SHA**，而不是分支名。这意味着子模块不会自动跟随上游变化，这是有意为之的——保证可重现构建。

## 📥 克隆含子模块的仓库

```bash title="克隆并初始化子模块"
# 方法一：克隆时一并初始化（推荐）
git clone --recurse-submodules https://github.com/team/webapp.git

# 方法二：克隆后再初始化（分步操作）
git clone https://github.com/team/webapp.git
cd webapp
git submodule init          # 根据 .gitmodules 初始化配置
git submodule update        # 下载子模块内容到对应提交

# 等同于合并写法
git submodule update --init
git submodule update --init --recursive    # 含嵌套子模块时
```

```bash
# 克隆后没执行子模块初始化，libs/ui 目录是空的
ls libs/ui      # （空目录）
git submodule update --init
ls libs/ui      # （有内容了）
```

## 🔄 更新子模块

```bash title="拉取子模块最新内容"
# 进入子模块目录手动更新（精确控制）
cd libs/ui
git fetch
git switch main
git pull        # 更新到上游最新
cd ..

# 回到父仓库，子模块指向的 SHA 已变化
git status
# modified:   libs/ui (new commits)

git add libs/ui
git commit -m "chore: 升级 ui-components 到最新版"

# 一键更新所有子模块到各自远程的最新
git submodule update --remote
git submodule update --remote libs/ui    # 只更新指定子模块
```

```bash title="多人协作：拉取别人更新的子模块引用"
git pull
# 别人更新了子模块的引用，但你本地子模块还在旧提交

git submodule update    # 将子模块同步到父仓库记录的 SHA
```

## 🗑️ 删除子模块

删除子模块比添加复杂，需要四步：

```bash
# 以删除 libs/ui 子模块为例

# 1. 反初始化（解除关联）
git submodule deinit libs/ui

# 2. 从版本控制中移除
git rm libs/ui

# 3. 删除 .git/modules/ 中的缓存（手动）
Remove-Item -Recurse -Force .git/modules/libs/ui   # Windows PowerShell

# 4. 提交变更
git commit -m "chore: 移除 ui-components 子模块"
```

## ⚠️ 常见陷阱

**陷阱1：忘记递归更新**

```bash
git pull     # 更新了父仓库（包括子模块引用变化）
# 但子模块本身还没更新！

git submodule update    # 必须执行这一步
# 或一步到位：
git pull --recurse-submodules
```

**陷阱2：在子模块里的修改丢失**

```bash
# 子模块默认处于"detached HEAD"状态
cd libs/ui
git status
# HEAD detached at a1b2c3d    ← 不在任何分支上！

# 在此状态修改并提交后，切换子模块版本会导致提交丢失
# 正确做法：进入子模块后先切到分支
git switch main
# 再做修改和提交
```

**陷阱3：忘记 push 子模块**

```bash
# 子模块有新提交但没 push，父仓库引用了这个提交
# 其他人 pull 父仓库并更新子模块时会报错

# 检查子模块是否有未推送提交
git push --recurse-submodules=check    # 有未推送则报错
git push --recurse-submodules=on-demand # 自动推送子模块再推父仓库
```

## 有用的配置

```bash
# 让 git status 显示子模块摘要
git config status.submoduleSummary true

# pull 时自动更新子模块
git config submodule.recurse true

# clone 时默认递归
git config --global submodule.recurse true
```

下一篇「Git 钩子」将介绍如何利用钩子自动化 lint 检查、提交信息验证等工作流守卫。

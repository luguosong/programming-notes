---
title: 重写历史
---

# 重写历史：amend / rebase -i / filter-repo

**本文你会学到：**

- `--amend` 修改最新提交
- `git rebase -i` 交互式整理多个提交
- `git filter-repo` 永久清理大文件/敏感信息
- 什么情况下**不该**重写历史

## ⚠️ 黄金法则：已推送的历史不要重写

重写历史的所有操作都是**危险的**，会修改提交 SHA，导致他人的仓库与你的不兼容。

**安全规则**：
- ✅ 只重写**本地尚未 push** 的提交
- ✅ 功能分支（只有你一人在用）可以谨慎重写后强推
- ❌ 永远不要重写 `main` / `master` 等公共分支的历史

## 📝 amend：修改最新提交

```bash title="修改上一条提交"
# 场景1：提交后发现漏了个文件
git add forgotten_file.js
git commit --amend --no-edit     # 追加文件，不修改提交信息

# 场景2：提交信息写错了
git commit --amend -m "fix: 修复购物车数量计算逻辑（非登录问题）"

# 场景3：同时修改内容和信息（会打开编辑器）
git add .
git commit --amend
```

!!! warning "amend 会生成新提交"

    `--amend` 并非"编辑"旧提交，而是用新提交替换旧提交，两者哈希不同。如果已经 push 过，需要 `git push --force-with-lease`。

## 🔧 rebase -i：交互式整理多个提交

当你本地积累了 10 个"WIP"提交，想在 PR 前整理得漂漂亮亮，`git rebase -i` 就是你的瑞士军刀。

``` mermaid
graph LR
    subgraph "整理前（杂乱提交）"
    A1["WIP: 先提交一版"] --> A2["fix typo"] --> A3["WIP: 继续写"] --> A4["加了注释"]
    end
    subgraph "rebase -i 整理后（干净提交）"
    B1["feat: 添加用户注册页面"]
    end
    classDef wip fill:transparent,stroke:#768390,color:#adbac7,stroke-width:1px
    classDef clean fill:transparent,stroke:#388e3c,color:#adbac7,stroke-width:2px
    class A1,A2,A3,A4 wip
    class B1 clean
```

```bash
# 整理最近 5 个提交
git rebase -i HEAD~5

# 整理从某个提交（不含）之后的所有提交
git rebase -i abc1234
```

执行后会打开编辑器，列出每个提交及其操作指令：

```
pick a1b2c3d feat: 添加用户注册页面
pick e4f5g6h fix: 修复表单验证
pick 7i8j9k0 WIP: 还没写完
pick b3c4a5b WIP: 继续写
pick d6e7f8a fix: typo
```

**修改指令后保存，Git 就会按你的意思执行**：

```
pick   a1b2c3d feat: 添加用户注册页面   ← 保留不变
squash e4f5g6h fix: 修复表单验证        ← 合并到上一个
squash 7i8j9k0 WIP: 还没写完           ← 合并到上一个
squash b3c4a5b WIP: 继续写             ← 合并到上一个
fixup  d6e7f8a fix: typo               ← 合并但丢弃此条信息
```

### 常用操作指令表

| 指令 | 缩写 | 效果 |
|------|------|------|
| `pick` | `p` | 保留提交不变 |
| `reword` | `r` | 保留提交，修改提交信息 |
| `squash` | `s` | 合并到上一个提交，保留此条提交信息 |
| `fixup` | `f` | 合并到上一个提交，丢弃此条提交信息 |
| `drop` | `d` | 删除这个提交 |
| `edit` | `e` | 暂停在此提交，允许修改文件后 `--amend` |

```bash title="rebase -i 中暂停修改文件（edit）"
# 编辑器中把某提交改为 edit
# Git 暂停，让你手动修改
vim src/auth.js    # 修改文件
git add .
git commit --amend # 修改该提交
git rebase --continue
```

## 🧹 filter-repo：永久清除大文件/敏感信息

场景：你不小心 commit 了一个 500MB 的视频文件，或者提交了包含密码的配置文件。即使后来删除，Git 历史中仍然保留着，仓库体积不会减小。

`git filter-repo` 是 Git 官方推荐的历史重写工具（替代已废弃的 `filter-branch`）。

```bash title="安装 git-filter-repo"
pip install git-filter-repo
# 或 brew install git-filter-repo（macOS）
```

```bash title="清除特定文件（所有历史中）"
# 永久删除 secrets.env（从所有提交中抹除）
git filter-repo --path secrets.env --invert-paths

# 清除整个目录
git filter-repo --path build/ --invert-paths

# 清除所有大于 10MB 的文件
git filter-repo --strip-blobs-bigger-than 10M
```

```bash title="操作后必须强推"
# filter-repo 完成后，远程历史已经不同步
# 需要强制推送覆盖
git push --force-with-lease origin main

# 让所有协作者重新 clone（他们的本地仓库已失效）
```

!!! danger "filter-repo 破坏性极强"

    1. 执行前**备份整个仓库**（`cp -r repo repo.bak`）
    2. **通知所有协作者**，他们需要重新 clone，不能简单 pull
    3. 如果是 GitHub/GitLab，还需要清理服务端的缓存（联系客服或使用平台的"清理历史"功能）
    4. 所有**已发出的 PR** 中的提交 SHA 都会失效

### 替换敏感信息（不删除整个文件）

```bash
# 将历史中所有的旧密码替换为占位符
echo "s/old_password_here/REDACTED/g" > replacements.txt
git filter-repo --replace-text replacements.txt
```

## 小结

| 场景 | 推荐工具 |
|------|---------|
| 修改上一条提交的信息或内容 | `git commit --amend` |
| 整理 PR 前的本地提交（压缩/排序）| `git rebase -i HEAD~N` |
| 永久清除大文件或密码 | `git filter-repo` |
| 误操作后找回历史 | `git reflog`（见「高级工具」）|

**再次强调**：重写历史只适合未推送的本地提交，或在单人独占的功能分支上。

下一篇「子模块」将讲解如何在一个 Git 仓库中管理另一个仓库作为依赖。

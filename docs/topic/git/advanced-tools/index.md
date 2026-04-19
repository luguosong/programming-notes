---
title: 高级工具
---

# 高级工具：cherry-pick / bisect / tag / reflog

**本文你会学到：**

- `git cherry-pick`：摘取特定提交
- `git bisect`：二分法定位 bug
- `git tag`：打标签标记版本
- `git reflog`：恢复"消失"的提交

## 🍒 cherry-pick：我只要这一个提交

场景：`develop` 分支有个紧急修复提交，但你不想合并整个 `develop`，只想把那一个 fix 搬到 `main`。

``` mermaid
graph LR
    subgraph develop
    D1["A"] --> D2["B\nfix: 修购物车 bug"] --> D3["C\nfeat: 推荐算法（不要）"]
    end
    subgraph main
    M1["E（HEAD）"] -->|"cherry-pick B"| M2["B'\n内容=B，新 SHA"]
    end
    classDef dev fill:transparent,stroke:#f57c00,color:#adbac7,stroke-width:1px
    classDef main fill:transparent,stroke:#539bf5,color:#adbac7,stroke-width:2px
    class D1,D2,D3 dev
    class M1,M2 main
```

```bash title="cherry-pick 基础用法"
# 查看 develop 分支的提交，找到那个 fix
git log develop --oneline
# a1b2c3d (develop) fix: 修复购物车数量计算错误  ← 我要这个
# e4f5g6h feat: 全新商品推荐算法（暂时不想要）
# 7i8j9k0 feat: 用户行为统计

# 切到 main，cherry-pick 那个 fix 提交
git switch main
git cherry-pick a1b2c3d

# cherry-pick 多个提交（顺序执行）
git cherry-pick a1b2c3d e4f5g6h

# cherry-pick 一个区间（不含 7i8j9k0，含 a1b2c3d）
git cherry-pick 7i8j9k0..a1b2c3d

# cherry-pick 但暂不提交（先让你检查）
git cherry-pick -n a1b2c3d
```

### cherry-pick 冲突处理

```bash
git cherry-pick a1b2c3d
# error: could not apply a1b2c3d... fix: 购物车计算
# hint: After resolving the conflicts, mark them with
#       "git cherry-pick --continue"

# 解决冲突后
git add .
git cherry-pick --continue

# 放弃这次 cherry-pick
git cherry-pick --abort
```

!!! info "cherry-pick 会生成新的提交哈希"

    cherry-pick 后，新提交与原提交**内容相同但哈希不同**（因为父提交不同）。历史中会看到两条内容相同的提交，这是正常的。

## 🔍 bisect：二分法找 bug

场景：昨天测试还好，今天发现有个严重 bug，但中间有 50 个提交，不知道是哪个提交引入的。

`git bisect` 用**二分搜索**自动定位问题提交，O(log n) 效率，50 个提交只需测试约 6 次。

```bash title="bisect 手动模式"
git bisect start

# 标记：当前是"有 bug 的"
git bisect bad

# 标记：某个已知"没问题"的旧提交
git bisect good v1.0.0
# 或者：git bisect good abc1234

# Git 自动切到中间那个提交，你测试后告诉它结果
# ... 测试 ...
git bisect good  # 这个提交没问题，bug 在更新的地方
# 或
git bisect bad   # 这个提交有 bug，问题在更旧的地方

# Git 继续二分，最终输出：
# b3f8a12 is the first bad commit
# Author: Zhang San ...

# 结束 bisect，回到原始 HEAD
git bisect reset
```

```bash title="bisect 自动模式（推荐）"
# 准备一个测试脚本（退出码 0=正常，非0=有bug）
cat run_test.sh
# #!/bin/bash
# node test/regression.js
# exit $?

git bisect start
git bisect bad HEAD
git bisect good v1.0.0

# 全自动！Git 自动切提交、运行脚本、标记结果
git bisect run bash run_test.sh

# 输出：
# b3f8a12 is the first bad commit
git bisect reset
```

## 🏷️ tag：给版本打标签

提交 SHA（`a1b2c3d`）难以记忆，`git tag` 允许给重要节点贴上有意义的名字（如 `v1.0.0`）。

```bash title="轻量标签（lightweight）"
# 轻量标签：只是一个指向提交的指针
git tag v1.0.0
git tag v1.0.0 a1b2c3d    # 给历史提交打标签

# 查看所有标签
git tag
git tag -l "v1.*"         # 筛选
```

```bash title="附注标签（annotated）—— 推荐正式发布使用"
# 附注标签：包含打标签人信息、时间、GPG 签名等元数据
git tag -a v1.0.0 -m "正式发布 1.0.0 版本：支持用户登录和商品浏览"

# 查看标签详情（附注标签才有 Tagger 信息）
git show v1.0.0
```

```bash title="推送和删除标签"
# 默认 push 不推送标签，需要显式操作
git push origin v1.0.0        # 推送单个标签
git push origin --tags        # 推送所有标签

# 删除本地标签
git tag -d v1.0.0

# 删除远程标签
git push origin --delete v1.0.0
git push origin :refs/tags/v1.0.0    # 旧写法
```

### 标签 vs 分支

| | 标签 | 分支 |
|-|------|------|
| 是否移动 | ❌ 固定不动 | ✅ 随提交移动 |
| 用途 | 标记发布节点（v1.0.0）| 记录当前工作线 |
| 删除影响 | 无影响（只删标记）| 可能丢失提交 |

## 🔮 reflog：找回"丢失"的提交

场景：你执行了 `git reset --hard` 回到了昨天，但后悔了，想找回刚才的提交。普通 `git log` 已经看不到那些提交了……

**`git reflog`** 记录了 HEAD 每次移动的完整历史，是你的终极后悔药：

```bash
git reflog
# 输出：
# a1b2c3d (HEAD -> main) HEAD@{0}: reset: moving to a1b2c3d
# f7e8d9c HEAD@{1}: commit: feat: 购物车功能        ← 我要这个！
# b3c4a5b HEAD@{2}: commit: fix: 修复登录验证
# ...

# 找到目标提交后，直接恢复
git reset --hard f7e8d9c
# 或者创建新分支保留它
git switch -c recovery f7e8d9c
```

!!! tip "reflog 的保留期限"

    reflog 条目默认保留 **90 天**，超时的孤立提交会被 `git gc` 清理。在此期间你几乎可以恢复任何"丢失"的工作。

```bash
# reflog 中的各种操作记录
git reflog --all              # 查看所有引用的变化（含分支、tag）
git reflog show feature/login # 只看某个分支的变化

# 按时间过滤
git reflog --since="2 hours ago"
```

## 📌 语义化版本与 git describe

### 语义化版本（SemVer）规范

发版时推荐使用 [Semantic Versioning](https://semver.org/lang/zh-CN/) 格式：`vMAJOR.MINOR.PATCH`

```
v1.2.3
│ │ └── PATCH：向后兼容的 bug 修复
│ └──── MINOR：向后兼容的新功能
└────── MAJOR：不兼容的 API 变更
```

```bash title="语义化版本打标签"
# bug 修复发版
git tag -a v1.2.4 -m "fix: 修复购物车清空时的 NullPointerException"

# 新功能发版
git tag -a v1.3.0 -m "feat: 添加用户推荐系统、优化搜索性能"

# 不兼容变更（API 重构）
git tag -a v2.0.0 -m "breaking: 重构认证模块 API，移除旧版 /api/v1/auth 端点"

# 预发布版本
git tag -a v2.0.0-beta.1 -m "beta: v2.0 第一个公测版"
git tag -a v2.0.0-rc.1   -m "rc: 发布候选版1"
```

### git describe：自动生成版本描述

`git describe` 基于最近的 tag 自动生成一个包含距离信息的版本字符串，常用于 CI/CD 场景：

```bash
git describe --tags
# v1.2.3-5-ga1b2c3d
#   │    │  └──── 当前提交的短 SHA（g 前缀表示 git）
#   │    └─────── 距离 v1.2.3 之后有 5 个提交
#   └──────────── 最近的 tag

# 如果当前 HEAD 就是 tag 节点，直接返回 tag 名
git describe --tags
# v1.2.3

# 用在构建脚本中自动生成版本号
VERSION=$(git describe --tags --always)
echo "构建版本：$VERSION"
# 构建版本：v1.2.3-5-ga1b2c3d
```

## 🍒 cherry-pick 高级场景：发版后的 backport

**场景**：`main` 分支发现了安全漏洞，需要同时修复到 `v1.x` 和 `v2.x` 两个支持中的旧版本分支。

```bash title="backport 流程"
# main 分支有安全修复提交：security-fix-sha

# backport 到 v1.x 分支
git switch v1.x
git cherry-pick security-fix-sha
# 如果有冲突（v1.x 的代码结构不同）：
git add .
git cherry-pick --continue
git push origin v1.x

# backport 到 v2.x 分支
git switch v2.x
git cherry-pick security-fix-sha
git push origin v2.x

# 在每个分支打 patch 版本 tag
git switch v1.x && git tag -a v1.8.15 -m "security: 修复 CVE-2024-xxxx"
git switch v2.x && git tag -a v2.3.7  -m "security: 修复 CVE-2024-xxxx"
git push origin --tags
```

## 🔍 bisect 进阶：跳过无法测试的提交

有时候某些提交因为编译错误或环境问题无法测试，可以用 `skip` 跳过：

```bash
git bisect start
git bisect bad HEAD
git bisect good v1.0.0

# 当前提交无法编译，跳过它
git bisect skip

# 跳过一段范围（这几个提交都有编译问题）
git bisect skip abc1234..def5678

# bisect 会绕开被跳过的提交，继续缩小范围
# 最终可能输出：
# There are only 'skip'ped commits left to test.
# The first bad commit could be any of:
# abc1234 def5678 ...
# We cannot bisect more!
```

## 小结

| 命令 | 使用场景 |
|------|---------|
| `git cherry-pick <sha>` | 从其他分支摘取特定提交 |
| `git cherry-pick --abort` | 放弃 cherry-pick |
| `git bisect run <script>` | 自动二分定位 bug 提交 |
| `git bisect skip` | 跳过无法测试的提交 |
| `git tag -a v1.0.0 -m "..."` | 打附注标签（正式发版）|
| `git describe --tags` | 自动生成基于 tag 的版本号 |
| `git reflog` | 找回 reset/drop 的"丢失"提交 |

下一篇「重写历史」将深入介绍 `filter-repo` 清理大文件等更激进的历史重写操作。

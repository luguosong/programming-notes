---
title: Git 起步
---

# Git 起步：版本控制与核心思想

**本文你会学到：**

- 为什么需要版本控制，三种控制系统的演进
- Git 的核心哲学：快照而非差异
- 三个区域（工作区 / 暂存区 / 仓库）与文件的三种状态
- 安装 Git 并完成初始配置

## 🤔 为什么需要版本控制？

想象你正在写一篇毕业论文，每次修改前你都会手动复制一份：
`论文_v1.docx` → `论文_v2.docx` → `论文_最终版.docx` → `论文_最最终版.docx` ...

这其实就是最原始的"版本控制"——你本能地意识到**需要保留历史记录**。版本控制系统（VCS）把这件事自动化了。

### 三种版本控制系统的演进

``` mermaid
graph TD
    A["🖥️ 本地 VCS<br/>（只在自己电脑上）"] -->|"多人协作困难"| B["🌐 集中式 VCS<br/>CVS / Subversion<br/>（中央服务器）"]
    B -->|"服务器宕机=全体停工"| C["🔗 分布式 VCS<br/>Git / Mercurial<br/>（每人都有完整仓库）"]
    classDef sys fill:transparent,stroke:#768390,color:#adbac7,stroke-width:1px
    classDef git fill:transparent,stroke:#f57c00,color:#adbac7,stroke-width:2px
    class A,B sys
    class C git
```

**集中式 VCS 的致命缺陷**：中央服务器宕机，所有人都无法提交代码；服务器硬盘损坏，历史记录全部丢失。

Git（分布式 VCS）的解决方案：**每个人的本地都是完整仓库的镜像**——即使服务器挂了，你也可以继续提交，之后再同步。

## 🧠 Git 的核心哲学：快照，而非差异

这是理解 Git 最关键的一步。其他大多数版本控制系统（如 SVN）记录的是文件随时间积累的**差异**（delta）：

```
版本1:  文件A
版本2:  文件A + Δ1  （记录改了什么）
版本3:  文件A + Δ1 + Δ2
```

**Git 完全不同**——Git 记录的是项目在某个时刻的**完整快照**：

```
提交1:  [文件A v1] [文件B v1]
提交2:  [文件A v2] [文件B v1*]   ← B未修改，只存一个指向v1的链接
提交3:  [文件A v2*] [文件B v2]
```

!!! tip "快照的好处"

    - `切换分支`极快——不需要算差异，直接换快照
    - `任何版本`都可独立恢复，不依赖差异链条的完整性
    - `离线工作`完全没有限制——所有历史就在本地

## 📦 三个区域，文件的一生

Git 把你的工作分成三个区域。理解这三个区域，是掌握所有 Git 命令的基础。

``` mermaid
graph LR
    W["🗂️ 工作目录<br/>Working Directory"]
    S["📋 暂存区<br/>Staging Area / Index"]
    R["🗄️ 本地仓库<br/>Repository (.git)"]
    W -->|"git add"| S
    S -->|"git commit"| R
    R -->|"git checkout"| W
    classDef area fill:transparent,stroke:#539bf5,color:#adbac7,stroke-width:2px
    class W,S,R area
```

- `工作目录（Working Directory）`：你实际编辑文件的地方，就是你在文件管理器看到的那些文件
- `暂存区（Staging Area）`：一个「候车室」，决定哪些修改要纳入下次提交。有时也叫 `Index`
- `本地仓库（Repository）`：`.git` 目录，Git 真正保存所有历史快照的地方

### 文件的四种状态

```
未跟踪（Untracked）  ─── git add ───►  已暂存（Staged）
                                             │
已跟踪且未修改                               git commit
（Unmodified）  ◄── git commit ──────────────┘
     │
   编辑
     ▼
已修改（Modified）  ─── git add ───►  已暂存（Staged）
```

| 状态 | 含义 | 如何到这里 |
|------|------|-----------|
| `Untracked`（未跟踪） | 新文件，Git 从未见过 | 新建文件 |
| `Unmodified`（未修改） | 已提交且没有变化 | `git commit` 之后 |
| `Modified`（已修改） | 对已跟踪文件做了修改 | 编辑文件 |
| `Staged`（已暂存） | 修改已放入暂存区，等待提交 | `git add` 之后 |

## ⚙️ 安装与初始配置

### 安装 Git

=== "macOS"

    ``` bash
    brew install git
    ```

=== "Ubuntu / Debian"

    ``` bash
    sudo apt install git
    ```

=== "Windows"

    从 [git-scm.com](https://git-scm.com/download/win) 下载安装包，一路 Next 即可。

验证安装：

``` bash
git --version
# 输出示例：git version 2.49.0
```

### 三级配置系统

Git 有三个配置层级，**更细的范围优先级更高**：

| 配置文件 | 作用范围 | 命令选项 |
|---------|---------|---------|
| `/etc/gitconfig` | 系统所有用户 | `--system` |
| `~/.gitconfig` | 当前用户所有仓库 | `--global` |
| `.git/config` | 当前仓库 | `--local`（默认）|

### 必做的初始配置

```bash title="首次使用 Git 必须配置姓名和邮箱"
# 配置用户名（会出现在每次提交记录中）
git config --global user.name "Your Name"

# 配置邮箱
git config --global user.email "you@example.com"

# 配置默认分支名为 main（现代做法，旧版本默认是 master）
git config --global init.defaultBranch main

# 配置默认编辑器（按个人喜好）
git config --global core.editor "code --wait"   # VS Code
# git config --global core.editor vim           # Vim
```

!!! warning "为什么 --global？"

    加了 `--global` 意味着「对我这台电脑上的所有 Git 仓库都生效」。如果你在公司和私人项目用不同邮箱，可以在具体项目目录里用 `--local` 覆盖。

### 常用配置速查

```bash title="查看当前所有配置及其来源"
git config --list --show-origin

# 查看单个配置项
git config user.name

# 配置 Git 命令别名（偷懒神器）
git config --global alias.st status     # git st → git status
git config --global alias.co checkout   # git co → git checkout
git config --global alias.br branch     # git br → git branch
git config --global alias.lg "log --oneline --graph --all"  # 好看的日志

# 配置换行符处理（跨平台团队推荐）
git config --global core.autocrlf input   # macOS/Linux
git config --global core.autocrlf true    # Windows
```

### 获取帮助

```bash
# 完整手册（会打开浏览器）
git help config

# 命令行快速参考（简洁版）
git config -h
```

## 小结

- Git 是**分布式**版本控制系统，每台机器都有完整历史
- Git 存储的是**快照**（snapshot），而不是文件差异（delta）
- 核心三区域：`工作目录` → `暂存区` → `本地仓库`
- 初始配置：`user.name` 和 `user.email` 是必须设置的

下一篇「基础操作」将带你完成第一次 `git init` → `git add` → `git commit` 的完整工作流。

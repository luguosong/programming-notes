---
layout: note
title: Git
create_time: 2023/6/9
---

# 各种版本控制系统

- 本地版本控制系统
  - `RCS`: Revision Control System，最早的本地版本控制系统，只能单个文件进行版本控制。 RCS 的工作原理是在硬盘上保存补
    丁集（补丁是指文件修订前后的变化）；通过应用所有的补丁，可以重新计算出各个版本的文件内容。
- 集中化版本控制系统（Centralized Version Control Systems，简称 CVCS）
  - `CVS`: Concurrent Versions System，集中化的版本控制系统，有一个单一的集中管理的服务器，保存所有文件的修订版本，协同工作的人们都通过客户端连接到这台服务器，取出最新的文件或者提交更新。缺点是服务器单点故障，如果服务器出现故障，所有人都无法提交更新。
  - `Subversion`: 集中化的版本控制系统，是CVS的替代品，解决了CVS的一些问题，但是仍然存在服务器单点故障的问题。
  - `Perforce`: 集中化的版本控制系统，解决了服务器单点故障的问题，但是需要购买。
- 分布式版本控制系统（（Distributed Version Control System，简称 DVCS））
  - `Git`: 分布式版本控制系统，每个人的电脑上都是一个完整的版本库，不需要联网就可以进行版本控制，可以提交更新，也可以从其他人那里获取更新。缺点是每个人的电脑上都有一个完整的版本库，占用空间大。
  - `Mercurial`: 分布式版本控制系统，和Git类似，但是没有Git流行。
  - `Darcs`: 分布式版本控制系统，和Git类似，但是没有Git流行。

# 历史



# 安装

## Linux安装

```shell
apt install git-all
```

## Windows

直接去官网下载安装：

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202306280959676-git-windows%E5%AE%89%E8%A3%85%E5%8C%85%E4%B8%8B%E8%BD%BD.png)

# 帮助手册

```shell
git help <options>
```

# Git初始化配置

## 配置查看

```shell
# 查看系统配置
git config --list --system

# 查看当前用户配置
git config --list --global

# 查看当前仓库配置
git config --list --local

# 查看所有的配置以及它们所在的文件
git config --list --show-origin
```

## 删除配置

```shell
git config --global --unset <key>
```

## 用户信息配置

```shell
git config --global user.name "luguosong"
git config --global user.email 1054595718@qq.com
```

## 文本编辑器

```shell
# 配置emacs
git config --global core.editor emacs

# 在Windows中配置Notepad++
git config --global core.editor "'C:/Program Files/Notepad++/notepad++.exe' -multiInst -notabbar -nosession -noPlugin"
```


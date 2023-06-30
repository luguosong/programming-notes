---
layout: note
title: 起步
nav_order: 10
parent: Git
create_time: 2023/6/9
---

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

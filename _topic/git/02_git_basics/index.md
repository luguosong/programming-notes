---
layout: note
title: 基础
nav_order: 20
parent: Git
create_time: 2023/6/28
---

# 初始化Git仓库

```shell
git init
```

# 克隆现有仓库

```shell
git clone https://github.com/libgit2/libgit2 mylibgit
```

# 检查当前文件状态

```shell
# 查看文件状态
git status

# 简化输出
git status -s
# 或
git status --short
```

# 跟踪新文件

```shell
git add <文件名>
```

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202306281356018-git-add.png)

# 暂存已修改的文件

与跟踪新文件命令

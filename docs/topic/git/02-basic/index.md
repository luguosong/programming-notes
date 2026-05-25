---
title: 基本语法
---

# 基本语法

## 创建新仓库

### git init-初始化仓库

```shell
git init
```

### git clone-从远程拉取仓库

```shell
git clone <repository>

# 示例
git clone https://github.com/torvalds/linux.git
```

## 三个区域

### 工作区

对项目的某个版本独立提取出来的内容。 这些从 Git 仓库的压缩数据库中提取出来的文件，放在磁盘上供你使用或修改。

位置：.git所在目录

### 暂存区

一个文件，保存了下次将要提交的文件列表信息，一般在 Git 仓库目录中。 按照 Git 的术语叫做“索引”，不过一般说法还是叫“暂存区”。

位置：.git/index

### 本地仓库

Git 用来保存项目的元数据和对象数据库的地方。 这是 Git 中最重要的部分，从其它计算机克隆仓库时，复制的就是这里的数据。

位置：.git/objects

## 文件状态

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260509171158486.png){ loading=lazy }
  <figcaption></figcaption>
</figure>

### 未跟踪（Untracked）

文件存在于工作区中，但 Git 尚未跟踪它——不在任何版本快照中，也不在暂存区中。新建的文件默认处于此状态，执行 `git add`
后进入「已暂存」。

#### git add-将未跟踪的文件添加到暂存区

```shell
git add <file>
```

### 未修改（Unmodified）

文件已被 Git 跟踪，且当前内容与仓库中最新提交的版本完全一致。此时文件无需任何操作，是最"安静"的状态。

### 已修改（Modified）

文件内容发生了变化，但修改还未添加到暂存区。此时改动只存在于工作区中，不会被 `git commit` 提交。需要通过 `git add`
将其转为「已暂存」。

#### git add-将已修改的文件添加到暂存区

```shell
git add <file>
```

add后，文件状态从「已修改」变为「已暂存」，表示这些改动已经准备好被提交了。

### 已暂存（Staged）

修改已通过 `git add` 添加到暂存区，标记为下次提交的一部分。暂存区让你精确选择哪些改动进入下一次提交，而非一次性提交工作区的所有修改。

#### git commit-提交已暂存文件到本地仓库

```shell
git commit -m "commit message"
```

提交后，暂存区的内容会被清空，文件状态回到「未修改」，等待下一次修改。

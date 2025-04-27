# 基础知识

## 版本控制系统分类

### 本地版本控制系统

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202412252139936.png){ loading=lazy }
  <figcaption>本地版本控制.</figcaption>
</figure>

- `RCS`:在硬盘上保存补丁集（补丁是指文件修订前后的变化）；通过应用所有的补丁，可以重新计算出各个版本的文件内容。

### 集中化的版本控制系统

相对于本地版本控制系统，集中化的版本控制系统（Centralized Version Control Systems，简称 `CVCS`）
`让在不同系统上的开发者协同工作`。

有一个单一的集中管理的服务器，保存所有文件的修订版本，而协同工作的人们都通过客户端连到这台服务器，取出最新的文件或者提交更新。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202412261724074.png){ loading=lazy }
  <figcaption>集中化的版本控制</figcaption>
</figure>

优点：

- 每个人都可以在一定程度上看到项目中的其他人正在做些什么。
- 管理员也可以轻松掌控每个开发者的权限
- 管理一个 CVCS 要远比在各个客户端上维护本地数据库来得轻松容易。

缺点：

- 中央服务器的单点故障。如果宕机一小时，那么在这一小时内，谁都无法提交更新，也就无法协同工作。
- 如果中心数据库所在的磁盘发生损坏，又没有做恰当备份，你将丢失所有数据（包括项目的整个变更历史），只剩下人们在各自机器上保留的单独快照。

相关系统：

- `CVS`
- `Subversion(SVN)`
- `Perforce`

### 分布式版本控制系统

分布式版本控制系统（Distributed Version Control System，简称 `DVCS`）中，客户端并不只提取最新版本的文件快照，
而是把代码仓库完整地镜像下来，包括完整的历史记录。

这么一来，任何一处协同工作用的服务器发生故障，事后都可以用任何一个镜像出来的本地仓库恢复。 因为每一次的克隆操作，实际上都是一次对代码仓库的完整备份。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202412261754847.png){ loading=lazy }
  <figcaption>分布式版本控制</figcaption>
</figure>

相关系统：

- `Git`
- `Mercurial`
- `Darcs`

## Git历史

- Linux 内核开源项目有着为数众多的参与者。 绝大多数的 Linux 内核维护工作都花在了提交补丁和保存归档的繁琐事务上（`1991－2002年间`）。
- 到 `2002 年`，整个项目组开始启用一个专有的分布式版本控制系统`BitKeeper`来管理和维护代码。
- `2005年`，安德鲁·垂鸠（Andrew Tridgell）写了一个简单程序，可以连接BitKeeper的仓库，BitKeeper著作权拥有者拉里·麦沃伊认为安德鲁·垂鸠对BitKeeper内部使用的协议进行逆向工程，决定收回无偿使用BitKeeper的许可。Linux内核开发团队与BitMover公司进行磋商，但无法解决他们之间的歧见。林纳斯·托瓦兹决定自行开发版本控制系统替代BitKeeper，以十天的时间编写出git第一个版本。


## Git是什么？

### 直接记录快照，而非差异比较

大部分版本控制系统（CVS、Subversion、Perforce 等等）存储的信息看作是一组基本文件和每个文件随时间逐步累积的差异 （它们通常称作 `基于差异`（delta-based）的版本控制）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202412262233902.png){ loading=lazy }
  <figcaption>存储每个文件与初始版本的差异.</figcaption>
</figure>

Git则不是这种解决方案

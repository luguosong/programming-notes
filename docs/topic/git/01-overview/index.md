---
title: 概述
---

# 概述

## Git 历史

- 绝大多数的 Linux 内核维护工作都花在了提交补丁和保存归档的繁琐事务上（1991－2002年间）
- 到 2002 年，整个项目组开始启用一个专有的分布式版本控制系统BitKeeper 来管理和维护代码。
- 到了 2005 年，开发 BitKeeper 的商业公司同 Linux 内核开源社区的合作关系结束，他们收回了 Linux 内核社区免费使用 BitKeeper
  的权力。
- 林纳斯·托瓦兹创作，于2005年以GPL许可协议发布Git。

## Git 的核心特点

- 直接记录快照，而非差异比较

当你提交更新或保存项目状态时，它基本上就会对当时的全部文件创建一个快照并保存这个快照的索引。

如果文件没有修改，Git 不再重新存储该文件，而是只保留一个链接指向之前存储的文件。

Git 对待数据更像是一个快照流

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260509170633392.png){ loading=lazy }
  <figcaption></figcaption>
</figure>

- 近乎所有操作都是本地执行

- Git 保证完整性

对内容计算哈希（SHA-1），以哈希作为唯一标识（内容寻址），对象不可篡改



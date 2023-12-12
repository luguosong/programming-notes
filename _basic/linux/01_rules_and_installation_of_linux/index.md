---
layout: note
title: Linux的规则与安装
nav_order: 10
parent: linux
create_time: 2023/11/29
---

# 操作系统概述

操作系统的功能：
- 有效率的控制这些硬件资源的分配
- 提供电脑运作所需要的功能(如网路功能)
- 提供一整组系统调用介面来给软件设计师开发用

Linux操作系统：
- `内核`：内核与`硬件`的关系非常的强烈，内核是操作系统的核心，它负责管理系统的所有资源，包括CPU、内存、硬盘、外设等等。
- `系统调用`：系统调用是内核提供给应用程序的接口，应用程序可以通过系统调用来使用内核的功能。

{: .warning}
> 由于不同的硬件他的功能函数并不相同,所以同一套操作系统是无法在不同的硬件平台上面运作的！

# 历史

- `1960年`代初期麻省理工学院(MIT)发展了`相容分时系统`(Compatible Time-Sharing System, CTSS),提供数个终端机(terminal)以连线进入主机
- `1965年`前后， 由贝尔实验室(Bell)、麻省理工学院(MIT)及奇异公司(GE, 或称为通用电器)共同发起了`Multics`的计划。目的是想要让大型主机可以达成提供300个以上的终端机连线使用的目标。
- `1969年`前后，计划进度落后，资金也短缺，`贝尔实验室(Bell)退出了Multics计划`，但是麻省理工学院(MIT)及奇异公司(GE)仍然继续进行Multics的开发。
- 在`1969年`八月份左右,贝尔研究室中原本参与Multics计划的`Ken Thompson`,经过四个星期的奋斗，以`组合语言(Assembler)`将Multics的核心功能移植到`PDP-7`上面，这个系统就是`Unics`(UNIX的原型，当时尚未有Unix的名称)。
- 因为`PDP-7`的性能不佳，`肯·汤普逊`与`丹尼斯·里奇`决定把第一版UNIX移植到`PDP-11/20`的机器上，开发第二版UNIX。在性能提升后，真正可以提供多人同时使用，布莱恩·柯林汉提议将它的名称改为`UNIX`。
- `1971年`,汤普逊和里奇共同发明了C语言。
- `1973年`汤普逊和里奇用C语言重写了Unix，形成`第三版UNIX`。
- 柏克莱大学的Bill Joy在`1977年`发表了`BSD Unix`。
- `1979年`时，AT&T推出System V 第七版Unix。可以支援x86架构的个人电脑系统。并提出`不可对学生提供源代码`的严格限制。
- 谭宁邦在`1984年`开始撰写`Minix`核心程序，为了避免版权纠纷，谭宁邦完全不看Unix核心源代码！并且强调他的Minix必须能够与Unix相容才行！到了`1986年`终于完成。Minix源码他并不是完全免费的，无法在网路上提供下载！必须要透过磁片/磁带购买才行。
- `1984年`，史托曼开始GNU计划， 这个计划的目的是：建立一个自由、开放的Unix操作系统(Free Unix)。
- `1985年`,史托曼草拟了有名的`通用公共许可证(General Public License, GPL)`。
- `1991年`，林纳斯·托瓦兹在赫尔辛基大学上学时，对操作系统很好奇。他对MINIX只允许在教育上使用很不满（在当时MINIX不允许被用作任何商业使用），于是他便开始写他自己的操作系统，这就是后来的Linux内核。

# Unix和Linux版本

- Unix
  - System V
- BSD
- Minix
- Linux
  - RPM 软件管理
    - Red Hat
    - Fedora
    - SuSE
  - DPKG 软件管理
    - Debian
    - Ubuntu
    - B2D

# Vmware安装Linux

## Vmware开机自启动

- 从服务列表中，右键单击 `VMware 自动启动服务`，然后单击属性。在 VMware 自动启动服务属性对话框的常规选项卡上，选择启动类型为自动，然后单击确定。

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202311232200137.png)

- 配置自动启动虚拟机

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202311232202928.png)

## Vmware固定ip

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202311291627692.png)

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202311291628336.png)

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202311291629753.png)

Kali Linux打开`/etc/network/interfaces`文件。

CentOS打开`/etc/sysconfig/network-scripts/ifcfg-eth0`文件。

```bash

# 这是Kali Linux需要增加的配置文件

auto eth0
# 配置eth0使用默认的静态地址
iface eth0 inet static
# 设置eth0的IP地址
address 192.168.40.129
# 配置eth0的子网掩码
netmask 255.255.255.0
# 配置当前主机的默认网关
gateway 192.168.40.2
```
# 目录树

- `/`：根目录
  - `/bin`：存放二进制可执行文件
  - `/boot`：存放启动 Linux 时使用的核心文件
  - `/dev`：存放设备文件
  - `/etc`：存放系统配置文件和子目录
  - `/home`：普通用户的主目录
  - `/root`：系统管理员的用户主目录
  - `/lib`：存放程序运行所需的共享库及内核模块
  - `/lost+found`：非正常关机后留下的文件
  - `/media`：自动识别设备后挂载的目录
  - `/mnt`：临时挂载其他文件系统的目录
  - `/opt`：额外安装软件的目录
  - `/proc`：系统内存映射，用于获取系统信息
  - `/selinux`：存放 SELinux 相关文件（Redhat/CentOS 特有）
  - `/srv`：存放服务启动后需要提取的数据
  - `/sys`：Linux 2.6 内核的 sysfs 文件系统，反映内核设备树
  - `/tmp`：存放临时文件的目录
  - `/sbin`：存放系统管理员使用的系统管理程序
  - `/usr`：存放用户应用程序和文件的目录
    - `/usr/bin`：系统用户使用的应用程序
    - `/usr/sbin`：超级用户使用的高级管理程序和系统守护程序
  - `/var`：存放经常被修改的文件，包括各种日志文件


# Linux线上求助

## --help

```shell
# 通过--help进行查看
date --help
```

## man

```shell
# 使用man
# manual(操作说明)的简写
man date
```

设置man命令为中文：

```shell
sudo apt install manpages-zh
```

## info

```shell
info date
```



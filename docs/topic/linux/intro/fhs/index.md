---
title: 目录结构（FHS）
---

# 目录结构（FHS）

**本文你会学到**：

- 为什么 Linux 需要统一的目录规范（FHS 标准的由来）
- 目录树的根（`/`）与挂载的关系
- 每个重要目录的职责和典型内容
- Debian/Ubuntu 与 Red Hat/RHEL 在配置文件路径上的差异

## 为什么需要统一目录规范

Linux 发行版众多，开发者、企业、社区各有各的想法。如果每套发行版把文件随意放置，运维工程师在不同系统间切换时就无从下手——同样是网络配置文件，A 发行版放在 `/etc/network/`，B 发行版放在 `/etc/sysconfig/`，谁也记不住。

**FHS（Filesystem Hierarchy Standard，文件系统层级标准）** 正是为了解决这个混乱而诞生的。它规定了根目录下每个子目录应该存放哪一类数据，让软件开发商、发行版制作者和系统管理员都能遵循同一套约定。

FHS 按两个维度把目录分成四类：

|  | 可分享的（shareable） | 不可分享的（unshareable） |
|---|---|---|
| **不变的（static）** | `/usr`（软件主体）、`/opt` | `/etc`（配置文件）、`/boot` |
| **可变动的（variable）** | `/var/mail`（邮件）| `/var/run`（进程信息）、`/var/lock` |

FHS 重点定义了三层目录的内容：

- **`/`**（根目录）：与开机和系统修复有关
- **`/usr`**（Unix Software Resource）：软件安装与执行相关
- **`/var`**（variable）：系统运行后产生的可变数据

## 目录树与挂载

Linux 的所有目录都从根目录 `/` 生长出来，形成一棵**目录树（directory tree）**。

关键特性：

- 目录树只有一个起点：根目录 `/`
- 每个文件的完整路径在整棵树中唯一
- 目录可以挂载来自不同分区甚至网络文件系统（如 NFS）的数据——这意味着 `/home` 可以在独立的磁盘分区上，甚至来自远端服务器

!!! note "为什么根目录越小越好"

    FHS 建议根目录所在分区尽量小，不要把应用软件装进来。根目录越小，文件系统出错的概率越低，系统救援时也更轻便。应用程序推荐装到 `/usr` 或 `/opt`。

## 重要目录详解

### `/`（根目录）

整个文件系统的起点。开机流程、系统修复都依赖根目录中的内容。现代发行版（如 CentOS 7+、Ubuntu 20.04+）已将部分原本在根目录下的子目录合并进 `/usr`，并用软链接保持兼容：

``` bash title="查看根目录下的软链接关系"
ls -l /
# bin -> usr/bin      ← /bin 已合并到 /usr/bin
# sbin -> usr/sbin    ← /sbin 已合并到 /usr/sbin
# lib -> usr/lib      ← /lib 已合并到 /usr/lib
# lib64 -> usr/lib64  ← /lib64 已合并到 /usr/lib64
```

### `/bin` 和 `/sbin`（基础命令）

| 目录 | 用途 | 典型命令 |
|---|---|---|
| `/bin` | 所有用户可用的基础命令，单用户维护模式也能用 | `ls`、`cp`、`mv`、`cat`、`bash` |
| `/sbin` | 系统管理员用的系统级命令，用于开机、修复、还原 | `fdisk`、`fsck`、`ifconfig`、`mkfs` |

现代系统上两者均为软链接，实际文件在 `/usr/bin` 和 `/usr/sbin`。

### `/boot`（引导文件）

存放开机所需的全部文件：

- Linux 内核（文件名通常为 `vmlinuz-<版本号>`）
- 初始内存盘（`initramfs` 或 `initrd`）
- 引导加载程序配置（GRUB 2 配置位于 `/boot/grub2/`）

``` bash title="/boot 目录典型内容"
ls /boot
# vmlinuz-5.15.0-91-generic    ← 内核镜像
# initrd.img-5.15.0-91-generic ← 初始内存盘
# grub/                        ← GRUB 引导配置目录
```

### `/dev`（设备文件）

Linux 将所有硬件设备抽象为文件，统一存放在 `/dev`。访问某个设备 = 读写对应文件。设备文件由 **udev** 在系统启动时动态创建，无需手动管理。

``` bash title="常见设备文件示例"
/dev/sda        # 第一块 SATA/SCSI 硬盘
/dev/sda1       # 第一块硬盘的第一个分区
/dev/nvme0n1    # NVMe 固态硬盘
/dev/tty1       # 第一个虚拟终端
/dev/null       # 黑洞设备（丢弃所有写入）
/dev/zero       # 零流设备（持续输出 0）
/dev/random     # 随机数设备
```

### `/etc`（系统配置文件）

几乎所有系统级配置都在这里。普通用户可读，只有 root 可修改。

!!! warning "FHS 明确规定"

    `/etc` 目录下不应存放可执行的二进制文件（binary），只放配置文件。

常见重要文件：

``` bash title="/etc 下的核心配置文件"
/etc/passwd        # 用户账号信息
/etc/shadow        # 用户密码（加密后）
/etc/fstab         # 文件系统挂载表（系统启动时自动挂载）
/etc/hosts         # 静态主机名解析
/etc/hostname      # 本机主机名
/etc/resolv.conf   # DNS 服务器配置
/etc/sudoers       # sudo 权限配置
/etc/crontab       # 系统级定时任务
```

FHS 还要求以下子目录存在：

- `/etc/opt/`：`/opt` 下第三方软件的配置文件
- `/etc/X11/`：X Window 系统配置（含 `xorg.conf`）

### `/home`（普通用户主目录）

每个普通用户都有一个 `/home/<用户名>` 目录，存放该用户的个人文件、配置和数据。

``` bash title="主目录的特殊符号"
~           # 当前登录用户的主目录
~alice      # 用户 alice 的主目录（即 /home/alice）
```

### `/lib` 和 `/lib64`（共享库）

存放开机时和 `/bin`、`/sbin` 中命令所需的共享库（`.so` 文件），类似 Windows 的 DLL。

- `/lib`：32 位库（或通用库）
- `/lib64`：64 位专用库

现代系统中两者均为软链接，实际文件在 `/usr/lib` 和 `/usr/lib64`。

内核模块存放在 `/lib/modules/<内核版本>/`（驱动程序就在这里）。

### `/media` 和 `/mnt`（挂载点）

两者都是挂载外部设备或文件系统的目录，但使用场景不同：

| 目录 | 用途 |
|---|---|
| `/media` | 系统自动挂载可移动设备（U 盘、光盘、DVD），由 udev/systemd 管理 |
| `/mnt` | 管理员手动临时挂载，如调试时挂载备份分区 |

``` bash title="手动挂载示例"
mount /dev/sdb1 /mnt   # 将 /dev/sdb1 分区临时挂载到 /mnt
```

### `/opt`（第三方软件）

独立安装的第三方商业或大型软件放在这里，与发行版自带的包管理系统互不干扰。

``` bash title="典型 /opt 内容"
/opt/google/chrome/     # Google Chrome 浏览器
/opt/idea/              # IntelliJ IDEA
/opt/oracle/            # Oracle 数据库
```

### `/proc`（内核运行状态）

`/proc` 是一个**虚拟文件系统（procfs）**，内容存在内存中，不占硬盘空间。它把内核的运行状态、进程信息、硬件状态全部以文件形式暴露出来。

``` bash title="常用 /proc 文件"
cat /proc/cpuinfo        # CPU 信息（型号、核数、频率）
cat /proc/meminfo        # 内存使用情况
cat /proc/mounts         # 当前挂载的文件系统
cat /proc/uptime         # 系统运行时间
ls /proc/1234/           # 进程 PID=1234 的详细信息目录
cat /proc/net/dev        # 网络接口统计
```

### `/root`（root 用户主目录）

系统管理员 root 的主目录。之所以不放在 `/home/root`，是因为单用户维护模式只挂载根目录时，root 仍然能访问自己的主目录，从而完成修复工作。

### `/run`（运行时数据）

存放系统启动后产生的临时数据，使用 **tmpfs**（内存文件系统）实现，重启自动清空。

- PID 文件（如 `/run/sshd.pid`）
- Unix 域 socket 文件
- 锁文件

!!! note "历史演变"

    旧版系统用 `/var/run` 和 `/var/lock`，新版 FHS 统一改为 `/run`，旧路径通常以软链接保持兼容：`/var/run -> /run`，`/var/lock -> /run/lock`。

### `/srv`（服务数据）

`srv` 是 service 的缩写，存放本机对外提供的网络服务所使用的数据。

``` bash title="典型 /srv 内容"
/srv/www/       # Web 服务器的网站文件（某些发行版）
/srv/ftp/       # FTP 服务器的文件目录
```

### `/sys`（内核设备树）

`/sys` 是另一个虚拟文件系统（sysfs），同样不占硬盘空间。与 `/proc` 互补：

| 文件系统 | 主要用途 |
|---|---|
| `/proc` | 进程信息、内核参数、运行统计 |
| `/sys` | 硬件设备树、驱动模块、内核对象属性 |

``` bash title="通过 /sys 读取硬件信息示例"
cat /sys/block/sda/queue/rotational   # 判断是否为机械硬盘（1=是，0=SSD）
cat /sys/class/net/eth0/speed         # 网卡速率
```

### `/tmp`（临时文件）

任何用户和程序都可以在此创建临时文件，但重启后通常会被清空（许多发行版在启动时自动清理，或使用 tmpfs 挂载）。

!!! warning "勿存重要数据"

    不要把重要文件放到 `/tmp`，重启后可能消失。程序的临时文件逻辑也应处理 `/tmp` 被清理的情况。

### `/usr`（软件主体）

`/usr` 不是 user 的缩写，而是 **Unix Software Resource**。发行版提供的绝大多数软件都安装在这里，功能类似 Windows 的 `C:\Windows\system32` + `C:\Program Files` 的组合。

`/usr` 下的重要子目录：

| 目录 | 内容 |
|---|---|
| `/usr/bin/` | 普通用户可用的所有命令（`/bin` 软链到此） |
| `/usr/sbin/` | 系统管理命令（`/sbin` 软链到此） |
| `/usr/lib/` | 共享库（`/lib` 软链到此） |
| `/usr/lib64/` | 64 位共享库（`/lib64` 软链到此） |
| `/usr/local/` | 管理员手动编译安装的软件（不由包管理器管理） |
| `/usr/share/` | 架构无关的只读数据（man 手册、时区文件、文档） |
| `/usr/include/` | C/C++ 头文件（编译程序时使用） |
| `/usr/src/` | 源代码（内核源码建议放 `/usr/src/linux/`） |

``` bash title="/usr/local 的典型结构"
/usr/local/bin/     # 手动安装软件的可执行文件
/usr/local/lib/     # 手动安装软件的库
/usr/local/etc/     # 手动安装软件的配置文件
```

### `/var`（可变数据）

系统运行后持续增长的数据都在 `/var`，建议独立分区以防日志暴涨撑满根目录。

| 目录 | 内容 |
|---|---|
| `/var/log/` | 系统日志（`syslog`、`auth.log`、`messages` 等） |
| `/var/lib/` | 应用程序运行状态数据（如 MySQL 数据库文件） |
| `/var/cache/` | 应用程序缓存（可安全删除，删后自动重建） |
| `/var/spool/` | 排队等待处理的数据（打印队列、邮件队列、cron 任务） |
| `/var/mail/` | 用户本地邮箱（通常软链到 `/var/spool/mail/`） |
| `/var/run/` | 软链到 `/run`（进程 PID 文件等） |
| `/var/lock/` | 软链到 `/run/lock`（设备/文件锁） |

``` bash title="查看系统日志示例"
tail -f /var/log/syslog         # 实时查看系统日志（Debian/Ubuntu）
tail -f /var/log/messages       # 实时查看系统日志（Red Hat/CentOS）
journalctl -f                   # systemd 日志（通用）
```

## Debian 与 Red Hat 的配置差异

FHS 只规定到第一层（`/`）和第二层（`/usr`、`/var`），更深的目录各发行版可自行约定。以下是常见的路径差异。

=== "网络配置"

    === "Debian / Ubuntu"

        ``` bash title="网络接口配置（传统 ifupdown）"
        # 网络接口静态配置文件
        /etc/network/interfaces

        # Netplan 配置（Ubuntu 18.04+）
        /etc/netplan/01-netcfg.yaml
        ```

        ``` yaml title="/etc/netplan/01-netcfg.yaml 示例"
        network:
          version: 2
          ethernets:
            eth0:
              dhcp4: true   # 启用 DHCP
        ```

    === "Red Hat / RHEL / CentOS"

        ``` bash title="网络接口配置文件目录"
        # 每个网卡对应一个文件（CentOS 7 / RHEL 7）
        /etc/sysconfig/network-scripts/ifcfg-eth0

        # NetworkManager 连接配置（RHEL 8+）
        /etc/NetworkManager/system-connections/eth0.nmconnection
        ```

        ``` ini title="/etc/sysconfig/network-scripts/ifcfg-eth0 示例"
        TYPE=Ethernet
        BOOTPROTO=dhcp    # 使用 DHCP
        NAME=eth0
        ONBOOT=yes        # 开机自动启动
        ```

=== "包管理数据库"

    === "Debian / Ubuntu"

        ``` bash title="APT 包管理相关路径"
        /var/lib/dpkg/          # dpkg 包数据库（已安装包信息）
        /var/lib/apt/lists/     # APT 软件源索引缓存
        /etc/apt/sources.list   # 软件源列表
        /etc/apt/sources.list.d/ # 附加软件源目录
        ```

    === "Red Hat / RHEL / CentOS"

        ``` bash title="RPM/YUM/DNF 包管理相关路径"
        /var/lib/rpm/           # RPM 包数据库（已安装包信息）
        /var/cache/yum/         # YUM 包缓存（CentOS 7）
        /var/cache/dnf/         # DNF 包缓存（RHEL 8+）
        /etc/yum.repos.d/       # YUM/DNF 软件源配置目录
        ```

=== "系统日志路径"

    === "Debian / Ubuntu"

        ``` bash title="主要日志文件"
        /var/log/syslog         # 系统综合日志
        /var/log/auth.log       # 认证和授权日志（sudo、SSH 登录等）
        /var/log/kern.log       # 内核日志
        /var/log/dpkg.log       # 包安装/卸载日志
        /var/log/apt/           # APT 操作日志目录
        ```

    === "Red Hat / RHEL / CentOS"

        ``` bash title="主要日志文件"
        /var/log/messages       # 系统综合日志（等价于 syslog）
        /var/log/secure         # 认证和授权日志
        /var/log/kern.log       # 内核日志（部分版本）
        /var/log/yum.log        # YUM 操作日志（CentOS 7）
        /var/log/dnf.log        # DNF 操作日志（RHEL 8+）
        ```

=== "服务单元配置"

    === "Debian / Ubuntu"

        ``` bash title="systemd 服务文件路径"
        /lib/systemd/system/      # 发行版提供的服务单元（只读）
        /etc/systemd/system/      # 管理员自定义/覆盖的服务单元
        # 查看服务状态
        systemctl status ssh      # Debian/Ubuntu 的 SSH 服务名为 ssh
        ```

    === "Red Hat / RHEL / CentOS"

        ``` bash title="systemd 服务文件路径"
        /usr/lib/systemd/system/  # 发行版提供的服务单元（只读）
        /etc/systemd/system/      # 管理员自定义/覆盖的服务单元
        # 查看服务状态
        systemctl status sshd     # RHEL 的 SSH 服务名为 sshd
        ```


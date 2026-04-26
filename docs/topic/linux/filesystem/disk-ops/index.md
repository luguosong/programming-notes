---
title: 磁盘分区、格式化与挂载
---

# 磁盘分区、格式化与挂载

**本文你会学到**：

- 如何用 `fdisk`、`gdisk`、`parted` 对磁盘进行分区
- 如何用 `mkfs` 系列命令格式化分区为指定文件系统
- 如何手动挂载/卸载，以及配置开机自动挂载
- swap 的创建与管理
- 特殊文件系统（tmpfs/devtmpfs）的用途

新增一块磁盘到 Linux 系统，通常要经历三个阶段：**分区 → 格式化 → 挂载**，对应的核心命令是 `gdisk`/`fdisk`/`parted`、`mkfs`、`mount`。

## 磁盘分区工具

Linux 下常用三种工具来管理磁盘分区。选择哪种，取决于磁盘的分区表类型：MBR 还是 GPT。

### 三种工具一览

| 工具 | 分区表支持 | 操作方式 | 最大磁盘 | 适用场景 |
|------|-----------|---------|---------|---------|
| `fdisk` | MBR（有限支持 GPT） | 交互式 | 2 TB | 老系统、小磁盘 |
| `gdisk` | GPT（支持 MBR 转换） | 交互式（类似 fdisk） | 无限制 | 推荐用于 GPT 磁盘 |
| `parted` | MBR + GPT | 交互式 / 单行命令 | 无限制 | 脚本自动化、需要跨类型操作 |

!!! tip "分区前先确认分区表类型"

    用 `parted /dev/sda print` 或 `fdisk -l /dev/sda` 先查看磁盘是 MBR（`msdos`）还是 GPT，再选对工具。
    GPT 磁盘用 `gdisk`，MBR 磁盘用 `fdisk`，或者统一用 `parted`。

### fdisk

`fdisk` 是最经典的 MBR 分区工具，支持主分区（primary）、扩展分区（extended）和逻辑分区（logical）的管理。MBR 最多 4 个主分区，单分区最大 2 TB。

``` bash title="fdisk 常用操作"
# 进入交互界面（针对整块磁盘，不加分区号）
fdisk /dev/sda

# 交互界面内常用指令：
# p  显示当前分区表
# n  新建分区
# d  删除分区
# t  修改分区类型
# w  保存并退出
# q  不保存退出

# 非交互式：查看磁盘信息
fdisk -l /dev/sda
```

!!! warning "操作提示"

    在 `fdisk` 交互界面内，所有操作在按下 `w` 之前不会写入磁盘，按 `q` 可随时退出且不生效。

### gdisk

`gdisk` 专为 GPT 分区设计，操作界面与 `fdisk` 高度相似，直接替换学习成本极低。GPT 支持最多 128 个分区，单盘容量无 2 TB 限制。

``` bash title="gdisk 常用操作"
# 进入交互界面
gdisk /dev/nvme0n1

# 交互界面内常用指令：
# ?  显示所有命令帮助
# p  打印当前分区表
# n  新建分区
# d  删除分区
# t  修改分区类型码（8300=Linux fs，8200=swap，EF00=EFI）
# w  保存写入并退出
# q  不保存退出
```

!!! danger "不要混用工具"

    **GPT 磁盘只用 `gdisk`，MBR 磁盘只用 `fdisk`**，不要在 GPT 磁盘上用 `fdisk` 操作分区，否则可能破坏分区表。

### parted

`parted` 同时支持 MBR 和 GPT，最大优势是**支持非交互式单行命令**，适合脚本自动化。

``` bash title="parted 常用操作"
# 显示分区表
parted /dev/sda print

# 以 MB 为单位显示
parted /dev/sda unit mb print

# 新建 GPT 分区表（危险！会清空磁盘！）
parted /dev/sdb mklabel gpt

# 新建分区（非交互式，直接指定起止位置）
# 语法：mkpart <类型> <文件系统> <起始> <结束>
parted /dev/sdb mkpart primary ext4 1MiB 10GiB

# 删除分区（按编号）
parted /dev/sdb rm 1
```

!!! tip "parted 单位说明"

    `parted` 支持 `MiB`（准确）和 `MB`（近似）两种单位。推荐使用 `MiB`/`GiB` 以避免对齐问题：
    起始位置建议从 `1MiB` 开始，确保 4K 对齐。

## 查看磁盘与分区状态

在动手分区之前，先观察当前磁盘状态。

### lsblk —— 树形显示所有块设备

``` bash title="lsblk 用法"
# 列出所有磁盘与分区（树形）
lsblk

# 同时显示文件系统类型和 UUID
lsblk -f

# 显示完整设备路径
lsblk -p /dev/sda

# 示例输出：
# NAME        MAJ:MIN RM  SIZE RO TYPE MOUNTPOINT
# sda           8:0    0   40G  0 disk
# ├─sda1        8:1    0    2M  0 part
# ├─sda2        8:2    0    1G  0 part /boot
# └─sda3        8:3    0   30G  0 part
#   └─centos-root ...              /
# nvme0n1     259:0    0  500G  0 disk
# └─nvme0n1p1 259:1    0  500G  0 part
```

### blkid —— 查询设备 UUID 和文件系统类型

``` bash title="blkid 用法"
# 列出所有已格式化的设备信息
blkid

# 查询指定设备
blkid /dev/sda1

# 示例输出：
# /dev/sda2: UUID="94ac5f77-cb8a-495e-a65b-2ef7442b837c" TYPE="xfs"
```

`UUID`（Universally Unique Identifier，通用唯一标识符）是设备的唯一标识，**推荐在 `/etc/fstab` 中用 UUID 而非 `/dev/sdX`**，防止磁盘重排导致挂载错误。

### fdisk -l / parted -l —— 查看详细分区信息

``` bash title="查看分区表"
# 查看所有磁盘的分区信息
fdisk -l

# 查看指定磁盘（含 GPT 详情）
parted /dev/sda print
```

## 分区实操

### gdisk 创建 GPT 分区（交互式流程）

以在 `/dev/nvme0n1` 上新建一个 20 GiB 的 Linux 数据分区为例：

``` bash title="gdisk 分区实操"
gdisk /dev/nvme0n1
```

交互流程如下：

``` text title="gdisk 交互步骤"
GPT fdisk (gdisk) version x.x.x

Command (? for help): p          # 先查看当前分区表，找到剩余空间

Disk /dev/nvme0n1: 1048576000 sectors, 500.0 GiB
...
Number  Start (sector)  End (sector)  Size       Code  Name
   1    2048            2099199       1024.0 MiB  EF00  EFI System
   2    2099200         4196351       1024.0 MiB  8300  Linux filesystem

Command (? for help): n          # 新建分区
Partition number (3-128, default 3): 3    # 直接回车用默认编号
First sector (34-1048575966, default = 4196352) or {+-}size{KMGTP}: # 回车使用默认起始
Last sector (...) or {+-}size{KMGTP}: +20G     # 用 +容量 方式，不要回车用默认（会用光全部空间）

Current type is 'Linux filesystem'
Hex code or GUID (L to show codes, Enter = 8300): # 回车默认 8300（Linux 文件系统）

Command (? for help): p          # 确认新分区

Command (? for help): w          # 保存写入磁盘
Do you want to proceed? (Y/N): y

# 让内核重新读取分区表（无需重启）
partprobe /dev/nvme0n1

# 确认新分区已出现
lsblk /dev/nvme0n1
```

!!! tip "分区类型码常用速查"

    - `8300`：Linux 文件系统（默认）
    - `8200`：Linux swap
    - `EF00`：EFI 系统分区
    - `8E00`：Linux LVM

### parted 单行创建分区

适合脚本或批量操作，无需交互：

``` bash title="parted 单行分区"
# 查看当前分区末尾位置
parted /dev/sdb print

# 在 20GiB 位置后新建一个 10GiB 的分区
parted /dev/sdb mkpart primary xfs 20GiB 30GiB

# 确认
parted /dev/sdb print

# 更新内核分区表
partprobe /dev/sdb
```

## 格式化（创建文件系统）

分区完成后需要格式化，即在分区上创建文件系统结构，操作系统才能使用。

### mkfs.xfs —— XFS 文件系统

XFS 是 RHEL/CentOS 7+ 的默认文件系统，支持大文件、高并发、在线扩容。

``` bash title="mkfs.xfs 格式化"
# 最简单的用法（默认参数）
mkfs.xfs /dev/nvme0n1p3

# 常用参数：
# -L <卷标>     设置卷标（Label），最长 12 字节
# -f            强制格式化（覆盖已有文件系统）
# -d agcount=N  设置 AG（分配组）数量，建议与 CPU 核心数一致

# 指定卷标
mkfs.xfs -L "data-vol" /dev/nvme0n1p3

# 强制重新格式化
mkfs.xfs -f /dev/nvme0n1p3

# 多核 CPU 优化（以 4 核为例）
mkfs.xfs -f -d agcount=4 /dev/nvme0n1p3

# 验证格式化结果
blkid /dev/nvme0n1p3
```

### mkfs.ext4 —— EXT4 文件系统

EXT4 是 Debian/Ubuntu 的传统默认文件系统，稳定成熟，兼容性好。

``` bash title="mkfs.ext4 格式化"
# 基本格式化
mkfs.ext4 /dev/sda1

# 常用参数：
# -L <卷标>     设置卷标
# -b <大小>     Block 大小，可选 1024/2048/4096（字节），默认 4096

# 指定卷标和 block 大小
mkfs.ext4 -L "backup" -b 4096 /dev/sda1

# 查看已格式化分区的详细信息
dumpe2fs -h /dev/sda1
```

### mkswap —— 创建 swap 分区

``` bash title="mkswap 创建 swap"
# 将分区格式化为 swap 格式
mkswap /dev/sda2

# 验证
blkid /dev/sda2
# 输出：/dev/sda2: UUID="xxx" TYPE="swap"
```

!!! note "mkfs 综合命令"

    `mkfs` 是前端封装命令，`mkfs -t xfs` 等价于 `mkfs.xfs`，`mkfs -t ext4` 等价于 `mkfs.ext4`。
    Tab 补全 `mkfs` 可以看到系统支持的所有文件系统类型。

## 挂载与卸载

格式化完成的文件系统需要挂载到目录树上才能访问。挂载点（mount point）就是那个**目录**。

### mount —— 手动挂载

``` bash title="mount 常用语法"
# 自动检测文件系统类型挂载（推荐）
mount UUID="xxxx-xxxx" /mnt/data

# 指定文件系统类型
mount -t xfs /dev/sda3 /mnt/data

# 以只读方式挂载
mount -o ro /dev/sda3 /mnt/data

# 挂载时指定多个选项（逗号分隔）
mount -o rw,noexec,nosuid /dev/sda3 /mnt/data

# 查看当前所有挂载
mount

# 重新挂载（修改挂载参数，不卸载）
mount -o remount,rw /

# 根据 /etc/fstab 挂载所有未挂载的分区
mount -a
```

挂载前需要先创建挂载点目录：

``` bash title="挂载前创建目录"
mkdir -p /mnt/data
mount UUID="e0a6af55-xxxx" /mnt/data
```

### umount —— 卸载

``` bash title="umount 常用语法"
# 用设备名卸载
umount /dev/sda3

# 用挂载点卸载（更推荐，语义清晰）
umount /mnt/data

# 强制卸载（谨慎使用，可能丢数据）
umount -f /mnt/data
```

卸载失败的最常见原因是**有进程正在使用该文件系统**。先排查再卸载：

``` bash title="排查占用进程"
# 查看哪些进程在使用挂载点
lsof /mnt/data

# 或用 fuser 找出进程号并终止
fuser -m /mnt/data
fuser -km /mnt/data    # 同时发送 SIGKILL 终止进程

# 如果你自己就在该目录中，先切换出去
cd /
umount /mnt/data
```

### 常用挂载选项速查

| 选项 | 说明 |
|------|------|
| `rw` / `ro` | 可读写 / 只读 |
| `exec` / `noexec` | 允许 / 禁止执行二进制文件 |
| `suid` / `nosuid` | 允许 / 禁止 SUID/SGID 特殊权限 |
| `auto` / `noauto` | `mount -a` 时是否自动挂载 |
| `user` / `nouser` | 允许 / 禁止普通用户挂载（默认 `nouser`） |
| `relatime` | 只在文件修改时才更新访问时间（性能优化） |
| `noatime` | 从不更新访问时间（最大 IO 节省） |
| `async` / `sync` | 异步（默认）/ 同步写入 |
| `defaults` | 等同于 `rw,suid,dev,exec,auto,nouser,async` |
| `nofail` | 设备不存在时不报错（推荐用于外部设备的 fstab 条目） |

## 开机自动挂载：/etc/fstab

手动 `mount` 重启后失效。要让系统每次开机自动挂载，需要编辑 `/etc/fstab`。

### 六个字段详解

`/etc/fstab` 每行代表一个挂载条目，共六个字段，以空白分隔：

``` text title="/etc/fstab 格式"
# <设备>              <挂载点>    <文件系统>  <选项>       <dump> <fsck>
UUID=xxxx-xxxx        /mnt/data   xfs         defaults     0      0
LABEL=backup          /mnt/bak    ext4        defaults     0      2
```

| 字段 | 说明 |
|------|------|
| 设备 | 设备名 `/dev/sda1`、`UUID=...`、`LABEL=...` 三者均可 |
| 挂载点 | 目标目录，swap 分区填 `swap` |
| 文件系统 | `xfs`、`ext4`、`swap`、`tmpfs`、`vfat` 等 |
| 选项 | 挂载参数，多个用逗号隔开，通常填 `defaults` |
| dump | 是否被 `dump` 命令备份，已过时，填 `0` 即可 |
| fsck | 开机时 `fsck` 检查顺序：`0`=不检查，`1`=根目录优先，`2`=其他分区 |

!!! warning "fsck 字段注意事项"

    XFS 文件系统有自己的日志恢复机制，**第六字段必须填 `0`**，不要填 `2`，否则会造成不必要的检查延迟甚至错误。
    EXT4 可以填 `2`（根目录 `/` 填 `1`）。

### 推荐用 UUID 而非 /dev/sdX

`/dev/sdX` 的字母顺序取决于内核检测磁盘的顺序，添加或拔除磁盘后编号可能改变，导致挂载错误。`UUID` 是唯一的，推荐使用：

``` bash title="查询 UUID"
# 查询指定设备的 UUID
blkid /dev/nvme0n1p3

# 或用 xfs_admin 查 xfs 分区
xfs_admin -lu /dev/nvme0n1p3
```

### /etc/fstab 完整示例

=== "ext4 分区"

    ``` text title="/etc/fstab（含 ext4 条目）"
    # <设备>                                     <挂载点>  <FS>    <选项>             <dump> <fsck>
    UUID=94ac5f77-cb8a-495e-a65b-2ef7442b837c   /boot     xfs     defaults           0      0
    UUID=899b755b-1da4-4d1d-9b1c-f762adb798e1   /backup   ext4    defaults           0      2
    UUID=6b17e4ab-9bf9-43d6-88a0-73ab47855f9d   swap      swap    defaults           0      0
    ```

=== "xfs 分区"

    ``` text title="/etc/fstab（含 xfs 条目）"
    # <设备>                                     <挂载点>  <FS>    <选项>    <dump> <fsck>
    UUID=94ac5f77-cb8a-495e-a65b-2ef7442b837c   /boot     xfs     defaults  0      0
    UUID=e0a6af55-26e7-4cb7-a515-826a8bd29e90   /data     xfs     defaults  0      0
    UUID=6b17e4ab-9bf9-43d6-88a0-73ab47855f9d   swap      swap    defaults  0      0
    tmpfs                                        /tmp      tmpfs   size=2G,mode=1777  0  0
    ```

写入后**务必测试**语法是否正确，配置错误可能导致系统无法开机：

``` bash title="测试 fstab 配置"
# 先卸载目标分区（如果已挂载）
umount /data

# 尝试挂载 fstab 中所有未挂载的条目
mount -a

# 验证挂载成功
df /data
```

!!! danger "fstab 写错的救援方法"

    如果因 `/etc/fstab` 错误导致开机进入 emergency mode，此时根目录是只读的，执行下面命令重新以读写方式挂载根目录，再修复文件：
    ```bash
    mount -n -o remount,rw /
    vi /etc/fstab
    ```

## swap 管理

swap（交换空间）是磁盘上划出的一块区域，当物理内存不足时，内核将部分内存页换出到 swap，释放内存给活跃进程使用。

### swapon / swapoff

``` bash title="swap 的启用与关闭"
# 启用 swap 分区
swapon /dev/sda2

# 启用 swap 文件
swapon /var/swapfile

# 查看当前所有 swap
swapon -s
# 或：
free -h

# 关闭指定 swap
swapoff /dev/sda2

# 关闭所有 swap
swapoff -a

# 启用 /etc/fstab 中的所有 swap
swapon -a
```

### swap 优先级

有多个 swap 时，可以设置优先级（`-1` 到 `32767`，数值越高优先被使用）：

``` bash title="设置 swap 优先级"
# 启用时指定优先级
swapon -p 10 /dev/sda2

# 在 fstab 中用 pri= 选项指定
# UUID=xxx   swap   swap   defaults,pri=10   0   0
```

### 用文件创建 swap（不需要额外分区）

当磁盘没有剩余分区空间时，可以用一个普通文件来充当 swap：

``` bash title="swap 文件创建流程"
# 创建一个 2GB 的空文件
dd if=/dev/zero of=/var/swapfile bs=1M count=2048

# 设置权限（只有 root 可读写）
chmod 600 /var/swapfile

# 格式化为 swap
mkswap /var/swapfile

# 启用
swapon /var/swapfile

# 验证
swapon -s

# 写入 fstab 永久生效（swap 文件不能用 UUID，必须用路径）
echo "/var/swapfile   swap   swap   defaults   0   0" >> /etc/fstab
```

!!! warning "swap 文件不能用 UUID"

    写入 `/etc/fstab` 时，swap **文件**必须使用文件路径（如 `/var/swapfile`），系统不会通过 UUID 查找普通文件。
    swap **分区**则推荐用 UUID。

## 特殊文件系统

Linux 中有几类不占用实际磁盘空间的特殊文件系统，它们存在于内存中。

### tmpfs —— 内存文件系统

`tmpfs` 将文件存储在内存（或 swap）中，读写速度极快，重启后数据消失。常用于 `/tmp`、`/run`、`/dev/shm` 等需要高速临时存储的目录。

``` bash title="tmpfs 挂载示例"
# 手动挂载 tmpfs（限制大小为 512M）
mount -t tmpfs -o size=512m tmpfs /mnt/ram

# 查看所有已挂载的 tmpfs
df -t tmpfs
# 示例输出：
# Filesystem   Size  Used Avail Use% Mounted on
# devtmpfs     7.6G     0  7.6G   0% /dev
# tmpfs         7.8G     0  7.8G   0% /dev/shm
# tmpfs         7.8G  1.2M  7.8G   1% /run
```

在 `/etc/fstab` 中配置 `tmpfs`：

``` text title="fstab 中的 tmpfs 条目"
tmpfs   /tmp   tmpfs   size=2G,mode=1777,nosuid,nodev   0   0
```

### devtmpfs —— 设备文件系统

`devtmpfs` 由内核在启动时自动创建并挂载到 `/dev`，包含所有设备节点（如 `/dev/sda`、`/dev/null`）。它由内核自动维护，**无需手动配置**。

## 发行版差异

=== "Debian / Ubuntu"

    **默认文件系统**：`ext4`（传统默认），近年 Ubuntu 部分配置开始支持 XFS。

    **`/etc/fstab` 特点**：

    EXT4 根目录默认带 `errors=remount-ro` 选项，发生错误时自动重新以只读方式挂载，防止数据损坏：

    ``` text
    UUID=xxxx   /   ext4   errors=remount-ro   0   1
    ```

    **swap 文件**：Ubuntu 22.04+ 默认安装时不创建 swap 分区，而是使用 `/swapfile`（swap 文件）。

=== "RHEL / CentOS / AlmaLinux / Rocky"

    **默认文件系统**：XFS（RHEL 7 起成为默认）。

    **`/etc/fstab` 特点**：

    XFS 根目录通常带 `defaults` 选项，第六字段为 `0`（XFS 不用 fsck）。启用磁盘配额时，在挂载选项中加入对应参数：

    - `uquota`：用户配额
    - `gquota`：组配额
    - `prjquota`：项目配额

    ``` text
    UUID=xxxx   /data   xfs   defaults,uquota,gquota   0   0
    ```

    **NVMe 设备命名**：NVMe 磁盘文件名为 `/dev/nvme0n1`，分区为 `/dev/nvme0n1p1`，与 SATA 磁盘（`/dev/sda`）命名规则不同。


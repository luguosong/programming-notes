---
title: 文件与目录操作
---

# 文件与目录操作

Linux 中一切皆文件，掌握目录导航和文件操作是使用系统的前提。本文覆盖路径概念、常用目录命令、文件增删改查，以及链接文件的原理。

**本文你会学到**：

- 绝对路径与相对路径的区别与用途
- `cd`、`pwd`、`mkdir`、`rmdir` 目录操作
- `ls`、`cp`、`rm`、`mv`、`touch` 文件操作
- `file`、`stat` 查看文件元数据
- 软链接与硬链接的区别

## 路径基础：绝对 vs 相对

Linux 文件系统是一棵以 `/` 为根的树。描述文件位置的方式有两种：

**绝对路径**：从根目录 `/` 开始写起，无论当前在哪个目录，路径含义不变。例如 `/usr/share/doc`。

**相对路径**：相对于"当前所在目录"来描述位置，不以 `/` 开头。例如，当前在 `/usr/share/doc`，要去 `/usr/share/man`，可以写 `../man`。

⚙️ **使用场景建议**：

- 在 Shell 脚本中管理系统——用绝对路径，避免因执行目录不同导致脚本失效
- 日常交互终端操作——用相对路径更快捷

### 特殊目录符号速查

| 符号 | 含义 |
|------|------|
| `.` | 当前目录 |
| `..` | 上一层目录 |
| `-` | 上一个工作目录（`cd -` 使用） |
| `~` | 当前用户的主目录（Home） |
| `~username` | 指定用户 `username` 的主目录 |

!!! tip "根目录的上层还是它自己"

    `/` 是整棵树的根，它没有更上层的目录。执行 `ls -al /` 可以看到 `.` 与 `..` 的属性完全相同——根目录的上层（`..`）就是它自己。

## cd — 切换目录

`cd`（Change Directory）是最频繁使用的命令。

``` bash
# 切换到绝对路径
cd /var/spool/mail

# 切换到相对路径（从当前目录出发）
cd ../postfix

# 回到当前用户主目录（以下三种写法等价）
cd
cd ~
cd /home/username

# 切换到 root 用户的主目录
cd /root

# 切换到 dmtsai 用户的主目录（需要相应权限）
cd ~dmtsai

# 返回上一次所在的目录（在两个目录间来回切换很实用）
cd -
```

!!! tip "善用 Tab 补全"

    输入目录名的前几个字母后按 `Tab`，Shell 会自动补全路径，避免打错字。

## pwd — 显示当前目录

`pwd`（Print Working Directory）打印出当前所在的完整绝对路径。

``` bash
# 显示当前目录
pwd
# 输出示例：/root

# -P：显示真实路径，不跟随符号链接
pwd -P
```

实际案例——`/var/mail` 是符号链接：

``` bash
cd /var/mail
pwd        # 输出 /var/mail（跟随链接显示）
pwd -P     # 输出 /var/spool/mail（真实物理路径）
```

## mkdir — 创建目录

``` bash
mkdir [选项] 目录名
```

| 选项 | 说明 |
|------|------|
| `-p` | 递归创建，自动创建所有缺失的上层目录 |
| `-m MODE` | 指定权限（如 `711`），不受 `umask` 影响 |

``` bash
# 创建单个目录
mkdir mydir

# 递归创建多层目录（父目录不存在时会自动创建）
mkdir -p project/src/main/java

# 创建目录并指定权限为 rwx--x--x
mkdir -m 711 mydir2

# 验证结果
ls -ld mydir mydir2
```

!!! warning "谨慎使用 -p"

    `-p` 不会报告打字错误——如果目录名写错，会默默创建一个错误命名的目录层级。

## rmdir — 删除空目录

`rmdir` 只能删除**空目录**，有文件时会报错，这是一种安全保护。

``` bash
# 删除空目录
rmdir mydir

# 递归删除空目录链（从最深层开始，逐层往上删除空目录）
rmdir -p project/src/main/java

# 如果要删除非空目录，需要用 rm -r（见下文）
```

## ls — 列出目录内容

`ls` 是使用频率最高的命令之一，默认只显示非隐藏文件名。

``` bash
ls [选项] [文件或目录]
```

### 常用参数速查

| 参数 | 说明 |
|------|------|
| `-l` | 长格式，显示权限、大小、时间等详细信息 |
| `-a` | 显示所有文件，包含 `.` 开头的隐藏文件 |
| `-A` | 同 `-a`，但不显示 `.` 和 `..` 这两个特殊目录 |
| `-h` | 以人类可读单位（K、M、G）显示文件大小，需配合 `-l` |
| `-t` | 按修改时间排序（最新在前） |
| `-S` | 按文件大小排序（最大在前） |
| `-r` | 反转排序顺序 |
| `-R` | 递归列出所有子目录内容 |
| `-d` | 只显示目录本身，不展开内容（常用于查看目录属性） |
| `-i` | 显示 inode 号（下一章文件系统中会详细介绍） |
| `--color=auto` | 按文件类型着色（大多数发行版默认开启） |
| `--full-time` | 显示完整时间（含年月日时分秒） |

``` bash
# 列出当前目录（含隐藏文件）详细信息
ls -al

# 列出目录，大小用 K/M/G 显示
ls -lh /var/log

# 按时间倒序排列（最旧在前）
ls -ltr

# 只查看某目录自身的属性，不展开内容
ls -ld /etc

# 查看完整时间戳
ls -al --full-time ~
```

!!! tip "ll 是 ls -l 的别名"

    大多数发行版已将 `ll` 设置为 `ls -l` 的别名，可以直接使用 `ll`。

## cp — 复制文件或目录

``` bash
cp [选项] 来源 目标
cp [选项] 来源1 来源2 ... 目标目录
```

| 选项 | 说明 |
|------|------|
| `-r` | 递归复制目录（复制目录时必须加） |
| `-p` | 保留原文件的权限、所有者、时间戳（备份时常用） |
| `-a` | 等同于 `-dr --preserve=all`，尽量完整保留所有属性 |
| `-i` | 目标已存在时询问是否覆盖 |
| `-f` | 强制覆盖，不询问 |
| `-u` | 仅当来源比目标新时才复制（增量备份） |
| `-d` | 来源为符号链接时，复制链接本身而非指向的文件 |
| `-s` | 创建符号链接而非复制 |
| `-l` | 创建硬链接而非复制 |

``` bash
# 复制文件（普通复制，权限会变为执行者所有）
cp /etc/passwd /tmp/passwd_bak

# 询问是否覆盖
cp -i ~/.bashrc /tmp/bashrc

# 完整保留属性复制（备份场景首选）
cp -a /var/log/wtmp /tmp/wtmp_bak

# 递归复制整个目录
cp -r /etc/ /tmp/etc_bak/

# 复制符号链接本身（而不是跟随链接复制目标文件）
cp -d /var/mail /tmp/mail_link_bak

# 仅在来源更新时才复制
cp -u ~/.bashrc /tmp/bashrc
```

!!! warning "复制时权限会被重置"

    默认情况下，`cp` 复制的文件拥有者会变为执行者自身，权限也可能改变。
    备份系统文件（如 `/etc/shadow`）时必须加 `-a` 或 `-p`，才能保留原有权限。

## rm — 删除文件或目录

``` bash
rm [选项] 文件或目录
```

| 选项 | 说明 |
|------|------|
| `-i` | 删除前询问确认（root 默认已启用） |
| `-r` | 递归删除目录及其所有内容 |
| `-f` | 强制删除，忽略不存在的文件，不询问 |

``` bash
# 删除文件（有交互确认）
rm -i myfile.txt

# 递归删除目录（会逐个询问，按 y 确认）
rm -r /tmp/mydir/

# 强制递归删除，不询问（⚠️ 危险，请确认路径无误）
rm -rf /tmp/mydir/

# 删除以 - 开头的特殊文件名（不能直接 rm -filename）
touch ./-aaa-
rm ./-aaa-      # 用 ./ 前缀绕开选项解析
# 或者
rm -- -aaa-
```

!!! warning "rm -rf 是最危险的命令之一"

    `rm -rf /` 会删除整个系统，且无法撤销。执行前务必核对路径，建议先用 `ls` 确认目标。

## mv — 移动或重命名

`mv` 既能移动文件，又能重命名（本质相同：改变路径）。

``` bash
mv [选项] 来源 目标
mv [选项] 来源1 来源2 ... 目标目录
```

| 选项 | 说明 |
|------|------|
| `-i` | 目标已存在时询问 |
| `-f` | 强制覆盖，不询问 |
| `-u` | 仅当来源比目标新时才移动 |

``` bash
# 将文件移入目录
mv report.txt /tmp/

# 重命名文件（同目录下 mv = rename）
mv old_name.txt new_name.txt

# 重命名目录
mv mydir/ newdir/

# 同时移动多个文件到目录（最后一个参数必须是目录）
mv file1 file2 file3 /tmp/dest/
```

!!! tip "批量重命名用 rename"

    `mv` 只能一次改一个名字。批量重命名请用 `rename` 命令：`rename 's/old/new/' *.txt`

## touch — 创建空文件与修改时间戳

Linux 为每个文件记录三种时间：

| 时间 | 全称 | 何时更新 |
|------|------|---------|
| `mtime` | modification time | 文件**内容**发生变化 |
| `atime` | access time | 文件被读取（`cat`、`less` 等） |
| `ctime` | status time | 文件**属性或权限**发生变化（无法手动修改） |

`ls -l` 默认显示 `mtime`。用 `--time=atime` 或 `--time=ctime` 可查看其他时间。

``` bash
touch [选项] 文件名
```

| 选项 | 说明 |
|------|------|
| （无选项） | 若文件不存在则创建空文件；若存在则更新 atime 和 mtime 为当前时间 |
| `-a` | 只更新 atime |
| `-m` | 只更新 mtime |
| `-d "日期"` | 将 atime/mtime 设为指定日期（如 `"2 days ago"`） |
| `-t YYYYMMDDhhmm` | 将 atime/mtime 设为精确时间 |
| `-c` | 若文件不存在，不创建新文件 |

``` bash
# 创建一个空文件
touch newfile.txt

# 同时创建多个空文件
touch file1 file2 file3

# 更新文件时间为当前时间（文件必须已存在）
touch existing_file.txt

# 将时间改为两天前
touch -d "2 days ago" myfile.txt

# 将时间改为指定值
touch -t 202401150830 myfile.txt

# 查看三种时间
stat myfile.txt
```

## file — 识别文件类型

Linux 不依赖扩展名判断文件类型，`file` 命令通过读取文件头部的**魔数（magic bytes）**来识别真实类型。

``` bash
# 检测单个文件类型
file /bin/bash
# bash: ELF 64-bit LSB shared object, x86-64, ...

file /etc/passwd
# /etc/passwd: ASCII text

file /dev/sda
# /dev/sda: block special (8/0)

# 检测多个文件
file /bin/* | head -10
```

常见输出类型说明：

| 输出关键词 | 说明 |
|-----------|------|
| `ASCII text` | 纯文本文件 |
| `ELF 64-bit LSB executable` | 可执行二进制文件 |
| `ELF 64-bit LSB shared object` | 动态链接库（`.so`） |
| `symbolic link` | 符号链接 |
| `directory` | 目录 |
| `gzip compressed data` | gzip 压缩文件 |

## stat — 查看文件详细元数据

`stat` 显示文件的完整元数据，包括三种时间、inode 号、权限、硬链接数等。

``` bash
stat [文件或目录]
```

``` bash
stat /etc/passwd
```

输出示例：

```
  File: /etc/passwd
  Size: 2315        Blocks: 8          IO Block: 4096   regular file
Device: fd00h/64768d  Inode: 17826091    Links: 1
Access: (0644/-rw-r--r--)  Uid: (    0/    root)   Gid: (    0/    root)
Access: 2024-06-16 08:30:12.000000000 +0800   # atime
Modify: 2024-05-04 17:54:00.000000000 +0800   # mtime
Change: 2024-05-04 17:54:00.000000000 +0800   # ctime
```

`stat` 比 `ls -l` 更详细，在调试权限问题、确认时间戳、查找 inode 编号时非常有用。

## 软链接 vs 硬链接

链接文件有两种：**符号链接（软链接）** 和 **硬链接**，原理完全不同。

### 硬链接 — 同一 inode 的多个目录项

``` bash
ln 原始文件 硬链接名
```

原理：Linux 文件系统用 `inode` 记录文件的实际数据。硬链接只是在目录中新增一条指向**同一 inode** 的记录，两个名字完全等价，没有"谁是原始、谁是链接"之分。

``` bash
# 创建硬链接
ln original.txt hardlink.txt

# 查看 inode 号（两者相同）
ls -li original.txt hardlink.txt
# 1234567 -rw-r--r--. 2 root root 176 Jun 11 19:01 hardlink.txt
# 1234567 -rw-r--r--. 2 root root 176 Jun 11 19:01 original.txt
# ↑ inode 相同，第 3 列的链接数从 1 变成了 2
```

硬链接的限制：

- ❌ 不能跨文件系统（不同分区）
- ❌ 不能链接目录（防止循环引用）
- ✅ 删除其中一个名字不影响文件，只有所有硬链接都删掉，文件才真正消失

### 软链接 — 指向路径的快捷方式

``` bash
ln -s 目标路径 软链接名
```

软链接本身是一个独立文件，内容是**目标路径字符串**，类似 Windows 快捷方式。

``` bash
# 创建软链接
ln -s /usr/share/doc mydoc

# 查看软链接
ls -l mydoc
# lrwxrwxrwx. 1 root root 14 Jun 11 19:06 mydoc -> /usr/share/doc

# 软链接指向不存在的路径时变为"悬空链接"（dangling link）
ln -s /nonexistent broken_link
ls -l broken_link   # 显示红色（文件不存在）
```

### 两者对比

| 特性 | 硬链接 | 软链接 |
|------|--------|--------|
| 原理 | 指向同一 inode | 保存目标路径字符串 |
| 跨文件系统 | ❌ 不支持 | ✅ 支持 |
| 链接目录 | ❌ 不支持 | ✅ 支持 |
| 原文件删除后 | 数据仍可访问 | 链接失效（悬空链接） |
| `ls -l` 标识 | 与普通文件相同（`-`） | `l` 开头 |
| inode 号 | 与原文件相同 | 独立的 inode |

=== "Debian/Ubuntu"

    ``` bash
    # 软链接在 /etc/alternatives 系统中大量使用，用于管理多版本程序
    ls -l /etc/alternatives/python3
    # lrwxrwxrwx 1 root root  ... /etc/alternatives/python3 -> /usr/bin/python3.11
    
    # update-alternatives 命令管理软链接切换
    update-alternatives --list python3
    ```

=== "Red Hat/RHEL"

    ``` bash
    # alternatives 系统同样使用软链接
    ls -l /etc/alternatives/python3
    
    # alternatives 命令管理软链接切换
    alternatives --list
    ```

!!! tip "软链接路径建议用绝对路径"

    创建软链接时，目标路径建议使用绝对路径，避免因移动链接文件到其他目录后路径失效：

    ``` bash
    # ✅ 推荐：使用绝对路径
    ln -s /usr/local/bin/python3 ~/bin/python

    # ❌ 谨慎：使用相对路径（移动链接文件后可能失效）
    ln -s ../lib/python3 ./python
    ```


---
title: 文件权限体系
---

# 文件权限体系

**本文你会学到**：

- 三类权限主体（User、Group、Others）的含义与作用
- 如何从 `ls -l` 输出解读权限字符串（如 `-rw-r--r--`）
- rwx 权限在文件与目录上的不同含义
- SUID、SGID、Sticky Bit 的原理与应用场景
- 用 `chmod` 修改权限（符号式与八进制式）
- 用 `chown` 和 `chgrp` 修改文件所有者与所属组
- 默认权限 umask 的计算与修改
- 特殊权限导致的安全陷阱与最佳实践

## 三类权限主体：谁在操控文件？

Linux 的每个文件都同时面向三类对象设置权限：

**文件拥有者（User / Owner）**：创建文件的账号，对自己的文件拥有最高控制权。

**所属群组（Group）**：文件关联的群组，群组内所有成员共享同一套权限。群组的典型使用场景是团队协作——把同一项目的成员加入同一群组，就能让他们互相读写项目文件，而其他人看不到。

**其他人（Others）**：既不是拥有者、也不在群组内的所有账号。

!!! tip "root 不受权限限制"

    `root` 是系统管理员账号，不受文件权限约束，可以访问系统中的任何文件。

系统账号与群组信息存储在以下三个文件中：

- `/etc/passwd` — 所有账号信息
- `/etc/shadow` — 账号密码（加密）
- `/etc/group` — 所有群组信息

## 读懂 ls -l 的每一列

`ls -l` 是查看文件权限最常用的命令：

``` bash
$ ls -l
-rw-r--r--. 1 root root   1864 May  4 18:01 initial-setup-ks.cfg
drwxr-xr-x. 3 root root     17 May  6 00:14 .config
```

输出共 7 列，含义如下：

```
[ 权限 ]  [硬链接数]  [拥有者]  [群组]  [大小(Bytes)]  [修改时间]  [文件名]
-rw-r--r--.    1        root     root      1864        May 4 18:01   initial-setup-ks.cfg
```

### 权限字符串逐位解读

第一列的 10 个字符是核心：

```
- r w x r - x r - -
↑ ↑↑↑ ↑↑↑ ↑↑↑
│ └─┬─┘ └─┬─┘ └─┬─┘
│  Owner  Group Others
└── 文件类型
```

**第 1 位：文件类型**

| 字符 | 类型 |
|------|------|
| `-` | 普通文件 |
| `d` | 目录 |
| `l` | 符号链接（软链接） |
| `b` | 块设备（如硬盘） |
| `c` | 字符设备（如键盘） |

**第 2–10 位：三组 rwx 权限**，每组代表 `r`（读）、`w`（写）、`x`（执行），无该权限则显示 `-`。

示例解读：`-rwxr-xr--`

```
- rwx r-x r--
  ↑↑↑ ↑↑↑ ↑↑↑
  拥有者:rwx  群组:r-x  其他人:r--
```

- 拥有者：可读、可写、可执行
- 群组成员：可读、可执行，不可写
- 其他人：只读

## 文件权限 vs 目录权限：含义截然不同

同样的 `r`、`w`、`x`，对文件和目录的意义差异很大：

| 权限 | 对文件的意义 | 对目录的意义 |
|------|-------------|-------------|
| `r` | 读取文件内容（`cat`、`less` 等） | 列出目录下的文件名（`ls`） |
| `w` | 编辑/修改文件内容（**不含删除文件本身**） | 在目录内新建、删除、重命名文件（**包括删除你没有写权限的文件！**） |
| `x` | 执行该文件（Linux 的可执行标志） | **进入该目录**（`cd`），相当于目录的"钥匙" |

!!! warning "w 权限对目录的威力"

    如果你对某目录有 `w` 权限，即使目录内某个文件属于 root 且权限为 `----------`，你仍然可以**删除**它！因为删除操作修改的是目录（文件名列表），而非文件本身。

### 只有 r 没有 x 会怎样？

``` bash
$ ls drwxr--r--
# 虽然有 r 权限，可以看到文件名列表（不完整）
# 但没有 x，无法 cd 进入，也无法读取文件内容
```

**开放目录给他人浏览，至少要同时给 `r` 和 `x`，即 `r-x`（5）。**

### 操作所需最小权限速查

对路径 `/dir1/file1`，要完成以下操作所需权限：

| 操作 | /dir1 | /dir1/file1 |
|------|-------|-------------|
| 读取 file1 内容 | `x` | `r` |
| 修改 file1 内容 | `x` | `rw` |
| 执行 file1 | `x` | `rx` |
| 删除 file1 | `wx` | — |

## chmod：修改文件权限

`chmod` 有两种用法：数字法和符号法。

### 数字法（精确设置）

每个权限对应一个数字：`r=4`、`w=2`、`x=1`，三个权限相加得到该组的分数。

``` bash
# -rwxr-xr-x 的计算：
# owner: rwx = 4+2+1 = 7
# group: r-x = 4+0+1 = 5
# other: r-x = 4+0+1 = 5
# 合并为 755

chmod 755 script.sh   # 设置为 -rwxr-xr-x
chmod 644 config.txt  # 设置为 -rw-r--r--（普通文件常用）
chmod 700 private/    # 只有拥有者能访问的目录

# -R 递归修改目录下所有文件
chmod -R 755 /var/www/html
```

常用权限速查：

| 数字 | 权限字符 | 适用场景 |
|------|---------|---------|
| `755` | `rwxr-xr-x` | 可执行文件、公开目录 |
| `644` | `rw-r--r--` | 普通文本文件 |
| `600` | `rw-------` | 私钥、密码文件 |
| `700` | `rwx------` | 私有目录 |
| `777` | `rwxrwxrwx` | 全开放（生产环境慎用） |

### 符号法（相对调整）

符号法不需要知道当前权限，直接增减更方便：

``` bash
# 语法：chmod [ugoa][+-=][rwx] 文件
# u=owner, g=group, o=others, a=all

chmod u+x script.sh      # 给拥有者加执行权限
chmod a-x dangerous.sh   # 所有人去掉执行权限
chmod go-w secret.txt    # 群组和其他人去掉写权限
chmod u=rwx,go=rx app    # 精确设置每组权限（等号覆盖）

# 实用场景：不知道原始权限，只想让脚本可执行
chmod a+x install.sh
```

!!! note "+ 和 = 的区别"

    - `+`、`-`：只修改指定权限位，其余保持不变
    - `=`：覆盖整组权限，未指定的位变为 `-`

## chown / chgrp：修改所有者与群组

``` bash
# chown：修改拥有者
chown alice file.txt          # 将拥有者改为 alice
chown alice:devteam file.txt  # 同时修改拥有者和群组
chown -R alice:devteam /project/  # 递归修改整个目录

# chgrp：只修改所属群组
chgrp devteam file.txt
chgrp -R devteam /project/

# 验证修改结果
ls -l file.txt
```

!!! tip "cp 后需要修改拥有者"

    `cp` 命令复制文件时，新文件的拥有者是执行 `cp` 的账号，而非原文件的拥有者。如果要将文件交给其他人使用，需要 `chown` 修改拥有者。

## umask：新建文件的默认权限

当你创建一个新文件时，它的权限不是随机的——由 `umask` 控制。

**计算规则**：

- 新建**文件**的基准权限是 `666`（无执行位）
- 新建**目录**的基准权限是 `777`
- 实际权限 = 基准权限 `AND NOT` umask（即从基准中去掉 umask 指定的位）

``` bash
# 查看当前 umask
umask        # 输出如 0022
umask -S     # 符号形式：u=rwx,g=rx,o=rx

# umask 022 时：
# 文件：666 - 022 = 644（rw-r--r--）
# 目录：777 - 022 = 755（rwxr-xr-x）
```

=== "RHEL/CentOS/Fedora"

    ``` bash
    # 普通用户默认 umask
    umask  # 0002
    # 文件默认权限：664（rw-rw-r--）
    # 目录默认权限：775（rwxrwxr-x）

    # root 默认 umask
    umask  # 0022
    # 文件默认权限：644（rw-r--r--）
    # 目录默认权限：755（rwxr-xr-x）
    ```

=== "Debian/Ubuntu"

    ``` bash
    # 普通用户默认 umask
    umask  # 0022
    # 文件默认权限：644（rw-r--r--）
    # 目录默认权限：755（rwxr-xr-x）

    # root 默认 umask 同为 0022
    ```

修改默认 umask（写入 `~/.bashrc` 或 `/etc/profile`）：

``` bash
# 团队协作场景，让群组也有写权限
umask 002
```

## SUID / SGID / Sticky Bit：三种特殊权限

标准的 `rwx` 权限之外，还有三种特殊权限位，显示在 `x` 的位置。

### SUID：以文件拥有者身份执行

`SUID`（Set User ID）设置在**可执行文件**上时，任何人执行该文件都会**临时获得文件拥有者的权限**。

典型例子是 `/usr/bin/passwd`：

``` bash
ls -l /usr/bin/passwd
# -rwsr-xr-x 1 root root ... /usr/bin/passwd
#    ↑ s 表示 SUID（原 x 位置变为 s）

# 普通用户执行 passwd 时，临时拥有 root 权限，
# 才能修改 /etc/shadow（root 专属文件）
```

SUID 设置方法：

``` bash
chmod u+s /path/to/binary   # 符号法
chmod 4755 /path/to/binary  # 数字法（4xxx 表示 SUID）
```

!!! warning "SUID 安全风险"

    SUID 是提权的常见途径，生产环境中应定期审计具有 SUID 的文件：
    ```bash
    find / -perm -4000 -type f 2>/dev/null
    ```

### SGID：以群组权限执行或继承群组

`SGID`（Set Group ID）有两种效果：

**用于可执行文件**：执行时临时获得文件所属群组的权限。

**用于目录**：在该目录下创建的新文件会**自动继承目录的所属群组**，而非创建者的默认群组。这对团队共享目录非常有用：

``` bash
# 设置目录 SGID
chmod g+s /project/shared/
chmod 2755 /project/shared/    # 数字法（2xxx 表示 SGID）

# 此后在 /project/shared/ 下创建的文件，
# 群组自动设置为 shared 目录的群组
ls -l /project/shared/
# drwxr-sr-x 2 root devteam ... shared/
#        ↑ s 表示 SGID
```

### Sticky Bit：保护目录内的文件不被他人删除

`Sticky Bit` 设置在目录上时，即使你对该目录有 `w` 权限，也**只能删除自己创建的文件**，不能删除他人的文件。

最典型的例子是 `/tmp`：

``` bash
ls -ld /tmp
# drwxrwxrwt 12 root root 4096 ... /tmp
#         ↑ t 表示 Sticky Bit（原 x 位置变为 t）

# 设置 Sticky Bit
chmod o+t /shared/dir/
chmod 1777 /shared/dir/    # 数字法（1xxx 表示 Sticky Bit）
```

### 特殊权限位汇总

| 特殊权限 | 数字 | 符号 | 作用对象 | 效果 |
|---------|------|------|---------|------|
| SUID | 4 | `u+s` | 可执行文件 | 执行时获得文件拥有者权限 |
| SGID | 2 | `g+s` | 可执行文件 / 目录 | 执行时获得群组权限 / 新文件继承目录群组 |
| Sticky Bit | 1 | `o+t` | 目录 | 只能删除自己创建的文件 |

权限字符串中，若特殊位设置但**没有** `x` 权限，则显示大写 `S` / `T`（表示设置了但无效）；若同时有 `x` 权限，则显示小写 `s` / `t`。

## 文件隐藏属性：连 root 也难改的保护

Linux 文件系统（ext4/xfs）提供了一层"隐藏属性"（extended attributes），独立于标准权限之外，连 `root` 也需要专用命令才能修改。

使用 `lsattr` 查看、`chattr` 修改：

``` bash
# 查看文件的隐藏属性
lsattr file.txt
# ----i--------e-- file.txt
#     ↑ i = 不可变（immutable）

# 设置不可变属性（连 root 也无法删除/修改）
chattr +i /etc/resolv.conf

# 解除不可变属性
chattr -i /etc/resolv.conf

# 追加模式：只允许追加内容，不允许覆盖（适合日志文件）
chattr +a /var/log/app.log
```

常用隐藏属性：

| 属性 | 含义 |
|------|------|
| `i`（immutable） | 不可变：不能修改、删除、重命名、创建硬链接，即使 root 也不行 |
| `a`（append only） | 只追加：只能向文件末尾追加内容，不能覆盖或删除 |
| `e` | 文件使用 extent 映射（ext4 默认，通常不需手动设置） |

!!! tip "防止关键配置文件被意外修改"

    对 `/etc/passwd`、`/etc/hosts` 等关键文件设置 `+i` 可以防止误操作，甚至阻止某些攻击脚本的篡改。但修改前必须先 `chattr -i` 解除保护。


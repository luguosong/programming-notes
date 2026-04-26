---
title: ACL 与特殊权限
---

# ACL 与特殊权限

**本文你会学到**：

- 传统权限的三层结构局限与 ACL 的改进
- 文件系统对 ACL 的支持状态检查
- `getfacl` 查询与 `setfacl` 设置 ACL 的命令
- ACL 中默认权限、掩码的含义
- 使用 ACL 实现细粒度权限控制
- SUID、SGID、Sticky Bit 的原理与危险性
- 特殊权限在不同场景中的实际应用
- 文件属性（chattr、lsattr）的使用
- SELinux 与传统权限、ACL 的关系

## 传统权限的局限

想象一个场景：`/srv/project` 目录属于 `devteam` 组，你想让 `alice` 只读访问，但她不是 `devteam` 成员，也不该加入。传统权限无法做到"仅对 alice 开放读权限"，只能在三种主体里二选一，精度不够。

ACL（Access Control List，访问控制列表）在传统 `rwx` 之上叠加了一层细粒度控制：可以针对**任意指定用户**或**任意指定组**单独设置权限，互不干扰。

## ACL 基础

### 确认文件系统支持

现代 Linux 系统（ext4、XFS）**默认支持 ACL**，无需额外配置。可以通过以下命令确认：

``` bash
# 查看内核挂载时是否启用了 ACL
dmesg | grep -i acl
# 输出示例：SGI XFS with ACLs...

# 或查看挂载选项（ext4）
tune2fs -l /dev/sda1 | grep "Default mount options"
```

!!! tip "XFS 无需显式启用"

    RHEL 8+/CentOS Stream 的系统盘通常是 XFS，ACL 开箱即用，`ls -l` 看到 `+` 号即说明 ACL 已生效。

### getfacl：查看 ACL

``` bash
# 查看单个文件的 ACL
getfacl file.txt

# 递归查看目录
getfacl -R /srv/project/
```

`getfacl` 输出解读（以 `acl_test1` 为例）：

``` bash
$ getfacl acl_test1
# file: acl_test1          # 文件名
# owner: root              # 文件拥有者（对应 ls -l 第三列）
# group: root              # 文件所属组（对应 ls -l 第四列）
user::rwx                  # 空用户名 → 文件拥有者（root）的权限
user:vbird1:r-x            # 针对 vbird1 单独设置的权限
group::r--                 # 文件所属组的权限
group:mygroup1:r-x         # 针对 mygroup1 单独设置的权限
mask::r-x                  # 有效权限上限（见下方说明）
other::r--                 # 其他人的权限
```

当文件设置了 ACL 后，`ls -l` 权限字符串末尾会出现 `+` 号：

``` bash
-rwxr-xr--+ 1 root root 0 Jul 21 17:33 acl_test1
#          ↑ 有 + 说明此文件带有 ACL 条目
```

### setfacl：设置 ACL

#### 针对用户授权

``` bash
# 为 alice 设置 rwx
setfacl -m u:alice:rwx file.txt

# 为 bob 设置只读
setfacl -m u:bob:r-- file.txt

# 明确拒绝（权限字段用 - 而非留空）
setfacl -m u:eve:- file.txt

# 空用户名代表文件拥有者本身
setfacl -m u::rwx file.txt
```

#### 针对组授权

``` bash
# 为 devteam 组设置 rw
setfacl -m g:devteam:rw file.txt
```

#### 默认 ACL（目录继承）

在**目录**上设置默认 ACL 后，该目录下**新建的文件和子目录**会自动继承这些规则：

``` bash
# 让 alice 在 /srv/project 下新建的所有文件都默认有 rx 权限
setfacl -m d:u:alice:rx /srv/project/

# -d 和 -m 可以合并写，效果相同
setfacl -d -m u:alice:rx /srv/project/
```

设置后，`getfacl` 输出中会出现 `default:` 前缀的条目：

``` bash
default:user::rwx
default:user:alice:r-x    # 继承规则
default:group::rwx
default:mask::rwx
default:other::---
```

#### 删除与清除

``` bash
# 删除 alice 的 ACL 条目（不加权限字段）
setfacl -x u:alice file.txt

# 删除 alice 的默认 ACL 条目
setfacl -x d:u:alice /srv/project/

# 删除所有 ACL 条目，恢复传统权限
setfacl -b file.txt

# 仅删除所有默认 ACL
setfacl -k /srv/project/
```

#### 递归与批量

``` bash
# 递归设置目录下所有文件
setfacl -R -m u:alice:rx /srv/project/

# 从一个目录复制 ACL 到另一个目录
getfacl /srv/dir1 | setfacl --set-file=- /srv/dir2
```

### mask 的作用

ACL `mask` 是**实际生效权限的上限**，它约束的对象是除文件 owner 和 others 以外的所有 ACL 条目（包括命名用户和组）。

``` bash
# 将 mask 压缩为只读，vbird1 的 r-x 中 x 不再生效
setfacl -m m:r acl_test1

$ getfacl acl_test1
user::rwx
user:vbird1:r-x   #effective:r--   # 与 mask 取交集后只剩 r
group::r--
mask::r--                          # 上限是 r
other::r--
```

!!! warning "setfacl 会自动更新 mask"

    每次用 `-m` 修改命名用户/组的权限时，`mask` 会被自动更新为所有命名权限的并集。手动设置 `mask` 之后，若再次 `setfacl -m u:...`，`mask` 会再次被覆盖。生产中通常将 `mask` 设为 `rwx`，再单独限制各用户/组。

## SUID（Set User ID）

当你执行 `/usr/bin/passwd` 修改密码时，这个普通用户凭什么能写入只有 root 才能访问的 `/etc/shadow`？答案就是 SUID。

SUID 设置在**可执行文件**上，任何用户执行该文件时，**进程临时以文件拥有者的身份运行**，而非调用者本身。

``` bash
$ ls -l /usr/bin/passwd
-rwsr-xr-x 1 root root ... /usr/bin/passwd
#    ↑ s 表示 SUID（owner 的 x 位变成了 s）
```

### 设置 SUID

``` bash
chmod u+s /path/to/binary    # 符号法
chmod 4755 /path/to/binary   # 数字法（4xxx 前缀表示 SUID）
```

权限字符说明：

| 显示 | 含义 |
|------|------|
| `-rwsr-xr-x` | owner 有 `x` + SUID → 显示小写 `s`，正常生效 |
| `-rwSr-xr-x` | owner 无 `x` + SUID → 显示大写 `S`，通常是错误配置 |

### 审计系统中的 SUID 文件

``` bash
# 找出系统中所有设置了 SUID 的可执行文件
find / -perm -4000 -type f 2>/dev/null
```

!!! danger "SUID 是高危权限"

    随意在自定义脚本上设置 SUID 是重大安全漏洞。攻击者只需找到一个带 SUID 的可利用文件，即可完成提权。定期执行上面的 `find` 命令审计 SUID 文件清单。

## SGID（Set Group ID）

SGID 有两种完全不同的使用场景。

### 用于可执行文件

执行时**临时以文件所属组身份运行**（类似 SUID，但换成了组身份）。实际使用较少。

### 用于目录（常用！）

在目录上设置 SGID 后，该目录下**新建的文件和子目录自动继承目录的所属组**，而不是创建者的默认组。这对团队协作目录非常实用：

``` bash
# 创建共享目录并设置 SGID
chmod g+s /shared/project/
chmod 2775 /shared/project/   # 数字法（2xxx 前缀表示 SGID）

$ ls -ld /shared/project/
drwxrwsr-x 2 root devteam ... /shared/project/
#        ↑ s 表示目录 SGID 已设置

# 现在任何人在此目录下创建的文件，所属组自动变为 devteam
```

!!! tip "SGID 目录 + ACL 的组合"

    协作目录的标准做法：目录设 SGID（保证组归属一致）+ ACL（精细控制特定成员权限），两者互补。

## Sticky Bit

`/tmp` 目录权限是 `1777`，任何人都可以在里面创建文件——但为什么你不能删除别人的文件？因为它设置了 Sticky Bit。

Sticky Bit 设置在目录上时，即使你对该目录有 `w` 权限，也**只能删除自己创建的文件**，不能删除他人的文件。

``` bash
$ ls -ld /tmp
drwxrwxrwt 12 root root 4096 ... /tmp
#         ↑ t 表示 Sticky Bit（others 的 x 位变成了 t）

# 设置 Sticky Bit
chmod o+t /shared/dir/
chmod 1777 /shared/dir/    # 数字法（1xxx 前缀表示 Sticky Bit）
```

| 显示 | 含义 |
|------|------|
| `drwxrwxrwt` | others 有 `x` + Sticky → 小写 `t`，正常生效 |
| `drwxrwxrwT` | others 无 `x` + Sticky → 大写 `T`，权限存在但无执行 |

## 特殊权限数字速查

| 特殊权限 | 数字前缀 | 对文件的效果 | 对目录的效果 |
|---------|---------|------------|------------|
| SUID | `4` | 执行时以 owner 身份运行 | 无意义 |
| SGID | `2` | 执行时以所属组身份运行 | 新建文件自动继承目录的所属组 |
| Sticky Bit | `1` | 无意义 | 只有文件 owner 能删除自己的文件 |

组合示例：`chmod 6755 file`（SUID + SGID = 4+2 = 6），`chmod 3777 dir`（SGID + Sticky = 2+1 = 3）。

## chattr / lsattr（不可变属性）

`chattr` 提供了一层超越权限系统的保护——即使是 `root` 也无法直接修改或删除设置了 `+i` 的文件。

``` bash
# 设置不可变（immutable）：不能删除、修改、重命名、创建硬链接
chattr +i /etc/resolv.conf

# 设置只追加（append-only）：只能向文件末尾追加，不能覆盖
chattr +a /var/log/app.log

# 查看文件属性
lsattr file.txt
lsattr -R /etc/

# 解除属性
chattr -i /etc/resolv.conf
chattr -a /var/log/app.log
```

`lsattr` 输出示例：

``` bash
$ lsattr /etc/resolv.conf
----i--------e-- /etc/resolv.conf
#    ↑ i = immutable
#              ↑ e = extent（ext4 默认，通常无需关注）
```

常用属性速查：

| 属性 | 全称 | 作用 |
|------|------|------|
| `i` | immutable | 不可删除/修改/重命名/硬链接，root 也不行 |
| `a` | append-only | 只能追加内容，不能覆盖或删除，适合日志文件 |
| `s` | secure delete | 删除时用 0 覆盖磁盘数据（安全删除） |
| `u` | undeletable | 删除后内容仍保留，可恢复 |
| `c` | compressed | 文件在磁盘上自动压缩存储 |

!!! tip "防止关键配置被覆盖"

    `chattr +i /etc/resolv.conf` 可以防止 DHCP 客户端或某些脚本自动覆盖 DNS 配置。修改前记得先 `chattr -i` 解除保护。

## 发行版差异

=== "Debian / Ubuntu"

    ``` bash
    # 安装 ACL 工具（通常已预装）
    apt install acl

    # 确认 ext4 分区已启用 ACL 挂载选项
    tune2fs -l /dev/sda1 | grep "Default mount options"
    # 应包含 acl 字样

    # 若未启用，永久开启（需重新挂载生效）
    tune2fs -o acl /dev/sda1
    ```

    ext4 在 Debian/Ubuntu 上默认启用 ACL，大多数情况下无需额外操作。

=== "RHEL / Rocky / AlmaLinux"

    ``` bash
    # RHEL 8+ 默认使用 XFS，XFS 原生支持 ACL，无需任何配置
    # 确认当前文件系统类型
    df -T /

    # ext4 分区同样支持，永久启用方式
    tune2fs -o acl /dev/sda1
    ```

    RHEL 8+ 的根文件系统通常是 XFS，ACL 开箱即用。`dmesg | grep -i acl` 可以看到 `SGI XFS with ACLs` 字样。



## 扩展属性（xattr）

传统文件元数据（权限/时间/大小）是固定字段，无法存储自定义信息。当你需要给文件附加安全标签、版本号、审计备注时，这些字段无处安放——这就是扩展属性（Extended Attributes，xattr / EA）存在的理由。

xattr 允许在文件 inode 上附加任意键值对，格式为 `namespace.name = value`，完全独立于文件内容本身。

### 命名空间（Namespace）

xattr 按命名空间划分用途，共有 4 类：

| 命名空间 | 用途 | 权限要求 |
|----------|------|----------|
| `user.*` | 普通用户自定义元数据（最常用） | 文件读权限（读）/ 写权限（写） |
| `security.*` | SELinux/AppArmor 安全标签、文件能力 | 系统/安全模块管理 |
| `system.*` | 内核使用，如 POSIX ACL 内部存储（`system.posix_acl_access`） | 内核内部 |
| `trusted.*` | 特权进程专用 | `CAP_SYS_ADMIN` |

!!! tip "user 命名空间的限制"

    `user.*` 只能用于**普通文件和目录**，不可用于符号链接、设备文件、套接字或 FIFO。
    此外，若目录设置了 sticky 位（如 `/tmp`）且非当前用户所有，普通进程不能在其上创建 `user.*` EA。

### 基本操作命令

``` bash title="查看扩展属性"
# 查看文件的所有 user.* xattr（默认只显示 user 命名空间）
getfattr -d filename

# 查看指定属性值
getfattr -n user.comment filename

# 列出所有命名空间的 xattr 键名（-m 指定正则，- 匹配所有）
getfattr --match=- -d filename
```

``` bash title="设置与删除扩展属性"
# 设置 user 命名空间属性
setfattr -n user.comment -v "reviewed by alice" filename

# 设置版本标记
setfattr -n user.version -v "1.2.0" filename

# 删除属性
setfattr -x user.comment filename
```

``` bash title="SELinux 安全上下文查看（security 命名空间）"
# 查看文件的 SELinux 上下文（security.selinux）
ls -Z filename

# 通过 getfattr 直接读取 security.selinux 原始值
getfattr -n security.selinux filename
```

### 文件系统支持

| 文件系统 | xattr 支持 | 备注 |
|----------|-----------|------|
| ext4 | 需 `user_xattr` 挂载选项 | 现代 Linux 发行版默认已启用 |
| XFS | 默认支持，无需额外配置 | RHEL 8+ 根文件系统首选 |
| Btrfs | 原生支持 | 备份工具大量使用 `user.*` |
| JFS | 支持，总量上限 128 KB | 另有 `os2` 命名空间 |

**ext4 单条 xattr 大小不能超过一个逻辑块**（通常 4 KB），且单个文件所有 EA 总量不能超过一个块大小（1024 / 2048 / 4096 字节，视格式化参数而定）。

### user_xattr 挂载选项

=== "Debian/Ubuntu"

    ext4 在 Debian/Ubuntu 上**默认启用** `user_xattr`，通常无需手动配置。若需显式开启，在 `/etc/fstab` 中为对应分区添加 `user_xattr` 挂载选项：

    ``` bash title="检查与启用 user_xattr（Debian/Ubuntu）"
    # 查看当前挂载选项（ext4 默认含 user_xattr）
    mount | grep "on / "

    # 若需显式添加，编辑 /etc/fstab 对应行
    # UUID=xxxx  /  ext4  defaults,user_xattr  0 1

    # 重新挂载使配置生效
    mount -o remount /
    ```

=== "Red Hat / RHEL"

    RHEL 8+ 默认使用 XFS 作为根文件系统，XFS **原生支持** xattr，无需任何配置。

    ``` bash title="检查与启用 user_xattr（RHEL/Rocky）"
    # 确认当前文件系统类型（XFS 无需操作）
    df -T /

    # 若根分区为 ext4，永久启用方式（需重启或重挂载）
    tune2fs -o acl,user_xattr /dev/sda1

    # XFS 验证（应看到 XFS 相关信息）
    dmesg | grep -i xfs
    ```

### 典型应用场景

- **安全标签**：SELinux/AppArmor 将上下文信息存储在 `security.*` 中，`ls -Z` 即可查看
- **版本与审计**：用 `user.version`、`user.audit_status` 标记文件状态，无需修改文件内容
- **备份元数据**：Btrfs 增量备份工具使用 `user.*` 记录快照信息
- **容器运行时**：Docker 等运行时使用 xattr 存储镜像层和容器配置信息
- **POSIX ACL 存储**：`setfacl` 设置的 ACL 规则实际存储在 `system.posix_acl_access` 中

### 注意事项：复制与归档

xattr 不会自动随文件一起传递，常见工具需显式指定：

``` bash title="保留 xattr 的复制与归档"
# cp 默认不复制 xattr，需加 --preserve=xattr
cp --preserve=xattr src.txt dst.txt

# rsync 归档时保留 xattr
rsync -aX src/ dst/

# tar 归档时保留 xattr（打包与解包均需指定）
tar --xattrs -czf archive.tar.gz files/
tar --xattrs -xzf archive.tar.gz
```

!!! warning "跨文件系统复制"

    将文件复制到不支持 xattr 的文件系统（如 FAT32、NFS v3）时，xattr 会静默丢失，不会报错。跨服务器传输重要 xattr 时，优先使用 `rsync -aX` 并确认目标文件系统支持。

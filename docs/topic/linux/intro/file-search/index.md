---
title: 文件与命令查找
---

# 文件与命令查找

在 Linux 日常维护中，"找东西"是高频需求：找可执行文件在哪儿、找配置文件路径、找昨天改过的日志……不同场景下，合适的工具能节省大量时间。本文介绍四种查找工具及 `grep` 在内容搜索中的基本用法。

**本文你会学到**：

- 四种查找工具的适用场景与速度差异
- `which` / `whereis` 快速定位命令和手册页
- `locate` 数据库查找与 `updatedb` 更新
- `find` 强大的实时扫描与条件组合
- `xargs` 配合 `find` 批量处理
- `grep` 在文件内容中搜索关键字

## 四种工具速览

当你需要"找东西"时，优先选速度快的工具；只有在前者无法满足需求时，才动用更慢但更强大的 `find`。

| 工具 | 搜索范围 | 原理 | 速度 | 适用场景 |
|------|---------|------|------|---------|
| `which` | `$PATH` 中的目录 | 遍历 PATH 环境变量 | 极快 | 确认命令是否可用及其路径 |
| `whereis` | 特定系统目录 | 预设目录列表扫描 | 快 | 找二进制/手册页/源码 |
| `locate` | 整个文件系统 | 数据库（`mlocate.db`） | 极快 | 按文件名模糊搜索 |
| `find` | 指定路径（可为整个系统） | 实时遍历文件系统 | 慢 | 按名称、权限、时间、大小等复杂条件 |

!!! tip "经验法则"

    优先使用 `whereis` → `locate` → 实在找不到再用 `find`。`find` 会直接读硬盘，在机械硬盘上可能需要等待很久。

## which — 确认命令可执行文件位置

### 当你不确定命令装在哪里

你输入 `ls`，系统能执行，但它究竟来自 `/usr/bin/ls` 还是 `/bin/ls`？`which` 就是用来回答这个问题的。

`which` 依据 `$PATH` 环境变量逐目录查找**可执行文件**，找到第一个匹配项就停下。

``` bash
# 基本用法
which command

# 列出 PATH 中所有同名可执行文件（不只第一个）
which -a command
```

``` bash
# 找到 ifconfig 的路径
$ which ifconfig
/sbin/ifconfig

# 找出所有叫 python3 的可执行文件
$ which -a python3
/usr/bin/python3
/usr/local/bin/python3
```

!!! warning "which 找不到内置命令"

    `which history` 会报"找不到"——因为 `history` 是 bash 的**内置命令**，不是独立的可执行文件，不存在于 `$PATH` 的任何目录中。对于内置命令，应使用 `type` 来查询：

    ```bash
    $ type history
    history is a shell builtin
    ```

## whereis — 查找二进制、手册页与源码

### 当你需要同时找到命令和它的帮助文档

`whereis` 不止找可执行文件，还能顺带找到对应的 man page 和源码，一条命令搞定。

与 `find` 不同，`whereis` 只扫描几个**预设的系统目录**（如 `/bin`、`/sbin`、`/usr/share/man` 等），速度快但覆盖范围有限。

``` bash
whereis [-bmsu] 文件名
```

| 选项 | 说明 |
|------|------|
| `-b` | 只找二进制文件（binary） |
| `-m` | 只找手册页（man page） |
| `-s` | 只找源码文件（source） |
| `-u` | 找不在以上三类中的其他文件 |
| `-l` | 列出 whereis 会搜索的目录清单 |

``` bash
# 找 passwd 相关的所有文件
$ whereis passwd
passwd: /usr/bin/passwd /etc/passwd /usr/share/man/man1/passwd.1.gz /usr/share/man/man5/passwd.5.gz

# 只找手册页
$ whereis -m passwd
passwd: /usr/share/man/man1/passwd.1.gz /usr/share/man/man5/passwd.5.gz

# 查看 whereis 搜索了哪些目录
$ whereis -l
```

!!! note "whereis 不是全局搜索"

    若软件安装到非标准目录（如 `/opt/myapp`），`whereis` 可能找不到，此时需要用 `find`。

## locate — 数据库极速查找

### 当你只记得文件名的片段

`locate` 的查找速度极快，因为它不扫描真实文件系统，而是查询预先建立好的**文件名数据库**（`/var/lib/mlocate/mlocate.db`）。

``` bash
locate [-icl] keyword
```

| 选项 | 说明 |
|------|------|
| `-i` | 忽略大小写 |
| `-c` | 只输出匹配数量，不列文件名 |
| `-l N` | 最多输出 N 行 |
| `-S` | 显示数据库统计信息 |
| `-r` | 使用正则表达式匹配 |

``` bash
# 找所有含 passwd 的文件，只显示前 5 条
$ locate -l 5 passwd
/etc/passwd
/etc/passwd-
/etc/pam.d/passwd
/etc/security/opasswd
/usr/bin/gpasswd

# 不区分大小写查找
$ locate -i README

# 查看数据库统计
$ locate -S
Database /var/lib/mlocate/mlocate.db:
  8,086 directories
  109,605 files
```

### 更新数据库：updatedb

`locate` 的数据库默认每天自动更新一次（通过 cron 任务）。如果你刚创建了新文件，在下次自动更新前 `locate` 找不到它——此时手动执行 `updatedb` 即可：

``` bash
# 手动更新（需要 root 权限，可能需要几分钟）
sudo updatedb
```

!!! note "不同发行版的 locate 实现"

    locate 有多种实现，安装命令略有不同：

=== "Debian/Ubuntu"

    ```bash
    # 安装 plocate（新版，推荐）
    sudo apt install plocate

    # 或安装 mlocate（旧版）
    sudo apt install mlocate
    ```

=== "Red Hat/RHEL/CentOS"

    ```bash
    # 安装 mlocate
    sudo dnf install mlocate
    # 或
    sudo yum install mlocate
    ```

## find — 实时全功能扫描

### 当其他工具都找不到时

`find` 是 Linux 中最强大的查找工具，直接遍历文件系统，支持几乎所有你能想到的条件。代价是速度慢、会读磁盘。

``` bash
find [搜索路径] [条件选项] [执行动作]
```

### find 查询条件

#### 按文件名查找

``` bash
# 精确匹配文件名（大小写敏感）
find /etc -name "passwd"

# 使用通配符匹配
find /etc -name "*httpd*"

# 忽略大小写（-iname）
find /home -iname "readme.txt"
```

#### 按文件类型查找

| 类型参数 | 说明 |
|---------|------|
| `-type f` | 普通文件 |
| `-type d` | 目录 |
| `-type l` | 符号链接 |
| `-type b` | 块设备文件 |
| `-type c` | 字符设备文件 |
| `-type s` | socket 文件 |
| `-type p` | FIFO（管道）文件 |

``` bash
# 找 /run 下所有 socket 文件
find /run -type s

# 找所有符号链接
find /usr/bin -type l
```

#### 按修改时间查找

`find` 的时间参数以**天**为单位，正负号含义不同：

| 参数 | 说明 |
|------|------|
| `-mtime n` | 恰好在 n 天前那一天（第 n\~n+1 天）内修改过 |
| `-mtime +n` | n 天前以前（不含第 n 天）修改过 |
| `-mtime -n` | n 天以内（含第 n 天）修改过 |
| `-newer file` | 比指定文件更新 |

``` bash
# 过去 24 小时内修改过的文件（0 = 今天）
find / -mtime 0

# 7 天以内修改过的文件
find /var/log -mtime -7

# 比 /etc/passwd 更新的文件
find /etc -newer /etc/passwd

# 恰好 4 天前那一天修改的文件
find /var -mtime 4
```

!!! tip "atime / ctime / mtime 区别"

    - `atime`（access time）：最后访问时间
    - `mtime`（modification time）：最后**内容**修改时间（最常用）
    - `ctime`（change time）：最后**属性**变更时间（包括权限、所有者等）

#### 按文件大小查找

``` bash
find [路径] -size [+-]SIZE
```

大小单位：`c`（字节）、`k`（1024 字节）、`M`（兆字节）、`G`（吉字节）

``` bash
# 大于 1MB 的文件
find / -size +1M

# 小于 10KB 的文件
find /tmp -size -10k

# 恰好 100 字节
find . -size 100c
```

#### 按权限查找

``` bash
# 权限恰好等于 755
find /usr/bin -perm 755

# 权限包含所有指定位（-mode：所有位都满足）
find /usr/bin -perm -4000    # 包含 SUID

# 权限包含任意指定位（/mode：任一位满足）
find /usr/bin /usr/sbin -perm /6000    # 含 SUID 或 SGID
```

#### 按用户与组查找

``` bash
# 找属于某个用户的所有文件
find /home -user alice

# 找属于某个组的文件
find /project -group devteam

# 找没有对应用户的文件（孤儿文件，用于清理已删除账号的残留）
find / -nouser

# 找没有对应组的文件
find / -nogroup
```

### find 动作与组合

#### 执行动作：-exec 与 -delete

找到文件后，`find` 可以直接对结果执行操作：

``` bash
# -exec：对每个结果执行命令（{} 代表当前文件，\; 表示命令结束）
find /usr/bin /usr/sbin -perm /7000 -exec ls -l {} \;

# -delete：直接删除找到的文件（谨慎使用！）
find /tmp -name "*.tmp" -mtime +7 -delete

# -print：打印到屏幕（默认行为，一般可省略）
find /etc -name "*.conf" -print
```

!!! warning "-exec 使用注意"

    `-exec` 后跟的命令**不支持命令别名**，例如不能用 `ll`，必须用 `ls -l`。

    `{}` 代表每一个查找结果，`\;` 是命令结束符（`;` 在 bash 中有特殊含义，需转义）。

#### 逻辑组合

多个条件可以用逻辑运算符组合：

``` bash
# AND（默认，两个条件并列即为 AND）
find /var -type f -name "*.log"

# OR（-o）：类型是目录 OR 文件名含 conf
find /etc \( -type d -o -name "*.conf" \)

# NOT（!）：找不是 .conf 的普通文件
find /etc -type f ! -name "*.conf"
```

## xargs — 配合 find 批量处理

`-exec` 每找到一个文件就启动一次子进程，文件很多时效率低。`xargs` 会将 `find` 的输出**合并**后一次性传给命令，性能更好：

``` bash
# 等价于 -exec，但效率更高
find /etc -name "*.conf" | xargs ls -l

# 文件名含空格时，用 -print0 + xargs -0 避免被空格截断
find /home -name "*.log" -print0 | xargs -0 rm -f

# 统计所有 .py 文件的行数
find . -name "*.py" | xargs wc -l
```

!!! tip "何时用 -exec，何时用 xargs"

    - 文件数量少：`-exec` 更直观
    - 文件数量多或文件名含空格：`xargs -0`（配合 `-print0`）更安全高效
    - 需要将多个结果作为一个命令的多个参数：只能用 `xargs`

## grep — 在文件内容中搜索

上述工具都是按**文件名**查找，`grep` 则是在**文件内容**中搜索关键字：

``` bash
# 在单个文件中搜索
grep "keyword" /etc/passwd

# 递归搜索目录下所有文件（-r）
grep -r "ServerName" /etc/apache2/

# 忽略大小写（-i）
grep -i "error" /var/log/syslog

# 显示行号（-n）
grep -n "root" /etc/passwd

# 只显示匹配的文件名（-l）
grep -rl "TODO" ./src/
```

结合 `find`，可以在"特定条件的文件"中搜索内容：

``` bash
# 在 7 天内修改的 .conf 文件中搜索 "timeout"
find /etc -name "*.conf" -mtime -7 | xargs grep -l "timeout"
```

!!! note "grep 的正则表达式"

    `grep` 支持强大的正则表达式匹配（`-E` 使用扩展正则，`-P` 使用 Perl 正则）。详细用法见「正则表达式」页面。


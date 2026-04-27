---
title: 文件内容查看工具
---

# 文件内容查看工具

当你拿到一个陌生的 Linux 系统，第一件事往往是"这个文件里写的什么？"。不同场景有不同的最佳工具——小文件直接 `cat`，大日志用 `less` 翻页，只看末尾用 `tail`，遇到二进制文件就要靠 `od`。本文覆盖日常工作中最常用的文件内容查看命令。

**本文你会学到**：

- `cat` / `tac` / `nl`：把文件直接打印到屏幕
- `more` / `less`：分页翻阅大文件
- `head` / `tail`：只取文件头部或尾部几行
- `od`：以八进制/十六进制查看二进制文件
- `file` / `iconv`：检测与转换文件编码
- 文件的三种时间戳：`atime` / `mtime` / `ctime`

## 直接打印到屏幕

### cat — 全文输出

`cat`（Concatenate）是最基础的文件查看命令，一口气把整个文件输出到屏幕。

``` bash
cat [-AbEnTv] 文件名
```

常用选项：

- `-n`：所有行都编行号（包括空行）
- `-b`：只对非空行编行号
- `-A`：等同于 `-vET`，显示所有不可见字符（`[Tab]` 显示为 `^I`，行尾显示 `$`）
- `-v`：显示非打印字符

``` bash
# 显示文件内容
cat /etc/issue

# 加行号
cat -n /etc/issue

# 显示特殊字符（排查 Tab / 换行符问题很有用）
cat -A /etc/man_db.conf
```

!!! tip "什么时候用 cat -A？"

    Windows 和 Linux 的换行符不同：Windows 是 `^M$`，Linux 是 `$`。当你把 Windows 文件传到 Linux 运行出错时，用 `cat -A` 能立刻看到多余的 `^M`。

`cat` 的缺点：文件超过一屏就看不过来了，这时换 `less`。

### tac — 反向输出

`tac` 是 `cat` 的反写，从最后一行到第一行反向输出。

``` bash
tac /etc/issue
# 先显示最后一行，再往前逐行输出
```

场景：查看日志文件时，最新条目往往在文件末尾，用 `tac` 可以把最新内容翻到最前面。

### nl — 带格式行号

`nl` 专门用来打印带行号的文件，比 `cat -n` 的格式控制更丰富。

``` bash
nl [-bnw] 文件名
```

常用选项：

- `-b a`：空行也加行号（同 `cat -n`）
- `-b t`：空行不加行号（默认）
- `-n ln`：行号左对齐
- `-n rn`：行号右对齐，不补零
- `-n rz`：行号右对齐，**补零**
- `-w <数字>`：行号字段宽度（默认 6 位）

``` bash
# 默认：空行不编号
nl /etc/issue

# 空行也编号，行号补零，字段宽度 3 位
nl -b a -n rz -w 3 /etc/issue
# 输出示例：
# 001  \S
# 002  Kernel \r on an \m
# 003
```

`cat -n` vs `nl` 的区别：`cat -n` 永远对所有行编号，而 `nl` 默认跳过空行。

## 分页翻阅

### more — 向下翻页

当文件内容超过一屏时，`more` 会暂停并等待你的指令。

``` bash
more /etc/man_db.conf
# 底部会显示：--More--(28%)
```

`more` 的按键：

- `Space`：向下翻一页
- `Enter`：向下翻一行
- `/字串`：向下搜索关键字
- `n`：重复上次搜索
- `b` / `Ctrl+b`：向上翻页（仅对文件有效）
- `q`：退出

`more` 的局限：**只能向下翻，不能往回看**。遇到大文件或需要来回跳转时，请用 `less`。

### less — 日常首选的分页工具

`less` 是 `more` 的增强版，支持双向翻页，是**日常工作中最重要的分页工具**。`man` 命令本身也是用 `less` 来展示内容的。

``` bash
less /etc/man_db.conf
# 底部显示冒号 : 等待输入
```

`less` 的核心快捷键：

| 按键 | 功能 |
|------|------|
| `Space` / `PageDown` | 向下翻一页 |
| `PageUp` / `b` | 向上翻一页 |
| `↑` / `↓` 或 `j` / `k` | 逐行上下移动 |
| `/字串` | 向下搜索 |
| `?字串` | 向上搜索 |
| `n` | 继续查找下一个（与 `/` 或 `?` 方向一致） |
| `N` | 反向查找（方向相反） |
| `g` | 跳到文件第一行 |
| `G` | 跳到文件最后一行 |
| `q` | 退出 |

!!! tip "less 实战技巧"

    查看日志时，按 `G` 直接跳到末尾（最新条目），然后用 `?ERROR` 向上搜索错误信息，用 `N` 反复跳到上一条匹配。

    ``` bash
    # 追踪日志（类似 tail -f 的交互版）
    less +F /var/log/syslog        # 开启 follow 模式（Ctrl+C 停止，G 转到末尾）

    # 检查多个文件
    less file1 file2 file3
    # :n — 下一个文件
    # :p — 前一个文件
    # :f — 显示当前文件名
    ```

## 截取头尾行

### head — 取前 N 行

``` bash
head [-n number] 文件名
```

- 默认显示前 **10 行**
- `-n 20`：显示前 20 行
- `-n -100`：显示除最后 100 行以外的所有内容

``` bash
head /etc/man_db.conf          # 前 10 行
head -n 20 /etc/man_db.conf    # 前 20 行
head -n -100 /etc/man_db.conf  # 去掉最后 100 行后的内容
```

### tail — 取后 N 行

``` bash
tail [-n number] [-f] 文件名
```

- 默认显示最后 **10 行**
- `-n 20`：显示最后 20 行
- `-n +100`：从第 100 行起显示到文件末尾
- `-f`：持续监视文件新增内容（直到按 `Ctrl+C`）

``` bash
tail /etc/man_db.conf           # 最后 10 行
tail -n 20 /etc/man_db.conf     # 最后 20 行
tail -n +100 /etc/man_db.conf   # 第 100 行到文件末尾

# 实时追踪日志
tail -f /var/log/messages
```

!!! note "-f 与 -F 的区别"

    `-f` 跟踪的是**文件描述符**。当日志发生轮转（logrotate 把旧文件重命名、创建新文件）时，`-f` 会继续跟踪旧文件，看不到新写入的内容。

    `-F`（大写）跟踪的是**文件名**。检测到文件名对应的 inode 变化后，会自动重新打开新文件，适合监控会发生轮转的日志。

    **生产环境监控日志推荐用 `tail -F`。**

### 组合截取指定行范围

取第 11 到第 20 行：

``` bash
head -n 20 /etc/man_db.conf | tail -n 10
```

带行号取第 11 到第 20 行：

``` bash
cat -n /etc/man_db.conf | head -n 20 | tail -n 10
```

`|`（管线）的意思是：把前一个命令的输出作为后一个命令的输入。

## 查看二进制文件

### od — 八进制/十六进制转储

纯文本工具（`cat`、`less` 等）遇到二进制文件只会输出乱码。`od`（Octal Dump）可以将任意文件以指定进制打印出来。

``` bash
od [-t TYPE] 文件名
```

`-t` 类型：

- `a`：用默认字符名称输出
- `c`：用 ASCII 字符输出
- `d[size]`：十进制，每个整数占 `size` 字节
- `o[size]`：八进制（默认）
- `x[size]`：十六进制

``` bash
# 用 ASCII 字符方式查看可执行文件
od -t c /usr/bin/passwd

# 同时显示八进制数值和 ASCII 对照（排查特殊字符很有用）
od -t oCc /etc/issue
```

输出示例：

``` text
0000000 134 123 012 113 145 162 156 145 154 040 134 162 040 157 156 040
        \   S  \n   K   e   r   n   e   l       \   r       o   n
```

最左列是从文件开头起的字节偏移量（八进制），之后两行分别是数值和对应的 ASCII 字符。

!!! tip "快速查 ASCII 码对照"

    ``` bash
    echo password | od -t oCc
    ```

    无需翻书，直接得到字符串每个字母的 ASCII 码。

## 文件编码

### file — 检测文件类型与编码

``` bash
file 文件名
```

``` bash
file ~/.bashrc
# /root/.bashrc: ASCII text

file /usr/bin/passwd
# /usr/bin/passwd: setuid ELF 64-bit LSB shared object, x86-64 ...

file /var/lib/mlocate/mlocate.db
# /var/lib/mlocate/mlocate.db: data
```

`file` 命令可以快速判断一个文件是文本、二进制、压缩包还是数据文件。同样适用于判断 `tar` 包使用了哪种压缩算法。

### iconv — 字符编码转换

当文件显示乱码时，通常是编码不匹配。`iconv` 可以在不同编码之间转换。

``` bash
# 查看系统支持的编码列表
iconv -l

# 将 GBK 文件转换为 UTF-8
iconv -f GBK -t UTF-8 input.txt -o output.txt

# 不生成新文件，直接输出转换结果
iconv -f GBK -t UTF-8 input.txt

# 忽略无法转换的字符
iconv -f GBK -t UTF-8//IGNORE input.txt

# 批量转换目录下所有 .txt 文件
for f in *.txt; do
  iconv -f GBK -t UTF-8 "$f" > "${f%.txt}_utf8.txt"
done
```

!!! tip "乱码排查流程"

    先用 `file -i` 检测实际编码，再用 `iconv -f <原编码> -t UTF-8` 转换。常见场景：Windows 上保存的 GBK 文件传到 Linux 后乱码。

## 三种时间戳

Linux 为每个文件记录三种时间，搞混了会让 `find` 搜索、`make` 构建等操作出现意外结果。

| 时间戳 | 触发条件 | `ls` 查看方式 |
|--------|----------|--------------|
| `mtime`（modification time）| 文件**内容**被修改 | `ls -l`（默认） |
| `atime`（access time）| 文件**内容被读取**（如 `cat`） | `ls -l --time=atime` |
| `ctime`（status time）| 文件**属性/权限**被修改 | `ls -l --time=ctime` |

``` bash
# 同时查看三种时间（分号分隔多条命令）
date; \
  ls -l /etc/man_db.conf; \
  ls -l --time=atime /etc/man_db.conf; \
  ls -l --time=ctime /etc/man_db.conf
```

!!! warning "ctime 无法被手动伪造"

    `mtime` 和 `atime` 可以用 `touch -d` 或 `touch -t` 修改，但 `ctime` 永远记录的是"上一次状态变更的真实时间"，`touch` 改时间本身也会更新 `ctime`。这一特性常用于安全审计。

### touch — 修改时间戳 / 创建空文件

``` bash
touch [-acdmt] 文件名
```

常用选项：

- `-a`：只更新 `atime`
- `-m`：只更新 `mtime`
- `-d "2 days ago"`：将 `atime` 和 `mtime` 修改为指定日期
- `-t YYYYMMDDHHMM`：将 `atime` 和 `mtime` 修改为指定时间点
- `-c`：若文件不存在，**不创建**新文件（默认会创建空文件）

``` bash
# 创建空文件（最常见用途）
touch newfile.txt

# 将时间改为两天前
touch -d "2 days ago" somefile

# 将时间改为指定时间点
touch -t 202406150202 somefile
```

!!! note "touch 的两大用途"

    - **创建空文件**：快速建立占位文件或触发 Makefile 依赖重新构建
    - **修改时间戳**：让文件"看起来"被更新了，常用于调试构建工具或回归测试

## 高级文件查看工具

### xxd — 十六进制转储

以十六进制和 ASCII 双列格式查看二进制文件，比 `od` 更直观。

``` bash
xxd file.bin | head

# 输出示例：
# 00000000: 7f45 4c46 0102 0101 0000 0000 0000 0000  .ELF............
# 00000010: 0200 3e00 0100 0000 8060 0000 0000 0000  ..>......`......
```

左列是十六进制偏移，中间是 HEX 字节，右列是 ASCII 对照（不可打印字符显示为 `.`）。

``` bash
# 显示文件前 256 字节
xxd -l 256 /usr/bin/ls

# 反向操作：用 xxd 生成的输出恢复二进制文件
xxd -r output.txt > recovered.bin
```

### strings — 从二进制提取可读文本（详细版）

`strings` 扫描二进制文件并提取所有连续的可打印 ASCII 字符序列，用于反向工程、查找版本信息或调试。

``` bash
# 提取可执行文件中的所有字符串
strings /usr/bin/curl | head

# 最少 10 个字符的字符串
strings -n 10 /usr/bin/curl

# 显示十六进制偏移
strings -t x /usr/bin/curl | head

# 在二进制中搜索特定文本（如检查密钥）
strings /var/lib/database.db | grep -i password

# 检查可执行文件中的依赖库或版本号
strings /usr/bin/gcc | grep "gcc version"
```

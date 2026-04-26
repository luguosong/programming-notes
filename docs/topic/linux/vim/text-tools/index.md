---
title: 文本处理工具
---

# 文本处理工具

**本文你会学到**：

- `sort`：排序（字典序、数值序、自定义分隔符、多列排序）
- `uniq`：去重与统计重复行
- `cut`：按分隔符或列号截取字段
- `paste`：横向合并多个文件
- `wc`：统计行数、字数、字节数
- `tr`：字符级别的转换与删除
- `sed`：流式行处理、替换、删除、插入
- `awk`：字段级别的结构化处理与模式匹配
- 用管道组合多个工具解决实际问题
- 性能优化与常见陷阱

## 排序与去重

### sort — 排序

拿到一份数据，最先做的往往是排序。`sort` 默认按**字典序**逐字符比较，对于数字需要加 `-n` 才能按数值排。

``` bash
sort file                     # 默认字典序排序
sort -n file                  # 按数值排序（10 排在 9 之后）
sort -r file                  # 逆序
sort -u file                  # 排序并去重（等同于 sort | uniq）
sort -h file                  # 人类可读大小排序（1K < 2M < 3G）
```

**按列排序**（`-k` 指定列，列号从 1 开始）：

``` bash
sort -k 2 file                # 按第 2 列排序
sort -k 2,2 -k 1,1 file       # 主键第 2 列，次键第 1 列
sort -t: -k 3 -n /etc/passwd  # 按冒号分隔的第 3 列（UID）数值排序
```

!!! tip "字典序 vs 数值序"

    `sort 10 9` 字典序结果是 `10 9`（因为字符 `1` < `9`），加 `-n` 才能得到 `9 10`。处理数字列时务必加 `-n`。

### uniq — 去重

`uniq` 只能去除**相邻**的重复行，所以通常要先 `sort` 再 `uniq`。

``` bash
uniq file              # 去除相邻重复行
uniq -c file           # 在每行前面显示该行出现次数
uniq -d file           # 只显示重复的行
uniq -u file           # 只显示不重复（唯一）的行
uniq -i file           # 忽略大小写
```

**统计词频的经典组合**：

``` bash
# 统计出现频率，取最高的 10 个
sort file | uniq -c | sort -rn | head -10
```

## 字段截取与合并

### cut — 截取字段

当你只想要每行中的某几列或某几个字符时，`cut` 是最直接的工具。

``` bash
# 按分隔符截取字段（-d 指定分隔符，-f 指定字段号）
cut -d: -f1 /etc/passwd        # 取用户名（第 1 字段）
cut -d: -f1,3 /etc/passwd      # 取第 1 和第 3 字段（用户名 + UID）
cut -f2- file                  # 取第 2 字段到行尾（默认 Tab 分隔）

# 按字符位置截取
cut -c1-10 file                # 每行第 1~10 个字符
cut -c1,5,10 file              # 取第 1、5、10 个字符
```

!!! warning "cut 的局限"

    `cut` 不支持正则表达式，也无法处理连续多个空格作为分隔符的情况（那是 `awk` 的擅长领域）。

### paste — 横向合并

`paste` 和 `cut` 方向相反——把多个文件的同行内容横向拼在一起。

``` bash
paste file1 file2              # 横向合并两文件（默认 Tab 分隔）
paste -d, file1 file2          # 用逗号作为分隔符
paste -s file                  # 把文件的所有行合并为一行（行变列）
```

**典型场景**：你有两个文件，一个存姓名、一个存分数，用 `paste` 把它们对齐合并。

### join — 按键关联合并

`join` 类似 SQL 的 `JOIN`，根据共同的键字段合并两个文件（要求两个文件都按键字段排好序）：

``` bash
# 两个文件都按第 1 列排序后，按第 1 列合并
sort -k1 file1 > f1.sorted
sort -k1 file2 > f2.sorted
join f1.sorted f2.sorted

# 指定连接字段
join -1 2 -2 3 file1 file2    # file1 的第 2 列 join file2 的第 3 列
```

## 统计

### wc — 统计行数、字数、字节数

`wc`（Word Count）是最快的文件统计工具。

``` bash
wc file                        # 同时输出：行数  词数  字节数  文件名
wc -l file                     # 只输出行数
wc -w file                     # 只输出词数
wc -c file                     # 字节数
wc -m file                     # 字符数（正确处理多字节 UTF-8 字符）
```

**常用技巧**：

``` bash
# 统计当前目录下有多少个文件
ls | wc -l

# 统计代码行数
find . -name "*.java" | xargs wc -l

# 去掉注释和空行后统计有效代码行
grep -v '^\s*#' config.conf | grep -v '^$' | wc -l
```

## 字符转换

### tr — 字符级转换与删除

`tr` 只从标准输入读取，不接受文件参数，因此总是配合重定向或管道使用。它的强项是**字符级别**的批量转换。

``` bash
# 大小写转换
tr 'a-z' 'A-Z' < file          # 小写转大写
tr 'A-Z' 'a-z' < file          # 大写转小写

# 字符替换
echo "hello" | tr 'el' 'EL'    # 输出 hELLo

# 删除字符（-d）
tr -d '\n' < file              # 删除所有换行符（把多行合为一行）
tr -d '[:space:]' < file       # 删除所有空白字符

# 压缩重复字符（-s）
tr -s ' ' < file               # 多个连续空格压缩为一个

# 只保留可打印字符（-dc 表示删除补集）
tr -dc '[:print:]' < file
```

!!! tip "字符类速查"

    `tr` 支持 POSIX 字符类：`[:alpha:]`（字母）、`[:digit:]`（数字）、`[:space:]`（空白）、`[:upper:]`（大写）、`[:lower:]`（小写）、`[:print:]`（可打印字符）。

## 流编辑器 sed

### sed 的工作原理

`sed`（Stream Editor）逐行读取文件，对每一行执行你指定的**命令**，再输出结果。**不修改原文件**（除非加 `-i`）。

基本语法：

``` bash
sed [选项] '命令' 文件
sed [选项] -e '命令1' -e '命令2' 文件
```

常用选项：

- `-n`：安静模式，只输出被 `p` 命令显式打印的行
- `-e`：在命令行指定多条命令
- `-i`：直接修改文件（原地修改）
- `-E`（或 `-r`）：使用扩展正则表达式（ERE）

### 替换（s 命令）

替换是 `sed` 最常用的功能，语法：`s/模式/替换/标志`。

``` bash
sed 's/old/new/' file          # 每行只替换第一次匹配
sed 's/old/new/g' file         # 全部替换（g = global）
sed 's/old/new/gi' file        # 忽略大小写全部替换
sed 's/old/new/2' file         # 只替换每行第 2 次匹配
sed -E 's/[0-9]+/NUM/g' file   # 用 ERE 正则替换所有数字
```

**提取 IP 地址**（sed 链式处理）：

``` bash
ifconfig eth0 | grep 'inet ' \
  | sed 's/^.*inet //g' \
  | sed 's/ *netmask.*$//g'
```

### 删除与打印（d / p 命令）

``` bash
# 删除
sed '/pattern/d' file          # 删除匹配行
sed '/^#/d' file               # 删除注释行
sed '/^$/d' file               # 删除空行
sed '1,3d' file                # 删除第 1~3 行

# 打印指定行（配合 -n 避免重复输出）
sed -n '5,10p' file            # 打印第 5~10 行
sed -n '/start/,/end/p' file   # 打印两个模式之间的内容
```

**实战：清理配置文件注释和空行**：

``` bash
sed '/^[[:space:]]*#/d; /^$/d' /etc/nginx/nginx.conf
```

### 插入与追加（i / a / c 命令）

``` bash
sed '3a\new line' file          # 在第 3 行之后追加一行
sed '3i\new line' file          # 在第 3 行之前插入一行
sed '3c\replaced line' file     # 把第 3 行替换为新内容
```

### 原地修改（-i 选项）

``` bash
sed -i 's/old/new/g' file       # 直接修改文件（无备份）
sed -i.bak 's/old/new/g' file   # 先备份为 file.bak，再修改
```

!!! warning "sed -i 在 macOS 上的差异"

    macOS 的 `sed -i` 需要提供备份后缀（哪怕是空字符串）：`sed -i '' 's/old/new/g' file`。Linux 的 GNU sed 不需要。

### 多命令写法

``` bash
# 用 -e 串联
sed -e 's/foo/bar/' -e 's/baz/qux/' file

# 用分号分隔（等效）
sed 's/foo/bar/; s/baz/qux/' file
```

## 文本处理语言 awk

### awk 的工作模型

`awk` 把每一行自动拆分为若干**字段**（`$1`, `$2`, ..., `$NF`），你可以针对字段做条件过滤、计算和格式化输出。

基本结构：

```
awk 'BEGIN{初始化} /pattern/{动作} END{收尾}' file
```

**内置变量**：

| 变量 | 含义 |
|------|------|
| `$0` | 整行内容 |
| `$1` ~ `$NF` | 第 1 到第 NF 个字段 |
| `NF` | 当前行的字段数 |
| `NR` | 当前处理的行号（全局） |
| `FNR` | 当前文件内的行号 |
| `FS` | 输入字段分隔符（默认空白） |
| `OFS` | 输出字段分隔符（默认空格） |
| `RS` | 输入行分隔符（默认换行） |
| `ORS` | 输出行分隔符（默认换行） |

### 打印与过滤

``` bash
# 打印指定字段
awk '{print $1, $3}' file
awk -F: '{print $1}' /etc/passwd        # -F 指定分隔符

# 条件过滤
awk '$3 > 1000' /etc/passwd             # 第 3 字段大于 1000
awk '/pattern/' file                    # 正则匹配（打印匹配行）
awk '!/pattern/' file                   # 正则排除

# 多条件
awk '$1 > 100 && $2 == "error"' log
```

### 计算与统计

``` bash
# 求和
awk '{sum += $1} END{print sum}' file

# 求平均
awk '{sum += $1} END{print sum/NR}' file

# 统计每个 IP 出现次数（类似 sort | uniq -c）
awk '{count[$1]++} END{for(ip in count) print count[ip], ip}' access.log
```

### 格式化输出与字段重组

``` bash
# printf 格式化（左对齐 %-15s，右对齐 %5d）
awk -F: '{printf "%-15s %5d\n", $1, $3}' /etc/passwd

# 修改输出分隔符
awk -F: 'BEGIN{OFS="|"} {print $1, $3, $7}' /etc/passwd

# 对换字段顺序
awk -F: '{print $3, $1}' /etc/passwd
```

### BEGIN 和 END 块

``` bash
# 打印表头和脚注
awk 'BEGIN{print "Name\tUID"} \
     -F: {print $1"\t"$3} \
     END{print "---\nDone"}' /etc/passwd

# 先设置分隔符再处理（正确方式）
awk 'BEGIN{FS=":"} $3 < 10 {print $1, $3}' /etc/passwd
```

### 去重技巧

``` bash
# 利用关联数组去重（比 sort | uniq 更灵活，不需要先排序）
awk '!seen[$0]++' file
```

!!! tip "awk 变量不需要 $ 前缀"

    在 awk 内部自定义变量时直接用名字（如 `sum`, `count`），不需要加 `$`。`$` 只用于引用字段（`$1`, `$NF`）。

## 综合实战

### 统计 Nginx 访问日志 TOP 10 IP

``` bash
awk '{print $1}' access.log | sort | uniq -c | sort -rn | head -10
```

管道拆解：

1. `awk '{print $1}'` — 提取每行第 1 个字段（IP 地址）
2. `sort` — 把相同 IP 排到一起
3. `uniq -c` — 统计每个 IP 出现次数
4. `sort -rn` — 按次数从大到小排
5. `head -10` — 取前 10 名

### 提取普通用户（UID ≥ 1000）

``` bash
awk -F: '$3 >= 1000 {print $1, $3}' /etc/passwd
```

### 统计各类文件数量

``` bash
ls -l | awk '{print $1}' | cut -c1 | sort | uniq -c
# 输出示例：
#  12 -    普通文件
#   3 d    目录
#   1 l    软链接
```

### 批量处理：删除配置文件注释和空行

``` bash
sed -i.bak '/^[[:space:]]*#/d; /^$/d' /etc/myapp/config.conf
```

### 从 CSV 中筛选并格式化输出

``` bash
# 找出第 2 列大于 100 的行，格式化打印
awk -F, '$2 > 100 {printf "%-20s %d\n", $1, $2}' data.csv
```

### 提取 IP 并验重

``` bash
grep -oE '[0-9]{1,3}(\.[0-9]{1,3}){3}' access.log \
  | sort | uniq -c | sort -rn
```

## 文件比较

对比两个文件的差异是日常工作中常见的需求。不同工具关注的粒度不同：`diff` 按行，`cmp` 按字节，`comm` 按已排序的行。

### diff — 逐行对比文本差异

``` bash
diff [options] file1 file2
```

最常用的输出格式是**行号+操作符**：

- `1d0` — 第 1 行要删除（d）
- `1a1` — 第 1 行后要添加（a）
- `1c1` — 第 1 行要改变（c）

``` bash
# 最基础的对比，3 行前后文
diff file1.txt file2.txt

# 统一格式（-u），类似 git diff，更易阅读
diff -u file1.txt file2.txt

# 递归对比两个目录
diff -r dir1/ dir2/

# 忽略空白差异
diff -b file1.txt file2.txt

# 忽略大小写
diff -i file1.txt file2.txt
```

### cmp — 字节级比较

对比二进制文件或精确检测第一处差异时使用。

``` bash
cmp file1 file2

# -l 列出所有差异的字节位置和内容（八进制）
cmp -l file1 file2
# 输出示例：
# 5 101 102
# （第 5 字节，file1 是 101，file2 是 102）
```

!!! note "何时用 cmp"

    - 对比二进制文件或数据库文件
    - 只关心是否相同，而不需要详细差异列表
    - 需要精确的字节位置

### comm — 逐行对比两个已排序文件

`comm` 专门用于对比两个**已排序的文本文件**，输出为三列：

- 第 1 列：仅在 file1 中
- 第 2 列：仅在 file2 中  
- 第 3 列：同时在两个文件中

``` bash
# file1.txt: apple\nbanana\ncherry
# file2.txt: banana\ncherry\ndate

comm file1.txt file2.txt
# 输出：
# apple              ← file1 独有
#      banana        ← file2 独有
#      cherry        ← 两者都有
# date               ← file2 独有
```

抑制列：`-1`/`-2`/`-3` 分别隐藏第 1/2/3 列。

``` bash
# 只显示两个文件都有的行
comm -12 file1.txt file2.txt

# 显示 file1 独有的行
comm -23 file1.txt file2.txt
```

!!! note "前置要求"

    两个文件必须事先排序，否则结果错误。若未排序，先用 `sort`：
    
    ``` bash
    comm <(sort file1.txt) <(sort file2.txt)
    ```

### patch — 用补丁文件更新原文件

`diff` 生成的差异可以用 `patch` 命令应用到旧版本上。

``` bash
# 生成补丁文件
diff -u oldfile newfile > changes.patch

# 应用补丁（会备份原文件为 .orig）
patch < changes.patch

# 删除备份
rm oldfile.orig
```

## 取头尾行

### head — 查看文件前 N 行

``` bash
head [options] [file]
```

常用选项：

- `-n N`：显示前 N 行（默认 10）
- `-c N`：显示前 N 个字节
- `-n -N`：显示除了最后 N 行以外的所有行

``` bash
# 看前 5 行
head -n 5 /var/log/syslog

# 去掉最后 10 行
head -n -10 /var/log/syslog

# 看前 100 个字节
head -c 100 /var/log/syslog
```

### tail — 查看文件后 N 行

``` bash
tail [options] [file]
```

常用选项：

- `-n N`：显示后 N 行（默认 10）
- `-n +N`：从第 N 行开始显示到末尾
- `-f`：持续追踪文件新增内容（常用于看实时日志）
- `-F`：追踪时文件被轮转则自动重连（比 `-f` 更安全）

``` bash
# 看最后 20 行
tail -n 20 /var/log/syslog

# 从第 100 行开始显示
tail -n +100 /var/log/syslog

# 实时追踪日志（按 Ctrl+C 停止）
tail -f /var/log/syslog

# 追踪时文件轮转也能自动适应（推荐）
tail -F /var/log/syslog

# 持续显示多个文件
tail -f /var/log/syslog /var/log/auth.log
```

## 文本格式化

按照不同的格式和宽度重新整理文本，多用于生成固定宽度的报表。

### nl — 添加行号

``` bash
nl [options] [file]
```

常用选项：

- `-b a`：给所有行编号（默认空行无号）
- `-b t`：仅给非空行编号（默认）
- `-n rz`：行号右对齐并补零（而非左对齐）
- `-w N`：行号字段宽度（默认 6）
- `-s SEP`：行号后的分隔符（默认制表符）

``` bash
# 默认：空行不编号
nl /etc/services | head

# 所有行都编号，右对齐补零，宽度 3
nl -ba -nrz -w3 /etc/services | head

# 输出示例：
# 001 \S
# 002 Kernel \r on an \m
# 003
```

### fold — 按宽度折行

当输出宽度受限（如终端窄度、邮件行宽限制等）时折行。

``` bash
fold [options] [file]
```

常用选项：

- `-w N`：设置每行最大宽度（默认 80）
- `-s`：在单词间（非字符中间）断行

``` bash
# 标准：80 字符折行
fold /path/to/long-line-file.txt

# 60 字符折行
fold -w 60 /path/to/long-line-file.txt

# 在单词间折行（避免单词被切断）
fold -w 60 -s /path/to/long-line-file.txt
```

### fmt — 段落重排

通常用于整理注释或文档段落，自动合并短行并保持段落完整。

``` bash
fmt [options] [file]
```

常用选项：

- `-w N`：目标行宽（默认 75）
- `-u`：统一间距（每句后一个空格）

``` bash
# 重新排版段落，每行 70 字符
fmt -w 70 README.txt

# 典型场景：整理 Bash 脚本中的注释块
fmt -w 70 << 'EOF'
# This is a long comment that spans multiple lines but should be
# reformatted into a single block that respects the column width
EOF
```

## 文件拆分与反转

### split — 将大文件切割成小文件

按行数或文件大小拆分，便于处理或传输。

``` bash
split [options] [inputfile [outputprefix]]
```

常用选项：

- `-l N`：按行数拆分（每个输出文件 N 行）
- `-b SIZE`：按大小拆分（如 `1M`、`100K`）
- `-d`：用数字后缀而非字母（xaa → x00）

``` bash
# 按 1000 行拆分
split -l 1000 largefile.txt chunk_

# 生成文件：chunk_aa, chunk_ab, ...

# 按 5MB 拆分
split -b 5M largefile.iso image_

# 合并回原文件
cat image_* > largefile.iso
```

### tac — 反向输出文件

按行逆序输出，即末行先显示。

``` bash
# 文件倒序输出
tac /var/log/syslog

# 等效于 tail -n 1, tail -n 2, ...，但 tac 更高效
tac longfile.txt | head

# 在管道中反转行顺序
seq 1 10 | tac
# 输出：10 9 8 7 6 5 4 3 2 1
```

### rev — 反转每行内的字符

将每行字符倒序排列。

``` bash
# 每行反转
echo "hello" | rev
# 输出：olleh

# 多行反转
seq 1 5 | rev
# 输出：
# 1
# 2
# ...（因为数字只有一位，看不出差异）

echo -e "abc\ndef\nghi" | rev
# 输出：
# cba
# fed
# ihg
```

### colrm — 删除指定列范围

从每行删除指定的列（字符位置）。

``` bash
colrm [startcol [endcol]]
```

``` bash
# 删除第 1 到 5 列
colrm 1 5 < file.txt

# 删除从第 10 列到行末
colrm 10 < file.txt

# 示例：从 `ps` 输出中删除 PID 列（通常第 9~15 列）
ps aux | colrm 9 15
```

## 特殊搜索

### look — 在词典中快速查找

在已排序的文件或系统词典中按前缀查找，比 `grep` 快。

``` bash
look [options] string [file]
```

``` bash
# 从系统词典查找以 "bio" 开头的单词
look bio

# 在自定义已排序文件中查找
look "arch" /usr/share/dict/words

# 忽略大小写
look -f arch /usr/share/dict/words
```

!!! note "前置要求"

    查找的文件必须**按字母顺序排序**，否则结果不完整或不准确。

### strings — 从二进制文件提取可读字符串

扫描二进制文件（可执行文件、编译后的库、数据库等），提取所有连续的可打印字符序列，用于反向工程或调试。

``` bash
strings [options] file
```

常用选项：

- `-n N`：最少 N 个字符的字符串（默认 4）
- `-t format`：显示每个字符串的偏移量（`x` 十六进制、`d` 十进制、`o` 八进制）

``` bash
# 提取可执行文件中的字符串（包括错误信息、版本号等）
strings /usr/bin/ls | head

# 最少 10 个字符
strings -n 10 /usr/bin/ls

# 显示十六进制偏移量
strings -t x /usr/bin/ls

# 在二进制日志中查找特定文本
strings app.log | grep "ERROR"

# 常见应用场景：检查程序中是否包含某个版本字符串
strings /usr/bin/curl | grep "curl"
```


---
title: 正则表达式
---

# 正则表达式

**本文你会学到**：

- 正则表达式与 Bash 万用字符的根本区别
- BRE（基础正则）与 ERE（延伸正则）的语法差异
- 基础元字符及其含义（`.`、`*`、`+`、`?`、`[]`、`^`、`$`）
- 分组与反向引用的用法
- 字符类与排除字符类的应用
- 锚点与边界匹配的技巧
- 贪心匹配与非贪心匹配的区别
- 在 grep、sed、awk 中的实际应用
- 常见的正则表达式陷阱与避坑指南

## 两种规范：BRE 与 ERE

Linux 的正则表达式按严谨度分为两个规范：

- **BRE（Basic Regular Expression）**：基础正则，`grep` 默认使用，部分元字符需要加 `\` 才能生效
- **ERE（Extended Regular Expression）**：延伸正则，`grep -E` / `egrep` 使用，语法更简洁

## 基础元字符

| 元字符 | 含义 | BRE 写法 | ERE 写法 |
|--------|------|----------|----------|
| `.` | 任意单个字符（除换行） | `.` | `.` |
| `*` | 前一元素零次或多次 | `*` | `*` |
| `+` | 前一元素一次或多次 | `\+` | `+` |
| `?` | 前一元素零次或一次 | `\?` | `?` |
| `^` | 行首锚点 | `^` | `^` |
| `$` | 行尾锚点 | `$` | `$` |
| `[...]` | 字符类（匹配其中任意一个） | `[...]` | `[...]` |
| `[^...]` | 排除字符类 | `[^...]` | `[^...]` |
| `{n,m}` | 重复 n 到 m 次 | `\{n,m\}` | `{n,m}` |
| `(...)` | 分组 | `\(...\)` | `(...)` |
| `\|` | 或（or） | `\|` | `\|` |

!!! tip "ERE 更简洁"

    BRE 中 `+`、`?`、`{}`、`()` 必须加反斜线，ERE 则直接写字符。日常推荐用 `grep -E` 或 `egrep`，写起来更清晰。

### 元字符详解

**`.`（点）**：匹配除换行符外的任意单个字符，"一定有且仅有一个"。

```bash
# 搜寻 g??d 形式（g 与 d 之间恰好两个字符）
grep -n 'g..d' file
# 匹配：good、glad，不匹配：god（只有一个字符）
```

**`*`（星号）**：重复前一个字符零次到无限次，常与 `.` 组合成 `.*` 表示"任意字符串"。

```bash
# ooo* = 至少两个连续 o（第一个 o 必须存在，第二个 o* 可有可无）
grep -n 'ooo*' file

# .* 匹配任意内容，g.*g 表示 g 开头 g 结尾
grep -n 'g.*g' file
```

**`^` 与 `$`（锚点）**：

```bash
grep -n '^the' file       # 行首是 the
grep -n '\.$' file        # 行尾是 .（需转义）
grep -n '^$' file         # 空行（行首紧跟行尾）
grep -n '^[a-z]' file     # 行首是小写字母
grep -n '^[^a-zA-Z]' file # 行首不是英文字母
```

!!! note "^ 在不同位置含义不同"

    - `^` 在 `[]` 之外：行首锚点
    - `^` 在 `[]` 之内（如 `[^abc]`）：反向选择，"不包含这些字符"

**`{n,m}`（限定次数）**：精确控制重复范围，BRE 中需转义花括号。

```bash
# 恰好两个 o
grep -n 'o\{2\}' file

# g 后面接 2 到 5 个 o，再接 g
grep -n 'go\{2,5\}g' file    # 匹配 goog gooog goooog gooooog

# 2 个 o 以上
grep -n 'go\{2,\}g' file
```

## 字符类（POSIX）

POSIX 字符类解决了语系编码差异问题（避免 `[a-z]` 在不同语系下结果不同）：

| 字符类 | 等价 | 含义 |
|--------|------|------|
| `[:alpha:]` | `A-Za-z` | 英文大小写字母 |
| `[:digit:]` | `0-9` | 数字 |
| `[:alnum:]` | `A-Za-z0-9` | 字母 + 数字 |
| `[:upper:]` | `A-Z` | 大写字母 |
| `[:lower:]` | `a-z` | 小写字母 |
| `[:space:]` | 空格/Tab/CR 等 | 空白字符 |
| `[:blank:]` | 空格/Tab | 空白键与 Tab |
| `[:print:]` | 可打印字符 | 包含空格 |
| `[:punct:]` | 标点符号 | `! ; : # $` 等 |
| `[:xdigit:]` | `0-9A-Fa-f` | 十六进制数字 |

**用法**：POSIX 字符类需要放在 `[]` 内，形成双括号 `[[:alpha:]]`：

```bash
grep -n '[[:digit:]]' file       # 含数字的行
grep -n '^[[:lower:]]' file      # 小写字母开头
grep -n '[^[:lower:]]oo' file    # oo 前面不是小写字母
```

## grep 家族

| 命令 | 等同于 | 使用的正则规范 |
|------|--------|--------------|
| `grep` | `grep -G` | BRE（基础正则） |
| `egrep` | `grep -E` | ERE（延伸正则） |
| `fgrep` | `grep -F` | 固定字符串（不解析正则） |
| `grep -P` | — | PCRE（Perl 兼容正则，支持 `\d`、非贪婪等） |

### 常用选项

| 选项 | 含义 |
|------|------|
| `-i` | 忽略大小写 |
| `-v` | 反向匹配（输出不匹配的行） |
| `-n` | 显示行号 |
| `-c` | 仅统计匹配行数 |
| `-l` | 仅显示包含匹配的文件名 |
| `-r` / `-R` | 递归搜索目录 |
| `-A n` | 同时输出匹配行之后 n 行 |
| `-B n` | 同时输出匹配行之前 n 行 |
| `-C n` | 同时输出匹配行前后各 n 行 |
| `-o` | 只输出匹配的部分（非整行） |
| `-w` | 全词匹配（单词边界） |
| `-x` | 全行匹配（整行必须完全匹配） |
| `--color=auto` | 关键字高亮显示 |

### 常用示例

```bash
# 忽略大小写搜索
grep -i "error" /var/log/syslog

# 显示匹配行号并高亮
grep -n --color=auto 'pattern' file

# 上下文：同时显示前 2 行和后 3 行
grep -n -A3 -B2 'qxl' /var/log/dmesg

# 显示匹配前后各 3 行
grep -C 3 "OutOfMemory" /var/log/app.log

# 递归搜索代码中的 TODO
grep -rn "TODO" src/

# 反向匹配：排除注释行与空行
grep -v '^#' /etc/ssh/sshd_config
grep -v '^$' file | grep -v '^#'

# 提取 IP 地址（-o 只输出匹配部分，-E 用 ERE）
grep -oE '[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}' file

# 只显示含匹配内容的文件名
grep -rl "password" /etc/

# 统计匹配行数
grep -c "ERROR" app.log

# 全词匹配（不会匹配 rooted）
grep -w "root" /etc/passwd

# ERE：一次搜索多个模式（或关系）
egrep -v '^$|^#' /etc/rsyslog.conf
```

## 锚点与边界

```bash
^pattern      # 行首
pattern$      # 行尾
^pattern$     # 整行精确匹配

\<word\>      # 单词边界（BRE，GNU grep 支持）
\bword\b      # 单词边界（PCRE，grep -P）
```

## 分组与反向引用

分组让你可以将一部分模式作为整体处理，也可以用于反向引用（引用之前捕获的内容）。

```bash
# ERE 分组：匹配重复字符串
echo "abcabc" | grep -E "(abc)\1"

# BRE 分组（用 \( \)）：匹配 foofoo
grep "\(foo\)\1" file

# ERE：搜寻 glad 或 good（共用 g 和 d）
egrep -n 'g(la|oo)d' file

# ERE：至少一个 xyz 重复组
echo 'AxyzxyzxyzxyzC' | egrep 'A(xyz)+C'
```

**在 `sed` 中使用反向引用（替换时交换两词）**：

```bash
# BRE 写法
sed 's/\(first\) \(second\)/\2 \1/' file

# ERE 写法
sed -E 's/(first) (second)/\2 \1/' file
```

## 贪婪与非贪婪

默认情况下，`.*` 是**贪婪的**，会尽可能多地匹配字符。只有 PCRE（`grep -P`）支持非贪婪量词 `.*?`。

```bash
# 贪婪：匹配从第一个 < 到最后一个 >（整个字符串）
echo "<a>text</a>" | grep -oE '<.*>'
# 结果：<a>text</a>

# 非贪婪（需要 -P 启用 PCRE）：每次尽可能少匹配
echo "<a>text</a>" | grep -oP '<.*?>'
# 结果：<a>  </a>
```

## 常用正则模式

``` bash title="常见场景速查"
# 匹配空行
^$

# 匹配注释行（# 开头，允许前置空白）
^[[:space:]]*#

# 匹配 IP 地址（简单版，BRE）
[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}

# 匹配 IP 地址（简单版，ERE）
[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}

# 匹配邮箱（简单版，ERE）
[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}

# 匹配 URL（ERE）
https?://[a-zA-Z0-9._/-]+

# 匹配日期 YYYY-MM-DD（BRE）
[0-9]\{4\}-[0-9]\{2\}-[0-9]\{2\}

# 排除空行和注释行（两种等价写法）
grep -v '^$' file | grep -v '^#'
egrep -v '^$|^#' file

# 找出链接文件（ls -l 输出行首为 l）
ls -l /etc | grep '^l'
```

## 在 sed/awk 中使用正则

正则表达式不仅用于 `grep`，`sed` 和 `awk` 同样支持，详见「文本处理工具」页面。

``` bash title="sed 正则应用"
# 替换：将每行末尾的 . 换成 !
sed 's/\.$/!/g' file

# 删除注释与空行（两步管道）
sed 's/#.*$//g' file | sed '/^$/d'

# ERE 替换：将所有数字替换为 NUM
sed -E 's/([0-9]+)/NUM/g' file

# 提取 IP 示例（逐步处理）
/sbin/ifconfig eth0 | grep 'inet ' | sed 's/^.*inet //g' | sed 's/ *netmask.*$//g'
```

``` bash title="awk 正则应用"
# 过滤含 pattern 的行
awk '/pattern/' file

# 排除含 pattern 的行
awk '!/pattern/' file

# 以冒号为分隔符，打印第三字段小于 10 的行
awk 'BEGIN {FS=":"} $3 < 10 {print $1 "\t" $3}' /etc/passwd
```

## POSIX 字符类深度

BRE 和 ERE 都支持 POSIX 字符类，即 `[:classname:]` 格式，必须嵌套在 `[]` 内使用。

### 常见 POSIX 字符类

| 类名 | 等价范围 | 示例 |
|------|---------|------|
| `[:alpha:]` | `[a-zA-Z]` | 匹配任意字母 |
| `[:digit:]` | `[0-9]` | 匹配任意数字 |
| `[:alnum:]` | `[a-zA-Z0-9]` | 字母或数字 |
| `[:space:]` | 空格、Tab、换行 | 任意空白符 |
| `[:upper:]` | `[A-Z]` | 大写字母 |
| `[:lower:]` | `[a-z]` | 小写字母 |
| `[:xdigit:]` | `[0-9a-fA-F]` | 十六进制数字 |
| `[:punct:]` | 标点符号 | 所有标点 |

``` bash
# 匹配纯数字行
grep "^[[:digit:]]*$" /path/to/file

# 匹配首字母大写的单词
grep "^[[:upper:]][[:lower:]]*" /path/to/file

# 匹配文件名（字母、数字、下划线）
grep "^[[:alnum:]_]*$" /path/to/file
```

## 单词边界（Word Boundary）

在 `grep` 中使用 `-w` 选项实现全词匹配，但在正则中也有边界概念。

### \< 和 \> （BRE 中的单词边界）

``` bash
# 仅匹配完整单词 "cat"，不匹配 "catch"、"concatenate"
grep "\<cat\>" /path/to/file

# 开头边界：cat 作为单词开头
grep "\<cat" /path/to/file

# 结尾边界：cat 作为单词结尾
grep "cat\>" /path/to/file
```

### 等价方法：`-w` 选项

``` bash
grep -w "cat" /path/to/file    # 等同于 \<cat\>
```

## 反向引用（Backreferences）

分组 `()` 的真正用处是在替换中引用分组内容。分组被自动编号为 `\1`、`\2` 等。

### BRE 反向引用

BRE 中用 `\(` `\)` 分组，在替换中用 `\1` 等引用。

``` bash
# 匹配重复单词：\1 引用第一个分组
grep '\([a-z]\+\) \1' /path/to/file
# 匹配：hello hello、foo foo 等

# 替换：交换两个单词的顺序
# 原文：hello world
# 需求：交换为 world hello
sed 's/\([a-z]\+\) \([a-z]\+\)/\2 \1/' file.txt
```

### ERE 反向引用

ERE 中用 `()` 分组，引用方式相同。

``` bash
# ERE 格式
grep -E '([a-z]+) \1' /path/to/file

# 替换
sed -E 's/([a-z]+) ([a-z]+)/\2 \1/' file.txt
```

### 实战：检测重复行与提取分量

``` bash
# 在配置文件中检测重复的键值对设置
grep -E '(^[a-z_]+)=.*\1=' config.conf

# 从 URL 中提取域名和路径
echo "http://example.com/path/to/file" | \
  sed -E 's|https?://([^/]+)(/.*)?|\1 - \2|'
# 输出：example.com - /path/to/file

# 从日志中提取用户和 IP
grep -E 'user=(\w+).*ip=([0-9.]+)' access.log | \
  sed -E 's/.*user=(\w+).*ip=([0-9.]+).*/\1 -> \2/'
```

## 常见实战模式

### IP 地址匹配（多个版本）

简化版（BRE）：

``` bash
grep '[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}' access.log
```

简化版（ERE）：

``` bash
grep -E '[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}' access.log
```

更严格的版本（检查范围 0-255）：

``` bash
# 需要 grep 支持 PCRE（-P 选项）
grep -P '(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)' access.log
```

### 邮箱匹配

简化版：

``` bash
grep -E '[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]' file.txt
```

### 日期匹配（YYYY-MM-DD）

BRE 版：

``` bash
grep '[0-9]\{4\}-[0-9]\{2\}-[0-9]\{2\}' file.txt
```

ERE 版：

``` bash
grep -E '[0-9]{4}-[0-9]{2}-[0-9]{2}' file.txt
```

### URL 匹配

``` bash
grep -E 'https?://[a-zA-Z0-9._/-]+' file.txt
```

### 电话号码匹配

中国手机号（11 位，第一位 1）：

``` bash
grep -E '1[0-9]{10}' file.txt
```

### 排除空行和注释行（综合对比）

三种等价写法：

``` bash
# 方法 1：两个 grep（经典，易理解）
grep -v '^$' file.txt | grep -v '^[[:space:]]*#'

# 方法 2：单个 grep -E 配合交替
grep -v -E '^[[:space:]]*(#|$)' file.txt

# 方法 3：单个 sed（等效）
sed '/^[[:space:]]*#/d; /^[[:space:]]*$/d' file.txt
```

## 正则与性能

### 贪婪与非贪婪再深入

#### 贪婪模式（默认）

``` bash
# 贪婪 .* 匹配尽可能多的字符
echo "<a>link</a> text <b>bold</b>" | \
  grep -o '<.*>'
# 输出：<a>link</a> text <b>bold</b>  ← 从第一个 < 到最后一个 >
```

#### 非贪婪模式（需要 PCRE）

``` bash
# 非贪婪 .*? 配合 PCRE (-P)
echo "<a>link</a> text <b>bold</b>" | \
  grep -oP '<.*?>'
# 输出：<a>
#      </a>
#      <b>
#      </b>
```

### 回溯问题

过于复杂的正则可能导致**灾难性回溯**，严重时会让 `grep` 假死。

``` bash
# ❌ 危险的正则（极端嵌套量词）
grep -E '(a+)+$' large_file.txt    # 可能造成超长时间等待

# ✅ 改进版（避免嵌套量词）
grep -E '^a+$' large_file.txt
```

### 性能建议

- 尽量用精确匹配而非通配符：`^admin` 比 `.*admin.*` 快
- 避免嵌套量词：`(a+)+` 改为 `a+`
- 用 `^` 和 `$` 限定行首行末，减少搜索范围
- 大文件上用 `grep -F`（固定字符串，不解释正则）更快

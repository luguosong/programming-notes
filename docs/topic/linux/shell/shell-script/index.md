---
title: Shell 脚本编程
---

# Shell 脚本编程

**本文你会学到**：

- 脚本的基础结构与良好编写习惯
- 四种执行方式的区别（子进程 vs 父进程）
- 特殊变量与位置参数的用法
- `test`、`[ ]`、`[[ ]]` 条件判断的差异
- `if`、`case`、`for`、`while`、`until` 控制流
- 函数的定义与参数传递
- 数组与算术运算
- 字符串操作、错误处理与调试技巧

---

## ⚙️ 脚本基础结构

### 为什么需要脚本？

假设你需要每天备份数据库、分析日志、检查服务状态——如果每次都手动一条一条输入命令，既浪费时间又容易出错。Shell 脚本就是把这一串命令写进文件，一次执行搞定。

Shell 脚本是纯文本文件，结合了 Shell 语法、外部命令、正则表达式、管道和重定向，用于自动化系统管理工作。

### 脚本文件结构

一个规范的 Shell 脚本通常分为以下几段：

``` bash title="hello.sh"
#!/usr/bin/env bash
# 功能：演示 Shell 脚本基础结构
# 作者：dmtsai
# 日期：2024-01-01
# 版本：1.0

# 推荐的安全选项
set -euo pipefail   # 出错即退出、未定义变量报错、管道错误传播
IFS=$'\n\t'         # 更安全的字段分隔符

# 重要环境变量
PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/bin:/usr/local/sbin:~/bin
export PATH

# 脚本主体
echo "Hello, World!"
exit 0
```

各段的作用：

- **Shebang 行**：`#!/usr/bin/env bash` 告诉系统用哪个解释器执行此脚本
- **注释头部**：记录功能、作者、日期、历史变更，方便维护
- **环境变量声明**：确保脚本在任何环境下都能找到外部命令
- **主程序体**：核心逻辑
- **`exit 0`**：明确返回 0（成功），可用 `$?` 获取

!!! tip "Shebang 的两种写法"

    `#!/bin/bash` 和 `#!/usr/bin/env bash` 都能工作，但推荐后者。`env` 会在 `$PATH` 中搜索 `bash`，当 bash 不在 `/bin/bash`（如 macOS、一些 BSD 系统）时仍能正常运行，可移植性更强。

!!! warning "set -euo pipefail 的含义"

    - `-e`：任意命令返回非零则立即退出
    - `-u`：引用未定义变量时报错（而非静默展开为空）
    - `-o pipefail`：管道中任意命令失败，整个管道视为失败

---

## 🚀 执行方式

### 四种执行脚本的方法

``` bash
bash script.sh          # 直接用 bash 执行，不需要执行权限
sh script.sh            # 用 sh（通常链接到 bash 或 dash）执行
./script.sh             # 需要执行权限（chmod +x script.sh）
source script.sh        # 在当前 Shell 中执行
. script.sh             # source 的简写
```

### 子进程 vs 父进程的关键区别

当你用 `bash script.sh` 或 `./script.sh` 执行时，系统会**新建一个子 bash 进程**来运行脚本。脚本结束后，子进程销毁，脚本中设置的变量**不会影响**当前终端：

``` bash
$ bash showname.sh
Please input your first name: VBird
Your full name is: VBird Tsai

$ echo ${firstname}    # 空的！变量只存在于子进程中
```

而 `source script.sh`（或 `. script.sh`）则在**当前 bash 进程**中执行，脚本中的变量设置会生效：

``` bash
$ source showname.sh
Please input your first name: VBird
Your full name is: VBird Tsai

$ echo ${firstname}
VBird    # ✅ 变量生效了！
```

这就是为什么修改 `~/.bashrc` 后要用 `source ~/.bashrc` 而不是 `bash ~/.bashrc` 来让配置立即生效。

---

## 📦 特殊变量

### 位置参数与内置变量

执行 `/path/to/script.sh arg1 arg2 arg3` 时，各参数对应的变量：

```
/path/to/script.sh  arg1  arg2  arg3
       $0            $1    $2    $3
```

完整的特殊变量列表：

| 变量 | 含义 |
|------|------|
| `$0` | 脚本文件名（包含路径） |
| `$1` ~ `$9` | 第 1~9 个位置参数 |
| `${10}` | 第 10 个及以上的参数需用花括号 |
| `$#` | 位置参数的个数 |
| `$@` | 所有参数，各自独立（推荐用于遍历） |
| `$*` | 所有参数合为一个字符串（以 `IFS` 分隔） |
| `$?` | 上一条命令的退出码（0=成功，非0=失败） |
| `$$` | 当前进程的 PID |
| `$!` | 最后一个后台进程的 PID |
| `$_` | 上一条命令的最后一个参数 |

### $@ 与 $* 的区别

``` bash title="show_params.sh"
#!/usr/bin/env bash
echo "脚本名: $0"
echo "参数个数: $#"
[ "$#" -lt 2 ] && echo "参数不足，至少需要 2 个" && exit 1
echo "所有参数 \$@: $@"
echo "第一个参数: $1"
echo "第二个参数: $2"
```

### shift：参数偏移

`shift` 命令可以将参数列表向左移动，`shift N` 移动 N 位：

``` bash
# 执行: sh shift_demo.sh a b c d e
echo "$@"    # a b c d e
shift        # 移掉第一个
echo "$@"    # b c d e
shift 2      # 再移掉两个
echo "$@"    # d e
```

---

## 🔍 条件判断

### test 命令与 [ ] 判断符号

`test` 和 `[ ]` 功能相同（`[` 实际上是命令），用于测试文件属性、字符串和数值：

``` bash
# 文件类型测试
[ -e path ]         # 路径存在（文件或目录均可）
[ -f file ]         # 是普通文件
[ -d dir ]          # 是目录
[ -L file ]         # 是符号链接
[ -b file ]         # 是块设备
[ -S file ]         # 是 Socket 文件

# 文件权限测试
[ -r file ]         # 可读
[ -w file ]         # 可写
[ -x file ]         # 可执行
[ -s file ]         # 非空文件（大小 > 0）
[ -u file ]         # 设置了 SUID
[ -g file ]         # 设置了 SGID

# 文件比较
[ file1 -nt file2 ] # file1 比 file2 新（newer than）
[ file1 -ot file2 ] # file1 比 file2 旧（older than）
[ file1 -ef file2 ] # 两个文件指向同一 inode（硬链接判断）

# 字符串测试
[ -z "$str" ]       # 字符串为空
[ -n "$str" ]       # 字符串非空
[ "$a" = "$b" ]     # 字符串相等（注意变量要加引号！）
[ "$a" != "$b" ]    # 字符串不等

# 整数比较
[ $a -eq $b ]       # 等于（equal）
[ $a -ne $b ]       # 不等于（not equal）
[ $a -lt $b ]       # 小于（less than）
[ $a -le $b ]       # 小于等于（less than or equal）
[ $a -gt $b ]       # 大于（greater than）
[ $a -ge $b ]       # 大于等于（greater than or equal）

# 逻辑组合（[ ] 内）
[ cond1 -a cond2 ]  # AND（旧写法）
[ cond1 -o cond2 ]  # OR（旧写法）
[ ! cond ]          # NOT
# 推荐写法：
[ cond1 ] && [ cond2 ]   # AND
[ cond1 ] || [ cond2 ]   # OR
```

!!! warning "[ ] 内必须有空格"

    中括号是命令，每个元素必须用空格分隔，变量必须加双引号：

    ``` bash
    # ❌ 错误：会报 "too many arguments"
    name="VBird Tsai"
    [ ${name} == "VBird" ]

    # ✅ 正确：加双引号防止空格切割
    [ "${name}" == "VBird" ]
    ```

### [[ ]] 扩展判断

`[[ ]]` 是 bash 的关键字（不是命令），功能更强，推荐在现代脚本中使用：

``` bash
# 正则匹配（[ ] 不支持）
[[ "$str" =~ ^[0-9]+$ ]]   # 检查是否全为数字

# 通配符匹配
[[ "$file" == *.log ]]

# 逻辑运算符（更自然）
[[ cond1 && cond2 ]]
[[ cond1 || cond2 ]]
```

### [ ] 与 [[ ]] 对比

| 特性 | `[ ]` | `[[ ]]` |
|------|-------|---------|
| 类型 | 命令（`test`） | bash 关键字 |
| 变量不加引号 | ❌ 可能出错 | ✅ 安全 |
| 正则匹配 `=~` | ❌ 不支持 | ✅ 支持 |
| 通配符匹配 | ❌ | ✅ |
| `&&` / `\|\|` 逻辑运算 | ❌ 需用 `-a` / `-o` | ✅ 直接使用 |
| 可移植性（`sh`） | ✅ POSIX 兼容 | ❌ 仅 bash/zsh |

---

## 🌿 if 条件分支

### 基础语法

``` bash
if [ 条件 ]; then
    # 条件成立时执行
fi

# 带 else
if [ 条件 ]; then
    # 成立时
else
    # 不成立时
fi

# 多分支
if [ "$1" -gt 100 ]; then
    echo "大于 100"
elif [ "$1" -gt 50 ]; then
    echo "大于 50，不超过 100"
else
    echo "50 或以下"
fi
```

### 实用示例：检测网络服务

``` bash title="netstat.sh"
#!/usr/bin/env bash
# 检测常用网络服务是否在运行

testfile=$(mktemp)
netstat -tuln > "${testfile}"

check_port() {
    local port="$1"
    local name="$2"
    if grep -q ":${port} " "${testfile}"; then
        echo "${name} 正在运行"
    fi
}

check_port 80  "WWW"
check_port 22  "SSH"
check_port 21  "FTP"
check_port 25  "Mail"

rm -f "${testfile}"
```

---

## 🎛️ case 多分支选择

### 语法结构

当变量的取值是有限的几个固定值时，`case` 比多个 `if-elif` 更清晰：

``` bash
case "$变量" in
    "值1")
        # 匹配值1时执行
        ;;
    "值2"|"值3")       # 多个值用 | 分隔
        # 匹配值2或值3
        ;;
    *)                  # 默认分支（相当于 else）
        echo "未知选项"
        exit 1
        ;;
esac
```

### 服务控制脚本示例

这种模式在 `/etc/init.d/` 下的服务脚本中非常常见：

``` bash title="service.sh"
#!/usr/bin/env bash
case "$1" in
    start)
        echo "启动服务..."
        ;;
    stop|quit)
        echo "停止服务..."
        ;;
    restart)
        echo "重启服务..."
        ;;
    status)
        echo "查询状态..."
        ;;
    *)
        echo "用法: $0 {start|stop|restart|status}"
        exit 1
        ;;
esac
```

执行示例：`/etc/init.d/netconsole restart`

---

## 🔄 循环

### while：条件为真时持续循环

``` bash
# 基础用法：条件成立就继续
count=0
while [ "${count}" -lt 10 ]; do
    echo "count = ${count}"
    ((count++))
done

# 等待用户输入正确内容
while [ "${yn}" != "yes" -a "${yn}" != "YES" ]; do
    read -p "请输入 yes/YES 继续: " yn
done
echo "你输入了正确答案！"

# 逐行读取文件（最安全的写法）
while IFS= read -r line; do
    echo "$line"
done < /etc/passwd

# 无限循环（后台监控脚本常用）
while true; do
    check_something
    sleep 60
done
```

### until：条件为假时持续循环

`until` 与 `while` 逻辑相反——条件成立时**退出**循环：

``` bash
# 等待主机上线
until ping -c1 192.168.1.1 &>/dev/null; do
    echo "等待主机上线..."
    sleep 5
done
echo "主机已上线！"
```

### for：遍历列表（固定循环）

``` bash
# 遍历字面量列表
for animal in dog cat elephant; do
    echo "There are ${animal}s..."
done

# 遍历命令输出（如读取 /etc/passwd 所有用户）
for username in $(cut -d: -f1 /etc/passwd); do
    id "${username}"
done

# 遍历文件通配符
for file in /etc/*.conf; do
    echo "配置文件: ${file}"
done

# 使用序列（两种等价写法）
for i in $(seq 1 100); do echo "$i"; done
for i in {1..100}; do echo "$i"; done

# C 风格（适合数值计算）
for ((i=1; i<=10; i++)); do
    echo "$i"
done
```

### break 与 continue

``` bash
for i in {1..10}; do
    [ "$i" -eq 3 ] && continue   # 跳过 3，继续下一次循环
    [ "$i" -eq 7 ] && break      # 到 7 时退出整个循环
    echo "$i"
done
# 输出: 1 2 4 5 6
```

### 综合示例：批量 ping 检测

``` bash title="pingip.sh"
#!/usr/bin/env bash
network="192.168.1"
for sitenu in {1..100}; do
    if ping -c1 -W1 "${network}.${sitenu}" &>/dev/null; then
        echo "Server ${network}.${sitenu} is UP"
    else
        echo "Server ${network}.${sitenu} is DOWN"
    fi
done
```

---

## 🔧 函数

### 定义与调用

函数必须**先定义后调用**（脚本从上到下执行）：

``` bash
# 定义函数（两种写法等价）
greet() {
    local name="$1"        # local 限制变量作用域，不污染全局
    echo "Hello, ${name}!"
    return 0               # return 只能返回 0-255 的整数（退出码）
}

# 调用
greet "Alice"

# 捕获函数的输出（通过 echo，而非 return）
get_date() {
    echo "$(date +%Y-%m-%d)"
}
today=$(get_date)
echo "今天是: ${today}"
```

### 函数有自己的参数

函数内的 `$1`, `$2`... 与脚本的位置参数**相互独立**：

``` bash
printit() {
    echo "你选择的是: $1"   # 这里的 $1 是函数的参数
}

case "${1}" in             # 这里的 $1 是脚本的参数
    "one")
        printit 1          # 传 1 给函数
        ;;
    "two")
        printit 2
        ;;
esac
```

执行 `sh script.sh one` 时：脚本的 `$1` 是 `"one"`，但函数内的 `$1` 是 `1`。

---

## 📊 数组

### 索引数组

``` bash
# 定义
arr=("apple" "banana" "cherry")

# 访问元素
echo "${arr[0]}"             # 第一个：apple
echo "${arr[@]}"             # 所有元素（各自独立，用于遍历）
echo "${arr[*]}"             # 所有元素（合为一个字符串）
echo "${#arr[@]}"            # 元素个数

# 修改
arr+=("date")                # 追加元素
arr[1]="blueberry"           # 修改第 2 个
unset arr[2]                 # 删除第 3 个

# 遍历（推荐用 ${arr[@]}）
for item in "${arr[@]}"; do
    echo "${item}"
done
```

### 关联数组（bash 4+）

``` bash
declare -A map

map["host"]="localhost"
map["port"]="3306"
map["db"]="mydb"

echo "${map["host"]}"        # 访问值
echo "${!map[@]}"            # 所有键
echo "${map[@]}"             # 所有值

# 遍历关联数组
for key in "${!map[@]}"; do
    echo "${key} = ${map[$key]}"
done
```

---

## ➕ 算术运算

### 整数运算

bash 默认只支持整数运算：

``` bash
# (( )) 算术求值（推荐）
result=$((1 + 2))
echo $((10 % 3))             # 取余：1
echo $((2 ** 10))            # 幂运算：1024

# 自增/自减
((count++))
((count--))
((count += 5))

# let 命令（较旧的写法）
let "result = 5 * 3"

# declare -i 整数声明
declare -i total
total=firstnu*secnu
```

### 浮点数运算（借助 bc）

bash 本身不支持浮点数，需要借助 `bc` 命令：

``` bash
# 简单浮点运算
echo "3.14 * 2" | bc                    # 6.28
echo "scale=4; 10 / 3" | bc            # 3.3333（scale 控制小数位数）

# 计算 pi（bc 内置函数）
echo "scale=10; 4*a(1)" | bc -lq       # 3.1415926535
```

### 乱数

`$RANDOM` 每次调用返回 0~32767 的随机整数：

``` bash
echo $RANDOM
# 取 1~9 之间的随机数
echo $(( RANDOM * 9 / 32767 + 1 ))
```

---

## ✂️ 字符串操作

### 参数展开速查

``` bash
str="Hello, World!"

echo ${#str}                 # 长度：13
echo ${str:7:5}              # 子串（从第7位取5个）：World
echo ${str/World/Linux}      # 替换第一个：Hello, Linux!
echo ${str//o/0}             # 替换全部：Hell0, W0rld!
echo ${str,,}                # 转全小写（bash 4+）：hello, world!
echo ${str^^}                # 转全大写（bash 4+）：HELLO, WORLD!

# 删除前缀/后缀
path="/usr/local/bin/bash"
echo ${path#*/}              # 删除最短前缀匹配 */：usr/local/bin/bash
echo ${path##*/}             # 删除最长前缀匹配 */：bash（取文件名）
echo ${path%/*}              # 删除最短后缀匹配 /*：/usr/local/bin（取目录）
echo ${path%%/*}             # 删除最长后缀匹配 /*：（空）

# 默认值
echo ${var:-"default"}       # 若 var 未设置或为空，使用 "default"
echo ${var:="default"}       # 若 var 未设置或为空，赋值并使用 "default"
```

---

## 🛡️ 错误处理

### set 选项

``` bash
set -e          # 任意命令失败立即退出
set -u          # 未定义变量报错
set -x          # 显示每条执行的命令（调试用）
set +x          # 关闭调试输出
set -o pipefail # 管道中任意命令失败则整体失败
```

### trap：捕获信号与退出事件

``` bash
# 脚本退出时清理临时文件
cleanup() {
    rm -f /tmp/tempfile.$$
    echo "清理完毕"
}
trap cleanup EXIT           # 无论正常还是异常退出都会执行

# 捕获错误，显示出错行号
trap 'echo "第 $LINENO 行出错，退出码: $?"; exit 1' ERR

# 捕获 Ctrl+C（SIGINT）
trap 'echo "用户中断"; exit 130' INT
```

### 手动检查退出码

``` bash
# 方式一：用 if 包裹
if ! cp source.txt dest.txt; then
    echo "复制失败" >&2
    exit 1
fi

# 方式二：用 || 短路
command_that_may_fail || { echo "失败了" >&2; exit 1; }

# 检查命令是否存在
if ! command -v git &>/dev/null; then
    echo "未安装 git" >&2
    exit 1
fi
```

---

## 🔬 调试技巧

### 命令行调试选项

``` bash
bash -n script.sh    # 仅检查语法，不执行（语法检查）
bash -v script.sh    # 执行前打印每行脚本内容
bash -x script.sh    # 执行时打印每条展开后的命令（最常用）
```

`-x` 的输出示例（每行前缀 `+` 表示命令被展开执行）：

```
+ for animal in dog cat elephant
+ echo 'There are dogs....'
There are dogs....
+ for animal in dog cat elephant
+ echo 'There are cats....'
There are cats....
```

### 脚本内部开关调试

``` bash
#!/usr/bin/env bash
# 可以在脚本特定区间开关调试
set -x          # 开始追踪
some_complex_logic
set +x          # 停止追踪
```

---

## 📋 实用脚本模板

### 带参数解析与帮助信息的模板

``` bash title="template.sh"
#!/usr/bin/env bash
set -euo pipefail

# 打印帮助信息
usage() {
    cat << EOF
用法: $(basename "$0") [选项] <参数>

选项:
    -h          显示此帮助信息
    -v          启用详细输出
    -o <文件>   输出到指定文件

示例:
    $(basename "$0") -v input.txt
    $(basename "$0") -o output.txt input.txt
EOF
    exit 0
}

# 参数解析
VERBOSE=false
OUTPUT=""

while getopts "hvo:" opt; do
    case $opt in
        h) usage ;;
        v) VERBOSE=true ;;
        o) OUTPUT="$OPTARG" ;;
        *) usage ;;
    esac
done
shift $((OPTIND - 1))    # 移除已解析的选项，$@ 剩余非选项参数

# 检查必要参数
[ $# -eq 0 ] && echo "错误: 缺少必要参数" >&2 && usage

# 日志函数
log() {
    $VERBOSE && echo "[INFO] $*"
}

log "开始处理: $1"
# 脚本主体逻辑
```

### 文件检测综合示例

``` bash title="file_check.sh"
#!/usr/bin/env bash
read -p "请输入文件路径: " filename

# 检查是否输入了内容
[ -z "${filename}" ] && echo "必须输入文件名" && exit 1

# 检查是否存在
if [ ! -e "${filename}" ]; then
    echo "文件 '${filename}' 不存在"
    exit 1
fi

# 判断类型
if [ -f "${filename}" ]; then
    filetype="普通文件"
elif [ -d "${filename}" ]; then
    filetype="目录"
else
    filetype="其他类型"
fi

# 检查权限
perm=""
[ -r "${filename}" ] && perm="${perm} 可读"
[ -w "${filename}" ] && perm="${perm} 可写"
[ -x "${filename}" ] && perm="${perm} 可执行"

echo "${filename} 是${filetype}，权限:${perm}"
```


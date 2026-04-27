---
title: Bash Shell 基础
---

# Bash Shell 基础

**本文你会学到**：

- Shell 的角色与常见 Shell 类型
- 变量的赋值、类型、参数展开与操作
- 环境变量的查看与 PATH 管理
- 命令行解析顺序与别名设置
- 命令历史的使用技巧
- Shell 启动文件加载时机
- 通配符与引号转义规则

---

## 🐚 Shell 是什么

### 用户与内核之间的桥梁

你不能直接操作 Linux 内核（Kernel）——它是系统的核心，必须被保护。当你输入一条命令时，是 Shell 将这条命令"翻译"并传递给内核，内核再驱动硬件完成任务，最后将结果通过 Shell 返回给你。

```
你 → Shell → 内核（Kernel） → 硬件
```

Shell 的意思就是"外壳"，包裹在内核外面，提供给用户操作系统的接口。

### 常见 Shell

| Shell | 特点 |
|-------|------|
| `bash` | Bourne Again SHell，Linux 默认 Shell，功能全面 |
| `zsh` | 功能最强大，macOS 新默认 Shell，支持丰富插件 |
| `fish` | 用户友好，语法简洁，适合新手 |
| `dash` | 轻量快速，Ubuntu/Debian 的 `/bin/sh` |
| `sh` | 最原始的 Bourne Shell，现多为 bash/dash 的符号链接 |

查看系统中可用的 Shell：

``` bash
cat /etc/shells
```

查看当前使用的 Shell：

``` bash
echo $SHELL        # 查看默认 Shell（登录时分配的）
ps -p $$           # 查看当前正在运行的 Shell 进程
```

用户的默认 Shell 记录在 `/etc/passwd` 的最后一列：

``` bash
grep "^$(whoami):" /etc/passwd
# dmtsai:x:1000:1000:dmtsai:/home/dmtsai:/bin/bash
```

!!! tip "为什么学 Bash？"

    无论哪个 Linux 发行版，bash 几乎都存在且行为一致。学会 bash，你就能在任何 Linux 系统上得心应手。

---

## 📦 变量

### 为什么需要变量？

当系统中有多个用户时，`mail` 命令如何知道该读取哪个用户的邮箱？答案是通过变量 `$MAIL`——每个用户登录后，系统会将 `MAIL` 变量设置为该用户自己的邮箱路径，`mail` 命令只需读取这个变量就能自动找到正确的文件，无需为每个用户单独编写程序。

变量就是用一个简短的名称来代表一段可变的内容。

### 变量赋值与使用

``` bash
name="Alice"        # 赋值（= 两侧不能有空格）
echo $name          # 使用变量
echo ${name}        # 推荐用 ${} 明确边界，避免歧义
echo "${name}_suffix"
```

!!! warning "赋值规则"

    - `=` 两侧**不能有空格**：`name=Alice` ✅，`name = Alice` ❌
    - 变量名只能含英文字母、数字、下划线，且**不能以数字开头**
    - 内容含空格时必须用引号：`name="VBird Tsai"`

### 变量类型

``` bash
# 本地变量（仅当前 Shell）
var=hello

# 环境变量（传递给子进程）
export VAR=hello
export VAR        # 或先赋值再 export

# 只读变量（不可修改、不可 unset）
readonly CONST="fixed"

# 整型变量（支持数学运算）
declare -i num=42
declare -i result=10+5    # result=15

# 数组
arr=(a b c d)
echo ${arr[0]}     # 第一个元素：a
echo ${arr[@]}     # 所有元素：a b c d
echo ${#arr[@]}    # 数组长度：4
```

取消变量：

``` bash
unset var
```

### 父进程与子进程

当你在 bash 中再启动一个 bash，新的 bash 就是**子进程**，原来的 bash 是**父进程**。

``` bash
name=Alice
bash          # 进入子进程
echo $name    # 空！子进程看不到父进程的本地变量
exit

export name   # 将变量提升为环境变量
bash
echo $name    # Alice！环境变量会传递给子进程
exit
```

规律：**本地变量不被子进程继承，环境变量（export 后）会被继承。**

### 参数展开（Parameter Expansion）

参数展开是 Bash 的强大功能，可以在不编写额外脚本的情况下对变量内容进行处理：

``` bash
# 默认值处理
${var:-default}     # var 未定义或为空时，返回 default（不修改 var）
${var:=default}     # var 未定义或为空时，赋值并返回 default
${var:+alt}         # var 已定义且非空时，返回 alt（否则返回空）
${var:?message}     # var 未定义或为空时，输出 message 并退出脚本

# 字符串操作
${#var}             # 字符串长度
${var:2:5}          # 子串：从位置 2 开始取 5 个字符

# 前缀/后缀删除
${var#prefix}       # 删除最短前缀匹配（常用：取去掉开头路径的文件名）
${var##prefix}      # 删除最长前缀匹配（常用：取最后一个路径段）
${var%suffix}       # 删除最短后缀匹配（常用：去掉文件扩展名）
${var%%suffix}      # 删除最长后缀匹配（常用：取第一段路径）

# 替换
${var/old/new}      # 替换第一个匹配
${var//old/new}     # 替换所有匹配
```

实际用法示例：

``` bash
filepath="/home/user/docs/report.txt"

echo ${filepath##*/}      # report.txt（取文件名）
echo ${filepath%/*}       # /home/user/docs（取目录路径）
echo ${filepath%.txt}     # /home/user/docs/report（去掉扩展名）

url="https://example.com/page"
echo ${url#*//}           # example.com/page（去掉协议头）
```

---

## 🌍 环境变量

### 常用环境变量

| 变量 | 说明 |
|------|------|
| `PATH` | 可执行文件的搜索路径，目录间用 `:` 分隔 |
| `HOME` | 当前用户的主目录（`~` 就是它） |
| `USER` | 当前登录用户名 |
| `SHELL` | 用户的默认 Shell 路径 |
| `LANG` | 系统语系（影响字符显示、排序、报错语言） |
| `TERM` | 终端类型（影响颜色、功能键） |
| `PS1` | 命令提示符格式（如 `[\u@\h \W]\$`） |
| `PS2` | 多行命令的续行提示符（默认 `>`） |
| `PWD` | 当前工作目录 |
| `OLDPWD` | 上一个工作目录（`cd -` 就是切换到这里） |
| `IFS` | 字段分隔符，默认为空格、Tab、换行 |
| `HISTSIZE` | 内存中保留的历史命令条数 |

### PATH 管理

`PATH` 决定了你在任何目录下能直接执行哪些命令。当你输入 `ls`，bash 会依次在 PATH 的每个目录里查找名为 `ls` 的可执行文件。

``` bash
echo $PATH
# /usr/local/bin:/usr/bin:/usr/local/sbin:/usr/sbin:/home/user/bin

# 追加目录（放在末尾，优先级较低）
export PATH="$PATH:/opt/myapp/bin"

# 前置目录（放在开头，优先级最高，会覆盖同名系统命令）
export PATH="/opt/myapp/bin:$PATH"
```

!!! warning "前置 PATH 的风险"

    将自定义目录前置到 `PATH` 时，若其中有与系统命令同名的文件（如自定义的 `ls`），系统命令将被屏蔽。除非有明确目的，否则优先使用追加方式。

### 查看环境变量

``` bash
env               # 显示所有环境变量
printenv PATH     # 显示指定环境变量
printenv          # 同 env
set               # 显示所有变量（含本地变量、函数），输出更多
export            # 显示所有已 export 的环境变量
```

### PS1 提示符定制

`PS1` 控制命令提示符的外观，支持以下特殊转义序列：

| 转义 | 含义 |
|------|------|
| `\u` | 当前用户名 |
| `\h` | 主机名（第一个 `.` 前的部分） |
| `\H` | 完整主机名 |
| `\w` | 完整工作目录（`~` 代表主目录） |
| `\W` | 工作目录的最后一段 |
| `\d` | 日期（Mon Jan 01 格式） |
| `\t` | 时间（24 小时 HH:MM:SS） |
| `\A` | 时间（24 小时 HH:MM） |
| `\$` | 普通用户显示 `$`，root 显示 `#` |
| `\#` | 当前会话中已执行的命令序号 |

``` bash
# 显示：[用户@主机 目录 时间 #序号]$
PS1='[\u@\h \w \A #\#]\$ '
```

---

## 🔍 命令行解析顺序

当你输入一条命令，bash 按以下顺序查找并执行，**先找到就执行，不再往下查**：

1. **绝对/相对路径**：`/bin/ls` 或 `./myscript.sh` 直接运行指定文件
2. **别名（alias）**：检查是否有同名别名
3. **Shell 内建命令（builtin）**：如 `cd`、`echo`、`read` 等
4. **`PATH` 中的外部命令**：依次在 PATH 各目录中查找

``` bash
# 查看命令的类型和来源
type ls             # ls is aliased to `ls --color=auto'
type cd             # cd is a shell builtin
type find           # find is /usr/bin/find

type -a echo        # 列出所有同名命令（含别名、内建、外部）
# echo is aliased to `echo -n'
# echo is a shell builtin
# echo is /usr/bin/echo

which find          # 只查找外部命令的路径：/usr/bin/find
```

---

## 🏷️ 别名（alias）

别名让你用短命令代替长命令，也可以为现有命令加上默认选项。

``` bash
# 创建别名
alias ll='ls -alh'
alias grep='grep --color=auto'
alias rm='rm -i'           # 删除前确认，防止误删
alias cls='clear'
alias h='history'

# 查看所有别名
alias

# 取消指定别名
unalias ll
```

!!! note "别名 vs 变量"

    别名创建了一个新的"命令"，可以直接在命令行执行；变量则需要 `echo $var` 才能显示内容。两者功能不同，不要混淆。

**持久化别名**：当前 Session 结束后别名就消失了。想让别名永久生效，需要写入配置文件：

``` bash
# 添加到用户配置文件（重新打开终端后生效）
echo "alias ll='ls -alh'" >> ~/.bashrc

# 立即生效
source ~/.bashrc
```

---

## 📜 命令历史

### 查看与执行历史

bash 会自动记录你执行过的命令，通过 `history` 查看和调用：

``` bash
history              # 查看所有历史（含序号）
history 20           # 只看最近 20 条

!!                   # 重复执行上一条命令
!n                   # 执行第 n 条历史命令（n 是序号）
!string              # 执行最近以 string 开头的命令
!$                   # 上一条命令的最后一个参数（非常常用！）
```

``` bash
# 反向交互式搜索（最实用的历史功能）
# 按 Ctrl+R，然后输入关键词，实时匹配
Ctrl+R
```

示例：

``` bash
history
  66  man rm
  67  alias
  68  man history
  69  history

!66          # 执行 man rm
!!           # 再次执行上一条（即 !66 → man rm）
!man         # 执行最近以 man 开头的命令（man history）
!$           # 假设上一条是 ls /etc，则此处等于 /etc
```

### 历史相关环境变量

| 变量 | 说明 |
|------|------|
| `HISTSIZE` | 内存中保留的历史条数（默认 1000） |
| `HISTFILESIZE` | `~/.bash_history` 文件保留的条数 |
| `HISTFILE` | 历史文件路径（默认 `~/.bash_history`） |
| `HISTCONTROL` | 控制记录行为：`ignoredups`（忽略重复）、`ignorespace`（忽略空格开头）、`ignoreboth`（两者都忽略） |

``` bash
# 忽略重复和空格开头的命令（加到 ~/.bashrc）
export HISTCONTROL=ignoreboth

# 清除当前 Session 的历史记录
history -c

# 强制将内存中的历史写入文件
history -w
```

!!! warning "历史记录的安全风险"

    `~/.bash_history` 中可能保存了含密码的命令（如 `mysql -u root -p密码`）。如果账号被入侵，攻击者可以直接读取这个文件获取敏感信息。对于高权限账号，建议定期清理历史记录。

---

## ⚙️ Shell 启动文件

### 登录 Shell vs 非登录 Shell

这是理解配置文件加载时机的关键：

- **登录 Shell（Login Shell）**：需要输入账号密码才能进入的 Shell，如直接登录 tty1~tty6、SSH 登录
- **非登录 Shell（Non-login Shell）**：在已登录的环境中直接打开，如在图形界面中开终端窗口、在 bash 中再执行 `bash`

两者读取的配置文件**不同**！

``` bash
bash -l         # 以 login shell 方式启动（可用于测试）
bash            # 以 non-login shell 方式启动
echo $0         # -bash（前面有 -）表示 login shell
```

### 配置文件加载时机

| 文件 | 加载时机 | 作用 |
|------|---------|------|
| `/etc/profile` | 登录 Shell 启动 | 系统级全局设置，所有用户 |
| `/etc/profile.d/*.sh` | 由 `/etc/profile` 调用 | 模块化全局设置（语系、颜色等） |
| `~/.bash_profile` | 登录 Shell 启动 | 用户级登录设置（优先读此文件） |
| `~/.bash_login` | 登录 Shell 启动 | 若无 `~/.bash_profile` 则读此 |
| `~/.profile` | 登录 Shell 启动 | 若前两个都不存在则读此 |
| `~/.bashrc` | **非登录 Shell** 启动 | 用户级交互设置（别名、函数等） |
| `/etc/bash.bashrc`（Debian）| 非登录 Shell 启动 | 系统级非登录设置 |
| `/etc/bashrc`（RHEL） | 由 `~/.bashrc` 调用 | 系统级非登录设置 |
| `~/.bash_logout` | 注销时执行 | 清理工作（清屏、备份等） |

**最常用的配置文件是 `~/.bashrc`**——无论哪种方式，最终都会读到它（登录 Shell 通过 `~/.bash_profile` 间接调用）。因此，别名、自定义函数、PATH 追加等个人配置都写在这里。

### source：立即生效

修改配置文件后，不需要重新登录，用 `source` 立即加载：

``` bash
source ~/.bashrc    # 重新加载配置
. ~/.bashrc         # 等同于 source（. 是 source 的简写）
```

---

## 🌟 通配符（Globbing）

通配符让 bash 在执行命令前先展开文件名模式，大幅减少重复输入。

| 模式 | 含义 | 示例 |
|------|------|------|
| `*` | 任意个任意字符（含空） | `*.txt`、`doc*` |
| `?` | 恰好一个任意字符 | `file?.log`、`?.sh` |
| `[abc]` | 括号中的任意一个字符 | `[abc]*.txt` |
| `[a-z]` | 字符范围内的任意一个 | `[0-9]*`、`[a-zA-Z]*` |
| `[!abc]` 或 `[^abc]` | 不在括号中的任意一个 | `[!0-9]*` |
| `{a,b,c}` | 展开为列表中的每一项 | `{jpg,png,gif}` |
| `**` | 递归匹配目录（需开启 globstar） | `**/*.py` |

``` bash
# 列出所有 .txt 文件
ls *.txt

# 列出 file1.log ~ file9.log
ls file[0-9].log

# 列出非小写字母开头的文件
ls /etc/[^a-z]*

# 同时操作多种扩展名
cp *.{jpg,png,gif} /backup/images/

# 开启 ** 递归匹配（需加到 ~/.bashrc 持久化）
shopt -s globstar
ls **/*.py          # 列出当前目录及所有子目录的 .py 文件
```

---

## ✏️ 引号与转义

### 三种引号的区别

这是 bash 中容易混淆的地方，关键在于**哪些展开会被阻止**：

| 引号 | 变量展开 `$var` | 命令替换 `$(cmd)` | 转义 `\` | 示例 |
|------|:--------------:|:----------------:|:-------:|------|
| 无引号 | ✅ | ✅ | ✅ | 会进行分词和通配符展开 |
| 双引号 `"..."` | ✅ | ✅ | ✅ | 阻止分词和通配符展开 |
| 单引号 `'...'` | ❌ | ❌ | ❌ | 完全字面量，什么都不展开 |

``` bash
name="World"

echo "Hello $name"       # Hello World（变量展开）
echo 'Hello $name'       # Hello $name（字面量，不展开）
echo "Price: \$5"        # Price: $5（\ 转义 $）

# 命令替换
echo "Kernel: $(uname -r)"    # 推荐写法
echo "Kernel: `uname -r`"     # 旧写法，不推荐

# 含空格的路径必须用引号
cd "/home/my user/docs"       # ✅
cd /home/my user/docs         # ❌ bash 会当成两个参数
```

### 转义字符

`\` 转义紧跟其后的**单个字符**，使其失去特殊含义：

``` bash
echo "It\'s a test"      # 在双引号内转义单引号
touch my\ file.txt       # 文件名含空格
echo \$PATH              # 输出字面量 $PATH（不展开变量）
echo line1\
line2                    # \ 在行尾表示续行，两行视为一行
```

---

## ⌨️ 命令行编辑快捷键（Readline）

Bash 使用 GNU Readline 库处理命令行输入。以下快捷键在任何 Readline 程序（如 Python REPL、SQLite CLI 等）中都有效，掌握这些能大幅提升终端操作效率。

### 光标移动

| 快捷键 | 功能 |
|--------|------|
| `Ctrl+A` | 移到行首 |
| `Ctrl+E` | 移到行末 |
| `Ctrl+F` | 前进一个字符（= 右箭头键） |
| `Ctrl+B` | 后退一个字符（= 左箭头键） |
| `Alt+F` | 前进一个词（光标跳到下一词的末尾） |
| `Alt+B` | 后退一个词（光标跳到上一词的开头） |

### 删除字符

| 快捷键 | 功能 |
|--------|------|
| `Ctrl+D` | 删除光标处字符（delete） |
| `Backspace` 或 `Ctrl+H` | 删除光标前字符 |
| `Alt+D` | 删除光标后一个词 |
| `Ctrl+W` | 删除光标前一个词 |
| `Ctrl+K` | 从光标处删到行末（kill）存入 kill ring |
| `Ctrl+U` | 从光标处删到行首（unix-line-discard）存入 kill ring |

### 剪贴与粘贴（Kill/Yank）

Readline 使用 kill ring（剪切缓冲）存储删除内容，类似 Emacs。

| 快捷键 | 功能 |
|--------|------|
| `Ctrl+K` | 剪切光标到行末 |
| `Ctrl+U` | 剪切光标到行首 |
| `Ctrl+W` | 剪切前一个词 |
| `Ctrl+Y` | 粘贴（yank）kill ring 中最后剪切的内容 |
| `Alt+Y` | 循环粘贴：连续按可轮转 kill ring 中的更早内容 |

### 历史导航

| 快捷键 | 功能 |
|--------|------|
| `Ctrl+P` | 上一条命令（= 上箭头键） |
| `Ctrl+N` | 下一条命令（= 下箭头键） |
| `Ctrl+R` | 反向增量搜索历史（最常用），按 Enter 执行，Ctrl+G 退出搜索 |
| `Ctrl+S` | 正向搜索历史（可能被流控制阻止，见下） |

!!! note "Ctrl+S 可能不工作"

    某些终端配置中 `Ctrl+S` 被用于流控制（XOFF），导致终端冻结。禁用方法：
    
    ``` bash
    stty -ixon
    ```
    
    将此命令加到 `~/.bashrc` 后面以持久化。

### 其他常用

| 快捷键 | 功能 |
|--------|------|
| `Ctrl+L` | 清屏（等同 `clear`） |
| `Ctrl+C` | 中断当前命令 |
| `Ctrl+Z` | 挂起到后台（可用 `fg` 恢复） |
| `Ctrl+T` | 交换光标处和前一字符 |
| `Alt+T` | 交换当前词与前一词 |
| `Alt+U` | 当前词改为大写 |
| `Alt+L` | 当前词改为小写 |
| `Alt+C` | 当前词首字大写 |
| `Alt+.` | 插入上一命令的最后一个参数 |

!!! tip "快速回到上一条命令的参数"

    ``` bash
    cd /very/long/path/to/project
    ls !$              # !$ 展开为上条命令的最后参数 = /very/long/path/to/project
    
    # 或用 Readline：
    cd /very/long/path/to/project
    cat Alt+.          # 粘贴上条命令的最后参数
    ```

## ⚙️ 终端控制（stty）

`stty`（set tty）用于查看和修改终端驱动层的键盘映射与行为。当按键不工作或需要自定义键位时有用。

### 查看当前配置

``` bash
stty -a
# 输出示例：
# speed 38400 baud; rows 30; columns 120; line = 0;
# intr = ^C; quit = ^\; erase = ^?; kill = ^U; eof = ^D; eol = <undef>;
# eol2 = <undef>; swtch = <undef>; start = ^Q; stop = ^S; susp = ^Z;
# ...
```

### 关键字段含义

| 字段 | 含义 | 默认 |
|------|------|------|
| `intr` | 中断（等同发送 SIGINT） | `^C` (Ctrl+C) |
| `quit` | 退出（等同发送 SIGQUIT） | `^\` (Ctrl+\) |
| `erase` | 删除一个字符 | `^?` (Backspace) |
| `kill` | 清除当前行 | `^U` (Ctrl+U) |
| `eof` | 文件末尾（告诉程序无更多输入） | `^D` (Ctrl+D) |
| `susp` | 挂起到后台 | `^Z` (Ctrl+Z) |
| `start` / `stop` | 流控制（恢复/冻结输出） | `^Q` / `^S` |

### 修改键位

``` bash
# 将删除键改为 Ctrl+H（某些远程终端可能需要）
stty erase ^H

# 禁用流控制（解锁 Ctrl+S）
stty -ixon

# 恢复默认
stty sane
```

## Shell 初始化文件深度解析

理解哪个文件何时被执行是编写可靠配置的关键。

### 登录 Shell vs 非登录 Shell

判断当前是否为登录 Shell：

``` bash
# 登录 Shell：返回 -bash 或 -sh（-号前缀表示登录）
# 非登录 Shell：返回 bash 或 sh（无前缀）
echo $0

# 或者检查环境变量
echo $MAIL   # 只在登录 Shell 中设置
```

| 类型 | 定义 | 例子 |
|------|------|------|
| **登录 Shell** | 需要输入用户名密码的 Shell | SSH 登录、虚拟终端（tty）、`su - user` |
| **非登录 Shell** | 从已登录的 Shell 启动 | 打开终端模拟器、`bash` 脚本执行、`(command)` 子 Shell |
| **交互式 Shell** | 接受用户输入的 Shell | 所有上述情景 |
| **非交互式 Shell** | 从脚本运行的 Shell | cron 任务、后台脚本 |

### 配置文件加载链

#### 🔵 登录交互式 Shell（SSH 登录、tty 登录）

加载顺序（按优先级，找到第一个就停）：

1. `/etc/profile` — 系统全局配置
2. `/etc/profile.d/*.sh` — 模块化配置目录（发行版会在此放一些标准初始化）
3. 以下三个选其一（**完全互斥**）：
   - `~/.bash_profile`（Bash 登录首选）
   - `~/.bash_login`（Bash 备选）
   - `~/.profile`（POSIX Shell 兼容）

登出时读取：`~/.bash_logout`

#### 🔵 非登录交互式 Shell（打开终端、`bash` 命令）

加载顺序：

1. `/etc/bash.bashrc`（Debian/Ubuntu）或 `/etc/bashrc`（Red Hat）
2. `~/.bashrc`

#### 🔵 非交互式 Shell（脚本执行）

加载 `$BASH_ENV` 指向的文件（通常不设，脚本手动 source 配置）。

### 该放在哪里？速查表

| 内容 | 放置位置 | 说明 |
|------|---------|------|
| **环境变量**（如 `export PATH=/usr/local/bin:$PATH`） | `~/.bash_profile` | 仅需在登录时执行一次，传给所有子 Shell |
| **别名** / **函数** / **PS1 提示符** | `~/.bashrc` | 每次打开 Shell 都需要，包括非登录的终端 |
| **两者都要** | `~/.bash_profile` 中 source `~/.bashrc` | 常见做法，登录 Shell 也能用别名 |
| **系统全局配置** | `/etc/profile.d/*.sh` | 放在以 `.sh` 结尾的脚本中，所有用户都加载 |

### 典型的 ~/.bash_profile 写法

为了让登录 Shell 也能使用 `~/.bashrc` 中定义的别名和函数，标准做法是在 `~/.bash_profile` 中 source 它：

``` bash
# ~/.bash_profile
export PATH=/usr/local/bin:$PATH
export EDITOR=vim

# 如果存在 ~/.bashrc，就加载它
if [ -f ~/.bashrc ]; then
    source ~/.bashrc
fi
```

然后 `~/.bashrc` 中放别名和交互式配置：

``` bash
# ~/.bashrc
alias ll='ls -lh'
alias grep='grep --color=auto'

PS1='\u@\h:\w\$ '
```

!!! warning "发行版差异"

    - **Debian/Ubuntu**：`~/.bash_profile` 可能不存在或不被加载，改用 `~/.profile`
    - **Red Hat/RHEL**：`~/.bash_profile` 默认存在且会 source `~/.bashrc`，所以上述做法已经内置

### source 与子 Shell

- `source file` 或 `. file`：在**当前 Shell 进程**中执行，变量和别名保存
- `bash file` 或 `./file`：启动新的**子 Shell** 执行，父 Shell 的配置对子 Shell 不可见

---

## 🖥️ 发行版差异


=== "Debian / Ubuntu"

    - 默认 Shell：`bash`；`/bin/sh` 链接到 `dash`（更轻量，不是 bash！）
    - `~/.bashrc` 默认已包含彩色提示符和 `ll`、`la` 等别名
    - 系统级非登录配置：`/etc/bash.bashrc`
    - 登录配置加载链：`/etc/profile` → `~/.profile` → `~/.bashrc`
    - 判断当前 `sh` 是否为 dash：`ls -la /bin/sh`

=== "Red Hat / RHEL / CentOS"

    - 默认 Shell：`bash`；`/bin/sh` 也链接到 `bash`
    - `~/.bash_profile` 默认会 source `~/.bashrc`，保证登录 Shell 也能用到别名
    - 系统级非登录配置：`/etc/bashrc`
    - `/etc/profile.d/` 下有大量模块化配置（颜色、语系、补全等）
    - 登录配置加载链：`/etc/profile` → `~/.bash_profile` → `~/.bashrc`

!!! tip "脚本中用 `#!/bin/bash` 而非 `#!/bin/sh`"

    Debian/Ubuntu 的 `/bin/sh` 是 dash，语法比 bash 更严格（不支持 `declare`、`[[ ]]` 等 bash 扩展）。如果脚本使用了 bash 特有语法，必须在首行写 `#!/bin/bash`，否则在 Debian 上会报错。


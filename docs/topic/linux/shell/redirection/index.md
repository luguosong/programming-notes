---
title: 重定向与管道
---

# 重定向与管道

**本文你会学到**：

- 三条标准数据流（stdin、stdout、stderr）的文件描述符
- 输出重定向的语法（`>`、`>>`、`2>`、`2>>`）
- 合并 stdout 和 stderr 的技巧（`2>&1`、`>&2`）
- 输入重定向与 here document 的使用
- 管道（pipe）的工作原理与连接多条命令
- exec 命令改变当前 shell 的文件描述符
- 避免覆盖写导致的数据丢失
- 常见重定向组合的场景应用

## 三条标准数据流

Linux 为每个进程默认打开三个文件描述符：

| 名称 | 文件描述符 | 默认来源/去向 | 重定向符号 |
|------|-----------|--------------|-----------|
| 标准输入（stdin） | `0` | 键盘 | `<` 或 `<<` |
| 标准输出（stdout） | `1` | 终端屏幕 | `>` 或 `>>` |
| 标准错误（stderr） | `2` | 终端屏幕 | `2>` 或 `2>>` |

执行 `cat /etc/crontab /etc/nofile` 时，能读到的文件内容从 stdout 输出，找不到文件的错误从 stderr 输出——两路数据默认都出现在屏幕上，看起来混在一起。重定向的作用就是把这两路数据分开、导向不同地方。

## 输出重定向

### 基础写法

``` bash
command > file        # 覆盖写入 stdout（文件不存在则创建）
command >> file       # 追加写入 stdout
command 2> file       # 覆盖写入 stderr
command 2>> file      # 追加写入 stderr
```

!!! warning "覆盖是危险操作"

    `>` 会先清空目标文件，再写入新内容。对已有数据的文件操作时，用 `>>` 追加更安全。

### 同时处理 stdout 和 stderr

当你想把正确输出和错误输出都保存下来，需要合并两路数据：

``` bash
command > file 2>&1    # stdout 写入 file，再把 stderr 合并到 stdout
command &> file        # 同上，bash 的简写形式
```

!!! tip "2>&1 的顺序很重要"

    `command > file 2>&1` 的执行顺序是：先把 stdout 重定向到 file，再把 stderr 合并到当前的 stdout（即 file）。

    如果写反成 `command 2>&1 > file`，则是：先把 stderr 合并到当前 stdout（即终端），再把 stdout 重定向到 file——结果是 stderr 仍然输出到终端。

### 分别保存到不同文件

``` bash
command > ok.log 2> err.log    # 正确输出 → ok.log，错误输出 → err.log
```

### 丢弃输出

`/dev/null` 是系统的"黑洞设备"，写入的数据都会被丢弃：

``` bash
command 2> /dev/null           # 静默错误，只保留正确输出
command > /dev/null 2>&1       # 丢弃所有输出（脚本后台任务常用）
```

## 输入重定向

### 从文件读取 stdin

``` bash
command < file    # 将文件内容作为命令的标准输入
```

等效于把文件内容"假装"成键盘输入。例如 `wc -l < /etc/passwd` 统计行数，和 `cat /etc/passwd | wc -l` 效果相同，但前者不启动额外进程。

### Here Document

当你需要向命令输入多行文本，不想专门创建一个临时文件时，用 Here Document：

``` bash
command << EOF
第一行内容
第二行内容
EOF
```

`EOF` 只是约定俗成的标记，可以换成任意单词。输入到同名标记时自动结束，等价于按 `Ctrl-D`。

典型用途——直接生成配置文件：

``` bash
cat << EOF > config.txt
HOST=localhost
PORT=8080
DB_NAME=myapp
EOF
```

### Here String

bash 特有的简写，适合把单个字符串作为 stdin 传入：

``` bash
command <<< "string"
```

例如 `grep "root" <<< "$(cat /etc/passwd)"` 或更简单的 `wc -c <<< "hello"`（统计字符数）。

## tee：分叉数据流

管道 `|` 会把数据完全交给下一条命令，屏幕上就看不到中间结果了。`tee` 解决这个问题——它把数据同时送往屏幕和文件，像一个 T 形接头：

``` bash
command | tee file              # 输出到终端，同时写入 file（覆盖）
command | tee -a file           # 输出到终端，同时追加到 file
command | tee file1 file2       # 同时写入多个文件
command 2>&1 | tee log.txt      # 连同错误输出一起捕获
```

实际场景：观察某个长时间命令的输出，同时保存日志：

``` bash
make 2>&1 | tee build.log
```

## 管道

管道符 `|` 把前一条命令的 stdout 接到下一条命令的 stdin：

``` bash
cmd1 | cmd2           # cmd1 的输出作为 cmd2 的输入
cmd1 | cmd2 | cmd3    # 链式管道，数据依次流经每个命令
```

!!! warning "管道只传递 stdout"

    `|` 默认只传递 stdout，stderr 不会流入管道。如果想让错误信息也进入管道处理，先合并：`command 2>&1 | ...`

能作为管道命令的，必须能接受 stdin 输入，例如 `less`、`grep`、`sort`、`wc`、`awk`。而 `ls`、`cp`、`mv` 不接受 stdin，不能放在管道右侧。

## 常用管道组合

### 分页查看

``` bash
command | less    # 上下滚动查看，q 退出
command | more    # 逐屏显示
```

### 过滤行

``` bash
command | grep "pattern"      # 只保留匹配的行
command | grep -v "exclude"   # 删除匹配的行（反向过滤）
```

### 排序与去重

``` bash
sort file | uniq              # 先排序，再去重（uniq 只删除相邻重复行）
sort -u file                  # 一步完成排序去重
command | sort | uniq -c | sort -rn    # 统计每行出现次数并降序排列
```

### 统计行数

``` bash
command | wc -l     # 统计行数
wc -l < file        # 直接从文件统计（比 cat file | wc -l 更简洁）
```

### 取前/后 N 行

``` bash
command | head -20           # 取前 20 行
command | tail -20           # 取后 20 行
command | tail -f logfile    # 实时追踪文件末尾（监控日志）
```

### 字段处理

``` bash
command | cut -d: -f1           # 以 : 为分隔符，取第 1 个字段
command | cut -d: -f1,3         # 取第 1 和第 3 字段
command | awk '{print $1}'      # 取第 1 个空白分隔字段（比 cut 更灵活）
command | awk -F: '{print $1}'  # 以 : 为分隔符取字段
```

### 实用组合示例

``` bash
# 找占用空间最大的 10 个文件/目录
du -sh * | sort -rh | head -10

# 统计登录次数最多的用户
last | cut -d' ' -f1 | sort | uniq -c | sort -rn | head

# 查找包含特定字符串的进程
ps aux | grep "nginx" | grep -v grep
```

## xargs：将 stdin 转为命令参数

有些命令（如 `rm`、`chmod`、`cp`）不读取 stdin，只接受命令行参数。`xargs` 充当转换器，把 stdin 的内容变成参数传给指定命令：

``` bash
# 基本用法：将前面的输出作为参数
echo "a b c" | xargs echo           # 输出: a b c
find . -name "*.log" | xargs rm -f  # 删除所有 .log 文件
```

文件名含空格时，上面的写法会出错（空格被当作参数分隔符）。安全写法：

``` bash
find . -name "*.log" -print0 | xargs -0 rm -f
# -print0 用 null 字符分隔，-0 告知 xargs 以 null 字符为分隔符
```

其他常用选项：

``` bash
# -n：每次最多传 N 个参数
echo "a b c d" | xargs -n 2 echo
# 输出:
# a b
# c d

# -I：指定占位符，灵活控制参数位置
find . -name "*.txt" | xargs -I {} cp {} /backup/

# -P：并行执行，加速批量处理
cat urls.txt | xargs -I {} -P 4 curl -O {}
```

## 命令执行控制

### 顺序执行

``` bash
cmd1 ; cmd2    # 依次执行，不管 cmd1 是否成功
```

适合无关联的命令连写，例如 `sync; sync; shutdown -h now`。

### 短路逻辑

Shell 通过 `$?`（上一条命令的退出码，0 为成功）来判断是否继续执行：

``` bash
cmd1 && cmd2    # cmd1 成功（$?=0）才执行 cmd2
cmd1 || cmd2    # cmd1 失败（$?≠0）才执行 cmd2
```

常见模式：

``` bash
# 全部成功才继续（构建/测试/安装三步）
make && make test && make install

# 失败则立即退出脚本
cd /some/dir || exit 1

# 目录不存在则创建
ls /tmp/mydir || mkdir /tmp/mydir

# 检测主机是否在线
ping -c1 host &> /dev/null && echo "up" || echo "down"
```

!!! tip "注意运算符优先级"

    `cmd1 && cmd2 || cmd3` 的执行逻辑：cmd1 成功则执行 cmd2，cmd2 成功则跳过 cmd3；cmd1 失败则跳过 cmd2 直接执行 cmd3。

    惯用模式是 `成功分支 && 操作 || 失败分支`，cmd2 和 cmd3 最好确保不会失败，否则逻辑容易出错。

### 子 Shell 与当前 Shell

``` bash
(cmd1 ; cmd2)     # 在子 Shell 中执行，变量/目录变更不影响当前 Shell
{ cmd1 ; cmd2 ; } # 在当前 Shell 中执行（注意前后空格和末尾分号）
```

## 进程替换

进程替换用 `<(cmd)` 把命令的输出"伪装"成一个文件，让需要文件参数的命令也能接受动态数据：

``` bash
diff <(ls dir1) <(ls dir2)    # 比较两个目录的文件列表差异
comm <(sort file1) <(sort file2)  # 比较两个已排序文件的交集/差集
```

输出端也有对应写法：

``` bash
command | tee >(gzip > file.gz)    # 同时输出到终端和压缩文件
```

## 后台执行与任务控制

### 将任务放到后台

``` bash
command &     # 后台运行，立即返回 shell 提示符
```

### 查看和管理后台任务

``` bash
jobs           # 列出当前 Shell 的后台任务（显示任务编号）
fg %1          # 把编号为 1 的后台任务调回前台
bg %1          # 让暂停的任务继续在后台运行
Ctrl-Z         # 暂停当前前台任务（发送 SIGTSTP）
```

### 脱离终端运行

后台任务 `&` 与终端绑定——关闭终端或退出 SSH 时，任务会收到 SIGHUP 信号而终止。

``` bash
nohup command &     # 忽略 SIGHUP，终端关闭后继续运行
disown %1           # 从 jobs 列表中移除任务，使其脱离当前 Shell
```

!!! tip "长时间任务推荐 tmux/screen"

    `nohup` 简单够用，但输出会写到 `nohup.out`。需要随时回来查看进度或与任务交互，推荐使用 `tmux` 或 `screen` 管理会话。


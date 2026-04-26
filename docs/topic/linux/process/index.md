---
title: 进程管理与工作控制
---

# 进程管理与工作控制

**本文你会学到**：

- 程序与进程的根本区别，以及进程的关键属性（PID、PPID、UID、NI、TTY）
- 进程的生命周期与状态转移（Running、Sleep、Stopped、Zombie）
- Fork-and-exec 的底层机制与父子进程关系
- 孤儿进程与僵尸进程的成因及消灭方法
- 用 `ps`、`top`、`jobs` 等工具查看、管理进程
- 前台进程与后台进程的控制（`Ctrl+Z`、`fg`、`bg`）
- 进程的信号处理与优先级调整（`kill`、`nice`、`renice`）
- 从进程树追踪程序的执行关系

## 进程基础概念

### 程序 vs 进程

**程序（program）**是存放在磁盘上的静态二进制文件，如 `/bin/bash`；当它被执行、载入内存并开始运行时，就成为了**进程（process）**。同一个程序可以被多次执行，产生多个独立的进程，彼此互不干扰。

| 概念 | 说明 |
|------|------|
| 程序（program） | 磁盘上的静态可执行文件 |
| 进程（process） | 运行中的程序实例，拥有独立的内存空间和 PID |

### 进程的关键属性

Linux 内核用以下属性来管理每个进程：

| 属性 | 含义 |
|------|------|
| `PID` | 进程 ID，系统唯一标识 |
| `PPID` | 父进程 ID |
| `UID` / `GID` | 实际运行者的用户/组 ID，决定进程的权限 |
| `NI` | Nice 值，影响 CPU 调度优先级 |
| `TTY` | 关联的终端（无终端的后台进程显示 `?`） |

### 进程状态

`ps` 和 `top` 的 `STAT` 列会显示进程当前状态：

| 状态码 | 含义 |
|--------|------|
| `R` | Running — 正在 CPU 上运行或在运行队列中等待 |
| `S` | Sleep — 可中断睡眠，等待某个事件（信号可唤醒） |
| `D` | 不可中断睡眠 — 通常在等待 I/O，不响应信号 |
| `T` | Stopped — 已被暂停（`Ctrl+Z` 或 `SIGSTOP`） |
| `Z` | Zombie — 已结束但父进程未回收其退出状态 |
| `I` | Idle — 空闲内核线程 |

状态码后还可附加修饰符，如 `s`（会话领导者）、`+`（前台进程组）、`<`（高优先级）、`N`（低优先级）。

### 父子进程与 fork/exec

当 bash 执行 `ls` 时，底层发生的是 `fork-and-exec` 流程：

1. 父进程（bash）调用 `fork()`，复制出一个与自身几乎完全相同的子进程
2. 子进程调用 `exec()`，用 `ls` 的程序码替换自身
3. bash 等待子进程结束，然后回收其退出状态

子进程可以继承父进程的环境变量。通过 `PPID` 字段可以追踪任何进程的父进程。

### 孤儿进程与僵尸进程

- **孤儿进程**：父进程先于子进程退出，子进程会被 `systemd`（PID 1）收养
- **僵尸进程**：子进程已退出，但父进程没有调用 `wait()` 来回收退出状态，进程条目卡在内核中

`ps aux` 中看到 `<defunct>` 标记的进程就是僵尸进程。僵尸进程不占用 CPU，但占用一个进程表槽。如果数量过多，说明父进程存在 bug。

!!! tip "消灭僵尸进程的正确姿势"

    不能直接 `kill -9` 僵尸进程（它已经"死"了）。应该找到其父进程（`ps -o ppid= -p <zombie_pid>`），然后终止或修复父进程，由内核自动清理僵尸。

## 查看进程

### ps — 进程快照

`ps` 在某一时刻对系统进程状态做快照。有两种常用风格：

``` bash
# BSD 风格（最常用）
ps aux               # 列出所有进程的完整信息
ps aux | grep nginx  # 过滤特定进程

# System V 风格
ps -ef               # 与 ps aux 类似，增加 PPID 列
ps --forest          # 树状显示父子关系（System V）

# 自定义输出列
ps -eo pid,ppid,user,comm,pcpu,pmem
```

`ps aux` 各列含义：

| 列名 | 含义 |
|------|------|
| `USER` | 进程所属用户 |
| `PID` | 进程 ID |
| `%CPU` | CPU 使用率 |
| `%MEM` | 物理内存占用百分比 |
| `VSZ` | 虚拟内存大小（KB） |
| `RSS` | 常驻内存大小，实际占用的物理内存（KB） |
| `TTY` | 关联终端，`?` 表示无终端 |
| `STAT` | 进程状态 |
| `START` | 启动时间 |
| `TIME` | 累计占用 CPU 时间 |
| `COMMAND` | 启动命令 |

### pstree — 进程树

当你需要快速找出某个进程是谁启动的，`pstree` 最直观：

``` bash
pstree               # 展示所有进程的树状关系
pstree -p            # 同时显示 PID
pstree -u            # 同时显示所属用户
pstree -Ap           # ASCII 线条 + PID（推荐，避免乱码）
pstree alice         # 只展示 alice 用户的进程树
```

### top — 动态实时监控

`top` 持续刷新显示系统状态，默认每 5 秒更新一次：

``` bash
top                  # 实时监控（默认按 CPU 使用率排序）
top -d 2             # 每 2 秒刷新一次
top -p 1234          # 只监控 PID=1234
```

**top 头部信息解读：**

- 第 1 行：当前时间、运行时间、在线用户数、**负载均值**（1/5/15 分钟）
    - 负载均值 > CPU 核心数时需要关注，表示进程在排队等待
- 第 2 行：进程总数及各状态数量，`zombie` 不为 0 时需排查
- 第 3 行：CPU 使用率细分，`wa`（I/O wait）高时表示存在磁盘瓶颈
- 第 4/5 行：物理内存与 Swap 使用量

**top 交互快捷键：**

| 键 | 操作 |
|----|------|
| `q` | 退出 |
| `1` | 展开/折叠各 CPU 核心使用率 |
| `P` | 按 CPU 使用率排序 |
| `M` | 按内存使用率排序 |
| `k` | 向指定 PID 发送信号 |
| `r` | 修改指定进程的 nice 值 |
| `<` / `>` | 切换排序列 |

### htop — 增强版交互监控

`htop` 支持鼠标操作和彩色显示，交互体验更好（需单独安装）：

``` bash
htop
```

### pgrep / pidof — 按名查找 PID

当你只需要获取进程的 PID，而不是完整列表：

``` bash
pgrep nginx          # 查找所有匹配 "nginx" 的进程 PID
pgrep -u alice       # 查找 alice 用户的所有进程 PID
pgrep -l nginx       # 同时显示进程名
pidof sshd           # 精确匹配进程名（比 pgrep 更严格）
```

## 发送信号

进程之间通过**信号（signal）**通信。`kill` 命令并不只是"杀进程"，而是向进程发送任意信号。

### 常用信号速查

| 信号名 | 编号 | 触发方式 | 含义 |
|--------|------|----------|------|
| `SIGHUP` | 1 | `kill -1` | 挂起信号，常用于让进程**重载配置文件** |
| `SIGINT` | 2 | `Ctrl+C` | 键盘中断，通知进程优雅退出 |
| `SIGQUIT` | 3 | `Ctrl+\` | 键盘退出，类似 SIGINT 但会产生 core dump |
| `SIGKILL` | 9 | `kill -9` | **强制终止**，不可被进程捕获或忽略 |
| `SIGTERM` | 15 | `kill`（默认）| 请求进程**优雅终止**，进程可以捕获并做清理 |
| `SIGSTOP` | 19 | `Ctrl+Z` / `kill -19` | **暂停进程**，不可被捕获或忽略 |
| `SIGCONT` | 18 | `kill -18` / `bg` | 继续被 SIGSTOP 暂停的进程 |
| `SIGUSR1` | 10 | `kill -10` | 用户自定义信号（各程序含义不同） |
| `SIGUSR2` | 12 | `kill -12` | 用户自定义信号 |

``` bash
kill -l              # 列出所有可用信号及编号
```

### kill — 向 PID 发送信号

``` bash
kill PID             # 发送 SIGTERM（15），请求优雅退出
kill -9 PID          # 发送 SIGKILL，强制杀死，最后手段
kill -1 PID          # 发送 SIGHUP，让进程重载配置（如 nginx、sshd）
kill -SIGTERM PID    # 用信号名代替编号，效果相同
```

!!! warning "SIGKILL 的副作用"

    `-9` 是强制终止，进程来不及做任何清理工作。`vim` 被 `-9` 杀死后会留下 `.swp` 锁文件；数据库进程被强杀可能导致数据文件损坏。**优先使用 `SIGTERM`，只有进程无响应时才用 `-9`。**

### pkill — 按名称发送信号

``` bash
pkill nginx          # 向所有名为 nginx 的进程发送 SIGTERM
pkill -9 nginx       # 强制杀死所有 nginx 进程
pkill -HUP sshd      # 让 sshd 重载配置
pkill -u alice       # 杀死 alice 用户的所有进程
```

### killall — 按命令名批量发送信号

``` bash
killall nginx        # 杀死所有同名进程（精确匹配命令名）
killall -9 httpd     # 强制杀死所有 httpd 进程
killall -i -9 bash   # 逐个询问是否杀死每个 bash 进程（交互模式）
```

!!! tip "kill vs pkill vs killall"

    - `kill` 精准，需要知道 PID；`kill %1` 操作的是工作号，`kill 1` 操作的是 PID（systemd！）
    - `pkill` 按正则匹配进程名，适合"杀掉所有包含某关键字的进程"
    - `killall` 精确匹配完整命令名（不含参数），适合"杀掉某个服务的所有实例"

## 进程优先级

### Nice 值与 PRI

Linux 用 `Nice` 值（NI）来影响 CPU 调度优先级。实际执行优先级 `PRI` 由内核动态计算（`PRI = PRI基础 + NI`），用户只能调整 `NI`。

- Nice 值范围：**-20**（最高优先级）到 **19**（最低优先级）
- **普通用户**只能将 nice 值调高（降低优先级），范围 `0~19`
- **root** 可以自由设置 `-20~19`，包括调高优先级

### 启动时指定 nice 值

``` bash
nice -n 10 tar -zcvf backup.tar.gz /data    # 以低优先级执行备份，避免抢占资源
nice -n -5 ./important-task                  # 需要 root 权限
```

### 调整运行中进程的 nice 值

``` bash
renice -n 5 -p 1234         # 将 PID=1234 的进程 nice 值改为 5
renice -n 10 -u alice       # 将 alice 所有进程的 nice 值改为 10
```

也可以在 `top` 中按 `r` 键，然后输入 PID 和新的 nice 值来交互式调整。

## 工作控制（Job Control）

工作控制（Job Control）是 `bash` 提供的机制，允许在单个终端内同时管理多个任务。每个任务都是当前 bash 的子进程，与其他终端的 bash 互相独立。

### 前台与后台

- **前台（foreground）**：占据终端输入/输出，可以用 `Ctrl+C` 中断
- **后台（background）**：在幕后运行，不占终端，可以继续输入其他命令

``` bash
# 将命令直接丢入后台运行
command &
# 后台任务有 stdout/stderr 输出时会干扰前台，建议重定向
command > output.log 2>&1 &

# 暂停当前前台任务，将其放入后台（Stopped 状态）
Ctrl+Z
```

### jobs — 查看当前后台任务

``` bash
jobs             # 列出所有后台任务及状态
jobs -l          # 同时显示 PID
jobs -r          # 只显示 Running 状态的任务
jobs -s          # 只显示 Stopped 状态的任务
```

任务状态说明：

| 状态 | 含义 |
|------|------|
| `Running` | 在后台正常运行中 |
| `Stopped` | 被暂停（`Ctrl+Z` 或 `SIGSTOP`） |
| `Done` | 已完成（下次调用 jobs 时消失） |

`jobs` 输出中，`+` 标记的是"默认任务"（`fg`/`bg` 不加参数时操作的目标），`-` 是次新的任务。

### fg / bg — 调度任务

``` bash
fg               # 将默认任务（+）调回前台
fg %1            # 将编号为 1 的任务调回前台
fg %2            # 将编号为 2 的任务调回前台
bg %1            # 让编号为 1 的 Stopped 任务在后台继续运行
bg               # 让默认 Stopped 任务在后台运行
```

### kill 工作号

``` bash
kill %1          # 向任务1发送 SIGTERM（优雅退出）
kill -9 %2       # 强制杀死任务2
```

!!! warning "kill %1 与 kill 1 的区别"

    `kill %1` 操作的是**工作号 1**，`kill 1` 操作的是 **PID=1 的进程（systemd）**。千万别搞混！

## 脱离终端运行

bash 的后台任务依然与终端绑定——关闭终端或断开 SSH 后，`SIGHUP` 信号会发给该 shell 的所有子进程，导致后台任务被终止。如果你需要任务在退出登录后继续运行，有以下几种方式：

### nohup — 忽略 SIGHUP

``` bash
nohup command &                          # 后台运行，输出重定向到 ~/nohup.out
nohup command > my.log 2>&1 &            # 自定义日志路径
```

`nohup` 只是让进程忽略 `SIGHUP` 信号，进程仍然是当前 shell 的子进程，所以在 `jobs` 中还能看到。

### disown — 从 jobs 列表中分离

``` bash
disown %1        # 将任务1从 jobs 列表中移除，使其不再受当前 shell 管理
disown -h %1     # 保留在 jobs 列表，但使其忽略 SIGHUP
disown -a        # 将所有后台任务从 jobs 列表移除
```

将 `nohup` 与 `disown` 结合使用是在不依赖 tmux/screen 时最常见的"后台化"方式：

``` bash
command > my.log 2>&1 &
disown
```

### screen / tmux — 持久会话（推荐）

screen 和 tmux 提供"持久化终端会话"，断线后可以随时恢复，是长时间任务的最佳选择：

=== "tmux"

    ``` bash
    tmux new -s mysession       # 创建命名会话
    # 在会话内运行任务...
    # Ctrl+B, D 键 — 脱离（detach）会话，任务继续运行

    tmux ls                     # 列出所有会话
    tmux attach -t mysession    # 重新连接到会话
    tmux kill-session -t mysession  # 删除会话
    ```

=== "screen"

    ``` bash
    screen -S mysession         # 创建命名会话
    # 在会话内运行任务...
    # Ctrl+A, D 键 — 脱离会话

    screen -ls                  # 列出所有会话
    screen -r mysession         # 重新连接到会话
    ```

## 系统资源监控

### free — 内存概览

``` bash
free -h          # 人类可读格式（自动选择单位）
free -m          # 以 MB 为单位
```

!!! tip "内存「用光」是正常的"

    Linux 会将闲置的物理内存用于文件系统缓存（`buff/cache`）以提升性能。`available` 列才是实际可用内存——它包含了可以被释放的缓存。真正需要关注的是 **Swap 使用量**，Swap 用量过多（>20%）说明物理内存不足。

### vmstat — 系统整体状态

``` bash
vmstat 1 5       # 每1秒输出一次，共5次（内存/进程/IO/CPU 综合视图）
vmstat -s        # 摘要统计
```

### iostat — 磁盘 I/O 统计

``` bash
iostat -x 1      # 每秒输出一次扩展 I/O 统计（包含 util、await 等关键指标）
iostat -d 1 5    # 只看磁盘，每秒一次，共5次
```

`%util` 接近 100% 时说明该磁盘是性能瓶颈；`await`（平均等待时间）高则说明 I/O 延迟大。

### sar — 历史数据分析

``` bash
sar -u 1 5       # CPU 使用率，每秒1次，共5次（需要 sysstat 包）
sar -r 1 5       # 内存使用率历史
sar -d 1 5       # 磁盘 I/O 历史
sar -n DEV 1 3   # 网络接口流量
```

### lsof — 进程打开的文件

``` bash
lsof -p 1234     # 查看 PID=1234 的进程打开了哪些文件/网络连接
lsof -u alice    # 查看 alice 用户打开的所有文件
lsof -i :80      # 查找占用 80 端口的进程
lsof +D /var/log # 查找 /var/log 目录下被哪些进程打开的文件
```

`lsof` 是排查"端口被占用"、"文件被锁定"、"文件系统无法卸载"等问题的利器。

### ss / netstat — 网络连接状态

``` bash
ss -tulnp        # 列出所有监听端口及对应进程（推荐，替代 netstat）
ss -s            # 连接统计摘要
ss -anp | grep nginx  # 查看 nginx 的所有连接

netstat -tulnp   # 旧版本系统使用（功能与 ss 相同）
```

### uptime / lscpu

``` bash
uptime           # 系统运行时间 + 1/5/15分钟负载均值
lscpu            # CPU 型号、核心数、缓存等硬件信息
```

## 发行版差异

=== "Debian / Ubuntu"

    ``` bash
    # 安装 htop
    apt install htop

    # 安装 sysstat（提供 sar、iostat）
    apt install sysstat
    # 安装后需启用数据收集服务
    systemctl enable --now sysstat
    ```

=== "RHEL / Rocky / AlmaLinux"

    ``` bash
    # htop 在 EPEL 仓库中
    dnf install epel-release
    dnf install htop

    # sysstat 在 RHEL 8+ 中默认可用
    dnf install sysstat
    systemctl enable --now sysstat
    ```


## /proc/PID 进程目录

`/proc` 是 Linux 内核暴露进程状态的虚拟文件系统（Virtual Filesystem）。它不占磁盘空间，所有文件均由内核实时生成，读取时直接从内核数据结构中取值。每个运行中的进程都对应 `/proc/<PID>/` 目录，通过读取其中的伪文件，可以在无需任何调试工具的情况下深入了解进程内部状态。

### 重要子路径速查

| 路径 | 内容说明 |
|------|---------|
| `/proc/PID/status` | 进程状态、内存使用量、UID/GID 凭证信息 |
| `/proc/PID/cmdline` | 启动命令行参数，各参数以 `\0`（null 字节）分隔 |
| `/proc/PID/environ` | 进程启动时的环境变量，以 `\0` 分隔 |
| `/proc/PID/exe` | 指向可执行文件的符号链接 |
| `/proc/PID/cwd` | 指向当前工作目录的符号链接 |
| `/proc/PID/fd/` | 所有已打开的文件描述符（每个描述符为一个符号链接） |
| `/proc/PID/maps` | 内存映射布局（代码段/堆/栈/mmap 匿名区域等） |
| `/proc/PID/limits` | 当前资源限制（等同于 `ulimit -a` 的输出） |
| `/proc/PID/net/` | 进程所在网络命名空间的接口、连接等信息 |
| `/proc/PID/task/` | 进程内各线程（`TID`）的独立目录 |

### 常用命令示例

``` bash title="查看当前 Shell 进程状态"
# $$ 是 Bash 内置变量，表示当前 Shell 的 PID
cat /proc/$$/status

# 输出示例（关键字段说明）：
# Name:    bash              # 进程名
# Pid:     12345             # 进程 PID
# VmSize:  15320 kB          # 虚拟内存总大小（含未分配的预留空间）
# VmRSS:   4096 kB           # 常驻内存集（实际占用的物理内存）
# Threads: 1                 # 线程数
# Uid:     1000  1000  1000  1000   # RUID EUID SSUID FSUID
# Gid:     1000  1000  1000  1000   # RGID EGID SSGID FSGID
```

``` bash title="查看进程文件描述符"
# 列出当前 Shell 已打开的所有文件描述符
ls -la /proc/$$/fd

# 输出示例：
# lrwx------ 1 user user 64 ... 0 -> /dev/pts/0   # 标准输入
# lrwx------ 1 user user 64 ... 1 -> /dev/pts/0   # 标准输出
# lrwx------ 1 user user 64 ... 2 -> /dev/pts/0   # 标准错误
# lrwx------ 1 user user 64 ... 255 -> /dev/pts/0 # Bash 自用 fd
```

``` bash title="查看进程内存映射布局"
# 查看内存映射的前 20 行
cat /proc/$$/maps | head -20

# 输出格式：起始地址-结束地址 权限 偏移 设备 inode 路径
# 5600a1234000-5600a1278000 r-xp 0 08:01 123456 /usr/bin/bash  # 代码段（只读可执行）
# 5600a1479000-5600a1480000 rw-p 0 00:00 0                     # 堆（可读写）
# 7ffd12345000-7ffd12367000 rw-p 0 00:00 0      [stack]        # 栈
```

!!! note "VmRSS 与 VmSize 的区别"

    - `VmSize`：进程的**虚拟地址空间**总大小，包括代码、数据、共享库、mmap 区域等，数值通常远大于实际物理内存使用量。
    - `VmRSS`（Resident Set Size）：**实际占用的物理内存**，即当前在 RAM 中的页面总量，是衡量进程真实内存开销的关键指标。
    - `Threads`：进程当前的线程数。多线程程序（如 Java 应用）此值会远大于 1。

---

## 进程凭证

每个进程都持有一组用户/组标识符，称为**进程凭证（Process Credentials）**，内核依据这些凭证决定进程能访问哪些资源、能向哪些进程发送信号。

### 四类用户 ID

Linux 进程实际上维护着**四个**用户 ID，而非普通认知中的一个：

| 标识符 | 全称 | 说明 |
|--------|------|------|
| `RUID` | 实际用户 ID（Real UID） | 进程真正属于谁，继承自登录用户，通常不变 |
| `EUID` | 有效用户 ID（Effective UID） | 内核进行权限检查时**实际使用**的 ID |
| `SSUID` | 保存的 set-UID（Saved Set-UID） | 用于在特权/非特权状态间安全切换的"备份值" |
| `FSUID` | 文件系统用户 ID（Filesystem UID） | 仅用于文件系统操作的权限检查（Linux 特有） |

### 正常进程：RUID = EUID

对于普通进程，三个 ID 通常相等：

```
以 alice（UID=1000）身份运行 bash：
  RUID = 1000（alice）
  EUID = 1000（alice）
  SSUID = 1000
```

内核在检查文件访问权限时只看 `EUID`，所以在通常情况下，进程的权限就等于其登录用户的权限。

### setuid 程序：EUID 的变化

当执行设置了 `set-user-ID` 位的程序时，内核会将 `EUID` 切换为**可执行文件的属主**：

``` bash title="passwd 是典型的 setuid-root 程序"
# 查看 passwd 的权限位，注意 s 标志（表示 set-user-ID 位已设置）
ls -l /usr/bin/passwd
# -rwsr-xr-x 1 root root ... /usr/bin/passwd
#    ^--- s 表示 set-user-ID 位

# 普通用户 alice（UID=1000）执行 passwd 时：
# RUID = 1000（alice，进程仍属于 alice）
# EUID = 0   （root，获得修改 /etc/shadow 的权限）
```

### 为什么需要 SSUID

`SSUID` 解决了"临时放弃再恢复权限"的问题：

```
setuid 程序（属主 root）执行流程：
  1. 进程启动：RUID=1000, EUID=0, SSUID=0
  2. 执行敏感操作前，临时降权：EUID → 1000
     （此时 SSUID=0 保存着特权值）
  3. 需要再次提权：将 EUID 恢复为 SSUID 的值（0）
  4. 完全放弃特权：将 EUID 和 SSUID 都改为 RUID（1000）
     → 之后无法再恢复 root 权限
```

这种机制确保 setuid 程序在不需要特权时以非特权身份运行，减少攻击面。

### 查看进程凭证

``` bash title="读取 /proc/PID/status 中的凭证信息"
# 查看指定 PID 的凭证
grep -E "^(Uid|Gid):" /proc/$(pgrep passwd)/status

# 输出示例（4 列分别对应 RUID EUID SSUID FSUID）：
# Uid:    1000  0    0    0
# Gid:    1000  1000 1000 1000
```

!!! note "GID 同理"

    组 ID 也有四个对应值：`RGID`（实际组 ID）/ `EGID`（有效组 ID）/ `SSGID`（保存的 set-GID）/ `FSGID`（文件系统组 ID）。`/proc/PID/status` 中的 `Gid:` 行按相同顺序展示这四个值。

---

## Linux Capabilities

### 问题：root 的全有或全无困境

传统 UNIX 权限模型只有两种状态：普通用户（受限）和 root（无限制）。这导致了一个问题——一个只需要绑定 80 端口的 Web 服务器，必须以 root 身份运行，进而获得**所有** root 权限，包括读取任意文件、杀死任意进程等。

Linux Capabilities 机制将超级用户权限**细化为 40+ 个独立单元**，程序只需申请真正需要的能力，而无需完整的 root 权限。

### 常用能力速查

| Capability | 作用 |
|------------|------|
| `CAP_NET_BIND_SERVICE` | 绑定 1024 以下的特权端口（如 80/443） |
| `CAP_NET_ADMIN` | 配置网络接口、修改路由表、设置防火墙规则 |
| `CAP_SYS_ADMIN` | mount/umount、修改主机名、操作 namespace 等（权限极广，需谨慎） |
| `CAP_KILL` | 向任意进程发送信号（忽略 UID 限制） |
| `CAP_CHOWN` | 修改任意文件的属主（owner） |
| `CAP_DAC_OVERRIDE` | 绕过文件的读/写/执行权限检查（DAC = Discretionary Access Control） |
| `CAP_SYS_PTRACE` | 使用 `ptrace()` 调试任意进程 |

### 三个能力集

每个进程维护三组能力集：

- **Permitted**（许可集）：进程**可以拥有**的能力上限，是 Effective 的超集
- **Effective**（有效集）：内核进行权限检查时**实际使用**的能力集
- **Inheritable**（可继承集）：通过 `exec()` 执行新程序时可以传递给子进程的能力

### 查看进程能力

``` bash title="读取并解码进程能力"
# 查看进程能力（十六进制位掩码）
grep -E "^Cap" /proc/$$/status
# CapInh: 0000000000000000
# CapPrm: 0000000000000000
# CapEff: 0000000000000000

# 使用 capsh 解码十六进制掩码（需安装 libcap）
# 以 root 进程为例，CapEff 通常为 000001ffffffffff
capsh --decode=000001ffffffffff

# 也可以直接查看当前进程的能力（更直观）
capsh --print
```

### 设置与管理文件能力

``` bash title="为可执行文件设置能力（以 nginx 为例）"
# 为 nginx 赋予绑定特权端口的能力
# +ep 表示将此能力加入文件的 Permitted 和 Effective 集
setcap cap_net_bind_service+ep /usr/sbin/nginx

# 验证：查看文件的能力
getcap /usr/sbin/nginx
# /usr/sbin/nginx cap_net_bind_service=ep

# 移除文件的所有能力
setcap -r /usr/sbin/nginx

# 验证移除成功（无输出表示已清除）
getcap /usr/sbin/nginx
```

!!! note "实践意义"

    通过文件能力机制，可以让 nginx、Node.js 等服务以普通用户身份运行并监听 80/443 端口，避免以 root 身份运行带来的安全风险。这是容器安全和最小权限原则的重要实践手段。

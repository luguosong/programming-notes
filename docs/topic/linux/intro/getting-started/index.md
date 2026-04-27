---
title: 首次登录与线上求助
---

# 首次登录与线上求助

**本文你会学到**：

- SSH 远程登录的基本用法与密钥认证
- Linux 终端类型（物理/虚拟/伪终端）及切换方式
- 命令行高效操作技巧（Tab 补全、快捷键、历史命令）
- 三种获取帮助的方式：`--help`、`man`、`info`
- man page 区段编号含义与搜索技巧
- 安全关机的原因与正确关机方式

---

## 🔐 SSH 远程登录

### 为什么不直接坐在服务器前操作？

生产环境的服务器通常放在机房，管理员需要通过网络远程控制。`SSH`（Secure Shell）是 Linux 上最主流的远程登录协议，所有数据均加密传输，替代了明文传输的 `Telnet`。

**基本登录命令**：

``` bash
# 以 username 身份登录 hostname（默认端口 22）
ssh username@hostname

# 指定端口（服务器非默认端口时使用）
ssh -p 2222 username@hostname

# 登出（等同于 Ctrl+D 或 exit）
logout
```

登录成功后，提示符会变为：

``` bash
[username@hostname ~]$   # 普通用户（$ 结尾）
[root@hostname ~]#       # root 用户（# 结尾）
```

### 密钥认证：告别每次输密码

每次 SSH 都输密码既麻烦又有安全风险。密钥认证让你只需在本机保存私钥，服务器保存对应公钥，即可免密登录。

``` bash
# 在本机生成 RSA 密钥对（一路回车即可）
ssh-keygen -t rsa -b 4096

# 将公钥复制到远程服务器（之后就可以免密登录了）
ssh-copy-id username@hostname
```

生成的密钥默认位置：

- 私钥：`~/.ssh/id_rsa`（绝对不能泄露）
- 公钥：`~/.ssh/id_rsa.pub`（可以放心分发）

!!! warning "不要用 root 直接登录"

    `root` 权限无限大，一条错误命令可能造成不可恢复的损坏。日常操作应使用普通账号，需要管理权限时再用 `sudo` 或切换到 `root`。

---

## 🖥️ 终端类型

### 三种"终端"有什么不同？

| 类型 | 说明 | 典型场景 |
|------|------|---------|
| 物理终端（Physical console） | 服务器机房实体屏幕 + 键盘 | 机房直接操作 |
| 虚拟终端（Virtual console / tty） | 系统提供的 6 个独立命令行界面 | `Ctrl+Alt+F2~F6` 切换 |
| 伪终端（Pseudo terminal / pts） | SSH 或图形界面里的终端窗口 | `pts/0`、`pts/1` 等 |

### 虚拟终端切换

Linux 默认提供 `tty1~tty6` 六个终端，可以同时登录多个会话：

``` bash
# 切换快捷键（在图形界面下需同时按 Ctrl+Alt）
Ctrl+Alt+F1   # tty1（通常是图形界面 GNOME/KDE）
Ctrl+Alt+F2   # tty2（文字登录界面）
Ctrl+Alt+F3   # tty3 ~ F6 类似
```

!!! tip "tty 的实际含义"

    `tty` 来自 Teletype（电传打字机），是历史遗留名称。可以用 `tty` 命令查看当前使用的终端设备名。
    
    ``` bash
    $ tty
    /dev/pts/0   # SSH 远程登录时显示伪终端
    /dev/tty2    # 虚拟控制台时显示 tty 号
    ```

---

## ⌨️ 命令行基础技巧

### 命令语法结构

``` bash
command  [-options]  parameter1  parameter2 ...
# 命令     选项（可选）  参数1        参数2
```

几个重要规则：

- 短选项用单破折号：`-h`；长选项用双破折号：`--help`
- 选项与参数之间用**空格**分隔，空几格均视为一格
- Linux **区分大小写**：`date` 和 `Date` 是完全不同的两个东西
- 命令太长可用 `\` 续行（`\` 后面紧接换行符）

### Tab 自动补全

这是 Bash 最实用的功能，避免打字错误：

``` bash
# 补全命令名（输入前几个字母后按 Tab）
$ dat<Tab>       # 自动补全为 date
$ ca<Tab><Tab>   # 列出所有 ca 开头的命令

# 补全文件名
$ ls ~/.bas<Tab><Tab>
# 列出 .bash_history  .bash_logout  .bash_profile  .bashrc

# 补全选项（需安装 bash-completion）
$ date --<Tab><Tab>
# 列出所有可用的长选项
```

### 必记快捷键

| 快捷键 | 作用 |
|--------|------|
| `Tab` | 命令/文件名/选项补全 |
| `Ctrl+C` | 中断当前正在运行的程序 |
| `Ctrl+D` | 输入结束（EOF），也可替代 `exit` 退出终端 |
| `Ctrl+A` | 光标移到行首 |
| `Ctrl+E` | 光标移到行尾 |
| `Ctrl+L` | 清屏（等同于 `clear`） |
| `Shift+PageUp` | 向上翻看之前的输出 |
| `Shift+PageDown` | 向下翻 |

### 查看历史命令

``` bash
# 查看历史命令列表
history

# 执行上一条命令
!!

# 执行历史中第 N 条命令
!N

# 搜索历史（Ctrl+R 进入反向搜索模式）
Ctrl+R
```

---

## 📖 获取帮助的三种方式

Linux 命令数以千计，不可能全部记住。掌握查帮助的方法，比死记硬背效率高得多。

### 快速查阅：`--help`

几乎所有命令都支持 `--help` 选项，快速查看用法和选项说明：

``` bash
$ date --help
Usage: date [OPTION]... [+FORMAT]
  -d, --date=STRING    display time described by STRING, not 'now'
  -u, --utc            print Coordinated Universal Time (UTC)
      --help           显示此求助说明并离开
...
```

**适合场景**：你已经知道这个命令，只是忘了某个选项的具体写法。

### 完整文档：`man page`

`man`（manual）是 Linux 的"说明书"系统，内容比 `--help` 详细得多：

``` bash
$ man date      # 查看 date 命令的完整说明
$ man 5 passwd  # 查看第 5 区段的 passwd（配置文件格式）
$ man man       # 查看 man 自身的说明
```

#### man page 区段编号

| 区段 | 内容类型 | 典型示例 |
|------|---------|---------|
| **1** | 普通用户可用的命令 | `ls(1)`、`date(1)` |
| 2 | 内核系统调用 | `fork(2)`、`read(2)` |
| 3 | C 库函数 | `printf(3)` |
| 4 | 设备文件 | `null(4)` |
| **5** | 配置文件格式 | `passwd(5)`、`crontab(5)` |
| 6 | 游戏 | — |
| 7 | 协议与惯例 | `tcp(7)` |
| **8** | 系统管理员命令 | `shutdown(8)`、`iptables(8)` |

!!! tip "区段 1、5、8 最重要"

    遇到同名条目（如 `passwd` 既有命令又有配置文件），可以加区段编号区分：
    
    ``` bash
    $ man 1 passwd   # passwd 命令的使用说明
    $ man 5 passwd   # /etc/passwd 文件的格式说明
    ```

#### man page 的结构

| 段落 | 内容 |
|------|------|
| `NAME` | 命令名称及简短描述 |
| `SYNOPSIS` | 命令语法概要 |
| `DESCRIPTION` | 详细说明（最值得仔细读） |
| `OPTIONS` | 所有选项的详细解释 |
| `EXAMPLES` | 使用示例 |
| `FILES` | 相关文件路径 |
| `SEE ALSO` | 相关命令或文档 |

#### man page 内导航快捷键

| 按键 | 作用 |
|------|------|
| `Space` / `PageDown` | 向下翻一页 |
| `PageUp` | 向上翻一页 |
| `/keyword` | 向下搜索关键字 |
| `?keyword` | 向上搜索关键字 |
| `n` | 跳到下一个搜索结果 |
| `N` | 跳到上一个搜索结果 |
| `q` | 退出 man page |

#### 搜索相关命令

``` bash
# 精确查找（等同于 whatis）：只搜命令名称
$ man -f passwd
# passwd (1)  - update user's authentication tokens
# passwd (5)  - password file

# 关键字搜索（等同于 apropos）：搜索命令名和简短描述
$ man -k passwd
# 列出所有描述中含 "passwd" 的说明文档

# 更新 whatis 数据库（需要 root）
$ mandb
```

### 超链接文档：`info page`

`info` 将文档分割成多个相互链接的节点（Node），类似文字版网页，比 man page 有更好的导航体验：

``` bash
$ info date     # 查看 date 的 info 文档
$ info info     # 查看 info 自身的说明
```

info page 内的常用按键：

| 按键 | 作用 |
|------|------|
| `Tab` | 在各节点链接间移动 |
| `Enter` | 进入当前节点 |
| `n` / `p` | 下一个 / 上一个节点 |
| `u` | 返回上一层节点 |
| `s` 或 `/` | 在当前文档中搜索 |
| `q` | 退出 |

### 软件附带的说明文档：`/usr/share/doc`

很多软件安装后会在 `/usr/share/doc/` 下留下 README、配置示例、Change Log 等文件：

``` bash
# 查看 grub2 的说明文档目录
ls /usr/share/doc/grub2-tools-*/

# 对于不知道如何配置的软件，先在这里找找
ls /usr/share/doc/ | grep nginx
```

### 三种求助方式对比

| 方式 | 适用场景 | 信息量 |
|------|---------|--------|
| `--help` | 已知命令，忘记某个选项 | 少，快速 |
| `man` | 不熟悉的命令或文件格式 | 全面，权威 |
| `info` | 需要在章节间跳转浏览 | 全面，易读 |
| `/usr/share/doc` | 了解软件整体配置或架构 | 软件级别 |

---

## 🔌 安全关机与重启

### 为什么不能直接断电？

Linux 是多用户多任务系统：后台随时可能有其他用户的任务在运行；数据采用**写缓存**机制，内存中的数据尚未写回磁盘时断电，会导致文件系统损坏。

正确关机应该：

1. 通知在线用户
2. 停止所有服务
3. 将内存中的数据刷写到磁盘
4. 然后才切断电源

### 手动将缓存写入磁盘：`sync`

``` bash
# 强制将内存中的未写数据刷入磁盘
sync

# 关机前多执行几次更安心
sync; sync; sync
```

!!! note "现代关机命令已内置 sync"

    `shutdown`、`reboot`、`poweroff` 等命令在执行前都会自动调用 `sync`，但手动多执行几次没有坏处。

### 推荐关机方式：`shutdown`

`shutdown` 支持定时关机和广播通知，是最推荐的关机命令：

``` bash
# 立即关机
shutdown -h now

# 10 分钟后关机，并向所有登录用户广播消息
shutdown -h 10 'System will shutdown in 10 minutes'

# 在指定时间关机（今天 20:30）
shutdown -h 20:30

# 立即重启
shutdown -r now

# 30 分钟后重启
shutdown -r +30 'Rebooting for kernel upgrade'

# 取消已经计划的关机
shutdown -c

# 仅发送警告消息，不真正关机（吓一吓用户）
shutdown -k now 'Maintenance in progress'
```

### systemd 关机命令

现代 Linux 发行版（使用 systemd）推荐用 `systemctl` 管理系统状态：

=== "systemctl（推荐）"

    ``` bash
    systemctl poweroff   # 关机
    systemctl reboot     # 重启
    systemctl halt       # 停止系统（屏幕保留信息，电源状态由硬件决定）
    systemctl suspend    # 进入休眠模式
    ```

=== "传统命令"

    ``` bash
    poweroff    # 关机
    reboot      # 重启
    halt        # 停止（等同于 systemctl halt）
    ```

!!! tip "reboot 前的好习惯"

    ``` bash
    # 先 sync 再重启，确保所有数据写入磁盘
    sync; sync; sync; reboot
    ```

### 关机命令权限

| 操作场景 | 有权限的用户 |
|---------|------------|
| 图形界面（本机） | 所有登录用户 |
| 虚拟终端 tty1~tty6（本机） | 所有登录用户（视发行版而定） |
| SSH 远程登录 | 仅 `root` |

## 常用系统信息命令

Linux 管理员或运维工程师经常需要快速查看系统状态、用户活动、日期时间等。这些工具虽然功能简单，但组合使用能快速诊断问题。

### date — 显示或设置系统日期时间

``` bash
date                # 显示当前日期时间
# Thu Feb 13 10:30:45 UTC 2025

date "+%Y-%m-%d %H:%M:%S"    # 自定义格式：2025-02-13 10:30:45

date -d "3 days ago"         # 计算相对日期
date -d "next monday"        # 显示下一个星期一的日期

date -d "2025-01-01" +%s     # 转换为 Unix 时间戳（秒数）
# 1735689600
```

!!! warning "修改系统时间需 root 权限"

    ``` bash
    # 仅 root 能修改
    sudo date -s "2025-02-13 10:30:00"
    
    # 现代系统推荐用 timedatectl（systemd）
    timedatectl set-time "2025-02-13 10:30:00"
    ```

### cal — 显示日历

``` bash
cal              # 显示当月日历
cal 2025         # 显示全年 2025
cal 2 2025       # 显示 2025 年 2 月
cal -3           # 显示前月、本月、下月（共 3 个月）
```

### bc — 命令行计算器

适合在脚本中进行数学运算或单位转换。

``` bash
bc               # 进入交互式计算器（输入 quit 或 Ctrl+D 退出）
# 支持 +、-、*、/、^ 等运算，以及 if/for/while 等编程结构

# 管道方式（脚本中最常用）
echo "2.5 * 3.14" | bc        # 7.85

echo "scale=4; 22/7" | bc     # 3.1428（scale 设置小数位数）

# 进制转换（obase=输出进制，ibase=输入进制）
echo "obase=16; 255" | bc     # FF（十进制 255 转十六进制）
echo "ibase=16; obase=2; FF" | bc    # 11111111（十六进制 FF 转二进制）
```

### uptime — 查看系统运行时长和负载

``` bash
uptime
# 10:30:45 up 42 days, 3:15, 2 users, load average: 0.45, 0.52, 0.48
```

输出字段含义：

- `10:30:45` — 当前时间
- `up 42 days, 3:15` — 运行时长（42 天 3 小时 15 分）
- `2 users` — 当前登录用户数
- `load average: 0.45, 0.52, 0.48` — 1/5/15 分钟内的平均 CPU 负载

!!! tip "理解负载值"

    - 单核 CPU 负载 `1.0` = 满载
    - 四核 CPU 负载 `4.0` = 满载
    - 如果 `load average` 超过 CPU 核心数，说明有进程在等待 CPU（系统过载）
    
    查看 CPU 核心数：`nproc` 或 `grep ^processor /proc/cpuinfo | wc -l`

### uname — 查看系统信息

``` bash
uname -a         # 显示所有信息
# Linux hostname 5.15.0-91-generic #101-Ubuntu SMP Tue Nov 14 19:20:26 UTC 2023 x86_64 GNU/Linux

uname -r         # 仅显示内核版本（脚本中常用）
# 5.15.0-91-generic

uname -m         # 硬件架构（x86_64 / aarch64 / armv7l）

uname -s         # 系统名（Linux / Darwin / FreeBSD）
```

### hostname — 查看或修改主机名

``` bash
hostname         # 显示当前主机名

hostname newname        # 临时修改（重启后恢复）
sudo hostnamectl set-hostname newname   # 永久修改（systemd）
```

## 用户状态查询

### who — 查看谁正在登录

``` bash
who
# root     pts/0        2025-02-13 08:15 (192.168.1.100)
# alice    tty1         2025-02-13 09:30
# bob      pts/1        2025-02-13 10:00 (10.0.0.50)
```

输出字段：

- **用户名** — 登录的用户
- **终端名** — 接入方式（`tty1-6` 本地虚拟终端，`pts/0` SSH 伪终端）
- **登录时间**
- **来源** — IP 地址（本地登录无此项）

``` bash
who am i         # 或 who -m，只显示自己的登录信息（SSH 跳板时确认身份）
```

### w — 更详细的用户活动状态

`w` 是 `who` 的增强版，显示用户当前在做什么。

``` bash
w
# 10:30:50 up 42 days,  3:15,  2 users,  load average: 0.45, 0.52, 0.48
# USER     TTY      FROM             LOGIN@   IDLE   JCPU   PCPU WHAT
# root     pts/0    192.168.1.100    08:15    5:40   1:20   0:05 vim /etc/hosts
# alice    tty1     -                09:30    1:15   0:30   0:12 bash
```

关键字段：

- **TTY** — 终端
- **FROM** — 源 IP
- **LOGIN@** — 登录时间
- **IDLE** — 空闲时长（如 `5:40` 表示 5 分 40 秒没有按键）
- **JCPU** — 该终端所有进程的总 CPU 时间
- **PCPU** — 当前进程的 CPU 时间
- **WHAT** — 当前执行的命令

### last — 查看登录历史

``` bash
last              # 显示所有用户的登录/登出历史（来自 `/var/log/wtmp`）

last username     # 仅显示指定用户的历史

last reboot       # 查看系统重启历史

lastb             # 查看登录失败记录（需 root，来自 `/var/log/btmp`）
```

### id — 显示用户身份信息

``` bash
id                # 显示当前用户的 uid/gid 及所属组
# uid=1000(alice) gid=1000(alice) groups=1000(alice),4(adm),27(sudo)

id username       # 显示指定用户的信息

id -g             # 仅显示主组 gid
id -G             # 显示所有组 gid
id -n             # 显示名称而非数字
```

!!! tip "何时用 id"

    - 检查当前权限（是否 root）
    - 确认用户属于哪些组（判断是否有某权限）
    - 脚本中获取 uid/gid（如在容器中创建同名用户）

### whoami — 显示当前用户名

``` bash
whoami            # 输出当前用户名（等同 `id -un`，但更简洁）
```



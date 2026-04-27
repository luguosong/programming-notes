---
title: systemd 服务管理
---

# systemd 服务管理

**本文你会学到**：

- SysVinit 的局限与 systemd 的改进
- Unit 的八种类型及各自的用途
- Unit 文件的三级目录结构与优先级
- `.service` 文件的常见字段含义
- 用 `systemctl` 进行服务的启停、启用、重启
- 依赖关系声明（Requires、Wants、After、Before）
- Target 的概念与运行级别的对应
- 创建自定义服务单元的完整步骤
- 用 `systemd timer` 替代 crontab
- 服务隔离与资源限制（cgroup）的基础

## 为什么需要 systemd

### SysVinit 的局限

在 systemd 出现之前，Linux 使用的是 **SysVinit**（System V init）机制。它有几个根本性的缺陷：

- **顺序启动**：所有服务按照 `/etc/rc.d/rcN.d/SXXdaemon` 链接文件的编号依次执行，互不相关的服务也要等上一个完成
- **依赖关系手动维护**：你需要自己保证编号顺序正确，一旦写错就可能出现超长的启动 timeout
- **脚本碎片化**：每个服务自己维护一个 bash 脚本，格式五花八门，难以统一管理

### systemd 带来的改变

systemd 从根本上解决了上述问题：

- **并行启动**：没有依赖关系的服务同时启动，显著缩短开机时间
- **依赖自动解析**：在 unit 文件里声明 `Requires=` / `Wants=`，systemd 自动按正确顺序唤醒
- **统一管理界面**：服务、挂载点、定时任务、设备——全部通过 `systemctl` 一个命令搞定
- **日志集中化**：所有服务输出统一收集到 `journald`，再也不用四处找日志文件

## Unit 基础概念

### Unit 类型

systemd 把所有被管理的对象称为 **unit（服务单位）**，并按功能分为不同类型，通过文件扩展名区分：

| 扩展名 | 类型 | 说明 |
|--------|------|------|
| `.service` | 系统服务 | 最常见的类型，网络服务、本机服务都是它 |
| `.socket` | 套接字激活 | 监控 socket 文件，有请求时才按需启动对应服务 |
| `.target` | 服务组 | 多个 unit 的集合，类似 SysVinit 的 runlevel |
| `.mount` / `.automount` | 挂载点 | 管理文件系统挂载，包括 NFS 自动挂载 |
| `.timer` | 定时任务 | cron 的现代替代，精度可到毫秒 |
| `.path` | 路径监控 | 监控特定目录/文件，变化时触发关联服务 |
| `.slice` | 资源控制组 | 通过 cgroup 限制一组进程的资源用量 |
| `.device` | 设备 | 内核识别到设备时触发 |

### Unit 文件位置

unit 文件分散在三个目录，**优先级从低到高**：

| 目录 | 优先级 | 说明 |
|------|:------:|------|
| `/usr/lib/systemd/system/` | 最低 | 软件包安装时提供，**不要手动修改** |
| `/run/systemd/system/` | 中 | 运行时临时生成，重启后消失 |
| `/etc/systemd/system/` | 最高 | 管理员自定义，覆盖前两处的同名文件 |

!!! tip "修改服务配置的正确做法"

    不要直接改 `/usr/lib/systemd/system/` 下的文件——软件包更新会覆盖你的修改。应在 `/etc/systemd/system/` 下创建同名文件（完全覆盖）或使用 `systemctl edit` 创建 drop-in 补丁文件。

## systemctl 常用操作

### 服务控制

``` bash
# 启动 / 停止 / 重启服务
systemctl start nginx
systemctl stop nginx
systemctl restart nginx

# 重载配置（不中断现有连接，nginx/sshd 等支持）
systemctl reload nginx

# 查看服务详细状态（含最近日志）
systemctl status nginx
```

### 开机自启管理

``` bash
# 设置开机自启 / 取消自启
systemctl enable nginx
systemctl disable nginx

# 立即启动 + 同时设置开机自启（最常用组合）
systemctl enable --now nginx

# 查询服务状态
systemctl is-enabled nginx    # enabled / disabled / static
systemctl is-active nginx     # active / inactive
```

### 查看 Unit 列表

``` bash
# 当前运行中的服务
systemctl list-units --type=service

# 所有服务（含未激活的）
systemctl list-units --type=service --all

# 列出服务文件及其启用状态
systemctl list-unit-files --type=service
```

### 依赖关系分析

``` bash
# 查看 nginx 依赖哪些 unit
systemctl list-dependencies nginx

# 反查：哪些 unit 依赖 nginx
systemctl list-dependencies --reverse nginx
```

### 重载配置

``` bash
# 修改 unit 文件后必须执行，否则 systemd 不会感知变化
systemctl daemon-reload
```

## Unit 文件结构

一个典型的 `.service` 文件分为三段：`[Unit]`、`[Service]`、`[Install]`。

### \[Unit\] 段

描述服务的元信息和依赖关系：

``` ini title="[Unit] 段示例"
[Unit]
Description=My Application Service
Documentation=https://example.com/docs

# 启动顺序（不是强制依赖，仅规定先后）
After=network.target

# 硬依赖：postgresql 未运行则本服务无法启动
Requires=postgresql.service

# 软依赖：redis 失败不影响本服务启动
Wants=redis.service

# 冲突：与指定服务不能同时运行
Conflicts=old-app.service
```

### \[Service\] 段

控制服务的实际运行方式：

``` ini title="[Service] 段示例"
[Service]
Type=simple            # 进程模型，见下方说明

# 运行身份
User=www-data
Group=www-data

WorkingDirectory=/opt/myapp
ExecStart=/opt/myapp/bin/server
ExecStop=/opt/myapp/bin/server stop
ExecReload=/bin/kill -HUP $MAINPID

# 环境变量
Environment=NODE_ENV=production
EnvironmentFile=/etc/myapp/env

# 日志输出到 journald
StandardOutput=journal
StandardError=journal

# 安全加固
NoNewPrivileges=true
ProtectSystem=strict
PrivateTmp=true
```

**`Type` 参数说明**：

| Type | 说明 | 适用场景 |
|------|------|---------|
| `simple` | ExecStart 进程即主进程（默认） | 大多数现代服务 |
| `forking` | 父进程 fork 后退出，子进程变主进程 | 传统 Unix 守护进程（如 nginx、sshd） |
| `oneshot` | 执行一次后退出，不常驻内存 | 开机初始化脚本、一次性任务 |
| `notify` | 进程通过 `sd_notify()` 主动通知就绪 | systemd-aware 的服务 |
| `dbus` | 通过 D-Bus 获取名称后视为就绪 | 图形界面相关服务 |
| `idle` | 等所有其他任务完成后才启动 | 开机最后阶段执行的服务 |

### \[Install\] 段

声明此 unit 挂载到哪个 target 下（`systemctl enable` 时生效）：

``` ini title="[Install] 段示例"
[Install]
WantedBy=multi-user.target    # 挂入多用户命令行环境（相当于 runlevel 3）
```

### Restart 重启策略

``` ini title="Restart 配置"
# 推荐：非零退出或被信号意外杀死时重启
Restart=on-failure

# 无论何种原因退出都重启（谨慎使用）
Restart=always

# 不重启（默认）
Restart=no

# 重启前等待时间（默认 100ms）
RestartSec=5

# 限速：60 秒窗口内最多重启 3 次，超出后标记为 failed
StartLimitIntervalSec=60
StartLimitBurst=3
```

## Target 与运行级别

`target` 是多个 unit 的集合，充当 SysVinit 中 runlevel 的角色：

| SysVinit runlevel | systemd Target | 说明 |
|:-----------------:|----------------|------|
| 0 | `poweroff.target` | 关机 |
| 1 | `rescue.target` | 单用户/救援模式 |
| 3 | `multi-user.target` | 多用户命令行（最常用） |
| 5 | `graphical.target` | 图形界面 |
| 6 | `reboot.target` | 重启 |

``` bash
# 查看当前默认 target
systemctl get-default

# 修改默认 target（持久，重启生效）
systemctl set-default multi-user.target

# 立即切换 target（不持久，重启恢复）
systemctl isolate multi-user.target

# 快捷操作
systemctl poweroff      # 关机
systemctl reboot        # 重启
systemctl suspend       # 挂起（状态保存到内存）
systemctl hibernate     # 休眠（状态保存到磁盘）
```

## journald 日志管理

systemd 内置的 **journald** 负责收集所有服务的日志，统一通过 `journalctl` 查询，彻底告别四处找 `/var/log/*.log` 的烦恼。

### 查看服务日志

``` bash
# nginx 的全部历史日志
journalctl -u nginx

# 实时跟踪（类似 tail -f）
journalctl -u nginx -f

# 最后 50 行
journalctl -u nginx -n 50

# 过去 1 小时内的日志
journalctl -u nginx --since "1 hour ago"
```

### 系统范围日志

``` bash
# 本次启动的全部日志
journalctl -b

# 上次启动的日志
journalctl -b -1

# 仅显示错误及以上级别（err/crit/alert/emerg）
journalctl -p err

# 按时间范围过滤
journalctl --since "2024-01-01 00:00" --until "2024-01-01 12:00"
```

### 日志清理

``` bash
# 查看 journal 占用磁盘空间
journalctl --disk-usage

# 清理到 500M 以内
journalctl --vacuum-size=500M

# 删除 30 天前的日志
journalctl --vacuum-time=30d
```

### 持久化日志

默认情况下，journald 将日志保存在内存中，重启后丢失。要让日志持久存储：

``` bash
# 方法一：创建目录（journald 检测到后自动持久化）
mkdir -p /var/log/journal
systemctl restart systemd-journald

# 方法二：修改配置文件
# /etc/systemd/journald.conf 中设置：
# Storage=persistent
```

## 自定义 Service 实战

以创建一个 Node.js 应用服务为例：

``` ini title="/etc/systemd/system/myapp.service"
[Unit]
Description=My Node.js Application
After=network.target

[Service]
Type=simple
User=nodejs
WorkingDirectory=/opt/myapp
ExecStart=/usr/bin/node /opt/myapp/app.js
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

创建好后依次执行：

``` bash
# 让 systemd 感知新文件
systemctl daemon-reload

# 立即启动并设置开机自启
systemctl enable --now myapp

# 实时查看日志验证服务是否正常
journalctl -u myapp -f
```

## systemd Timer — cron 的现代替代

相比 cron，systemd timer 的优势在于：日志统一收集到 journald、可以与 cgroup 结合限制资源、精度可到毫秒级。

### Timer 文件结构

timer 与 service 需要**同名配对**（如 `backup.timer` + `backup.service`）：

``` ini title="/etc/systemd/system/backup.timer（按日历触发）"
[Unit]
Description=Daily Backup Timer

[Timer]
# 每天凌晨 02:30 执行
OnCalendar=*-*-* 02:30:00

# 错过的任务（如机器关机）在下次启动后补跑
Persistent=true

[Install]
WantedBy=timers.target
```

``` ini title="/etc/systemd/system/cleanup.timer（按间隔触发）"
[Unit]
Description=Cleanup Timer

[Timer]
# 开机 2 小时后执行一次
OnBootSec=2h

# 此后每隔 2 天执行一次
OnUnitActiveSec=2d

[Install]
WantedBy=timers.target
```

**`[Timer]` 常用参数**：

| 参数 | 说明 |
|------|------|
| `OnCalendar=daily` | 每天零点 |
| `OnCalendar=weekly` | 每周一零点 |
| `OnCalendar=Mon *-*-* 03:00:00` | 每周一 03:00 |
| `OnBootSec=5min` | 开机 5 分钟后执行 |
| `OnUnitActiveSec=1h` | 上次执行后 1 小时再次执行 |
| `Persistent=true` | 错过时补跑（配合 `OnCalendar` 使用） |

``` bash
# 启用并启动 timer（不是 service！）
systemctl enable --now backup.timer

# 查看所有定时器及下次触发时间
systemctl list-timers --all
```

## 发行版差异

=== "Debian / Ubuntu"

    - systemd 从 **Ubuntu 15.04 / Debian 8（Jessie）** 起成为默认 init
    - 与 **AppArmor** 集成：`systemctl status apparmor`
    - 日志持久化：`/var/log/journal/` 目录需手动创建才会持久存储
    - 网络管理：默认使用 `systemd-networkd` 或 NetworkManager，取决于版本

=== "RHEL / CentOS / Fedora"

    - **RHEL 7** 起以 systemd 替换 SysVinit，是业界最早大规模推广的发行版之一
    - 与 **SELinux** 深度集成：unit 文件中可通过 `SELinuxContext=` 设置安全域
    - RHEL 9+ 新增 **`systemd-oomd`** 守护进程，主动应对 OOM 而不依赖内核 OOM killer
    - 使用 `dnf` 安装软件后，服务 unit 文件位于 `/usr/lib/systemd/system/`

=== "Arch Linux"

    - 最早在主流发行版中全面切换到 systemd（2012 年）
    - Arch Wiki 的 [systemd 页面](https://wiki.archlinux.org/title/Systemd) 是全网最详尽的参考之一
    - `systemd-boot` 常作为 GRUB 的替代品使用


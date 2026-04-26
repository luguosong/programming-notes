---
title: 计划任务
---

# 计划任务

**本文你会学到**：

- `at` 命令的用途、时间格式、队列管理
- `at.allow`、`at.deny` 的访问控制逻辑
- crontab 的五个时间字段与通配符的组合
- crontab 常见时间表达式与调试技巧
- crontab 的执行环境（PATH、工作目录、邮件通知）
- 如何在 crontab 中正确使用绝对路径与环境变量
- crontab 日志查询与故障排查
- systemd timer 与 crontab 的对比
- 创建与管理 systemd timer 单元文件

## at —— 一次性任务

### 为什么需要 at？

有时候你需要在特定时间做一件事：明天凌晨停机维护、下周一发送通知邮件……这种「只做一次」的场景不适合 crontab。`at` 命令正是为此而生——把任务投入队列，到时间自动执行一次，执行完就结束。

### 安装与启动

`at` 依赖 `atd` 后台服务。部分发行版默认未安装：

=== "Debian/Ubuntu"

    ``` bash
    apt install at
    systemctl enable --now atd
    ```

=== "RHEL/CentOS"

    ``` bash
    dnf install at
    systemctl enable --now atd
    ```

### 创建与管理任务

``` bash
# 创建任务：执行 at 后进入交互提示符，输入命令，Ctrl+D 结束
at now + 5 minutes
at 10:30 tomorrow
at 2:00 PM + 3 days
at 23:00 2025-08-04

# 任务管理
atq              # 查看任务队列（等同于 at -l）
atrm 3           # 删除 ID=3 的任务（等同于 at -d 3）
at -c 3          # 查看 ID=3 任务的实际内容
```

!!! tip "使用绝对路径"

    at 执行时 PATH 很短，且工作目录是**提交任务时所在的目录**。命令中应始终使用绝对路径，例如 `/usr/bin/backup.sh` 而非 `backup.sh`。

### batch —— 低负载时才执行

`batch` 是 `at` 的变体。它会等待系统 CPU 负载低于 0.8 时才执行任务，适合不紧急但资源密集的工作：

``` bash
batch
at> /usr/bin/updatedb
at> <EOT>     # Ctrl+D 结束输入
```

用 `atq` 可看到 `batch` 创建的任务（队列类型标记为 `b`），同样用 `atrm` 删除。

### 访问控制

两个文件控制哪些用户可以使用 `at`：

- `/etc/at.allow`：**白名单**。文件存在时，只有其中列出的用户才能使用 `at`
- `/etc/at.deny`：**黑名单**。`at.allow` 不存在时生效，文件中的用户不能使用 `at`
- 两个文件都不存在：只有 `root` 可用

大多数发行版默认保留一个空的 `/etc/at.deny`，即允许所有用户使用。

## crontab —— 周期性任务

### 理解时间字段格式

crontab 的核心是五个时间字段：

```
分钟  小时  日期  月份  星期  命令
0-59  0-23  1-31  1-12  0-7   command
```

星期字段中，`0` 和 `7` 都代表星期日。四个特殊字符扩展了表达能力：

| 字符 | 含义 | 示例 |
|------|------|------|
| `*` | 任意值 | `* * * * *` 每分钟 |
| `,` | 多个值 | `0 3,6 * * *` 3:00 和 6:00 |
| `-` | 范围 | `0 8-18 * * *` 8:00 到 18:00 每小时整点 |
| `/` | 间隔 | `*/5 * * * *` 每 5 分钟 |

!!! warning "日期与星期不要同时指定"

    在 crontab 中同时指定「日期」和「星期」字段（非 `*`）时，系统会将两者**分别**作为 OR 条件执行，而非 AND。如果你想要「9 月 11 日且是周五」，crontab 做不到——请改用脚本内部逻辑判断。

### 常用时间写法示例

``` bash
# 每分钟执行
* * * * * /path/to/script

# 每天凌晨 2:30
30 2 * * * /usr/bin/backup.sh

# 每周一 9:00
0 9 * * 1 /usr/bin/weekly-report.sh

# 每月 1 日 0:00
0 0 1 * * /usr/bin/monthly.sh

# 每 5 分钟
*/5 * * * * /usr/bin/check.sh

# 工作日（周一至周五）8:00-18:00 每小时执行
0 8-18 * * 1-5 /usr/bin/hourly.sh

# 多个时刻：3:00 和 6:00
0 3,6 * * * /usr/bin/twice-daily.sh
```

### 用户 crontab

每个用户拥有独立的 crontab，存储在 `/var/spool/cron/crontabs/用户名`。

``` bash
crontab -e          # 编辑当前用户的 crontab
crontab -l          # 查看当前用户的 crontab
crontab -r          # 删除当前用户的全部 crontab（⚠️ 不可撤销！）
crontab -u alice -l  # root 查看指定用户的 crontab
```

!!! warning "crontab -r 的陷阱"

    `-r` 会**立即删除所有任务**，没有确认提示。若只想删除某一条，请用 `crontab -e` 进入编辑器手动删除对应行。

### 系统级 crontab

系统管理任务不应写入用户 crontab，应放在系统级配置文件：

``` ini title="/etc/crontab 格式（比用户 crontab 多了 USER 字段）"
# 分 时 日 月 周  用户  命令
0 2 * * *  root  /usr/bin/backup.sh
```

系统级目录（直接放入可执行脚本即可，无需指定时间）：

- `/etc/cron.hourly/` — 每小时执行（约 01 分后随机 5 分钟内）
- `/etc/cron.daily/` — 每天执行（由 anacron 管理）
- `/etc/cron.weekly/` — 每周执行
- `/etc/cron.monthly/` — 每月执行

`/etc/cron.d/` 目录用于第三方软件放置自己的 crontab 配置文件，格式与 `/etc/crontab` 相同（含 USER 字段）。

### 常见问题与最佳实践

**环境变量问题**：cron 运行时的 `PATH` 非常短，远比交互式 shell 短。始终使用绝对路径：

``` bash
# ❌ 可能找不到命令
*/5 * * * * backup.sh

# ✅ 使用绝对路径
*/5 * * * * /usr/local/bin/backup.sh
```

**输出导致邮件泛滥**：cron 任务的标准输出默认会发送邮件给执行者。重定向到文件或丢弃：

``` bash
# 丢弃所有输出
*/5 * * * * /usr/bin/check.sh > /dev/null 2>&1

# 追加到日志文件
0 2 * * * /usr/bin/backup.sh >> /var/log/backup.log 2>&1
```

**分散高负载任务**：多个重量级任务不要都设置在整点，错开执行时间：

``` bash
# 错开 5 分钟执行的四个任务
1,6,11,16,21,26,31,36,41,46,51,56 * * * * root /usr/bin/task1
2,7,12,17,22,27,32,37,42,47,52,57 * * * * root /usr/bin/task2
```

**查看 cron 日志**：

=== "Debian/Ubuntu"

    ``` bash
    grep CRON /var/log/syslog
    ```

=== "RHEL/CentOS"

    ``` bash
    grep CRON /var/log/cron
    ```

### 访问控制

与 at 相同，`/etc/cron.allow`（白名单）和 `/etc/cron.deny`（黑名单）控制用户是否可使用 `crontab`。

## anacron —— 补跑机制

### 解决什么问题？

cron 是「定时」执行——错过了就错过了。如果你的机器周末关机，那么本该在周日凌晨跑的备份任务就永远缺失了。

anacron 的定位是「**定期**」执行：它会检查上次执行时间戳，如果超过了设定的天数还没执行，则在开机后补跑。适合非 24 小时运转的服务器。

### 工作原理

anacron 本身不是常驻服务，而是由 crond 每小时调用一次（通过 `/etc/cron.hourly/0anacron`）。它读取 `/var/spool/anacron/` 中的时间戳文件，判断哪些任务超期，然后延迟一段时间后执行。

### 配置文件

``` ini title="/etc/anacrontab"
# 格式：间隔天数  延迟分钟  任务ID        命令
1               5         cron.daily    run-parts /etc/cron.daily
7               25        cron.weekly   run-parts /etc/cron.weekly
@monthly        45        cron.monthly  run-parts /etc/cron.monthly
```

四个字段的含义：

- **间隔天数**：与上次执行时间相差超过这个天数，才触发执行
- **延迟分钟**：触发后等待多少分钟再执行（避免开机初期资源竞争）
- **任务 ID**：日志记录用的标识，与 `/var/spool/anacron/` 中的文件名对应
- **命令**：要执行的命令

``` bash
anacron -f    # 强制执行所有任务（忽略时间戳）
anacron -n    # 立即执行（不延迟）
anacron -u    # 仅更新时间戳，不执行任何任务
```

### crond + anacron 协作流程

``` mermaid
graph TD
    A[crond 每分钟检查] --> B[/etc/crontab + /etc/cron.d/ + /var/spool/cron/]
    A --> C[每小时触发 /etc/cron.hourly/]
    C --> D[0anacron 脚本]
    D --> E[anacron 检查时间戳]
    E --> F{超过间隔天数？}
    F -- 是 --> G[延迟指定分钟后执行]
    G --> H[/etc/cron.daily/ 等目录中的脚本]
    F -- 否 --> I[跳过，等待下次检查]
```

## systemd Timer —— 现代替代方案

### 为什么要了解 systemd timer？

cron 已经服务了几十年，但它有几个痛点：日志难以追溯、错过的任务不会补跑（需要额外配置 anacron）、任务失败没有重启机制。systemd timer 解决了这些问题，在 RHEL 8+ 等现代发行版中被推荐优先使用。

### 创建定时任务

systemd timer 由两个文件组成：一个 `.service`（定义做什么）和一个 `.timer`（定义何时做）。

``` ini title="/etc/systemd/system/backup.service"
[Unit]
Description=Daily Backup

[Service]
Type=oneshot
ExecStart=/usr/bin/backup.sh
```

``` ini title="/etc/systemd/system/backup.timer"
[Unit]
Description=Run backup daily at 2:30

[Timer]
# 每天 02:30 执行
OnCalendar=*-*-* 02:30:00
# 开机时若错过，立即补跑
Persistent=true

[Install]
WantedBy=timers.target
```

``` bash
systemctl daemon-reload
systemctl enable --now backup.timer
systemctl list-timers            # 查看所有定时器及下次执行时间
journalctl -u backup.service     # 查看任务执行日志
```

### OnCalendar 时间语法

| 表达式 | 含义 |
|--------|------|
| `daily` | 每天 00:00:00 |
| `weekly` | 每周一 00:00:00 |
| `monthly` | 每月 1 日 00:00:00 |
| `hourly` | 每小时 :00:00 |
| `*-*-* 02:30:00` | 每天 02:30 |
| `Mon *-*-* 09:00:00` | 每周一 09:00 |
| `*-*-1,15 00:00:00` | 每月 1 日和 15 日 |
| `*-*-* *:0/15:00` | 每 15 分钟 |

## cron vs systemd Timer

| 特性 | cron | systemd Timer |
|------|------|--------------|
| 日志 | syslog（有限） | journald（完整） |
| 错过任务补跑 | 需搭配 anacron | `Persistent=true` |
| 依赖管理 | 无 | `After=` / `Requires=` |
| 精度 | 分钟级 | 微秒级 |
| 配置方式 | 单行语法 | 两个 unit 文件 |
| 错误处理 | 邮件通知 | 重启策略（`Restart=on-failure`） |
| 学习成本 | 低 | 中 |

**建议**：简单的周期任务用 cron，需要日志追踪、补跑保障、任务依赖时选 systemd timer。

## 发行版差异

=== "Debian/Ubuntu"

    - cron 守护进程：`cron`（通常预装，`apt install cron`）
    - 服务名：`cron.service`
    - 日志位置：`/var/log/syslog`
    - `at` 需要单独安装：`apt install at`
    - 查看 cron 日志：`grep CRON /var/log/syslog`

=== "RHEL/CentOS"

    - cron 守护进程：`crond`（`dnf install cronie`）
    - 服务名：`crond.service`
    - 日志位置：`/var/log/cron`
    - RHEL 8+ 推荐优先使用 systemd timer 替代 cron
    - 查看 cron 日志：`grep CRON /var/log/cron`


---
title: sudo 与 PAM
---

# sudo 与 PAM

**本文你会学到**：

- `su` 与 `sudo` 的本质区别
- sudo 的工作流程与密码缓存机制
- `/etc/sudoers` 配置文件的语法与最佳实践
- 用 `visudo` 安全编辑 sudoers
- 针对单个命令、命令组进行精细授权
- 环境变量的继承与隔离（`env_keep`、`env_reset`）
- 日志审计与 sudoreplay 回放
- NOPASSWD 与免密 sudo 的场景应用
- PAM（可插拔认证模块）的基础概念
- sudo 与 PAM 协同工作的原理

## sudo 的工作原理

### 为什么需要 sudo？

想象一下这个场景：你的团队有 5 个运维人员，都需要重启 nginx，但你不想把 root 密码告诉他们，因为 root 可以做任何事——包括误删系统文件。

`sudo` 解决的正是这个问题：**按策略授权**，让指定用户执行特定命令，使用自己的密码验证身份，操作全程留有审计日志。

### su 与 sudo 的核心区别

| | `su` | `sudo` |
|--|------|--------|
| 验证密码 | 目标用户的密码（通常是 root 密码） | 自己的密码 |
| 效果 | 完全切换到目标用户身份 | 以目标用户身份执行单条命令 |
| 权限粒度 | 全或无 | 可精细控制到具体命令 |
| 审计日志 | 无（只记录切换行为） | 每条命令都有记录 |

### sudo 的执行流程

当用户执行 `sudo` 时，系统按以下步骤处理：

1. 在 `/etc/sudoers` 中查找该用户是否有 `sudo` 权限
2. 若有权限，提示用户输入**自己的密码**确认身份（root 执行 `sudo` 不需要密码）
3. 密码正确后执行后续命令
4. 若切换的身份与执行者相同，也不需要密码

!!! tip "sudo 缓存"

    首次执行 `sudo` 后，认证结果会缓存 **5 分钟**。在这段时间内再次执行 `sudo` 不需要重新输入密码。可以用 `sudo -k` 立即清除缓存。

## 配置 sudo：/etc/sudoers

### 永远用 visudo 编辑

直接用 `vi` 编辑 `/etc/sudoers` 很危险——一旦语法错误，所有用户都将无法使用 `sudo`，而你可能因此被锁在系统外。

`visudo` 在退出时会自动做语法检查，有错误会提示你修改，不会写入损坏的配置：

```bash
visudo
```

### sudoers 语法格式

每一行的基本格式：

```
用户或组  来源主机=(以谁身份)  可执行的命令
```

内置关键字 `ALL` 代表"任意"（任意主机、任意身份、任意命令）。

### 常见配置示例

```bash title="典型 sudoers 配置片段"
# 允许 wheel 组所有成员使用 sudo（RHEL/CentOS 默认）
%wheel  ALL=(ALL:ALL)  ALL

# 允许特定用户执行所有 root 命令
alice   ALL=(ALL)  ALL

# 无密码 sudo（谨慎使用，仅用于自动化脚本账号）
bob     ALL=(ALL)  NOPASSWD: ALL

# 限制只能执行特定命令（绝对路径）
charlie ALL=(ALL)  /bin/systemctl restart nginx, /bin/systemctl status nginx

# 禁止切换到交互式 shell，防止用户绕过命令限制
dave    ALL=(ALL)  ALL, !/bin/bash, !/bin/sh

# 以特定非 root 用户身份运行（适合部署脚本场景）
eve     ALL=(deploy)  /opt/deploy/run.sh
```

!!! warning "命令必须使用绝对路径"

    `/etc/sudoers` 中的命令字段**必须填写绝对路径**，否则 `visudo` 会报语法错误。使用 `which 命令名` 可以查到绝对路径。

### 别名简化批量配置

当需要为多个用户、主机或命令配置相同规则时，别名可以大幅减少重复：

```bash title="sudoers 别名定义示例"
# 用户别名（名称必须全大写）
User_Alias  ADMINS = alice, bob, charlie

# 主机别名
Host_Alias  SERVERS = server1, server2, 192.168.1.0/24

# 命令别名
Cmnd_Alias  SHUTDOWN = /sbin/shutdown, /sbin/reboot, /sbin/halt

# 以身份别名运行
Runas_Alias OP = root, operator

# 引用别名
ADMINS  SERVERS=(OP)  SHUTDOWN
```

### /etc/sudoers.d/ 目录

直接修改 `/etc/sudoers` 主文件有一定风险。推荐的做法是在 `/etc/sudoers.d/` 目录下创建独立的配置文件，每个团队或应用对应一个文件，模块化管理、互不干扰：

```bash
# 为 deploy 用户创建独立配置
visudo -f /etc/sudoers.d/deploy
```

### Defaults 行：全局行为调整

```bash title="常用 Defaults 配置"
Defaults  timestamp_timeout=15    # sudo 认证缓存时间（分钟，0 表示每次都问）
Defaults  passwd_tries=3          # 密码最多尝试次数
Defaults  logfile=/var/log/sudo.log  # 额外日志文件
Defaults  requiretty              # 要求真实 TTY，防止脚本滥用
Defaults  env_reset               # 重置环境变量（安全默认）
Defaults  secure_path="..."       # sudo 执行时使用的 PATH
```

## sudo 常用命令

```bash
sudo command              # 以 root 身份执行命令
sudo -u username command  # 以指定用户身份执行
sudo -i                   # 打开登录 Shell（等同于 su -）
sudo -s                   # 打开 Shell（保留当前环境）
sudo -l                   # 列出当前用户被授权的命令
sudo -v                   # 刷新认证缓存（延长 15 分钟）
sudo -k                   # 立即使认证缓存失效
sudo -e file              # 以安全方式编辑文件（等同于 sudoedit）
sudo !!                   # 以 sudo 重新执行上一条命令
```

## PAM 认证框架

### 为什么需要 PAM？

在 PAM 出现之前，每个程序都需要自己实现登录验证逻辑——`login` 一套、`sshd` 一套、`su` 一套，密码策略难以统一，更改认证方式需要重新编译程序。

**PAM（Pluggable Authentication Modules，可插拔认证模块）** 把认证逻辑从程序中剥离出来，抽象成一套统一的 API。程序只需调用 PAM API，具体用什么方式认证（密码、指纹、LDAP、双因素）由配置文件决定，程序本身不需要修改。

```mermaid
graph LR
    A[应用程序\nssh / login / sudo] -->|调用 PAM API| B[PAM 框架]
    B -->|读取配置| C[/etc/pam.d/服务名]
    B --> D[pam_unix.so\n密码认证]
    B --> E[pam_ldap.so\nLDAP 认证]
    B --> F[pam_google_authenticator.so\n双因素认证]
    D & E & F -->|返回结果| B
    B -->|成功/失败| A

    classDef app fill:transparent,stroke:#0288d1,color:#adbac7,stroke-width:2px
    classDef pam fill:transparent,stroke:#388e3c,color:#adbac7,stroke-width:2px
    classDef mod fill:transparent,stroke:#768390,color:#adbac7,stroke-width:1px
    classDef conf fill:transparent,stroke:#f57c00,color:#adbac7,stroke-width:1px
    class A app
    class B pam
    class C conf
    class D,E,F mod
```

### PAM 四种模块类型

PAM 把认证过程分为四个阶段，每个阶段使用对应类型的模块：

| 类型 | 英文全称 | 职责 |
|------|----------|------|
| `auth` | authentication | 验证用户身份（如密码校验） |
| `account` | account | 检查账号状态（是否过期、是否有访问权限） |
| `password` | password | 处理密码修改（强度检查、更新密码） |
| `session` | session | 管理会话（登录/登出时的环境设置、日志记录） |

这四种类型通常有顺序：先验证身份（`auth`）→ 检查账号权限（`account`）→ 管理会话（`session`）→ 修改密码时才涉及（`password`）。

### PAM 控制标志

控制标志决定某个模块的验证结果如何影响整体认证流程：

| 标志 | 含义 |
|------|------|
| `required` | 必须成功；失败时继续检查其他模块（但最终会失败）。最常用，便于日志记录 |
| `requisite` | 必须成功；失败时**立即终止**并返回失败，不再检查后续模块 |
| `sufficient` | 成功时**立即终止**并返回成功（前提是之前没有 `required` 失败）；失败时继续 |
| `optional` | 结果通常不影响整体；只有在 `required`/`sufficient` 都不存在时才起决定作用 |

!!! tip "记忆技巧"

    `required` 和 `requisite` 都是失败就最终失败，区别是是否继续走流程。`sufficient` 和 `requisite` 是镜像关系：成功时 `sufficient` 立即终止，失败时 `requisite` 立即终止。

### PAM 配置文件位置

每个服务在 `/etc/pam.d/` 下对应一个同名配置文件：

```
/etc/pam.d/sshd      # sshd 的 PAM 配置
/etc/pam.d/login     # 控制台登录的 PAM 配置
/etc/pam.d/sudo      # sudo 的 PAM 配置
/etc/pam.d/passwd    # passwd 命令的 PAM 配置
/etc/pam.d/system-auth  # 被多个服务 include 的通用配置（RHEL 系）
```

每一行格式：`验证类型  控制标志  模块路径  [模块参数]`

```bash title="典型 /etc/pam.d/sshd 配置（精简版）"
auth       required     pam_unix.so
auth       optional     pam_group.so
account    required     pam_unix.so
account    required     pam_access.so
password   required     pam_unix.so sha512 shadow
session    required     pam_unix.so
session    optional     pam_lastlog.so
```

配置中的 `include` 关键字表示引入另一个配置文件的内容：

```bash
auth       include      system-auth   # 引入 /etc/pam.d/system-auth 的 auth 部分
```

### 常用 PAM 模块

| 模块 | 功能 |
|------|------|
| `pam_unix.so` | 传统 `/etc/shadow` 密码认证，功能最全面（auth/account/password/session 都支持） |
| `pam_securetty.so` | 限制 root 只能从 `/etc/securetty` 列出的终端登录（这就是为什么 root 不能 telnet） |
| `pam_nologin.so` | 若 `/etc/nologin` 文件存在，拒绝所有普通用户登录 |
| `pam_pwquality.so` | 密码复杂度检查（最小长度、字符类型要求等） |
| `pam_limits.so` | 资源限制，即 `ulimit` 功能，配置文件为 `/etc/security/limits.conf` |
| `pam_access.so` | 基于 `/etc/security/access.conf` 的细粒度访问控制 |
| `pam_faillock.so` | 登录失败次数统计与账号锁定（RHEL 8+，推荐） |
| `pam_tally2.so` | 登录失败计数与锁定（较旧系统） |
| `pam_env.so` | 设置额外环境变量 |
| `pam_selinux.so` | 认证期间临时关闭 SELinux，验证通过后再启用 |
| `pam_ldap.so` / `pam_sss.so` | LDAP / SSSD 集中认证 |
| `pam_google_authenticator.so` | Google Authenticator 两步验证 |

## 密码强度策略：pam_pwquality

### 配置密码复杂度

`pam_pwquality.so` 模块通过 `/etc/security/pwquality.conf` 配置密码策略：

```bash title="/etc/security/pwquality.conf"
minlen = 12          # 密码最短长度
minclass = 3         # 至少包含 3 种字符类型（大写/小写/数字/特殊字符）
maxrepeat = 3        # 最多连续重复相同字符数
dcredit = -1         # 至少包含 1 个数字（负数表示"至少N个"）
ucredit = -1         # 至少包含 1 个大写字母
lcredit = -1         # 至少包含 1 个小写字母
ocredit = -1         # 至少包含 1 个特殊字符
```

在 `/etc/pam.d/system-auth`（或 `common-password`）中启用：

```bash
password  requisite  pam_pwquality.so  try_first_pass local_users_only retry=3
```

## 登录失败锁定：pam_faillock

### 防止暴力破解

配置 `/etc/security/faillock.conf`：

```bash title="/etc/security/faillock.conf"
deny = 5             # 连续失败 5 次后锁定账号
unlock_time = 300    # 锁定 300 秒（5 分钟）后自动解锁
fail_interval = 900  # 统计失败次数的时间窗口（秒）
```

常用管理命令：

```bash
# 查看某用户的失败记录
faillock --user username

# 手动解锁账号
faillock --user username --reset
```

### 资源限制：limits.conf

`pam_limits.so` 读取 `/etc/security/limits.conf`，为用户或组设置系统资源上限：

```bash title="/etc/security/limits.conf 示例"
# 格式：账号/组  soft|hard  限制项目  限制值
# soft 是警告阈值，hard 是不可逾越的上限

alice   soft  fsize   90000    # 文件大小警告（90MB，单位 KB）
alice   hard  fsize   100000   # 文件大小硬限制（100MB）
@devs   hard  maxlogins  3     # dev 组成员最多同时 3 个会话
*       hard  nproc    65535   # 所有用户的最大进程数
```

!!! note "修改后何时生效？"

    `limits.conf` 的修改对**已登录用户无效**，只对下次登录生效。这是因为 PAM 在程序启动时调用，不是持续监控。

## 发行版差异

=== "Debian / Ubuntu"

    - `sudo` 组名为 `sudo`（而非 `wheel`），新建用户加入 `sudo` 组即可使用 `sudo`
    - PAM 密码策略包：`libpam-pwquality`（需手动安装 `apt install libpam-pwquality`）
    - 配置文件：`/etc/pam.d/common-password`（而非 `system-auth`）
    - 旧系统用 `pam_tally2.so`，新版（Ubuntu 22.04+）已切换到 `pam_faillock.so`
    - PAM 通用配置分散在 `/etc/pam.d/common-auth`、`common-account`、`common-password`、`common-session`

=== "Red Hat / RHEL / CentOS"

    - `sudo` 组名为 `wheel`，CentOS 7+ 默认已在 sudoers 中启用 `%wheel` 行
    - `pam_faillock.so` 从 RHEL 8 起默认集成，开箱即用
    - PAM 通用配置集中在 `/etc/pam.d/system-auth` 和 `/etc/pam.d/password-auth`
    - **推荐使用 `authselect` 管理 PAM 配置**，避免直接编辑容易被工具覆盖的文件：

    ```bash
    authselect list                             # 查看可用 profile
    authselect select sssd with-mkhomedir       # 选择 sssd profile
    authselect select sssd with-faillock        # 启用 faillock
    authselect current                          # 查看当前使用的 profile
    ```

## PAM 问题排查

遇到无法登录或认证异常时，PAM 的日志是第一现场：

```bash
# 查看 PAM 相关日志
tail -f /var/log/secure         # RHEL 系
tail -f /var/log/auth.log       # Debian 系

# 或通过 journald 过滤
journalctl -u sshd --since "1 hour ago"
journalctl -t sudo
```

日志中会出现类似 `pam_unix(sshd:auth): authentication failure` 或 `pam_faillock: user alice locked` 的信息，直接指向问题所在的模块。


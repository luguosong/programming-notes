---
title: 网络安全基础
---

# 网络安全基础

一台联网的 Linux 主机，从封包进入网卡那一刻起，就面临来自互联网的各种威胁。
理解攻击者的视角，才能有针对性地构建防线。本文从威胁分类出发，逐层介绍主机
加固、防火墙概念、暴力破解防护、强制访问控制、账号安全与日志审计，最后给出
可落地的加固 checklist。

**本文你会学到**：

- 攻击者常用的威胁手段分类及对应防御思路
- 主机安全加固的核心原则与实操命令
- 防火墙在 Linux 技术栈中的演进脉络
- Fail2ban 防暴力破解的配置方法
- 账号安全与日志审计的关键操作
- 一份可直接使用的安全加固 checklist

## 网络安全威胁分类

### 扫描与探测

攻击者在发起真正的攻击之前，首先需要摸清目标：开放了哪些端口、运行了什么服务、
操作系统版本是什么。这个阶段叫做 **侦察（Reconnaissance）**。

**端口扫描（Port Scanning）**

攻击者使用 `nmap` 等工具逐一探测目标主机的 TCP/UDP 端口是否开放，从而推断
运行的服务。常见扫描类型：

- `SYN scan`（半开放扫描）：发送 SYN 包，根据 SYN/ACK 或 RST 判断端口状态，不完成三次握手，速度快且隐蔽
- `Connect scan`：完整三次握手，日志更容易被记录
- `UDP scan`：扫描 DNS（53）、SNMP（161）等 UDP 服务

**操作系统指纹识别（OS Fingerprinting）**

通过分析 TCP/IP 协议栈的细微差异（TTL 值、TCP 窗口大小、选项顺序等），
工具可以较准确地判断目标操作系统类型和版本，为后续选择漏洞利用方式提供依据。

!!! tip "防御思路"

    - 用防火墙隐藏不必要的开放端口，减少攻击面
    - 定期用 `nmap` 对自身做扫描，了解自己暴露了什么（详见「端口扫描自查」一节）
    - 开启操作系统层的 ICMP 限制，减少指纹暴露

### 拒绝服务攻击（DoS / DDoS）

拒绝服务攻击（Denial of Service）的目标不是「入侵」，而是让你的服务**无法正常响应**。
分布式版本（DDoS）借助大量僵尸主机同时发起，更难防御。

**SYN Flood**

利用 TCP 三次握手的缺陷：攻击者大量发送 SYN 包但不回应 SYN/ACK，
使服务器半连接队列被占满，无法处理合法请求。

**UDP Flood**

向目标端口大量发送 UDP 包，消耗带宽与处理资源。

**放大攻击（Amplification Attack）**

利用 DNS、NTP、Memcached 等协议的请求/响应放大特性：攻击者伪造受害者 IP
发送小请求，反射服务器返回数倍乃至数百倍的响应流量给受害者。典型的有：

- DNS 放大：小 ANY 查询 → 大响应包
- NTP 放大：`monlist` 命令可返回 600 倍放大

!!! warning "DDoS 最难防御"

    大规模 DDoS 超出单机防御能力，需要上游 ISP 或 CDN/DDoS 清洗服务介入。
    单机层面可配置内核参数（`net.ipv4.tcp_syncookies=1`）来缓解 SYN Flood。

### 中间人攻击（MITM）

攻击者插入通信链路，对双方假冒对方身份，从而窃听或篡改流量。

**ARP 欺骗（ARP Spoofing）**

ARP 协议无认证机制。攻击者在局域网内广播伪造的 ARP 响应，将网关 IP 对应的
MAC 地址替换为自己的 MAC，所有流经网关的流量就会先经过攻击者主机。

**DNS 污染（DNS Poisoning）**

向 DNS 缓存中注入伪造的解析记录，将合法域名指向恶意 IP，劫持用户访问。

!!! tip "防御思路"

    - 局域网内部署动态 ARP 检测（DAI，交换机功能）
    - 强制使用 HTTPS（HSTS），中间人即使劫持流量也无法解密
    - 使用 DNSSEC 或加密 DNS（DoH / DoT）

### 漏洞利用与提权

当一个软件存在安全漏洞（Buffer Overflow、SQL Injection、RCE 等）时，
攻击者无需猜密码，可在数秒内取得系统控制权。典型路径：

1. 扫描发现目标运行有漏洞的服务版本
2. 使用公开 PoC（Exploit Database / Metasploit）发起攻击
3. 获取低权限 Shell 后，再利用本地提权漏洞（LPE）获取 `root`

此外，`rootkit` 会修改系统命令（`ls`、`ps`、`netstat`），隐藏自身进程和文件，
使管理员难以察觉入侵。

!!! tip "防御思路"

    - 及时更新软件，消灭已知漏洞
    - 最小化安装和开放服务，减少暴露面
    - SELinux / AppArmor 限制进程权限，即使被攻破也难以横向移动

## 主机安全加固原则

### 最小化原则

**最小权限（Principle of Least Privilege）**：每个进程、用户只应拥有完成
其工作所必需的最低权限，不多给一分。

**最小化安装（Minimal Install）**：服务器不需要图形界面、开发工具、
多媒体库等软件，安装的包越少，潜在漏洞面越小。安装后检查多余包：

``` bash title="检查并移除不必要的包"
# Debian/Ubuntu：列出手动安装的包
apt-mark showmanual | sort

# Red Hat/RHEL：查询安装的包列表
rpm -qa --qf '%{NAME}\n' | sort
```

### 及时打补丁

漏洞从披露到被大规模利用的窗口期往往只有数小时，自动安全更新是最低成本的
防御手段。

=== "Debian/Ubuntu"

    ``` bash title="启用自动安全更新"
    apt install unattended-upgrades
    dpkg-reconfigure --priority=low unattended-upgrades
    ```

    配置文件位于 `/etc/apt/apt.conf.d/50unattended-upgrades`，
    默认只自动安装安全更新，不升级大版本。

=== "Red Hat/RHEL"

    ``` bash title="启用自动安全更新"
    # RHEL 8+ / CentOS Stream / Fedora
    dnf install dnf-automatic
    systemctl enable --now dnf-automatic-install.timer
    ```

    配置文件位于 `/etc/dnf/automatic.conf`，将 `apply_updates = yes` 设为启用。

### 关闭不必要的服务

每个监听端口都是潜在的攻击入口。一台 Web 服务器不需要运行 FTP、Telnet、
Bluetooth 等服务。

``` bash title="检查并禁用不必要的服务"
# 列出所有启用的服务
systemctl list-unit-files --state=enabled --type=service

# 停止并禁用某个服务（以 cups 为例）
systemctl stop cups
systemctl disable cups
```

### 检查监听端口

``` bash title="查看当前所有监听端口"
# -t TCP  -u UDP  -l 监听中  -n 不解析主机名  -p 显示进程
ss -tlnp
ss -ulnp
```

输出示例解读：

```
State   Recv-Q  Send-Q  Local Address:Port   Peer Address:Port  Process
LISTEN  0       128     0.0.0.0:22            0.0.0.0:*          sshd
LISTEN  0       511     0.0.0.0:80            0.0.0.0:*          nginx
```

`0.0.0.0` 表示监听所有接口（对外暴露），`127.0.0.1` 表示只监听本地。
对不认识的监听进程要重点排查。

## 防火墙基础概念

> 本节只介绍概念和 Linux 技术栈演进。具体 iptables / nftables / firewalld
> 规则配置请参见「防火墙与 NAT 配置」。

### 无状态包过滤 vs 状态防火墙

| | 无状态包过滤 | 状态防火墙（Stateful） |
|---|---|---|
| 工作方式 | 逐包检查 IP/端口头信息 | 追踪连接状态表（conntrack） |
| 性能 | 较高（无状态维护开销） | 略低，但现代内核已优化 |
| 能力 | 只能匹配固定规则 | 可识别 `ESTABLISHED`、`RELATED` 连接 |
| 典型问题 | 无法放行响应包（需额外规则） | 连接追踪表可被 DoS 攻击耗尽 |

现代 Linux 防火墙默认都工作在有状态模式：对已建立连接的响应包自动放行，
大幅简化规则配置。

### Linux 防火墙技术栈演进

```
内核空间                              用户空间管理工具
──────────────────────────────────────────────────────
[Netfilter hooks]
      │
      ├── iptables（内核 2.4+，2001～现在）     ← iptables-nft（兼容层）
      │         历史最悠久，语法复杂，规则线性匹配
      │
      └── nftables（内核 3.13+，2014～现在）    ← firewalld（D-Bus 管理层）
                原生支持集合、映射，性能更好                ← ufw（简化前端）
                官方推荐替代 iptables
```

- **`iptables`**：2001 年引入，长期统治地位，规则为线性链式，管理复杂
- **`nftables`**：2014 年引入，语法统一、性能更好，是现代发行版的默认选择
- **`firewalld`**：基于 Zone 概念的动态防火墙管理服务，底层可调用 nftables 或 iptables，RHEL 默认
- **`ufw`**（Uncomplicated Firewall）：Debian/Ubuntu 提供的简化前端，底层调用 nftables

!!! tip "选哪个？"

    - RHEL 9 / CentOS Stream 9+：用 `firewalld`（底层 nftables）
    - Debian 12 / Ubuntu 22.04+：用 `ufw` 或直接写 `nftables` 规则
    - 需要精细控制：直接写 `nftables` 规则

## TCP Wrappers 与 hosts.allow/deny

`TCP Wrappers` 是 1990 年代的主机级访问控制机制，通过 `/etc/hosts.allow`
和 `/etc/hosts.deny` 两个文件，限制哪些 IP 可以连接到受保护的服务。

``` bash title="经典 TCP Wrappers 配置示例（历史参考）"
# /etc/hosts.allow
sshd: 192.168.1.0/24

# /etc/hosts.deny
sshd: ALL
```

!!! warning "已被现代工具取代"

    TCP Wrappers 只对使用 `libwrap` 编译的程序有效。现代 OpenSSH、
    Nginx、systemd-managed 服务大多不再链接 `libwrap`，这个机制
    实际上已失效。Debian 12 已将 `tcpd` 移出默认安装。

    **现代替代方案**：用防火墙规则（`nftables`/`firewalld`）+ Fail2ban 组合。

## Fail2ban：防暴力破解

### 为什么需要 Fail2ban

SSH 是 Linux 服务器最常见的管理入口，也是暴力破解的首要目标。
开放在公网的 SSH 服务器，每天会收到成千上万次的密码猜测请求：

``` bash title="查看暴力破解记录"
grep "Failed password" /var/log/auth.log | tail -20
# 你会看到大量来自不同 IP 的失败登录尝试
```

`Fail2ban` 的方案：监控日志文件，当某个 IP 在短时间内失败次数超过阈值，
自动通过 `iptables`/`nftables` 封禁该 IP 一段时间。

### 工作原理

```
[日志文件]  →  [Filter：正则匹配]  →  [Jail：计数/时间窗口]  →  [Action：封禁 IP]
/var/log/auth.log    sshd filter          ≥5次/10分钟          nftables DROP 规则
```

Fail2ban 的核心概念：

- **Filter**：正则表达式，从日志中提取失败的 IP 地址
- **Jail**：一个监控单元，绑定 Filter + 阈值 + Action
- **Action**：封禁动作，默认调用 `iptables`/`nftables`，也可发邮件通知
- **bantime**：封禁持续时间（默认 10 分钟，建议调高）

### 安装与配置

=== "Debian/Ubuntu"

    ``` bash title="安装 Fail2ban"
    apt install fail2ban
    systemctl enable --now fail2ban
    ```

=== "Red Hat/RHEL"

    ``` bash title="安装 Fail2ban（需 EPEL）"
    dnf install epel-release
    dnf install fail2ban
    systemctl enable --now fail2ban
    ```

**配置 SSH jail**

永远不要直接修改 `jail.conf`（升级会覆盖），应创建 `jail.local` 覆盖：

``` bash title="创建 /etc/fail2ban/jail.local"
[DEFAULT]
# 永久白名单（逗号或空格分隔）
ignoreip = 127.0.0.1/8 ::1 192.168.1.0/24

# 封禁时间：1h，支持 -1 表示永久封禁
bantime  = 1h

# 统计窗口：10 分钟内失败超过 maxretry 次则封禁
findtime = 10m
maxretry = 5

[sshd]
enabled = true
port    = ssh
# Debian/Ubuntu 日志路径
logpath = /var/log/auth.log
# Red Hat/RHEL 日志路径
# logpath = /var/log/secure
```

``` bash title="常用管理命令"
# 查看所有 jail 状态
fail2ban-client status

# 查看 sshd jail 详情（包括已封禁 IP 列表）
fail2ban-client status sshd

# 手动解封某个 IP
fail2ban-client set sshd unbanip 1.2.3.4

# 手动封禁某个 IP
fail2ban-client set sshd banip 1.2.3.4
```

!!! tip "Debian vs RHEL 差异"

    | 项目 | Debian/Ubuntu | Red Hat/RHEL |
    |------|--------------|--------------|
    | SSH 日志 | `/var/log/auth.log` | `/var/log/secure` |
    | 后端默认 | `iptables`（Ubuntu 22 后逐步迁移 nftables） | `nftables` |
    | 包来源 | 官方仓库 | 需要 EPEL |
    | systemd-journal | 也支持 `backend = systemd` 直接读 journal |同左|

## SELinux / AppArmor 简介

SELinux 和 AppArmor 是 Linux 的强制访问控制（MAC）机制，用于限制已入侵进程的破坏范围。详细原理与配置参见专题页面：

- [SELinux 详解](../process/selinux/index.md)
- [AppArmor 详解](../process/apparmor/index.md)

## 用户账号安全

### 禁用 root 直接 SSH 登录

`root` 账号是所有攻击者的首要目标，允许 root 直接 SSH 登录意味着：
攻击者只需猜中一个密码（或利用一个漏洞）就可以完全控制主机。

``` bash title="禁用 root SSH 登录"
# 编辑 /etc/ssh/sshd_config
PermitRootLogin no

# 重载配置（不中断现有连接）
systemctl reload sshd
```

!!! warning "先确保有其他管理员账号再操作"

    禁用 root SSH 前，必须先创建一个普通用户并加入 `sudo` 组，
    否则你会把自己锁在外面。

### 密码策略

**`/etc/login.defs`**：控制密码有效期等全局策略

``` bash title="/etc/login.defs 关键参数"
PASS_MAX_DAYS   90    # 密码最长使用天数
PASS_MIN_DAYS   1     # 两次修改密码的最短间隔天数
PASS_MIN_LEN    8     # 最短密码长度（已被 PAM 取代，保留为后备）
PASS_WARN_AGE   7     # 到期前几天开始警告
```

**PAM `pwquality` 模块**：控制密码复杂度

=== "Debian/Ubuntu"

    ``` bash title="安装并配置 pwquality"
    apt install libpam-pwquality

    # 编辑 /etc/security/pwquality.conf
    minlen = 12         # 最短 12 位
    dcredit = -1        # 至少 1 个数字
    ucredit = -1        # 至少 1 个大写字母
    lcredit = -1        # 至少 1 个小写字母
    ocredit = -1        # 至少 1 个特殊字符
    ```

=== "Red Hat/RHEL"

    ``` bash title="配置 pwquality（已内置）"
    # 编辑 /etc/security/pwquality.conf（同上参数）
    # RHEL 默认已在 /etc/pam.d/system-auth 中引用 pwquality
    ```

### sudo 替代 su

`su` 需要知道 `root` 密码，`sudo` 基于当前用户自己的密码，且操作全部
记录在 `/var/log/auth.log`（或 `/var/log/secure`）。

``` bash title="将用户加入 sudo / wheel 组"
# Debian/Ubuntu：加入 sudo 组
usermod -aG sudo username

# Red Hat/RHEL：加入 wheel 组
usermod -aG wheel username
```

对于需要精细控制的场景，用 `visudo` 编辑 `/etc/sudoers`：

``` bash title="sudo 精细控制示例"
# 允许 deploy 用户不输密码重启 nginx
deploy ALL=(ALL) NOPASSWD: /bin/systemctl restart nginx
```

### 检查 SUID / SGID 文件

设置了 SUID 位的程序以**文件所有者**（通常是 `root`）的权限运行，
是提权漏洞的重要来源，系统外的可疑 SUID 文件要高度警惕。

``` bash title="查找所有 SUID/SGID 文件"
# /6000 = SUID(4000) | SGID(2000) 任一匹配
find / -perm /6000 -type f 2>/dev/null | sort

# 对比系统包管理器的基准（以 Debian 为例）
dpkg --verify 2>/dev/null | grep '^..5'
```

!!! tip "正常的 SUID 程序"

    `/usr/bin/passwd`、`/usr/bin/sudo`、`/usr/bin/ping` 等是合法的 SUID 程序。
    重点关注不在标准路径下、或文件所有者不是 `root` 的 SUID 文件。

## 日志审计

### 关键日志文件

| 日志文件 | 发行版 | 内容 |
|---------|--------|------|
| `/var/log/auth.log` | Debian/Ubuntu | SSH 登录、`sudo`、PAM 认证 |
| `/var/log/secure` | Red Hat/RHEL | 同上 |
| `/var/log/syslog` | Debian/Ubuntu | 系统消息 |
| `/var/log/messages` | Red Hat/RHEL | 系统消息 |
| `/var/log/fail2ban.log` | 通用 | Fail2ban 封禁记录 |

!!! tip "使用 journalctl 统一查看"

    现代系统使用 `systemd-journald` 集中收集所有日志，可用 `journalctl` 查询：

    ``` bash title="用 journalctl 查看 SSH 登录"
    # 查看 SSH 服务日志（实时滚动）
    journalctl -u sshd -f

    # 查看今天的认证失败
    journalctl --since today | grep "Failed password"
    ```

### 登录历史查询

``` bash title="登录历史查询命令"
# 查看最近成功登录记录（读取 /var/log/wtmp）
last

# 查看失败登录记录（读取 /var/log/btmp）
lastb

# 查看当前在线用户
who
w
```

`lastb` 输出是暴力破解的直接证据：

``` bash title="统计失败登录次数最多的 IP"
lastb --time-format iso | awk '{print $3}' | \
  sort | uniq -c | sort -rn | head -20
```

### auditd 简介

`auditd` 是内核级别的审计框架，能记录远比普通日志更细粒度的事件：
谁在什么时间读写了哪个文件、执行了哪条系统调用。

``` bash title="auditd 基本使用"
# 安装并启动
apt install auditd     # 或 dnf install audit

# 监控 /etc/passwd 的读写
auditctl -w /etc/passwd -p rwa -k passwd-changes

# 查看审计日志
ausearch -k passwd-changes
aureport --summary
```

`auditd` 特别适合满足 PCI-DSS、等保等合规要求的场景。

## 端口扫描自查

防御的前提是知道自己暴露了什么。定期用 `nmap` 扫描自身，从攻击者视角
看自己的主机。

``` bash title="用 nmap 扫描本机"
# 安装 nmap
apt install nmap     # 或 dnf install nmap

# 扫描本机所有 TCP 端口（SYN 扫描，需要 root）
nmap -sS -p- localhost

# 扫描 UDP 常用端口（慢，需要 root）
nmap -sU --top-ports 20 localhost

# 服务版本识别 + OS 指纹
nmap -sV -O localhost

# 从外部视角扫描（模拟攻击者，替换为真实 IP）
nmap -sS -p 1-1024 <your-public-ip>
```

!!! warning "只扫描你自己的主机"

    未经授权对他人主机进行端口扫描在很多国家和地区属于违法行为。

对比 `nmap` 结果与预期开放端口，找出意外开放的服务立即关闭。

## 安全加固 Checklist

| 类别 | 检查项 | 命令/方法 | 优先级 |
|------|--------|----------|--------|
| **系统更新** | 启用自动安全更新 | `unattended-upgrades` / `dnf-automatic` | 🔴 高 |
| **系统更新** | 当前无待更新安全包 | `apt list --upgradable` / `dnf check-update` | 🔴 高 |
| **服务管理** | 关闭不必要的服务 | `systemctl list-unit-files --state=enabled` | 🔴 高 |
| **服务管理** | 端口监听只开必要项 | `ss -tlnp` / `ss -ulnp` | 🔴 高 |
| **防火墙** | 防火墙已启用 | `ufw status` / `firewall-cmd --state` | 🔴 高 |
| **防火墙** | 默认策略为 DROP/DENY | 检查 `INPUT` chain 默认规则 | 🔴 高 |
| **SSH 安全** | 禁用 root 直接登录 | `grep PermitRootLogin /etc/ssh/sshd_config` | 🔴 高 |
| **SSH 安全** | 使用密钥认证替代密码 | `PasswordAuthentication no` | 🔴 高 |
| **SSH 安全** | 修改默认端口（可选） | `Port 2222`（需同步更新防火墙） | 🟡 中 |
| **Fail2ban** | Fail2ban 已安装并运行 | `fail2ban-client status sshd` | 🔴 高 |
| **账号安全** | 无空密码账号 | `awk -F: '($2==""){print $1}' /etc/shadow` | 🔴 高 |
| **账号安全** | 密码策略已配置 | 检查 `/etc/login.defs` 和 `pwquality.conf` | 🟡 中 |
| **账号安全** | 无可疑 SUID 文件 | `find / -perm /6000 -type f 2>/dev/null` | 🟡 中 |
| **强制访问控制** | SELinux / AppArmor 已启用 | `getenforce` / `aa-status` | 🟡 中 |
| **日志审计** | 日志服务运行正常 | `systemctl status rsyslog` / `journalctl` | 🟡 中 |
| **日志审计** | 定期检查 `lastb` 异常登录 | `lastb \| head -30` | 🟡 中 |
| **端口自查** | 定期 nmap 扫描自身 | `nmap -sS -p- localhost` | 🟢 低 |
| **内核参数** | 开启 SYN Cookie | `sysctl net.ipv4.tcp_syncookies` = 1 | 🟡 中 |
| **内核参数** | 禁止 IP 转发（非路由器） | `sysctl net.ipv4.ip_forward` = 0 | 🟡 中 |

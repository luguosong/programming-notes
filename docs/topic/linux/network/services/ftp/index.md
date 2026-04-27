---
title: FTP 服务器
---

# FTP 服务器

**本文你会学到**：

- FTP 协议的命令通道与数据通道
- 主动模式与被动模式的区别与防火墙配置
- 匿名 FTP 服务配置与限制
- 用户账号认证与权限隔离
- vsftpd 配置文件的关键参数
- 客户端连接与故障排查
- 为何 FTP 应被 SFTP / FTPS 替代
- FTP 的安全陷阱与最小权限原则

!!! warning "安全警告：FTP 是明文传输协议"

    FTP 在网络上以**明文**传输账号、密码和文件内容，极易被中间人截获。**现代环境请优先使用 SFTP（基于 SSH）或 FTPS（FTP over TLS）**。本文介绍的纯 FTP 仅适用于内网隔离环境或公开匿名下载场景。

## FTP 协议概述

FTP（File Transfer Protocol）是最古老的文件传输协议之一，使用 TCP 协议，并同时维护两条连接通道：

- **命令通道**：客户端连接服务器 `port 21`，传输用户名、密码、目录列表等命令
- **数据通道**：传输实际文件内容，建立方向取决于工作模式

### 主动模式与被动模式

FTP 有两种数据连接模式，区别在于数据通道由谁发起连接。

``` mermaid
sequenceDiagram
    participant C as 客户端
    participant S as FTP 服务器

    Note over C,S: 主动模式（Active）
    C->>S: 命令通道: 连接 port 21
    C->>S: PORT 命令: 告知本地 port BB
    S->>C: 服务器主动从 port 20 连接客户端 port BB（数据通道）

    Note over C,S: 被动模式（Passive / PASV）
    C->>S: 命令通道: 连接 port 21
    C->>S: PASV 命令: 请求被动模式
    S->>C: 回应: 服务器监听 port PASV（随机高端口）
    C->>S: 客户端主动连接服务器 port PASV（数据通道）
```

| 维度 | 主动模式 | 被动模式 |
|------|---------|---------|
| 数据连接发起方 | **服务器** → 客户端 | **客户端** → 服务器 |
| 服务器数据端口 | `port 20`（固定） | 随机高端口（可限定范围） |
| 客户端在 NAT 后 | ❌ 服务器无法回连内网 | ✅ 客户端主动出去，正常工作 |
| 防火墙复杂度 | 客户端防火墙需放行入站 | 服务器防火墙需放行被动端口范围 |

!!! tip "现代实践"

    绝大多数 FTP 客户端默认使用**被动模式**，因为客户端通常在 NAT 或防火墙后面。服务器端需通过 `pasv_min_port` / `pasv_max_port` 限定被动端口范围，便于防火墙放行。

### 安全问题与替代方案

FTP 的核心安全问题：

- 账号密码以**明文**通过命令通道传输
- 文件内容以**明文**通过数据通道传输
- 历史上多次暴露严重安全漏洞

主要替代方案对比见本文末尾「FTP vs SFTP vs FTPS 对比」章节。

## vsftpd 安装与基础配置

`vsftpd`（Very Secure FTP Daemon）以安全为设计核心：以低权限用户身份运行、支持 `chroot` 监狱、将高权限操作限制在独立受控进程中。

### 安装

=== "Debian/Ubuntu"

    ``` bash title="安装 vsftpd"
    apt update && apt install vsftpd -y
    systemctl enable --now vsftpd
    ```

=== "Red Hat/RHEL"

    ``` bash title="安装 vsftpd"
    dnf install vsftpd -y
    systemctl enable --now vsftpd
    ```

### 关键配置项说明

vsftpd 的配置集中在 `/etc/vsftpd.conf`（Debian）或 `/etc/vsftpd/vsftpd.conf`（RHEL）。注意：**等号两侧不能有空格**。

``` text title="/etc/vsftpd.conf 关键参数速查"
# ── 服务模式 ──────────────────────────────
listen=YES                    # standalone 模式（IPv4），推荐
listen_ipv6=NO                # 若启用 listen，此项应为 NO

# ── 用户登录权限 ──────────────────────────
anonymous_enable=NO           # 是否允许匿名登录
local_enable=YES              # 是否允许系统本地用户登录
write_enable=YES              # 是否允许上传（写入操作）

# ── chroot 限制 ──────────────────────────
chroot_local_user=YES         # 将本地用户限制在家目录（推荐开启）
chroot_list_enable=YES        # 启用例外名单
chroot_list_file=/etc/vsftpd/chroot_list  # 不受 chroot 限制的账号列表

# ── 被动模式端口范围 ──────────────────────
pasv_min_port=40000
pasv_max_port=40100

# ── 连接控制 ─────────────────────────────
max_clients=50                # 最大同时连接数（0 = 不限）
max_per_ip=5                  # 同一 IP 最大连接数
idle_session_timeout=300      # 空闲超时（秒）
data_connection_timeout=120   # 数据传输超时（秒）

# ── 日志 ─────────────────────────────────
xferlog_enable=YES
xferlog_file=/var/log/xferlog
dual_log_enable=YES
vsftpd_log_file=/var/log/vsftpd.log

# ── 其他 ─────────────────────────────────
use_localtime=YES             # 使用本地时区（否则默认 GMT）
pam_service_name=vsftpd
userlist_enable=YES
userlist_deny=YES
```

### 匿名 FTP 配置（只读公开下载）

适用场景：校园内网公开软件镜像、团队内部公开文档分享。

``` text title="/etc/vsftpd.conf —— 匿名只读配置"
# 禁用本地用户，只允许匿名
local_enable=NO
anonymous_enable=YES
no_anon_password=YES          # 跳过密码验证
anon_world_readable_only=YES  # 仅允许下载可读文件
write_enable=NO               # 禁止任何写操作
anon_upload_enable=NO
anon_mkdir_write_enable=NO

# 匿名根目录为 ftp 账号的家目录（默认 /var/ftp）
# 将文件放入 /var/ftp/pub/ 即可对外提供下载

# 速率与连接限制
anon_max_rate=2000000         # 2 MB/s
max_clients=100
max_per_ip=5
idle_session_timeout=600

use_localtime=YES
xferlog_enable=YES
listen=YES
pam_service_name=vsftpd
```

``` bash title="设置匿名目录权限"
# 匿名根目录不能被 ftp 用户写入（安全要求）
chmod 755 /var/ftp
# 公开下载目录
mkdir -p /var/ftp/pub
chmod 755 /var/ftp/pub
```

### 本地用户 FTP（chroot 限制）

允许系统账号通过 FTP 管理自己的家目录，同时用 `chroot` 防止跨目录访问。

``` text title="/etc/vsftpd.conf —— 本地用户配置"
anonymous_enable=NO
local_enable=YES
write_enable=YES
local_umask=022

# 所有本地用户默认 chroot，例外账号写入 chroot_list
chroot_local_user=YES
chroot_list_enable=YES
chroot_list_file=/etc/vsftpd/chroot_list

userlist_enable=YES
userlist_deny=YES
userlist_file=/etc/vsftpd/user_list

use_localtime=YES
xferlog_enable=YES
listen=YES
pam_service_name=vsftpd
```

!!! warning "chroot + 可写家目录的限制"

    vsftpd 要求：若用户被 chroot 到家目录，**家目录本身不能被该用户写入**（否则报 `500 OOPS: vsftpd: refusing to run with writable root inside chroot`）。

    解决方案：

    - 家目录权限设为 `755`（不可被用户写入），在家目录下创建可写子目录
    - 或在 vsftpd.conf 中添加 `allow_writeable_chroot=YES`（有安全风险，不推荐）

## vsftpd 虚拟用户

虚拟用户是不存在于系统 `/etc/passwd` 的 FTP 专属账号，通过 PAM 和 Berkeley DB 实现认证。

### 创建用户数据库

``` bash title="创建虚拟用户数据库"
# 安装 db 工具
apt install libdb-utils -y        # Debian
# dnf install libdb-utils -y     # RHEL

# 创建用户文件：奇数行为用户名，偶数行为密码
cat > /etc/vsftpd/virtual_users.txt << 'EOF'
alice
alice_password
bob
bob_password
EOF

# 生成 Berkeley DB 格式数据库
db_load -T -t hash -f /etc/vsftpd/virtual_users.txt /etc/vsftpd/virtual_users.db
chmod 600 /etc/vsftpd/virtual_users.db
```

### PAM 配置

``` text title="/etc/pam.d/vsftpd-virtual"
auth    required pam_userdb.so db=/etc/vsftpd/virtual_users
account required pam_userdb.so db=/etc/vsftpd/virtual_users
```

### vsftpd 配置（虚拟用户模式）

``` text title="/etc/vsftpd.conf —— 虚拟用户配置"
# 虚拟用户映射到系统用户 ftp
guest_enable=YES
guest_username=ftp
local_enable=YES
pam_service_name=vsftpd-virtual

# 虚拟用户目录（每个用户对应一个子目录）
user_sub_token=$USER
local_root=/var/ftp/virtual/$USER
write_enable=YES
virtual_use_local_privs=YES
```

``` bash title="为虚拟用户创建目录"
mkdir -p /var/ftp/virtual/alice /var/ftp/virtual/bob
chown ftp:ftp /var/ftp/virtual/alice /var/ftp/virtual/bob
```

## FTPS（FTP over TLS）配置

FTPS 在 FTP 基础上加入 TLS 加密，保护命令和数据通道。分两种模式：

| 模式 | 端口 | 说明 |
|------|------|------|
| 显式 FTPS（推荐） | `21` | 先建立普通 FTP，再通过 `AUTH TLS` 升级为加密 |
| 隐式 FTPS | `990` | 连接时直接协商 TLS，较旧的方式 |

### 生成自签名证书

``` bash title="生成 vsftpd TLS 证书"
openssl req -x509 -nodes -days 3650 -newkey rsa:2048 \
    -keyout /etc/ssl/private/vsftpd.key \
    -out /etc/ssl/certs/vsftpd.crt \
    -subj "/CN=$(hostname)"

chmod 600 /etc/ssl/private/vsftpd.key
```

### vsftpd SSL 配置

``` text title="/etc/vsftpd.conf —— 显式 FTPS 配置"
# 启用 SSL/TLS
ssl_enable=YES

# 证书与私钥
rsa_cert_file=/etc/ssl/certs/vsftpd.crt
rsa_private_key_file=/etc/ssl/private/vsftpd.key

# 强制本地用户使用 TLS（匿名用户可豁免）
force_local_logins_ssl=YES
force_local_data_ssl=YES
allow_anon_ssl=NO

# 仅允许 TLS 1.2+（禁用旧版不安全协议）
ssl_tlsv1_2=YES
ssl_sslv2=NO
ssl_sslv3=NO
ssl_tlsv1=NO

# 推荐的加密套件
ssl_ciphers=HIGH
```

!!! tip "客户端支持"

    FileZilla 连接 FTPS 时，在站点管理器的「加密」选项中选择「要求显式 FTP over TLS」（显式模式）即可。服务器使用自签名证书时，客户端首次连接会提示接受证书。

## 被动模式与防火墙

### 限定被动端口范围

``` text title="/etc/vsftpd.conf —— 被动端口配置"
pasv_enable=YES
pasv_min_port=40000
pasv_max_port=40100

# 若服务器在 NAT 后，需告知客户端真实公网 IP
# pasv_address=203.0.113.10
```

### 防火墙放行（firewalld）

``` bash title="firewalld 放行 FTP 相关端口"
# 放行命令端口
firewall-cmd --permanent --add-service=ftp

# 放行被动模式端口范围
firewall-cmd --permanent --add-port=40000-40100/tcp

# 加载 FTP 连接跟踪模块（支持主动模式 NAT 穿透）
firewall-cmd --permanent --add-module=nf_conntrack_ftp

firewall-cmd --reload
```

### 防火墙放行（iptables）

``` bash title="iptables 放行 FTP 相关端口"
# 加载连接跟踪模块（主动模式必须）
modprobe nf_conntrack_ftp
echo "nf_conntrack_ftp" >> /etc/modules-load.d/ftp.conf

# 放行命令端口
iptables -A INPUT -p tcp --dport 21 -j ACCEPT

# 放行被动端口范围
iptables -A INPUT -p tcp --dport 40000:40100 -j ACCEPT

# 允许已建立的连接（数据通道回包）
iptables -A INPUT -m state --state ESTABLISHED,RELATED -j ACCEPT
```

!!! tip "nf_conntrack_ftp 的作用"

    `nf_conntrack_ftp` 内核模块能感知 FTP 协议，自动识别 PORT/PASV 命令中的数据端口，从而让 `RELATED` 规则自动放行数据通道。**主动模式下必须加载此模块**，否则防火墙会拦截服务器从 port 20 发出的数据连接。

## FTP 客户端

### 命令行客户端

``` bash title="ftp 命令基础用法"
# 连接 FTP 服务器
ftp ftp.example.com

# 在 ftp> 提示符下常用命令
# ls          列出远程目录
# cd pub      进入远程目录
# get file    下载文件
# put file    上传文件
# binary      切换二进制模式（传输非文本文件必须）
# passive     切换被动/主动模式
# bye         退出
```

``` bash title="lftp 功能更强的客户端"
# 安装
apt install lftp -y

# 连接并自动使用被动模式
lftp -u alice ftp.example.com

# 镜像下载整个目录
lftp -e "mirror /pub /local/backup; bye" ftp.example.com

# 支持 FTPS
lftp -e "set ftp:ssl-force true; open ftp.example.com"
```

### curl 访问 FTP

``` bash title="curl 操作 FTP"
# 列目录
curl ftp://ftp.example.com/pub/

# 下载文件
curl -o file.tar.gz ftp://user:pass@ftp.example.com/pub/file.tar.gz

# 上传文件
curl -T localfile.txt ftp://user:pass@ftp.example.com/upload/

# 使用 FTPS（显式）
curl --ftp-ssl ftp://user:pass@ftp.example.com/pub/
```

### FileZilla 图形客户端

FileZilla 是跨平台的开源 FTP 客户端（[filezilla-project.org](https://filezilla-project.org/)），同时支持 FTP、FTPS（显式/隐式）和 SFTP。

连接配置要点：

- **协议**：选「FTP」（纯 FTP）或「SFTP」
- **加密**：选「要求显式 FTP over TLS」（FTPS 显式）
- **传输设置**：建议选「被动模式」，最大连接数与服务器 `max_per_ip` 保持一致

## FTP vs SFTP vs FTPS 对比

| 特性 | FTP | SFTP | FTPS（显式） |
|------|-----|------|------------|
| 传输加密 | ❌ 明文 | ✅ SSH 全程加密 | ✅ TLS 加密 |
| 身份认证加密 | ❌ 明文 | ✅ SSH 密钥/密码 | ✅ TLS 加密 |
| 底层协议 | TCP（21/20） | SSH（22） | TCP（21）+ TLS |
| 防火墙友好性 | ⚠️ 需要两个端口+被动范围 | ✅ 单端口 22 | ⚠️ 与 FTP 相同 |
| NAT 穿透 | ⚠️ 需要连接跟踪模块 | ✅ 无问题 | ⚠️ 与 FTP 相同 |
| 服务器配置复杂度 | 中 | 低（SSH 内置） | 高（需 TLS 证书） |
| 客户端兼容性 | 极广（遗留系统） | 广（现代系统） | 较广 |
| 适用场景 | 遗留兼容、内网匿名 | **推荐：通用文件传输** | 必须 FTP 协议且需加密 |

!!! tip "现代建议"

    - **优先选 SFTP**：基于 SSH，配置简单，单端口，全程加密，安全性最高
    - **次选 FTPS**：需要兼容仅支持 FTP 的旧客户端时，叠加 TLS 加密
    - **纯 FTP 仅用于**：公开匿名只读下载（内网或公网均可），或完全隔离的遗留系统集成

## 访问控制

### 黑白名单机制

vsftpd 提供两层用户访问控制：

**`/etc/vsftpd/ftpusers`（PAM 层黑名单）**

由 `/etc/pam.d/vsftpd` 引用，列表内的用户**始终**无法登录 FTP，不受 vsftpd.conf 控制。默认包含 `root`、`bin`、`daemon` 等系统账号。

**`/etc/vsftpd/user_list`（vsftpd 层黑/白名单）**

行为由 `userlist_deny` 控制：

``` text title="/etc/vsftpd.conf —— user_list 行为控制"
userlist_enable=YES
userlist_file=/etc/vsftpd/user_list

# 黑名单模式（默认）：列表内的用户不能登录
userlist_deny=YES

# 白名单模式：只有列表内的用户才能登录
# userlist_deny=NO
```

``` bash title="示例：切换为白名单模式"
# vsftpd.conf 设置 userlist_deny=NO 后
# user_list 只写允许登录的账号
echo "alice" >> /etc/vsftpd/user_list
echo "bob"   >> /etc/vsftpd/user_list
systemctl restart vsftpd
```

!!! warning "两个文件的优先级"

    `ftpusers`（PAM）检查先于 `user_list`（vsftpd）。即使 `user_list` 设为白名单，`ftpusers` 中的账号依然无法登录。如需允许 `root` 登录（**强烈不推荐**），必须同时从两个文件中移除。

## 日志与排查

### 日志格式

vsftpd 支持两种日志：

``` text title="xferlog 格式示例（wu-ftp 兼容格式）"
Mon Aug  8 16:35:22 2024 1 192.168.1.100 1234567 /home/alice/data.tar.gz b _ o r alice ftp 0 * c
# 字段依次：时间 传输秒数 客户端IP 文件大小 文件路径 传输类型 特殊操作 传输方向 访问模式 用户名 服务 认证方式 认证用户ID 传输结果
```

``` text title="vsftpd.log 格式示例（vsftpd 原生格式）"
Mon Aug  8 16:35:22 2024 [pid 12345] CONNECT: Client "192.168.1.100"
Mon Aug  8 16:35:23 2024 [pid 12345] [alice] OK LOGIN: Client "192.168.1.100"
Mon Aug  8 16:35:30 2024 [pid 12345] [alice] OK DOWNLOAD: Client "192.168.1.100", "/home/alice/data.tar.gz", 1234567 bytes, 2.34Mbyte/sec
```

### 启用双日志

``` text title="/etc/vsftpd.conf —— 日志配置"
xferlog_enable=YES
xferlog_file=/var/log/xferlog        # wu-ftp 兼容格式
xferlog_std_format=YES

dual_log_enable=YES
vsftpd_log_file=/var/log/vsftpd.log  # vsftpd 原生格式（更易读）
```

### 常见错误排查

**`530 Login incorrect`**

用户名或密码错误，或账号被拒绝登录。排查步骤：

- 检查 `/etc/vsftpd/ftpusers` 和 `/etc/vsftpd/user_list` 中是否包含该账号
- 确认 `local_enable=YES`（本地用户）或 `anonymous_enable=YES`（匿名）
- 查看 `/var/log/vsftpd.log` 中的详细拒绝原因
- RHEL 系统检查 SELinux：`getsebool -a | grep ftp`

``` bash title="RHEL 系统 SELinux 放行 FTP 家目录访问"
# 允许本地用户访问家目录
setsebool -P ftp_home_dir=1

# 若需要匿名上传
setsebool -P allow_ftpd_anon_write=1
```

**`500 OOPS: cannot change directory`**

用户登录后无法切换到家目录，通常是 SELinux 或文件权限问题。

``` bash title="排查目录访问问题"
# 检查家目录权限
ls -ld /home/alice

# 检查 SELinux 上下文
ls -Z /home/alice

# 恢复默认 SELinux 上下文
restorecon -Rv /home/alice
```

**被动模式无法传输数据**

客户端连上命令通道但无法列目录或下载，典型提示 `Can't build data connection`：

- 检查防火墙是否放行了 `pasv_min_port`～`pasv_max_port` 端口范围
- 确认已加载 `nf_conntrack_ftp` 模块：`lsmod | grep conntrack_ftp`
- 若服务器在 NAT 后，检查 `pasv_address` 是否配置为公网 IP


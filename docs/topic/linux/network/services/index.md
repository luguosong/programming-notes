---
title: 服务器概览
---

# 服务器概览

**本文你会学到**：

- 服务器（守护进程）的概念与运行模型
- inetd 超级服务器与 standalone 模式的区别
- 按协议层和用途的服务器分类方式
- Linux 服务器的通用配置模式（配置文件、日志、端口、启停）
- 服务器安全加固的基本清单
- 常见服务的速查表

## 服务器是什么？

### 从守护进程说起

当你启动一个 Web 服务器（如 Nginx）时，背后运行的是一种特殊的进程——**守护进程**（Daemon）。它长期驻留在后台，等待客户端的连接请求，收到请求后进行处理并返回结果。

守护进程有几个共同特征：

- **后台运行**：脱离终端控制，不占用任何终端
- **长期存活**：从系统启动到关闭期间持续运行
- **等待事件**：监听端口或文件描述符，等待客户端请求
- **系统级管理**：通常由 root 或专用系统用户运行

在 Linux 中，守护进程的管理已经从早期的 SysVinit 脚本全面过渡到 systemd。你可以用 `systemctl` 统一管理所有服务器服务：

``` bash title="systemd 管理服务器的基本命令"
# 查看所有正在运行的服务
systemctl list-units --type=service --state=running

# 启动/停止/重启一个服务
systemctl start nginx
systemctl stop nginx
systemctl restart nginx

# 设置开机自启/取消自启
systemctl enable nginx
systemctl disable nginx
```

### inetd 超级服务器 vs standalone

传统上，服务器有两种运行模型：

| 模型 | 工作方式 | 优点 | 缺点 |
|------|---------|------|------|
| **standalone** | 每个服务独立启动，自己监听端口 | 响应快、性能好 | 占用资源多，服务多了管理复杂 |
| **inetd/xinetd** | 一个超级服务器监听所有端口，按需启动实际服务 | 节省资源，统一管理 | 每次连接有启动开销，不适合高频服务 |

现代 Linux 发行版中，inetd 模式已很少使用。大多数服务器都采用 standalone 模式运行，由 systemd 统一管理生命周期。

???+ info "什么时候还会遇到 inetd？"

    一些老旧的或低频使用的服务（如 `finger`、`daytime`）仍然通过 inetd 配置。你主要在维护遗留系统时会碰到它。

## 服务器分类

### 按协议层分类

``` mermaid
graph LR
    A[服务器类型] --> B[网络层]
    A --> C[传输层]
    A --> D[应用层]
    B --> B1["DHCP<br>IP 地址分配"]
    C --> C1["SSH<br>加密远程连接"]
    D --> D1["HTTP/HTTPS<br>Web 服务"]
    D --> D2["DNS<br>域名解析"]
    D --> D3["FTP/SFTP<br>文件传输"]
    D --> D4["SMTP/IMAP<br>邮件服务"]
    D --> D5["NTP<br>时间同步"]

    classDef regular fill:transparent,stroke:#0288d1,color:#adbac7,stroke-width:1px
    class A,B,C,D,B1,C1,D1,D2,D3,D4,D5 regular
```

### 按用途分类

| 类别 | 典型服务 | 说明 |
|------|---------|------|
| **远程访问** | SSH、Telnet | 允许用户远程登录和管理系统 |
| **文件共享** | NFS、Samba、FTP | 在网络中共享文件和目录 |
| **Web 服务** | Nginx、Apache、Tomcat | 托管网站和 Web 应用 |
| **域名解析** | BIND、dnsmasq | 将域名翻译为 IP 地址 |
| **邮件服务** | Postfix、Dovecot | 发送和接收电子邮件 |
| **时间同步** | chrony、ntpd | 保持系统时钟准确 |
| **地址分配** | DHCP | 自动为网络设备分配 IP 地址 |

## 通用配置模式

虽然每个服务器功能不同，但它们的配置方式有着高度一致的模式。掌握了这个模式，学习任何新服务器都会事半功倍。

### 配置文件

几乎所有 Linux 服务器的配置文件都遵循以下约定：

| 位置 | 用途 | 示例 |
|------|------|------|
| `/etc/服务名/` | 主配置目录 | `/etc/nginx/`、`/etc/ssh/` |
| `/etc/服务名.conf` | 主配置文件 | `/etc/dhcp/dhcpd.conf` |
| `/etc/服务名/*.conf` | 拆分的子配置 | `/etc/nginx/conf.d/*.conf` |

???+ tip "配置修改后的通用流程"

    1. 编辑配置文件
    2. 检查配置语法（大多数服务提供了检查命令）
    3. 重启或重载服务

    ``` bash
    # 以 Nginx 为例
    vim /etc/nginx/nginx.conf          # 编辑配置
    nginx -t                            # 检查语法
    systemctl reload nginx              # 重载配置（不中断服务）
    ```

### 日志

服务器日志是排查问题的关键工具：

| 位置 | 说明 |
|------|------|
| `/var/log/服务名/` | 服务自己的日志目录 |
| `/var/log/messages` 或 `syslog` | 系统级日志（systemd 管理的服务） |
| `journalctl -u 服务名` | systemd journal 中查看特定服务日志 |

### 端口与防火墙

每个服务器需要开放对应的网络端口。配置防火墙是部署服务器的必经步骤：

``` bash title="防火墙配置示例（firewalld）"
# 查看已开放的端口和服务
firewall-cmd --list-all

# 永久开放 HTTP 端口
firewall-cmd --permanent --add-service=http
firewall-cmd --reload

# 永久开放自定义端口
firewall-cmd --permanent --add-port=8080/tcp
firewall-cmd --reload
```

### 启停命令

不管什么服务，管理命令的套路都是一样的：

``` bash
# 标准的 systemd 服务管理
systemctl start 服务名       # 启动
systemctl stop 服务名        # 停止
systemctl restart 服务名     # 重启（先停后启）
systemctl reload 服务名      # 重载配置（不中断服务）
systemctl status 服务名      # 查看状态
systemctl enable 服务名      # 开机自启
systemctl disable 服务名     # 取消自启
```

## 服务器安全清单

部署任何服务器之前，都应该过一遍这份基本安全清单：

| 安全措施 | 说明 | 常用做法 |
|---------|------|---------|
| 最小权限 | 只给服务运行所需的最小权限 | 用专用系统用户运行服务 |
| 限制访问 | 不对全世界开放 | 防火墙限制来源 IP |
| 加密传输 | 明文协议改用加密版本 | SSH 替代 Telnet，HTTPS 替代 HTTP |
| SELinux/AppArmor | 利用强制访问控制 | 保持启用，按需放行 |
| 日志审计 | 记录所有访问和异常 | 配置 logrotate 防止日志撑爆磁盘 |
| 及时更新 | 关注安全补丁 | `yum update` / `apt upgrade` |
| chroot/容器 | 将服务隔离在受限环境中 | Docker 或 systemd 的 `RootDirectory=` |

???+ warning "千万不要做的事"

    - 用 root 用户运行服务（除非服务本身要求）
    - 在公网暴露数据库端口（3306、5432 等）
    - 使用默认密码或弱密码
    - 关闭 SELinux 而不是正确配置它

## 服务器速查表

| 服务 | 默认端口 | 配置文件 | 常用命令 | 本站笔记 |
|------|---------|---------|---------|---------|
| SSH | 22/tcp | `/etc/ssh/sshd_config` | `ssh`、`scp`、`sftp` | 「SSH 远程连接」 |
| DHCP | 67/udp（服务端） | `/etc/dhcp/dhcpd.conf` | `dhcpd` | 「DHCP 服务器」 |
| DNS | 53/tcp+udp | `/etc/named.conf` 或 `/etc/dnsmasq.conf` | `dig`、`nslookup` | 「DNS 服务器」 |
| FTP | 21/tcp | `/etc/vsftpd/vsftpd.conf` | `ftp`、`lftp` | 「FTP 服务器」 |
| NFS | 2049/tcp | `/etc/exports` | `exportfs`、`showmount` | 「NFS 与 Samba」 |
| Samba | 139+445/tcp | `/etc/samba/smb.conf` | `smbclient`、`testparm` | 「NFS 与 Samba」 |
| NTP | 123/udp | `/etc/chrony/chrony.conf` | `chronyc` | 「NTP 时间同步」 |
| 邮件（SMTP） | 25/tcp | `/etc/postfix/main.cf` | `postfix`、`sendmail` | 「邮件服务器」 |
| HTTP | 80/tcp | `/etc/nginx/nginx.conf` | `nginx -t` | 「Web 服务器」 |
| HTTPS | 443/tcp | 同 HTTP + TLS 配置 | `certbot` | 「Web 服务器」 |

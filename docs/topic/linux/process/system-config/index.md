---
title: 系统基本配置
---

# 系统基本配置

**本文你会学到**：

- 用 `ip` 命令查看网络信息与网卡命名规则
- NetworkManager 与 `nmcli` 的基础配置
- 在 Debian、RHEL 上配置静态 IP 的方法
- DNS 客户端配置与域名解析排查
- 修改主机名与时区的永久设置
- 内核参数调优（`sysctl`）的常见场景
- 防火墙（firewalld、ufw）的开放端口操作
- 网络性能诊断命令与工具
- 网络故障排查的系统思路

## 网络配置

### 查看当前网络状态

配置网络之前，先摸清楚现状：

``` bash
ip addr show          # 查看所有网卡 IP（推荐，替代旧版 ifconfig）
ip link show          # 查看链路状态（UP/DOWN、MTU 等）
ip route show         # 查看路由表
ss -tuln              # 查看监听端口（替代旧版 netstat）
nmcli device status   # NetworkManager 管理的设备状态
```

网卡命名规则（现代 Linux 使用"可预测网络接口名"）：

- `eno1`：主板 BIOS 内置网卡
- `ens3`：主板 BIOS 内置的 PCIe 网卡
- `enp2s0`：独立 PCIe 网卡（`p2` 表示 PCIe 插槽 2，`s0` 表示接口 0）
- `eth0`：上述命名均不适用时的回退名称

### 用 nmcli 配置网络

绝大多数现代发行版（RHEL、Debian 12+、Ubuntu 等）默认使用 `NetworkManager` 管理网络，`nmcli` 是其命令行前端。

``` bash
# 查看所有连接及其状态
nmcli connection show
nmcli connection show --active

# 配置静态 IP（修改后需 up 才生效）
nmcli connection modify eth0 \
  ipv4.method manual \
  ipv4.addresses "192.168.1.100/24" \
  ipv4.gateway "192.168.1.1" \
  ipv4.dns "8.8.8.8,8.8.4.4"
nmcli connection up eth0

# 切换回 DHCP 自动获取
nmcli connection modify eth0 ipv4.method auto
nmcli connection up eth0

# 创建新以太网连接
nmcli connection add type ethernet ifname eth0 con-name "myconn"

# 查看指定连接的 DNS 设置
nmcli connection show eth0 | grep DNS
```

!!! tip "nmcli 支持 Tab 补全"

    `nmcli` 完整支持 bash-completion，输入 `nmcli connection modify eth0 ` 后按 Tab 可列出所有可用属性，无须死记硬背参数名。

### 配置文件直接编辑

不同发行版的网络配置文件格式不同：

=== "Debian / Ubuntu（Netplan）"

    Ubuntu 18.04+ 默认使用 Netplan，配置文件为 YAML 格式：

    ``` yaml title="/etc/netplan/01-netcfg.yaml"
    network:
      version: 2
      ethernets:
        eth0:
          dhcp4: false
          addresses:
            - 192.168.1.100/24
          gateway4: 192.168.1.1
          nameservers:
            addresses: [8.8.8.8, 8.8.4.4]
    ```

    ``` bash
    netplan try      # 测试配置（120 秒后自动回滚，安全）
    netplan apply    # 应用配置（立即生效，无回滚）
    ```

=== "Red Hat / RHEL（NetworkManager keyfile）"

    RHEL 8+ 推荐使用 keyfile 格式（INI 风格）：

    ``` ini title="/etc/NetworkManager/system-connections/eth0.nmconnection"
    [connection]
    id=eth0
    type=ethernet

    [ipv4]
    method=manual
    addresses=192.168.1.100/24
    gateway=192.168.1.1
    dns=8.8.8.8;8.8.4.4;
    ```

    修改后执行 `nmcli connection reload` 让 NetworkManager 重新读取配置文件。

### hosts 与 resolv.conf

`/etc/hosts` 提供本地静态 DNS 解析，优先级高于 DNS 查询，适合写死内网服务器地址：

```
127.0.0.1   localhost
192.168.1.10  myserver myserver.local
```

`/etc/resolv.conf` 记录 DNS 服务器地址，通常由 NetworkManager 自动管理——不要直接手动修改，重启后会被覆盖：

```
nameserver 8.8.8.8
nameserver 8.8.4.4
search example.com    # 短域名自动补全搜索后缀
```

## 主机名配置

``` bash
# 查看当前主机名详情（包含操作系统、内核版本等）
hostnamectl

# 持久化设置主机名（修改 /etc/hostname 并立即生效）
hostnamectl set-hostname myserver

# 三种主机名类型
hostnamectl set-hostname "My Server" --pretty       # 展示名：可含空格和特殊字符
hostnamectl set-hostname myserver --static          # 静态名：内核使用，通常与文件一致
hostnamectl set-hostname myserver.local --transient # 瞬态名：网络动态分配，重启重置
```

主机名持久化存储在 `/etc/hostname`（systemd 系统），修改后无需重启即可生效。

!!! tip "同步更新 /etc/hosts"

    修改主机名后，建议同步将新主机名写入 `/etc/hosts`（如 `127.0.1.1 myserver`），避免某些程序反查主机名时超时。

## 时区与时间配置

### 查看与设置时区

``` bash
# 查看当前时间状态（本地时间、UTC、硬件时钟、NTP 同步状态）
timedatectl

# 设置时区（修改 /etc/localtime 符号链接）
timedatectl set-timezone Asia/Shanghai

# 搜索可用时区
timedatectl list-timezones | grep Asia
timedatectl list-timezones | grep Shanghai
```

时区文件存放于 `/usr/share/zoneinfo/`，`/etc/localtime` 是指向对应时区文件的符号链接：

``` bash
ls -l /etc/localtime
# lrwxrwxrwx ... /etc/localtime -> ../usr/share/zoneinfo/Asia/Shanghai
```

### NTP 自动同步

``` bash
# 启用 NTP 时间自动同步
timedatectl set-ntp true

# 查看 NTP 同步状态
timedatectl show-timesync
```

大多数发行版使用轻量级的 `systemd-timesyncd` 作为 NTP 客户端，配置文件为：

``` ini title="/etc/systemd/timesyncd.conf"
[Time]
NTP=ntp.aliyun.com time.cloudflare.com
FallbackNTP=time1.google.com
```

修改后执行 `systemctl restart systemd-timesyncd` 生效。

### 手动校时与硬件时钟

``` bash
# 手动设置系统时间（启用 NTP 时此操作会被覆盖）
timedatectl set-time "2024-01-15 10:30:00"

# 查看硬件时钟（BIOS/RTC）
hwclock --show

# 将系统时间写入硬件时钟
hwclock --systohc

# 将硬件时钟同步到系统时间
hwclock --hctosys
```

!!! warning "虚拟机注意事项"

    虚拟机的硬件时钟通常与宿主机同步，且默认为 UTC 时区。若发现每次开机时间偏差 8 小时，检查 `timedatectl` 输出中 `RTC in local TZ` 是否为 `no`，以及宿主机的时区配置。

## 语言与字符集

### 查看与设置系统语言

``` bash
# 查看系统 locale 配置（/etc/locale.conf）
localectl

# 查看当前 shell 会话的 locale 变量
locale

# 列出系统已安装的 locale
localectl list-locales
localectl list-locales | grep zh

# 持久化设置系统语言（重新登录后生效）
localectl set-locale LANG=en_US.UTF-8
localectl set-locale LANG=zh_CN.UTF-8

# 当前 shell 临时切换（不影响其他会话）
export LANG=en_US.UTF-8
export LC_ALL=en_US.UTF-8
```

`LANG` 是所有 `LC_*` 变量的默认值；`LC_ALL` 强制覆盖所有 `LC_*` 变量，优先级最高。

### 生成与安装 locale

=== "Debian / Ubuntu"

    ``` bash
    # 生成 locale（写入 /etc/locale.gen 后编译）
    locale-gen en_US.UTF-8
    locale-gen zh_CN.UTF-8

    # 更新系统默认 locale
    update-locale LANG=en_US.UTF-8
    ```

=== "Red Hat / RHEL"

    RHEL 通过 glibc 语言包提供 locale，无需单独生成：

    ``` bash
    # 安装中文语言包
    dnf install glibc-langpack-zh

    # 安装英文语言包
    dnf install glibc-langpack-en
    ```

## 内核参数调优（sysctl）

内核的很多运行时行为可以通过 `sysctl` 接口动态调整，无需重新编译内核或重启系统。

### 查看与临时修改

``` bash
# 查看某个参数当前值
sysctl net.ipv4.ip_forward

# 临时修改（重启后失效）
sysctl -w net.ipv4.ip_forward=1

# 等效的直接写法（proc 虚拟文件系统）
echo 1 > /proc/sys/net/ipv4/ip_forward

# 查看所有参数
sysctl -a
sysctl -a | grep ipv4
```

### 持久化配置

将参数写入 `/etc/sysctl.d/*.conf`（推荐）或 `/etc/sysctl.conf`：

``` bash title="/etc/sysctl.d/99-custom.conf"
# 开启 IP 转发（路由/NAT 必须开启）
net.ipv4.ip_forward = 1

# 禁用 IPv6（可选）
net.ipv6.conf.all.disable_ipv6 = 1

# 调整 TCP 连接队列（高并发服务器）
net.core.somaxconn = 65535
net.ipv4.tcp_max_syn_backlog = 65535

# 内存过量分配（容器/数据库环境常用）
vm.overcommit_memory = 1

# 最大文件句柄数
fs.file-max = 1000000
```

``` bash
sysctl -p                                    # 重新加载 /etc/sysctl.conf
sysctl -p /etc/sysctl.d/99-custom.conf       # 加载指定文件
sysctl --system                              # 加载所有配置文件（推荐）
```

!!! tip "配置文件命名约定"

    `/etc/sysctl.d/` 下的文件按字典序加载，数字前缀越大优先级越高（后加载的值覆盖先加载的值）。自定义配置建议使用 `99-` 前缀，确保覆盖发行版默认值。

## 防火墙基本配置

防火墙工具因发行版而异，但核心操作相同：放行端口/服务、拒绝未知流量。

=== "firewalld（RHEL 默认）"

    ``` bash
    # 启动并设置开机自启
    systemctl enable --now firewalld
    firewall-cmd --state

    # 开放端口/服务（--permanent 写入永久配置，需 reload 生效）
    firewall-cmd --permanent --add-port=80/tcp
    firewall-cmd --permanent --add-port=443/tcp
    firewall-cmd --permanent --add-service=http
    firewall-cmd --reload

    # 查看当前规则
    firewall-cmd --list-all
    firewall-cmd --list-ports

    # 放行整个网段
    firewall-cmd --permanent --add-rich-rule='rule family="ipv4" source address="192.168.1.0/24" accept'
    firewall-cmd --reload

    # 删除规则
    firewall-cmd --permanent --remove-port=80/tcp
    firewall-cmd --reload
    ```

    !!! warning "运行时配置与永久配置"

        不带 `--permanent` 的 `firewall-cmd` 只修改运行时规则，重启 `firewalld` 后丢失。**生产环境必须加 `--permanent`，并在最后执行 `--reload`。**

=== "ufw（Debian / Ubuntu）"

    ``` bash
    # 启用防火墙
    ufw enable
    ufw status verbose

    # 允许常用端口
    ufw allow 80/tcp
    ufw allow 443/tcp
    ufw allow ssh           # 等效于 ufw allow 22/tcp

    # 拒绝端口
    ufw deny 23/tcp

    # 放行来自特定 IP 的指定端口
    ufw allow from 192.168.1.0/24 to any port 22

    # 删除规则
    ufw delete allow 80/tcp
    ```

## 发行版差异汇总

| 配置项 | Debian / Ubuntu | Red Hat / RHEL |
|--------|----------------|----------------|
| 网络管理 | Netplan（Ubuntu 18.04+）→ NetworkManager | NetworkManager（`nmcli` / `nmtui`） |
| 网络配置文件 | `/etc/netplan/*.yaml` | `/etc/NetworkManager/system-connections/*.nmconnection` |
| 防火墙前端 | `ufw` → iptables/nftables 后端 | `firewalld` → nftables 后端 |
| locale 生成 | `locale-gen` + `update-locale` | `dnf install glibc-langpack-*` |
| locale 配置文件 | `/etc/locale.gen`、`/etc/default/locale` | `/etc/locale.conf` |
| 主机名文件 | `/etc/hostname` | `/etc/hostname` |
| 时区链接 | `/etc/localtime` → `/usr/share/zoneinfo/...` | `/etc/localtime` → `/usr/share/zoneinfo/...` |


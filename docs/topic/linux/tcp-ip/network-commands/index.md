---
title: 网络命令与诊断
---

# 网络命令与诊断

网络不通时，你知道从哪里下手吗？本文从接口管理到抓包分析，系统整理 Linux 网络诊断的核心工具链，帮你建立从物理层到应用层的排查思路。

**本文你会学到**：

- 用 `ip` 命令管理网络接口与路由（替代过时的 `ifconfig` / `route`）
- 用 `ss` 查看端口监听状态（替代 `netstat`）
- `ping` / `traceroute` / `mtr` 逐跳诊断网络连通性
- `dig` / `nslookup` 深度排查 DNS 解析问题
- `curl` / `wget` / `nc` 进行 HTTP 调试与端口测试
- `tcpdump` 抓包分析报文流向
- 各发行版网络配置文件的差异与现代化配置方式
- 从物理层到应用层的完整排查流程

---

## 网络接口管理

### 为什么不再用 ifconfig？

`ifconfig` 是 `net-tools` 包中的老工具，许多现代发行版已默认不安装。`ip` 命令来自 `iproute2` 包，功能更完整，已成为标准。

查看所有网络接口：

``` bash title="查看接口状态"
# 查看所有接口的 IP 信息（等价于旧的 ifconfig）
ip addr show
ip addr   # 简写

# 只看接口链路状态（MAC 地址、MTU、是否 UP）
ip link show

# 查看指定接口的统计信息
ip -s link show eth0
```

输出中的关键信息：

- `state UP / DOWN`：接口是否启用
- `inet 192.168.1.100/24`：IPv4 地址与子网掩码
- `link/ether 08:00:27:71:85:bd`：MAC 地址
- `mtu 1500`：最大传输单元

### 临时配置 IP 地址

!!! warning "临时生效"

    以下操作重启网络服务或系统后会失效，永久配置请修改对应发行版的配置文件。

``` bash title="添加/删除 IP"
# 为 eth0 添加 IP（子网掩码 /24）
ip addr add 192.168.1.100/24 broadcast + dev eth0

# 删除 IP
ip addr del 192.168.1.100/24 dev eth0

# 在同一张网卡上添加第二个 IP（虚拟接口别名）
ip addr add 192.168.50.50/24 dev eth0 label eth0:vbird
```

### 启用与禁用接口

``` bash title="接口开关"
# 启用接口
ip link set eth0 up

# 禁用接口
ip link set eth0 down

# 修改 MTU
ip link set eth0 mtu 9000
```

### 发行版永久配置差异

=== "Debian/Ubuntu"

    **Debian 传统方式**（`/etc/network/interfaces`）：

    ``` bash title="/etc/network/interfaces"
    auto eth0
    iface eth0 inet static
        address 192.168.1.100
        netmask 255.255.255.0
        gateway 192.168.1.254
        dns-nameservers 8.8.8.8
    ```

    **Ubuntu 18.04+ Netplan 方式**（`/etc/netplan/*.yaml`）：

    ``` yaml title="/etc/netplan/01-network.yaml"
    network:
      version: 2
      renderer: networkd
      ethernets:
        eth0:
          addresses:
            - 192.168.1.100/24
          routes:
            - to: default
              via: 192.168.1.254
          nameservers:
            addresses: [8.8.8.8, 8.8.4.4]
    ```

    应用配置：`sudo netplan apply`

=== "Red Hat/RHEL"

    **旧式配置文件**（RHEL 7 及以下，`/etc/sysconfig/network-scripts/ifcfg-eth0`）：

    ``` bash title="/etc/sysconfig/network-scripts/ifcfg-eth0"
    TYPE=Ethernet
    BOOTPROTO=static
    IPADDR=192.168.1.100
    PREFIX=24
    GATEWAY=192.168.1.254
    DNS1=8.8.8.8
    ONBOOT=yes
    ```

    **现代方式 nmcli**（RHEL 8+）：

    ``` bash title="nmcli 配置"
    # 查看连接
    nmcli connection show

    # 设置静态 IP
    nmcli con mod eth0 ipv4.addresses 192.168.1.100/24
    nmcli con mod eth0 ipv4.gateway 192.168.1.254
    nmcli con mod eth0 ipv4.method manual
    nmcli con up eth0

    # 图形化 TUI 工具
    nmtui
    ```

---

## 路由管理

### 查看与配置路由表

``` bash title="ip route 基本用法"
# 查看路由表（等价于旧的 route -n）
ip route show

# 输出示例：
# 192.168.1.0/24 dev eth0 proto kernel scope link src 192.168.1.100
# default via 192.168.1.254 dev eth0
```

添加、删除路由：

``` bash title="路由增删"
# 添加直连网段路由
ip route add 192.168.100.0/24 dev eth0

# 添加需要经过网关的路由
ip route add 192.168.200.0/24 via 192.168.100.1 dev eth0

# 添加默认网关
ip route add default via 192.168.1.254 dev eth0

# 删除路由
ip route del 192.168.100.0/24
```

!!! tip "旧工具兼容"

    旧文档中常见的 `route -n` 已被 `ip route show` 取代，`route add / del` 已被 `ip route add / del` 取代。两者功能等价，但 `ip` 提供更精细的控制（如指定 MTU、metric 等）。

### 用 ss 查看端口监听

当你不确定某个服务是否正常启动时，第一反应应该是看端口是否在监听。`ss` 是 `netstat` 的现代替代品，速度更快：

``` bash title="ss 常用用法"
# 查看所有监听的 TCP/UDP 端口（含进程名）
ss -tulnp

# 参数说明：
# -t  只看 TCP
# -u  只看 UDP
# -l  只看 LISTEN 状态
# -n  不解析主机名/服务名，直接显示 IP 和端口号
# -p  显示对应进程 PID 和名称

# 查看所有已建立的连接
ss -antp

# 筛选特定端口
ss -tnp 'sport = :22'
```

典型输出解读：

``` text
State  Recv-Q Send-Q  Local Address:Port   Peer Address:Port  Process
LISTEN 0      128     0.0.0.0:22           0.0.0.0:*          users:(("sshd",pid=1155))
ESTAB  0      52      192.168.1.100:22     192.168.1.101:1937 users:(("sshd",pid=4716))
```

- `LISTEN`：服务正在等待连接
- `ESTAB`：已建立的活跃连接
- `0.0.0.0:22`：监听所有接口的 22 端口；`127.0.0.1:25` 则只监听本地回环

!!! tip "旧工具兼容"

    `netstat -tulnp` 的功能由 `ss -tulnp` 完全替代。如果系统仍有 `netstat`，两者均可使用，但新脚本建议使用 `ss`。

---

## 网络连通性测试

### ping：验证主机可达性

`ping` 发送 ICMP Echo Request 报文，是最基础的连通性测试工具：

``` bash title="ping 常用参数"
# 发送 3 次后停止（不指定 -c 会一直发）
ping -c 3 8.8.8.8

# 不解析主机名（速度更快）
ping -n -c 3 192.168.1.254

# 测试 MTU 路径（发送 1472 字节不允许分片）
ping -c 2 -s 1472 -M do 192.168.1.254
# MTU 值 = 包大小 + 28（IP头20 + ICMP头8）

# 设置 TTL 值
ping -t 64 -c 3 8.8.8.8
```

输出中 `ttl=245` 表示该报文经过了 `255-245=10` 个路由节点（假设对方初始 TTL 为 255）。`time=15.4 ms` 越小表示网络时延越低。

### traceroute：逐跳追踪路径

当 `ping` 通了但速度慢，用 `traceroute` 定位是哪一跳出了问题：

``` bash title="traceroute / tracepath"
# 默认使用 UDP 探测（部分节点可能不响应）
traceroute -n google.com

# 使用 ICMP 模式（类似 Windows 的 tracert）
traceroute -I -n google.com

# 使用 TCP 80 端口（穿透大多数防火墙）
traceroute -T -p 80 -n google.com

# tracepath：不需要 root 权限的简化版
tracepath -n google.com
```

输出中出现 `* * *` 表示该节点不响应探测报文（可能是防火墙过滤），不一定代表网络中断，看后续节点是否恢复即可判断。

### mtr：动态综合诊断

`mtr` 结合了 `ping` 和 `traceroute` 的功能，实时显示每个节点的丢包率和时延：

``` bash title="mtr 用法"
# 交互式界面（实时刷新）
mtr google.com

# 非交互模式，输出报告
mtr -n --report -c 20 google.com
```

重点关注 `Loss%` 列：某跳出现丢包但后续节点正常，通常是该节点限速 ICMP；若从某跳起持续丢包，则问题在该跳或之前。

---

## DNS 查询工具

### 为什么访问不了域名但能 ping 通 IP？

能 `ping 8.8.8.8` 但打不开网页，99% 是 DNS 解析出了问题。先检查 `/etc/resolv.conf`：

``` bash title="/etc/resolv.conf 配置"
# 查看当前 DNS 配置
cat /etc/resolv.conf

# 典型内容：
# nameserver 8.8.8.8
# nameserver 8.8.4.4
# search example.com
```

### nslookup：快速查询

``` bash title="nslookup 基本用法"
# 查询 A 记录（域名 → IP）
nslookup www.google.com

# 查询 MX 记录（邮件服务器）
nslookup -query=mx gmail.com

# 查询 NS 记录（权威域名服务器）
nslookup -query=ns google.com

# 反向查询（IP → 域名）
nslookup 8.8.8.8

# 指定 DNS 服务器查询
nslookup www.google.com 1.1.1.1
```

### dig：专业 DNS 诊断

`dig` 输出更详细，是排查 DNS 问题的首选工具：

``` bash title="dig 详细用法"
# 查询 A 记录
dig www.google.com

# 查询 MX 记录
dig mx gmail.com

# 查询 NS 记录
dig ns google.com

# PTR 反向解析
dig -x 8.8.8.8

# 指定 DNS 服务器
dig @1.1.1.1 www.google.com

# 只显示答案部分（去掉冗余信息）
dig +short www.google.com

# 追踪完整解析链（从根域名服务器开始）
dig +trace www.google.com
```

`dig` 输出结构：

- `QUESTION SECTION`：查询内容
- `ANSWER SECTION`：解析结果（`A` 记录是 IPv4，`CNAME` 是别名）
- `Query time: 15 msec`：查询耗时

### /etc/hosts：本地覆盖解析

`/etc/hosts` 优先于 DNS 解析，常用于本地开发或临时绕过 DNS：

``` bash title="/etc/hosts 示例"
# 格式：IP  主机名  [别名...]
127.0.0.1   localhost
192.168.1.10  myserver.local myserver
```

!!! tip "解析顺序"

    Linux 默认解析顺序：`/etc/hosts` → DNS（由 `/etc/nsswitch.conf` 中的 `hosts:` 行控制）。

---

## 远程连接与传输

### curl：HTTP/HTTPS 调试利器

当你需要调试 API、确认服务是否正常响应，`curl` 是最常用的工具：

``` bash title="curl 核心参数"
# 普通 GET 请求
curl https://api.example.com/status

# 显示响应头（-I 只看头，-v 看完整交互）
curl -I https://www.google.com
curl -v https://api.example.com

# 自定义请求方法与 Header
curl -X POST https://api.example.com/login \
  -H "Content-Type: application/json" \
  -d '{"user":"admin","pass":"secret"}'

# 保存响应到文件
curl -o output.html https://example.com

# 跟随重定向
curl -L https://example.com

# 忽略 SSL 证书验证（仅测试用）
curl -k https://self-signed.example.com

# 限速下载（调试带宽限制场景）
curl --limit-rate 100K -O https://example.com/bigfile.tar.gz
```

### wget：文件下载与递归抓取

``` bash title="wget 常用用法"
# 下载单个文件
wget https://example.com/file.tar.gz

# 后台下载（不阻塞终端）
wget -b https://example.com/bigfile.tar.gz

# 断点续传
wget -c https://example.com/bigfile.tar.gz

# 递归下载整个站点（谨慎使用）
wget -r -np https://example.com/docs/

# 通过代理下载
wget -e "https_proxy=http://proxy:3128" https://example.com/file
```

### nc（netcat）：网络瑞士军刀

`nc` 用于端口测试、简单数据传输，常被称为「TCP/UDP 的 telnet」：

``` bash title="nc 常用场景"
# 测试某个端口是否可达（替代 telnet）
nc -zv 192.168.1.10 80
nc -zv -w 3 192.168.1.10 22   # 3 秒超时

# 扫描端口范围
nc -zv 192.168.1.10 20-100

# 开启本地监听（服务端）
nc -l -p 9999

# 客户端连接（另一个终端）
nc 127.0.0.1 9999

# 简单文件传输（接收端先监听）
nc -l -p 9999 > received_file.txt
# 发送端：
nc 192.168.1.10 9999 < local_file.txt
```

!!! warning "安全提示"

    `nc` 传输不加密，生产环境传输敏感数据请使用 `scp` 或 `sftp`。

---

## 抓包分析

### tcpdump：命令行抓包

当 `ping` 通了但服务无响应，用 `tcpdump` 看报文是否真的到达了目标端口：

``` bash title="tcpdump 核心语法"
# 抓取 eth0 上的所有报文（-n 不解析名字）
tcpdump -i eth0 -n

# 抓取特定主机的流量
tcpdump -i eth0 -n host 192.168.1.101

# 抓取特定端口
tcpdump -i eth0 -n port 80

# 组合过滤（来源 IP 且端口 22）
tcpdump -i eth0 -n 'src host 192.168.1.101 and port 22'

# 抓取并保存到文件（后续用 Wireshark 分析）
tcpdump -i eth0 -w capture.pcap

# 从文件读取回放
tcpdump -r capture.pcap

# 显示报文内容（ASCII 模式，适合 HTTP 调试）
tcpdump -i eth0 -n -A port 80

# 限制抓取数量
tcpdump -i eth0 -n -c 100 port 443
```

常用过滤表达式：

| 表达式 | 含义 |
|--------|------|
| `host 10.0.0.1` | 抓取与该主机相关的所有流量 |
| `src host 10.0.0.1` | 只抓来自该主机的报文 |
| `dst port 80` | 只抓目标端口为 80 的报文 |
| `tcp port 22` | 只抓 TCP 22 端口 |
| `not port 22` | 排除 SSH 流量 |
| `net 192.168.1.0/24` | 抓取整个子网的流量 |

!!! tip "三次握手验证"

    `tcpdump -i lo -n port 22` 可以观察到 SSH 建立连接时的三次握手：先看到 `[S]`（SYN），再看 `[S.]`（SYN+ACK），最后看 `[.]`（ACK），连接建立成功。

### Wireshark：图形化抓包分析

`Wireshark` 提供图形界面，适合深度分析复杂协议：

``` bash title="安装 Wireshark"
# Debian/Ubuntu
sudo apt install wireshark

# RHEL/CentOS
sudo yum install wireshark-gnome
```

Wireshark 三大区域：

- **上区**：报文列表（时间、来源、目的、协议、摘要）
- **中区**：选中报文的分层详情（以太网帧 → IP → TCP → 应用层）
- **下区**：原始十六进制与 ASCII 内容

常用捕获过滤器（在开始抓包前设置）：

- `tcp port 80` — 只抓 HTTP
- `host 192.168.1.10` — 只抓该主机流量

常用显示过滤器（抓包后筛选）：

- `http.request.method == "POST"` — 只显示 POST 请求
- `tcp.flags.syn == 1` — 只显示 SYN 报文

---

## 网络配置文件

### Debian 系配置文件

=== "Debian/Ubuntu（传统）"

    **`/etc/network/interfaces`**（Debian / Ubuntu 18.04 以前）：

    ``` text title="/etc/network/interfaces"
    # 回环接口
    auto lo
    iface lo inet loopback

    # 静态 IP
    auto eth0
    iface eth0 inet static
        address 192.168.1.100
        netmask 255.255.255.0
        gateway 192.168.1.254
        dns-nameservers 8.8.8.8 8.8.4.4

    # DHCP 动态获取
    # iface eth0 inet dhcp
    ```

    重启网络：`systemctl restart networking`

=== "Ubuntu（Netplan）"

    **`/etc/netplan/*.yaml`**（Ubuntu 18.04+）：

    ``` yaml title="/etc/netplan/00-installer-config.yaml"
    network:
      version: 2
      ethernets:
        enp3s0:
          dhcp4: false
          addresses:
            - 192.168.1.100/24
          nameservers:
            addresses:
              - 8.8.8.8
              - 8.8.4.4
          routes:
            - to: default
              via: 192.168.1.254
    ```

    应用：`sudo netplan apply`；测试：`sudo netplan try`（30 秒无确认自动回滚）

### RHEL 系配置文件

=== "RHEL 7（旧）"

    **`/etc/sysconfig/network-scripts/ifcfg-<interface>`**：

    ``` text title="ifcfg-eth0"
    TYPE=Ethernet
    NAME=eth0
    DEVICE=eth0
    ONBOOT=yes
    BOOTPROTO=static
    IPADDR=192.168.1.100
    PREFIX=24
    GATEWAY=192.168.1.254
    DNS1=8.8.8.8
    DNS2=8.8.4.4
    ```

    重启网络：`systemctl restart network`

=== "RHEL 8+（nmcli）"

    ``` bash title="nmcli 完整配置流程"
    # 查看所有连接
    nmcli connection show

    # 创建静态 IP 连接
    nmcli con add type ethernet \
      con-name eth0 ifname eth0 \
      ipv4.method manual \
      ipv4.addresses 192.168.1.100/24 \
      ipv4.gateway 192.168.1.254 \
      ipv4.dns "8.8.8.8 8.8.4.4"

    # 激活连接
    nmcli con up eth0

    # 修改已有连接
    nmcli con mod eth0 ipv4.addresses 192.168.1.200/24
    nmcli con up eth0

    # 图形 TUI（交互式菜单）
    nmtui
    ```

---

## 网络诊断流程

遇到网络问题，按层次从下往上排查，快速定位故障所在：

``` mermaid
graph TD
    A[网络不通] --> B{网卡是否加载？}
    B -- 否 --> B1[lspci 查硬件\n驱动模块未加载]
    B -- 是 --> C{能 ping 自身 IP？}
    C -- 否 --> C1[ip addr 检查 IP 配置\n子网掩码是否正确]
    C -- 是 --> D{能 ping 局域网网关？}
    D -- 否 --> D1[ip link 查接口状态\n检查网线/交换机]
    D -- 是 --> E{能 ping 公网 IP？}
    E -- 否 --> E1[ip route 检查默认路由\n确认 gateway 设置]
    E -- 是 --> F{能解析域名？}
    F -- 否 --> F1[检查 /etc/resolv.conf\ndig / nslookup 验证]
    F -- 是 --> G{能访问目标服务？}
    G -- 否 --> G1[ss 查端口监听\ntcpdump 确认报文到达\n检查防火墙规则]
    G -- 是 --> H[✅ 网络正常]
```

### 分层排查命令速查

**第一层：物理 / 接口**

``` bash title="第一层排查"
# 检查网卡是否被识别
lspci | grep -i network
ip link show

# 确认接口已启用
ip link set eth0 up
ip addr show eth0
```

**第二层：IP 配置**

``` bash title="第二层排查"
# 确认 IP 和子网掩码
ip addr show

# 测试自身 IP
ping -c 3 <本机IP>

# 测试局域网网关
ping -c 3 <gateway_IP>
```

**第三层：路由**

``` bash title="第三层排查"
# 确认默认路由存在
ip route show

# 测试对外连通性
ping -c 3 8.8.8.8
```

**第四层：DNS**

``` bash title="第四层排查"
# 检查 DNS 配置
cat /etc/resolv.conf

# 验证 DNS 解析
dig +short www.google.com
nslookup www.google.com
```

**第五层：应用 / 服务**

``` bash title="第五层排查"
# 查看端口是否在监听
ss -tulnp | grep 80

# 抓包确认报文是否到达
tcpdump -i eth0 -n port 80

# 测试 HTTP 响应
curl -v http://192.168.1.10

# 测试端口连通性
nc -zv 192.168.1.10 80
```

!!! tip "问题定位思路"

    能 `ping` 通 IP 但打不开域名 → DNS 问题；能解析域名但服务无响应 → 端口/防火墙问题；偶发性丢包 → 用 `mtr` 定位丢包节点；速度慢 → `traceroute` 找高延迟跳点，再结合 `tcpdump` 分析重传。

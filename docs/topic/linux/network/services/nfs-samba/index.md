---
title: 文件共享：NFS 与 Samba
---

# 文件共享：NFS 与 Samba

**本文你会学到**：

- NFS 的 RPC 机制与 NFSv3/v4/v4.1 的核心区别
- 如何配置 `/etc/exports`、挂载 NFS 共享、以及用 `autofs` 实现按需挂载
- NFS 的 UID/GID 映射陷阱与 Kerberos 认证简介
- Samba 的 SMB 协议原理与服务组件
- 如何用 `smb.conf` 配置公共共享与需认证共享
- Linux 和 Windows 客户端挂载 Samba 的方法
- NFS vs Samba 选型建议

---

## NFS 原理与版本演进

### 为什么需要 RPC？

NFS（Network FileSystem）让不同主机可以将远程目录挂载到本地，像使用本地磁盘一样透明访问。

NFS 服务本身监听固定的 `2049` 端口，但它依赖若干辅助进程（`rpc.mountd`、`rpc.lockd`、`rpc.statd`）——这些进程每次启动时会随机占用小于 1024 的端口。为了让客户端能找到正确端口，NFS 引入了 **RPC（Remote Procedure Call，远程过程调用）** 机制：

1. 所有 NFS 相关进程启动后主动向 RPC 服务（`rpcbind`，监听 `111` 端口）注册自己的端口号
2. 客户端先向服务器的 `111` 端口询问"我要用 mountd，它在哪个端口？"
3. `rpcbind` 回报正确端口，客户端再直接连接

!!! warning "重要顺序"

    必须先启动 `rpcbind`，再启动 `nfs`。若重启 `rpcbind`，所有已注册的 NFS 进程都需重新启动来重新注册。

### NFSv3 vs NFSv4 vs NFSv4.1

| 特性 | NFSv3 | NFSv4 | NFSv4.1（pNFS） |
|------|-------|-------|----------------|
| 所需端口 | 111（RPC） + 2049 + 随机端口 | **仅 2049** | **仅 2049** |
| 防火墙配置 | 复杂（需固定辅助端口） | 简单 | 简单 |
| 认证 | 基于 IP/UID | 支持 Kerberos（RPCSEC_GSS） | 支持 Kerberos |
| 有状态/无状态 | 无状态 | 有状态（支持文件锁） | 有状态 |
| 并行 I/O | ❌ | ❌ | ✅（多存储节点） |
| 推荐场景 | 旧系统兼容 | **现代标准推荐** | 高性能存储集群 |

!!! tip "现代系统默认 NFSv4"

    Linux 内核 3.0 以后客户端默认协商 NFSv4，无需额外配置。若服务器仅需支持 NFSv4，防火墙只需开放 `2049` 端口，大幅简化配置。

### NFS 服务组件

- `rpc.nfsd`：核心服务，处理客户端文件访问请求、UID 身份判断
- `rpc.mountd`：处理挂载请求，读取 `/etc/exports` 验证客户端权限
- `rpc.lockd`（可选）：文件锁定，防止多客户端同时写入冲突
- `rpc.statd`（可选）：文件一致性检查，与 `rpc.lockd` 配合使用

---

## NFS 服务端配置

### 安装软件包

=== "Debian/Ubuntu"

    ``` bash title="安装 NFS 服务端"
    apt install nfs-kernel-server
    systemctl enable --now nfs-server
    ```

=== "Red Hat/RHEL"

    ``` bash title="安装 NFS 服务端"
    dnf install nfs-utils
    systemctl enable --now nfs-server
    # rpcbind 通常作为依赖自动安装并启动
    ```

### /etc/exports 配置语法

每行格式为：`共享目录  客户端1(选项)  客户端2(选项)`

``` text title="/etc/exports 语法示例"
# 格式：目录  客户端(选项,选项,...)
/srv/data     192.168.1.0/24(rw,sync,no_subtree_check)
/srv/readonly *(ro,sync,all_squash)
/home/andy    192.168.1.10(rw)
```

客户端写法支持：

- 单个 IP：`192.168.1.10`
- 网段：`192.168.1.0/24`
- 主机名通配符：`*.example.com`
- 所有主机：`*`

**常用选项说明**：

| 选项 | 说明 |
|------|------|
| `rw` | 可读写（默认 `ro` 只读） |
| `sync` | 同步写入磁盘（数据安全，推荐） |
| `async` | 异步写入（性能更好，断电可能丢数据） |
| `root_squash` | 客户端 root 映射为匿名用户 `nobody`（**默认**，安全） |
| `no_root_squash` | 客户端 root 保持 root 权限（仅内部可信主机使用） |
| `all_squash` | 所有客户端用户均映射为匿名用户 |
| `anonuid=UID` | 指定匿名用户映射的 UID（配合 `all_squash` 使用） |
| `anongid=GID` | 指定匿名用户映射的 GID |
| `no_subtree_check` | 不检查子目录树（推荐，减少性能开销和挂载错误） |

!!! warning "空格陷阱"

    `客户端(选项)` 之间**不能有空格**。`192.168.1.0/24(rw)` 和 `192.168.1.0/24 (rw)` 含义完全不同——后者相当于对该网段设为只读，对 `(rw)` 设为任意主机可读写。

**典型配置示例**：

``` text title="/etc/exports 实战配置"
# 内网公共目录：可读写，root 压缩为匿名
/srv/shared     192.168.1.0/24(rw,sync,no_subtree_check)

# 只读公共资源：所有客户端，匿名访问
/srv/public     *(ro,sync,all_squash,no_subtree_check)

# 上传目录：强制映射为 UID=1050 的专用用户
/srv/upload     192.168.1.0/24(rw,sync,all_squash,anonuid=1050,anongid=1050,no_subtree_check)

# 仅授权单台主机（如备份服务器），保留 root 权限
/srv/backup     192.168.1.5(rw,sync,no_root_squash,no_subtree_check)
```

### 重载导出配置

修改 `/etc/exports` 后，**无需重启 NFS 服务**，使用 `exportfs` 重载即可：

``` bash title="重载 NFS 共享配置"
# 重载所有共享（常用）
exportfs -ra

# 查看当前导出列表
exportfs -v
```

### 防火墙配置

**NFSv4 只需开放一个端口**：

``` bash title="NFSv4 防火墙规则（firewalld）"
# 推荐：仅开放 NFSv4 所需端口
firewall-cmd --permanent --add-service=nfs
firewall-cmd --reload
```

若需兼容 NFSv3（同时需要 RPC 端口），可固定辅助端口后再配置防火墙：

``` bash title="NFSv3 固定辅助端口（/etc/nfs.conf 或 /etc/sysconfig/nfs）"
[mountd]
port = 20048

[lockd]
port = 32803
udp-port = 32769
```

``` bash title="NFSv3 防火墙规则"
firewall-cmd --permanent --add-service=nfs
firewall-cmd --permanent --add-service=rpc-bind
firewall-cmd --permanent --add-service=mountd
firewall-cmd --reload
```

---

## NFS 客户端挂载

### 手动挂载

``` bash title="挂载 NFS 共享"
# 先查询服务器提供的共享目录
showmount -e 192.168.1.100

# 创建挂载点并挂载（NFSv4 默认）
mkdir -p /mnt/nfs/shared
mount -t nfs 192.168.1.100:/srv/shared /mnt/nfs/shared

# 指定版本挂载（兼容 NFSv3 场景）
mount -t nfs -o vers=3 192.168.1.100:/srv/shared /mnt/nfs/shared

# 挂载时附加安全参数（推荐）
mount -t nfs -o rw,nosuid,noexec,nodev,soft 192.168.1.100:/srv/shared /mnt/nfs/shared
```

常用挂载参数：

| 参数 | 说明 |
|------|------|
| `nosuid` | 禁止挂载点上的 SUID 位生效（安全） |
| `noexec` | 禁止直接执行挂载点上的二进制文件（安全） |
| `nodev` | 禁止挂载点上的设备文件（安全） |
| `soft` | 网络超时后返回错误，不无限等待（推荐） |
| `hard` | 无限重试直到服务器恢复（适合关键业务） |
| `bg` | 挂载失败时转后台重试（开机脚本常用） |
| `rsize=32768,wsize=32768` | 增大读写缓冲块，提升局域网性能 |

### /etc/fstab 持久挂载

``` text title="/etc/fstab NFS 挂载条目"
# NFSv4（推荐）
192.168.1.100:/srv/shared  /mnt/nfs/shared  nfs4  rw,soft,nosuid,noexec,nodev,_netdev  0 0

# NFSv3 兼容写法
192.168.1.100:/srv/shared  /mnt/nfs/shared  nfs   rw,vers=3,soft,nosuid,_netdev  0 0
```

!!! tip "_netdev 参数"

    必须加 `_netdev` 选项，告知系统等网络就绪后再挂载，避免开机时因网络未启动导致挂载失败、系统卡死。

### autofs 按需自动挂载

`autofs` 在用户首次访问目录时才自动挂载，一段时间无访问后自动卸载，避免 NFS 服务器离线时客户端卡顿。

``` bash title="安装 autofs"
# Debian/Ubuntu
apt install autofs

# RHEL/CentOS
dnf install autofs
```

``` text title="/etc/auto.master 主配置"
# 格式：监控的父目录  对应的映射文件  [超时时间]
/mnt/nfs  /etc/auto.nfs  --timeout=300
```

``` text title="/etc/auto.nfs 映射文件"
# 格式：子目录名  [-挂载选项]  服务器:远程路径
shared  -rw,soft,nosuid  192.168.1.100:/srv/shared
public  -ro,soft         192.168.1.100:/srv/public
```

``` bash title="启动 autofs"
systemctl enable --now autofs

# 访问 /mnt/nfs/shared 时自动触发挂载
ls /mnt/nfs/shared
```

---

## NFS 权限与安全

### UID/GID 映射问题

NFS 不做独立的身份认证，**直接以客户端进程的 UID/GID 访问服务器文件系统**。这带来一个关键问题：

客户端 `alice`（UID=1001）访问 NFS 服务器时，服务器会以 UID=1001 来判断权限。如果服务器上 UID=1001 是另一个用户 `bob`，那么 `alice` 就拥有了 `bob` 的文件权限——这是严重的安全隐患。

**解决方案**：

- **保持 UID/GID 一致**：使用 LDAP 或 NIS 统一管理账号，确保客户端与服务器 UID 对应
- **使用 `all_squash`**：对公共共享目录，将所有用户强制映射为统一的匿名账号
- **NFSv4 + Kerberos**：基于身份名称而非 UID 认证，彻底解决映射混乱问题

### NFSv4 with Kerberos 简介

NFSv4 支持通过 `RPCSEC_GSS`（Kerberos）进行强认证，提供三种安全级别：

| 安全选项 | 挂载写法 | 说明 |
|---------|---------|------|
| `sec=sys` | 默认 | 基于 UID/GID，无加密 |
| `sec=krb5` | `mount -o sec=krb5` | Kerberos 身份认证，不加密 |
| `sec=krb5i` | `mount -o sec=krb5i` | 认证 + 完整性校验 |
| `sec=krb5p` | `mount -o sec=krb5p` | 认证 + 完整性 + 加密传输 |

Kerberos 需要额外部署 KDC（密钥分发中心），适合企业环境，详细配置参见 `nfs-utils` 和 `krb5` 文档。

---

## NFS 排查工具

``` bash title="常用排查命令"
# 查询服务器提供的共享目录列表
showmount -e 192.168.1.100

# 查看当前 NFS 连接状态（已挂载的客户端）
showmount -a 192.168.1.100

# 查询 RPC 服务注册情况（确认 NFS 各组件是否正常注册）
rpcinfo -p 192.168.1.100

# 查看 NFS 服务统计（请求次数、错误数等）
nfsstat -s    # 服务端统计
nfsstat -c    # 客户端统计
nfsstat -n    # NFS 详细统计

# 验证特定 RPC 程序是否响应
rpcinfo -t 192.168.1.100 nfs    # TCP 检查
rpcinfo -u 192.168.1.100 nfs    # UDP 检查
```

常见故障判断：

- `Connection refused`：`rpcbind` 未启动
- `Program not registered`：`rpcbind` 正常但 `nfs` 服务未启动，或重启 `rpcbind` 后 NFS 未重新注册
- `access denied by server`：客户端 IP 不在 `/etc/exports` 允许范围内
- 挂载后访问极慢或卡死：检查防火墙是否阻断了 NFS 所需端口

---

## Samba 原理

### 为什么需要 Samba？

NFS 是 Unix-Like 系统之间的文件共享协议，而 Windows 使用的是 **SMB/CIFS（Server Message Block / Common Internet File System）** 协议（即"网络上的芳邻"）。

Samba 是 Linux 上 SMB 协议的开源实现（由 Andrew Tridgell 在 1991 年通过逆向工程开发），使 Linux 能够直接加入 Windows 网络，实现跨平台文件共享。

### 服务组件

| 进程 | 协议 | 端口 | 职责 |
|------|------|------|------|
| `smbd` | TCP | 139, 445 | 文件/打印共享的核心服务，处理权限与访问控制 |
| `nmbd` | UDP | 137, 138 | NetBIOS 名称解析，工作组浏览支持 |
| `winbindd` | — | — | 与 Windows AD/域控对接，映射域账号到 Linux UID/GID |

### SMB 协议版本

| 版本 | 支持系统 | 状态 |
|------|---------|------|
| SMB1 | Windows XP 及更早 | ⚠️ **已弃用，存在严重安全漏洞（WannaCry 利用此协议）** |
| SMB2 | Windows Vista/Server 2008+ | ✅ 推荐（性能与安全大幅改善） |
| SMB3 | Windows 8/Server 2012+ | ✅ 推荐（支持端到端加密） |
| SMB3.1.1 | Windows 10/Server 2016+ | ✅ 当前最佳（支持 AES-128-GCM 加密） |

!!! danger "禁用 SMB1"

    现代 Samba 配置应在 `[global]` 中明确禁用 SMB1：`min protocol = SMB2`。SMB1 存在 EternalBlue 等严重漏洞，已被主流发行版默认禁用。

---

## Samba 服务端配置

### 安装软件包

=== "Debian/Ubuntu"

    ``` bash title="安装 Samba 服务端"
    apt install samba
    systemctl enable --now smbd nmbd
    ```

=== "Red Hat/RHEL"

    ``` bash title="安装 Samba 服务端"
    dnf install samba samba-common samba-client
    systemctl enable --now smb nmb
    ```

### smb.conf 配置文件结构

Samba 的所有配置集中在 `/etc/samba/smb.conf`，分为两类区块：

- `[global]`：服务器全局参数（工作组、认证方式、协议版本等）
- `[共享名称]`：每个共享目录的独立配置

``` text title="/etc/samba/smb.conf 基本结构"
[global]
    workgroup = WORKGROUP           # 工作组名称（与 Windows 保持一致）
    server string = Samba Server    # 服务器描述
    min protocol = SMB2             # 禁用 SMB1（安全要求）
    security = user                 # 认证方式：user（本地账号）
    passdb backend = tdbsam         # 密码数据库格式
    log file = /var/log/samba/log.%m
    max log size = 50

[homes]                             # 特殊：自动映射每个用户的家目录
    comment = Home Directories
    browseable = no                 # 不在列表中显示（仅用户自己可见）
    writable = yes
    create mode = 0664
    directory mode = 0775

[shared]                            # 自定义共享名称
    comment = Public shared folder
    path = /srv/samba/shared
    browseable = yes
    writable = yes
    valid users = @staff            # 只允许 staff 组访问
```

### 公共只读共享（无需密码）

``` text title="/etc/samba/smb.conf 公共只读共享"
[global]
    workgroup = WORKGROUP
    security = user
    map to guest = Bad User         # 未知用户映射为 Guest
    min protocol = SMB2

[public]
    comment = Public Read-Only Share
    path = /srv/samba/public
    browseable = yes
    read only = yes
    guest ok = yes                  # 允许匿名访问
```

``` bash title="创建目录并设置权限"
mkdir -p /srv/samba/public
chmod 755 /srv/samba/public
```

### 需认证的共享

``` text title="/etc/samba/smb.conf 需认证共享"
[global]
    workgroup = WORKGROUP
    security = user
    passdb backend = tdbsam
    min protocol = SMB2

[project]
    comment = Project directory (staff only)
    path = /srv/samba/project
    browseable = yes
    writable = yes
    valid users = @staff            # 只有 staff 组成员可访问
    write list = @staff             # 有写权限的用户/组
    create mask = 0664              # 新建文件权限
    directory mask = 0775           # 新建目录权限
    force group = staff             # 强制新建文件的所属组为 staff
```

``` bash title="创建目录与设置权限"
mkdir -p /srv/samba/project
groupadd staff
chgrp staff /srv/samba/project
chmod 2770 /srv/samba/project       # SGID：新文件继承 staff 组
```

### Samba 用户管理

Samba 用户**必须先是 Linux 系统用户**，但 Samba 有独立的密码数据库：

``` bash title="添加 Samba 用户"
# 步骤一：确保 Linux 账号存在
useradd -M -s /sbin/nologin alice   # -M 不创建家目录，-s 禁止 shell 登录
usermod -aG staff alice             # 加入 staff 组

# 步骤二：将用户添加到 Samba 密码库（会提示输入 Samba 密码）
smbpasswd -a alice

# 其他常用操作
smbpasswd alice          # 修改 alice 的 Samba 密码
smbpasswd -d alice       # 禁用账号
smbpasswd -e alice       # 启用账号
smbpasswd -x alice       # 删除账号

# 查看 Samba 用户列表
pdbedit -L
pdbedit -L -v            # 详细信息
```

!!! tip "Samba 密码与 Linux 密码独立"

    Samba 密码存储在 `/var/lib/samba/private/passdb.tdb`，与 `/etc/shadow` 中的 Linux 系统密码**互相独立**。修改 Linux 密码不会自动同步 Samba 密码，反之亦然。

### 验证配置语法

每次修改 `smb.conf` 后，务必用 `testparm` 检查语法：

``` bash title="检查 smb.conf 语法"
testparm
# 按 Enter 查看解析后的完整配置（含默认值）

testparm -v              # 显示所有参数（含未显式设置的默认值）

# 重启服务使配置生效
systemctl restart smbd nmbd
```

### 防火墙配置

``` bash title="Samba 防火墙规则"
# firewalld
firewall-cmd --permanent --add-service=samba
firewall-cmd --reload

# iptables（手动）
iptables -A INPUT -p tcp --dport 139 -j ACCEPT
iptables -A INPUT -p tcp --dport 445 -j ACCEPT
iptables -A INPUT -p udp --dport 137 -j ACCEPT
iptables -A INPUT -p udp --dport 138 -j ACCEPT
```

---

## Samba 客户端

### Linux 客户端：smbclient 浏览

`smbclient` 类似 FTP 命令行客户端，可浏览和交互式访问共享：

``` bash title="smbclient 常用操作"
# 列出服务器上的共享资源
smbclient -L //192.168.1.100 -U alice

# 匿名浏览（无需密码）
smbclient -L //192.168.1.100 -N

# 进入交互式会话（类 FTP 操作：ls、get、put、cd）
smbclient //192.168.1.100/project -U alice

# 常用交互命令
# ls          列出文件
# get 文件名  下载文件
# put 文件名  上传文件
# cd 目录     切换目录
# exit        退出
```

### Linux 客户端：mount.cifs 挂载

``` bash title="挂载 Samba 共享（临时）"
# 安装 cifs 支持
apt install cifs-utils   # Debian
dnf install cifs-utils   # RHEL

# 挂载
mkdir -p /mnt/samba/project
mount -t cifs //192.168.1.100/project /mnt/samba/project \
    -o username=alice,password=密码,uid=1000,gid=1000

# 不显示密码（推荐）
mount -t cifs //192.168.1.100/project /mnt/samba/project \
    -o credentials=/etc/samba/credentials
```

### /etc/fstab 持久挂载

使用 credentials 文件避免密码明文出现在 `fstab` 中：

``` bash title="创建 credentials 文件"
cat > /etc/samba/credentials << 'EOF'
username=alice
password=your_password
domain=WORKGROUP
EOF
chmod 600 /etc/samba/credentials
```

``` text title="/etc/fstab Samba 挂载条目"
//192.168.1.100/project  /mnt/samba/project  cifs  credentials=/etc/samba/credentials,uid=1000,gid=1000,_netdev,iocharset=utf8  0 0
```

### Windows 客户端

Windows 访问 Samba 共享无需安装额外软件：

- **文件资源管理器**：地址栏输入 `\\192.168.1.100\project`
- **映射网络驱动器**：右键"此电脑" → "映射网络驱动器" → 填入 `\\IP\共享名`
- **命令行**：`net use Z: \\192.168.1.100\project /user:alice 密码`

!!! tip "网络发现"

    若 Windows 无法浏览到 Samba 服务器，可直接输入 IP 路径访问。Windows 防火墙可能需要开放"文件和打印机共享"规则。

---

## Samba 权限模型

### 双层权限取最严

Samba 的权限受**两层**控制，最终权限取**二者中更严格的那个**：

```
实际权限 = min(Samba 共享权限, Linux 文件系统权限)
```

例如：`smb.conf` 中设置了 `writable = yes`，但 Linux 目录对该用户只有 `r-x` 权限——用户仍然**无法写入**。

**常见调试步骤**：

1. 检查 Linux 文件系统权限：`ls -la /srv/samba/project`
2. 检查 `smb.conf` 中的 `writable`、`valid users`、`write list`
3. 检查 SELinux（RHEL 系统）：`getsebool -a | grep samba`

### 常用权限调整参数

``` text title="smb.conf 权限相关参数"
[project]
    path = /srv/samba/project
    writable = yes
    valid users = @staff, alice     # 允许访问的用户或组（@ 前缀表示组）
    write list = @staff             # 有写权限的用户/组
    read list = bob                 # 仅读用户
    force user = smbuser            # 强制所有访问以 smbuser 身份操作
    force group = staff             # 强制新建文件的所属组
    create mask = 0664              # 新建文件最大权限掩码
    directory mask = 0775           # 新建目录最大权限掩码
```

### SELinux 注意事项（RHEL/CentOS）

在 RHEL 系统上，SELinux 会阻断 Samba 访问大多数目录，需要额外配置：

``` bash title="SELinux Samba 相关配置"
# 允许 Samba 访问用户家目录
setsebool -P samba_enable_home_dirs on

# 允许 Samba 读写自定义目录
setsebool -P samba_export_all_rw on

# 给自定义共享目录打上正确的 SELinux 类型
chcon -t samba_share_t /srv/samba/project
# 或永久设置（需要 semanage）
semanage fcontext -a -t samba_share_t "/srv/samba/project(/.*)?"
restorecon -Rv /srv/samba/project
```

---

## NFS vs Samba 选型建议

| 对比维度 | NFS | Samba |
|---------|-----|-------|
| **适用平台** | Linux/Unix 之间 | 跨 Linux 与 Windows |
| **性能** | ✅ 更高（无协议转换开销） | 一般（SMB2/3 已大幅改善） |
| **配置复杂度** | NFSv4 简单，NFSv3 复杂 | 适中 |
| **认证机制** | 基于 IP/UID（NFSv4+Kerberos 可选） | 基于账号密码（支持 AD 域集成） |
| **Windows 兼容** | ❌ Windows 需要额外 NFS 客户端 | ✅ 原生支持 |
| **用户权限隔离** | 依赖 UID 一致性（需 LDAP/NIS） | 独立密码库，用户管理方便 |
| **文件锁支持** | NFSv4 内置 | SMB2/3 内置 |
| **防火墙配置** | NFSv4 只需 2049 | 需要 139、445、137、138 |
| **典型场景** | Linux 集群、容器持久卷、HPC 计算节点 | 办公室文件服务器、Windows/Linux 混合环境 |

**选型建议**：

- 纯 Linux 环境（如 Kubernetes PV、科学计算集群）→ **选 NFSv4**
- 需要 Windows 客户端访问，或有 AD 域管理需求 → **选 Samba（SMB2/3）**
- 同时需要两类客户端 → 可以同时部署两个服务，共享同一目录

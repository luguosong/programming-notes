---
title: WSL 使用指南
---

# WSL 使用指南

**本文你会学到**：

- WSL 是什么，为什么 Windows 开发者需要它
- 如何安装 WSL 2 平台
- 发行版的完整生命周期管理：安装 → 启动 → 更新 → 备份 → 迁移 → 卸载
- Windows 与 Linux 之间的文件互访方式
- 网络配置、代理设置与端口转发
- 通过 `.wslconfig` 和 `wsl.conf` 调优 WSL 行为
- 开发环境集成技巧与常见问题排查

---

## 为什么需要 WSL？

做过 Linux 开发的人都知道，在 Windows 上搭建原生 Linux 开发环境曾经非常痛苦——双系统切换麻烦，虚拟机占用资源大，`Cygwin` 又不够原生。`WSL`（Windows Subsystem for Linux）就是微软给出的答案：**在 Windows 上直接运行一个真正的 Linux 环境**，不需要虚拟机，不需要双系统。

WSL 目前有两个版本：

| 特性 | WSL 1 | WSL 2 |
|------|-------|-------|
| 架构 | 系统调用翻译层 | 真正的 Linux 内核（Hyper-V 轻量虚拟机） |
| 兼容性 | 部分系统调用不支持 | 接近 100% Linux 兼容 |
| 文件性能 | 跨系统文件访问快 | Linux 内部文件 I/O 更快 |
| Docker 支持 | 不支持 | 原生支持 |
| 内存占用 | 较低 | 略高（动态分配） |

!!! tip "WSL 2 是当前推荐版本"

    除非有特殊原因（如在旧硬件上运行），否则应该始终使用 `WSL 2`。本文所有内容基于 `WSL 2`。

---

## 安装 WSL

### 系统要求

- Windows 10 版本 2004+（内部版本 19041 及更高）或 Windows 11
- 启用 `虚拟机平台` 和 `适用于 Linux 的 Windows 子系统` 两个可选功能
- BIOS 中已开启虚拟化（`Intel VT-x` / `AMD-V`）

### 一键安装

Windows 10 较新版本和 Windows 11 支持一条命令完成安装：

``` powershell
wsl --install
```

这条命令会自动完成以下操作：

1. 启用 `适用于 Linux 的 Windows 子系统` 功能
2. 启用 `虚拟机平台` 功能
3. 下载并安装 `WSL 2` Linux 内核
4. 将 `Ubuntu` 设置为默认发行版（可更改）
5. 安装完成后提示重启计算机

重启后会自动弹出 Ubuntu 的终端窗口，要求你设置用户名和密码。

### 手动安装（逐步骤）

如果 `--install` 不支持或你想手动控制过程：

``` powershell
# 步骤 1：启用 WSL 功能
dism.exe /online /enable-feature /featurename:Microsoft-Windows-Subsystem-Linux /all /norestart

# 步骤 2：启用虚拟机平台
dism.exe /online /enable-feature /featurename:VirtualMachinePlatform /all /norestart

# 步骤 3：重启计算机
Restart-Computer

# 步骤 4：下载并安装 WSL 2 Linux 内核更新包
# 从 https://aka.ms/wsl2kernel 下载并运行安装程序

# 步骤 5：将 WSL 2 设为默认版本
wsl --set-default-version 2
```

### 验证安装

``` bash
# 在 WSL 终端中执行
cat /etc/os-release    # 查看 Linux 发行版信息
uname -r               # 查看内核版本（应包含 microsoft 字样）
# 5.15.133.1-microsoft-standard-WSL2

# 确认 WSL 版本
wsl.exe --version
# WSL 版本： 2.x.x.x
# 内核版本： 5.15.x
# WSLg 版本： 1.x.x
```

---

## 发行版管理

WSL 支持同时安装多个 Linux 发行版，每种发行版独立运行、互不干扰。本节覆盖发行版的完整生命周期操作。

### 查看与安装发行版

**查看可安装的发行版**：

``` powershell
wsl --list --online
# 或简写
wsl -l -o
```

输出示例：

```
NAME                            FRIENDLY NAME
Ubuntu                          Ubuntu
Ubuntu-24.04                    Ubuntu 24.04 LTS
Debian                          Debian GNU/Linux
kali-linux                      Kali Linux Rolling
Arch                            Arch Linux
openSUSE-Tumbleweed             openSUSE Tumbleweed
openSUSE-Leap-15.6              openSUSE Leap 15.6
SUSE-Linux-Enterprise-15-SP5    SUSE Linux Enterprise 15 SP5
Ubuntu-22.04                    Ubuntu 22.04 LTS
Ubuntu-20.04                    Ubuntu 20.04 LTS
OracleLinux_9_1                 Oracle Linux 9.1
```

**安装指定发行版**：

``` powershell
# 安装 Ubuntu 24.04（最流行的选择，社区资源丰富）
wsl --install -d Ubuntu-24.04

# 安装 Debian（轻量稳定，适合服务器场景）
wsl --install -d Debian

# 安装 Arch（滚动更新，软件包最新）
wsl --install -d Arch

# 安装 Kali Linux（安全测试和渗透测试专用）
wsl --install -d kali-linux

# 安装但不立即启动
wsl --install -d Debian --no-launch
```

首次启动时，会提示设置 Linux 用户名和密码。这个用户自动拥有 `sudo` 权限。

也可以在 Microsoft Store 中搜索发行版名称（如 `Ubuntu 24.04`）直接安装，与命令行安装的完全一致。

**查看已安装的发行版**：

``` powershell
wsl --list --verbose
# 或简写
wsl -l -v

# 输出示例：
# NAME            STATE       VERSION
# * Ubuntu-24.04    Running     2
#   Debian          Stopped     2
#   Ubuntu-22.04    Stopped     1
```

各字段含义：

| 字段 | 说明 |
|------|------|
| NAME | 发行版名称 |
| STATE | 当前状态（Running / Stopped） |
| VERSION | WSL 版本（1 或 2） |
| `*` | 星号标记表示当前默认发行版 |

### 设置默认发行版

安装多个发行版后，裸执行 `wsl` 命令会进入默认发行版：

``` powershell
# 查看当前默认（带 * 号的）
wsl -l -v

# 将 Ubuntu-24.04 设为默认
wsl --set-default Ubuntu-24.04
```

### 启动与停止

``` powershell
# 进入默认发行版
wsl

# 进入指定发行版
wsl -d Ubuntu-24.04

# 以指定用户身份进入
wsl -u root

# 执行单条命令（不进入交互模式）
wsl ls -la /home
wsl -d Debian -- cat /etc/debian_version
```

也可以通过以下方式快速进入：

- Windows Terminal 下拉菜单中选择发行版配置文件
- 开始菜单中点击发行版图标
- 在文件资源管理器地址栏输入 `\\wsl$` 后回车

**停止运行中的发行版**：

``` bash
# 在 WSL 内退出当前会话
exit
# 或按 Ctrl+D
```

``` powershell
# 关闭指定发行版
wsl -t Ubuntu-24.04

# 关闭所有正在运行的发行版
wsl --shutdown

# 查看 WSL 运行状态
wsl --status
```

!!! warning "`wsl --shutdown` 会关闭所有发行版"

    如果你有多个发行版同时运行（如 Ubuntu 跑着服务、Debian 跑着编译），`--shutdown` 会全部停掉。需要精确控制时，用 `wsl -t <发行版名>` 只关闭目标实例。

### 版本转换（WSL 1 ↔ WSL 2）

某些场景下需要切换 WSL 版本（比如旧项目依赖 WSL 1 的文件系统行为）：

``` powershell
# 将某个发行版从 WSL 1 升级到 WSL 2
wsl --set-version Ubuntu-22.04 2
# 转换过程可能需要几分钟，取决于发行版中文件的大小

# 将某个发行版从 WSL 2 降级到 WSL 1
wsl --set-version Ubuntu-22.04 1

# 设置新安装的发行版默认使用 WSL 2
wsl --set-default-version 2
```

!!! warning "版本转换会短暂停机"

    转换过程中该发行版无法使用，正在运行的服务会被中断。建议先保存工作再操作。

### 更新与升级

WSL 的更新分三个层面：WSL 平台本身、发行版内的软件包、发行版的大版本。三者独立，需要分别操作。

**更新 WSL 平台**：

``` powershell
# 更新 WSL 到最新版本
wsl --update

# 检查是否有更新（不实际执行）
wsl --update --check

# 回滚到 WSL 的上一个版本
wsl --update --rollback
```

**更新发行版内的软件包**：

``` bash
# Ubuntu / Debian
sudo apt update && sudo apt upgrade -y

# Arch
sudo pacman -Syu

# Alpine
sudo apk update && sudo apk upgrade
```

**发行版大版本升级**（以 Ubuntu 22.04 → 24.04 为例）：

``` bash
# 1. 确认当前版本
lsb_release -a

# 2. 先将当前版本的所有包更新到最新
sudo apt update && sudo apt upgrade -y
sudo apt dist-upgrade -y

# 3. 确保有足够磁盘空间（建议至少 5GB）
df -h /

# 4. 确保升级工具已安装
sudo apt install update-manager-core -y

# 5. 执行升级（过程较长，需要交互确认）
sudo do-release-upgrade
```

升级过程中的交互提示：

- 是否继续升级 → 输入 `y`
- 是否替换修改过的配置文件 → 通常选 `N`（保留当前配置）
- 是否重启服务 → 选 `Yes`
- 升级完成后是否删除旧的包 → 选 `y`

``` bash
# 升级完成后：重启发行版（在 PowerShell 中执行）
# wsl -t Ubuntu-22.04

# 重新进入后确认版本
lsb_release -a

# 清理不再需要的旧包
sudo apt --purge autoremove -y
sudo apt clean
```

!!! warning "升级期间不要关闭终端"

    `do-release-upgrade` 过程可能持续 20-40 分钟，期间**绝对不能关闭终端窗口**。建议先用 `wsl --export` 备份再操作。WSL 中不需要重启系统，`wsl -t <发行版名>` 关闭再进入即可。

**其他发行版的大版本升级**：

``` bash
# Debian（如 11 → 12）：编辑 /etc/apt/sources.list，将 bullseye 替换为 bookworm
sudo sed -i 's/bullseye/bookworm/g' /etc/apt/sources.list
sudo apt update && sudo apt upgrade --without-new-pkgs -y
sudo apt full-upgrade -y
sudo apt --purge autoremove -y

# Arch（滚动更新，无需大版本升级）
sudo pacman -Syu
```

### 导出与导入

#### 导出发行版（备份）

将整个发行版导出为 `tar` 文件，用于备份、迁移或分享给他人：

``` powershell
# 导出为 tar 文件
wsl --export Ubuntu-24.04 D:\backup\ubuntu-2404.tar

# 导出为 tar.gz 压缩文件（节省空间）
wsl --export Ubuntu-24.04 D:\backup\ubuntu-2404.tar.gz
```

!!! tip "导出前先关闭发行版"

    虽然 WSL 支持在运行状态下导出，但建议先 `wsl -t Ubuntu-24.04` 关闭后再导出，确保文件一致性。

#### 导入发行版（恢复）

从 `tar` 备份文件导入为新的发行版实例：

``` powershell
# 基本语法：wsl --import <名称> <安装位置> <备份文件>
wsl --import MyUbuntu D:\WSL\MyUbuntu D:\backup\ubuntu-2404.tar

# 导入时指定 WSL 版本
wsl --import MyUbuntu D:\WSL\MyUbuntu D:\backup\ubuntu-2404.tar --version 2
```

各参数说明：

| 参数 | 说明 |
|------|------|
| `名称` | 新发行版的名称（自定义，不能与已有的重复） |
| `安装位置` | 虚拟磁盘文件（`ext4.vhdx`）的存放目录 |
| `备份文件` | 之前导出的 tar 或 tar.gz 文件路径 |

!!! warning "导入后默认用户为 root"

    通过 `--import` 导入的发行版，默认登录用户是 `root` 而不是原来的普通用户。需要手动恢复：

    ``` powershell
    # 方法 1：使用发行版自带的命令设置默认用户
    ubuntu2404.exe config --default-user username

    # 方法 2：在 /etc/wsl.conf 中添加
    # [user]
    # default=username
    ```

    方法 1 中的 `ubuntu2404.exe` 是发行版在 Windows 开始菜单中注册的可执行文件名，不同发行版名称不同（如 `debian.exe`、`kali.exe`）。

### 迁移到其他磁盘

WSL 默认将发行版安装在 C 盘。如果 C 盘空间紧张，可以迁移到其他磁盘：

``` powershell
# 方法 1：使用 --move 直接迁移（WSL 最新版本支持）
wsl --move Ubuntu-24.04 D:\WSL\Ubuntu-24.04

# 方法 2：导出 → 注销 → 导入（适用于旧版本 WSL）
wsl --export Ubuntu-24.04 D:\backup\ubuntu-2404.tar
wsl --unregister Ubuntu-24.04
wsl --import Ubuntu-24.04 D:\WSL\ D:\backup\ubuntu-2404.tar
ubuntu2404.exe config --default-user username    # 恢复默认用户
del D:\backup\ubuntu-2404.tar                    # 清理备份
```

### 注销（卸载）发行版

``` powershell
# 注销指定发行版——删除所有数据，不可恢复！
wsl --unregister OldDistro
```

!!! danger "`--unregister` 是不可逆操作"

    执行后该发行版的所有文件、配置、安装的软件都会被永久删除。如果有重要数据，务必先 `--export` 备份。

    卸载后如果想重新使用，需要再次 `wsl --install -d <名称>` 安装。

也可以通过 Windows 设置卸载：

1. 打开「设置」→「应用」→「已安装的应用」
2. 搜索发行版名称（如 `Ubuntu 24.04`）
3. 点击「卸载」

### 重命名发行版

WSL 没有直接的重命名命令，需要通过导出 → 导入的方式间接实现：

``` powershell
wsl --export OldName D:\temp\backup.tar
wsl --unregister OldName
wsl --import NewName D:\WSL\NewName D:\temp\backup.tar
del D:\temp\backup.tar
```

---

## 文件系统互访

WSL 最实用的特性之一是 `Windows` 和 `Linux` 文件系统可以互相访问。

### 从 WSL 访问 Windows 文件

Windows 的所有驱动器都自动挂载到 `/mnt/` 下：

``` bash
# 访问 C 盘
cd /mnt/c/
ls /mnt/c/Users/

# 访问 D 盘
ls /mnt/d/projects/

# 在 WSL 中直接操作 Windows 文件
cp /mnt/c/Users/luguosong/file.txt ~/linux-dir/
```

!!! warning "性能陷阱"

    在 `/mnt/c/` 等 Windows 挂载点下执行 `git` 操作、`npm install` 等大量文件 I/O 的任务时，性能会明显下降。**开发项目应放在 Linux 文件系统内**（如 `~/projects/`），只在需要与 Windows 交换文件时才访问 `/mnt/`。

### 从 Windows 访问 WSL 文件

Windows 可以通过 `\\wsl$` 路径访问 WSL 的文件系统：

``` powershell
# 在资源管理器地址栏输入
\\wsl$\Ubuntu-24.04\home\username

# 或在 PowerShell 中操作
explorer.exe \\wsl$\Ubuntu-24.04\home\username

# 用 Windows 程序打开 WSL 中的文件
notepad.exe \\wsl$\Ubuntu-24.04\home\username\script.sh
```

在 WSL 终端中也可以快速打开资源管理器：

``` bash
# 在当前目录打开 Windows 资源管理器
explorer.exe .

# 用 VS Code 打开当前目录
code .
```

### 文件权限映射

WSL 通过 `metadata` 挂载选项将 Linux 权限映射到 Windows 的 NTFS ACL：

``` bash
# 查看当前挂载选项
mount | grep drvfs
# C:\ on /mnt/c type 9p (rw,noatime,uid=1000,gid=1000,...)

# 在 /etc/wsl.conf 中配置自动挂载选项
# 见「配置 wsl.conf」部分
```

| Linux 权限 | Windows 属性 |
|-----------|-------------|
| 可读可写 | 普通文件 |
| 只读 | 只读属性 |
| 可执行 | 无特殊映射（通过 metadata 选项启用） |

---

## 网络配置

### 网络模式

WSL 2 默认使用 `NAT` 模式：WSL 实例运行在一个虚拟网络中，通过 `NAT` 与宿主机共享网络。

``` bash
# 查看 WSL 的 IP 地址（每次启动可能变化）
ip addr show eth0
# 172.x.x.x

# 查看宿主机（Windows）的 IP
cat /etc/resolv.conf | grep nameserver
# nameserver 172.x.x.1
```

!!! note "镜像网络模式（推荐）"

    Windows 11 22H2+ 支持镜像网络模式，让 WSL 直接使用与 Windows 相同的网络栈，省去端口转发的麻烦。配置方法见「配置 `.wslconfig`」部分。

### 端口转发

默认 NAT 模式下，WSL 内启动的服务在 Windows 上可以通过 `localhost` 直接访问（WSL 自动处理端口转发）：

``` bash
# 在 WSL 中启动一个 HTTP 服务
python3 -m http.server 8080
```

然后在 Windows 浏览器访问 `http://localhost:8080` 即可。

但**从外部设备**（同一局域网的其他机器）访问 WSL 中的服务时，需要手动配置端口转发：

``` powershell
# 获取 WSL 的 IP 地址
$wslIP = wsl -d Ubuntu-24.04 -- hostname -I

# 添加端口转发规则（将 Windows 的 8080 转发到 WSL）
netsh interface portproxy add v4tov4 `
    listenport=8080 `
    listenaddress=0.0.0.0 `
    connectport=8080 `
    connectaddress=$wslIP.Trim()

# 查看所有端口转发规则
netsh interface portproxy show all

# 删除端口转发规则
netsh interface portproxy delete v4tov4 `
    listenport=8080 `
    listenaddress=0.0.0.0
```

### 代理配置

WSL 2 在 NAT 模式下无法直接使用 `127.0.0.1` 访问 Windows 上运行的代理。需要获取宿主机 IP 后配置：

``` bash
# 在 ~/.bashrc 或 ~/.zshrc 中添加
export HOST_IP=$(cat /etc/resolv.conf | grep nameserver | awk '{print $2}')
export http_proxy="http://${HOST_IP}:7890"
export https_proxy="http://${HOST_IP}:7890"
export no_proxy="localhost,127.0.0.1,::1"
```

!!! tip "使用镜像网络模式后无需特殊代理配置"

    启用镜像网络模式后，WSL 和 Windows 共享网络栈，直接用 `127.0.0.1` 即可访问 Windows 代理。

---

## 配置参考

WSL 的配置分为两个文件，作用范围不同：

| 配置文件 | 位置 | 作用范围 |
|---------|------|---------|
| `.wslconfig` | Windows 用户目录（`C:\Users\用户名\.wslconfig`） | 所有发行版全局配置 |
| `wsl.conf` | Linux 发行版内（`/etc/wsl.conf`） | 单个发行版配置 |

### 配置 .wslconfig（全局）

`.wslconfig` 放在 Windows 用户目录下，修改后需要 `wsl --shutdown` 重启 WSL 才生效。

``` ini title="C:\Users\luguosong\.wslconfig"
[wsl2]
# 限制 WSL 虚拟机最大内存（默认占用宿主机一半）
memory=4GB

# 限制处理器核心数
processors=4

# 限制交换空间大小（swap）
swap=2GB

# 交换文件路径（Windows 路径）
# swapfile=D:\\WSL\\swap.vhdx

# 本地化地址（localhost 转发）
localhostForwarding=true

# 启用镜像网络模式（Windows 11 22H2+）
networkingMode=mirrored

# 启用 DNS 隧道（镜像网络模式下推荐）
dnsTunneling=true

# 启用自动代理（跟随 Windows 代理设置）
autoProxy=true

# 启用嵌套虚拟化（WSL 内再运行虚拟化）
nestedVirtualization=true
```

!!! warning "memory 默认值的问题"

    WSL 2 默认可以占用宿主机 `50%` 的内存，在某些配置下甚至会更多。如果你的电脑内存有限（8GB 或 16GB），强烈建议手动设置 `memory` 上限，否则 WSL 可能把系统内存吃光导致卡顿。

### 配置 wsl.conf（单发行版）

`wsl.conf` 在 Linux 发行版内的 `/etc/wsl.conf`，修改后在当前发行版中执行 `wsl -t <发行版名>` 重启生效。

``` ini title="/etc/wsl.conf"
[boot]
# 启用 systemd（Ubuntu 22.04+ 默认启用）
systemd=true

# 开机自动执行的命令
command="service docker start"

[interop]
# 允许从 WSL 调用 Windows 程序（如 explorer.exe、code.exe）
enabled=true
# 将 Windows PATH 追加到 WSL 的 PATH 中
appendWindowsPath=true

[automount]
# 自动挂载 Windows 驱动器
enabled=true
# 挂载根目录
root=/mnt/
# 文件系统权限映射选项
options="metadata,umask=22,fmask=11"

[network]
# 生成 /etc/resolv.conf
generateResolvConf=true
# 如果使用自定义 DNS，设为 false 并手动编辑 /etc/resolv.conf
# generateResolvConf=false

[user]
# 默认登录用户
default=username
```

### systemd 支持

现代 Ubuntu（22.04+）和 Debian 发行版默认已启用 `systemd`。如果你的发行版没有启用：

``` bash
# 检查 systemd 是否运行
ps -p 1 -o comm=
# 如果输出 systemd 则已启用
# 如果输出 init 则未启用

# 启用 systemd
sudo sed -i 's/\[boot\]/\[boot\]\nsystemd=true/' /etc/wsl.conf

# 重启发行版（在 PowerShell 中执行）
wsl -t Ubuntu-24.04
```

启用 `systemd` 后就可以使用 `systemctl` 管理服务了：

``` bash
# 启动 Docker
sudo systemctl start docker

# 查看 SSH 服务状态
sudo systemctl status ssh

# 设置服务开机自启
sudo systemctl enable docker
```

---

## 开发环境集成

### Windows Terminal

安装 WSL 发行版后，`Windows Terminal` 会自动检测并添加对应的配置文件。推荐自定义配置：

- 将 WSL 配置文件设为默认启动项
- 设置 Ubuntu 的配色方案（如 `One Half Dark`）
- 启用亚克力（毛玻璃）背景效果
- 调整字体为 `Nerd Font`（支持图标显示）

### VS Code 远程开发

`VS Code` 通过 `Remote - WSL` 扩展实现无缝远程开发：

``` bash
# 在 WSL 项目目录中直接打开
code ~/projects/my-app

# VS Code 会自动安装 WSL 端的服务器组件
# 之后所有编辑、终端、调试都在 WSL 内执行
```

!!! tip "注意区分打开方式"

    ``` bash
    # ✅ WSL 模式：终端、扩展、调试全部运行在 Linux 中
    code ~/projects/my-app

    # ❌ Windows 模式：只是通过网络路径访问 WSL 文件，性能差且扩展不兼容
    code /mnt/c/Users/...
    ```

    判断是否在 WSL 模式：VS Code 左下角会显示绿色标记 `WSL: Ubuntu-24.04`。

### Docker Desktop

`Docker Desktop` for Windows 原生支持 WSL 2 后端：

1. 安装 Docker Desktop
2. 进入 Settings → General → 勾选 `Use the WSL 2 based engine`
3. 在 Resources → WSL Integration 中选择要启用的发行版

配置完成后，在 WSL 终端中可以直接使用 `docker` 命令：

``` bash
docker run --rm hello-world
docker compose up -d
```

### Git 配置

WSL 和 Windows 的 Git 配置是独立的，需要分别设置：

``` bash
# 在 WSL 中配置 Git
git config --global user.name "你的名字"
git config --global user.email "your@email.com"

# 配置 SSH 密钥（如果还没有）
ssh-keygen -t ed25519 -C "your@email.com"
eval "$(ssh-agent -s)"
ssh-add ~/.ssh/id_ed25519

# 测试连接
ssh -T git@github.com
```

!!! tip "共享 Windows 的 Git 凭据"

    如果不想在 WSL 中重复配置凭据，可以使用 Windows 的凭据管理器：

    ``` bash
    git config --global credential.helper "/mnt/c/Program Files/Git/mingw64/bin/git-credential-manager.exe"
    ```

---

## 常见问题与技巧

### 内存占用过高

WSL 2 默认可能占用大量内存。在 `.wslconfig` 中限制：

``` ini
[wsl2]
memory=4GB
swap=1GB
```

修改后执行 `wsl --shutdown` 重启生效。

### 启动速度慢

- 检查 `.wslconfig` 中是否配置了过多资源
- 禁用不必要的 `systemd` 服务：`systemctl list-unit-files --state=enabled`
- 关闭不需要的 Windows 启动项

### 文件权限问题

Windows 分区上的文件在 WSL 中可能显示错误的权限：

``` bash
# 修复 /mnt/c 下所有文件的执行权限问题
sudo umount /mnt/c
sudo mount -t drvfs C: /mnt/c -o metadata,umask=22,fmask=11
```

永久修复：在 `/etc/wsl.conf` 中配置 `options="metadata,umask=22,fmask=11"`。

### 时间不同步

WSL 偶尔出现时间偏移（休眠唤醒后常见）：

``` bash
# 手动同步时间
sudo hwclock -s

# 或通过 ntp 同步
sudo ntpdate time.windows.com
```

### 快速重启 WSL

在 PowerShell 中创建一个快捷命令：

``` powershell
# 添加到 PowerShell 配置文件
function Restart-WSL {
    wsl --shutdown
    Start-Sleep -Seconds 2
    wsl
}
Set-Alias rwsl Restart-WSL
```

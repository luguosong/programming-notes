---
name: Linux 笔记重构与扩充设计
date: 2026-04-27
status: approved
---

# Linux 笔记重构与扩充设计

## 目标

基于四本参考书（Harley Hahn's Guide to Unix and Linux、鸟哥基础篇 4e、鸟哥服务器篇 3e、TLPI），对 `docs/topic/linux/` 进行目录重命名 + 内容扩充 + 重复修复。

## 约束

- 保留 8 章独立架构，不合并/降级章节
- 混合视角（系统管理员 + 开发者并重）
- 服务器内容全部保留并优化
- 遵循项目 CLAUDE.md 中的文档规范

## 目录变更

### 1. `vim/` → `shell/`

- 原因：`vim/` 下 6 个子页面中仅 1 个关于 vim 编辑器，其余 5 个（bash、redirection、regex、text-tools、shell-script）属于 Shell 和文本处理
- 操作：重命名目录，更新 zensical.toml nav 路径
- nav 标签：`Shell 与文本处理`
- `shell/index.md` 的 front matter title 改为 `Shell 与文本处理`

### 2. `tcp-ip/` + `ssh/` → `network/`

- 原因：`tcp-ip/` 和 `ssh/` 在导航中已合并为「网络与通信」，但磁盘上是两个独立目录；`ssh/` 目录名无法代表 DHCP/DNS/FTP 等服务器内容
- 操作：
  - 创建 `network/` 目录
  - 将 `tcp-ip/` 的 4 个页面移入 `network/` 根目录
  - 创建 `network/services/` 子目录
  - 将 `ssh/` 的 8 个服务器页面移入 `network/services/`
  - 删除原 `tcp-ip/` 和 `ssh/` 目录
- nav 标签：`网络与服务`
- `network/index.md` 的 front matter title 保持 `TCP/IP 网络基础`
- `network/services/index.md` 为新增概览页，front matter title 为 `服务器概览`

### 3. 不变的章节

- `intro/`（基础入门）
- `filesystem/`（存储与文件）
- `user-management/`（用户与权限）
- `process/`（进程与系统管理）
- `compile/`（软件安装）
- `linux-api/`（Linux 编程接口）

## 新增页面（4 个）

### `intro/hardware/index.md` — 计算机硬件基础

参考来源：鸟哥基础篇第 0 章、Harley Hahn 第 1 章

| H2 | H3 | 内容要点 |
|----|-----|---------|
| 为什么了解硬件？ | — | Linux 直接管理硬件，理解硬件有助于理解系统行为 |
| CPU 架构 | RISC vs CISC、x86_64 vs ARM、多核与超线程、`lscpu`、`/proc/cpuinfo` |
| 内存层次 | 寄存器 → L1/L2/L3 → 主存 → 磁盘、缓存行、TLB、`free`、`dmidecode` |
| 存储设备 | HDD vs SSD、NVMe vs SATA、分区与扇区、IOPS 与吞吐量、`lsblk`、`fdisk -l` |
| 主板与总线 | PCIe、DMA、IRQ、`lspci`、`lsusb` |
| 数据表示 | 二进制/八进制/十六进制、ASCII → Unicode → UTF-8、端序 |

目标深度：400-500 行

### `network/services/index.md` — 服务器概览

参考来源：鸟哥服务器篇前言

| H2 | H3 | 内容要点 |
|----|-----|---------|
| 服务器是什么？ | — | 守护进程概念、inetd vs standalone、systemd 管理 |
| 服务器分类 | 按协议层分类、按用途分类 |
| 通用配置模式 | — | 配置文件 `/etc/`、日志 `/var/log/`、端口与防火墙、启停命令 |
| 服务器安全清单 | — | 最小权限、chroot、TLS/SSL、SELinux/AppArmor、日志审计 |
| 服务器速查表 | — | 服务名、默认端口、配置文件、常用命令 |

目标深度：200-300 行

### `linux-api/memory/index.md` — 内存管理

参考来源：TLPI 第 7 章、第 50 章

| H2 | H3 | 内容要点 |
|----|-----|---------|
| 程序的内存布局 | — | Text → Data → BSS → Heap → Stack、`size`、`/proc/PID/maps` |
| 堆内存分配 | `malloc`/`free` 基础、`calloc`/`realloc`、分配失败处理、`alloca` |
| 底层分配机制 | `brk`/`sbrk`、`mmap` 匿名映射、glibc ptmalloc 概述 |
| 虚拟内存控制 | `mprotect`、`mlock`/`mlockall`、`madvise`、`mincore` |
| 内存映射 | `mmap` 文件映射 vs 匿名映射、`munmap`、共享 vs 私有映射 |
| 实用工具 | `pmap`、`vmstat`、`smem`、`top` 内存列、OOM Killer |

目标深度：500-600 行

### `linux-api/timers/index.md` — 定时器与休眠

参考来源：TLPI 第 23 章

| H2 | H3 | 内容要点 |
|----|-----|---------|
| 为什么需要定时器？ | — | 超时控制、周期任务、性能测量 |
| 时间基础 API | — | `time()`、`gettimeofday()`、`clock_gettime()`、CLOCK 类型对比 |
| 休眠函数 | — | `sleep()`/`usleep()`/`nanosleep()`、信号中断与重启 |
| alarm 定时器 | — | `alarm()` + SIGALRM、限制 |
| interval 定时器 | — | `setitimer()`/`getitimer()`、三种模式 |
| POSIX 定时器 | — | `timer_create`/`timer_settime`/`timer_delete`、信号通知 vs 线程通知 |
| timerfd | — | `timerfd_create`/`timerfd_settime`、与 epoll 配合 |
| 选型指南 | — | API 对比表 |

目标深度：400-500 行

## 现有页面修改

### 修复项

| 文件 | 问题 | 修改方案 |
|------|------|---------|
| `intro/getting-started/index.md` | 570 行后「常用系统信息命令」和 720 行后「用户状态查询」与前面内容重复 | 删除重复段落 |
| `intro/file-content/index.md` | 513 行起「文本分页和翻阅补充」重复了 less/more 内容 | 合并到对应段落，删除重复 H2 |
| `tcp-ip/network-security/index.md` | 346 行起 SELinux/AppArmor 内容与 `process/selinux/` 重叠 | 改为简短引用，删除重复详述 |

### 内容扩充

| 文件 | 扩充内容 | 参考来源 |
|------|---------|---------|
| `linux-api/index.md` | 补充「内存分配概览」和「定时器概览」两个 H3，链接到新增子页面 | TLPI |
| `linux-api/file-io-adv/index.md` | 补充 `O_DIRECT` 直接 I/O 完整说明、`setvbuf`/`fflush` 详解 | TLPI 第 13 章 |
| `linux-api/process-advanced/index.md` | 补充 `#!` 解释器脚本机制细节、`execve` 对脚本的处理流程 | TLPI 第 28 章 |
| `process/index.md` | 补充进程调度策略（`SCHED_FIFO`/`SCHED_RR`/`SCHED_OTHER`）和 `sched_setscheduler` | TLPI 第 35 章 |

### 结构微调

- 所有包含「发行版差异」段落的文件：统一为 `???+ info "发行版差异"` 折叠块格式
- 所有内部交叉引用：更新路径（`vim/` → `shell/`、`tcp-ip/` → `network/`、`ssh/` → `network/services/`）

## zensical.toml nav 变更

nav 中 Linux 部分的路径变更：

| 原路径 | 新路径 |
|--------|--------|
| `topic/linux/vim/...` | `topic/linux/shell/...` |
| `topic/linux/tcp-ip/...` | `topic/linux/network/...` |
| `topic/linux/ssh/...` | `topic/linux/network/services/...` |

新增条目：

```toml
{ "计算机硬件基础" = "topic/linux/intro/hardware/index.md" }
{ "服务器概览" = "topic/linux/network/services/index.md" }
{ "内存管理" = "topic/linux/linux-api/memory/index.md" }
{ "定时器与休眠" = "topic/linux/linux-api/timers/index.md" }
```

## 实施顺序

1. 目录重命名与迁移（`vim/` → `shell/`，`tcp-ip/` + `ssh/` → `network/`）
2. 更新 `zensical.toml` nav 路径
3. 更新所有文件中的内部交叉引用
4. 修复重复内容
5. 编写新增页面（hardware、services/index、memory、timers）
6. 扩充现有页面（linux-api/index、file-io-adv、process-advanced、process/index）
7. 统一发行版差异格式
8. 更新 front matter title（shell/index.md）
9. 验证站点构建和导航

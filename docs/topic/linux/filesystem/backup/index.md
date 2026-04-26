---
title: 备份策略
---

# 备份策略

**本文你会学到**：

- 3-2-1 备份原则与三种备份类型的区别
- 用 `tar` 实现全量与增量备份
- 用 `rsync` 实现高效增量同步备份
- 利用 LVM 快照做无停机一致性备份
- XFS 专用工具 `xfsdump` / `xfsrestore` 的用法
- 用 cron 实现定时自动化备份

## 为什么备份比你想的难

硬盘随时可能坏，误操作随时可能发生，勒索软件随时可能加密你的数据。大多数人直到数据真正丢失，才意识到"我应该早点备份的"。

但备份本身也有坑：只备份了一份、备份文件和原始文件在同一块硬盘上、备份了但从没测试过能否恢复——这些都是"伪备份"，关键时刻靠不住。

本文帮你建立一套**真正能恢复数据**的备份策略。

## 备份原则与策略选择

### 3-2-1 原则

业界公认的最低安全基线：

- **3** 份数据副本（1 份生产 + 2 份备份）
- **2** 种不同存储介质（如本地硬盘 + 云存储）
- **1** 份异地存储（防止机房灾难导致所有副本同时损毁）

### 三种备份类型

```mermaid
graph TD
    subgraph full["全量备份（每次都完整）"]
        direction LR
        D0["周日\n完整数据"] --> D1["周一\n完整数据"] --> D2["周二\n完整数据"]
    end

    subgraph incr["增量备份（只记录上次以来的变化）"]
        direction LR
        I0["周日\n完整(L0)"] --> I1["周一\n仅变化(L1)"] --> I2["周二\n仅L1变化(L2)"]
    end

    subgraph diff["差异备份（每次与全量对比）"]
        direction LR
        F0["周日\n完整(L0)"] --> F1["周一\n与L0差异"] --> F2["周二\n与L0差异"]
    end

    classDef full fill:transparent,stroke:#0288d1,color:#adbac7,stroke-width:2px
    classDef incr fill:transparent,stroke:#388e3c,color:#adbac7,stroke-width:2px
    classDef diff fill:transparent,stroke:#f57c00,color:#adbac7,stroke-width:2px
    class D0,D1,D2 full
    class I0,I1,I2 incr
    class F0,F1,F2 diff
```

| 类型 | 备份速度 | 占用空间 | 恢复速度 | 典型工具 |
|------|---------|---------|---------|---------|
| 全量备份 | 慢 | 大 | 最快（单文件） | `tar`、`rsync` |
| 增量备份 | 最快 | 最小 | 慢（需按顺序叠加）| `xfsdump`、`tar --listed-incremental` |
| 差异备份 | 中 | 中 | 快（全量 + 最新差异）| `tar --newer`、`rsync` |

### 备份什么，不备份什么

**需要备份**：

- `⚙️ /etc/` — 系统所有配置文件，恢复系统的关键
- `🏠 /home/` — 用户数据，最不可替代
- 服务数据 — 数据库文件、应用数据（路径因服务而异）
- `/var/log/`（可选）— 日志，用于故障溯源

**不需要备份**（动态虚拟文件系统，重启后自动重建）：

- `/proc/`、`/sys/` — 内核运行时信息
- `/tmp/`、`/run/` — 临时文件
- `/dev/` — 设备文件

!!! tip "SELinux 安全上下文提示"

    用 `tar` 备份 `/etc/` 后在另一台机器恢复，可能因 SELinux 类型标签改变而导致无法登录（如 `/etc/shadow` 权限异常）。恢复后立即执行：

    ``` bash
    restorecon -Rv /etc
    ```

    或者在重启前创建 `/.autorelabel` 文件，系统下次启动时会自动修复所有 SELinux 标签。

## tar 备份

`tar` 是最通用的 Linux 备份工具，跨平台、无需额外安装，适合做周期性归档备份。

### 全量备份

``` bash title="全量备份整个系统（排除虚拟文件系统）"
tar -czf /backup/full_$(date +%Y%m%d).tar.gz \
    --exclude=/proc \
    --exclude=/sys \
    --exclude=/tmp \
    --exclude=/dev \
    --exclude=/run \
    --exclude=/backup \
    /
```

``` bash title="仅备份关键目录"
tar -czf /backup/etc_$(date +%Y%m%d).tar.gz /etc/
tar -czf /backup/home_$(date +%Y%m%d).tar.gz /home/
```

### 增量备份

`tar` 提供两种增量备份方式：

**方式一：`--newer` 时间戳**（简单，但时间精度有限）

``` bash title="基于时间戳的增量备份"
# 备份自上次备份以来新增/修改的文件
tar -czf /backup/incr_$(date +%Y%m%d).tar.gz \
    --newer /backup/last_backup_time \
    /home /etc

# 备份完成后更新时间戳
touch /backup/last_backup_time
```

**方式二：`--listed-incremental`**（GNU tar 推荐，记录完整文件状态）

``` bash title="GNU tar 级联增量备份"
# 首次：全量备份（自动创建 snapshot.snar 快照文件）
tar -czf /backup/full.tar.gz \
    --listed-incremental=/backup/snapshot.snar \
    /home

# 次日：增量备份（snapshot.snar 自动更新）
tar -czf /backup/incr1.tar.gz \
    --listed-incremental=/backup/snapshot.snar \
    /home
```

### 恢复

``` bash title="从全量 + 增量备份中恢复"
# 先恢复全量
tar -xzf /backup/full.tar.gz -C /restore/

# 再依次叠加增量
tar -xzf /backup/incr1.tar.gz -C /restore/
tar -xzf /backup/incr2.tar.gz -C /restore/
```

### 验证归档完整性

``` bash
# -t 列出内容但不解压，输出无报错则归档完好
tar -tzf backup.tar.gz > /dev/null && echo "归档完整" || echo "归档损坏！"
```

## rsync 同步备份

`rsync` 是 Linux 最常用的备份工具。它的核心优势是**增量传输**——每次只传输变化的部分，速度快、带宽占用低，并且完整保留文件权限、时间戳和符号链接。

### 基础用法

``` bash title="本地目录同步"
rsync -avz --delete /home/ /backup/home/
```

`rsync` 常用参数速查：

| 参数 | 含义 |
|------|------|
| `-a` | 归档模式：递归 + 保留权限/时间/链接/所有者 |
| `-v` | 详细输出，显示传输的每个文件 |
| `-z` | 压缩传输，适合网络带宽有限的场景 |
| `-P` | 等同于 `--partial --progress`，断点续传 + 进度条 |
| `--delete` | 删除目标中源已删除的文件，保持同步 |
| `--exclude` | 排除匹配的文件或目录 |
| `--bwlimit` | 限制传输速度（单位 KB/s） |
| `-n` | 模拟运行，不实际操作，用于预览变更 |
| `--checksum` | 用校验和而非时间戳判断是否变化（更精确但更慢） |

### 远程备份（SSH）

``` bash title="通过 SSH 同步到远程备份服务器"
rsync -avz -e ssh /home/ user@backup-server:/backup/home/

# 排除缓存和临时文件
rsync -avz --exclude='*.tmp' --exclude='.cache' \
    /home/ user@backup-server:/backup/home/

# 限速 1 MB/s，避免占满带宽
rsync -avz --bwlimit=1000 /data/ backup-server:/backup/data/
```

### 模拟运行

``` bash title="用 -n 预览 rsync 会做哪些操作"
# 加 -n 后只打印计划操作，不实际执行
rsync -avzn --delete /home/ /backup/home/
```

!!! warning "注意路径末尾的斜杠"

    `rsync` 中路径末尾的 `/` 有重要区别：

    - `/home/`（有斜杠）：同步 `/home/` **目录内的内容** 到目标
    - `/home`（无斜杠）：将 `/home` **目录本身**（含目录名）同步到目标

    大多数场景下，源路径加 `/`、目标路径加 `/` 是最直观的写法。

## LVM 快照备份

`tar` 和 `rsync` 备份都有一个问题：备份过程中文件可能正在被写入，导致备份数据不一致（例如数据库文件在写到一半时被复制）。

**LVM 快照**解决了这个问题——在某个瞬间为整个逻辑卷创建"时间点副本"，备份快照中的数据，而系统继续正常运行。

``` bash title="LVM 快照备份完整流程"
# 1. 为目标逻辑卷创建 5 GB 快照
lvcreate -L 5G -s -n snap_home /dev/vg_data/lv_home

# 2. 以只读方式挂载快照
mount -o ro /dev/vg_data/snap_home /mnt/snap

# 3. 用 rsync 备份快照数据（此时源数据是静态的）
rsync -avz /mnt/snap/ /backup/home/

# 4. 备份完成后卸载并删除快照
umount /mnt/snap
lvremove -f /dev/vg_data/snap_home
```

!!! tip "快照空间分配建议"

    快照的写时复制（CoW）机制：原始卷每写入一个数据块，快照就保存该块的旧版本。如果快照空间耗尽，快照会失效。

    - 备份窗口较短（< 1 小时）：分配原始卷大小的 10%~20%
    - 写入频率高或备份时间长：分配 30%~50%
    - 可用 `lvs` 命令实时查看快照空间使用率

## dump / restore（ext4 专用）

`dump` 是专门为 ext2/ext3/ext4 文件系统设计的备份工具，支持 0~9 共 10 个备份级别（0 为全量，1~9 为增量）。

!!! warning "仅支持 ext 文件系统"

    `dump` 不支持 XFS、Btrfs 等文件系统。CentOS 7+ / RHEL 7+ 默认使用 XFS，请改用下文的 `xfsdump`。

``` bash title="dump / restore 基础用法"
# 0 级全量备份
dump -0 -f /backup/home.dump /home

# 1 级增量备份（仅备份与 level 0 的差异）
dump -1 -f /backup/home_incr.dump /home

# 恢复（在目标目录中执行）
cd /restore && restore -rf /backup/home.dump
```

## xfsdump / xfsrestore（XFS 专用）

XFS 文件系统不兼容 `dump`，需要使用专门的 `xfsdump`。它同样支持 0~9 级别的增量备份，并且将备份元数据记录在 `/var/lib/xfsdump/inventory/` 中，方便追踪备份历史。

`xfsdump` 有几个重要限制：

- 只能备份**已挂载**的 XFS 文件系统（不支持卸载状态备份）
- 只能备份**整个文件系统**，不支持单独备份某个目录（如不能直接备份 `/etc`，因为它不是独立挂载的文件系统）
- 备份文件只能用 `xfsrestore` 恢复
- 需要 root 权限

### 全量备份

``` bash title="xfsdump 全量备份（level 0）"
# -l 0  指定 level 0（全量）
# -L    给本次备份会话打标签（方便后续恢复时识别）
# -M    给存储介质打标签
# -f    指定备份输出文件
xfsdump -l 0 -L home_full -M disk1 -f /backup/home.dump /home
```

### 增量备份

``` bash title="xfsdump 增量备份（level 1）"
# level 1 只备份与 level 0 相比的变化
xfsdump -l 1 -L home_incr1 -M disk1 -f /backup/home_incr1.dump /home
```

### 查看备份历史

``` bash title="查询 xfsdump 备份记录"
# -I 从 /var/lib/xfsdump/inventory 读取所有备份信息
xfsdump -I
# 或
xfsrestore -I
```

输出示例（已简化）：

```
file system 0:
  fs id: 94ac5f77-cb8a-495e-a65b-2ef7442b837c
  session 0:
    mount point: /home
    time: 2024-01-01 02:00:00
    session label: "home_full"
    level: 0
    pathname: /backup/home.dump
  session 1:
    session label: "home_incr1"
    level: 1
    pathname: /backup/home_incr1.dump
```

### 恢复

``` bash title="xfsrestore 恢复"
# 全量恢复（-L 指定 session label）
xfsrestore -f /backup/home.dump -L home_full /restore/

# 先恢复全量，再叠加增量
xfsrestore -f /backup/home.dump -L home_full /restore/
xfsrestore -f /backup/home_incr1.dump /restore/

# 只恢复备份中的某个子目录
xfsrestore -f /backup/home.dump -L home_full -s alice /restore/

# 进入交互模式（适合不确定要恢复哪些文件时）
xfsrestore -f /backup/home.dump -i /restore/
```

!!! tip "覆盖恢复 vs 全新恢复的区别"

    恢复到**已有数据的目录**时，同名文件会被覆盖，但目标目录中**原有的、备份中不存在的文件会被保留**。如果想要精确还原到备份时的状态，应恢复到空目录，再覆盖到目标位置。

## 自动化备份

手动备份总会有忘记的时候，用 `cron` + 脚本实现全自动定时备份才是生产环境的正确姿势。

``` bash title="cron 自动备份配置示例（/etc/cron.d/backup）"
# 每天凌晨 2:00 将 /home 同步到备份服务器
0 2 * * * root rsync -avz --delete /home/ backup-server:/backup/home/ \
    >> /var/log/backup.log 2>&1

# 每周日凌晨 3:00 归档备份 /etc
0 3 * * 0 root tar -czf /backup/etc_$(date +\%Y\%m\%d).tar.gz /etc/ \
    >> /var/log/backup.log 2>&1
```

!!! warning "cron 中 % 需要转义"

    在 crontab 文件中，`%` 是特殊字符（表示换行），必须写成 `\%` 才能传递给 shell 命令。例如 `date +\%Y\%m\%d`。

## 备份验证

备份从不测试等于没有备份——你需要**定期演练恢复**，而不是等到真正出事时才发现备份损坏或流程不对。

``` bash title="备份完整性验证"
# 验证 tar 归档：-t 列出内容，无报错说明归档可读
tar -tzf /backup/home_20240101.tar.gz > /dev/null \
    && echo "✅ 归档完整" \
    || echo "❌ 归档损坏！"

# 验证 xfsdump 备份：查询 inventory
xfsdump -I | grep -A3 "home_full"

# 模拟恢复测试：先用 -n 预演
rsync -avzn backup-server:/backup/home/ /mnt/test-restore/
```

!!! tip "定期恢复演练"

    建议每月至少一次将备份恢复到**隔离的测试环境**（如虚拟机），完整走一遍恢复流程并验证数据完整性。真正的灾难恢复能力，只有在演练中才能检验出来。

## 发行版工具差异

=== "Debian / Ubuntu"

    ``` bash
    # 安装备份工具
    apt install rsync       # 通常预装
    apt install dump        # ext4 专用的 dump/restore
    apt install xfsdump     # XFS 专用备份工具
    ```

    推荐组合：`rsync`（日常增量同步）+ `tar`（周期性归档）。`dump`/`restore` 在 Debian/Ubuntu 上仍可正常使用，适合 ext4 文件系统环境。

=== "Red Hat / RHEL / CentOS"

    ``` bash
    # 安装备份工具
    dnf install rsync
    dnf install xfsdump     # XFS 默认文件系统，优先使用

    # 系统级备份恢复工具（RHEL 官方推荐）
    dnf install rear        # Relax-and-Recover，支持裸机恢复
    ```

    RHEL 7+ 默认文件系统为 XFS，`dump` 不再适用，应优先使用 `xfsdump`。`rear`（Relax-and-Recover）是 RHEL 官方推荐的全系统备份恢复解决方案，支持将系统恢复到不同硬件（裸机恢复）。


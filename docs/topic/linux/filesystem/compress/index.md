---
title: 压缩与归档
---

# 压缩与归档

文件在磁盘中存储时往往包含大量"空余空间"——压缩技术通过统计编码将这些冗余消除，使文件体积显著缩小，从而节省存储空间、加快网络传输。Linux 提供了多种压缩工具，各有侧重；而 `tar` 则扮演"打包员"角色，将多文件/目录捆绑成一个归档，再配合压缩工具一同处理。

**本文你会学到**：

- 常见压缩格式的特点与适用场景
- `gzip`、`bzip2`、`xz`、`zstd` 的基本用法
- `tar` 归档的创建、解压、查看与高级技巧
- `zip`/`unzip` 跨平台场景
- 管道流式压缩等实用技巧

## 常见压缩格式对比

| 格式 | 工具 | 压缩率 | 速度 | 典型场景 |
|------|------|--------|------|---------|
| `.gz` | gzip / gunzip | 中 | 快 | 日志压缩、通用场景 |
| `.bz2` | bzip2 / bunzip2 | 高 | 慢 | 软件发布包 |
| `.xz` | xz / unxz | 最高 | 最慢 | Linux 内核、大型软件包 |
| `.zst` | zstd / unzstd | 高 | 极快 | 现代发行版包管理（RHEL 9、Debian 12+） |
| `.lz4` | lz4 | 低 | 极快 | 实时压缩、日志流 |
| `.zip` | zip / unzip | 中 | 中 | 跨平台（与 Windows 交换文件） |

!!! tip "压缩率与时间的权衡"

    以同一个 `services` 文件（670 KB）为例，三种工具的压缩时间分别约为：`gzip` 0.019 s、`bzip2` 0.042 s、`xz` 0.261 s，时间差了 10 倍，但压缩后体积依次更小。需要根据场景在"压缩率"与"时间成本"之间取舍。

## gzip

`gzip` 是 Linux 使用最广泛的压缩工具，由 GNU 计划开发，用于替代老旧的 `compress`。

### 基本用法

``` bash title="gzip 常用操作"
# 压缩文件（原文件被替换为 .gz）
gzip services

# 压缩并保留原文件（-k keep）
gzip -k services

# 解压缩（两种写法等价）
gunzip services.gz
gzip -d services.gz

# 查看压缩比信息（-v verbose）
gzip -v services
# 输出示例：services: 79.7% -- replaced with services.gz

# 指定压缩等级（-1 最快 / -9 最佳，默认 -6）
gzip -9 services

# 输出到标准输出，不修改原文件（配合重定向保留原文件）
gzip -c services > services.gz

# 验证压缩文件完整性
gzip -t services.gz
```

### 不解压直接读取

`zcat`、`zmore`、`zless`、`zgrep` 是对应的"透明读取"工具，使用方式与 `cat`/`more`/`less`/`grep` 完全相同，无需先解压：

``` bash title="透明读取压缩文件"
# 查看内容
zcat services.gz

# 在压缩文件中搜索关键字（-n 显示行号）
zgrep -n 'http' services.gz
```

## bzip2

`bzip2` 为替代 `gzip` 而生，压缩率更高，但耗时更长。选项与 `gzip` 几乎完全相同，文件扩展名由 `.gz` 变为 `.bz2`。

``` bash title="bzip2 常用操作"
# 压缩（原文件被替换）
bzip2 services

# 压缩并保留原文件
bzip2 -k services

# 解压缩
bunzip2 services.bz2
bzip2 -d services.bz2

# 显示压缩比信息
bzip2 -v services

# 查看内容（不解压）
bzcat services.bz2

# 输出到标准输出
bzip2 -c services > services.bz2
```

## xz

`xz` 拥有目前三种主流工具中最高的压缩率，代价是更长的运算时间，常用于 Linux 内核源码包等对体积要求极高的场景。

``` bash title="xz 常用操作"
# 压缩（原文件被替换）
xz services

# 压缩并保留原文件
xz -k services

# 解压缩
unxz services.xz
xz -d services.xz

# 查看压缩文件信息（压缩前后大小、压缩比）
xz -l services.xz
# 输出示例：97.3 KiB / 654.6 KiB = 0.149

# 多线程压缩（-T 0 自动使用所有 CPU 核心）
xz -T 0 large-file

# 查看内容（不解压）
xzcat services.xz
```

!!! note "xz 多线程"

    `xz -T 0` 会自动检测 CPU 核心数并启用并行压缩，可显著缩短大文件的压缩时间。

## zstd（现代首选）

`zstd`（Zstandard）由 Facebook 开发，兼顾高压缩率与极快速度，已成为 RHEL 9、Debian 12+ 等现代发行版包管理的首选格式。

``` bash title="zstd 常用操作"
# 压缩（生成 .zst 文件，保留原文件）
zstd file

# 解压缩（两种写法等价）
zstd -d file.zst
unzstd file.zst

# 查看压缩文件信息
zstd -l file.zst

# 多线程压缩
zstd -T0 large-file

# 指定压缩等级（1 最快 ~ 19 最佳，默认 3）
zstd -19 file
```

## tar 归档

`tar` 本身只做**打包**（将多个文件/目录合并成一个文件），并不压缩。通过配合 `-z`、`-j`、`-J`、`--zstd` 等选项，才会调用相应的压缩工具一并处理。

纯打包文件称为 **tarfile**（`*.tar`），打包并压缩后的文件称为 **tarball**（如 `*.tar.gz`、`*.tar.xz`）。

### 常用参数速查

| 参数 | 含义 |
|------|------|
| `-c` | 创建归档（Create） |
| `-x` | 解包/解压（eXtract） |
| `-t` | 列出归档内容（lisT） |
| `-v` | 显示详细过程（Verbose） |
| `-f` | 指定归档文件名（File），必须紧跟文件名 |
| `-z` | 通过 gzip 压缩/解压（`.tar.gz`） |
| `-j` | 通过 bzip2 压缩/解压（`.tar.bz2`） |
| `-J` | 通过 xz 压缩/解压（`.tar.xz`） |
| `--zstd` | 通过 zstd 压缩/解压（`.tar.zst`） |
| `-C` | 解压到指定目录（Change directory） |
| `-p` | 保留文件原始权限与属性（备份时常用） |
| `-P` | 保留绝对路径（⚠️ 慎用，还原时会覆盖系统文件） |
| `--exclude` | 排除指定文件或目录 |
| `--newer-mtime` | 仅打包比指定时间更新的文件（增量备份） |

!!! warning "-c / -x / -t 三者互斥"

    `-c`（创建）、`-x`（解压）、`-t`（列出）不能同时出现在同一条命令中，三选其一。

### 创建归档

``` bash title="创建压缩归档"
# gz 压缩
tar -czf archive.tar.gz dir/

# bz2 压缩（压缩率更高，速度更慢）
tar -cjf archive.tar.bz2 dir/

# xz 压缩（压缩率最高）
tar -cJf archive.tar.xz dir/

# zstd 压缩（速度与压缩率均衡）
tar --zstd -cf archive.tar.zst dir/

# 保留文件权限（备份配置文件时推荐）
tar -czpf etc-backup.tar.gz /etc

# 排除特定文件/目录
tar -czf backup.tar.gz /home/ --exclude=/home/user/cache --exclude=/home/user/.npm
```

### 解压归档

``` bash title="解压归档"
# 解压到当前目录
tar -xzf archive.tar.gz

# 解压到指定目录（-C 后接目标路径）
tar -xzf archive.tar.gz -C /opt/

# 自动检测压缩格式解压（tar 新版本支持）
tar -xf archive.tar.xz -C /opt/

# 仅解压归档中的单个文件（先用 -t 找到文件名，再指定）
tar -tf archive.tar.bz2 | grep 'shadow'
tar -xjf archive.tar.bz2 etc/shadow
```

### 查看归档内容

``` bash title="查看归档"
# 列出归档文件名
tar -tf archive.tar.gz

# 列出详细信息（权限、属主、大小）
tar -tvf archive.tar.gz
```

!!! tip "tar 默认会去掉开头的 /"

    打包 `/etc` 时，tar 会自动去除路径开头的 `/`，将文件记录为 `etc/xxx` 而非 `/etc/xxx`。这是为了安全：解压时不会强制覆盖系统目录。若确实需要保留绝对路径，加 `-P` 选项，但**解压时务必确认目标，否则可能覆盖系统文件**。

### 增量备份（仅打包更新的文件）

``` bash title="增量备份示例"
# 仅打包 2025/01/01 之后修改过的文件
tar -jcv -f incremental.tar.bz2 \
  --newer-mtime="2025/01/01" /etc/*
```

## zip / unzip

`zip` 格式与 Windows 完全兼容，适合需要与 Windows 系统交换文件的场景。

``` bash title="zip / unzip 常用操作"
# 递归压缩目录
zip -r archive.zip dir/

# 解压到当前目录
unzip archive.zip

# 解压到指定目录
unzip archive.zip -d /opt/

# 列出压缩包内容（不解压）
unzip -l archive.zip

# 向已有压缩包追加文件
zip archive.zip newfile.txt
```

## 实用技巧

### 管道流式压缩

将打包与压缩通过管道连接，避免生成中间文件：

``` bash title="管道压缩"
# 等价于 tar -czf backup.tar.gz dir/，但展示了管道原理
tar -cf - dir/ | gzip > backup.tar.gz

# 用 zstd 替换 gzip（速度更快）
tar -cf - dir/ | zstd > backup.tar.zst
```

### 远程传输不落盘

直接通过 SSH 将本地目录传到远程，全程不在本地写临时文件：

``` bash title="远程传输"
tar -czf - dir/ | ssh user@host "cat > /backup/dir.tar.gz"

# 反向：从远程拉取并在本地解压
ssh user@host "tar -czf - /etc" | tar -xzf - -C /restore/
```

### 测试归档完整性

``` bash title="验证完整性"
# 列出内容到 /dev/null，若有错误会输出到 stderr
tar -tf archive.tar.gz > /dev/null

# gzip 自带的完整性验证
gzip -t archive.tar.gz
```

### 系统目录备份示例

``` bash title="完整系统备份示例"
mkdir -p /backups
chmod 700 /backups

tar -jcpv -f /backups/backup-$(date +%Y%m%d).tar.bz2 \
  --exclude=/backups \
  --exclude=/proc \
  --exclude=/sys \
  --exclude=/dev \
  --exclude=/run \
  /etc /home /var/spool/mail /var/spool/cron /root
```

!!! warning "解压后的 SELinux 问题"

    在 SELinux 开启的系统（如 RHEL/CentOS）上，通过 tar 还原 `/etc` 等系统目录后，文件的 SELinux 标签可能发生变化，导致系统无法正常登录。还原后立即执行 `restorecon -Rv /etc` 修复标签，或在重启前创建 `/.autorelabel` 文件让系统开机时自动修复。

## 各发行版安装差异

=== "Debian / Ubuntu"

    默认仓库已包含 `gzip`、`bzip2`、`xz-utils`，但 `zstd` 和 `lz4` 需要手动安装：

    ``` bash
    sudo apt install zstd lz4
    ```

=== "Red Hat / RHEL / CentOS"

    RHEL 8+ 已内置 `zstd`（RPM 包格式从 RHEL 8 起采用 zstd 压缩）：

    ``` bash
    # RHEL 8+ 通常已预装，如缺失：
    sudo dnf install zstd

    # lz4
    sudo dnf install lz4
    ```

=== "Arch Linux"

    所有工具均在官方仓库，一次性安装：

    ``` bash
    sudo pacman -S gzip bzip2 xz zstd lz4 zip unzip
    ```

---
title: 用户与权限
---

# 用户与组管理

**本文你会学到**：

- 四个核心配置文件的结构与含义（/etc/passwd、/etc/shadow、/etc/group、/etc/gshadow）
- UID 与 GID 的范围约定及系统账号的特点
- 密码加密算法与过期策略的配置
- 创建、修改、删除用户的命令与选项（useradd、usermod、userdel、passwd）
- 组管理的完整流程（groupadd、groupmod、groupdel、gpasswd）
- 初始组与附加组的区别与实际用途
- `su` 与 `sudo` 的区别及使用场景
- Visudo 编辑 sudoers 配置的正确方法
- Debian 与 RHEL 系统在用户管理上的差异

## 核心配置文件：账号信息存储在哪里？

Linux 把账号、密码、组信息分别存储在四个纯文本文件中，理解这四个文件，就掌握了 Linux 账号体系的底层逻辑。

### /etc/passwd：账号基本信息

每一行代表一个账号，字段以 `:` 分隔，共七个字段：

```
root:x:0:0:root:/root:/bin/bash
用户名:密码:UID:GID:注释:主目录:Shell
```

| 字段 | 说明 |
|------|------|
| 用户名 | 账号名称，供人类阅读，系统内部只认 UID |
| 密码 | 固定为 `x`，实际加密密码存储在 `/etc/shadow` |
| UID | 用户识别码，系统真正用来识别身份的数字 |
| GID | 主组（初始组）的 ID，对应 `/etc/group` |
| 注释 | 说明信息，`finger` 命令会读取此字段 |
| 主目录 | 用户登录后默认所在目录 |
| Shell | 登录后使用的 Shell 程序路径 |

**UID 范围约定**：

| 范围 | 用途 |
|------|------|
| `0` | `root` 超级管理员，不受权限约束 |
| `1~999` | 系统账号，用于运行后台服务，通常不可登录 |
| `1000+` | 普通用户账号 |

!!! warning "不要随意修改 UID"

    `/etc/passwd` 权限为 `-rw-r--r--`，所有程序都能读取。若随意修改 UID，文件的拥有者字段会变成"找不到账号的数字"，还可能导致用户无法进入自己的主目录。

### /etc/shadow：加密密码与过期策略

密码相关数据独立存储在 `/etc/shadow`（权限 `----------`，只有 root 可读），共九个字段：

```
dmtsai:$6$M4Ip...$B418...:16559:5:60:7:5:16679:
用户名:密码哈希:最后修改日:最短天数:最长天数:警告天数:宽限天数:账号失效日:保留
```

| 字段 | 说明 |
|------|------|
| 密码哈希 | 格式为 `$算法$盐值$哈希`；`!!` 或 `*` 表示账号被锁定 |
| 最后修改日 | 从 1970-01-01 起累积的天数 |
| 最短使用天数 | 改密后多少天内不能再改（`0` = 随时可改） |
| 最长使用天数 | 超过此天数必须修改密码（`99999` ≈ 永不过期） |
| 警告天数 | 密码过期前提前警告的天数 |
| 宽限天数 | 密码过期后还能继续使用旧密码的天数 |
| 账号失效日 | 到达此日期后账号完全失效，无论密码状态 |

**密码哈希算法标识**：

| 前缀 | 算法 |
|------|------|
| `$6$` | SHA-512（主流） |
| `$5$` | SHA-256 |
| `$y$` | yescrypt（较新发行版） |

### /etc/group：组信息

```
wheel:x:10:alice,bob
组名:密码:GID:成员列表
```

- 密码字段通常为 `x`（实际存在 `/etc/gshadow`）
- 成员列表只列出**附加组**成员；主组成员不出现在此字段

!!! tip "初始组与附加组的区别"

    用户的**初始组**（primary group）在 `/etc/passwd` 第四字段指定，登录后立即生效，新建文件默认属于此组。**附加组**（supplementary group）在 `/etc/group` 第四字段列出，用于扩展用户的访问权限。

### /etc/gshadow：组的影子文件

```
wheel:::alice,bob
组名:组密码:组管理员:成员列表
```

密码字段为 `!` 或空时，表示该组没有组管理员，无法通过 `newgrp` 切换。

## 用户管理命令

### useradd：创建用户

```bash
# 使用系统默认值创建用户（CentOS/RHEL 会自动创建主目录）
useradd username

# 常用选项组合
useradd -m username                     # 强制创建主目录（Debian 系需要此选项）
useradd -m -s /bin/bash username        # 指定 Shell
useradd -m -u 1500 username             # 指定 UID
useradd -m -g groupname username        # 指定主组（初始组）
useradd -m -G wheel,docker username     # 指定附加组
useradd -m -c "Full Name" username      # 添加注释
useradd -r -s /sbin/nologin svcuser     # 系统账号（UID < 1000，无主目录，不可登录）
```

`useradd` 执行时会同时修改以下文件：

- `/etc/passwd`、`/etc/shadow` — 账号与密码记录
- `/etc/group`、`/etc/gshadow` — 新建与账号同名的私有组
- `/home/username/` — 从 `/etc/skel/` 复制初始配置文件

**默认值来源**：`/etc/default/useradd`（Shell、主目录基准路径等）和 `/etc/login.defs`（UID/GID 范围、密码策略等）。

!!! note "新建用户后记得设置密码"

    `useradd` 完成后，`/etc/shadow` 中密码字段为 `!!`（锁定状态），账号无法登录，必须用 `passwd username` 设置密码才算完成创建。

### usermod：修改用户属性

```bash
usermod -aG wheel username         # 追加到附加组（-a 是关键！不加 -a 会覆盖现有附加组）
usermod -g newgroup username       # 修改主组
usermod -s /bin/zsh username       # 修改 Shell
usermod -l newname oldname         # 修改用户名（不会移动主目录！）
usermod -L username                # 锁定账号（在密码哈希前加 !）
usermod -U username                # 解锁账号
usermod -e 2025-12-31 username     # 设置账号过期日期
usermod -c "New Comment" username  # 修改注释
```

!!! warning "-aG 中 -a 不能省略"

    `usermod -G wheel username` 会将用户的附加组**重置为只剩 `wheel`**，清除原来的所有附加组。必须写成 `usermod -aG wheel username` 才是"追加"。

### userdel：删除用户

```bash
userdel username      # 删除账号（保留主目录与邮件，可供后续审计）
userdel -r username   # 删除账号并删除主目录（/home/username 和 /var/spool/mail/username）
```

!!! tip "删除前先查残留文件"

    用户在系统上操作过后，文件可能散落各处。删除前建议先执行 `find / -user username` 找出所有属于该用户的文件，再决定如何处理。

### passwd：设置与管理密码

```bash
passwd                          # 修改自己的密码（需输入旧密码）
passwd username                 # root 修改他人密码（不需旧密码）
passwd -l username              # 锁定账号（密码前加 !!）
passwd -u username              # 解锁账号
passwd -d username              # 清空密码（无密码可直接登录，危险！）
passwd -e username              # 强制下次登录时必须修改密码
passwd -n 7 -x 90 -w 14 username  # 设置：最短 7 天、最长 90 天、提前 14 天警告
```

### chage：精细管理密码过期策略

`chage` 是专门用于管理 `/etc/shadow` 中时间字段的工具，比 `passwd -S` 输出更直观：

```bash
chage -l username          # 查看密码过期详情（可读格式）
chage -M 90 username       # 最长使用 90 天
chage -m 7 username        # 最短使用 7 天（防止频繁改密）
chage -W 14 username       # 到期前 14 天开始警告
chage -E 2025-12-31 username  # 账号在此日期失效
chage -d 0 username        # 强制下次登录必须改密（将最后修改日设为 1970-01-01）
```

**实用场景**——新建账号后强制首次登录改密：

```bash
useradd newuser
echo "temppass" | passwd --stdin newuser
chage -d 0 newuser   # 用户首次登录时必须立即修改密码
```

### id / who / last：查询用户信息

```bash
id                   # 显示当前用户的 UID / GID / 附加组
id username          # 显示指定用户的 UID / GID / 附加组
who                  # 当前已登录的用户列表
w                    # 详细登录信息 + 当前执行的命令
last                 # 近期登录历史（读取 /var/log/wtmp）
lastlog              # 所有账号的最后登录时间一览
finger username      # 显示用户详细信息（需单独安装 finger 包）
```

## 组管理命令

```bash
groupadd groupname         # 创建组
groupadd -g 1500 groupname # 指定 GID 创建组
groupmod -n newname old    # 修改组名
groupmod -g 1600 groupname # 修改 GID（谨慎！会影响已有文件的组属性）
groupdel groupname         # 删除组（有用户以此为主组时无法删除）
```

**管理组成员**：

```bash
gpasswd -a user group      # 添加用户到组
gpasswd -d user group      # 从组中移除用户
gpasswd -M u1,u2,u3 group  # 设置组成员列表（覆盖式）
gpasswd -A user group      # 设置组管理员（可以自行管理组成员）
groups username            # 查看用户所属的所有组
newgrp groupname           # 临时切换有效组（新开子 Shell，exit 退出）
```

!!! note "newgrp 的作用"

    `newgrp` 会新开一个子 Shell，在该子 Shell 中用户的**有效组**切换为指定组，之后创建的新文件将属于此组。使用 `exit` 退出子 Shell 即可恢复原来的有效组。

## 切换用户

```bash
su username              # 切换用户（非登录 Shell，不加载目标用户的环境变量）
su - username            # 切换用户（登录 Shell，加载完整环境，相当于重新登录）
su -c "command" username # 以指定用户身份执行单条命令后立即返回
```

**`su` 与 `su -` 的区别**：

| 命令 | 环境变量 | PATH | 工作目录 |
|------|---------|------|---------|
| `su username` | 保留当前用户的环境 | 保留当前 PATH | 不变 |
| `su - username` | 加载目标用户的 `.bash_profile` | 更新为目标用户的 PATH | 切换到目标主目录 |

!!! tip "切换到 root"

    `su -` 或 `su - root` 以登录 Shell 方式切换到 root，加载完整的 root 环境。在 Ubuntu/Debian 中，root 账号默认没有密码，需用 `sudo -i` 代替。

## sudo：以其他用户身份执行命令

### 基本用法

```bash
sudo command                  # 以 root 身份执行命令
sudo -u username command      # 以指定用户身份执行命令
sudo -i                       # 以登录 Shell 方式切换到 root
sudo -l                       # 查看当前用户被授权的 sudo 权限
```

### visudo：安全编辑 /etc/sudoers

**始终**使用 `visudo` 编辑 sudoers 文件，它会在保存前进行语法检查，防止配置错误导致 sudo 失效：

```bash
visudo   # 以默认编辑器打开 /etc/sudoers
```

**常见 sudoers 配置**：

```
# 允许 wheel 组成员使用完整 sudo 权限（RHEL/CentOS 惯例）
%wheel ALL=(ALL) ALL

# 允许特定用户无需密码使用 sudo
alice ALL=(ALL) NOPASSWD: ALL

# 限制只能执行特定命令（更安全的授权方式）
bob ALL=(ALL) /sbin/systemctl restart nginx, /sbin/systemctl status nginx
```

sudoers 规则格式：`用户/组 主机=(以哪个用户执行) 命令列表`

- `%` 前缀表示组，如 `%wheel`
- `ALL` 可用作通配符（主机名、用户名、命令均可使用）
- `NOPASSWD:` 表示执行 sudo 时不询问密码

## 发行版差异

=== "Debian / Ubuntu"

    **高层封装命令**：

    ```bash
    adduser username          # 交互式创建用户，自动创建主目录、提示设置密码
    adduser username groupname  # 将用户添加到组
    addgroup groupname        # 创建组
    deluser username          # 删除用户
    delgroup groupname        # 删除组
    ```

    `adduser` 比 `useradd` 更友好，会交互式询问姓名、密码等信息，并自动处理主目录权限。配置文件在 `/etc/adduser.conf`。

    **sudo 组名**：Ubuntu/Debian 使用 `sudo` 组（不是 `wheel`）：

    ```bash
    usermod -aG sudo username   # Ubuntu：赋予 sudo 权限
    ```

=== "RHEL / CentOS / Fedora"

    **只有底层命令**：

    ```bash
    useradd username   # 无 adduser 封装，直接使用 useradd
    ```

    **sudo 组名**：使用 `wheel` 组：

    ```bash
    usermod -aG wheel username  # RHEL：赋予 sudo 权限
    ```

    安装系统时创建的第一个普通用户会自动加入 `wheel` 组。

    **密码算法查询**：

    ```bash
    authconfig --test | grep hashing   # 较旧版本
    grep ENCRYPT_METHOD /etc/login.defs  # 通用方式
    ```


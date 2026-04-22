---
title: DOS 命令
---

# DOS 命令速查手册

!!! note "适用范围"

    本手册收录 Windows `命令提示符（CMD）` 内置命令及常用外部命令。PowerShell 专有命令不在本手册范围内。
    打开 CMD：++win+r++ 输入 `cmd` 回车，或在文件夹地址栏输入 `cmd` 回车（自动定位到当前目录）。

---

## 📁 文件操作

文件的复制、移动、删除、重命名及内容查看。

### 复制文件 copy

复制一个或多个文件到目标位置。

| 参数 | 说明 |
|------|------|
| `/y` | 覆盖目标文件时不提示确认 |
| `/-y` | 覆盖目标文件时提示确认（默认） |
| `/b` | 以二进制模式复制（合并文件时不插入 EOF 标记） |
| `/a` | 以 ASCII 模式复制（在文件末尾添加 EOF 标记） |

``` bat
copy source.txt dest.txt
copy /y C:\src\*.log D:\backup\
```

### 移动或重命名文件 move

移动文件或重命名文件（跨驱动器时相当于复制后删除）。

``` bat
move old.txt newname.txt
move C:\temp\*.txt D:\archive\
```

### 删除文件 del / erase

删除一个或多个文件。

| 参数 | 说明 |
|------|------|
| `/f` | 强制删除只读文件 |
| `/s` | 递归删除子目录中匹配的文件 |
| `/q` | 静默模式（删除通配符文件时不提示确认） |
| `/a:属性` | 按属性删除（h 隐藏、r 只读、s 系统等） |

``` bat
del temp.txt
del /f /s /q C:\Temp\*.tmp
```

### 重命名文件或目录 ren / rename

重命名文件或目录（不支持跨目录移动）。

``` bat
ren oldname.txt newname.txt
ren *.log *.bak
```

### 查看文件内容 type

将文本文件内容输出到控制台，可用于查看小文件或合并文件。

``` bat
type readme.txt
type file1.txt file2.txt > merged.txt
```

### 增强复制目录树 xcopy

增强版复制，支持目录树复制。

| 参数 | 说明 |
|------|------|
| `/e` | 复制所有子目录（含空目录） |
| `/s` | 复制子目录（忽略空目录） |
| `/i` | 目标不存在时自动创建为目录 |
| `/h` | 复制隐藏文件和系统文件 |
| `/y` | 覆盖时不提示 |
| `/d:日期` | 只复制指定日期之后修改的文件 |
| `/exclude:文件` | 排除文件中列出的路径 |

``` bat
xcopy C:\src D:\dest /e /i /h
```

### 鲁棒文件同步 robocopy

企业级鲁棒文件复制工具，支持断点续传、镜像同步等。

| 参数 | 说明 |
|------|------|
| `/mir` | 镜像模式：删除目标中多余的文件（相当于 `/e /purge`） |
| `/e` | 复制所有子目录（含空目录） |
| `/z` | 可恢复模式（网络中断后可续传） |
| `/b` | 备份模式（绕过文件权限读取） |
| `/mt[:n]` | 多线程复制，默认 8 个线程 |
| `/xf 通配符` | 排除匹配的文件 |
| `/xd 目录` | 排除指定目录 |
| `/log:文件` | 将输出写入日志文件 |
| `/log+:文件` | 追加到日志文件 |
| `/eta` | 显示预计完成时间 |

``` bat
robocopy C:\src D:\dest /mir /z /log:backup.log
```

### 比较文件差异 fc

逐行（文本）或逐字节（二进制）比较两个文件的差异。`/b` 二进制模式，`/l` 文本模式（默认）。

``` bat
fc file1.txt file2.txt
fc /b image1.png image2.png
```

### 查看或修改文件属性 attrib

查看或修改文件属性。`+r/-r` 只读，`+h/-h` 隐藏，`+s/-s` 系统，`+a/-a` 存档。`/s` 递归。

``` bat
attrib +h secret.txt
attrib -r -h C:\Windows\*.ini /s
```

---

## 📂 目录操作

目录的列举、切换、创建与删除。

### 列出目录内容 dir

列出目录内容。

| 参数 | 说明 |
|------|------|
| `/b` | 裸格式，只输出文件名（无标题和摘要） |
| `/s` | 递归显示所有子目录内容 |
| `/a[:属性]` | 显示指定属性的文件（h 隐藏、s 系统、r 只读、d 目录） |
| `/o[:排序]` | 排序方式：n 名称、e 扩展名、d 日期、s 大小、g 目录先 |
| `/w` | 宽格式显示（多列） |
| `/p` | 分页显示 |
| `/q` | 显示文件所有者 |

``` bat
dir
dir /b /s C:\Windows\*.exe
dir /a:h
```

### 切换工作目录 cd / chdir

切换当前工作目录。`cd ..` 上级，`cd \` 根目录，`cd /d D:\path` 跨驱动器切换（需加 `/d`）。

``` bat
cd C:\Users\Administrator\Desktop
cd /d D:\projects
cd ..
```

### 创建目录 md / mkdir

创建目录，支持同时创建多级目录。

``` bat
md newfolder
md C:\a\b\c
```

### 删除目录 rd / rmdir

删除目录。`/s` 删除目录及其所有内容，`/q` 静默不提示。

``` bat
rd emptydir
rd /s /q C:\Temp\old
```

### 树状显示目录层次 tree

以树状结构显示目录层次。`/f` 同时显示文件名，`/a` 使用 ASCII 字符绘制。

``` bat
tree
tree /f > structure.txt
```

### 保存并切换目录 pushd

保存当前目录并切换到新目录，可跨驱动器（自动映射网络路径为驱动器）。

``` bat
pushd D:\work
```

### 恢复保存的目录 popd

恢复 `pushd` 之前保存的目录。

``` bat
popd
```

---

## 🌐 网络命令

网络连通性测试、接口信息查询与 DNS 解析。

### 测试网络连通性 ping

向目标主机发送 ICMP 回显请求，测试网络连通性及延迟。`-t` 持续发送，`-n` 指定次数，`-l` 指定包大小。

``` bat
ping google.com
ping -t 192.168.1.1
ping -n 10 -l 1000 8.8.8.8
```

### 查看网络适配器配置 ipconfig

显示本机所有网络适配器的 IP 配置。`/all` 显示详细信息（MAC 地址、DHCP 等），`/release` 释放 IP，`/renew` 重新获取，`/flushdns` 清空 DNS 缓存。

``` bat
ipconfig
ipconfig /all
ipconfig /flushdns
```

### 查看网络连接与端口 netstat

显示网络连接、路由表及端口占用。

| 参数 | 说明 |
|------|------|
| `-a` | 显示所有连接和监听端口 |
| `-n` | 用数字形式显示 IP 和端口（不解析主机名） |
| `-o` | 显示每个连接对应的进程 PID |
| `-b` | 显示建立连接的可执行文件名（需管理员） |
| `-e` | 显示以太网统计信息 |
| `-s` | 按协议显示统计信息 |
| `-r` | 显示路由表 |
| `-p 协议` | 只显示指定协议（TCP/UDP/IP） |

``` bat
netstat -ano
netstat -ano | findstr :8080
```

### 追踪路由路径 tracert

追踪数据包到目标主机所经过的路由节点及延迟。`-d` 不解析主机名（更快）。

``` bat
tracert google.com
tracert -d 8.8.8.8
```

### 查询 DNS 解析 nslookup

查询 DNS 解析结果，支持交互式和单行模式，可指定 DNS 服务器。

``` bat
nslookup google.com
nslookup google.com 8.8.8.8
```

### 管理网络资源 net

管理网络资源，涵盖共享、连接、用户、服务等。常用子命令见「用户与权限」和「进程与服务」章节。

``` bat
net view                    :: 查看局域网共享
net use Z: \\server\share   :: 映射网络驱动器
net use Z: /delete          :: 断开映射
```

### 查看 ARP 缓存 arp

查看或修改 ARP 缓存（IP 与 MAC 地址映射表）。`-a` 显示所有条目，`-d` 删除条目，`-s` 静态绑定。

``` bat
arp -a
arp -a 192.168.1.1
```

### 查看或修改路由表 route

查看或修改本机路由表。`print` 显示，`add` 添加，`delete` 删除，`change` 修改。

``` bat
route print
route add 10.0.0.0 mask 255.0.0.0 192.168.1.254
```

### 统计路由节点丢包率 pathping

结合 `ping` 和 `tracert`，统计每跳节点的丢包率与延迟（采集时间较长）。

``` bat
pathping google.com
```

---

## ⚙️ 进程与服务

查看运行中的进程、终止进程及管理 Windows 服务。

### 列出运行中的进程 tasklist

列出当前运行的进程。

| 参数 | 说明 |
|------|------|
| `/fi "条件"` | 过滤条件，如 `imagename eq chrome.exe`、`pid eq 1234`、`status eq running` |
| `/fo 格式` | 输出格式：`TABLE`（默认）、`LIST`、`CSV` |
| `/nh` | 不显示表头 |
| `/v` | 显示详细信息（标题、CPU 时间、内存等） |
| `/svc` | 显示各进程所托管的服务名 |
| `/m [模块]` | 显示加载了指定 DLL 的进程 |

``` bat
tasklist
tasklist /fi "imagename eq chrome.exe"
tasklist /fi "pid eq 1234"
```

### 终止进程 taskkill

终止进程。`/pid` 按进程 ID，`/im` 按映像名称，`/f` 强制终止，`/t` 同时终止子进程。

``` bat
taskkill /pid 1234 /f
taskkill /im notepad.exe /f /t
```

### 管理 Windows 服务 sc

管理 Windows 服务（Service Control）。`query` 查询状态，`start/stop` 启停，`config` 修改配置，`create/delete` 创建/删除服务。

``` bat
sc query
sc query type= service state= running
sc stop "服务名"
sc start "服务名"
sc config "服务名" start= auto
```

### 启停 Windows 服务 net start / net stop

启动或停止 Windows 服务（比 `sc` 更简洁）。

``` bat
net start "Windows Update"
net stop "Windows Update"
net start               :: 列出所有已启动的服务
```

### 通过 WMI 查询进程 wmic process

通过 WMI 查询进程详情，支持丰富的过滤与格式化输出。

!!! warning "已弃用"

    `wmic` 在 Windows 11 22H2+ 中已被微软标记为弃用，执行时会出现警告。
    建议改用 PowerShell：`Get-Process`、`Get-WmiObject` 或 `Get-CimInstance`。

``` bat
wmic process list brief
wmic process where "name='chrome.exe'" get processid,commandline
wmic process where "processid=1234" call terminate
```

---

## 📋 注册表操作

通过命令行读写 Windows 注册表，常用于脚本自动化配置。

!!! warning "注意事项"

    修改注册表前建议先导出备份。错误的注册表修改可能导致系统不稳定。

### 查询注册表 reg query

查询注册表键或值。`/v` 指定值名，`/s` 递归子键，`/f` 搜索匹配字符串。

``` bat
reg query HKCU\Software\Microsoft\Windows\CurrentVersion\Run
reg query HKLM\SYSTEM\CurrentControlSet\Services /s /f "ImagePath"
```

### 添加或修改注册表值 reg add

添加或修改注册表键/值。`/v` 值名，`/t` 数据类型（REG_SZ/REG_DWORD 等），`/d` 数据，`/f` 强制覆盖。

``` bat
reg add HKCU\Software\MyApp /v Setting /t REG_SZ /d "value" /f
reg add HKLM\SOFTWARE\MyApp /v Count /t REG_DWORD /d 10 /f
```

### 删除注册表键或值 reg delete

删除注册表键或值。`/v` 删除指定值，`/va` 删除所有值，`/f` 不提示确认。

``` bat
reg delete HKCU\Software\MyApp /v Setting /f
reg delete HKCU\Software\OldApp /f
```

### 导出注册表为文件 reg export

将注册表键导出为 `.reg` 文件（用于备份或迁移）。

``` bat
reg export HKCU\Software\MyApp C:\backup\myapp.reg
```

### 从文件导入注册表 reg import

从 `.reg` 文件导入注册表数据。

``` bat
reg import C:\backup\myapp.reg
```

---

## 🖥️ 系统信息

查询操作系统版本、环境变量、主机信息及硬件详情。

### 查看系统详细信息 systeminfo

显示系统详细信息（OS 版本、补丁、内存、网卡等）。`/fo` 指定格式（TABLE/LIST/CSV）。

``` bat
systeminfo
systeminfo /fo csv > sysinfo.csv
```

### 查看 Windows 版本号 ver

输出 Windows 版本号（简短）。

``` bat
ver
```

### 显示或设置系统日期 date

显示或设置系统日期。不带参数则显示当前日期并等待输入新日期（按 Enter 取消）。

``` bat
date /t      :: 只显示，不提示修改
```

### 显示或设置系统时间 time

显示或设置系统时间。`/t` 只显示不修改。

``` bat
time /t
```

### 查看或设置环境变量 set

显示、设置或删除环境变量。不带参数列出所有变量，`set VAR=value` 设置，`set VAR=` 删除，`set /p VAR=提示` 从输入读取。在批处理脚本中的变量定义与算术用法，参见「批处理基础」章节。

``` bat
set
set PATH
set MYVAR=hello
set /p NAME=请输入你的名字：
```

### 定位可执行文件路径 where

定位可执行文件的完整路径（类似 Linux `which`）。`/r` 在指定目录递归搜索。

``` bat
where java
where python
where /r C:\ notepad.exe
```

### 查看当前登录用户 whoami

显示当前登录用户的用户名和域名。`/priv` 显示特权信息，`/groups` 显示所属组。

``` bat
whoami
whoami /priv
whoami /groups
```

### 查看计算机名 hostname

显示本机计算机名。

``` bat
hostname
```

### 查询硬件与软件信息 wmic

Windows Management Instrumentation 命令行工具，可查询硬件、软件、OS 等几乎所有系统信息。

!!! warning "已弃用"

    `wmic` 在 Windows 11 22H2+ 中已被微软标记为弃用，执行时会出现警告。
    建议改用 PowerShell：`Get-WmiObject` 或 `Get-CimInstance`。

``` bat
wmic cpu get name,numberofcores
wmic memorychip get capacity
wmic diskdrive get model,size
wmic os get caption,version,buildnumber
wmic product get name,version    :: 已安装软件列表（较慢）
```

---

## 👤 用户与权限

本地用户/组管理及文件访问控制列表（ACL）操作。

### 管理本地用户账户 net user

查看或管理本地用户账户。需管理员权限修改。

``` bat
net user                         :: 列出所有用户
net user 用户名                  :: 查看指定用户详情
net user 用户名 密码 /add        :: 新建用户
net user 用户名 /delete          :: 删除用户
net user 用户名 新密码           :: 修改密码
net user 用户名 /active:no       :: 禁用账户
```

### 管理本地用户组 net localgroup

管理本地用户组。

``` bat
net localgroup                         :: 列出所有本地组
net localgroup Administrators          :: 查看管理员组成员
net localgroup Administrators 用户名 /add    :: 加入管理员组
net localgroup Administrators 用户名 /delete :: 移出管理员组
```

### 管理文件访问控制列表 icacls

查看或修改文件/目录的访问控制列表（推荐，取代 `cacls`）。

| 参数/权限 | 说明 |
|----------|------|
| `/grant 用户:权限` | 授予权限 |
| `/deny 用户:权限` | 拒绝权限 |
| `/remove 用户` | 移除用户的所有权限 |
| `/reset` | 重置为仅继承父目录权限 |
| `/t` | 递归处理子目录和文件 |
| `/c` | 忽略错误继续执行 |
| `/inheritance:e/d/r` | e=启用继承，d=禁用继承，r=删除继承的条目 |
| `F` | 完全控制 |
| `M` | 修改（含读/写/删除） |
| `RX` | 读取和执行 |
| `R` | 只读 |
| `W` | 只写 |
| `(OI)(CI)` | 对象继承+容器继承（文件+子目录均继承） |

``` bat
icacls C:\mydir
icacls C:\mydir /grant Users:(OI)(CI)F /t    :: 递归授予完全控制
icacls C:\secret /deny Everyone:(F)           :: 拒绝所有人访问
icacls C:\mydir /reset /t                     :: 重置为继承权限
```

### 旧版 ACL 工具 cacls

旧版 ACL 工具（Windows XP 时代），新版系统推荐改用 `icacls`。

``` bat
cacls C:\mydir
```

### 以其他用户身份运行程序 runas

以其他用户身份运行程序（类似 Linux `sudo`）。`/user` 指定用户，`/savecred` 保存凭据。

``` bat
runas /user:Administrator cmd
runas /user:DOMAIN\admin "notepad C:\Windows\system32\drivers\etc\hosts"
```

---

## 🔧 批处理基础

`.bat` / `.cmd` 脚本中的核心控制结构与内置命令。

### 关闭命令回显 @echo off

关闭命令回显（脚本首行惯例），避免每条命令都被打印到屏幕。`@` 表示不回显本行本身。

``` bat
@echo off
echo 这行会显示，但命令本身不显示
```

### 输出文本 echo

输出文本到控制台，或输出空行 `echo.`，或重定向到文件。`echo on/off` 切换回显。

``` bat
echo Hello, World!
echo.
echo 写入文件 > output.txt
echo 追加文件 >> output.txt
```

### 注释行 rem

注释行，`::` 也可作注释（速度略快，但不能用于代码块内）。

``` bat
rem 这是注释
:: 这也是注释
```

### 定义变量与算术运算 set

在批处理中定义变量。使用 `%VAR%` 引用。`/a` 执行算术运算，`/p` 从用户输入读取。

``` bat
set NAME=World
echo Hello, %NAME%!
set /a RESULT=1+2*3
echo %RESULT%
```

### 条件判断 if

条件判断。支持字符串比较、数字比较、文件存在、错误码检测。`not` 取反，`else` 分支（需在同一行或括号内）。

``` bat
if "%1"=="" (echo 缺少参数) else (echo 参数为：%1)
if exist C:\log.txt del C:\log.txt
if %ERRORLEVEL% neq 0 (echo 上条命令失败，退出码：%ERRORLEVEL%)
if /i "%OS%"=="Windows_NT" echo 这是 Windows NT 系列
```

### 循环结构 for

循环结构。`%%i` 在脚本中（命令行用 `%i`），`/l` 数字范围，`/f` 解析文件/字符串，`/d` 仅目录，`/r` 递归文件。

``` bat
:: 遍历文件
for %%f in (*.txt) do echo %%f

:: 数字循环（1 到 5，步长 1）
for /l %%i in (1,1,5) do echo %%i

:: 解析文件内容（按行读取）
for /f "delims=" %%line in (input.txt) do echo %%line
```

### 跳转到标签 goto

跳转到指定标签（`:标签名`）。`:eof` 是内置标签，跳转后退出当前脚本/子程序。

``` bat
goto start
:start
echo 从这里开始执行
goto :eof
```

### 调用脚本或子程序 call

调用另一个批处理脚本或子程序（标签），执行完毕后返回调用处。

``` bat
call other.bat
call :myFunc arg1 arg2
goto :eof

:myFunc
echo 子程序参数：%1 %2
exit /b
```

### 暂停等待按键 pause

暂停执行并显示"请按任意键继续..."，等待用户按键。常用于脚本末尾。

``` bat
pause
```

### 获取上条命令退出码 %ERRORLEVEL%

上一条命令的退出状态码。`0` 通常表示成功，非 `0` 表示失败。可用 `exit /b 退出码` 在子程序中设置。

``` bat
ping -n 1 google.com
if %ERRORLEVEL% equ 0 (echo 网络正常) else (echo 网络不通)
```

---

## 🔀 输入输出重定向与管道

CMD 通过重定向符和管道符连接命令，实现数据的传递与文件的读写。

### 标准输出重定向到文件 >

将命令的标准输出（stdout）写入文件，原有内容会被`覆盖`。

``` bat
dir > list.txt
echo Hello > greeting.txt
```

### 追加输出到文件 >>

将命令的标准输出`追加`到文件末尾，不覆盖原有内容。

``` bat
echo 第一行 > log.txt
echo 第二行 >> log.txt
systeminfo >> log.txt
```

### 重定向错误输出 2>

将标准错误（stderr，文件描述符 2）重定向到文件，标准输出仍显示在屏幕。

``` bat
dir nonexistent 2> error.txt
```

### 合并标准输出与错误输出 2>&1

将 stderr 合并到 stdout，常与 `>` 配合使用，把所有输出都写入同一文件。

``` bat
:: 把 stdout 和 stderr 都写入 all.log
dir C:\ > all.log 2>&1

:: 在批处理中抑制所有输出（静默执行）
some-command > nul 2>&1
```

### 从文件读取输入 <

将文件内容作为命令的标准输入（stdin）。

``` bat
:: 让 sort 从 data.txt 读取数据
sort < data.txt
```

### 管道传递数据 |

将左侧命令的标准输出直接作为右侧命令的标准输入，无需中间文件。

``` bat
:: 查找包含 "error" 的日志行
type app.log | find "error"

:: 列出所有进程并搜索指定名称
tasklist | findstr chrome

:: 分页显示长输出
dir /s | more

:: 统计文件行数（find /c 计数模式）
type access.log | find /c /v ""
```

---

## 🔍 文本过滤与搜索

对文本内容进行搜索、过滤、排序与分页显示。

### 搜索文本字符串 find

在文件或输入流中搜索指定字符串（区分大小写）。

| 参数 | 说明 |
|------|------|
| `/i` | 忽略大小写 |
| `/v` | 反向：输出`不包含`该字符串的行 |
| `/c` | 只输出匹配行数（统计模式） |
| `/n` | 在匹配行前显示行号 |

``` bat
find "ERROR" app.log
find /i "error" app.log            :: 忽略大小写
find /v "DEBUG" app.log            :: 不含 DEBUG 的行
find /c "ERROR" app.log            :: 统计 ERROR 出现次数
find /n "Exception" app.log        :: 显示行号
```

### 用正则表达式搜索文本 findstr

功能更强的字符串搜索工具，支持正则表达式。

| 参数 | 说明 |
|------|------|
| `/i` | 忽略大小写 |
| `/r` | 将搜索字符串解析为正则表达式（默认） |
| `/l` | 将搜索字符串作为字面量（不解析正则） |
| `/s` | 递归搜索子目录 |
| `/n` | 显示匹配行的行号 |
| `/v` | 反向：输出不匹配的行 |
| `/c:"字符串"` | 精确搜索指定字符串（含空格时使用） |
| `/m` | 只输出包含匹配的文件名，不显示匹配行 |
| `/p` | 忽略含不可打印字符的文件 |
| `/g:文件` | 从文件中读取搜索字符串列表 |

``` bat
:: 搜索 .java 文件中包含 "Exception" 的行
findstr "Exception" *.java

:: 正则：搜索以数字开头的行
findstr /r "^[0-9]" data.txt

:: 递归搜索当前目录下所有 .log 文件
findstr /s /i "error" *.log

:: 搜索多个关键词（用空格分隔，OR 关系）
findstr "error warning" app.log

:: 端口占用快速查找
netstat -ano | findstr :8080
```

### 对文本内容排序 sort

对输入内容按行排序后输出。`/r` 逆序，`/+n` 从第 n 列开始比较，`/o` 输出到文件。

``` bat
sort names.txt
sort /r names.txt
dir /b | sort > sorted.txt
```

### 分页显示输出 more

将长输出分页显示，每次显示一屏，按空格翻页，按 Q 退出。

``` bat
more longfile.txt
type longfile.txt | more
dir /s | more
```

### 过滤重复行 fc（配合管道）

CMD 没有内置去重命令，可借助临时文件 + `sort /unique`（需要 PowerShell 或脚本处理）；但可用 `sort` 对文件排序后用批处理去重。

---

## 🔒 文件安全与加密

文件加密、压缩及所有权管理。

### 加密 / 解密文件（EFS） cipher

管理 Windows EFS（加密文件系统）加密。`/e` 加密，`/d` 解密，`/s` 递归目录，`/w` 安全擦除磁盘空闲空间（防数据恢复）。

!!! note "EFS 使用前提"

    EFS 只在 NTFS 文件系统上可用，且依赖用户账户的证书。迁移文件前需导出证书，否则换机后无法解密。

``` bat
:: 加密单个文件
cipher /e secret.txt

:: 加密整个目录（含子目录）
cipher /e /s:C:\ConfidentialData

:: 解密文件
cipher /d secret.txt

:: 查看文件加密状态
cipher C:\MyDocs

:: 安全擦除 D: 盘空闲空间（防止数据恢复，较慢）
cipher /w:D:\
```

### 压缩 / 解压 NTFS 文件 compact

管理 NTFS 内置压缩属性（透明压缩，无需手动解压即可访问）。`/c` 压缩，`/u` 取消压缩，`/s` 递归，`/a` 含隐藏/系统文件，`/i` 忽略错误继续执行，`/q` 只显示汇总。

``` bat
:: 压缩目录（递归）
compact /c /s:C:\Logs

:: 取消压缩
compact /u /s:C:\Logs

:: 查看压缩状态与压缩率
compact C:\Logs\*.log
```

### 获取文件或目录所有权 takeown

将文件或目录的所有权夺回给当前用户（需管理员权限）。`/f` 指定路径，`/r` 递归，`/a` 所有权给管理员组，`/d y` 自动确认。

``` bat
:: 获取单个文件所有权
takeown /f C:\Windows\System32\drivers\etc\hosts

:: 递归获取整个目录所有权
takeown /f C:\inaccessible /r /d y
```

???+ tip "takeown 之后"

    取得所有权后，还需用 `icacls` 授予修改权限才能编辑文件：
    ``` bat
    takeown /f C:\Windows\System32\drivers\etc\hosts
    icacls C:\Windows\System32\drivers\etc\hosts /grant %USERNAME%:F
    ```

### 生成文件哈希校验值 certutil

通过 `certutil -hashfile` 计算文件的哈希值，用于验证文件完整性。支持 MD5、SHA1、SHA256（推荐）等算法。

``` bat
certutil -hashfile myfile.zip SHA256
certutil -hashfile installer.exe MD5
```

---

## 💾 磁盘与系统修复

磁盘分区管理、文件系统检查与系统文件修复。

### 检查并修复磁盘错误 chkdsk

检查磁盘文件系统错误及坏扇区。

| 参数 | 说明 |
|------|------|
| `/f` | 修复发现的文件系统错误（系统盘需重启后执行） |
| `/r` | 定位坏扇区并尝试恢复数据（包含 `/f` 的功能） |
| `/x` | 强制卸载卷后再检查（不适用于系统盘） |
| `/scan` | 在线扫描（不修复，Windows 8+ 支持） |
| `/spotfix` | 仅修复已知问题（快速，Windows 8+ 支持） |
| `/b` | 重新评估坏扇区列表（需先使用 `/r`） |

!!! warning "注意"

    对系统盘（通常 C:）运行 `chkdsk /f` 时，系统会要求重启后在启动阶段执行。

``` bat
:: 检查 D: 盘（只读，不修复）
chkdsk D:

:: 修复 D: 盘文件系统错误
chkdsk D: /f

:: 完整检查（含坏扇区修复，较慢）
chkdsk D: /r
```

### 修复 Windows 系统文件 sfc

系统文件检查器（System File Checker），扫描并修复受损的 Windows 系统文件。需要管理员权限。

| 参数 | 说明 |
|------|------|
| `/scannow` | 扫描所有受保护的系统文件并立即修复 |
| `/verifyonly` | 只扫描，不修复 |
| `/scanfile=路径` | 扫描并修复指定文件 |
| `/verifyfile=路径` | 只验证指定文件，不修复 |
| `/offwindir=路径` | 指定离线 Windows 目录（用于离线修复） |

``` bat
:: 扫描并修复所有受损系统文件
sfc /scannow

:: 只扫描不修复
sfc /verifyonly

:: 修复单个文件
sfc /scanfile=C:\Windows\System32\kernel32.dll
```

### 修复系统映像 DISM

部署映像服务和管理工具（DISM），用于检查与修复 Windows 系统映像，通常配合 `sfc` 使用。需要管理员权限。

| 参数 | 说明 |
|------|------|
| `/CheckHealth` | 快速检查映像是否标记为已损坏（不联网，秒完成） |
| `/ScanHealth` | 深度扫描映像损坏情况（较慢，不修复） |
| `/RestoreHealth` | 扫描并在线修复（从 Windows Update 下载替换文件） |
| `/Source:路径` | 指定修复文件来源（如 ISO 中的 install.wim，离线修复用） |
| `/LimitAccess` | 禁止从 Windows Update 下载（配合 /Source 使用） |

``` bat
:: 检查映像是否可修复
DISM /Online /Cleanup-Image /CheckHealth

:: 扫描映像损坏情况
DISM /Online /Cleanup-Image /ScanHealth

:: 在线修复（需要网络，从 Windows Update 下载）
DISM /Online /Cleanup-Image /RestoreHealth
```

???+ tip "SFC + DISM 修复顺序"

    系统文件损坏时，推荐先运行 DISM 修复映像，再运行 SFC 修复文件：
    ``` bat
    DISM /Online /Cleanup-Image /RestoreHealth
    sfc /scannow
    ```

### 磁盘分区管理 diskpart

交互式磁盘分区工具，支持创建/删除/格式化分区、设置活动分区等。需要管理员权限。

!!! danger "危险操作"

    `diskpart` 的 `clean`、`delete partition` 等命令会`立即销毁数据`，操作前务必确认目标磁盘编号。

``` bat
:: 进入 diskpart 交互模式
diskpart

:: 以下命令在 diskpart 提示符下执行：
list disk            :: 列出所有磁盘
select disk 1        :: 选择磁盘 1
list partition       :: 列出当前磁盘的分区
select partition 1   :: 选择分区 1
detail disk          :: 查看磁盘详情
detail partition     :: 查看分区详情
exit                 :: 退出 diskpart
```

### 格式化磁盘 format

格式化指定驱动器。`/fs` 指定文件系统（NTFS/FAT32/exFAT），`/q` 快速格式化，`/v` 设置卷标，`/x` 格式化前强制卸载。

!!! danger "数据丢失警告"

    格式化会`清除磁盘上的所有数据`，操作前请确认驱动器盘符正确。

``` bat
:: 快速格式化 D: 为 NTFS，卷标为 Data
format D: /fs:NTFS /q /v:Data

:: 格式化 U 盘为 FAT32
format E: /fs:FAT32 /q
```

### 查看磁盘空间使用情况 fsutil

文件系统工具，可查询磁盘信息、空闲空间、文件系统类型等。

``` bat
:: 查询指定驱动器的磁盘信息（空闲空间、总空间）
fsutil volume diskfree C:

:: 查询文件系统类型
fsutil fsinfo volumeinfo C:

:: 查看文件的实际分配大小（含簇对齐）
fsutil file queryextents myfile.dat
```

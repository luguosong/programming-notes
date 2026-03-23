---
icon: lucide/terminal
---

# DOS 命令速查手册

!!! note "适用范围"
    本手册收录 Windows **命令提示符（CMD）** 内置命令及常用外部命令。PowerShell 专有命令不在本手册范围内。
    打开 CMD：++win+r++ 输入 `cmd` 回车，或在文件夹地址栏输入 `cmd` 回车（自动定位到当前目录）。

---

## 文件操作

文件的复制、移动、删除、重命名及内容查看。

`copy`
:   复制一个或多个文件到目标位置。`/y` 覆盖时不提示确认。

    ``` bat
    copy source.txt dest.txt
    copy /y C:\src\*.log D:\backup\
    ```

`move`
:   移动文件或重命名文件（跨驱动器时相当于复制后删除）。

    ``` bat
    move old.txt newname.txt
    move C:\temp\*.txt D:\archive\
    ```

`del` / `erase`
:   删除一个或多个文件。`/f` 强制删除只读文件，`/s` 递归子目录，`/q` 静默模式。

    ``` bat
    del temp.txt
    del /f /s /q C:\Temp\*.tmp
    ```

`ren` / `rename`
:   重命名文件或目录（不支持跨目录移动）。

    ``` bat
    ren oldname.txt newname.txt
    ren *.log *.bak
    ```

`type`
:   将文本文件内容输出到控制台，可用于查看小文件或合并文件。

    ``` bat
    type readme.txt
    type file1.txt file2.txt > merged.txt
    ```

`xcopy`
:   增强版复制，支持目录树复制。`/e` 复制所有子目录（含空目录），`/i` 目标不存在时自动创建为目录，`/h` 含隐藏文件。

    ``` bat
    xcopy C:\src D:\dest /e /i /h
    ```

`robocopy`
:   企业级鲁棒文件复制工具，支持断点续传、镜像同步等。`/mir` 镜像模式（删除目标多余文件），`/z` 可恢复模式，`/log` 写入日志。

    ``` bat
    robocopy C:\src D:\dest /mir /z /log:backup.log
    ```

`fc`
:   逐行（文本）或逐字节（二进制）比较两个文件的差异。`/b` 二进制模式，`/l` 文本模式（默认）。

    ``` bat
    fc file1.txt file2.txt
    fc /b image1.png image2.png
    ```

`attrib`
:   查看或修改文件属性。`+r/-r` 只读，`+h/-h` 隐藏，`+s/-s` 系统，`+a/-a` 存档。`/s` 递归。

    ``` bat
    attrib +h secret.txt
    attrib -r -h C:\Windows\*.ini /s
    ```

---

## 目录操作

目录的列举、切换、创建与删除。

`dir`
:   列出目录内容。`/b` 只输出文件名（裸格式），`/s` 递归子目录，`/a` 含隐藏/系统文件，`/o:d` 按日期排序，`/o:s` 按大小排序，`/w` 宽格式显示。

    ``` bat
    dir
    dir /b /s C:\Windows\*.exe
    dir /a:h
    ```

`cd` / `chdir`
:   切换当前工作目录。`cd ..` 上级，`cd \` 根目录，`cd /d D:\path` 跨驱动器切换（需加 `/d`）。

    ``` bat
    cd C:\Users\Administrator\Desktop
    cd /d D:\projects
    cd ..
    ```

`md` / `mkdir`
:   创建目录，支持同时创建多级目录。

    ``` bat
    md newfolder
    md C:\a\b\c
    ```

`rd` / `rmdir`
:   删除目录。`/s` 删除目录及其所有内容，`/q` 静默不提示。

    ``` bat
    rd emptydir
    rd /s /q C:\Temp\old
    ```

`tree`
:   以树状结构显示目录层次。`/f` 同时显示文件名，`/a` 使用 ASCII 字符绘制。

    ``` bat
    tree
    tree /f > structure.txt
    ```

`pushd`
:   保存当前目录并切换到新目录，可跨驱动器（自动映射网络路径为驱动器）。

    ``` bat
    pushd D:\work
    ```

`popd`
:   恢复 `pushd` 之前保存的目录。

    ``` bat
    popd
    ```

---

## 网络命令

网络连通性测试、接口信息查询与 DNS 解析。

`ping`
:   向目标主机发送 ICMP 回显请求，测试网络连通性及延迟。`-t` 持续发送，`-n` 指定次数，`-l` 指定包大小。

    ``` bat
    ping google.com
    ping -t 192.168.1.1
    ping -n 10 -l 1000 8.8.8.8
    ```

`ipconfig`
:   显示本机所有网络适配器的 IP 配置。`/all` 显示详细信息（MAC 地址、DHCP 等），`/release` 释放 IP，`/renew` 重新获取，`/flushdns` 清空 DNS 缓存。

    ``` bat
    ipconfig
    ipconfig /all
    ipconfig /flushdns
    ```

`netstat`
:   显示网络连接、路由表及端口占用。`-a` 所有连接和监听端口，`-n` 数字地址，`-o` 显示进程 PID，`-b` 显示可执行文件名（需管理员）。

    ``` bat
    netstat -ano
    netstat -ano | findstr :8080
    ```

`tracert`
:   追踪数据包到目标主机所经过的路由节点及延迟。`-d` 不解析主机名（更快）。

    ``` bat
    tracert google.com
    tracert -d 8.8.8.8
    ```

`nslookup`
:   查询 DNS 解析结果，支持交互式和单行模式，可指定 DNS 服务器。

    ``` bat
    nslookup google.com
    nslookup google.com 8.8.8.8
    ```

`net`
:   管理网络资源，涵盖共享、连接、用户、服务等。常用子命令见「用户与权限」和「进程与服务」章节。

    ``` bat
    net view                    :: 查看局域网共享
    net use Z: \\server\share   :: 映射网络驱动器
    net use Z: /delete          :: 断开映射
    ```

`arp`
:   查看或修改 ARP 缓存（IP 与 MAC 地址映射表）。`-a` 显示所有条目，`-d` 删除条目，`-s` 静态绑定。

    ``` bat
    arp -a
    arp -a 192.168.1.1
    ```

`route`
:   查看或修改本机路由表。`print` 显示，`add` 添加，`delete` 删除，`change` 修改。

    ``` bat
    route print
    route add 10.0.0.0 mask 255.0.0.0 192.168.1.254
    ```

`pathping`
:   结合 `ping` 和 `tracert`，统计每跳节点的丢包率与延迟（采集时间较长）。

    ``` bat
    pathping google.com
    ```

---

## 进程与服务

查看运行中的进程、终止进程及管理 Windows 服务。

`tasklist`
:   列出当前运行的进程。`/fi` 过滤条件，`/fo` 输出格式（TABLE/LIST/CSV），`/v` 详细信息，`/svc` 显示各进程托管的服务。

    ``` bat
    tasklist
    tasklist /fi "imagename eq chrome.exe"
    tasklist /fi "pid eq 1234"
    ```

`taskkill`
:   终止进程。`/pid` 按进程 ID，`/im` 按映像名称，`/f` 强制终止，`/t` 同时终止子进程。

    ``` bat
    taskkill /pid 1234 /f
    taskkill /im notepad.exe /f /t
    ```

`sc`
:   管理 Windows 服务（Service Control）。`query` 查询状态，`start/stop` 启停，`config` 修改配置，`create/delete` 创建/删除服务。

    ``` bat
    sc query
    sc query type= service state= running
    sc stop "服务名"
    sc start "服务名"
    sc config "服务名" start= auto
    ```

`net start` / `net stop`
:   启动或停止 Windows 服务（比 `sc` 更简洁）。

    ``` bat
    net start "Windows Update"
    net stop "Windows Update"
    net start               :: 列出所有已启动的服务
    ```

`wmic process`
:   通过 WMI 查询进程详情，支持丰富的过滤与格式化输出。

    !!! warning "已弃用"
        `wmic` 在 Windows 11 22H2+ 中已被微软标记为弃用，执行时会出现警告。
        建议改用 PowerShell：`Get-Process`、`Get-WmiObject` 或 `Get-CimInstance`。

    ``` bat
    wmic process list brief
    wmic process where "name='chrome.exe'" get processid,commandline
    wmic process where "processid=1234" call terminate
    ```

---

## 注册表操作

通过命令行读写 Windows 注册表，常用于脚本自动化配置。

!!! warning "注意事项"
    修改注册表前建议先导出备份。错误的注册表修改可能导致系统不稳定。

`reg query`
:   查询注册表键或值。`/v` 指定值名，`/s` 递归子键，`/f` 搜索匹配字符串。

    ``` bat
    reg query HKCU\Software\Microsoft\Windows\CurrentVersion\Run
    reg query HKLM\SYSTEM\CurrentControlSet\Services /s /f "ImagePath"
    ```

`reg add`
:   添加或修改注册表键/值。`/v` 值名，`/t` 数据类型（REG_SZ/REG_DWORD 等），`/d` 数据，`/f` 强制覆盖。

    ``` bat
    reg add HKCU\Software\MyApp /v Setting /t REG_SZ /d "value" /f
    reg add HKLM\SOFTWARE\MyApp /v Count /t REG_DWORD /d 10 /f
    ```

`reg delete`
:   删除注册表键或值。`/v` 删除指定值，`/va` 删除所有值，`/f` 不提示确认。

    ``` bat
    reg delete HKCU\Software\MyApp /v Setting /f
    reg delete HKCU\Software\OldApp /f
    ```

`reg export`
:   将注册表键导出为 `.reg` 文件（用于备份或迁移）。

    ``` bat
    reg export HKCU\Software\MyApp C:\backup\myapp.reg
    ```

`reg import`
:   从 `.reg` 文件导入注册表数据。

    ``` bat
    reg import C:\backup\myapp.reg
    ```

---

## 系统信息

查询操作系统版本、环境变量、主机信息及硬件详情。

`systeminfo`
:   显示系统详细信息（OS 版本、补丁、内存、网卡等）。`/fo` 指定格式（TABLE/LIST/CSV）。

    ``` bat
    systeminfo
    systeminfo /fo csv > sysinfo.csv
    ```

`ver`
:   输出 Windows 版本号（简短）。

    ``` bat
    ver
    ```

`date`
:   显示或设置系统日期。不带参数则显示当前日期并等待输入新日期（按 Enter 取消）。

    ``` bat
    date /t      :: 只显示，不提示修改
    ```

`time`
:   显示或设置系统时间。`/t` 只显示不修改。

    ``` bat
    time /t
    ```

`set`
:   显示、设置或删除环境变量。不带参数列出所有变量，`set VAR=value` 设置，`set VAR=` 删除，`set /p VAR=提示` 从输入读取。在批处理脚本中的变量定义与算术用法，参见「批处理基础」章节。

    ``` bat
    set
    set PATH
    set MYVAR=hello
    set /p NAME=请输入你的名字：
    ```

`where`
:   定位可执行文件的完整路径（类似 Linux `which`）。`/r` 在指定目录递归搜索。

    ``` bat
    where java
    where python
    where /r C:\ notepad.exe
    ```

`whoami`
:   显示当前登录用户的用户名和域名。`/priv` 显示特权信息，`/groups` 显示所属组。

    ``` bat
    whoami
    whoami /priv
    whoami /groups
    ```

`hostname`
:   显示本机计算机名。

    ``` bat
    hostname
    ```

`wmic`
:   Windows Management Instrumentation 命令行工具，可查询硬件、软件、OS 等几乎所有系统信息。

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

## 用户与权限

本地用户/组管理及文件访问控制列表（ACL）操作。

`net user`
:   查看或管理本地用户账户。需管理员权限修改。

    ``` bat
    net user                         :: 列出所有用户
    net user 用户名                  :: 查看指定用户详情
    net user 用户名 密码 /add        :: 新建用户
    net user 用户名 /delete          :: 删除用户
    net user 用户名 新密码           :: 修改密码
    net user 用户名 /active:no       :: 禁用账户
    ```

`net localgroup`
:   管理本地用户组。

    ``` bat
    net localgroup                         :: 列出所有本地组
    net localgroup Administrators          :: 查看管理员组成员
    net localgroup Administrators 用户名 /add    :: 加入管理员组
    net localgroup Administrators 用户名 /delete :: 移出管理员组
    ```

`icacls`
:   查看或修改文件/目录的访问控制列表（推荐，取代 `cacls`）。`/grant` 授权，`/deny` 拒绝，`/remove` 移除，`/t` 递归，`/inheritance` 继承设置。

    ``` bat
    icacls C:\mydir
    icacls C:\mydir /grant Users:(OI)(CI)F /t    :: 递归授予完全控制
    icacls C:\secret /deny Everyone:(F)           :: 拒绝所有人访问
    icacls C:\mydir /reset /t                     :: 重置为继承权限
    ```

`cacls`
:   旧版 ACL 工具（Windows XP 时代），新版系统推荐改用 `icacls`。

    ``` bat
    cacls C:\mydir
    ```

`runas`
:   以其他用户身份运行程序（类似 Linux `sudo`）。`/user` 指定用户，`/savecred` 保存凭据。

    ``` bat
    runas /user:Administrator cmd
    runas /user:DOMAIN\admin "notepad C:\Windows\system32\drivers\etc\hosts"
    ```

---

## 批处理基础

`.bat` / `.cmd` 脚本中的核心控制结构与内置命令。

`@echo off`
:   关闭命令回显（脚本首行惯例），避免每条命令都被打印到屏幕。`@` 表示不回显本行本身。

    ``` bat
    @echo off
    echo 这行会显示，但命令本身不显示
    ```

`echo`
:   输出文本到控制台，或输出空行 `echo.`，或重定向到文件。`echo on/off` 切换回显。

    ``` bat
    echo Hello, World!
    echo.
    echo 写入文件 > output.txt
    echo 追加文件 >> output.txt
    ```

`rem`
:   注释行，`::` 也可作注释（速度略快，但不能用于代码块内）。

    ``` bat
    rem 这是注释
    :: 这也是注释
    ```

`set`
:   在批处理中定义变量。使用 `%VAR%` 引用。`/a` 执行算术运算，`/p` 从用户输入读取。

    ``` bat
    set NAME=World
    echo Hello, %NAME%!
    set /a RESULT=1+2*3
    echo %RESULT%
    ```

`if`
:   条件判断。支持字符串比较、数字比较、文件存在、错误码检测。`not` 取反，`else` 分支（需在同一行或括号内）。

    ``` bat
    if "%1"=="" (echo 缺少参数) else (echo 参数为：%1)
    if exist C:\log.txt del C:\log.txt
    if %ERRORLEVEL% neq 0 (echo 上条命令失败，退出码：%ERRORLEVEL%)
    if /i "%OS%"=="Windows_NT" echo 这是 Windows NT 系列
    ```

`for`
:   循环结构。`%%i` 在脚本中（命令行用 `%i`），`/l` 数字范围，`/f` 解析文件/字符串，`/d` 仅目录，`/r` 递归文件。

    ``` bat
    :: 遍历文件
    for %%f in (*.txt) do echo %%f

    :: 数字循环（1 到 5，步长 1）
    for /l %%i in (1,1,5) do echo %%i

    :: 解析文件内容（按行读取）
    for /f "delims=" %%line in (input.txt) do echo %%line
    ```

`goto`
:   跳转到指定标签（`:标签名`）。`:eof` 是内置标签，跳转后退出当前脚本/子程序。

    ``` bat
    goto start
    :start
    echo 从这里开始执行
    goto :eof
    ```

`call`
:   调用另一个批处理脚本或子程序（标签），执行完毕后返回调用处。

    ``` bat
    call other.bat
    call :myFunc arg1 arg2
    goto :eof

    :myFunc
    echo 子程序参数：%1 %2
    exit /b
    ```

`pause`
:   暂停执行并显示"请按任意键继续..."，等待用户按键。常用于脚本末尾。

    ``` bat
    pause
    ```

`%ERRORLEVEL%`
:   上一条命令的退出状态码。`0` 通常表示成功，非 `0` 表示失败。可用 `exit /b 退出码` 在子程序中设置。

    ``` bat
    ping -n 1 google.com
    if %ERRORLEVEL% equ 0 (echo 网络正常) else (echo 网络不通)
    ```

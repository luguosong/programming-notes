---
title: 源码编译安装
---

# 源码编译安装

当你想使用一个软件时，通常首先想到的是 `apt install` 或 `dnf install`。但有时候，官方仓库里的版本太旧，或者你需要开启某个默认关闭的功能模块——这时就只能自己动手编译了。

**本文你会学到**：

- 什么情况下需要源码编译，以及编译前如何准备工具链
- 经典的"三步走"：`./configure` → `make` → `make install`
- 动态库的搜索机制与 `ldconfig` 的使用
- CMake 项目的编译流程
- 源码安装的软件如何卸载
- 常见编译报错的排查方法

## 为什么需要源码编译

不是每次都能从包管理器直接安装，以下场景往往需要自行编译：

- **官方仓库版本过旧**：比如你需要 OpenSSL 3.2 的新特性，但系统仓库只有 3.0
- **需要自定义编译选项**：启用某个默认关闭的模块（如 Nginx 的 `--with-http_v2_module`）
- **软件尚无预编译包**：刚发布的版本、内部工具、研究性项目
- **学习编译原理**：了解 C/C++ 程序从源码到可执行文件的完整过程

## 编译前准备

### 安装编译工具链

编译 C/C++ 程序至少需要：编译器（`gcc`/`g++`）、链接工具（`ld`）、构建工具（`make`）。不同发行版的安装命令如下：

=== "Debian/Ubuntu"

    ``` bash title="安装编译工具链（Debian/Ubuntu）"
    # build-essential 包含 gcc、g++、make、libc-dev 等
    apt install build-essential

    # 自动配置工具（configure 脚本依赖）
    apt install autoconf automake

    # 库路径查找工具
    apt install pkg-config
    ```

=== "Red Hat/RHEL"

    ``` bash title="安装编译工具链（RHEL/CentOS/Fedora）"
    # Development Tools 组包含 gcc、g++、make、binutils 等
    dnf groupinstall "Development Tools"

    # 自动配置工具
    dnf install autoconf automake

    # 库路径查找工具
    dnf install pkgconfig
    ```

!!! tip "验证工具链是否就绪"

    ``` bash
    gcc --version     # 应输出 gcc 版本号
    make --version    # 应输出 GNU Make 版本号
    ```

### 查找依赖库与头文件

编译软件时经常需要某个库的开发包（包含头文件 `.h` 和静态库 `.a`）。Debian 系的开发包后缀是 `-dev`，RHEL 系是 `-devel`。

``` bash title="查找依赖库信息"
# 查看 OpenSSL 的编译与链接参数
pkg-config --libs --cflags libssl

# 查看系统已缓存的动态库
ldconfig -p | grep libssl
```

``` bash title="根据头文件反查应安装的包"
apt-file search stdio.h       # Debian（需先 apt install apt-file）
dnf provides "*/stdio.h"      # RHEL
```

## 标准三步走

绝大多数使用 `autoconf` 生成配置脚本的 C/C++ 项目，都遵循"配置 → 编译 → 安装"三个步骤。

``` bash title="完整的源码编译安装流程"
# 下载并解压源码包（解压到 /usr/local/src 是惯例）
cd /usr/local/src
wget https://example.com/software-1.0.tar.gz
tar xvf software-1.0.tar.gz
cd software-1.0/

# 步骤一：配置，生成 Makefile
./configure --prefix=/usr/local

# 步骤二：编译
make -j$(nproc)       # -j$(nproc) 使用所有 CPU 核心并行编译，更快

# 步骤三：安装到 --prefix 指定目录
sudo make install
```

每一步都必须成功才能进入下一步——`./configure` 失败则 Makefile 无法生成，后续步骤全部无法执行。

### configure 选项速查

`./configure` 脚本负责侦测当前系统环境，并生成适配当前平台的 `Makefile`。最重要的参数是 `--prefix`，它决定软件最终安装到哪个目录。

``` bash title="常用 configure 参数"
./configure \
  --prefix=/usr/local \         # 安装目录（默认 /usr/local）
  --sysconfdir=/etc \           # 配置文件目录
  --localstatedir=/var \        # 运行时数据目录
  --enable-ssl \                # 启用 SSL 支持
  --disable-ipv6 \              # 禁用 IPv6
  --with-openssl=/usr \         # 指定 OpenSSL 安装路径
  --without-python              # 不编译 Python 绑定

# 查看所有支持的选项
./configure --help | less
```

!!! tip "安装到独立目录便于卸载"

    建议给每个软件指定独立的 `--prefix`，例如 `--prefix=/usr/local/nginx`。
    这样所有文件都集中在同一目录，卸载时直接 `rm -rf /usr/local/nginx` 即可，
    而不会与其他软件的文件混在一起难以追踪。

### 常用 make 目标

``` bash title="make 常用目标"
make                   # 编译（默认目标 all）
make -j$(nproc)        # 并行编译，显著缩短时间

make install           # 安装
make uninstall         # 卸载（如果 Makefile 支持）

make clean             # 清除 .o 等中间文件
make distclean         # 清除编译产物 + configure 生成的文件

make check             # 运行测试套件（可选，验证编译正确性）

# 安装到临时目录（打包成 .deb/.rpm 时常用）
make DESTDIR=/tmp/pkg install
```

## 动态库机制

### 库文件命名规则

- **动态库**：`libXXX.so.主版本.次版本`，例如 `libssl.so.3`
- **静态库**：`libXXX.a`

链接时用 `-lXXX` 表示链接 `libXXX.so`（省略 `lib` 前缀和 `.so` 后缀）。

### 动态库搜索路径

程序运行时，系统按以下顺序查找动态库：

1. `LD_LIBRARY_PATH` 环境变量（临时，优先级最高）
2. `/etc/ld.so.conf` 及 `/etc/ld.so.conf.d/*.conf` 中配置的目录
3. 系统默认目录：`/lib`、`/lib64`、`/usr/lib`、`/usr/lib64`

``` bash title="动态库常用操作"
# 查看程序依赖哪些动态库
ldd /usr/bin/nginx
ldd /usr/local/bin/myapp

# 临时指定库路径（不修改系统配置）
LD_LIBRARY_PATH=/usr/local/lib ./myapp

# 更新动态库缓存（修改 /etc/ld.so.conf.d/ 后必须执行）
ldconfig
ldconfig -v    # 显示加载的库列表
```

### 将自编译库加入系统

``` bash title="将 /usr/local/lib 加入动态库搜索路径"
echo "/usr/local/lib" > /etc/ld.so.conf.d/local.conf
ldconfig
```

## CMake 项目

许多现代 C/C++ 项目（如 OpenCV、CMake 自身）使用 CMake 代替 autoconf，流程稍有不同：

``` bash title="CMake 标准编译流程"
# 安装 cmake
apt install cmake      # Debian/Ubuntu
dnf install cmake      # RHEL

# 创建独立构建目录（避免污染源码目录）
mkdir build && cd build

# 配置（等价于 ./configure）
cmake .. -DCMAKE_INSTALL_PREFIX=/usr/local

# 编译（等价于 make -j N）
cmake --build . -j$(nproc)

# 安装
sudo cmake --install .
```

`-D` 前缀用于传递配置变量，常用的有：

| 变量 | 含义 |
|------|------|
| `CMAKE_INSTALL_PREFIX` | 安装目录（等同 `--prefix`） |
| `CMAKE_BUILD_TYPE` | 构建类型：`Release` / `Debug` / `RelWithDebInfo` |
| `BUILD_SHARED_LIBS` | `ON` 构建动态库，`OFF` 构建静态库 |

## 卸载源码安装的软件

源码安装的软件没有包管理器跟踪，卸载方式取决于当初的安装方式。

**方法一：make uninstall**（部分软件支持）

``` bash
cd /usr/local/src/software-1.0/
make uninstall
```

**方法二：安装到独立目录，卸载时直接删除**

``` bash
./configure --prefix=/usr/local/software-1.0
# 卸载时：
rm -rf /usr/local/software-1.0
```

**方法三：checkinstall（Debian 推荐）**

`checkinstall` 将 `make install` 的结果打包为系统包（`.deb` 或 `.rpm`），之后可用包管理器卸载：

``` bash title="用 checkinstall 代替 make install"
apt install checkinstall    # Debian/Ubuntu

# 编译完成后，用 checkinstall 替代 make install
checkinstall --pkgname=myapp --pkgversion=1.0

# 之后可以通过包管理器卸载
dpkg -r myapp
```

## 编译问题排查

### 常见报错速查表

| 错误信息 | 原因 | 解决方法 |
|---------|------|---------|
| `configure: error: cannot find xxx` | 缺少依赖库 | `apt install libxxx-dev` 或 `dnf install libxxx-devel` |
| `fatal error: xxx.h: No such file` | 缺少头文件（开发包未安装）| 安装对应 `-dev`/`-devel` 包 |
| `undefined reference to 'xxx'` | 链接时找不到库符号 | 检查 `-l` 参数，或设置 `LDFLAGS=-L/path` |
| `Permission denied` during install | 安装目录无写权限 | 使用 `sudo make install` |
| `make: command not found` | 未安装 make | 安装开发工具组 |

!!! warning "依赖包命名差异"

    Debian/Ubuntu 的开发包后缀是 `-dev`（如 `libssl-dev`），
    RHEL/CentOS/Fedora 的后缀是 `-devel`（如 `openssl-devel`）。

### 调试编译过程

``` bash title="查看详细编译命令"
make V=1         # autoconf 项目：显示完整的 gcc 命令
make VERBOSE=1   # CMake 项目：显示完整的编译命令
```

## 实战：编译安装 Nginx

以 Nginx 为例，演示带自定义模块的完整编译流程：

``` bash title="编译安装 Nginx 1.26"
# 安装依赖
apt install libpcre3-dev zlib1g-dev libssl-dev    # Debian/Ubuntu
# dnf install pcre-devel zlib-devel openssl-devel  # RHEL

# 下载解压
wget http://nginx.org/download/nginx-1.26.0.tar.gz
tar xvf nginx-1.26.0.tar.gz && cd nginx-1.26.0

# 配置（启用常用功能模块）
./configure \
  --prefix=/usr/local/nginx \
  --with-http_ssl_module \
  --with-http_v2_module \
  --with-http_gzip_static_module

# 编译安装
make -j$(nproc)
sudo make install

# 验证安装结果
/usr/local/nginx/sbin/nginx -V
```

!!! tip "如何查找 Nginx 支持哪些模块"

    运行 `./configure --help` 可以看到所有可用的 `--with-xxx` 和 `--without-xxx` 选项，
    每个模块后面都有简短说明。


## 创建与使用共享库

当多个程序依赖同一段功能代码时，如果把代码静态编译进每个可执行文件，会造成磁盘和内存的重复浪费。共享库（`.so`，Shared Object）解决了这个问题：多个进程在运行时共享内存中同一份代码，操作系统只需加载一次；更新库文件后，所有依赖它的程序下次启动就会自动使用新版本，无需重新编译。

### 共享库 vs 静态库

| 特性 | 静态库（`.a`）| 共享库（`.so`）|
|------|-------------|--------------|
| 链接时机 | 编译时 | 运行时 |
| 可执行文件大小 | 更大 | 更小 |
| 内存占用 | 每个进程各自一份 | 所有进程共享一份 |
| 更新方式 | 需重新编译程序 | 只需替换 `.so` 文件 |
| 运行时依赖 | 无 | 需要 `.so` 文件存在 |

### 命名规范与 SONAME

共享库的文件名遵循严格的三层命名规范，这是实现版本共存的基础：

- **实际文件名**：`libfoo.so.1.2.3`（库名.so.主版本.次版本.修订号）
- **SONAME**：`libfoo.so.1`（只含主版本号，是运行时动态链接器真正查找的名字）
- **链接名**：`libfoo.so`（编译时 `-lfoo` 查找的名字）

三者通过符号链接形成调用链：

```
libfoo.so → libfoo.so.1 → libfoo.so.1.2.3
```

`ldconfig` 负责自动创建和维护 SONAME 符号链接，链接名通常由包管理器或开发者手动创建。

### 创建共享库

``` bash title="创建共享库完整流程"
# 1. 编译为位置无关代码（Position Independent Code，PIC）
#    -fPIC 使生成的代码可加载到任意内存地址
gcc -fPIC -c mylib.c -o mylib.o

# 2. 链接为共享库，通过 -Wl,-soname 嵌入 SONAME
gcc -shared -Wl,-soname,libmylib.so.1 -o libmylib.so.1.0.0 mylib.o

# 3. 手动创建符号链接（开发环境用）
ln -s libmylib.so.1.0.0 libmylib.so.1    # SONAME 链接（运行时使用）
ln -s libmylib.so.1 libmylib.so           # 链接名（编译时使用）

# 4. 安装到系统目录后更新 ld.so 缓存
sudo cp libmylib.so.1.0.0 /usr/local/lib/
sudo ldconfig   # 自动创建 SONAME 链接并重建缓存
```

!!! info "为什么需要 -fPIC"

    普通编译产生的代码使用绝对地址引用数据和函数，只能加载到固定内存位置。
    多个进程使用同一共享库时各自需要不同的加载地址，`-fPIC` 使代码改用相对地址
    （通过 Global Offset Table），从而可以映射到任意虚拟地址，实现真正的内存共享。

### 使用共享库编译程序

``` bash title="链接并运行使用共享库的程序"
# 编译时指定库路径和库名
gcc -o myapp main.c -L/usr/local/lib -lmylib

# 如果运行时找不到库，可临时设置搜索路径
LD_LIBRARY_PATH=/usr/local/lib ./myapp
```

### 库搜索路径与 ldconfig

动态链接器（`ld.so`）在加载共享库时按以下顺序搜索：

1. `LD_LIBRARY_PATH` 环境变量指定的路径
2. 可执行文件 ELF 头中嵌入的 `rpath`（编译时通过 `-Wl,-rpath` 指定）
3. `/etc/ld.so.cache`（由 `ldconfig` 根据 `/etc/ld.so.conf` 构建的缓存）
4. 默认路径：`/lib`、`/usr/lib`

添加自定义库路径的规范做法：

``` bash title="添加自定义库路径到系统缓存"
# 创建配置文件（每行一个路径）
echo "/usr/local/lib" | sudo tee /etc/ld.so.conf.d/mylib.conf

# 重建缓存（必须执行，否则新路径不生效）
sudo ldconfig

# 查看缓存中所有已知库
ldconfig -p

# 重建缓存时显示处理了哪些库
ldconfig -v
```

!!! warning "LD_LIBRARY_PATH 的局限性"

    `LD_LIBRARY_PATH` 只是临时方案，适合开发调试。生产环境应将库安装到
    标准路径并执行 `ldconfig`，或在编译时嵌入 `rpath`。
    此外，`setuid`/`setgid` 程序会完全忽略 `LD_LIBRARY_PATH`，以防权限提升攻击。

### 调试工具

当程序找不到共享库或出现符号冲突时，以下工具可以快速定位问题：

- `ldd myapp`：列出程序依赖的所有共享库及其解析路径，`not found` 表示缺失
- `ldd -v myapp`：显示详细版本依赖信息
- `readelf -d myapp | grep NEEDED`：直接读取 ELF 头中的 `NEEDED` 条目，不实际加载库
- `nm -D libmylib.so`：查看共享库导出的动态符号（函数和变量名）
- `objdump -p libmylib.so | grep SONAME`：查看库中嵌入的 SONAME

!!! tip "ldd 的安全注意事项"

    `ldd` 实际上会执行目标程序（通过设置环境变量触发动态链接器输出信息），
    对不可信的二进制文件应改用 `readelf -d` 来查看依赖，避免意外执行恶意代码。

### 版本管理与库升级

SONAME 机制使不同主版本的共享库可以在系统上共存，互不干扰：

- **主版本号变更**（接口不兼容）→ 旧程序继续链接 `libfoo.so.1`，新程序链接 `libfoo.so.2`，两个版本同时存在于系统中
- **次版本号/修订变更**（向后兼容）→ 只需替换 `libfoo.so.1.x.y` 文件，SONAME 符号链接不变，所有依赖该库的程序下次启动自动使用新版本

``` bash title="升级共享库（次版本兼容升级）"
# 安装新版本文件
sudo cp libfoo.so.1.3.0 /usr/local/lib/

# 更新 SONAME 符号链接（或让 ldconfig 自动处理）
sudo ldconfig

# 验证符号链接是否正确更新
ls -la /usr/local/lib/libfoo.so*
```

### LD_PRELOAD：运行时函数替换

`LD_PRELOAD` 是动态链接器提供的一种机制，允许在所有其他共享库之前加载指定的库，从而覆盖标准库中的函数实现。

``` bash title="使用 LD_PRELOAD 替换 malloc"
# 用自定义内存分配库替换系统 malloc，方便调试内存问题
LD_PRELOAD=/path/to/custom_malloc.so ./myapp

# 多个库用冒号分隔
LD_PRELOAD=/lib/libasan.so:/path/to/custom.so ./myapp
```

常见用途包括：内存调试（`valgrind`、AddressSanitizer）、性能分析、测试时 mock 系统调用等。

!!! warning "LD_PRELOAD 的安全限制"

    出于安全考虑，`setuid`/`setgid` 程序会完全忽略 `LD_PRELOAD`，
    防止低权限用户通过注入代码获得提升后的权限。

---
title: 内存管理
---

# 内存管理

**本文你会学到**：

- 进程的虚拟内存布局（Text、Data、BSS、Heap、Stack）
- `malloc`/`free` 的使用与实现原理
- `brk`/`sbrk` 底层机制与 program break
- `mmap` 匿名映射与文件映射
- `mprotect` 修改内存保护、`mlock` 内存锁
- `madvise` 提示内核优化内存使用
- 内存分析工具（`pmap`、`vmstat`、`top` 等）

## 程序的内存布局

### 进程虚拟地址空间

每个 Linux 进程看到的是一片连续的虚拟地址空间，内核和 MMU 负责将其映射到物理内存。对于一个典型的 C 程序，虚拟内存从低地址到高地址分为以下几个段：

``` mermaid
graph TD
    subgraph 高地址
        STACK["栈（Stack）<br>局部变量、函数调用<br>向下增长"]
        GAP[""]
        HEAP["堆（Heap）<br>动态分配（malloc）<br>向上增长"]
    end
    subgraph 数据段
        BSS["BSS 段<br>未初始化的全局/静态变量<br>（运行时清零）"]
        DATA["Data 段<br>已初始化的全局/静态变量"]
    end
    subgraph 代码段
        TEXT["Text 段<br>程序机器指令<br>只读"]
    end
    subgraph 固定
        FIXED["0x00000000<br>保留（NULL 指针陷阱）"]
    end

    FIXED --> TEXT
    TEXT --> DATA
    DATA --> BSS
    BSS --> HEAP
    HEAP --> GAP
    GAP --> STACK

    classDef regular fill:transparent,stroke:#0288d1,color:#adbac7,stroke-width:1px
    classDef highlight fill:transparent,stroke:#f57c00,color:#adbac7,stroke-width:2px
    class HEAP,STACK highlight
    class TEXT,DATA,BSS,FIXED,GAP regular
```

各段的作用：

| 段 | 存储内容 | 特点 |
|----|---------|------|
| **Text（代码段）** | 程序的机器指令 | 只读，可共享，禁止修改 |
| **Data（数据段）** | 已初始化的全局变量和静态变量 | 编译时确定值，可读写 |
| **BSS（Block Started by Symbol）** | 未初始化的全局/静态变量 | 程序加载时由内核清零 |
| **Heap（堆）** | 运行时动态分配的内存 | 向高地址增长，由 `brk`/`sbrk`/`mmap` 管理 |
| **Stack（栈）** | 局部变量、函数参数、返回地址 | 向低地址增长，自动分配释放 |

可以通过以下命令查看可执行文件的段信息：

``` bash title="查看程序段信息"
size /bin/bash   # 查看 Text/Data/BSS 段大小
readelf -S /bin/bash  # 查看详细段信息
```

???+ info "`size` 命令输出示例"

    ``` bash
    $ size /bin/bash
       text    data     bss     dec     hex filename
    1020650   47480   32800 1100930  10cb02 /bin/bash
    ```

运行时可以通过 `/proc/PID/maps` 查看进程实际的内存布局：

``` bash title="查看进程内存映射"
cat /proc/self/maps  # 查看当前 shell 的内存映射
pmap $$              # pmap 命令封装了 /proc/PID/maps
```

输出中的每一行对应一个虚拟内存区域，格式为：

```
地址范围           权限  偏移量    设备     i-node  路径
555555554000-555555556000 r--p 00000000 08:01 1234567 /bin/bash
```

权限字段含义：`r`=可读、`w`=可写、`x`=可执行、`p`=私有（Copy-on-Write）、`s`=共享。

## 堆内存分配

### malloc/free 基础

C 程序在堆上分配内存最常用的接口是 `malloc` 函数族：

``` c title="malloc 函数族声明"
#include <stdlib.h>

void *malloc(size_t size);        // 分配 size 字节，不初始化
void *calloc(size_t nmemb, size_t size); // 分配并清零
void *realloc(void *ptr, size_t size);   // 调整已分配内存的大小
void free(void *ptr);             // 释放内存
```

`malloc` 返回 `void*`，可以赋给任意类型的指针。分配的内存未经初始化：

``` c title="malloc 使用示例"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main(void) {
    int *arr = malloc(10 * sizeof(int));
    if (arr == NULL) {
        perror("malloc");
        exit(EXIT_FAILURE);
    }

    // 使用内存
    for (int i = 0; i < 10; i++) {
        arr[i] = i * i;
    }

    free(arr);  // ❗ 必须释放，否则内存泄漏
    return 0;
}
```

关键规则：

- `malloc` 失败返回 `NULL`，**必须检查返回值**
- `free(NULL)` 是安全的（什么都不做）
- 释放后继续使用指针（dangling pointer）会导致未定义行为
- **禁止**：多次释放同一块内存、释放非 `malloc` 返回的指针

???+ warning "分配失败处理"

    ``` c
    // ✅ 正确：检查 malloc 返回值
    int *p = malloc(1024 * 1024 * 100); // 尝试分配 100MB
    if (p == NULL) {
        fprintf(stderr, "内存分配失败\n");
        exit(EXIT_FAILURE);
    }

    // ❌ 错误：不检查返回值
    int *q = malloc(100);
    // 如果 q 为 NULL，下面这行会导致段错误
    *q = 42;
    ```

### calloc 和 realloc

**calloc**：为数组分配内存并自动初始化为 0：

``` c
// 分配 10 个 int，全部初始化为 0
int *arr = calloc(10, sizeof(int));
```

**realloc**：调整已分配内存块的大小：

``` c
int *arr = malloc(10 * sizeof(int));
// 扩展为 20 个 int
int *tmp = realloc(arr, 20 * sizeof(int));
if (tmp == NULL) {
    // realloc 失败，原内存不变
    perror("realloc");
    free(arr);
    exit(EXIT_FAILURE);
}
arr = tmp;  // ✅ 先检查再赋值
```

注意：`realloc` 可能会移动内存块，导致原指针失效。**不要直接将返回值赋给原指针**，否则 `realloc` 失败时原指针变为 `NULL`，造成内存泄漏。

### alloca：栈上分配

`alloca` 从栈上分配内存，函数返回时自动释放，速度比 `malloc` 快得多：

``` c
#include <alloca.h>

void func(void) {
    int *tmp = alloca(100 * sizeof(int));
    // 无需调用 free()
    // 函数返回时自动释放
}
```

限制：

- 不能用于函数参数列表中
- 栈溢出无法通过返回值检测（可能导致 SIGSEGV）
- 不可移植（非 POSIX 标准，但多数 UNIX 支持）

## 底层分配机制

### brk/sbrk：调整 program break

堆的顶部边界称为 **program break**。初始时它与数据段末尾重合。分配堆内存本质上就是上移这个边界：

``` c
#include <unistd.h>

int brk(void *end_data_segment);     // 设置 program break 到指定位置
void *sbrk(intptr_t increment);      // 增加 program break（返回旧地址）
```

- `sbrk(0)` 返回当前 program break 位置（不改变它），用于监控堆大小
- `brk` 和 `sbrk` 是 SUSv2 标记为 Legacy 的接口，SUSv3 已删除
- `malloc` 内部使用 `sbrk` 来分配大块内存，但更倾向于使用 `mmap`（见下文）

### mmap 匿名映射

现代 `malloc` 实现（如 glibc 的 ptmalloc）对于大块内存分配（默认 >128 KB）会使用 `mmap` 创建匿名映射，而非调整 program break。这样做的好处是：

- 释放时可以直接 `munmap` 归还给操作系统
- 不会造成堆碎片
- 不同大小的大块分配互不干扰

``` c
#include <sys/mman.h>

void *addr = mmap(NULL, size, PROT_READ | PROT_WRITE,
                  MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
if (addr == MAP_FAILED) {
    perror("mmap");
    exit(EXIT_FAILURE);
}

// 使用完后释放
munmap(addr, size);
```

### glibc ptmalloc 概述

glibc 的 `malloc` 实现（ptmalloc）采用以下策略：

| 分配大小 | 分配方式 | 释放行为 |
|---------|---------|---------|
| 小内存（默认 ≤128 KB） | `sbrk` 调整堆 | 放入空闲列表，不归还 OS |
| 大内存（默认 >128 KB） | `mmap` 匿名映射 | `munmap` 直接归还 OS |

可通过 `mallopt` 调整这一阈值：

``` c
#include <malloc.h>

// 设置 mmap 分配阈值（单位：字节）
mallopt(M_MMAP_THRESHOLD, 64 * 1024);  // 64 KB 以上使用 mmap
```

## 虚拟内存控制

### mprotect：修改内存保护

`mprotect` 修改一块虚拟内存区域上的访问权限：

``` c
#include <sys/mman.h>

int mprotect(void *addr, size_t length, int prot);
```

- `addr` 必须是系统分页大小（通常 4096 字节）的整数倍
- `prot` 是以下值的位或：`PROT_NONE`（拒绝访问）、`PROT_READ`、`PROT_WRITE`、`PROT_EXEC`
- 违反保护会产生 `SIGSEGV` 信号

``` c title="mprotect 示例：创建只读内存"
#include <sys/mman.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

int main(void) {
    long pagesize = sysconf(_SC_PAGESIZE);
    void *mem = mmap(NULL, pagesize,
                     PROT_READ | PROT_WRITE,
                     MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);

    // 写入数据
    *(char *)mem = 'A';

    // 改为只读
    if (mprotect(mem, pagesize, PROT_READ) == -1) {
        perror("mprotect");
        exit(EXIT_FAILURE);
    }

    // ❌ 下面的写操作会触发 SIGSEGV
    // *(char *)mem = 'B';  // 段错误！

    munmap(mem, pagesize);
    return 0;
}
```

### mlock/mlockall：内存锁

内存锁防止指定内存被交换到磁盘。这对安全敏感数据（如密码）和实时应用很有用：

``` c
#include <sys/mman.h>

int mlock(const void *addr, size_t len);      // 锁定指定区域
int munlock(const void *addr, size_t len);     // 解锁指定区域

int mlockall(int flags);                       // 锁定所有内存
int munlockall(void);                          // 解锁所有
```

`mlockall` 的 `flags` 参数：

| 标志 | 含义 |
|------|------|
| `MCL_CURRENT` | 锁定当前所有已映射的分页 |
| `MCL_FUTURE` | 锁定将来所有映射的分页 |

非特权进程受 `RLIMIT_MEMLOCK` 限制（默认 8 个分页，约 32 KB）：

``` bash title="查看内存锁限制"
ulimit -l       # 查看 RLIMIT_MEMLOCK 值（KB）
```

内存锁的关键语义：

- **不可继承**：`fork` 创建的子进程不继承父进程的内存锁
- **exec 后失效**：`exec` 会丢弃内存锁
- **不叠加**：同一区域多次 `mlock` 只需一次 `munlock` 即可解锁

``` c title="mlock 使用示例：保护密码不被换出"
#include <sys/mman.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>

int main(void) {
    char *password = malloc(64);
    if (password == NULL) {
        perror("malloc");
        exit(EXIT_FAILURE);
    }

    // 锁定包含密码的内存页
    if (mlock(password, 64) == -1) {
        perror("mlock");
        free(password);
        exit(EXIT_FAILURE);
    }

    // 使用密码...
    strcpy(password, "s3cr3t!");
    printf("Password: %s\n", password);

    // 使用完毕后：清零、解锁、释放
    memset(password, 0, 64);
    munlock(password, 64);
    free(password);
    return 0;
}
```

### madvise：建议后续内存使用模式

`madvise` 向内核提供内存使用模式的建议，帮助内核优化 I/O 和缓存行为：

``` c
#include <sys/mman.h>

int madvise(void *addr, size_t length, int advice);
```

| advice 值 | 含义 | 内核优化行为 |
|-----------|------|------------|
| `MADV_NORMAL` | 默认行为 | 适度预读 |
| `MADV_RANDOM` | 随机访问 | 关闭预读，每次只取少量 |
| `MADV_SEQUENTIAL` | 顺序访问一次 | 激进预读，访问后释放 |
| `MADV_WILLNEED` | 即将访问 | 提前加载到内存 |
| `MADV_DONTNEED` | 不再需要 | 丢弃（`MAP_PRIVATE` 下会丢失修改） |

``` c title="madvise 示例：顺序读取文件映射"
#include <sys/mman.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdio.h>

int main(void) {
    int fd = open("largefile.dat", O_RDONLY);
    if (fd == -1) {
        perror("open");
        return 1;
    }

    off_t len = lseek(fd, 0, SEEK_END);
    char *data = mmap(NULL, len, PROT_READ, MAP_PRIVATE, fd, 0);
    close(fd);

    if (data == MAP_FAILED) {
        perror("mmap");
        return 1;
    }

    // 告知内核我们将顺序读取
    madvise(data, len, MADV_SEQUENTIAL);

    // 顺序读取（内核会激进预读）
    long total = 0;
    for (off_t i = 0; i < len; i++) {
        total += data[i];
    }

    // 告知内核不再需要
    madvise(data, len, MADV_DONTNEED);
    munmap(data, len);
    return 0;
}
```

### mincore：检查内存驻留性

`mincore` 报告指定虚拟地址范围内的分页是否驻留在物理内存中：

``` c
#include <sys/mman.h>

int mincore(void *addr, size_t length, unsigned char *vec);
```

- `addr` 必须分页对齐
- `vec` 数组每个字节的最低有效位表示对应分页是否在内存中

``` c title="mincore 使用示例"
#include <sys/mman.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>

int main(void) {
    long pagesize = sysconf(_SC_PAGESIZE);
    size_t len = 32 * pagesize;
    unsigned char *vec = malloc(len / pagesize);

    void *mem = mmap(NULL, len, PROT_READ | PROT_WRITE,
                     MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);

    // 写入一些数据触发分页分配
    memset(mem, 0, len);

    // 检查驻留性
    if (mincore(mem, len, vec) == 0) {
        int resident = 0;
        for (size_t i = 0; i < len / pagesize; i++) {
            if (vec[i] & 0x01) resident++;
        }
        printf("驻留分页：%d / %zu\n", resident, len / pagesize);
    }

    munmap(mem, len);
    free(vec);
    return 0;
}
```

## 内存映射

### mmap：文件映射 vs 匿名映射

`mmap` 可以在进程虚拟地址空间创建两种类型的映射：

| 类型 | 用途 | 示例 |
|------|------|------|
| **文件映射** | 将文件内容映射到内存 | 文件 I/O、共享库加载 |
| **匿名映射** | 分配大块内存（不关联文件） | `malloc` 大内存、进程间共享内存 |

``` c
#include <sys/mman.h>

void *mmap(void *addr, size_t length, int prot, int flags, int fd, off_t offset);
int munmap(void *addr, size_t length);
```

**文件映射示例**：

``` c
#include <sys/mman.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdio.h>

int main(void) {
    int fd = open("hello.txt", O_RDONLY);
    off_t len = lseek(fd, 0, SEEK_END);

    char *content = mmap(NULL, len, PROT_READ, MAP_PRIVATE, fd, 0);
    close(fd);

    if (content == MAP_FAILED) {
        perror("mmap");
        return 1;
    }

    write(STDOUT_FILENO, content, len);
    munmap(content, len);
    return 0;
}
```

### 共享 vs 私有映射

| 标志 | 写操作影响 | 对其他进程可见 | 典型用途 |
|------|-----------|-------------|---------|
| `MAP_PRIVATE` | Copy-on-Write，不写回文件 | 不可见 | 读取配置文件、调试 |
| `MAP_SHARED` | 直接写回文件（或共享内存） | 可见 | 共享内存 IPC、文件 I/O |

``` mermaid
graph LR
    subgraph 进程A
        A_PRIVATE["MAP_PRIVATE<br>写时复制副本"]
        A_SHARED["MAP_SHARED<br>直接写入"]
    end
    subgraph 进程B
        B_PRIVATE["MAP_PRIVATE<br>写时复制副本"]
        B_SHARED["MAP_SHARED<br>直接写入"]
    end
    subgraph 物理内存
        FILE["文件内容<br>（物理页）"]
        PRIVATE_COPY["修改后的副本"]
    end

    A_PRIVATE -- 首次写 --> PRIVATE_COPY
    B_PRIVATE -- 首次写 --> PRIVATE_COPY
    A_SHARED -- 写 --> FILE
    B_SHARED -- 写 --> FILE
    FILE -- 读取 --> A_PRIVATE
    FILE -- 读取 --> B_PRIVATE

    classDef regular fill:transparent,stroke:#0288d1,color:#adbac7,stroke-width:1px
    classDef orange fill:transparent,stroke:#f57c00,color:#adbac7,stroke-width:1px
    classDef green fill:transparent,stroke:#388e3c,color:#adbac7,stroke-width:1px
    class A_PRIVATE,B_PRIVATE,PRIVATE_COPY regular
    class A_SHARED,B_SHARED orange
    class FILE green
```

???+ warning "MAP_PRIVATE 的写时复制"

    写时复制意味着修改 `MAP_PRIVATE` 映射不会影响文件。内核会复制被修改的分页，两个进程各自持有独立副本。这是 `fork` 和共享库加载的核心机制。

## 实用工具

### pmap：查看进程内存映射

``` bash title="pmap 命令"
pmap $$                    # 查看当前 shell 进程的内存映射
pmap -x <PID>              # 查看详细映射（含 RSS/Dirty 信息）
```

输出中包含每个映射的区域地址、大小、权限、映射路径，可以直观了解进程的内存分布。

### vmstat：虚拟内存统计

``` bash title="vmstat 实时监控"
vmstat 1                   # 每秒输出一次系统内存/CPU 状态
vmstat -s                  # 显示内存统计摘要
```

关键列：

| 字段 | 含义 |
|------|------|
| `swapd` | 已使用的交换空间 (KB) |
| `free` | 空闲内存 (KB) |
| `buff` | 缓冲区缓存 (KB) |
| `cache` | 页面缓存 (KB) |
| `si` / `so` | 换入 / 换出速率 (KB/s) |

### top/htop 内存列

``` bash title="top 内存信息"
top                         # 按 Shift+M 以内存排序
```

`top` 中内存相关的关键列：

| 列 | 含义 |
|----|------|
| `VIRT` | 虚拟内存总量（包括未分配物理页的部分） |
| `RES` | 常驻物理内存大小 (RSS) |
| `SHR` | 共享内存大小 |
| `MEM%` | 物理内存使用百分比 |

### smem：更准确的内存统计

`smem` 报告 **PSS（Proportional Set Size）**，比 RSS 更准确地反映共享内存的实际消耗：

``` bash
smem                         # 按 PSS 排序显示进程内存
smem -t -p                   # 显示总和 + 百分比
```

### OOM Killer

当系统内存耗尽时，Linux 内核会调用 **OOM Killer**（Out-Of-Memory Killer）选择并终止一个进程来释放内存。选择策略基于 `oom_score`（权重）：

``` bash title="OOM Killer 相关操作"
# 查看进程的 OOM 分数
cat /proc/<PID>/oom_score

# 调整 OOM 优先级（-1000 禁用 OOM 杀死，+1000 优先被杀死）
echo -500 > /proc/<PID>/oom_score_adj

# 查看 OOM Killer 日志
dmesg | grep -i "killed process"
```

## 选型指南

| 需求 | 推荐 API | 原因 |
|------|---------|------|
| 常规小内存分配 | `malloc` | 标准、易用、自动管理 |
| 数组分配需清零 | `calloc` | 自动初始化 0 |
| 调整已分配大小 | `realloc` | 避免手动复制 |
| 临时小内存 | `alloca` | 自动释放、速度最快 |
| 大块内存（>128 KB） | `mmap` 匿名映射 | 释放时立即归还 OS |
| 文件内容访问 | `mmap` 文件映射 | 简化 I/O、利用页面缓存 |
| 共享内存 IPC | `mmap` `MAP_SHARED` | 高性能、标准化 |
| 保护敏感数据 | `mlock` + `mprotect` | 防换出、防未授权访问 |

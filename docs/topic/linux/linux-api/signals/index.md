---
title: 信号机制
---

# 信号机制

**本文你会学到**：

- 信号的本质与软件中断的概念
- 信号的三个来源：内核、其他进程、键盘输入
- 常见信号的编号与含义（SIGKILL、SIGTERM、SIGSTOP 等）
- 信号的生命周期与默认处理行为
- 用 `signal()` 和 `sigaction()` 安装信号处理器
- 可靠信号与不可靠信号的区别
- 信号掩码与 `sigprocmask()` 的使用
- 信号竞争条件与安全的信号处理
- 从用户态角度使用 `kill` 命令发送信号
- 信号处理中的常见陷阱与最佳实践

## 信号是什么

### 软件中断的本质

信号与硬件中断在概念上非常相似：它们都会打断程序当前的执行流程，迫使进程处理某个事件。不同之处在于，信号是"软件"层面的——由内核、其他进程或进程自身触发，而不是硬件电路。

每个信号都有一个唯一的整数编号和对应的符号名（`SIGxxx`）。由于编号在不同硬件架构上可能不同，编程时**始终使用符号名**，不要硬编码数字。

### 信号的三个来源

**内核产生**：

- 硬件异常：进程执行非法指令（`SIGILL`）、除以零（`SIGFPE`）、访问无效内存（`SIGSEGV`）
- 定时器到期：`alarm()` 触发 `SIGALRM`，`setitimer()` 触发 `SIGVTALRM` / `SIGPROF`
- 软件事件：子进程退出触发 `SIGCHLD`，向已关闭管道写数据触发 `SIGPIPE`

**其他进程发送**：

- 使用 `kill()` 系统调用或 `kill` 命令向目标进程发信号
- 非特权进程只能向同一用户的进程发信号；特权进程（`CAP_KILL`）可发给任何进程

**用户键盘输入**：

- `Ctrl+C` → `SIGINT`（中断前台进程组）
- `Ctrl+Z` → `SIGTSTP`（暂停前台进程组）
- `Ctrl+\` → `SIGQUIT`（退出并产生 core dump）

### 信号的生命周期

```
产生（generated）→ 未决（pending）→ 投递（delivered）
```

信号从产生到投递之间存在一个"未决"窗口。在此期间，信号记录在进程的**等待信号集**中，等待时机投递。

**投递时机**：进程从内核态返回用户态时（系统调用返回、中断处理完毕后），内核检查是否有待投递的信号。

**阻塞（blocking）**：进程可以将某些信号添加到**信号掩码**中，暂时阻止这些信号的投递。被阻塞的信号继续保持未决状态，解除阻塞后才投递。

## 标准信号速查表

Linux 标准信号编号范围 1～31（部分信号编号因架构不同而有差异，括号内标注常见架构差异）：

| 信号名 | 编号（x86） | 默认动作 | 说明 |
|--------|------------|---------|------|
| `SIGHUP` | 1 | term | 终端挂断；常用于通知守护进程重载配置 |
| `SIGINT` | 2 | term | `Ctrl+C`，用户中断 |
| `SIGQUIT` | 3 | core | `Ctrl+\`，产生 core dump 后退出 |
| `SIGILL` | 4 | core | 执行非法（格式错误）的机器语言指令 |
| `SIGTRAP` | 5 | core | 断点调试陷阱，由调试器使用 |
| `SIGABRT` | 6 | core | 调用 `abort()` 触发，产生 core dump |
| `SIGBUS` | 7 | core | 内存访问对齐错误或映射文件越界 |
| `SIGFPE` | 8 | core | 算术异常（除以零、浮点溢出等） |
| `SIGKILL` | 9 | term | **必杀信号，不可捕获/阻塞/忽略** |
| `SIGUSR1` | 10 | term | 用户自定义信号 1 |
| `SIGSEGV` | 11 | core | 段错误：非法内存访问（空指针解引用等） |
| `SIGUSR2` | 12 | term | 用户自定义信号 2 |
| `SIGPIPE` | 13 | term | 写入已无读者的管道/套接字 |
| `SIGALRM` | 14 | term | `alarm()` 实时定时器到期 |
| `SIGTERM` | 15 | term | 礼貌终止请求（**可捕获，用于优雅退出**） |
| `SIGCHLD` | 17 | ignore | 子进程终止、停止或继续时通知父进程 |
| `SIGCONT` | 18 | cont | 使已停止的进程继续执行 |
| `SIGSTOP` | 19 | stop | **必停信号，不可捕获/阻塞/忽略** |
| `SIGTSTP` | 20 | stop | `Ctrl+Z`，终端暂停（可捕获） |
| `SIGTTIN` | 21 | stop | 后台进程尝试从终端读取 |
| `SIGTTOU` | 22 | stop | 后台进程尝试向终端写入（启用 TOSTOP 时） |
| `SIGWINCH` | 28 | ignore | 终端窗口尺寸改变（`vi`/`less` 用此重绘） |
| `SIGXCPU` | 24 | core | 超出 CPU 时间资源限制 |
| `SIGXFSZ` | 25 | core | 超出文件大小资源限制 |

默认动作说明：`term`=终止进程，`core`=产生 core dump 后终止，`stop`=暂停进程，`cont`=恢复进程，`ignore`=忽略。

!!! warning "SIGKILL 与 SIGSTOP"

    这两个信号是无法被应用程序处理的——无法捕获、无法阻塞、无法忽略。`SIGKILL` 由内核强制终止进程；`SIGSTOP` 由内核强制暂停进程。试图用 `sigaction()` 或 `signal()` 为它们设置处理器会失败。

## 发送信号

### kill() 系统调用与 kill 命令

`kill()` 是发送信号的核心系统调用，尽管名字叫"kill"，它可以发送任意信号：

``` c title="kill() 原型"
#include <signal.h>
int kill(pid_t pid, int sig);
```

`pid` 参数决定目标：

| pid 值 | 目标 |
|--------|------|
| `pid > 0` | 发给 PID 为 pid 的进程 |
| `pid == 0` | 发给调用进程所在的整个进程组 |
| `pid == -1` | 广播：发给调用者有权发送的所有进程（init 除外） |
| `pid < -1` | 发给组 ID 为 `abs(pid)` 的进程组中所有进程 |

`sig` 传 0（空信号）可以**检测进程是否存在**，而不实际发送任何信号：

``` c title="检测进程是否存在"
if (kill(pid, 0) == -1 && errno == ESRCH) {
    /* 进程不存在 */
}
```

对应的 shell 命令：

``` bash title="kill 命令示例"
# 发送默认信号 SIGTERM
kill 1234

# 发送 SIGKILL
kill -9 1234
kill -KILL 1234
kill -SIGKILL 1234

# 列出所有信号编号和名称
kill -l

# 发送给整个进程组（加负号）
kill -TERM -1234
```

### killall 与 pkill

``` bash title="按名称或条件发送信号"
# 向所有名为 nginx 的进程发送 SIGHUP
killall -HUP nginx

# pkill 支持正则匹配进程名
pkill -TERM "java.*myapp"

# pgrep 查找匹配进程的 PID（不发信号，只列出）
pgrep -l nginx
```

### raise()：进程向自身发信号

``` c title="raise() 原型"
#include <signal.h>
int raise(int sig);
```

在单线程程序中，等价于 `kill(getpid(), sig)`。调用 `raise()` 时，信号会**立即投递**（在 `raise()` 返回前）。

### alarm()：定时发送 SIGALRM

``` c title="alarm() 原型"
#include <unistd.h>
unsigned int alarm(unsigned int seconds);
```

`alarm(seconds)` 设置一个定时器，`seconds` 秒后向进程发送 `SIGALRM`。传 `0` 取消已有定时器。返回值是上一个定时器的剩余秒数（如果有）。

``` c title="alarm() 用法示例"
/* 为某个操作设置超时 */
alarm(5);           /* 5 秒后触发 SIGALRM */
slow_operation();   /* 如果 5 秒内没完成，会被 SIGALRM 打断 */
alarm(0);           /* 操作完成，取消定时器 */
```

### 键盘信号触发

终端驱动程序会将特殊按键转换为信号，发送给当前**前台进程组**：

| 按键 | 信号 | 默认效果 |
|------|------|---------|
| `Ctrl+C` | `SIGINT` | 中断进程 |
| `Ctrl+Z` | `SIGTSTP` | 暂停进程（可后台用 `fg`/`bg` 恢复） |
| `Ctrl+\` | `SIGQUIT` | 产生 core dump 后退出 |

## 信号处置

### 三种处置方式

每个信号都有一个**处置（disposition）**，决定信号到来时的行为：

1. **默认动作**（`SIG_DFL`）：按照信号的默认行为处理（终止/停止/忽略/core dump）
2. **忽略**（`SIG_IGN`）：内核直接丢弃信号，进程完全不知道信号来过
3. **自定义处理器（handler）**：跳转到用户定义的函数执行，完成后恢复原执行流

### signal()：简单但不推荐

``` c title="signal() 原型"
#include <signal.h>
typedef void (*sighandler_t)(int);
sighandler_t signal(int sig, sighandler_t handler);
```

``` c title="signal() 示例"
void sigint_handler(int sig) {
    /* 注意：这里不能调用 printf！后文会解释原因 */
    write(STDOUT_FILENO, "收到 SIGINT\n", 13);
}

int main(void) {
    signal(SIGINT, sigint_handler);   /* 安装处理器 */
    signal(SIGPIPE, SIG_IGN);         /* 忽略 SIGPIPE */
    /* ... */
}
```

`signal()` 的问题在于：其行为在不同 UNIX 实现之间存在差异（早期实现中处理器执行一次后会被重置为 `SIG_DFL`），可移植性差。**生产代码应使用 `sigaction()`**。

### sigaction()：推荐的可靠方式

``` c title="sigaction() 原型"
#include <signal.h>
int sigaction(int sig, const struct sigaction *act,
              struct sigaction *oldact);
```

`struct sigaction` 结构：

``` c title="sigaction 结构"
struct sigaction {
    void     (*sa_handler)(int);          /* 处理器函数，或 SIG_DFL/SIG_IGN */
    sigset_t   sa_mask;                   /* 处理器执行期间额外阻塞的信号集 */
    int        sa_flags;                  /* 选项标志 */
    void     (*sa_sigaction)(int, siginfo_t *, void *); /* 带额外信息的处理器 */
};
```

``` c title="sigaction() 完整示例"
#include <signal.h>
#include <unistd.h>

void handler(int sig) {
    const char msg[] = "收到信号\n";
    write(STDOUT_FILENO, msg, sizeof(msg) - 1); /* 异步安全 */
}

int main(void) {
    struct sigaction sa;
    sigemptyset(&sa.sa_mask);          /* 初始化为空信号集 */
    sigaddset(&sa.sa_mask, SIGTERM);   /* 处理器执行时额外阻塞 SIGTERM */
    sa.sa_flags = SA_RESTART;          /* 被信号中断的系统调用自动重启 */
    sa.sa_handler = handler;

    if (sigaction(SIGINT, &sa, NULL) == -1) {
        /* 错误处理 */
    }
    pause(); /* 等待信号 */
    return 0;
}
```

常用的 `sa_flags` 标志：

| 标志 | 说明 |
|------|------|
| `SA_RESTART` | 自动重启被信号中断的系统调用（推荐设置） |
| `SA_SIGINFO` | 使用 `sa_sigaction` 三参数处理器，可获取信号来源等额外信息 |
| `SA_NOCLDSTOP` | 仅用于 `SIGCHLD`：子进程停止/继续时不发送该信号 |
| `SA_NODEFER` | 处理器执行期间不自动阻塞当前信号（允许递归） |
| `SA_RESETHAND` | 处理器执行前将处置重置为 `SIG_DFL`（一次性处理器） |

### 异步信号安全函数

信号处理器可能在主程序执行**任意位置**被打断，因此处理器内部只能调用**异步信号安全（async-signal-safe）**函数——即那些可重入（reentrant）、不使用全局状态或锁的函数。

✅ **可以在处理器中安全调用**：

- `write()`、`read()`、`open()`、`close()`（底层 I/O 系统调用）
- `_exit()`（注意不是 `exit()`）
- `signal()`、`sigaction()`、`sigprocmask()`
- `kill()`、`raise()`
- `fork()`、`execve()`

❌ **绝对不能在处理器中调用**（它们不可重入）：

- `printf()`、`fprintf()`——内部使用了 `FILE` 流锁，可能死锁
- `malloc()`、`free()`——堆管理器使用全局锁，可能导致堆损坏
- `exit()`——会刷新 `stdio` 缓冲区，不安全
- `syslog()`、`strtok()` 等使用静态内部状态的函数

!!! tip "信号处理器的设计原则"

    最好的信号处理器只做一件事：**设置一个 `volatile sig_atomic_t` 类型的标志变量**，然后由主循环检查该标志并响应。这样可以把不安全的操作移到主程序中执行。

    ``` c title="推荐的信号处理器模式"
    static volatile sig_atomic_t got_sigterm = 0;

    void sigterm_handler(int sig) {
        got_sigterm = 1; /* 只设置标志，主循环中再处理 */
    }

    /* 主循环 */
    while (!got_sigterm) {
        /* 正常工作 */
    }
    /* 在这里安全地做清理工作 */
    ```

## 信号掩码

### sigprocmask()：阻塞与解除阻塞

有时你需要保证某段关键代码不被信号打断（如修改共享数据结构）。`sigprocmask()` 可以临时阻塞一组信号：

``` c title="sigprocmask() 原型"
#include <signal.h>
int sigprocmask(int how, const sigset_t *set, sigset_t *oldset);
```

`how` 参数：

| how | 效果 |
|-----|------|
| `SIG_BLOCK` | 将 `set` 中的信号**加入**当前掩码（合并阻塞） |
| `SIG_UNBLOCK` | 从当前掩码中**移除** `set` 中的信号 |
| `SIG_SETMASK` | 将当前掩码**替换**为 `set` |

``` c title="临时阻塞信号的标准模式"
sigset_t block_set, old_mask;
sigemptyset(&block_set);
sigaddset(&block_set, SIGINT);
sigaddset(&block_set, SIGTERM);

/* 阻塞 SIGINT 和 SIGTERM，保存旧掩码 */
sigprocmask(SIG_BLOCK, &block_set, &old_mask);

/* 临界区：在此期间信号不会被投递 */
critical_section();

/* 恢复原有掩码，此时挂起的信号会被投递 */
sigprocmask(SIG_SETMASK, &old_mask, NULL);
```

!!! note "SIGKILL 和 SIGSTOP 不可阻塞"

    试图阻塞 `SIGKILL` 或 `SIGSTOP` 会被内核静默忽略，`sigprocmask()` 不会返回错误。阻塞除它们之外的所有信号：

    ``` c title="阻塞所有可阻塞信号"
    sigset_t all_sigs;
    sigfillset(&all_sigs);  /* 包含所有信号，但 SIGKILL/SIGSTOP 的阻塞请求会被忽略 */
    sigprocmask(SIG_BLOCK, &all_sigs, NULL);
    ```

### sigpending()：查询未决信号

被阻塞的信号会进入进程的**等待信号集**，可以用 `sigpending()` 查询：

``` c title="sigpending() 示例"
sigset_t pending_set;
sigpending(&pending_set);

if (sigismember(&pending_set, SIGINT)) {
    /* SIGINT 当前处于未决状态（被阻塞，等待投递） */
}
```

### 标准信号的"非排队"特性

⚠️ 等待信号集是一个**位掩码**，不是队列。如果同一信号在阻塞期间产生了 100 次，解除阻塞后**只会投递一次**。

这是标准信号（1~31）的固有限制。如果需要精确计数，应使用**实时信号**（见后文）。

### 信号集操作函数

``` c title="信号集操作 API"
sigset_t set;
sigemptyset(&set);           /* 初始化为空集 */
sigfillset(&set);            /* 初始化为全集（包含所有信号） */
sigaddset(&set, SIGINT);     /* 向集合中加入 SIGINT */
sigdelset(&set, SIGINT);     /* 从集合中移除 SIGINT */
sigismember(&set, SIGINT);   /* 检查 SIGINT 是否在集合中，返回 1/0 */
```

!!! warning "必须先初始化信号集"

    C 语言不会自动将局部变量清零，**不能**用 `memset(&set, 0, sizeof(set))` 来清空信号集（在某些实现中，信号集的内部表示不是简单的位掩码）。**始终**用 `sigemptyset()` 或 `sigfillset()` 初始化。

## 等待信号

### pause()：暂停直到收到信号

``` c title="pause()"
#include <unistd.h>
int pause(void); /* 始终返回 -1，errno = EINTR */
```

`pause()` 使进程进入睡眠，直到任意信号被投递（且信号未被忽略）。这是最简单的"等待信号"方式。

### 为什么不能用 sigprocmask + pause 的组合

一个看似合理但**有竞争条件**的写法：

``` c title="有竞争条件的错误写法"
/* ❌ 危险：SIGTERM 可能在解除阻塞后、pause() 之前到达 */
sigprocmask(SIG_UNBLOCK, &block_set, NULL); /* 解除阻塞 */
pause();                                     /* 等待信号 — 信号可能已经错过了！ */
```

问题：如果 `SIGTERM` 在 `sigprocmask()` 返回之后、`pause()` 调用之前到达，信号被处理完毕，`pause()` 将永远等待。

### sigsuspend()：原子性地替换掩码并等待

`sigsuspend()` 解决了这个竞争条件，它**原子性地**完成两步：用新掩码替换当前掩码，然后挂起等待信号：

``` c title="sigsuspend() 原型"
#include <signal.h>
int sigsuspend(const sigset_t *mask); /* 始终返回 -1，errno = EINTR */
```

``` c title="正确的等待特定信号模式"
sigset_t block_mask, wait_mask;
sigemptyset(&block_mask);
sigaddset(&block_mask, SIGTERM);

/* 阻塞 SIGTERM，防止在准备阶段被打断 */
sigprocmask(SIG_BLOCK, &block_mask, NULL);

/* 做一些准备工作... */

/* 构造等待时的掩码：允许 SIGTERM 投递 */
sigemptyset(&wait_mask); /* 空掩码 = 不额外阻塞任何信号 */

/* 原子性地：解除对 SIGTERM 的阻塞 + 挂起等待
   信号到达并处理完毕后，sigsuspend 返回，
   同时自动恢复调用前的掩码（block_mask）*/
sigsuspend(&wait_mask);
```

## SIGCHLD 与子进程回收

### 为什么要处理 SIGCHLD

当一个子进程终止时，它会变成**僵尸进程（zombie）**——已退出但内核保留了其进程表项，等待父进程调用 `wait()` 来读取退出状态。不回收的僵尸进程会浪费系统资源（进程表项）。

父进程有两种方式知道子进程退出：

1. **同步等待**：调用 `waitpid()` 阻塞等待
2. **异步通知**：为 `SIGCHLD` 安装处理器，在处理器中调用 `waitpid()`

### 正确的 SIGCHLD 处理器

关键点：**必须循环调用 `waitpid()`**，直到没有更多已结束的子进程。

原因：如果多个子进程同时退出，内核可能只向父进程投递一次 `SIGCHLD`（因为标准信号不排队）。如果只调用一次 `waitpid()`，剩余的僵尸进程就回收不了。

``` c title="正确的 SIGCHLD 处理器"
#include <sys/wait.h>
#include <signal.h>
#include <errno.h>

void sigchld_handler(int sig) {
    int status;
    pid_t child_pid;
    int saved_errno = errno; /* 保存 errno，信号处理器可能破坏它 */

    /* WNOHANG：不阻塞，立即返回；-1：等待任意子进程 */
    while ((child_pid = waitpid(-1, &status, WNOHANG)) > 0) {
        if (WIFEXITED(status)) {
            /* 子进程正常退出，退出码为 WEXITSTATUS(status) */
        } else if (WIFSIGNALED(status)) {
            /* 子进程被信号杀死，信号编号为 WTERMSIG(status) */
        }
    }

    errno = saved_errno; /* 恢复 errno */
}

/* 安装处理器 */
struct sigaction sa;
sa.sa_handler = sigchld_handler;
sigemptyset(&sa.sa_mask);
sa.sa_flags = SA_RESTART | SA_NOCLDSTOP; /* 不关心子进程停止，只关心退出 */
sigaction(SIGCHLD, &sa, NULL);
```

!!! tip "SA_NOCLDSTOP 标志"

    默认情况下，子进程**停止**（被 `SIGSTOP`/`SIGTSTP`）或**继续**（被 `SIGCONT`）也会触发父进程的 `SIGCHLD`。如果只关心子进程的退出，设置 `SA_NOCLDSTOP` 可以屏蔽停止/继续通知，减少不必要的 `waitpid()` 调用。

## 实时信号

### 与标准信号的区别

Linux 提供 `SIGRTMIN` 到 `SIGRTMAX` 范围内的实时信号（至少 32 个，通常 34 个）：

| 特性 | 标准信号（1~31） | 实时信号（SIGRTMIN~SIGRTMAX） |
|------|-----------------|------------------------------|
| 排队 | ❌ 不排队（多个同号信号只保留一个） | ✅ 排队（每个信号都会投递） |
| 优先级 | 无特定顺序 | 编号小的先投递（低编号高优先级） |
| 携带数据 | 只有信号编号 | 可携带一个整数或指针（`sigqueue()`） |
| 标准 | POSIX.1-1990 | POSIX.1b 实时扩展 |

### 发送实时信号

``` c title="sigqueue()：发送带数据的实时信号"
#include <signal.h>
int sigqueue(pid_t pid, int sig, const union sigval value);

union sigval {
    int   sival_int;  /* 整数数据 */
    void *sival_ptr;  /* 指针数据（仅在同进程内有意义） */
};
```

``` c title="接收实时信号携带的数据"
void rt_handler(int sig, siginfo_t *info, void *ucontext) {
    /* info->si_value.sival_int 是发送方传来的整数 */
    int data = info->si_value.sival_int;
}

struct sigaction sa;
sa.sa_sigaction = rt_handler;
sigemptyset(&sa.sa_mask);
sa.sa_flags = SA_SIGINFO;  /* 使用三参数处理器 */
sigaction(SIGRTMIN, &sa, NULL);
```

### 实时信号的典型用途

- **Linux AIO**（异步 I/O）：I/O 完成时通知进程
- **POSIX 定时器**（`timer_create()`）：高精度定时器通知
- **应用级进程间通知**：替代 `SIGUSR1/SIGUSR2`，可排队且可携带数据

## 常见实践

### 优雅关机：先 SIGTERM 后 SIGKILL

正确的进程终止流程：先给进程机会做清理，再强制终止：

``` bash title="优雅终止进程"
PID=12345

# 先发 SIGTERM，给进程清理的机会
kill -TERM $PID

# 等待最多 10 秒
for i in $(seq 1 10); do
    kill -0 $PID 2>/dev/null || break  # 进程已退出则跳出
    sleep 1
done

# 如果还没退出，强制终止
if kill -0 $PID 2>/dev/null; then
    echo "进程未响应 SIGTERM，发送 SIGKILL"
    kill -KILL $PID
fi
```

!!! warning "直接 kill -9 是坏习惯"

    `kill -9`（`SIGKILL`）会跳过进程的所有清理逻辑：临时文件不会删除、数据库连接不会优雅关闭、内存中的数据不会落盘。**总是先尝试 `SIGTERM`，把 `SIGKILL` 作为最后手段。**

### 配置热重载：SIGHUP

nginx、Apache、rsyslog 等守护进程的经典用法——用 `SIGHUP` 触发配置重载，无需重启服务：

``` bash title="nginx 配置热重载"
# 修改配置后，发送 SIGHUP 让 nginx 重载，不中断现有连接
nginx -s reload
# 等价于
kill -HUP $(cat /run/nginx.pid)
```

``` c title="守护进程的 SIGHUP 处理器示例"
static volatile sig_atomic_t reload_config = 0;

void sighup_handler(int sig) {
    reload_config = 1;
}

/* 主循环 */
while (running) {
    if (reload_config) {
        reload_config = 0;
        load_config("/etc/myapp/config.conf"); /* 安全：在主循环中执行 */
    }
    /* 正常服务逻辑... */
}
```

### trap：bash 脚本中捕获信号

在 bash 脚本中用 `trap` 命令捕获信号，实现优雅退出和临时文件清理：

``` bash title="trap 捕获信号"
#!/bin/bash

TMPFILE=$(mktemp)

# 注册清理函数，在脚本被 SIGINT/SIGTERM 终止时执行
cleanup() {
    echo "清理临时文件..."
    rm -f "$TMPFILE"
    exit 0
}

trap cleanup INT TERM EXIT

# 正常工作...
echo "工作中，临时文件：$TMPFILE"
sleep 30
```

### 查看进程的信号状态

``` bash title="查看进程信号掩码（/proc 文件系统）"
# 查看 PID 1234 的信号状态（十六进制位掩码）
cat /proc/1234/status | grep -E "^Sig"
# 输出示例：
# SigPnd: 0000000000000000   # 未决（等待投递）的信号
# SigBlk: 0000000000000000   # 被阻塞的信号
# SigIgn: 0000000000001000   # 被忽略的信号（位13=SIGPIPE）
# SigCgt: 0000000180014002   # 已安装处理器的信号
```

``` bash title="解码信号位掩码"
# SigCgt: 0000000180014002 → 哪些信号被捕获？
# 使用 Python 解码
python3 -c "
mask = 0x0000000180014002
for i in range(1, 65):
    if mask & (1 << (i-1)):
        print(f'  信号 {i}')
"
```

``` bash title="常用信号诊断命令"
# 列出所有信号名称和编号
kill -l

# 查看进程接收到的信号统计（使用 strace）
strace -e trace=signal -p 1234

# 用 ps 显示信号掩码（需要 BSD 风格 ps）
ps -o pid,comm,pending,blocked,ignored,caught -p 1234
```

### Mermaid：信号处理完整流程

```mermaid
graph TD
    A[事件发生] --> B{信号来源}
    B -->|硬件异常| C[内核产生信号]
    B -->|kill() 调用| C
    B -->|键盘输入| C
    C --> D[信号加入等待集]
    D --> E{信号是否被阻塞?}
    E -->|是| F[保持 pending 状态]
    F --> G[等待解除阻塞]
    G --> E
    E -->|否| H{查看处置设置}
    H -->|SIG_DFL| I[执行默认动作]
    H -->|SIG_IGN| J[丢弃信号]
    H -->|自定义处理器| K[保存执行上下文]
    K --> L[跳转到处理器函数]
    L --> M[处理器返回]
    M --> N[恢复执行上下文]
    N --> O[主程序从中断点继续]

classDef kernel fill:transparent,stroke:#0288d1,color:#adbac7,stroke-width:2px
classDef decision fill:transparent,stroke:#f57c00,color:#adbac7,stroke-width:1px
classDef action fill:transparent,stroke:#388e3c,color:#adbac7,stroke-width:1px

class C,D kernel
class E,H decision
class I,J,K,L,M,N,O action
```


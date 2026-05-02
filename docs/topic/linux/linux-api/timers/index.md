---
title: 定时器与休眠
---

# 定时器与休眠

**本文你会学到**：

- 时间获取 API（`time`、`gettimeofday`、`clock_gettime`）
- 休眠函数（`sleep`、`usleep`、`nanosleep`、`clock_nanosleep`）
- `alarm` + `SIGALRM` 实现超时控制
- `setitimer` / `getitimer` 间隔定时器（3 种模式）
- POSIX 定时器（`timer_create`、`timer_settime`、`timer_delete`）
- timerfd API（`timerfd_create`、`timerfd_settime` + `epoll`）
- 各定时器 API 的选型对比

## 时间获取 API

在涉及定时和休眠之前，先了解 Linux 提供的几种获取时间的接口。

### time：秒级时间

``` c
#include <time.h>

time_t time(time_t *tloc);
```

返回自 Epoch（1970-01-01 00:00:00 +0000, UTC）以来的秒数。若 `tloc` 非 `NULL`，也将结果写入其中：

``` c
time_t now = time(NULL);
printf("当前时间戳：%ld\n", now);
```

精度只有秒级，适用于日志时间戳等粗粒度场景。

### gettimeofday：微秒级时间

``` c
#include <sys/time.h>

int gettimeofday(struct timeval *tv, struct timezone *tz);
```

`timeval` 结构提供秒和微秒：

``` c
struct timeval {
    time_t      tv_sec;     /* 秒 */
    suseconds_t tv_usec;    /* 微秒 */
};
```

``` c title="测量代码段执行时间"
#include <stdio.h>
#include <sys/time.h>

int main(void) {
    struct timeval start, end;

    gettimeofday(&start, NULL);
    // 模拟工作
    for (volatile long i = 0; i < 100000000; i++);
    gettimeofday(&end, NULL);

    long elapsed = (end.tv_sec - start.tv_sec) * 1000000 +
                   (end.tv_usec - start.tv_usec);
    printf("耗时：%ld 微秒\n", elapsed);
    return 0;
}
```

`tz` 参数已废弃，始终传 `NULL`。

### clock_gettime：纳秒级 + 多时钟源

``` c
#include <time.h>

int clock_gettime(clockid_t clockid, struct timespec *tp);
int clock_getres(clockid_t clockid, struct timespec *res);
```

`timespec` 结构：

``` c
struct timespec {
    time_t tv_sec;  /* 秒 */
    long   tv_nsec; /* 纳秒 */
};
```

`clockid` 可选值：

| 时钟 ID | 含义 |
|---------|------|
| `CLOCK_REALTIME` | 系统级实时时钟，可设置 |
| `CLOCK_MONOTONIC` | 单调时间，系统启动后不跳变，不受修改系统时间影响 |
| `CLOCK_PROCESS_CPUTIME_ID` | 进程 CPU 时间 |
| `CLOCK_THREAD_CPUTIME_ID` | 线程 CPU 时间 |

``` c title="clock_gettime 示例"
#include <stdio.h>
#include <time.h>

int main(void) {
    struct timespec tp;

    clock_gettime(CLOCK_MONOTONIC, &tp);
    printf("系统启动后：%ld 秒 %ld 纳秒\n", tp.tv_sec, tp.tv_nsec);

    // 检查时钟分辨率
    clock_getres(CLOCK_MONOTONIC, &tp);
    printf("MONOTONIC 时钟分辨率：%ld 纳秒\n", tp.tv_nsec);

    return 0;
}
```

???+ tip "时间 API 选型"

    - **日志时间戳**：`time()`（秒级足够）
    - **性能测量**：`clock_gettime(CLOCK_MONOTONIC)`（不受系统时间调整影响）
    - **高精度时间戳**：`clock_gettime(CLOCK_REALTIME)`（纳秒级，但受系统时间影响）

## 休眠：暂停执行一段时间

### sleep：秒级休眠

``` c
#include <unistd.h>

unsigned int sleep(unsigned int seconds);
```

- 正常结束返回 0
- 被信号中断返回剩余秒数

``` c
sleep(3);  // 休眠 3 秒
```

`sleep` 在 Linux 上基于 `nanosleep` 实现，与 `alarm` / `setitimer` 无交互。但不可移植的实现可能使用 `alarm`，应避免混用。

### usleep：微秒级休眠（已废弃）

``` c
#include <unistd.h>

int usleep(useconds_t usec);
```

SUSv4 已标记为废弃，推荐使用 `nanosleep`。

### nanosleep：纳秒级休眠

``` c
#include <time.h>

int nanosleep(const struct timespec *req, struct timespec *rem);
```

- `req`：指定休眠时长
- `rem`：若被信号中断，返回剩余时间，可据此重启休眠

``` c title="nanosleep 使用与信号中断重启"
#include <stdio.h>
#include <time.h>
#include <stdlib.h>

int main(void) {
    struct timespec req = { .tv_sec = 10, .tv_nsec = 0 };
    struct timespec rem;

    while (nanosleep(&req, &rem) == -1) {
        // 被信号中断，用剩余时间重启
        req = rem;
    }

    printf("休眠完成\n");
    return 0;
}
```

### clock_nanosleep：指定时钟源的休眠

``` c
#include <time.h>

int clock_nanosleep(clockid_t clockid, int flags,
                    const struct timespec *req, struct timespec *rem);
```

相对 `nanosleep` 的优势：

1. **可选择时钟**：如 `CLOCK_MONOTONIC`，不受系统时间调整影响
2. **绝对时间模式**：设置 `TIMER_ABSTIME` 标志，`req` 为绝对时间，避免在计算相对时间时被抢占导致的"嗜睡"

``` c title="clock_nanosleep 绝对时间模式"
#include <stdio.h>
#include <time.h>

int main(void) {
    struct timespec deadline;

    // 获取当前时间 + 5 秒
    clock_gettime(CLOCK_MONOTONIC, &deadline);
    deadline.tv_sec += 5;

    // 绝对时间休眠，即使被信号中断也可直接重调用（参数不变）
    clock_nanosleep(CLOCK_MONOTONIC, TIMER_ABSTIME, &deadline, NULL);
    printf("5 秒到了\n");
    return 0;
}
```

## alarm：简单的一次性定时器

``` c
#include <unistd.h>

unsigned int alarm(unsigned int seconds);
```

- 设置一次性实时定时器，到期发送 `SIGALRM` 信号
- 返回前一定时器剩余秒数，未设置则返回 0
- `alarm(0)` 取消前一定时器
- 默认 `SIGALRM` 会终止进程，需设置信号处理器

``` c title="alarm 实现 read 超时"
#include <stdio.h>
#include <unistd.h>
#include <signal.h>
#include <errno.h>

volatile sig_atomic_t timeout = 0;

void handler(int sig) {
    timeout = 1;
}

int main(void) {
    struct sigaction sa = { .sa_handler = handler };
    sigaction(SIGALRM, &sa, NULL);

    alarm(5);  // 5 秒超时
    char buf[256];
    int n = read(STDIN_FILENO, buf, sizeof(buf));

    if (n == -1 && errno == EINTR && timeout) {
        printf("\n读取超时\n");
    }

    return 0;
}
```

## setitimer：间隔定时器

### 三种定时器类型

``` c
#include <sys/time.h>

int getitimer(int which, struct itimerval *curr_value);
int setitimer(int which, const struct itimerval *new_value,
              struct itimerval *old_value);
```

`which` 指定定时器类型：

| 类型 | 计时方式 | 到期信号 |
|------|---------|---------|
| `ITIMER_REAL` | 真实时间 | `SIGALRM` |
| `ITIMER_VIRTUAL` | 进程虚拟时间（用户态 CPU 时间） | `SIGVTALRM` |
| `ITIMER_PROF` | 进程时间（用户态 + 内核态 CPU 时间） | `SIGPROF` |

`itimerval` 结构：

``` c
struct itimerval {
    struct timeval it_value;     /* 首次到期时间 */
    struct timeval it_interval;  /* 后续间隔时间 */
};

struct timeval {
    time_t      tv_sec;     /* 秒 */
    suseconds_t tv_usec;    /* 微秒 */
};
```

- `it_value` 为 0：停止定时器
- `it_interval` 为 0：一次性定时器
- 进程只能有每种类型各一个定时器

### 示例：每秒输出一次

``` c title="setitimer 示例"
#include <stdio.h>
#include <sys/time.h>
#include <signal.h>
#include <unistd.h>

volatile sig_atomic_t tick = 0;

void handler(int sig) {
    tick = 1;
}

int main(void) {
    struct sigaction sa = { .sa_handler = handler };
    sigaction(SIGALRM, &sa, NULL);

    struct itimerval timer = {
        .it_value = { .tv_sec = 1, .tv_usec = 0 },      // 1 秒后首次到期
        .it_interval = { .tv_sec = 1, .tv_usec = 0 },    // 之后每隔 1 秒
    };

    setitimer(ITIMER_REAL, &timer, NULL);

    int count = 0;
    while (count < 5) {
        if (tick) {
            tick = 0;
            count++;
            printf("tick %d\n", count);
        }
        pause();  // 等待信号
    }

    return 0;
}
```

### setitimer 的限制

1. 每种类型只能有一个定时器（总共 3 个）
2. 只能通过信号通知，不能改变到期信号
3. 定时器溢出时（信号被阻塞），只会调用一次信号处理器，无法获知溢出次数
4. 分辨率仅微秒级，SUSv4 已标记为废弃，推荐 POSIX 定时器

## POSIX 定时器

POSIX.1b 定义的定时器 API 突破了 `setitimer` 的限制：可创建多个定时器、可选择通知方式、可获取溢出计数。

编译时需链接实时库：`gcc -lrt prog.c`

### 创建定时器

``` c
#include <signal.h>
#include <time.h>

int timer_create(clockid_t clockid, struct sigevent *sevp,
                 timer_t *timerid);
```

`sevp` 指定通知方式（设为 `NULL` 相当于 `SIGEV_SIGNAL` + `SIGALRM`）：

| `sigev_notify` 值 | 通知方式 |
|------------------|---------|
| `SIGEV_NONE` | 不通知，通过 `timer_gettime` 监控 |
| `SIGEV_SIGNAL` | 发送信号（通过 `sigev_signo` 指定） |
| `SIGEV_THREAD` | 在新线程中调用指定函数 |
| `SIGEV_THREAD_ID` | Linux 特有，发送信号给指定线程 |

### 启动 / 停止定时器

``` c
#include <time.h>

int timer_settime(timer_t timerid, int flags,
                  const struct itimerspec *new_value,
                  struct itimerspec *old_value);
```

- `flags=0`：相对时间
- `flags=TIMER_ABSTIME`：绝对时间
- `itimerspec` 使用 `timespec`（秒 + 纳秒），比 `itimerval`（秒 + 微秒）精度更高

### 获取定时器状态

``` c
int timer_gettime(timer_t timerid, struct itimerspec *curr_value);
```

### 获取溢出计数

``` c
int timer_getoverrun(timer_t timerid);
```

返回上次定时器到期到捕获信号之间额外到期的次数。对于频率较高的周期性定时器，如果信号被阻塞，溢出计数非常有用。

### 删除定时器

``` c
int timer_delete(timer_t timerid);
```

### 示例：信号通知

``` c title="POSIX 定时器（信号通知）"
#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <time.h>
#include <unistd.h>

timer_t g_timerid;

void handler(int sig, siginfo_t *si, void *uc) {
    int overrun = timer_getoverrun(g_timerid);
    printf("定时器到期（溢出次数：%d）\n", overrun);
}

int main(void) {
    struct sigaction sa = {
        .sa_sigaction = handler,
        .sa_flags = SA_SIGINFO,
    };
    sigaction(SIGALRM, &sa, NULL);

    struct sigevent sev = {
        .sigev_notify = SIGEV_SIGNAL,
        .sigev_signo = SIGALRM,
        .sigev_value.sival_ptr = &g_timerid,
    };
    timer_create(CLOCK_REALTIME, &sev, &g_timerid);

    struct itimerspec ts = {
        .it_value = { .tv_sec = 2, .tv_nsec = 0 },
        .it_interval = { .tv_sec = 1, .tv_nsec = 0 },
    };
    timer_settime(g_timerid, 0, &ts, NULL);

    sleep(5);
    timer_delete(g_timerid);
    printf("定时器已删除\n");
    return 0;
}
```

### 示例：线程通知

``` c title="POSIX 定时器（线程通知）"
#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <time.h>
#include <unistd.h>
#include <pthread.h>

void timer_thread(union sigval sv) {
    timer_t *tid = sv.sival_ptr;
    printf("定时器线程通知\n");
}

int main(void) {
    timer_t timerid;

    struct sigevent sev = {
        .sigev_notify = SIGEV_THREAD,
        .sigev_notify_function = timer_thread,
        .sigev_value.sival_ptr = &timerid,
    };
    timer_create(CLOCK_REALTIME, &sev, &timerid);

    struct itimerspec ts = {
        .it_value = { .tv_sec = 2, .tv_nsec = 0 },
        .it_interval = { .tv_sec = 1, .tv_nsec = 0 },
    };
    timer_settime(timerid, 0, &ts, NULL);

    sleep(5);
    timer_delete(timerid);
    return 0;
}
```

## timerfd API：基于文件描述符的定时器

timerfd 是 Linux 特有的定时器 API（内核 2.6.25+），将定时器到期事件暴露为文件描述符，可用 `select`、`poll`、`epoll` 统一监控。

### 创建 timerfd

``` c
#include <sys/timerfd.h>

int timerfd_create(int clockid, int flags);
```

- `clockid`：`CLOCK_REALTIME` 或 `CLOCK_MONOTONIC`
- `flags`：`TFD_CLOEXEC`（设置 close-on-exec）、`TFD_NONBLOCK`（非阻塞读）

### 启动 / 停止

``` c
int timerfd_settime(int fd, int flags,
                    const struct itimerspec *new_value,
                    struct itimerspec *old_value);

int timerfd_gettime(int fd, struct itimerspec *curr_value);
```

### 读取到期通知

``` c
#include <stdint.h>

uint64_t expirations;
read(fd, &expirations, sizeof(uint64_t));  // 返回自上次读取后的到期次数
```

### 示例：timerfd + epoll

``` c title="timerfd 配合 epoll"
#include <stdio.h>
#include <stdlib.h>
#include <sys/timerfd.h>
#include <sys/epoll.h>
#include <unistd.h>
#include <stdint.h>

int main(void) {
    int tfd = timerfd_create(CLOCK_MONOTONIC, TFD_NONBLOCK);
    if (tfd == -1) {
        perror("timerfd_create");
        exit(EXIT_FAILURE);
    }

    struct itimerspec ts = {
        .it_value = { .tv_sec = 2, .tv_nsec = 0 },
        .it_interval = { .tv_sec = 1, .tv_nsec = 0 },
    };
    timerfd_settime(tfd, 0, &ts, NULL);

    // 创建 epoll 实例并注册 timerfd
    int epfd = epoll_create1(0);
    struct epoll_event ev = { .events = EPOLLIN, .data.fd = tfd };
    epoll_ctl(epfd, EPOLL_CTL_ADD, tfd, &ev);

    struct epoll_event revent;
    for (int i = 0; i < 5; i++) {
        epoll_wait(epfd, &revent, 1, -1);
        uint64_t exp;
        read(tfd, &exp, sizeof(exp));
        printf("定时器到期 %lu 次\n", exp);
    }

    close(tfd);
    close(epfd);
    return 0;
}
```

timerfd 继承了文件描述符的优点：可通过 `fork` 继承、通过 `exec` 保存、配合 `epoll` 实现事件驱动，特别适合需要同时处理多个 I/O 事件和多个定时器的场景。

## API 选型对比

| 需求 | 推荐 API | 原因 |
|------|---------|------|
| 一次性超时（秒级） | `alarm` | 简单直观 |
| 间隔定时器（兼容旧系统） | `setitimer` | 经典接口，三种计时模式 |
| 多个定时器 | POSIX 定时器 | 无数量限制，纳秒精度 |
| 定时器 + 线程通知 | `timer_create` + `SIGEV_THREAD` | 避免信号处理，线程函数直接处理 |
| 定时器 + I/O 多路复用 | timerfd + `epoll` | 统一的事件循环 |
| 高精度休眠 | `clock_nanosleep` | 纳秒级，可选择时钟，绝对时间模式 |
| 被信号中断的可靠休眠 | `clock_nanosleep` + `TIMER_ABSTIME` | 避免嗜睡问题 |

## 定时器精度说明

传统上，定时器精度受软件时钟频率（jiffy）限制。Linux 2.6.21+ 支持高分辨率定时器（`CONFIG_HIGH_RES_TIMERS`），精度可达硬件支持的微秒级。可用 `clock_getres` 检查系统是否支持高精度定时器。

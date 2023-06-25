---
layout: note
title: 计算机概论
nav_order: 10
parent: 深入理解计算机系统
latex: true
create_time: 2023/6/13
---

# 电脑分类

- 超级电脑(Supercomputer)
- 大型电脑(Mainframe Computer)
- 迷你电脑(Minicomputer)
- 工作站(Workstation)
- 微电脑(Microcomputer)

# 计算机单位

容量单位：

- `位(bit)`：计算机中最小的数据单位，表示一位二进制数，取值为0或1。
- `字节(Byte)`：计算机中最基本的存储单位，`1Byte=8bit`，可以表示256种不同的状态，即0~255。
- `千字节(KB)`：`1KB=1024Byte`。
- `兆字节(MB)`：`1MB=1024KB`。
- `吉字节(GB)`：`1GB=1024MB`。
- `太字节(TB)`：`1TB=1024GB`。

速度单位：

- `bps`：`每秒传输的位数`，`1bps=1bit/s`。
- `Kbps`：`千比特每秒`，`1Kbps=1024bps`, `1Kbps=128Byte/s`。
- `Mbps`：`兆比特每秒`，`1Mbps=1024Kbps`, `1Mbps=128KB/s`。
- `Gbps`：`千兆比特每秒`，`1Gbps=1024Mbps`, `1Gbps=128MB/s`。

{: .note}
> 也就是说100兆宽带（100Mbps），传输速度位12.5MB/s。

# 计算机五大组成概述

- `输入设备`
- `输出设备`
- CPU
    - `算数逻辑单元`
    - `控制单元`
- `存储器`
    - 内存
    - 外存

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202306131514746-%E8%AE%A1%E7%AE%97%E6%9C%BA%E4%BA%94%E5%A4%A7%E7%BB%84%E6%88%90.gif)

# CPU

## 指令集

- `精简指令集`(Reduced Instruction Set Computer，RISC)
    - 安谋公司(ARM Holdings) 的`ARM CPU`系列
    - 甲骨文(Oracle) 公司的SPARC 系列
    - IBM 公司的Power Architecture (包括PowerPC) 系列
- `复杂指令集`(Complex Instruction Set Computer，CISC)
    - AMD、Intel、VIA等公司`x86架构的CPU`

{: .note-title}
> 为什么叫做x86架构的CPU？
>
> 最早的那颗Intel发展出来的CPU代号称为8086，后来依此架构又开发出80286, 80386...， 因此这种架构的CPU就被称为x86架构了。

## 频率

{: .new-title}
> 频率
>
> 频率就是CPU每秒钟可以进行的工作次数
>
> Intel的Core 2 Duo型号E8400的CPU频率为`3.0GHz`， 表示这颗CPU在一秒内可以进行$3.0\times10^9$次工作

$$CPU的实际频率=外频\times倍频$$

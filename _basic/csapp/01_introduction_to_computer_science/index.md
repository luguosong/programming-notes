---
layout: note
title: 计算机概论
nav_order: 10
parent: 深入理解计算机系统
latex: true
create_time: 2023/6/13
---

# 计量单位

## 容量单位

字节单位表(B大小，一般表示大小)：

| 名称  | 符号               | 说明         |
|-----|------------------|------------|
| 位   | bit,b            | 0和1        |
| 字节  | Byte,B           | 1B = 8b    |
| 千字节 | kilobyte,kB,KB,K | 1K = 1024B |
| 兆字节 | megabyte,MB,M    | 1M = 1024K |
| 吉字节 | gigabyte,GB,G    | 1G = 1024M |
| 太字节 | terabyte,TB,T    | 1T = 1024G |

比特单位说明表(b小写，一般表示速率):

| 名称  | 符号              | 说明                            |
|-----|-----------------|-------------------------------|
| 位   | bit, b          | 0和1                           |
| 千比特 | kilobit,kbit,Kb | 1kbit = 1,000 bit             |
| 兆比特 | Megabit,Mbit,Mb | 1Mbit = 1,000,000 bit         |
| 吉比特 | gigabit,Gbit,Gb | 1Gbit = 1,000,000,000 bit     |
| 太比特 | terabit,Tbit,Tb | 1Tbit = 1,000,000,000,000 bit |

## 速度单位

CPU命令周期：

| 名称   | 符号            | 说明             |
|------|---------------|----------------|
| 赫兹   | Hertz，Hz      | 每秒一次           |
| 千赫兹  | Kilohertz，kHz | 1kHz = 1000Hz  |
| 百萬赫茲 | MHz           | 1MHz = 1000kHz |
| 吉赫茲  | GHz           | 1GHz = 1000MHz |

网络传输方面：

| 名称                         | 符号   | 转换               |
|----------------------------|------|------------------|
| 比特每秒（bits per second）      | bps  | 表示1bit每秒         |
| 千比特每秒（kilobits per second） | kbps | 1kbps = 1000bps  |
| 兆比特每秒（megabits per second） | Mbps | 1Mbps = 1000kbps |
| 吉比特每秒（gigabits per second） | Gbps | 1Gbps = 1000Mbps |

# 计算机五大组成概述

- `输入单元`
- 主机部分
    - 主板
    - CPU
        - `算数逻辑单元`
        - `控制单元`
    - `存储单元`
        - 内存
        - 外部存储设备
- `输出单元`

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202306252305456-%E8%AE%A1%E7%AE%97%E6%9C%BA%E4%BA%94%E5%A4%A7%E5%8D%95%E5%85%83.png)

# 主板

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202306261437186-%E4%B8%BB%E6%9D%BF%E6%9E%B6%E6%9E%84.png)

- 北桥：负责连接速度较快的CPU、内存、显卡。大多将北桥整合到CPU当中
- 南桥：负责连接速度较慢的外部设备，如硬盘、USB、网卡等

# CPU

## 指令集

- `精简指令集`(Reduced Instruction Set Computer，RISC)
    - ⭐安谋公司(ARM Holdings) 的 `ARM CPU`系列
        - 手机、平板
        - 嵌入式系统
        - 汽车系统
        - 交换机、路由器
    - 甲骨文(Oracle) 公司的SPARC系列
        - 学术领域
        - 大型工作站中
    - IBM 公司的Power Architecture(包括PowerPC)系列
        - 索尼的PS3
- `复杂指令集`(Complex Instruction Set Computer，CISC)
    - ⭐AMD、Intel、VIA等公司 `x86架构的CPU`
        - 个人电脑
        - 服务器
        - 游戏主机
        - 工作站和高性能计算

{: .note-title}

> 为什么叫做x86架构的CPU？
>
> 最早的那颗Intel发展出来的CPU代号称为8086，后来依此架构又开发出80286, 80386...， 因此这种架构的CPU就被称为x86架构了。

## CPU位数和内存的关系

位数表示CPU能够直接寻址的 `内存地址的范围`。

32位CPU可以寻址的内存地址范围为2^32（约4GB），

而64位CPU可以寻址的内存地址范围为2^64（极大，远超现实需求）。因此，64位CPU在理论上可以支持更大的内存容量。

## 频率

CPU的命令周期比其它设备都要快。

`外频`：CPU与外部组件进行数据传输的速度。

`倍频`：CPU内部用来加速工作性能的一个倍数

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202306261549263-cpu%E9%A2%91%E7%8E%87.png)

图中：$Core Speed = Bus Speed \times Multiplier$

即：$CPU的实际频率 = 外频 \times 倍频$

`超频`：有些电脑的CPU的倍频出场时已经锁定，所以只能提高`外频`来超频，这将是不稳定的。

# 存储

## 主内存

CPU所使用的数据都来自`内存（Main Memory）`

`内存频率（Memory Frequency）`:每秒钟能够完成的数据传输次数或时钟周期数。

`位宽（Bit Width）`:表示内存模块内部数据通道的宽度，也可以理解为每个内存模块芯片能够同时传输的数据位数。一个内存模块的位宽为64位，表示每次数据传输可以同时传输64个数据位。

$带宽=频率 \times 位宽$

{: .note-title}
> DRAM(Dynamic random-access memory,动态随机存取存储器)
> 
> 是一种半导体存储器，通常被用作`主内存`
> 
> 由于晶体管会有漏电流的现象，导致电容上所存储的电荷数量并不足以正确的判别数据，进而导致数据毁损。因此对于DRAM来说，周期性地充电是一个不可避免的条件。由于这种需要定时刷新的特性，因此被称为“动态”存储器。相对来说，静态存储器（SRAM）只要存入数据后，即使不刷新也不会丢失记忆。

## CPU缓存

- `L1 Cache（一级缓存）`:位于 CPU 核心内部,用于存储 CPU 频繁访问的数据和指令，以提高数据读取和指令执行的速度。
- `L2 Cache（二级缓存）`：位于 CPU 核心和主内存之间。L2 缓存可以为 CPU 提供更多的数据和指令，以满足高速缓存未命中（cache miss）的情况。
- `L3 Cache（三级缓存）`：位于 CPU 和主内存之间，提供更大的缓存容量来存储多个 CPU 核心共享的数据和指令。L3 缓存可以提高多个 CPU 核心之间的数据共享和通信效率。

{: .note-title}
> SRAM（Static random-access memory,静态随机存取存储器）
> 
> 缓存芯片通常采用速度较快的SRAM。所谓的`静态`，是指这种存储器只要保持通电，里面存储的数据就可以恒常保持

## 只读存储器（ROM）

非易失性存储器，数据不会因为断电而丢失。

在早期`BIOS（Basic Input Output System）`程序一般会存储在ROM中的。

但考虑到BIOS的升级，由于ROM是只读的，因此现在的BIOS一般都存储在`可擦写可编程只读存储器（EEPROM）`或`闪存（Flash Memory）`中。

## 存储设备

`存储设备`有硬盘、软盘、光盘、U盘、固态硬盘、磁带等。

_机械硬盘组成：_

- 圆形磁片
  - `扇区`：磁盘的最小物理存储单位
  - `磁道`：同一个同心圆的扇区组成的圆形区域
  - `柱面`：所有磁片上同一个`磁道`的集合
- 磁头
- 机械手臂
- 主轴马达

_磁盘传输接口：_

- SATA
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202306261904414-SATA%E6%8E%A5%E5%8F%A3.png)
- SAS
- ❌IDE(已被SATA取代)
- ❌SCSI(已被SAS取代)
- USB
- eSATA

# 显卡

`显示卡（英语：display card）`简称显卡，也称图形卡（graphics card）、视频卡（video card）、图形适配器（graphics adapter）或视频适配器（video adapter），是个人电脑上以`图形处理器（GPU）`为核心的扩展卡，用途是提供中央处理器以外的微处理器`帮助计算图像信息`，并将计算机系统所需要的`显示信息进行转换`并提供逐行或隔行扫描信号给`显示设备`，是连接`显示器`和`个人电脑主板`的重要组件，是`人机对话`的重要设备之一。

{: .note-title}
> 工作管理及模式
> 
> 显卡是插在主板上的扩展槽里的（现在一般是PCI-E插槽，此前还有AGP、PCI、ISA等插槽），主要负责把主机向显示器发出的显示信号转化为一般电器信号，使得显示器能明白个人电脑在让它做什么。显卡的主要芯片叫“显示芯片”（Video chipset，也叫GPU或VPU，图形处理器或视觉处理器），是显卡的主要处理单元。显卡上也有和电脑存储器相似的存储器，称为`显示存储器`，简称`显存`。
>
> 早期的显卡只是单纯意义的显卡，只起到`信号转换的作用`；目前的显卡一般都带有3D画面运算和图形加速功能，所以也叫做`图形加速卡`或`3D加速卡`。PC上最早的显卡是IBM在1981年推出的5150个人电脑上所搭载的MDA和CGA两款2D加速卡。
>
> 显卡通常由总线接口、PCB板、显示芯片、显示存储器、RAMDAC、VGA BIOS、VGA端子及其他外围组件构成，现在的显卡大多使用VGA、DVI、HDMI接口或DisplayPort接口。


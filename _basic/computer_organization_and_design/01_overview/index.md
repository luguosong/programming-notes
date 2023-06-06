---
layout: note
title: 概述
nav_order: 10
parent: 计算机组成原理
create_time: 2023/5/4
latex: true
---

# 教材推荐

- [计算机组成与设计](https://book.douban.com/subject/30443432/)
- [数字设计和计算机体系结构](https://book.douban.com/subject/26824111/)
- [计算机组成原理](https://book.douban.com/subject/35737337/)

# 各年份跑的最快的计算机

速度单位：

- 一个MFLOPS（megaFLOPS）等于每秒100万（=$10^6$）次的浮点运算
- 一个GFLOPS（gigaFLOPS）等于每秒10亿（=$10^9$）次的浮点运算
- 一个TFLOPS（teraFLOPS）等于每秒1兆（=$10^{12}$）次的浮点运算
- 一个PFLOPS（petaFLOPS）等于每秒1千兆（=$10^{15}$）次的浮点运算
- 一个EFLOPS（exaFLOPS）等于每秒100京（=$10^{18}$）次的浮点运算

每年最快的超级计算机：

- 1993年 - Thinking Machines CM-5 - 59.7 GigaFLOPS（美国）
- 1994年 - Intel Paragon XP/S 140 - 143.4 GigaFLOPS（美国）
- 1996年 - Hitachi CP-PACS - 368.2 GigaFLOPS（日本）
- 1997年 - Intel ASCI Red - 1.068 TeraFLOPS（美国）
- 1999年 - IBM ASCI White - 12.3 TeraFLOPS（美国）
- 2002年 - NEC Earth Simulator - 35.86 TeraFLOPS（日本）
- 2004年 - IBM Blue Gene/L - 70.72 TeraFLOPS（美国）
- 2008年 - IBM Roadrunner - 1.026 PetaFLOPS（美国）
- 2010年 - 天河一号（Tianhe-1A）- 2.566 PetaFLOPS（中国）
- 2012年 - Cray Titan - 17.59 PetaFLOPS（美国）
- 2013年 - 国防科技大学天河二号（NUDT Tianhe-2）- 33.86 PetaFLOPS（中国）
- 2016年 - 神威·太湖之光（Sunway TaihuLight）- 93.01 PetaFLOPS（中国）
- 2018年 - IBM Summit - 148.6 PetaFLOPS（美国）
- 2020年 - 富岳（Fugaku，日本理化学研究所/産業技術総合研究所）- 442 PetaFLOPS（日本）

# 冯诺依曼结构

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20230504183705.png)

- `运算器`：算术逻辑单元（Arithmetic and Logic Unit，ALU
- `控制器`：控制单元（Control Unit，CU）
- `存储器`：存储器用于存储计算机中的数据和程序。存储器通常包括`内存（RAM，Random Access Memory）`和`外存（如硬盘、固态硬盘等）`。
- `输入设备`
- `输出设备`

# 冯诺依曼结构特点

- 以`运算单`元为中心
- 采用存储程序原理:计算机的`指令`和`数据`都存储在`同一个可读写的存储器中`。
- 存储器是按地址访问、线性编址的空间
- 控制流由指令流产生
- 指令由`操作码`和`地址码`组成
- 数据以`二进制编码`

# 现代计算机硬件框架结构

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20230504214726.png)

- 硬件
  - 主机
    - CPU
      - 运算器（ALU）
      - 控制器（CU）
    - 存储器(主存)
  - I/O

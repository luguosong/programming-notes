# Linux概述

## 历史

### 兼容分时系统

`兼容分时系统`（英语：Compatible Time-Sharing System，缩写为
CTSS），最早的分时操作系统，由美国麻省理工学院计算机中心设计与实现，计划领导者为`费南多·柯巴托`。`1961年`
首次进行示范运作，`1962年`发表论文，一直运作到`1963年`为止。麻省理工学院的MAC项目拥有它的第二份拷贝，它只在这两个地方运作过。

分时系统解决了以往批处理系统（Batch System）无法让程序员及时调试程序的人机交互问题。

在兼容分时系统的基础上，发展出接下来的`Multics操作系统`。

### Multics系统

Multics，名称来自于`多工信息与计算系统`（英语：MULTiplexed Information and Computing
System）的缩写，它是一套分时多工操作系统，是`1964年`
由贝尔实验室、麻省理工学院及美国通用电气公司所共同参与研发，并安装在大型主机上。最后一个装有Multics的计算机已于2000年10月30日关闭。通过UNIX，几乎所有现代操作系统都深受Multics的影响，无论是直接
（Linux, OS X）或间接（Microsoft Windows）。

MULTICS以`兼容分时系统`（CTSS）做基础，建置在美国通用电力公司的大型机`GE-645`
上。目的是`连接1000部终端，支持300位用户同时上线`。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202407250948674.png){ loading=lazy }
  <figcaption>Multics</figcaption>
</figure>

### UNIX

`1969年`
因MULTICS项目的工作进度过于缓慢，最后终究遭裁撤的命运，贝尔实验室由此退出此项目。当时，肯·汤普逊正在撰写一个称为`星际旅行（Space
Travel）`
的游戏程序。贝尔实验室退出Multics计划后，由贝尔实验室的两位软件工程师肯·汤普逊与丹尼斯·里奇以B语言和汇编语言为基础而发展出`UNIX`。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202407251017931.png){ loading=lazy }
  <figcaption>肯·汤普逊（左）和丹尼斯·里奇</figcaption>
</figure>

`1973年`汤普逊和里奇用`C语言重写了Unix`，成为后来普及的版本。

### BSD

`伯克利软件包`（英语：Berkeley Software Distribution，缩写：BSD；也被称为伯克利Unix或Berkeley
Unix）是一个派生自Unix（类Unix）的操作系统，1970年代由伯克利加州大学的学生比尔·乔伊开创，也被用来代表其派生出的各种包。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202407251016907.png){ loading=lazy }
  <figcaption>比尔·乔伊</figcaption>
</figure>

BSD许可证非常地宽松，因此BSD常被当作工作站级别的Unix系统，许多1980年代成立的计算机公司都从BSD中获益，比较著名的例子如DEC的Ultrix，以及Sun公司的SunOS。
1990年代，BSD大幅度被System V 4.x版以及OSF/1系统所取代，但其开源版本被用在互联网的开发。

在`1977年`，伯克利的研究生比尔·乔伊将程序整理到磁带上，作为First Berkeley Software Distribution（1BSD）发行。
1BSD被作为第六版Unix系列，而不是单独的操作系统。主要程序包括Pascal编译器，以及比尔·乔伊的ex行编辑器。

1992年，AT&T的Unix系统实验室正式对Berkeley Software Design提起诉讼，这导致Net/2发布被中止，直到其源码能够被鉴定为符合Unix系统实验室的著作权。

`1994年`6月，4.4BSD以两种形式发布：可自由再发布的`4.4BSD-Lite`，不包含AT&T源码；另有`4.4BSD-Encumbered`，跟以前的版本一样，遵照AT&T的许可证。

伯克利的最终版本是`1995年`的`4.4BSD-Lite Release 2`，而后CSRG解散，在伯克利的BSD开发告一段落。在这之后，几种基于`4.4BSD`
的包（`比如FreeBSD`、`OpenBSD`和`NetBSD`）得以继续维护。

### UNIX System V

UNIX System V是Unix操作系统众多版本中的一支。它最初由AT&T开发，在`1983年`第一次发布，因此也被称为AT&T System V。一共发行了4个System V的主要版本：版本1、2、3和4。System V Release 4，或者称为SVR4，是最成功的版本，成为一些UNIX共同特性的源头，例如“SysV 初始化脚本”（/etc/init.d），用来控制系统启动和关闭，System V Interface Definition（SVID）是一个System V如何工作的标准定义。

AT&T出售运行System V的专有硬件，但许多（或许是大多数）客户在其上运行一个转售的版本，这个版本基于AT&T的实现说明。流行的SysV派生版本包括Dell SVR4和Bull SVR4。当今广泛使用的System V版本是SCO OpenServer，基于System V Release 3，以及SUN Solaris和SCO UnixWare，都基于System V Release 4。

System V是AT&T的第一个商业UNIX版本（UNIX System III）的加强。传统上，System V被看作是两种UNIX`风味`之一（另一个是BSD）。然而，随着一些并不基于这两者代码的类UNIX实现的出现，例如Linux和QNX，这一归纳不再准确，但不论如何，像POSIX这样的标准化努力一直在试图减少各种实现之间的不同。

### Minix

Minix，是一个迷你版本的类Unix操作系统，由`塔能鲍姆`教授为了教学之用而创作，采用微核心设计。它启发了Linux核心的创作。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202407251014230.png){ loading=lazy }
  <figcaption>安德鲁·斯图尔特·特南鲍姆</figcaption>
</figure>

它的名称取自英语：Mini UNIX的缩写。与Xinu、Idris、Coherent和Uniflex等类Unix操作系统类似，派生自Version 7 Unix，但并没有使用任何AT&T的代码。第一版于`1987年`发布，只需要购买它的磁片，就提供完整的源代码给大学系所与学生，作为授课及学习之用。

`2000年`4月，重新以BSD许可协议发布，成为开放源代码软件。

### GNU计划和FSF基金会

`GNU计划`（英语：GNU Project），又译为革奴计划，是一个自由软件集体协作计划，`1983年`9月27日由`理查德·斯托曼`在麻省理工学院公开发起。它的目标是创建一套完全自由的操作系统，称为GNU。理查德·斯托曼最早在net.unix-wizards新闻组上公布该消息，并附带一份《GNU宣言》等解释为何发起该计划的文章，其中一个理由就是要`重现当年软件界合作互助的团结精神`。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202407251013098.png){ loading=lazy }
  <figcaption>理查德·斯托曼</figcaption>
</figure>

`自由软件基金会`（英语：Free Software Foundation，缩写：FSF）是一个致力于推广自由软件的美国民间非营利性组织。它于`1985年`10月由理查德·斯托曼建立。其主要工作是执行GNU计划，开发更多的自由软件。 从其建立到1990年代中自由软件基金会的基金主要被用来雇佣编程师来发展自由软件。从1990年代中开始写自由软件的公司和个人繁多，因此自由软件基金会的雇员和志愿者主要在自由软件运动的法律和结构问题上工作。

### Linux诞生

`1991年`，`林纳斯·托瓦兹`在赫尔辛基大学上学时，对操作系统很好奇。他对MINIX只允许在教育上使用很不满（在当时MINIX不允许被用作任何商业使用），于是他便开始写他自己的操作系统，这就是后来的`Linux内核`。该操作系统的内核由林纳斯·托瓦兹在1991年10月5日首次发布。

林纳斯·托瓦兹开始在MINIX上开发Linux内核，为MINIX写的软件也可以在Linux内核上使用。后来使用GNU软件代替MINIX的软件，因为使用从GNU系统来的源代码可以自由使用，这对Linux的发展有益。使用GNU GPL协议的源代码可以被其他项目所使用，只要这些项目使用同样的协议发布。为了让Linux可以在商业上使用，林纳斯·托瓦兹决定更改他原来的协议（这个协议会限制商业使用），以GNU GPL协议来代替。之后许多开发者致力融合GNU元素到Linux中，做出一个有完整功能的、自由的操作系统。

`1994年`3月，Linux1.0版正式发布。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202407251022892.png){ loading=lazy }
  <figcaption>林纳斯·托瓦兹</figcaption>
</figure>

## Linux发行版

- Linux
    - Debian
        - Kali Linux
        - Ubuntu
    - SUSE Linux
    - Red Hat
        - CentOS
        - Rocky Linux

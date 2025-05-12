# 概述

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
的游戏程序。贝尔实验室退出Multics计划后，由贝尔实验室的两位软件工程师肯·汤普逊与丹尼斯·里奇以B语言和汇编语言为基础而发展出
`UNIX`。

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

UNIX System V是Unix操作系统众多版本中的一支。它最初由AT&T开发，在`1983年`第一次发布，因此也被称为AT&T System
V。一共发行了4个System V的主要版本：版本1、2、3和4。System V Release 4，或者称为SVR4，是最成功的版本，成为一些UNIX共同特性的源头，例如“SysV
初始化脚本”（/etc/init.d），用来控制系统启动和关闭，System V Interface Definition（SVID）是一个System V如何工作的标准定义。

AT&T出售运行System V的专有硬件，但许多（或许是大多数）客户在其上运行一个转售的版本，这个版本基于AT&T的实现说明。流行的SysV派生版本包括Dell
SVR4和Bull SVR4。当今广泛使用的System V版本是SCO OpenServer，基于System V Release 3，以及SUN Solaris和SCO
UnixWare，都基于System V Release 4。

System V是AT&T的第一个商业UNIX版本（UNIX System III）的加强。传统上，System V被看作是两种UNIX`风味`
之一（另一个是BSD）。然而，随着一些并不基于这两者代码的类UNIX实现的出现，例如Linux和QNX，这一归纳不再准确，但不论如何，像POSIX这样的标准化努力一直在试图减少各种实现之间的不同。

### Minix

Minix，是一个迷你版本的类Unix操作系统，由`塔能鲍姆`教授为了教学之用而创作，采用微核心设计。它启发了Linux核心的创作。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202407251014230.png){ loading=lazy }
  <figcaption>安德鲁·斯图尔特·特南鲍姆</figcaption>
</figure>

它的名称取自英语：Mini UNIX的缩写。与Xinu、Idris、Coherent和Uniflex等类Unix操作系统类似，派生自Version 7
Unix，但并没有使用任何AT&T的代码。第一版于`1987年`发布，只需要购买它的磁片，就提供完整的源代码给大学系所与学生，作为授课及学习之用。

`2000年`4月，重新以BSD许可协议发布，成为开放源代码软件。

### GNU计划和FSF基金会

`GNU计划`（英语：GNU Project），又译为革奴计划，是一个自由软件集体协作计划，`1983年`9月27日由`理查德·斯托曼`
在麻省理工学院公开发起。它的目标是创建一套完全自由的操作系统，称为GNU。理查德·斯托曼最早在net.unix-wizards新闻组上公布该消息，并附带一份《GNU宣言》等解释为何发起该计划的文章，其中一个理由就是要
`重现当年软件界合作互助的团结精神`。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202407251013098.png){ loading=lazy }
  <figcaption>理查德·斯托曼</figcaption>
</figure>

`自由软件基金会`（英语：Free Software Foundation，缩写：FSF）是一个致力于推广自由软件的美国民间非营利性组织。它于`1985年`
10月由理查德·斯托曼建立。其主要工作是执行GNU计划，开发更多的自由软件。
从其建立到1990年代中自由软件基金会的基金主要被用来雇佣编程师来发展自由软件。从1990年代中开始写自由软件的公司和个人繁多，因此自由软件基金会的雇员和志愿者主要在自由软件运动的法律和结构问题上工作。

### Linux诞生

`1991年`，`林纳斯·托瓦兹`在赫尔辛基大学上学时，对操作系统很好奇。他对MINIX只允许在教育上使用很不满（在当时MINIX不允许被用作任何商业使用），于是他便开始写他自己的操作系统，这就是后来的
`Linux内核`。该操作系统的内核由林纳斯·托瓦兹在1991年10月5日首次发布。

林纳斯·托瓦兹开始在MINIX上开发Linux内核，为MINIX写的软件也可以在Linux内核上使用。后来使用GNU软件代替MINIX的软件，因为使用从GNU系统来的源代码可以自由使用，这对Linux的发展有益。使用GNU
GPL协议的源代码可以被其他项目所使用，只要这些项目使用同样的协议发布。为了让Linux可以在商业上使用，林纳斯·托瓦兹决定更改他原来的协议（这个协议会限制商业使用），以GNU
GPL协议来代替。之后许多开发者致力融合GNU元素到Linux中，做出一个有完整功能的、自由的操作系统。

`1994年`3月，Linux1.0版正式发布。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202407251022892.png){ loading=lazy }
  <figcaption>林纳斯·托瓦兹</figcaption>
</figure>

## Linux发行版

- `Linux`
	- `Debian`:是完全由自由软件组成的类UNIX操作系统，其包含的多数软体使用GNU通用公共许可协议授权，并由Debian计划的参与者组成团队对其进行打包、开发与维护。
		- `Kali Linux`:基于Debian的Linux发行版，设计用于数字鉴识和渗透测试。由Offensive Security公司维护和资助。Kali
		  Linux内置了大约600个渗透测试程序（工具），包括Armitage（图形化网络攻击管理工具）、Nmap（端口扫描工具）、Wireshark（数据包分析器）、Metasploit（渗透测试框架）、John
		  the Ripper（密码破解器）、sqlmap（自动SQL注入和数据库接管工具）、Aircrack-ng（用于渗透测试无线局域网的软件包）、Burp
		  suite以及OWASP ZAP网络应用安全扫描器等。
		- `Ubuntu`:
		  基于Debian，以桌面应用为主的Linux发行版。Ubuntu有三个正式版本，包括桌面版、服务器版及用于物联网设备和机器人的Core版。前述三个版本既能安装于实体电脑，也能安装于虚拟电脑。从17.10版本开始，Ubuntu以GNOME为默认桌面环境。Ubuntu是著名的Linux发行版之一，也是目前最多用户的Linux版本。Ubuntu每六个月（即每年的四月与十月）发布一个新版本，长期支持（LTS）版本每两年发布一次。普通版本一般只支持9个月，但LTS版本一般能提供5年的支持。
	- `SUSE Linux`:是SUSE开发的企业级Linux操作系统。
	- `Red Hat Enterprise Linux（RHEL）`:是一个由Red Hat开发的商业市场导向的Linux发行版。红帽公司从 Red Hat Enterprise
	  Linux 5 开始对企业版LINUX的每个版本提供10年的支持。红帽之前曾使用严格的商标规则来限制其官方支持的 Red Hat
	  Enterprise Linux 版本的自由再分发，但仍然免费提供其源代码。第三方派生品可以通过去除非自由组件（如红帽的商标）来构建和再分发。例如，包括社区支持的发行版如
	  Rocky Linux 和 AlmaLinux，以及商业发行版如 Oracle Linux。2023 年，红帽决定停止向公众提供 Red Hat Enterprise Linux
	  的源代码。该代码仍然可供红帽客户以及使用免费账户的开发者获取，但条件是禁止源代码的再分发。RHEL可以使用 Fedora EPEL
	  来补足软件。
		- `CentOS`:是Linux发行版之一，它是来自于Red Hat Enterprise
		  Linux（RHEL）依照开放源代码规定发布的源代码所编译而成。由于出自同样的源代码，因此有些要求高度稳定性的服务器以CentOS替代商业版的Red
		  Hat Enterprise Linux使用。两者的不同，在于CentOS不包含封闭源代码软件。CentOS
		  对上游代码的主要修改是为了移除不能自由使用的商标。CentOS和RHEL一样，都可以使用Fedora EPEL来补足软件。
		- `Rocky Linux`:旨在成为一个使用红帽企业Linux（RHEL）操作系统源代码的完整的下游二进制兼容版本。该项目旨在提供一个由社区支持且可用于生产的企业操作系统。Rocky
		  Linux、Red Hat Enterprise Linux以及SUSE Linux Enterprise，已经成为企业操作系统使用的热门选择。Rocky
		  Linux的首个候选版本发布于2021年4月30日，首个正式版本发布于2021年6月21日。Rocky Linux 8将提供支持至2029年5月。

## Vmware安装

### Windows开启虚拟化

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202505102309293.png){ loading=lazy }
  <figcaption>任务栏管理器中可以检测到虚拟化是否已开启</figcaption>
</figure>

### CentOS7安装示例

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202505101643646.png){ loading=lazy }
  <figcaption>文件 -> 新建虚拟机</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202505101745365.png){ loading=lazy }
  <figcaption>选择典型即可</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202505101746208.png){ loading=lazy }
  <figcaption>选择稍后安装操作系统</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202505101748823.png){ loading=lazy }
  <figcaption>选择要安装的操作系统类型</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202505101749013.png){ loading=lazy }
  <figcaption>选择安装位置</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202505101749181.png){ loading=lazy }
  <figcaption>设置磁盘占用的最大容量</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202505101752746.png){ loading=lazy }
  <figcaption>完成配置</figcaption>
</figure>

可以点击完成页面中的`自定义硬件`，指定硬件分配标准。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202505102150583.png){ loading=lazy }
  <figcaption>选择分配给虚拟机的内存量</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202505102151778.png){ loading=lazy }
  <figcaption>选择IOS安装镜像</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202505102242224.png){ loading=lazy }
  <figcaption>点击开启虚拟机</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202505102246930.png){ loading=lazy }
  <figcaption>选择 Install CentOS 7,按Tab键，在下方弹出输入框中输入net.ifrnames=0 biosdevname=0</figcaption>
</figure>

!!! note

	CentOS7中网卡名默认为`ens33`这种形式。

	上面这步是为了让网卡形式编程`ethxxx`这种形式,比如`eth0`、`eth1`、`eth2`等。

完成上面配置后，按`回车键`开始安装系统。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202505102252730.png){ loading=lazy }
  <figcaption>选择安装过程语言,继续</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202505102306464.png){ loading=lazy }
  <figcaption>可以选择网络和主机名，验证开头配置是否生效，网卡名是否为eth0</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202505102333288.png){ loading=lazy }
  <figcaption>配置网卡，勾选可用时自动连接到这个网络</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202505102336393.png){ loading=lazy }
  <figcaption>将ipv4配置改为手动，使用固定ip</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202505102337756.png){ loading=lazy }
  <figcaption>可配置自定义主机名</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202505102340849.png){ loading=lazy }
  <figcaption>配置安装位置，可以使用默认位置，点击完成</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202505102343730.png){ loading=lazy }
  <figcaption>可以选择关闭KDUMP</figcaption>
</figure>

!!! note

	KDUMP的功能是在系统崩溃时，将系统内存中的数据保存到磁盘中，以便进行故障排除。

	考虑到安全问题，可以将它关闭。

开始安装

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202505102348008.png){ loading=lazy }
  <figcaption>安装时可以设置root密码</figcaption>
</figure>

## 远程连接

### 配置网络

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202505111603716.png){ loading=lazy }
  <figcaption>查看虚拟机ip</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202505111700738.png){ loading=lazy }
  <figcaption>选择虚拟网络编辑器</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202505111704016.png){ loading=lazy }
  <figcaption>选择NAT模式->修改子网ip</figcaption>
</figure>

### MobaXterm

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202505102353556.png){ loading=lazy }
  <figcaption>使用new session创建一个ssh连接</figcaption>
</figure>


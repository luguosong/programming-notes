# 2.Spring Security 入门

!!! note "本章内容包括"

    - 使用 Spring Security 创建你的第一个项目
    - 利用基础组件设计简单的认证与授权功能
    - 介绍底层原理及其在实际项目中的应用
    - 应用基本契约并理解它们之间的关联
    - 针对核心职责编写自定义实现
    - 重写 Spring Boot 默认的 Spring Security 配置

Spring Boot 是基于 Spring Framework 应用开发的一个进化阶段。它无需手动编写所有配置，而是自带了一些预设配置，你只需覆盖那些与你实现不符的部分。这种方式也被称为
`约定优于配置`。如今，Spring Boot 已不再是新鲜事物，我们也已经习惯于使用其第三个版本(Spring Boot 3.x)来开发应用程序。

在 Spring Boot
出现之前，开发者们常常需要为每个应用反复编写几十行重复的代码。过去，这种情况并不那么明显，因为大多数架构都是以单体方式开发的。在单体架构中，这些配置只需在项目初期写一次，之后很少需要再去修改。随着面向服务的软件架构逐渐普及，我们开始切身体会到为每个服务配置时不得不写的大量样板代码所带来的痛苦。如果你对此感到好奇，不妨看看
Willie Wheeler 和 Joshua White 合著的`《Spring实战》`第3章（Manning, 2013）。这一章详细描述了如何用 Spring 3 编写一个 Web
应用，你会直观感受到，为了实现一个简单的一页式 Web
应用，需要写多少配置。该章节可在 [http://mng.bz/46la ](https://livebook.manning.com/book/spring-in-practice/chapter-3/)查看。

正因为如此，随着近年来应用程序的发展，尤其是微服务相关的应用，Spring Boot 变得越来越受欢迎。Spring Boot
为你的项目提供了自动化配置，大大缩短了搭建环境所需的时间。可以说，它非常契合当今软件开发的理念。

在本章中，我们将从第一个使用 Spring Security 的应用程序开始。对于你用 Spring Framework 开发的应用来说，Spring Security
是实现应用级安全性的绝佳选择。我们会使用 Spring Boot，并讨论基于约定的默认配置，同时简单介绍如何覆盖这些默认设置。了解默认配置不仅是学习
Spring Security 的良好起点，也有助于理解认证的基本概念。

一旦我们开始第一个项目，就会更详细地讨论各种身份认证的方案。在第3到第6章中，我们将针对本例中涉及的不同职责，继续介绍更具体的配置方法。你还会看到，根据不同的架构风格，可以用不同的方式应用这些配置。本章我们将讨论以下几个步骤：

1. 创建一个只包含 Spring Security 和 Web 依赖的项目，观察在没有任何配置的情况下它的默认行为。这样，你就能了解默认的身份认证和授权配置会带来什么效果。
2. 修改项目，通过自定义用户和密码来覆盖默认设置，从而增加用户管理功能。
3. 观察到应用默认会对所有接口进行认证后，进一步学习如何自定义这一行为。
4. 针对相同的配置，尝试不同的实现方式，了解最佳实践。

## 开始你的第一个项目

让我们创建第一个项目，这样就有一个实际的例子可以参考。这个项目是一个小型的 Web 应用程序，对外暴露了一个 REST
接口。你将看到，几乎无需额外配置，Spring Security 就能通过 `HTTP Basic 认证`为这个接口提供安全保护。`HTTP Basic` 是一种通过
HTTP 请求头中的一组凭证（用户名和密码）来对用户进行身份验证的方式。

!!! note

	在默认配置下，应用程序同时启用了两种不同的认证机制：HTTP Basic 和表单登录。不过，我打算一步步带大家学习这个示例，关于表单登录的内容会在后面的章节详细讲解。如果你用浏览器访问这个 URL，会发现应用程序实现了一个漂亮的用户认证表单，而不是弹出难看的 HTTP Basic 认证框。之所以这样做，是为了避免让你感到困惑，尤其是在你用浏览器尝试的时候。关于 HTTP Basic 的内容，我们会在后面的章节中重点介绍。

只需创建项目并添加相应的依赖项，Spring Boot 在启动应用程序时就会自动应用默认配置，包括用户名和密码。

!!! note

	你有多种方式可以创建 Spring Boot 项目。一些开发环境支持直接生成项目。想了解更多细节，我推荐阅读 Mark Heckler 的《Spring Boot: Up and Running》（O’Reilly Media, 2021）、Somnath Musib 的《Spring Boot in Practice》（Manning, 2022），或者我自己写的另一本书《Spring Start Here》（Manning, 2021）。

本书中的示例都与配套的源代码相关联。每个示例我都会注明你需要在 pom.xml
文件中添加的依赖项。我建议你下载本书提供的项目和在 [https://www.manning.com/downloads/2105](https://manning-content.s3.amazonaws.com/download/6/0d605f3-dd1a-42a1-8786-e4e9284deb3d/spilca4-main.zip)
上提供的源代码，这对你非常有帮助。如果你在学习过程中遇到困难，这些项目可以为你提供参考，你也可以用它们来验证你的最终解决方案。

!!! note

	本书中的示例与您选择的构建工具无关，您可以使用 Maven 或 Gradle。为了保持一致性，我所有的示例都是用 Maven 构建的。

第一个项目也是最小的一个。它是一个简单的应用程序，提供了一个 REST 接口，你可以调用它并收到响应，如图 2.1 所示。通过这个项目，你可以学习到使用
Spring Security 和 Spring Boot 开发应用的初步步骤。它展示了 Spring Security 在认证和授权方面的基本架构。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202506041001330.png){ loading=lazy }
  <figcaption>图2.1 我们的初始应用在访问某个端点时，采用HTTP Basic进行用户认证和授权。它在指定路由（/hello）上提供了一个REST端点。当请求成功时，返回HTTP 200状态码以及响应体。这个示例展示了Spring Security默认配置下的认证与授权机制。</figcaption>
</figure>

我们从创建一个空项目开始学习 Spring Security，并将其命名为 `ssia-ch2-ex1`。（你也可以在其他提供的项目中找到同名的示例。）在第一个项目中，你只需要添加
`spring-boot-starter-web` 和 `spring-boot-starter-security` 这两个依赖，如代码清单 2.1 所示。创建好项目后，记得把这两个依赖加到你的
pom.xml 文件中。我们做这个项目的主要目的是了解在默认配置下，集成了 Spring Security
的应用会有怎样的表现。同时，我们也希望搞清楚默认配置中包含了哪些组件，以及它们各自的作用。

``` xml title="清单 2.1 我们第一个 Web 应用的 Spring Security 依赖"
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

我们现在可以直接启动应用。Spring Boot 会根据我们添加到项目中的依赖，为我们应用 Spring 上下文的默认配置。不过，如果没有至少一个受保护的端点，我们就很难深入理解安全性。我们先创建一个简单的端点并调用它，看看会发生什么。为此，我们在空项目中添加一个类，命名为 `HelloController`。我们将其放在 Spring Boot 项目主命名空间下的 `controllers` 包中。

!!! note

	Spring Boot 只会扫描包含有 `@SpringBootApplication 注解`的类所在包及其子包中的组件。如果你在主包之外使用 Spring 的任何组件注解标记类，就必须通过 @ComponentScan 注解显式声明这些类的位置。

在下面的代码示例中，HelloController 类定义了一个 REST 控制器和一个用于本例的 REST 接口。

```java title="代码清单 2.2 HelloController 类及其 REST 接口"

@RestController
public class HelloController {

	@GetMapping("/hello")
	public String hello() {
		return "Hello!";
	}
}
```

@RestController 注解会将该 bean 注册到 Spring 容器中，并告知 Spring 该实例作为 Web 控制器使用。此外，该注解还指定应用需要将方法的返回值作为
HTTP 响应的响应体返回。@GetMapping 注解则将 /hello 路径通过 GET 请求映射到对应的方法上。当你运行应用时，除了控制台的其他输出，还应该能看到类似如下的信息：

```shell
Using generated security password: 93a01cf0-794b-4b98-86ef-54860f36f7f3
```

每次运行该应用程序时，都会生成一个新密码，并在控制台中打印出来，就像前面的代码片段所示。你需要使用这个密码，通过 HTTP Basic
认证来调用应用程序的任意接口。首先，我们先尝试在不使用 Authorization 头的情况下调用该接口：

```shell
curl http://localhost:8080/hello
```

!!! note

	在本书中，所有示例我们都使用 cURL 来调用接口。我认为 cURL 是最易读的解决方案。当然，如果你有自己的偏好，也可以选择其他工具。例如，你可能更喜欢带有图形界面的操作方式，这种情况下，Postman、Insomnia 或 Bruno 都是非常不错的选择。如果你的操作系统尚未安装这些工具，可能需要你自行安装。

对呼叫的响应是

```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Unauthorized",
  "path": "/hello"
}
```

响应状态为 HTTP 401 未授权。我们预料到了这个结果，因为我们没有使用正确的认证凭据。默认情况下，Spring Security
期望使用默认用户名（user）和提供的密码（在我的例子中，是以 93a01 开头的那串）。现在让我们用正确的凭据再试一次：

```shell
curl -u user:93a01cf0-794b-4b98-86ef-54860f36f7f3 http://localhost:8080/hello
```

对调用的响应是

```shell
Hello!
```

!!! note

	HTTP 401 未授权状态码有些让人摸不着头脑。通常，它用来表示身份验证失败，而不是授权失败。开发者在设计应用时，会在缺少或填写错误凭证的情况下使用这个状态码。如果是授权失败，我们更倾向于使用 403 Forbidden 状态码。一般来说，HTTP 403 表示服务器已经识别了请求的发起者，但对方没有执行该操作所需的权限。

一旦我们发送了正确的凭证，你就可以在响应体中准确看到我们之前定义的 HelloController 方法返回的内容。

!!! note "使用 HTTP 基本认证调用该端点"

	使用 cURL 时，你可以通过 -u 参数设置 HTTP 基本认证的用户名和密码。实际上，cURL 会将 <用户名>:<密码> 这个字符串用 Base64 编码，并在 Authorization 请求头中以 Basic 为前缀发送。通常来说，使用 -u 参数会更方便。但了解实际的请求内容同样很重要。接下来，我们来试试手动创建 Authorization 请求头。

	第一步，先将 <用户名>:<密码> 这个字符串用 Base64 编码。当我们的应用程序发送请求时，需要知道如何正确生成 Authorization 头的值。你可以在 Linux 控制台使用 Base64 工具来完成编码，也可以在像 https://www.base64encode.org 这样的网站上进行 Base64 编码。下面的代码片段展示了在 Linux 或 Git Bash 控制台中使用的命令（-n 参数表示不添加结尾换行符）：

	```shell
	echo -n user:93a01cf0-794b-4b98-86ef-54860f36f7f3 | base64
	```

	运行此命令会返回以下 Base64 编码的字符串：

	```text
	dXNlcjo5M2EwMWNmMC03OTRiLTRiOTgtODZlZi01NDg2MGYzNmY3ZjM=
	```

	现在，您可以将该 Base64 编码的值用作调用时 Authorization 请求头的值。这个调用应当会产生与使用 -u 选项相同的结果。

	```shell
	curl -H "Authorization: Basic dXNlcjo5M2EwMWNmMC03OTRiLTRiOTgtODZlZi01NDg2MGYzNmY3ZjM="  localhost:8080/hello
	```

	调用的结果是

	```shell
	Hello!
	```

对于默认项目来说，没有太多值得讨论的重要安全配置。我们主要是利用默认配置来验证依赖项是否正确安装。它在认证和授权方面几乎没有任何作用。这样的实现并不适合用于生产环境。但作为起点，默认项目是一个非常不错的示例。

通过第一个示例的运行，至少我们已经确认 Spring Security 已经集成到项目中了。接下来，我们需要调整配置，使其符合项目的具体需求。首先，我们会深入了解
Spring Boot 在 Spring Security 方面默认配置了哪些内容，然后再探讨如何覆盖这些默认配置。

## Spring Security类设计的整体架构

在本节中，我们将讨论在整个架构中参与认证与授权流程的主要组件。你需要了解这些内容，因为你之后可能需要重写这些预设的组件，以满足你的应用需求。我会先介绍
Spring Security
在认证和授权方面的架构原理，随后我们会将这些原理应用到本章的项目中。由于一次性讲解所有内容会过于繁杂，为了降低你的学习难度，本章我会先为每个组件梳理一个整体的框架。关于每个组件的具体细节，你将在接下来的章节中逐步学习到。

在第2.1节中，你已经看到了一些用于认证和授权的逻辑。我们有一个默认用户，并且每次启动应用程序时都会获得一个随机密码。我们可以使用这个默认的用户名和密码去调用某个接口。但这些逻辑到底是在哪里实现的呢？你可能已经知道，Spring
Boot 会根据你所使用的依赖自动为你配置一些组件（也就是我们在本章开头讨论过的`约定优于配置`原则）。

图2.2展示了Spring Security架构中主要参与者（组件）及其之间的关系全貌。这些组件在第一个项目中已经有了预配置的实现。本章将演示Spring
Boot在你的应用中为Spring Security做了哪些配置，并讨论在认证流程中各实体之间的关系。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202508151746307.png){ loading=lazy }
  <figcaption>图2.2 这里重点展示了 Spring Security 认证流程中的核心要素及其相互关系。这一框架构成了使用 Spring Security 实现认证的基础结构。在本书后续章节中，我们会在探讨不同认证与授权策略时频繁引用这一架构。</figcaption>
</figure>

图2.2展示了以下内容：

1. `认证过滤器`将认证请求委托给`认证管理器`，并根据其响应配置`SecurityContext`。
2. `认证管理器`通过`认证提供者`处理认证流程。
3. `认证提供者`实现具体的认证逻辑。
4. `用户详情服务`负责用户管理，这一服务会被认证提供者在认证逻辑中调用。
5. `密码编码器`负责密码管理，同样会被认证提供者在认证逻辑中使用。
6. `SecurityContext`在认证流程结束后保存认证数据，并会一直持有这些数据直到操作结束。通常，在每个请求对应一个线程的应用中，这意味着直到应用向客户端发送响应为止。

在接下来的段落中，我将讨论以下自动配置的 Bean：

- UserDetailsService
- PasswordEncoder

实现了 UserDetailsService 接口的对象会与 Spring Security 一起管理用户的详细信息。到目前为止，我们一直在使用 Spring Boot
提供的默认实现。这个默认实现只会在应用程序的内部内存中注册默认的凭据。默认的用户名是 “user”，密码则是一个全局唯一标识符（UUID）。每次
Spring 上下文加载（即应用启动）时，默认密码都会随机生成。此时，应用会将密码输出到控制台，你可以在控制台上看到它。因此，你可以在本章刚刚演示的示例中使用这个默认密码。

这个默认实现仅作为概念验证，主要用于确认依赖已经建立。它将凭证存储在内存中——应用程序并不会持久化这些凭证。这种做法适合用在示例或概念验证阶段，但在正式生产环境中应当避免使用。

接下来我们来看 PasswordEncoder。PasswordEncoder 主要有两个功能：

- 对密码进行编码（通常使用加密或哈希算法）
- 验证密码是否与已有的编码匹配

即使不像 UserDetailsService 对象那样显眼，PasswordEncoder 在 Basic
认证流程中也是必不可少的。最简单的实现方式是以明文管理密码，并且不进行加密。我们会在第四章更详细地讨论这个对象的具体实现。目前你只需要知道，PasswordEncoder
会和默认的 UserDetailsService 一起存在。当我们替换掉默认的 UserDetailsService 实现时，也必须指定一个 PasswordEncoder。

Spring Boot 在配置默认设置时，也会选择一种认证方式：HTTP Basic 访问认证。这是一种最简单直接的认证方式。基本认证只要求客户端通过
HTTP 的 Authorization 头部发送用户名和密码。在该头部的值中，客户端会加上 Basic 前缀，后面跟着用 Base64
编码的字符串，这个字符串由用户名和密码组成，中间用冒号（:）分隔。

!!! note

	HTTP Basic 认证并不能保证凭证的机密性。Base64 仅仅是一种编码方式，目的是便于传输，它并不是加密或哈希方法。在数据传输过程中，如果被拦截，任何人都可以直接看到凭证。因此，通常我们不会在没有 HTTPS 的情况下单独使用 HTTP Basic 认证来保护机密信息。你可以在 RFC 7617（[https://tools.ietf.org/html/rfc7617](https://datatracker.ietf.org/doc/html/rfc7617)）中查阅 HTTP Basic 的详细定义。

AuthenticationProvider 定义了认证逻辑，并将用户和密码的管理委托出去。默认情况下，AuthenticationProvider 会使用
UserDetailsService 和 PasswordEncoder 的默认实现。实际上，应用会自动保护所有接口，因此在我们的示例中，只需要添加接口即可。而且，只有一个用户可以访问所有接口，所以在授权方面基本无需额外处理。

!!! note "HTTP vs. HTTPS"

	你可能已经注意到，在前面的示例中我只使用了 HTTP。不过在实际应用中，你的程序通常只会通过 HTTPS 进行通信。对于本书中讨论的示例来说，无论是使用 HTTP 还是 HTTPS，Spring Security 相关的配置其实并没有区别。为了让你更专注于 Spring Security 相关的内容，我们在示例中不会为接口配置 HTTPS。当然，如果你有需要，也可以像本侧栏所介绍的那样，为任意接口启用 HTTPS。

	在系统中配置 HTTPS 有多种方式。有些情况下，开发者会在应用层配置 HTTPS；有时也可能借助服务网格，或者选择在基础设施层面进行配置。使用 Spring Boot，你可以非常方便地在应用层启用 HTTPS，接下来的小节会通过示例为你详细介绍。

	在上述任一配置场景中，你都需要一个由证书颁发机构（CA）签署的证书。通过这个证书，调用端点的客户端可以确认响应确实来自认证服务器，并且通信过程未被第三方拦截。如果有需要，你可以购买这样的证书。如果只是为了测试应用而配置 HTTPS，也可以使用像 OpenSSL（https://www.openssl.org/）这样的工具生成自签名证书。接下来，我们将生成自签名证书，并在项目中进行配置：

	```shell
	openssl req -newkey rsa:2048 -x509 -keyout key.pem -out cert.pem -days 365
	```

	第二条命令以第一条命令生成的两个文件作为输入，并输出自签名证书。

	请注意，如果你在 Windows 系统的 Bash shell 中运行这些命令，可能需要在命令前加上 winpty。

	```shell
	winpty openssl req -newkey rsa:2048 -x509 -keyout key.pem -out cert.pem -days 365
	winpty openssl pkcs12 -export -in cert.pem -inkey key.pem -out certificate.p12 -name "certificate"
	```

	最后，拿到自签名证书后，你就可以为你的接口配置 HTTPS 了。将 certificate.p12 文件复制到 Spring Boot 项目的 resources 目录下，并在 application.properties 文件中添加以下配置：

	```properties
	server.ssl.key-store-type=PKCS12
	server.ssl.key-store=classpath:certificate.p12
	server.ssl.key-store-password=12345
	```

	在生成证书的命令执行后，系统会在提示中要求输入密码（比如我这里用的是12345），所以你在命令里看不到密码。现在，我们来给应用添加一个测试接口，然后通过 HTTPS 调用它：

	```java
	@RestController
	public class HelloController {
	
	 @GetMapping("/hello")
	 public String hello() {
	   return "Hello!";
	 }
	}
	```

	如果你使用自签名证书，需要在你调用接口所用的工具中进行相应配置，使其跳过证书真实性的校验。如果工具对证书进行真实性校验，由于无法识别自签名证书为可信，调用将无法成功。以 cURL 为例，你可以使用 -k 选项来跳过证书真实性校验：

	```shell
	curl -k -u user:93a01cf0-794b-4b98-86ef-54860f36f7f3  https://localhost:8080/hello
	```	

	响应调用的是

	```shell
	Hello!
	```

	请记住，即使你使用了 HTTPS，系统各组件之间的通信也并非万无一失。我经常听到有人说：“我不加密这个了，直接用 HTTPS 就行！”虽然 HTTPS 能在一定程度上保护通信安全，但它只是系统安全防护墙中的一块砖。始终要以负责任的态度对待系统安全，关注并妥善处理涉及的每一层防护。

## 覆盖默认配置

既然你已经了解了第一个项目的默认设置，接下来就该看看如何替换这些默认配置了。你需要掌握覆盖默认组件的各种方式，因为这是将自定义实现集成进项目并根据实际需求应用安全措施的关键所在。正如你将在本节中学到的，开发过程同样涉及如何编写配置，以保持应用程序的高可维护性。在我们接下来要做的项目中，你会发现通常有多种方式可以覆盖配置。这种灵活性有时也会带来困惑。我经常看到同一个应用里，不同部分的
Spring Security 配置混用了不同的风格，这其实并不理想。因此，这种灵活性也需要谨慎对待。你需要学会如何在这些选项中做出选择，所以本节同样会帮助你了解有哪些可用的方案。

在某些情况下，开发者会选择在 Spring 容器中使用 Bean 进行配置；而在另外一些情况下，则会通过重写各种方法来实现同样的目的。Spring
生态系统的快速发展，可能是导致出现多种配置方式的主要原因之一。如果在一个项目中混合使用不同的配置风格，会让代码变得难以理解，也会影响应用的可维护性。了解各种配置选项及其用法是一项非常有价值的技能，这不仅能帮助你更好地理解项目中应用级安全的配置方式，也有助于提升整体开发效率。

在本节中，你将学习如何配置 UserDetailsService 和 PasswordEncoder。这两个组件通常参与认证过程，大多数应用程序会根据自身需求对其进行定制。虽然我们将在第
3 章和第 4 章详细讨论如何自定义它们，但现在了解如何接入自定义实现同样非常重要。本章中用到的实现均由 Spring Security 提供。

### 自定义用户信息管理

本章我们首先讨论的组件是 UserDetailsService。正如你所看到的，应用程序在认证过程中会用到这个组件。在本节中，你将学习如何自定义一个
UserDetailsService 类型的 Bean，以覆盖 Spring Boot 默认配置的实现。正如你将在第三章中更详细地了解到的，你可以选择自己实现一个
UserDetailsService，或者使用 Spring Security 提供的预定义实现。在本章中，我们不会详细介绍 Spring Security
提供的各种实现，也不会自己动手写一个实现。我会直接使用 Spring Security 提供的 InMemoryUserDetailsManager
这个实现。通过这个例子，你将学会如何将这类对象集成到你的架构中。

!!! note

	在 Java 中，接口用于定义对象之间的契约。在应用程序的类设计中，我们通过接口来实现对象之间的解耦。为了强调接口的这一特性，在本书中讨论相关内容时，我通常将接口称为“契约”。

为了向你展示如何用我们选择的实现方式来重写这个组件，我们将对第一个示例进行一些修改。这样一来，我们就可以使用自己管理的凭证来进行身份验证。在这个例子中，我们并没有自己实现一个类，而是采用了
Spring Security 提供的实现。

在这个示例中，我们使用了 InMemoryUserDetailsManager 实现。虽然它不仅仅是一个 UserDetailsService，但目前我们只从
UserDetailsService 的角度来介绍它。该实现会将用户凭据存储在内存中，Spring Security 随后可以利用这些信息来对请求进行身份验证。

!!! note

	InMemoryUserDetailsManager 的实现`并不适用于生产环境`，但它是进行示例演示或概念验证的绝佳工具。在某些情况下，你只需要一些用户数据，无需花时间去实现这部分功能。在我们的例子中，我们用它来了解如何重写默认的 UserDetailsService 实现。

我们首先定义一个配置类。通常，我们会在名为 config 的单独包中声明配置类。下面的代码展示了该配置类的定义。你也可以在项目
ssia-ch2-ex2 中找到这个示例。

```java title="代码清单 2.3 UserDetailsService Bean 的配置类"

@Configuration
public class ProjectConfig {

	@Bean
	UserDetailsService userDetailsService() {
		return new InMemoryUserDetailsManager();
	}
}
```

我们在类上添加了 @Configuration 注解。@Bean 注解则告诉 Spring，将该方法返回的实例加入到 Spring
容器中。如果你现在直接运行这段代码，控制台里将不会再显示自动生成的密码。应用现在会使用你手动添加到容器中的
UserDetailsService 实例，而不是默认自动配置的那个。但与此同时，你也将无法再访问该端点，原因有两个：

- 你还没有任何用户。
- 你也没有配置 PasswordEncoder。

在图 2.2 中你已经看到，认证同样依赖于 PasswordEncoder。让我们一步一步来解决这两个问题。我们需要：

1. 至少创建一个拥有用户名和密码凭证的用户；
2. 将该用户添加到我们实现的 UserDetailsService 中进行管理；
3. 定义一个 PasswordEncoder 类型的 Bean，供我们的应用用来校验输入的密码与 UserDetailsService 存储和管理的密码是否一致。

首先，我们声明并添加一组可用于身份验证的凭据到 InMemoryUserDetailsManager
实例中。在第三章，我们会更详细地讨论用户以及如何管理他们。现在，我们先用一个预定义的构建器来创建一个 UserDetails 类型的对象。

!!! note

	有时候你会看到我在代码中使用 var。Java 10 引入了保留类型名 var，并且你只能用它来声明局部变量。虽然从代码整洁的角度来看，本书中某些地方对 var 的用法可能并不理想，但这样做是为了让语法更简洁，同时也隐藏了变量的类型。这样可以让你更专注于当前示例中真正重要的内容。我们会在后面的章节详细讨论被 var 隐藏的类型，所以你现在不用担心这些类型，等到需要深入分析时再了解也不迟。

在创建实例时，我们必须提供用户名、密码，以及至少一个权限。权限指的是该用户被允许执行的某项操作，这里可以使用任意字符串。在下面的示例中，我将权限命名为
read，不过由于目前我们不会用到这个权限，所以具体叫什么其实无关紧要。

```java title="代码清单 2.4 使用 User 构建器类为 UserDetailsService 创建用户"

@Configuration
public class ProjectConfig {

	@Bean
	UserDetailsService userDetailsService() {
		var user = User.withUsername("john")
				.password("12345")
				.authorities("read")
				.build();

		return new InMemoryUserDetailsManager(user);
	}
}

```

!!! note

	你会在 org.springframework.security.core.userdetails 包中找到 User 这个类。它是我们用来创建用户对象的构建器实现。此外，本书有一个通用规则：如果我没有在代码示例中展示如何编写某个类，那就说明这个类是由 Spring Security 提供的。

如清单2.4所示，我们必须为用户名、密码以及至少一个权限分别提供一个值。不过，这些还不足以让我们调用该端点，我们还需要声明一个
PasswordEncoder。

在使用默认的 UserDetailsService 时，PasswordEncoder 也会被自动配置。但由于我们重写了 UserDetailsService，因此还需要手动声明一个
PasswordEncoder。现在如果你尝试运行这个示例，在调用接口时会看到一个异常。Spring Security
在进行认证时发现无法处理密码，因此认证失败。异常信息类似于下面的代码片段，你应该能在应用的控制台中看到。客户端会收到一个
HTTP 401 Unauthorized 响应，且响应体为空：

```shell
curl -u john:12345 http://localhost:8080/hello
```

在应用的控制台中，调用的结果是

```shell
java.lang.IllegalArgumentException: 
未找到与 id "null" 对应的 PasswordEncoder
    at 
org.springframework.security.crypto.
password.DelegatingPasswordEncoder$
UnmappedIdPasswordEncoder.matches(
DelegatingPasswordEncoder.java:289) 
~[spring-security-crypto-6.0.0.jar:6.0.0]
    at org.springframework.security.crypto.
password.DelegatingPasswordEncoder.matches(
DelegatingPasswordEncoder.java:237) 
~[spring-security-crypto-6.0.0.jar:6.0.0]
```

为了解决这个问题，我们可以像添加 UserDetailsService 一样，在上下文中添加一个 PasswordEncoder Bean。对于这个
Bean，我们可以直接使用已有的 PasswordEncoder 实现类。

```java

@Bean
public PasswordEncoder passwordEncoder() {
	return NoOpPasswordEncoder.getInstance();
}
```

!!! note

	NoOpPasswordEncoder 实例将密码视为明文处理，不会对其进行加密或哈希。在进行匹配时，NoOpPasswordEncoder 只是通过 String 类底层的 equals(Object o) 方法来比较字符串。你不应该在生产环境的应用中使用这种类型的 PasswordEncoder。NoOpPasswordEncoder 更适合用于那些不需要关注密码哈希算法的示例场景。因此，该类的开发者已经将其标记为 @Deprecated，你的开发环境中也会以删除线的形式显示它的名称。

您可以在下方的代码清单中查看完整的配置类代码。

```java

@Configuration
public class ProjectConfig {

	@Bean
	UserDetailsService userDetailsService() {
		var user = User.withUsername("john")
				.password("12345")
				.authorities("read")
				.build();

		return new InMemoryUserDetailsManager(user);
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return NoOpPasswordEncoder.getInstance();
	}
}

```

让我们用用户名为 john、密码为 12345 的新用户来试一下这个接口：

```shell
curl -u john:12345 http://localhost:8080/hello
Hello!
```

!!! note

	了解单元测试和集成测试的重要性后，有些读者可能会好奇，为什么我们没有为示例代码编写测试。其实，本书中提供的所有示例都配套有相关的 Spring Security 集成测试。不过，为了让大家能够专注于每一章节所讲解的核心内容，我将关于 Spring Security 集成测试的相关讨论单独放在了第18章进行详细讲解。

### 在端点级别实施授权

随着用户管理方式的更新（详见第2.3.1节），我们现在可以讨论终端的认证方式和配置了。关于授权配置，你将在第7到第12章学到很多内容。但在深入细节之前，你需要先了解整体框架。最好的方式就是通过我们的第一个示例。默认配置下，所有终端都假定你拥有由应用程序管理的有效用户。同时，应用默认采用HTTP
Basic认证，不过你可以很方便地修改这一配置。

正如你将在接下来的章节中了解到的，HTTP Basic
认证并不适用于大多数应用架构。有时候，我们希望根据自己的应用需求对其进行调整。同样，应用的所有端点也并不都需要安全保护，对于那些需要保护的端点，我们可能还需要选择不同的认证方式和授权规则。为了自定义认证和授权的处理方式，我们需要定义一个
SecurityFilterChain 类型的 bean。在这个例子中，我会继续在项目 ssia-ch2-ex2 中编写相关代码。

```java title="代码清单 2.6 定义 SecurityFilterChain Bean"

@Configuration
public class ProjectConfig {

	@Bean
	SecurityFilterChain configure(HttpSecurity http)
			throws Exception {

		return http.build();
	}

	// Omitted code
}
```

接下来，我们可以通过 HttpSecurity 对象的不同方法来修改配置，如下所示。

```java title="代码清单 2.7 通过 HttpSecurity 参数修改配置"

@Configuration
public class ProjectConfig {

	@Bean
	SecurityFilterChain configure(HttpSecurity http)
			throws Exception {

		http.httpBasic(Customizer.withDefaults());

		http.authorizeHttpRequests(
				c -> c.anyRequest().authenticated()
		);

		return http.build();
	}

	// Omitted code

}

```

清单2.7中的代码配置了端点授权，其行为与默认配置相同。你可以再次调用该端点，看看它的表现是否与2.3.1节中的上一次测试一致。只需稍作修改，你就可以让所有端点在无需凭证的情况下访问。接下来你将看到如何实现这一点。

```java title="代码清单 2.8 使用 permitAll() 修改授权配置"

@Configuration
public class ProjectConfig {

	@Bean
	public SecurityFilterChain configure(HttpSecurity http)
			throws Exception {

		http.httpBasic(Customizer.withDefaults());

		http.authorizeHttpRequests(
				c -> c.anyRequest().permitAll()
		);

		return http.build();
	}

	// Omitted code
}

```

现在，我们可以在无需凭证的情况下调用 /hello 接口。在配置中使用 permitAll() 方法，并结合 anyRequest()
方法，使得所有接口都可以在不需要凭证的情况下访问。

```shell
curl http://localhost:8080/hello
```

调用的响应体是

```shell
Hello!
```

在这个示例中，我们使用了两种配置方法：

- `httpBasic()`：用于配置认证方式。调用该方法后，你就指定应用程序采用 HTTP Basic 作为认证方式。
- `authorizeHttpRequests()`：用于在端点级别配置授权规则。通过调用该方法，你可以指定应用程序如何对特定端点接收到的请求进行授权。

对于这两种方法，你都需要传入一个 Customizer 对象作为参数。Customizer 是一个接口，你可以通过实现它来定义对 Spring Security
各个元素的自定义配置，比如认证、授权，或者特定的安全机制（如 CSRF 或 CORS，这些内容将在第 9 章和第 10 章中讨论）。

下面的代码片段展示了 Customizer 接口的定义。可以看到，Customizer 是一个函数式接口（因此我们可以用 lambda 表达式来实现它），而我在代码清单
2.8 中使用的 withDefaults() 方法，其实就是一个什么都不做的 Customizer 实现：

``` java
@FunctionalInterface
public interface Customizer<T> {
 void customize(T t);

 static <T> Customizer<T> withDefaults() {
   return (t) -> {
   };
 }
}
```

在早期的 Spring Security 版本中，你可以通过链式调用语法直接应用配置，而无需使用 Customizer 对象，如下面的代码片段所示。请注意，这里在调用
authorizeHttpRequests() 方法时，并没有传入 Customizer 对象，配置内容直接跟在方法调用之后。

``` java
http.authorizeHttpRequests() 
      .anyRequest().authenticated()
```

之所以不再采用这种方式，是因为使用 Customizer 对象可以让你在需要的时候更灵活地调整配置。没错，对于简单的场景来说，使用
lambda 表达式确实很方便。但在实际应用中，配置项往往会变得非常复杂。这时候，将这些配置拆分到独立的类中，不仅有助于维护，也更便于测试。

本例旨在让你了解如何自定义默认配置。关于授权的具体内容，我们将在第7到第10章详细介绍。

!!! note

	在早期版本的 Spring Security 中，安全配置类需要继承一个名为 WebSecurityConfigurerAdapter 的类。现在我们已经不再采用这种做法。如果你的应用还在使用旧的代码库，或者需要升级旧代码库，建议你也阅读《Spring Security实战》第一版。

### 以不同方式进行配置

在使用 Spring Security 进行配置时，常常会遇到同一个功能有多种配置方式，这也是让人困惑的地方之一。本节将为你介绍配置
UserDetailsService 和 PasswordEncoder
的不同方法。了解这些选项非常重要，这样你在阅读本书或其他博客、文章时，能够识别出相关的配置方式。同时，你也需要清楚这些方法在你的应用中该如何选择和使用。后续章节还会通过不同的示例，进一步扩展本节的内容。

让我们来看第一个项目。在创建了一个默认应用之后，我们通过在 Spring 上下文中添加新的实现类作为 Bean，成功地重写了
UserDetailsService 和 PasswordEncoder。现在，我们来探索另一种方式，对 UserDetailsService 和 PasswordEncoder 进行相同的配置。

我们可以直接通过 SecurityFilterChain bean 来同时设置 UserDetailsService 和 PasswordEncoder，具体实现如下所示。你可以在项目
ssia-ch2-ex3 中找到这个示例。

```java title="清单 2.9 通过 SecurityFilterChain Bean 设置 UserDetailsService"

@Configuration
public class ProjectConfig {

	@Bean
	public SecurityFilterChain configure(HttpSecurity http)
			throws Exception {

		http.httpBasic(Customizer.withDefaults());
		http.authorizeHttpRequests(
				c -> c.anyRequest().authenticated()
		);

		var user = User.withUsername("john")
				.password("12345")
				.authorities("read")
				.build();

		var userDetailsService =
				new InMemoryUserDetailsManager(user);

		http.userDetailsService(userDetailsService);

		return http.build();
	}

	// Omitted code

}

```

在代码清单2.9中，你可以看到我们声明 UserDetailsService 的方式与清单2.5相同。不同之处在于，这次我们是在创建
SecurityFilterChain 的 bean 方法内部进行本地声明的。我们还通过 HttpSecurity 的 userDetailsService() 方法注册了
UserDetailsService 实例。接下来的清单展示了配置类的完整内容。

```java title="清单 2.10 配置类的完整定义"

@Configuration
public class ProjectConfig {

	@Bean
	SecurityFilterChain configure(HttpSecurity http)
			throws Exception {

		http.httpBasic(Customizer.withDefaults());

		http.authorizeHttpRequests(
				c -> c.anyRequest().authenticated()
		);

		var user = User.withUsername("john")
				.password("12345")
				.authorities("read")
				.build();

		var userDetailsService =
				new InMemoryUserDetailsManager(user);

		http.userDetailsService(userDetailsService);

		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return NoOpPasswordEncoder.getInstance();
	}
}

```

这几种配置方式都是正确的。第一种方式是将 bean 添加到上下文中，这样你就可以在其他类中注入这些值，如果你可能会用到的话。但如果你的场景不需要这样做，第二种方式同样也很合适。

### 自定义认证逻辑

正如你已经注意到的，Spring Security 组件非常灵活，能够根据我们的应用架构提供多种适配方案。到目前为止，你已经了解了
UserDetailsService 和 PasswordEncoder 在 Spring Security
架构中的作用，也看过了几种配置它们的方法。现在，是时候进一步学习如何自定义委托给它们的组件——`AuthenticationProvider`，如图
2.3
所示。AuthenticationProvider 实现了认证逻辑，并将用户和密码的管理工作分别交给 UserDetailsService 和
PasswordEncoder。因此，可以说在本节中，我们将更深入地探讨认证架构，学习如何通过 AuthenticationProvider 实现自定义的认证逻辑。

由于这是第一个示例，我只为你展示一个简要的示意图，帮助你更好地理解架构中各个组件之间的关系。接下来的第3到第6章，我们会进行更深入的讲解。

我建议你参考 Spring Security 架构中设计好的各项职责。这个架构采用了松耦合和细粒度的责任分配，这也是 Spring Security
灵活且易于集成到你的应用中的原因之一。根据你对其灵活性的具体使用方式，你也可以调整其设计。不过需要注意，这些做法可能会让你的解决方案变得更加复杂。举个例子，你可以选择以某种方式重写默认的
AuthenticationProvider，从而不再需要 UserDetailsService 或 PasswordEncoder。考虑到这些因素，代码清单 2.11
展示了如何创建一个自定义的认证提供者。你可以在项目 ssia-ch2-ex4 中找到这个示例。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202508181750618.png){ loading=lazy }
  <figcaption>图2.3 AuthenticationProvider 实现了认证逻辑。它接收来自 AuthenticationManager 的请求，并将查找用户的任务委托给 UserDetailsService，将密码校验的任务交给 PasswordEncoder。</figcaption>
</figure>

```java title="代码清单 2.11 实现 AuthenticationProvider 接口"

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {

		// 在此编写认证逻辑
	}

	@Override
	public boolean supports(Class<?> authenticationType) {

		// 在此处填写认证实现的类型
	}
}
```

authenticate(Authentication authentication) 方法包含了所有的认证逻辑，因此我们会像清单 2.12 那样添加一个实现。关于
supports() 方法的具体用法，我会在第 6 章详细讲解。现在，你可以暂时不用关心它的实现，对于当前的示例来说并不是关键。

```java title="代码清单 2.12 实现认证逻辑"

@Override
public Authentication authenticate(
		Authentication authentication)
		throws AuthenticationException {

	String username = authentication.getName();
	String password = String.valueOf(
			authentication.getCredentials());

	if ("john".equals(username) &&
			"12345".equals(password)) {
		return new UsernamePasswordAuthenticationToken(
				username,
				password,
				Arrays.asList());
	} else {
		throw new AuthenticationCredentialsNotFoundException("Error!");
	}

}

```

这里，if-else 语句的条件实际上替代了 UserDetailsService 和 PasswordEncoder 的职责。你并不是必须使用这两个
bean，但如果你需要处理用户和密码的认证，强烈建议你将相关管理逻辑进行分离。即使你重写了认证的实现方式，也应按照 Spring
Security 的架构设计来应用它。

你可能会发现，通过实现自定义的 AuthenticationProvider 来替换默认的认证逻辑会更适合你的需求。如果默认的实现无法完全满足你应用的要求，你可以选择编写自己的认证逻辑。完整的
AuthenticationProvider 实现如下所示。

``` java title="代码清单 2.13 认证提供者的完整实现"

@Component
public class CustomAuthenticationProvider
		implements AuthenticationProvider {

	@Override
	public Authentication authenticate(
			Authentication authentication)
			throws AuthenticationException {

		String username = authentication.getName();
		String password = String.valueOf(authentication.getCredentials());

		if ("john".equals(username) &&
				"12345".equals(password)) {
			return new UsernamePasswordAuthenticationToken(
					username, password, Arrays.asList());
		} else {
			throw new AuthenticationCredentialsNotFoundException("Error!");
		}
	}

	@Override
	public boolean supports(Class<?> authenticationType) {
		return UsernamePasswordAuthenticationToken
				.class
				.isAssignableFrom(authenticationType);
	}
}
```

在配置类中，你可以通过如下示例所示的 HttpSecurity 的 authenticationProvider() 方法来注册 AuthenticationProvider。

```java title="代码清单 2.14 注册新的 AuthenticationProvider 实现"

@Configuration
public class ProjectConfig {

	private final CustomAuthenticationProvider authenticationProvider;

	public ProjectConfig(
			CustomAuthenticationProvider authenticationProvider) {

		this.authenticationProvider = authenticationProvider;
	}

	@Bean
	SecurityFilterChain configure(HttpSecurity http) throws Exception {
		http.httpBasic(Customizer.withDefaults());

		http.authenticationProvider(authenticationProvider);

		http.authorizeHttpRequests(
				c -> c.anyRequest().authenticated()
		);

		return http.build();
	}
}
```

现在，你可以调用该端点。根据认证逻辑，只有被识别的用户 john（密码为 12345）可以访问。

```shell
curl -u john:12345 http://localhost:8080/hello
```

响应体是

```shell
Hello!
```

在第六章中，你将进一步了解 AuthenticationProvider，并学习如何在认证过程中自定义其行为。同样在本章，我们还会探讨
Authentication 接口及其实现类，比如 UserPasswordAuthenticationToken。

### 使用多个配置类

在之前的示例中，我们只使用了一个配置类。不过，将配置类的职责进行拆分其实是一个更好的实践。随着配置变得越来越复杂，这种拆分就显得尤为重要。在一个面向生产环境的应用中，你的配置声明很可能会比我们最初的示例多得多。为了让项目结构更加清晰，通常也会采用多个配置类。

通常来说，每个类只负责一个职责是一个很好的实践。在本例中，我们可以将用户管理配置和授权配置分离开来。为此，我们定义了两个配置类：UserManagementConfig（在下一个代码清单中定义）和WebAuthorizationConfig（见代码清单2.16）。你可以在项目ssia-ch2-ex5中找到这个示例。

```java title="代码清单 2.15 用户与密码管理的配置类定义"

@Configuration
public class UserManagementConfig {

	@Bean
	public UserDetailsService userDetailsService() {
		var userDetailsService = new InMemoryUserDetailsManager();

		var user = User.withUsername("john")
				.password("12345")
				.authorities("read")
				.build();

		userDetailsService.createUser(user);
		return userDetailsService;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return NoOpPasswordEncoder.getInstance();
	}
}
```

在这种情况下，UserManagementConfig 类只包含了负责用户管理的两个 Bean：UserDetailsService 和 PasswordEncoder。下面的代码展示了这一定义。

```java title="代码清单 2.16 定义授权管理的配置类"

@Configuration
public class WebAuthorizationConfig {

	@Bean
	SecurityFilterChain configure(HttpSecurity http)
			throws Exception {

		http.httpBasic(Customizer.withDefaults());

		http.authorizeHttpRequests(
				c -> c.anyRequest().authenticated()
		);

		return http.build();
	}
}
```

在这里，WebAuthorizationConfig 类需要定义一个 SecurityFilterChain 类型的 Bean，用于配置认证和授权规则。

## 总结

- 当你在应用的依赖中加入 Spring Security 时，Spring Boot 会自动提供一些默认配置。
- 你需要实现以下用于认证和授权的基础组件：UserDetailsService、PasswordEncoder 和 AuthenticationProvider。
- 你可以通过 User 类来定义用户。一个用户至少需要包含用户名、密码和权限。权限指的是你允许用户在应用中执行的操作。
- Spring Security 提供了一个简单的 UserDetailsService 实现——InMemoryUserDetailsManager。你可以将用户添加到这个
  UserDetailsService 实例中，从而在应用内存中管理用户。
- NoOpPasswordEncoder 是 PasswordEncoder 接口的一种实现，它以明文方式存储和校验密码。这个实现适合用于学习示例或概念验证，但不适合生产环境。
- 你可以通过实现 AuthenticationProvider 接口，在应用中自定义认证逻辑。
- 配置方式有多种选择，但在同一个应用中，建议选择并坚持一种方式，这样可以让代码更加简洁易懂。

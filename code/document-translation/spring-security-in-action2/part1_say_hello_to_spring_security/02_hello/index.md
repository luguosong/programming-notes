# Spring Security 入门

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
文件中添加的依赖项。我建议你下载本书提供的项目和在 [https://www.manning.com/downloads/2105](https://www.manning.com/downloads/2105)
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

我们现在可以直接启动应用程序了。Spring Boot 会根据我们在项目中添加的依赖，为我们自动应用 Spring
上下文的默认配置。不过，如果没有至少一个受保护的接口，我们其实很难深入了解安全相关的内容。接下来，我们来创建一个简单的接口，并调用它看看会发生什么。为此，我们需要在空项目中添加一个类，命名为
HelloController。具体做法是在 Spring Boot 项目的主命名空间下，新建一个名为 controllers 的包，并在其中添加这个类。

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

通过第一个示例的运行，至少我们已经确认 Spring Security 已经集成到项目中了。接下来，我们需要调整配置，使其符合项目的具体需求。首先，我们会深入了解 Spring Boot 在 Spring Security 方面默认配置了哪些内容，然后再探讨如何覆盖这些默认配置。

## Spring Security类设计的整体架构

在本节中，我们将讨论在整个架构中参与认证与授权流程的主要组件。你需要了解这些内容，因为你之后可能需要重写这些预设的组件，以满足你的应用需求。我会先介绍 Spring Security 在认证和授权方面的架构原理，随后我们会将这些原理应用到本章的项目中。由于一次性讲解所有内容会过于繁杂，为了降低你的学习难度，本章我会先为每个组件梳理一个整体的框架。关于每个组件的具体细节，你将在接下来的章节中逐步学习到。

在第2.1节中，你已经看到了一些用于认证和授权的逻辑。我们有一个默认用户，并且每次启动应用程序时都会获得一个随机密码。我们可以使用这个默认的用户名和密码去调用某个接口。但这些逻辑到底是在哪里实现的呢？你可能已经知道，Spring Boot 会根据你所使用的依赖自动为你配置一些组件（也就是我们在本章开头讨论过的“约定优于配置”原则）。

图2.2展示了Spring Security架构中主要参与者（组件）及其之间的关系全貌。这些组件在第一个项目中已经有了预配置的实现。本章将演示Spring Boot在你的应用中为Spring Security做了哪些配置，并讨论在认证流程中各实体之间的关系。



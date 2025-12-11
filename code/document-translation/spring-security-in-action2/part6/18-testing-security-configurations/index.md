# 测试安全配置

本章内容包括：

- 测试端点与 Spring Security 配置的集成
- 在测试中定义模拟用户（mock users）
- 测试与 Spring Security 的方法级安全集成
- 测试基于响应式 Spring 的实现

---

传说，单元测试和集成测试的起源可以用这样一段打油诗来概括：

代码里有 99 个小 bug，

99 个小 bug。

查出一个，修补一下，

代码里现在有 113 个小 bug。

——佚名

---

随着时间推移，软件变得越来越复杂，团队规模也不断扩大。要完全了解其他人陆续实现的所有功能已变得不可能。开发人员因此需要一种机制，来确保在修复缺陷或实现新功能时，不会破坏已有的功能。

在开发应用程序的过程中，我们会持续编写测试，用来验证我们实现的功能是否按预期工作。  
我们之所以编写单元测试和集成测试，主要是为了在修复缺陷或实现新功能时，确保已有功能不会被意外破坏。这类测试也被称为回归测试。

如今，当开发者完成某项修改后，会将变更提交到团队用于代码版本管理的服务器上。这个操作会自动触发持续集成工具，运行所有现有测试。如果这次修改破坏了某些已有功能，测试就会失败，持续集成工具也会通知团队（见图
18.1）。通过这种方式，就不太容易把影响现有功能的变更发布出去。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251207160915155.png){ loading=lazy }
  <figcaption>图 18.1 测试是开发流程的一部分。每当开发者提交代码时，测试都会自动运行。如果有任何测试失败，持续集成工具会通知开发者。</figcaption>
</figure>

!!! note

	在这张图里使用 Jenkins，并不是说它是唯一可用的持续集成工具，也不是说它是最好的。你还有很多其他选择，比如 Bamboo、GitLab CI、CircleCI 等等。

在测试应用程序时，需要牢记的一点是：你不仅要测试自己写的应用代码，还要测试与所用框架和库之间的集成情况（见图
18.2）。将来某个时间点，你可能会把这些框架或库升级到新版本。当你调整依赖的版本时，需要确保你的应用仍然能与新版本正常集成。如果集成方式发生了变化，导致应用无法像之前那样工作，你就需要能够快速定位到哪些地方需要修改，以修复这些集成问题。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251207161007867.png){ loading=lazy }
  <figcaption>图 18.2 应用程序的功能依赖于许多外部组件。当你升级或更换某个依赖时，可能会影响现有功能。为这些依赖编写集成测试，有助于你在依赖发生变化时及早发现其是否对应用程序的既有功能产生影响。</figcaption>
</figure>

这也正是你需要了解本章内容的原因——如何测试应用程序与 Spring Security 的集成。Spring Security 和整个 Spring
生态一样，迭代非常迅速。你很可能会不断将应用升级到新版本，而你当然需要明确，升级到某个特定版本后，是否会在你的应用中引入新的漏洞、错误，或者兼容性问题。

还记得我们从第一章就强调的一点吗：你必须从应用最初的设计阶段就开始考虑安全问题，而且要严肃对待这件事。为任何安全配置编写测试都应该是强制性的工作内容，并且要被纳入“完成定义”（definition
of done）的一部分。如果安全相关的测试还没有就绪，你就不应该认为这个任务已经完成。

在本章中，我们将讨论几种用于测试应用与 Spring Security
集成的实践。我们会回到前几章中实现过的一些示例，带你学习如何为这些功能编写集成测试。整体而言，测试是一个至关重要的环节，而深入学习这一主题将带来诸多好处。

在本章中，我们将重点介绍如何测试应用程序与 Spring Security
之间的集成。在开始示例之前，我想先推荐几本帮助我深入理解这一主题的书。如果你希望更系统、更细致地掌握相关内容，或者想要温故知新，可以参考这些书籍。我相信它们会对你大有裨益！

- 《JUnit in Action, Third Edition》，作者：Cătălin Tudose 等（Manning，2020）
- 《Unit Testing Principles, Practices, and Patterns》，作者：Vladimir Khorikov（Manning，2020）
- 《Testing Java Microservices》，作者：Alex Soto Bueno 等（Manning，2018）

我们在为安全性实现编写测试的旅程中，将从测试授权配置开始。  
在 18.1 节中，你将学习如何跳过认证，并定义模拟用户（mock users），以便在端点级别测试授权配置。  
接着在 18.2 节中，你会学习如何结合 UserDetailsService 中的用户来测试授权配置。  
在 18.3 节里，我们会讨论如何在需要使用特定 Authentication 实现的情况下，完整地配置安全上下文。  
最后，在 18.4 节中，你将把前几节学到的这些方法应用到方法级安全上，对方法安全的授权配置进行测试。

在完成关于授权测试的讨论之后，第 18.5 节会带你学习如何测试认证流程。接下来，在第 18.6 和 18.7
节中，我们将讨论如何测试其他安全配置，比如跨站请求伪造（CSRF）和跨域资源共享（CORS）。本章最后在第 18.8 节中，将介绍 Spring
Security 与响应式应用的集成测试。

## 在测试中使用模拟用户

本节将介绍如何使用模拟用户来测试授权配置。这种方式是测试授权配置最直接、也是最常用的方法。使用模拟用户时，测试会完全跳过认证流程（见图
18.3）。

在测试中跳过认证、专注测试授权是非常常见的做法。每次验证系统是否正确应用某条授权规则时，并不需要同时验证认证流程。要记住，认证和授权彼此依赖，但通过安全上下文（security
context）实现了完全解耦。因此，如果你想单独测试授权配置，可以定义一个 mock 的安全上下文，并通过它来控制和覆盖所有需要的授权场景。

之所以这样做，是因为在大多数情况下，一个应用只会实现有限几种（实际上通常只有一种）认证方式，但却会有大量、种类繁多的授权规则，分别作用在不同的用例或接口上。所以，你更倾向于将授权测试独立出来编写，这样在验证某个具体元素的授权是否正常时，就不必一遍又一遍地重复执行认证相关的测试了。

模拟用户只在测试执行期间有效。针对这个用户，你可以按需配置各种特性，用来验证特定场景。例如，你可以为该用户分配特定角色（如
ADMIN、MANAGER 等），或者设置不同的权限，以验证应用在这些条件下的行为是否符合预期。

!!! note

	了解在集成测试中涉及到框架的哪些组件非常重要。只有这样，你才能清楚测试实际覆盖了集成的哪一部分。比如，一个模拟用户（mock user）只能用于覆盖授权（authorization）相关的逻辑。（在第 18.5 节中，你会学习如何处理认证 authentication。）我有时会看到开发者在这方面感到困惑。他们以为在使用模拟用户时，也顺带测试了某个自定义的 AuthenticationProvider 实现，但事实并非如此。务必要清楚地理解自己到底在测试什么。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251207163713020.png){ loading=lazy }
  <figcaption>图 18.3 在执行测试时，我们会跳过 Spring Security 认证流程中阴影标出的组件。测试会直接使用一个模拟的 SecurityContext，其中包含你定义的模拟用户来调用待测功能。</figcaption>
</figure>

为了演示如何编写这样的测试，我们回到本书中用过的最简单示例项目：`ssia-ch2-ex1`。  
这个项目仅使用 Spring Security 的默认配置，对路径 `/hello` 暴露了一个端点。我们期望出现什么结果呢？

- 当在没有用户的情况下调用该端点时，HTTP 响应状态码应为 **401 Unauthorized（未授权）**。
- 当在已有认证用户的情况下调用该端点时，HTTP 响应状态码应为 **200 OK**，并且响应体应为 `Hello!`。

让我们来测试这两种场景！要编写测试，我们需要在 pom.xml 文件中添加几个依赖。下面的代码片段展示了本章示例中会用到的几个类。在开始编写测试之前，你需要确认这些依赖已经配置在
pom.xml 文件中。依赖如下：

``` xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-test</artifactId>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.springframework.security</groupId>
  <artifactId>spring-security-test</artifactId>
  <scope>test</scope>
</dependency>
```

!!! note

	在本章的示例中，我们使用 JUnit 5 来编写测试。不过，如果你现在仍在使用 JUnit 4，也完全不用担心。从 Spring Security 的集成角度来看，本章将介绍的这些注解和其他类在 JUnit 4 与 JUnit 5 中的用法基本相同。

	Cătălin Tudose 等人编写的《JUnit in Action》（Manning，2020）第 4 章专门讨论了如何从 JUnit 4 迁移到 JUnit 5，其中包含一些非常有用的表格，展示了 JUnit 4 和 JUnit 5 各类与注解之间的对照关系。你可以通过这个链接查看相关内容：[http://mng.bz/OPJn](https://livebook.manning.com/book/junit-in-action-third-edition/chapter-4)。

在这个 Spring Boot Maven 项目的 test 目录下，我们新建一个名为 MainTests 的类。我们将这个类写在应用程序的主包下面，主包名为
com.laurentiuspilca.ssia。下面的代码示例中，可以看到用于测试的空类定义。我们使用了 @SpringBootTest 注解，它提供了一种方便的方式来为整个测试套件管理
Spring 容器上下文。

```java title="清单 18.1 用于编写测试的类"

@SpringBootTest
public class MainTests {

}

```

一种方便的方式是使用 Spring 的 MockMvc 来实现对端点行为的测试。在 Spring Boot 应用中，你只需在测试类上添加一个注解，就可以自动配置
MockMvc 工具，用于测试端点调用，如下面的示例所示。

```java title="代码清单 18.2 使用 MockMvc 实现测试场景"

@SpringBootTest
@AutoConfigureMockMvc
public class MainTests {

	@Autowired
	private MockMvc mvc;
}

```

现在我们已经有了一个可以用来测试端点行为的工具，就从第一个场景开始吧。当在没有经过身份验证的用户的情况下调用 /hello
端点时，HTTP 响应状态应该是 401 Unauthorized（未授权）。

你可以参考图 18.4 来理解运行这个测试时各组件之间的关系。测试会调用该端点，但使用的是一个模拟的 SecurityContext。我们可以自己决定往这个
SecurityContext 里放些什么。

在这个测试中，我们需要验证的是：如果我们没有往 SecurityContext 中添加用户，也就是有人在未认证的情况下调用该端点时，应用会拒绝这次调用，并返回状态码为
401 Unauthorized 的 HTTP 响应。而当我们向 SecurityContext 中添加一个用户后，应用就会接受这次调用，并返回状态码为 200 OK 的
HTTP 响应。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251207164242229.png){ loading=lazy }
  <figcaption>图 18.4 在运行测试时，我们跳过了认证步骤。测试使用一个模拟的 SecurityContext 并调用由 HelloController 暴露的 /hello 端点。我们在测试的 SecurityContext 中添加一个模拟用户，以根据授权规则验证行为是否正确。如果我们不定义模拟用户，就期望应用不允许该调用通过；而如果定义了用户，则期望该调用能够成功。</figcaption>
</figure>

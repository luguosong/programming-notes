# 在响应式应用中实现安全性

本章涵盖以下内容：

- 在响应式应用中使用 Spring Security
- 在采用 OAuth 2 身份验证设计的系统中使用响应式应用

响应式是一种编程范式，它在开发应用程序时引入了不同的思维方式。响应式编程是一种强大的 Web
应用开发方式，已经被广泛接受。甚至可以说，几年前它曾成为一种流行趋势——只要是重要的会议，总会有几场关于响应式应用的演讲。不过，和软件开发中的其他技术一样，响应式编程并不能在所有场景下都适用。

在某些情况下，响应式方法非常契合需求，而在另一些情况下，它可能只会增加复杂度。但归根结底，采用响应式方式的初衷是为了解决命令式编程的某些局限，因此才会用它来规避这些问题。其中一个典型场景是执行可以拆分的大型任务。采用命令式方式时，你把任务交给应用，由它负责完成；若任务规模较大，应用可能需要耗费大量时间去处理，而指派任务的客户端必须等到整个任务完成后才能拿到返回结果。而在响应式编程里，你可以将任务拆解，使应用有机会并行处理多个子任务，从而让客户端更快地拿到处理后的数据。

本章探讨在响应式应用中结合 Spring Security 实现的应用级安全。与其他应用一样，安全性对响应式应用同样至关重要。但由于响应式应用的设计方式有所不同，Spring
Security 对本书前面讨论的功能实现方式也进行了相应调整。

我们先在第17.1节简要概述如何在 Spring
框架中实现响应式应用。接着，我们将在本书中学到的安全特性应用到安全应用中。在第17.2节，我们将讨论响应式应用中的用户管理；第17.3节则继续讲解如何应用授权规则。最后，在第17.4节，你将学习如何在基于
OAuth 2 的系统中实现响应式应用。你会了解在响应式应用场景下 Spring Security 方面有哪些变化，当然，也会通过示例掌握如何应用这些内容。

## 什么是响应式应用？

在本节中，我们简要讨论响应式应用。本章聚焦于为响应式应用实现安全性，因此在深入 Spring Security
配置之前，我希望你先掌握响应式应用的基本概念。由于响应式应用是一个庞大的主题，我这里只是回顾其主要要点，帮助你快速回忆。如果你还不了解响应式应用的工作原理，或希望更深入地理解它们，建议你阅读
Craig Walls 在 2022 年出版的《Spring in Action》第六版第三部分（Manning 出版）。

在实现应用时，我们通常采用两种方式来完成功能。以下内容详细说明了这些方法：

- 采用命令式方法时，应用会一次性处理绝大部分数据。例如，客户端应用调用服务器暴露的某个端点，并将需要处理的全部数据发送到后端。
  假设你实现了一个用户上传文件的功能。如果用户选择了多个文件，后端一次性接收到所有这些文件并进行处理，那么这就属于命令式方法。
- 采用响应式方法时，应用会分批接收并处理数据。并不需要所有数据在一开始就全部就绪才能处理，后端会在接收到数据的同时进行处理。
  比方说用户选择了若干文件，后端需要上传并处理这些文件。后端不会等到所有文件都接收完成才开始处理，
  而是可能一个接一个地接收文件并在等待后续文件的过程中逐个处理。

图 17.1
展示了两种编程方法的类比。想象一个装瓶牛奶的工厂。如果这个工厂早上一次性拿到所有牛奶，并在完成装瓶后才开始配送，我们就称之为非响应式（命令式）。如果工厂全天陆续拿到牛奶，并在装够订单所需的牛奶后立即配送，我们则称之为响应式。显然，对于这个牛奶工厂而言，采用响应式方式比非响应式更具优势。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251205164035230.png){ loading=lazy }
  <figcaption>图17.1 非响应式与响应式。非响应式方式中，奶厂在早上接收所有要包装的牛奶，晚上一次性送完所有箱子；而响应式方式则是在牛奶送到厂里后立即包装并配送。对于该场景，响应式方式更优，因为它可以在全天持续收集牛奶，并更快地送达客户。</figcaption>
</figure>

为了实现响应式应用，Reactive Streams 规范（[http://www.reactive-streams.org/](https://www.reactive-streams.org/)
）提供了一套用于异步流处理的标准方式。该规范的其中一个实现是 Project Reactor，它构建了 Spring 响应式编程模型的基础。Project
Reactor 提供了一个用于组合 Reactive Streams 的函数式 API。

为了更直观地体验一下，我们先从实现一个简单的响应式应用入手。在第 17.2 节讨论响应式应用中的用户管理时，我们会继续基于这个应用展开。我新建了一个名为
ssia-ch17-ex1 的项目，我们会开发一个暴露示例端点的响应式 Web 应用。需要在 pom.xml 文件中添加响应式 Web 依赖，如下代码片段所示。该依赖包含了
Project Reactor，并让我们能够在项目中使用其相关的类和接口：

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

接下来，我们定义一个简单的 HelloController 来包含示例接口的相关定义。清单 17.1 展示了 HelloController
类的具体实现。在接口定义中，你会注意到我使用了 Mono 作为返回类型。Mono 是 Reactor 实现中最核心的概念之一。在使用 Reactor
时，通常会使用 Mono 和 Flux，它们都定义了发布者（数据源）。在 Reactive Streams 规范中，发布者由 Publisher 接口描述。该接口定义了
Reactive Streams 中使用的关键契约之一。另一个契约是 Subscriber，它描述了负责消费数据的组件。

在设计一个返回数据的端点时，该端点会变成一个发布者，因此必须返回一个 Publisher 实现。如果使用 Project Reactor，这通常是 Mono
或 Flux。Mono 是用于单个值的发布者，而 Flux 则用于多个值。图 17.2 展示了这些组件及其之间的关系。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251205164427314.png){ loading=lazy }
  <figcaption>图 17.2 在响应式流中，发布者生产值，订阅者消费这些值。《响应式流规范》定义了发布者和订阅者所遵循的契约。Project Reactor 实现了该规范，并实现了 Publisher 和 Subscriber 的契约。图中阴影部分代表本章示例中使用的组件。</figcaption>
</figure>

为了让这个解释更加准确，我们再回到牛奶工厂的比喻。牛奶工厂是一种响应式后端实现，它会暴露一个用于接收待处理牛奶的端点。这个端点会输出一些东西（瓶装牛奶），因此需要返回一个
Publisher。如果请求的不止一瓶牛奶，那么牛奶工厂就需要返回一个 Flux，Flux 是 Project Reactor 中用于处理零个或多个输出值的
Publisher 实现。

```java title="清单 17.1 HelloController 类的定义"

@RestController
public class HelloController {

	@GetMapping("/hello")
	public Mono<String> hello() {
		return Mono.just("Hello!");
	}
}

```

你现在可以启动并测试应用。首先在应用的终端可以看到，Spring Boot 不再配置 Tomcat 服务器。Spring Boot 过去会默认为 Web 应用配置
Tomcat，在本书之前的示例中你可能也看过这一点。而如今，Spring Boot 会自动配置 Netty 作为 Spring Boot 项目的默认响应式 Web
服务器。

您调用该端点时可能观察到的第二点是，其行为与使用非响应式方式开发的端点并无差异。您仍然可以在 HTTP 响应体中找到端点在其定义的
Mono 流中返回的 “Hello!” 消息。下面的代码片段展示了调用该端点时应用程序的行为：

```shell
curl http://localhost:8080/hello
```

响应主体是

```shell
Hello!
```

但为什么在 Spring Security 中响应式方式会有所不同呢？在背后，响应式实现通过多个线程来处理流上的任务。换句话说，它改变了我们在命令式架构下
Web 应用所采用的“每个请求一个线程”理念（见图 17.3）。因此，还带来了更多差异：

- SecurityContext 的实现方式在响应式应用中也不一样。记住，SecurityContext 是基于 ThreadLocal 的，而现在每个请求可能会涉及多个线程。
- 正因为 SecurityContext 的变化，任何授权配置都会受到影响。回想第5章的内容，授权规则通常依赖于保存在 SecurityContext 中的
  Authentication 实例。现在，无论是在端点层应用的安全配置，还是全局方法级别的安全功能都会受到影响。
- UserDetailsService 作为负责获取用户详情的组件，本质上是一个数据源。因此，它也需要支持响应式方式。（我们在第2章已经学习过这个契约。）

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251205165120465.png){ loading=lazy }
  <figcaption>图 17.3 中，每条箭头代表不同线程的时间线，方块则表示来自请求 A、B 和 C 的处理任务。由于在响应式应用中，一个请求的任务可能由多个线程处理，因此无法再将认证细节存储在线程级别。</figcaption>
</figure>

图 17.4 展示了这种方法的另一种理解方式。想象一下，一个团队正在处理一组任务。每个人都可以接手某个任务，当遇到阻塞时就离开。继续处理被搁置任务的并不总是同一个线程。因此，安全上下文不能再绑定到某个线程，而必须以某种方式关联到任务本身。

幸运的是，Spring Security 为响应式应用提供了支持，涵盖了所有无法再使用非响应式实现的场景。本章将继续介绍在响应式应用中如何使用
Spring Security 实现安全配置。我们将在 17.2 节开始讲解用户管理的实现，接着在 17.3
节探讨应用端点授权规则，届时会了解响应式应用中安全上下文的工作方式。随后，我们将转向响应式方法安全的讨论，它取代了命令式应用中的全局方法安全。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251205165442878.png){ loading=lazy }
  <figcaption>图 17.4 展示了一个响应式应用的工作机制类比。线程不会按顺序接受某个请求的任务然后在阻塞时等待。相反，所有请求的任务都会进入一个待办队列。任何空闲线程都可以处理任意请求的任务。通过这种方式，互不依赖的任务可以并行处理，线程也不会闲置。</figcaption>
</figure>

## 响应式应用中的用户管理

在许多应用中，用户的认证方式通常基于一对用户名与密码凭证。这种方式很基础，我们也已经讲过，从第二章实现的最简单应用开始。但在响应式应用中，负责用户管理的组件实现也会发生变化。本节我们将讨论在响应式应用中实现用户管理。

我们在 17.1 节开始实现的 ssia-ch17-ex1 应用中继续开发，为应用上下文添加一个 ReactiveUserDetailsService。我们希望 /hello
端点仅在用户完成认证后才能访问。正如其名称所示，ReactiveUserDetailsService 这个契约定义了响应式应用的用户详情服务。

合约的定义和 `UserDetailsService` 一样简单。`ReactiveUserDetailsService` 定义了一个方法，供 Spring Security
根据用户名获取用户。不同之处在于，`ReactiveUserDetailsService` 所描述的方法直接返回一个 `Mono<UserDetails>`，而不是像
`UserDetailsService` 那样返回 `UserDetails`。下面的代码片段展示了 `ReactiveUserDetailsService` 接口的定义：

``` java
public interface ReactiveUserDetailsService {
 Mono<UserDetails> findByUsername(String username);
}
```

就像在 UserDetailsService 的示例中一样，你也可以自定义 ReactiveUserDetailsService 的实现，为 Spring Security
提供获取用户信息的途径。为了简化演示，我们使用 Spring Security 提供的一个实现。MapReactiveUserDetailsService
实现将用户信息存储在内存中（与第二章介绍的 InMemoryUserDetailsManager 相同）。我们需要修改 ssia-ch17-ex1 项目的 pom.xml
文件，添加 Spring Security 依赖，代码片段如下：

``` xml
<dependency>
 <groupId>org.springframework.boot</groupId>
 <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
 <groupId>org.springframework.boot</groupId>
 <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

然后我们创建一个配置类，并向 Spring Security 上下文中添加一个 ReactiveUserDetailsService 和一个
PasswordEncoder。我将这个配置类命名为 ProjectConfig。你可以在清单 17.2 中找到该类的定义。通过
ReactiveUserDetailsService，我们定义了一个用户名为 john、密码为 12345、权限为我命名的 read 的用户。如你所见，这与使用
UserDetailsService 非常相似。ReactiveUserDetailsService 实现的主要区别在于，其方法返回的是包含 UserDetails 的响应式
Publisher 对象，而不是 UserDetails 实例本身。剩下的集成工作就交给 Spring Security 处理。

``` java title="列表 17.2 ProjectConfig 类"
@Configuration
public class ProjectConfig {

  @Bean
  public ReactiveUserDetailsService userDetailsService() {
    var  u = User.withUsername("john")
              .password("12345")
              .authorities("read")
              .build();

    var uds = new MapReactiveUserDetailsService(u);

    return uds;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return NoOpPasswordEncoder.getInstance();
  }
}

```

现在，在启动并测试应用程序时，你可能会注意到，只有在使用正确凭据进行身份验证后，才能调用该端点。就我们而言，只能使用用户 john
及其密码 12345，因为这是我们唯一添加的用户记录。下面的代码片段展示了使用有效凭据调用该端点时应用程序的行为：

```shell
curl -u john:12345 http://localhost:8080/hello
```

响应体是

```shell
Hello!
```

图 17.5 展示了我们在该应用中使用的架构。在幕后，AuthenticationWebFilter 会拦截 HTTP 请求。该过滤器将认证责任委托给认证管理器。认证管理器实现了
ReactiveAuthenticationManager 接口。与非响应式应用不同，我们没有认证提供者。ReactiveAuthenticationManager 直接实现认证逻辑。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251205171749445.png){ loading=lazy }
  <figcaption>图 17.5 所示，AuthenticationWebFilter 拦截请求，并将认证责任委托给 ReactiveAuthenticationManager。如果认证逻辑涉及用户与密码，ReactiveAuthenticationManager 会使用 ReactiveUserDetailsService 查找用户详情，并通过 PasswordEncoder 验证密码。</figcaption>
</figure>

如果你想实现自定义的认证逻辑，可以实现 ReactiveAuthenticationManager 接口。响应式应用的架构与我们在本书中讨论的非响应式应用并无太大差别。如图
17.4 所示，如果认证涉及用户凭证，则可以使用 ReactiveUserDetailsService 获取用户详情，并通过 PasswordEncoder 验证密码。

此外，当你需要认证实例时，框架依然会自动注入。你只需在控制器类的方法参数中添加 `Mono<Authentication>` 即可获取认证信息。清单
17.3 展示了控制器类所做的修改。同样，关键的变化是你开始使用响应式发布器。注意，在响应式应用中我们需要使用
`Mono<Authentication>`，而不是像在非响应式应用中那样直接使用 `Authentication`。

``` java title="清单 17.3 HelloController 类"
@RestController
public class HelloController {

  @GetMapping("/hello")
  public Mono<String> hello(
    Mono<Authentication> auth) {

    Mono<String> message =
      auth.map(a -> "Hello " + a.getName());

    return message;
  }
}

```

重新运行应用程序并调用该端点，您会观察到如下面代码片段所示的行为：

```shell
curl -u john:12345 http://localhost:8080/hello
```

响应体是

```shell
Hello john
```

现在你可能会问，Authentication 对象是从哪儿来的？既然这是个响应式应用，我们就不能再使用 ThreadLocal 了，因为框架会负责管理
SecurityContext。但 Spring Security 为响应式应用提供了另一种上下文持有器实现：ReactiveSecurityContextHolder。我们用它在响应式应用中处理
SecurityContext。也就是说我们依然有 SecurityContext，只是它的管理方式不同了。图 17.6 展示了在 ReactiveAuthenticationManager
成功认证请求后，认证流程的末端。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251205172039899.png){ loading=lazy }
  <figcaption>图 17.6 一旦ReactiveAuthenticationManager成功完成认证，它就会将Authentication对象返回给过滤器。过滤器将该Authentication实例存入SecurityContext中。</figcaption>
</figure>

清单 17.4 展示了如果想直接从安全上下文中获取认证详情，如何重写控制器类。这种做法是让框架通过方法参数注入认证信息的替代方案。你可以在项目
ssia-ch17-ex2 中看到该改动的具体实现。

``` java title="清单 17.4 使用 ReactiveSecurityContextHolder"
@RestController
public class HelloController {

    @GetMapping("/hello")
    public Mono<String> hello() {
      Mono<String> message =
        ReactiveSecurityContextHolder.getContext()

          .map(ctx -> ctx.getAuthentication())

          .map(auth -> "Hello " + auth.getName());

      return message;
    }
}

```

如果你重新运行应用并再次测试该端点，就会发现它的表现与本节前面几个示例一致。命令如下：

```shell
curl -u john:12345 http://localhost:8080/hello
```

响应体是

```shell
Hello john
```

既然你已经了解 Spring Security 在响应式环境中提供了用于正确管理 SecurityContext
的实现，那么你就清楚应用程序就是这样应用授权规则的。而你刚刚掌握的这些细节也为配置授权规则铺平了道路，我们将在 17.3
节中讨论这一部分。

## 在响应式应用中配置授权规则

在本节中，我们讨论如何配置授权规则。正如你在前几章中所了解到的，授权是在认证之后进行的。我们在 17.1 和 17.2 节中已经介绍了
Spring Security 是如何在响应式应用中管理用户以及 SecurityContext 的。但一旦应用完成认证并将已认证请求的详细信息存入
SecurityContext，就到了执行授权的时候。

与其他任何应用一样，在开发响应式应用时你也需要配置授权规则。为了教你如何在响应式应用中设置授权规则，我们将在第 17.3.1
节先讨论在端点层进行配置的方式。在讨论完端点层的授权配置后，你将在第 17.3.2 节学习如何通过方法安全在应用的其他层中应用这些规则。

### 在响应式应用的端点层实施授权

在本节中，我们讨论在响应式应用的端点层配置授权。将授权规则设置在端点层是配置 Web
应用授权最常见的方式。你在前面的示例中已经体验过这一点。端点层的授权配置至关重要——几乎每个应用都会用到。所以你也需要掌握如何在响应式应用中应用它。

您从前几章中学习过通过将类型为 SecurityFilterChain 的 bean
添加到应用上下文来设置授权规则。这种方法在响应式应用中行不通。为了教您如何为响应式应用的端点层正确配置授权规则，我们从一个新项目开始，我将其命名为
ssia-ch17-ex3。

在响应式应用中，Spring Security 通过名为 SecurityWebFilterChain 的契约来应用我们在前几章通过 SecurityFilterChain 类型的
bean 所配置的内容。对于响应式应用，我们需要在 Spring 上下文中添加一个 SecurityWebFilterChain 类型的
bean。为教大家如何操作，我们来实现一个基础应用，包含两个我们将分别进行保护的端点。在新创建的 ssia-ch17-ex3 项目的 pom.xml
文件中，添加响应式 Web 应用以及 Spring Security 所需的依赖：

``` xml
<dependency>
 <groupId>org.springframework.boot</groupId>
 <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
 <groupId>org.springframework.boot</groupId>
 <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

创建一个控制器类，用于定义我们为其配置授权规则的两个端点。这两个端点分别通过 /hello 和 /ciao 路径访问。要调用 /hello
端点，用户需要先通过认证；但 /ciao 端点可以在未认证的情况下访问。下面的示例展示了该控制器的定义。

``` java title="列表 17.5 定义要保护的端点的 HelloController 类"
@RestController
public class HelloController {

 @GetMapping("/hello")
 public Mono<String> hello(Mono<Authentication> auth) {
   Mono<String> message = auth.map(a -> "Hello " + a.getName());
   return message;
 }

 @GetMapping("/ciao")
 public Mono<String> ciao() {
   return Mono.just("Ciao!");
 }
}
```

在配置类中，我们会像在第 17.2 节中学习到的那样，声明一个 ReactiveUserDetailsService 和一个
PasswordEncoder，用以定义用户。下面的示例定义了这些声明。

``` java title="清单 17.6 声明用户管理组件的配置类"
@Configuration
public class ProjectConfig {

 @Bean
 public ReactiveUserDetailsService userDetailsService() {
   var  u = User.withUsername("john")
           .password("12345")
           .authorities("read")
           .build();

   var uds = new MapReactiveUserDetailsService(u);

   return uds;
 }

 @Bean
 public PasswordEncoder passwordEncoder() {
   return NoOpPasswordEncoder.getInstance();
 }

 // ...
}
```

在清单17.7中，我们仍在清单17.6中声明的同一个配置类里操作，不过省略了ReactiveUserDetailsService和PasswordEncoder的声明，以便你可以专注于我们要讨论的授权配置。在清单17.7里，你可能会注意到我们将一个SecurityWebFilterChain类型的bean添加到了Spring上下文中。该方法的参数是由Spring注入的ServerHttpSecurity对象。ServerHttpSecurity使我们能够构建SecurityWebFilterChain的实例，它提供的配置方法与配置非响应式应用时使用的那些类似。

```java title="清单 17.7 为响应式应用配置端点授权"

@Configuration
public class ProjectConfig {

	// Omitted code

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(
			ServerHttpSecurity http) {

		http.httpBasic(Customizer.withDefaults());

		http.authorizeExchange(
				c -> c.pathMatchers(HttpMethod.GET, "/hello")
						.authenticated()
						.anyExchange()
						.permitAll()
		);


		return http.build();
	}
}

```

我们通过 authorizeExchange() 方法开始授权配置。调用方式与在为非响应式应用配置端点授权时调用 authorizeHttpRequests()
方法类似。接下来使用 pathMatchers() 方法。你可以将该方法视为在为非响应式应用配置端点授权时使用 requestMatchers() 的等价替代。

对于非响应式应用，一旦我们通过 matcher 方法将要应用授权规则的请求分组，接下来就可以指定具体的授权规则了。在我们的示例中，调用了
authenticated() 方法，表示只允许已认证的请求。配置非响应式应用的端点授权时也用到了 authenticated()
方法。为了便于理解，响应式应用中对应的方法也是同样的名称。和 authenticated() 类似，你还可以调用以下方法：

- permitAll() —— 配置应用允许无需认证的请求
- denyAll() —— 拒绝所有请求
- hasRole() 与 hasAnyRole() —— 根据角色应用规则
- hasAuthority() 与 hasAnyAuthority() —— 根据权限应用规则

看起来好像少了点什么，是吧？我们在非响应式应用中用于配置授权规则的 access()
方法，在这里也有。不过它有些不同，所以我们会通过一个单独的示例来说明。另一个命名上的相似之处是 anyExchange()
方法，它在响应式应用中相当于非响应式应用里的 anyRequest()。

!!! note

	为什么叫 anyExchange()，开发人员为什么不把这个方法命名为 anyRequest()？为什么是 authorizeExchange() 而不是 authorizeHttpRequests()？这种差异源于用于响应式应用的术语。我们通常把两个组件之间的响应式通信称为“交换数据”。这样可以强化这样一种印象：数据是以连续流中的分段形式发送的，而不是一次请求中传输的大块数据。

我们同样需要像配置其他相关设置一样明确认证方式。我们仍然使用同一个 `ServerHttpSecurity` 实例，调用与非响应式应用中学到的一致的方法：
`httpBasic()`、`formLogin()`、`csrf()`、`cors()`，以及添加过滤器、定制过滤链等。最后，调用 `build()` 方法构建出
`SecurityWebFilterChain` 实例，并将其返回以注入到 Spring 上下文中。

我在本节前面就提到过，在响应式应用的端点授权配置中也可以像在非响应式应用中一样使用 access() 方法。但正如我在第 7 章和第 8
章讲到非响应式应用配置时所说的，只有在无法通过其他方式实现配置时才使用 access()
方法。该方法虽然提供了极大的灵活性，却也让应用的配置变得不那么直观可读。凡事应优先选择更简洁的方案，而不是复杂的做法。不过，在某些场景下你确实需要这份灵活性。例如，当你必须实现更复杂的授权规则，而
hasAuthority()、hasRole() 及其相关方法无法满足时，就需要用到 access()。因此，本段我也会介绍如何使用 access()
方法。为此示例，我新建了一个名为 ssia-ch17-ex4 的项目。在接下来的代码示例中，你可以看到我是如何构建 SecurityWebFilterChain
对象的，使其仅在用户拥有 admin 角色且当前时间为中午之前时，允许访问 /hello 路径。而对于其他所有端点，我则完全阻止访问。

``` java title="清单 17.8 在实现配置规则时使用 access() 方法"

@Configuration
public class ProjectConfig {

	// Omitted code

	@Bean
	public SecurityWebFilterChain
	securityWebFilterChain(ServerHttpSecurity http) {

		http.httpBasic(Customizer.withDefaults());

		http.authorizeExchange(
				c -> c.anyExchange()
						.access(this::getAuthorizationDecisionMono)
		);


		return http.build();
	}

	private Mono<AuthorizationDecision>
	getAuthorizationDecisionMono(
			Mono<Authentication> a,
			AuthorizationContext c) {

		String path = getRequestPath(c);

		boolean restrictedTime =
				LocalTime.now().isAfter(LocalTime.NOON);

		if (path.equals("/hello")) {
			return a.map(isAdmin())
					.map(auth -> auth && !restrictedTime)
					.map(AuthorizationDecision::new);
		}

		return Mono.just(new AuthorizationDecision(false));
	}

	// Omitted code
}

```

可能看起来挺复杂，但其实并不难。当你使用 access() 方法时，会传入一个函数，该函数可以获取到请求的所有相关信息，即
Authentication 对象和 AuthorizationContext。通过 Authentication
对象，你可以获取到已认证用户的详细信息：用户名、角色或权限，以及依据你具体实现的认证逻辑所定义的其它自定义信息。AuthorizationContext
则提供了请求的相关信息：路径、请求头、查询参数、Cookie 等等。

你传递给 access() 方法的参数函数应当返回一个 AuthorizationDecision 类型的对象。如你所料，AuthorizationDecision
是告诉应用请求是否被允许的答案。当你用 new AuthorizationDecision(true) 创建一个实例时，表示你允许该请求；如果用 new
AuthorizationDecision(false) 创建实例，则表示你拒绝该请求。

在示例 17.9 中，我为方便起见补上了示例 17.8 中省略的两个方法：getRequestPath() 和 isAdmin()。通过省略它们，我希望你专注于
access() 方法所使用的逻辑。你可以看到，这两个方法很简单。isAdmin() 方法返回一个函数，该函数会对具有 ROLE_ADMIN 属性的
Authentication 实例返回 true，而 getRequestPath() 方法仅返回请求的路径。

```java title="列表 17.9 getRequestPath() 和 isAdmin() 方法的定义"

@Configuration
public class ProjectConfig {

	// Omitted code

	private String getRequestPath(AuthorizationContext c) {
		return c.getExchange()
				.getRequest()
				.getPath()
				.toString();
	}

	private Function<Authentication, Boolean> isAdmin() {
		return p ->
				p.getAuthorities().stream()
						.anyMatch(e -> e.getAuthority().equals("ROLE_ADMIN"));
	}
}
```

运行应用程序并调用端点时，如果我们应用的任何授权规则未被满足，则会得到 403 Forbidden 响应状态；否则，在 HTTP 响应体中直接显示一条消息：

```shell
curl -u john:12345 http://localhost:8080/hello
```

响应正文为

```shell
Hello john
```

本节示例背后发生了什么？在身份验证完成后，又有一个过滤器拦截了请求。AuthorizationWebFilter 将授权责任委托给一个
ReactiveAuthorizationManager（见图 17.7）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251205172950380.png){ loading=lazy }
  <figcaption>图 17.7 在认证流程成功完成后，另一个名为 AuthorizationWebFilter 的过滤器会拦截该请求。这个过滤器将授权责任委托给一个 ReactiveAuthorizationManager。</figcaption>
</figure>

等一下！这是不是意味着我们只用了一个
ReactiveAuthorizationManager？这个组件如何根据我们所做的配置来授权请求？针对第一个问题：并不是，ReactiveAuthorizationManager
实际上有多个实现。AuthorizationWebFilter 会使用我们添加到 Spring 上下文中的 SecurityWebFilterChain bean。借助这个
bean，过滤器就能决定将授权责任委托给哪一个 ReactiveAuthorizationManager 实现（见图17.8）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251205173128840.png){ loading=lazy }
  <figcaption>图 17.8 中的 AuthorizationFilter 通过我们添加到上下文中的 SecurityWebFilterChain Bean（阴影部分），来确定使用哪个 ReactiveAuthorizationManager。</figcaption>
</figure>

### 在响应式应用中使用方法安全

在本节中，我们讨论如何在响应式应用的各个层面应用授权规则。对于非响应式应用，我们使用方法级安全性；在第11章和第12章中，你已经学过了在方法级应用授权规则的不同方式。能够在端点层以外的层面应用授权规则，可以为你提供更大的灵活性，并让你能够对非
Web 应用施加授权。为了教你如何在响应式应用中使用方法安全性，我们将用一个单独的示例——我将其命名为 ssia-ch17-ex5。

在非响应式应用中，我们不使用全局方法安全性，而是采用“响应式方法安全性”的方式，在方法级别直接应用授权规则。以本例为例，我们使用
@PreAuthorize 来验证用户是否具备调用测试端点所需的特定角色。为了简化示例演示，我们直接将 @PreAuthorize
注解写在定义端点的方法上。不过，你也可以像第11章和第12章中讨论的那样，在响应式应用中的其他组件方法上使用该注解。清单17.10展示了控制器类的定义。请注意，我们使用了
@PreAuthorize，就像你在第11章中学习的一样。通过 SpEL 表达式，我们声明只有管理员才能调用被注解的方法。

```java title="清单 17.10 控制器类的定义"

@RestController
public class HelloController {

	@GetMapping("/hello")
	@PreAuthorize("hasRole('ADMIN')")
	public Mono<String> hello() {
		return Mono.just("Hello");
	}
}

```

在这里你可以看到配置类，其中我们使用注解 @EnableReactiveMethodSecurity
来启用响应式方法安全功能。与方法安全类似，我们需要显式使用注解来开启它。除了这个注解，配置类中还包含了常规的用户管理定义。

``` java title="清单 17.11 配置类"
@Configuration
@EnableReactiveMethodSecurity
public class ProjectConfig {

  @Bean
  public ReactiveUserDetailsService userDetailsService() {
    var  u1 = User.withUsername("john")
            .password("12345")
            .roles("ADMIN")
            .build();

    var  u2 = User.withUsername("bill")
            .password("12345")
            .roles("REGULAR_USER")
            .build();

    var uds = new MapReactiveUserDetailsService(u1, u2);

    return uds;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return NoOpPasswordEncoder.getInstance();
  }
}

```

现在你可以启动应用程序，并分别以每个用户的身份调用该端点来测试其行为。你会发现，只有 John 可以调用该端点，因为我们已将他定义为管理员。Bill
只是普通用户，所以如果我们以 Bill 的身份尝试调用该端点，会收到一个 HTTP 403 Forbidden 状态的响应。以用户 John 的身份调用
/hello 端点的情况如下：

```shell
curl -u john:12345 http://localhost:8080/hello
```

响应体为

```shell
Hello!
```

调用 /hello 端点并用用户 Bill 进行身份验证的样子如下：

```shell
curl -u bill:12345 http://localhost:8080/hello
```

响应体是

```shell
Access Denied
```

在后台，这项功能的实现与非响应式应用完全一致。在第11章和第12章中，你已经学过，切面会拦截对方法的调用并执行授权。如果调用不满足预设的预授权规则，切面就不会将调用委托给该方法（见图17.9）。


<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251205174245314.png){ loading=lazy }
  <figcaption>图 17.9 在使用方法安全性时，一个切面会拦截对受保护方法的调用。如果该调用未满足预授权规则，切面就不会将调用委托给该方法。</figcaption>
</figure>

## 创建响应式OAuth2资源服务器

你现在可能就在想，是否能够在基于 OAuth 2 框架构建的系统中使用响应式应用程序。本节我们将讨论如何将资源服务器实现为响应式应用，并学习如何配置响应式应用以依赖于基于
OAuth 2 的认证方式。由于如今 OAuth 2 的使用非常普遍，你可能会遇到要求将资源服务器设计为响应式服务器的场景。我新建了一个名为
ssia-ch17-ex6 的项目，我们将实现一个响应式资源服务器应用。接下来需要像下段代码片段所示那样，在 pom.xml 中添加相应依赖。

``` xml
<dependency>
 <groupId>org.springframework.boot</groupId>
 <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
<dependency>
 <groupId>org.springframework.boot</groupId>
 <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
 <groupId>org.springframework.cloud</groupId>
 <artifactId>spring-cloud-starter-oauth2</artifactId>
</dependency>
<dependency>
 <groupId>org.springframework.boot</groupId>
 <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

我们需要一个用于测试应用的接口，因此添加一个控制器类。下面的代码片段展示了该控制器类：

```java

@RestController
public class HelloController {

	@GetMapping("/hello")
	public Mono<String> hello() {
		return Mono.just("Hello!");
	}
}
```

下面是这个示例中最关键的一部分：安全配置。在本示例中，我们将资源服务器配置为使用授权服务器公开的公钥来验证令牌签名。

为了配置身份验证方式，我们使用第 17.3 节中介绍的 SecurityWebFilterChain。不过我们不是调用 httpBasic() 方法，而是调用
oauth2ResourceServer() 方法。接着通过调用 jwt() 方法来定义所使用的令牌类型，并通过使用 Customizer
对象来指定令牌签名的验证方式。下一个代码清单展示了该配置类的定义。

``` java title="清单 17.12 定义安全 Web 过滤器链配置"

@Configuration
public class ProjectConfig {

	@Value("${jwk.endpoint}")
	private String jwkEndpoint;

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(
			ServerHttpSecurity http) {

		http.oauth2ResourceServer(
				c -> c.jwt(
						j -> j.jwkSetUri(jwkEndpoint);
        )
    );

		http.authorizeExchange(
				c -> c.anyExchange().authenticated()

		);

		return http.build();
	}
}

```

同样地，我们也可以直接配置公钥，而不是指定一个公开公钥的 URI。唯一需要修改的地方，是调用 `jwtSpec` 实例的 `publicKey()`
方法，并传入一个合法的公钥作为参数。你可以使用我们在第 13 章中讨论过的任意一种方式，当时我们已经详细分析了资源服务器验证访问令牌的多种方案。

接下来，我们需要修改 `application.properties` 文件：新增用于指定密钥集合（key set）对外暴露的 URI 配置项，并将服务器端口改为
9090。这样一来，就可以让授权服务器继续运行在 8080 端口上。下面的代码片段展示了 `application.properties` 文件的完整内容：

``` properties
server.port=9090
jwk.endpoint=http://localhost:8080/auth/realms/master/protocol/openid-connect/certs
```

让我们运行并验证一下应用是否具有我们期望的行为。首先通过授权服务器生成一个访问令牌：

``` shell
curl -XPOST 'http://localhost:8080/auth/
realms/master/protocol/openid-connect/token' \
-H 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'grant_type=password' \
--data-urlencode 'username=bill' \
--data-urlencode 'password=12345' \
--data-urlencode 'client_id=fitnessapp' \
--data-urlencode 'scope=fitnessapp'
```

在 HTTP 响应体中，我们会收到如下面所示的访问令牌：

``` json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI…",
  "expires_in": 6000,
  "refresh_expires_in": 1800,
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5c… ",
  "token_type": "bearer",
  "not-before-policy": 0,
  "session_state": "610f49d7-78d2-4532-8b13-285f64642caa",
  "scope": "fitnessapp"
}
```

使用该访问令牌，我们像下面这样调用应用程序的 /hello 端点：

``` shell
curl -H 'Authorization: BearereyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJMSE9zT0VRSmJuTmJVb
jhQbVpYQTlUVW9QNTZoWU90YzNWT2swa1V2ajVVIn…' \
'http://localhost:9090/hello'
```

响应体为：

```shell
Hello!
```

## 概要

- 响应式应用在处理数据以及与其他组件交换消息时，采用的是一种不同的风格。当我们能够将数据拆分为若干更小的片段进行处理和交互时，响应式应用往往是更合适的选择。
- 和其他任何类型的应用一样，响应式应用同样需要通过安全配置来进行保护。Spring Security
  提供了一整套优秀的工具，可以用于为响应式应用以及非响应式应用配置安全机制。
- 在使用 Spring Security 为响应式应用实现用户管理时，我们使用的是 `ReactiveUserDetailsService` 接口。这个组件的作用与在非响应式应用中使用的
  `UserDetailsService` 相同：告诉应用如何获取用户详细信息。
- 要为响应式 Web 应用实现端点授权规则，你需要创建一个 `SecurityWebFilterChain` 类型的实例，并将其加入 Spring 容器。
  `SecurityWebFilterChain` 实例是通过 `ServerHttpSecurity` 构建器来创建的。
- 通常，用于定义授权配置的方法名称，与在非响应式应用中使用的方法名称基本一致。不过，由于响应式术语的差异，命名上会有一些小区别。例如，在响应式应用中，我们使用
  `authorizeExchange()`，而不是非响应式应用中的 `authorizeHttpRequests()`。
- Spring Security 还提供了一种在方法级别定义授权规则的方式，称为响应式方法安全（reactive method
  security），这使得我们可以在响应式应用的任意层灵活地应用授权规则。它与在非响应式应用中使用的全局方法安全（global method
  security）概念类似。

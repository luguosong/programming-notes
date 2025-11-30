# 11.在方法级别实现授权

本章涵盖：

- Spring 应用中的方法安全
- 基于权限、角色和许可的前置方法授权
- 基于权限、角色和许可的后置方法授权

到目前为止，我们已经讨论了多种身份验证的配置方式。第2章中我们从最简单的 HTTP Basic
开始，第6章我又向你展示了如何设置表单登录。不过，在授权方面，我们只讲过在端点级别的配置。如果你的应用并不是Web应用，是否就不能用
Spring Security 做认证和授权了？实际上，Spring Security 很适合在非 HTTP 端点场景下使用。在本章中，你将学习如何在方法级别配置授权。我们会用这种方法为
Web 和非 Web 应用配置授权，并称之为方法安全（图11.1）。


<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511231658595.png){ loading=lazy }
  <figcaption>图 11.1 方法安全性使您能够更细粒度地控制，并在应用程序的任意指定层级应用授权规则。</figcaption>
</figure>

对于非 Web 应用，即使没有端点，方法安全也能让我们实施授权规则。在 Web
应用中，这种方式让我们有灵活性可以对不同应用层（而不仅仅是端点层）施加授权规则。让我们深入本章学习如何通过方法安全在方法级别应用授权。

## 启用方法安全

本节将展示如何在方法级别启用权限控制，以及 Spring Security 提供的多种应用授权规则的选项。这种方式让你在应用授权时获得更大的灵活性，是应对那些仅在接口层无法完成授权配置场景的关键技能。

默认情况下，方法安全性是禁用的，因此若要使用该功能，首先必须启用它。除此之外，方法安全性提供了多种授权应用方式。我们将在本章的后续部分以及第12章中讨论这些方式并通过示例实现。简而言之，启用全局方法安全性后，可以进行两件主要工作：

• 调用授权（Call authorization）——通过某些已实现的权限规则，决定某人是否可以调用某个方法（预授权），或者在方法执行后决定某人是否可以访问方法返回的结果（后授权）。
• 过滤（Filtering）——决定一个方法在执行前可以接受哪些参数（预过滤），以及方法执行后调用方可以接收哪些返回值（后过滤）。我们将在第12章中详细讨论并实现过滤功能。

### 理解调用授权

一种用于方法安全配置授权规则的方法是调用授权。调用授权是指应用那些决定是否允许调用某个方法，或者先允许调用该方法再决定调用者是否可以访问该方法返回值的授权规则。通常我们需要根据提供的参数或方法的执行结果来判断某个逻辑是否可访问。那么，接下来我们来讨论调用授权，并将其应用到一些示例中。

方法级别的安全是如何实现的？授权规则的应用背后机制是什么？当我们在应用中启用方法级安全时，实际上是启用了一个 Spring
切面。这个切面会拦截我们为其配置了授权规则的方法调用，并根据这些规则判断是否将调用继续传递给被拦截的方法（见图 11.2）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511231731700.png){ loading=lazy }
  <figcaption>图 11.2 当我们启用全局方法安全时，切面会拦截对受保护方法的调用。如果未遵守指定的授权规则，切面就不会将调用委托给受保护的方法。</figcaption>
</figure>

在 Spring 框架中，有大量实现都依赖于面向切面编程（AOP）。方法安全只是 Spring 应用中众多依赖切面的组件之一。如果你需要复习一下切面与
AOP，建议阅读我写的另一本书《Spring Start Here》（Manning，2021）中的第六章。简而言之，我们将调用授权分为：

- 预授权——框架在方法调用前检查授权规则。
- 事后授权——框架在方法执行后再检查授权规则。

接下来，我们会分别讲解这两种方式，并通过一些示例来实现它们。

#### 使用预授权确保方法访问安全

假设我们有一个方法 `findDocumentsByUser(String username)`
，用于返回某个特定用户的文档。调用方通过方法参数提供用户名，方法根据这个用户名获取文档。现在我们要确保经过身份验证的用户只能获取自己的文档。有没有办法为这个方法设置规则，只有那些将经过身份验证的用户名作为参数传入的调用才被允许？有的！这正是我们通过预授权来实现的。

当我们在特定情况下应用完全禁止任何人调用某个方法的授权规则时，就称之为预授权（见图
11.3）。这种方法意味着框架在执行方法之前会验证授权条件。如果调用方根据我们定义的授权规则没有权限，框架就不会将调用委托给该方法，而是抛出一个名为
AccessDeniedException 的异常。这种方式是全局方法安全中迄今为止使用最广泛的方法。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511231809688.png){ loading=lazy }
  <figcaption>图11.3 使用预授权时，会在进一步委派方法调用之前先验证授权规则。如果不满足授权规则，框架将不会继续委派调用，而是向方法调用者抛出异常。</figcaption>
</figure>

通常情况下，如果某些条件不满足，我们就不希望某个功能被执行。你可以基于已认证用户应用条件，也可以引用方法通过参数接收到的值。

#### 使用后授权来保障方法调用的安全

当我们应用的授权规则允许某人调用某个方法，但不一定允许其获取方法返回结果时，就是在使用后授权（见图11.4）。在后授权中，Spring
Security 会在方法执行之后检查授权规则。这种授权方式可用于在特定条件下限制对方法返回值的访问。由于后授权发生在方法执行之后，因此可以将授权规则应用到返回的结果上。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511231812942.png){ loading=lazy }
  <figcaption>图 11.4 通过后置授权，切面将调用委托给受保护的方法。在受保护的方法执行完成后，切面会检查授权规则。如果规则未被满足，切面不会将结果返回给调用方，而是抛出异常。</figcaption>
</figure>

通常我们使用后置授权（postauthorization）在方法执行后根据返回结果应用授权规则。但是要小心使用后置授权！如果方法在执行过程中修改了某些内容，无论授权是否通过，该修改都会发生。

!!! note

	即便加了 @Transactional 注解，如果后置授权（postauthorization）失败，变更也不会回滚。后置授权功能抛出的异常发生在事务管理器提交事务之后。

### 在项目中启用方法安全

本节我们将针对一个项目，应用方法安全所提供的预授权和后授权功能。Spring Security
项目默认并未启用方法安全。要使用它，需先手动开启。不过，启用过程非常简单，只需在配置类上添加 @EnableMethodSecurity 注解即可。

我为这个示例创建了一个新项目，ssia-ch11-ex1。在这个项目中，我编写了一个名为 ProjectConfig 的配置类，如清单 11.1
所示。在该配置类上，我们添加了 @EnableMethodSecurity 注解。方法级安全为我们提供了本章中讨论的三种定义授权规则的方法：

- 预/后置授权注解（默认启用）
- JSR 250 注解 @RolesAllowed
- @Secured 注解

由于在几乎所有情况下，都只使用预/后授权注解，因此我们在本章中重点讨论这种方式。一旦添加了 `@EnableMethodSecurity`
注解，该方式即已预先激活。本章末尾将简要介绍另外两种可选方案。

```java title="清单 11.1 启用方法安全"

@Configuration
@EnableMethodSecurity
public class ProjectConfig {
}
```

您可以在任何认证方式下使用全局方法级别安全，从 HTTP Basic 认证到 OAuth 2（将在本书第三部分学习）。为保持简洁、便于您专注于新内容，我们在示例中采用
HTTP Basic 认证来提供方法级别安全。因此，本章项目的 pom.xml 文件仅需包含 Web 和 Spring Security 依赖，如下一个代码片段所示：

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

!!! note

	在以前的 Spring Security 版本中，我们使用 @EnableGlobalMethodSecurity 注解，并且默认情况下不会启用前置和后置授权。如果你需要在旧版本（6 之前）的 Spring Security 中使用方法授权，可能会发现《Spring Security in Action》第一版的第16章很有用。

## 应用预授权规则

在本节中，我们实现一个预授权示例。对于这个示例，我们继续使用在第 11.1 节中启动的项目 ssia-ch11-ex1。正如第 11.1 节所述，预授权意味着定义
Spring Security 在调用某个特定方法之前要应用的授权规则。如果这些规则得不到满足，框架就不会调用该方法。

我们在本节实现的应用场景很简单。它对外暴露一个端点 /hello，该端点返回字符串“Hello”，后接一个姓名。为获取该姓名，控制器调用了一个服务方法（图
11.5）。该方法应用了预授权规则，用以验证用户是否具备写权限。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511231942944.png){ loading=lazy }
  <figcaption>图11.5：要调用NameService的getName()方法，经过身份验证的用户必须具有写权限。如果用户没有该权限，框架将拒绝该调用并抛出异常。</figcaption>
</figure>

我添加了一个 UserDetailsService 和一个
PasswordEncoder，以确保能够进行用户认证。为了验证方案，我们需要两个用户：一个拥有写权限，另一个不具备。我们证明第一个用户可以成功调用该端点，而当第二个用户尝试调用该方法时，应用会抛出授权异常。下方代码清单展示了配置类的完整定义，其中包含了
UserDetailsService 和 PasswordEncoder。

```java title="第11.2节 用户详情服务与密码编码器的配置类"

@Configuration
@EnableMethodSecurity
public class ProjectConfig {

	@Bean
	public UserDetailsService userDetailsService() {
		var service = new InMemoryUserDetailsManager();

		var u1 = User.withUsername("natalie")
				.password("12345")
				.authorities("read")
				.build();

		var u2 = User.withUsername("emma")
				.password("12345")
				.authorities("write")
				.build();

		service.createUser(u1);
		service.createUser(u2);

		return service;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return NoOpPasswordEncoder.getInstance();
	}
}

```

要为这个方法定义授权规则，我们使用 @PreAuthorize 注解。@PreAuthorize 接受一个 Spring
表达式语言（SpEL）表达式作为值，用来描述授权规则。在这个示例中，我们应用了一个简单的规则。

你可以使用 hasAuthority() 方法，根据用户的权限定义访问限制。在第七章中我们已经学习过 hasAuthority()
方法，并讨论了如何在端点级别应用授权。下面的清单定义了服务类，该类提供了 name 的值。

```java title="示例 11.3 定义方法预授权规则的服务类"

@Service
public class NameService {

	@PreAuthorize("hasAuthority('write')")
	public String getName() {
		return "Fantastico";
	}
}

```

我们在下列清单中定义控制器类，该类以 NameService 作为依赖。

```java title="清单 11.4 实现端点并调用服务的控制器类"

@RestController
public class HelloController {

	private final NameService nameService;

	// omitted constructor

	@GetMapping("/hello")
	public String hello() {
		return "Hello, " + nameService.getName();
	}
}

```

你现在可以启动应用并测试其行为。我们期望只有用户 Emma 拥有调用该端点的权限，因为她拥有写入授权。下面的代码片段展示了我们两位用户
Emma 和 Natalie 调用该端点的情况。要调用 /hello 端点并使用用户 Emma 进行认证，请使用以下 cURL 命令：

```shell
curl -u emma:12345 http://localhost:8080/hello
```

响应体是

```shell
Hello, Fantastico
```

要调用 /hello 端点并以用户 Natalie 进行身份验证，请使用以下 cURL 命令：

```shell
curl -u natalie:12345 http://localhost:8080/hello
```

响应正文是

```shell
{
 "status":403,
 "error":"Forbidden",
 "message":"Forbidden",
 "path":"/hello"
}
```

同样地，你也可以使用第七章中介绍的任意其他表达式来进行端点认证。简要回顾如下：

- hasAnyAuthority()——指定多个权限。用户必须拥有其中至少一个权限才能调用该方法。
- hasRole()——指定调用该方法所需的角色。
- hasAnyRole()——指定多个角色。用户必须拥有其中至少一个角色才能调用该方法。

让我们扩展这个示例，演示如何利用方法参数的值来定义授权规则（图 11.6）。你可以在名为 ssia-ch11-ex2 的项目中找到此示例。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511232024210.png){ loading=lazy }
  <figcaption>图 11.6 在实现预授权时，我们可以在授权规则中使用方法参数的值。在我们的示例中，只有经过认证的用户才能获取其密钥名称的信息。</figcaption>
</figure>

对于该项目，我定义了与第一个示例中相同的 `ProjectConfig` 类，以便我们可以继续使用两个用户 Emma 和
Natalie。该端点现在通过路径变量接收一个值，并调用服务类来获取给定用户名的“秘密名称”。当然，在这里，“秘密名称”只是我创造的一个概念，用于指代用户的某种特征，这不是每个人都能看到的。我将控制器类定义如下例所示。

``` java title="列表 11.5 定义测试端点的控制器类"

@RestController
public class HelloController {

	private final NameService nameService;

	// omitted constructor

	@GetMapping("/secret/names/{name}")
	public List<String> names(@PathVariable String name) {
		return nameService.getSecretNames(name);
	}
}

```

现在我们来看看如何实现清单 11.6 中的 NameService 类。我们用于授权的表达式是 `#name == authentication.principal.username`
。在这个表达式中，我们使用 `#name` 来引用名为 `name` 的 `getSecretNames()` 方法参数的值，同时可以直接访问 `authentication`
对象，从而引用当前已认证的用户。该表达式表示，只有当已认证用户的用户名与通过方法参数传递的值相同时，才能调用此方法。换句话说，用户只能获取属于自己的密钥名称。

```java title="清单11.6 定义受保护方法的 NameService 类"

@Service
public class NameService {

	private Map<String, List<String>> secretNames =
			Map.of(
					"natalie", List.of("Energico", "Perfecto"),
					"emma", List.of("Fantastico"));

	@PreAuthorize
			("#name == authentication.principal.username")
	public List<String> getSecretNames(String name) {
		return secretNames.get(name);
	}
}

```

我们启动应用并进行测试以验证其按预期运行。下面的代码片段展示了在调用端点时的应用表现，其中将路径变量的值设置为用户的名称：

```shell
curl -u emma:12345 http://localhost:8080/secret/names/emma
```

响应体是

```shell
["Fantastico"]
```

在使用用户 Emma 进行身份验证时，我们尝试获取 Natalie 的秘密名称。调用未成功：

```shell
curl -u emma:12345 http://localhost:8080/secret/names/natalie
```

响应正文是

```shell
{
 "status":403,
 "error":"Forbidden",
 "message":"Forbidden",
 "path":"/secret/names/natalie"
}
```

不过，用户 Natalie 可以获取她自己的秘密名称。下面的代码片段证明了这一点：

```shell
curl -u natalie:12345 http://localhost:8080/secret/names/natalie
```

响应体是

```shell
["Energico","Perfecto"]
```

!!! note

	请记住，方法级安全控制可以应用于应用中的任何层级。在本章所示的示例中，权限规则是应用在服务类的方法级别上的。然而，你也可以在应用的任何部分使用方法级安全来应用授权规则：控制器、仓库(repositories)、管理器(managers)、代理等。

## 应用事后授权规则

假设你希望允许调用某个方法，但在特定情况下又要确保调用方无法获取该方法的返回值。当我们想在方法调用之后再进行授权规则验证时，就会用到
postauthorization。乍看起来这似乎有点奇怪：为什么有人可以执行代码，却不能拿到结果？其实重点不在于方法本身，而是想象一下这个方法是从某个数据源（比如
Web 服务或数据库）中获取数据。你需要根据返回的数据来决定是否授权。所以你允许方法执行，但会对其返回值进行验证，如果不符合条件，就不让调用方访问这个结果。

要在 Spring Security 中应用后置授权规则，我们使用 @PostAuthorize 注解，其用法类似于第 11.2 节中讨论的 @PreAuthorize。该注解以
SpEL 表达式作为值，用于定义授权规则。我们继续通过示例展示如何使用 @PostAuthorize 注解，并为某个方法定义后置授权规则（见图
11.7）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511232111701.png){ loading=lazy }
  <figcaption>图11.7 使用后置授权时，我们并不阻止方法被调用，而是如果定义的授权规则未能满足，就防止返回值被暴露。</figcaption>
</figure>


我们的例子场景（我为之创建了名为 ssia-ch11-ex3 的项目）定义了一个名为 Employee 的对象。这个 Employee 拥有姓名、书籍列表以及权限列表。我们将每个
Employee 对应到应用中的一个用户。为了与本章的其他示例保持一致，我们定义了相同的两个用户：Emma 和
Natalie。我们要确保只有在员工拥有读取权限时，调用者才能获取该员工的详细信息。由于在检索员工记录之前我们无法得知其关联的权限，因此必须在方法执行之后再应用授权规则。正因为如此，我们使用
@PostAuthorize 注解。

配置类与我们之前的示例中使用的相同。为了方便你，我在下面的清单中再次列出。

``` java title="清单 11.7 启用方法安全并定义用户"

@Configuration
@EnableMethodSecurity
public class ProjectConfig {

	@Bean
	public UserDetailsService userDetailsService() {
		var service = new InMemoryUserDetailsManager();

		var u1 = User.withUsername("natalie")
				.password("12345")
				.authorities("read")
				.build();

		var u2 = User.withUsername("emma")
				.password("12345")
				.authorities("write")
				.build();

		service.createUser(u1);
		service.createUser(u2);

		return service;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return NoOpPasswordEncoder.getInstance();
	}
}
```

我们还需要声明一个类来表示包含姓名、书籍列表和角色列表的 Employee 对象。下面的清单定义了 Employee 类。

```java title="清单 11.8 员工类的定义"
public class Employee {

	private String name;
	private List<String> books;
	private List<String> roles;

	// Omitted constructor, getters, and setters
}
```

我们可能会从数据库中获取员工信息。为了简化示例，我使用了一个包含少量记录的 Map 作为数据源。在清单 11.9 中，你会看到
BookService 类的定义。BookService 类还包含我们应用授权规则的方法。请注意，我们在 @PostAuthorize 注解中使用的表达式，是针对方法返回的
returnObject 值。后置授权表达式可以使用方法执行后可用的返回值。

```java title="清单 11.9 定义 authorized 方法的 BookService 类"

@Service
public class BookService {

	private Map<String, Employee> records =
			Map.of("emma",
					new Employee("Emma Thompson",
							List.of("Karamazov Brothers"),
							List.of("accountant", "reader")),
					"natalie",
					new Employee("Natalie Parker",
							List.of("Beautiful Paris"),
							List.of("researcher"))
			);

	@PostAuthorize("returnObject.roles.contains('reader')")
	public Employee getBookDetails(String name) {
		return records.get(name);
	}
}

```

我们还需要编写一个控制器，并实现一个端点来调用我们为其应用授权规则的方法。以下列表展示了该控制器类。

``` java title="清单 11.10 实现该端点的控制器类"

@RestController
public class BookController {

	private final BookService bookService;

	// omitted constructor

	@GetMapping("/book/details/{name}")
	public Employee getDetails(@PathVariable String name) {
		return bookService.getBookDetails(name);
	}
}
```

现在你可以启动应用程序并调用该端点来观察应用行为。在接下来的代码片段中，你会看到调用该端点的示例。由于返回的角色列表中包含字符串“reader”，所以任何用户都可以访问
Emma 的详细信息，但没有用户能够获取 Natalie 的信息。调用端点获取 Emma 的详细信息，并以用户 Emma 身份进行身份验证时，我们执行以下命令：

```shell
curl -u emma:12345 http://localhost:8080/book/details/emma
```

响应主体是

```shell
{
 "name":"Emma Thompson",
 "books":["Karamazov Brothers"],
 "roles":["accountant","reader"]
}
```

调用端点以获取 Emma 的详细信息，并使用用户 Natalie 进行身份验证，我们使用

```shell
curl -u natalie:12345 http://localhost:8080/book/details/emma
```

响应体为

```shell
{
 "name":"Emma Thompson",
 "books":["Karamazov Brothers"],
 "roles":["accountant","reader"]
}
```

调用端点获取 Natalie 的详情，并使用用户 Emma 进行认证，我们使用

```shell
curl -u emma:12345 http://localhost:8080/book/details/natalie
```

响应体是

```shell
{
 "status":403,
 "error":"Forbidden",
 "message":"Forbidden",
 "path":"/book/details/natalie"
}
```

调用端点获取 Natalie 的详细信息，并使用 Natalie 用户进行身份验证，我们使用以下命令：

```shell
curl -u natalie:12345 http://localhost:8080/book/details/natalie
```

响应正文是

```shell
{
 "status":403,
 "error":"Forbidden",
 "message":"Forbidden",
 "path":"/book/details/natalie"
}
```

!!! note

	如果你的需求既需要在方法执行前进行授权检查，又需要在执行后做授权判断，那么同一个方法可以同时使用 @PreAuthorize 和 @PostAuthorize。

## 为方法实现权限控制

到目前为止，你已经了解了如何使用简洁表达式为预授权和后授权定义规则。现在假设授权逻辑更加复杂，无法在一行中描述。编写冗长的
SpEL 表达式绝对不舒服。我从不建议在任何情况下使用长的 SpEL 表达式，无论是授权规则还是其他场景。它们只会让代码难以阅读，影响应用的可维护性。当你需要实现复杂授权规则时，与其写冗长的
SpEL 表达式，不如将逻辑拆分到单独的类中。Spring Security 提供了权限的概念，使得在单独的类中编写授权规则变得简单，从而让你的应用更易读、更易理解。

在本节中，我们通过权限在一个项目中应用授权规则。我将该项目命名为
ssia-ch11-ex4。在这个场景中，你的应用负责管理文档。每份文档都有一个所有者，即创建该文档的用户。要获取现有文档的详细信息，用户必须是管理员，或者必须是该文档的所有者。我们通过实现权限评估器来满足这一需求。下面的清单定义了文档，它只是一个普通的
Java 对象。

```java title="清单 11.11 Document 类"
public class Document {

	private String owner;

	// Omitted constructor, getters, and setters
}
```

为了模拟数据库并简化示例以提高易读性，我创建了一个仓储类，用于在一个 Map 里管理几个文档实例。该类将在下一个清单中展示。

``` java title="列表 11.12 管理若干 Document 实例的 DocumentRepository 类"
@Repository
public class DocumentRepository {

  private Map<String, Document> documents =
    Map.of(«abc123», new Document(«natalie»),
           «qwe123», new Document(«natalie»),
           «asd555», new Document(«emma»));

  public Document findDocument(String code) {
    return documents.get(code);
  }
}

```

一个服务类定义了一个方法，该方法通过仓库根据文档代码获取文档。我们将授权规则应用于该服务类中的这个方法。这个类的逻辑很简单：定义一个方法，通过文档的唯一代码返回
Document。我们在该方法上添加了 @PostAuthorize 注解，并使用 hasPermission() 的 SpEL
表达式。这个方法允许我们引用一个在后续示例中实现的外部授权表达式。同时，请注意我们传递给 hasPermission()
方法的参数：returnObject（代表方法返回的值）以及我们允许访问的角色名称，即 “ROLE_admin”。该类的定义如下所示。

```java title="列表 11.13 实现受保护方法的 DocumentService 类"

@Service
public class DocumentService {

	private final DocumentRepository documentRepository;

	// omitted constructor

	@PostAuthorize
			("hasPermission(returnObject, 'ROLE_admin')")
	public Document getDocument(String code) {
		return documentRepository.findDocument(code);
	}
}

```

我们的职责是实现权限逻辑。为此，我们编写了一个实现 `PermissionEvaluator` 合约的对象。`PermissionEvaluator`
合约提供了两种实现权限逻辑的方式：

- 基于对象与权限——当前示例采用的方式，假设权限评估器接收两个对象：一个是受授权规则约束的对象，另一个提供实现权限逻辑所需的额外信息。
- 基于对象 ID、对象类型与权限——假设权限评估器接收一个对象
  ID，用于检索所需的对象。同时接收一个对象类型，在同一个权限评估器适用于多个对象类型的情况下，可用于获取提供额外信息以评估权限的对象。

在下一个示例中，你会看到包含两个方法的 PermissionEvaluator 合约。

```java title="清单 11.14 PermissionEvaluator 合约定义"
public interface PermissionEvaluator {

	boolean hasPermission(
			Authentication a,
			Object subject,
			Object permission);

	boolean hasPermission(
			Authentication a,
			Serializable id,
			String type,
			Object permission);
}
```

对于当前的示例，使用第一种方式即可。我们已有主体，在本例中即方法返回的值。我们还传递了角色名 '
ROLE_admin'，按照示例设定，该角色可以访问任意文档。当然，在本示例中，我们完全可以在权限评估类中直接使用角色名，而无需将其作为
hasPermission()
方法的参数传递。这里之所以仅演示前者，是为了示范。在真实的业务场景中，往往更加复杂，包含多个方法，且每个方法在授权过程中可能需要不同的信息。因此，我们在方法级别留有一个参数，用于传递在授权逻辑中所需的具体细节。

为了让你了解情况并避免混淆，我还想提一下，你不需要传入 Authentication 对象。Spring Security 在调用 hasPermission()
方法时会自动提供该参数值。框架之所以能获取到认证实例的值，是因为它已经存在于 SecurityContext 中。接下来的列表中展示了
DocumentsPermissionEvaluator 类，在我们的示例中它实现了 PermissionEvaluator 接口，用以定义自定义的授权规则。

```java title="示例11.15 实现授权规则"

@Component
public class DocumentsPermissionEvaluator
		implements PermissionEvaluator {

	@Override
	public boolean hasPermission(
			Authentication authentication,
			Object target,
			Object permission) {

		Document document = (Document) target;
		String p = (String) permission;

		boolean admin =
				authentication.getAuthorities()
						.stream()
						.anyMatch(a -> a.getAuthority().equals(p));

		return admin ||
				document.getOwner()
						.equals(authentication.getName());

	}

	@Override
	public boolean hasPermission(Authentication authentication,
								 Serializable targetId,
								 String targetType,
								 Object permission) {
		return false;
	}
}

```

为了让 Spring Security 能识别我们新的 PermissionEvaluator 实现，需要在配置类中定义一个 MethodSecurityExpressionHandler
Bean。下列代码展示了如何通过定义 MethodSecurityExpressionHandler 来让自定义的 PermissionEvaluator 被识别。

```java title="清单 11.16 在配置类中配置 PermissionEvaluator"

@Configuration
@EnableMethodSecurity
public class ProjectConfig {

	private final DocumentsPermissionEvaluator evaluator;

	// omitted constructor

	@Bean
	protected MethodSecurityExpressionHandler createExpressionHandler() {
		var expressionHandler =
				new DefaultMethodSecurityExpressionHandler();

		expressionHandler.setPermissionEvaluator(
				evaluator);

		return expressionHandler;
	}

	// Omitted definition of the UserDetailsService and PasswordEncoder beans
}

```

!!! note

	我们在这里使用的是 Spring Security 提供的名为 `DefaultMethodSecurityExpressionHandler` 的 `MethodSecurityExpressionHandler` 实现。当然，你也可以自行实现一个自定义的 `MethodSecurityExpressionHandler`，以定义用于应用授权规则的自定义 SpEL 表达式。实际项目中这种情况比较少见，因此在我们的示例中不会实现这种定制对象。只是想让你知道这是可行的。

我把 UserDetailsService 和 PasswordEncoder 的定义分开，这样你就只需要关注新代码即可。在清单 11.17
中可以看到配置类的其余部分。关于这些用户，唯一重要的是他们的角色：Natalie 是管理员，可以访问任何文档；Emma 是经理，只能访问她自己的文档。

``` java title="清单 11.17 配置类的完整定义"
@Configuration
@EnableMethodSecurity
public class ProjectConfig {

 private final DocumentsPermissionEvaluator evaluator;

 // Omitted constructor

 @Override
 protected MethodSecurityExpressionHandler createExpressionHandler() {
   var expressionHandler =
       new DefaultMethodSecurityExpressionHandler();

   expressionHandler.setPermissionEvaluator(evaluator);

   return expressionHandler;
 }

 @Bean
 public UserDetailsService userDetailsService() {
   var service = new InMemoryUserDetailsManager();

   var u1 = User.withUsername("natalie")
            .password("12345")
            .roles("admin")
            .build();

    var u2 = User.withUsername("emma")
             .password("12345")
             .roles("manager")
             .build();

    service.createUser(u1);
    service.createUser(u2);

    return service;
 }

 @Bean
 public PasswordEncoder passwordEncoder() {
   return NoOpPasswordEncoder.getInstance();
 }
}
```

为了测试该应用，我们定义了一个端点。下列清单展示了此定义。

```java title="清单11.18 定义控制器类并实现一个端点"

@RestController
public class DocumentController {

	private final DocumentService documentService;

	// Omitted constructor

	@GetMapping("/documents/{code}")
	public Document getDetails(@PathVariable String code) {
		return documentService.getDocument(code);
	}
}
```

运行应用并调用端点以观察其行为。用户 Natalie 可以访问任意所有者的文档，而用户 Emma 只能访问自己拥有的文档。针对 Natalie
的文档并以用户 “natalie” 进行认证时，我们使用如下命令：

```shell
curl -u natalie:12345 http://localhost:8080/documents/abc123
```

响应体为

```shell
{
 "owner":"natalie"
}
```

调用属于 Emma 的文档的端点，并使用用户 “natalie” 进行身份验证，我们使用

```shell
curl -u natalie:12345 http://localhost:8080/documents/asd555
```

响应主体是

```shell
{
 "owner":"emma"
}
```

调用属于 Emma 的文档的端点，并使用用户 “emma” 进行认证，我们这样写

```shell
curl -u emma:12345 http://localhost:8080/documents/asd555
```

响应正文是

```shell
{
 "owner":"emma"
}
```

对属于 Natalie 的文档调用端点并使用用户“emma”进行身份验证时，我们使用

```shell
curl -u emma:12345 http://localhost:8080/documents/abc123
```

响应体是

```shell
{
 "status":403,
 "error":"Forbidden",
 "message":"Forbidden",
 "path":"/documents/abc123"
}
```

在类似的方式下，您可以使用第二个 PermissionEvaluator 方法来编写授权表达式。第二种方式是使用标识符和主体类型，而不是直接使用对象。例如，假设我们想将当前示例改为在方法执行前应用授权规则，使用
@PreAuthorize。这种情况下，我们尚未获取返回的对象。但我们可以用文档的
code，它是其唯一标识符来替代对象本身。下面的代码清单演示了如何更改权限评估器类以实现该场景。我把示例分别放在名为
ssia-ch11-ex5 的项目中，您可以单独运行这些项目。

```java title="清单 11.19 DocumentsPermissionEvaluator 类的更改"

@Component
public class DocumentsPermissionEvaluator
		implements PermissionEvaluator {

	private final DocumentRepository documentRepository;

	// Omitted constructor

	@Override
	public boolean hasPermission(Authentication authentication,
								 Object target,
								 Object permission) {
		return false;
	}

	@Override
	public boolean hasPermission(Authentication authentication,
								 Serializable targetId,
								 String targetType,
								 Object permission) {

		String code = targetId.toString();
		Document document = documentRepository.findDocument(code);

		String p = (String) permission;

		boolean admin =
				authentication.getAuthorities()
						.stream()
						.anyMatch(a -> a.getAuthority().equals(p));

		return admin ||
				document.getOwner().equals(
						authentication.getName());
	}
}

```

当然，我们还需要在 @PreAuthorize 注解中通过适当的调用来使用权限评估器。在下面的清单中，你会看到我在 DocumentService
类中所做的更改，以便通过新方法应用授权规则。

```java title="列表 11.20 DocumentService 类"

@Service
public class DocumentService {

	private final DocumentRepository documentRepository;

	// Omitted contructor

	@PreAuthorize
			("hasPermission(#code, 'document', 'ROLE_admin')")
	public Document getDocument(String code) {
		return documentRepository.findDocument(code);
	}
}

```

你可以重新运行应用程序并观察该端点的行为。你会看到与我们最初使用权限评估器第一种方式实现授权规则时的结果相同：用户 Natalie
是管理员，可访问任意文档详情，而用户 Emma 只能访问她拥有的文档。调用一个属于 Natalie 的文档的端点，并使用用户 “natalie”
进行认证时，我们会发出

```shell
curl -u natalie:12345 http://localhost:8080/documents/abc123
```

响应正文为

```shell
{
 "owner":"natalie"
}
```

调用属于 Emma 的文档的端点并使用用户 “natalie” 进行身份验证，我们执行

```shell
curl -u natalie:12345 http://localhost:8080/documents/asd555
```

响应体是

```shell
{
 "owner":"emma"
}
```

为 Emma 的文档调用端点，并使用用户 “emma” 进行身份验证，我们发送

```shell
curl -u emma:12345 http://localhost:8080/documents/asd555
```

响应体是

```shell
{
 "owner":"emma"
}
```

调用属于 Natalie 的文档接口，并以用户 “emma” 进行身份验证，我们执行了

```shell
curl -u emma:12345 http://localhost:8080/documents/abc123
```

响应正文为

```shell
{
 "status":403,
 "error":"Forbidden",
 "message":"Forbidden",
 "path":"/documents/abc123"
}
```

### 使用@Secured和@RolesAllowed注解

在本章中，我们讨论了如何通过全局方法安全来应用授权规则。我们首先了解到该功能默认是关闭的，可在配置类上通过
`@EnableMethodSecurity` 注解来开启。此外，当使用前置和后置授权时，无需通过 `@EnableMethodSecurity`
注解的某个属性来指定应用授权规则的方式。我们是这样使用该注解的：

``` java
@EnableMethodSecurity
```

@EnableMethodSecurity 注解提供了两个属性，用于启用不同的注解。你可以通过 jsr250Enabled 属性启用 @RolesAllowed 注解，通过
securedEnabled 属性启用 @Secured 注解。与 @PreAuthorize 和 @PostAuthorize
相比，这两种注解的能力略弱，在实际项目中也不太常见。不过，我还是想让你了解它们，当然不会在细节上花费太多时间。

你可以通过将 @EnableMethodSecurity 的某些属性设置为 true，像我们之前为 preauthorization 和 postauthorization
所做的那样，启用这些注解的使用。你可以启用代表某一类注解的属性，比如 @Secure 或 @RolesAllowed。下一段代码示例中展示了如何操作。

``` java
@EnableMethodSecurity(
  jsr250Enabled = true,
  securedEnabled = true
)
```

启用这些属性后，你可以使用 @RolesAllowed 或 @Secured 注解来指定登录用户调用某个方法时必须具备的角色或权限。下面的代码片段演示了如何使用
@RolesAllowed 注解来指定只有拥有 ADMIN 角色的用户才能调用 getName() 方法：

```java

@Service
public class NameService {

	@RolesAllowed("ADMIN")
	public String getName() {
		return "Fantastico";
	}
}
```

同样，你也可以使用 @Secured 注解来替代 @RolesAllowed 注解：

``` java
@Service
public class NameService {
 @Secured("ROLE_ADMIN")
 public String getName() {
     return "Fantastico";
 }
}
```

您现在可以测试您的示例。以下代码片段演示了如何操作：

```shell
curl -u emma:12345 http://localhost:8080/hello
```

响应体是

```shell
Hello, Fantastico
```

要调用该端点并以用户 Natalie 进行认证，请使用

```shell
curl -u natalie:12345 http://localhost:8080/hello
```

响应体为

```shell
{
 "status":403,
 "error":"Forbidden",
 "message":"Forbidden",
 "path":"/hello"
}
```

你可以在项目 **ssia-ch9-ex6** 中找到一个完整示例，展示如何使用 @RolesAllowed 和 @Secured 注解。

## 总结

- Spring Security 支持在任意应用层应用授权规则，而不仅限于终端级别。为此，我们需要启用方法级安全功能。
- 方法级安全功能默认是关闭的。要启用它，我们可以在应用的配置类上添加 @EnableMethodSecurity 注解。
- 你可以定义应用在调用方法前要检查的授权规则。如果这些规则未被满足，框架就不会执行该方法。我们把在方法执行前验证授权的方式称为
  preauthorization（预授权）。
- 实现预授权时，使用 @PreAuthorize 注解，并在其值中填写定义授权规则的 SpEL 表达式。
- 如果我们希望在方法执行后再判断调用者是否可以使用返回值、以及后续执行流程是否可继续，就需要使用 postauthorization（后授权）。
- 实现后授权时，使用 @PostAuthorize 注解，并在其值中填写表示授权规则的 SpEL 表达式。
- 在处理复杂的授权逻辑时，建议将这些逻辑拆分到其他类中，以提升代码可读性。在 Spring Security 中，一个常见做法是实现
  PermissionEvaluator 接口。
- Spring Security 兼容旧规范，例如 @RolesAllowed 和 @Secured 注解。虽然可以使用它们，但功能上不如 @PreAuthorize 和
  @PostAuthorize 强大，且在实际项目中与 Spring 共同使用的情况也较少见。

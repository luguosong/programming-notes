# 12.在方法级别实现过滤

本章涵盖：

- 使用预过滤器限制方法接收的参数值。
- 使用后过滤器限制方法返回的内容。
- 将过滤机制与 Spring Data 集成。

在第11章中，你已经学习了如何使用全局方法安全来应用授权规则。我们通过示例演示了 @PreAuthorize 和 @PostAuthorize
注解的使用。当你使用这些注解时，应用要么允许方法的调用，要么完全拒绝它。假设你并不想禁止对某个方法的调用，但你希望传入的参数符合某些规则。或者在另一种情况下，你希望在方法调用之后，调用方只能收到经过授权的返回值部分。这种功能称为过滤，分为两类：

- 预过滤—框架在调用方法之前过滤参数的值。
- 后过滤—框架在方法调用之后过滤返回值。

过滤与调用授权不同（图12.1）。在过滤过程中，框架会执行调用，即使某个参数或返回值不符合你定义的授权规则，也不会抛出异常。相反，它会过滤掉不满足特定条件的元素。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251124101406885.png){ loading=lazy }
  <figcaption>图 12.1 客户端调用端点时提供的值不符合授权规则。使用预授权机制时，方法根本不会被调用，调用方会收到异常；而使用预过滤机制时，切面会调用该方法，但只传递符合规则的值。</figcaption>
</figure>

首先要说明的是，只有集合和数组才能应用过滤。只有当方法的参数是对象数组或集合时，才能使用预过滤。框架会根据你定义的规则对这个集合或数组进行过滤。后置过滤同样适用：只有方法返回集合或数组时才能使用，框架会根据你指定的规则对方法返回的值进行过滤。

## 针对方法授权应用预筛选

本节先讲解预过滤机制背后的原理，然后通过一个示例来实现预过滤。你可以使用过滤功能，让框架在有人调用某个方法时，对该方法参数传入的值进行校验。框架会过滤掉不符合条件的值，并只用符合条件的值来调用该方法。这一功能称为预过滤（图
12.2）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251124103117274.png){ loading=lazy }
  <figcaption>图 12.2 通过预过滤，切面拦截对受保护方法的调用。切面对调用方传入的参数值进行过滤，只有符合既定规则的值才会被传递给该方法。</figcaption>
</figure>

在实际案例中，你会发现预过滤非常适用，因为它能将授权规则与方法所实现的业务逻辑解耦。比如你实现了一个用例，只处理由已认证用户拥有的具体数据。这个用例可能会在多个地方被调用，但其职责始终明确：无论谁调用该用例，都只能处理已认证用户的数据。与其依赖调用方正确地执行授权规则，不如让该用例自行施加授权控制。当然，你也可以在方法内部处理。但将授权逻辑与业务逻辑分离，可以提升代码的可维护性，也让其他人更容易阅读和理解。

就像我们在第11章讨论的调用授权一样，Spring Security 也通过切面实现过滤。切面可以拦截特定方法的调用，并为其附加额外指令。对于预过滤而言，切面会拦截使用
@PreFilter 注解的方法，并按照你定义的条件过滤作为参数传入的集合中的值（见图12.3）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251124105458349.png){ loading=lazy }
  <figcaption>图 12.3 通过预过滤，我们将授权职责与业务实现解耦。Spring Security 提供的切面只负责授权规则，而服务方法仅关注其所实现的用例的业务逻辑。</figcaption>
</figure>

与第11章中讨论的 @PreAuthorize 和 @PostAuthorize 注解类似，你可以在 @PreFilter 注解的值中设置授权规则。在这些以 SpEL
表达式形式提供的规则中，通过 filterObject 引用传入方法的集合或数组中的任意元素。

为了看到预过滤的效果，我们来操作一个项目。我将这个项目命名为 ssia-ch12-ex1。假设你有一个买卖商品的应用，其后端实现了 /sell
接口。当用户出售商品时，前端会调用该接口。但已登录的用户只能出售自己拥有的商品。我们来实现一个简单场景：一个服务方法会将接收到的商品参数作为待售商品。通过这个例子，你将学习如何应用
@PreFilter 注解，因为我们正是通过该注解确保方法接收到的商品都是当前登录用户拥有的。

创建项目后，我们编写一个配置类，用以确保有几个用户来测试实现。配置类的简单定义见清单 12.1。我称之为 ProjectConfig 的配置类仅声明了一个
UserDetailsService 和一个 PasswordEncoder，并用 @EnableMethodSecurity 注解标记。对于过滤注解，我们仍然需要使用
@EnableMethodSecurity 注解，并启用 pre-/postauthorization 注解。所提供的 UserDetailsService 定义了测试所需的两位用户：Nikolai
和 Julien。

``` java title="清单 12.1 配置用户并启用方法安全"

@Configuration
@EnableMethodSecurity
public class ProjectConfig {

	@Bean
	public UserDetailsService userDetailsService() {
		var uds = new InMemoryUserDetailsManager();

		var u1 = User.withUsername("nikolai")
				.password("12345")
				.authorities("read")
				.build();

		var u2 = User.withUsername("julien")
				.password("12345")
				.authorities("write")
				.build();

		uds.createUser(u1);
		uds.createUser(u2);

		return uds;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return NoOpPasswordEncoder.getInstance();
	}
}
```

我使用下一节展示的模型类来描述该产品。

```java title="清单 12.2 产品类定义"
public class Product {

	private String name;
	private String owner;

	// Omitted constructor, getters, and setters
}

```

ProductService 类中定义了我们通过 @PreFilter 保护的服务方法。你可以在清单 12.3 中找到 ProductService 类。在该清单中，在
sellProducts() 方法之前就使用了 @PreFilter 注解。与该注解配合使用的 Spring 表达式语言（SpEL）是
`filterObject.owner == authentication.name`，它只允许那些 Product 的 owner 属性等于当前登录用户名的值通过。在表达式的等号左侧，我们使用
filterObject，它用来引用列表中的对象作为参数。由于我们处理的是产品列表，因此此处的 filterObject 类型是 Product，因此可以访问其
owner 属性。等号右侧则使用 authentication 对象。对于 @PreFilter 和 @PostFilter 注解，可以直接引用 authentication
对象，该对象在认证之后会被保存在 SecurityContext 中（见图 12.4）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251124105936423.png){ loading=lazy }
  <figcaption>图12.4 在通过 filterObject 进行预过滤时，我们引用调用方作为参数传入的列表中的对象。认证对象是在SecurityContext中完成认证过程后存储的对象。</figcaption>
</figure>

服务方法会原样返回接收到的列表。这样一来，我们就可以通过检查 HTTP 响应体中返回的列表，来验证框架是否按预期过滤了该列表。

```java title="图12.3 在 ProductService 类中使用 @PreFilter 注解"

@Service
public class ProductService {

	@PreFilter("filterObject.owner == authentication.name")
	public List<Product> sellProducts(List<Product> products) {
		// sell products and return the sold products list
		return products;
	}
}

```

为了方便测试，我定义了一个端点来调用受保护的服务方法。列表 12.4 在一个名为 ProductController
的控制器类中定义了该端点。这里，为了缩短端点调用，我创建了一个列表，并直接将其作为参数传给服务方法。在真实场景中，这个列表应该由客户端在请求体中提供。你还会注意到我使用了
@GetMapping 来处理一个表示变更的操作，这种做法并不标准。但请理解，我这样做是为了避免在示例中处理 CSRF 保护，从而让你能更专注于当前的主题。第
9 章里我们已经介绍过 CSRF 保护。

```java title="列表 12.4 用于测试的端点所对应的控制器类"

@RestController
public class ProductController {

	private final ProductService productService;

	// omitted constructor

	@GetMapping("/sell")
	public List<Product> sellProduct() {
		List<Product> products = new ArrayList<>();

		products.add(new Product("beer", "nikolai"));
		products.add(new Product("candy", "nikolai"));
		products.add(new Product("chocolate", "julien"));

		return productService.sellProducts(products);
	}
}
```

我们启动应用并查看调用 /sell 接口时的情况。请留意我们作为参数传入服务方法的三个商品。我将其中两个商品分配给用户
Nikolai，另一个分配给用户 Julien。当我们调用接口并使用用户 Nikolai 进行认证时，期望在响应中只看到与她相关的两个商品；当我们使用
Julien 认证调用接口时，响应中应该仅包含与 Julien 相关的那一个商品。在下面的代码片段中可以看到测试调用及其结果。要调用 /sell
接口并使用用户 Nikolai 进行认证，请使用以下命令：

```shell
curl -u nikolai:12345 http://localhost:8080/sell
```

响应体为

```shell
[
 {"name":"beer","owner":"nikolai"},
 {"name":"candy","owner":"nikolai"}
]
```

要调用 /sell 端点并使用用户 Julien 进行认证，请使用

```shell
curl -u julien:12345 http://localhost:8080/sell
```

响应正文是

```shell
[
 {"name":"chocolate","owner":"julien"}
]
```

你需要注意，切面会修改传入的集合。在我们的例子中，别指望它会返回一个新的 List
实例。实际上，它只是对原来那个集合实例进行了修改，移除了不符合条件的元素。这一点很重要，必须考虑到。你必须确保传入的集合实例不是不可变的。因为如果提供了不可变集合，切面在执行时就会抛出异常——过滤切面无法修改集合内容（见图
12.5）。


<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251124110704966.png){ loading=lazy }
  <figcaption>图12.5 该切面会拦截并修改作为参数传入的集合。你需要提供一个可变的集合实例，以便切面能够对其进行修改。</figcaption>
</figure>

下面的代码清单展示了我们本节之前处理的同一个项目，不过我用 `List.of()` 方法返回的不可变实例替换了 List 定义，以测试这种情况下会发生什么。

```java title="清单 12.5 使用不可变集合"

@RestController
public class ProductController {

	private final ProductService productService;

	// omitted constructor

	@GetMapping("/sell")
	public List<Product> sellProduct() {
		List<Product> products = List.of(
				new Product("beer", "nikolai"),
				new Product("candy", "nikolai"),
				new Product("chocolate", "julien"));

		return productService.sellProducts(products);
	}
}

```

我把这个示例单独放在项目 ssia-ch12-ex2 文件夹里，这样你也可以自己运行测试。启动应用并调用 /sell 接口时，会收到一个状态为
500 Internal Server Error 的 HTTP 响应，并且控制台日志中会出现异常，具体情况如下面的代码片段所示：

```shell
curl -u julien:12345 http://localhost:8080/sell
```

响应正文是

```shell
{
 "status":500,
 "error":"Internal Server Error",
 "path":"/sell"
}
```

在应用控制台中，你会看到类似下面代码片段所示的异常：

```shell
java.lang.UnsupportedOperationException: null
   at java.base/java.util.ImmutableCollections.uoe(ImmutableCollections.java:73) ~[na:na]
...
```

## 为方法授权应用后置过滤

在本节中，我们实现后过滤。假设有如下场景：一个前端基于 Angular、后端基于 Spring
的应用在管理一些商品。用户拥有自己的商品，他们只能获取自己商品的详情。为了获取商品详情，前端会调用由后端暴露的接口（见图
12.6）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251124111221234.png){ loading=lazy }
  <figcaption>图 12.6 后过滤场景。客户端调用某个端点以获取前端展示所需的数据。后过滤实现确保客户端只能获取当前认证用户拥有的数据。</figcaption>
</figure>

在后端的某个服务类中，开发者编写了一个方法 `List<Product> findProducts()`
用于获取商品详情。客户端应用会在前端展示这些信息。开发者如何确保调用该方法的人只能获取属于自己的商品，而不是他人的？一种将授权规则与应用的业务逻辑解耦的实现方式被称为后过滤（postfiltering）。本节将介绍后过滤的工作原理，并演示如何在应用中实现它。

与预过滤类似，后过滤也依赖于一个切面。该切面允许调用方法，但一旦方法返回，它会拿到返回值并确保其遵循你定义的规则。和预过滤一样，后过滤也会改变方法返回的集合或数组。你需要提供返回集合中元素应满足的条件。后过滤切面会从返回的集合或数组中筛除不符合规则的元素。

要应用后置过滤（postfiltering），需要使用 @PostFilter 注解。@PostFilter 的使用方式与我们在第11章及本章中涉及的其他前置/后置注解类似。你需要将授权规则作为
SpEL 表达式提供给注解的 value，过滤切面就会使用这个规则，如图 12.7 所示。同样，和前置过滤一样，后置过滤只对数组和集合有效。请确保仅在返回类型为数组或集合的方法上使用
@PostFilter 注解。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251124111902808.png){ loading=lazy }
  <figcaption>图12.7 后置过滤。一个切面拦截受保护方法返回的集合，并过滤掉不符合你提供规则的值。与后置授权不同，当返回值不符合授权规则时，后置过滤不会向调用方抛出异常。</figcaption>
</figure>

让我们在一个示例中应用后置过滤，示例项目名为 ssia-ch12-ex3。为保持一致性，我沿用本章前面示例中的相同用户配置，因此配置类保持不变。为方便起见，我在下面的清单中重复展示了该配置。

```java title="清单 12.6 配置类"

@Configuration
@EnableMethodSecurity
public class ProjectConfig {

	@Bean
	public UserDetailsService userDetailsService() {
		var uds = new InMemoryUserDetailsManager();

		var u1 = User.withUsername("nikolai")
				.password("12345")
				.authorities("read")
				.build();

		var u2 = User.withUsername("julien")
				.password("12345")
				.authorities("write")
				.build();

		uds.createUser(u1);
		uds.createUser(u2);

		return uds;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return NoOpPasswordEncoder.getInstance();
	}
}
```

接下来的代码片段显示 Product 类也保持不变：

```java
public class Product {

	private String name;
	private String owner;

	// Omitted constructor, getters, and setters
}
```

在 ProductService 类中，我们现在实现了一个返回产品列表的方法。在现实场景中，我们假定应用会从数据库或其他数据源读取产品。为了保持示例简洁，并让您专注于我们讨论的内容，我们使用了一个简单的集合，如清单
12.7 所示。

我在返回产品列表的 `findProducts()` 方法上添加了 `@PostFilter` 注解。作为该注解的值，我设置了条件
`filterObject.owner == authentication.name`，只允许返回拥有者与当前认证用户相同的产品（见图 12.8）。等号左侧使用
`filterObject` 来引用返回集合中的元素；右侧使用 `authentication` 来引用保存在 `SecurityContext` 中的 `Authentication` 对象。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251124112400218.png){ loading=lazy }
  <figcaption>图 12.8 在用于授权的 SpEL 表达式中，我们使用 filterObject 来引用返回集合中的对象，使用 authentication 来引用来自SecurityContext的 Authentication 实例。</figcaption>
</figure>

```java title="清单 12.7 ProductService 类"

@Service
public class ProductService {

	@PostFilter("filterObject.owner == authentication.name")
	public List<Product> findProducts() {
		List<Product> products = new ArrayList<>();

		products.add(new Product("beer", "nikolai"));
		products.add(new Product("candy", "nikolai"));
		products.add(new Product("chocolate", "julien"));

		return products;
	}
}

```

我们定义一个控制器类，以便通过一个端点访问我们的方法。下一个代码清单展示了该控制器类。

``` java title="代码清单12.8 ProductController类"
@RestController
public class ProductController {

 private final ProductService productService;

 // Omitted constructor

 @GetMapping("/find")
 public List<Product> findProducts() {
   return productService.findProducts();
 }
}
```

是时候运行应用程序，并通过调用 /find 接口来测试其行为了。我们期待在 HTTP 响应正文中仅看到被认证用户拥有的产品。接下来的代码片段展示了分别使用我们的两个用户
Nikolai 和 Julien 调用该接口时的结果。要调用 /find 接口并以用户 Julien 进行认证，请使用以下 cURL 命令：

```shell
curl -u julien:12345 http://localhost:8080/find
```

响应体是

```shell
[
 {"name":"chocolate","owner":"julien"}
]
```

要调用 /find 端点并以用户 Nikolai 进行身份验证，请使用以下 cURL 命令：

```shell
curl -u nikolai:12345 http://localhost:8080/find
```

响应体是

```shell
[
  {"name":"beer","owner":"nikolai"},
  {"name":"candy","owner":"nikolai"}
]
```

## 在Spring Data仓库中使用过滤

在本节中，我们将讨论如何在 Spring Data 仓库层应用过滤。理解这种方式非常重要，因为我们经常使用数据库来持久化应用的数据。在
Spring Boot 应用中，通常会使用 Spring Data 作为连接 SQL 或 NoSQL 数据库的高级封装层。我们将介绍在使用 Spring Data
时，在仓库层应用过滤的两种方式，并通过示例加以实现。

我们首先采用的是本章前面已经学习过的方法：使用 @PreFilter 和 @PostFilter 注解。第二种方法则是在查询中直接集成授权规则。正如本节所要说明的，选择在
Spring Data 仓库中应用过滤的方式时需要格外谨慎。如前所述，我们有两种选项：

- 使用 @PreFilter 和 @PostFilter 注解
- 在查询中直接应用过滤逻辑

在仓库层使用 @PreFilter 注解和在应用的其他层使用该注解是一样的。但在涉及后置过滤时，情况就不一样了。虽然在仓库方法上使用
@PostFilter 技术上可以工作正常，但从性能角度来看，这通常不是一个明智的选择。

假设你有一个用于管理公司文档的应用。开发者需要实现一个功能：用户登录后，所有文档都在一个网页上列出。于是开发者决定使用
Spring Data 的 findAll() 方法，并加上 @PostFilter 注解，让 Spring Security
过滤文档，使方法只返回当前登录用户拥有的文档。这个做法显然是错误的，因为它让应用先从数据库中取出所有记录，再在应用层去过滤。如果文档数量很大，一次性调用
findAll() 而不分页，很容易直接导致内存溢出（OutOfMemoryError）。即便文档数量没有大到会撑爆堆空间，从应用里过滤记录的效率也不如一开始就从数据库中只取需要的数据（见图12.9）。


<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251124112955790.png){ loading=lazy }
  <figcaption>图 12.9 不良设计的剖析。当需要在仓库层进行过滤时，最好先确保只检索所需数据。否则，应用可能会面临严重的内存和性能问题。</figcaption>
</figure>

在服务层，你别无选择，只能在应用中对记录进行过滤。不过，如果你从仓库层就知道只需提取当前登录用户所拥有的记录，那就应该实现一个只从数据库中提取所需文档的查询。

!!! note

	无论何种场景下从数据源（数据库、Web 服务、输入流或其他任何来源）检索数据，都应确保应用只获取所需的数据，尽量避免在应用内部再次对数据进行过滤。

让我们先在 Spring Data 仓库方法上使用 @PostFilter 注解，再切换到第二种方式——直接在查询中编写条件。这样我们就能有机会试验两种方式并进行比较。

我创建了一个名为 ssia-ch12-ex4 的新项目，使用与本章前面示例相同的配置类。和之前一样，我们开发一个管理产品的应用，不过这次我们从数据库中的表里获取产品详情。在这个示例中，我们实现了一个产品搜索功能（见图
12.10）。我们编写了一个接收字符串并返回包含该字符串名称的产品列表的端点。不过，必须确保只返回与已认证用户关联的产品。


<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251124131021016.png){ loading=lazy }
  <figcaption>图 12.10 在我们的场景中，首先我们通过使用 @PostFilter 在应用层根据所有者过滤产品。随后我们改变实现方式，直接在查询中添加条件。通过这种方式，确保应用从数据源中获取的仅是所需的记录。</figcaption>
</figure>

我们使用 Spring Data JPA 来连接数据库。因此，还需要在 pom.xml 文件中添加 spring-boot-starter-data-jpa
依赖以及与所使用的数据库服务器技术匹配的连接驱动。以下代码片段展示了我在 pom.xml 中使用的依赖项：

``` xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
  <groupId>mysql</groupId>
  <artifactId>mysql-connector-java</artifactId>
  <scope>runtime</scope>
</dependency> 
```

在 application.properties 文件中，我们添加了 Spring Boot 创建数据源所需的属性。在下面的代码片段中，你可以看到我在
application.properties 文件中添加的属性：

```properties
spring.datasource.url=jdbc:mysql://localhost/spring?useLegacyDatetimeCode=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=
spring.datasource.initialization-mode=always
```

我们还需要在数据库中创建一张用于存储应用程序获取的产品详情的表。我们在 schema.sql 文件中编写建表脚本，在 data.sql
文件中编写插入测试数据的语句。你需要将这两个文件（schema.sql 和 data.sql）放置在 Spring Boot 项目的 resources
目录下，这样它们才能在应用启动时被发现并执行。下面的代码片段展示了用于创建表的建表语句，我们需要将其写入 schema.sql 文件：

```sql
CREATE TABLE IF NOT EXISTS `spring`.`product`
(
    `id`    INT         NOT NULL AUTO_INCREMENT,
    `name`  VARCHAR(45) NULL,
    `owner` VARCHAR(45) NULL,
    PRIMARY KEY (`id`)
);
```

在 data.sql 文件中，我写了三条 INSERT 语句，下一段代码片段展示了它们。这些语句创建了我们后续用来验证应用行为的测试数据：

``` sql
INSERT IGNORE INTO `spring`.`product` (`id`, `name`, `owner`)
VALUES ('1', 'beer', 'nikolai');
INSERT IGNORE INTO `spring`.`product` (`id`, `name`, `owner`)
VALUES ('2', 'candy', 'nikolai');
INSERT IGNORE INTO `spring`.`product` (`id`, `name`, `owner`)
VALUES ('3', 'chocolate', 'julien');
```

!!! note

	请记住，本书其他示例中也使用了相同名称的表。如果你之前的示例已经创建了同名表，建议在开始本项目之前先删除它们。另一种做法是使用不同的 schema。

为了映射我们应用中的产品表，我们需要编写一个实体类。以下代码段定义了 Product 实体。

```java title="清单 12.9 产品实体类"

@Entity
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String name;
	private String owner;

	// Omitted getters and setters
}
```

对于 Product 实体，我们还编写了下一个清单中定义的 Spring Data 仓库接口。请注意，这次我们直接在仓库接口声明的方法上使用了
@PostFilter 注解。

```java title="清单 12.10 ProductRepository 接口"
public interface ProductRepository
		extends JpaRepository<Product, Integer> {

	@PostFilter("filterObject.owner == authentication.name")
	List<Product> findProductByNameContains(String text);
}

```

下面的清单展示了如何定义一个控制器类，该类实现了我们用于测试行为的端点。

```java title="列表 12.11 ProductController 类"

@RestController
public class ProductController {

	private final ProductRepository productRepository;

	// Omitted constructor

	@GetMapping("/products/{text}")
	public List<Product> findProductsContaining(
			@PathVariable String text) {

		return productRepository.findProductByNameContains(text);
	}
}
```

启动应用后，我们可以测试调用 `/products/{text}` 接口时的情况。在以用户 Nikolai 进行认证并搜索字母 c 时，HTTP 响应中只包含产品
candy。尽管 chocolate 也包含字母 c，但因为它归 Julien 所有，所以不会出现在响应中。下面的代码片段中可以找到这些调用及其响应。要调用
`/products` 接口并使用用户 Nikolai 进行认证，请执行以下命令：

```shell
curl -u nikolai:12345 http://localhost:8080/products/c
```

响应体是

```shell
[
 {"id":2,"name":"candy","owner":"nikolai"}
]
```

要调用 /products 端点并使用用户 Julien 进行身份验证，请发出

```shell
curl -u julien:12345 http://localhost:8080/products/c
```

响应体是

```shell
[
 {"id":3,"name":"chocolate","owner":"julien''}
]
```

我们在本节前面已经讨论过，在仓库里使用 @PostFilter
并不是最佳方案。我们应该确保不要从数据库中查询那些无需的数据。那么，如何调整示例以便仅选择所需内容，而不是在查询后再进行过滤呢？我们可以直接在仓库类使用的查询中提供
SpEL 表达式。为此，只需两个简单步骤：

1. 向 Spring 容器中添加一个类型为 SecurityEvaluationContextExtension 的对象。可以在配置类中通过一个简单的 @Bean 方法完成。
2. 在仓库类的查询中添加适当的筛选子句，确保只选择所需字段。

在我们的项目中，为了在上下文中添加 SecurityEvaluationContextExtension
bean，需要将配置类按下一个代码清单所示进行修改。为了让书中的所有示例代码集中管理，我这里使用了另一个名为 ssia-ch12-ex5 的项目。

```java title="清单12.12 将 SecurityEvaluationContextExtension 添加到上下文"

@Configuration
@EnableMethodSecurity
public class ProjectConfig {

	@Bean
	public SecurityEvaluationContextExtension
	securityEvaluationContextExtension() {

		return new SecurityEvaluationContextExtension();
	}

	// Omitted declaration of the UserDetailsService and PasswordEncoder
}

```

在 ProductRepository 接口中，我们在方法前添加了查询，并通过 SpEL 表达式在 WHERE 子句中使用了恰当的条件。以下清单展示了这一更改。

``` java title="清单 12.13 在仓库接口的查询中使用 SpEL"
public interface ProductRepository
        extends JpaRepository<Product, Integer> {

    Query("""SELECT p FROM Product p WHERE 
               p.name LIKE %:text% AND
               p.owner=?#{authentication.name}
           """
    List<Product> findProductByNameContains(String text);
}

```

我们现在可以启动应用程序，并通过调用 /products/{text} 端点进行测试。我们期望其行为与使用 @PostFilter
时保持一致。但现在数据库中只会检索到属于正确所有者的记录，这使得功能更快且更可靠。下面的代码片段展示了对端点的调用。要调用
/products 端点并使用用户 Nikolai 进行身份验证，我们使用

```shell
curl -u nikolai:12345 http://localhost:8080/products/c
```

响应体是

```shell
[
 {"id":2,"name":"candy","owner":"nikolai"}
]
```

要调用 /products 端点并使用用户 Julien 进行身份验证，我们使用

```shell
curl -u julien:12345 http://localhost:8080/products/c
```

响应体是

```shell
[
  {"id":3,"name":"chocolate","owner":"julien"}
]
```

## 总结

- 过滤是一种授权方式，框架会验证方法的输入参数或返回值，并排除那些不满足你定义条件的元素。作为一种授权手段，过滤侧重于方法的输入和输出值，而不是方法本身的执行。
- 通过过滤，你可以确保方法只接收它被授权处理的值，并且不会返回调用方不应该获取的内容。
- 使用过滤时，不是限制对方法的访问，而是限制通过方法参数传入的内容或方法的返回值，从而控制方法的输入输出。
- 要限制通过方法参数传入的值，可使用 @PreFilter 注解。@PreFilter 接收一个条件，用于指定允许哪些值作为方法参数。框架会从作为参数传入的集合中，筛除所有不符合该规则的值。
- 使用 @PreFilter 时，方法的参数必须是集合或数组。在注解的 SpEL 表达式中，通过 filterObject 引用集合内的对象，从而定义规则。
- 要限制方法返回的值，可使用 @PostFilter 注解。使用 @PostFilter 时，方法的返回类型必须是集合或数组。框架会根据 @PostFilter
  注解中定义的规则，对返回的集合进行过滤。
- @PreFilter 和 @PostFilter 也可以用于 Spring Data 仓库方法。但在 Spring Data 仓库中使用 @PostFilter
  通常不是理想的选择，因为这样容易引发性能问题，此类过滤应尽可能直接在数据库层完成。
- Spring Security 能与 Spring Data 无缝集成，你可以借助这一特性，避免在 Spring Data 仓库方法上使用 @PostFilter。










































































































































































































































































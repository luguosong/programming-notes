# IOC容器

## Spring IoC容器与Bean简介

本章介绍了Spring框架对`控制反转（Inversion of Control，简称IoC）`原则的实现。`依赖注入（Dependency Injection，简称DI）`是IoC的一种特殊形式，通过它，对象仅通过构造函数参数、工厂方法的参数，或者在对象实例构造完成后或从工厂方法返回后设置的属性来定义其依赖关系（即它所需要协作的其他对象）。`IoC容器在创建Bean时会注入这些依赖关系`。这个过程本质上与传统方式相反（因此称为控制反转），即Bean本身不再通过直接实例化类或使用类似服务定位器（Service Locator）模式的机制来控制其依赖的实例化或定位。

`org.springframework.beans` 和 `org.springframework.context` 包是 Spring 框架 IoC 容器的基础。`BeanFactory 接口`提供了一种高级配置机制，能够管理任何类型的对象。`ApplicationContext` 是 `BeanFactory` 的一个子接口，它增加了以下功能：

- 更方便地与 Spring 的 AOP 特性集成  
- 消息资源处理（用于国际化）  
- 事件发布  
- 应用层特定的上下文，例如用于 Web 应用的 WebApplicationContext。

简而言之，`BeanFactory` 提供了配置框架和基本功能，而 `ApplicationContext` 增加了更多面向企业的功能。`ApplicationContext` 是 `BeanFactory` 的`完整超集`，在本章中专门用于描述 Spring 的 IoC 容器。

在 Spring 中，由 `Spring IoC 容器`管理的、构成应用程序核心的对象被称为 `Bean`。`Bean` 是由 Spring IoC 容器`实例化`、`组装`并`管理`的对象。除此之外，`Bean` 只是应用程序中众多对象之一。`Bean` 及其之间的依赖关系体现在容器使用的配置元数据中。

## 容器概览

`org.springframework.context.ApplicationContext` 接口代表了 `Spring IoC 容器`，负责`实例化`、`配置`和`组装` Bean。容器通过读取`配置元数据(configuration metadata)`获取有关需要实例化、配置和组装的组件的指令。`配置元数据`可以用`注解的组件类(annotated component classes)`、`带有工厂方法的配置类(configuration classes with factory methods)`，或者`外部的 XML 文件( external XML files)`或 `Groovy 脚本(Groovy scripts)`来表示。无论使用哪种格式，都可以构建应用程序以及这些组件之间复杂的依赖关系。

在核心 Spring 中，有多个 ApplicationContext 接口的实现。在独立应用程序中，通常会创建 `AnnotationConfigApplicationContext` 或 `ClassPathXmlApplicationContext` 的实例。

在大多数应用场景中，通常`不需要显式编写用户代码来实例化一个或多个 Spring IoC 容器的实例`。例如，在普通的 Web 应用场景中，只需在应用的 web.xml 文件中添加一个简单的模板化 Web 描述符 XML 即可。而在 Spring Boot 场景中，应用上下文会根据常见的配置约定为你隐式地引导启动。

以下图表展示了 Spring 工作原理的高层次概览。您的`应用程序类`与`配置元数据`相结合，在 `ApplicationContext` 创建并初始化后，您将拥有一个`完全配置且可执行`的系统或应用程序。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202502201536475.png){ loading=lazy }
  <figcaption>图 1. Spring IoC 容器</figcaption>
</figure>

### 配置元数据(Configuration Metadata)

正如上图所示，Spring IoC 容器使用了一种配置元数据。这些配置元数据用于表示作为应用程序开发者的你，如何指示 Spring 容器实例化、配置以及组装应用程序中的组件。

Spring IoC 容器本身与配置元数据的具体书写格式完全解耦。如今，许多开发者为他们的 Spring 应用选择基于 Java 的配置方式：

- `基于注解的配置（Annotation-based configuration）`：通过在应用的组件类上使用注解来定义 Bean 的配置元数据。
- `基于 Java 的配置（Java-based configuration）`：通过 Java 配置类，在应用类之外定义 Bean。要使用这些功能，可以参考 `@Configuration`、`@Bean`、`@Import` 和 `@DependsOn` 注解。

Spring 配置至少包含一个，通常包含多个需要容器管理的 Bean 定义。Java 配置通常在 `@Configuration` 类中使用带有 `@Bean` 注解的方法，每个方法对应一个 Bean 定义。

这些 Bean 定义对应于构成应用程序的实际对象。通常，你会定义`服务层对象`、`持久层对象`（如仓库或数据访问对象 DAO）、`表示层对象`（如 Web 控制器）、`基础设施对象`（如 JPA 的 EntityManagerFactory、JMS 队列等）。一般来说，`不会在容器中配置细粒度的领域对象`，因为创建和加载领域对象通常是仓库和业务逻辑的职责。

!!! note "领域对象"

	领域对象（Domain Object）是指在面向对象设计中，代表领域模型中的一个实体、概念或事物，它通常与特定业务领域中的核心数据和行为相关联。领域对象通常用于表示业务逻辑中需要操作和处理的实体，并且与业务规则紧密结合。

	例如，在一个电子商务系统中，Order（订单）、Product（产品）、Customer（客户）都可以是领域对象。每个对象不仅仅包含数据，还包含与这些数据相关的操作，如计算价格、检查库存等。

	总结来说，领域对象的关键特点是它们和业务逻辑高度相关，是对业务领域的抽象和建模。

#### XML作为外部配置DSL

!!! DSL

	在 Spring 容器文档中提到的 DSL 是 `Domain-Specific Language（领域特定语言）`的缩写。它指的是一种专门针对某一特定领域的问题所设计的编程语言或语法结构。

	具体来说，DSL 是一种为了解决特定领域问题而简化和优化的语言，不同于通用编程语言（如 Java 或 Python）。它通常通过简洁的语法，帮助开发者更高效地表达特定的领域知识或业务逻辑。

基于 XML 的配置元数据通过顶层的 <beans/> 元素内的 <bean/> 元素来配置这些 Bean。以下示例展示了基于 XML 的配置元数据的基本结构：

``` xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.springframework.org/schema/beans
		https://www.springframework.org/schema/beans/spring-beans.xsd">

	<bean id="..." class="...">
		<!-- 此 Bean 的协作者和配置内容在此填写。 -->
	</bean>

	<bean id="..." class="...">
		<!-- 此 Bean 的协作者和配置内容在此填写。 -->
	</bean>

	<!-- 更多 Bean 定义在此处 -->

</beans>
```

1. `id 属性`是一个字符串，用于标识单个 bean 定义。
2. `class属性`定义了 bean 的类型，并使用`全限定类名`。

`id 属性`的值可用于引用协作对象。本示例中未展示用于引用协作对象的 XML。

要实例化容器，需要将 XML 资源文件的路径或路径集合提供给 `ClassPathXmlApplicationContext` 构造函数，以便容器能够从多种外部资源（例如本地文件系统、Java CLASSPATH 等）加载配置元数据。

``` java
ApplicationContext context = new ClassPathXmlApplicationContext("services.xml", "daos.xml");
```

!!! note

	在了解了 Spring 的 IoC 容器之后，您可能会想进一步了解 Spring 的资源抽象（如“资源”部分所述），它提供了一种便捷的机制，用于从以 URI 语法定义的位置读取 InputStream。尤其是，资源路径被用于构建应用上下文，具体内容请参见“应用上下文和资源路径”部分。

以下示例展示了`服务层对象（services.xml）`的配置文件：

``` xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.springframework.org/schema/beans
		https://www.springframework.org/schema/beans/spring-beans.xsd">

	<!-- services -->

	<bean id="petStore" class="org.springframework.samples.jpetstore.services.PetStoreServiceImpl">
		<property name="accountDao" ref="accountDao"/>
		<property name="itemDao" ref="itemDao"/>
		<!-- additional collaborators and configuration for this bean go here -->
	</bean>

	<!-- more bean definitions for services go here -->

</beans>
```

以下示例展示了`数据访问对象（daos.xml）文件`：

``` xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.springframework.org/schema/beans
		https://www.springframework.org/schema/beans/spring-beans.xsd">

	<bean id="accountDao"
		class="org.springframework.samples.jpetstore.dao.jpa.JpaAccountDao">
		<!-- additional collaborators and configuration for this bean go here -->
	</bean>

	<bean id="itemDao" class="org.springframework.samples.jpetstore.dao.jpa.JpaItemDao">
		<!-- additional collaborators and configuration for this bean go here -->
	</bean>

	<!-- more bean definitions for data access objects go here -->

</beans>
```

在前面的示例中，服务层由 `PetStoreServiceImpl` 类以及两种数据访问对象 `JpaAccountDao` 和 `JpaItemDao` 组成（基于 JPA 对象关系映射标准）。`property` 元素的 `name` 属性指的是 JavaBean `属性的名称`，而 `ref` 属性则指向另一个 bean `定义的名称`。`id` 和 `ref` 元素之间的这种关联表达了协作对象之间的依赖关系。

!!! note "JPA"

	JPA（Java Persistence API）是一种 Java 标准，用于简化 Java 应用程序与数据库之间的交互。它提供了一种通过对象关系映射（ORM）将 Java 对象与数据库表进行关联的方式。JPA 定义了一套标准的注解和接口，允许开发者将 Java 类映射到数据库表，同时也能管理实体对象的生命周期（如持久化、更新、删除等）。

#### 编写基于XML的配置元数据

将 Bean 定义分布在多个 XML 文件中可能会很有用。通常，每个独立的 XML 配置文件代表了架构中的一个逻辑层或模块。

您可以使用 `ClassPathXmlApplicationContext` 构造方法从 XML 片段中加载 Bean 定义。该构造方法可以接收多个资源位置，正如上一节所示。或者，您也可以使用一个或多个 `<import/> 元素`，从其他文件中加载 Bean 定义。以下示例展示了如何实现：

```xml
<beans>
	<import resource="services.xml"/>
	<import resource="resources/messageSource.xml"/>
	<import resource="/resources/themeSource.xml"/>

	<bean id="bean1" class="..."/>
	<bean id="bean2" class="..."/>
</beans>
```

在前面的示例中，外部 Bean 定义从三个文件中加载：`services.xml`、`messageSource.xml` 和 `themeSource.xml`。所有路径均相对于执行导入的定义文件，因此 services.xml 必须与执行导入的文件位于同一目录或类路径位置，而 messageSource.xml 和 themeSource.xml 必须位于导入文件位置下的资源路径中。如您所见，路径开头的斜杠会被忽略。然而，由于这些路径是相对路径，最好完全不要使用斜杠。被导入文件的内容，包括顶层的 <beans/> 元素，必须是符合 Spring Schema 的有效 XML Bean 定义。

!!! note

	可以使用相对路径`../`引用父目录中的文件，但并不推荐这样做。这样会导致对当前应用程序之外的文件产生依赖。尤其是在使用 `classpath: URL`（例如，`classpath:../services.xml`）时，这种引用方式并不建议，因为运行时的解析过程会选择`最近`的 `classpath` 根目录，然后再查找其父目录。classpath 配置的变更可能会导致选择到其他错误的目录。

	您始终可以使用完全限定的资源路径来代替相对路径，例如：`file:C:/config/services.xml` 或 `classpath:/config/services.xml`。然而，需要注意的是，这样会将应用程序的配置与特定的绝对路径绑定在一起。通常，更推荐为这些绝对路径保留一种间接方式，例如通过 `${...}` 占位符，在运行时根据 JVM 系统属性进行解析。

命名空间本身提供了导入指令的功能。除了简单的 Bean 定义之外，Spring 还通过一系列 XML 命名空间提供了更多的配置功能，例如 context 和 util 命名空间。

#### Groovy Bean 定义 DSL

作为外部化配置元数据的另一个示例，Bean 定义也可以使用 Spring 的 Groovy Bean Definition DSL 来表示，这种方式源自 Grails 框架。通常，这类配置会存放在一个“.groovy”文件中，其结构如下例所示：

```groovy
beans {
	dataSource(BasicDataSource) {
		driverClassName = "org.hsqldb.jdbcDriver"
		url = "jdbc:hsqldb:mem:grailsDB"
		username = "sa"
		password = ""
		settings = [mynew:"setting"]
	}
	sessionFactory(SessionFactory) {
		dataSource = dataSource
	}
	myService(MyService) {
		nestedBean = { AnotherBean bean ->
			dataSource = dataSource
		}
	}
}
```

这种配置风格在很大程度上等同于 XML Bean 定义，甚至支持 Spring 的 XML 配置命名空间。同时，它还允许通过 importBeans 指令导入 XML Bean 定义文件。

### 使用容器

ApplicationContext 是一个高级工厂接口，能够维护不同 Bean 及其依赖关系的注册表。通过使用方法 T getBean(String name, Class<T> requiredType)，您可以获取 Bean 的实例。

ApplicationContext 允许您读取 Bean 的定义并访问它们，如以下示例所示：

``` java
// 创建和配置 Bean
ApplicationContext context = new ClassPathXmlApplicationContext("services.xml", "daos.xml");

// 检索已配置的实例
PetStoreService service = context.getBean("petStore", PetStoreService.class);

// 使用已配置的实例
List<String> userList = service.getUsernameList();
```

使用 Groovy 配置时，引导过程看起来非常相似。它使用了一个不同的上下文实现类，该类支持 Groovy（同时也能解析 XML Bean 定义）。以下示例展示了 Groovy 配置：

``` java
ApplicationContext context = new GenericGroovyApplicationContext("services.groovy", "daos.groovy");
```

最灵活的变体是将 `GenericApplicationContext` 与`读取器`代理结合使用，例如，与 XmlBeanDefinitionReader 一起处理 XML 文件，如以下示例所示：

``` java
GenericApplicationContext context = new GenericApplicationContext();
new XmlBeanDefinitionReader(context).loadBeanDefinitions("services.xml", "daos.xml");
context.refresh();
```

您还可以使用 GroovyBeanDefinitionReader 来处理 Groovy 文件，如以下示例所示：

``` java
GenericApplicationContext context = new GenericApplicationContext();
new GroovyBeanDefinitionReader(context).loadBeanDefinitions("services.groovy", "daos.groovy");
context.refresh();
```

您可以在同一个 ApplicationContext 中混合搭配这些读取器委托，从各种配置源中读取 Bean 定义。

然后，你可以使用 `getBean` 方法来获取你的 Bean 实例。虽然 `ApplicationContext` 接口还有其他一些方法可以用来检索 Bean，但理想情况下，你的应用程序代码不应该使用这些方法。实际上，你的应用程序代码完全不应该调用 `getBean()` 方法，从而避免对 Spring API 的任何依赖。例如，Spring 与 Web 框架的集成为各种 Web 框架组件（如控制器和 JSF 管理的 Bean）提供了依赖注入功能，使你可以通过元数据（例如自动装配注解）来声明对特定 Bean 的依赖。

## Bean概览

`Spring IoC 容器`管理一个或多个 `Bean`。这些 `Bean` 是根据您提供给容器的`配置元数据`创建的（例如，以 XML 中的 <bean/> 定义形式）。

在容器内部，这些 Bean 定义被表示为 `BeanDefinition` 对象，其中包含以下元数据（以及其他信息）：

- `包限定类名`：通常是所定义 Bean 的实际实现类。
- Bean 的`行为`配置元素：声明 Bean 在容器中应如何表现（作用域、生命周期回调等）。
- 对其他 Bean 的`引用`：这些引用是 Bean 完成其工作所需的，通常也被称为协作者或依赖项。
- 其他配置设置：用于设置新创建对象的属性，例如管理连接池的 Bean 中的池大小限制或连接数。

这些元数据被转换为组成每个 Bean 定义的一组属性。

除了包含如何创建特定 Bean 信息的 `Bean 定义`外，`ApplicationContext` 的实现还允许注册由用户在容器外部创建的现有对象。这可以通过调用 `ApplicationContext` 的 `getAutowireCapableBeanFactory() `方法来访问其 `BeanFactory` 实现实现，该方法返回 `DefaultListableBeanFactory` 实现。`DefaultListableBeanFactory` 通过 `registerSingleton(..)` 和 `registerBeanDefinition(..)` 方法支持这种注册。然而，典型的应用程序通常仅使用通过常规 Bean 定义元数据定义的 Bean。


!!! note

	Bean 元数据和手动提供的单例实例需要尽早注册，以便容器在自动装配和其他内省步骤中能够正确处理它们。虽然在一定程度上支持覆盖现有元数据和单例实例，但在运行时注册新 Bean（与工厂的实时访问同时进行）并未被官方支持，可能会导致并发访问异常、Bean 容器状态不一致，或两者兼而有之。

### 覆盖Beans

当使用已分配的`标识符`注册一个 Bean 时，就会发生 Bean 覆盖。虽然 Bean 覆盖是可行的，但这会使配置变得更难阅读。

!!! warning

	在未来的版本中，Bean 覆盖功能将被弃用。

要完全禁用 Bean 覆盖功能，可以在 `ApplicationContext` 刷新之前将 `allowBeanDefinitionOverriding 标志`设置为 false。在这种配置下，如果尝试使用 Bean 覆盖，将会抛出异常。

默认情况下，容器会以 INFO 级别记录每次覆盖 Bean 的尝试日志，以便您可以相应调整配置。虽然不推荐，但您可以通过将 `allowBeanDefinitionOverriding` 标志设置为 `true` 来关闭这些日志。

!!! Java配置

	如果使用 Java 配置，只要 @Bean 方法的返回类型与具有相同组件名称的扫描到的 Bean 类匹配，对应的 @Bean 方法总是会默默地覆盖该 Bean 类。这意味着容器会优先调用 @Bean 工厂方法，而不是使用该 Bean 类中预先声明的构造方法。

!!! note

	我们了解到，在测试场景中覆盖 Bean 是一种方便的做法，并且从 Spring Framework 6.2 开始对此提供了明确的支持。有关更多详情，请参阅本节内容。

### 命名Bean

每个 Bean 都有一个或多个`标识符`。这些标识符在托管该 Bean 的容器中必须是`唯一`的。通常，一个 Bean 只有一个标识符。然而，如果需要多个标识符，额外的标识符可以被视为`别名`。

在基于XML的配置元数据中，可以使用`id`属性、`name`属性或两者同时指定Bean的标识符。`id`属性允许您精确指定一个唯一的ID。按照惯例，这些名称通常是字母数字组合（如`myBean`、`someService`等），但也可以包含特殊字符。如果需要为Bean引入其他别名，可以通过`name`属性指定，多个别名之间可以用逗号（`,`）、分号（`;`）或`空格`分隔。尽管`id`属性被定义为`xsd:string`类型，但Bean ID的唯一性由容器强制执行，而不是由XML解析器保证。

您无需为一个 bean 提供名称或 ID。如果您没有显式提供名称或 ID，容器会为该 bean `生成一个唯一名称`。然而，如果您希望通过使用 `ref 元素`或服务定位器风格的查找方式`引用该 bean`，则必须提供一个名称。不提供名称的原因通常与使用内部 bean 和自动装配协作对象有关。


!!! note "Bean 命名规范"

	按照惯例，在为 Bean 命名时，应遵循标准的 Java 实例字段命名规范。也就是说，Bean 名称以`小写字母开头`，并采用`驼峰命名法`。例如，常见的命名包括 accountManager、accountService、userDao、loginController 等。

	保持 Bean 命名的一致性可以让配置文件更易于阅读和理解。此外，如果你使用 Spring AOP，在为一组通过名称关联的 Bean 应用通知时，这种命名方式也会带来很大的便利。

!!! note

	在类路径中进行组件扫描时，Spring 会为未命名的组件生成 Bean 名称，遵循前面描述的规则：基本上是取简单类名并将其首字母转换为小写。然而，在一种（不常见的）特殊情况下，如果类名有多个字符且前两个字符都是大写字母，则会保留原始大小写。这些规则与 java.beans.Introspector.decapitalize 定义的规则相同（Spring 在这里使用了该方法）。

#### 在Bean定义外部为Bean创建别名

在一个 Bean 定义中，你可以为该 Bean 提供多个名称，这可以通过结合使用 id 属性指定的一个名称以及 name 属性中指定的任意数量的其他名称来实现。这些名称可以作为同一个 Bean 的等效别名，在某些情况下非常有用，例如让应用程序中的每个组件通过使用特定于该组件的 Bean 名称来引用一个通用的依赖项。

指定所有别名并在 bean 实际定义的地方使用并不总是足够的。有时需要为在其他地方定义的 bean 引入一个别名。这种情况在大型系统中很常见，因为配置通常会分散在各个子系统中，每个子系统都有自己的一套对象定义。在基于 XML 的配置元数据中，可以使用 <alias/> 元素来实现这一点。以下示例展示了如何操作：

``` xml
<alias name="fromName" alias="toName"/>
```
在这种情况下，同一容器中的一个名为 fromName 的 bean，在使用该别名定义后，也可以被称为 toName。

例如，子系统A的配置元数据可能通过名称`subsystemA-dataSource`引用一个DataSource。子系统B的配置元数据可能通过名称`subsystemB-dataSource`引用一个DataSource。当组合使用这两个子系统的主应用程序时，主应用程序通过名称`myApp-dataSource`引用该DataSource。为了让这三个名称都引用同一个对象，可以在配置元数据中添加以下别名定义：

``` xml
<alias name="myApp-dataSource" alias="subsystemA-dataSource"/>
<alias name="myApp-dataSource" alias="subsystemB-dataSource"/>
```

现在，每个组件和主应用程序都可以通过一个唯一且保证不会与其他定义冲突的名称（实际上创建了一个命名空间）引用数据源，同时它们指向的是同一个 Bean。

### 实例化Bean

一个 `Bean 定义`本质上是创建一个或多个对象的配方。当容器被请求时，会查看指定 Bean 的配方，并使用该 Bean 定义所包含的配置元数据来创建（或获取）实际的对象。

如果您使用基于 `XML` 的配置元数据，可以在 `<bean/> 元素`的 `class 属性`中指定要实例化的对象类型（或类）。这个 class 属性（在内部是 BeanDefinition 实例上的一个 Class 属性）通常是必需的。（有关例外情况，请参阅使用实例工厂方法进行实例化和 Bean 定义继承。）您可以通过以下两种方式使用 Class 属性：

- 通常情况下，当容器通过反射调用构造方法直接创建 Bean 时，需要指定类似于要构造的 Bean 类，这使用 Java 中的 `new` 操作符。  
- 在较少见的情况下，如果容器通过调用某个类的静态工厂方法来创建 Bean，则需要指定包含该静态工厂方法的实际类。静态工厂方法返回的对象类型可以是同一个类，也可以是完全不同的类。

!!! note "嵌套类名"

	如果您想为嵌套类配置一个 Bean 定义，可以使用嵌套类的二进制名称或源名称。

	例如，如果你有一个位于 `com.example` 包中的名为 `SomeThing` 的类，而这个 `SomeThing` 类中有一个静态嵌套类 `OtherThing`，它们可以通过美元符号（`$`）或点号（`.`）分隔。因此，在 bean 定义中，class 属性的值可以是 `com.example.SomeThing$OtherThing` 或 `com.example.SomeThing.OtherThing`。

#### 使用构造函数进行实例化

当你通过`构造函数`方式创建一个 Bean 时，所有普通类都可以被 Spring 使用并与之兼容。也就是说，正在开发的类不需要实现任何特定的接口，也不需要以特定的方式编写代码。只需指定 Bean 的类即可。然而，根据你为该特定 Bean 使用的 IoC 类型，可能需要一个默认（空）构造函数。

Spring IoC 容器几乎可以管理任何你希望它管理的类，并不限于管理真正的 JavaBeans。大多数 Spring 用户更倾向于使用实际的 JavaBeans，这些 JavaBeans 只有一个默认（无参数）构造函数，以及基于容器中属性设计的合适的 setter 和 getter 方法。你也可以在容器中使用更特殊的非 Bean 风格的类。例如，如果你需要使用一个完全不符合 JavaBean 规范的遗留连接池，Spring 同样可以对其进行管理。

使用基于XML的配置元数据，您可以按如下方式指定您的Bean类：

``` xml
<bean id="exampleBean" class="examples.ExampleBean"/>

<bean name="anotherExample" class="examples.ExampleBeanTwo"/>
```

!!! note

	对于构造函数参数的情况，容器可以从多个重载的构造函数中选择一个对应的构造函数。不过，为了避免歧义，建议尽量保持构造函数的签名简单明了。

#### 使用静态工厂方法进行实例化

当使用`静态工厂方法`定义一个 bean 时，可以通过 `class 属性`指定`包含该静态工厂方法的类`，并通过 `factory-method 属性`指定工厂方法的名称。你应该能够调用这个方法（可以包含可选参数，稍后会详细说明），并返回一个活动对象，该对象随后会被视为通过构造函数创建的一样处理。这种 bean 定义的一种用途是调用遗留代码中的静态工厂方法。

以下的 Bean 定义指定了通过调用工厂方法来创建 Bean。该定义并未指定返回对象的类型（类），而是指定了包含工厂方法的类。在此示例中，createInstance() 方法必须是一个`静态方法`。以下示例展示了如何指定一个工厂方法：

``` xml
<bean id="clientService"
	class="examples.ClientService"
	factory-method="createInstance"/>
```

以下示例展示了一个与前述 Bean 定义配合使用的类：

``` java
public class ClientService {
	private static ClientService clientService = new ClientService();
	private ClientService() {}

	public static ClientService createInstance() {
		return clientService;
	}
}
```

!!! note

	对于工厂方法的参数情况，容器可以从多个同名的重载方法中选择一个对应的方法。不过，为了避免产生歧义，建议尽量保持工厂方法的签名简单明了。

!!! tip

	使用工厂方法重载时，一个典型的问题案例是 Mockito，它的 mock 方法有许多重载版本。应尽量选择 mock 方法中最具体的变体：

	``` xml
	<bean id="clientService" class="org.mockito.Mockito" factory-method="mock">
		<constructor-arg type="java.lang.Class" value="examples.ClientService"/>
		<constructor-arg type="java.lang.String" value="clientService"/>
	</bean>
	```

#### 使用实例工厂方法进行实例化

类似于通过静态工厂方法进行实例化，通过`实例工厂方法`进行实例化是调用容器中某个现有 Bean 的非静态方法来创建一个新的 Bean。要使用这种机制，可以将 class 属性留空，并在 factory-bean 属性中指定当前容器（或父容器或祖先容器）中包含要调用的实例方法的 Bean 的名称。通过 factory-method 属性设置工厂方法本身的名称。以下示例展示了如何配置这样的 Bean：

``` xml
<!-- 工厂 Bean，包含一个名为 createClientServiceInstance() 的方法 -->
<bean id="serviceLocator" class="examples.DefaultServiceLocator">
	<!-- 注入该定位器 Bean 所需的任何依赖项 -->
</bean>

<!-- 通过工厂 Bean 创建的 Bean -->
<bean id="clientService"
	factory-bean="serviceLocator"
	factory-method="createClientServiceInstance"/>
```

以下示例展示了对应的类：

``` java
public class DefaultServiceLocator {

	private static ClientService clientService = new ClientServiceImpl();

	public ClientService createClientServiceInstance() {
		return clientService;
	}
}
```

一个工厂类也可以包含多个工厂方法，如以下示例所示：

``` xml
<bean id="serviceLocator" class="examples.DefaultServiceLocator">
	<!-- 为此定位器 Bean 注入所需的任何依赖项。 -->
</bean>

<bean id="clientService"
	factory-bean="serviceLocator"
	factory-method="createClientServiceInstance"/>

<bean id="accountService"
	factory-bean="serviceLocator"
	factory-method="createAccountServiceInstance"/>
```

以下示例展示了对应的类：

``` java
public class DefaultServiceLocator {

	private static ClientService clientService = new ClientServiceImpl();

	private static AccountService accountService = new AccountServiceImpl();

	public ClientService createClientServiceInstance() {
		return clientService;
	}

	public AccountService createAccountServiceInstance() {
		return accountService;
	}
}
```

这种方法表明，工厂 Bean 本身可以通过依赖注入（DI）进行管理和配置。

!!! note

	在 Spring 文档中，“factory bean” 指的是在 Spring 容器中配置的一个 bean，它通过实例工厂方法或静态工厂方法创建对象。而 FactoryBean（注意大小写）则特指 Spring 特有的 FactoryBean 实现类。

#### 确定Bean的运行时类型

确定特定 Bean 的运行时类型并非易事。在 Bean 元数据定义中指定的类只是一个初始类引用，这可能与声明的工厂方法结合使用，或者是一个 FactoryBean 类，这可能导致 Bean 的运行时类型有所不同；如果是实例级别的工厂方法（通过指定的 factory-bean 名称解析），甚至可能根本没有设置。此外，AOP 代理可能会使用基于接口的代理包装 Bean 实例，从而限制对目标 Bean 实际类型的暴露（仅限其实现的接口）。

推荐的方式是通过调用 `BeanFactory.getType` 方法并传入指定的 bean 名称，来了解特定 bean 的实际运行时类型。此方法会考虑上述所有情况，并返回 `BeanFactory.getBean` 方法针对同一 bean 名称将要返回的对象类型。

## 依赖注入

一个典型的企业应用程序并不是由单一的对象（或在 Spring 术语中称为 Bean）组成的。即使是最简单的应用程序，也包含一些对象，这些对象协同工作，为终端用户呈现一个连贯的应用程序。接下来的部分将解释如何从定义一系列独立的 Bean 定义，发展到一个完整实现的应用程序，其中的对象相互协作以实现目标。

`依赖注入（Dependency Injection，简称 DI）`是一种过程，通过该过程，对象仅通过构造函数参数、工厂方法的参数，或在对象实例构造完成后或从工厂方法返回后设置的属性来定义其依赖关系（即与之协作的其他对象）。容器在创建 Bean 时会注入这些依赖关系。这个过程本质上与传统方式相反（因此被称为控制反转，Inversion of Control），即由 Bean 自身通过直接实例化类或使用服务定位器模式来控制其依赖的实例化或定位。

遵循依赖注入（DI）原则可以让代码更加简洁，同时在为对象提供其依赖项时，解耦也更加高效。对象无需自行查找其依赖项，也无需了解依赖项的位置或具体的类。因此，类的测试变得更加容易，尤其是当依赖项基于接口或抽象基类时，可以在单元测试中使用桩（stub）或模拟（mock）实现。

DI 主要有两种形式：`基于构造函数的依赖注入(Constructor-based dependency injection)`和`基于 Setter 的依赖注入(Setter-based dependency injection)`。

### 构造函数注入

基于构造函数的依赖注入（DI）是通过容器调用带有多个参数的构造函数来实现的，每个参数都代表一个依赖项。使用带有特定参数的静态工厂方法来构造 Bean 的方式几乎是等效的，因此在讨论中，构造函数参数和静态工厂方法的参数被视为类似。以下示例展示了一个只能通过构造函数注入进行依赖注入的类：

```java
public class SimpleMovieLister {

	// SimpleMovieLister 依赖于 MovieFinder。
	private final MovieFinder movieFinder;

	// 一个构造函数，使得 Spring 容器可以注入一个 MovieFinder。
	public SimpleMovieLister(MovieFinder movieFinder) {
		this.movieFinder = movieFinder;
	}

	// 实际使用注入的 MovieFinder 的业务逻辑已被省略……
}
```

请注意，这个类并没有什么特别之处。它是一个普通的 POJO，没有依赖于容器特定的接口、基类或注解。

#### 构造函数参数解析

构造函数参数的解析匹配是通过参数的`类型`来完成的。如果一个 Bean 定义的构造函数参数中不存在潜在的歧义，那么在 Bean 实例化时，构造函数参数的定义顺序将决定这些参数被传递给相应构造函数的顺序。请参考以下类：

``` java
package x.y;

public class ThingOne {

	public ThingOne(ThingTwo thingTwo, ThingThree thingThree) {
		// ...
	}
}
```

假设 ThingTwo 和 ThingThree 类之间没有继承关系，则不存在潜在的歧义。因此，以下配置可以正常工作，且无需在 <constructor-arg/> 元素中显式指定构造函数参数的索引或类型。

``` xml

<beans>
    <bean id="beanOne" class="x.y.ThingOne">
        <constructor-arg ref="beanTwo"/>
        <constructor-arg ref="beanThree"/>
    </bean>

    <bean id="beanTwo" class="x.y.ThingTwo"/>

    <bean id="beanThree" class="x.y.ThingThree"/>
</beans>
```

当引用另一个 Bean 时，其类型是已知的，因此可以进行匹配（就像前面的示例中那样）。但当使用简单类型时，例如 `<value>true</value>`，Spring 无法确定该值的类型，因此在没有额外帮助的情况下无法通过类型进行匹配。请参考以下类：

```java
package examples;

public class ExampleBean {

	// 计算终极答案所需的年份数量
	private final int years;

	// 生命、宇宙以及一切的答案
	private final String ultimateAnswer;

	public ExampleBean(int years, String ultimateAnswer) {
		this.years = years;
		this.ultimateAnswer = ultimateAnswer;
	}
}
```

##### 构造函数参数类型匹配

在上述场景中，如果通过 type 属性显式指定构造函数参数的类型，容器可以对简单类型使用类型匹配，如以下示例所示：

``` xml
<bean id="exampleBean" class="examples.ExampleBean">
	<constructor-arg type="int" value="7500000"/>
	<constructor-arg type="java.lang.String" value="42"/>
</bean>
```

##### 构造函数参数索引

您可以使用 index 属性显式指定构造函数参数的索引，如以下示例所示：

``` xml
<bean id="exampleBean" class="examples.ExampleBean">
	<constructor-arg index="0" value="7500000"/>
	<constructor-arg index="1" value="42"/>
</bean>
```

除了可以解决多个简单值的歧义之外，指定索引还可以解决构造函数中存在两个相同类型参数时的歧义问题。

!!! note

	索引是从0开始的。

##### 构造函数参数名称

您还可以使用构造函数参数名称来消除值的歧义，如以下示例所示：

``` xml
<bean id="exampleBean" class="examples.ExampleBean">
	<constructor-arg name="years" value="7500000"/>
	<constructor-arg name="ultimateAnswer" value="42"/>
</bean>
```

请记住，为了让代码开箱即用，必须在启用 `-parameters` 标志的情况下编译代码，这样 Spring 才能从构造函数中获取参数名称。如果您无法或不想使用 `-parameters` 标志编译代码，可以使用 `@ConstructorProperties` JDK 注解显式指定构造函数参数的名称。示例类的代码应如下所示：

``` java

package examples;

public class ExampleBean {

	// Fields omitted

	@ConstructorProperties({"years", "ultimateAnswer"})
	public ExampleBean(int years, String ultimateAnswer) {
		this.years = years;
		this.ultimateAnswer = ultimateAnswer;
	}
} 
```

### 基于Setter的依赖注入

通过调用`无参构造方法`或`无参静态工厂方法`实例化 Bean 后，容器会调用 Bean 的 `setter 方法`来实现基于 Setter 的依赖注入（DI）。

以下示例展示了一个只能通过纯Setter注入进行依赖注入的类。这个类是传统的Java类，是一个没有依赖于容器特定接口、基类或注解的POJO。

```java
public class SimpleMovieLister {

	// SimpleMovieLister 依赖于 MovieFinder。
	private MovieFinder movieFinder;

	// 为使 Spring 容器能够注入一个 MovieFinder，提供一个 setter 方法。
	public void setMovieFinder(MovieFinder movieFinder) {
		this.movieFinder = movieFinder;
	}

	// 实际使用注入的 MovieFinder 的业务逻辑被省略了……
}
```

`ApplicationContext` 支持对其管理的 Bean 进行基于`构造函数`和基于 `Setter` 的依赖注入（DI）。它还支持在通过构造函数方式注入了一些依赖后，再进行基于 Setter 的依赖注入。您可以通过 `BeanDefinition` 的形式配置依赖项，并结合 `PropertyEditor` 实例将属性从一种格式转换为另一种格式。然而，大多数 Spring 用户并不会直接（即以编程方式）使用这些类，而是通过 `XML Bean` 定义、`带有注解的组件`（例如使用 @Component、@Controller 等注解的类）或基于 `Java 的 @Configuration 类`中的 `@Bean 方法`来定义。这些来源随后会在内部转换为 `BeanDefinition 实例`，并用于加载整个 Spring IoC 容器实例。


!!! note "构造器注入还是设值注入？"

	由于可以混合使用基于构造函数和基于 setter 的依赖注入（DI），一个好的经验法则是：`对于必需的依赖项使用构造函数，而对于可选的依赖项使用 setter 方法或配置方法。`需要注意的是，在 setter 方法上使用 `@Autowired 注解`可以将属性设为必需的依赖项；然而，带有参数程序化验证的构造函数注入通常是更优的选择。

	Spring团队通常提倡使用构造函数注入，因为它可以让你将应用程序组件实现为不可变对象，并确保所需的依赖项不会为null。此外，通过构造函数注入的组件在返回给客户端（调用方）代码时，总是处于完全初始化的状态。顺带一提，如果构造函数参数过多，这通常是代码设计不良的信号，暗示该类可能承担了过多的职责，应该进行重构以更好地实现关注点分离。

	Setter注入主要应用于那些可以在类中分配合理默认值的可选依赖项。否则，代码在使用该依赖项时必须到处进行非空检查。Setter注入的一个好处是，setter方法使该类的对象能够在之后重新配置或重新注入。因此，通过JMX MBeans进行管理是一个非常适合使用setter注入的场景。

	请根据具体类的需求选择最合适的依赖注入（DI）方式。有时，当处理第三方类且无法获取其源码时，这种选择可能已经被限定。例如，如果某个第三方类没有公开任何 setter 方法，那么构造函数注入可能是唯一可用的依赖注入方式。

### 依赖解析过程

容器执行 Bean 依赖解析的方式如下：

- 应用上下文（ApplicationContext）会根据配置元数据创建并初始化，这些元数据描述了所有的 Bean。配置元数据可以通过 XML、Java 代码或注解来指定。
- 对于每个 Bean，其依赖关系通过属性、构造函数参数或静态工厂方法的参数（如果你选择使用静态工厂方法而不是普通构造函数）来表示。这些依赖关系会在 Bean 实际创建时提供给它。
- 每个属性或构造函数参数要么是一个实际的值定义，要么是对容器中另一个 Bean 的引用。
- 每个作为属性或构造函数参数的值都会从其指定格式转换为该属性或构造函数参数的实际类型。默认情况下，Spring 可以将以字符串格式提供的值转换为所有内置类型，例如 int、long、String、boolean 等。

Spring 容器在创建时会验证每个 Bean 的配置。然而，Bean 的属性本身只有在 Bean 实际被创建时才会被设置。默认情况下，那些具有单例作用域并设置为预实例化的 Bean 会在容器创建时被实例化。Bean 的作用域可以在 Bean Scopes 中定义。否则，只有在 Bean 被请求时才会创建它。创建一个 Bean 可能会导致一个 Bean 图被创建，因为该 Bean 的依赖项及其依赖项的依赖项（以此类推）都会被创建并分配。需要注意的是，这些依赖项之间的解析不匹配可能会在稍后显现——也就是说，会在首次创建受影响的 Bean 时暴露出来。

!!! note "循环依赖"

	如果主要使用构造函数注入，可能会导致无法解决的循环依赖问题。例如：类 A 通过构造函数注入需要类 B 的实例，而类 B 通过构造函数注入需要类 A 的实例。如果将类 A 和类 B 的 Bean 配置为相互注入，Spring IoC 容器会在运行时检测到这种循环引用，并抛出 `BeanCurrentlyInCreationException` 异常。

	一种可能的解决方案是修改某些类的源码，将其配置为通过 Setter 方法注入而非构造函数注入。或者，可以完全避免使用构造函数注入，仅使用 Setter 方法注入。换句话说，尽管不推荐，但可以通过 Setter 方法注入来配置循环依赖。

	与典型情况（没有循环依赖）不同，Bean A 和 Bean B 之间的循环依赖会导致其中一个 Bean 在自身尚未完全初始化之前被注入到另一个 Bean 中（这就是经典的“先有鸡还是先有蛋”的问题）。

通常情况下，你可以信任 Spring 来做出正确的决策。它会在容器加载时检测配置问题，例如引用不存在的 Bean 和循环依赖。Spring 会尽可能延迟设置属性和解析依赖关系，直到实际创建 Bean 时才进行。这意味着，即使一个 Spring 容器已经正确加载，在你请求某个对象时，如果在创建该对象或其依赖项时出现问题（例如，某个 Bean 因缺少或无效的属性而抛出异常），仍然可能会抛出异常。正是由于某些配置问题的可见性可能会被延迟，ApplicationContext 的实现默认会预先实例化单例 Bean。虽然这会在创建这些 Bean 时消耗一些额外的时间和内存，但它能让你在 ApplicationContext 创建时就发现配置问题，而不是在之后才发现。当然，你仍然可以覆盖这种默认行为，使单例 Bean 延迟初始化，而不是急切地预先实例化。

如果不存在循环依赖，当一个或多个协作 Bean 被注入到一个依赖 Bean 中时，每个协作 Bean 都会在注入到依赖 Bean 之前完全配置好。这意味着，如果 Bean A 依赖于 Bean B，Spring IoC 容器会在调用 Bean A 的 setter 方法之前，完全配置好 Bean B。换句话说，Bean 会被实例化（如果它不是一个预先实例化的单例），其依赖项会被设置，并且相关的生命周期方法（例如配置的初始化方法或 InitializingBean 回调方法）会被调用。

### 依赖注入示例

以下示例使用基于 XML 的配置元数据来实现基于 Setter 的依赖注入。Spring XML 配置文件的一小部分定义了一些 Bean，如下所示：

``` xml
<bean id="exampleBean" class="examples.ExampleBean">
	<!-- 使用嵌套的 ref 元素进行 setter 注入 -->
	<property name="beanOne">
		<ref bean="anotherExampleBean"/>
	</property>

	<!-- 使用更简洁的 ref 属性进行 setter 注入 -->
	<property name="beanTwo" ref="yetAnotherBean"/>
	<property name="integerProperty" value="1"/>
</bean>

<bean id="anotherExampleBean" class="examples.AnotherBean"/>
<bean id="yetAnotherBean" class="examples.YetAnotherBean"/>
```

以下示例展示了对应的 ExampleBean 类：

``` java
public class ExampleBean {

	private AnotherBean beanOne;

	private YetAnotherBean beanTwo;

	private int i;

	public void setBeanOne(AnotherBean beanOne) {
		this.beanOne = beanOne;
	}

	public void setBeanTwo(YetAnotherBean beanTwo) {
		this.beanTwo = beanTwo;
	}

	public void setIntegerProperty(int i) {
		this.i = i;
	}
}
```

在前面的示例中，使用了与 XML 文件中指定属性相匹配的 setter 方法。以下示例则采用基于构造函数的依赖注入（DI）：

``` xml
<bean id="exampleBean" class="examples.ExampleBean">
	<!-- constructor injection using the nested ref element -->
	<constructor-arg>
		<ref bean="anotherExampleBean"/>
	</constructor-arg>

	<!-- constructor injection using the neater ref attribute -->
	<constructor-arg ref="yetAnotherBean"/>

	<constructor-arg type="int" value="1"/>
</bean>

<bean id="anotherExampleBean" class="examples.AnotherBean"/>
<bean id="yetAnotherBean" class="examples.YetAnotherBean"/>
```

以下示例展示了对应的 ExampleBean 类：

```java
public class ExampleBean {

	private AnotherBean beanOne;

	private YetAnotherBean beanTwo;

	private int i;

	public ExampleBean(
		AnotherBean anotherBean, YetAnotherBean yetAnotherBean, int i) {
		this.beanOne = anotherBean;
		this.beanTwo = yetAnotherBean;
		this.i = i;
	}
}
```

在 bean 定义中指定的构造函数参数会作为 ExampleBean 构造函数的参数使用。

现在来看这个示例的一个变体，在这个变体中，Spring 被告知调用一个静态工厂方法来返回对象的实例，而不是使用构造函数：

``` xml
<bean id="exampleBean" class="examples.ExampleBean" factory-method="createInstance">
	<constructor-arg ref="anotherExampleBean"/>
	<constructor-arg ref="yetAnotherBean"/>
	<constructor-arg value="1"/>
</bean>

<bean id="anotherExampleBean" class="examples.AnotherBean"/>
<bean id="yetAnotherBean" class="examples.YetAnotherBean"/>
```

以下示例展示了对应的 ExampleBean 类：

``` java
public class ExampleBean {

	// 私有构造函数
	private ExampleBean(...) {
		...
	}

	// 一个静态工厂方法；传递给该方法的参数可以被视为返回的 Bean 的依赖项，
	// 无论这些参数实际是如何被使用的。
	public static ExampleBean createInstance (
		AnotherBean anotherBean, YetAnotherBean yetAnotherBean, int i) {

		ExampleBean eb = new ExampleBean (...);
		// 一些其他操作...
		return eb;
	}
}
```

静态工厂方法的参数通过`<constructor-arg/>`元素提供，这与实际使用构造函数时完全相同。工厂方法返回的类的类型不一定要与包含静态工厂方法的类的类型相同（尽管在本例中是相同的）。实例（非静态）工厂方法的使用方式基本相同（只是使用`factory-bean属性`代替`class属性`），因此我们在此不详细讨论这些细节。

## 依赖项与配置详解

正如前一节所提到的，您可以将 Bean 的属性和构造函数参数定义为对其他受管 Bean（协作者）的引用，或者直接内联定义为具体的值。Spring 基于 XML 的配置元数据支持在其 <property/> 和 <constructor-arg/> 元素中使用子元素类型来实现这一目的。正如前一节所提到的，您可以将 Bean 的属性和构造函数参数定义为对其他受管 Bean（协作者）的引用，或者直接内联定义为具体的值。Spring 基于 XML 的配置元数据支持在其 <property/> 和 <constructor-arg/> 元素中使用子元素类型来实现这一目的。

### 直接值（原始类型、字符串等）

`<property/>` 元素的 value 属性以人类可读的字符串形式指定属性或构造函数参数的值。Spring 的转换服务会将这些字符串值转换为属性或参数的实际类型。以下示例展示了设置各种值的方式：

``` xml
<bean id="myDataSource" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
	<!-- results in a setDriverClassName(String) call -->
	<property name="driverClassName" value="com.mysql.jdbc.Driver"/>
	<property name="url" value="jdbc:mysql://localhost:3306/mydb"/>
	<property name="username" value="root"/>
	<property name="password" value="misterkaoli"/>
</bean>
```

以下示例使用 p-命名空间，使 XML 配置更加简洁：

``` xml
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:p="http://www.springframework.org/schema/p"
	xsi:schemaLocation="http://www.springframework.org/schema/beans
	https://www.springframework.org/schema/beans/spring-beans.xsd">

	<bean id="myDataSource" class="org.apache.commons.dbcp.BasicDataSource"
		destroy-method="close"
		p:driverClassName="com.mysql.jdbc.Driver"
		p:url="jdbc:mysql://localhost:3306/mydb"
		p:username="root"
		p:password="misterkaoli"/>

</beans>
```

上述的 XML 更为简洁。然而，除非使用支持自动属性补全功能的 IDE（例如 IntelliJ IDEA 或 Spring Tools for Eclipse）来创建 Bean 定义，否则拼写错误会在运行时而非设计时被发现。强烈推荐使用此类 IDE 辅助工具。

您还可以按照以下方式配置一个 java.util.Properties 实例：XML

```xml
<bean id="mappings"
	class="org.springframework.context.support.PropertySourcesPlaceholderConfigurer">

	<!-- 类型为 java.util.Properties -->
	<property name="properties">
		<value>
			jdbc.driver.className=com.mysql.jdbc.Driver
			jdbc.url=jdbc:mysql://localhost:3306/mydb
		</value>
	</property>
</bean>
```

Spring 容器通过 JavaBeans 的 PropertyEditor 机制，将 <value/> 元素中的文本转换为一个 java.util.Properties 实例。这是一种非常方便的快捷方式，也是 Spring 团队在少数情况下更倾向于使用嵌套的 <value/> 元素而非 value 属性风格的地方之一。

#### idref 元素

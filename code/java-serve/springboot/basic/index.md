# 基础知识

## 功能概述

- 创建独立的 Spring 应用程序
- 直接嵌入 Tomcat、Jetty 或 Undertow（无需部署 WAR 文件）
- 提供预配置的 "starter" 依赖项，以简化构建配置
- 在可能的情况下，自动配置 Spring 和第三方库
- 提供生产就绪的功能，如指标、健康检查和外部化配置
- 绝不进行代码生成，也不需要 XML 配置

## 入门案例

### Maven方式

- 创建Maven项目
- pom.xml继承`spring-boot-starter-parent`父项目，并引入相关`依赖`，配置`打包插件`：

``` xml title="pom.xml"
--8<-- "code/java-serve/springboot/basic/springboot-hello/pom.xml"
```

- 创建Spring Boot配置文件

``` yaml title="application.yml"
--8<-- "code/java-serve/springboot/basic/springboot-hello/src/main/resources/application.yml"
```

- 编写项目入口程序：

``` java title="SpringBootHelloApplication.java"
--8<-- "code/java-serve/springboot/basic/springboot-hello/src/main/java/com/luguosong/SpringBootHelloApplication.java"
```

- 编写Controller:

``` java title="HelloController.java"
--8<-- "code/java-serve/springboot/basic/springboot-hello/src/main/java/com/luguosong/controller/HelloController.java"
```

### Spring Initializr方式

- 创建Spring Initializr项目

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409141347754.png){ loading=lazy }
  <figcaption>创建Spring Initializr项目</figcaption>
</figure>

- 选中需要的启动器(spring-boot-starter-xxx)

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409141351990.png){ loading=lazy }
  <figcaption>选中需要的启动器</figcaption>
</figure>

- 创建工具会引入相关依赖，并自动生成`配置文件`和`启动类`。

## 依赖管理机制

### 场景启动器

`Spring场景启动器`会将该场景需要的依赖全部导入

!!! note "场景启动器命名规则"

	- 官方场景：spring-boot-starter-xxx
	- 第三方场景：xxx-spring-boot-starter

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409141423974.png){ loading=lazy }
  <figcaption>spring-boot-starter-web导入web开发的全部依赖</figcaption>
</figure>

### 版本管理

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409141436307.png){ loading=lazy }
  <figcaption>依赖管理机制</figcaption>
</figure>

Spring Boot 父项目`spring-boot-starter-parent`继承自`spring-boot-dependencies`项目（版本仲裁中心），该项目通过
`<dependencyManagement>`
管理依赖版本。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409141425489.png){ loading=lazy }
  <figcaption>spring-boot-dependencies负责版本管理</figcaption>
</figure>

Maven版本具有就近原则特性，如果我们不想使用Spring Boot提供的依赖版本。可以在当前项目自定义`<properties>`
标签自定义版本，标签名需要与spring-boot-dependencies中的一致。

## 自动配置机制

### spring-boot-autoconfigure依赖

所有`spring-boot-starter-xxx`启动器都会依赖`spring-boot-starter`依赖。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409301603718.png){ loading=lazy }
  <figcaption>以spring-boot-starter-web为例，所有场景启动器内部都会依赖spring-boot-starter</figcaption>
</figure>

`spring-boot-starter`依赖内部会依赖`spring-boot-autoconfigure`依赖。`spring-boot-autoconfigure`负责自动配置。

### 配置属性类

在Spring Boot配置文件`application.yml`中，所有`配置项`都和某个`类的属性`值是一一绑定的。

比如，配置文件中的`server.port`属性值就对应`org.springframework.boot.autoconfigure.web.ServerProperties`类中的`port`属性的值。

属性类可以通过`@ConfigurationProperties`注解指定配置的前缀。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202410012237998.png){ loading=lazy }
  <figcaption>通过@ConfigurationProperties注解指定配置的前缀</figcaption>
</figure>

也可以使用`@EnableConfigurationProperties`注解指定配置类：

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202410021819305.png){ loading=lazy }
  <figcaption>指定配置类</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409301544981.png){ loading=lazy }
  <figcaption>可以在官方文档中查看全部配置对应的配置类</figcaption>
</figure>

### 按需自动配置

`spring-boot-autoconfigure`会使用`@ConditionalOnxxx`条件注解在条件满足时自动配置。

- `@ConditionalOnClass`:如果类路径中存在该类，则触发指定行为。
- `@ConditionalOnMissingClass`:如果类路径中不存在该类，则触发指定行为。
- `@ConditionalOnBean`:如果容器中存在该类，则触发指定行为。
- `@ConditionalOnMissingBean`:如果容器中不存在该类，则触发指定行为。

### 完整流程

<figure markdown="span">
  ![](https://edrawcloudpubliccn.oss-cn-shenzhen.aliyuncs.com/viewer/self/1059758/share/2024-10-4/1728016059/main.svg){ loading=lazy }
  <figcaption>执行流程</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409141503347.png){ loading=lazy }
  <figcaption>Spring Boot 自动配置机制</figcaption>
</figure>

## 包扫描配置

### 默认配置

默认情况下，Spring Boot会扫描入口类下面以及其子包下的所有类。

``` java

@SpringBootApplication
public class SpringBootHelloApplication {
	/*
	 * Spring Boot启动代码
	 * */
	public static void main(String[] args) {
		SpringApplication.run(SpringBootHelloApplication.class, args);
	}
}
```

### scanBasePackages参数

可以使用`@SpringBootApplication`注解的`scanBasePackages`参数来配置包的扫描范围。

``` java

@SpringBootApplication(scanBasePackages = "com.luguosong")
public class SpringBootHelloApplication {
	/*
	 * Spring Boot启动代码
	 * */
	public static void main(String[] args) {
		SpringApplication.run(SpringBootHelloApplication.class, args);
	}
}
```

### @ComponentScan注解

可以使用`@ComponentScan`注解来配置包的扫描范围

``` java

@SpringBootApplication
@ComponentScan("com.luguosong")
public class SpringBootHelloApplication {
	/*
	 * Spring Boot启动代码
	 * */
	public static void main(String[] args) {
		SpringApplication.run(SpringBootHelloApplication.class, args);
	}
}
```

## Profiles

Spring Profiles 提供了一种方法，可以将应用程序配置的部分进行隔离，使其仅在特定环境中可用。任何 @Component、@Configuration 或 @ConfigurationProperties 都可以使用 `@Profile` 标注，以限制其加载时机，如下例所示：

``` java
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration(proxyBeanMethods = false)
@Profile("production")
public class ProductionConfiguration {

	// ...

}
```

!!! note

	如果通过`@EnableConfigurationProperties`而不是自动扫描来注册`@ConfigurationProperties` bean，则需要在具有`@EnableConfigurationProperties`注解的`@Configuration`类上指定`@Profile`注解。在扫描`@ConfigurationProperties`的情况下，可以直接在`@ConfigurationProperties`类上指定@Profile注解。

您可以使用 `spring.profiles.active` 环境属性来指定哪些配置文件是激活的。您可以通过本章前面描述的任何方式来指定该属性。例如，您可以将其包含在 `application.yml` 文件中，如下例所示：

```yaml
spring:
  profiles:
    active: "dev,hsqldb"
```

您还可以在命令行中使用以下开关指定它：`--spring.profiles.active=dev,hsqldb`。

如果没有激活任何配置文件，则会启用默认配置文件。默认配置文件的名称是 `default`，可以使用 `spring.profiles.default` `Environment` 属性进行调整，如下例所示：

```yaml
spring:
  profiles:
    default: "none"
```

`spring.profiles.active` 和 `spring.profiles.default` 只能用于非特定配置文件的文档。这意味着它们不能包含在特定配置文件中，也不能用于通过 `spring.config.activate.on-profile` 激活的文档。

例如，第二个文档配置无效：

```yaml
# 此文件有效
spring:
  profiles:
    active: "prod"
---
# 该文件无效。
spring:
  config:
    activate:
      on-profile: "prod"
  profiles:
    active: "metrics"
```

### 添加Active Profiles

`spring.profiles.active` 属性遵循与其他属性相同的排序规则：优先级最高的 `PropertySource` 会生效。这意味着你可以在 `application.yml` 中指定活动的配置文件，然后通过命令行开关进行替换。

有时候，添加属性以补充现有的活动配置文件而不是替换它们是很有用的。可以使用 `spring.profiles.include` 属性在 `spring.profiles.active` 属性激活的配置文件基础上添加活动配置文件。`SpringApplication` 的入口点也提供了一个 Java API 用于设置额外的配置文件。请参阅 `SpringApplication` 中的 `setAdditionalProfiles()` 方法。

例如，当运行具有以下属性的应用程序时，即使使用 `--spring.profiles.active` 开关运行，`common`和`local`配置文件也会被激活：

```yaml
spring:
  profiles:
    include:
      - "common"
      - "local"
```

!!! warning

	与 `spring.profiles.active` 类似，`spring.profiles.include` 只能用于非特定配置文件的文档。这意味着它不能包含在特定配置文件中，也不能用于通过 `spring.config.activate.on-profile` 激活的文档。

在下一个部分中描述的配置文件组也可以用于在某个配置文件处于活动状态时添加活动配置文件。

### Profile Groups

有时，您在应用程序中定义和使用的配置文件过于细化，使用起来变得繁琐。例如，您可能有用于分别启用数据库和消息功能的proddb和prodmq配置文件。

为此，Spring Boot 允许您定义配置文件组。配置文件组使您可以为一组相关的配置文件定义一个逻辑名称。

例如，我们可以创建一个由我们的proddb和prodmq配置文件组成的生产组。

```yaml
spring:
  profiles:
    group:
      production:
        - "proddb"
        - "prodmq"
```

我们的应用程序现在可以使用 --spring.profiles.active=production 来启动，从而一次性激活 production、proddb 和 prodmq 配置文件。

!!! warning

	与 spring.profiles.active 和 spring.profiles.include 类似，spring.profiles.group 只能用于非特定配置文件的文档。这意味着它不能包含在特定配置文件中，也不能在通过 spring.config.activate.on-profile 激活的文档中使用。

### 程序化设置Profiles

在应用程序运行之前，可以通过调用 `SpringApplication.setAdditionalProfiles(…)` 来以编程方式设置活动配置文件。也可以使用 Spring 的 ConfigurableEnvironment 接口来激活配置文件。

### 特定配置文件

特定配置文件的 application.properties（或 application.yaml）以及通过 @ConfigurationProperties 引用的文件都会被视为文件并加载。详情请参阅特定配置文件。

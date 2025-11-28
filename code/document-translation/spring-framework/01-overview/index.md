# Spring 框架概述

Spring框架让创建 Java 企业应用变得轻而易举。它提供了在企业环境中充分利用 Java 语言所需的一切，同时支持 Groovy 和 Kotlin 作为 JVM 上的替代语言，并具备根据应用需求构建多种架构的灵活性。从 `Spring Framework 6.0` 开始，Spring 要求使用 `Java 17` 及以上版本。

Spring 支持广泛的应用场景。在大型企业中，应用程序通常需要长期运行，并且可能依赖于开发者无法控制升级周期的 JDK 和应用服务器。也有一些应用程序可能以嵌入服务器的单个 jar 包形式运行，甚至部署在云环境中。还有一些可能是独立运行的应用程序（例如批处理或集成任务），无需依赖服务器。

Spring 是开源的。它拥有一个庞大且活跃的社区，基于各种真实场景提供持续的反馈。这使得 Spring 能够在长时间内成功地不断发展。

## 我们所说的“Spring”是什么意思

`Spring`一词在不同的语境中有不同的含义。它可以指代最初的 `Spring Framework 项目`，这是整个故事的起点。随着时间推移，其他 Spring 项目逐渐基于 Spring Framework 构建而成。大多数情况下，当人们提到`Spring`时，他们指的是`整个项目家族`。而本参考文档则专注于基础部分：`Spring Framework 本身`。

Spring 框架被划分为多个模块，应用程序可以根据需要选择所需的模块。核心部分是核心容器模块，包括`配置模型`和`依赖注入机制`。除此之外，Spring 框架还为不同的应用架构提供了基础支持，包括`消息传递`、`事务数据与持久化`以及 `Web 应用`。此外，它还包含基于 Servlet 的 `Spring MVC Web 框架`，以及并行的 `Spring WebFlux` 响应式 Web 框架。

关于模块的说明：Spring Framework 的 jar 文件支持部署到模块路径（Java 模块系统）。在启用模块的应用程序中使用时，Spring Framework 的 jar 文件带有 `Automatic-Module-Name` 清单条目，这些条目定义了稳定的语言级模块名称（如 `spring.core`、`spring.context` 等），与 jar 文件的具体名称无关。这些 jar 文件遵循相同的命名模式，只是将 `.` 替换为 `-`，例如 `spring-core` 和 `spring-context`。当然，Spring Framework 的 jar 文件在类路径上也可以正常工作。

## Spring 和 Spring 框架的历史

Spring 于 2003 年诞生，旨在应对早期 J2EE 规范的复杂性。尽管有人认为 Java EE 及其现代继任者 Jakarta EE 与 Spring 存在竞争关系，但实际上它们是互为补充的。Spring 编程模型并未完全采用 Jakarta EE 平台规范，而是选择性地与传统 EE 体系中的部分规范进行集成：

- Servlet API (JSR 340)
- WebSocket API (JSR 356)
- Concurrency Utilities (JSR 236)
- JSON Binding API (JSR 367)
- Bean Validation (JSR 303)
- JPA (JSR 338)
- JMS (JSR 914)
- 以及 JTA/JCA 设置以进行事务协调（如有必要）

Spring 框架同样支持`依赖注入`（JSR 330）和`通用注解`（JSR 250）规范，应用开发者可以选择使用这些规范，而不是依赖于 Spring 框架提供的特定机制。这些规范最初是基于常见的 `javax` 包开发的。

自 `Spring Framework 6.0` 起，Spring 已升级至 `Jakarta EE 9` 级别（例如，`Servlet 5.0+`、`JPA 3.0+`），基于 `jakarta` 命名空间，而非传统的 `javax 包`。在以 EE 9 为最低要求并已支持 EE 10 的基础上，Spring 已准备好为 Jakarta EE API 的进一步发展提供开箱即用的支持。`Spring Framework 6.0` 完全兼容 `Tomcat 10.1`、`Jetty 11` 和 `Undertow 2.3` 作为 Web 服务器，同时也兼容 `Hibernate ORM 6.1`。

随着时间的推移，Java/Jakarta EE 在应用开发中的角色不断演变。在 J2EE 和 Spring 的早期阶段，应用程序通常被设计为部署到应用服务器上。而如今，在 Spring Boot 的帮助下，应用程序以更适合 DevOps 和云环境的方式构建，嵌入式的 Servlet 容器既方便使用又易于更换。从 Spring Framework 5 开始，WebFlux 应用甚至不再直接使用 Servlet API，并且可以运行在非 Servlet 容器的服务器（如 Netty）上。

Spring框架（Spring Framework）不断创新和发展。除了 Spring Framework 之外，还有其他项目，例如 `Spring Boot`、`Spring Security`、`Spring Data`、`Spring Cloud`、`Spring Batch` 等等。需要注意的是，每个项目都有其独立的源码库、问题跟踪系统和发布节奏。完整的 Spring 项目列表请访问 [spring.io/projects](https://spring.io/projects)。

## 设计理念

当你学习一个框架时，不仅需要了解它的功能，还要明白它遵循的原则。以下是 Spring 框架的指导原则：

- `在每个层面提供选择`。Spring 允许您尽可能推迟设计决策。例如，您可以通过配置切换持久化提供程序，而无需更改代码。对于许多其他基础设施问题以及与第三方 API 的集成也是如此。
- `包容多样化的视角`。Spring 拥抱灵活性，对如何完成任务并不持强硬立场。它支持从不同视角出发的各种应用需求。
- `保持强大的向后兼容性`。Spring 的演进经过精心管理，在版本之间尽量减少破坏性更改。Spring 支持经过精挑细选的 JDK 版本和第三方库，以便于维护依赖于 Spring 的应用程序和库。
- `注重 API 设计`。Spring 团队投入了大量精力和时间，致力于设计直观且能够经受多个版本和多年考验的 API。
- `设定高标准的代码质量`。Spring Framework 非常注重有意义、最新且准确的 javadoc。它是少数几个能够声称代码结构清晰且包之间无循环依赖的项目之一。

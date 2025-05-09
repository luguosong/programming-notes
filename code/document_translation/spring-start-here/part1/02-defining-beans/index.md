# 定义Bean

本章内容包括：

- 理解 Spring 上下文的必要性
- 向 Spring 上下文中添加新的对象实例

在本章中，你将开始学习如何使用 Spring 框架中的一个核心元素：`上下文(context)` （在 Spring 应用中也称为
`应用上下文,application context`）。你可以把上下文想象成应用内存中的一个空间，我们会把希望由框架管理的所有对象实例都放到这里。默认情况下，Spring
并不了解你在应用中定义的任何对象。为了让 Spring 能够识别你的对象，你需要把它们添加到上下文中。在本书后续内容中，我们还会讨论如何在应用中利用
Spring 提供的各种功能。你会发现，集成这些功能的方式，就是通过上下文添加对象实例，并建立它们之间的关系。Spring
会利用上下文中的这些实例，将你的应用与它所提供的各种功能连接起来。在本书中，你还会逐步学习到 Spring 最重要的一些特性（比如
`事务`、`测试`等）的基础知识。

了解什么是 Spring 上下文以及它的工作原理，是学习使用 Spring 的第一步。因为如果不了解如何管理 Spring
上下文，几乎无法实现你之后要学的任何功能。Spring 上下文是一个复杂的机制，它让 Spring 能够管理你定义的实例。通过这种方式，你才能充分利用框架所提供的各种能力。

本章我们将从学习`如何将对象实例添加到 Spring 上下文`开始。在第三章，你会学到`如何引用你添加的实例，并在它们之间建立关系`。

我们把这些对象实例称为`bean`。当然，为了让你掌握所需的语法，我们会编写代码片段，这些代码片段都可以在本书配套的项目中找到（你可以在
`Book resources章节`下载这些项目）。我还会通过可视化和详细的讲解来丰富代码示例。

为了让你能够循序渐进地学习 Spring，本章我们只关注操作 Spring 上下文所需的语法。你之后会发现，并不是应用中的所有对象都需要由
Spring 管理，所以你也不必把应用里的所有对象实例都添加到 Spring 上下文中。现在，请你专注于学习如何将一个实例交给 Spring 来管理。

## 创建Maven项目

本节将介绍如何创建一个 Maven 项目。虽然 Maven 并不是 Spring
直接相关的话题，但它是一款能够简化应用构建流程的工具，无论你使用哪种开发框架都可以用它来高效管理项目构建。为了更好地理解后续的代码示例，你需要掌握
Maven 项目的基础知识。在实际开发中，Maven 也是 Spring 项目最常用的构建工具之一（另一个常用的构建工具是 Gradle，不过本书不做讨论）。由于
Maven 十分流行，你可能已经了解如何通过配置文件创建项目并添加依赖。如果你已经熟悉这些内容，可以跳过本节，直接阅读第 2.2 节。

构建工具是一种帮助我们更轻松地构建应用程序的软件。你可以通过配置构建工具，让它自动完成构建应用过程中涉及的各项任务，而无需手动操作。通常，构建应用时常见的任务包括：

- 下载应用所需的依赖项
- 运行测试
- 校验代码语法是否符合你设定的规范
- 检查安全漏洞
- 编译应用程序
- 将应用打包成可执行的归档文件

为了让我们的示例能够方便地管理依赖项，我们需要为开发的项目使用构建工具。本节只讲解开发本书示例所需了解的内容；我们将一步步演示如何创建一个
Maven 项目，并为你讲解其结构的基本要点。如果你想深入了解 Maven 的更多细节，推荐阅读 Balaji Varanasi 所著的
`《Introducing Maven: A Build Tool for Today’s Java Developers》（APress, 2019）`。

让我们从最基础的部分开始。首先，就像开发其他应用程序一样，你需要一个集成开发环境（IDE）。现在任何专业的IDE都支持Maven项目，所以你可以随意选择：IntelliJ
IDEA、Eclipse、Spring STS、Netbeans等都可以。本书中我使用的是IntelliJ IDEA，因为这是我最常用的IDE。别担心，无论你选择哪款IDE，Maven项目的结构都是一样的。

让我们先创建一个新项目。在 IntelliJ 中，你可以通过`文件>新建>项目`来新建一个项目。这样会弹出一个类似于图 2.1 所示的窗口。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202504231110068.png){ loading=lazy }
  <figcaption>图2.1 创建新的Maven项目。依次点击 File > New > Project 后，会进入这个窗口。在左侧面板中选择项目类型，这里我们选择 Maven。在窗口上方，可以选择用于编译和运行项目的 JDK 版本。</figcaption>
</figure>

在选择好项目类型后，在接下来的窗口（如图2.2所示），你需要为项目命名。除了填写项目名称和选择存储位置之外，对于Maven项目，你还可以指定以下内容：

- `Group ID`：用于将多个相关项目进行分组
- `Artifact ID`：当前应用的名称
- `Version`：当前实现状态的标识符

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202504231116906.png){ loading=lazy }
  <figcaption>如图2.2所示，在完成项目创建之前，你需要为项目命名，并指定IDE存储项目的位置。你还可以选择为项目设置组ID、Artifact ID和版本号。最后，点击右下角的“完成”按钮，即可完成项目的创建。</figcaption>
</figure>

在实际应用开发中，这三个属性都是非常重要的细节，建议务必填写。但在我们的例子中，因为只是做理论演示，你可以省略这些内容，让 IDE
自动为这些属性填充默认值。

项目创建完成后，你会发现它的结构如图 2.3 所示。需要注意的是，Maven 的项目结构与所选用的 IDE 无关，无论你用哪个开发工具，结构都是一样的。

初次查看项目时，你会注意到两个主要部分：

- `src 文件夹`（也叫源代码文件夹），你所有与应用相关的内容都放在这里；
- `pom.xml 文件`，你可以在这里配置 Maven 项目，比如添加新的依赖项。

Maven 会将 `src 文件夹`进一步划分为以下几个子文件夹：

- `main 文件夹`，用于存放应用的源代码。这个文件夹下又分为 `java` 和 `resources` 两个子文件夹，分别用于存放 Java 代码和配置文件；
- `test 文件夹`，用于存放单元测试的源代码（关于单元测试及其定义方法，我们会在第 15 章详细介绍）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202504231121581.png){ loading=lazy }
  <figcaption>图2.3 展示了一个 Maven 项目的组织结构。在 src 文件夹下，我们会添加所有属于应用程序的内容：应用的源代码放在 main 文件夹中，单元测试的源代码则放在 test 文件夹中。pom.xml 文件用于编写 Maven 项目的相关配置（在我们的示例中，主要用于定义依赖项）。</figcaption>
</figure>

图2.4展示了如何将新的源代码添加到Maven项目的“main/java”文件夹中。应用程序的新类都放在这个文件夹下。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202504231123179.png){ loading=lazy }
  <figcaption>图2.4 在“java”文件夹中，你可以像往常一样创建应用程序所需的 Java 包和类。这些类负责实现整个应用的业务逻辑，并且会用到你所引入的依赖库。</figcaption>
</figure>

在本书中我们创建的项目里，会用到许多外部依赖，也就是我们用来实现示例功能的各种库或框架。要将这些依赖添加到你的 Maven
项目中，需要修改 pom.xml 文件的内容。下面的代码清单展示了新建 Maven 项目后，pom.xml 文件的默认内容。

```xml title="清单 2.1 pom.xml 文件的默认内容"
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>org.example</groupId>
    <artifactId>sq-ch2-ex1</artifactId>
    <version>1.0-SNAPSHOT</version>

</project>
```

在这个 pom.xml 文件中，项目没有使用任何外部依赖。如果你查看项目的外部依赖文件夹，你应该只能看到 JDK（见图 2.5）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202504231128418.png){ loading=lazy }
  <figcaption>图2.5 默认情况下，pom.xml 文件只会将 JDK 作为项目的外部依赖。你之所以需要修改 pom.xml 文件（本书也会这样做），其中一个原因就是为了添加应用所需的新依赖项。</figcaption>
</figure>

下面的示例展示了如何为你的项目添加外部依赖。你需要在 `<dependencies> </dependencies>` 标签之间编写所有依赖项。每个依赖项都用一组
`<dependency> </dependency>` 标签来表示，在其中填写该依赖的属性：包括依赖的 `group ID`、`artifact 名称`和`版本号`。Maven
会根据你提供的这三个属性的值来查找依赖，并从仓库中下载所需的依赖。我这里不会详细介绍如何配置自定义仓库，你只需要知道，Maven
默认会从名为 Maven Central 的仓库下载依赖（通常是 jar 文件）。下载好的 jar 文件可以在你项目的 `External Dependencies 文件夹`
中找到，如图 2.6 所示。

``` xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>org.example</groupId>
    <artifactId>sq_ch2_ex1</artifactId>
    <version>1.0-SNAPSHOT</version>
	<!--你需要在 <dependencies> 和 </dependencies> 标签之间填写项目的依赖项。-->
    <dependencies>
		<!--一个依赖项由一组 <dependency> </dependency> 标签表示。-->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-jdbc</artifactId>
            <version>5.2.6.RELEASE</version>
        </dependency>
    </dependencies>
</project>
```

在你按照前面的示例在 pom.xml 文件中添加依赖后，IDE 会自动下载这些依赖项，你现在可以在`External Libraries（外部库）`
文件夹中找到它们（见图 2.6）。

现在我们可以进入下一部分，来讨论 Spring 上下文的基础知识。你将会创建 Maven 项目，并学习如何使用名为 spring-context 的
Spring 依赖来管理 Spring 上下文。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202504231133679.png){ loading=lazy }
  <figcaption>图2.6 当你在 pom.xml 文件中添加新的依赖项时，Maven 会自动下载对应的 jar 文件。这些 jar 文件可以在项目的 External Libraries 文件夹中找到。</figcaption>
</figure>

## 向Spring容器中添加新Bean

在本节中，你将学习如何向 Spring 容器中添加新的对象实例（即 Bean）。你会发现，有多种方式可以将 Bean 添加到 Spring
容器中，从而让Spring 管理它们，并将其提供的各种功能集成到你的应用中。根据具体需求，你可以选择不同的方式来添加Bean；我们也会讨论在什么情况下该选择哪种方式。你可以通过以下几种方式将
Bean 添加到容器中（本章后面会详细介绍）：

- 使用 `@Bean 注解`
- 使用`组件注解`（如 @Component 等）
- 通过`编程方式(Programmatically)`添加

我们先创建一个`没有依赖任何框架的项目`——甚至不包括 Spring。接下来，我们会添加使用 `Spring 上下文所需的依赖`，并创建 Spring
上下文（见图 2.7）。这个示例将作为后续 2.2.1 到 2.2.3 小节中，向 Spring 上下文中添加 bean 的前置准备。

我们创建了一个 Maven 项目，并定义了一个类。因为想象起来挺有趣的，这里我用 Parrot（鹦鹉）作为类名，这个类只有一个字符串属性，用来表示鹦鹉的名字（见代码清单
2.3）。请记住，在本章中，我们只关注如何将 bean 添加到 Spring 容器中，所以你可以随意使用任何有助于你记忆语法的对象。你可以在项目
`sq-ch2-ex1` 中找到本例的代码（你可以在本书“资源”部分下载这些项目）。在你的项目中，你可以用相同的名字，也可以选择你喜欢的名字。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202504232156965.png){ loading=lazy }
  <figcaption>图2.7 首先，我们创建一个对象实例和一个空的Spring上下文。</figcaption>
</figure>

```java title="代码清单 2.3 鹦鹉类"
public class Parrot {
	private String name;
// 省略了 getter 和 setter
}
```

```java title="代码清单 2.4 创建 Parrot 类的一个实例"
public class Main {
	public static void main(String[] args) {
		Parrot p = new Parrot();
	}
}
```

现在，我们需要为项目添加所需的依赖项。由于我们使用的是 Maven，因此我会在 pom.xml 文件中添加这些依赖，具体如下所示。

```xml title="代码清单 2.5 添加 Spring Context 依赖"

<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>org.example</groupId>
    <artifactId>sq-ch2-ex1</artifactId>
    <version>1.0-SNAPSHOT</version>
    <dependencies>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
            <version>5.2.6.RELEASE</version>
        </dependency>
    </dependencies>
</project>
```

需要特别注意的一点是，Spring 的设计本身就是`模块化`的。所谓模块化，就是说当你在项目中使用 Spring 生态系统中的某个功能时，并不需要把整个
Spring 框架都引入进来，只需添加你实际用到的部分。因此，在代码清单 2.5 中，你会看到我只添加了 `spring-context 依赖`，这样
Maven 就会自动帮我们拉取使用 Spring 上下文所需的相关依赖。在本书的后续内容中，我们会根据实际实现的功能，陆续为项目添加不同的依赖，但始终只会引入真正需要的部分。

!!! note

	你可能会好奇我是怎么知道该添加哪个 Maven 依赖的。其实，这些依赖我用得太多了，早就烂熟于心。不过，你完全没必要去死记硬背。每当你在做新的 Spring 项目时，可以直接在 Spring 官方文档（https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/core.html）里查找需要添加的依赖。一般来说，Spring 的依赖都属于 org.springframework 这个 group ID。

在将依赖添加到项目后，我们就可以创建一个 Spring 上下文实例了。接下来你可以看到，我是如何修改 main 方法来创建 Spring 上下文实例的。

```java title="代码清单 2.6 创建 Spring 上下文实例"
public class Main {
	public static void main(String[] args) {
		//创建一个 Spring 上下文实例
		var context = new AnnotationConfigApplicationContext();
		Parrot p = new Parrot();
	}
}
```

!!! note

	我们使用 `AnnotationConfigApplicationContext 类`来创建 Spring 上下文实例。Spring 提供了多种实现方式，但在大多数情况下，你会用到 `AnnotationConfigApplicationContext` 这个类（它采用了当前最常用的注解方式）。因此，本书将重点介绍这种实现方式。同时，我只会讲解当前讨论所需了解的内容。如果你刚开始接触 Spring，我建议你不要过多关注上下文实现的细节以及这些类的继承关系。因为一旦陷入这些细枝末节，你很可能会迷失在不重要的细节中，而忽略了真正关键的内容。

如图2.8所示，你已经创建了一个 Parrot 实例，将 Spring 上下文的依赖添加到了项目中，并且创建了 Spring 上下文的实例。接下来的目标是将
Parrot 对象添加到上下文中。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202504232307244.png){ loading=lazy }
  <figcaption>图 2.8 你已经创建了 Spring 上下文实例和一个 Parrot 实例。现在，你希望将这个 Parrot 实例添加到 Spring 上下文中，让 Spring 能够识别并管理它。</figcaption>
</figure>

我们刚刚完成了前置（骨架）项目的创建，接下来将在后续章节中用它来学习如何将 bean 添加到 Spring 容器中。在第 2.2.1
节，我们会继续学习如何通过 @Bean 注解将实例添加到 Spring 容器。此外，在第 2.2.2 和 2.2.3
节，你还会了解使用注解（如组件注解）和编程方式添加实例的其他方法。在介绍完这三种方式后，我们会对它们进行对比，并帮助你了解在什么情况下选择哪种方式最为合适。

### 使用@Bean注解向Spring容器中添加Bean

在本节中，我们将讨论如何使用 `@Bean 注解`将对象实例添加到 Spring 容器中。通过这种方式，你不仅可以将自己项目中定义的类（比如我们这里的
Parrot）实例加入到 Spring 容器，还可以添加那些你没有自己编写但在应用中需要用到的类的实例。我认为，对于刚开始学习的人来说，这种方式是最容易理解的。请记住，学习如何向
Spring 容器中添加 Bean 的原因在于，Spring 只能管理属于它容器中的对象。

首先，我会给你一个简单的例子，演示如何使用 @Bean 注解将一个 Bean 添加到 Spring 容器中。接下来，我还会展示如何添加多个相同类型或不同类型的
Bean。

将一个 bean 添加到 Spring 容器中并使用 @Bean 注解，具体步骤如下（见图 2.9）：

1. 定义一个`配置类`（使用 `@Configuration 注解`），这个类用于配置 Spring 的上下文，后面我们会详细介绍如何使用它来进行相关配置。
2. 在配置类中添加一个`方法`，该方法返回你希望加入到 Spring 容器中的对象实例，并使用 `@Bean 注解`标注这个方法。
3. 让 Spring 加载你在第一步中定义的`配置类`。后续你会了解到，我们可以通过配置类为框架编写不同的配置方案。

让我们按照这些步骤操作，并将其应用到名为`sq-c2-ex2`的项目中。为了便于区分我们讨论的每个步骤，建议你为每个示例都新建一个项目。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202504240944136.png){ loading=lazy }
  <figcaption>图2.9 使用 @Bean 注解将 bean 添加到上下文的步骤。通过将实例加入到 Spring 上下文中，框架便能够识别并管理该对象。</figcaption>
</figure>

!!! note

	配置类是 Spring 应用中的一种特殊类，我们用它来指示 Spring 执行特定的操作。例如，我们可以让 Spring 创建 bean，或者启用某些功能。在本书接下来的内容中，你将学习到在配置类中可以定义的各种内容。

#### 第1步：在项目中定义一个配置类

首先，需要在项目中创建一个`配置类`。Spring 的配置类通常使用 `@Configuration 注解`进行标识。我们通过配置类来为项目定义各种与
Spring 相关的配置。在本书的后续章节中，你会学到如何通过配置类进行不同的配置。现在，我们只关注如何向 Spring
容器中添加新的实例。下面的代码展示了如何定义一个配置类。我将这个配置类命名为 `ProjectConfig`。

```java title="代码清单 2.7 为项目定义一个配置类"
//我们使用 @Configuration 注解将该类定义为一个 Spring 配置类。
@Configuration
public class ProjectConfig {
}
```

!!! note

	我会把不同的类分别放在不同的包里，这样代码结构会更清晰易懂。比如，我会把配置类放在名为 `config` 的包中，而把主类放在 main 包里。将类有条理地归类到不同的包中是一种很好的编程习惯，我也建议你在实际开发中遵循这种做法。

#### 第2步：创建一个返回Bean的方法，并用@Bean注解该方法

在`配置类`中，你可以做的一件事就是`向 Spring 容器中添加 Bean`。为此，我们需要定义一个`方法`，该方法返回我们希望加入容器的对象实例，并用
`@Bean` 注解这个方法。这样，Spring 在初始化容器时就会调用这个方法，并将返回的对象加入到容器中。下面的代码展示了为实现当前步骤，对配置类所做的修改。

!!! note

	在本书的项目中，我使用的是 Java 11，这是目前最新的长期支持版本。现在，越来越多的项目都在采用这个版本。通常来说，我在代码示例中用到的、在早期 Java 版本中无法使用的唯一特性，就是 var 这个保留类型名。我偶尔会用 var，让代码更简洁、更易读。如果你想用早期的 Java 版本（比如 Java 8），只需要把 var 替换成对应的类型即可。这样一来，这些项目同样可以在 Java 8 上运行。

```java title="代码清单 2.8 定义 @Bean 方法"

@Configuration
public class ProjectConfig {
	//	通过添加 @Bean 注解，
//	我们指示 Spring 在初始化上下文时调用该方法，
//	并将其返回值添加到上下文中。
	@Bean
	Parrot parrot() {
		var p = new Parrot();
		//为我们稍后测试应用时要用的鹦鹉设置一个名字。
		p.setName("Koko");
		//Spring 会将该方法返回的 Parrot 实例添加到其上下文中。
		return p;
	}
}
```

请注意，我为这个方法起的名字里并没有用到动词。你可能学过，Java 的最佳实践之一是方法名要包含动词，因为方法通常表示某种操作。但在
Spring 容器中用于添加 bean 的方法，我们并不遵循这个惯例。这类方法其实代表了它们返回的对象实例，这些实例会成为 Spring
容器的一部分。方法名也会成为 bean 的名字（比如在代码清单 2.8 中，bean 的名字就是 “parrot”）。按照惯例，这里可以用名词，而且通常会和类名保持一致。

#### 第3步：让Spring在初始化上下文时使用新创建的配置类

我们已经实现了一个`配置类`
，并在其中告知Spring需要将哪个对象实例注册为bean。现在，我们需要确保Spring在初始化上下文时会使用这个配置类。下面的代码演示了如何在主类中修改Spring上下文的实例化方式，以便使用我们在前两步中实现的
`配置类`。

```java title="代码清单 2.9 基于已定义的配置类初始化 Spring 上下文"
public class Main {
	public static void main(String[] args) {
		//在创建 Spring 上下文实例时，将配置类作为参数传递，以指示 Spring 使用该配置。
		var context = new AnnotationConfigApplicationContext(ProjectConfig.class);
	}
}
```

要确认 Parrot 实例已经成功加入到上下文中，你可以引用该实例，并在控制台打印它的名称，如下所示。

```java title="代码清单 2.10 在上下文中引用 Parrot 实例"
public class Main {
	public static void main(String[] args) {
		var context = new AnnotationConfigApplicationContext(ProjectConfig.class);
		//从 Spring 容器中获取一个 Parrot 类型的 bean 实例
		Parrot p = context.getBean(Parrot.class);
		System.out.println(p.getName());
	}
}
```

现在你会在控制台看到你在上下文中为鹦鹉取的名字，在我的例子里是 Koko。

!!! note

	在实际应用中，我们通常会通过单元测试和集成测试来验证我们的实现是否符合预期。本书中的各个项目都实现了单元测试，用以验证所讨论的行为。由于这是一本“入门”书籍，你可能还不太了解单元测试。为了避免引起混淆，并让你专注于当前讨论的主题，我们会等到第15章才详细介绍单元测试。不过，如果你已经会编写单元测试，并且阅读相关内容有助于你更好地理解本书内容，你可以在每个 Maven 项目的 test 文件夹中找到所有实现的单元测试。如果你还不了解单元测试的工作原理，建议你先专注于当前讨论的主题。

和前面的例子一样，你可以向 Spring 容器中添加任何类型的对象（见图 2.10）。我们也可以添加一个字符串和一个整数，来验证它们是否能够正常工作。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202504241008167.png){ loading=lazy }
  <figcaption>图2.10 你可以将任何对象添加到Spring上下文中，让Spring能够识别它。</figcaption>
</figure>

下面的代码示例展示了我是如何修改配置类，以同时添加一个 String 类型的 bean 和一个 Integer 类型的 bean。

```java title="代码清单 2.11 向上下文中添加另外两个 Bean"

@Configuration
public class ProjectConfig {
	@Bean
	Parrot parrot() {
		var p = new Parrot();
		p.setName("Koko");
		return p;
	}

	//将字符串“Hello”添加到 Spring 上下文中
	@Bean
	String hello() {
		return "Hello";
	}

	//将整数 10 添加到 Spring 上下文中
	@Bean
	Integer ten() {
		return 10;
	}
}
```

!!! note

	请记住 Spring 容器的作用：我们只将那些希望由 Spring 管理的实例添加进去（这样才能接入框架所提供的各种功能）。在实际应用中，我们不会把所有对象都放进 Spring 容器。从第4章开始，随着示例代码逐渐接近生产环境中的实际应用，我们也会更加关注哪些对象需要交由 Spring 管理。现在，请你重点了解有哪些方式可以将 bean 添加到 Spring 容器中。

现在，你可以像之前引用鹦鹉那样，引用这两个新 bean。下面的代码示例展示了如何修改 main 方法，以打印出这两个新 bean 的值。

```java title="代码清单 2.12 在控制台打印这两个新 Bean"
public class Main {
	public static void main(String[] args) {
		var context = new AnnotationConfigApplicationContext(
				ProjectConfig.class);
		// 你不需要进行任何显式类型转换。
		// Spring 会在其上下文中查找你所请求类型的 bean。
		// 如果找不到这样的 bean，Spring 就会抛出异常。
		Parrot p = context.getBean(Parrot.class);
		System.out.println(p.getName());
		String s = context.getBean(String.class);
		System.out.println(s);
		Integer n = context.getBean(Integer.class);
		System.out.println(n);
	}
}
```

现在运行应用程序时，控制台会打印出这三个 Bean 的值，如下一个代码片段所示。

```shell
Koko
Hello
10
```

到目前为止，我们已经向 Spring 容器中添加了一个或多个不同类型的 bean。那么，我们能否向 Spring 容器中添加`多个相同类型的对象`
（见图 2.11）？如果可以，我们又该`如何单独引用这些对象`呢？接下来，我们将创建一个新项目`sq-ch2-ex3`，来演示如何向 Spring
容器中添加多个相同类型的 bean，以及之后如何引用它们。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202504241029895.png){ loading=lazy }
  <figcaption>图2.11 你可以通过编写多个带有 @Bean 注解的方法，将同类型的 bean 添加到 Spring 容器中。每个实例都会拥有唯一的标识符。之后如果需要引用这些 bean，就需要使用它们各自的标识符。</figcaption>
</figure>

!!! note

	不要把 `bean 的名字`和`鹦鹉的名字`混淆。在我们的例子中，Spring 容器中 `bean 的名字（或标识符）`是 parrot1、parrot2 和 parrot3（和定义它们的 @Bean 方法名一样）。我给这些`鹦鹉起的名字`分别是 Koko、Miki 和 Riki。鹦鹉的名字只是 Parrot 对象的一个属性，对 Spring 来说并没有任何特殊含义。

只需在配置类中声明更多带有 @Bean 注解的方法，你就可以根据需要声明任意数量的同类型实例。下面的代码示例展示了如何在配置类中声明三个
Parrot 类型的 bean。你可以在项目 `sq-ch2-ex3` 中找到这个例子。

```java title="清单 2.13 向 Spring 容器中添加多个相同类型的 Bean"

@Configuration
public class ProjectConfig {
	@Bean
	Parrot parrot1() {
		var p = new Parrot();
		p.setName("Koko");
		return p;
	}

	@Bean
	Parrot parrot2() {
		var p = new Parrot();
		p.setName("Miki");
		return p;
	}

	@Bean
	Parrot parrot3() {
		var p = new Parrot();
		p.setName("Riki");
		return p;
	}
}

```

当然，仅仅通过指定类型，你已经无法再从上下文中获取 bean 了。如果你这么做，Spring
会抛出一个异常，因为它无法判断你到底想引用哪一个实例。请看下面的代码示例。运行这样的代码会抛出异常，Spring
会提示你需要明确指定你想使用的是哪个实例。

```java title="代码清单 2.14 通过类型引用 Parrot 实例"
public class Main {
	public static void main(String[] args) {
		var context = new AnnotationConfigApplicationContext(ProjectConfig.class);
		//❌你会在这一行遇到异常，
		// 因为 Spring 无法判断你指的是哪一个 Parrot 实例（共有三个）。
		Parrot p = context.getBean(Parrot.class);
		System.out.println(p.getName());
	}
}
```

在运行你的应用程序时，你会遇到类似于下方代码片段所示的异常。

```shell
Exception in thread "main"
org.springframework.beans.factory.NoUniqueBeanDefinitionException: No
qualifying bean of type 'main.Parrot' available: expected single matching
bean but found 3:
	parrot1,parrot2,parrot3
	at …
```

为了解决这个歧义问题，你需要通过指定 `bean 的名称`来精确引用其中一个实例。默认情况下，Spring 会将带有 @Bean 注解的`方法名`
作为 bean 的名称。记住，这也是我们不使用动词来命名 @Bean 方法的原因。在我们的例子中，这些 bean 的名称分别是 parrot1、parrot2
和 parrot3（记住，方法就代表了 bean）。你可以在前面的代码片段中异常信息里找到这些名称，你注意到了吗？现在，让我们修改 main
方法，通过名称显式引用其中一个 bean。请看下面的代码示例，我是如何引用 parrot2 这个 bean 的。

```java
public class Main {
	public static void main(String[] args) {
		var context = new AnnotationConfigApplicationContext(ProjectConfig.class);
		//第一个参数是我们所指实例的名称。
		Parrot p = context.getBean("parrot2", Parrot.class);
		System.out.println(p.getName());
	}
}

```

现在运行这个应用，你不会再遇到异常了。相反，你会在控制台看到第二只鹦鹉的名字：Miki。

如果你想为这个 bean 取一个别的名字，可以使用 @Bean 注解的 `name 或 value 属性`。下面的任意一种写法都可以将该 bean 在 "
miki" 中的名称更改为你指定的名字：

``` java
@Bean(name = "miki")
@Bean(value = "miki")
@Bean("miki")
```

在接下来的代码片段中，你可以看到代码中的变化。如果你想运行这个示例，可以在名为`sq-ch2-ex4`的项目中找到它。

```java
// 设置Bean的名字
@Bean(name = "miki")
Parrot parrot2() {
	var p = new Parrot();
	// 设置鹦鹉的名字
	p.setName("Miki");
	return p;
}
```

!!! note "primary bean"

	在前面的部分我们提到过，在 Spring 容器中可以存在多个同类型的 bean，但你需要通过它们的名字来引用它们。其实还有另一种方式可以在有多个同类型 bean 时引用它们。

	当你在 Spring 容器中有多个同类型的 bean 时，可以将其中一个设置为`主 bean(primary bean)`。你只需要在想要设为主 bean 的地方加上 @Primary 注解即可。如果有多个可选 bean，而你又没有指定名字，Spring 就会选择主 bean，也就是说，主 bean 就是 Spring 默认会选用的那个。下面的代码片段展示了如何用 @Primary 注解标记 @Bean 方法：

	``` java
	@Bean
	@Primary
	Parrot parrot2() {
		var p = new Parrot();
		p.setName("Miki");
		return p;
	}
	
	```

	如果你在引用 Parrot 时没有指定名称，Spring 现在会默认选择 Miki。当然，你只能将某一类型的 bean 定义为 primary。你可以在项目`sq-ch2-ex5`中找到这个示例的实现。

### 使用stereotype 注解将 Bean 添加到 Spring 容器中

在本节中，你将学习另一种将 Bean 添加到 Spring 容器的方法（本章后面我们还会对这些方法进行比较，并讨论在什么情况下选择哪一种）。请记住，将
Bean 添加到 Spring 容器中至关重要，因为这是让 Spring 识别并管理你应用中对象实例的方式。Spring 提供了多种方式来将 Bean
加入其容器。在不同的场景下，你会发现某种方式比其他方式用起来更方便。例如，使用`构造型注解`时，你只需写更少的代码，就能让
Spring 将 Bean 加入到容器中。

稍后你会了解到，Spring 提供了多种类型的标注注解。不过在本节中，我希望你先专注于如何使用这类标注注解。我们将以最基础的
`@Component 注解`为例，通过它来演示相关用法。

通过使用`构造型注解（stereotype annotations）`，你只需在需要被Spring容器管理的`类上方添加相应注解`。这样做时，我们通常称为
`将该类标记为一个组件`。当应用程序创建Spring上下文时，Spring会自动为你标记为组件的类创建一个实例，并将其加入到上下文中。即使采用这种方式，我们仍然需要一个
`配置类`，用来告诉Spring去哪里查找带有构造型注解的类。此外，你也可以同时使用这两种方式（即结合使用 `@Bean 注解`和`构造型注解`
）；关于这类更复杂的用法，我们会在后续章节中详细讲解。

我们在这个过程中需要遵循的步骤如下（见图2.12）：

1. 使用 `@Component 注解`，标记那些你希望 Spring 自动添加到其上下文中的类（在我们的例子中是 Parrot 类）。
2. 在配置类上使用 `@ComponentScan 注解`，告诉 Spring 去哪里查找你标记过的类。

以 Parrot 类为例，我们可以通过在 Parrot 类上添加一个类似 `@Component` 的构件注解，将其实例加入到 Spring 的上下文中。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202504241118180.png){ loading=lazy }
  <figcaption>图2.12 使用构造型注解时，需要考虑两个步骤。首先，使用构造型注解（@Component）标注你希望Spring将其实例化为Bean的类。其次，使用@ComponentScan注解，告知Spring去哪里扫描带有构造型注解的类。</figcaption>
</figure>

下面的代码示例演示了如何在 Parrot 类中使用 @Component 注解。你可以在项目 “sq-ch2-ex6” 中找到这个示例。

```java title="代码清单 2.16 为 Parrot 类使用 stereotype 注解"
// 通过在类上使用 @Component 注解，
// 我们指示 Spring 创建该类的实例并将其添加到上下文中。
@Component
public class Parrot {
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
```

等等！这段代码现在还不能直接运行。默认情况下，Spring 并不会自动去查找那些带有构造型注解的类，所以如果我们就这样写，Spring
并不会把 Parrot 类型的 bean 加入到它的上下文中。为了让 Spring 能够扫描带有构造型注解的类，我们需要在配置类上加上
`@ComponentScan 注解`。此外，通过 @ComponentScan 注解，我们还可以指定 Spring
应该去哪些包中查找这些类。我们需要把定义了构造型注解的类所在的包都列出来。下面的代码展示了如何在项目的配置类上使用
@ComponentScan 注解。在我的例子中，包名是 “main”。

```java

@Configuration
//通过注解的 basePackages 属性，我们可以指定 Spring 去哪里查找带有构造型注解的类。
@ComponentScan(basePackages = "main")
public class ProjectConfig {
}

```

现在你已经告诉了 Spring 以下两点：

1. 需要将哪些类的实例添加到它的上下文中（比如 Parrot）；
2. 通过 @ComponentScan 注解指定去哪里查找这些类。

!!! note

	我们现在不再需要通过方法来定义 bean 了。这样看起来更好，因为用更少的代码就能实现同样的功能。不过，先别急着下结论，等本章结束你会发现，这两种方式各有优势，具体用哪种还要看实际场景。

你可以按照下面的代码示例继续编写 main 方法，以验证 Spring 是否在其上下文中创建并添加了该 bean。

```java title="代码清单 2.18 定义 main 方法以测试 Spring 配置"
public class Main {
	public static void main(String[] args) {
		var context = new AnnotationConfigApplicationContext(ProjectConfig.class);
		Parrot p = context.getBean(Parrot.class);
		//打印从 Spring 上下文中获取的实例的默认字符串表示。
		System.out.println(p);
		//打印出 null，因为我们没有为 Spring 容器中添加的 parrot 实例指定任何名称。
		System.out.println(p.getName());
	}
}
```

运行这个应用程序时，你会发现 Spring 已经将一个 Parrot 实例添加到了它的上下文中，因为打印的第一个值就是该实例的默认字符串表示。然而，第二个打印出来的值是
null，因为我们并没有给这只鹦鹉分配任何名字。Spring 只是创建了这个类的实例，但如果我们想要对这个实例进行任何修改（比如给它起个名字），这仍然需要我们自己来完成。

现在我们已经介绍了将 Bean 添加到 Spring 容器中最常见的两种方式，接下来让我们简单对比一下它们（见表 2.1）。

你会发现，在实际开发中，你通常会尽量使用注解来标注组件（因为这样可以减少代码量），只有在无法通过注解方式注册 Bean 时才会用到
`@Bean`，比如你需要`为某个第三方库中的类创建 Bean`，但又无法修改这个类来添加注解。

表2.1 优缺点对比：两种将 Bean 添加到 Spring 上下文的方法比较，帮助你了解各自适用的场景:

| @Bean                                                                                                   | stereotype注解                                                                                                      |
|---------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------|
| 你可以完全掌控添加到 Spring 容器中的实例创建过程。你需要在带有 @Bean 注解的方法体内自行创建并配置该实例。Spring 只会将你创建的实例原样添加到容器中。                   | 你只能在框架创建实例之后才能对其实例进行控制。                                                                                           |
| 你可以通过这种方式向 Spring 容器中添加多个相同类型的实例。还记得在第 2.1.1 节中，我们向 Spring 容器中添加了三个 Parrot 实例。                          | 采用这种方式，你只能将该类的一个实例添加到上下文中。                                                                                        |
| 你可以使用 @Bean 注解将任何对象实例添加到 Spring 容器中。定义该实例的类不一定要在你的应用中定义。还记得之前我们把一个 String 和一个 Integer 也添加到了 Spring 容器中。 | 你只能用构造型注解为你自己应用程序拥有的类创建 bean。例如，你不能像在2.1.1节中用 @Bean 注解那样添加 String 或 Integer 类型的 bean，因为这些类并不属于你，无法通过添加构造型注解来修改它们。 |
| 你需要为每个要创建的 bean 单独编写一个方法，这会让你的应用增加一些模板代码。因此，在我们的项目中，@Bean 通常作为补充选项，而不是首选，优先考虑使用构造型注解                    | 使用构造型注解将 bean 添加到 Spring 上下文时，不会给你的应用增加样板代码。通常，对于属于你应用的类，你会更倾向于采用这种方式。                                            |

!!! note "使用@PostConstruct管理实例创建后的操作"

	正如我们在本节中讨论的，使用构造型注解可以让 Spring 创建一个 bean 并将其加入到上下文中。但与使用 @Bean
	注解不同，你无法完全掌控实例的创建过程。通过 @Bean 注解，我们可以为添加到 Spring 上下文中的每一个 Parrot 实例指定名称，而使用
	@Component 注解时，在 Spring 调用 Parrot 类的构造方法后，我们并没有机会再做额外的处理。如果我们希望在 Spring 创建 bean
	之后立即执行一些操作，该怎么办呢？这时就可以使用 @PostConstruct 注解。
	
	Spring 借用了 Java EE 的 `@PostConstruct 注解`。我们同样可以在 Spring Bean 中使用这个注解，来指定一组在 Bean 创建后由 Spring
	执行的操作。你只需要在组件类中定义一个方法，并用 @PostConstruct 注解标记该方法，Spring 就会在构造方法执行完毕后自动调用它。
	
	接下来，我们需要在 pom.xml 文件中添加使用 @PostConstruct 注解所需的 Maven 依赖：
	
	```xml
	
	<dependency>
	    <groupId>javax.annotation</groupId>
	    <artifactId>javax.annotation-api</artifactId>
	    <version>1.3.2</version>
	</dependency>
	```
	
	如果你使用的 Java 版本低于 Java 11，就不需要添加这个依赖。在 Java 11 之前，Java EE 相关的依赖是包含在 JDK 里的。从 Java 11
	开始，JDK 移除了与 SE 无关的 API，包括 Java EE 相关的依赖。
	
	如果你希望使用那些已被移除的 API 中的功能（比如 @PostConstruct），现在需要在你的应用中显式添加相关依赖。
	
	现在，您可以在 Parrot 类中定义一个方法，如下段代码所示：
	
	```java
	
	@Component
	public class Parrot {
		private String name;
	
		@PostConstruct
		public void init() {
			this.name = "Kiki";
		}
		// 省略的代码
	}
	```

	你可以在项目`sq-ch2-ex7`中找到这个示例。如果你现在在控制台打印鹦鹉的名字，你会看到应用程序在控制台输出了“Kiki”这个值。

	同样地，虽然在实际应用中不太常见，你也可以使用一个叫做 @PreDestroy 的注解。有了这个注解，你可以定义一个方法，Spring 会在关闭并清理上下文之前立即调用它。@PreDestroy 注解同样在 JSR-250 中有描述，并被 Spring 借鉴了。不过，我通常建议开发者避免使用它，最好用其他方式来实现在 Spring 清理上下文前需要执行的操作，主要原因是你不能保证 Spring 一定会成功清理上下文。比如说，如果你在 @PreDestroy 方法里处理了一些敏感操作（比如关闭数据库连接），但 Spring 没有调用这个方法，那你可能会遇到很大的麻烦。

### 以编程方式向Spring上下文中添加Bean

本节我们将讨论如何以编程方式向 Spring 上下文中添加 Bean。从 Spring 5 开始，我们就可以通过编程的方式将 Bean 添加到 Spring
上下文中，这为开发带来了极大的灵活性，因为你可以直接通过调用上下文实例的方法，将新的实例加入到上下文中。当你需要以自定义的方式向上下文中添加
Bean，而 @Bean 或其他注解（如组件注解）无法满足你的需求时，就可以采用这种方式。比如，你可能需要根据应用的特定配置，在 Spring
上下文中注册特定的 Bean。虽然通过 @Bean 和组件注解可以实现大多数场景，但有些需求却无法像下面代码片段那样实现：

``` java
if(condition){
// 如果条件为真，则向 Spring 上下文中添加一个特定的 bean。
registerBean(b1);
}else{
// 否则，请在 Spring 上下文中再添加一个 bean。
registerBean(b2);
}

```

以鹦鹉为例，场景如下：应用程序会读取一组鹦鹉，其中有些是绿色的，有些是橙色的。你希望应用程序只将绿色的鹦鹉添加到 Spring
上下文中（见图 2.13）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202504281115306.png){ loading=lazy }
  <figcaption>图2.13 通过 registerBean() 方法将特定对象实例添加到 Spring 容器</figcaption>
</figure>

让我们来看一下这个方法是如何运作的。要通过编程方式向 Spring 容器中添加一个 bean，只需调用 ApplicationContext 实例的
`registerBean()` 方法即可。`registerBean() 方法`有四个参数，具体如下代码片段所示：

```java
<T> void registerBean(
		String beanName,
		Class<T> beanClass,
		Supplier<T> supplier,
		BeanDefinitionCustomizer... customizers);
```

1. 第一个参数 beanName 用于为你在 Spring 容器中添加的 bean 指定一个`名称`。如果你不需要为这个 bean 命名，在调用方法时可以将该参数设置为
   null。
2. 第二个参数是你要添加到容器中的 bean 所对应的`类`。比如你想添加一个 Parrot 类的实例，这个参数就传 Parrot.class。
3. 第三个参数是一个 `Supplier 实例`。你需要实现这个 Supplier，使其返回你要添加到容器中的实例对象。请注意，Supplier 是
   java.util.function 包下的一个函数式接口，其作用就是在不接收任何参数的情况下返回一个你定义的值。
4. 第四个也是最后一个参数是 BeanDefinitionCustomizer 的可变参数（varargs）。如果你对这个接口不熟悉也没关系，BeanDefinitionCustomizer
   只是一个用来`配置 bean 不同属性的接口`，比如可以用来设置 bean 为主实例（primary）。由于它是可变参数类型，你可以完全省略这个参数，也可以传入一个或多个
   BeanDefinitionCustomizer 类型的值。

在项目`sq-ch2-ex8`中，你可以看到一个使用 registerBean() 方法的示例。你会注意到，这个项目的配置类是空的，而我们用来作为 bean
定义示例的 Parrot 类只是一个普通的 Java 对象（POJO），并没有使用任何注解。下面的代码片段展示了我为这个示例定义的配置类：

```java

@Configuration
public class ProjectConfig {
}
```

我定义了用于创建该 bean 的 Parrot 类：

```java
public class Parrot {
	private String name;
	// 省略了 getter 和 setter
}

```

在项目的主方法中，我使用了 registerBean() 方法将一个 Parrot 类型的实例添加到了 Spring 容器中。下面的代码展示了主方法的实现。图
2.14 重点展示了调用 registerBean() 方法的语法。

```java
public class Main {
	public static void main(String[] args) {
		var context =
				new AnnotationConfigApplicationContext(
						ProjectConfig.class);
		//我们创建了想要添加到 Spring 上下文中的实例。
		Parrot x = new Parrot();
		x.setName("Kiki");
		//我们定义了一个 Supplier 来返回该实例。
		Supplier<Parrot> parrotSupplier = () -> x;
		//我们调用 registerBean() 方法，将该实例添加到 Spring 容器中。
		context.registerBean("parrot1",
				Parrot.class, parrotSupplier);

		//为了验证该 bean 已经被加入到上下文中，
		// 我们可以引用 parrot bean，并在控制台打印它的名称。
		Parrot p = context.getBean(Parrot.class);
		System.out.println(p.getName());
	}
}
```

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202504281126389.png){ loading=lazy }
  <figcaption>图2.14 通过调用 registerBean() 方法以编程方式向 Spring 容器中添加 Bean</figcaption>
</figure>

在添加 bean 时，可以将一个或多个 bean 配置器实例作为最后的参数，用于设置不同的 bean 特性。例如，你可以通过修改
registerBean() 方法的调用方式，将某个 bean 设置为主 bean（primary），如下方代码片段所示。当上下文中存在多个同类型的 bean 时，主
bean 会成为 Spring 默认选择的实例：

``` java
context.registerBean("parrot1",
					 Parrot.class,
					 parrotSupplier,
					 bc -> bc.setPrimary(true));
```

你已经迈出了进入Spring世界的重要第一步。学会如何将bean添加到Spring上下文中，乍一看似乎没什么大不了，但其实比你想象的要重要得多。有了这项技能，你现在就可以继续学习如何在Spring上下文中引用这些bean了，这部分内容我们会在第三章详细讲解。

!!! note

    在本书中，我们只采用了现代的配置方式。不过，我认为你也有必要了解一下 Spring 框架早期是如何进行配置的。那时候，我们通常使用 XML 来编写这些配置。在附录 B 中，我们提供了一个简短的示例，让你感受一下如何通过 XML 向 Spring 容器中添加一个 bean。

## 总结

- 在学习 Spring 时，首先需要掌握的就是如何将对象实例（我们称之为 bean）添加到 Spring 容器中。你可以把 Spring 容器想象成一个桶，你希望
  Spring 能够管理哪些实例，就把它们放进这个桶里。Spring 只能“看到”你添加到其容器中的那些实例。
- 你可以通过三种方式将 bean 添加到 Spring 容器中：使用 @Bean 注解、使用构造型注解，以及通过编程方式添加。
	- 使用 @Bean 注解将实例添加到 Spring 容器时，你可以将任何类型的对象实例作为 bean 注入，甚至可以将同一类型的多个实例添加到
	  Spring 容器。从这个角度来看，这种方式比使用构造型注解更加灵活。不过，这种方式需要你为每个要添加到容器的独立实例，在配置类中单独编写一个方法，因此代码量会相对多一些。
	- 使用构造型注解（比如 @Component），你只能为带有特定注解的应用类创建
	  bean。这种配置方式需要编写的代码更少，使得配置更加简洁易读。对于你自己定义并可以添加注解的类，通常会优先选择这种方式，而不是
	  @Bean 注解。
	- 使用 registerBean() 方法，则可以实现自定义逻辑，将 bean 添加到 Spring 容器中。需要注意的是，这种方式仅适用于 Spring 5
	  及以上版本。

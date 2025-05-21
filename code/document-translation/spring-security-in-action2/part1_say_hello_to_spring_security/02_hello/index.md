# Spring Security 入门

Spring Boot 是应用开发在 Spring 框架上的一个进化阶段。它取代了手动编写所有配置的需求，提供了一些预配置项，你只需覆盖那些与你实现不匹配的配置。这种方法也被称为约定优于配置。Spring
Boot 已经不再是一个新概念，如今我们已经在使用它的第三个版本来编写应用程序。

在 Spring Boot
之前，开发者们常常需要为每个应用程序重复编写几十行代码。在过去，大多数架构都是单体架构，这种情况并不明显。使用单体架构时，你只需在一开始编写一次配置，之后很少需要再动它们。随着面向服务的软件架构的发展，我们开始感受到为每个服务配置时必须编写样板代码的痛苦。

由于这个原因，随着最近应用程序的发展，尤其是微服务应用程序，Spring Boot 变得越来越受欢迎。Spring Boot
为你的项目提供自动配置，缩短了设置所需的时间。可以说，它符合当今软件开发的理念。

在本章中，我们将从第一个使用Spring Security的应用程序开始。对于使用Spring Framework开发的应用程序，Spring
Security是实现应用级安全的绝佳选择。我们将使用Spring Boot，并讨论按约定配置的默认设置，同时简要介绍如何覆盖这些默认设置。考虑默认配置是了解Spring
Security的一个很好的入门方法，同时也能说明认证的概念。

一旦我们开始第一个项目，我们将更详细地讨论各种身份验证选项。在第3到第6章中，我们将继续介绍每个不同职责的具体配置，你将在第一个示例中看到这些配置。你还会看到根据不同的架构风格应用这些配置的方法。本章我们将讨论的步骤如下：

- 创建一个仅包含 Spring Security 和 Web 依赖项的项目，看看在没有添加任何配置的情况下它的表现。这样，你就能了解默认配置在身份验证和授权方面的预期效果。
- 将项目更改为添加用户管理功能，通过重写默认设置来定义自定义用户和密码。
- 在观察到应用程序默认对所有端点进行身份验证后，了解到这一点也可以进行自定义。
- 对相同配置应用不同风格，以了解最佳实践。

## 开始你的第一个项目

让我们创建第一个项目，作为我们的第一个示例。这个项目是一个小型的Web应用程序，公开一个REST端点。你会看到，在不做太多工作的情况下，Spring
Security使用`HTTP Basic认证`来保护这个端点。HTTP Basic是一种通过一组凭证（用户名和密码）在HTTP请求头中进行用户认证的方式。

!!! note

    在默认配置下，该应用程序有两种不同的`身份验证机制`：`HTTP Basic`和`表单登录`。不过，我决定一步步来，在后面的章节讨论表单登录。但如果你尝试在浏览器中访问该URL，你会发现你的应用程序实现了一个漂亮的用户身份验证表单，而不是显示一个难看的HTTP Basic框。这样做是为了避免你在使用浏览器实验时感到困惑，但我们会在HTTP Basic部分详细讨论这个问题。

只需创建项目并添加正确的依赖项，Spring Boot 在启动应用程序时就会应用默认配置，包括用户名和密码。

!!! note

    您有多种创建 Spring Boot 项目的选择。一些开发环境提供了直接创建项目的功能。有关更多详细信息，我推荐 Mark Heckler 的《Spring Boot: Up and Running》（O'Reilly Media, 2021）和 Somnath Musib 的《Spring Boot in Practice》（Manning, 2022），或者我写的另一本书《Spring Start Here》（Manning, 2021）。

本文中的示例涉及到书籍配套的源代码。对于每个示例，我还会指定需要添加到pom.xml文件中的依赖项。我建议你下载对应的项目和[源代码](https://www.manning.com/downloads/2105)
。这些项目可以在你遇到问题时提供帮助，你也可以用它们来验证你的最终解决方案。

!!! note

    这本书中的示例不依赖于您选择的构建工具。您可以使用 Maven 或 Gradle。为了保持一致性，我使用 Maven 构建了所有示例。

第一个项目也是最小的一个。它是一个简单的应用程序，提供一个可以调用的`REST端点`，然后接收响应，如图2.1所示。这个项目足以让你学习使用Spring
Security和Spring Boot开发应用程序的初步步骤。它展示了Spring Security在`身份验证`和`授权`方面的基本架构。


<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202408231033150.png){ loading=lazy }
  <figcaption>图2.1 我们的初始应用程序在访问端点时使用HTTP Basic进行用户身份验证和授权。它在指定路径（/hello）提供一个REST端点。请求成功后，它会返回一个HTTP 200状态消息以及响应主体。此实例展示了Spring Security设置的默认身份验证和授权机制。</figcaption>
</figure>

我们开始学习 Spring Security 时，首先创建一个空项目，并将其命名为 ssia-ch2-ex1[^1]
。（在其他提供的项目中，你也会找到同名的示例。）在我们的第一个项目中，你只需要添加
`spring-boot-starter-web` 和 `spring-boot-starter-security` 这两个依赖，如清单 2.1 所示。创建项目后，请确保将这些依赖添加到你的
pom.xml 文件中。进行这个项目的主要目的是观察一个默认配置的应用程序在 Spring Security 下的行为。我们还希望了解哪些组件是这个默认配置的一部分，以及它们的用途。

[^1]: ssia-ch2-ex1：完全使用Spring Security默认配置

```xml title="清单 2.1 我们第一个 Web 应用的 Spring Security 依赖项"

<dependencies>
    <!--Spring Mvc依赖-->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!--Spring Security-->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
</dependencies>
```

我们现在可以直接启动应用程序。Spring Boot 会根据我们添加到项目中的依赖项为我们应用 Spring
上下文的默认配置。然而，如果我们没有至少一个受保护的端点，就无法学到太多关于安全性的知识。让我们创建一个简单的端点并调用它，看看会发生什么。为此，我们在空项目中添加一个类，并将其命名为
HelloController。为此，我们将该类添加到 Spring Boot 项目主命名空间中的一个名为 controllers 的包中。

!!! note

    Spring Boot 仅扫描包含 `@SpringBootApplication 注解类`的`包及其子包`中的组件。如果你在主包之外使用 Spring 的任何构件注解对类进行标注，则必须使用 `@ComponentScan 注解`显式声明其位置。

``` java title="清单 2.2 HelloController 类和一个 REST 端点"
--8<-- "code/document-translation/spring-security-in-action2/part1_say_hello_to_spring_security/02_hello/ssia-ch2-ex1/src/main/java/com/luguosong/ssiach2ex1/controllers/HelloController.java"
```

`@RestController` 注解在上下文中注册了 bean，并告知 Spring 应用程序将此实例用作 Web 控制器。此外，该注解指定应用程序必须将
HTTP 响应的响应体设置为方法的返回值。

`@GetMapping` 注解通过 GET 请求将 /hello 路径映射到实现的方法。

一旦运行应用程序，除了控制台中的其他行，你应该会看到类似这样的内容：

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202408231101797.png){ loading=lazy }
  <figcaption>控制台中会显示密码</figcaption>
</figure>

每次运行应用程序时，它都会`生成一个新密码`，并在控制台中打印出该密码，如之前的代码片段所示。您必须使用此密码通过HTTP基本认证调用应用程序的任何端点。首先，让我们尝试在不使用授权头的情况下调用端点：

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202408231448444.png){ loading=lazy }
  <figcaption>访问服务响应401</figcaption>
</figure>

响应状态是HTTP `401 未授权`。我们预料到了这个结果，因为我们没有使用正确的凭证进行身份验证。默认情况下，Spring Security
期望使用默认用户名（user）和提供的密码。让我们再试一次，这次使用正确的凭证：

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202408231452750.png){ loading=lazy }
  <figcaption>成功响应内容</figcaption>
</figure>

!!! note

    HTTP`401`未授权状态码有些模棱两可。通常，它用于表示`身份验证失败`，而`不是授权失败`。开发人员在应用程序设计中使用它来处理诸如`凭证缺失或不正确的情况`。对于`授权失败`，我们可能会使用`403`禁止状态码。一般来说，HTTP 403 意味着服务器识别了请求的调用者，但他们没有进行该请求所需的权限。

一旦我们发送正确的凭证，您就可以在响应主体中准确看到我们之前定义的 HelloController 方法返回的内容。

!!! note "使用HTTP基本身份验证调用端点"

    使用 cURL，你可以通过 -u 标志设置 HTTP 基本用户名和密码。在后台，cURL 会将字符串 <用户名>:<密码> 编码为 Base64，并将其作为 `Authorization 头`的值发送，前缀为字符串 Basic。使用 cURL，可能更方便的是使用 -u 标志。但了解实际请求的样子也很重要。使用-u与下面的命令行示例是等价的，如：

     ```shell
     curl -H "Authorization: Basic dXNlcjo5M2EwMWNmMC03OTRiLTRiOTgtODZlZi01NDg2MGYzNmY3ZjM="  localhost:8080/hello
     ```   

默认项目没有重要的安全配置需要讨论。我们主要使用默认配置来证明正确的依赖项已就位。它对身份验证和授权作用不大。这种实现并不是我们希望在生产就绪的应用程序中看到的。但默认项目是一个很好的起点示例。

通过这个第一个示例的运行，至少我们知道 Spring Security 已经就位。接下来的步骤是更改配置，以便这些配置适用于我们的项目需求。首先，我们将深入了解
Spring Boot 在 Spring Security 方面的配置，然后我们将看看如何覆盖这些配置。

## Spring Security 类设计的整体概况

在本节中，我们将讨论在整个架构中参与认证和授权过程的主要角色。你需要了解这一方面，因为你将需要重写这些预配置的组件以满足你的应用需求。我将首先描述
Spring Security
在认证和授权方面的架构是如何运作的，然后我们会将其应用到本章的项目中。一次性讨论所有内容会显得过于繁杂，因此，为了减少你在本章的学习负担，我将对每个组件的整体情况进行讨论。关于每个组件的详细信息，你将在接下来的章节中学习到。

在上一节中，你看到了用于身份验证和授权的一些逻辑执行。我们有一个默认用户，每次启动应用程序时都会获得一个`随机密码`
。我们可以使用这个默认用户和密码来调用一个端点。但是，这些逻辑是在哪里实现的呢？你可能已经知道，Spring
Boot会根据你使用的依赖项为你设置一些组件（即我们在本章开头讨论的约定优于配置）。

图2.2展示了Spring Security架构中主要参与者（组件）的整体概况及其相互关系。这些组件在第一个项目中有预配置的实现。在本章中，我将演示Spring
Boot在您的应用程序中关于Spring Security的配置。我们还将讨论身份验证流程中各实体之间的关系。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202408231536368.png){ loading=lazy }
  <figcaption>图 2.2 这里重点介绍了 Spring Security 认证过程中涉及的核心元素及其相互关系。这个框架构成了使用 Spring Security 执行认证的基本结构。在本书中，我们将在研究各种认证和授权策略时频繁引用这一架构。</figcaption>
</figure>

1. `认证过滤器(Authentication filter)`将认证请求委托给`认证管理器(Authentication manager)`，并根据响应配置安全上下文。
2. `认证管理器(Authentication manager)`使用认证提供者来处理认证。
3. `认证提供者(Authentication provider)`实现认证逻辑。
4. `用户详情服务(User details service)`实现了用户管理职责，`认证提供者(Authentication provider)`在认证逻辑中使用该服务。
5. `密码编码器(Password encoder)`实现了密码管理，`认证提供者(Authentication provider)`在身份验证逻辑中使用它。
6. `安全上下文(Security context)`在认证过程后保存认证数据。安全上下文会保留这些数据直到操作结束。通常，在每个请求一个线程的应用程序中，这意味着直到应用程序将响应发送回客户端。

在接下来的段落中，我将讨论这些自动配置的 bean：

- `UserDetailsService`
- `PasswordEncoder`

在Spring Security中，实现`UserDetailsService接口`的对象负责管理用户的详细信息。到目前为止，我们一直使用Spring
Boot提供的默认实现。这个实现仅在应用程序的内部内存中注册默认凭据。默认凭据是用户名`user`和一个`默认密码`
，该密码是一个全局唯一标识符（UUID）。默认密码在Spring上下文加载时（应用程序启动时）随机生成。此时，应用程序会将密码写入控制台，您可以在控制台中看到它。因此，您可以在本章我们刚刚进行的示例中使用它。

此默认实现仅作为概念验证，帮助我们确认依赖关系已就绪。该实现将凭证存储在内存中——应用程序不会持久化这些凭证。这种方法适用于示例或概念验证，但在生产环境的应用程序中应避免使用。

接下来是`PasswordEncoder`。`PasswordEncoder`的作用有两个：

- 对密码进行编码（通常使用加密或哈希算法）
- 验证密码是否与现有编码匹配

即使不像 `UserDetailsService` 对象那样明显，`PasswordEncoder`
在基本身份验证流程中也是必需的。最简单的实现是以明文形式管理密码，并且不对其进行编码。我们将在第4章中更详细地讨论这个对象的实现。目前，你需要知道
`PasswordEncoder` 与默认的 `UserDetailsService` 一起存在。当我们替换 `UserDetailsService` 的默认实现时，也必须指定一个
`PasswordEncoder`。

Spring Boot在配置默认设置时也选择了一种身份验证方法：`HTTP基本访问认证`
。这是最简单的访问认证方法。基本认证只要求客户端通过HTTP授权头发送用户名和密码。在头部的值中，客户端附加前缀Basic，后面跟着包含用户名和密码的字符串的Base64编码，用户名和密码之间用冒号（:
）分隔。

!!! note

    `HTTP基本认证`不提供凭证的保密性。Base64仅是一种便于传输的编码方法，并不是加密或哈希方法。在传输过程中，如果被拦截，任何人都可以看到凭证。通常情况下，我们不会在没有至少使用HTTPS来保证保密性的情况下使用HTTP基本认证。你可以在[RFC 7617](https://tools.ietf.org/html/rfc7617)中阅读HTTP基本认证的详细定义。

`AuthenticationProvider` 定义了认证逻辑，并委托用户和密码管理。默认的 `AuthenticationProvider` 实现使用了
`UserDetailsService` 和 `PasswordEncoder` 提供的`默认实现`。隐式地，您的应用程序会`保护所有端点`
。因此，在我们的示例中，我们只需要添加端点。此外，只有一个用户可以访问所有端点，所以在这种情况下，我们可以说在`授权`方面没有太多需要做的。

!!! note "HTTP vs. HTTPS"

    在所展示的示例中，你可能注意到我只使用了HTTP。然而，在实际应用中，你的应用程序只通过HTTPS进行通信。在本文讨论的示例中，与Spring Security相关的配置无论使用HTTP还是HTTPS都没有区别。我们不会为示例中的端点配置HTTPS，以便你可以专注于与Spring Security相关的示例。但如果你愿意，可以按照本侧栏所示为任何端点启用HTTPS。

    在系统中配置HTTPS有多种模式。在某些情况下，开发人员会在应用程序层面配置HTTPS；在其他情况下，他们可能会使用服务网格，或者选择在基础设施层面设置HTTPS。使用Spring Boot，你可以轻松地在应用程序层面启用HTTPS，正如你将在本侧栏的下一个示例中学习到的那样。

    在任何这些配置场景中，您都需要一个由认证机构（CA）签署的证书。使用此证书，调用端点的客户端可以确认响应是否来自认证服务器，并确保通信未被拦截。如果需要，您可以购买这样的证书。如果您只需要配置HTTPS来测试您的应用程序，可以使用[OpenSSL](https://www.openssl.org/)等工具生成一个自签名证书。让我们生成自签名证书，然后在项目中进行配置：

    ```shell
    openssl req -newkey rsa:2048 -x509 -keyout key.pem -out cert.pem -days 365
    ```

    在终端中运行openssl命令后，系统会要求您输入密码和有关您的CA的详细信息。由于这只是一个用于测试的自签名证书，您可以随意输入任何数据；只需确保记住密码即可。该命令会输出两个文件：`key.pem（私钥）`和`cert.pem（公用证书）`。我们将使用这些文件进一步生成自签名证书以启用HTTPS。在大多数情况下，证书是`公钥密码学标准#12（PKCS12）`。较少情况下，我们使用`Java KeyStore（JKS）格式`。让我们继续使用PKCS12格式的示例（关于密码学的精彩讨论，我推荐David Wong的《Real-World Cryptography》[Manning, 2020]）。

    ```shell
    openssl pkcs12 -export -in cert.pem -inkey key.pem -out certificate.p12 -name "certificate"
    ```

     我们使用的第二个命令将第一个命令生成的两个文件作为输入，并输出自签名证书。

     请注意，如果您在 Windows 系统的 Bash shell 中运行这些命令，可能需要在命令前添加 winpty：

    ```shell
    winpty openssl req -newkey rsa:2048 -x509 -keyout key.pem -out cert.pem -days 365
    
    winpty openssl pkcs12 -export -in cert.pem -inkey key.pem -out certificate.p12 -name "certificate"
    ```

    最后，拥有自签名证书后，您可以为端点配置HTTPS。将`certificate.p12文件`复制到Spring Boot项目的`resources文件夹`中，并在`application.properties`文件中添加以下几行：

    ```properties
    server.ssl.key-store-type=PKCS12
    server.ssl.key-store=classpath:certificate.p12
    server.ssl.key-store-password=12345
    
    ```    

    在运行生成证书的命令后，系统会在提示中要求输入密码（在我的情况下是12345）。这就是为什么你在命令中看不到它。现在，让我们为应用程序添加一个测试端点，然后使用HTTPS调用它：

    ``` java
    @RestController
    public class HelloController {
    
     @GetMapping("/hello")
     public String hello() {
       return "Hello!";
     }
    }
    ```

    如果您使用自签名证书，您应该配置所使用的工具，使其在进行端点调用时跳过证书真实性的验证。如果工具验证证书的真实性，它将无法识别证书为真实的，调用将无法进行。使用 cURL 时，您可以使用 `-k 选项`来跳过证书真实性的验证：

    ```shell
    curl -k -u user:93a01cf0-794b-4b98-86ef-54860f36f7f3  https://localhost:8080/hello
    ```    

    请记住，即使使用 HTTPS，你的系统组件之间的通信也不是万无一失的。很多时候，我听到有人说：`我不再加密这个了，我会用 HTTPS！` 虽然 HTTPS 有助于保护通信，但它只是系统安全墙的一块砖。始终以负责任的态度对待系统的安全，并关注所有相关层面的安全。

## 覆盖默认配置

现在你已经了解了第一个项目的默认设置，是时候看看如何替换它们了。你需要理解可以用来覆盖默认组件的选项，因为这是你插入自定义实现并根据应用需求应用安全措施的方式。正如你将在本节中学习到的，开发过程还涉及如何编写配置以保持应用程序的高可维护性。在我们将要进行的项目中，你会经常发现有多种方法可以覆盖配置。这种灵活性可能会导致混淆。我经常看到在同一个应用程序中混合使用不同风格来配置Spring
Security的不同部分，这是不理想的。因此，这种灵活性也带来了警示。你需要学习如何从中选择，所以本节也涉及了解你的选项有哪些。

在某些情况下，开发人员选择`在Spring上下文中使用bean进行配置`。在其他情况下，他们会`重写各种方法`
以达到相同的目的。Spring生态系统的快速发展可能是导致这些多种方法出现的主要因素之一。用多种风格混合配置一个项目并不可取，因为这会使代码难以理解，并影响应用程序的可维护性。了解你的选项以及如何使用它们是一项宝贵的技能，这有助于你更好地理解如何在项目中配置应用级别的安全性。

在本节中，您将学习如何配置 `UserDetailsService` 和 `PasswordEncoder`。这两个组件通常参与身份验证，大多数应用程序会根据其需求进行自定义。本章中我们使用的实现均由
Spring Security 提供。

### 定制用户详情管理

在本章中，我们首先讨论的组件是 `UserDetailsService`。如你所见，应用程序在认证过程中使用了这个组件。在本节中，你将学习如何定义一个
`自定义类型的 UserDetailsService bean`。我们这样做是为了覆盖 Spring Boot 配置的默认实现。正如你将在第三章中更详细地看到的，你可以选择创建自己的实现，或者使用
Spring Security 提供的预定义实现。在本章中，我们不会详细介绍 Spring Security 提供的实现，也不会立即创建我们自己的实现。我将使用
Spring Security 提供的一个实现，名为 `InMemoryUserDetailsManager`。通过这个例子，你将学习如何将这种对象集成到你的架构中。

!!! note

    在 Java 中，接口定义了对象之间的契约。在应用程序的类设计中，我们使用接口来解耦相互使用的对象。为了强调接口的这种特性，在本书中讨论这些接口时，我主要将它们称为契约。

为了向您展示如何使用我们选择的实现来覆盖此组件，我们将更改第一个示例中的内容。这样做使我们能够拥有自己的托管凭据进行身份验证。在这个示例中，我们不实现自己的类，而是使用Spring
Security提供的实现。

在这个例子中，我们使用了 `InMemoryUserDetailsManager 实现`。尽管它不仅仅是一个 UserDetailsService，但目前我们仅从
UserDetailsService 的角度来讨论它。这个实现将凭证存储在内存中，随后可以被 Spring Security 用于验证请求。

!!! note

    `InMemoryUserDetailsManager 的实现`并不适用于`生产环境`的应用程序，但它是用于示例或概念验证的绝佳工具。在某些情况下，你只需要用户数据，而不需要花时间实现这部分功能。在我们的例子中，我们使用它来理解如何重写默认的 `UserDetailsService` 实现。

我们首先定义一个`配置类`。通常，我们在一个名为 config 的单独包中声明`配置类`。以下列表展示了配置类的定义。你也可以在
项目 ssia-ch2-ex2[^2] 中找到这个示例。

[^2]: ssia-ch2-ex2: 定制用户详情管理,配置UserDetailsService和PasswordEncoder对象,并通过HttpSecurity创建SecurityFilterChain对象，从而配置认证方式和授权规则

``` java

@Configuration
public class ProjectConfig {

	@Bean
	UserDetailsService userDetailsService() {
		return new InMemoryUserDetailsManager();
	}
}
```

我们用`@Configuration注解`标注这个类。`@Bean注解`
指示Spring将方法返回的实例添加到Spring上下文中。如果你按现在的代码执行，将不再在控制台中看到自动生成的密码。应用程序现在使用你添加到上下文中的
`UserDetailsService`类型的实例，而不是默认的自动配置实例。但与此同时，你将无法再访问该端点，原因有两个：

- 您还没有任何用户。
- 您没有密码编码器。

在图2.2中，你看到身份验证也依赖于`PasswordEncoder`。让我们一步步解决这两个问题。我们需要

1. 创建至少一个具有凭证（用户名和密码）的用户。
2. 将用户添加到由我们的 UserDetailsService 实现管理的用户中
3. 定义一个 PasswordEncoder 类型的 bean，供我们的应用程序用于验证给定密码与由 UserDetailsService 存储和管理的密码。

首先，我们声明并添加一组`凭证`，以便在 `InMemoryUserDetailsManager 实例`
中用于身份验证。在第三章中，我们将详细讨论用户及其管理方法。目前，我们使用一个预定义的构建器来创建一个 `UserDetails` 类型的对象。

!!! note

    有时你会看到我在代码中使用var。Java 10引入了保留类型名var，你只能在局部声明中使用它。虽然在某些情况下，本书中使用var的方式从代码整洁的角度来看可能不太好，但这样做是为了简化语法，并隐藏变量类型。这种方法可以帮助你专注于示例中相关的部分。我们将在后面的章节中讨论var隐藏的类型，所以在需要深入分析之前，你不必担心这些类型。

在创建实例时，我们必须提供`用户名`、`密码`和`至少一个权限`。权限是允许该用户执行的操作，我们可以使用任何字符串来表示。在接下来的示例中，我将权限命名为
`read`，但因为我们暂时不会使用这个权限，所以这个名称并不重要。

``` java

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

    您会在 `org.springframework.security.core.userdetails` 包中找到 User 类。我们使用这个构建器实现来创建表示用户的对象。此外，作为本书的一般规则，如果我没有在代码示例中展示如何编写一个类，这意味着 Spring Security 已经提供了该类。

如上面代码所示，我们必须提供一个`用户名的值`、`一个密码的值`以及`至少一个权限`。然而，这仍不足以让我们调用端点。我们还需要声明一个
`PasswordEncoder`。

使用`默认的UserDetailsService`时，`PasswordEncoder`也会自动配置。由于我们重写了`UserDetailsService`，因此也必须声明一个
`PasswordEncoder`。现在尝试这个示例时，当你调用端点时会看到一个异常。在尝试进行身份验证时，Spring
Security意识到它不知道如何管理密码，因此失败。异常的样子如下一个代码片段所示，你应该能在应用程序的控制台中看到它。客户端会收到一个
`HTTP 401 Unauthorized`消息和一个空的响应体：

```shell
C:\Users\10545>curl -i -u john:12345 http://localhost:8080/hello
HTTP/1.1 401
WWW-Authenticate: Basic realm="Realm"
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Set-Cookie: JSESSIONID=0AED260DB67D15962B6366275EF3C26C; Path=/; HttpOnly
Content-Length: 0
Date: Mon, 26 Aug 2022 09:13:41 GMT
```

为了解决这个问题，我们可以像处理 `UserDetailsService` 一样，在上下文中添加一个 `PasswordEncoder bean`。对于这个
bean，我们使用现有的 PasswordEncoder 实现：

``` java

@Bean
public PasswordEncoder passwordEncoder() {
	return NoOpPasswordEncoder.getInstance();
}
```

!!! note

    `NoOpPasswordEncoder`实例将密码视为纯文本处理，不对其进行加密或哈希。在匹配时，`NoOpPasswordEncoder`仅使用String类的`equals(Object o)方法`比较字符串。在生产环境的应用程序中不应使用这种类型的PasswordEncoder。

!!! warning

    `NoOpPasswordEncoder`适用于不想关注密码哈希算法的示例。因此，该类的开发者将其标记为`@Deprecated`(已弃用)，并且在开发环境中其名称会显示为`删除线`。
    
    这个 PasswordEncoder 不安全。应改用适应性单向函数，如 BCryptPasswordEncoder、Pbkdf2PasswordEncoder 或 SCryptPasswordEncoder。更好的是使用DelegatingPasswordEncoder，它支持密码升级。没有计划移除这个支持。弃用它是为了表明这是一个遗留实现，使用它被认为不安全。

目前，完整的配置文件类代码如下：

``` java

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

让我们用新用户尝试这个端点，用户名为john，密码为12345：

```shell
C:\Users\10545>curl -i -u john:12345 http://localhost:8080/hello
HTTP/1.1 200
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: text/plain;charset=UTF-8
Content-Length: 6
Date: Mon, 26 Aug 2024 09:30:54 GMT

Hello!
```

### 在端点级别应用授权

随着用户管理的新安排到位，如上一小节节所述，我们现在可以讨论终端的`认证方法和配置`
。在深入细节之前，您必须了解整体概况。实现这一点的最佳方式是通过我们的第一个示例。在默认配置下，所有终端都假定您有一个由应用程序管理的有效用户。此外，默认情况下，您的应用程序使用HTTP基本认证，但您可以轻松覆盖此配置。

正如你将在接下来的章节中了解到的，HTTP基本认证并不适合大多数应用程序架构。有时，我们希望对其进行更改以匹配我们的`应用程序`
。同样，并不是所有的应用程序端点都需要安全保护，对于那些需要保护的端点，我们可能需要选择不同的认证方法和授权规则。为了定制认证和授权的处理，我们需要定义一个类型为
`SecurityFilterChain`的bean。在这个例子中，我将继续在项目`ssia-ch2-ex2`中编写代码。

``` java

@Configuration
public class ProjectConfig {

	@Bean
	SecurityFilterChain configure(HttpSecurity http)
			throws Exception {

		return http.build();
	}

	// 省略的代码
}
```

然后，我们可以使用 `HttpSecurity` 对象的不同方法来修改配置，如下所示。

``` java title="清单2.7:使用 HttpSecurity 参数修改配置"

@Configuration
public class ProjectConfig {

	@Bean
	SecurityFilterChain configure(HttpSecurity http)
			throws Exception {

		http.httpBasic(Customizer.withDefaults());

		http.authorizeHttpRequests(
				// 认证后可以访问
				c -> c.anyRequest().authenticated()
		);

		return http.build();
	}

	// Omitted code

}
```

清单2.7中的代码配置了与默认行为相同的端点授权。你可以再次调用该端点，查看其行为是否与上一节中的测试相同。稍作修改，你可以使所有端点在
`无需凭证的情况下访问`。你将在接下来的清单中看到如何实现这一点。

``` java title="清单2.8 使用 permitAll() 更改授权配置"

@Configuration
public class ProjectConfig {

	@Bean
	public SecurityFilterChain configure(HttpSecurity http)
			throws Exception {

		http.httpBasic(Customizer.withDefaults());

		http.authorizeHttpRequests(
				// 允许所有访问
				c -> c.anyRequest().permitAll()
		);

		return http.build();
	}

	// Omitted code
}
```

现在我们可以在不需要凭证的情况下调用 `/hello 端点`。配置中的 `permitAll()` 调用与 `anyRequest()`
方法一起，使所有端点都可以在无需凭证的情况下访问：

```shell
C:\Users\10545>curl -i  http://localhost:8080/hello
HTTP/1.1 200
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: text/plain;charset=UTF-8
Content-Length: 6
Date: Mon, 26 Aug 2024 12:34:44 GMT

Hello!
```

在此示例中，我们使用了两种配置方法：

- `httpBasic()` 帮助我们配置了认证方式。通过调用此方法，您指示应用程序接受 `HTTP Basic` 作为认证方法。
- `authorizeHttpRequests() `方法帮助我们在`端点级别`配置授权规则。通过调用此方法，您可以指导应用程序如何对特定端点接收到的请求进行授权。

对于这两种方法，你需要使用一个`Customizer对象`作为参数。Customizer是一个契约，你可以通过实现它来定义你配置的Spring
Security元素的自定义设置：认证、授权，或者特定的保护机制，如CSRF或CORS（将在第9章和第10章中讨论）。以下代码片段展示了Customizer接口的定义。注意，Customizer是一个
`函数式接口`（因此我们可以使用lambda表达式来实现它），而我在代码清单2.8中使用的`withDefaults()方法`
实际上只是一个不执行任何操作的Customizer实现：

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

在早期版本的 Spring Security 中，你可以使用`链式语法`来应用配置，而无需使用 `Customizer 对象`，如下代码片段所示。注意，这里并没有为
authorizeHttpRequests() 方法提供 Customizer 对象，配置直接跟在方法调用之后：

``` java
http.authorizeHttpRequests().anyRequest().authenticated()
```

这种方法被弃用的原因是，Customizer 对象可以让你在需要时更灵活地移动配置。确实，对于简单的例子，使用 lambda
表达式很方便。但在实际应用中，配置可能会变得非常复杂。在这种情况下，将这些配置移到独立的类中有助于使配置更易于维护和测试。

这个例子的目的是让你了解如何覆盖默认配置。关于授权的详细信息，我们将在第7到第10章中深入探讨。

!!! note

    在早期版本的 Spring Security 中，安全配置类需要继承名为 `WebSecurityConfigurerAdapter` 的类。我们现在不再使用这种做法。

### 以不同方式进行配置

使用 Spring Security 创建配置时，一个令人困惑的方面是可以通过多种方式配置同一事物。在本节中，您将学习配置
`UserDetailsService` 和 `PasswordEncoder`
的替代方法。了解您拥有的选项非常重要，这样您才能在本书或其他来源如博客和文章中找到的示例中识别这些选项。同样重要的是，您要理解如何以及何时在您的应用程序中使用这些选项。后续章节将提供不同的示例，以扩展本节中的信息。

让我们来看第一个项目。在创建了一个默认应用程序后，我们通过在 Spring 上下文中添加新的实现作为 bean，成功地重写了
`UserDetailsService` 和 `PasswordEncoder`。让我们寻找另一种方式来进行 `UserDetailsService` 和 `PasswordEncoder` 的相同配置。

我们可以直接使用 SecurityFilterChain bean 来设置 UserDetailsService 和 PasswordEncoder，如下所示。你可以在项目
ssia-ch2-ex3[^3] 中找到这个示例。

[^3]: ssia-ch2-ex3: 以不同方式进行配置，通过HttpSecurity配置userDetailsService，而不是构建UserDetailsService Bean

``` java title="清单 2.9 使用 SecurityFilterChain bean 设置 UserDetailsService"

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

在代码清单2.9中，你可以看到我们以与代码清单2.5相同的方式声明了UserDetailsService。不同之处在于，现在这是在创建
`SecurityFilterChain的bean方法内部本地完成的`。我们还调用了HttpSecurity的`userDetailsService()方法`来注册
`UserDetailsService实例`。下一个清单展示了配置类的完整内容。

``` java title="清单 2.10 配置类的完整定义"
--8<-- "code/document-translation/spring-security-in-action2/part1_say_hello_to_spring_security/02_hello/ssia-ch2-ex3/src/main/java/com/luguosong/ssiach2ex3/config/ProjectConfig.java"
```

这些配置选项都是正确的。第一种选项是将 beans 添加到`上下文`中，这样你可以在可能需要的其他类中注入这些值。但如果你不需要这样做，第二种选项同样不错。

### 定义自定义认证逻辑

正如您已经观察到的，Spring Security组件提供了很大的灵活性，在适应我们应用程序的架构时提供了许多选项。到目前为止，您已经了解了
`UserDetailsService`和`PasswordEncoder`在Spring Security架构中的作用，并且也看到了几种配置它们的方法。现在是时候学习如何自定义委托给这些组件的
`AuthenticationProvider`，如图2.3所示。`AuthenticationProvider实现了认证逻辑`，并委托给`UserDetailsService`和
`PasswordEncoder`进行用户和密码管理。因此，我们可以说，通过这一部分的学习，我们将更深入地了解`认证架构`，学习如何使用
`AuthenticationProvider`实现自定义认证逻辑。

因为这是第一个例子，我只给你展示一个简要的图示，以便你更好地理解架构中各组件之间的关系。但我们将在第3到第6章中进行更详细的探讨。

我建议你考虑一下 Spring Security 架构中设计的职责。这个架构是松耦合的，具有细粒度的职责分配。这种设计是使 Spring Security
灵活且易于与应用程序集成的原因之一。根据你如何利用其灵活性，你也可以改变设计。你必须小心这些方法，因为它们可能会使你的解决方案变得复杂。例如，你可以选择以
`不再需要 UserDetailsService 或 PasswordEncoder 的方式重写默认的 AuthenticationProvider`。考虑到这一点，清单 2.11
展示了如何创建自定义认证提供者。你可以在项目ssia-ch2-ex4[^4]中找到这个示例。

[^4]: ssia-ch2-ex4: 通过实现AuthenticationProvider接口，自定义CustomAuthenticationProvider类，实现自定义认证逻辑。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202408271105101.png){ loading=lazy }
  <figcaption>图 2.3 AuthenticationProvider 实现了认证逻辑。它接收来自 AuthenticationManager 的请求，并将查找用户的任务委托给 UserDetailsService，将密码验证任务委托给 PasswordEncoder。</figcaption>
</figure>

``` java title="清单 2.11 实现 AuthenticationProvider 接口"

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {

		// authentication logic here
	}

	@Override
	public boolean supports(Class<?> authenticationType) {

		// type of the Authentication implementation here
	}
}
```

`authenticate(Authentication authentication) 方法`包含了所有的`认证逻辑`，因此我们将在清单2.12中添加一个这样的实现。我将在第6章详细解释
`supports()方法`的用法。目前，我建议你暂时接受它的实现。在当前的例子中，它并不是必需的。

``` java title="清单 2.12 实现认证逻辑"

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

在这里，`if-else 语句`的条件替代了 `UserDetailsService` 和 `PasswordEncoder` 的职责。你并不需要使用这两个
bean，但如果你处理用户和密码进行身份验证，我强烈建议你将其管理逻辑分开。即使你重写身份验证的实现，也要按照 Spring Security
架构的设计来应用它。

您可能会发现，通过实现自己的 AuthenticationProvider 来替换认证逻辑是有用的。如果默认实现不完全符合您的应用需求，您可以选择实现自定义的认证逻辑。完整的
AuthenticationProvider 实现如下所示。

``` java title="清单 2.13 认证提供者的完整实现"
--8<-- "code/document-translation/spring-security-in-action2/part1_say_hello_to_spring_security/02_hello/ssia-ch2-ex4/src/main/java/com/luguosong/ssiach2ex4/security/CustomAuthenticationProvider.java"
```

在配置类中，您可以使用 HttpSecurity 的 `authenticationProvider() 方法`注册 `AuthenticationProvider`，如下所示。

``` java title="清单 2.14 注册新的 AuthenticationProvider 实现"
--8<-- "code/document-translation/spring-security-in-action2/part1_say_hello_to_spring_security/02_hello/ssia-ch2-ex4/src/main/java/com/luguosong/ssiach2ex4/config/ProjectConfig.java"
```

```shell
C:\Users\10545>curl -i -u john:12345 http://localhost:8080/hello
HTTP/1.1 200
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: text/plain;charset=UTF-8
Content-Length: 6
Date: Tue, 27 Aug 2024 07:06:26 GMT

Hello!
```

在第六章中，您将深入了解`AuthenticationProvider`，以及如何在认证过程中重写其行为。在同一章节中，我们还将讨论
`Authentication接口`及其实现，例如`UserPasswordAuthenticationToken`。

### 使用多个配置类

在之前实现的示例中，我们只使用了一个配置类。然而，最好还是将`职责分离`
，即使是对于配置类也是如此。我们需要这种分离，因为配置开始变得更加复杂。在一个准备投产的应用程序中，可能会有比我们最初示例中更多的声明。你可能还会发现，拥有多个配置类有助于提高项目的可读性。

通常来说，每个职责只对应一个类是个不错的实践。在这个例子中，我们可以将`用户管理配置`与`授权配置`分开。我们通过定义两个配置类来实现这一点：
`UserManagementConfig`（在下一个列表中定义）和 `WebAuthorizationConfig`（在列表 2.16 中定义）。你可以在项目 ssia-ch2-ex5[^5]
中找到这个例子。

[^5]: ssia-ch2-ex5: 使用多个配置类

``` java title="清单2.15 定义用户和密码管理的配置类"

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

在这种情况下，`UserManagementConfig` 类仅包含负责用户管理的两个 bean：`UserDetailsService` 和 `PasswordEncoder`
。下面的列表展示了这个定义。

``` java

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

在这里，`WebAuthorizationConfig` 类需要定义一个类型为 `SecurityFilterChain` 的 bean，以配置认证和授权规则。

## 小结

- 当你将 Spring Security 添加到应用程序的依赖项中时，Spring Boot 会提供一些默认配置。
- 您实现了以下用于身份验证和授权的基本组件：`UserDetailsService`、`PasswordEncoder` 和 `AuthenticationProvider`。
- 您可以使用 User 类定义用户。一个用户至少应具备用户名、密码和权限。权限是指您允许用户在应用程序中执行的操作。
- Spring Security 提供的一个简单实现 `UserDetailsService` 的方式是 `InMemoryUserDetailsManager`。你可以向这样的
  `UserDetailsService 实例`中添加用户，以便在应用程序的内存中管理用户。
- `NoOpPasswordEncoder` 是 `PasswordEncoder` 合约的一种实现，它使用明文密码。此实现适合用于学习示例和（可能的）概念验证，但不适合用于生产环境的应用程序。
- 您可以使用 `AuthenticationProvider` 合约在应用程序中实现自定义身份验证逻辑。
- 在一个应用程序中，有多种方式可以编写配置，但你应该选择并坚持一种方法。这有助于使代码更简洁且更易于理解。



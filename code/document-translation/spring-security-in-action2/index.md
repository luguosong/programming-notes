---
icon: material/book
---

# Spring Security实战

## 思维导图

<figure markdown="span">
  ![](https://edrawcloudpubliccn.oss-cn-shenzhen.aliyuncs.com/viewer/self/1059758/share/2024-11-21/1732177226/main.svg){ loading=lazy }
  <figcaption>Spring Security知识点</figcaption>
</figure>

## 关于这本书

安全性在软件开发中至关重要，从一开始就将其融入开发流程是必不可少的。《Spring Security实战（第二版）》深入讲解了如何利用Spring Security为你的项目注入应用级安全防护。精通Spring Security并正确应用它，是每一位开发者不可或缺的能力。缺乏这方面的知识就贸然开发应用，无异于冒着巨大的风险。

### 谁适合阅读本书？

本书主要面向使用 Spring 框架进行企业级应用开发的程序员。虽然内容特别为初学 Spring Security 的读者设计，但你需要具备 Spring 框架的基础知识，包括：

- 使用 Spring 上下文
- 编写 REST 接口
- 操作数据源

第15章深入探讨了响应式应用的安全配置。因此，提前了解响应式应用及其在Spring中的开发至关重要。在本书的学习过程中，我会为你推荐补充资源，以帮助你巩固或了解相关主题。

本书中的所有示例均采用 Java 语言。鉴于 Java 在 Spring 生态系统中的广泛应用，默认读者已具备一定的 Java 基础。当然，也有一些开发者可能会使用 Kotlin 等其他语言，但其基础原理大致相同。如果需要，书中的示例也可以方便地转换为 Kotlin。

如果你觉得在开始阅读本书之前需要回顾一下相关的基础知识，我非常推荐你阅读我写的另一本书`《Spring Start Here》（Manning, 2021）`。

### 本书结构一览：导读 

我精心编排了本书的内容，旨在带领你系统学习 Spring Security，从基础概念到进阶应用，循序渐进地深入每一个知识点。全书各部分环环相扣，帮助你顺畅地推进学习进程，沉浸式掌握相关内容。以下是本书的简要结构说明：

- `第1部分：初识Spring Security`:在本部分中，我将带你了解现代安全领域以及Spring Security。我们将从安全在当今数字时代中的关键作用谈起，探讨Spring Security如何应对这些挑战，为后续内容打下基础。
- `第2部分：配置认证`:深入探讨认证的核心内容。我会讲解用户管理、密码规范、过滤器在Web应用安全中的重要作用，以及认证的具体实现方式。
- `第3部分：配置授权`:我们将从认证过渡到授权。一起探索端点级别的授权、防御CSRF等威胁的安全措施、CORS管理，并深入讲解方法级别的复杂授权与过滤。
- `第4部分：实现OAuth 2与OpenID Connect`:本部分将带你进入OAuth 2和OpenID Connect的世界。你将了解它们的重要性，并学习如何搭建OAuth 2服务器、资源服务器和客户端，从而全面提升应用安全性。
- `第5部分：迈向响应式编程`:在这里，我将为你介绍响应式编程范式，详细讲解如何保障响应式应用的安全，确保你的异步操作同样安全可靠。
- `第6部分：测试安全配置`:我将强调上线前安全测试的重要性。我们会深入探讨各种技术手段，确保你的安全配置能够精准无误地发挥作用。
- `附录部分`:包含了官方文档资源和进一步阅读材料，以便为您的学习和探索提供补充。

虽然我将本书设计为一步步的学习旅程，但如果你已经有一定的 Spring Security 基础，可以直接跳转到自己感兴趣的章节。不过需要注意，后面的章节可能会引用前面讲解过的概念。如果你已经熟悉 Spring Security 的基础内容，可以从第三部分或第四部分开始，深入了解 OAuth 2 和 OpenID。对于对响应式编程感兴趣的读者，则可以直接阅读第五部分。无论你从哪里开始，都要确保彻底掌握每一个概念，这样才能更好地理解后续章节的内容。

### 关于代码

本书提供了70多个项目，我们将从第2章一直到第18章逐步实践这些项目。在讲解具体示例时，我会提到实现该示例的项目名称。我的建议是，你可以结合书中的讲解，自己从零开始编写示例代码，然后再用书中提供的项目与自己的实现进行对比。这样的学习方式能帮助你更好地理解所学的安全配置知识。每个项目都是基于Maven构建的，因此可以很方便地导入到任何IDE中。我本人使用的是IntelliJ IDEA来编写这些项目，但你也可以选择在Eclipse、STS、NetBeans或其他你喜欢的开发工具中运行它们。附录部分还为你复习了如何创建Spring Boot项目的相关内容。

本书包含了大量的源码示例，既有编号的代码清单，也有直接嵌入正文的代码片段。无论哪种情况，源码都会采用等宽字体进行排版，以便与普通文本区分开来。很多情况下，原始源码经过了重新排版，我们增加了换行并调整了缩进，以适应书页的排版需求。极少数情况下，即使这样也无法完全展示代码，我们会用行续标记（➥）来表示代码的延续。此外，当代码在正文中有详细讲解时，源码中的注释通常会被省略。许多代码清单还配有注释，突出重要的概念。

你可以在本书的liveBook（在线）版本 [https://livebook.manning.com/book/spring-security-in-action-second-edition](https://livebook.manning.com/book/spring-security-in-action-second-edition) 上获取可执行的代码片段。本书所有示例的完整代码也可以在Manning官网 [www.manning.com](https://www.manning.com) 下载。

### liveBook 讨论区  

购买《Spring Security实战（第2版）》即可免费获得对liveBook的访问权限，这是Manning推出的在线阅读平台。通过liveBook独有的讨论功能，你可以在全书范围内，或针对特定章节和段落，添加评论。无论是做个人笔记、提出和解答技术问题，还是向作者及其他用户寻求帮助，都非常方便。要访问讨论区，请前往：[https://livebook.manning.com/book/spring-security-in-action-second-edition/discussion](https://livebook.manning.com/book/spring-security-in-action-second-edition/discussion)。你还可以在[https://livebook.manning.com/discussion](https://livebook.manning.com/discussion)了解更多关于Manning论坛及其行为规范的信息。

Manning致力于为读者提供一个有意义的交流平台，方便读者之间以及读者与作者之间进行互动。需要注意的是，作者参与论坛的行为完全是自愿且无偿的，因此我们无法保证作者一定会参与到某个具体程度。建议你多向作者提出一些有挑战性的问题，以激发他们的兴趣！只要本书仍在出版，论坛及以往讨论的存档都可以通过出版社网站访问。



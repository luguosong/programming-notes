---
layout: note
title: 服务API文档
nav_order: 110
create_time: 2023/4/18
---

# OpenAPI

`OpenAPI规范`（以前称为`Swagger规范`）是一种用于描述、生成、消费和可视化Web服务的机器可读接口定义语言规范。它曾经是`Swagger框架`的一部分，在2016年成为独立项目，由`Linux Foundation`下的开源协作项目`OpenAPI Initiative`进行管理。Swagger和其他一些工具可以从接口文件生成代码、文档和测试用例。

| 版本   | 日期       | 说明                                  |
|-------|------------|---------------------------------------|
| 3.1.0 | 2021-02-15 | 发布 OpenAPI Specification 3.1.0         |
| 3.0.3 | 2020-02-20 | OpenAPI Specification 3.0.3 补丁版本发布 |
| 3.0.2 | 2018-10-08 | OpenAPI Specification 3.0.2 补丁版本发布 |
| 3.0.1 | 2017-12-06 | OpenAPI Specification 3.0.1 补丁版本发布 |
| 3.0.0 | 2017-07-26 | 发布 OpenAPI Specification 3.0.0         |
| 2.0   | 2014-09-08 | 发布 Swagger 2.0                       |
| 1.2   | 2014-03-14 | 正式文档的初始版本发布                 |
| 1.1   | 2012-08-22 | 发布 Swagger 1.1                       |
| 1.0   | 2011-08-10 | 发布 Swagger 规范的第一个版本           |

[详情请见](https://en.wikipedia.org/wiki/OpenAPI_Specification)

# swagger

`Swagger`是一种流行的API开发工具和框架，最初由`Tony Tam`于2010年创建。它提供了一套开放源代码的API框架，可以帮助开发者设计、构建、测试和文档化RESTful Web服务。

在Swagger的流行程度不断提高的过程中，其规范也被社区广泛采纳。后来，`Swagger规范`被重命名为`OpenAPI规范`，并在Linux基金会的OpenAPI倡议下继续发展。因此，可以说OpenAPI规范是Swagger规范的`下一代版本`，也是Swagger的演变和发展。

# springfox

`Springfox`是一个开源项目，它是基于Swagger规范和Spring Framework的集成库，可以帮助Java开发人员通过自动生成API文档、测试用例等方式来快速开发RESTful Web服务。与OpenAPI规范类似，Swagger规范也是Springfox所依赖的一部分。

{: .warning-title}
> springfox已经很久没更新了
> 
> ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20230418182319.png)

# springdoc

[Springdoc](https://github.com/springdoc/springdoc-openapi)和`Springfox`都是用于在Spring应用程序中集成`OpenAPI规范`的库，它们都提供了自动生成API文档的功能，并且可以使用Swagger UI或ReDoc等工具来可视化这些文档。

相比之下，Springdoc的设计目标是提供一个轻量级、易用、快速的OpenAPI规范实现，它的API和配置选项相对较少，但仍然提供了足够的功能来生成和展示API文档。

此库支持：

- OpenAPI 3
- Spring-boot（v1、v2和v3）
- JSR-303，特别是@NotNull、@Min、@Max和@Size。
- Swagger-ui
- OAuth 2
- GraalVM native images

# 快速入门

maven依赖`springdoc-openapi-starter-webmvc-ui`:

{% highlight xml %}
{% include_relative springdoc_01_hello/pom.xml %}
{% endhighlight %}

启动spring boot项目，既可以通过`http://localhost:port/swagger-ui.html`访问swagger-ui页面。

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20230419143429.png)

提供xml文档：`http://localhost:port/v3/api-docs`

提供yaml文档：`http://localhost:port/v3/api-docs.yaml`

{: .warning}
> 为了支持Spring Boot v

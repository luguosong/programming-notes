# MVC架构模式

## 不使用MVC模式存在的问题

- 不使用MVC模式，Servlet需要负责`数据接收`、`核心业务逻辑处理`、`数据库连接和增删改查操作`、`页面展示`等功能。职责过重。
- 代码的`复用性差`，相同的业务操作或数据库操作，需要在不同Servlet中编写重复代码，不方便维护。
- 代码`耦合度高`，导致代码很难扩展。
- 操作数据库的代码和处理业务逻辑的代码混杂在一起，很容易出错，无法专注于业务逻辑的编写。

## MVC模式概述

- `M(Model、模型)`:用于处理业务
- `V(View、视图)`:负责页面展示
- `C(Controller、控制器)`:控制器是MVC架构的核心，

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/diagrams/java_serve/web_application/mvc/MVC%E6%9E%B6%E6%9E%84%E6%A8%A1%E5%BC%8F.svg){ loading=lazy }
  <figcaption>MVC架构图解</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202406201047597.png){ loading=lazy }
  <figcaption>MVC请求响应过程</figcaption>
</figure>

!!! note "DAO(Data Access Object、数据访问对象)"

    属于JavaEE的设计模式之一。只负责数据库的增删改查，没有任何业务逻辑在里面

## 三层架构

- `表示层`：Controller控制器+View视图
- `业务逻辑层`：Service服务
- `持久化层`：DAO数据访问对象

## Spring MVC概述

`Spring Web MVC` 是基于 Servlet API 构建的原始 Web 框架，从 Spring 框架诞生之初就已包含其中。其正式名称`Spring Web MVC`
源自其源码模块名称:`spring-webmvc`，但更常被称为`Spring MVC`。

与 Spring Web MVC 并行，Spring Framework 5.0 引入了一个名为`Spring WebFlux`的响应式堆栈Web框架，其名称也基于其源模块
`spring-webflux`。

`Spring MVC`是实现`MVC架构模式`的Web框架。底层使用`Servlet`实现。

!!! note "Spring MVC能干什么"

    - 入口控制：通过`DispatcherServlet`作为入口控制器负责接收请求和分发请求。
    - 自动将表单请求参数封装为JavaBean对象
    - 统一使用IOC容器管理对象
    - 统一请求处理：提供拦截器、统一异常处理等机制
    - 视图解析：轻松切换JSP、Freemarker、Velocity等视图模板
    - 对Controller进行单元测试时

- `入口控制`：Servlet开发中，每个Servlet都需要在web.xml中进行配置，Spring MVC通过`DispatcherServlet`作为入口控制器负责统一接收请求和分发请求。
- Spring MVC会自动将表单数据封装为JavaBean对象,而不需要手动通过request对象获取表单数据。
- Spring MVC通过IOC容器管理对象，不需要手动创建对象。
- Spring MVC提供拦截器、异常处理器等统一处理请求机制。不需要手动编写过滤器。
- `视图解析器`：Spring MVC提供了JSP、Freemarker、Velocity等视图解析器。

## Spring MVC入门案例

创建maven工程，将工程改为war包，引入依赖：

``` xml
--8<-- "code/java_serve/web_application/mvc/springmvc-hello/pom.xml"
```

!!! warning

    注意，需要将maven工程改为war包：`<packaging>war</packaging>`

创建`webapp/WEB-INF/web.xml`目录和文件。

在`web.xml`中配置前端控制器（DispatcherServlet）：

!!! note

    相比于Servlet开发，Spring MVC会配置一个全局统一的`DispatcherServlet`来管理所有请求。

``` xml
--8<-- "code/java_serve/web_application/mvc/springmvc-hello/src/main/webapp/WEB-INF/web.xml"
```

在Spring MVC配置文件配置包扫描视图解析器：

> 其中常见的视图解析器有以下几种：
>
> - JSP的视图解析器：InternalResourceViewResolver
> - FreeMarker的视图解析器：FreeMarkerViewResolver
> - Velocity的视图解析器：VelocityViewResolver

``` xml
--8<-- "code/java_serve/web_application/mvc/springmvc-hello/src/main/webapp/WEB-INF/springmvc-servlet.xml"
```

编写视图：

``` html title="hello.thymeleaf"
--8<-- "code/java_serve/web_application/mvc/springmvc-hello/src/main/webapp/WEB-INF/template/hello.thymeleaf"
```

编写Controller：

``` java
--8<-- "code/java_serve/web_application/mvc/springmvc-hello/src/main/java/com/luguosong/controller/HelloController.java"
```

!!! note

    `逻辑视图名称`会根spring mvc配置文件中的`prefix`和`suffix`属性进行拼接。找到具体的视图位置(物理视图名称)。

启动Tomcat，通过以下地址可以访问视图：

```text
http://localhost:8080/springmvc_hello_war_exploded/hello-mvc
```

## Spring MVC执行流程

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/diagrams/java_serve/web_application/mvc/SpringMVC%E6%89%A7%E8%A1%8C%E8%BF%87%E7%A8%8B.svg){ loading=lazy }
  <figcaption>Spring MVC执行流程</figcaption>
</figure>

1. 发送请求，`DispatcherServlet类`接收请求。
	1. `doDispatch方法`负责处理请求。
		1. 通过`HttpServletRequest`请求对象得到uri，根据uri得到`HandlerExecutionChain处理器执行链`对象(
		   其中包含拦截器和处理器)。
		2. `HandlerExecutionChain处理器执行链`获取处理器适配器`HandlerAdapter`对象。
		3. `HandlerExecutionChain对象`执行该请求所有拦截器中的`preHandle方法`。
		4. 通过消息转换器将请求参数进行转换，`HandlerAdapter`对象调用Controller处理器方法。返回`ModelAndView`对象。
		5. `HandlerExecutionChain对象`执行该请求所有拦截器中的`postHandle方法`。
		6. `processDispatchResult方法`处理响应结果。
            1. 通过`视图解析器`解析，返回`视图对象`。调用视图对象的渲染方法。
            2. 执行该请求所有拦截器中的`afterCompletion方法`。

## 自定义Spring MVC配置文件名称

默认情况下，Spring MVC会根据web.xml中`<servlet-name>标签`的值去寻找Spring MVC配置文件。

比如`<servlet-name>`的值为`springmvc`，那么就会去寻找`/WEB-INF/springmvc-servlet.xml`配置文件。

当然，也可以手动指定Spring MVC配置文件：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd"
         version="6.0">
    <servlet>
        <servlet-name>springmvc</servlet-name>
        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
        <!--指定配置文件位置-->
        <init-param>
            <param-name>contextConfigLocation</param-name>
            <param-value>classpath:springmvc-servlet.xml</param-value>
        </init-param>
    </servlet>
    <servlet-mapping>
        <servlet-name>springmvc</servlet-name>
        <url-pattern>/</url-pattern>
    </servlet-mapping>
</web-app>
```

## @RequestMapping注解

### @RequestMapping注解的使用

您可以使用`@RequestMapping`注解将请求映射到控制器方法。

`@RequestMapping`可以作用于`类`或者`方法`。

### value属性

`value属性`与`path属性`功能相同,都是用于映射请求路径。

``` java

@Controller
public class HelloController {
	//请求映射
	@RequestMapping("/hello1-1")
	public String hello() {
		//返回逻辑视图名称
		return "hello";
	}

	//多个映射可以指向同一个方法
	@RequestMapping({"/hello2-1", "/hello2-2"})
	public String hello() {
		//返回逻辑视图名称
		return "hello";
	}
}
```

### value属性Ant风格

value属性也支持`Ant风格`的通配符：

- `?`:匹配任意单个字符
- `*`:匹配任意多个字符
- `**`:匹配任意多个字符（包括目录,即`/`）

!!! warning

    如果使用`**`，左右两边只能是`/`。

``` java

@Controller
public class HelloController {
	//?表示任意单个字符，比如 hello1 或 helloa 都会访问到该方法
	@RequestMapping("/hello?")
	public String hello() {
		//返回逻辑视图名称
		return "hello";
	}
}
```

### value属性占位符

使用`占位符`，可以实现`Restful风格`的参数传递

方法中通过`@PathVariable`获取参数

``` java

@Controller
public class HelloController {
	@RequestMapping("/login/{username}/{password}")
	public String login(@PathVariable String username,
						@PathVariable String password) {
		//用户登录
		//...
		return "ok";
	}
}
```

### method属性

`method属性`用于限制请求方法，`method`属性的值可以是`GET`、`POST`、`PUT`、`DELETE`等。

``` java

@Controller
public class HelloController {
	//只会接收Get类型的请求
	@RequestMapping(value = "/hello", method = RequestMethod.GET)
	public String hello() {
		//返回逻辑视图名称
		return "hello";
	}
}
```

### 衍生Mapping

除了`@RequestMapping`注解，还可以使用`@GetMapping`、`@PostMapping`、`@PutMapping`、`@DeleteMapping`、`@PatchMapping`
等注解。表示具体method方法的请求。

### params属性

`params属性`对请求参数进行限制

``` java

@Controller
public class HelloController {
	//表示请求参数中必须存在username和password，且username必须为张三
	@PostMapping(value = "/hello", params = {"username=张三", "password"})
	public String hello() {
		//返回逻辑视图名称
		return "ok";
	}
}
```

### headers属性

`headers属性`对请求头进行限制

``` java

@Controller
public class HelloController {
	//表示请求头中必须存在token
	@PostMapping(value = "/hello", headers = {"token"})
	public String hello() {
		//返回逻辑视图名称
		return "ok";
	}
}
```

## 请求参数处理⭐

### 消息转换器

`消息转换器`可以将HTTP请求的消息转换为Java对象，或者将Java对象转换为HTTP响应。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/diagrams/java_serve/web_application/mvc/MVC%E6%B6%88%E6%81%AF%E8%BD%AC%E6%8D%A2%E5%99%A8.svg){ loading=lazy }
  <figcaption>消息转换器接口和实现类</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409042205422.png){ loading=lazy }
  <figcaption>消息转换器作用</figcaption>
</figure>

### Form请求-形参解析参数

``` java title="FormController.java"
--8<-- "code/java_serve/web_application/mvc/springmvc-parameters/src/main/java/com/luguosong/controller/FormController.java"
```

!!! warning "如果是Spring6+,想要省略@RequestParam注解，需要在pom.xml中配置`-parameters`标记"

    ```xml
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.13.0</version>
                <configuration>
                    <source>17</source>
                    <target>17</target>
                    <compilerArgs>
                        <arg>-parameters</arg>
                    </compilerArgs>
                </configuration>
            </plugin>
        </plugins>
    </build>
    ```

### Form请求-Bean对象解析参数

!!! note

    SpringMVC会使用`FormHttpMessageConverter`消息转换器将表单数据转为JavaBean。

``` java title="FormPojoController.java"
--8<-- "code/java_serve/web_application/mvc/springmvc-parameters/src/main/java/com/luguosong/controller/FormPojoController.java"
```

``` java title="User.java"
--8<-- "code/java_serve/web_application/mvc/springmvc-parameters/src/main/java/com/luguosong/pojo/User.java"
```

### Form请求-获取参数原始字符串

通过`@RequestBody注解`可以拿到请求参数的原始字符串。

``` java title="FormStringController.java"
--8<-- "code/java_serve/web_application/mvc/springmvc-parameters/src/main/java/com/luguosong/controller/FormStringController.java"
```

!!! note

    底层使用`FormHttpMessageConverter`消息转换器。

### JSON请求-Bean对象解析参数

在pom.xml引入处理json的依赖：

```xml
<!--负责json字符串和java对象之间的转换-->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.17.0</version>
</dependency>
```

通过`@RequestBody注解`可以将JSON格式的请求参数转为Java对象。

``` java title="JsonPojoController.java"
--8<-- "code/java_serve/web_application/mvc/springmvc-parameters/src/main/java/com/luguosong/controller/JsonPojoController.java"
```

### Get请求中文乱码问题

Tomcat8以及之前版本，解决Get请求中文乱码，在Tomcat服务器`CATALINA_HOME/conf/server.xml`中配置:

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202408211608413.png){ loading=lazy }
  <figcaption>解决Get请求乱码问题</figcaption>
</figure>

Tomcat8之后版本，请求行默认采用UTF-8编码，无需解决中文乱码问题。

### Post请求中文乱码问题

Tomcat9以及之前的版本，需要解决Post请求中文乱码问题。

在`Servlet编程`中，可以使用`request.setCharacterEncoding("UTF-8");`解决乱码问题。

但在Spring MVC中，无法在Controller中使用以上方法解决中文乱码。

解决方案一：编写`Servlet过滤器`，过滤器会在DispatcherServlet之前执行。因此在过滤器中设置
`request.setCharacterEncoding("UTF-8");`
可以解决乱码问题。

解决方案二：Spring MVC为我们提供了类似的过滤器类`CharacterEncodingFilter`，无需我们重新手写过滤器类。只需要在`web.xml`
中配置该过滤器并设置`encoding属性`即可。

```xml title="解决Post请求中文乱码问题"

<web-app>
    <filter>
        <filter-name>characterEncodingFilter</filter-name>
        <filter-class>org.springframework.web.filter.CharacterEncodingFilter</filter-class>
        <!--指定编码-->
        <init-param>
            <param-name>encoding</param-name>
            <param-value>UTF-8</param-value>
        </init-param>
        <!--是否强制设置编码-->
        <init-param>
            <param-name>forceRequestEncoding</param-name>
            <param-value>true</param-value>
        </init-param>
        <!--是否强制设置编码-->
        <init-param>
            <param-name>forceResponseEncoding</param-name>
            <param-value>true</param-value>
        </init-param>
    </filter>
    <filter-mapping>
        <filter-name>characterEncodingFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
</web-app>
```

Tomcat10请求体默认采用UTF-8编码，无需解决中文乱码问题。

### RequestEntity对象

`RequestEntity对象`中存储了所有请求信息，包括请求行、请求头、请求体。

RequestEntity的`泛型`对应请求体信息，如果是String表示请求体字符串，如果是实体类会将请求体转换为实体类。

``` java title="RequestEntityController.java"
--8<-- "code/java_serve/web_application/mvc/springmvc-parameters/src/main/java/com/luguosong/controller/RequestEntityController.java"
```

### 文件上传

❗Spring MVC 5以及之前版本在pom.xml引入处理文件的依赖：

```xml
<!--负责文件上传-->
<!--Spring MVC 6之后不再需要添加此依赖-->
<dependency>
    <groupId>commons-fileupload</groupId>
    <artifactId>commons-fileupload</artifactId>
    <version>1.5</version>
</dependency>
```

上传参数配置：

=== "Spring MVC 6在web.xml中配置"

    ```xml
      
      <web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
               xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
               xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd"
               version="6.0">
          <servlet>
              <servlet-name>springmvc</servlet-name>
              <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
              <!--配置上传参数-->
              <multipart-config>
                  <max-file-size>102400</max-file-size>
                  <!--设置整个表单所有文件上传的最大值-->
                  <max-request-size>102400</max-request-size>
                  <!--最小上传大小-->
                  <file-size-threshold>0</file-size-threshold>
              </multipart-config>
          </servlet>
          <servlet-mapping>
              <servlet-name>springmvc</servlet-name>
              <url-pattern>/</url-pattern>
          </servlet-mapping>
      </web-app>
    ```

=== "Spring MVC 5在Spring MVC 配置文件中配置"

    ```xml
      
      <beans>
          <bean id="multipartResolver" class="org.springframework.web.multipart.commons.CommonsMultipartResolver">
              <property name="maxUploadSizePerFile" value="#{10*1024*1024}"/>
              <property name="maxUploadSize" value="#{100*1024*1024}"/>
          </bean>
      </beans>
    ```

文件上传必须时post请求，因为文件数据需要通过请求体传递，get请求没有请求体。

设置请求参数类型为`multipart/form-data`。

```html

<form method="post" th:action="@{/fileUpload/springMvc}" enctype="multipart/form-data">
    文件上传：<input type="file" name="fileName">
    <input type="submit" value="文件上传">
</form>
```

Controller通过`MultipartFile对象`接收文件：

``` java title="FileUploadController.java"
--8<-- "code/java_serve/web_application/mvc/springmvc-parameters/src/main/java/com/luguosong/controller/FileUploadController.java"
```

## 响应结果处理⭐

### 返回逻辑视图名称

默认情况下，Controller返回String，回转到对应的视图解析器进行视图解析。

### 响应纯字符串

默认情况下，Controller返回String，回转到对应的视图解析器进行视图解析。

可以通过`@ResponseBody注解`返回String字符串，此时返回的不再是逻辑视图名称，而是直接返回`text/html`。

!!! note

    @ResponseBody采用的是`StringHttpMessageConverter`消息转换器将String字符串转换为`text/html`格式。

``` java title="ResponseStringController.java"
--8<-- "code/java_serve/web_application/mvc/springmvc-parameters/src/main/java/com/luguosong/controller/ResponseStringController.java"
```

### 响应JSON字符串

在pom.xml引入处理json的依赖：

```xml
<!--负责json字符串和java对象之间的转换-->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.17.0</version>
</dependency>
```

在spring mvc配置文件中需要配置：

```xml

<mvc:annotation-driven/>
```

``` java title="ResponseJSONStringController.java"
--8<-- "code/java_serve/web_application/mvc/springmvc-parameters/src/main/java/com/luguosong/controller/ResponseJSONStringController.java"
```

!!! note

    当处理器方法上面有@ResponseBody注解，并返回一个Java对象，SpringMVC会自动将对象转为json字符串并响应。

    此时使用的是`MappingJackson2HttpMessageConverter`消息转换器。

### RestController注解

在类上添加`@RestController注解`，等同于在该类上添加了`@Controller注解`，同时为该类的所有方法添加了`@ResponseBody注解`。

### ResponseEntity对象

`ResponseEntity对象`可以定制响应协议，包括状态行、响应头和响应体。当想自定定制响应协议时，可以使用该类。

``` java title="ResponseEntityController.java"
--8<-- "code/java_serve/web_application/mvc/springmvc-parameters/src/main/java/com/luguosong/controller/ResponseEntityController.java"
```

### 文件下载

``` java title="FileDownloadController.java"
--8<-- "code/java_serve/web_application/mvc/springmvc-parameters/src/main/java/com/luguosong/controller/FileDownloadController.java"
```

## 获取请求头信息

### 获取请求头信息

``` java title="HeaderInfoController.java"
--8<-- "code/java_serve/web_application/mvc/springmvc-header-info/src/main/java/com/luguosong/controller/header_info/HeaderInfoController.java"
```

### 获取Cookie信息

``` java title="CookieController.java"
--8<-- "code/java_serve/web_application/mvc/springmvc-header-info/src/main/java/com/luguosong/controller/header_info/CookieController.java"
```

## 域对象操作

### request域

``` java title="RequestScopeController.java"
--8<-- "code/java_serve/web_application/mvc/springmvc-scope/src/main/java/com/luguosong/controller/RequestScopeController.java"
```

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202408290914385.png){ loading=lazy }
  <figcaption>BindingAwareModelMap类结构</figcaption>
</figure>

不管是`Model对象`、`Map集合`还是`ModelMap对象`，实际创建的的都是`BindingAwareModelMap对象`。

Spring MVC为了更好的体现MVC架构模式，还提供了`ModelAndView类`
，这个类封装了Model和View。也就是说这个类封装业务处理之后的数据，体支持跳转指定视图。通过ModelAndView也可以设置请求域。

### session域

``` java title="SessionScopeController.java"
--8<-- "code/java_serve/web_application/mvc/springmvc-scope/src/main/java/com/luguosong/controller/SessionScopeController.java"
```

一般情况下`modelAndView.addObject`是设置request域的，但通过`@SessionAttributes({"xxx"})`注解可以指定特定字段为session域。

### application域

``` java title="ApplicationScopeController.java"
--8<-- "code/java_serve/web_application/mvc/springmvc-scope/src/main/java/com/luguosong/controller/ApplicationScopeController.java"
```

一般直接采用Servlet原始方式设置application域。

## 视图(View)

### 常见的视图

- `InternalResourceView`:内部资源视图，Spring MVC框架内置，专门为JSP模板语法准备
- `RedirectView`：重定向视图，Spring MVC框架内置,用来完成重定向效果
- `ThymeLeafView`:Thymeleaf 是一种现代化的服务器端 Java 模板引擎，适用于网页和独立环境。Thymeleaf
  的主要目标是为您的开发流程带来优雅的自然模板——这些 HTML 可以在浏览器中正确显示，同时也能作为静态原型使用，从而增强开发团队的协作。它提供了
  Spring Framework 的模块、与您喜爱的工具的多种集成，并允许您插入自己的功能，因此 Thymeleaf 非常适合现代 HTML5 JVM
  的网页开发——尽管它的功能远不止于此。
- `FreeMarkerView`：Apache FreeMarker™ 是一个模板引擎：它是一个 Java 库，用于根据模板和变化的数据生成文本输出（如 HTML
  网页、电子邮件、配置文件、源代码等）。模板使用 FreeMarker 模板语言（FTL）编写，这是一种简单的专用语言（不像 PHP
  那样是完整的编程语言）。通常，会使用通用编程语言（如 Java）来准备数据（执行数据库查询、进行业务计算）。然后，Apache FreeMarker
  使用模板展示这些准备好的数据。在模板中，你专注于如何展示数据，而在模板之外，你专注于展示哪些数据。
- `VelocityView`:VelocityView 包含所有的 GenericTools，并增加了在 Web 应用程序（Java EE 项目）视图层中使用 Velocity
  的基础设施和专用工具。这包括用于处理 Velocity 模板请求的 VelocityViewServlet 或 VelocityLayoutServlet，以及用于在 JSP
  中嵌入 Velocity 的 VelocityViewTag。
- `PDFView`:第三方，用于生成pdf文件视图
- `ExcelView`:第三方，用于生成excel文件视图

### 配置JSP视图解析器

``` xml title="springmvc-servlet.xml"
--8<-- "code/java_serve/web_application/mvc/springmvc-view-jsp/src/main/webapp/WEB-INF/springmvc-servlet.xml"
```

### 配置Thymeleaf视图解析器

``` xml title="springmvc-servlet.xml"
--8<-- "code/java_serve/web_application/mvc/springmvc-hello/src/main/webapp/WEB-INF/springmvc-servlet.xml"
```

### 视图控制器

如何仅仅是进行视图转发，无需编写Controller类，可以通过spring mvc配置文件`mvc:view-controller`标签进行配置。

``` xml title="springmvc-servlet.xml"
--8<-- "code/java_serve/web_application/mvc/springmvc-view-controller/src/main/webapp/WEB-INF/springmvc-servlet.xml"
```

## 转发和重定向

``` java
--8<-- "code/java_serve/web_application/mvc/springmvc-forward-redirect/src/main/java/com/luguosong/controller/TestController.java"
```

## 静态资源访问

由于DispatcherServlet的`url-pattern`配置的是`/`,访问静态资源会经过`DispatcherServlet`。`DispatcherServlet`没有静态资源处理。

### 方式一:开启默认Servlet

Tomcat目录`conf/web.xml`中存在名为`default`的servlet

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409021617210.png){ loading=lazy }
  <figcaption>DefaultServlet</figcaption>
</figure>

其`url-pattern`与`DispatcherServlet`一样也是`/`，因此`默认servlet`访问被`DispatcherServlet`覆盖。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409021618904.png){ loading=lazy }
  <figcaption>default Servlet servlet-mapping</figcaption>
</figure>

Spring MVC仍然允许静态资源请求由Tomcat的`默认Servlet`处理。它配置了一个`DefaultServletHttpRequestHandler`，URL映射为`/**`
，并且相对于其他URL映射具有`最低优先级`。

以下示例展示了如何通过默认设置启用该功能：

``` java

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}
}
```

以下示例展示了如何在Spring MVC配置文件 XML 中实现相同的配置：

```xml

<beans>
    <mvc:default-servlet-handler/>
    <mvc:annotation-driven/>
</beans>
```

### 方式二：配置静态资源处理

在下一个示例中，对于以 /resources 开头的请求，将使用相对路径来查找和提供相对于 Web 应用程序根目录下的 /public 或类路径下的
/static 的静态资源。这些资源的过期时间设置为一年，以确保最大限度地利用浏览器缓存并减少浏览器发出的 HTTP 请求。Last-Modified
信息通过 Resource#lastModified 推断，以支持带有 "Last-Modified" 头的 HTTP 条件请求。

以下列表展示了如何使用 Java 配置来实现：

``` java

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/resources/**")
				.addResourceLocations("/public", "classpath:/static/")
				.setCacheControl(CacheControl.maxAge(Duration.ofDays(365)));
	}
}
```

以下示例展示了如何在 XML 中实现相同的配置：XML

```xml

<beans>
    <mvc:resources mapping="/resources/**"
                   location="/public, classpath:/static/"
                   cache-period="31556926"/>
    <mvc:annotation-driven/>
</beans>
```

## RESTFul

### 概述

RESTFul是web服务接口的一种`设计风格`。提供了一套约束，可以让web服务接口更加简介、易于理解。

- 查询：使用GET方法请求
- 添加：使用POST方法请求
- 更新：使用PUT方法请求
- 删除：使用DELETE方法请求

请求参数从`/springmvc/getUserById?id=1`风格转为`/springmvc/user/1`风格,变得更加简洁。

### HiddenHttpMethodFilter

理论上表单只能发送`get请求`和`post请求`。

但是可以借助`HiddenHttpMethodFilter过滤器`，将`post`方法转为`put`、`delete`或`patch`方法。

### 示例

模拟通过表单发送`get`、`post`、`put`、`delete`请求。

在web.xml中配置`HiddenHttpMethodFilter过滤器`：

``` xml title="web.xml"
--8<-- "code/java_serve/web_application/mvc/springmvc-restful/src/main/webapp/WEB-INF/web.xml"
```

Controller中的`@RequestMapping`地址是一样的，通过`请求方法`区分请求：

``` java
--8<-- "code/java_serve/web_application/mvc/springmvc-restful/src/main/java/com/luguosong/controller/TestController.java"
```

表单发送不同方法的请求：

``` html
--8<-- "code/java_serve/web_application/mvc/springmvc-restful/src/main/webapp/WEB-INF/templates/test.html"
```

## 异常处理器

Controller在执行过程中发生异常，通过`异常处理器`跳转到对应视图，在视图上展示友好信息。

Spring MVC提供了一个接口：`HandlerExceptionResolver`，用于处理异常。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409101004922.png){ loading=lazy }
  <figcaption>异常处理器接口</figcaption>
</figure>

### DefaultHandlerExceptionResolver

`DefaultHandlerExceptionResolver`是Spring MVC默认的异常处理器。

比如Post方法的Controller方法，通过Get请求访问，就会进入这个处理器。

### SimpleMappingExceptionResolver

SimpleMappingExceptionResolver可以让我们自定义异常处理。

方式一：通过spring mvc配置文件配置`SimpleMappingExceptionResolver`

``` xml title="springmvc-servlet.xml"
--8<-- "code/java_serve/web_application/mvc/springmvc-exception-xml/src/main/webapp/WEB-INF/springmvc-servlet.xml"
```

---

方式二：使用注解

``` java title="ExceptionController.java"
--8<-- "code/java_serve/web_application/mvc/springmvc-exception-annotation/src/main/java/com/luguosong/controller/ExceptionController.java"
```

## 拦截器(Interceptor)

### 概述

Spring MVC`拦截器`作用是在请求到达Controller`之前`和`之后`进行拦截，可以对请求和响应进行一些特殊处理。

常见用途：

- 登录验证
- 权限校验
- 请求日志
- 更改响应

### 示例

通过实现`HandlerInterceptor接口`，实现拦截器。

`preHandle方法`如果`返回false`，请求将被拦截，不会再执行后续的拦截器和Controller。

编写拦截器：

``` java title="Interceptor1.java"
--8<-- "code/java_serve/web_application/mvc/springmvc-interceptor-hello/src/main/java/com/luguosong/interceptors/Interceptor1.java"
```

``` java title="Interceptor2.java"
--8<-- "code/java_serve/web_application/mvc/springmvc-interceptor-hello/src/main/java/com/luguosong/interceptors/Interceptor2.java"
```

在spring mvc配置文件中配置拦截器：

``` xml title="springmvc-servlet.xml"
--8<-- "code/java_serve/web_application/mvc/springmvc-interceptor-hello/src/main/webapp/WEB-INF/springmvc-servlet.xml"
```

## 全注解开发

编写Spring 配置类，继承`AbstractAnnotationConfigDispatcherServletInitializer`类，相当于`web.xml`：

``` java title="WebAppInitialize.java"
--8<-- "code/java_serve/web_application/mvc/springmvc-hello-annotation/src/main/java/com/luguosong/config/WebAppInitialize.java"
```

Spring MVC配置类：

``` java title="SpringMvcConfig.java"
--8<-- "code/java_serve/web_application/mvc/springmvc-hello-annotation/src/main/java/com/luguosong/config/SpringMvcConfig.java"
```


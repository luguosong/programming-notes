# JSP

## 设置网站欢迎页

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405191755672.png){ loading=lazy }
  <figcaption>Tomcat全局配置欢迎页</figcaption>
</figure>

项目自定义配置欢迎页：

```xml

<web-app>
    <welcom-file-list>
        <welcom-file>login.jsp</welcom-file>
    </welcom-file-list>
</web-app>
```

## JSP概述

`JSP`（全称`Jakarta Server Pages`，曾称为`JavaServer Pages`）是由Sun微系统公司主导建立的一种动态网页技术标准。

!!! note "JSP和Servlet"

    JSP文件在运行时会被其编译器转换成更原始的`Servlet`程序码。`JSP编译器`可以把`JSP文件`转换成用Java程序码写的`Servlet`，然后再由`Java编译器`来编译成能快速执行的二进制机器码，也可以直接编译成二进制码。

JSP将Java程序码和特定变动内容嵌入到静态的页面中，实现以静态页面为模板，动态生成其中的部分内容。

JSP引入了被称为`JSP动作`的XML标签，用来调用内置功能。

另外，可以创建`JSP标签库`，然后像使用标准HTML或XML标签一样使用它们。标签库能增强功能和服务器性能，而且不受跨平台问题的限制。

## Hello World

`.jsp`文件直接编写在`WEB-INF目录`之外，可以直接访问。

``` jsp title="hello_jsp/src/main/webapp/index.jsp"
--8<-- "code/java_serve/web_application/jsp/hello_jsp/src/main/webapp/index.jsp"
```

Tomcat会将`.jsp`文件编译成`Servlet`。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405221757065.png){ loading=lazy }
  <figcaption>jsp文件被转换为Servlet代码</figcaption>
</figure>

## JSP中的注释

JSP采用这种注释不会被翻译到`Servlet`代码中去：

``` jsp
<%--JSP中的注释--%>
```

## JSP中编写Java代码

### <% %>

JSP中通过`<% %>`添加Java代码，添加的代码会在`service()方法`中执行。

``` jsp
--8<-- "code/java_serve/web_application/jsp/hello_jsp/src/main/webapp/java_in_jsp1.jsp"
```

### <%= %>

`<%= %>`等价于在`service()方法`中执行`out.write();`

``` jsp
--8<-- "code/java_serve/web_application/jsp/hello_jsp/src/main/webapp/java_in_jsp2.jsp"
```

### <%! %>

`<%! %>`中的代码会在`service()方法`之外执行。

!!! warning

    一般不常使用，因为Servlet在Tomcat中是单例的，会存在线程安全问题。

``` jsp
--8<-- "code/java_serve/web_application/jsp/hello_jsp/src/main/webapp/java_in_jsp3.jsp"
```

## JSP内置对象

### request对象

```jsp
<a href="<%=request.getContextPath()%>/hello-servlet">Hello Servlet</a>
```

使用request`请求域`进行数据查询流程：

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/diagrams/java_serve/web_application/jsp/request%E8%AF%B7%E6%B1%82%E5%9F%9F%E6%95%B0%E6%8D%AE%E6%9F%A5%E8%AF%A2%E6%B5%81%E7%A8%8B.svg){ loading=lazy }
  <figcaption>通过请求域对象查询数据</figcaption>
</figure>

### pageContext对象

页面作用域，用于访问页面范围内的属性，并管理JSP页面的各种范围（如request、session、application）。

### session对象

会话作用域，等价于：

``` java
public class GetServletDemo extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
    }
}
```

### application对象

应用作用域

### response对象

负责响应

### out对象

负责输出

### exception对象

用于处理JSP页面抛出的未处理异常。

### config对象

Servlet配置对象，等价于：

``` java
public class ServletConfigDemo extends GenericServlet {
    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        ServletConfig config = getServletConfig();
    }
}
```

### page对象

表示当前Servlet对象

## 指令

`指令`指导JSP翻译引擎如何工作。

``` jsp
// include指令,包含其他文件,在JSP中完成静态包含,很少用了。
<%include file="request_object.jsp"%>

// 引入标签库指令
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

// page指令
<%@ page contentType="text/html; charset=UTF-8" language="java" %>
```

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405280924354.png){ loading=lazy }
  <figcaption>与Page指令相关的属性</figcaption>
</figure>

## EL表达式

`EL表达式`（Expression Language）是一种用于在JavaServer Pages（JSP）和JavaServer Faces（JSF）等Java Web应用程序中嵌入表达式的语言。它允许开发人员在页面上直接访问和操作JavaBean组件的属性，以及调用Java方法，从而简化了在页面上处理数据的过程。 EL表达式通常用于动态地获取和显示数据，以及执行条件判断和迭代操作。

`EL表达式`用于代替JSP中的Java代码，让JSP变得整洁。

### 获取域数据

从`PageContext域`、`请求域`、`会话域`、`应用域`中获取数据

```jsp
// 获取域数据
${username}
```

### 指定域范围

El表达式优先级，`域的范围越小，优先级越高`：

``` jsp
--8<-- "code/java_serve/web_application/jsp/jsp_el/src/main/webapp/priority.jsp"
```

### pageContext使用

`EL表达式`可以与`pageContext对象`结合使用，获取内置对象。

!!! warning

    EL表达式中是无法直接使用内置对象的。

``` jsp
--8<-- "code/java_serve/web_application/jsp/jsp_el/src/main/webapp/pageContext.jsp"
```

### param、paramValues使用

param用于获取请求参数。

``` jsp
--8<-- "code/java_serve/web_application/jsp/jsp_el/src/main/webapp/param.jsp"
```

```text title="输入携带参数的请求"
http://localhost:8080/jsp_el_war_exploded/param.jsp?username=%E5%BC%A0%E4%B8%89&hobby=%E6%8A%BD%E7%83%9F&hobby=%E5%96%9D%E9%85%92&hobby=%E7%83%AB%E5%A4%B4
```

### initParam使用

``` xml title="设置应用初始化参数"
--8<-- "code/java_serve/web_application/jsp/jsp_el/src/main/webapp/WEB-INF/web.xml"
```

``` jsp
--8<-- "code/java_serve/web_application/jsp/jsp_el/src/main/webapp/initParam.jsp"
```

## JSTL标签库

`JSTL`（JavaServer Pages Standard Tag Library）是一组用于简化JavaServer Pages（JSP）开发的标准标签。它提供了一套标签，用于执行常见的任务，如迭代集合、条件判断、格式化数据等。 JSTL使开发人员能够在JSP页面中使用标签而不是Java代码来实现常见的Web应用逻辑。

### Maven依赖

```xml

<dependencies>
    <dependency>
        <groupId>org.glassfish.web</groupId>
        <artifactId>jakarta.servlet.jsp.jstl</artifactId>
        <version>3.0.1</version>
    </dependency>
    <dependency>
        <groupId>jakarta.servlet.jsp.jstl</groupId>
        <artifactId>jakarta.servlet.jsp.jstl-api</artifactId>
        <version>3.0.0</version>
    </dependency>
</dependencies>
```

### 案例

``` jsp
--8<-- "code/java_serve/web_application/jsp/jsp_jstl/src/main/webapp/jstlHello.jsp"
```

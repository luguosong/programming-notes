# Session和Cookie

## Session入门案例

``` jsp title="获取session相关信息"
--8<-- "code/java-serve/web_application/session_cookie/session_and_cookie/src/main/webapp/hello_session.jsp"
```

``` java title=""
--8<-- "code/java-serve/web_application/session_cookie/session_and_cookie/src/main/java/com/luguosong/AddSession.java"
```

``` java
--8<-- "code/java-serve/web_application/session_cookie/session_and_cookie/src/main/java/com/luguosong/DeleteSession.java"
```

## Session原理图解

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/diagrams/java-serve/web_application/session_cookie/session%E5%8E%9F%E7%90%86%E5%9B%BE%E8%A7%A3.svg){ loading=lazy }
  <figcaption>Session原理图解</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405241645997.png){ loading=lazy }
  <figcaption>第一次访问Servlet，服务端会返回Set-Cookie响应头。浏览器会将JSESSIONID存储到Cookie中去</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405241647182.png){ loading=lazy }
  <figcaption>后续访问，请求头Cookie会携带JSESSIONID信息</figcaption>
</figure>

## Session过期时间

Session的默认超时时间是30分钟。

可以在Web.xml中进行自定义配置：

``` xml title="web.xml"
--8<-- "code/java-serve/web_application/session_cookie/session_and_cookie/src/main/webapp/WEB-INF/web.xml"
```

!!! note

    每次访问Servlet，Session的剩余超时时间都会刷新。

## Cookie概述

`HTTP cookie`，简称`cookie`，是浏览网站时由网络服务器创建并由网页浏览器存放在用户计算机或其他设备的小文本文件。

Cookie使Web服务器能`在用户的设备存储状态信息`（如添加到在线商店购物车中的商品）或跟踪用户的浏览活动（如点击特定按钮、登录或记录历史）。

## Cookie有效期

- 不设置（默认,等价于小于0）：运行`内存`中，浏览器关闭后Cookie消失。
- 大于0：保存在`硬盘`上，浏览器关闭Cookie仍然存在。
- 等于0：表示Cookie被删除，通常用这种方式删除浏览器上的同名Cookie。

## 请求何时携带Cookie

- `默认路径`：如果不设置路径，Cookie的默认路径是创建该Cookie的Web应用的`上下文路径`。
- `路径匹配`：只有请求URL的路径与`Cookie的路径`匹配时，浏览器才会在请求中发送该Cookie。

只有url在Cookie路径之下，request请求中才会包含对应的Cookie信息。

## Cookie入门案例

``` jsp
--8<-- "code/java-serve/web_application/session_cookie/session_and_cookie/src/main/webapp/hello_cookie.jsp"
```

``` java
--8<-- "code/java-serve/web_application/session_cookie/session_and_cookie/src/main/java/com/luguosong/AddCookie.java"
```

``` java
--8<-- "code/java-serve/web_application/session_cookie/session_and_cookie/src/main/java/com/luguosong/DeleteCookie.java"
```




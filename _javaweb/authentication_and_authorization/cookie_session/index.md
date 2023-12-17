---
layout: note
title: 会话跟踪
nav_order: 10
parent: 认证和鉴权
create_time: 2023/9/26
---

# 会话跟踪技术

http请求本身是无状态的，`会话跟踪技术`可以让服务器知道当前请求是`哪个用户`发出的。

# Cookie

Cookie是基于Http协议的。

`响应`通过响应头`Set-Cookie`设置cookie，浏览器会将cookie保存在本地，下次请求时请求头会自动带上cookie。

服务端通过响应设置Cookie:

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202309272216675.png)

浏览器再次发起请求，会携带最新的Cookie：

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202309272218388.png)

{: .warning}
> 如果不设置过期时间，Cookie只存在于浏览器的内存当中，浏览器关闭，Cookie将被清空。

{: .warning}
> Cookie不可以存储中文，需要URL编码后再存储。

{% highlight java %}
{% include_relative cookie-hello/src/main/java/com/example/cookiehello/controller/TestController.java %}
{% endhighlight %}

# Session

Session是基于Cookie的。创建Session时，会在Cookie中设置一个`JSESSIONID`，浏览器再次请求时，会携带`JSESSIONID`
，服务器通过`JSESSIONID`找到对应的Session。但程序员无需关注这些细节，使用时只需要调用`request.getSession()`即可。

{% highlight java %}
{% include_relative session-hello/src/main/java/com/example/sessionhello/controller/SessionController.java %}
{% endhighlight %}

{: .warning}
> 浏览器关闭，Session不会被清空，但是Cookie会被清空，下次请求时，服务器无法通过Cookie找到对应的Session，所以Session也就无法使用了。




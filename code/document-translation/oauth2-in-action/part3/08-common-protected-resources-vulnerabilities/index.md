# 8.常见的受保护资源漏洞

本章涵盖

- 避免在受保护资源中常见的实现漏洞
- 盘点针对受保护资源的已知攻击
- 在设计受保护资源的端点时，善用现代浏览器的防护能力

在上一章中，我们回顾了针对 `OAuth` 客户端的常见攻击。现在，是时候看看如何保护资源服务器，并防御那些专门瞄准 `OAuth`
受保护资源的常见攻击了。本章将学习如何设计资源端点，把令牌伪造（`token spoofing`）和令牌重放（`token replay`
）的风险降到最低。我们还会看到如何借助现代浏览器的防护机制，让设计者的工作更轻松。

## 受保护资源为何会存在漏洞？

受保护资源可能以多种方式暴露风险，其中最明显的一点是访问令牌（`access token`）可能泄露，从而让攻击者获取受保护资源的相关数据。这种情况可能源于上一章提到的令牌劫持（
`token hijacking`），也可能是因为令牌熵（`entropy`）不足，或作用域（`scope`）过于宽泛。

另一个与受保护资源相关的问题是：端点可能会受到跨站脚本（`XSS`）攻击。确实如此——如果资源服务器选择支持将 `access_token` 作为
`URI` 参数，[^1] 攻击者就能构造一个包含 `XSS` 攻击代码的 `URI`，再通过社会工程手段诱骗受害者点击该链接。[^2]
这甚至可以非常简单：比如发布一篇带有应用评测的博客文章，邀请大家去体验。当有人点击链接时，恶意的 `JavaScript` 代码就会被执行。

[^1]: RFC
6750 [https://tools.ietf.org/html/rfc6750#section-2.3](https://datatracker.ietf.org/doc/html/rfc6750#section-2.3)

[^2]: [http://intothesymmetry.blogspot.ch/2014/09/bounty-leftover-part-2-target-google.html](https://blog.intothesymmetry.com/2014/09/bounty-leftover-part-2-target-google.html)

!!! note "什么是 `XSS`？"

	跨站脚本攻击（`XSS`）在 `开放式 Web 应用安全项目（OWASP）Top Ten` 中排名第三[^3]，并且迄今仍是最常见的 Web 应用安全漏洞。攻击者会将恶意脚本注入到原本正常且可信的网站中，从而绕过诸如 `同源策略` 等访问控制机制。由此，攻击者可能注入脚本并按其目的篡改 Web 应用，例如窃取可用于冒充已认证用户的数据，或植入恶意代码交由浏览器执行。

[^3]: [https://www.owasp.org/index.php/Top_10_2013-A3-Cross-Site_Scripting_(XSS)](https://owasp.org/www-project-top-ten/)

## 受保护资源端点的设计

设计一个 Web API 是一项相当复杂的工作（任何 `API` 都是如此），需要综合考虑诸多因素。本节将介绍如何设计一个安全的 Web `API`
，充分利用现代浏览器所提供的各种安全能力。如果你在设计一个 `REST API`，且返回结果会受到用户输入的影响，那么遭遇 `XSS`
漏洞的风险就会很高。我们需要在任何对外暴露 Web 资源的环节，尽可能结合现代浏览器的特性与一些通用的最佳实践来降低风险。

作为一个具体示例，我们将引入一个新的端点 `(/helloWorld)`，同时新增一个新的作用域 `greeting`。这个新的 `API` 将如下所示：

```shell
GET /helloWorld?language={language}
```

该端点相当简单：它会根据输入的语言向用户致意。目前支持的语言见`表 8.1`；输入其他语言将会返回错误。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260105104051499.png){ loading=lazy }
  <figcaption>`表 8.1`：我们的测试 `API` 支持的语言</figcaption>
</figure>

### 如何保护资源端点

你可以在 `ch-8-ex-1` 中看到该端点的实现。打开该目录下的 `protectedResource.js` 文件。把页面往下翻到文件底部，就能看到我们这部分功能相当简洁的实现。

``` javascript
app.get("/helloWorld", getAccessToken, function(req, res){
  if (req.access_token) {
      if (req.query.language == "en") {
            res.send('Hello World');
      } else if (req.query.language == "de") {
            res.send('Hallo Welt');
      } else if (req.query.language == "it") {
            res.send('Ciao Mondo');
      } else if (req.query.language == "fr") {
            res.send('Bonjour monde');
      } else if (req.query.language == "es") {
            res.send('Hola mundo');
      } else {
            res.send("Error, invalid language: "+ req.query.language);
      }
  }
});
```

为试用前面的示例，请同时运行这三个组件，并按图 `8.1` 所示照常完成 `OAuth` 的“dance”（授权流程）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260105104439180.png){ loading=lazy }
  <figcaption>图 8.1：带有 `greeting` 作用域的访问令牌（`Access Token`）</figcaption>
</figure>

点击 `Greet In` 按钮后，你就可以请求一条英文问候语，这会触发客户端调用受保护的资源并展示结果（见图 `8.2`）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260105104526095.png){ loading=lazy }
  <figcaption>`图 8.2`：`英文`问候</figcaption>
</figure>

选择另一种语言（例如 `German`）后，将显示如 `Figure 8.3` 所示的内容。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260105104607901.png){ loading=lazy }
  <figcaption>图 8.3：`德语`问候语</figcaption>
</figure>

如果不支持该语言，将会显示一条错误信息，如图 `8.4` 所示。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260105104650235.png){ loading=lazy }
  <figcaption>`图 8.4`：`无效语言`</figcaption>
</figure>

也可以直接访问资源端点，并通过命令行 HTTP 客户端（例如 `curl`）传入 `access_token`：[^4]

[^4]: [https://curl.haxx.se/](https://curl.se/)

``` shell
> curl -v -H "Authorization: Bearer TOKEN" http://localhost:9002/helloWorld?language=en
```

或者利用之前对 `URI` 参数 `access_token` 的支持：

``` shell
> curl -v "http://localhost:9002/helloWorld?access_token=TOKEN&language=en"
```

在这两种情况下，最终的结果都会类似于下面这样的响应，它会用英文显示一条问候语：

``` shell
HTTP/1.1 200 OK
X-Powered-By: Express
Content-Type: text/html; charset=utf-8
Content-Length: 11
Date: Mon, 25 Jan 2016 21:23:26 GMT
Connection: keep-alive

Hello World
```

现在我们来试着调用 `/helloWorld` 端点，并传入一个无效的语言参数：

``` shell
> curl -v "http://localhost:9002/helloWorld?access_token=TOKEN&language=fi"
```

响应大致如下：由于 `Finnish` 不在支持的语言列表中，因此会显示一条错误提示信息。

``` shell
HTTP/1.1 200 OK
Content-Type: text/html; charset=utf-8
Content-Length: 27
Date: Tue, 26 Jan 2016 16:25:00 GMT
Connection: keep-alive

Error, invalid language: fi
```

到目前为止，一切顺利。但任何抓 `bug` 的人都会注意到，`/helloWorld` 端点的错误响应似乎是这样设计的：把错误输入原封不动地回显到响应里。我们不妨更进一步，试着塞进一段恶意的
`payload`。

``` shell
> curl -v   "http://localhost:9002/helloWorld?access_token=TOKEN&language=<script>alert('XSS')</script>"
```

这将生成：

``` shell
HTTP/1.1 200 OK
Content-Type: text/html; charset=utf-8
Content-Length: 59
Date: Tue, 26 Jan 2016 17:02:16 GMT
Connection: keep-alive

Error, invalid language: <script>alert('XSS')</script>
```

正如你所见，所提供的`payload`被原封不动地返回，且未经过任何净化处理。到这里，基本可以认定该端点很可能存在`XSS`
漏洞，接下来的步骤也相当简单。为了加以利用，攻击者会伪造一个指向受保护资源的恶意`URI`：

```shell
http://localhost:9002/helloWorld?access_token=TOKEN&language=<script>alert('XSS')</script>
```

当受害者点击它时，攻击即告完成，从而强制执行 `JavaScript`（见图 `8.5`）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260105111038478.png){ loading=lazy }
  <figcaption>图 8.5：受保护资源端点中的 `XSS`</figcaption>
</figure>

当然，真实的攻击不会只是弹出一个简单的 `JavaScript` `alert`，而是会植入恶意代码，例如窃取数据，使攻击者能够冒充已通过认证的用户。我们的接口显然存在
`XSS` 漏洞，因此必须修复。此时，推荐的做法是对所有不可信数据进行正确的转义。这里我们使用 `URL` 编码。

``` javascript
app.get("/helloWorld", getAccessToken, function(req, res){
  if (req.access_token) {
      if (req.query.language == "en") {
            res.send('Hello World');
      } else if (req.query.language == "de") {
            res.send('Hallo Welt');
      } else if (req.query.language == "it") {
            res.send('Ciao Mondo');
      } else if (req.query.language == "fr") {
            res.send('Bonjour monde');
      } else if (req.query.language == "es") {
            res.send('Hola mundo');
      } else {
            res.send("Error, invalid language: "+
                                            querystring.escape(req.query.language));
      }
  }
});
```

有了这个修复之后，伪造请求返回的错误响应大致会是下面这样：

``` shell
HTTP/1.1 200 OK
X-Powered-By: Express
Content-Type: text/html; charset=utf-8
Content-Length: 80
Date: Tue, 26 Jan 2016 17:36:29 GMT
Connection: keep-alive

Error, invalid language:
%3Cscript%3Ealert(%E2%80%98XSS%E2%80%99)%3C%2Fscript%3E
```

因此，浏览器会渲染响应，但不会执行那个 `rouge` 脚本（见图 `8.6`）。这就结束了吗？其实还没有。`输出净化` 是防御 `XSS`
的首选方案，但它是唯一的办法吗？`输出净化` 的问题在于，开发者经常会忘记做这一步；而且只要有哪怕一个输入字段忘了校验，`XSS`
防护就会瞬间回到原点。浏览器厂商也在努力遏制 `XSS`，并提供了一系列缓解措施，其中最重要的一项，就是为受保护的资源端点返回正确的
`Content-Type`。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260105111205525.png){ loading=lazy }
  <figcaption>图 8.6：受保护资源端点中的清理后响应</figcaption>
</figure>

按定义，[^5] `Content-Type` 实体首部字段用于指明发送给接收方的实体主体的媒体类型；或者在使用 `HEAD` 方法时，指明如果该请求改为
`GET` 会发送的媒体类型。

[^5]: RFC
7231 [https://tools.ietf.org/html/rfc7231#section-3.1.1.5](https://datatracker.ietf.org/doc/html/rfc7231#section-3.1.1.5)

返回正确的 `Content-Type` 往往能省去很多麻烦。回到我们最初那个未做净化处理的 `/helloWorld` 端点，看看该如何改进现状。原始响应是这样的：

```shell
HTTP/1.1 200 OK
X-Powered-By: Express
Content-Type: text/html; charset=utf-8
Content-Length: 27
Date: Tue, 26 Jan 2016 16:25:00 GMT
Connection: keep-alive

Error, invalid language: fi
```

此处的 `Content-Type` 是 `text/html`。这或许能解释为什么浏览器会在所示的 `XSS` 攻击中“乐于”执行那段恶意 `JavaScript`
。我们尝试换用不同的 `Content-Type`，比如 `application/json`：

``` javascript
app.get("/helloWorld", getAccessToken, function(req, res){
  if (req.access_token) {

      var resource = {
             "greeting" : ""
      };
      if (req.query.language == "en") {
             resource.greeting = 'Hello World';
      } else if (req.query.language == "de") {
             resource.greeting ='Hallo Welt';
      } else if (req.query.language == "it") {
             resource.greeting = 'Ciao Mondo';
      } else if (req.query.language == "fr") {
             resource.greeting = 'Bonjour monde';
      } else if (req.query.language == "es") {
             resource.greeting ='Hola mundo';
      } else {
             resource.greeting = "Error, invalid language: "+
             req.query.language;
      }
      res.json(resource);
  }
});
```

在这种情况下，

``` shell
> curl -v "http://localhost:9002/helloWorld?access_token=TOKEN&language=en"
```

将返回

``` shell
HTTP/1.1 200 OK
X-Powered-By: Express
Content-Type: application/json; charset=utf-8
Content-Length: 33
Date: Tue, 26 Jan 2016 20:19:05 GMT
Connection: keep-alive

{"greeting": "Hello World"}
```

如果这样， 

``` shell
> curl -v   "http://localhost:9002/helloWorld?access_token=TOKEN&language=<script>alert('XSS')</script>"
```

将产生如下输出：

``` shell
HTTP/1.1 200 OK
X-Powered-By: Express
Content-Type: application/json; charset=utf-8
Content-Length: 76
Date: Tue, 26 Jan 2016 20:21:15 GMT
Connection: keep-alive

{"greeting": "Error, invalid language: <script>alert('XSS')</script>" }
```

请注意，输出字符串并未以任何方式进行清理（sanitize）或编码（encode），但它现在被包裹在一个 `JSON` 字符串值中。如果我们直接在浏览器里这样尝试，就会发现只要设置了正确的 `Content-Type`，攻击就会立刻自行失效（见图 `8.7`）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260105112732510.png){ loading=lazy }
  <figcaption>图 8.7 受保护资源端点中的 `Content-Type application/json`</figcaption>
</figure>

这之所以会发生，是因为浏览器会遵循与 `-application/json` `Content-Type` 相关的“约定”，并拒绝在返回资源采用这种形式时执行 `JavaScript`。不过，如果客户端应用写得不够严谨，仍然完全可能在未对字符串进行转义的情况下，把 `JSON` 输出直接注入到 `HTML` 页面中，从而触发恶意代码执行。正如我们所说，这只是一种缓解手段；更好的做法依然是始终对输出进行清理与过滤。我们将这些要点归纳如下：

``` javascript
app.get("/helloWorld", getAccessToken, function(req, res){
  if (req.access_token) {

      var resource = {
            "greeting" : ""
      };
      if (req.query.language == "en") {
            resource.greeting = 'Hello World';
      } else if (req.query.language == "de") {
            resource.greeting ='Hallo Welt';
      } else if (req.query.language == "it") {
            resource.greeting = 'Ciao Mondo';
      } else if (req.query.language == "fr") {
            resource.greeting = 'Bonjour monde';
      } else if (req.query.language == "es") {
            resource.greeting ='Hola mundo';
      } else {
             resource.greeting = "Error, invalid language: "+ querystring.escape(req.query.language);
      }
      }
      res.json(resource);
  }
});
```

这确实有所改进，但我们还能做得更多，把安全性直接拉满。另一个很实用、且除 `Mozilla Firefox` 外所有浏览器都支持的响应头是 `X-Content-Type-Options: nosniff`。这个安全头最早由 `Internet Explorer`[^6] 引入，用于防止浏览器对响应进行 `MIME-sniffing`，从而偏离已声明的 `Content-Type`（以防万一）。另一个安全头是 `X-XSS-Protection`，它会自动启用大多数较新 Web 浏览器内置的 `XSS` 过滤器（同样不包括 `Mozilla Firefox`）。下面我们来看看如何在我们的端点中集成这些响应头。

[^6]: [https://blogs.msdn.microsoft.com/ie/2008/09/02/ie8-security-part-vi-beta-2-update](https://learn.microsoft.com/zh-cn/archive/blogs/ie/ie8-security-part-vi-beta-2-update)

``` javascript
app.get("/helloWorld", getAccessToken, function(req, res){
  if (req.access_token) {

      res.setHeader('X-Content-Type-Options', 'nosniff');
      res.setHeader('X-XSS-Protection', '1; mode=block');
      var resource = {
            "greeting" : ""
      };
      if (req.query.language == "en") {
             resource.greeting = 'Hello World';
      } else if (req.query.language == "de") {
             resource.greeting ='Hallo Welt';
      } else if (req.query.language == "it") {
             resource.greeting = 'Ciao Mondo';
      } else if (req.query.language == "fr") {
             resource.greeting = 'Bonjour monde';
      } else if (req.query.language == "es") {
             resource.greeting ='Hola mundo';
      } else {
             resource.greeting = "Error, invalid language: "+ querystring.escape(req.query.language);
      }
      res.json(resource);
  }
});
```

我们的响应会是这样：

``` shell
HTTP/1.1 200 OK
X-Powered-By: Express
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
Content-Type: application/json; charset=utf-8
Content-Length: 102
Date: Wed, 27 Jan 2016 17:07:50 GMT
Connection: keep-alive

{
    "greeting": "Error, invalid language:
    %3Cscript%3Ealert(%E2%80%98XSS%E2%80%99)%3C%2Fscript%3E"
}
```

这里仍有提升空间，那就是 `Content Security Policy (CSP)`。[^7] 这是另一种响应头（`Content-Security-Policy`）。正如规范中所述，它“通过在 `HTTP Header` 中声明允许加载哪些动态资源，帮助你在现代浏览器上降低 `XSS` 风险”。这个话题值得单独写一章，也并非本书的主要关注点；如何正确设置合适的 `CSP` 头字段，就留作读者的练习。


[^7]: [http://content-security-policy.com/](https://content-security-policy.com/)

资源服务器还可以做最后一件事，从根本上杜绝某个特定端点受到 `XSS` 影响的任何可能性：选择不支持通过请求参数传递 `access_token`。[^8] 这样一来，即便该端点理论上仍可能发生 `XSS`，也无法被利用，因为攻击者无法伪造一个同时包含访问令牌的 `URI`（访问令牌现在应通过 `Authorization: Bearer` 请求头发送）。这听起来或许过于苛刻，而且在某些场景下，使用这个请求参数可能确实是唯一可行的方案。不过，所有这类情况都应被视为例外，并以应有的谨慎态度对待。

[^8]: RFC 6750 [https://tools.ietf.org/html/rfc6750#section-2.3](https://datatracker.ietf.org/doc/html/rfc6750#section-2.3)

### 添加对Implicit Grant的支持

我们来实现一个资源端点，同时让它具备为支持第 6 章中详细介绍的 `Implicit Grant` 流程的 `OAuth` 客户端提供服务的能力。上一节讨论的所有安全注意事项依然适用，但我们还需要额外考虑一些因素。打开 `ch-8-ex-2` 文件夹，并执行其中的三个 `Node.js` 文件。

接着在浏览器中打开 `http://127.0.0.1:9000`，按惯例完成常见的 `OAuth` “舞步”。不过，当你尝试获取资源时，会遇到一个问题（见图 `8.8`）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260105132608915.png){ loading=lazy }
  <figcaption>图 8.8：`Same Origin Policy` 问题</figcaption>
</figure>

如果你打开浏览器的 `JavaScript` 控制台（或同类调试工具），会看到如下错误：

> Cross-Origin Request Blocked: The Same Origin Policy disallows reading the remote resource at http://localhost:9002/helloWorld. (Reason: CORS header ‘Access-Control-Allow-Origin’ missing).

那么，这到底是怎么回事？浏览器是在告诉我们：我们正在做一件不被允许的事情——试图用 `JavaScript` 去调用一个**不同源**的 `URL`，从而触犯了浏览器强制执行的 `Same Origin Policy`（同源策略）[^9]。具体来说，从运行在 `http://127.0.0.1:9000` 的隐式客户端，我们发起了一个指向 `http://127.0.0.1:9002` 的 `AJAX` 请求。本质上，同源策略的含义是：“只有当浏览器窗口来自同一个基础 `URL`（由 `protocol://domain:port` 组成）时，彼此之间才能在对方的上下文中工作。”在这里我们显然违反了这一规则，因为端口不一致：`9000` 对 `9002`。在实际的 Web 场景中，更常见的是：客户端应用部署在一个域名下，而受保护资源部署在另一个域名下，就像我们照片打印的例子一样。

[^9]: [https://en.wikipedia.org/wiki/Same-origin_policy](https://zh.wikipedia.org/wiki/%E5%90%8C%E6%BA%90%E7%AD%96%E7%95%A5)

!!! note "`Internet Explorer` 中的同源策略"

	在 `Internet Explorer` 里，练习 `8.1` 的那个错误不会出现。原因在这里有说明：`https://developer.mozilla.org/en-US/docs/Web/Security/Same-origin_policy#IE_Exceptions`。简单来说，`Internet Explorer` 在判定同源时不会把端口算进去；因此，`http://localhost:9000` 和 `http://localhost:9002` 会被认为是同一个源，自然也就不会施加任何限制。这一点和其他所有主流浏览器都不一样，而且在作者看来相当离谱。

同源策略的目的是防止某个页面里的 `JavaScript` 去加载来自另一个域的恶意内容。但在这个场景下，让 `JavaScript` 调用我们的 `API` 完全没问题，尤其是我们一开始就用 `OAuth` 保护了该 `API`。为了解决这个问题，我们直接采用 `W3C` 规范中的方案：[^10] `cross-origin resource sharing (CORS)`。在 `Node.js` 里添加 `CORS` 支持非常简单，而且如今在很多语言和平台上都已经很常见了。打开 `ch-8-ex-2` 文件夹中的 `protectedResource.js` 文件进行编辑，并引入 `CORS` 库：

[^10]: [https://www.w3.org/TR/cors/](https://fetch.spec.whatwg.org/)

``` javascript
var cors = require('cors');
```

然后把这个函数作为过滤器，放在其他函数之前。注意，我们这里也加入了对 `HTTP OPTIONS` 方法的支持，这样我们的 `JavaScript` 客户端就能在不发起完整请求的情况下，先获取关键的请求头信息，包括 `CORS` 相关的头部。

``` javascript
app.options('/helloWorld', cors());
app.get("/helloWorld", cors(), getAccessToken, function(req, res){
  if (req.access_token) {
```

其余的处理代码完全不需要改动。现在，当我们尝试完成一次完整的 `round-trip` 时，就能得到期望的结果（见图 `8.9`）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260105134356849.png){ loading=lazy }
  <figcaption>图 8.9：启用 `CORS` 的受保护资源</figcaption>
</figure>

为了弄清楚为什么这次一切都能顺利通过，我们来看看客户端对受保护资源发起的那次 `HTTP` 调用。再次使用 `curl`，我们就能把所有请求头都看得一清二楚。

``` shell
> curl -v -H "Authorization: Bearer TOKEN"
http://localhost:9002/helloWorld?language=en
```

现在给出

``` shell
HTTP/1.1 200 OK
X-Powered-By: Express

Access-Control-Allow-Origin: *
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
Content-Type: application/json; charset=utf-8
Content-Length: 33
Date: Fri, 29 Jan 2016 17:42:01 GMT
Connection: keep-alive

{
    "greeting": "Hello World"
}
```

这个新的请求头会告诉承载 JavaScript 应用的浏览器：允许任何来源（origin）调用这个端点是可以的。它相当于对`同源策略`提供了一个受控的例外。把它用在`API`（例如受保护资源）上是合理的；但对于需要用户交互的页面和表单，则应保持关闭（在大多数系统里这也是默认设置）。

`CORS`是一个相对较新的解决方案，过去并不是所有浏览器都支持。以前更常用的方案是`JSON with Padding`[^11]（也就是`JSONP`）。`JSONP`是 Web 开发者用来绕过浏览器跨域限制的一种手段，使页面能够从与其来源不同的系统获取数据，但它本质上只是个“技巧”。具体来说，`JSON`数据会被包装成一段`JavaScript`脚本，在目标环境中加载并执行，通常通过指定的回调函数来处理结果。由于这次数据请求以脚本加载的形式呈现，而不是`AJAX`调用，浏览器就会绕过`同源策略`的校验。多年来，出于安全考虑，`JSONP`逐渐被`CORS`取代，因为有一些漏洞曾把`JSONP`当作攻击向量（首先是`Rosetta Flash`[^12]）。因此，我们不会提供受保护资源端点支持`JSONP`的示例。


[^11]: [https://en.wikipedia.org/wiki/JSONP](https://en.wikipedia.org/wiki/JSONP)

[^12]: [https://miki.it/blog/2014/7/8/abusing-jsonp-with-rosetta-flash/](https://blog.miki.it/posts/abusing-jsonp-with-rosetta-flash/)

!!! note "`Rosetta Flash` 漏洞利用"

	`Rosetta Flash` 是一种利用技术，由 `Google` 安全工程师 `Michele Spagnuolo` 于 `2014` 年发现并公开。它允许攻击者通过诱使 `Adobe Flash Player` 误以为攻击者指定的 `Flash` 小程序源自存在漏洞的服务器，从而利用带有脆弱 `JSONP` 端点的服务器。为在大多数现代浏览器中抑制这一攻击向量，可以返回 `HTTP` 头 `X-Content-Type-Options: nosniff`，并且/或者在被反射的回调前追加 `/**/`。

## Token重放

在上一章中，我们看到访问令牌是如何被窃取的。即使受保护资源运行在 `HTTPS` 之上，一旦攻击者拿到访问令牌，就仍然能够访问受保护资源。因此，访问令牌的有效期应尽量设得短一些，以降低 `Token` 重放的风险。确实如此：即便攻击者设法拿到了受害者的访问令牌，如果它已经过期（或即将过期），攻击的危害也会大幅降低。我们会在第 10 章深入讲解如何保护令牌。

`OAuth 2.0` 与其前身的一个主要区别在于：其核心框架不依赖密码学机制。相反，它完全依赖在各个连接上使用 `Transport Layer Security (TLS)`。因此，在整个 `OAuth` 生态中，尽可能强制使用 `TLS` 被视为最佳实践。同样地，又有一个标准可以派上用场：`HTTP Strict Transport Security (HSTS)`[^13]，定义于 `RFC6797`[^14]。`HSTS` 允许 Web 服务器声明：浏览器（或其他符合规范的用户代理）只能通过安全的 `HTTPS` 连接与其交互，绝不能使用不安全的 `HTTP` 协议。将 `HSTS` 集成到我们的端点非常简单，并且和 `CORS` 一样，只需要额外添加几个 `Header`。打开并编辑 `ch-8-ex-3` 文件夹中的 `protectedResource.js` 文件，添加相应的 `Header`。

[^13]: [https://en.wikipedia.org/wiki/HTTP_Strict_Transport_Security](https://zh.wikipedia.org/wiki/HTTP%E4%B8%A5%E6%A0%BC%E4%BC%A0%E8%BE%93%E5%AE%89%E5%85%A8)

[^14]: RFC 6797 [https://tools.ietf.org/html/rfc6797](https://datatracker.ietf.org/doc/html/rfc6797)

``` javascript
app.get("/helloWorld", cors(), getAccessToken, function(req, res){
  if (req.access_token) {

      res.setHeader('X-Content-Type-Options','nosniff');
      res.setHeader('X-XSS-Protection', '1; mode=block');
      res.setHeader('Strict-Transport-Security', 'max-age=31536000');
      var resource = {
            "greeting" : ""
      };

      if (req.query.language == "en") {
            resource.greeting = 'Hello World';
      } else if (req.query.language == "de") {
            resource.greeting ='Hallo Welt';
      } else if (req.query.language == "it") {
            resource.greeting = 'Ciao Mondo';
      } else if (req.query.language == "fr") {
            resource.greeting = 'Bonjour monde';
      } else if (req.query.language == "es") {
            resource.greeting ='Hola mundo';
      } else {
             resource.greeting = "Error, invalid language: "+ querystring.escape(req.query.language);
      }
      res.json(resource);
  }
});
```

现在，当你尝试通过 `HTTP` 客户端访问 ` /helloWorld` 端点时：

``` shell
> curl -v -H "Authorization: Bearer TOKEN"
http://localhost:9002/helloWorld?language=en
```

你可以注意到 `HSTS` 响应头

``` shell
HTTP/1.1 200 OK
X-Powered-By: Express
Access-Control-Allow-Origin: *
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
Strict-Transport-Security: max-age=31536000
Content-Type: application/json; charset=utf-8
Content-Length: 33
Date: Fri, 29 Jan 2016 20:13:06 GMT
Connection: keep-alive

{
    "greeting": "Hello World"
}
```

此时，每当你尝试用浏览器通过 `HTTP`（而不是通过 `TLS`）访问该端点时，都会注意到浏览器会触发一次内部的 `307` 重定向。这样可以避免任何意外的未加密通信（例如协议降级攻击）。我们的测试环境完全不使用 `TLS`，因此这个头部实际上会让我们的资源彻底无法访问。尽管这当然非常安全，但作为一个资源来说并不怎么实用。带有真实 `API` 的生产系统需要在安全性与可访问性之间取得平衡。

## 总结

最后给出一些要点，帮助你确保受保护资源的安全性。

- 对受保护资源响应中的所有不可信数据进行清理（`Sanitize`）。
- 为特定端点选择合适的 `Content-Type`。
- 尽可能利用浏览器防护机制和各类安全头（`security headers`）。
- 如果你的受保护资源端点需要支持 `implicit grant flow`，请使用 `CORS`。
- 尽量避免让受保护资源支持 `JSONP`（如果可以的话）。
- 始终将 `TLS` 与 `HSTS` 结合使用。

现在我们已经加固了客户端和受保护资源，接下来看看要如何加固 `OAuth` 生态中最复杂的组件：`authorization server`。

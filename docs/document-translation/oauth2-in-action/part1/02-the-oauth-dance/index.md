# 2.OAuth 之舞

本章将涵盖

- OAuth 2.0 协议概览
- OAuth 2.0 系统中的不同组件
- 各组件之间如何相互通信
- 各组件之间具体传递哪些信息

到目前为止，你已经对 OAuth 2.0 协议是什么以及它为何重要有了相当不错的认识。你大概也已经知道自己可能会在什么场景、以什么方式使用它。但要真正完成一次
OAuth 交互，你需要哪些步骤？一次 OAuth 交互结束后，你最终会得到什么？这种设计又是如何让 OAuth 保持安全的？

## OAuth 2.0 协议概览：获取并使用令牌

OAuth 是一种复杂的安全协议，不同组件以一种精准的平衡相互传递信息，颇有一种“技术舞步”的感觉。但从本质上看，一次 OAuth
交互只有两个核心步骤：签发令牌与使用令牌。令牌代表客户端被委托获得的访问权限，并且在 OAuth 2.0
的每个环节都处于核心位置。尽管每一步的细节会因多种因素而有所不同，但最典型的 OAuth 交互通常包含如下事件序列：

1. 资源所有者向客户端表明希望客户端代表自己执行操作（例如：“去那个服务把我的照片加载出来，我好把它们打印出来。”）。
2. 客户端在授权服务器处向资源所有者请求授权。
3. 资源所有者向客户端授予授权。
4. 客户端从授权服务器获得令牌。
5. 客户端向受保护资源出示令牌。

在不同的 OAuth 部署中，这些步骤的处理方式可能略有差异，通常会通过把多个步骤合并为一次动作来优化流程，但核心过程基本一致。接下来，我们将来看一个最典型的
OAuth 2.0 示例。

## 详细跟踪一次 OAuth 2.0 授权许可流程

下面我们将详细分析一次 OAuth 授权许可（authorization grant）流程。我们会观察不同参与方之间的每一个步骤，并追踪每一步所对应的
HTTP 请求与响应。特别地，我们将跟随一个用于 Web 客户端应用的授权码许可（authorization code grant）流程。该客户端会由资源所有者以交互方式直接完成授权。

!!! note

	本章中的示例取自本书后续将使用的练习代码。虽然你不需要理解这些练习也能看懂这里的内容，但建议你查看附录 A，并运行一些已完成的示例来亲自体验。另外请注意，这些示例中反复出现的 localhost 仅是巧合；OAuth 完全可以、也确实经常运行在多台彼此独立的机器之间。

授权码许可使用一种临时凭据——授权码（authorization code）——来表示资源所有者对客户端的授权委托，其形式如图 2.1 所示。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251224164611136.png){ loading=lazy }
  <figcaption>图 2.1：授权码授予流程详解</figcaption>
</figure>

让我们把这个流程拆成几个步骤来讲。首先，资源所有者进入客户端应用，并告知客户端：希望它代表自己去访问某个特定的受保护资源。比如，用户会告诉打印服务去使用某个指定的照片存储服务。该服务是一个
API，客户端知道如何对接处理，并且也清楚需要通过 OAuth 来完成授权访问。

!!! note "我该如何找到服务器？"

	为了保持最大的灵活性，OAuth 将真实 API 系统中的许多细节排除在规范范围之外。尤其是：客户端如何得知与某个受保护资源通信的方式，或客户端如何找到与该受保护资源绑定的授权服务器，这些都不是 OAuth 规定的内容。一些构建在 OAuth 之上的协议（例如 OpenID Connect 和 User Managed Access，UMA）确实以标准化的方式解决了这些问题，我们将在第 13 章和第 14 章中介绍。为了演示 OAuth 本身，我们假设客户端已通过静态配置，预先知道如何与受保护资源和授权服务器进行通信。

当客户端意识到需要获取一个新的 OAuth 访问令牌时，它会把资源所有者引导到授权服务器，并发起一个请求，表明客户端希望由该资源所有者委托授予一部分权限（见图
2.2）。例如，我们的照片打印应用可以向照片存储服务请求读取其中已存照片的权限。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251224165815678.png){ loading=lazy }
  <figcaption>图 2.2 将资源所有者引导至授权服务器，以启动流程</figcaption>
</figure>

由于我们使用的是 Web 客户端，因此这里会通过一次 HTTP 重定向跳转到授权服务器的授权端点。客户端应用返回的响应如下所示：

``` shell
HTTP/1.1 302 Moved Temporarily
x-powered-by: Express
Location: http://localhost:9001/authorize?response_type=code&scope=foo&client
_id=oauth-client-1&redirect_uri=http%3A%2F%2Flocalhost%3A9000%2Fcallback&
state=Lwt50DDQKUB8U7jtfLQCVGDL9cnmwHH1
Vary: Accept
Content-Type: text/html; charset=utf-8
Content-Length: 444
Date: Fri, 31 Jul 2015 20:50:19 GMT
Connection: keep-alive
```

这种重定向到浏览器的操作会触发浏览器向授权服务器发起一次 HTTP GET 请求。

``` shell
GET /authorize?response_type=code&scope=foo&client_id=oauth-client
-1&redirect_uri=http%3A%2F%2Flocalhost%3A9000%
2Fcallback&state=Lwt50DDQKUB8U7jtfLQCVGDL9cnmwHH1 HTTP/1.1
Host: localhost:9001
User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:39.0) Gecko/20100101 Firefox/39.0
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
Referer: http://localhost:9000/
Connection: keep-alive
```

客户端通过在发送用户跳转的 URL 中携带查询参数来表明自身身份，并请求特定内容（例如
scopes）。授权服务器可以解析这些参数并据此处理，即便该请求并非由客户端直接发起。

!!! note "查看 HTTP 事务"

	所有 HTTP 交互记录都是用现成工具抓取的，市面上这类工具多得很。浏览器端的检查工具——比如 Firefox 的 Firebug 插件——可以对前端通道的通信进行全面的监控和修改。后端通道则可以通过代理系统来观察，或者使用 Wireshark、Fiddler 之类的网络抓包工具来捕获分析。

接下来，授权服务器通常会要求用户进行身份验证。这一步至关重要，用于确定资源所有者是谁，以及他们可以向客户端委托哪些权限（见图
2.3）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251224170021347.png){ loading=lazy }
  <figcaption>图 2.3 资源所有者登录</figcaption>
</figure>

用户的身份验证过程是直接在用户（及其浏览器）与授权服务器之间完成的；客户端应用根本不会接触到这些信息。这个关键点确保用户无需把自己的凭证交给客户端应用——而这正是
OAuth 诞生要解决的反模式（上一章已讨论）。

此外，由于资源所有者是通过浏览器与授权端点交互的，他们的身份认证同样是在浏览器中完成的。因此，用户认证流程可以采用非常丰富的认证手段。OAuth
并不规定必须使用哪种认证技术，授权服务器可以自由选择，例如用户名/密码、加密证书、安全令牌、联合单点登录（SSO）等各种方式。这里我们确实需要在一定程度上信任浏览器，尤其是在资源所有者使用用户名和密码这类较为简单的认证方式时；不过，OAuth
协议在设计上已经考虑并能够抵御多种主要的基于浏览器的攻击，我们将在第 7、8、9 章进行讲解。

这种分离式的设计也让客户端免受用户认证方式变化的影响，使得简单的客户端应用也能受益于授权服务器上不断涌现的新技术，比如基于风险的启发式认证。不过，这并不会向客户端传递任何关于已认证用户的信息；这一点我们会在第
13 章深入讨论。

接下来，用户对客户端应用进行授权（见图
2.4）。在这一步中，资源所有者选择将自身权限中的一部分委托给客户端应用，而授权服务器也有多种不同的方式来实现这一点。客户端的请求可以包含它希望获得哪类访问权限的说明（称为
OAuth scope，见 2.4 节）。授权服务器可以允许用户拒绝其中部分或全部 scope，也可以让用户对整个请求进行整体批准或拒绝。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251230143911087.png){ loading=lazy }
  <figcaption>图 2.4 资源所有者批准客户端的访问请求</figcaption>
</figure>

此外，许多授权服务器允许将这次授权决定保存下来，以便日后复用。启用该机制后，同一客户端对同一访问权限的后续请求将不会再以交互方式提示用户确认。用户仍会被重定向到授权端点，也仍需要登录，但将权限委托给该客户端的决定已在此前某次请求中做出。授权服务器甚至可以依据内部策略（例如客户端白名单或黑名单）覆盖终端用户的决定。

接下来，授权服务器会将用户重定向回客户端应用（见图 2.5）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251230144230826.png){ loading=lazy }
  <figcaption>图 2.5：授权码被发送回客户端</figcaption>
</figure>

这会以 HTTP 重定向的形式，将客户端跳转到其 `redirect_uri`。

```shell
HTTP 302 Found
Location: http://localhost:9000/oauth_callback?code=8V1pr0rJ&state=Lwt50DDQKUB8U7jtfLQCVGDL9cnmwHH1
```

这又会导致浏览器向客户端发起如下请求：

```shell
GET /callback?code=8V1pr0rJ&state=Lwt50DDQKUB8U7jtfLQCVGDL9cnmwHH1 HTTP/1.1
Host: localhost:9000
```

请注意，这个 HTTP 请求是发送给客户端的，而不是发送到授权服务器。

```shell
User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:39.0) Gecko/20100101 Firefox/39.0
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
Referer: http://localhost:9001/authorize?response_type=code&scope=foo&client_id=oauth-client-1&redirect_uri=http%3A%2F%2Flocalhost%3A9000%2Fcallback&
state=Lwt50DDQKUB8U7jtfLQCVGDL9cnmwHH1
Connection: keep-alive
```

由于我们使用的是授权码（Authorization Code）授权类型，这次重定向会携带一个特殊的 `code`
查询参数。该参数的值是一种一次性凭证，称为授权码（authorization
code），用于表示用户授权决策的结果。客户端在收到请求后，可以解析该参数获取授权码的值，并在下一步中使用它。客户端还会校验
`state` 参数的值是否与前一步发送的值一致。

当客户端拿到授权码后，就可以将其发送回授权服务器的令牌端点（token endpoint）（见图 2.6）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251230145532215.png){ loading=lazy }
  <figcaption>图 2.6 客户端将授权码及自身凭据回传至授权服务器</figcaption>
</figure>

客户端发起一次 HTTP POST 请求，将参数以表单编码（form-encoded）的方式放在 HTTP 实体请求体中，并通过 HTTP Basic 认证头传递其
client_id 和 client_secret。该 HTTP 请求完全在客户端与授权服务器之间直接进行，全程不经过浏览器，也不涉及资源所有者。

```shell
POST /token
Host: localhost:9001
Accept: application/json
Content-type: application/x-www-form-encoded
Authorization: Basic b2F1dGgtY2xpZW50LTE6b2F1dGgtY2xpZW50LXNlY3JldC0x

grant_type=authorization_code&
redirect_uri=http%3A%2F%2Flocalhost%3A9000%2Fcallback&code=8V1pr0rJ
```

这种对不同 HTTP 连接的隔离，确保客户端能够直接完成身份认证，而不会被其他组件窥探或篡改令牌请求。

授权服务器接收该请求后，如果请求有效，就会签发一个令牌（见图 2.7）。为确保请求合法，授权服务器会执行一系列步骤：首先，校验客户端的凭据（此处通过
Authorization 头传递），以确定是哪一个客户端在请求访问；然后，从请求体中读取 code
参数的值，并检索与该授权码相关的所有信息，包括最初发起授权请求的是哪个客户端、由哪个用户完成授权、以及授权的具体范围/用途。如果该授权码有效、此前未被使用过，并且发起此次请求的客户端与最初发起请求的客户端一致，授权服务器就会为该客户端生成并返回一个新的访问令牌。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251230151006990.png){ loading=lazy }
  <figcaption>图 2.7 客户端接收访问令牌</figcaption>
</figure>

该令牌会在 HTTP 响应中以 JSON 对象的形式返回。

```shell
HTTP 200 OK
Date: Fri, 31 Jul 2015 21:19:03 GMT
Content-type: application/json

{
  "access_token": "987tghjkiu6trfghjuytrghj",
  "token_type": "Bearer"
}
```

客户端现在可以解析令牌响应，从中获取访问令牌，并在访问受保护资源时使用。在这个例子中，响应里的 `token_type` 字段表明我们拿到的是一个
OAuth Bearer
令牌。响应还可能包含刷新令牌（用于在无需再次请求授权的情况下获取新的访问令牌），以及有关访问令牌的更多信息，例如令牌作用域的提示和过期时间。只要客户端还打算使用该令牌，就可以将访问令牌安全地存放起来，即使用户已经离开也不受影响。

!!! note "携带令牌的权利"

	OAuth 核心规范讨论的是持有者令牌（bearer token）。这意味着，任何持有该令牌的人都有权使用它。本书中的所有示例都会使用持有者令牌，除非特别说明。持有者令牌具有一些特定的安全属性，我们会在第 10 章逐一说明；同时也会在第 15 章提前介绍非持有者令牌。

拿到令牌后，客户端就可以将令牌提交给受保护资源（见图 2.8）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251230151755767.png){ loading=lazy }
  <figcaption>图 2.8：客户端使用访问令牌来执行操作</figcaption>
</figure>

客户端有多种方式可以携带访问令牌。本例将采用推荐做法：通过 `Authorization` 请求头传递。

```shell
GET /resource HTTP/1.1
Host: localhost:9002
Accept: application/json
Connection: keep-alive
Authorization: Bearer 987tghjkiu6trfghjuytrghj
```

受保护资源随后可以从请求头中解析出令牌，判断其是否仍然有效，查询是谁授权的以及授权的范围，并据此返回相应的响应。受保护资源在进行令牌查询时有多种实现方式，我们将在第 11 章更深入地讨论。最简单的一种方式是资源服务器与授权服务器共享一个包含令牌信息的数据库：授权服务器在生成新令牌时将其写入存储，资源服务器在收到令牌时从存储中读取并校验。

## OAuth 的参与方：客户端、授权服务器、资源所有者，以及受保护资源

正如我们在上一节提到的，OAuth 系统中主要有四类参与方：客户端、资源所有者、授权服务器以及受保护资源（见图 2.9）。这些组件分别负责 OAuth 协议的不同环节，并协同配合，确保整个 OAuth 协议顺畅运行。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251230152028454.png){ loading=lazy }
  <figcaption>图 2.9：OAuth 2.0 协议的主要组成部分</figcaption>
</figure>

`OAuth 客户端`是一类软件，用来代表资源所有者去访问受保护资源，并通过 OAuth 获取访问权限。得益于 OAuth 协议的设计，客户端通常是 OAuth 系统中最简单的组件，它的职责主要集中在从授权服务器获取令牌，并在访问受保护资源时使用令牌。客户端不需要理解令牌，也不应该去查看令牌的内容；相反，客户端只把令牌当作一个不透明的字符串来使用。OAuth 客户端可以是 Web 应用、原生应用，甚至是浏览器中的 JavaScript 应用；我们会在第 6 章介绍这些客户端类型之间的差异。在我们的云打印示例中，打印服务就是 OAuth 客户端。

`OAuth 受保护资源`通过 HTTP 服务器对外提供访问，并且必须携带 OAuth 令牌才能访问。受保护资源需要校验客户端提交的令牌，并据此决定是否以及如何响应请求。在 OAuth 架构中，是否接受某个令牌，最终由受保护资源说了算。在我们的云打印示例中，照片存储网站就是受保护资源。

`资源所有者`是有权将访问权限委托给客户端的实体。与 OAuth 系统的其他部分不同，资源所有者不是软件。在大多数情况下，资源所有者就是使用客户端软件去访问自己所拥有或控制资源的那个人。在流程的至少一部分阶段，资源所有者会使用 Web 浏览器（更通用的称呼是用户代理）与授权服务器交互。资源所有者也可能通过 Web 浏览器与客户端交互，就像我们在这里演示的那样，但这完全取决于客户端的形态。在我们的云打印示例中，资源所有者是想要打印照片的最终用户。

`OAuth 授权服务器`是一台 HTTP 服务器，是 OAuth 系统的核心组件。授权服务器负责对资源所有者和客户端进行身份认证，提供让资源所有者向客户端授权的机制，并向客户端签发令牌。有些授权服务器还提供额外能力，例如令牌内省（token introspection）以及记住授权决策等。在我们的云打印示例中，照片存储网站为其受保护资源运行了一套自建的内部授权服务器。

## OAuth 的核心组成部分：令牌、作用域与授权许可

除了这些参与方之外，OAuth 生态还依赖若干其他机制，既有概念层面的，也有实体层面的。它们共同把上一节的各个参与方串联起来，组成更完整的协议。

### 访问令牌  

OAuth 访问令牌（access token），在非正式语境下也常被简称为 token，是授权服务器颁发给客户端的一种凭证，用来表明客户端被委托的权限范围。OAuth 并不规定令牌本身的格式或内容，但它始终代表以下要素的组合：客户端所请求的访问权限、为客户端授权的资源所有者，以及在授权过程中授予的权利（通常还会包含对受保护资源的某种指示）。

对客户端而言，OAuth 令牌是不可透明的（opaque）：客户端既不需要（也往往无法）查看令牌本身。客户端的职责只是携带令牌——向授权服务器申请令牌，并把它提交给受保护资源。令牌并非对系统中的所有组件都不可透明：授权服务器负责签发令牌，受保护资源负责校验令牌，因此它们都需要能够理解令牌本身及其所代表的含义。然而，客户端完全不必关心这些细节。这种设计让客户端可以比原本更简单，同时也为授权服务器与受保护资源在令牌的具体落地方式上提供了极大的灵活性。

### 作用域  

OAuth 作用域（scope）用于表示在某个受保护资源上的一组权限。在 OAuth 协议中，作用域以字符串形式表示，并且可以用空格分隔的列表组合成一个集合。因此，scope 值本身不能包含空格字符。除此之外，OAuth 并不定义 scope 值的格式与结构。

作用域是限制授予客户端访问权限的重要机制。作用域由受保护资源根据其提供的 API 来定义。客户端可以请求特定作用域，授权服务器则可以在处理请求时让资源所有者针对某个客户端对特定作用域进行授予或拒绝。作用域通常具有可叠加的特性。

回到我们的云打印示例。照片存储服务的 API 定义了多个不同的作用域用于访问照片：read-photo、read-metadata、update-photo、update-metadata、create 和 delete。照片打印服务完成工作只需要读取照片，因此它请求 read-photo 作用域。一旦拿到包含该作用域的访问令牌，打印机就可以按需读取照片并输出图像。如果用户决定使用高级功能——根据日期把一系列照片打印成相册——打印服务还需要额外的 read-metadata 作用域。由于这属于新增访问权限，打印服务需要通过常规的 OAuth 流程请求用户就这一额外作用域进行授权。当打印服务拿到同时包含两个作用域的访问令牌后，它就可以使用同一个访问令牌执行需要其中任一作用域或两者同时满足的操作。

### 刷新令牌

OAuth 刷新令牌在概念上与访问令牌类似：它由授权服务器颁发给客户端，客户端无需知道（也不关心）令牌内部包含什么。不过，不同之处在于，刷新令牌永远不会发送给受保护资源。相反，客户端使用刷新令牌来申请新的访问令牌，而无需让资源所有者参与其中（见图 2.10）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251230152517929.png){ loading=lazy }
  <figcaption>图 2.10 使用刷新令牌</figcaption>
</figure>

为什么客户端还要费心用刷新令牌（refresh token）？在 OAuth 中，访问令牌（access token）可能随时对客户端失效：用户可能撤销了令牌，令牌可能过期，或者被系统中的某个触发机制判定为无效。客户端通常是在实际使用令牌时收到错误响应，才发现它已经不可用了。当然，客户端也可以让资源所有者重新授权，但如果资源所有者此时不在怎么办？

在 OAuth 1.0 里，客户端别无选择，只能等资源所有者回来。为了避免这种情况，OAuth 1.0 的令牌往往会一直有效，除非被明确撤销。但这会带来问题：一旦令牌被盗，攻击面就会扩大——攻击者可以永远使用这枚被盗令牌。到了 OAuth 2.0，访问令牌可以选择自动过期，但我们仍然需要一种机制，在用户不在场时也能继续访问资源。于是，刷新令牌取代了过去那种长期有效的令牌：它不直接用于获取资源，而只用于换取新的访问令牌；而新的访问令牌才用于获取资源。这样一来，刷新令牌和访问令牌在暴露风险上各自受限，并且相互补充。

刷新令牌还让客户端具备“缩小权限范围”（down-scope）的能力。比如客户端被授予了 A、B、C 三个 scope，但它知道某次调用只需要 A，那么就可以用刷新令牌去申请一个仅包含 scope A 的访问令牌。这样，聪明的客户端就能遵循“最小权限原则”，而不必让不那么聪明的客户端去费劲推断某个 API 到底需要哪些权限。多年的落地经验表明，OAuth 客户端往往谈不上聪明，但为那些愿意这么做的客户端保留这种高级能力仍然很有价值。

那如果刷新令牌本身也失效了怎么办？客户端总可以在资源所有者有空的时候再去打扰他，让他重新授权。换句话说，OAuth 客户端的兜底方案就是：再走一遍 OAuth 流程。

### 授权许可

授权许可（authorization grant）指的是：OAuth 客户端通过 OAuth 协议获取对受保护资源访问权限的一种方式；如果流程成功，最终会让客户端拿到一个令牌（token）。这可能是 OAuth 2.0 里最容易让人困惑的术语之一，因为它既用来描述用户委托授权的具体机制，也用来指代“委托授权”这一行为本身。更容易混淆的是“授权码许可类型”（authorization code grant type）：我们前面已经详细介绍过它，但开发者有时会看到回传给客户端的授权码（authorization code），就误以为这个产物——而且只有这个产物——就是授权许可。尽管授权码确实代表了用户的授权决定，但它本身并不是授权许可。真正的授权许可是整个 OAuth 过程：客户端把用户引导到授权端点（authorization endpoint），随后收到授权码，最后再用授权码去兑换令牌。

换句话说，授权许可就是获取令牌的方法。本书以及整个 OAuth 社区也会不时把它称为 OAuth 协议的一个“流程”（flow）。OAuth 中存在多种不同的授权许可类型，各自都有不同的特性。我们会在第 6 章详细讲解这些内容，不过我们的大多数示例和练习（比如上一节的那些）使用的都是“授权码授权许可类型”。

## OAuth 各参与方与组件之间的交互：后端通道、前端通道与各类端点

既然我们已经了解了 OAuth 系统的各个组成部分，接下来就来看看它们之间究竟是如何进行通信的。OAuth 是一种基于 HTTP 的协议，但与大多数基于 HTTP 的协议不同，OAuth 的通信并不总是通过一次简单的 HTTP 请求-响应来完成。

!!! note "通过非 HTTP 通道使用 OAuth"  

	尽管 OAuth 仅以 HTTP 为基础进行定义，但已有多项规范说明如何将 OAuth 流程的不同环节迁移到非 HTTP 协议中。例如，一些标准草案定义了如何在通用安全服务应用程序接口（GSS-API）[^1] 和受限应用协议（CoAP）[^2] 上使用 OAuth 令牌。这些方案仍可用 HTTP 来引导整个流程，并且通常会尽可能直接地把基于 HTTP 的 OAuth 组件映射到这些其他协议上。

[^1]: [RFC 7628](https://datatracker.ietf.org/doc/html/rfc7628)

[^2]: [https://tools.ietf.org/html/draft-ietf-ace-oauth-authz](https://datatracker.ietf.org/doc/html/draft-ietf-ace-oauth-authz)

### 后端通道通信

OAuth 流程中的很多环节都会通过标准的 HTTP 请求/响应来相互通信。由于这些请求通常发生在资源所有者和用户代理的视野之外，因此统称为后端通道通信（见图 2.11）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251230153426084.png){ loading=lazy }
  <figcaption>图 2.11 反向通道通信</figcaption>
</figure>

这些请求与响应会利用所有常规的 HTTP 机制进行通信：请求头、查询参数、方法以及实体主体中都可能包含对本次交互至关重要的信息。需要注意的是，这里涉及的 HTTP 协议栈内容可能比你平时接触的更“全”，因为很多简单的 Web API 往往只要求客户端开发者关注响应体。

授权服务器提供一个令牌端点，客户端通过该端点申请访问令牌和刷新令牌。客户端会直接调用这个端点，并提交一组以表单编码（form-encoded）方式传递的参数，由授权服务器解析并处理。随后，授权服务器返回一个用于表示令牌的 JSON 对象作为响应。

此外，当客户端连接受保护资源时，它同样会在后端通道（back channel）发起一次直接的 HTTP 调用。该调用的细节完全取决于受保护资源本身，因为 OAuth 可以用来保护种类极其丰富的 API 和架构风格。在所有这些场景中，客户端都会携带 OAuth 令牌，而受保护资源必须能够理解该令牌及其所代表的权限范围。

### 前端通道通信

在常规的 HTTP 通信中，正如我们在上一节看到的那样，HTTP 客户端会将包含请求头、查询参数、实体正文以及其他信息的请求直接发送到服务器。服务器随后会根据这些信息判断如何响应，并返回一个包含响应头、实体正文以及其他信息的 HTTP 响应。

但在 OAuth 中，有多种场景下两个组件无法彼此直接发起请求并返回响应，例如客户端与授权服务器的授权端点交互时。前端通道通信（front-channel communication）是一种通过中间的 Web 浏览器，利用 HTTP 请求在两个系统之间进行间接通信的方法（见图 2.12）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251230153859979.png){ loading=lazy }
  <figcaption>图 2.12  前端通道通信</figcaption>
</figure>

这种技术会将浏览器两端的会话隔离开来，因此能够跨不同的安全域工作。比如，用户如果需要在某个组件上进行身份验证，可以在不把凭据暴露给另一个系统的情况下完成登录。我们既能保持信息隔离，又能在用户在场的情况下实现通信。

两段软件在彼此从不直接“对话”的前提下，如何完成通信？前端通道（front-channel）通信的做法是：把参数附加到某个 URL 上，并指示浏览器去访问该 URL。接收方随后解析由浏览器请求过来的 URL，并消费其中携带的信息。接收方还可以用同样的方式（继续通过 URL 携带参数）把浏览器重定向回发起方托管的某个 URL，从而作出响应。于是，双方通过 Web 浏览器作为中介，以间接方式相互通信。这也意味着，每一次前端通道的请求与响应，实际上都对应两组 HTTP 请求/响应事务（见图 2.13）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251230154452223.png){ loading=lazy }
  <figcaption>图 2.13 前端通道请求与响应的组成部分</figcaption>
</figure>

例如，在我们之前看到的授权码授权流程中，客户端需要将用户引导到授权端点，同时还必须把请求中的一些关键信息传递给授权服务器。为此，客户端会向浏览器发起一次 HTTP 重定向。该重定向的目标地址是授权服务器的 URL，并在其后以查询参数的形式附带若干字段：

```shell
HTTP 302 Found
Location: http://localhost:9001/authorize?client_id=oauth-client-1&response_type=code&state=843hi43824h42tj
```

授权服务器可以像处理任何其他 HTTP 请求一样解析传入的 URL，并从这些参数中找到客户端发送的信息。此时，授权服务器可以与资源所有者进行交互，通过浏览器发起的一系列 HTTP 交互对其进行身份验证，并征求其授权。到了需要将授权码返回给客户端时，授权服务器同样会向浏览器发送一个 HTTP 重定向，不过这次以客户端的 redirect_uri 作为基址。授权服务器还会在该重定向中附带自己的查询参数：

```shell
HTTP 302 Found
Location: http://localhost:9000/oauth_callback?code=23ASKBWe4&state=843hi43824h42tj
```

当浏览器跟随该重定向时，请求会被客户端应用接收并处理——在这里是通过一次 HTTP 请求完成的。客户端可以从传入请求中解析 URL 参数。这样一来，客户端与授权服务器无需直接通信，也能借助一个中间媒介来回传递消息。

!!! note "如果我的客户端不是 Web 应用怎么办？"

	OAuth 既适用于 Web 应用，也适用于原生应用，但两者都需要使用同一种前端通道机制，从授权端点接收返回信息。前端通道始终通过 Web 浏览器和 HTTP 重定向来完成，不过最终并不一定要由常规的 Web 服务器来承载。好在有一些很实用的技巧可用，比如内置 Web 服务器、应用专用的 URI Scheme，以及由后端服务发起的推送通知等。只要浏览器能够对该 URI 发起调用，就能正常工作。我们会在第 6 章详细探讨这些方案。

所有通过前端通道传递的信息对浏览器都是可见的，既可能被读取，也可能在最终请求发出前被篡改。OAuth 协议对此已有考虑：它会限制经由前端通道传递的信息类型，并确保前端通道中使用的任何信息片段都无法单独完成授权委托这一任务。在本章展示的典型场景中，授权码不能被浏览器直接使用，而必须连同客户端凭据一起通过后端通道提交。还有一些协议（如 OpenID Connect）提供了更高等级的安全性：允许客户端或授权服务器对这些前端通道消息进行签名，以增加一层额外防护；我们会在第 13 章简要介绍。

## 总结

OAuth 是一个由许多环节组成的协议，但它建立在一系列简单的操作之上，这些操作组合起来，形成了一种安全的授权委托方式。

- OAuth 的核心就是获取令牌并使用令牌。  
- OAuth 系统中的不同组件各自关注流程的不同部分。  
- 各组件之间通过直接（后端通道）和间接（前端通道）的 HTTP 进行通信。

现在你已经了解了 OAuth 是什么以及它如何工作，让我们开始动手构建吧！在下一章中，我们将从零开始构建一个 OAuth 客户端。

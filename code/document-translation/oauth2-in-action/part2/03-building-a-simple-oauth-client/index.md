# 3.构建一个简单的 OAuth 客户端

本章内容包括

- 在授权服务器上注册 OAuth 客户端，并配置客户端与授权服务器通信
- 使用授权码（authorization code）授权模式向资源所有者发起授权请求
- 使用授权码换取 Token
- 将访问令牌作为 Bearer Token 访问受保护资源
- 刷新访问令牌

正如我们在上一章看到的，OAuth 协议的核心就是把令牌发放给客户端，并允许客户端代表资源所有者使用访问令牌去访问受保护资源。本章我们将构建一个简单的
OAuth 客户端，使用授权码授权模式从授权服务器获取一个 Bearer 访问令牌，并使用该令牌访问受保护资源。

!!! note

	本书中的所有练习与示例都基于 Node.js 和 JavaScript 构建。每个练习由多个组件组成，这些组件被设计为在同一台系统上运行，并通过 localhost 的不同端口进行访问。关于该框架及其结构的更多信息，请参见附录 A。

## 在授权服务器上注册一个 OAuth 客户端

首先，OAuth 客户端和授权服务器在开始通信之前，需要先彼此了解一些基本信息。OAuth 协议本身并不关心这个过程具体如何完成，只要以某种方式完成即可。OAuth
客户端通过一个特殊字符串来标识，这个字符串称为客户端标识符（client identifier），在我们的练习以及 OAuth 协议的多个部分中都用
client_id 来表示。对于同一个授权服务器来说，每个客户端的 client identifier
都必须是唯一的，因此几乎总是由授权服务器分配给客户端。这个分配过程可以通过开发者门户、动态客户端注册（第 12
章会讨论），或其他流程来完成。在我们的示例中，我们采用手动配置。

打开 ch-3-ex-1 文件夹，并在其中运行 npm install。这个练习里，我们会编辑 client.js，而 authorizationServer.js 和
protectedResource.js 都保持不变。

!!! note "为什么是 Web 客户端？"

	你可能已经注意到，我们的 OAuth 客户端本身就是一个 Web 应用，运行在由 Node.js 应用托管的 Web 服务器上。客户端同时也是服务器这一点可能会让人困惑，但其实很简单：OAuth Client 始终是那段从授权服务器获取 token，并用这个 token 去访问受保护资源的软件——正如我们在第 2 章所说。

	我们在这里构建基于 Web 的客户端，不仅因为这是 OAuth 最初的使用场景，也是最常见的场景之一。移动端、桌面端以及浏览器内应用同样可以使用 OAuth，但每一种都需要略有不同的考量和处理方式才能正常工作。我们将在第 6 章逐一讲解这些场景，并重点说明它们与这里的 Web 客户端究竟有哪些不同。

我们的授权服务器已经为该客户端分配了 client_id：oauth-client-1（见图 3.1）。接下来，我们需要把这条信息同步到客户端软件中（你可以打开
authorizationServer.js 文件顶部，找到最上方的 client 变量来查看，或者直接访问 http://localhost:9001/）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251230225152532.png){ loading=lazy }
  <figcaption>图 3.1 授权服务器首页，展示客户端与服务器信息</figcaption>
</figure>

我们的客户端把注册信息存放在一个名为 client 的顶层对象变量里，并且把 client_id 存在这个对象中一个（毫不意外就叫）client_id
的字段里。我们只需要编辑这个对象，填入分配到的 client_id 值即可：

```shell
"client_id": "oauth-client-1"
```

在 OAuth 的语境里，我们的客户端还属于所谓的机密客户端（confidential client），这意味着它会保存一个共享密钥，用于在与授权服务器通信时进行自我认证，这个密钥称为
client_secret。client_secret 可以通过多种方式传给授权服务器的 token 端点，但在我们的示例中会使用 HTTP Basic。client_secret
几乎总是由授权服务器分配；在这里，授权服务器为我们的客户端分配的 client_secret 是
oauth-client-secret-1。这当然是个糟糕的密钥：不仅达不到最基本的熵要求，而且我们把它写进书里之后它也就不再“保密”了。不过没关系，它足够支撑我们的示例，我们把它加到客户端的配置对象里：

```shell
"client_secret": "oauth-client-secret-1"
```

不少 OAuth 客户端库也会在这种对象里包含其他配置项，比如 redirect_uri、要请求的一组 scope，以及我们会在后续章节详细介绍的其他内容。与
client_id 和 client_secret 不同，这些参数由客户端软件决定，而不是授权服务器分配的。因此它们已经包含在客户端的配置对象中了。我们的对象应当长这样：

``` javascript
var client = {
"client_id": "oauth-client-1",
"client_secret": "oauth-client-secret-1",
"redirect_uris": ["http://localhost:9000/callback"]
};
```

另一方面，客户端还需要知道它要和哪个服务器通信，以及如何通信。在这个练习中，客户端需要知道授权端点（authorization endpoint）和
token 端点（token endpoint）的位置，除此之外并不需要了解服务器的更多信息。服务器的配置信息存放在一个名为 authServer
的顶层变量中，我们已经把相关配置填好了：

```javascript
var authServer = {
    authorizationEndpoint: 'http://localhost:9001/authorize',
    tokenEndpoint: 'http://localhost:9001/token'
};
```

到这里，客户端已经具备了连接授权服务器所需的全部信息。接下来，让它真正跑起来做点事。

## 使用授权码（authorization code）授权类型获取 token

OAuth 客户端要从授权服务器拿到 token，必须以某种形式从资源所有者（resource
owner）那里获得授权委托。本章我们将使用一种交互式的委托方式，称为授权码（authorization
code）授权类型：客户端会把资源所有者（在我们的场景中就是客户端侧的终端用户）引导到授权服务器的授权端点。随后服务器会通过客户端的
redirect_uri 把授权码返回给客户端。最后，客户端把收到的授权码发送到授权服务器的 token 端点，以换取 OAuth 访问令牌（access
token），并对其进行解析和保存。想查看该授权类型的全部步骤细节（包括每一步使用的 HTTP 报文），请回顾第 2 章。本章我们将重点关注实现。

!!! note "为什么选择授权码（authorization code）授权类型？"

	你可能已经注意到，我们在聚焦一个特定的 OAuth 授权类型：授权码。你也许在本书之外已经用过其他 OAuth 授权类型（例如隐式授权 implicit grant，或客户端凭证授权 client credentials grant），那为什么不从它们开始？正如我们在第 6 章将讨论的那样，授权码授权类型能够彻底分离 OAuth 的各个参与方，因此它是本书将介绍的核心授权类型中最基础、也最复杂的一种。其他 OAuth 授权类型都是对它的优化，用于适配特定用例和运行环境。我们会在第 6 章详细介绍它们，你也将有机会把练习代码改造为支持这些授权类型。

我们将继续使用上一节中你一直在构建的 ch-3-ex-1 练习，并扩展其能力，使之成为一个可用的 OAuth
客户端。客户端已经预置了一个落地页，用于发起授权流程。这个落地页托管在项目根路径下。请记得像附录 A
讨论的那样，同时运行三个组件，并分别在各自的终端窗口中启动。

在整个练习过程中，你可以让授权服务器和受保护资源一直运行，但客户端应用每次修改后都需要重启，才能让改动生效。

### 发送授权请求

客户端应用的首页上有两个按钮：一个会把用户带到 http://localhost:9000/authorize，另一个用于获取受保护资源（见图 3.2）。我们先关注
Get OAuth Token 按钮。这个页面由下面这个（目前为空的）函数提供服务：

```javascript
app.get('/authorize', function (req, res) {

});
```

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251230225701347.png){ loading=lazy }
  <figcaption>图 3.2 客户端在获取令牌前的初始状态</figcaption>
</figure>

要启动授权流程，我们需要将用户重定向到服务器的授权端点，并在该 URL 上带上所有必要的查询参数。我们会使用一个工具函数配合
JavaScript 的 url 库来生成要跳转的 URL；它会帮你处理查询参数的拼接，并自动对参数值进行 URL 编码。我们已经为你提供了这个工具函数，但在任何
OAuth 实现中，你都需要正确构建 URL 并添加查询参数，才能使用前端通道通信（front-channel communication）。

``` javascript
var buildUrl = function(base, options, hash) {
  var newUrl = url.parse(base, true);
  delete newUrl.search;
  if (!newUrl.query) {
      newUrl.query = {};
  }
  __.each(options, function(value, key, list) {
      newUrl.query[key] = value;
  });
  if (hash) {
      newUrl.hash = hash;
  }

  return url.format(newUrl);
};
```

你可以通过传入一个 URL 基址，以及一个包含所有需要追加到该 URL 查询参数中的参数对象，来调用这个工具函数。这里务必使用真正的
URL 库，因为在 OAuth 流程中，我们可能需要向已经带有查询参数的 URL 继续追加参数，或者处理那些格式奇怪但依然合法的 URL。

```javascript
var authorizeUrl = buildUrl(authServer.authorizationEndpoint, {
    response_type: 'code',
    client_id: client.client_id,
    redirect_uri: client.redirect_uris[0]
});
```

现在我们可以向用户的浏览器发送一个 HTTP 重定向，让它跳转到授权端点：

```javascript
res.redirect(authorizeUrl);
```

redirect 函数属于 express.js 框架的一部分，会针对 `http://localhost:9000/authorize` 的请求向浏览器返回一条 HTTP 302
重定向响应。按照我们示例客户端应用的实现，每次调用这个页面都会去申请一个新的 OAuth Token。真正的 OAuth
客户端应用绝不应该使用这种对外可访问的触发机制，而应通过跟踪应用内部状态来判断何时需要新的 Access
Token。对这个简单练习来说，用外部触发也没问题。把所有内容整合起来后，我们最终的函数如附录 B 的清单 1 所示。

现在，当你在客户端主页面点击 Get OAuth Token 按钮时，浏览器应会自动重定向到授权服务器的授权端点，并弹出提示让你为客户端应用进行授权（见图
3.3）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251231093737036.png){ loading=lazy }
  <figcaption>图 3.3 授权服务器的审批页面，用于处理我们客户端的请求</figcaption>
</figure>

在这个练习中，授权服务器在功能上已经完整，不过我们会在第 5 章深入讲解让它真正跑起来还需要哪些东西。点击 Approve
按钮后，服务器会把你重定向回客户端。目前还不会发生什么特别有意思的事情，所以我们在下一节把它改得更有看头。

### 处理授权响应

此时，你已经回到了客户端应用，并访问了 http://localhost:9000/callback 这个 URL，同时还带着几个额外的查询参数。该 URL
由（目前还是空的）函数提供服务：

```javascript
app.get('/callback', function (req, res) {

});
```

在 OAuth 流程的这一环，我们需要检查输入参数，并从授权服务器返回的参数 code 中读取授权码。记住，这个请求是授权服务器重定向过来的，而不是对我们直接发起请求的
HTTP 响应。

```javascript
var code = req.query.code;
```

现在我们需要拿到这个授权码，通过 HTTP POST 直接发送到令牌端点。在请求体中，我们会将该 code 作为表单参数提交。

```javascript
var form_data = qs.stringify({
    grant_type: 'authorization_code',
    code: code,
    redirect_uri: client.redirect_uris[0]
});
```

顺便说一句，为什么我们要在这个调用里带上 redirect_uri？毕竟我们并没有做任何重定向。按照 OAuth 规范，如果在授权请求中指定了重定向
URI，那么在令牌请求中也必须包含同一个 URI。这样做可以防止攻击者利用已被攻破的重定向
URI，借助一个本意良好的客户端，把某个会话里的授权码注入到另一个会话中。我们会在第 9 章看看服务端如何实现这项校验。

我们还需要发送一些 Header，告诉服务器这是一个以 HTTP 表单方式编码的请求，同时使用 HTTP Basic 来认证客户端。HTTP Basic 的
Authorization 头是一个 base64 编码字符串：把用户名和密码用一个冒号（:）拼接起来后再进行编码。OAuth 2.0 规定用 client_id
作为用户名、client_secret 作为密码，但在拼接之前这两者都要先做 URL 编码。[^1] 我们已经提供了一个简单的工具函数来处理 HTTP
Basic 编码的细节。

[^1]: 很多客户端不会对 client_id 和 client_secret 进行 URL 编码，有些服务器端也会省略对其进行解码。由于常见的 client_id 和
client_secret 通常只是由简单的随机 ASCII 字符组成，一般不会出什么问题；但为了完全符合规范并支持扩展字符集，务必按要求正确进行
URL 编码与解码。

``` javascript
var headers = {
    'Content-Type': 'application/x-www-form-urlencoded',
    'Authorization': 'Basic ' + encodeClientCredentials(client.client_id, client.client_secret)
};
```

接下来，我们需要通过向服务器的授权端点发起一个 POST 请求，把它对接起来：

``` javascript
var tokRes = request('POST', authServer.tokenEndpoint,
    {
        body: form_data,
        headers: headers
    }
);
res.render('index', {access_token: body.access_token, scope: scope});
```

如果请求成功，授权服务器会返回一个 JSON 对象，其中包含访问令牌的值以及其他一些信息。返回结果大致如下：

```json
{
  "access_token": "987tghjkiu6trfghjuytrghj",
  "token_type": "Bearer"
}
```

我们的应用需要读取该返回结果，并解析 JSON 对象以获取 access token 的值，因此我们将响应解析到 body 变量中：

```javascript
var body = JSON.parse(tokRes.getBody());
```

我们的客户端现在需要把这个 token 保存下来，方便后续使用：

```javascript
access_token = body.access_token;
```

OAuth 客户端这一部分的最后一个函数如附录 B 的代码清单 2 所示。

当成功获取并保存访问令牌后，我们就可以把用户重定向回一个页面，在浏览器中显示该令牌的值（图 3.4）。在真实的 OAuth
应用中，这样展示访问令牌是个非常糟糕的做法，因为它属于机密信息，客户端理应加以保护。但在我们的演示应用里，这么做有助于直观地看到流程发生了什么，因此我们暂时放任这种糟糕的安全实践，同时也提醒你：在生产环境中一定要更谨慎、更聪明。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251231094232168.png){ loading=lazy }
  <figcaption>图 3.4 获取访问令牌后客户端的主页；每次运行程序时，访问令牌的值都会不同</figcaption>
</figure>

### 使用state参数添加跨站保护

在当前配置下，只要有人访问 http://localhost:9000/callback，客户端就会不加甄别地接收传入的 code 值，并尝试将其 POST
到授权服务器。这意味着攻击者可以利用我们的客户端去授权服务器“钓”取有效的授权码，既浪费客户端和服务器资源，也可能导致客户端获取到一个它从未发起请求的令牌。

我们可以通过一个可选的 OAuth 参数 state 来缓解这个问题：用随机值填充它，并把该值保存到应用中的变量里。在我们丢弃旧的访问令牌之后，会立刻生成这个值：

```javascript
state = randomstring.generate();
```

关键在于把这个值保存到应用中一个在 redirect_uri 回调返回时仍然可用的位置。要记住，在这个阶段我们通过前端通道进行通信，一旦把重定向发往授权端点，客户端应用就会在
OAuth 协议流程上暂时交出控制权，直到收到这次回调为止。我们还需要把 state 加入到授权 URL 发送的参数列表中。

```javascript
var authorizeUrl = buildUrl(authServer.authorizationEndpoint, {
    response_type: 'code',
    client_id: client.client_id,
    redirect_uri: client.redirect_uris[0],
    state: state
});
```

当授权服务器收到包含 state 参数的授权请求时，必须始终将该 state 参数原封不动地与授权码一并返回给客户端。这意味着，我们可以在
redirect_uri 页面中校验传入的 state 值，并将其与之前保存的值进行对比。如果不一致，就向终端用户显示错误信息。

```javascript
if (req.query.state != state) {
    res.render('error', {error: 'State value did not match'});
    return;
}
```

如果 state 的值与我们预期的不一致，那几乎可以确定有异常情况在发生，比如会话固定（session
fixation）攻击、试探（钓取）有效的授权码，或其他恶意操作。此时，客户端会立即停止对该请求的所有处理，并将用户跳转到错误页面。

## 使用令牌访问受保护资源

现在我们已经拿到访问令牌了，然后呢？能用它做什么？好在我们已经运行了一个方便的受保护资源，它正等着一个有效的访问令牌；一旦收到令牌，就会返回一些有价值的信息。

客户端要做的只是调用受保护资源，并把访问令牌放在三种合法位置中的任意一种。对于我们的客户端，我们会把令牌放到 Authorization
HTTP 头里——只要条件允许，这也是规范推荐的方式。

!!! note "发送 Bearer Token 的方式"

	我们手里的这类 OAuth 访问令牌称为 Bearer Token（不记名令牌），意思是谁持有这个令牌，谁就可以把它提交给受保护资源。OAuth Bearer Token Usage 规范实际上提供了三种传递令牌值的方式：

	- 作为 HTTP Authorization 头
	- 作为表单编码的请求体参数
	- 作为 URL 编码的查询参数
	
	只要可行，就推荐使用 Authorization 头，因为另外两种方式各有局限。使用查询参数时，访问令牌作为 URL 的一部分，可能会不小心泄露到服务器端日志中。使用表单编码参数则会限制受保护资源的输入类型——必须使用表单编码参数并通过 POST 方法提交。如果 API 本来就是这样设计的，那也没问题，因为它不会像查询参数那样带来同样的安全隐患。

	在这三种方法里，Authorization 头在灵活性和安全性方面都是最佳选择，但缺点是对某些客户端来说使用起来更麻烦。成熟的客户端或服务端库通常会在合适的场景下同时支持这三种方式；实际上，我们的演示受保护资源也会接受这三处任意位置携带的访问令牌。

再次打开客户端应用的首页 `http://localhost:9000/`，可以看到第二个按钮：Get Protected Resource。点击该按钮会跳转到数据展示页面。

```javascript
app.get('/fetch_resource', function (req, res) {

});
```

首先，我们得先确认是否真的拿到了 access token。要是没有，就给用户提示错误，然后直接退出。

```javascript
if (!access_token) {
    res.render('error', {error: 'Missing access token.'});
    return;
}
```

如果我们在未获取访问令牌的情况下运行这段代码，就会出现预期的错误页面，如图 3.5 所示。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251231101246274.png){ loading=lazy }
  <figcaption>图 3.5：当缺少访问令牌时，客户端显示的错误页面</figcaption>
</figure>

在这个函数的主体里，我们需要调用受保护资源，并把返回的数据交给页面进行渲染。首先得明确请求要发到哪里，我们已经在客户端代码顶部通过
protectedResource 变量配置好了目标地址。我们会向该 URL 发起一次 POST 请求，并期望得到一个 JSON 响应。换句话说，这就是一次非常标准的
API 调用请求。尽管如此，它现在还跑不通。受保护资源会认为这应该是一次已授权的调用，而我们的客户端虽然能够拿到 OAuth
Token，但还没把它用起来。我们需要按照 OAuth 的规范，通过 Authorization: Bearer 请求头把 Token 发送过去，并将 Token 作为该请求头的值。

```javascript
var headers = {
    'Authorization': 'Bearer ' + access_token
};
var resource = request('POST', protectedResource,
    {headers: headers}
);
```

这会向受保护的资源发起请求。如果请求成功，我们会解析返回的 JSON，并交给数据模板处理；否则，就把用户跳转到错误页面。

```javascript
if (resource.statusCode >= 200 && resource.statusCode < 300) {
    var body = JSON.parse(resource.getBody());

    res.render('data', {resource: body});
    return;
} else {
    res.render('error', {error: 'Server returned response code: ' + resource.statusCode});
    return;
}
```

总的来说，我们的请求函数如附录 B 的清单 3 所示。现在，当我们获取到访问令牌并拉取资源时，就会看到从 API 返回的数据展示（见图
3.6）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251231101438479.png){ loading=lazy }
  <figcaption>图 3.6：显示受保护资源 API 数据的页面。</figcaption>
</figure>

作为一个额外练习，可以尝试在发起请求且请求失败时，自动提示用户为客户端授权。你也可以在客户端检测到一开始就没有访问令牌时，进行同样的自动提示。

## 刷新访问令牌

现在我们已经用访问令牌获取了受保护资源，但如果在后续某个时刻访问令牌过期了怎么办？我们还需要再去打扰用户，让他重新为客户端应用授权一次吗？

OAuth 2.0 提供了一种在不需要用户交互参与的情况下获取新访问令牌的方法：刷新令牌（refresh token）。这项能力非常重要，因为 OAuth
经常用于这样一种场景：在最初完成授权委托之后，用户就不再在线了。我们在第 2 章已经详细讲过刷新令牌的特性，因此接下来我们会为客户端加入对刷新令牌的支持。

本练习将从一套新的代码开始，请打开 ch-3-ex-2 并运行 npm install
进行初始化。这个客户端已经配置了访问令牌和刷新令牌，但它的访问令牌已经不再有效，就像自签发以来已经过期一样。客户端并不知道自己的访问令牌当前无效，所以仍会照常尝试使用它。对受保护资源的这次请求会失败，我们将编写逻辑，让客户端使用刷新令牌获取新的访问令牌，然后用新的访问令牌再次调用受保护资源。启动三个应用，并用文本编辑器打开
client.js。如果你愿意，可以在修改之前先运行客户端，看看它确实会因令牌无效而失败，返回的 HTTP 错误码为 401（见图 3.7）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251231101602286.png){ loading=lazy }
  <figcaption>图 3.7 由于访问令牌无效，受保护资源返回 HTTP 错误码的错误页面</figcaption>
</figure>

!!! note "我的令牌还能用吗？"

	OAuth 客户端要如何判断自己的访问令牌是否仍然有效？唯一真正可靠的办法就是拿它去调用一次，看看结果如何。如果令牌预期会过期，授权服务器可以在令牌响应中通过可选的 expires_in 字段给出一个到期时间提示。该值表示从令牌签发时刻起，以秒为单位，令牌预计在多长时间后将不再可用。一个行为规范的客户端会留意这个值，并丢弃所有超过过期时间的令牌。

	不过，仅仅知道过期时间并不足以让客户端判断令牌的真实状态。在许多 OAuth 实现中，资源所有者可以在令牌到期之前就将其吊销。设计良好的 OAuth 客户端必须始终假设访问令牌可能在任何时刻突然失效，并能够据此做出相应处理。

如果你完成了上一练习的额外部分，你就知道可以提示用户再次为客户端授权，从而获取一个新的令牌。但这一次，我们拿到了刷新令牌，所以如果它能正常工作，我们就完全不必去打扰用户。刷新令牌最初是在与访问令牌相同的
JSON 对象中一起返回给该客户端的，如下所示：

```json
{
  "access_token": "987tghjkiu6trfghjuytrghj",
  "token_type": "Bearer",
  "refresh_token": "j2r3oj32r23rmasd98uhjrk2o3i"
}
```

我们的客户端已将该值保存到 refresh_token 变量中；我们在代码顶部将其设置为这个已知值，以此进行模拟。

```javascript
var access_token = '987tghjkiu6trfghjuytrghj';
var scope = null;
var refresh_token = 'j2r3oj32r23rmasd98uhjrk2o3i';
```

我们的授权服务器在启动时会先清空数据库，然后自动写入上一份 refresh token。需要注意的是，我们会在写入前刻意停顿 5
秒，让应用能够平稳完成启动。我们不会写入与上一份 refresh token 对应的 access token，因为我们希望构建这样一种环境：access
token 已经过期，但 refresh token 仍然可用。

```javascript
nosql.clear();
setTimeout(() => nosql.insert({
    refresh_token: 'j2r3oj32r23rmasd98uhjrk2o3i',
    client_id: 'oauth-client-1', scope: 'foo bar'
}), 5000);
```

现在我们需要处理令牌刷新。首先，我们捕获错误情况并使当前的访问令牌失效。为此，我们会在处理受保护资源响应的代码的 else
分支中加入相应的处理逻辑。

```javascript
if (resource.statusCode >= 200 && resource.statusCode < 300) {
    var body = JSON.parse(resource.getBody());
    res.render('data', {resource: body});
    return;
} else {
    access_token = null;
    if (refresh_token) {
        refreshAccessToken(req, res);
        return;
    } else {
        res.render('error', {error: resource.statusCode});
        return;
    }
}
```

在 refreshAccessToken 函数中，我们会像之前一样向 token 端点发起请求。可以看到，刷新访问令牌本质上是授权授予（authorization
grant）的一种特殊情况，因此 grant_type 参数使用 refresh_token。同时，我们还会把刷新令牌作为参数之一一并传过去。

```javascript
var form_data = qs.stringify({
    grant_type: 'refresh_token',
    refresh_token: refresh_token
});
var headers = {
    'Content-Type': 'application/x-www-form-urlencoded',
    'Authorization': 'Basic ' + encodeClientCredentials(client.client_id, client.client_secret)
};
var tokRes = request('POST', authServer.tokenEndpoint, {
    body: form_data,
    headers: headers
});
```

如果刷新令牌有效，授权服务器会返回一个 JSON 对象，就像这是首次正常调用令牌端点一样：

```json
{
  "access_token": "IqTnLQKcSY62klAuNTVevPdyEnbY82PB",
  "token_type": "Bearer",
  "refresh_token": "j2r3oj32r23rmasd98uhjrk2o3i"
}
```

现在我们可以像之前一样保存 access token 的值。这个响应里也可能会包含 refresh
token，而且它可能和第一次拿到的不一样。如果出现这种情况，客户端需要丢弃之前一直保存的旧 refresh token，并立即开始使用新的。

```javascript
access_token = body.access_token;
if (body.refresh_token) {
    refresh_token = body.refresh_token;
}
```

最后，我们可以让客户端再尝试获取一次资源。由于客户端的操作是由 URL 触发的，我们可以重定向回获取资源的
URL，重新启动整个流程。在线上环境中，实际实现通常会采用更精细、更完善的触发机制。

```javascript
res.redirect('/fetch_resource');
```

要看到它实际运行的效果，先加载你的组件，然后在客户端点击 Get Protected
Resource。客户端启动时带的是无效的访问令牌，本来会报错；但现在你应该会看到受保护资源的数据页面。再看看授权服务器的控制台：它会提示正在签发刷新令牌，并展示每次请求所使用的令牌值。

```shell
We found a matching refresh token: j2r3oj32r23rmasd98uhjrk2o3i
Issuing access token IqTnLQKcSY62klAuNTVevPdyEnbY82PB for refresh token j2r3oj32r23rmasd98uhjrk2o3i
```

你还可以通过点击客户端应用的标题栏，在客户端首页看到访问令牌（access token）的值已经发生变化。将当前的访问令牌和刷新令牌（refresh
token）值与应用启动时使用的值进行对比（见图 3.8）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251231102050336.png){ loading=lazy }
  <figcaption>图 3.8 刷新访问令牌后的客户端主页</figcaption>
</figure>

如果刷新令牌不起作用，会发生什么？我们会同时丢弃刷新令牌和访问令牌，并渲染一个错误页面。

``` javascript
} else {
  refresh_token = null;
  res.render('error', {error: 'Unable to refresh token.'});
  return;
}
```

不过，我们不必止步于此。既然这是一个 OAuth 客户端，我们其实回到了那种状态：就像一开始根本没有拿到 access token 一样——这时可以再次让用户为客户端授权。作为额外练习，请检测这种错误情况，并向授权服务器请求一个新的 access token。别忘了把新的 refresh token 也保存下来。

完整的 fetch 和 refresh 函数见附录 B 的清单 4。

## 小结

OAuth 客户端是 OAuth 生态中使用最广泛的一环。

- 使用授权码（authorization code）授权类型获取 token 只需要几个简单、直接的步骤。
- 如果客户端拿得到 refresh token，就可以在不打扰终端用户的情况下，用它换取新的 access token。
- 使用 OAuth 2.0 Bearer Token 比获取它更简单：只需在任意 HTTP 请求里加上一个 HTTP 头即可。

现在你已经了解客户端是如何工作的，接下来我们来构建一个资源，让客户端可以去访问它。

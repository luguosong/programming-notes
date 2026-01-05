# 5.构建一个简单的 OAuth 授权服务器

本章将涵盖

- 管理已注册的 OAuth 客户端
- 让用户为客户端进行授权
- 向已获授权的客户端签发令牌
- 签发并响应刷新令牌

在前两章中，我们构建了一个 OAuth
客户端应用：它从授权服务器获取令牌，并在受保护资源处使用该令牌；同时我们也搭建了供客户端访问的受保护资源。本章我们将构建一个简单的授权服务器，支持授权码（authorization
code）授权类型。这个组件负责管理客户端，执行 OAuth 的核心委托流程，并向客户端签发令牌。

!!! note

	本书中的所有练习与示例均使用 Node.js 和 JavaScript 编写。每个练习由若干组件组成，设计为在同一台机器上运行，并通过 localhost 的不同端口访问。有关该框架及其结构的更多信息，请参见附录 A。

可以说，授权服务器是 OAuth 生态中最复杂的组件，也是整个 OAuth 系统中的核心安全权威。只有授权服务器能够对用户进行身份认证、注册客户端并签发令牌。在
OAuth 2.0 规范的制定过程中，能从客户端或受保护资源转移的复杂性，尽可能都被转移到了授权服务器上。这主要源于组件数量的差异：客户端远多于受保护资源，而受保护资源又远多于授权服务器。

本章我们将从构建一个简单的服务器开始，后续会逐步加入更多能力与功能。

## 管理 OAuth 客户端注册

为了让客户端能够与 OAuth 服务器通信，OAuth 服务器需要为每个客户端分配一个唯一的客户端标识符。我们的服务器将采用静态注册（第
12 章会介绍动态客户端注册），并把所有客户端信息存储在服务器端的一个变量中。

打开 ch-5-ex-1，编辑 authorizationServer.js 文件。本练习中我们不会修改其他文件。在文件顶部有一个数组变量，用来保存客户端信息。

``` javascript
var clients = [

];
```

这个变量目前是空的，它将作为服务器端存放所有客户端信息的数据仓库。服务器的某个部分需要查询某个客户端的信息时，就会到这个数组里查找。在生产环境的
OAuth 系统中，这类数据通常会存放在某种数据库中；但在我们的练习里，希望你能直接看到并手动修改它。这里使用数组，是因为一般认为在
OAuth 系统中，授权服务器会同时处理大量客户端。

!!! note "谁来创建客户端 ID？"

	我们已经在 client.js 里把客户端配置成使用特定的 ID 和 secret，所以这里会直接把它们复制过来。在标准的 OAuth 系统中，授权服务器会向客户端软件签发 client ID 和 client secret，就像我们上一节练习里做的那样。我们这样处理是为了让你少敲点字，并尽可能把练习限定在只编辑一个文件。当然，如果你愿意，也可以打开 client.js，修改它配置里的这些值。

首先，我们先接收由客户端自身提供、而不是由授权服务器生成的那些值。我们客户端的 redirect URI
是 `http://localhost:9000/callback`，所以我们会在客户端列表中创建一个包含该值的新对象：

```javascript
var clients = [
    {
        "redirect_uris": ["http://localhost:9000/callback"]
    }
];
```

接下来，我们需要为客户端分配一个 client ID 和 client secret。我们将分别沿用上一章练习中使用的值：oauth-client-1 和
oauth-client-secret-1。（客户端已使用这些信息完成配置。）这样一来，我们的对象就会补全为如下结构：

```javascript
var clients = [
    {
        "client_id": "oauth-client-1",
        "client_secret": "oauth-client-secret-1",
        "redirect_uris": ["http://localhost:9000/callback"],

    }
];
```

最后，我们还需要一种方式，能够根据客户端 ID 来查到这些信息。通常数据库会用查询来完成这件事，而我们这里提供了一个简单的辅助函数，用来遍历数据结构，找到对应的客户端。

``` javascript
var getClient = function(clientId) {
  return __.find(clients, function(client) { return client.client_id == clientId; });
};
```

这个函数的实现细节并不重要，它只是对列表做了一次简单的线性遍历，查找指定客户端 ID 对应的客户端。调用这个函数会返回我们要找的客户端对象；如果没找到，则返回
undefined。现在服务器已经至少认识了一个客户端，我们就可以开始编写对应各个端点的服务端逻辑代码了。

## 授权客户端

在 OAuth
协议中，授权服务器必须提供两个端点：授权端点用于前端通道（front-channel）交互，令牌端点用于后端通道（back-channel）交互。如果你想了解前端通道和后端通道的具体工作方式，以及为什么两者缺一不可，可以参考第
2 章的讲解。本节我们将实现授权端点。

!!! note "一定要用 Web 服务器吗？"

	一句话：是的。正如我们在第 2 章所说，OAuth 2.0 是围绕 HTTP 设计的协议。尤其是授权服务器需要在前端通道和后端通道上都能通过 HTTP 被客户端访问。我们示例中使用的授权码（authorization code）授权类型要求前端通道与后端通道的接口都可用：前端通道必须能被资源所有者的浏览器访问，后端通道必须能被客户端自身直接访问。正如第 6 章将看到的那样，OAuth 里还有其他授权类型只使用前端通道或只使用后端通道；而我们这里的练习会同时用到两者。

目前也有人在推动将 OAuth 迁移到非 HTTP 协议上，例如受限应用协议（CoAP，Constrained Application Protocol），但这些方案仍以原本基于
HTTP 的规范为基础，我们在这里不会直接展开。想挑战一下的话，可以把练习里的 HTTP 服务器改造为使用另一种承载协议。

### 授权端点

在 OAuth 授权委派流程中，用户首先会到达授权端点。授权端点属于前端通道（front-channel）端点，客户端会将用户的浏览器重定向到这里以发起授权请求。该请求始终使用
GET 方法，我们会在 /authorize 路径上提供该服务。

``` javascript
app.get("/authorize", function(req, res){

});
```

首先，我们需要弄清楚是哪个客户端发起了请求。客户端会在 client_id 参数里传入自己的标识符，因此我们可以把它取出来，并通过上一节的辅助函数查找对应的客户端：

``` javascript
var client = getClient(req.query.client_id);
```

接下来，我们需要检查客户端传入的 client 是否存在。如果不存在，就无法授权它访问任何资源，因此我们会向用户显示一条错误信息。框架内置了一个简单的错误页面，我们在这里用它来把错误展示给用户。

``` javascript
if (!client) {
  res.render('error', {error: 'Unknown client'});
  return;
```

既然我们已经知道是哪个客户端声称在发起请求，接下来就需要对这个请求做一些基本的合理性校验。到目前为止，通过浏览器传进来的只有
client_id；由于它是经由浏览器的前端通道传递的，因此属于公开信息。也就是说，此时任何人都可能冒充这个客户端。不过我们仍然有一些手段来判断请求是否可信，其中最关键的一项就是核对传入的
redirect_uri 是否与该客户端注册的 redirect_uri 一致；如果不一致，同样应当视为错误。

``` javascript
} else if (!__.contains(client.redirect_uris, req.query.redirect_uri)) {
  res.render('error', {error: 'Invalid redirect URI'});
  return;
```

OAuth 规范以及我们对它的简化实现，都允许在同一个客户端注册信息中配置多个 redirect_uri。这样一来，应用在不同场景下可能通过不同
URL 提供服务时，就能把用户授权同意（consent）关联起来。作为一个额外练习，等这个服务器搭建并运行起来后，再回来为它增加对多个重定向
URI 的支持。

OAuth 定义了一种向客户端返回错误的机制：通过在客户端的重定向 URI 上追加错误码来实现。但我们这里的这两种错误情况都没有这么做。为什么？如果传入的
client ID 无效，或者 redirect URI 与预期不匹配，这都可能意味着有恶意方在对用户发起攻击。由于重定向 URI
的内容完全不受授权服务器控制，它可能指向钓鱼页面或恶意软件下载。授权服务器永远无法完全保护用户免受恶意客户端应用的侵害，但至少可以用很小的代价过滤掉某些类型的攻击。更多讨论见第
9 章。

最后，如果客户端校验通过，我们就需要渲染一个页面，请求用户授权。用户需要在该页面上进行交互并提交表单回到服务器，这会由浏览器发起另一次
HTTP 请求来完成。我们会保留当前入站请求的查询参数，并将它们以随机生成的 key 存入 requests 变量中，这样在表单提交后就能把这些参数取回来。

```javascript
var reqid = randomstring.generate(8);
requests[reqid] = req.query;
```

在生产系统中，你可以使用 Session 或其他服务端存储机制来保存这些内容。本练习在 approve.html
文件中提供了一个授权页面，接下来我们会将其渲染给用户。我们会传入之前查询到的客户端信息，以及前面生成的随机 Key。

```javascript
res.render('approve', {client: client, reqid: reqid});
```

客户端信息会展示给用户，帮助他们做出授权决策；同时，随机生成的 reqid
键会作为隐藏字段写入表单。这个随机值为我们的授权页面提供了一些基础的跨站请求伪造（CSRF）防护，因为在下一步进一步处理时，我们需要用它来查找原始请求数据。

至此，我们的函数整体效果如附录 B 中的清单 7 所示。

这只是对授权端点请求处理流程的前半部分。接下来该提示资源所有者，并询问他们是否授权该客户端。

### 授权客户端

如果你已经完成前面的步骤，现在就可以运行当前状态的代码了。记得同时启动这三个服务：client.js、authorizationServer.js 和
protectedResource.js。从 OAuth 客户端首页 http://localhost:9000/ 进入并点击 Get Token 按钮后，你会看到授权审批页面，如图
5.1 所示。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260104111033465.png){ loading=lazy }
  <figcaption>图 5.1：一个简单的审批页面</figcaption>
</figure>

我们的审批页面很简单：它会展示客户端信息，并让用户给出一个简单的同意/拒绝选择。现在该处理这个表单的结果了。

!!! note "用户到底是谁？"

	在我们的练习里，我们省略了一个关键步骤：对资源所有者进行身份认证。用户认证可以采用多种方式，许多中间件都能承担大部分繁重工作。在生产环境中，这是至关重要的一步，必须谨慎实现，才能正确处理。OAuth 协议并不规定、甚至也不关心资源所有者如何完成认证，只要授权服务器执行了这一步即可。

	可以尝试把用户身份认证加入到授权页和同意页，作为额外练习。你甚至可以使用基于 OAuth 的认证协议（例如 OpenID Connect，见第 13 章）来让资源所有者登录到授权服务器。

尽管本页表单的具体细节完全是我们应用自身的设计，并不属于 OAuth 协议的一部分，但使用授权表单在授权服务器中是相当常见的模式。我们的表单会向授权服务器的
/approve URL 发送一个 HTTP POST 请求，因此我们先从为它设置一个监听器开始。

``` javascript
app.post('/approve', function(req, res) {

});
```

当表单提交时，会发送类似下面这样的请求，其中表单值以 HTTP 表单编码格式（application/x-www-form-urlencoded）传递：

``` shell
POST /approve HTTP/1.1
Host: localhost:9001
User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:39.0) Gecko/20100101 Firefox/39.0
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
Referer: http://localhost:9001/authorize?response_type=code&scope=foo&client_id=oauth-client-1&redirect_uri=http%3A%2F%2Flocalhost%3A9000%2Fcallback&
state=GKckoHfwMHIjCpEwXchXvsGFlPOS266u
Connection: keep-alive

reqid=tKVUYQSM&approve=Approve
```

那个 reqid 是从哪儿来的？在页面的 HTML 里，服务器把我们在上一步生成并传给模板的随机字符串插了进去。渲染后的 HTML 大概长这样：

``` html
<input type="hidden" value="tKVUYQSM" name="reqid">
```

这个值会随表单一起提交，我们可以从请求体中取出该值，并以此查找对应的待处理授权请求。如果找不到与该代码匹配的待处理请求，很可能是一次跨站请求伪造（CSRF）攻击，我们可以将用户引导到错误页面。

``` javascript
var reqid = req.body.reqid;
var query = requests[reqid];
delete requests[reqid];

if (!query) {
  res.render('error', {error: 'No matching authorization request'});
  return;
}
```

接下来，我们需要判断用户点击的是 Approve 按钮还是 Deny 按钮。我们可以通过表单提交中是否包含 approve 变量来判断：只有在点击
Approve 按钮时，这个变量才会被提交。Deny 按钮同样会提交一个类似的 deny 变量，不过我们这里的处理逻辑是：只要不是点击了
Approve 按钮，一律视为拒绝。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260104112205791.png){ loading=lazy }
  <figcaption></figcaption>
</figure>

我们先处理第二种情况，因为它更简单。如果用户拒绝向一个本来有效的客户端授予访问权限，我们可以放心地把发生了什么告知客户端。由于这里使用的是前端通道通信，我们无法直接向客户端发送消息。不过，我们可以用与客户端向我们发起请求相同的方式：选取一个由客户端托管的
URL，在该 URL 上附加几个特殊的查询参数，然后将用户的浏览器重定向到生成的地址。为此会使用客户端的重定向
URI，这也就是为什么在第一请求到达时，我们要把它与已注册的客户端信息进行校验。在这种情况下，我们会返回一个错误消息，告诉客户端用户已拒绝访问。

``` javascript
var urlParsed = buildUrl(query.redirect_uri, {
  error: 'access_denied'
});
res.redirect(urlParsed);
return;
```

相反，如果用户批准了该应用，我们首先要确认客户端期望返回哪种响应。由于我们实现的是授权码（authorization code）授权方式，我们会检查
response_type 的值是否为 code。若为其他任何值，就用刚才同样的方法向客户端返回错误。（第 6 章将介绍如何支持其他取值。）

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260104112344295.png){ loading=lazy }
  <figcaption></figcaption>
</figure>

既然我们已经明确了要返回哪种响应，就可以生成一个授权码并回传给客户端。同时，我们还需要把这个授权码存到服务器上的某个地方，方便客户端在下一步访问令牌端点时进行查询。对于这个简化版的授权服务器，我们会沿用搭建审批页面时用过的做法：把刚生成的授权码作为索引，将相关数据保存到服务器端的一个对象里。在生产环境中，这些数据通常会存储在数据库中，但仍然需要能够通过授权码的值进行访问，稍后你会看到原因。

``` javascript
var code = randomstring.generate(8);

codes[code] = { request: query };

var urlParsed = buildUrl(query.redirect_uri, {
  code: code,
  state: query.state
});
res.redirect(urlParsed);
return;
```

请注意，我们返回的不只是 code。还记得上一章我们在客户端设置了 state 参数并随请求一起发给服务器，用来保护客户端自身吗？现在我们处在流程的另一端，就必须把
state 参数原封不动地透传回去，和我们收到的完全一致。尽管客户端并不强制要求发送 state
值，但只要客户端发送了，服务器就必须在响应中把它返回。因此，综合起来，我们用于处理用户授权页面返回响应的函数现在如附录 B 的清单
8 所示。

到这里，授权服务器已经把控制权交还给客户端应用，接下来需要等待交易的下一步：客户端通过后通道向 Token Endpoint 发起请求。

## 签发令牌

回到客户端后，上一节生成的授权码会通过客户端的重定向 URI 传入。客户端随后拿着这个 code，向授权服务器的 token 端点构造并发送一个
POST 请求。这种后端通道通信发生在用户浏览器之外，由客户端与授权服务器直接交互。由于 token 端点并不面向用户，因此完全不使用
HTML 模板系统。错误则通过 HTTP 错误码与 JSON 对象的组合返回给客户端，我们将在这里看到它们的实际用法。

接下来我们会在 /token 上设置一个 POST 请求监听器来处理它：

### 客户端认证

首先，我们得确认到底是哪一个客户端发起了请求。OAuth 提供了几种让客户端向授权服务器进行认证的方式，而在 OAuth 之上构建的协议（比如
OpenID Connect）又定义了更多方法（第 13 章会更深入地讲 OpenID Connect，不过那些额外方法同样留给读者自行练习）。对于我们这个简化版服务器，我们只支持两种最常见的方式：通过
HTTP Basic 认证传递 client_id 和 client_secret，以及通过表单参数传递它们。

这里我们遵循良好的服务端编程原则，对输入更“宽容”一些：允许客户端按自己的选择，用任意一种方式提交凭据。我们会先检查
Authorization 头（规范里更推荐这种方式），如果没有再回退到表单参数。

在 HTTP Basic 中，Authorization 头的内容是一个 Base64 编码的字符串：把用户名和密码用一个冒号（:）拼接起来后再编码。OAuth 2.0
规定用户名使用 client_id、密码使用 client_secret，但在拼接前，两者都要先进行 URL
编码。服务端需要按相反的顺序把它解出来，因此我们提供了一个工具函数来替你处理这些繁琐细节。把该函数的返回结果取出来，并保存到变量中。

``` javascript
var auth = req.headers['authorization'];
if (auth) {
  var clientCredentials = decodeClientCredentials(auth);
  var clientId = clientCredentials.id;
  var clientSecret = clientCredentials.secret;
}
```

接下来，我们需要检查客户端是否在表单请求体中提交了 client_id 和 client_secret。你可能会认为只有在没有 Authorization
头的情况下才需要检查这里，但我们还必须确保客户端没有同时在两个位置都发送 ID 和
secret——否则会触发错误（甚至可能造成安全漏洞）。如果没有错误，获取这些值就很简单了：直接从表单输入中拷贝出来即可，如下所示。

``` javascript
if (req.body.client_id) {
  if (clientId) {
      res.status(401).json({error: 'invalid_client'});
      return;
  }
  var clientId = req.body.client_id;
  var clientSecret = req.body.client_secret;
}
```

接下来，我们通过辅助函数查找客户端。如果未找到客户端，则返回错误。

``` javascript
var client = getClient(clientId);
if (!client) {
  res.status(401).json({error: 'invalid_client'});
  return;
}
```

我们还需要确保，客户端在传输过程中带来的 client secret，和我们为该客户端预期的 client secret 完全一致。如果不一致，你猜对了：我们就返回一个错误。

``` javascript
if (client.client_secret != clientSecret) {
  res.status(401).json({error: 'invalid_client'});
  return;
}
```

此时，我们已经确认客户端是合法的，可以开始正式处理令牌请求了。

### 处理授权许可请求

首先，我们需要检查 grant_type 参数，确保它是我们能够处理的类型。我们这个小服务器只支持授权码（authorization
code）许可类型，对应的值也不意外，就是 authorization_code。如果收到的 grant type 不是我们支持、能够处理的类型，就返回一个错误。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260104131324327.png){ loading=lazy }
  <figcaption></figcaption>
</figure>

如果我们确实收到了授权码授权（authorization code grant），就需要从请求中取出该 code，并到我们在上一节写入的授权码存储对象中进行查找。如果找不到这个
code，就向客户端返回错误。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260104132342216.png){ loading=lazy }
  <figcaption></figcaption>
</figure>

如果我们能在代码存储中找到传入的授权码，就需要确认它确实是签发给这个客户端的。好在上一节保存授权码时，我们也一并保存了发起到授权端点请求的相关信息，其中包含客户端
ID。把两者进行比对；如果不一致，就返回错误。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260104132729982.png){ loading=lazy }
  <figcaption></figcaption>
</figure>

注意，一旦我们确认该授权码有效，就会立刻将其从存储中移除，而不管后续流程是否继续处理。这样做是出于稳妥考虑：如果被恶意客户端拿到并提交了被盗的授权码，就应当视为该授权码已经丢失。即便之后真正的客户端带着同一个授权码再来请求，该授权码也不会再生效，因为我们已经知道它早已泄露并被攻破。接下来，如果客户端确实匹配，我们就需要生成一个访问令牌（access
token），并将其存储起来，以便后续能够根据它进行查询。

``` javascript
var access_token = randomstring.generate();
nosql.insert({access_token: access_token, client_id: clientId});
```

为简单起见，我们使用 Node.js 的 nosql 模块，把令牌保存到本地的、基于文件的 NoSQL 数据库中。在生产级的 OAuth
系统里，令牌的处理方式有很多选择。你可以把令牌存进一个完整的数据库；为了进一步提升安全性，也可以只存储令牌值的密码学哈希，这样即使数据库被攻破，令牌本身也不会泄露。[^1]
另外，你的资源服务器还可以通过令牌内省（token
introspection）回到授权服务器查询令牌信息，而不需要共享数据库。或者，如果你无法存储（或不想存储）令牌，也可以采用结构化格式，把所有必要信息直接封装到令牌里，让受保护资源在后续使用时无需再查询即可解析消费。我们将在第
11 章介绍这些方法。

[^1]: 当然，如果你的安全服务器数据库被攻破了，你要担心的可不止这一件事。

!!! note "令牌里到底有什么？"

	OAuth 2.0 在访问令牌的内部内容方面“刻意保持沉默”，而且这是有充分理由的：可选方案很多，每一种都有各自的取舍，适用于不同的使用场景。与 Kerberos、WS-Trust、SAML 等早期安全协议不同，OAuth 的运作并不要求客户端了解令牌内部包含什么。授权服务器和受保护资源需要能够处理令牌，但它们可以自行选择任何方式来相互传递这些信息。

	因此，OAuth 令牌可以只是一个没有任何内部结构的随机字符串——就像我们练习中的令牌一样。如果资源服务器与授权服务器部署在同一处（正如本练习），它就可以在共享数据库中查询令牌值，从而确定该令牌颁发给了谁，以及具备哪些权限。另一种做法是让 OAuth 令牌具备结构化内容，例如 JSON Web Token（JWT），甚至是 SAML 断言。这些令牌可以被签名、加密，或同时进行签名与加密；而在使用过程中，客户端依然可以完全不必知道令牌内部到底有什么。我们会在第 11 章更深入地介绍 JWT。

现在我们已经拿到了令牌，并将其保存以便后续使用，终于可以把它返回给客户端了。令牌端点的响应是一个 JSON 对象，其中包含访问令牌的值，以及一个
token_type 指示符，用来告诉客户端这是什么类型的令牌，从而也就决定了该如何将它用于访问受保护资源。我们的 OAuth 系统使用的是
bearer token，因此我们会这样告知客户端。我们也会在第 15 章提前介绍另一种令牌——持有证明（Proof of Possession，PoP）。

``` javascript
var token_response = { access_token: access_token, token_type: 'Bearer' };
res.status(200).json(token_response);
```

有了最后这段代码，我们的令牌端点处理函数就和附录 B 的清单 9 一样了。

到这里，我们已经实现了一个简单但功能完整的授权服务器。它能够对客户端进行认证，提示用户进行授权，并通过授权码流程签发随机生成的
Bearer Token。你可以这样验证：从 OAuth 客户端 http://localhost:9000/ 开始，获取一个令牌，批准授权，然后在受保护资源上使用它。

作为额外练习，给访问令牌添加一个较短的过期时间。你需要存储它们的过期时间，并在响应中把 expires_in 参数返回给客户端。还需要修改
protectedResource.js，让资源服务器在处理请求前先检查令牌是否已过期。

## 添加刷新令牌支持

现在我们已经能够签发访问令牌了，也希望能够同时签发并支持刷新令牌。你应该还记得第 2
章提到过，刷新令牌并不是用来访问受保护资源的，而是客户端用来在不需要用户再次介入的情况下获取新的访问令牌。好在我们为让服务器签发访问令牌所做的工作不会白费，我们将基于上一个练习继续完善项目。打开
ch-5-ex-2 并编辑 authorizationServer.js 文件；如果你愿意，也可以在完成上一个练习后，直接在其基础上继续添加。

首先，我们需要签发刷新令牌。刷新令牌与 Bearer Token 类似，会与访问令牌一起签发。在我们的令牌端点函数中，我们将生成并保存刷新令牌的值，并与现有的访问令牌值一并存储。

``` javascript
var refresh_token = randomstring.generate();
nosql.insert({ refresh_token: refresh_token, client_id: clientId });
```

这里我们使用同一个随机字符串生成函数，并将 refresh token 存储在同一个 NoSQL 数据库中。不过，我们会把 refresh token
存在另一个不同的键下面，这样授权服务器和受保护资源就能区分这两类 token。之所以要这么做，是因为 refresh token 只能在授权服务器上使用，而
access token 只能在受保护资源上使用。token 生成并存储完成后，将它们并行一起返回给客户端：

``` javascript
var token_response = { access_token: access_token, token_type: 'Bearer',
refresh_token: req.body.refresh_token };
```

token_type 参数（以及在发送时的 expires_in 和 scope
参数）只适用于访问令牌，不适用于刷新令牌；刷新令牌也没有对应的等价参数。刷新令牌同样可能过期，但由于刷新令牌通常被设计为具有较长的生命周期，客户端不会获得它何时过期的提示。当刷新令牌失效后，客户端只能退回到最初用于获取访问令牌的常规
OAuth 授权方式，例如授权码模式（authorization code grant）。

既然我们开始签发刷新令牌，就需要能够响应刷新令牌的请求。在 OAuth 2.0 中，刷新令牌是在令牌端点（token
endpoint）以一种特殊的授权许可（authorization grant）来使用的。它对应一个独立的 grant_type 值：refresh_token；我们可以在之前处理
authorization_code 授权类型的那段分支逻辑里，同样对它进行判断处理。

``` javascript
} else if (req.body.grant_type == 'refresh_token') {
```

首先，我们需要在令牌存储中查找刷新令牌。在示例代码里，我们会通过对 NoSQL 存储发起查询来完成这一步。尽管具体实现依赖于示例所用的框架，但本质上就是一次简单的检索操作。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260104134305767.png){ loading=lazy }
  <figcaption></figcaption>
</figure>

现在我们必须确认，这个令牌确实是签发给在令牌端点完成认证的那个客户端的。如果不做这一步校验，恶意客户端就可能窃取正常客户端的刷新令牌，并用它为自己换取全新、完全有效（但属于欺诈）的访问令牌，而且这些令牌看起来还像是由合法客户端获取的。我们还会把该刷新令牌从存储中移除，因为可以认为它已经被泄露了。

``` javascript
if (token.client_id != clientId) {
  nosql.remove().make(function(builder) { builder.where('refresh_token', 
  req.body.refresh_token); });
  res.status(400).json({error: 'invalid_grant'});
  return;
}
```

最后，如果所有检查都通过，我们就可以基于该刷新令牌生成一个新的访问令牌，将其保存，并返回给客户端。令牌端点的响应与其他 OAuth 授权类型使用的响应完全一致。这意味着客户端无需做任何特殊处理，就能统一处理通过刷新令牌或授权码获取的访问令牌。我们还会在响应中返回发起本次请求时使用的同一个刷新令牌，告诉客户端它之后仍然可以继续使用该刷新令牌。

``` javascript
var access_token = randomstring.generate();
nosql.insert({ access_token: access_token, client_id: clientId });
var token_response = { access_token: access_token, token_type: 'Bearer',  refresh_token: token.refresh_token };
res.status(200).json(token_response);
```

你的令牌端点中处理刷新令牌的那段逻辑，看起来就像附录 B 的清单 10。

当客户端获得授权后，会在拿到访问令牌的同时获得一个刷新令牌。之后，如果访问令牌因任何原因被撤销或禁用，客户端就可以使用这个刷新令牌来继续获取访问权限。

!!! note "把我的令牌都丢掉！"

	除了可以选择设置过期时间之外，访问令牌和刷新令牌也可以在任何时候因为各种原因被撤销。资源所有者可能决定不再使用这个客户端，或者授权服务器可能对某个客户端的行为产生怀疑，从而决定提前清除发放给该客户端的所有令牌。作为一个附加练习，在授权服务器上做一个页面，让你可以按系统中的每个客户端清空其访问令牌。

	我们会在第 11 章更深入地讨论令牌生命周期，包括令牌撤销协议。

当刷新令牌被使用时，授权服务器可以选择签发一个新的刷新令牌来替换旧的。授权服务器也可以决定丢弃该客户端在使用刷新令牌之前拿到的所有仍处于有效状态的访问令牌。作为一个附加练习，把这些能力也加入到授权服务器中。

## 添加 scope 支持

OAuth 2.0 中一个非常重要的机制是 scope。正如我们在第 2 章中了解到的，并在第 4 章的实践中看到的那样，scope 表示与某次具体授权委托绑定的一部分访问权限子集。要完整支持 scope，我们需要在服务器端做一些调整。打开 ch-5-ex-3 并编辑 authorizationServer.js 文件；或者在完成上一个练习后，继续在之前的代码基础上修改。本练习中，client.js 和 protectedResource.js 文件无需改动。

首先，服务端通常会限制每个客户端能够申请的 scope 范围。这能作为对异常客户端的第一道防线，也能让系统控制哪些软件可以在受保护资源上执行哪些操作。我们要在文件顶部的客户端结构中新增一个成员：scope。

``` javascript
var clients = [
  {
      "client_id": "oauth-client-1",
      "client_secret": "oauth-client-secret-1",
      "redirect_uris": ["http://localhost:9000/callback"],
      "scope": "foo bar"
  }
];
```

该成员是一个以空格分隔的字符串列表，每个字符串代表一个 OAuth 作用域（scope）值。仅仅以这种方式注册，并不会让 OAuth 客户端自动获得该作用域所保护资源的访问权限，因为它仍然需要资源所有者的授权。

客户端在发起授权请求时，可以通过 scope 参数申请其作用域集合中的一个子集。scope 参数是一个字符串，包含以空格分隔的作用域值列表。我们需要在授权端点解析它，并将其转换为数组以便处理，然后存入 rscope 变量。类似地，客户端也可以（可选地）关联一组作用域，就像我们之前看到的那样，我们会把它解析成数组并存入 cscope 变量。不过，由于 scope 是可选参数，我们在处理时需要稍微谨慎一些，以防请求中没有传入该值。

```javascript
var rscope = req.query.scope ? req.query.scope.split(' ') : undefined;
var cscope = client.scope ? client.scope.split(' ') : undefined;
```

通过这样解析，我们就能避免误把不存在的值按空格拆分，否则会导致代码执行失败。

!!! note "为什么要用空格分隔的字符串集合？"

	scope 参数在整个 OAuth 流程里都被表示为用空格分隔的一组字符串（再编码成一个字符串），乍看之下确实有点奇怪，尤其是流程中的某些环节（例如 token 端点的响应）使用的是 JSON，而 JSON 天生就支持数组。你也会注意到，在我们自己的代码里处理 scope 时，通常会直接使用字符串数组。你可能还已经意识到，这种编码方式意味着 scope 值本身不能包含空格（因为空格就是分隔符）。那为什么还要用这种看起来别扭的编码方式呢？

	原因在于，HTTP 表单和查询字符串并不擅长表达数组、对象这类复杂结构，而 OAuth 又需要通过前端通道使用查询参数来传递值。要把任何东西塞进这个空间里，就必须用某种方式编码。虽然也有一些比较常见的“取巧”做法，比如把 JSON 数组序列化成字符串，或者重复同一个参数名多次，但 OAuth 工作组认为，对客户端开发者来说，把多个 scope 值用空格连接成一个字符串会简单得多。之所以选择空格作为分隔符，是因为这样在以 URI 作为 scope 值的系统中，分隔看起来更自然。

接下来，我们需要确保客户端不会申请超出自身权限范围的 scope。这个可以通过一个简单的比较来实现：把请求的 scopes 和客户端注册的 scopes 做对比（这里使用 Underscore 库的集合差集函数来完成）。

``` javascript
if (__.difference(rscope, cscope).length > 0) {
  var urlParsed = buildUrl(req.query.redirect_uri, {
      error: 'invalid_scope'
  });
  res.redirect(urlParsed);
  return;
}
```

我们还会修改对用户审批页面模板的调用，把 rscope 的值传进去。这样我们就能渲染一组复选框，让用户精确选择要批准客户端哪些 scope。通过这种方式，客户端最终拿到的 token 可能会比它申请的权限更弱，但这取决于授权服务器的策略，以及在我们的场景中资源所有者的决定。如果客户端对获批的 scope 不满意，它随时可以再让用户授权一次。实际使用中这会让用户很烦，因此更推荐客户端只申请正常运行所必需的 scope，避免出现这种情况。

``` javascript
res.render('approve', {client: client, reqid: reqid, scope: rscope});
```

在页面里，我们放了一小段代码，会遍历这些 scope，并在表单中为每个 scope 渲染一个复选框。我们已经提供了这部分代码；如果你愿意，可以打开 approve.html 页面，亲自看看这段代码具体长什么样。

``` html
<% if (scope) { %>
<p>The client is requesting access to the following:</p>
<ul>
<% _.each(scope, function(s) { %>
  <li><input type="checkbox" name="scope_<%- s %>" id="scope_<%- s %>" checked="checked"> <label for="scope_<%- s %>"><%- s %></label></li>
<% }); %>
</ul>
<% } %>
```

我们一开始把所有选项都勾选上，是因为客户端之所以请求这些权限，通常都有其合理原因，而且大多数用户很可能会保持页面的默认状态不作修改。不过，我们也希望给资源所有者一个选择：他们可以通过取消勾选复选框来移除其中一部分权限。

接下来我们要深入看看负责处理授权页面的函数内部实现。记住，它一开始是这样的：

``` javascript
app.post('/approve', function(req, res) {
```

由于表单模板会用 scope_ 前缀加上对应的 scope 值为每个复选框设置唯一标识，我们可以通过查看表单提交的入参来判断哪些复选框仍然处于勾选状态，从而确定资源所有者允许了哪些 scope。我们将使用几个 Underscore 的函数来让这段处理更简洁清晰；当然，如果你更喜欢，用 for 循环也同样能实现。我们已经把这部分封装成一个工具函数，并为你准备好了。

``` javascript
var getScopesFromForm = function(body) {
  return __.filter(__.keys(body), function(s) { return
  __.string.startsWith(s, 'scope_'); })
                      .map(function(s) { return
  s.slice('scope_'.length); });
};
```

既然我们已经拿到了已批准的 scope 列表，就需要再次确认它没有超出客户端被授权的范围。你可能会问：“等等，我们上一步不是已经检查过了吗？”没错，我们检查过，但我们渲染到浏览器的表单，或表单提交回来的 POST 请求，都可能被用户或浏览器内运行的代码篡改。用户可能注入客户端并未请求、甚至可能根本未被授权的新增 scope。再说了，只要条件允许，服务端对所有输入进行校验始终是个好习惯。

``` javascript
var rscope = getScopesFromForm(req.body);
var client = getClient(query.client_id);
var cscope = client.scope ? client.scope.split(' ') : undefined;
if (__.difference(rscope, cscope).length > 0) {
  var urlParsed = buildUrl(query.redirect_uri, {
      error: 'invalid_scope'
  });
  res.redirect(urlParsed);
  return;
}
```

现在我们需要把这些 scope 和我们生成的授权码一起存起来，这样在 token 端点就能再次取到。你会发现，用这种方式我们可以在授权码上挂载各种任意信息，这对于更高级的处理流程很有帮助。

``` javascript
codes[code] = { request: query, scope: rscope };
```

接下来，我们需要编辑 Token 端点的处理器。回想一下，它一开始是这样的：

``` javascript
app.post("/token", function(req, res){
```

这里我们需要把这些 scope 从原始的授权记录里取出来，并应用到我们生成的 token 上。由于它们是和授权码对象一起存储的，我们可以直接从那里读取，然后写入到我们的 token 中。

```javascript
nosql.insert({ access_token: access_token, client_id: clientId, scope: code.scope });
nosql.insert({ refresh_token: refresh_token, client_id: clientId, scope: code.scope });
```

最后，我们可以在令牌端点的响应中告诉客户端该令牌签发时包含的 scope。为了与请求时使用的空格分隔格式保持一致，在把 scope 数组加入响应的 JSON 对象时，需要将其重新格式化为字符串。

``` javascript
var token_response = { access_token: access_token, token_type: 'Bearer',
refresh_token: refresh_token, scope: code.scope.join(' ') };
```

现在，我们的授权服务器已经能够处理带作用域的令牌请求，并允许用户决定最终向客户端签发哪些作用域。这样一来，受保护资源就能把访问权限划分得更精细，客户端也可以只申请自己真正需要的访问权限。

在刷新令牌请求中，允许指定刷新令牌最初签发时所包含作用域的一个子集，并将其绑定到新的访问令牌上。这使得客户端可以用刷新令牌去请求权限严格更弱的新访问令牌，而不是动用它已获授权的全部权限，从而符合“最小权限原则”的安全理念。作为一个额外练习，请在令牌端点的处理函数中，为 refresh_token 授权类型加入这种“降权（down-scoping）”支持。我们已经在服务器中保留了基础的刷新令牌支持，但你需要自己改造令牌端点，让它能够正确解析、校验并按需附加这些作用域。

## 总结  

OAuth 授权服务器可以说是 OAuth 系统中最复杂的部分。

- 前端通道与后端通道的响应通信需要使用不同的技术手段，即便请求和响应看起来相似。
- 授权码流程需要在多个步骤之间跟踪数据，最终产出访问令牌。
- 授权服务器存在许多潜在的攻击入口，必须逐一采取恰当的缓解措施。
- 刷新令牌与访问令牌一并签发，可在无需用户参与的情况下生成新的访问令牌。
- 作用域用于限制访问令牌的权限范围。

现在你已经看到，在最典型的配置下，OAuth 系统的各个组件是如何协同工作的；接下来我们再看看一些其他选项，以及在真实世界中整个系统是如何组合在一起的。

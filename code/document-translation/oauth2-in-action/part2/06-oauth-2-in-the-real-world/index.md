# 6.真实世界中的 OAuth 2.0

本章内容包括

- 在不同场景下使用不同的 OAuth 授权类型
- 应对原生 Web 应用与基于浏览器的应用
- 在配置阶段与运行阶段处理密钥

到目前为止，本书讨论的 OAuth 2.0 相对处在一种理想化的状态：所有应用看起来都一样，所有资源也都一样，大家做事的方式也完全一致。第
2 章中我们用一个带客户端密钥（client secret）的 Web 应用，完整演示了授权许可（authorization grant）协议。而第 3、4、5
章的所有练习，也都沿用了同样的设置。

用这种方式做一些简化假设，有助于学习系统的基本原理；但现实世界里我们构建的应用必须面对各种各样的变化。OAuth 2.0
通过在协议关键位置引入灵活性，提前考虑了这些差异。在本章中，我们将更深入地讨论其中一些扩展点。

## 授权类型（Authorization grant types）

在 OAuth 1.0 中，获取访问令牌（access token）只有一种方法，所有客户端都必须使用它。它被设计得尽可能通用，试图适配各种部署方式。结果就是：该协议并不特别适合任何一种具体场景。Web
应用不得不处理“请求令牌”（request token）——这原本是给原生应用用来轮询状态变化的；原生应用则不得不处理“消费者密钥”（consumer
secret）——这原本是用来保护 Web 应用的；而所有人还都得应付一套定制的签名机制。它的表现足以让 OAuth
成为强大且基础性的技术，但仍然有许多地方不尽如人意。

在开发 OAuth 2.0 时，工作组明确决定把核心协议当作一个框架，而不是一套单一协议。通过夯实协议的核心概念，并在特定区域允许扩展，OAuth
2.0 可以以多种不同方式落地应用。虽然有人认为任何系统的第二版都会演变成一个抽象框架，[^1] 但就 OAuth
而言，这些抽象极大提升了它的可扩展性与实用性。

[^1]: 这被称为第二系统综合征，而且已经有大量研究。众所周知，这种综合征会因为引入过多的抽象和复杂性，把原本完全合理的解决方案活活“做死”。不过，OAuth
2.0 大概率不会走到那一步。我们希望如此。

OAuth 2.0 的一个关键差异点在于授权许可（authorization grant），也就是大家常说的 OAuth
流程（flow）。正如我们在前几章中提到的那样，授权码（authorization code）许可类型只是 OAuth
客户端从授权服务器获取令牌的多种方式之一。由于我们已经对授权码许可进行了非常详细的讲解，本节将重点介绍其他几种主要选项。

### 隐式许可类型

授权码流程中的各个步骤有一个重要特点：它会在不同组件之间对信息进行隔离。这样一来，浏览器不会得知只有客户端才应该知道的信息，客户端也无法看到浏览器的状态，等等。但如果我们把客户端放进浏览器里呢（见图
6.1）？

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260104140654412.png){ loading=lazy }
  <figcaption>图 6.1：隐式授权类型</figcaption>
</figure>

这就是一个完全在浏览器内运行的 JavaScript
应用会遇到的情况。客户端无法对浏览器保留任何秘密，因为浏览器能够完整洞察客户端的执行过程。在这种场景下，把授权码经由浏览器传回客户端，再由客户端用它去交换令牌，并没有真正的收益：这层额外的“秘密”并不能抵御任何参与其中的一方。

隐式授权类型通过直接在授权端点返回令牌，省去了这份额外的秘密及其带来的往返过程。因此，隐式授权类型只使用前端通道[^2]
与授权服务器通信。对于嵌入在网站中的 JavaScript 应用来说，这种流程非常有用：它们需要在不同安全域之间进行已授权、且可能受限的会话共享。

[^2]: 我们在第 2 章里讲过前通道和后通道，还记得吗？

隐式授权（Implicit Grant）存在一些必须认真权衡的严重限制。首先，使用该流程的客户端几乎不可能安全地保存 client secret，因为这个
secret 会暴露给浏览器本身。由于该流程只使用授权端点（authorization endpoint），而不使用令牌端点（token
endpoint），这一限制并不会影响其正常工作——客户端本来也不需要在授权端点进行身份认证。然而，缺乏任何对客户端进行认证的手段会显著影响该授权类型的安全性，因此应谨慎使用。此外，隐式流程无法获取
refresh token。由于浏览器内应用天生是短生命周期的，通常只在加载它们的浏览器上下文会话期间存活，refresh token
的实际价值也非常有限。更进一步，与其他授权类型不同，可以认为资源所有者仍然在浏览器中并可随时重新为客户端授权。授权服务器仍然可以应用首次使用即信任（Trust
On First Use，TOFU）原则，使得必要时的重新认证能够以无感、顺畅的用户体验完成。

客户端向授权服务器的授权端点发送请求，方式与授权码（authorization code）流程相同，只是这一次 response_type 参数设置为 token
而不是 code。这会通知授权服务器立即生成访问令牌，而不是先生成一个需要再兑换为令牌的授权码。

``` shell
HTTP/1.1 302 Moved Temporarily
Location: http://localhost:9001/authorize?response_type=token&scope=foo&client_
id=oauth-client-1&redirect_uri=http%3A%2F%2Flocalhost%3A9000%2Fcallback&state
=Lwt50DDQKUB8U7jtfLQCVGDL9cnmwHH1
Vary: Accept
Content-Type: text/html; charset=utf-8
Content-Length: 444
Date: Fri, 31 Jul 2015 20:50:19 GMT
```

客户端可以通过整页重定向来实现，也可以在页面内嵌入一个内联框架（iframe）来完成。无论哪种方式，浏览器都会向授权服务器的授权端点发起请求。资源所有者以与授权码流程相同的方式完成身份认证并对客户端进行授权。不同的是，这一次授权服务器会立即生成令牌，并将其附加到授权端点响应的
URI 片段（fragment）中返回。请记住，由于这是前端通道（front channel），返回给客户端的响应是通过一次 HTTP 重定向，跳回到客户端的重定向
URI。

``` shell
GET /callback#access_token=987tghjkiu6trfghjuytrghj&token_type=Bearer HTTP/1.1
Host: localhost:9000
User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:39.0) Gecko/20100101 Firefox/39.0
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
Referer: http://localhost:9001/authorize?response_type=code&scope=foo&client_
id=oauth-client-1&redirect_uri=http%3A%2F%2Flocalhost%3A9000%2Fcallback&state
=Lwt50DDQKUB8U7jtfLQCVGDL9cnmwHH1
```

URI 的片段（fragment）部分通常不会回传给服务器，这意味着 token 的值只会在浏览器内可用。不过需要注意，不同浏览器的实现和版本可能会导致这一行为有所差异。

我们来动手实现一下。打开 ch-6-ex-1，开始编辑 -authorizationServer.js 文件。在处理授权页提交的那个函数里，我们已经在 if
语句中写好了一个分支，用来处理 response_type 为 code 的情况。

``` javascript
if (query.response_type == 'code') {
```

我们将在这个代码块中新增一个分支，用来处理 response_type 为 token 的情况。

``` javascript
} else if (query.response_type == 'token') {
```

在这个新的代码块中，我们需要像处理授权码模式那样，对请求做同样的处理：校验 scope，并根据请求核验用户授权。注意，这里错误信息是通过
URL 的 hash（片段）返回的，而不是作为查询参数返回。

``` javascript
var rscope = getScopesFromForm(req.body);
var client = getClient(query.client_id);
var cscope = client.scope ? client.scope.split(' ') : undefined;
if (__.difference(rscope, cscope).length > 0) {
  var urlParsed = buildUrl(query.redirect_uri,
      {},
      qs.stringify({error: 'invalid_scope'})
  );
  res.redirect(urlParsed);
  return;
}
```

然后我们会像往常一样生成访问令牌。请记住，我们不会创建刷新令牌。

``` javascript
var access_token = randomstring.generate();
nosql.insert({ access_token: access_token, client_id: clientId.client_id, scope: rscope });

var token_response = { access_token: access_token, token_type: 'Bearer', scope: rscope.join(' ') };
if (query.state) {
      token_response.state = query.state;
}
```

最后，通过重定向 URI 的哈希片段（fragment）把它传回客户端。

``` javascript
var urlParsed = buildUrl(query.redirect_uri,
  {},
  qs.stringify(token_response)
);
res.redirect(urlParsed);
return;
```

我们会在 6.2.2 节讨论浏览器内客户端时，详细介绍客户端侧实现的细节。现在，你应该可以在 http://localhost:9000/
加载客户端页面，客户端会像其他练习一样获取访问令牌并调用受保护资源。当你从授权服务器返回后，注意客户端会在重定向 URI
的哈希（hash）部分带回令牌值本身。受保护资源在处理和校验这个令牌时不需要做任何不同的事情，但它需要配置跨域资源共享（CORS），我们将在第
8 章介绍。

### 客户端凭证授权类型

如果没有明确的资源所有者，或者资源所有者与客户端软件本身无法区分，该怎么办？这种情况相当常见：后端系统需要彼此直接通信，并不一定是代表某个特定用户。在没有用户将授权委托给客户端的前提下，我们还能使用
OAuth 吗（见图 6.2）？

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260104141301219.png){ loading=lazy }
  <figcaption>图 6.2：客户端凭证授权类型</figcaption>
</figure>

我们可以利用 OAuth 2.0 为这种场景新增的 client
credentials（客户端凭据）授权类型来实现。在隐式流程中，客户端被推到浏览器端，因此进入前端通道；而在这种流程里，资源所有者被“下沉”到客户端，用户代理则从整个过程里消失。结果就是，该流程完全走后端通道，客户端以自己的名义（把自己当作资源所有者）从令牌端点获取访问令牌。

!!! note "OAuth 的“腿”"

	在 OAuth 1.0 中，并没有让客户端获取“自己的令牌”的机制，因为该协议的设计核心是让用户进行授权委托——这是一个由客户端、服务器和用户共同参与的“三腿”协议。不过，在 OAuth 1.0 的落地过程中，人们很快发现，用 OAuth 的一些机制来连接后端服务、替代 API Key 很有价值。这种用法被称为“双腿 OAuth”，因为它不再涉及资源所有者，只剩客户端和资源服务器。但人们并没有使用 OAuth 的令牌，而是只使用 OAuth 1.0 的签名机制，让客户端向资源服务器发起带签名的请求。这样一来，资源服务器必须掌握客户端的 secret 才能校验请求签名。由于整个过程中不存在令牌或凭据的交换，更准确地说它其实是“零腿 OAuth”。

	在设计 OAuth 2.0 时，工作组参考了 OAuth 1.0 的部署模式，决定将“客户端以自己的名义访问受保护资源”这一模式正式规范化，而且这次尽可能复用“三腿”委托流程所使用的令牌机制。这种对齐确保授权服务器仍然负责管理客户端凭据，使资源服务器只需要处理令牌即可。无论令牌是由终端用户委托而来，还是直接颁发给客户端，资源服务器都能以同样的方式处理，从而简化整个 OAuth 系统的代码实现与架构设计。

客户端像使用授权码模式那样向令牌端点请求令牌，不同之处在于：这一次 grant_type 参数使用
client_credentials，并且不会带授权码或其他临时凭据去“兑换”令牌。取而代之的是，客户端直接进行自身认证，授权服务器随后签发相应的访问令牌。客户端还可以在这次调用中通过
scope 参数申请特定的权限范围，这与授权码模式和隐式模式在授权端点使用的 scope 参数类似。

``` shell
POST /token
Host: localhost:9001
Accept: application/json
Content-type: application/x-www-form-encoded
Authorization: Basic b2F1dGgtY2xpZW50LTE6b2F1dGgtY2xpZW50LXNlY3JldC0x

grant_type=client_credentials&scope=foo%20bar
```

授权服务器返回的是标准的 OAuth 令牌端点响应：一个包含令牌信息的 JSON
对象。客户端凭据模式不会签发刷新令牌，因为默认客户端随时都可以为自身申请新的令牌，而无需引入独立的资源所有者，因此在这种场景下刷新令牌并无必要。

``` shell
HTTP 200 OK
Date: Fri, 31 Jul 2015 21:19:03 GMT
Content-type: application/json

{
  "access_token": "987tghjkiu6trfghjuytrghj",
  "scope": "foo bar",
  "token_type": "Bearer"
}
```

客户端使用该访问令牌的方式，与通过其他流程获取的访问令牌完全一致；受保护资源甚至不一定需要知道令牌是如何获取的。令牌本身很可能会关联不同的访问权限，取决于它们是由用户委托签发，还是由客户端直接申请。不过，这类差异可以交由授权策略引擎来处理，它能够区分这两种情况。换句话说，尽管这些令牌从资源服务器的入口看起来一模一样，但它们所代表的含义仍然可能不同。

我们把这项功能集成到服务器和客户端中。打开 ch-6-ex-2，编辑 authorizationServer.js
文件。接下来进入令牌端点的处理器，找到那段用于处理“授权码”授权类型的令牌请求的代码。

``` javascript
if (req.body.grant_type == 'authorization_code') {
```

我们将在这个 if 语句中增加一个分支，用于处理客户端凭证（client credentials）授权类型。

``` javascript
} else if (req.body.grant_type == 'client_credentials') {
```

此时，我们的代码已经验证了提交到令牌端点的客户端 ID 和密钥。接下来需要确定：当前传入的请求是否可以为这个特定客户端签发令牌。这里可以做多种校验，例如检查请求的
scope 是否在客户端被允许的范围内、检查客户端是否被允许使用该授权类型，甚至检查该客户端是否已经有一个正在流转中的访问令牌，以便我们决定是否需要提前吊销。在这个简单的练习中，我们只校验
scope，并复用授权码（authorization code）授权类型中的 scope 匹配代码来完成这一步。

``` javascript
var rscope = req.body.scope ? req.body.scope.split(' ') : undefined;
var cscope = client.scope ? client.scope.split(' ') : undefined;
if (__.difference(rscope, cscope).length > 0) {
  res.status(400).json({error: 'invalid_scope'});
  return;
}

```

!!! note "作用域与授权类型  "

	由于客户端凭证（client credentials）授权类型不涉及任何直接的用户交互，它主要面向受信任的后端系统，用于直接访问服务。鉴于这种模式具备较高权限，受保护资源在处理请求时通常需要能够区分交互式与非交互式客户端。一个常见做法是为这两类客户端分别使用不同的 scope，并在客户端向授权服务器注册时将其作为注册信息的一部分进行管理。

明确了这一点之后，我们就可以签发 access token 了。我们会像之前一样，将它保存到数据库中。

``` javascript
var access_token = randomstring.generate();
var token_response = { access_token: access_token, token_type: 'Bearer', scope: rscope.join(' ') };
nosql.insert({ access_token: access_token, client_id: clientId, scope:
rscope });
res.status(200).json(token_response);
return;
```

现在我们把注意力转到客户端。在同一个练习中编辑 client.js，找到负责处理客户端授权的函数。

``` javascript
app.get('/authorize', function(req, res){
```

这次我们不再重定向资源所有者，而是直接调用令牌端点。我们将以授权码模式中处理回调 URI 的那段代码为基础来实现：一次简单的
HTTP POST 请求，并通过 HTTP Basic 认证携带客户端凭证。

``` javascript
var form_data = qs.stringify({
  grant_type: 'client_credentials',
  scope: client.scope
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

随后，我们像之前一样解析令牌响应，只是这次无需再考虑刷新令牌。为什么？因为客户端可以随时代表自己轻松请求新的令牌，无需用户介入，因此在这种情况下根本没有必要提供刷新令牌。

``` javascript
if (tokRes.statusCode >= 200 && tokRes.statusCode < 300) {
  var body = JSON.parse(tokRes.getBody());
  access_token = body.access_token;

  scope = body.scope;

  res.render('index', {access_token: access_token, scope: scope});
} else {
  res.render('error', {error: 'Unable to fetch access token, server response: ' + tokRes.statusCode})
}
```

从这里开始，客户端就可以像之前一样调用资源服务器。受保护资源无需改动任何处理代码，因为它接收并校验的是访问令牌。

### 资源所有者凭据授权类型

如果资源所有者在授权服务器上只有普通的用户名和密码，那么客户端就可能让用户输入这些凭据，并用它们换取访问令牌。资源所有者凭据授权类型（也称为密码模式）正是用来实现这一点的：它允许客户端这样做。资源所有者只与客户端直接交互，而不会与授权服务器本身交互。该授权类型只使用令牌端点，整个流程始终限定在后端通道中（见图
6.3）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260104143610779.png){ loading=lazy }
  <figcaption>图 6.3 资源所有者凭据授权类型</figcaption>
</figure>

到这里，这个方法你应该会觉得似曾相识。“等等，”你可能在想，“我们在第 1 章就讲过这个，而且你还说这是个馊主意！”没错：这种授权类型虽然被纳入了
OAuth 核心规范，但本质上建立在“直接要钥匙”的反模式之上。总体来说，这确实不是个好主意。

!!! note "把反模式写进规范"

	我们回顾一下：为什么不该用这种模式？它的确比处理一堆来回重定向更好写。但这种“简单”，是以显著更高的安全风险、以及更差的灵活性和功能性为代价的。资源所有者的凭据会以明文形式暴露给客户端，客户端可能缓存它们，或在需要时随意重放。凭据也会以明文形式（尽管走的是 TLS 加密连接）提交给授权服务器，授权服务器还必须去校验它们，这又引入了一个潜在的攻击面。与 OAuth Token 不同——Token 可以在不影响用户体验的情况下撤销和轮换——用户名和密码往往更难管理和变更。要求收集并重放用户凭据，也限制了可用于用户认证的凭据类型。通过浏览器访问的授权服务器可以采用各种主认证技术和交互体验，比如证书或身份联合；但其中很多最有效、最安全的方案，恰恰就是为防止这种授权类型所依赖的“凭据重放”而设计的。结果就是，认证几乎被限定为朴素的用户名/密码或其等价形式。最后，这种做法会“训练”用户把密码交给任何开口索要的应用。正确的方式应当是让用户只把密码交给少数可信的核心应用，比如授权服务器。

	那为什么 OAuth 还要把这种糟糕做法写进规范？在有其他选择时，这种授权类型确实相当不推荐，但并非总有可行的替代方案。它面向的客户端场景是：客户端本来就会提示资源所有者输入用户名和密码，然后把这些凭据转发给每一个受保护资源。为了不反复打扰用户，这类客户端很可能会把用户名和密码存起来，以便将来重放。与此同时，受保护资源不得不在每次请求时看到并校验用户密码，这会让敏感信息暴露面变得极其巨大。

	因此，这种授权类型可以作为迈向更现代安全架构的“过渡方案”，帮助系统逐步切换到 OAuth 其他更安全的授权方式。首先，受保护资源不再需要知道、也不必再看到用户密码，只需处理 OAuth Token 即可，这立刻减少了用户凭据在网络中的暴露范围，也减少了接触到它们的组件数量。其次，使用这种授权类型后，一个出于善意的客户端应用不再需要存储密码、也不再需要把密码发给资源服务器；客户端用这些凭据换取访问令牌，并用该令牌访问各个受保护资源。再结合刷新令牌，用户体验与之前基本一致，但相较于旧方案，整体安全性大幅提升。尽管使用授权码模式之类的方案要好得多，但在某些情况下，这条流程仍然比在每次请求时把用户密码重放给受保护资源要更可取。

这种授权类型的工作方式很简单：客户端会通过自身可用的任意交互界面收集资源所有者的用户名和密码，然后将这些凭据转发给授权服务器。

``` shell
POST /token
Host: localhost:9001
Accept: application/json
Content-type: application/x-www-form-encoded
Authorization: Basic b2F1dGgtY2xpZW50LTE6b2F1dGgtY2xpZW50LXNlY3JldC0x

grant_type=password&scope=foo%20bar&username=alice&password=secret
```

授权服务器会从传入请求中读取用户名和密码，并与本地用户存储进行比对。若匹配成功，授权服务器就会为该资源所有者签发一个令牌。

如果你觉得这看起来很像中间人攻击，那确实八九不离十。你知道自己不该这么做，也明白原因；但我们还是会一步步把它搭出来，这样你以后就更清楚哪些东西千万别做——如果能避免的话。希望你也能通过观察这些数据是如何被拼装起来的，看到使用这种授权方式所固有的一些问题。打开
ch-6-ex-3，编辑 authorizationServer.js 文件开始吧。由于这是一个后端通道（back-channel）流程，我们将再次处理令牌端点（token
endpoint）。找到那段用于处理授权码（authorization code）授权类型的代码。

``` javascript
if (req.body.grant_type == 'authorization_code') {
```

我们要在这个 if 语句中再加一个分支，用来检查 grant_type 参数里的 password 值。

``` javascript
} else if (req.body.grant_type == 'password') {
```

请记住，代码执行到这里时，我们已经确认客户端是合法的，并且已完成认证。接下来需要确定资源所有者是谁。在示例代码中，我们把用户信息存放在一个名为
userInfo 的内存数据对象里。在生产系统中，包含密码在内的用户信息通常会存储在某种数据库或目录服务中。我们提供了一个简单的查询函数，可以根据用户名获取对应的用户信息对象。

``` javascript
var getUser = function(username) {
  return userInfo[username];
};
```

这个函数的具体实现细节对构建 OAuth 功能并不重要，因为在生产环境中通常会使用数据库或其他用户存储。我们会用这个函数来根据传入的用户名进行查询并确认用户是否存在；如果不存在，就返回错误。

``` javascript
var username = req.body.username;
var user = getUser(username);
if (!user) {
  res.status(401).json({error: 'invalid_grant'});
  return;
}
```

接下来，我们需要检查密码是否与用户对象中存储的密码一致。由于我们把简单的用户信息保存在内存里，而且密码是明文存储的，因此这里只需要直接对输入密码做字符串比较即可。在任何靠谱的生产系统中，密码都应该进行哈希处理，最好再加盐。如果密码不匹配，我们就返回一个错误。

``` javascript
var password = req.body.password;
if (user.password != password) {
  res.status(401).json({error: 'invalid_grant'});
  return;
}
```

客户端也可以传入一个 scope 参数，这样我们就能像在前面的练习中那样，进行同类型的 scope 校验。

``` javascript
var rscope = req.body.scope ? req.body.scope.split(' ') : undefined;
var cscope = client.scope ? client.scope.split(' ') : undefined;
if (__.difference(rscope, cscope).length > 0) {
  res.status(401).json({error: 'invalid_scope'});
  return;
}
```

在完成所有校验后，我们就可以生成并返回访问令牌。注意，我们还可以（并且确实会）同时生成刷新令牌。把刷新令牌交给客户端后，客户端就不再需要保存资源所有者的密码了。

``` javascript
var access_token = randomstring.generate();
var refresh_token = randomstring.generate();

nosql.insert({ access_token: access_token, client_id: clientId, scope:
rscope });
nosql.insert({ refresh_token: refresh_token, client_id: clientId, scope: rscope });

var token_response = { access_token: access_token, token_type: 'Bearer',  refresh_token: refresh_token, scope: rscope.join(' ') };

res.status(200).json(token_response);
```

这会生成我们在令牌端点中一贯预期的标准 JSON 对象。该令牌在功能上与通过任何其他 OAuth 授权类型获取的令牌完全一致。

在客户端，我们需要先让用户输入用户名和密码。我们已经搭建了一个表单，用于提示用户填写用户名和密码以获取令牌（见图 6.4）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260104144121100.png){ loading=lazy }
  <figcaption>图 6.4：客户端提示用户输入用户名和密码</figcaption>
</figure>

在这个练习中，使用授权服务器的 userInfo 集合中第一个用户对象提供的用户名 alice 和密码 password。用户在该表单中输入信息并点击按钮后，客户端会通过
HTTP POST 将其凭据发送到 /username_password。接下来，我们将为这个请求配置一个监听器。

``` javascript
app.post('/username_password', function(req, res) {

});
```

我们会从传入请求中取出用户名和密码，原封不动地转发给授权服务器——就像一次“合格”的中间人攻击。不同于真正的中间人攻击，我们会做正确的事：把刚拿到的用户名和密码立刻忘掉，因为接下来我们要拿到的是访问令牌。

``` javascript
var username = req.body.username;
var password = req.body.password;

var form_data = qs.stringify({
  grant_type: 'password',
  username: username,
  password: password,
  scope: client.scope
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

授权服务器的令牌端点返回结果和我们熟悉的一样，因此我们会解析出访问令牌，然后继续推进应用的后续流程，仿佛自己刚才没有犯下什么严重的安全失误。

``` javascript
if (tokRes.statusCode >= 200 && tokRes.statusCode < 300) {
  var body = JSON.parse(tokRes.getBody());

  access_token = body.access_token;

  scope = body.scope;

  res.render('index', {access_token: access_token, refresh_token: refresh_token, scope: scope});

} else {
  res.render('error', {error: 'Unable to fetch access token, server response: ' + tokRes.statusCode})
}
```

客户端应用的其余部分完全不需要改动。这里拿到的访问令牌，仍然会以一模一样的方式提交给受保护资源；这样一来，受保护资源就不必知道我们刚刚“原汁原味”地看到了用户的明文密码。要记住，在过去处理这类问题的老办法里，客户端会在每一次请求时把用户的密码直接重放给受保护资源。现在用了这种授权类型，即便客户端在实现上并没有做到尽善尽美，受保护资源本身也不需要以任何形式知道或接触用户的凭据。

既然你已经知道怎么用这种授权类型了——如果能避免，请千万别在真实环境里这么做。它只应该用于把那些原本不得不直接处理用户名和密码的客户端“过渡”到
OAuth 体系中；而这类客户端在绝大多数情况下都应尽快改用授权码流程。因此，除非别无选择，否则不要使用这种授权类型。互联网会感谢你。

### 断言授权类型

在 OAuth 工作组发布的首批官方扩展授权类型[^3]
中，断言授权类型会向客户端提供一种结构化、并经过密码学保护的内容，称为断言（assertion）。客户端把这个断言交给授权服务器，以换取令牌。你可以把断言理解为一种经过认证的文件，比如毕业证或执照。只要你信任认证机构有能力并且愿意如实作出这些陈述，你就可以相信该文件内容的真实性（见图
6.5）。

[^3]: [RFC 7521:用于 OAuth 2.0 客户端认证与授权授予的断言框架](https://datatracker.ietf.org/doc/html/rfc7521)

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260104144448806.png){ loading=lazy }
  <figcaption>图 6.5：断言授权类型家族</figcaption>
</figure>

迄今为止已经标准化了两种格式：一种使用安全断言标记语言（SAML）[^4]，另一种使用 JSON Web Token（JWT）[^5]（我们将在第 11
章介绍）。这种授权类型完全通过后端通道（back
channel）进行，与客户端凭证流程类似，可能并不存在明确的资源所有者参与。不同于客户端凭证流程，最终令牌所关联的权限由所提交的断言决定，而不是仅由客户端本身决定。由于该断言通常来自客户端之外的第三方，客户端可以对断言本身的具体内容保持不了解。

[^4]: [RFC 7522](https://datatracker.ietf.org/doc/html/rfc7522)

[^5]: [RFC 7523](https://datatracker.ietf.org/doc/html/rfc7523)

与其他后端通道流程一样，客户端会向授权服务器的令牌端点发起一次 HTTP POST
请求。客户端按常规方式进行自身认证，并将断言作为一个参数提交。客户端获取该断言的方式千差万别，许多相关协议也将其视为非讨论范围。断言可能由用户提供，也可能来自配置系统，或通过其他非
OAuth 协议获取。归根结底，就像访问令牌一样，客户端如何获得断言并不重要，关键在于它能够将断言提交给授权服务器。在这个示例中，客户端提交的是一个
JWT 断言，这一点也体现在 grant_type 参数的取值上。

``` shell
POST /token HTTP/1.1
Host: as.example.com
Content-Type: application/x-www-form-urlencoded
Authorization: Basic b2F1dGgtY2xpZW50LTE6b2F1dGgtY2xpZW50LXNlY3JldC0x

grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Ajwt-bearer
&assertion=eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6InJzYS0xIn0.eyJpc3MiOi
JodHRwOi8vdHJ1c3QuZXhhbXBsZS5uZXQvIiwic3ViIjoib2F1dGgtY2xpZW50LTEiLCJzY29wZSI
6ImZvbyBiYXIgYmF6IiwiYXVkIjoiaHR0cDovL2F1dGhzZXJ2ZXIuZXhhbXBsZS5uZXQvdG9rZW4i
LCJpYXQiOjE0NjU1ODI5NTYsImV4cCI6MTQ2NTczMzI1NiwianRpIjoiWDQ1cDM1SWZPckRZTmxXO
G9BQ29Xb1djMDQ3V2J3djIifQ.HGCeZh79Va-7meazxJEtm07ZyptdLDu_Ocfw82F1zAT2p6Np6Ia_
vEZTKzGhI3HdqXsUG3uDILBv337VNweWYE7F9ThNgDVD90UYGzZN5VlLf9bzjnB2CDjUWXBhgepSy
aSfKHQhfyjoLnb2uHg2BUb5YDNYk5oqaBT_tyN7k_PSopt1XZyYIAf6-5VTweEcUjdpwrUUXGZ0fl
a8s6RIFNosqt5e6j0CsZ7Eb_zYEhfWXPo0NbRXUIG3KN6DCA-ES6D1TW0Dm2UuJLb-LfzCWsA1W_
sZZz6jxbclnP6c6Pf8upBQIC9EvXqCseoPAykyR48KeW8tcd5ki3_tPtI7vA
```

此示例断言的主体可转换为以下内容：

``` json
{
  "iss": "http://trust.example.net/",
  "sub": "oauth-client-1",
  "scope": "foo bar baz",
  "aud": "http://authserver.example.net/token",
  "iat": 1465582956,
  "exp": 1465733256,
  "jti": "X45p35IfOrDYNlW8oACoWoWc047Wbwv2"
}
```

授权服务器会解析断言，校验其加密保护，并处理其中的内容，以决定要生成哪种类型的令牌。这个断言可以表达多种不同的含义，例如资源所有者的身份，或一组被允许的
Scope。授权服务器通常会有一套策略，用来确定它接受哪些主体签发的断言，以及这些断言应当如何解释。最终，它会像令牌端点的其他响应一样生成一个访问令牌。客户端随后即可携带该令牌，以常规方式访问受保护资源。

这种授权类型的实现方式与其他仅后端通道的流程类似：客户端向令牌端点提交信息，授权服务器直接签发令牌。在现实场景中，断言通常只会出现在一些有限的、往往是企业级的场景里。如何以安全的方式生成和处理断言属于高级主题，足以单独写一套书；至于断言流程的具体实现，就留给读者作为练习。

### 选择合适的授权类型

在有这么多授权类型可选的情况下，要决定哪一种最适合当前任务，可能会让人望而生畏。好在，有一些通用的基本原则可以遵循，帮助你做出正确选择（见图
6.6）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260104144814096.png){ loading=lazy }
  <figcaption>图 6.6 选择合适的授权类型</figcaption>
</figure>

你的客户端是在代表某个特定的资源所有者行事吗？你能否把该用户引导到其浏览器中的某个网页？如果可以，你就应该使用基于重定向的流程之一：授权码（Authorization
Code）或隐式（Implicit）。选哪一个？取决于客户端类型。

你的客户端是否完全运行在浏览器里？这里不包括“在服务器上执行、只是在浏览器里访问界面”的应用，而是指从头到尾都生存在浏览器里的那种应用。如果是，那么应当使用隐式流程，因为它就是为这种场景做的优化。如果不是——无论应用是由
Web 服务器提供，还是在用户电脑上以原生方式运行——你都应该使用授权码流程，因为它在安全性上表现最好，也最灵活。

你的客户端是原生应用吗？你本来就应该使用授权码授权类型。不过，正如我们将在第 7、10 和 12
章看到的那样，除了授权码授权类型之外，你还需要使用一些特定的安全扩展，比如动态注册（DynReg）或用于代码交换的证明密钥（PKCE）。在本章后续讨论原生应用时，我们会更深入地展开这些内容。

你的客户端是在代表自己行事吗？这包括访问那些不一定对应单个用户的 API，例如批量数据传输。如果是，那么你应该使用客户端凭证（Client
Credentials）流程。如果你使用的 API 要求你在某个参数里指定“你正在代表哪个用户”，那你应该考虑改用基于重定向的流程，因为这样可以实现按用户粒度的授权同意与审计。

你的客户端是在某个权威第三方的指示下工作吗？该第三方能否直接给你某种证明，让你可以代表它行事？如果可以，那么你很可能应该使用断言（Assertion）流程之一。具体用哪一种，取决于授权服务器以及签发断言的第三方。

你的客户端无法在浏览器里重定向用户吗？该用户是否只有一套简单的凭据，你还能说服他们交给你？而且你别无选择？如果是，那也许你可以使用资源所有者密码凭证（Resource
Owner Password Credentials）流程，并清楚认识到它的局限性。但别说我们没提醒过你。

## 客户端部署形态

OAuth 客户端的形态多种多样，但大体可以归为三类：Web 应用、浏览器内应用和原生应用。它们各有优劣，我们将依次展开介绍。

### Web 应用

OAuth 客户端最初的典型场景就是 Web 应用。这类应用运行在远程服务器上，通过 Web 浏览器访问。应用的配置和运行时状态保存在 Web
服务器端，浏览器与服务器的连接通常通过会话 Cookie 来维持。

这类应用可以充分利用前通道和后通道两种通信方式。由于用户本来就通过浏览器进行交互，在前通道发起请求只需要向浏览器发送一个
HTTP 重定向即可；在前通道接收响应也同样简单，因为应用本身就一直在监听 HTTP 请求。后通道通信则可以由运行应用的 Web 服务器直接发起
HTTP 调用来完成。凭借这种灵活性，Web 应用最适合使用授权码（authorization code）、客户端凭据（client
credentials）或断言（assertions）等流程。由于请求 URI 的片段（fragment）部分通常不会被浏览器传给服务器，隐式（implicit）流程在大多数情况下并不适用于
Web 应用。

我们在第 2 章和第 3 章已经介绍过多个 Web 应用的示例和变体，因此这里就不再进一步展开。

### 浏览器应用

浏览器应用是指完全在 Web 浏览器内运行的应用，通常使用 JavaScript。虽然应用代码需要由 Web 服务器提供，但代码本身并不在服务器上执行，Web
服务器也不会保存应用的任何运行时状态。相反，应用的一切都发生在终端用户的电脑上，并在其浏览器中完成。

这类客户端可以很方便地使用前端通道（front channel），因为通过 HTTP 重定向把用户带到另一个页面非常容易。前端通道返回的响应也同样简单，因为客户端软件确实需要从
Web 服务器加载。不过，后端通道（back-channel）通信就更复杂了：浏览器应用受同源策略以及其他安全限制的约束，这些限制旨在防止跨域攻击。因此，这类应用最适合使用隐式流程（implicit
flow），该流程正是针对这种场景优化的。

下面我们动手看一个浏览器应用。打开 ch-6-ex-4，并编辑 files/client/index.html。与本书中的其他示例不同，这次我们不改 Node.js
代码，而是查看运行在浏览器里的代码。为了让它正常工作，我们仍然需要客户端配置和授权服务器配置；它们已经像我们的 Web
应用示例一样，作为对象放在 main 函数的顶部。

``` javascript
var client = {
  'client_id': 'oauth-client-1',
  'redirect_uris': ['http://localhost:9000/callback'],
  'scope': 'foo bar'
};

var authServer = {
  authorizationEndpoint: 'http://localhost:9001/authorize'
};

var protectedResource = 'http://localhost:9002/resource';
```

当用户点击 Authorize 按钮时，我们会生成一个前端通道（front-channel）请求，并发送到授权服务器的授权端点（authorization
endpoint）。首先，我们会生成一个 state 值，并将其存入 HTML5 本地存储（local storage），以便后续取回使用。

``` javascript
var state = generateState(32);
localStorage.setItem('oauth-state', state);
```

接下来，我们将构造指向授权端点的 URI，并通过 HTTP 重定向将资源所有者引导到该地址。

```javascript
location.href = authServer.authorizationEndpoint + '?' +
    'response_type=token' +
    '&state=' + state +
    '&scope=' + encodeURIComponent(client.scope) +
    '&client_id=' + encodeURIComponent(client.client_id) +
    '&redirect_uri=' + encodeURIComponent(client.redirect_uris[0]);
```

该请求与 Web 应用示例中使用的请求完全一致，唯一的区别是将 response_type 设置为
token。该应用通过整页重定向到授权服务器来启动该流程，这意味着整个应用会被重新加载，并且在回调时必须重新启动。另一种做法是使用内联框架（inline
frame），也就是 iframe，将资源所有者引导到服务器。

当资源所有者通过重定向 URI 返回时，我们需要能够监听该回调并处理响应。我们的应用在页面加载时检查 URI 片段（fragment），也就是
hash 的状态来实现这一点。如果该片段存在，我们会解析其组成部分，提取出访问令牌和作用域。

``` javascript
var h = location.hash.substring(1);
var whitelist = ['access_token', 'state']; // for parameters

callbackData = {};

h.split('&').forEach(function (e) {
  var d = e.split('=');

  if (whitelist.indexOf(d[0]) > -1) {
    callbackData[d[0]] = d[1];
  }
});

if (callbackData.state !== localStorage.getItem('oauth-state')) {
  callbackData = null;
  $('.oauth-protected-resource').text("Error state value did not match");
} else {
  $('.oauth-access-token').text(callbackData.access_token);
}
```

从这里开始，我们的应用就可以使用访问令牌去访问受保护资源了。需要注意的是，JavaScript 应用要访问外部站点，受保护资源端仍然必须做好跨域安全配置（例如
CORS），这一点我们会在第 8 章讨论。在这类应用中使用
OAuth，可以实现一种“跨域会话”：由资源所有者参与调度，并以访问令牌作为载体。在这种场景下，访问令牌通常生命周期较短，而且往往会限制作用域。要刷新这个会话，需要把资源所有者再次引导回授权服务器以获取新的访问令牌。

### 原生应用

原生应用是指直接运行在终端用户设备上的应用，无论是电脑还是移动平台。应用软件通常在设备外部完成编译或打包，然后安装到设备上。

这类应用可以很方便地使用后端通道，通过直接向远程服务器发起 HTTP 出站调用即可。由于用户并不在 Web 浏览器里（不像 Web
应用或浏览器客户端那样），前端通道就会更棘手。要发起前端通道请求，原生应用需要能够调起系统浏览器或内嵌的浏览器视图，把用户直接带到授权服务器。要接收前端通道的响应，原生应用还必须能够提供一个
URI，供授权服务器将浏览器重定向回来。通常有以下几种形式：

- 在 localhost 上运行一个内嵌 Web 服务器
- 使用一个远程 Web 服务器，并通过某种带外（out-of-band）的推送通知能力把结果推送回应用
- 使用自定义 URI Scheme，例如 com.oauthinaction.mynativeapp:/，并在操作系统中注册，使得访问该 Scheme 的 URI 时会唤起应用

对移动应用来说，自定义 URI Scheme 最常见。原生应用很容易使用授权码（authorization code）、客户端凭证（client
credentials）或断言（assertion）流程；但由于它们可以把信息隔离在浏览器之外，因此不建议原生应用使用隐式（implicit）流程。

我们来看看如何构建一个原生应用。打开 ch-6-ex-5，你会像往常一样在里面找到授权服务器和受保护资源的代码。不过这次客户端不再是主目录下的
client.js 脚本，而是在 native-client 子目录中。本书到目前为止的所有练习，都是使用运行在 Node.js 上的 Express.js Web 应用框架，以
JavaScript 开发的。原生应用并不需要能从浏览器访问，但我们仍尽量在语言选择上保持一致。为此，我们选择使用 Apache Cordova[^6]
平台，它允许我们用 JavaScript 来构建原生应用。

[^6]: [https://cordova.apache.org/](https://cordova.apache.org/)

!!! note "我需要使用 Web 技术来构建 OAuth 客户端吗？"

	为了在本书的所有练习中保持一致性，我们在原生应用练习里仍然沿用了许多在 Web 应用中使用过的语言和技术。但这并不意味着你必须用 HTML、JavaScript，或任何特定语言/平台来开发自己的原生应用。一个 OAuth 应用需要具备几项能力：能够直接向后通道（back-channel）端点发起 HTTP 调用；能够为前通道（front-channel）端点拉起系统浏览器；还能在浏览器可访问的某种 URI 上监听来自这些前通道端点的响应。不同平台实现细节各不相同，但许多应用框架都提供了这些能力。

和之前一样，我们尽量把重点放在 OAuth 上，并尽可能帮你（读者）屏蔽各个平台特有的“坑”。Apache Cordova 可以作为 Node
包管理器（NPM）中的一个模块使用，因此安装方式与其他 Node.js 模块类似。尽管不同系统之间细节会有所差异，我们将以 Mac OSX
平台为例进行演示。

``` shell
> sudo npm install -g cordova
> npm install ios-sim
```

完成这些之后，我们来看看原生应用的代码。打开 ch-6-ex-5/native-client/，编辑
www/index.html。和浏览器应用的练习一样，这次我们不会修改任何代码，而是查看运行在原生应用内部的代码。

你需要在电脑上运行这个原生应用。为此还得多做几步：在 ch-6-ex-5/native-client/ 目录下，需要添加一个运行时平台。这里我们使用
iOS；Cordova 框架也支持其他不同的平台。

``` shell
> cordova platform add ios
```

接下来，你需要安装几个插件，让原生应用能够调用系统浏览器，并监听自定义 URL Scheme。

``` shell
> cordova plugin add cordova-plugin-inappbrowser
> cordova plugin add cordova-plugin-customurlscheme --variable URL_SCHEME=com.oauthinaction.mynativeapp
```

最后，我们就可以运行我们的原生应用了。

``` shell
> cordova run ios
```

这应该会在手机模拟器中启动该应用程序（见图 6.7）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260104145613932.png){ loading=lazy }
  <figcaption>图 6.7：原生移动端 OAuth 客户端应用程序</figcaption>
</figure>

现在我们来看看代码。首先要注意的是客户端的配置。

``` javascript
var client = {
  "client_id": "native-client-1",
  "client_secret": "oauth-native-secret-1",
  "redirect_uris": ["com.oauthinaction.mynativeapp:/"],
  "scope": "foo bar"
};
```

如你所见，注册信息和普通的 OAuth 客户端是一样的。可能会让你注意到的一点是注册的 redirect_uris。它和传统客户端不同，因为这里用的是自定义
URI scheme——本例中是 com.oauthinaction.mynativeapp:/——而不是更常见的 https://。系统浏览器只要看到以
com.oauthinaction.mynativeapp:/ 开头的 URL，不管是用户点击的链接、从其他页面发起的 HTTP
重定向，还是由其他应用显式唤起，我们的应用都会通过一个特殊的处理器被调用。在这个处理器里，我们可以拿到用于链接或重定向的完整
URL 字符串，就像我们是通过 HTTP 提供该 URL 的 Web 服务器一样。

!!! note "在原生应用中保存密钥"

	在我们的练习里，我们像第 3 章的 Web 应用那样，直接在客户端里配置了一个 client secret。但在生产环境的原生应用中，这种做法并不理想，因为应用的每一份拷贝都能拿到这个密钥，那它当然就称不上“秘密”了。实际使用时还有一些替代方案可选。我们会在 6.2.4 节更详细地讨论这个问题，不过为了让本章示例保持一致，这里暂时仍采用这种方式。

授权服务器和受保护资源的配置与其他示例相同。

``` javascript
var authServer = {
  authorizationEndpoint: 'http://localhost:9001/authorize',
  tokenEndpoint: 'http://localhost:9001/token',
};

var protectedResource = 'http://localhost:9002/resource';
```

由于我们将采用授权码模式，当用户点击 Authorize 按钮时，我们会通过设置请求参数 response_type=code
来生成一个前端通道（front-channel）请求。我们仍然需要生成一个 state 值，并将其存储在应用中（在 Apache Cordova 中使用 HTML5
本地存储），以便后续再取出来使用。

``` javascript
var state = generateState(32);
localStorage.setItem('oauth-state', state);
```

完成这一步后，我们就可以开始构建请求了。这个请求与我们在第 3 章首次接触授权码（Authorization Code）授予类型时使用的授权请求完全一致。

``` javascript
var url = authServer.authorizationEndpoint + '?' +
   'response_type=code' +
   '&state=' + state +
   '&scope=' + encodeURIComponent(client.scope) +
   '&client_id=' + encodeURIComponent(client.client_id) +
   '&redirect_uri=' + encodeURIComponent(client.redirect_uris[0]);
```

要向授权服务器发起请求，我们需要在应用中调起系统浏览器。由于用户此时并不在网页浏览器里，我们无法像 Web 客户端那样直接通过
HTTP 重定向来完成跳转。

``` javascript
cordova.InAppBrowser.open(url, '_system');
```

在资源所有者授权客户端后，授权服务器会在系统浏览器中将其重定向到 redirect URI。我们的应用需要能够监听该回调，并像 HTTP
服务器一样处理返回结果。这一逻辑由 handleOpenURL 函数实现。

``` javascript
function handleOpenURL(url) {
  setTimeout(function() {
      processCallback(url.substr(url.indexOf('?') + 1));
  }, 0);
}
```

该函数会监听 com.oauthinaction.mynativeapp:/ 上的传入回调，并从 URI 中提取请求参数，然后将这些参数传递给 processCallback
函数。在 processCallback 中，我们会解析各个组成部分，以获取 code 和 state 参数。

``` javascript
var whitelist = ['code', 'state']; // for parameters

callbackData = {};

h.split('&').forEach(function (e) {
var d = e.split('=');

if (whitelist.indexOf(d[0]) > -1) {
  callbackData[d[0]] = d[1];
}
```

我们需要再次检查 state 是否一致。如果不一致，就提示错误。

``` javascript
if (callbackData.state !== localStorage.getItem('oauth-state')) {
  callbackData = null;
  $('.oauth-protected-resource').text("Error: state value did not match");
```

如果返回的 state 参数正确，我们就可以用授权码换取访问令牌。这个过程通过后端通道发起一次直接的 HTTP 请求来完成。在 Cordova 框架中，我们使用 jQuery 的 Ajax 方法来发起该请求。

``` javascript
$.ajax({
  url: authServer.tokenEndpoint,
  type: 'POST',
  crossDomain: true,
  dataType: 'json',
  headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
  },
  data: {
      grant_type: 'authorization_code',
      code: callbackData.code,
      client_id: client.client_id,
      client_secret: client.client_secret,
  }
}).done(function(data) {
  $('.oauth-access-token').text(data.access_token);
  callbackData.access_token = data.access_token;
}).fail(function() {
  $('.oauth-protected-resource').text('Error while getting the access token');
});
```

一旦拿到访问令牌（access token），我们就可以使用该令牌去调用受保护的资源 API。这里我们已经把这个调用绑定到了按钮的事件处理函数上。

``` javascript
function handleFetchResourceClick(ev) {
  if (callbackData != null ) {
  $.ajax({
      url: protectedResource,
      type: 'POST',
      crossDomain: true,
      dataType: 'json',
      headers: {
            'Authorization': 'Bearer ' + callbackData.access_token
      }
  }).done(function(data) {
      $('.oauth-protected-resource').text(JSON.stringify(data));
  }).fail(function() {
      $('.oauth-protected-resource').text('Error while fetching the protected resource');
  });
}
```

原生应用现在可以在需要的时间内使用该令牌来访问受保护资源。由于我们采用了授权码模式，在访问令牌过期后，还可以签发刷新令牌用于续期。这种做法既能让原生应用的用户体验更顺畅，又能保留 OAuth 的安全性特征。

### 处理密钥

客户端密钥（client secret）的作用，是让某个客户端软件实例在授权服务器面前证明“自己是谁”，这一点独立于资源所有者授予它的任何授权。客户端密钥不会暴露给资源所有者或浏览器，因此可以用来唯一标识该客户端软件应用。在 OAuth 1.0 中，无论客户端类型是什么，每个客户端都应当拥有自己的客户端密钥（在该规范中称为 consumer key）。但正如本章反复强调的，并不是所有 OAuth 客户端都生而平等。Web 应用可以把客户端密钥配置在远离浏览器和终端用户的位置；而原生应用和浏览器应用则做不到。

问题的根源在于：需要区分配置时密钥与运行时密钥。配置时密钥会随每一份客户端拷贝一同分发；运行时密钥则因实例而异。客户端密钥属于配置时密钥，因为它代表的是客户端软件本身，会被写入客户端软件的配置中。访问令牌、刷新令牌和授权码都属于运行时密钥，因为它们是在客户端部署运行后，由客户端软件保存的。运行时密钥当然也需要安全存储并得到妥善保护，但它们的设计目标是易于吊销和轮换。相比之下，配置时密钥通常不期望频繁变更。

在 OAuth 2.0 中，这一矛盾通过取消“所有客户端都必须有客户端密钥”的要求来解决，转而依据客户端是否具备保护配置时密钥的能力，将客户端划分为两类：公开客户端（public client）与机密客户端（confidential client）。

顾名思义，公开客户端无法持有配置时密钥，因此没有客户端密钥。原因通常在于客户端代码会以某种方式暴露给终端用户：要么下载到浏览器中执行，要么在用户设备上以原生方式运行。因此，大多数浏览器应用以及许多原生应用都属于公开客户端。无论哪种情况，每一份客户端软件拷贝都是相同的，并且可能存在大量实例。任何一个实例的用户都可能提取出该实例的配置信息，包括已配置的客户端 ID 和客户端密钥。虽然所有实例共享同一个客户端 ID，但这并不会造成问题，因为客户端 ID 本就不是秘密值。即便有人试图通过复制客户端 ID 来冒充该客户端，也仍然需要使用其重定向 URI，并受其他限制措施约束。在这种场景下额外增加一个客户端密钥也无济于事，因为它同样可以和客户端 ID 一起被提取并复制。

对于使用授权码流程的应用来说，一种可行的缓解措施是使用第 10 章讨论的 Proof Key for Code Exchange（PKCE）。PKCE 这一协议扩展允许客户端在不使用客户端密钥或同等机制的情况下，更紧密地将其最初的请求与最终收到的授权码绑定起来。

机密客户端能够保存配置阶段的秘密信息。每个客户端软件实例都有各自独立的配置，包括客户端 ID 和密钥，而且这些值对终端用户而言很难被提取。Web 应用是最常见的机密客户端类型，因为它通常是在 Web 服务器上运行的单一实例，能够用同一个 OAuth 客户端服务多个资源所有者。客户端 ID 会通过浏览器暴露出来，因此可以被获取；但客户端密钥只会在后端通道中传递，不会被直接暴露。

解决该问题的另一种思路是使用第 12 章深入讨论的动态客户端注册。通过动态客户端注册，某个客户端软件实例可以在运行时自行完成注册。这实际上把原本需要在配置阶段保存的秘密转换成了运行时秘密，从而为那些原本无法使用相关能力的客户端提供更高的安全性与功能性。

## 小结  

OAuth 2.0 在统一的协议框架内提供了大量选择。

- 经典的授权码授权类型可以针对不同部署方式进行多种优化。  
- 隐式授权可用于仅在浏览器内运行、没有独立客户端的应用。  
- 客户端凭据授权和断言授权可用于没有明确资源所有者的服务端应用。  
- 除非确实别无选择，否则不应使用资源所有者密码凭据授权。  
- Web 应用、浏览器应用和原生应用在使用 OAuth 时各有不同的特点，但都共享相同的核心机制。  
- 机密客户端可以保管客户端密钥，而公共客户端做不到。  

现在我们已经全面了解了 OAuth 生态中“应该如何工作”，接下来将看看哪些地方可能出错。继续阅读，了解如何应对在 OAuth 的实现与部署中发现的漏洞。

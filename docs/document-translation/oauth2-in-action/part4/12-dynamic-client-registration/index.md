# 12.动态客户端注册

本章将介绍

- 为什么要动态注册 OAuth 客户端
- 如何动态注册 OAuth 客户端
- 如何在长期使用中管理客户端注册信息
- 动态 OAuth 客户端的安全注意事项
- 如何使用软件声明（software statements）保护动态注册

在 OAuth 中，客户端通过一个客户端标识符（client identifier）向授权服务器表明身份。一般来说，这个客户端 ID 会唯一对应充当
OAuth 客户端的软件应用。在交互式 OAuth 流程（例如我们在第 3～5 章实现的授权码模式）中，客户端 ID
会在授权请求阶段通过前端传给授权端点。授权服务器可以根据该客户端 ID 决定：允许哪些重定向 URI、允许哪些
scope，以及要向最终用户展示哪些信息。客户端 ID 也会在令牌端点出示；当它与客户端密钥（client secret）组合使用时，就能在 OAuth
的授权委派流程中对客户端进行认证。

需要特别强调的是，这个客户端标识符与资源所有者可能拥有的任何标识符或账号是两回事。在 OAuth 里，这种区分非常重要，因为你还记得，OAuth
并不鼓励冒充资源所有者。事实上，OAuth 协议的核心思想就是承认：有一段软件正在代表资源所有者行事。但问题来了：客户端如何获得这个标识符？服务器又如何知道该用哪些元数据（例如有效的重定向
URI 集合或 scope）与这个标识符关联起来？

## 服务器如何认识客户端

到目前为止，我们所有练习中的客户端 ID 都是由授权服务器与客户端进行静态配置的。换句话说，双方存在一个带外协议（out-of-band
agreement）——具体来说，就是本书的文字内容——提前约定好客户端 ID 及其对应的密钥。客户端 ID 由服务器确定，然后人工复制到客户端软件中。

这种做法的一个主要缺点是：对于某个 API，每一个客户端软件实例都必须与保护该 API
的每一个授权服务器实例建立绑定关系。当客户端与授权服务器之间关系明确且相对稳定时，这样的预期是合理的，例如授权服务器只用于保护某一个专有
API。举例来说，在我们的云打印案例中，用户可以选择从某个特定且广为人知的照片存储服务导入照片；客户端也是专门为与该服务对接而编写的。在这种相当常见的场景下，能访问该
API 的客户端数量有限，静态注册也就足够好用。

但如果客户端是为了访问分布在许多不同服务器上的某个 API 而编写的呢？如果我们的打印服务能够与任何实现了标准化照片存储 API
的照片存储服务通信呢？这些照片存储服务很可能各自拥有独立的授权服务器，而这样的客户端就需要在它所对接的每一个服务上都有一个对应的客户端标识符。我们当然可以尝试规定：无论面对哪个授权服务器，都复用同一个客户端
ID——但问题是，该由哪个授权服务器来分配这个 ID？毕竟，并不是所有授权服务器都会采用相同的 ID 生成规则；我们还必须确保选出来的
ID 在任何授权服务器上都不会与其他软件发生冲突。那当生态里引入一个新的客户端时又怎么办？无论它被分配到什么客户端
ID，都需要连同相关元数据一起同步给所有授权服务器。

又或者，同一款客户端软件有许多实例，而每个实例都需要与同一个授权服务器通信呢？正如第 6
章所提到的，这在原生应用中非常常见，每个客户端实例都需要一个客户端标识符来与授权服务器交互。我们同样可以说：所有实例都使用同一个标识符，在某些情况下确实可行。但客户端密钥（client
secret）该如何处理？我们在第 7 章已经知道，不能把同一个密钥复制到各处，因为那样它就不再“秘密”了。[^1]
一种做法是干脆不使用密钥，让客户端作为公共客户端（public client）存在，这也是 OAuth
标准所接受并明确规定的。然而，公共客户端会暴露在各种攻击面前，包括授权码和令牌被窃取，以及恶意软件冒充真实客户端等。有时候这是可以接受的权衡，但很多时候并不行——我们更希望每个实例都拥有各自独立的客户端密钥。

[^1]: 众所周知，Google 为了绕过 OAuth 1.0 “每个客户端都必须持有 client secret”的要求，直接规定：所有使用 Google OAuth 1.0
服务器的原生应用统一使用 client ID “anonymous”，client secret 也同样用 “anonymous”。这彻底打破了该安全模型的基本假设。更进一步，Google
又新增了一个扩展参数，用来替代缺失的 client ID，从而进一步破坏了协议本身。

在这两种情况下，手动注册都无法扩展。为了更直观地说明这个问题，来看一个极端但真实的例子：电子邮件。一个开发者在发布软件之前，真的需要把每一份邮件客户端分别向每一个潜在的邮件服务提供商注册一遍吗？毕竟，互联网上的每一个域名和主机都可能拥有自己独立的邮件服务器，更不用说内网的邮件服务了。显然，这样做完全不现实——但
OAuth 的手动注册恰恰隐含了这种前提。那如果有另一种方式呢？我们能否在无需人工介入的情况下，让客户端与授权服务器“相互认识”？

## 运行时注册客户端

OAuth 动态客户端注册协议[^2] 为客户端提供了一种向授权服务器自我介绍的方式，其中可以包含客户端的各类信息。随后，授权服务器可以为该客户端软件分配一个唯一的客户端
ID，客户端在后续所有 OAuth 交互中都可以使用该 ID；在合适的情况下，还可以为该 ID 关联一个客户端密钥（client
secret）。该协议既可以由客户端软件直接使用，也可以作为构建与部署系统的一部分，由该系统代表客户端开发者完成注册（见图 12.1）。

[^2]: RFC 7591 [https://tools.ietf.org/html/rfc7591](https://datatracker.ietf.org/doc/html/rfc7591)

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260106110509080.png){ loading=lazy }
  <figcaption>图 12.1 动态注册过程中传递的信息</figcaption>
</figure>

### 协议如何工作

动态客户端注册协议的核心流程很简单：客户端向授权服务器的客户端注册端点发起一次 HTTP 请求，并接收相应的响应。该端点会监听
HTTP POST 请求，请求体为 JSON，包含客户端拟提交的元数据信息。此调用可选择使用 OAuth 令牌进行保护；不过在我们的示例中，注册是开放的，无需任何授权。

``` shell
POST /register HTTP/1.1
Host: localhost:9001
Content-Type: application/json
Accept: application/json

{
  "client_name": "OAuth Client",
  "redirect_uris": ["http://localhost:9000/callback"],
  "client_uri": "http://localhost:9000/",
  "grant_types": ["authorization_code"],
  "scope": "foo bar baz"
}
```

这些元数据可以包含显示名称、重定向 URI、Scope，以及客户端功能的诸多其他方面。（完整的官方列表在第 12.3.1
节，如果你想提前阅读的话。）不过，请求的元数据绝不能包含客户端 ID 或客户端密钥。相反，这些值始终由授权服务器控制，以防止冒充其他客户端
ID、与其他客户端 ID 发生冲突，或选择强度不足的客户端密钥。授权服务器可以针对所提交的数据执行一些基本的一致性检查，例如，确保所请求的
grant_types 与 response_types 能够配合使用，或确认所请求的 Scope 对动态注册的客户端是有效的。和 OAuth
的一般原则一致，授权服务器负责决定什么是有效的；而客户端作为更简单的一端，则需要遵从授权服务器的规定。

在注册请求成功后，授权服务器会生成新的客户端
ID，并通常还会生成客户端密钥。它们会与该客户端关联的元数据副本一起返回给客户端。客户端在请求中提交的任何值都只是给授权服务器的建议输入，但最终哪些值会与客户端注册关联，仍由授权服务器拍板；授权服务器也可以根据需要自由覆盖或拒绝任何输入。最终的注册结果会以
JSON 对象的形式返回给客户端。

``` shell
HTTP/1.1 201 Created
Content-Type: application/json

{
  "client_id": "1234-wejeg-0392",
  "client_secret": "6trfvbnklp0987trew2345tgvcxcvbjkiou87y6t5r",
  "client_id_issued_at": 2893256800,
  "client_secret_expires_at": 0,
  "token_endpoint_auth_method": "client_secret_basic",
  "client_name": "OAuth Client",
  "redirect_uris": ["http://localhost:9000/callback"],
  "client_uri": "http://localhost:9000/",
  "grant_types": ["authorization_code"],
  "response_types": ["code"],
  "scope": "foo bar baz"
}
```

在这个例子中，授权服务器为该客户端分配了客户端
ID：1234-wejeg-0392，以及客户端密钥：6trfvbnklp0987trew2345tgvcxcvbjkiou87y6t5r。客户端现在可以保存这些值，并在后续与授权服务器的所有通信中使用它们。此外，授权服务器还在客户端的注册记录中补充了几项内容。首先，token_endpoint_auth_method
的值表明，客户端在与令牌端点通信时应使用 HTTP Basic 认证。其次，服务器补全了缺失的 response_types 值，使其与客户端请求中的
grant_types 值相对应。最后，服务器告知客户端客户端 ID 的生成时间，并说明客户端密钥不会过期。

### 为什么要使用动态注册？

在 OAuth 中使用动态注册有几个非常有说服力的理由。最初的 OAuth 使用场景主要围绕“单一入口”的 API，例如提供 Web 服务的公司所开放的
API。这类 API 需要专用客户端进行访问，而这些客户端通常只需要与一个 API 提供方通信。在这种情况下，要求客户端开发者投入精力把客户端注册到该
API 上，并不算过分，因为提供方只有一家。

但你已经看到过两种重要的例外情况，在这些场景里上述假设并不成立。如果某个 API 不止一个提供方，或者同一个 API
可以随时搭建出新的实例，会怎样？例如，OpenID Connect 提供了标准化的身份
API，而跨域身份管理系统（SCIM）协议提供了标准化的账户/资源开通（provisioning）API。两者都由 OAuth
保护，也都可以由不同的提供方部署。尽管某个客户端软件无论这些标准 API 运行在哪个域名下都能与之交互，但我们知道，在这样的环境中管理客户端
ID 根本不可行。简单来说，为这套协议生态写一个新客户端，或部署一个新服务器，都会变成一场后勤层面的噩梦。

即便我们只需要面对一个授权服务器，那如果同一个客户端存在多个实例呢？这在移动平台的原生客户端中尤其棘手，因为每一份客户端软件拷贝都会使用同一个客户端
ID 和客户端密钥。借助动态注册，每个客户端实例都可以自行向授权服务器注册。随后，每个实例都会获得自己的客户端
ID，更重要的是获得自己的客户端密钥，用来保护其用户。

还记得我们提到过，电子邮件客户端与服务器之间的交互模式，是推动动态注册的重要用例之一。如今，OAuth
可以通过简单认证与安全层——通用安全服务应用程序接口（SASL-GSSAPI）扩展，用于访问 Internet Message Access
Protocol（IMAP）邮件服务。如果没有动态注册，每一个邮件客户端都必须预先向每一家允许 OAuth
访问的邮件服务提供商完成注册。而且这项注册必须由开发者在软件发布之前完成，因为终端用户在安装后无法再修改和配置邮件客户端。对于必须了解每一个邮件客户端的授权服务器，以及必须了解每一台服务器的邮件客户端来说，可能的组合数量都大得惊人。更好的做法是使用动态客户端注册：每个邮件客户端实例都可以向它需要通信的每个授权服务器实例自行完成注册。

!!! note "白名单、黑名单与灰名单  "

	允许在授权服务器上进行动态注册看起来可能让人望而生畏。毕竟，你真的希望任何一款软件都能大摇大摆地跑来申请令牌吗？但事实是，很多时候你恰恰需要这样做。从本质上讲，互操作性与“未经请求的访问”并没有可区分的边界。

	需要强调的是，客户端在授权服务器上完成注册，并不意味着它就有权访问该授权服务器所保护的任何资源。相反，资源所有者仍然需要将某种形式的访问权限委托给该客户端。正是这一关键事实，将 OAuth 与其他安全协议区分开来：在那些协议中，注册事件本身往往就意味着获得了访问资源的权限，因此必须通过严格的入驻流程来保护。

	对于已通过授权服务器管理员审核、并由这些可信机构进行静态注册的客户端，授权服务器可能希望跳过向资源所有者请求同意的步骤。通过将特定的可信客户端加入白名单，授权服务器可以为这些客户端优化用户体验。OAuth 协议的流程仍与之前一致：资源所有者被重定向到授权端点，在那里完成身份认证，授权服务器通过前端通道读取访问请求。但对于可信客户端，授权服务器不再提示用户做出选择，而是由策略判定该客户端已获授权，并立即返回授权请求的结果。

	在另一个极端，授权服务器也可以决定永远不允许带有某些属性的客户端注册或发起授权请求。这些属性可能是已知承载恶意软件的一组重定向 URI，或是已知会刻意误导终端用户的显示名称，或其他可检测到的恶意特征。将这些属性值加入黑名单后，授权服务器就能阻止客户端使用它们。

	除此之外的情况都可以归入灰名单，由资源所有者做最终的授权决策。动态注册的客户端只要不在黑名单内且尚未被加入白名单，就应自动落入灰名单。这些客户端可能会比静态注册客户端受到更多限制，例如无法申请某些 scope 或使用某些授权类型，但除此之外，它们仍然作为普通的 OAuth 客户端运行。这样既能在不削弱安全性的前提下，提升授权服务器的可扩展性与灵活性。一个动态注册客户端若在足够长的时间里被大量用户成功使用，最终可能被加入白名单；而恶意客户端则可能被撤销注册，其关键属性也可能被加入黑名单。

### 实现注册端点

现在你已经了解了该协议的工作方式，我们就来把它实现出来。我们先从服务端的注册端点入手。打开 ch-12-ex-1，并编辑
authorizationServer.js 来完成本节练习。我们的授权服务器将继续沿用第 5
章中用于客户端功能的同一个内存数组，这意味着每次服务器重启后，这份存储都会被重置。相比之下，在生产环境中通常会使用数据库或其他更可靠的存储机制。

首先，我们需要创建注册端点。在我们的服务器上，它会监听 /register URL 上的 HTTP POST
请求，因此我们要为它配置一个处理器。在本服务器中，我们只实现公开注册（public registration），也就是说，注册端点不会要求提供可选的
OAuth 访问令牌（access token）。同时，我们还会设置一个变量，用于在处理过程中收集传入的客户端元数据请求。

``` javascript
app.post('/register', function (req, res){
  var reg = {};
});
```

我们应用中的 Express.js 代码框架已经配置为自动将传入消息解析为 JSON 对象，并通过 `req.body`
变量提供给代码使用。接下来我们会对传入数据做几项基础的一致性校验。首先，检查客户端请求使用哪种认证方式；如果未指定，则默认通过
HTTP Basic 使用客户端密钥。否则，就采用客户端指定的输入值。随后我们会验证该值是否有效；若无效，则返回
`invalid_client_metadata` 错误。需要注意的是，该字段的取值（如 `secret_basic`）由规范定义，也可以通过新增定义进行扩展。

``` javascript
if (!req.body.token_endpoint_auth_method) {
  reg.token_endpoint_auth_method = 'secret_basic';
} else {
  reg.token_endpoint_auth_method = req.body.token_endpoint_auth_method;
}

if (!__.contains(['secret_basic', 'secret_post', 'none'],
reg.token_endpoint_auth_method)) {
  res.status(400).json({error: 'invalid_client_metadata'});
  return;
}
```

接下来，我们会读取 grant_type 和 response_type 的值，并确保它们彼此一致。如果客户端两者都未指定，我们会默认使用授权码（authorization
code）授权类型。如果他们只请求了 grant_type 却没有提供对应的
response_type（或反过来），我们会替他们补齐缺失的值。规范不仅定义了这些值应取的范围，也规定了两者之间的对应关系。我们的简易服务器只支持授权码和刷新令牌（refresh
token）两种授权类型，因此如果客户端请求其他类型，我们将返回 invalid_client_metadata 错误。

``` javascript
if (!req.body.grant_types) {
  if (!req.body.response_types) {
      reg.grant_types = ['authorization_code'];
      reg.response_types = ['code'];
  } else {
      reg.response_types = req.body.response_types;
      if (__.contains(req.body.response_types, 'code')) {
            reg.grant_types = ['authorization_code'];
      } else {
            reg.grant_types = [];
      }
  }
} else {
  if (!req.body.response_types) {
      reg.grant_types = req.body.grant_types;
      if (__.contains(req.body.grant_types, 'authorization_code')) {
            reg.response_types =['code'];
      } else {
            reg.response_types = [];
      }
  } else {
      reg.grant_types = req.body.grant_types;
      reg.reponse_types = req.body.response_types;
      if (__.contains(req.body.grant_types, 'authorization_code') && !__.contains(req.body.response_types, 'code')) {
            reg.response_types.push('code');
      }
      if (!__.contains(req.body.grant_types, 'authorization_code') && __.contains(req.body.response_types, 'code')) {
            reg.grant_types.push('authorization_code');
      }
  }
}

if (!__.isEmpty(__.without(reg.grant_types, 'authorization_code',
refresh_token')) ||
      !__.isEmpty(__.without(reg.response_types, 'code'))) {
  res.status(400).json({error: 'invalid_client_metadata'});
  return;
}
```

接下来，我们要确保客户端至少注册了一个重定向 URI。我们对所有客户端都强制执行这一点，因为当前版本的服务器只支持授权码（authorization
code）授权类型，而它是基于重定向流程的。如果你还要支持其他不使用重定向的授权类型，那么就应该根据授权类型来决定是否进行这项检查。如果你需要将重定向
URI 与黑名单进行比对，这里也是实现该功能的合适位置；不过，这类过滤逻辑的实现就留给读者作为练习了。

``` javascript
if (!req.body.redirect_uris || !__.isArray(req.body.redirect_uris) || __.isEmpty(req.body.redirect_uris)) {
  res.status(400).json({error: 'invalid_redirect_uri'});
  return;
} else {
  reg.redirect_uris = req.body.redirect_uris;
}
```

接下来，我们会把其他我们关心的字段也复制过来，并在此过程中检查它们的数据类型。我们的实现会忽略传入但无法识别的额外字段；不过在生产级实现中，可能会选择保留这些额外字段，以便服务器将来新增功能时能够兼容。

``` javascript
if (typeof(req.body.client_name) == 'string') {
  reg.client_name = req.body.client_name;
}

if (typeof(req.body.client_uri) == 'string') {
  reg.client_uri = req.body.client_uri;
}

if (typeof(req.body.logo_uri) == 'string') {
  reg.logo_uri = req.body.logo_uri;
}

if (typeof(req.body.scope) == 'string') {
  reg.scope = req.body.scope;
}
```

最后，我们会生成一个客户端 ID；如果该客户端使用了合适的令牌端点认证方式，还会生成一个客户端密钥。我们也会记录注册时间戳，并标记该密钥永不过期。随后，将这些信息直接附加到我们一直在构建的客户端注册对象上。

``` javascript
reg.client_id = randomstring.generate();
if (__.contains(['client_secret_basic', 'client_secret_post']),
  reg.token_endpoint_auth_method) {
  reg.client_secret = randomstring.generate();
}

reg.client_id_created_at = Math.floor(Date.now() / 1000);
reg.client_secret_expires_at = 0;
```

现在我们可以把这个客户端对象存到客户端存储里。提醒一下，这里我们用的是一个简单的内存数组，但在生产环境中，这一步通常会用数据库来实现。存储完成后，我们会把这个
JSON 对象返回给客户端。

``` javascript
clients.push(reg);

res.status(201).json(reg);
return;
```

全部拼装完成后，我们的注册端点就如附录 B 的清单 13 所示。

我们授权服务器的注册机制很简单，但完全可以扩展，用于对客户端做更多校验，比如：将所有 URL 与黑名单比对、限制动态注册客户端可用的
scope、确保客户端提供联系人地址，等等。注册端点也可以用 OAuth token 进行保护，从而把注册行为与授权该 token
的资源所有者关联起来。至于这些增强功能，就留给读者作为练习自行完成。

### 让客户端自注册

接下来我们要配置客户端，让它在需要时能够自行完成注册。基于上一节的练习，编辑 client.js。在文件靠前的位置，可以看到我们预留了一个空对象，用来存放客户端信息：

``` javascript
var client = {};
```

我们不再像第 3 章那样手动填写，而是改用动态注册协议。和之前一样，这是一种基于内存的存储方案：每次客户端软件重启都会被重置；在生产环境中，通常会用数据库或其他存储机制来承担这一角色。

首先，我们得先判断是否真的需要注册，因为我们不希望每次要和授权服务器通信时都重新注册一个新客户端。当客户端准备发送初始授权请求时，它会先检查自己是否已经拥有该授权服务器对应的客户端
ID。如果没有，就会调用一个工具函数来完成客户端注册。注册成功则继续执行；如果失败，客户端会渲染错误信息并终止。相关代码已经包含在客户端中。

``` javascript
if (!client.client_id) {
  registerClient();
  if (!client.client_id) {
      res.render('error', {error: 'Unable to register client.'});
      return;
  }
}
```

接下来我们来定义 `registerClient` 这个工具函数。它很简单：向授权服务器发送一个 POST 注册请求，并将返回结果保存到 `client`
对象中。

``` javascript
var registerClient = function() {

};
```

首先，我们需要定义要发送给授权服务器的元数据值。这些值相当于客户端配置的模板，授权服务器会为我们补齐其他必填字段，例如客户端
ID 和客户端密钥。

``` javascript
var template = {
   client_name: 'OAuth in Action Dynamic Test Client',
   client_uri: 'http://localhost:9000/',
   redirect_uris: ['http://localhost:9000/callback'],
   grant_types: ['authorization_code'],
   response_types: ['code'],
   token_endpoint_auth_method: 'secret_basic'
};
```

我们会在一次 HTTP POST 请求中把这个模板对象发送到服务器。

``` javascript
var headers = {
   'Content-Type': 'application/json',
   'Accept': 'application/json'
};

var regRes = request('POST', authServer.registrationEndpoint,
  {
       body: JSON.stringify(template),
       headers: headers
  }
);
```

现在我们来检查结果对象。如果返回的 HTTP 状态码是 201
Created，我们就把返回的对象保存到客户端对象中。如果返回任何类型的错误，我们就不保存客户端对象，并让调用我们的函数感知到客户端处于未注册的错误状态，再按需进行相应处理。

``` javascript
if (regRes.statusCode == 201) {
  var body = JSON.parse(regRes.getBody());
  console.log("Got registered client", body);
  if (body.client_id) {
       client = body;
  }
}
```

从这里开始，应用程序的其余部分会照常接管运行。对授权服务器的调用、令牌的处理，或对受保护资源的访问（见图
12.2），都不需要再做任何改动。你注册的客户端名称现在会显示在授权页面上，同时也会显示动态生成的客户端
ID。要验证这一点，可以修改客户端的模板对象，重启客户端，然后再次运行测试。注意，注册成功并不需要重启授权服务器。由于授权服务器无法识别发起请求的客户端软件，它会很乐意从同一客户端软件重复接受新的注册请求，并且每次都会签发一个新的客户端
ID 和密钥。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260106112545803.png){ loading=lazy }
  <figcaption>图 12.2 授权服务器的批准页面，显示随机生成的客户端 ID 以及请求的客户端显示名称</figcaption>
</figure>

有些客户端需要能够从不止一个授权服务器获取令牌。作为额外练习，请重构客户端对其注册信息的存储方式，使其能够根据客户端正在交互的授权服务器而变化。再进一步的挑战是：使用持久化数据库来实现，而不是采用内存中的存储机制。

## 客户端元数据

与已注册客户端相关联的一组属性，统称为客户端元数据（client metadata）。这些属性既包括影响底层协议功能的内容（例如
redirect_uris、token_endpoint_auth_method），也包括影响用户体验的内容（例如
client_name、logo_uri）。正如你在前面的示例中看到的那样，在动态注册协议中，这些属性以两种不同方式使用：

- 客户端发送给服务器：客户端向授权服务器发送一组期望的属性值。但这些期望值未必与某个授权服务器的配置兼容，例如请求了服务器不支持的
  grant_types，或请求了客户端未获授权的 scope。因此，客户端不能总是指望注册成功后得到的结果与其请求完全一致。
-

服务器返回给客户端：授权服务器会向客户端返回一组已注册的属性值。授权服务器可以按需替换、补充或移除客户端请求的属性。它通常会尽量满足客户端的请求，但最终决定权始终在授权服务器手里。无论如何，授权服务器都必须把实际注册生效的属性返回给客户端。客户端可以自行应对不理想的注册结果，比如尝试用更合适的值去修改注册信息，或直接拒绝与该授权服务器通信。

和 OAuth 的大多数场景一样，客户端处于从属地位：客户端可以提出请求，但最终现实由授权服务器说了算。

### 核心客户端元数据字段名表

核心动态客户端注册协议定义了一组通用的客户端元数据名称，并且该集合可以扩展。例如，OpenID Connect 动态客户端注册规范基于并兼容
OAuth 动态客户端注册，在此基础上又扩展了少量自身的元数据项，这些项是 OpenID Connect 协议特有的，我们将在第 13 章介绍。我们在表
12.1 中也纳入了几个 OpenID Connect 特有但对 OAuth 客户端同样具有普遍适用性的扩展项。

### 面向用户的客户端元数据的国际化

在注册请求与响应中可能包含的各类客户端信息里，有几项是用于在授权页或授权服务器的其他用户界面上展示给资源所有者的。这些信息要么是直接展示给用户的字符串（例如
client_name，即客户端软件的显示名称），要么是供用户点击的 URL（例如
client_uri，即客户端的主页）。但如果某个客户端需要支持不止一种语言或地区设置，那么这些面向用户的可读字段就可能在每种支持的语言下都对应一个版本。这样的客户端是否需要按语言分别注册多次？

答案是否定的。动态客户端注册协议提供了一套机制（借鉴自 OpenID Connect），用于同时表示多种语言的值。对于普通的声明（claim），例如
client_name，该字段及其值会作为常规 JSON 对象的成员进行存储：

``` json
"client_name": "My Client"
```

为了表示不同的语言或文字体系，客户端还会发送一个带语言标签的字段版本：在字段名后用 #（井号/哈希符）追加语言标签。例如，假设该客户端在法语中被称为
“Mon Client”。法语的语言代码是 fr，因此在 JSON 对象中，该字段会表示为 client_name#fr。这两个字段会一起发送。

``` json
"client_name": "My Client",
"client_name#fr": "Mon Client"
```

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260106113307952.png){ loading=lazy }
  <figcaption>表 12.1 动态客户端注册中可用的客户端元数据字段</figcaption>
</figure>

授权服务器在与用户交互时，应尽可能使用最具体的条目。例如，如果授权服务器上的某个用户将其偏好语言注册为法语，授权服务器就应显示法语版本的名称，而不是通用版本。客户端应始终提供字段名的通用版本，因为当没有更具体的版本可用，或不支持国际化语言环境时，授权服务器会显示不带语言环境限定符的通用文本。

该特性的实现与使用留作读者练习，因为要让它真正有用，需要对客户端的数据模型以及 Web 服务器的 locale 设置做一些调整。尽管某些编程语言能够将
JSON 对象自动解析为平台原生对象，从而以原生对象成员的方式访问这些值，但这种国际化方法使用的 #
字符往往不是对象方法名的合法字符。因此，需要采用其他访问方式。比如在 JavaScript 中，上一个对象里的第一个值可以通过
client.client_name 访问，但第二个值则需要通过 client["client_name#fr"] 来访问。

### 软件声明

客户端在动态注册请求中发送的每一个元数据值，都必须被视为完全由客户端自我声明。在这种情况下，没有任何机制能阻止客户端谎报一个具有误导性的客户端名称，或宣称一个位于他人域名下的重定向
URI。正如你在第 7 章和第 9 章看到的，如果授权服务器不够谨慎，这会引发一系列漏洞。

但如果我们能以一种方式向授权服务器提交客户端元数据，使授权服务器能够验证这些信息来自可信方呢？有了这样的机制，授权服务器就能锁定客户端中的某些元数据属性，并更有把握这些元数据是有效的。OAuth
动态注册协议通过“软件声明（software statement）”提供了这样的机制。

简单来说，软件声明是一个已签名的 JWT，其载荷（payload）包含客户端元数据，格式与 12.2
节中注册端点请求里的内容一致。客户端开发者无需将每一个客户端软件实例都手动注册到所有授权服务器；相反，可以先在一个可信第三方处预注册其客户端元数据的某个子集——尤其是那些不太可能随时间变化的部分——并由该可信方签发一份带签名的软件声明。随后，客户端软件在向各个授权服务器注册时，可以提交这份软件声明，并附带注册所需的其他补充元数据。

我们来看一个具体的例子。假设开发者希望预先注册一个客户端，并确保客户端名称、客户端主页、Logo
以及服务条款在该客户端的所有实例以及所有授权服务器之间都保持一致。为此，开发者将这些字段提交给可信机构进行注册，并获得一份以签名
JWT 形式签发的软件声明（software statement）。

``` shell
eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzb2Z0d2FyZV9pZCI6Ijg0MDEyLTM5MTM0LTM5
MTIiLCJzb2Z0d2FyZV92ZXJzaW9uIjoiMS4yLjUtZG9scGhpbiIsImNsaWVudF9uYW1lIjoiU3BlY
2lhbCBPQXV0aCBDbGllbnQiLCJjbGllbnRfdXJpIjoiaHR0cHM6Ly9leGFtcGxlLm9yZy8iLCJsb2
dvX3VyaSI6Imh0dHBzOi8vZXhhbXBsZS5vcmcvbG9nby5wbmciLCJ0b3NfdXJpIjoiaHR0cHM6Ly9
leGFtcGxlLm9yZy90ZXJtcy1vZi1zZXJ2aWNlLyJ9.X4k7X-JLnOM9rZdVugYgHJBBnq3s9RsugxZ
QHMfrjCo
```

这个 JWT 的载荷解码后会得到一个 JSON 对象，很像注册请求中会发送的那个。

``` json
{
  "software_id": "84012-39134-3912",
  "software_version": "1.2.5-dolphin",
  "client_name": "Special OAuth Client",
  "client_uri": "https://example.org/",
  "logo_uri": "https://example.org/logo.png",
  "tos_uri": "https://example.org/terms-of-service/"
}
```

客户端发起的注册请求可以包含软件声明中未包含的其他字段。在这个例子中，客户端软件可能会安装在不同的主机上，因此需要不同的重定向
URI，并且还可以配置为访问不同的 Scope。针对该客户端的注册请求会将其软件声明作为一个额外参数一并提交。

``` shell
POST /register HTTP/1.1
Host: localhost:9001
Content-Type: application/json
Accept: application/json

{
  "redirect_uris": ["http://localhost:9000/callback"],
  "scope": "foo bar baz",
  "software_statement": " eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzb2Z0d2FyZV
  9pZCI6Ijg0MDEyLTM5MTM0LTM5MTIiLCJzb2Z0d2FyZV92ZXJzaW9uIjoiMS4yLjUtZG9scGhp
  biIsImNsaWVudF9uYW1lIjoiU3BlY2lhbCBPQXV0aCBDbGllbnQiLCJjbGllbnRfdXJpIjoiaH
  R0cHM6Ly9leGFtcGxlLm9yZy8iLCJsb2dvX3VyaSI6Imh0dHBzOi8vZXhhbXBsZS5vcmcvbG9n
  by5wbmciLCJ0b3NfdXJpIjoiaHR0cHM6Ly9leGFtcGxlLm9yZy90ZXJtcy1vZi1zZXJ2aWNlLy
  J9.X4k7X-JLnOM9rZdVugYgHJBBnq3s9RsugxZQHMfrjCo"
}
```

授权服务器会解析软件声明，验证其签名，并确认该声明由其信任的权威机构签发。若确认无误，软件声明中的各项声明（claims）将覆盖未签名
JSON 对象中提交的声明。

软件声明提供了比 OAuth
中常见的自我声明值更高等级的信任机制。它还允许一组授权服务器信任某个（或多个）中心权威，由其为不同客户端签发软件声明。此外，授权服务器还能基于软件声明中所有实例都会提供的信息，将同一客户端的多个实例在逻辑上归为一组。尽管每个实例仍会获得各自的客户端
ID 和客户端密钥，但如果任一实例存在恶意行为，服务器管理员可以选择一次性禁用或撤销某款软件的所有副本。

软件声明的实现方式留作读者练习。

## 动态注册客户端的管理

客户端的元数据并不总是随时间保持不变。在客户端生命周期内，它可能会更改展示名称、增加或删除重定向 URI、为新增功能申请新的
scope，或进行其他各种变更。客户端也可能希望读取自己的配置信息。如果授权服务器在一段时间后或因某个触发事件轮换了客户端密钥，客户端就需要获知新的密钥。最后，如果客户端明确自己不会再被使用（例如用户正在卸载该客户端），它可以通知授权服务器删除其客户端
ID 及相关数据。

### 管理协议如何工作

针对上述所有用例，OAuth 动态客户端注册管理协议[^3] 定义了对 OAuth 动态客户端注册的 RESTful
协议扩展。该管理协议在核心注册协议的“创建（create）”方法基础上，增加了相应的“读取（read）”“更新（update）”和“删除（delete）”方法，从而支持对动态注册客户端的全生命周期管理。

[^3]: RFC 7592 [https://tools.ietf.org/html/rfc7592](https://datatracker.ietf.org/doc/html/rfc7592)

为此，管理协议会在注册端点的响应中扩展两个额外字段。首先，服务器会通过 `registration_client_uri` 字段向客户端下发一个“客户端配置端点”的
URI。这个 URI 提供了针对该特定客户端的全部管理能力。客户端应当按原样直接使用该
URI，无需添加任何额外参数或做任何转换。它通常对授权服务器上注册的每个客户端都是唯一的，但 URI 的具体结构完全由授权服务器自行决定。其次，服务器还会在
`registration_access_token` 字段中返回一种专用的访问令牌，称为“注册访问令牌”。这是一种 OAuth Bearer
Token，客户端只能用它来访问客户端配置端点，不能用于其他任何地方。与所有其他 OAuth 令牌一样，该令牌的格式也完全由授权服务器决定，客户端同样按原样使用即可。

我们来看一个具体示例。首先，客户端向注册端点发送与第 12.1.3 节示例中相同的注册请求。服务器的响应也与之前一致，只是我们所说的那样扩展了
JSON 对象。我们的授权服务器遵循常见的 RESTful 设计原则，通过将客户端 ID 拼接到注册端点上来生成客户端配置端点的
URI，不过授权服务器也可以按其偏好的方式来组织该 URL。我们服务器中的注册访问令牌与我们生成的其他令牌一样，也是另一段随机字符串。

``` shell
HTTP/1.1 201 Created
Content-Type: application/json

{
  "client_id": "1234-wejeg-0392",
  "client_secret": "6trfvbnklp0987trew2345tgvcxcvbjkiou87y6t5r",
  "client_id_issued_at": 2893256800,
  "client_secret_expires_at": 0,
  "token_endpoint_auth_method": "client_secret_basic",
  "client_name": "OAuth Client",
  "redirect_uris": ["http://localhost:9000/callback"],
  "client_uri": "http://localhost:9000/",
  "grant_types": ["authorization_code"],
  "response_types": ["code"],
  "scope": "foo bar baz",
  "registration_client_uri": "http://localhost:9001/register/1234-wejeg-0392"
  "registration_access_token": "ogh238fj2f0zFaj38dA"
}
```

注册响应的其余部分与之前相同。如果客户端想读取其注册信息，它会向客户端配置端点发送一条 HTTP GET 请求，并在 Authorization
请求头中携带注册访问令牌。

``` shell
GET /register/1234-wejeg-0392 HTTP/1.1
Accept: application/json
Authorization: Bearer ogh238fj2f0zFaj38dA
```

授权服务器会进行校验，确保配置端点 URI 中所引用的客户端与注册访问令牌签发的客户端是同一个。只要一切都有效，服务器的响应方式与普通的注册请求类似。响应体仍然是一个描述已注册客户端的
JSON 对象，但响应码变为 HTTP 200 OK。授权服务器可以自由更新客户端的任意字段，包括 client secret 和注册访问令牌，但 client ID
不会改变。在这个示例中，服务器为客户端轮换了一个新的 secret，其他所有值保持不变。需要注意的是，响应中包含客户端配置端点的 URI
以及注册访问令牌。

``` shell
HTTP/1.1 200 OK
Content-Type: application/json

{
  "client_id": "1234-wejeg-0392",
  "client_secret": "6trfvbnklp0987trew2345tgvcxcvbjkiou87y6",
  "client_id_issued_at": 2893256800,
  "client_secret_expires_at": 0,
  "token_endpoint_auth_method": "client_secret_basic",
  "client_name": "OAuth Client",
  "redirect_uris": ["http://localhost:9000/callback"],
  "client_uri": "http://localhost:9000/",
  "grant_types": ["authorization_code"],
  "response_types": ["code"],
  "scope": "foo bar baz",
  "registration_client_uri": "http://localhost:9001/register/1234-wejeg-0392"
  "registration_access_token": "ogh238fj2f0zFaj38dA"
}
```

如果客户端希望能够更新其注册信息，它会向配置端点发送一个 HTTP PUT 请求，并再次在 Authorization
头中使用注册访问令牌。客户端会携带其完整的配置信息——也就是注册请求返回的全部内容——其中包括之前签发的 client ID 和 client
secret。不过，与最初的动态注册请求一样，客户端不能自行指定 client ID 或 client secret 的值。客户端在请求中也不会包含以下字段（或这些字段对应的值）：

- client_id_issued_at
- client_secret_expires_at
- registration_client_uri
- registration_access_token

请求对象中的其他所有值，都表示要用来替换客户端注册中现有的对应值。请求中未包含的字段，会被视为删除现有值。

``` shell
PUT /register/1234-wejeg-0392 HTTP/1.1
Host: localhost:9001
Content-Type: application/json
Accept: application/json
Authorization: Bearer ogh238fj2f0zFaj38dA

{
  "client_id": "1234-wejeg-0392",
  "client_secret": "6trfvbnklp0987trew2345tgvcxcvbjkiou87y6",
  "client_name": "OAuth Client, Revisited",
  "redirect_uris": ["http://localhost:9000/callback"],
  "client_uri": "http://localhost:9000/",
  "grant_types": ["authorization_code"],
  "scope": "foo bar baz"
}
```

授权服务器会再次检查，以确配置端点 URI 中所指的客户端，确实与注册访问令牌（registration access
token）签发给的客户端一致。若请求中包含客户端密钥（client secret），授权服务器也会校验其是否与预期值匹配。授权服务器会返回与读取请求相同的响应：HTTP
200 OK，并在响应体中以 JSON 对象形式给出已注册客户端的详细信息。与初始注册请求一样，授权服务器可以按需拒绝或替换客户端提交的任何输入。除客户端
ID 外，授权服务器同样可以再次修改客户端的任意元数据信息。

如果客户端希望从授权服务器注销自身，它会向客户端配置端点发送 HTTP DELETE 请求，并在 Authorization 请求头中携带注册访问令牌。

``` shell
DELETE /register/1234-wejeg-0392 HTTP/1.1
Host: localhost:9001
Authorization: Bearer ogh238fj2f0zFaj38dA
```

授权服务器会再次检查，确认配置端点 URI 中引用的客户端，是否与注册访问令牌所签发的客户端一致。如果一致，并且服务器能够删除该客户端，则会返回一个空的
HTTP 204 No Content 响应。

``` shell
HTTP/1.1 204 No Content
```

从那之后，客户端需要丢弃其注册信息，包括客户端 ID、客户端密钥以及注册访问令牌。授权服务器也应在条件允许的情况下，删除所有与该已删除客户端关联的访问令牌和刷新令牌。

### 实现动态客户端注册管理 API

现在我们已经明确了每个操作的预期行为，接下来要在授权服务器中实现管理 API。打开 ch-12-ex-2，并编辑本练习中的
authorizationServer.js
文件。我们已经提供了动态客户端注册核心协议的实现，因此接下来将重点实现支持管理协议所需的新功能。记住，如果你愿意，可以访问授权服务器首页 http://localhost:9001/
查看所有已注册客户端；页面会打印出全部已注册客户端的客户端信息（见图 12.3）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260106134444642.png){ loading=lazy }
  <figcaption>图 12.3：授权服务器展示多个已注册客户端</figcaption>
</figure>

在注册处理函数中，你首先会注意到，我们把第 12.1
节练习里对客户端元数据的校验抽离成了一个工具函数。这样做是为了能在多个函数中复用同一套校验逻辑。若请求的元数据通过所有检查，就会原样返回；如果任意一项检查失败，工具函数会通过
HTTP 通道发送相应的错误响应并返回 null，让调用方无需再做额外处理，直接立即返回即可。在注册函数里调用该检查时，我们现在是这样写的：

``` javascript
var reg = checkClientMetadata(req);
if (!reg) {
  return;
}
```

首先，我们需要扩展注册端点返回的客户端信息。在生成 client_id 和 client_secret 之后、将输出渲染到响应之前，我们需要创建一个注册访问令牌（registration
access token），并将其附加到客户端对象上，以便后续校验。我们还需要生成并返回客户端配置端点（client configuration endpoint）的
URI；在我们的服务器实现中，它将通过把 client_id 追加到注册端点 URI 的末尾来构造。

``` javascript
reg.client_id = randomstring.generate();
if (__.contains(['client_secret_basic', 'client_secret_post']),
reg.token_endpoint_auth_method) {
  reg.client_secret = randomstring.generate();
}

reg.client_id_created_at = Math.floor(Date.now() / 1000);
reg.client_secret_expires_at = 0;

reg.registration_access_token = randomString.generate();
reg.registration_client_uri = 'http://localhost:9001/register/' + reg.client_id;

clients.push(reg);

res.status(201).json(reg);
return;
```

现在，存储的客户端信息和返回的 JSON 对象里都包含了访问令牌以及客户端注册 URI。接下来，由于我们需要对管理 API
的每个请求都校验注册访问令牌（registration access token），我们将创建一个过滤器函数来处理这段通用逻辑。别忘了，这个过滤器函数会接收第三个参数
next，也就是在过滤器成功执行后要调用的函数。

``` javascript
var authorizeConfigurationEndpointRequest = function(req, res, next) {

};
```

首先，我们会从传入请求的 URL 中提取客户端 ID，并尝试查找对应的客户端。如果找不到，就返回错误并直接退出。

``` javascript
var clientId = req.params.clientId;
var client = getClient(clientId);
if (!client) {
  res.status(404).end();
  return;
}
```

接下来，从请求中解析注册访问令牌（registration access token）。尽管在这里我们可以采用任何符合规范的 Bearer Token
传递方式，但为简化起见，我们只支持通过 Authorization 请求头传递。就像在受保护资源中一样，检查 Authorization 请求头并提取其中的
Bearer Token；如果没有找到，则返回错误。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260106134925932.png){ loading=lazy }
  <figcaption></figcaption>
</figure>

最后，如果我们确实拿到了访问令牌，就必须确认它是颁发给这个已注册客户端的正确令牌。若匹配无误，就可以继续执行处理器链中的下一个函数。由于我们已经查过客户端了，会把它挂到请求对象上，避免再次查询。若令牌不匹配，则返回错误。

``` javascript
if (regToken == client.registration_access_token) {
  req.client = client;
  next();
  return;
} else {
  res.status(403).end();
  return;
}
```

现在我们可以开始实现这些功能了。首先，我们会为这三个函数都设置对应的处理器，并确保在处理器配置中加入 filter
函数。每个处理器都会配置一个特殊的 `:clientId` 路径参数，该参数会由 Express.js 框架解析，并通过 `req.params.clientId`
变量传递给我们，就像前面的 filter 函数中使用的那样。

``` javascript
app.get('/register/:clientId', authorizeConfigurationEndpointRequest, function(req, res) {

});

app.put('/register/:clientId', authorizeConfigurationEndpointRequest, function(req, res) {

});

app.delete('/register/:clientId', authorizeConfigurationEndpointRequest, function(req, res) {

});
```

我们先从 `read` 函数说起。由于 `filter` 函数已经帮我们校验了客户端注册访问令牌（registration access
token），并加载了客户端信息，我们要做的只是把该客户端作为一个 JSON 对象返回即可。当然，如果你愿意，也可以在返回客户端信息之前更新客户端密钥（client
secret）和注册访问令牌，但这部分就留给读者自行练习了。

``` javascript
app.get('/register/:clientId', authorizeConfigurationEndpointRequest, function(req, res) {
  res.status(200).json(req.client);
  return;
});
```

接下来我们来处理更新函数。先检查客户端 ID 和客户端密钥（如果提供）是否与当前已存储在该客户端中的信息一致。

``` javascript
if (req.body.client_id != req.client.client_id) {
  res.status(400).json({error: 'invalid_client_metadata'});
  return;
}

if (req.body.client_secret && req.body.client_secret !=
req.client.client_secret) {
  res.status(400).json({error: 'invalid_client_metadata'});
}
```

接下来，我们需要校验客户端传入的其余元数据。我们将复用注册步骤中使用的客户端元数据校验函数。该函数会过滤掉所有不应出现的输入字段，例如
registration_client_uri 和 registration_access_token。

``` javascript
var reg = checkClientMetadata(req, res);
if (!reg) {
  return;
}
```

最后，把请求对象中的值复制到我们已保存的 client 中并返回。由于我们使用的是简单的内存存储机制，不需要再把 client
写回数据存储；但如果是以数据库为后端的系统，可能就有这样的要求。reg 中的各项值在内部是一致的，会直接替换 client 里对应的内容；而对于
reg 中缺失的字段，它们会覆盖 client 中的原有值。

``` javascript
__.each(reg, function(value, key, list) {
  req.client[key] = reg[key];
});
```

完成这次拷贝后，我们就可以像在 read 函数中那样，以同样的方式返回 client 对象。

``` javascript
res.status(200).json(req.client);
return;
```

对于删除功能，我们需要把该客户端从数据存储中移除。为此，我们会借助 Underscore.js 提供的几个库函数来实现。

``` javascript
clients = __.reject(clients, __.matches({client_id: req.client.client_id}));
```

作为授权服务器，我们也会尽到应尽的审查义务，并在返回之前立即吊销此前签发给该客户端的所有未失效令牌——无论是访问令牌还是刷新令牌。

``` javascript
nosql.remove().make(function(builder) {
  builder.where('client_id', clientId);
  builder.callback(function(err, count) {
    console.log("Removed %s tokens", count);
  });
});

res.status(204).end();
return;
```

通过这些小幅补充，授权服务器现已支持完整的动态客户端注册管理协议，使动态客户端能够管理其完整生命周期。

接下来我们将修改客户端来调用这些功能，请编辑 client.js。加载客户端并获取令牌后，客户端首页会额外显示一组控制项（见图 12.4）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260106135331347.png){ loading=lazy }
  <figcaption>图 12.4 客户端首页：显示动态注册的客户端 ID，并提供用于管理注册的控制项</figcaption>
</figure>

我们来给那些闪亮的新按钮接上点实际功能。首先，为了读取客户端数据，我们会向客户端的配置管理端点发起一个简单的 GET 请求，并使用注册访问令牌进行认证。我们会把这次调用的返回结果保存为新的客户端对象，以防配置发生了变化，同时使用受保护资源查看器模板将其展示出来，直接呈现服务器返回的原始内容。

``` javascript
app.get('/read_client', function(req, res) {

  var headers = {
      'Accept': 'application/json',
      'Authorization': 'Bearer ' + client.registration_access_token
  };

  var regRes = request('GET', client.registration_client_uri, {
                headers: headers
     });

  if (regRes.statusCode == 200) {
      client = JSON.parse(regRes.getBody());
      res.render('data', {resource: clien});
      return;
  } else {
      res.render('error', {error: 'Unable to read client ' +
  regRes.statusCode});
      return;
  }

});
```

接下来我们来处理用于更新客户端显示名称的表单。我们需要先克隆一份 client 对象，按照前面讨论的那样删除多余的注册字段，然后替换其中的 name。接着携带注册访问令牌（registration access token），通过 HTTP PUT 将这个新对象发送到客户端配置（client configuration）端点。服务器返回成功响应后，我们就把返回结果保存为新的 client 对象，并跳回到索引页。

``` javascript
app.post('/update_client', function(req, res) {

  var headers = {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      'Authorization': 'Bearer ' + client.registration_access_token
  };

  var reg = __.clone(client);
  delete reg['client_id_issued_at'];
  delete reg['client_secret_expires_at'];
  delete reg['registration_client_uri'];
  delete reg['registration_access_token'];

  reg.client_name = req.body.client_name;

  var regRes = request('PUT', client.registration_client_uri, {
                body: JSON.stringify(reg),
                headers: headers
     });

  if (regRes.statusCode == 200) {
      client = JSON.parse(regRes.getBody());
      res.render('index', {access_token: access_token, refresh_token: refresh_token, scope: scope, client: client});
      return;
  } else {
      res.render('error', {error: 'Unable to update client ' +
  regRes.statusCode});
      return;
  }

});
```

最后，我们来处理删除客户端。这一步很简单：对客户端配置端点发起一次 DELETE 请求，并再次携带注册访问令牌用于鉴权。无论返回什么结果，我们都会丢弃本地的客户端信息，因为在我们看来，我们已经尽力注销该客户端了——至于服务器是否真正完成了注销，则另当别论。

``` javascript
app.get('/unregister_client', function(req, res) {

  var headers = {
      'Authorization': 'Bearer ' + client.registration_access_token
  };

  var regRes = request('DELETE', client.registration_client_uri, {
       headers: headers
  });

  client = {};

  if (regRes.statusCode == 204) {
      res.render('index', {access_token: access_token, refresh_token: refresh_token, scope: scope, client: client});
      return;
  } else {
      res.render('error', {error: 'Unable to delete client ' + regRes.statusCode});
      return;
  }

});
```

有了这些，我们就拥有了一个完全托管、可动态注册的 OAuth 客户端。更高级的客户端管理（例如编辑其他字段、轮换客户端密钥以及注册访问令牌）就留给读者自行实现。

## 总结  

动态客户端注册是 OAuth 协议生态中的一项强大扩展。

- 客户端可以动态地向授权服务器“自我介绍”，但要访问受保护资源，仍然需要资源所有者的授权。  
- 客户端 ID 和客户端密钥最好由将要接受它们的授权服务器签发。  
- 客户端元数据用于描述客户端的诸多属性，也可以包含在签名的软件声明（software statement）中。  
- 动态客户端注册管理协议通过 RESTful API，为动态注册的客户端提供了一整套覆盖全生命周期的管理操作。  

既然你已经了解如何以编程方式将客户端引入授权服务器，我们接下来看看 OAuth 的一个常见应用：终端用户认证。

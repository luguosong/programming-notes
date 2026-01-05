# 4.构建一个简单的 OAuth 受保护资源

本章内容包括

- 解析传入的 HTTP 请求中的 OAuth 令牌
- 响应令牌相关错误
- 根据 scope 以不同方式处理请求
- 根据资源所有者以不同方式处理请求

现在我们已经有了一个可用的 OAuth 客户端，是时候创建一个受保护资源，让客户端能携带这些 access token
来调用了。在本章中，我们将搭建一个简单的资源服务器：客户端可以调用它，授权服务器也可以对它进行保护。每个练习我们都会提供一套完整可运行的客户端和授权服务器，并且都经过设计，能够彼此配合工作。

!!! note

	本书所有练习与示例都基于 Node.js 和 JavaScript 构建。每个练习由多个组件组成，这些组件会在同一台机器上运行，并通过 localhost 的不同端口对外提供访问。关于该框架及其结构的更多信息，请参见附录 A。

对大多数基于 Web 的 API 来说，为其增加一层 OAuth 安全机制是个相当轻量的过程。资源服务器需要做的事情很简单：从传入的 HTTP
请求中解析出 OAuth 令牌，验证该令牌，并判断该令牌允许执行哪些类型的请求。你正在阅读这一章，说明你很可能已经有现成的系统，或至少有一个设计好的接口，希望用
OAuth 来保护它。针对本章的练习，我们并不要求你为了练习保护接口而从零开发一个
API；相反，我们已经为你准备了一些资源端点和数据对象可供使用，并且每个练习附带的客户端应用也已经配置好能够调用这些接口。在本章的练习中，我们的资源服务器将是一个简单的数据存储：根据不同练习，它会在多个不同的
URL 上响应 HTTP GET 和 POST 请求，并返回 JSON 对象。

尽管在 OAuth 的结构中，受保护资源与授权服务器在概念上是相互独立的组件，但许多 OAuth
实现会将资源服务器与授权服务器部署在同一位置。当两个系统之间耦合较紧时，这种做法通常很合适。在本章的练习中，我们会在同一台机器上用独立进程运行受保护资源，但会让它能够访问授权服务器所使用的同一套数据库。我们会在第
11 章进一步探讨如何把这种连接拆分得更彻底一些。

## 从 HTTP 请求中解析 OAuth Token

打开练习 ch-4-ex-1，编辑 protectedResource.js 文件。本练习中，client.js 和 authorizationServer.js 文件无需修改，保持原样即可。

我们的受保护资源期望接收 OAuth Bearer Token，这很合理，因为授权服务器生成的就是 Bearer Token。《OAuth Bearer Token
Usage》规范[^1]定义了三种将 Bearer Token 传递给受保护资源的方式：通过 HTTP Authorization 头、放在表单编码的 POST
请求体中，以及作为查询参数传递。我们将把受保护资源配置为支持这三种方式，并优先使用 Authorization 头。

[^1]: [RFC 6750](https://datatracker.ietf.org/doc/html/rfc6750)

由于我们会在多个资源 URL 上做这件事，我们会把 token 的扫描逻辑放到一个辅助函数里。我们练习所基于的 Express.js Web
应用框架提供了一个相当简单的实现方式。虽然这里的实现细节是 Express.js 特有的，但整体思路同样适用于其他 Web 框架。与目前大多数
HTTP 处理函数不同，这个辅助函数会接收三个参数。第三个参数 next 是一个函数，调用它可以继续处理当前请求。这样我们就能把多个函数串联起来共同处理一次请求，从而把
token 扫描功能注入到应用中各处的其他 handler 里。现在这个函数还是空的，稍后我们会把这段代码替换掉。

```javascript
var getAccessToken = function (req, res, next) {

}
)
;
```

OAuth Bearer 令牌规范规定：当令牌通过 HTTP Authorization 头传递时，头部的值由关键字 Bearer、一个空格以及令牌值本身组成。此外，OAuth
规范还说明 Bearer 关键字不区分大小写。与此同时，HTTP 规范也指出 Authorization 头字段名同样不区分大小写。这意味着下面所有这些请求头都是等价的：

```shell
Authorization: Bearer 987tghjkiu6trfghjuytrghj
Authorization: bearer 987tghjkiu6trfghjuytrghj
authorization: BEARER 987tghjkiu6trfghjuytrghj
```

首先，我们会尝试获取请求中是否包含 Authorization 头，然后检查它是否携带 OAuth Bearer token。由于 Express.js 会自动将所有传入的
HTTP 头字段名转换为小写，我们会在传入的 request 对象上用字符串字面量 authorization 来进行匹配。同样地，我们也会把该头的值转换为小写，再去检查是否包含
bearer 关键字。

``` javascript
var inToken = null;
var auth = req.headers['authorization'];
if (auth && auth.toLowerCase().indexOf('bearer') == 0) {
```

如果这两项都通过了，接下来就需要从请求头中取出 token：把 Bearer 关键字以及其后面的空格去掉即可。请求头里剩下的内容就是
OAuth token 的值，不需要再做任何额外处理。好在这种字符串操作在 JavaScript 以及大多数其他语言中都非常简单。注意 token
值本身是区分大小写的，所以要从原始字符串上进行截取，而不是在经过转换后的字符串上操作。

``` javascript
inToken = auth.slice('bearer '.length);
```

接下来，我们来处理在请求体中以表单编码参数传递的 token。OAuth 规范并不推荐这种方式，因为它会人为地把 API
的输入限制在一组表单编码的键值对里。如果 API 原生使用 JSON 作为输入，这种限制会导致客户端应用无法在提交业务输入的同时把
token 一并发送过去。在这种情况下，更推荐使用 Authorization 请求头。但对于确实接收表单编码输入的 API
来说，这种方式为客户端提供了一种简单、一致的途径来传递访问令牌，而无需处理 Authorization
请求头。我们的练习代码已经配置为自动解析传入的表单请求体，因此我们需要先检查它是否存在，并在前一个 if 语句的额外分支中从中取出
token 的值。

``` javascript
} else if (req.body && req.body.access_token) {
  inToken = req.body.access_token;
```

最后，我们来处理将令牌作为查询参数传递的情况。OAuth 仅在另外两种方式都不适用时，才把这种方式作为最后的选择推荐使用。采用这种方式时，访问令牌更有可能被无意记录到服务器访问日志中，或通过
Referrer 头意外泄露——这两者都会完整复现 URL。不过，在某些场景下，客户端应用无法直接访问 Authorization
头（受平台或库的访问限制），也无法使用表单编码的请求体参数（例如 HTTP GET）。此外，这种方式还使得 URL
不仅包含资源本身的定位信息，还包含访问该资源所需的凭据。在这些情况下，在充分考虑相应安全因素的前提下，OAuth
允许客户端将访问令牌作为查询参数发送。我们将以与前面表单编码请求体参数相同的方式处理它。

``` javascript
} else if (req.query && req.query.access_token) {
  inToken = req.query.access_token
}
```

在这三种方法都就位之后，我们的函数就如附录 B 的代码清单 5 所示。

我们收到的访问令牌值保存在 inToken 变量中；如果没有传入令牌，它就是 null。不过这还不够：我们还得确认该令牌是否有效，以及它具体能用来做什么。

## 根据我们的数据存储校验令牌

在示例应用中，我们可以直接访问授权服务器用来存储令牌的数据库。这在小规模的 OAuth 部署中很常见：授权服务器与其保护的 API
通常部署在同一环境中。本步骤的细节取决于具体实现，但其思路和模式具有普遍适用性。关于这种本地查询方式的替代方案，我们会在第
11 章讨论。

我们的授权服务器使用一个基于磁盘文件的 NoSQL 数据库，并通过一个简单的 Node.js 模块提供访问。如果你想在程序运行时实时查看数据库内容，可以监控练习目录下的
database.nosql 文件。注意：系统运行期间手动编辑该文件有风险。好在重置数据库非常简单——删除 database.nosql
文件并重启程序即可。另外，该文件只有在授权服务器首次将令牌写入时才会创建；并且每次重启授权服务器时，文件内容都会被清空重置。

接下来我们会在数据库中做一次简单查询：根据传入的令牌值找到对应的 access token。服务器会将每个 access token 和 refresh
token 作为数据库中的独立条目存储，因此只需要利用数据库的检索能力找到匹配项即可。查询函数的具体实现取决于所用 NoSQL
数据库，但同样的查询思路也适用于其他数据库。

``` javascript
nosql.one().make(function(builder) {
  builder.where('access_token', inToken);
  builder.callback(function(err, token) {
    if (token) {
      console.log("We found a matching token: %s", inToken);
    } else {
      console.log('No matching token was found.');
    };
    req.access_token = token;
    next();
    return;
  });
});
```

函数的第一部分会将已存储的访问令牌与我们从网络中取到的输入令牌进行比对。只要找到匹配项，就会返回该令牌并停止搜索。后面的部分定义了一个回调函数：当找到匹配项或数据库已遍历完毕时（以先发生者为准）触发。如果确实在存储中找到了令牌，它会通过
token 参数传入；如果无法根据输入值找到令牌，该参数则为 null。无论结果如何，我们都会把它挂到 req 对象的 access_token 成员上，然后调用
next 函数。req 对象会被自动传递到处理流程的下一环节。

返回的 token 对象与授权服务器在生成令牌时写入的对象完全一致。比如，我们这个简易的授权服务器会用类似下面这样的 JSON
对象来存储访问令牌及其 scope：

``` json
{
  "access_token": "s9nR4qv7qVadTUssVD5DqA7oRLJ2xonn",
  "clientId": "oauth-client-1",
  "scope": ["foo"]
}
```

!!! note "我必须共享数据库吗？ "

	尽管使用共享数据库是 OAuth 部署中非常常见的模式，但绝不是唯一选择。有一种标准化的 Web 协议叫作 Token Introspection（令牌自省），授权服务器可以提供该能力，让资源服务器在运行时检查令牌的状态。这样一来，资源服务器就可以像客户端一样把令牌视为不透明的字符串来处理，代价是会带来更多的网络流量。另一种方式（也可以与前者同时使用）是让令牌本身携带受保护资源能够直接解析和理解的信息。其中一种结构是 JSON Web Token（JWT），它在经过密码学保护的 JSON 对象中承载一组声明（claims）。第 11 章将介绍这两种技术。

	你可能还会疑惑，是否必须像我们的示例那样把令牌以原始值的形式存到数据库里。虽然这是简单且常见的做法，但也有替代方案。比如，你可以存储令牌值的哈希而不是令牌本身，类似用户密码通常的存储方式。当需要查询令牌时，再对令牌值进行一次哈希计算，并与数据库中的内容进行比对。或者，你也可以在令牌内部加入一个唯一标识符（unique identifier），并使用服务器密钥对其进行签名，数据库里只保存这个唯一标识符。当必须查询令牌时，资源服务器可以先验证签名，再解析令牌找到该标识符，并用它在数据库中查到对应的令牌信息。

加入这段代码后，我们的辅助函数就变成了附录 B 中的代码清单 6。

接下来要把它接入我们的服务。在 Express.js 应用里，主要有两种接法：要么对所有请求都执行一次，要么只对我们需要校验 OAuth Token
的那些请求执行。若希望每个请求都走这段处理逻辑，我们就新增一个监听器并把该函数挂上去。由于路由中的中间件会按代码添加顺序依次执行，所以这一步必须放在
router 里其他函数之前完成连接。

``` javascript
app.all('*', getAccessToken);
```

或者，我们也可以把新函数插入到现有的处理器（handler）配置里，让它优先被调用。比如，在当前代码中我们有这样一个函数。

``` javascript
app.post("/resource", function (req, res) {

});
```

要让我们的令牌处理器函数最先被调用，我们只需要在定义处理器之前，把该函数添加到路由中即可。

``` javascript
app.post("/resource", getAccessToken, function (req, res) {

});
```

当处理器被调用时，request 对象上会挂载一个 access_token 成员。如果找到了该 token，它将包含从数据库取出的 token 对象；如果没找到，则为
null。我们可以据此对代码进行分支处理。

``` javascript
if (req.access_token) {
  res.json(resource);
} else {
  res.status(401).end();
}
```

现在运行客户端应用，并让它去获取受保护的资源，应该会看到一个类似图 4.1 所示的界面。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260103223119659.png){ loading=lazy }
  <figcaption>图 4.1 客户端成功访问受保护资源时的页面</figcaption>
</figure>

在客户端不携带访问令牌去调用受保护资源时，会返回一条错误信息；该错误信息会从客户端收到的受保护资源的 HTTP 响应中透传出来（见图
4.2）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260103223237296.png){ loading=lazy }
  <figcaption>图 4.2  客户端页面在从受保护资源收到 HTTP 错误时的显示状态</figcaption>
</figure>

现在我们有了一个非常简单的受保护资源，它可以根据是否存在有效的 OAuth 令牌来决定是否响应请求。有时候这就足够了，但 OAuth
也为你提供了更大的空间，可以更灵活地为受保护的 API 应用安全策略。

## 基于令牌提供内容

如果你的 API 并不是在静态资源前面加一道简单的“是/否”式网关来决定是否放行，该怎么办？很多 API 的设计是：对 API
的不同操作需要不同的访问权限。有些 API 会根据调用者所代表的权限主体返回不同的结果，或者在不同访问级别下只返回部分信息。接下来我们会基于
OAuth 的 scope（作用域）机制，并结合对资源所有者（resource owner）和客户端（client）的引用，搭建几种这样的场景。

在后续每个练习中，如果你查看受保护资源服务器（protected resource server）的代码，会发现我们已经把上一个练习里的
getAccessToken 工具函数包含进来了，并且把它接到了所有 HTTP handler 上。不过，这个函数只负责提取 access
token（访问令牌），并不会根据令牌是否存在来做处理决策。为此，我们还接入了一个简单的工具函数
requireAccessToken：当令牌不存在时，它会负责返回错误；否则就把控制权交给最终的 handler 继续处理。

``` javascript
var requireAccessToken = function(req, res, next) {
  if (req.access_token) {
      next();
  } else {
      res.status(401).end();
  }
});
```

在每个练习中，我们都会为各个 handler 增加代码，用于检查 Token 的状态，并根据检查结果正确返回相应的响应。我们已经在每个练习里把客户端配置好，使其能够请求所有必要的
scope，而授权服务器会允许你以资源所有者的身份，决定在某次交易中具体授予哪些 scope（见图 4.3）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260103225545975.png){ loading=lazy }
  <figcaption>图 4.3：审批页面展示了不同的权限范围，可供勾选确认</figcaption>
</figure>

每个练习中的客户端也可以通过不同的按钮调用该练习里的所有受保护资源。无论当前访问令牌包含哪些 scope，所有按钮都会始终可用。

### 不同操作对应不同 scope

在这种 API 设计风格下，不同类型的操作需要不同的 scope
才能调用成功。这样资源服务器就能根据客户端被允许执行的操作来划分功能。这也是一种常见做法：让同一个访问令牌可以在同一个授权服务器所关联的多个资源服务器之间通用。

打开 ch-4-ex-2 并编辑 protectedResource.js，client.js 和 authorizationServer.js 保持不变。客户端提供了一个页面：一旦你拿到令牌，就可以访问
API 的所有功能（见图 4.4）。蓝色按钮会读取当前的词列表并显示出来，同时带上时间戳。橙色按钮会向受保护资源中保存的当前列表新增一个词。红色按钮会删除集合中的最后一个词。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260103225648498.png){ loading=lazy }
  <figcaption>图 4.4：客户端包含三种不同的功能，每种功能分别映射到一个作用域</figcaption>
</figure>

我们的应用注册了三条路由，每条都对应不同的 HTTP 方法。目前，只要传入任意类型的有效访问令牌，它们都会执行。

``` javascript
app.get('/words', getAccessToken, requireAccessToken, function(req, res) {
  res.json({words: savedWords.join(' '), timestamp: Date.now()});
});

app.post('/words', getAccessToken, requireAccessToken, function(req, res) {
  if (req.body.word) {
      savedWords.push(req.body.word);
  }
  res.status(201).end();
});

app.delete('/words', getAccessToken, requireAccessToken, function(req, res) {
  savedWords.pop();
  res.status(204).end();
});
```

我们将逐一修改这些内容，确保令牌的 scope 至少包含每个函数所对应的 scope。由于令牌在数据存储中的保存方式，我们需要获取与该令牌关联的
scope 成员。对于 GET 方法，我们希望客户端具备与之关联的 read scope。它也可以同时拥有其他 scope，但我们的 API 并不特别在意是否还有其他
scope。

``` javascript
app.get('/words', getAccessToken, requireAccessToken, function(req, res) {
  if (__.contains(req.access_token.scope, 'read')) {
      res.json({words: savedWords.join(' '), timestamp: Date.now()});
  } else {
      res.set('WWW-Authenticate', 'Bearer realm=localhost:9002, error="insufficient_scope", scope="read"');
      res.status(403).end();
  }
});
```

我们会在 WWW-Authenticate 头中把错误信息返回给客户端。这样客户端就会知道，该资源需要一个 OAuth Bearer
Token，并且要让请求成功，至少必须携带 read 作用域。我们会在另外两个函数中加入类似的代码，分别校验 write 和 delete
作用域。无论哪种情况，只要令牌不具备正确的作用域，即使令牌本身是有效的，也会返回错误。

``` javascript
app.post('/words', getAccessToken, requireAccessToken, function(req, res) {
  if (__.contains(req.access_token.scope, 'write')) {
      if (req.body.word) {
            savedWords.push(req.body.word);
      }
      res.status(201).end();
  } else {
      res.set('WWW-Authenticate', 'Bearer realm=localhost:9002, error="insufficient_scope", scope="write"');
      res.status(403).end();
  }
});

app.delete('/words', getAccessToken, requireAccessToken, function(req, res) {
  if (__.contains(req.access_token.scope, 'delete')) {
      savedWords.pop();
      res.status(204).end();
  } else {
      res.set('WWW-Authenticate', 'Bearer realm=localhost:9002, error="insufficient_scope", scope="delete"');
      res.status(403).end();
  }
});
```

完成上述设置后，重新为客户端应用授权，以便支持不同的 scope
组合。比如，给客户端授予读和写权限，但不授予删除权限。你会发现可以把数据写入集合，但永远无法将其删除。想进阶一点的话，可以扩展受保护资源和客户端，让它们支持更多的
scope 和更多类型的访问方式。别忘了在授权服务器上更新本次练习所需的客户端注册信息！

### 不同数据结果对应不同的 scope

在这种 API 设计风格下，同一个处理器会根据传入 Token 中携带的 scope 不同，返回不同类型的信息。当你需要返回一组复杂的结构化数据，并希望在客户端无需为每种信息分别调用不同
API 端点的情况下，只授予其中部分数据的访问权限时，这种方式非常实用。

打开 ch-4-ex-3，编辑 protectedResource.js，client.js 和 authorizationServer.js 保持不变。客户端提供了一个页面：在你拿到
Token 之后可以用它调用 API，并展示返回的农产品列表（见图 4.5）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260104104618171.png){ loading=lazy }
  <figcaption>图 4.5 获取任何数据之前的客户端页面</figcaption>
</figure>

在受保护资源的代码中，我们没有为每种农产品类型分别编写多个独立的处理器，而是用一个统一的处理器来处理所有农产品相关的调用。目前，该处理器会返回一个对象，其中包含按类别划分的各类农产品列表。

``` javascript
app.get('/produce', getAccessToken, requireAccessToken, function(req, res) {
  var produce = {fruit: ['apple', 'banana', 'kiwi'],
      veggies: ['lettuce', 'onion', 'potato'],
      meats: ['bacon', 'steak', 'chicken breast']};
  res.json(produce);
});
```

在做任何事情之前，如果我们现在用任意一个有效的访问令牌去调用这个 API，返回的始终都是全部农产品的列表。如果你授权客户端获取访问令牌，但没有给它分配任何
scope，那么你会看到一个类似图 4.6 所示的页面。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260104104722557.png){ loading=lazy }
  <figcaption>图 4.6：客户端页面展示了在未指定任何作用域时返回的全部数据</figcaption>
</figure>

不过，我们希望受保护资源能够根据已授权给客户端的 scope，把农产品区按权限拆分开来。首先，需要把数据对象切分成多个部分，这样后续处理起来会更方便。

``` javascript
var produce = {fruit: [], veggies: [], meats: []};
produce.fruit = ['apple', 'banana', 'kiwi'];
produce.veggies = ['lettuce', 'onion', 'potato'];
produce.meats = ['bacon', 'steak', 'chicken breast'];
```

现在，我们可以把这些部分分别封装到控制语句中，通过检查各个产物类型对应的特定作用域来进行判断。

```javascript
var produce = {fruit: [], veggies: [], meats: []};
if (__.contains(req.access_token.scope, 'fruit')) {
    produce.fruit = ['apple', 'banana', 'kiwi'];
}
if (__.contains(req.access_token.scope, 'veggies')) {
    produce.veggies = ['lettuce', 'onion', 'potato'];
}
if (__.contains(req.access_token.scope, 'meats')) {
    produce.meats = ['bacon', 'steak', 'chicken breast'];
}
```

现在只为客户端应用授权 fruit 和 veggies 这两个 scope，然后再试一次请求。你应该会收到一份素食购物清单（见图 4.7）。[^2]

[^2]: 这会把所有肉类都去掉，尽管众所周知，培根有时候也算蔬菜。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260104104933242.png){ loading=lazy }
  <figcaption>图 4.7：客户端页面展示基于作用域返回的有限数据</figcaption>
</figure>

当然，OAuth 并不要求我们必须按这种方式把 API 按高层对象拆分。作为一个额外练习，给客户端和资源服务器加上一个 lowcarb 的
scope 选项，让它在每个分类下只返回低碳水的食物。它既可以和上一个练习中的 type/category scope 组合使用，也可以单独生效。归根结底，scope
的语义由你这个 API 设计者来定义；OAuth 只是提供了一种承载它的机制。

### 不同用户返回不同的数据结果

在这种 API 设计风格下，同一个处理器会根据是谁授权了客户端而返回不同的信息。这是很常见的 API 设计思路：客户端应用只需要调用一个
URL，即使还不知道用户是谁，也能拿到个性化的结果。第 1、2 章的云打印示例就是这种 API：打印服务不管用户是谁，调用的都是同一个照片存储
API，但拿到的会是该用户的照片。打印服务完全不需要知道用户标识符，或者任何关于用户身份的信息。

打开 ch-4-ex-4，编辑 protectedResource.js，保持 client.js 和 authorizationServer.js 不变。这个练习会提供一个单一的资源
URL，用来根据是谁授权了访问令牌，在多个分类中返回该用户的收藏信息。尽管在客户端与受保护资源之间的连接中，资源所有者并不在场也不会被认证，但生成的令牌会包含对资源所有者的引用——也就是在审批流程中完成认证的那位用户（图
4.8）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260104105206801.png){ loading=lazy }
  <figcaption>图 4.8 授权服务器的批准页面，展示资源所有者身份的选择</figcaption>
</figure>

!!! note "下拉菜单不等于身份认证"

	授权服务器的审批页面会让你选择要代表哪个用户进行响应：Alice 还是 Bob。通常，这一步应通过在授权服务器上对资源所有者进行身份认证来完成；而允许未认证的用户在系统中随意冒充任意身份，一般被认为是极其糟糕的安全实践。不过为了便于测试，我们保持示例代码尽可能简单，允许你通过下拉菜单选择当前用户。作为额外练习，试着为授权服务器加入用户身份认证组件。Node.js 和 Express.js 都有大量可用模块，你可以用来进行实验。

客户端有一个页面，拿到 token 后就可以调用该 API，并展示返回的个性化信息（见图 4.9）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260104105329638.png){ loading=lazy }
  <figcaption>图 4.9：在数据尚未获取之前的客户端页面</figcaption>
</figure>

目前，如你所见，它并不知道你在查询哪个用户，因此返回的是一个未知用户，并且没有任何收藏。查看受保护资源的代码，很容易就能看出这是怎么发生的。

``` javascript
app.get('/favorites', getAccessToken, requireAccessToken, function(req, res) {
  var unknown = {user: 'Unknown', favorites: {movies: [], foods: [], music: []}};
  console.log('Returning', unknown);
  res.json(unknown);
});
```

原来，在受保护资源端我们掌握了一些关于 Alice 和 Bob 的信息，并分别存储在 aliceFavorites 和 bobFavorites 变量中。

``` javascript
var aliceFavorites = {
  'movies': ['The Multidmensional Vector', 'Space Fights', 'Jewelry Boss'],
  'foods': ['bacon', 'pizza', 'bacon pizza'],
  'music': ['techno', 'industrial', 'alternative']
};

var bobFavorites = {
  'movies': ['An Unrequited Love', 'Several Shades of Turquoise', 'Think Of The Children'],
  'foods': ['bacon', 'kale', 'gravel'],
  'music': ['baroque', 'ukulele', 'baroque ukulele']
};
```

接下来我们要做的，就是根据是谁授权了客户端，分发对应要返回的那条数据记录。我们的授权服务器已经把资源所有者的用户名存到了访问令牌记录的
user 字段里，所以我们只需要据此切换返回的内容即可。

``` javascript
app.get('/favorites', getAccessToken, requireAccessToken, function(req, res) {
  if (req.access_token.user == 'alice') {
      res.json({user: 'Alice', favorites: aliceFavorites});
  } else if (req.access_token.user == 'bob') {
      res.json({user: 'Bob', favorites: bobFavorites});
  } else {
      var unknown = {user: 'Unknown', favorites: {movies: [], foods: [], music: []}};
      res.json(unknown);
  }
});
```

现在，如果你在授权服务器上以 Alice 或 Bob 的身份为客户端完成授权，那么客户端就会拿到他们各自的个性化数据。比如，图 4.10 展示了 Alice 的列表。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260104105541776.png){ loading=lazy }
  <figcaption>图 4.10  客户端页面显示 Alice 的资源数据</figcaption>
</figure>

在 OAuth 流程中，客户端始终不知道自己是在和 Alice 对话，而不是 Bob、Eve 或者其他任何人。客户端之所以“顺带”知道了 Alice 的名字，只是因为它调用的 API 在响应里包含了她的姓名；而这个字段也完全可以不返回。这是一种非常强大的设计模式，因为它可以在不必要时不暴露可识别个人身份的信息，从而保护资源所有者的隐私。当 OAuth 与会泄露用户信息的 API 结合使用时，它就会开始接近一种认证协议。我们会在第 13 章更深入地讨论这一点，并重点介绍支持终端用户认证所需的额外功能与特性。

当然，你也可以把这些方法组合起来使用。我们的授权服务器和客户端在本练习中已经配置为使用不同的 scope，但受保护资源目前还忽略了它们。作为一个额外练习，请根据客户端被授权的 movies、foods、music 这几个 scope，对 favorites API 的响应进行过滤。

### 额外的访问控制

本章列出的、受保护资源在使用 OAuth 时可应用的潜在访问控制远称不上全面。实际上，今天在用的具体模式可能和被保护的资源数量一样多。正因如此，OAuth 本身一直避开“授权决策”的制定过程，而是通过 token 和 scope 扮演授权信息的载体。这种做法使 OAuth 能够适配互联网上极其多样化的 API 风格。

资源服务器可以使用 token 以及附着在 token 上的信息（例如 scope），直接据此做出授权决策。或者，资源服务器也可以把访问令牌所关联的权限与其他访问控制信息结合起来，以决定是否响应某个 API 调用，以及针对特定请求应该返回什么内容。比如，资源服务器可以决定无论 token 是否有效，都只允许某些客户端和用户在特定时间段访问某些内容。资源服务器甚至可以把 token 作为输入，调用外部策略引擎，从而将复杂的授权规则集中到组织内部统一管理。

无论哪种情况，资源服务器对“访问令牌意味着什么”拥有最终解释权。不管它把决策过程外包了多少，针对任何给定请求，在具体上下文中该如何处理，最终都由资源服务器来决定。

## 小结

使用 OAuth 保护 Web API 相当直接。

- 从传入请求中解析出 token。  
- 向授权服务器验证 token。  
- 根据 token 所允许的权限来返回响应，而这种方式可以有多种形式。  

现在你已经构建了客户端和受保护资源，是时候来构建 OAuth 系统中最复杂、也可以说最重要的组件了：授权服务器。

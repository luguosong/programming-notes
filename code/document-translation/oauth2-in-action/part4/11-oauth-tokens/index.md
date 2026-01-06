# 11.OAuth 令牌

本章内容包括

- 什么是 OAuth 令牌
- 在结构化的 JSON Web Token（JWT）中携带信息
- 使用 JOSE 保护令牌数据
- 通过令牌内省（token introspection）实时查询令牌信息
- 通过令牌撤销（token revocation）管理令牌生命周期

无论 OAuth 协议里有多少重定向、流程和组件，归根结底讲的都是令牌。回想第 1
章的云打印示例：为了让照片存储服务确认打印机具备访问照片的权限，打印机服务需要拿出某种东西来证明授权。打印机交给存储服务的这个东西，我们称之为访问令牌（access
token）；在本书中，我们已经大量使用并反复接触过它们。现在，我们将更深入地了解 OAuth 令牌，以及如何在 OAuth 生态中管理它们。

## 什么是 OAuth 令牌？

令牌是所有 OAuth
交互的核心。客户端从授权服务器获取令牌，用于向受保护资源发起访问。授权服务器负责创建令牌并发放给客户端，同时在这个过程中管理资源所有者的授权委托以及客户端权限，并将这些信息附加到令牌上。受保护资源从客户端接收令牌并进行校验，将令牌中附带的权限与客户端发起的请求进行匹配。

令牌代表一次授权委托行为的结果：它可以看作由资源所有者、客户端、授权服务器、受保护资源、scope，以及授权决策相关的其他信息共同构成的一个“组合”。当客户端需要在不再次打扰资源所有者的情况下刷新访问令牌时，会使用另一种令牌：刷新令牌（refresh
token）。OAuth 令牌是整个 OAuth 生态的关键机制；没有令牌，严格意义上也就谈不上 OAuth。甚至，OAuth 的非官方标识也是基于实体公交代币的外观设计的（见图
11.1）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260105154306169.png){ loading=lazy }
  <figcaption>图 11.1：非官方 OAuth 标志，造型参考了公交代币</figcaption>
</figure>

尽管 OAuth 把大量精力都放在令牌（token）上，但它完全不会对令牌的内容是什么做任何主张或说明。正如我们在第 2 章和第 3 章讨论过的，在
OAuth
里，客户端并不需要了解令牌本身的任何细节。客户端只需要知道两件事：如何从授权服务器获取令牌，以及如何在资源服务器上使用令牌。不过，授权服务器和资源服务器确实需要理解令牌。授权服务器需要知道该如何构造一个令牌并交给客户端；资源服务器则需要知道如何识别并验证客户端递交给它的令牌。

为什么 OAuth 的核心规范会把如此基础的内容留空？正因为不规定令牌本身，OAuth 才能适配各种不同特性、风险画像和需求的部署场景。OAuth
令牌可以过期、可以被撤销、也可以长期有效，或者根据具体情况进行组合。它们可以代表某个特定用户，也可以代表系统中的所有用户，甚至也可以不代表任何用户。它们可以具有内部结构，也可以只是随机的无意义字符串；可以经过密码学保护，也可以是上述选项的任意组合。正是这种灵活性与模块化，让
OAuth 能以多种方式进行适配；而像 WS-*、SAML、Kerberos 这类更“全面”的安全协议会规定令牌格式，并要求系统中的各方都理解该格式，因此在适配上往往更受限制。

不过，业界仍有几种常见的令牌生成与校验方式，每种方式都有各自的优缺点，适用于不同的场景。在本书第 3、4、5
章的练习中，我们创建的令牌是由字母数字组成的随机字符串块（blob）。在网络传输中，它们大致长这样：

``` shell
s9nR4qv7qVadTUssVD5DqA7oRLJ2xonn
```

当授权服务器创建令牌时，它会把令牌的值存到磁盘上的共享数据库里。当受保护资源从客户端收到令牌后，就到同一个数据库中查询该令牌的值，以确定这个令牌具有什么权限。这类令牌本身不携带任何信息，而是作为用于数据查询的简单“句柄”。这是一种完全有效且并不少见的访问令牌创建与管理方式；它的优势在于既能让令牌本身保持很小，又能提供足够高的熵。

但在授权服务器与受保护资源之间共享数据库并不总是现实可行，尤其是当一个授权服务器需要为下游多个不同的受保护资源提供保护时。那我们还能怎么做？本章将介绍另外两种常见选择：结构化令牌（structured
tokens）与令牌自省（token introspection）。

## 结构化令牌：JSON Web Token（JWT）

如果我们能创建一种令牌，把所有必要信息都直接放进令牌里，而不是依赖共享数据库查询，会怎么样？这样一来，授权服务器就能通过令牌本身，间接把信息传递给受保护资源，无需任何网络
API 调用。

在这种方式下，授权服务器会把受保护资源所需的信息打包进令牌，例如令牌的过期时间戳、授权该令牌的用户等。这些内容会一并发送给客户端，但客户端并不会察觉，因为在所有
OAuth 2.0 系统中，令牌对客户端而言始终是“不透明”的（opaque）。客户端拿到令牌后，会像发送一段随机字节数据一样把令牌发送给受保护资源。受保护资源则需要理解令牌：它会解析令牌中包含的信息，并据此做出授权决策。

### JWT 的结构

要创建这种令牌，我们需要一种方式来组织并序列化要携带的信息。JSON Web Token[^1] 格式（JWT[^2]
）提供了一种简洁的方法，用来承载令牌需要传递的各类信息。从本质上讲，JWT 是一个 JSON 对象，外面再封装一层，便于在网络上传输。最简单的
JWT 形式是未签名令牌，看起来大致如下：

[^1]: RFC 7519 [https://tools.ietf.org/html/rfc7519](https://datatracker.ietf.org/doc/html/rfc7519)

[^2]: 通常读作“jot”。

``` shell
eyJ0eXAiOiJKV1QiLCJhbGciOiJub25lIn0.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6Ikpva
  G4gRG9lIiwiYWRtaW4iOnRydWV9.
```

这看起来可能和我们之前用的那些 token 一样，也是一团随机的字符，但实际上这里面包含的信息要丰富得多。首先，注意有两段字符被单个句点分隔开。每一段都是
token 的不同组成部分；如果我们按点号把 token 字符串拆开，就可以分别对这些部分进行处理。（在我们这个示例里，最后一个点号后面其实还隐含着第三段，但我们会在
11.3 节再讲。）

``` shell
eyJ0eXAiOiJKV1QiLCJhbGciOiJub25lIn0
.
eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9
.
```

点与点之间的每一段内容都不是随机的，而是一个经过 Base64URL 编码的 JSON 对象。[^3] 如果我们对第一部分进行 Base64 解码并解析其中的
JSON 对象，就会得到一个简单的对象。

[^3]: 具体来说，就是使用 URL 安全字符集、且不带填充字符的 Base64 编码。

``` json
{
  "typ": "JWT",
  "alg": "none"
}
```

!!! note "为什么要用 Base64？"

	为什么我们要费这么大劲把内容编码成 Base64？毕竟它对人并不直观可读，还得多走几道处理流程才能理解。直接用 JSON 不好吗？答案的一部分在于 JWT 通常会出现在哪里：HTTP Header、Query 参数、表单参数，以及各种数据库和编程语言里的字符串字段。这些位置往往只能在不做额外编码的情况下使用一小部分字符集。举例来说，要通过 HTTP 表单参数传一个 JSON 对象，左花括号 { 和右花括号 } 就得分别编码成 %7B 和 %7D。引号、冒号以及其他常见字符也都需要编码成对应的实体编码。甚至连空格这种再常见不过的字符，根据 Token 所在位置不同，也可能需要编码成 %20 或 +。另外，很多场景下，用于编码的 % 本身也得再编码，结果常常导致值被不小心二次编码。

	JWT 原生采用 Base64URL 编码方案后，就可以在这些常见位置安全传递，而无需再做额外编码。更进一步，因为 JSON 对象是以编码后的字符串形式传输的，它也更不容易被中间件处理后再序列化——这一点在下一节会看到很关键。这种“抗传输折腾”的防护层对部署方和开发者都很有吸引力，也正因如此，JWT 才能在其他安全令牌格式屡屡受挫的地方站稳脚跟。

这个头部始终是一个 JSON 对象，用来描述令牌其余部分的相关信息。`typ` 头字段会告诉处理令牌其余部分的应用，在第二段（也就是
payload）里应该期待什么。在我们的示例中，它表明这是一个 JWT。虽然也有其他数据容器会使用同样的结构，但 JWT 绝对是最常见的，并且作为
OAuth 令牌也最符合我们的需求。头部还包含 `alg` 字段，并设置为特殊值 `none`，用于表示这是一个未签名的令牌。

第二段是令牌本身的 payload（载荷），它的序列化方式与头部相同：对 JSON 进行 Base64URL 编码。由于这是一个 JWT，payload 可以是任意
JSON 对象；在我们之前的示例里，它只是一个简单的用户数据集合。

```json
{
  "sub": "1234567890",
  "name": "John Doe",
  "admin": true
}
```

### JWT 声明

除了通用的数据结构之外，JWT 还提供了一组可在不同应用之间通用的声明（claim）。尽管 JWT 可以包含任何合法的 JSON
数据，但这些声明为涉及此类令牌的常见操作提供了支持。JWT 中的这些字段都是可选的，不过具体服务可以自行规定必须包含哪些字段（见表
11.1）。

我们也可以根据应用的实际需要添加额外字段。在前面的示例令牌中，我们在负载（payload）里加入了 name 和 admin 字段：name
用于展示用户名称，admin 则是一个布尔字段，用来标识该用户是否为管理员。这些字段的取值可以是任何合法的 JSON
值，包括字符串、数字、数组，甚至是其他对象。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260105155803497.png){ loading=lazy }
  <figcaption>表 11.1 标准 JSON Web Token（JWT）声明</figcaption>
</figure>

这些字段的名称可以是任何合法的 JSON 字符串——和其他任何 JSON 对象一样。不过，JWT 规范[^4]确实给出了一些建议，用于避免不同
JWT 实现之间发生命名冲突。尤其当 JWT 需要跨安全域被消费时，这些建议会非常有价值，因为不同的声明可能会被定义出来，甚至可能具有不同的语义。

[^4]: RFC 7519 [https://tools.ietf.org/html/rfc7519](https://datatracker.ietf.org/doc/html/rfc7519)

### 在我们的服务器中实现 JWT

让我们为授权服务器加入 JWT 支持。打开 ch-11-ex-1，编辑 authorizationServer.js 文件。在第 5
章中，我们实现了一个服务器，用来签发非结构化的随机令牌。这里我们将改造该服务器，使其生成不带签名、但符合 JWT
格式的令牌。尽管在实际项目中我们建议使用 JWT 库，但为了让你直观了解这些令牌包含哪些内容，我们会手动构造 JWT。下一节你会进一步体验如何使用
JWT 库。

首先，找到生成令牌本身的那段代码。本节的所有修改都会在这里完成，我们先从把下面这一行注释掉（或直接删除）开始：

``` javascript
var access_token = randomstring.generate();
```

要创建我们的 JWT，首先需要一个 Header。和上一个示例 Token 一样，我们会标明这个 Token 是 JWT，并且未签名。由于服务端发出的每个
Token 都具有相同的特性，这里可以直接使用一个静态对象。

``` javascript
var header = { 'typ': 'JWT', 'alg': 'none' };
```

接下来，我们要创建一个对象来承载 JWT 的 payload，并根据我们在令牌中关心的内容为其各字段赋值。我们会为每个令牌设置相同的签发者（issuer），即授权服务器的
URL；如果存在，则使用授权页面中的 user 变量作为令牌的主题（subject）。同时，将令牌的受众（audience）设置为受保护资源的
URL。我们会记录令牌的签发时间戳，并将过期时间设置为五分钟之后。需要注意的是，JavaScript 原生处理的时间戳单位是毫秒，而 JWT
规范要求所有时间字段使用秒。因此，在与原生时间值相互转换时，需要考虑 1000
的换算因子。最后，我们会为令牌加入一个随机标识符（identifier），使用的仍是最初生成整个令牌值时那套随机字符串生成函数。综合起来，创建
payload 的代码如下：

``` javascript
var payload = {
  iss: 'http://localhost:9001/',
  sub: code.user ? code.user.sub : undefined,
  aud: 'http://localhost:9002/',
  iat: Math.floor(Date.now() / 1000),
  exp: Math.floor(Date.now() / 1000) + (5 * 60),
  jti: randomstring.generate(8)
};
```

这会返回一个大致如下的对象，当然时间戳和随机字符串会不一样：

``` json
{
  "iss": "http://localhost:9001/",
  "sub": "alice",
  "aud": "http://localhost:/9002/",
  "iat": 1440538696,
  "exp": 1440538996,
  "jti": "Sl66JdkQ"
}
```

接下来，我们把 header 和 payload 对象拿出来，将其序列化为 JSON 字符串，用 Base64URL 对该字符串进行编码，并用句点作为分隔符把它们拼接起来即可。序列化
JSON 对象时无需做任何特殊处理——不需要特殊的格式化，也不要求字段顺序固定；使用任意标准的 JSON 序列化函数都可以。

``` javascript
var access_token = base64url.encode(JSON.stringify(header))
  + '.'
  + base64url.encode(JSON.stringify(payload))
  + '.';
```

现在我们的 access_token 值看起来类似于这样一个未签名的 JWT：

``` shell
eyJ0eXAiOiJKV1QiLCJhbGciOiJub25lIn0.eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjkwMDEvI
   iwic3ViIjoiOVhFMy1KSTM0LTAwMTMyQSIsImF1ZCI6Imh0dHA6Ly9sb2NhbGhvc3Q6OTAwMi
   8iLCJpYXQiOjE0NjcyNDk3NzQsImV4cCI6MTQ2NzI1MDA3NCwianRpIjoiMFgyd2lQanUifQ.
```

注意，我们的令牌现在带上了过期时间，但客户端并不需要为此做任何特殊处理。客户端可以一直使用这个令牌，直到它失效为止；一旦用不了了，客户端就像平时一样再去获取一个新的令牌。授权服务器可以在令牌响应中通过
`expires_in` 字段向客户端提供一个过期时间提示，但客户端同样不一定要用到这个信息——而且大多数客户端确实也不会处理它。

现在，我们该让受保护资源直接从传入的令牌里读取信息，而不是再去数据库里查令牌值了。打开 `protected-Resource.js`
，找到处理入站令牌的那段代码。首先需要解析令牌，做的事情与授权服务器生成它时的步骤正好相反：用点号把它拆分成不同的段。然后将第二段（payload）从
Base64 URL 解码，并把解码结果解析成一个 JSON 对象。

```javascript
var tokenParts = inToken.split('.');
var payload = JSON.parse(base64url.decode(tokenParts[1]));
```

这为我们提供了一个可在应用中直接校验的原生数据结构。我们会确保该令牌来自预期的签发方（issuer），其时间戳落在合理的范围内，并且我们的资源服务器确实是该令牌的目标受众（audience）。尽管这类校验通常会用布尔逻辑串联在一起，我们还是将它们拆成了独立的
if 语句，这样每一项检查都能更清晰、更独立地阅读和理解。

```javascript
if (payload.iss == 'http://localhost:9001/') {
    if ((Array.isArray(payload.aud) && __.contains(payload.aud, 'http://localhost:9002/')) ||
        payload.aud == 'http://localhost:9002/') {
        var now = Math.floor(Date.now() / 1000);
        if (payload.iat <= now) {
            if (payload.exp >= now) {
                req.access_token = payload;
            }
        }
    }
}
```

如果这些检查都通过了，我们就会把令牌解析后的载荷交给应用的其余部分；应用可以（如有需要）根据 subject
等字段来做授权决策。这相当于在上一版应用中，从授权服务器的数据库里把它存储的数据加载出来。

记住，JWT 的载荷是一个 JSON
对象，我们的受保护资源现在可以直接从请求对象中访问它。接下来就由其他处理函数来决定，这个令牌是否足以支撑当前这些请求——就像以前令牌存放在共享数据库里时我们做的那样。我们示例里令牌正文包含的属性信息并不算多，但我们完全可以很容易地把客户端、资源所有者、scope，或其他与受保护资源决策相关的信息放进去。

尽管签发出来的令牌已经和以前不一样了，我们却完全不需要改动任何客户端代码。这都得益于令牌对客户端而言是“不透明”的——这是
OAuth 2.0 中一个至关重要的简化因素。事实上，授权服务器完全可以选择很多不同的令牌格式，而客户端软件无需做任何更改。

现在我们确实可以把信息直接带在令牌里了，但这就够了吗？

## 令牌的密码学保护：JSON 对象签名与加密（JOSE）

说到这里，作为作者，我们得坦白：我们刚刚让你做了一件非常不安全的事。你可能已经注意到这个关键遗漏了，并且在想我们是不是疯了。我们漏掉了什么？简单来说，如果授权服务器输出的令牌没有任何保护，而受保护资源又在不做其他校验的情况下就信任令牌里的内容，那么客户端拿到明文令牌后，在把它提交给受保护资源之前，篡改令牌内容就会变得轻而易举。客户端甚至可以凭空伪造一个令牌，完全不需要与授权服务器交互；而一个天真的资源服务器会直接接受并处理它。

我们显然不希望这种事发生，因此应该为这个令牌加上一些保护。好在已经有一整套规范明确告诉我们该怎么做：JSON 对象签名与加密标准（JSON
Object Signing and Encryption，[^5]），简称 JOSE。[^6] 这套规范基于 JSON 作为底层数据模型，提供签名（JSON Web
Signature，JWS）、加密（JSON Web Encryption，JWE），甚至还包括密钥存储格式（JSON Web Key，JWK）。上一节里我们手工构造的那个未签名
JWT，其实只是一个未签名 JWS 对象的特殊形式——载荷采用 JSON。JOSE 的细节完全可以单独写成一本书；这里我们只看两种常见场景：使用
HMAC 签名方案进行对称签名与校验，以及使用 RSA 签名方案进行非对称签名与校验。我们还会用 JWK 来存储 RSA 的公钥和私钥。

[^5]: JWS: RFC 7515 [https://tools.ietf.org/html/rfc7515](https://datatracker.ietf.org/doc/html/rfc7515); JWE: RFC
7516 [https://tools.ietf.org/html/rfc7516](https://datatracker.ietf.org/doc/html/rfc7516); JWK: RFC
7517 [https://tools.ietf.org/html/rfc7517](https://datatracker.ietf.org/doc/html/rfc7517); JWA: RFC
7518 [https://tools.ietf.org/html/rfc7518](https://datatracker.ietf.org/doc/html/rfc7518)

[^6]: 发音意在与西班牙语名 José 相同，即“ho-zay”。

为了完成繁重的密码学工作，我们将使用一个名为 JSRSASign 的 JOSE 库。该库提供基础的签名与密钥管理能力，但不提供加密功能。加密令牌就留给读者作为练习。

### 使用 HS256 的对称签名

接下来的练习中，我们会在授权服务器端使用共享密钥对令牌进行签名，然后在受保护资源端用同一共享密钥来校验该令牌。当授权服务器与受保护资源之间的关系足够紧密，可以长期共享一个密钥（类似
API Key），但二者又没有直接连接、无法逐个令牌实时验证时，这是一种很实用的方案。

打开 ch-11-ex-2，并编辑本练习所需的 authorizationServer.js 和 protected-Resource.js 文件。首先，我们要在授权服务器中添加一个共享密钥。在文件靠上的位置，找到
sharedTokenSecret 的变量定义，可以看到我们把它设置为一个秘密字符串。在生产环境中，这个密钥通常会通过某种凭据管理流程进行管理，其值也不太可能这么短、这么容易输入；但为了便于练习，我们在这里做了简化。

``` javascript
var sharedTokenSecret = 'shared OAuth token secret!';
```

现在我们要用那个密钥来给 Token 签名。我们的代码结构和上一个练习一样：先生成一个未签名的 Token，所以请先找到生成 Token
的那段代码继续往下做。首先需要修改 Header 参数，表明我们使用的是 HS256 签名算法。

``` javascript
var header = { 'typ': 'JWT', 'alg': 'HS256'};
```

我们的 JOSE 库要求在把数据传入签名函数之前先完成 JSON 序列化（但不需要做 Base64 URL
编码），不过这部分我们已经配置好了。这一次，我们不再用点号把字符串拼接起来，而是使用 JOSE 库，基于共享密钥对令牌应用 HMAC
签名算法。由于所选 JOSE 库的一个“怪癖”，我们需要把共享密钥以十六进制字符串的形式传入；其他库在密钥格式的处理要求上可能各不相同。该库的输出会是一个字符串，我们将用它作为令牌的值。

``` javascript
var access_token = jose.jws.JWS.sign(header.alg,
  JSON.stringify(header),
  JSON.stringify(payload),
  Buffer.from(sharedTokenSecret).toString('hex'));
```

最终生成的 JWT 大致如下：

``` shell
eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjkwMDEv
   Iiwic3ViIjoiOVhFMy1KSTM0LTAwMTMyQSIsImF1ZCI6Imh0dHA6Ly9sb2NhbGhvc3Q6OTAwMi
   8iLCJpYXQiOjE0NjcyNTEwNzMsImV4cCI6MTQ2NzI1MTM3MywianRpIjoiaEZLUUpSNmUifQ.
   WqRsY03pYwuJTx-9pDQXftkcj7YbRn95o-16NHrVugg
```

头部（header）和载荷（payload）仍然与之前一样，都是经过 Base64URL 编码的 JSON 字符串。签名（signature）位于 JWT 格式中最后一个点号之后，以
Base64URL 编码的字节序列形式呈现，因此带签名的 JWT 整体结构为 header.payload.signature。把各部分按点号拆开后，结构会更直观一些。

``` shell
eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9
.
eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjkwMDEvIiwic3ViIjoiOVhFMy1KSTM0LTAwMTMyQSIs
   ImF1ZCI6Imh0dHA6Ly9sb2NhbGhvc3Q6OTAwMi8iLCJpYXQiOjE0NjcyNTEwNzMsImV4cCI6MT
   Q2NzI1MTM3MywianRpIjoiaEZLUUpSNmUifQ
.
WqRsY03pYwuJTx-9pDQXftkcj7YbRn95o-16NHrVugg
```

现在你可以看到，我们的无签名 JWT，其实就是签名段为空（缺失）的情况。服务器的其余部分保持不变，因为我们仍然会把 token
存在数据库里。不过，如果愿意的话，我们完全可以取消授权服务器端对 token 的存储需求，因为服务器可以通过签名来识别该 token。

同样，客户端并不知道 token 的格式已经发生了变化。不过，我们需要修改受保护资源，使其能够校验 token 的签名。为此，打开
protectedResource.js，注意文件顶部同样有一段随机的密钥字符串。同样地，在生产环境中，这通常会通过密钥管理流程来处理，密钥也不太可能像这样简单到可以直接手动输入。

``` javascript
var sharedTokenSecret = 'shared OAuth token secret!';
```

首先我们需要解析这个 token，不过这和上次差不多。

```javascript
var tokenParts = inToken.split('.');
var header = JSON.parse(base64url.decode(tokenParts[0]));
var payload = JSON.parse(base64url.decode(tokenParts[1]));
```

注意，这次我们会使用 token 的 header。接下来，基于我们共享的密钥验证签名，这将是我们对 token
内容的第一道校验。别忘了，我们的库在进行校验之前，需要先把密钥转换成十六进制格式。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260105173023101.png){ loading=lazy }
  <figcaption></figcaption>
</figure>

请特别注意，我们传入的 token 字符串与它在网络上传到我们这里时的样子完全一致。我们没有使用解码或解析后的 JSON
对象，也没有自行重新编码。要是我们做了其中任何一步，JSON 的序列化结果就很可能（而且完全合法）会出现些许差异，比如增减空格和缩进，或者调整数据对象中成员的顺序。正如我们讨论过的，JOSE
规范实际上是在传输过程中为 token 提供了一层“防护装甲”，目的就是让我们在无需任何重新规范化处理的情况下完成这一步校验。

只有在签名有效时，我们才会解析 JWT，并检查其内容是否一致。若所有检查都通过，就可以像之前一样把它交给应用处理。现在，资源服务器只会接受使用它与授权服务器共享的密钥签名过的
token。要验证这一点，可以在授权服务器或受保护资源的代码里修改 secret，让两边的值不一致。资源服务器应当会拒绝由此产生的
token。

### 使用 RS256 的非对称签名

在本节的练习中，我们会像上一节那样再次用密钥对 token
进行签名。但这一次，我们将使用公钥密码学来完成。使用共享密钥时，两个系统在创建或验证签名时都需要同一把密钥。这实际上意味着，在上一练习中，授权服务器和资源服务器都可能生成
token，因为它们都能访问到所需的密钥材料。而在公钥密码学体系下，授权服务器同时持有私钥和公钥，可用于生成
token；受保护资源只需要能够获取授权服务器的公钥来验证 token。与共享密钥不同的是，即便受保护资源可以轻松验证
token，它也无法生成属于自己的有效 token。我们将使用 JOSE 中的 RS256 签名方法，其底层使用的是 RSA 算法。

打开 ch-11-ex-3，从 authorizationServer.js 文件开始。首先，我们需要在授权服务器中加入一对公私钥。我们的密钥对是 2048 位 RSA
密钥，这是推荐的最小长度。本练习使用以 JSON 为基础的 JWK 格式存储密钥，库可以原生读取。为了避免你必须按书里原样输入这一长串复杂字符，我们已经把它直接放进代码里了，去看看即可。

``` javascript
var rsaKey = {
  "alg": "RS256",
  "d": "ZXFizvaQ0RzWRbMExStaS_-yVnjtSQ9YslYQF1kkuIoTwFuiEQ2OywBfuyXhTvVQxIiJq
     PNnUyZR6kXAhyj__wS_Px1EH8zv7BHVt1N5TjJGlubt1dhAFCZQmgz0D-PfmATdf6KLL4HIijG
     rE8iYOPYIPF_FL8ddaxx5rsziRRnkRMX_fIHxuSQVCe401hSS3QBZOgwVdWEb1JuODT7KUk7xPp
     MTw5RYCeUoCYTRQ_KO8_NQMURi3GLvbgQGQgk7fmDcug3MwutmWbpe58GoSCkmExUS0U-KEkH
     tFiC8L6fN2jXh1whPeRCa9eoIK8nsIY05gnLKxXTn5-aPQzSy6Q",
  "e": "AQAB",
  "n": "p8eP5gL1H_H9UNzCuQS-vNRVz3NWxZTHYk1tG9VpkfFjWNKG3MFTNZJ1l5g_COMm2_2i_
     YhQNH8MJ_nQ4exKMXrWJB4tyVZohovUxfw-eLgu1XQ8oYcVYW8ym6Um-BkqwwWL6CXZ70X81
     YyIMrnsGTyTV6M8gBPun8g2L8KbDbXR1lDfOOWiZ2ss1CRLrmNM-GRp3Gj-ECG7_3Nx9n_s5
     to2ZtwJ1GS1maGjrSZ9GRAYLrHhndrL_8ie_9DS2T-ML7QNQtNkg2RvLv4f0dpjRYI23djxV
     tAylYK4oiT_uEMgSkc4dxwKwGuBxSO0g9JOobgfy0--FUHHYtRi0dOFZw",
  "kty": "RSA",
  "kid": "authserver"
};
```

这对密钥是随机生成的。在生产环境中，你通常需要为每个服务配置一把独立的密钥。作为额外练习，可以使用 JOSE 库生成你自己的
JWK，并替换掉这里代码中的那一份。

接下来，我们需要用私钥对 Token 进行签名。这个过程与我们之前处理共享密钥的方式类似，我们仍然会在 Token 生成函数中操作。首先，需要声明该
Token 使用 RS256 算法进行签名。我们还会声明使用来自授权服务器、key ID（kid）为 -authserver
的那把密钥。授权服务器目前可能只有一把密钥，但如果你将来把其他密钥也加入这个集合，就需要让资源服务器能够识别你实际使用的是哪一把。

``` javascript
var header = { 'typ': 'JWT', 'alg': rsaKey.alg, 'kid': rsaKey.kid };
```

接下来，我们需要把 JWK 格式的密钥对转换成库能够用于加密操作的形式。好在这个库提供了一个简单的工具函数来完成转换。[^7]
然后，我们就可以用这把密钥对令牌进行签名。

[^7]: 其他库和其他平台可能需要根据 JWK 的不同部分创建密钥对象。

``` javascript
var privateKey = jose.KEYUTIL.getKey(rsaKey);
```

接下来，我们会像之前一样生成访问令牌字符串，只不过这一次会使用我们的私钥，并采用 RS256 非对称签名算法。

``` javascript
var access_token = jose.jws.JWS.sign(header.alg,
  JSON.stringify(header),
  JSON.stringify(payload),
  privateKey);
```

结果是一个与之前类似的令牌，但现在已经使用非对称方式进行了签名。

``` shell
eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6ImF1dGhzZXJ2ZXIifQ.eyJpc3MiOiJodH
   RwOi8vbG9jYWxob3N0OjkwMDEvIiwic3ViIjoiOVhFMy1KSTM0LTAwMTMyQSIsImF1ZCI6Imh
   0dHA6Ly9sb2NhbGhvc3Q6OTAwMi8iLCJpYXQiOjE0NjcyNTE5NjksImV4cCI6MTQ2NzI1MjI2
   OSwianRpIjoidURYMWNwVnYifQ.nK-tYidfd6IHW8iwJ1ZHcPPnbDdbjnveunKrpOihEb0JD5w
   fjXoYjpToXKfaSFPdpgbhy4ocnRAfKfX6tQfJuFQpZpKmtFG8OVtWpiOYlH4Ecoh3soSkaQyIy
   4L6p8o3gmgl9iyjLQj4B7Anfe6rwQlIQi79WTQwE9bd3tgqic5cPBFtPLqRJQluvjZerkSdUo
   7Kt8XdyGyfTAiyrsWoD1H0WGJm6IodTmSUOH7L08k-mGhUHmSkOgwGddrxLwLcMWWQ6ohmXa
   Vv_Vf-9yTC2STHOKuuUm2w_cRE1sF7JryiO7aFRa8JGEoUff2moaEuLG88weOT_S2EQBhYB
   0vQ8A
```

Header 和 Payload 仍然是经过 Base64URL 编码的 JSON，Signature 则是经过 Base64URL 编码的字节数组。由于使用了 RSA
算法，Signature 现在会长得多。

``` javascript
eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6ImF1dGhzZXJ2ZXIifQ
.
eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjkwMDEvIiwic3ViIjoiOVhFMy1KSTM0L
   TAwMTMyQSIsImF1ZCI6Imh0dHA6Ly9sb2NhbGhvc3Q6OTAwMi8iLCJpYXQiOjE0
   NjcyNTE5NjksImV4cCI6MTQ2NzI1MjI2OSwianRpIjoidURYMWNwVnYifQ
.
nK-tYidfd6IHW8iwJ1ZHcPPnbDdbjnveunKrpOihEb0JD5wfjXoYjpToXKfaSFPdpgbhy4ocnRAfKfX6tQfJuFQpZpKmtFG8OVtWpiOYlH4Ecoh3soSkaQyIy4L6p8o3gmgl9iyjLQj4B7Anfe6rwQlI
   Qi79WTQwE9bd3tgqic5cPBFtPLqRJQluvjZerkSdUo7Kt8XdyGyfTAiyrsWoD1H0WGJm6IodTmS
   UOH7L08k-mGhUHmSkOgwGddrxLwLcMWWQ6ohmXaVv_Vf-9yTC2STHOKuuUm2w_cRE1sF7JryiO7
   aFRa8JGEoUff2moaEuLG88weOT_S2EQBhYB0vQ8A
```

客户端依然无需做任何改动，但我们确实需要告诉受保护资源如何验证这个新 JWT 的签名。打开
protectedResource.js，把服务器的公钥配置进去。同样地，为了免去你费劲手动誊写密钥信息的麻烦，我们已经提前把它放在文件里了。

``` javascript
var rsaKey = {
  "alg": "RS256",
  "e": "AQAB",
  "n": "p8eP5gL1H_H9UNzCuQS-vNRVz3NWxZTHYk1tG9VpkfFjWNKG3MFTNZJ1l5g_COMm2_2i_
     YhQNH8MJ_nQ4exKMXrWJB4tyVZohovUxfw-eLgu1XQ8oYcVYW8ym6Um-BkqwwWL6CXZ70X81
     YyIMrnsGTyTV6M8gBPun8g2L8KbDbXR1lDfOOWiZ2ss1CRLrmNM-GRp3Gj-ECG7_3Nx9n_s5
     to2ZtwJ1GS1maGjrSZ9GRAYLrHhndrL_8ie_9DS2T-ML7QNQtNkg2RvLv4f0dpjRYI23djxV
     tAylYK4oiT_uEMgSkc4dxwKwGuBxSO0g9JOobgfy0--FUHHYtRi0dOFZw",
  "kty": "RSA",
  "kid": "authserver"
};
```

这份数据与授权服务器中的那对密钥属于同一密钥对，但不包含私钥信息（在 RSA 密钥中由 d 参数表示）。因此，受保护资源只能验证传入的已签名
JWT，无法生成这些 JWT。

!!! note "我得把密钥到处拷贝一遍吗？  "

	你可能会觉得，在这样的软件组件之间来回复制签名密钥和验签密钥很麻烦——确实如此。只要授权服务器决定更新密钥，所有下游受保护资源中保存的对应公钥副本都必须同步更新。对于一个规模庞大的 OAuth 生态来说，这会带来不少问题。

	一种常见的做法（OpenID Connect 协议就采用了这种方式，我们会在第 13 章介绍）是让授权服务器在一个固定的 URL 上发布它的公钥。通常会以 JWK Set 的形式提供，其中可以包含多个密钥，大致如下所示。

	``` json
	{
	  "keys": [
	    {
	      "alg": "RS256",
	      "e": "AQAB",
	     "n": "p8eP5gL1H_H9UNzCuQS-vNRVz3NWxZTHYk1tG9VpkfFjWNKG3MFTNZJ1l5g_
	COMm2_2i_YhQNH8MJ_nQ4exKMXrWJB4tyVZohovUxfw-eLgu1XQ8oYcVYW8ym6Um-Bkqww
	WL6CXZ70X81YyIMrnsGTyTV6M8gBPun8g2L8KbDbXR1lDfOOWiZ2ss1CRLrmNM-GRp3Gj-
	ECG7_3Nx9n_s5to2ZtwJ1GS1maGjrSZ9GRAYLrHhndrL_8ie_9DS2T-ML7QNQtNkg2RvLv4f
	0dpjRYI23djxVtAylYK4oiT_uEMgSkc4dxwKwGuBxSO0g9JOobgfy0--FUHHYtRi0dOFZw",
	      "kty": "RSA",
	      "kid": "authserver"
	    }
	  ]
	}
	```

	受保护资源随后可以按需获取并缓存该密钥。这种方式使授权服务器能够在需要时随时轮换密钥，或随着时间推移新增密钥，而这些变更会自动在整个网络中同步生效。

	作为一个额外练习，将服务器修改为以 JWK Set 的形式发布其公钥，并将受保护资源修改为在需要时通过网络拉取该密钥。务必格外小心：授权服务器只能发布公钥，绝不能把私钥也一并发布出去！

现在，我们将使用我们的库，基于服务器的公钥来验证传入 Token 的签名。先把公钥加载成库可用的对象，然后用该密钥校验 Token 的签名。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260105175435414.png){ loading=lazy }
  <figcaption></figcaption>
</figure>

我们仍然需要对该令牌执行与处理未签名令牌时相同的所有校验。随后，payload
对象会再次交由应用程序的其余部分处理，让它自行决定：所提交的令牌是否足以满足当前请求。完成这些设置后，授权服务器还可以选择为受保护资源的使用提供额外信息，例如
scope 或客户端标识符。作为一个扩展练习，你可以通过自定义 JWT Claim 加入其中一些信息，并让受保护资源读取并使用这些值。

### 其他令牌保护方案

我们在这些练习中讲到的方法，并不是基于 JOSE 保护令牌内容的唯一手段。比如，我们之前使用了 HS256 对称签名算法，它会对令牌内容生成一个
256 字节的哈希值。JOSE 还定义了 HS384 和 HS512：它们使用更大的哈希值，以更长的令牌签名为代价换取更高的安全性。类似地，我们还使用了
RS256 非对称签名算法，它会对 RSA 签名输出生成一个 256 字节的哈希值。JOSE 同样定义了 RS384 和
RS512，与对称算法类似，也存在安全性与签名大小之间的权衡。此外，JOSE 还定义了 PS256、PS384 和 PS512 签名算法，它们基于另一种
RSA 签名与哈希机制。

JOSE 也支持椭圆曲线，核心标准中引用了三条曲线及其对应哈希，分别对应 ES256、ES384 和 ES512。与 RSA
相比，椭圆曲线密码学有多项优势，包括更小的签名体积，以及更低的校验计算开销；但在本文写作时，对底层加密函数的支持还远不如 RSA
普及。除此之外，JOSE 的算法列表还可以通过新规范进行扩展，使得新的算法可以在被发明并产生需求时被定义并纳入。

不过，有时候仅靠签名还不够。对于只做了签名的令牌，客户端可能会窥探令牌本身，获知一些本不该有权限知道的信息，例如 sub
字段中的用户标识。好消息是，除了签名机制之外，JOSE 还提供了一套名为 JWE 的加密机制，支持多种选项与算法。与三段式结构不同，使用
JWE 加密后的 JWT 是五段式结构。每一段仍然使用 Base64 URL 编码，但此时 payload 变成了加密对象，没有相应的密钥就无法读取。本章完整讲解
JWE 流程会有些超出篇幅；不过作为进阶练习，你可以尝试为令牌加入 JWE。首先，为资源服务器生成一对密钥，并让授权服务器能够访问这对密钥中的公钥部分。然后使用该公钥通过
JWE 加密令牌内容。最后，让资源服务器使用自己的私钥解密令牌内容，并将令牌的 payload 传递给应用程序。

!!! note "了解 COSE[^8]"

	一种正在兴起的标准——CBOR 对象签名与加密（COSE）——提供了与 JOSE 大体相同的功能，但它是基于简明二进制对象表示（CBOR）这种数据序列化格式。顾名思义，CBOR 是一种非人类可读的二进制格式，专为对空间极其敏感的环境而设计。它的底层数据模型以 JSON 为基础，JSON 能表达的内容都可以很容易地转换为 CBOR。COSE 规范试图在 CBOR 领域复现 JOSE 之于 JSON 的作用，这意味着在不久的将来，它很可能会成为紧凑型、类似 JWT 的令牌的一种可行选择。

[^8]: 发音为“cozy”（读作 /ˈkoʊzi/），就像短语 “a cozy couch” 里的“cozy”。

## 在线查询令牌信息：令牌内省

把令牌相关信息直接打包进令牌本身确实有一些缺点。随着令牌承载了所有必需的声明（claim）以及用于保护这些声明的加密结构，令牌体积往往会变得相当大。此外，如果受保护资源完全依赖令牌自身携带的信息，那么一旦令牌被创建并发放出去，想要撤销仍处于有效期内的令牌就会变得极其困难，几乎不可行。

### 内省协议

OAuth 令牌内省（Token Introspection）协议[^9] 定义了一种机制，使受保护资源可以主动向授权服务器查询令牌的状态。由于令牌是由授权服务器签发的，它天然最了解该令牌所代表的授权委派背后的上下文与细节。

[^9]: RFC 7662 [https://tools.ietf.org/html/rfc7662](https://datatracker.ietf.org/doc/html/rfc7662)

该协议是在 OAuth 基础上的一种简单扩展。授权服务器向客户端签发令牌，客户端将令牌出示给受保护资源，而受保护资源则在授权服务器处对该令牌进行自省（见图
11.2）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260105175839998.png){ loading=lazy }
  <figcaption>图 11.2  将受保护资源连接到授权服务器</figcaption>
</figure>

自省请求（introspection request）是一种采用表单编码（form-encoded）的 HTTP 请求，发送到授权服务器的自省端点（introspection
endpoint）。它让受保护资源（protected
resource）可以向授权服务器发问：“有人把这个令牌给了我；它能用来做什么？”受保护资源在该请求过程中需要对自身进行认证，这样授权服务器就能知道是谁在提问，并且可能会根据提问方的不同返回不同的响应。自省规范并不规定受保护资源必须以何种方式完成自我认证，只要求它必须进行认证。在我们的示例中，受保护资源通过
HTTP Basic 携带 ID 和密钥（secret）进行认证，这与 OAuth 客户端在令牌端点（token endpoint）进行自我认证的方式非常相似。也可以通过单独的访问令牌（access
token）来完成认证——第 14 章讨论的 UMA 协议就是这么做的。

``` shell
POST /introspect HTTP/1.1
Host: localhost:9001
Accept: application/json
Content-type: application/x-www-form-encoded
Authorization: Basic cHJvdGVjdGVkLXJlc291cmNlLTE6cHJvdGVjdGVkLXJlc291cmNlLXNlY3JldC0x

token=987tghjkiu6trfghjuytrghj
```

内省（Introspection）响应是一个用于描述令牌的 JSON 文档。其内容与 JWT 的负载（payload）类似，任何有效的 JWT
声明（claim）都可以作为响应的一部分使用。

``` shell
HTTP 200 OK
Content-type: application/json

{
  "active": true,
  "scope": "foo bar baz",
  "client_id": "oauth-client-1",
  "username": "alice",
  "iss": "http://localhost:9001/",
  "sub": "alice",
  "aud": "http://localhost:/9002/",
  "iat": 1440538696,
  "exp": 1440538996,
}
```

内省规范除了 JWT 已定义的声明之外，还额外定义了若干声明，其中最重要的是 active
声明。该声明用于告知受保护资源：当前令牌在授权服务器上是否处于有效状态，也是唯一一个必须返回的声明。由于 OAuth
令牌的部署方式多种多样，“有效令牌”并不存在统一的定义。总体而言，这通常意味着：令牌由该授权服务器签发、尚未过期、未被吊销，并且发起请求的受保护资源被允许获取该令牌的信息。有意思的是，这类信息无法直接写入令牌本身，因为不会有任何令牌会声明自己是“非有效”的。

内省响应还可以包含令牌的 scope，形式与最初的 OAuth 请求一致，为以空格分隔的 scope 字符串列表。正如我们在第 4 章看到的，令牌的
scope 使受保护资源能够以更细粒度的方式判断资源所有者授予客户端的权限。最后，响应中也可以包含客户端与用户的相关信息。将这些信息结合起来，受保护资源就能获得一组更丰富的数据，用于做出最终的授权决策。

使用令牌内省会带来额外的开销，主要体现在 OAuth
系统的网络流量增加。为缓解这一点，受保护资源可以针对某个令牌缓存内省调用的结果。建议缓存的存活时间相对于令牌的预期生命周期要更短，以降低在缓存生效期间令牌被吊销的风险。

### 构建内省端点

接下来我们将在应用中加入内省支持。打开 ch-11-ex-4，从 authorizationServer.js
开始，我们将在这里实现内省端点。首先，我们会为受保护资源添加一组凭据，使其能够向内省端点进行认证。

``` javascript
var protectedResources = [
  {
      "resource_id": "protected-resource-1",
      "resource_secret": "protected-resource-secret-1"
  }
];
```

我们刻意参照客户端认证来建模，因为在 Introspection 规范中，受保护资源的认证方式默认选项之一就是客户端认证。我们还加入了一个
getProtectedResource 函数，用来对应第 5 章里创建的 getClient 函数。

``` javascript
var getProtectedResource = function(resourceId) {
  return __.find(protectedResources, function(protectedResource) { return protectedResource.resource_id == resourceId; });
};
```

我们会在授权服务器上把令牌自省端点部署在 /introspect，并且它将接收 POST 请求。

``` javascript
app.post('/introspect', function(req, res) {

});
```

我们的受保护资源将使用 HTTP Basic 认证和一个共享密钥进行身份验证，因此我们会像在令牌端点验证客户端凭据一样，从
Authorization 请求头中读取并校验这些信息。

``` javascript
var auth = req.headers['authorization'];
var resourceCredentials = decodeClientCredentials(auth);
var resourceId = resourceCredentials.id;
var resourceSecret = resourceCredentials.secret;
```

拿到提交的凭据后，先用我们的辅助函数查找对应资源，再确认密钥是否匹配。

``` javascript
var resource = getProtectedResource(resourceId);
if (!resource) {
  res.status(401).end();
  return;
}

if (resource.resource_secret != resourceSecret) {
  res.status(401).end();
  return;
}
```

现在我们需要在数据库里查询这个 token。如果能找到，我们会把关于该 token 的所有信息都补充到响应中，并以 JSON
对象的形式返回。如果找不到，就只返回一条通知，说明该 token 未激活。

``` javascript
var inToken = req.body.token;
console.log('Introspecting token %s', inToken);
nosql.one().make(function(builder) {
  builder.where('access_token', inToken);
  builder.callback(function(err, token) {
    if (token) {
      var introspectionResponse = {
        active: true,
        iss: 'http://localhost:9001/',
        aud: 'http://localhost:9002/',
        sub: token.user ? token.user.sub : undefined,
        username: token.user ? token.user.preferred_username : undefined,
        scope: token.scope ? token.scope.join(' ') : undefined,
        client_id: token.client_id 
      };
       res.status(200).json(introspectionResponse);
      return;
    } else {
      var introspectionResponse = {
        active: false
      };       res.status(200).json(introspectionResponse);
      return;
    }
  });
});
```

出于安全考虑，我们很重要的一点是：不要把令牌为何未处于有效状态的具体原因告诉受保护资源——无论是已过期、已被撤销，还是根本从未签发过——而是只需要说明它无效即可。否则，一旦受保护资源被攻陷，攻击者就可能利用它向授权服务器“探测”，套取与令牌相关的信息。对于合法的交易而言，令牌为什么无效最终并不重要，重要的只是它确实无效。

把这些内容串起来，我们的内省（introspection）端点如附录 B 的清单 11 所示。内省端点也应该能够查询刷新令牌（refresh
token），但我们把这部分额外功能留作读者练习。

### 令牌内省

既然我们已经有了可调用的内省端点，就需要让受保护资源去调用它。我们将继续上一节的同一个练习 ch-11-ex-4，不过这次打开
protectedResource.js 并进行编辑。首先，为受保护资源配置它的 ID 和密钥（secret），就像我们在第 5 章为客户端做的那样。

``` javascript
var protectedResource = {
  "resource_id": "protected-resource-1",
  "resource_secret": "protected-resource-secret-1"
};
```

接下来，在 getAccessToken 函数中，我们将调用 introspection 端点。这只是一次简单的 HTTP POST：把前面拿到的客户端 ID 和 secret
通过 HTTP Basic 认证传过去，同时将从客户端收到的 token 值作为表单参数提交。

``` javascript
var form_data = qs.stringify({
  token: inToken
});
var headers = {
  'Content-Type': 'application/x-www-form-urlencoded',
'Authorization': 'Basic ' + encodeClientCredentials(protectedResource.resource_id, protectedResource.resource_secret)
};

var tokRes = request('POST', authServer.introspectionEndpoint, {
  body: form_data,
  headers: headers
});
```

最后，将 introspection 端点返回的响应解析为 JSON 对象。如果 active 声明为 true，就把 introspection 调用的结果传递给应用程序的其余部分，供后续处理。

``` javascript
if (tokRes.statusCode >= 200 && tokRes.statusCode < 300) {
  var body = JSON.parse(tokRes.getBody());

  console.log('Got introspection response', body);
  var active = body.active;
  if (active) {
      req.access_token = body;
  }
}
```

从这里开始，受保护资源的服务函数将决定该令牌是否足以、或是否适用于当前的请求。

### 结合内省与 JWT

本章介绍了结构化令牌（尤其是 JWT）和令牌内省，作为在授权服务器与受保护资源之间传递信息的两种替代方案。看起来你似乎必须二选一，但实际上，它们完全可以配合使用，而且效果非常好。

JWT
可以用来承载核心信息，例如过期时间、唯一标识符以及签发方。这些信息是每个受保护资源都需要的，用于对令牌是否可信进行第一层校验。在此基础上，受保护资源可以再通过令牌内省获取更细致（且可能更敏感）的令牌信息，例如授权该令牌的用户、令牌签发给了哪个客户端，以及签发时包含了哪些
scope。

当受保护资源需要接收来自多个授权服务器的访问令牌时，这种做法尤其有用。受保护资源可以先解析
JWT，判断是哪个授权服务器签发了该令牌，然后再到对应的授权服务器对令牌进行内省，以获取更多信息。

!!! 令牌的状态

	对于客户端来说，它并不关心令牌是否被其他方撤销，因为 OAuth 客户端始终必须随时准备去重新获取一个新令牌。OAuth 协议不会根据令牌是被撤销、过期还是以其他方式失效来区分错误响应，因为客户端的处理方式永远是一样的。

	但对于受保护资源而言，明确知道令牌是否已被撤销就非常重要，因为接受一个已撤销的令牌会造成巨大的安全漏洞。而巨大的安全漏洞通常都不是什么好东西。如果受保护资源使用本地数据库查询或像内省这样的实时查询，就能轻松且快速地得知令牌是否已被撤销。但如果它使用的是 JWT 呢？

	由于 JWT 表面上是自包含的，因此常被视为无状态的。在不借助外部信号的情况下，无法向受保护资源表明该令牌已经被撤销。同样的问题也会出现在基于证书的公钥基础设施（PKI）中：只要所有签名都能匹配，证书就被视为有效。PKI 通过证书吊销列表（CRL）和在线证书状态协议（OCSP）来解决撤销问题，而这相当于 OAuth 世界里的令牌内省。

## 通过令牌撤销管理令牌生命周期

OAuth 令牌通常有一套可预测的生命周期：由授权服务器签发，被客户端使用，并由受保护资源进行校验。它们可能在到期后自动失效，也可能由资源所有者（或管理员）在授权服务器端撤销。正如我们已经看到的，OAuth
的核心规范提供了多种获取和使用令牌的机制。刷新令牌甚至允许客户端在访问令牌失效后申请新的访问令牌进行替换。在 11.2 和 11.3
节中，我们也介绍了如何使用 JWT 和令牌内省来帮助受保护资源验证令牌。不过，有时客户端明确知道自己不再需要某个令牌。那么它是否只能等令牌自然过期，或等别人来撤销它呢？

到目前为止，我们还没有看到一种机制能让客户端通知授权服务器撤销那些仍然有效的令牌，而这正是 OAuth 令牌撤销规范[^10]
要解决的问题。该规范允许客户端在自身侧触发事件发生时，主动管理令牌生命周期。例如，客户端可能是一个原生应用，正在从用户设备上被卸载；或者它提供了用户界面，让用户可以取消对客户端的配置/授权（deprovision）。又或者，客户端软件检测到可疑行为，希望将对其已获授权的受保护资源的潜在损害降到最低。无论触发事件是什么，令牌撤销规范都能让客户端向授权服务器发出信号：它所签发的这些令牌不应再被使用。

[^10]: RFC 7009 [https://tools.ietf.org/html/rfc7009](https://datatracker.ietf.org/doc/html/rfc7009)

### 令牌撤销协议

OAuth 令牌撤销是一种简单的协议，允许客户端简明扼要地向授权服务器表达：“我有这个令牌，我希望你把它作废。”与我们在 11.4
节介绍的令牌自省类似，客户端会向一个专用端点（撤销端点）发起经过身份验证的 HTTP POST 请求，并在请求体中以表单编码参数的形式携带需要撤销的令牌。

``` shell
POST /revoke HTTP/1.1
Host: localhost:9001
Accept: application/json
Content-type: application/x-www-form-encoded
Authorization: Basic b2F1dGgtY2xpZW50LTE6b2F1dGgtY2xpZW50LXNlY3JldC0x

token=987tghjkiu6trfghjuytrghj
```

客户端使用与向令牌端点发起请求时相同的凭据进行身份认证。授权服务器会查找该令牌值；如果找到了，就会将其从用于存储令牌的数据存储中删除，并向客户端返回操作成功的响应。

``` shell
HTTP 201 No Content
```

真的，就这么简单。客户端把自己那份令牌丢掉，然后继续该干嘛干嘛。

如果授权服务器找不到该令牌，或者出示该令牌的客户端无权撤销该令牌，授权服务器也会返回“一切正常”。为什么在这些情况下不返回错误呢？因为一旦这么做，就会无意中向客户端泄露有关其他令牌的信息。举例来说，假设某个客户端试图撤销另一个客户端的令牌，而我们对它返回
HTTP 403 Forbidden。在这种情况下，我们大概率不应撤销该令牌，因为那会让它可以对其他客户端发起拒绝服务攻击。[^11]
但与此同时，我们也不希望告诉这个客户端：它以某种方式拿到的令牌是真实有效的，并且可能在别处可用。为避免信息泄露，我们每次都假装已经撤销了该令牌。对行为规范的客户端而言，这在功能上没有任何差别；对恶意客户端而言，我们也没有透露任何不该透露的信息。当然，对于客户端身份验证失败等错误，我们仍会像令牌端点那样返回恰当的错误响应。

[^11]: 尽管这可能会让事情变得更复杂，但这个具体用例的细节其实更微妙一些：因为我们现在能够发现某个客户端已被攻破、令牌被盗——而我们大概率需要对此采取一些措施。

### 实现撤销端点

接下来我们要为授权服务器添加撤销支持。打开 ch-11-ex-5，编辑 authorizationServer.js 文件。我们会在授权服务器上配置一个位于
/revoke 的撤销端点，用于监听 HTTP POST 请求。同时，我们也会直接复用 token 端点中的客户端认证代码并将其引入到这里。

``` javascript
app.post('/revoke', function(req, res) {
  var auth = req.headers['authorization'];
  if (auth) {
      var clientCredentials = decodeClientCredentials(auth);
      var clientId = clientCredentials.id;
      var clientSecret = clientCredentials.secret;
  }

  if (req.body.client_id) {
      if (clientId) {
            res.status(401).json({error: 'invalid_client'});
            return;
      }

      var clientId = req.body.client_id;
      var clientSecret = req.body.client_secret;
  }

  var client = getClient(clientId);
  if (!client) {
      res.status(401).json({error: 'invalid_client'});
      return;
  }
  if (client.client_secret != clientSecret) {
      res.status(401).json({error: 'invalid_client'});
      return;
  }

});
```

撤销端点接收一个必填参数 token，通过 HTTP POST 请求体中的表单编码参数传入，方式与自省端点相同。我们会解析出该 token
并在数据库中查询。如果找到了，并且发起请求的客户端与该 token 的签发客户端一致，我们就会将其从数据库中删除。

``` javascript
var inToken = req.body.token;
nosql.remove().make(function(builder) {
  builder.and();
  builder.where('access_token', inToken);
  builder.where('client_id', clientId);
  builder.callback(function(err, count) {
    console.log("Removed %s tokens", count);
    res.status(204).end();
    return;
  });
});
```

无论我们是否真的删除了该令牌，我们都会表现得像已经删除了一样，并告诉客户端一切正常。最终的函数如附录 B 的清单 12 所示。

与内省（introspection）类似，授权服务器也必须能够响应吊销刷新令牌的请求。因此，一个完全合规的实现除了要在数据存储中检查访问令牌（access
token），还需要检查刷新令牌（refresh token）。客户端甚至可以发送一个 token_type_hint
参数，提示授权服务器应该先从哪里开始查找；不过授权服务器也可以忽略这个建议，直接在所有位置都检查。此外，一旦刷新令牌被吊销，与该刷新令牌关联的所有访问令牌也应当同时吊销。该功能的实现留作读者练习。

### 吊销令牌

现在我们要让客户端能够吊销令牌。我们将通过对客户端某个 URL 发起 HTTP POST 请求来触发令牌吊销。我们已经在客户端首页接好了一个新按钮，方便你从
UI 进入这项功能。在生产系统中，你需要对这项功能进行保护，防止外部应用或网站在你的应用毫不知情的情况下吊销你应用的令牌（见图
11.3）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260106091654821.png){ loading=lazy }
  <figcaption>图 11.3 客户端主页，带有用于触发令牌吊销的控制项</figcaption>
</figure>

我们先为 /revoke URL 配置一个处理器，用于监听 HTTP POST 请求。

``` javascript
app.post('/revoke', function(req, res) {

});
```

在这个方法里，我们将向撤销端点发起一个请求。客户端会使用常规凭证进行认证，并通过 HTTP Basic 的 Authorization
头传递；同时会在请求体中以表单参数的形式提交它的访问令牌。

``` javascript
var form_data = qs.stringify({
  token: access_token
});
var headers = {
  'Content-Type': 'application/x-www-form-urlencoded',
  'Authorization': 'Basic ' + encodeClientCredentials(client.client_id, client.client_secret)
};

var tokRes = request('POST', authServer.revocationEndpoint, {
  body: form_data,
  headers: headers
});
```

如果返回的是成功类状态码，我们就重新渲染应用的主页面；如果返回的是错误码，我们会向用户输出一条错误信息。无论哪种情况，为了我们这边的安全起见，都会丢弃
access token。

``` javascript
access_token = null;
refresh_token = null;
scope = null;

if (tokRes.statusCode >= 200 && tokRes.statusCode < 300) {
res.render('index', {access_token: access_token, refresh_token: refresh_token, scope: scope});
  return;
} else {
  res.render('error', {error: tokRes.statusCode});
  return;
}
```

客户端也可以用几乎同样的方式请求吊销它的刷新令牌。授权服务器收到这类请求后，也应该一并丢弃所有与该刷新令牌关联的访问令牌。至于如何实现这项功能，就留给读者自己练习了。

## OAuth 令牌的生命周期

OAuth 的访问令牌和刷新令牌都有明确的生命周期。它们由授权服务器创建，由客户端使用，并由受保护资源进行校验。我们也看到，它们可能因多种因素而失效，包括过期和被吊销。总体而言，令牌生命周期大致如图
11.4 所示。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260106101213152.png){ loading=lazy }
  <figcaption></figcaption>
</figure>

# 15.bearer 令牌以外的选择

本章内容包括

- 为什么 OAuth Bearer Token 并不适用于所有场景
- 提议中的 OAuth 持有证明（PoP，Proof of Possession）Token 类型
- 提议中的传输层安全（TLS，Transport Layer Security）Token 绑定方法

OAuth 是一种协议，它在多种应用与 API 之上提供了强大的授权委托机制，而 OAuth 协议的核心就是 OAuth Token。到目前为止，本书中使用的所有
Token 都是 Bearer Token。正如我们在第 10 章所讨论的，Bearer Token 可以被任何携带（bear）它的人拿去访问受保护资源。这是一种在许多系统中有意为之的设计选择，并且在
OAuth 体系中迄今仍是使用最广泛的 Token 类型。除了 Bearer Token 用起来足够简单之外，它之所以如此普及还有一个很现实的原因：截至本书出版之时，标准规范中定义的
Token 类型也只有这一种。[^1]

[^1]: RFC 6750 [https://tools.ietf.org/html/rfc6750](https://datatracker.ietf.org/doc/html/rfc6750)

不过，目前已经有一些工作正在推进，试图突破 Bearer Token 的局限。这些努力尚未形成完整标准，而且在本书出版到相关规范最终定稿之间，它们的实现细节很可能还会发生变化。

!!! note

	本章所述概念反映的是社区当前的思路，但很可能并不代表这些规范最终的定案结果。请带着保留态度阅读本章内容，因为随着所引用规范的进一步演进，这里写到的很多内容都可能会过时。

尽管如此，我们在这里讨论的内容至少代表了 OAuth 协议当前发展方向的一部分，因此接下来我们不妨花一点时间，看看未来会走向哪里。

## 为什么我们需要的不止是持有者令牌？

持有者令牌非常好用，因为客户端无需额外处理或理解即可使用。回想第 1 章和第 2 章的讨论，OAuth 2.0
的设计目标之一，就是尽可能把复杂性从客户端侧移走。使用持有者令牌时，客户端从授权服务器获得一个令牌，然后在访问受保护资源时原样携带并提交该令牌。很多时候，从客户端视角看，持有者令牌不过就是授权服务器为某个特定资源签发给客户端的一段“密码”。

但在很多场景下，我们希望更进一步：让客户端能够证明自己确实持有某个不会在网络上传输的秘密信息。这样，即便请求在传输途中被截获，攻击者也无法复用其中的令牌，因为攻击者并不掌握那份秘密。

截至本文撰写时，主要有两条思路正在被考虑：持有证明（Proof of Possession，PoP）令牌，以及传输层安全（TLS）令牌绑定（token
binding）。这两种方案各有特点，我们将在接下来的几节中分别讨论。

## 持有证明（PoP）令牌

互联网工程任务组（IETF）的 OAuth 工作组已经开始推进一种替代令牌形态：持有证明（Proof of
Possession，PoP）令牌。与持有者令牌不同，持有者令牌本身就是一个自包含的秘密；而 PoP 令牌由两部分组成：令牌本身和一个密钥（见图
15.1）。使用 PoP 令牌时，客户端除了提交令牌，还必须能够证明自己确实持有对应的密钥。令牌会随请求在网络上传输，但密钥不会。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260106163944014.png){ loading=lazy }
  <figcaption>图 15.1 OAuth PoP 令牌的两部分</figcaption>
</figure>

令牌部分在很多方面都类似于持有者令牌（bearer
token）。客户端并不知道、也不关心这个令牌里包含什么内容，它只需要知道该令牌代表了对受保护资源的访问委托。客户端会像以前一样，原封不动地发送这部分令牌，不做任何修改。

令牌中的密钥部分用于生成随 HTTP 请求一并发送的密码学签名。客户端在将请求发送到受保护资源之前，会对 HTTP
请求的某些部分进行签名，并把签名包含在请求中；用于生成该签名的就是这把密钥。为了对密钥进行编码，OAuth 的 PoP 体系采用了 JSON
Web Key（JWK）格式，这是我们在第 11 章讨论过的 JSON Object Signing and Encryption（JOSE）规范套件的一部分。JWK
既支持对称密钥和非对称密钥类型，也支持随时间演进的密码算法灵活性（crypto agility）。

PoP 流程也有几种不同的选择，就像持有者令牌一样。首先，你需要获取一个令牌（图 15.2）。然后，你需要使用这个令牌（图 15.3）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260106164104721.png){ loading=lazy }
  <figcaption>图 15.2 获取 OAuth PoP 令牌（及其关联密钥）</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260106164136492.png){ loading=lazy }
  <figcaption>图 15.3：使用并验证 OAuth PoP 令牌</figcaption>
</figure>

现在我们来更深入地看看这个流程的主要步骤。

### 请求并签发 PoP 令牌

要签发 PoP 令牌，授权服务器需要知道要与该令牌绑定的密钥。根据客户端类型以及整体部署环境的不同，这个密钥可以由客户端提供，也可以由服务器生成。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260106164247198.png){ loading=lazy }
  <figcaption>表 15.1 与 PoP 令牌关联的密钥类型</figcaption>
</figure>

在这个示例中，授权服务器为客户端生成一对非对称密钥供其使用。客户端对令牌端点的请求与之前相同。响应同样包含 access_token
字段（与 Bearer Token 的返回一致），但 token_type 字段被设置为 PoP，并且响应中还包含 access_token_key 字段，用于携带该密钥。

``` json
{
  "access_token": "8uyhgt6789049dafsdf234g3",
  "token_type": "PoP",
  "access_token_key": {
    "d": "RE8jjNu7p_fGUcY-aYzeWiQnzsTgIst6N4ljgUALSQmpDDlkziPO2dHcYLgZM28Hs8y
       QRXayDAdkv-qNJsXegJ8MlNuiv70GgRGTOecQqlHFbufTVsE480kkdD-zhdHy9-P9cyDzp
       bEFBOeBtUNX6Wxb3rO-ccXo3M63JZEFSULzkLihz9UUW1yYa4zWu7Nn229UrpPUC7PU7FS
       g4j45BZJ_-mqRZ7gXJ0lObfPSMI79F1vMw2PpG6LOeHM9JWseSPwgEeiUWYIY1y7tUuNo5
       dsuAVboWCiONO4CgK7FByZH7CA7etPZ6aek4N6Cgvs3u3C2sfUrZlGySdAZisQBAQ",
    "e": "AQAB",
    "n": "xaH4c1td1_yLhbmSVB6l-_W3Ei4wGFyMK_sPzn6glTwaGuE5_mEohdElgTQNsSnw7up
       NUx8kJnDuxNFcGVlua6cA5y88TB-27Q9IaeXPSKxSSDUv8n1lt_c6JnjJf8SbzLmVqosJ-
       aIu_ZCY8I0w1LIrnOeaFAe2-m9XVzQniR5XHxfAlhngoydqCW7NCgr2K8sXuxFp5lK5s-
       tkCsi2CnEfBMCOOLJE8iSjTEPdjoJKSNro_Q-pWWJDP74h41KIL4yryggdFd-8gi-E6uHE
       wyKYi57cR8uLtspN5sU4110sQX7Z0Otb0pmEMbWyrs5BR3RY8ewajL8SN5UyA0P1XQ",
    "kty": "RSA",
    "kid": "tk-11234"
  },
  "alg": "RS256"
}
```

这个 JWK 是一对 RSA 密钥（我们在第 11 章已经见过），客户端可以在下一步用它来对请求进行签名。由于这是 RSA
密钥，授权服务器在生成密钥对后只需要保存公钥部分即可，从而避免授权服务器遭到攻击时泄露私钥等关键材料。

在我们的示例中，访问令牌本身是一个随机字符串；当然，它也完全可以像第 11 章所述那样采用 JSON Web
Token（JWT）。关键在于，令牌对客户端依然是不可解析的（opaque），这也与我们此前讨论的 OAuth 各个环节保持一致。

### 在受保护资源上使用 PoP 令牌

此时客户端同时持有令牌和密钥，需要以一种方式把它们发送给受保护资源，使受保护资源能够验证：客户端确实控制着与该令牌绑定的那把密钥。

为此，客户端会创建一个 JSON 对象，其中至少包含访问令牌。作为可选项，客户端还可以将 HTTP
消息的某些部分纳入或哈希，用于在通道保护之外，再按“逐条消息”的粒度对请求做完整性保护。相关细节在 OAuth
工作组的草案文档中已有详细说明，这里留作读者自行拓展。在这个简单示例中，我们将保护 HTTP 方法和主机名，并额外加入时间戳。

``` json
{
  "at": "8uyhgt6789049dafsdf234g3",
  "ts": 3165383,
  "http": { "v": "POST", "u": "locahost:9002" }
}
```

客户端随后将该 JSON 对象作为 JSON Web Signature（JWS）的载荷（payload），并使用与该令牌关联的密钥对其进行签名。这样就会生成如下所示的
JWS 对象：

``` shell
eyJhbGciOiJSUzI1NiJ9.eyJhdCI6ICI4dXloZ3Q2Nzg5MDQ5ZGFmc2RmMjM0ZzMiLCJ0cyI6IDMx
   NjUzODMsImh0dHAiOnsidiI6IlBPU1QiLCJ1IjoibG9jYWhvc3Q6OTAwMiJ9fQo.m2Na5CCbyt
   0bvmiWIgWB_yJ5ETsmrB5uB_hMu7a_bWqn8UoLZxadN8s9joIgfzVO9vl757DvMPFDiE2XWw1m
   rfIKn6Epqjb5xPXxqcSJEYoJ1bkbIP1UQpHy8VRpvMcM1JB3LzpLUfe6zhPBxnnO4axKgcQE8Sl
   gXGvGAsPqcct92Xb76G04q3cDnEx_hxXO8XnUl2pniKW2C2vY4b5Yyqu-mrXb6r2F4YkTkrkHH
   GoFH4w6phIRv3Ku8Gm1_MwhiIDAKPz3_1rRVP_jkID9R4osKZOeBRcosVEW3MoPqcEL2OXRrLh
   Yjj9XMdXo8ayjz_6BaRI0VUW3RDuWHP9Dmg
```

客户端随后将该 JWS 对象作为请求的一部分发送到受保护资源。与 Bearer Token 一样，它可以通过查询参数、表单参数或 HTTP
Authorization 头传递。其中，使用 Authorization 头最灵活也最安全，这也是我们在这里展示的示例。

``` shell
HTTP POST /foo
Host: example.org
Authorization: PoP eyJhbGciOiJSUzI1NiJ9.eyJhdCI6ICI4dXloZ3Q2Nzg5MDQ5...
```

请注意，客户端并不会对访问令牌本身做任何处理；要实现这一点，客户端也不需要理解访问令牌的格式或内容。与持有者令牌（bearer
token）一样，访问令牌对客户端始终是不可解析的“黑盒”。唯一发生变化的是令牌提交给受保护资源的方式：客户端会使用与令牌关联的密钥作为证明来呈现该令牌。

### 验证 PoP 令牌请求

在受保护资源端，我们会收到一个类似前面生成的请求。我们可以使用任何 JOSE 库轻松解析 PoP
请求以获取其载荷（payload），从而拿到访问令牌本身。要弄清访问令牌“能做什么”（例如它代表哪些
scope、由哪个资源所有者批准），我们仍然可以沿用持有者令牌那套方法：在本地数据库中查找、解析访问令牌自身的某种结构，或使用令牌自省（token
introspection，第 11 章讨论）之类的服务来查询。这些流程与持有者令牌基本一致，只有一个关键差异。

我们不仅需要确认令牌确实来自授权服务器，还需要确认该请求来自“应该持有该令牌密钥”的那个客户端。无论我们在受保护资源端如何校验和验证令牌，也必须同时验证
PoP 请求上使用的签名。为此，我们需要拿到与该令牌关联的密钥。与验证访问令牌本身类似，我们也有多种方式来查找该密钥，而且通常与查令牌的方式相同。授权服务器可以把令牌和密钥一起存到共享数据库中，让受保护资源能够访问；这也是
OAuth 1.0 的常见做法，当时令牌同时包含公开部分和密钥部分。我们也可以用 JOSE
将密钥封装到访问令牌内部，甚至对密钥加密，使得只有特定受保护资源才能接受特定令牌。最后，还可以通过令牌自省调用授权服务器，让其返回与令牌关联的密钥。无论通过哪种方式拿到密钥，我们都可以用它来验证入站请求的签名。

受保护资源会根据所使用的密钥类型以及客户端采用的签名机制，执行相应的 JWS
签名校验。若签名对象中包含主机名、端口、路径、方法等信息，受保护资源可对这些字段进行检查，并与客户端实际发出的请求逐项比对。如果
HTTP 报文的某些部分被哈希处理过（例如查询参数或请求头），受保护资源也会计算对应的哈希值，并与 JWS 载荷中包含的值进行比对。

到这一步，受保护资源就能确认：发起该 HTTP 请求的一方不仅持有访问令牌，也持有与之关联的签名密钥。这样的设计让 OAuth
客户端无需把该机密信息通过网络传给受保护资源，就能证明自己确实“持有”该机密。在客户端自行生成密钥对且授权服务器从未见过私钥的场景下，这还能最大限度减少整个网络中私钥信息的暴露面。

## 实现 PoP 令牌支持

现在我们将为我们的 OAuth 生态系统添加 PoP 令牌支持，并沿用本书其他章节使用过的同一套代码框架。请记住，由于相关规范仍在不断演进，本练习中的代码不保证与
OAuth PoP 令牌的最终规范完全一致，但我们认为这个练习能以动手实践的方式，帮助你理解这类系统的架构是如何运作的。

在我们的方案中，客户端会照常请求一个 OAuth
令牌。授权服务器会生成一个随机值作为令牌，并将其与服务器生成的一对密钥绑定，然后把这对密钥传给客户端。授权服务器会把这对密钥中的公钥部分与令牌值一起存储，并连同我们在之前练习中保存的其他信息（如
scope、客户端标识符等）一并记录下来。当客户端调用受保护资源时，它会生成一条签名消息，其中包含令牌以及 HTTP
请求的若干关键部分。这条签名消息会作为一个 Header 放入发送给受保护资源的 HTTP 请求中。受保护资源会解析传入的
Header，从签名消息中提取访问令牌，并将该令牌值发送到令牌自省（introspection）端点。随后，授权服务器会查找该访问令牌，并把关联的令牌数据（包括其公钥）返回给受保护资源。接着，受保护资源会校验传入
Header 的签名，并将其中的内容与实际请求逐项比对。若一切吻合，就返回资源。

听起来很简单，对吧？那就开始动手。

### 签发令牌与密钥

本节请打开 ch-15-ex-1。我们会在现有基础设施上构建 PoP 支持——到目前为止它只支持 Bearer
令牌。我们的访问令牌本身仍然是随机字符串，但我们会在其旁边生成并存储一把密钥。

打开 authorizationServer.js，找到 token endpoint 函数中生成令牌的那段代码。此前它会创建一个随机的 access
token，将其保存并返回。现在我们要为这个令牌加入一把密钥。我们已经引入了一个库来帮助生成 JWK
格式的密钥，这样就可以在整个应用中存储并复用。需要注意的是，由于我们使用的这个库的特性，你必须在 JavaScript
的回调函数中管理密钥；而在其他平台上，通常会直接生成并返回密钥。

``` javascript
if (code.authorizationEndpointRequest.client_id == clientId) {

  keystore.generate('RSA', 2048).then(function(key) {
      var access_token = randomstring.generate();

      var access_token_key = key.toJSON(true);
      var access_token_public_key = key.toJSON();

      var token_response = { access_token: access_token, access_token_key:
access_token_key, token_type: 'PoP',  refresh_token: req.body.refresh_
token, scope: code.scope, alg: 'RS256' };

      nosql.insert({ access_token: access_token, access_token_key: access_
token_public_key, client_id: clientId, scope: code.scope });

      res.status(200).json(token_response);
      console.log('Issued tokens for code %s', req.body.code);

      return;
  });
  return;
}
```

请注意，由于我们使用的是非对称密钥，我们存储的内容并不与发送给客户端的内容完全相同。我们会将公钥与其他令牌信息（例如作用域和客户端
ID）一起保存到数据库中。我们把公钥和私钥的密钥对作为 JSON 对象中的 access_token_key 字段返回，因此令牌端点的返回结果大致如下所示：

``` shell
HTTP 200 OK
Date: Fri, 31 Jul 2015 21:19:03 GMT
Content-type: application/json

{
  "access_token": "987tghjkiu6trfghjuytrghj",
  "access_token_key": {
      "d":
"l5zO96Jpij5xrccN7M56U4ytB3XTFYCjmSEkg8X20QgFrgp7TqfIFcrNh62JPzosfaaw9vx13Hg_
yNXK9PRMq-gbtdwS1_QHi-0Y5__TNgSx06VGRSpbS8JHVsc8sVQ3ajH-wQu4k0DlEGwlJ8pmHXYAQ
prKa7RObLJHDVQ_uBtj-iCJUxqodMIY23c896PDFUBl-M1SsjXJQCNF1aMv2ZabePhE_m2xMeUX3
LhOqXNT2W6C5rPyWRkvV_EtaBNdvOIxHUbXjR2Hrab5I-yIjI0yfPzBDlW2ODnK2hZirEyZPTP8
vQVQCVtZe6lqnW533V6zQsH7HRdTytOY14ak8Q",
      "e": "AQAB",
      "n": "ojoQ9oFh0b9wzkcT-3zWsUnlBmk2chQXkF9rjxwAg5qyRWh56sWZx8uvPhwqmi9r
1rOYHgyibOwimGwNPGWsP7OG_6s9S3nMIVbz9GIztckai-O0DrLEF-oLbn3he4RV1_TV_p1FSl
D6YkTUMVW4YpceXiWldDOnHHZVX0F2SB5VfWSU7Dj3fKvbwbQLudi1tDMpL_dXBsVDIkPxoCir
7zTaVmSRudvsjfx_Z6d2QAClm2XnZo4xsfHX_HiCiDH3bp07y_3vPR0OksQ3tgeeyyoA8xlrPs
AVved2nUknwIiq1eImbOhoG3e8alVgA87HlkiTu5sLGEwY5AghjRe8sw",
      "kty": "RSA"
  },
  "alg": "RS256",
  "scope": "foo bar",
  "token_type": "PoP"
}
```

注意，我们还把令牌类型从 Bearer 改成了
PoP。这个练习中，服务器端还需要做最后一件事：在令牌自省（introspection）响应里返回访问令牌的密钥，因为接下来我们会通过令牌自省来查询令牌的详细信息（更多内容见第
11 章）。请在自省端点添加下面这一行：

``` javascript
introspectionResponse.access_token_key = token.access_token_key;
```

现有的 OAuth 客户端几乎不需要做任何改动就能解析这种结构，我们会在下一节看到这一点。

### 创建签名头并将其发送给资源端

这一节我们仍然在 ch-15-ex-1 中继续操作，不过这次请打开 client.js。首先，我们需要让客户端能够保存这个密钥。由于它返回的结构与
access token 的值相同，你需要先找到解析并存储 access token 值的那段代码。现在，它大致如下所示：

``` javascript
var body = JSON.parse(tokRes.getBody());

access_token = body.access_token;
if (body.refresh_token) {
  refresh_token = body.refresh_token;
}

scope = body.scope;
```

关键在于，这把密钥是以 JWK 格式传给我们的，而我们的库原生支持直接接收 JWK 格式的密钥。因此，我们需要在上一节的基础上再加一行代码，把密钥值提取出来并存入变量（key），与
access token 一起保存。同时，我们也会把预期使用的算法一并保存下来。

``` javascript
key = body.access_token_key;
alg = body.alg;
```

接下来，我们需要使用该密钥去调用受保护资源。为此，我们将创建一个 JWS 对象：其 payload 用于表示我们的请求，并使用刚刚签发的访问令牌（access
token）对应的密钥对其进行签名。请找到当前发送 Bearer Token 的那段代码。首先，我们会构造一个 header，并在 payload
中加入访问令牌的值以及时间戳。

``` javascript
var header = { 'typ': 'PoP', 'alg': alg, 'kid': key.kid };

var payload = {};
payload.at = access_token;
payload.ts = Math.floor(Date.now() / 1000);
```

接下来，我们会在 payload 中加入一些关于目标请求的信息。这部分规范是可选的，但建议将访问令牌与具体的 HTTP 请求绑定起来。在这里，我们会加入对
HTTP 方法、主机名以及路径的引用。在这个简单示例中，我们不打算保护请求头或查询参数；不过你可以把为它们添加支持作为进阶练习。

``` javascript
payload.m = 'POST';
payload.u = 'localhost:9002';
payload.p = '/resource';
```

现在我们已经拿到了这个请求体，接下来就按第 11 章里的同样步骤，创建一个用 JWS 签名的对象。我们会用之前保存的、与访问令牌关联的密钥来对我们的
payload 进行签名。

``` javascript
var privateKey = jose.KEYUTIL.getKey(key);
var signed = jose.jws.JWS.sign(alg, JSON.stringify(header),
JSON.stringify(payload), privateKey);
```

这在机制上与授权服务器在第 11
章创建签名令牌时所做的事情类似，但你会发现我们这里并不是在创建令牌。事实上，我们是在签名对象里把令牌包含进去。另外，别忘了我们现在是在客户端内部操作，而客户端并不会签发令牌。我们要做的是生成一个签名，受保护资源可以验证该签名，从而证明我们这个客户端确实持有正确的密钥。正如我们将在下一节看到的，这并不能说明被包含的令牌具体能做什么，甚至也无法说明它是否有效。

最后，我们会把这个签名对象放在请求发往受保护资源时的 Authorization 头中发送出去。注意，这里我们不再使用 Bearer 授权类型来发送
access_token 的值，而是改用 PoP 授权类型来发送这个签名对象。访问令牌被包含在签名值内部，并由签名保护，无需再单独传输。除此之外，请求的机制与之前相同。

``` javascript
var headers = {
  'Authorization': 'PoP ' + signed,
  'Content-Type': 'application/x-www-form-urlencoded'
};
```

从这里开始，客户端会像以往一样处理受保护资源返回的响应。尽管 PoP 令牌更复杂、需要做一些额外工作，但和持有者令牌一样，相比系统的其他部分，客户端的负担仍然微乎其微。

### 解析请求头、审视令牌并验证签名

在最后这一节，我们将继续在 ch-15-ex-1 中完成剩余工作，不过这次要处理的是客户端把令牌发送到受保护资源之后的流程。打开
protectedResource.js，找到 getAccessToken 函数。首先要做的是：把之前查找的 Bearer 关键字改为查找 PoP 关键字。

``` javascript
var auth = req.headers['authorization'];
var inToken = null;
if (auth && auth.toLowerCase().indexOf('pop') == 0) {
  inToken = auth.slice('pop '.length);
} else if (req.body && req.body.pop_access_token) {
  inToken = req.body.pop_access_token;
} else if (req.query && req.query.pop_access_token) {
  inToken = req.query.pop_access_token
}
```

现在我们需要像第 11 章那样解析 JWS 结构。我们按句点（.）分割字符串，并对 header 和 payload 进行解码。拿到对象形式的 payload
之后，我们从它的 at 成员中取出 access token 的值。

``` javascript
var tokenParts = inToken.split('.');
var header = JSON.parse(base64url.decode(tokenParts[0]));
var payload = JSON.parse(base64url.decode(tokenParts[1]));

var at = payload.at;
```

接下来，我们需要查询这个访问令牌的相关信息，包括它的作用域（scopes）以及关联的密钥。和 Bearer Token
一样，这里也有几种实现方式可选，例如查数据库，或者将 at 的值按 JWT 解析。本文练习中，我们会像第 12 章那样，通过令牌自省（token
introspection）来完成查询。调用令牌自省端点的方法与之前几乎一致，只是我们不再发送 inToken 值（即从入站请求中解析出来的那个令牌），而是发送提取出的
at 值。

``` javascript
var form_data = qs.stringify({
  token: at
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

如果自省接口的响应为正，并且该令牌被标记为 active，我们就可以解析出密钥并验证已签名对象。注意，这里返回的只有公钥，这就避免了受保护资源能够基于该访问令牌伪造请求。相比之下，这是相对
Bearer Token 的一大优势——恶意的受保护资源很容易对其进行重放。不过，我们的受保护资源并不打算做这些“偷鸡摸狗”的事，因此我们先从校验签名开始。

``` javascript
if (tokRes.statusCode >= 200 && tokRes.statusCode < 300) {
  var body = JSON.parse(tokRes.getBody());

  var active = body.active;
  if (active) {

      var pubKey = jose.KEYUTIL.getKey(body.access_token_key);
      if (jose.jws.JWS.verify(inToken, pubKey, [header.alg])) {
```

接下来，检查已签名对象的其他部分，确保它们与传入的请求一致。

``` javascript
if (!payload.m || payload.m == req.method) {
  if (!payload.u || payload.u == 'localhost:9002') {
      if (!payload.p || payload.p == req.path) {
```

如果这些检查都通过了，我们就像之前一样把 token 添加到 req 对象上。应用里的各个处理函数会知道在后续流程中去检查这些值，而我们也无需修改应用的其他部分。

``` javascript
req.access_token = {
  access_token: at,
  scope: body.scope
};
```

整个函数看起来就像附录 B 中的清单 16。到这里，我们应该已经基于某个草案标准的实现思路，搭建出一套完全可用的 PoP
系统了。最终规范很可能会与我们练习中的实现有所不同，但眼下还无法判断差异会有多大。希望相关内容能尽快稳定下来，不久之后工作组就能给出一套可落地、可互操作的
PoP 系统。

## TLS 令牌绑定

TLS 规范通过对消息传输所经过的传输通道进行加密，来保护传输中的消息。这种加密发生在网络上的两个端点之间，最常见的就是发起请求的
Web 客户端与返回响应的 Web 服务器。令牌绑定（Token Binding）是一种方法，允许将 TLS 中的信息带到应用层协议（如 HTTP）以及运行在
HTTP 之上的协议（如 OAuth）中使用。这样就可以在不同层之间对这些信息进行比对，确保需要对话的是同一组组件。

在 HTTPS 上进行令牌绑定的基本思路相对简单：当 HTTP 客户端与 HTTP 服务器建立 TLS 连接时，客户端会在 HTTP
头中携带一个公钥（令牌绑定标识符），并证明自己持有对应的私钥。服务器在签发令牌时，会将该令牌与这个标识符绑定。之后客户端再次连接服务器时，会用相应的私钥对该标识符进行签名，并将签名放在
TLS 头中传递给服务器。服务器随后即可验证该签名，确保提交绑定令牌的客户端，与最初提交那对临时密钥对的客户端是同一个。令牌绑定最初是为浏览器
Cookie 等场景设计的，这类用法相当直接，因为所有交互都发生在同一条通道上（见图 15.4）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260106174246959.png){ loading=lazy }
  <figcaption>图 15.4：浏览器 Cookie 上的 TLS 令牌绑定</figcaption>
</figure>

令牌绑定需要访问 TLS 层；一旦引入 TLS 终止器（比如 Apache HTTPD 反向代理），就往往很难使用。它也不同于双向 TLS（mTLS）认证：在
mTLS 中，TLS 握手所用证书的身份会在通信两端被验证与校验。尽管如此，令牌绑定这种方式允许应用更直接地利用 TLS
系统中已有的信息来提升安全性。随着令牌绑定能力被集成进 TLS 中间件库，各类应用都将能够无感知地使用它。

在 OAuth 体系中，这对管理资源所有者浏览器与客户端或授权服务器之间的连接非常适用。用于在客户端与授权服务器之间传递的刷新令牌也同样适用。但访问令牌就会变得棘手：签发令牌的
HTTP 服务器（授权服务器）与接收令牌的 HTTP 服务器（受保护资源）往往不是同一个，这就要求客户端分别建立不同的 TLS 连接。若假设是
Web 客户端并采用令牌内省，那么把各组件之间所有可能的连接算一遍，最终至少会有五条不同的 TLS 通道（见图 15.5）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260106174334577.png){ loading=lazy }
  <figcaption>图 15.5 典型 OAuth 生态系统中的不同 TLS 通道</figcaption>
</figure>

1. 资源所有者的浏览器到授权服务器的授权端点
2. 资源所有者的浏览器到客户端
3. 客户端到授权服务器的令牌端点
4. 客户端到受保护资源
5. 受保护资源到授权服务器的内省端点

在一个简单的令牌绑定（token
binding）配置中，这些通道各自都会得到不同的令牌绑定标识符。令牌绑定协议通过允许客户端将某条连接的标识符发送到另一条连接上来处理这种二分性，从而有意识地弥合两条原本彼此独立的连接之间的鸿沟。换句话说，客户端的意思是：“我现在在通道
3 上和你通信，但我会在通道 4 上使用这个令牌，所以请把令牌绑定到那条通道。”如果还存在额外的受保护资源，情况会更复杂，因为客户端与每个新增资源之间的连接都会构成一条不同的
TLS 通道。

本质上，当客户端向授权服务器请求获取 OAuth
令牌时，会把与受保护资源连接对应的令牌绑定标识符一并带上。授权服务器会把签发的令牌绑定到这个标识符上，而不是绑定到客户端与授权服务器之间那条连接所使用的标识符上。之后，当客户端携带该令牌调用受保护资源时，受保护资源会验证
TLS 连接上使用的标识符是否正是与该令牌关联的那个标识符。

这种做法要求客户端主动维护授权服务器与受保护资源之间的映射关系，但许多 OAuth
客户端本来就会这么做，以避免把令牌发送给错误的受保护资源。令牌绑定既可用于持有者令牌（bearer token），也可用于 PoP
令牌；在这两种情况下，它都会在“仅凭持有令牌”以及“令牌可能关联的任何密钥”之外，再额外增加一层确认机制。

## 总结

OAuth Bearer 令牌提供了简单而稳健的能力，但在某些使用场景下，进一步超越它们也很有价值。

- PoP 令牌会与一个客户端已知的密钥关联。
- 客户端使用 PoP 密钥对 HTTP 请求进行签名，并将其发送到受保护资源。
- 受保护资源会连同访问令牌本身一起验证该签名。
- TLS 令牌绑定能够打通网络协议栈的不同层，从而在连接上提供更高等级的可信保障。

你已经快读到全书末尾了。至此，我们从头到尾、从前到后，也从过去到未来，完整讲解了 OAuth。继续往下读，我们将在总结与结论中为这段旅程画上句号。

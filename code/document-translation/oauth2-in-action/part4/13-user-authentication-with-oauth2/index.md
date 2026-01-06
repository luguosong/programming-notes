# 13.使用 OAuth 2.0 进行用户认证

本章内容包括

- OAuth 2.0 为什么不是认证协议
- 如何基于 OAuth 2.0 构建认证协议
- 在用 OAuth 2.0 做认证时如何识别并避免常见错误
- 如何在 OAuth 2.0 之上实现 OpenID Connect

OAuth 2.0 规范定义了一种委派协议，用于在由 Web 应用与 API 组成的网络中传递授权决策。由于 OAuth 2.0 常被用来获取已认证终端用户的同意，很多开发者和
API 提供方便认为 OAuth 2.0 是一种认证协议，可以用来安全地让用户登录。然而，尽管它是一种会涉及用户交互的安全协议，OAuth 2.0
并不是认证协议。为了强调清楚，我们再说一遍：

`OAuth 2.0 不是认证协议。`

之所以容易混淆，很大程度上是因为 OAuth 2.0 经常被用在各种认证协议内部，而且在一次常规的 OAuth 2.0
流程中也会嵌入若干与认证相关的事件。因此，许多开发者看到 OAuth 2.0 的流程就想当然地以为：用了
OAuth，就等于在做用户认证。事实证明，这不仅不成立，而且对服务提供方、开发者以及终端用户都存在风险。

## 为什么 OAuth 2.0 不是认证协议

首先我们得回答一个根本问题：到底什么是“认证”？在这里，认证指的是让应用知道当前用户是谁，以及此刻是否正在使用你的应用。它是安全架构中的一部分，用来确认用户确实就是其声称的那个人——通常通过向应用提供一组凭证（例如用户名和密码）来证明这一点。一个实用的认证协议通常还会告诉你该用户的一些身份属性，例如唯一标识符、电子邮件地址，以及当应用对用户说“早上好”时要显示的名字。

不过，OAuth 2.0 并不会把这些告诉应用。单靠 OAuth 2.0，本身对用户只字不提；它既不说明用户如何证明自己“在场”，甚至连用户是否真的在场都不关心。对一个
OAuth 2.0 客户端来说，它做的事情很简单：去申请一个令牌，拿到一个令牌，然后在需要时用这个令牌去访问某个
API。它并不知道究竟是谁授权了这个应用，也无法确定当时是否真的有用户参与。事实上，OAuth 2.0
的许多重要使用场景，恰恰是为了在用户无法再进行交互式授权时，依然能够获取并使用访问令牌。回到我们之前的打印示例：虽然用户确实分别登录过打印服务和存储服务，但用户并没有直接参与打印服务与存储服务之间的连接过程。相反，OAuth
2.0 的访问令牌让打印服务能够代表用户执行操作。这是一种非常强大的“委托式客户端授权”范式，但它在某种意义上与“认证”背道而驰——认证的核心目的，正是确认用户是否在场，以及用户到底是谁。

### 认证 vs. 授权：一个“美味”的比喻

为了把问题讲清楚，不妨用一个比喻来理解认证（authentication）与授权（authorization）的区别：太妃糖（fudge）和巧克力（chocolate）。[^1]
它们表面上有些相似，但本质完全不同：巧克力是一种原料，而太妃糖是一种甜点。你可以做巧克力太妃糖——在我们这些作者看来，这可真是人间美味。这种甜点的特征显然由“巧克力味”所定义。也正因如此，人们很容易（但最终是错误地）把巧克力与太妃糖当成等价的东西。我们接下来就把这个比喻拆开讲讲，看看它和
OAuth 2.0 到底有什么关系。

[^1]: 非常感谢 Vittorio Bertocci 提供的这个精彩比喻，出自他的博文《OAuth 2.0 and
Sign-In》，可在以下链接查看：[http://www.cloudidentity.com/blog/2013/01/02/oauth-2-0-and-sign-in-4/](https://www.cloudidentity.com/blog/2013/01/02/oauth-2-0-and-sign-in-4/)

巧克力可以以多种形态做成各种不同的东西，但它的基础始终是可可。它用途广泛、非常实用，独特的风味能为从蛋糕、冰淇淋到酥点夹馅、摩尔酱等各类食物增色。你甚至可以不加任何其他配料，直接把巧克力本身当作食物来享用——即便如此，它也依然有很多不同的形态。另一个当然也很受欢迎、可以用巧克力制作的东西，就是巧克力软糖。在这里，吃软糖的人一眼就能看出来：巧克力是这款甜点当之无愧的主角配料。

在这个比喻里，OAuth 2.0 就是巧克力。它是一种用途广泛的基础性“原料”，是当今 Web 上许多不同安全架构不可或缺的组成部分。OAuth
2.0 的授权委派模型很有辨识度，并且始终由同样的角色与参与方构成。OAuth 2.0 可以用来保护 RESTful API 和 Web 资源；可以被部署在
Web 服务器上的客户端以及原生应用的客户端使用；既可以让终端用户委派有限权限，也可以让受信任的应用通过后端通道传递数据。OAuth
2.0 甚至还能用来构建身份与认证 API——此时也很清楚，OAuth 2.0 才是背后真正的关键使能技术。

相反，软糖是一种可以用很多不同原料制作的糖果，而且会呈现出原料本身的风味：从花生酱到椰子，从橙子到土豆。[^2]
尽管风味五花八门，软糖总有一种特定的形态与口感，让人一眼就能认出它是软糖，而不是慕斯或甘纳许之类的其他风味甜点。当然，软糖中很受欢迎的一种口味就是巧克力软糖。即便在这种糖果里，巧克力显然是主角原料，但要把巧克力变成巧克力软糖，仍需要加入几种额外配料，并经过几个关键步骤。最终成品在风味上依然能让人认出是巧克力，但在形态上则是软糖；而用巧克力做出软糖，并不意味着巧克力就等同于软糖。

[^2]: 真的不是开玩笑，土豆软糖意外地好吃。

在我们的比喻里，认证更像是在做软糖。要想既正确又安全地“做出来”，必须把几个关键组件和流程以恰当的方式组合起来，而这些组件和流程的可选项非常多。比如，你可以要求用户携带某个设备、记住一个秘密口令、提供生物特征样本、证明自己能登录到另一台远程服务器，或者采用其他各种办法。为了完成工作，这类系统可能会用到公钥基础设施（PKI）与证书、联合信任框架、浏览器
Cookie，甚至是专有的软硬件。OAuth 2.0 也可以作为其中的一个技术组件，但当然并非必须。缺少其他要素时，单靠 OAuth 2.0
并不足以承担用户认证。

就像做巧克力软糖有不同配方一样，基于 OAuth 的认证协议也有各种模式。其中不少是为特定提供方量身定制的，例如
Facebook、Twitter、LinkedIn 或 GitHub；也有像 OpenID Connect 这样的开放标准，能够跨多个不同提供方工作。这些协议都以 OAuth
的通用基础为起点，再叠加各自的附加组件，以略有不同的方式提供认证能力。

## 将 OAuth 映射到认证协议

那么，如何以 OAuth 作为基础来构建一个认证协议呢？首先，我们需要把 OAuth 2.0 的不同参与方映射到一次认证交易的相应角色上。在
OAuth 2.0 交易中，资源所有者授权客户端，使用来自授权服务器的令牌访问受保护资源。在认证交易中，终端用户借助身份提供方（IdP）登录到依赖方（RP）。基于这一点，设计此类认证协议时的一种常见做法，是把依赖方映射为受保护资源（见图
13.1）。毕竟，依赖方不正是认证协议所要保护的组件吗？

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260106141220348.png){ loading=lazy }
  <figcaption>图 13.1 试图用 OAuth 拼凑出一个认证协议，却以失败告终</figcaption>
</figure>

尽管乍看之下，把身份协议部署在 OAuth 2.0 之上似乎是个合理的做法，但从图 13.1 可以看出，安全边界并不能很好地对齐。在 OAuth
2.0 中，客户端与资源所有者是协同工作的——客户端代表资源所有者行事。授权服务器与受保护资源同样协同工作，因为授权服务器生成的令牌会被受保护资源接受。换句话说，用户/客户端与授权服务器/受保护资源之间存在一条安全与信任边界，而
OAuth 2.0 正是用来跨越这条边界的协议。可当我们像图 13.1 那样去映射时，边界却变成了 IdP
与受保护资源之间。这就迫使我们以一种不自然的方式跨越该安全边界：受保护资源开始直接与用户交互。然而在 OAuth 2.0
里，资源所有者通常并不会与受保护资源交互——受保护资源是供客户端应用调用的 API。回想前面章节的编码练习，我们的受保护资源甚至连一个像样的
UI 都没有。真正与用户交互的客户端，在这种新的映射里反而消失得无影无踪。

这显然行不通，我们需要换一种尊重这些安全边界的方式。我们试着把 RP 建立在 OAuth 2.0
客户端之上，因为终端用户——也就是资源所有者——本来就通常与它交互。同时，我们把授权服务器和受保护资源合并成一个组件：IdP。接下来，资源所有者会把访问权限委托给客户端，但他们委托访问的资源是自己的身份信息。也就是说，他们授权
RP 去确认“此刻在这里的是谁”，而这正是我们试图构建的认证交易的本质（见图 13.2）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260106141828522.png){ loading=lazy }
  <figcaption>图 13.2：用 OAuth 构建认证协议，并取得了更好的效果</figcaption>
</figure>

尽管在授权机制之上构建认证听起来有些反直觉，但从这里可以看出，借助 OAuth 2.0 的安全委派模型，我们获得了一种强有力的系统互联方式。此外，请注意，我们可以将
OAuth 2.0 体系的各个部分清晰地映射到认证协议中的对应组件上。如果我们对 OAuth 2.0
进行扩展，让授权服务器与受保护资源返回的信息能够传递用户信息及其认证上下文，那么客户端就能拿到安全登录用户所需的一切信息。

至此，我们就拥有了一个由熟悉的 OAuth 2.0 组件构成的认证协议。由于进入了新的协议领域，这些组件也有了新的名称。客户端现在称为依赖方（Relying
Party，RP），在该协议中两者可以互换使用。从概念上，我们将授权服务器与受保护资源合并为身份提供方（Identity
Provider，IdP）。当然，服务的两个部分——签发令牌与提供用户身份信息——也可能由不同的服务器承担，但对 RP
而言，它们作为一个整体在工作。除此之外，我们还会在访问令牌（access token）之外新增第二种令牌，并使用这个新的 ID 令牌（ID
token）来承载认证事件本身的信息（见图 13.3）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260106141917216.png){ loading=lazy }
  <figcaption>图 13.3 基于 OAuth 的身份验证与身份协议的组成部分</figcaption>
</figure>

RP 现在可以知道用户是谁，以及他们是如何登录的。但为什么这里要用两个 token 呢？我们完全可以把用户信息直接塞进从授权服务器拿到的
token 里，或者提供一个用户信息 API，作为受 OAuth 保护的资源来调用。事实证明，这两种方式同时存在是有价值的。本章稍后我们会看看
OpenID Connect 协议是如何处理这一点的。为此，我们让两个 token 并行使用，稍后会进一步展开一些细节。

## OAuth 2.0 如何使用认证

在上一节中，我们看到可以在授权协议之上构建认证协议。不过同样也要看到，一次 OAuth
交易要想让授权委派流程正常运转，本身就需要发生多种形式的认证：资源所有者需要在授权服务器的授权端点进行认证，客户端需要在授权服务器的
token 端点进行认证，此外根据具体部署方式可能还会有其他认证环节。我们是在授权之上构建认证，而授权协议自身又依赖认证——这是不是有点太复杂了？

这套设计看起来确实有些绕，但注意它可以利用“用户在授权服务器上完成认证”这一事实，同时在整个 OAuth 2.0
协议流程中，终端用户的原始凭据从未传递给客户端应用（我们的
RP）。通过限制各方所需掌握的信息，整个交易可以变得更安全、更不容易出错，并且能够跨越不同的安全域运行。用户只需要直接向单一一方完成认证，客户端也是如此，双方都不需要相互冒充。

以这种方式在授权之上构建认证的另一个重要优势，是可以在运行时征得终端用户同意。让终端用户自行决定把自己的身份信息授予哪些应用，使得基于
OAuth 2.0 的身份协议能够跨越安全域扩展到整个互联网。组织不必事先决定是否允许所有用户去另一个系统登录，而是由每个用户自己决定去哪里登录。这与我们在第
2 章首次提到的 OAuth 2.0“首次使用即信任”（TOFU）模型相契合。

此外，用户还可以在委派身份信息的同时，把对其他受保护 API
的访问权限一并委派出去。一次调用，应用既可以确认用户是否已登录，知道应用该如何称呼该用户，还能下载照片用于打印，并向用户的消息流发布更新。如果某个服务已经提供了受
OAuth 2.0 保护的 API，那么进一步提供认证服务也并非难事。在当今以 API 为驱动的 Web 世界里，这种将包括身份在内的服务叠加提供的能力已被证明非常有用。

这一切都很好地契合了 OAuth 2.0 的访问模型，而这种简洁性确实很有吸引力。不过，当同时获取身份信息与授权信息时，很多开发者会把这两种功能混为一谈。下面我们来看看这种做法常见会引发的一些错误。

## 使用 OAuth 2.0 进行认证的常见陷阱

我们已经说明了：在 OAuth 之上构建一个认证协议是可行的，但实践中有不少地方很容易让人踩坑。这些错误既可能出在身份提供方一侧，也可能出在身份使用方一侧，而大多数问题都源于对协议各个部分含义的误解。

### 将访问令牌当作认证成功的证明

由于资源所有者通常需要先在授权端点完成认证，才会签发访问令牌，因此很容易把“拿到访问令牌”当作认证已经发生且成功的证明。然而，访问令牌本身并不携带任何关于认证事件的信息，甚至也无法说明本次交互过程中是否真的发生过认证。毕竟，这个令牌可能来自一个持续很久的（甚至可能已被劫持的）会话；也可能是针对某些非个人化的
scope 被自动授权后签发的。令牌还可能通过不需要用户交互的 OAuth 2.0 授权类型直接签发给客户端，例如 client
credentials、assertion，或使用 refresh token 的调用。此外，如果客户端对令牌来源缺乏严格校验，该令牌还可能是签发给另一个客户端后被注入进来的（该场景详见
13.4.3 节）。

无论令牌是通过什么方式获得的，客户端都无法仅凭访问令牌判断任何关于用户的信息或其认证状态。这是因为客户端并不是 OAuth 2.0
访问令牌的目标受众。在 OAuth 2.0
中，访问令牌被设计为对客户端不透明；但客户端又需要能够从令牌中推导出一些用户信息。实际上，客户端只是访问令牌的出示者（presenter），而真正的受众（audience）是受保护资源（protected
resource）。

当然，我们也可以定义一种客户端能够解析并理解的令牌格式，让令牌携带用户信息与认证上下文，供客户端读取和校验。但通用的 OAuth
2.0 并未规定访问令牌的具体格式或结构，许多现有的 OAuth
部署也各自采用不同的令牌格式。更重要的是，访问令牌的生命周期往往会长于它所代表的那次认证事件。由于令牌会被传递给受保护资源，其中不少资源与身份无关，因此让这些资源获知用户登录事件等敏感信息也可能带来问题。为了解决这些限制，OpenID
Connect 的 ID Token、Facebook Connect 的 Signed Response 等协议会在访问令牌之外再提供一个辅助令牌，用于将认证信息直接传达给客户端。这样，主要的访问令牌仍可像标准
OAuth 那样对客户端保持不透明，而用于认证的辅助令牌则可以被明确定义并可解析。

### 访问受保护的 API 作为认证证明

即使客户端无法理解令牌的内容，它也始终可以把令牌提交给能够理解它的受保护资源。那么，如果我们定义一个受保护资源，用来告诉客户端是谁签发了这个令牌，会怎样？由于访问令牌可以兑换为一组用户属性，人们很容易认为：只要持有一个有效的访问令牌，就足以证明用户已经通过认证。

这种想法在某些情况下确实成立，但前提是：该访问令牌是在授权服务器对用户完成认证的上下文中刚刚签发的。需要注意的是，在 OAuth
中，获取访问令牌并不只有这一种方式。刷新令牌和断言（assertion）都可以在用户不在场的情况下换取访问令牌；而在某些场景下，甚至可能在用户根本无需认证的情况下就完成授权授予（access
grant）。

此外，访问令牌通常在用户早已不在场之后仍然可用。受保护资源一般无法仅凭令牌本身判断用户是否在场；因为按照 OAuth 2.0
协议的特性，客户端与受保护资源之间的连接上本就不会有用户参与。在许多规模较大的 OAuth 2.0
生态中，用户甚至没有任何途径在受保护资源处完成认证。尽管受保护资源大概率能识别最初授权该令牌的是哪位用户，但要判断该用户当前状态，通常会非常困难。

当授权事件与受保护资源使用令牌之间存在较大的时间间隔时，这个问题尤为突出。OAuth 2.0
在用户不再出现在客户端或授权服务器的情况下依然能正常运作；但认证协议的核心目的在于确认用户是否在场，因此客户端不能依赖“访问令牌还能用”来判断用户是否真的在。客户端可以通过两种方式缓解这一问题：只在明确令牌相对“新鲜”时才去查询用户信息；并且不要因为某个访问令牌能够访问用户
API，就想当然地认为用户在场。另一种对策是，让客户端直接接收一个它只接受来自 IdP 的凭据（artifact），例如上一节讨论的 ID
令牌与已签名请求。这类令牌与访问令牌具有独立的生命周期，其内容可以结合受保护资源提供的额外信息一并使用。

### 访问令牌注入

当客户端从非“向令牌端点发起的明确请求所返回的结果”之外的来源接收访问令牌时，会出现一种额外（且危险）的威胁。对于使用隐式流程（implicit
flow）的客户端来说，这尤其棘手，因为令牌会作为 URL 哈希片段中的参数直接传给客户端。攻击者可以拿到一个访问令牌——可能是来自其他应用的有效令牌，也可能是伪造的令牌——然后把它当作该
RP 请求得到的结果一样，直接塞给正在等待的 RP。即便在纯 OAuth 2.0
场景下这也已经很严重：客户端可能被诱导去访问并非真实资源所有者的资源；而在认证协议中，这几乎是灾难性的，因为攻击者可以复制令牌，并用它们登录到另一个应用。

如果应用的不同部分为了“共享”访问能力而在组件之间传递访问令牌，这个问题同样可能出现。这样做的风险在于，它为外部注入访问令牌以及令牌泄露到应用外部打开了入口。如果客户端应用没有通过某种机制校验访问令牌，它就无法区分一个合法令牌与攻击者注入的令牌。

缓解方式是使用授权码流程（authorization code flow）替代隐式流程，这样客户端只会接受直接来自授权服务器令牌端点的令牌。state
参数允许客户端提供一个攻击者无法猜测的值；如果该参数缺失，或与预期值不一致，客户端就可以轻松将传入的令牌判定为无效并予以拒绝。

### 缺少受众限制

大多数 OAuth 2.0 API
并未对其返回的信息提供任何“受众（audience）限制”机制。也就是说，客户端无法判断某个访问令牌究竟是签发给它的，还是签发给另一个客户端的。攻击者可以把一个（有效的）其他客户端令牌交给一个天真的客户端，让这个天真的客户端去调用用户
API。由于受保护资源并不知道发起调用的客户端身份，只会验证令牌是否有效，因此会返回有效的用户信息。然而，这些信息原本是提供给另一个客户端使用的。用户甚至从未授权过这个天真的客户端，但它却把用户当作已登录来处理。

缓解这一问题的方法是：在向客户端传递认证信息时，同时携带一个客户端能够识别并验证为“属于自己”的标识符。这样客户端就可以区分“为自己签发的认证”与“为其他应用签发的认证”。此外，还可以进一步降低此类攻击风险：在
OAuth 2.0 流程中直接把整套认证信息传递给客户端，而不是通过诸如受 OAuth 2.0 保护的 API
这类二次机制再取回，从而避免在流程后期被注入一组客户端未知且不可信的信息。

### 注入无效的用户信息

如果攻击者能够拦截或劫持客户端发起的某一次调用，就可能在客户端毫无察觉的情况下篡改返回的用户信息内容。这样攻击者就能在一个天真的客户端上冒充某个用户——例如在恰当的调用顺序中替换用户标识符，比如篡改用户信息
API 的返回值，或篡改一个发往客户端的令牌中的相关内容。

缓解该攻击的方法是：在认证信息传递给客户端的过程中，对其进行加密保护并在客户端侧做校验。客户端与授权服务器之间的所有通信通道都必须使用
TLS 保护，并且客户端在连接时需要校验服务器证书。此外，用户信息或令牌（或两者）还可以由服务器进行签名，并由客户端验证。即使攻击者能够劫持通信连接，这一额外的签名也能阻止其篡改或注入用户信息。

### 每个潜在身份提供方各用一套协议

基于 OAuth 2.0 的身份 API 最大的问题之一在于：不同的身份提供方即使都以完全符合标准的 OAuth 为基础，也不可避免会在身份 API
的细节实现上各不相同。比如，某个提供方可能在 user_id 字段里给出用户的唯一标识，而另一个提供方则放在 sub
字段中。尽管这两个字段在语义上等价，但在处理时却需要两套不同的代码路径。即便各提供方的授权流程可能一致，认证信息的传递方式也可能不同。

之所以会出现这个问题，是因为这里讨论的“如何传递认证信息”的机制被明确排除在 OAuth 2.0 的范围之外。OAuth 2.0
不规定具体的令牌格式，不定义访问令牌通用的 scope 集合，也不涉及受保护资源应如何校验访问令牌。因此，可以通过让提供方在 OAuth
标准之上采用一个标准化的认证协议来缓解这一问题——这样无论身份信息来自哪里，都能以一致的方式传输。那么，是否存在这样的标准呢？

## OpenID Connect：构建在 OAuth 2.0 之上的认证与身份标准

OpenID Connect[^3] 是 OpenID Foundation 于 2014 年 2 月发布[^4]的开放标准，定义了一种可互操作的方式，用 OAuth 2.0
来完成用户认证。从本质上说，它就像一份被广泛传播的“巧克力软糖配方”，并已由众多实现者构建与验证。作为开放标准，OpenID Connect
的实现不受许可或知识产权方面的限制。由于该协议以互操作为设计目标，OpenID 客户端应用可以用同一套协议对接多个身份提供方，而不必为每个提供方分别实现略有差异的协议。

[^3]: [http://openid.net/connect/](https://openid.net/developers/how-connect-works/)

[^4]: [http://openid.net/specs/openid-connect-core-1_0.html](https://openid.net/specs/openid-connect-core-1_0.html)

OpenID Connect 直接构建在 OAuth 2.0 之上，并与其保持兼容。在很多场景中，它会与用于保护其他 API 的传统 OAuth 基础设施一同部署。除
OAuth 2.0 之外，OpenID Connect 还使用 JSON Object Signing and Encryption（JOSE）规范套件（我们在第 11
章讨论过），用来在不同位置传递已签名和加密的信息。一个具备 JOSE 能力的 OAuth 2.0 部署，距离成为完全符合 OpenID Connect
的系统已经相当接近，因为两者之间的差异（delta）其实不大。但这点差异却影响深远：OpenID Connect 通过在 OAuth 2.0
基础之上增加几个关键组件，成功规避了前文提到的许多陷阱。

### ID Token

OpenID Connect 的 ID Token 是一个已签名的 JSON Web Token（JWT），会与常规的 OAuth Access Token 一并发给客户端应用。与 Access
Token 不同，ID Token 的目标接收方是 RP（Relying Party），并且设计目的就是让 RP 解析它。

与我们在第 11 章创建的已签名 Access Token 类似，ID Token 包含一组关于认证会话的声明（claims），包括用户标识符（sub）、签发该
Token 的身份提供方标识符（iss），以及该 Token 为之创建的客户端标识符（aud）。此外，ID Token 还包含自身有效时间窗口的信息（通过
exp 和 iat 声明），以及需要传递给客户端的其他认证上下文信息。例如，Token 可以说明用户距离上次被要求进行主要认证（primary
authentication）已经过去了多久（auth_time），或者用户在 IdP 使用了哪种主要认证方式（acr）。ID Token 还可以包含其他声明，既包括第
11 章列出的标准 JWT 声明，也包括 OpenID Connect 协议扩展的声明。表 13.1 中用黑体标出的为必需声明。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260106142545901.png){ loading=lazy }
  <figcaption>表 13.1：ID Token 中的声明</figcaption>
</figure>

ID Token 会在颁发 Access Token 的同时签发，作为令牌端点响应中的 id_token 字段返回，而不是用来替代 Access
Token。这是因为两类令牌面向的受众和用途不同。采用“双令牌”机制后，Access Token 可以像标准 OAuth 一样对客户端保持不透明，而 ID
Token 则允许客户端解析。此外，这两种令牌的生命周期也可以不同，ID Token 往往会更快过期。尽管 ID Token
只代表一次单独的认证事件，并且从不会传递给外部服务，但 Access Token 可能在用户离开很久之后仍可用于获取受保护资源。虽然你仍然可以用
Access Token 去查询最初是谁授权了客户端，但正如你之前所看到的，这并不能说明用户是否仍然在线或当前是否在场。

``` json
{
  "access_token": "987tghjkiu6trfghjuytrghj",
  "token_type": "Bearer",
  "id_token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJodHRwOi8vbG9jY
  Wxob3N0OjkwMDEvIiwic3ViIjoiOVhFMy1KSTM0LTAwMTMyQSIsImF1ZCI6Im9hdXRoLWNsaWVud
  C0xIiwiZXhwIjoxNDQwOTg3NTYxLCJpYXQiOjE0NDA5ODY1NjF9.LC5XJDhxhA5BLcT3VdhyxmMf6
  EmlFM_TpgL4qycbHy7JYsO6j1pGUBmAiXTO4whK1qlUdjR5kUm ICcYa5foJUfdT9xFGDtQhRcG3-
  dOg2oxhX2r7nhCjzUnOIebr5POySGQ8ljT0cLm45edv_rO5fSVPdwYGSa7QGdhB0bJ8KJ__Rs
  yKB707n09y1d92ALwAfaQVoyCjYB0uiZM9Jb8yHsvyMEudvSD5urRuHnGny8YlGDIofP6SXh5-
  1TlR7ST7R7h9f4Pa0lD9SXEzGUG816HjIFOcD4aAJXxn_QMlRGSfL8NlIz29PrZ2xqg8w2w84hBQ
  cgchAmj1TvaT8ogg6w"
}
```

最后，ID Token 本身会由身份提供方的密钥进行签名。除了最初获取该 Token 时所依赖的 TLS 传输层保护之外，这又为其中的声明增加了一层额外防护。由于
ID Token 是由授权服务器签名的，它还提供了一个位置，用于对授权码（c_hash）和访问令牌（at_hash）添加分离式签名（detached
signatures）。客户端可以校验这些哈希值，同时仍让授权码与访问令牌的内容对客户端保持不透明，从而防御一整类注入攻击。

通过对这个 ID Token 做一些简单校验——也就是第 11 章中处理已签名 JWT 时使用的同一套检查——客户端就能保护自己免受大量常见攻击：

1. 解析 ID Token，确保它是一个有效的 JWT，并收集其中的声明。
	1. 以 “.” 字符拆分字符串。
	2. 对各段进行 Base64URL 解码。
	3. 将前两段（Header 与 Payload）按 JSON 解析。
2. 使用 IdP 的公钥验证 Token 签名；该公钥会发布在可发现的位置。
3. 确认该 ID Token 是由受信任的 IdP 签发的。
4. 确保 ID Token 的 audience 列表中包含客户端自身的 client_id。
5. 结合当前时间，确认过期时间（exp）、签发时间（iat）与生效时间（nbf）这些时间戳是合理的。
6. 如果存在 nonce，确保其与先前发送的值一致。
7. 如适用，校验授权码或访问令牌对应的哈希值。

上述每一步都是确定且机械化的，实现起来几乎不需要多少编码工作。OpenID Connect 还有一些更高级的模式支持对 ID Token
进行加密，这会让解析与验证流程略有变化，但最终达到的效果一致。

### UserInfo 端点

由于 ID Token 已经包含处理认证事件所需的全部信息，OpenID Connect
客户端仅凭它就足以处理一次成功登录。不过，访问令牌也可以用于访问一个标准的受保护资源，其中包含当前用户的资料信息，这个资源称为
UserInfo 端点。该端点返回的声明并不属于前面讨论的认证流程本身，而是提供打包好的身份属性，让认证协议对应用开发者更有价值。毕竟，比起说“早上好，9XE3-JI34-00132A”，大家更愿意说“早上好，Alice”。

对 UserInfo 端点的请求很简单：使用 HTTP GET 或 POST，并把访问令牌（不是 ID Token）作为授权信息发送。常规请求不需要输入参数，尽管与
OpenID Connect 的很多能力一样，这里也可以使用一些高级用法。UserInfo 端点采用受保护资源的一种设计：系统中所有用户都对应同一个资源，而不是为每个用户分配不同的资源
URI。IdP 会通过解引用（dereference）访问令牌来判断正在查询的是哪个用户。

``` shell
GET /userinfo HTTP/1.1
Host: localhost:9002
Accept: application/json
```

UserInfo 端点返回的是一个 JSON 对象，其中包含关于用户的各类声明（claims）。这些声明通常会随着时间保持稳定，因此更常见的做法是缓存对
UserInfo 端点调用的结果，而不是在每次认证请求时都重新拉取。借助 OpenID Connect 的高级能力，还可以将 UserInfo 的响应以已签名或加密的
JWT 形式返回。

``` shell
HTTP/1.1 200 OK
Content-type: application/json

{
  "sub": "9XE3-JI34-00132A",
  "preferred_username": "alice",
  "name": "Alice",
  "email": "alice.wonderland@example.com",
  "email_verified": true
}
```

OpenID Connect 使用特殊的 openid scope 值来控制对 UserInfo 端点的访问。OpenID Connect 定义了一组标准化的 OAuth
scope，可映射到这些用户属性的不同子集（profile、email、phone 和 address，如表 13.2 所示），使得普通的 OAuth
交互也能请求完成认证所需的一切信息。OpenID Connect 规范对每个 scope 及其对应的属性映射关系都做了更为详尽的说明。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260106143130156.png){ loading=lazy }
  <figcaption>表 13.2 将 OAuth 作用域映射到 OpenID Connect UserInfo 声明</figcaption>
</figure>

OpenID Connect 定义了一个特殊的 openid scope，用于控制访问令牌对 UserInfo 端点的整体访问权限。OpenID Connect 的 scopes
可以与其他非 OpenID Connect 的 OAuth 2.0 scopes 并行使用而不产生冲突，并且签发的访问令牌除了可用于 UserInfo
端点外，还可能面向多个不同的受保护资源。通过这种方式，OpenID Connect 身份系统可以与 OAuth 2.0 授权系统平滑共存。

### 动态服务器发现与客户端注册

OAuth 2.0 的设计目标是支持多种不同的部署形态，但它在规范层面并未说明这些部署应如何搭建，或各组件如何相互发现与建立关联。在传统的
OAuth 场景中，这一点通常可以接受：一个授权服务器保护某个特定 API，二者往往紧密耦合。OpenID Connect 则定义了一套通用
API，可在丰富多样的客户端与提供方之间部署。若要求每个客户端都必须预先了解每个提供方，显然无法扩展；同样，让每个提供方都掌握所有潜在客户端的信息，也完全不现实。

为了解决这一问题，OpenID Connect 定义了发现协议[^5]，使客户端能够便捷地获取与特定身份提供方交互所需的信息。发现过程分为两步：首先，客户端需要发现
IdP 的 issuer URL。该信息可以直接配置，例如图 13.4 中常见的类似 NASCAR 风格的提供方选择器。

[^5]: [http://openid.net/specs/openid-connect-discovery-1_0.html](https://openid.net/specs/openid-connect-discovery-1_0.html)

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260106143324958.png){ loading=lazy }
  <figcaption>图 13.4：纳斯卡（NASCAR）风格的身份提供商选择器</figcaption>
</figure>

或者，也可以基于 WebFinger 协议来发现颁发方（Issuer）。WebFinger
的做法是：以一种常见的用户标识方式——电子邮箱地址——作为输入，并提供一套确定性的转换规则，把这种面向用户、易读的输入转换为一个用于发现的
URI（见图 13.5）。本质上，你取邮箱标识符的域名部分，在前面加上 https://，再在末尾追加 /.well-known/webfinger，从而构造出一个
URI。你也可以选择性地附带用户最初输入的内容，以及你要查询的信息类型。在 OpenID Connect 中，可以通过 HTTPS 获取这个发现
URI，从而确定某个用户地址对应的颁发方。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260106143420685.png){ loading=lazy }
  <figcaption>图 13.5：WebFinger 将电子邮件地址转换为 URL</figcaption>
</figure>

确定颁发者（issuer）后，客户端仍然需要获取服务器的一些关键信息，例如授权端点（authorization endpoint）和令牌端点（token
endpoint）的位置。获取方式是：在第一步发现的 issuer URI 后追加 `/.well-known/openid-configuration`，并请求生成的
URL。服务器会返回一个 JSON 文档，其中包含客户端启动认证交易所需的全部服务器属性。下面是一个改编自公开测试服务器的示例：

``` json
{
"issuer": "https://example.com/",
"request_parameter_supported": true,
"registration_endpoint": "https://example.com/register",
"token_endpoint": "https://example.com/token",
"token_endpoint_auth_methods_supported":
[ "client_secret_post", "client_secret_basic", "client_secret_jwt", "private_key_jwt", "none" ],
"jwks_uri": "https://example.com/jwk",
"id_token_signing_alg_values_supported":
[ "HS256", "HS384", "HS512", "RS256", "RS384", "RS512", "ES256", "ES384", "ES512", "PS256", "PS384", "PS512", "none" ],
"authorization_endpoint": "https://example.com/authorize",
"introspection_endpoint": "https://example.com/introspect",
"service_documentation": "https://example.com/about",
"response_types_supported":
[ "code", "token" ],
"token_endpoint_auth_signing_alg_values_supported":
[ "HS256", "HS384", "HS512", "RS256", "RS384", "RS512", "ES256", "ES384", "ES512", "PS256", "PS384", "PS512" ],
"revocation_endpoint": "https://example.com/revoke",
"grant_types_supported":
[ "authorization_code", "implicit", "urn:ietf:params:oauth:grant-type:jwt-bearer", "client_credentials", "urn:ietf:params:oauth:grant_type:redelegate" ],
"scopes_supported":
[ "profile", "email", "address", "phone", "offline_access", "openid" ],
"userinfo_endpoint": "https://example.com/userinfo",
"op_tos_uri": "https://example.com/about",
"op_policy_uri": "https://example.com/about",
}
```

一旦客户端了解了服务器，服务器也需要了解客户端。为此，OpenID Connect 定义了客户端注册协议[^6]，使客户端能够向新的身份提供方进行登记并建立信任。第
12 章讨论的 OAuth 动态客户端注册协议扩展与 OpenID Connect 的版本是并行开发的，两者在协议层面（wire-level）彼此兼容。


[^6]: [http://openid.net/specs/openid-connect-registration-1_0.html](https://openid.net/specs/openid-connect-registration-1_0.html)

通过利用发现机制、注册机制、统一的身份 API 以及终端用户的自主选择，OpenID Connect 能够在互联网规模上运行。即便各方事先互不相识，两个符合规范的
OpenID Connect 实例也可以相互交互，在安全边界之间完成授权协议。

### 与 OAuth 2.0 的兼容性

尽管具备强大的认证能力，OpenID Connect 在设计上仍然与纯粹的 OAuth 2.0 兼容。事实上，如果某个服务已经在使用 OAuth 2.0 以及
JOSE 规范（包括 JWT），那么它距离完整支持 OpenID Connect 其实已经相当接近了。

为了帮助构建优秀的客户端应用，OpenID Connect 工作组发布了相关文档，介绍如何使用授权码流程构建一个基础的 OpenID Connect
客户端[^7]，以及如何构建一个隐式（Implicit）模式的 OpenID Connect 客户端[^8]。这两份文档都会带领开发者先搭建一个基础的 OAuth
2.0 客户端，然后再补齐实现 OpenID Connect 所需的少量组件，其中许多内容已在此处介绍过。

[^7]: [http://openid.net/specs/openid-connect-basic-1_0.html](https://openid.net/specs/openid-connect-basic-1_0.html)

[^8]: [http://openid.net/specs/openid-connect-implicit-1_0.html](https://openid.net/specs/openid-connect-implicit-1_0.html)

### 高级能力

尽管 OpenID Connect 规范的核心相当直观，但仅靠基础机制并不能充分覆盖所有使用场景。为支持更多高级用例，OpenID Connect 在标准
OAuth 之上还定义了一系列可选的高级能力。要把这些内容全部深入展开，完全可以再写一本书，[^9] 不过本节至少会带大家了解其中几个关键点。

[^9]: 如果你觉得这是个好主意，请联系我们的出版方并告诉他们！

OpenID Connect 客户端可以选择使用签名 JWT 来进行认证，以替代 OAuth 更传统的共享客户端密钥方式。该 JWT
可以使用客户端的非对称密钥签名（前提是客户端已在服务器上注册其公钥），也可以使用客户端密钥进行对称签名。这种方式能为客户端提供更高的安全性，避免将“密码”在网络中传输。

同样，OpenID Connect 客户端也可以选择将发往授权端点的请求封装为一个签名
JWT，而不是以一组表单参数的形式发送。只要用于签名该请求对象的密钥已在服务器上注册，服务器就能校验请求对象中的参数，并确信这些参数未被浏览器篡改。

OpenID Connect 服务器也可以选择将服务器输出（包括 UserInfo 端点的响应）签名或加密为 JWT。ID Token
除了由服务器签名外，也同样可以被加密。这些保护措施不仅能在 TLS 连接所提供的保障之外，进一步确保客户端确信输出未被篡改。

作为对 OAuth 2.0 端点的扩展，OpenID Connect 还新增了其他参数，包括用于显示类型提示、提示（prompt）行为以及认证上下文引用（ACR）的参数。借助“请求对象”（request
object）机制，OpenID Connect 客户端可以利用其 JSON 负载天然的表达力，向授权服务器发起比 OAuth 2.0
客户端更精细、更可控的请求。这些请求甚至可以包含细粒度的用户声明（claims）信息，例如要求仅允许匹配某个特定标识符的用户登录。

OpenID Connect 还提供了一种由服务器（或其他第三方）发起登录流程的方式。尽管所有规范的 OAuth 2.0
交易都由客户端应用发起，但这一可选特性使客户端能够接收信号，以便与指定的 IdP 启动登录流程。

OpenID Connect 还定义了多种获取令牌的方式，包括混合流（hybrid flow）：其中部分信息（如 ID Token）通过前端通道（front
channel）传递，另一部分信息（如 Access Token）通过后端通道（back channel）传递。这些流程不应被视为对现有 OAuth 2.0
流程的简单拼接，而应理解为面向不同应用场景的新能力。

最后，OpenID Connect 还提供了用于管理 RP 与 IdP 之间会话（甚至多个 RP 之间会话）的规范。由于 OAuth 2.0
除了授权委派发生的那一刻之外，并不具备“用户在线/存在”的概念，因此要处理联邦认证的生命周期，就需要额外的扩展。如果用户从某个
RP 登出，他们可能也希望同步登出其他 RP；此时 RP 需要能够向 IdP 发出信号以触发该行为。其他 RP 也需要能够监听来自 IdP
的登出信号，并据此做出相应处理。

OpenID Connect 在不破坏与 OAuth 2.0 兼容性的前提下，提供了以上所有扩展。

## 构建一个简单的 OpenID Connect 系统

打开 ch-13-ex-1，你会看到一个完全可用的 OAuth 2.0 系统。接下来，我们将在现有的 OAuth 2.0 基础设施之上构建一个简单的 OpenID
Connect 系统。尽管仅实现 OpenID Connect 的全部功能就足以写一本书，但在本练习中我们只覆盖基础内容。我们将为授权服务器的授权码（authorization
code）流程增加签发 ID Token 的支持。同时，我们会在受保护资源中实现一个 UserInfo 端点，并与授权服务器共享数据库，因为这是一种很常见的部署模式。注意，虽然授权服务器与
UserInfo 端点运行在不同的进程中，但从 RP 的视角来看，它们作为一个统一的 IdP 在工作。我们还会把通用的 OAuth 2.0 客户端改造成
OpenID Connect 的 RP：通过解析并校验 ID Token，并拉取 UserInfo 用于展示。

在所有这些练习里，我们都刻意省略了一个关键组件：用户认证。相反，我们再次沿用一个简单的下拉选择框，在授权页面上决定哪个用户“登录”到了
IdP——就像第 3 章里做的那样。在生产系统中，IdP
所采用的主认证机制至关重要，因为服务器签发的联邦身份完全依赖于它。市面上有很多优秀的主认证库，把它们集成进我们的框架就留作读者练习。不过还是要强调一句：请不要在生产系统中使用简单的下拉框作为认证机制。

### 生成 ID Token

首先，我们需要生成一个 ID Token，并在下发 Access Token 的同时一并返回。我们会沿用第 11 章使用的库和方法，因为 ID Token
本质上就是一种特殊的 JWT。如果你想了解 JWT 的细节，请回到第 11 章查看。

用编辑器打开 authorizationServer.js。在文件靠近顶部的位置，我们已经为系统中的两个用户 Alice 和 Bob 提供了用户信息。创建 ID
Token 和 UserInfo 响应时都会用到这些信息。为简化起见，我们采用了一个简单的内存变量，以用户名为索引；用户名可在授权页面的下拉菜单中选择。在生产环境中，这通常会对接数据库、目录服务或其他持久化存储。

``` javascript
var userInfo = {

  "alice": {
       "sub": "9XE3-JI34-00132A",
       "preferred_username": "alice",
       "name": "Alice",
       "email": "alice.wonderland@example.com",
       "email_verified": true
  },

  "bob": {
       "sub": "1ZT5-OE63-57383B",
       "preferred_username": "bob",
       "name": "Bob",
       "email": "bob.loblob@example.net",
       "email_verified": false
  }

};
```

接下来，我们会在创建好访问令牌（access token）之后再生成 ID 令牌（ID token）。首先要判断是否需要生成 ID 令牌：只有当用户授权了
`openid` scope，并且确实存在对应的用户信息时，才会生成 ID 令牌。

``` javascript
if (__.contains(code.scope, 'openid') && code.user) {
```

接下来，我们将为 ID Token
创建一个头部，并补齐载荷（payload）所需的全部字段。首先，将授权服务器设置为签发方（issuer），并加入用户的主体标识符（subject）。请记住，这两个字段组合在一起，可以为用户提供一个全局唯一的标识。然后，把发起请求的客户端的
Client ID 设为该 Token 的受众（audience）。最后，为 Token 添加时间戳，并将过期时间设置为五分钟后。通常这已经足够让 ID Token 在
RP 侧完成处理并绑定到用户会话。别忘了，RP 不需要在任何外部资源上使用 ID Token，因此超时时间可以且应当相对较短。

``` javascript
var header = { 'typ': 'JWT', 'alg': rsaKey.alg, 'kid': rsaKey.kid };

var ipayload = {
   iss: 'http://localhost:9001/',
   sub: code.user.sub,
   aud: client.client_id,
   iat: Math.floor(Date.now() / 1000),
   exp: Math.floor(Date.now() / 1000) + (5 * 60)
};
```

我们还会把 nonce 值一并加入进来，但前提是客户端在最初向授权端点发起请求时携带了该值。它在很多方面都类似于 state
参数，不过针对的是另一种略有不同的跨站攻击向量。

``` javascript
if (code.request.nonce) {
  ipayload.nonce = code.request.nonce;
}
```

然后我们会用服务器的密钥对其进行签名，并将其序列化为 JWT。

``` javascript
var privateKey = jose.KEYUTIL.getKey(rsaKey);
var id_token = jose.jws.JWS.sign(header.alg, JSON.stringify(header),
  JSON.stringify(ipayload), privateKey);
```

最后，我们将通过修改现有的令牌响应，将其与访问令牌一并下发。

``` javascript
token_response.id_token = id_token;
```

这就完成了我们需要做的全部工作。虽然如果愿意，我们也可以把 ID Token 和其他令牌一起存起来，但它从不会再被传回授权服务器或任何受保护资源；因此其实没什么必要。它并不像
Access Token 那样使用，而更像是授权服务器发给客户端的一份“断言”。一旦把它交给客户端，我们基本就不再需要处理它了。

### 创建 UserInfo 端点

接下来，我们要在受保护资源中加入 UserInfo 端点。打开 protectedResource.js 来完成这一部分练习。注意，尽管在 OpenID 协议里 IdP
是一个单一的逻辑组件，但像我们这样把它拆成多个独立服务器来实现，同样是可接受且合法的。我们从之前的练习中引入了
getAccessToken 和 requireAccessToken 这两个辅助函数。它们会使用本地数据库，不仅查询令牌信息，也会查出与该令牌关联的用户信息。我们的
IdP 将在 /userinfo 上响应 HTTP GET 或 POST 请求，返回用户信息。由于我们代码中使用的 Express.js
框架存在一些限制，我们需要和之前的练习略有不同：用一个外部命名的函数变量来定义处理器代码，但最终效果与之前基本一致。

``` javascript
var userInfoEndpoint = function(req, res) {

};

app.get('/userinfo', getAccessToken, requireAccessToken, userInfoEndpoint);
app.post('/userinfo', getAccessToken, requireAccessToken, userInfoEndpoint);
```

接下来，我们会检查传入的令牌是否至少包含 `openid` scope；如果没有，就返回错误。

``` javascript
if (!__.contains(req.access_token.scope, 'openid')) {
  res.status(403).end();
  return;
}
```

我们再次需要从数据存储中获取正确的用户信息。我们会以授权该访问令牌的用户为依据来获取数据，这和第 4
章某个练习中分发信息的方式类似。如果找不到对应用户，我们就返回错误。

``` javascript
var user = req.access_token.user;
if (!user) {
  res.status(404).end();
  return;
}
```

接下来需要构建响应。我们不能直接返回完整的用户信息对象，因为用户可能只授权了可用 scope 的一部分。由于每个 scope
都对应用户信息中的一部分字段，我们会遍历访问令牌中的每个 scope，并在遍历过程中将对应的 claims 逐项加入到输出对象中。

``` javascript
var out = {};
__.each(req.access_token.scope, function (scope) {
  if (scope == 'openid') {
      __.each(['sub'], function(claim) {
            if (user[claim]) {
                  out[claim] = user[claim];
            }
      });
  } else if (scope == 'profile') {
      __.each(['name', 'family_name', 'given_name', 'middle_name',
  'nickname', 'preferred_username', 'profile', 'picture', 'website',
  'gender', 'birthdate', 'zoneinfo', 'locale', 'updated_at'],
  function(claim) {
            if (user[claim]) {
                  out[claim] = user[claim];
            }
      });
  } else if (scope == 'email') {
      __.each(['email', 'email_verified'], function(claim) {
            if (user[claim]) {
                  out[claim] = user[claim];
            }
      });
  } else if (scope == 'address') {
      __.each(['address'], function(claim) {
            if (user[claim]) {
                  out[claim] = user[claim];
            }
      });
  } else if (scope == 'phone') {
      __.each(['phone_number', 'phone_number_verified'], function(claim) {
            if (user[claim]) {
                  out[claim] = user[claim];
            }
      });
  }
});
```

最终得到的是一个对象，其中包含了对应用户为该客户端授权的、该用户的所有声明（claims）。这一流程在隐私、安全以及用户自主选择方面提供了极高的灵活性。我们会以
JSON 的形式返回该对象。

``` javascript
res.status(200).json(out);
return;
```

最终的函数如附录 B 的清单 14 所示。

只需做两处小改动，我们就把这个功能完备的 OAuth 2.0 服务器也升级成了一个 OpenID Connect 身份提供方（IdP）。我们复用了前面章节中已经探讨过的许多组件，例如
JWT 生成（第 11 章）、入站访问令牌处理（第 4 章）以及作用域扫描（第 4 章）。OpenID Connect 还有很多我们之前提到过的扩展特性，包括请求对象（request
objects）、发现（discovery）和注册（registration），但这些功能的实现就留给读者作为练习（或者留给另一本书的读者）。

### 解析 ID Token

现在服务器已经能生成 ID Token，客户端也需要能够解析它。我们将采用与第 11 章类似的方法：在受保护资源端解析并校验
JWT。这一次，令牌的受众是客户端，因此我们会在编辑器里打开 client.js 开始处理。我们已经把客户端与服务器的彼此信息做了静态配置；但在
OpenID Connect 中，这些都可以通过动态客户端注册（dynamic client registration）和服务器发现（server
discovery）来动态完成。作为额外练习，可以把第 12 章中的动态客户端注册代码引入，并在这个框架之上实现服务器发现功能。

首先，我们需要从令牌响应中取出该令牌的值。由于它传给我们的结构与访问令牌相同，我们会在令牌响应解析函数里从对应对象上把它取出来。同时，我们也会清理掉上一次登录可能残留的旧用户信息或旧的
ID Token。

``` javascript
if (body.id_token) {
  userInfo = null;
  id_token = null;
```

之后，我们会把 ID token 的 payload 解析成一个 JSON 对象，并对 ID token 的内容进行校验，从签名开始。在 OpenID Connect
中，客户端通常会从 JSON Web Key（JWK）Set 的 URL
拉取服务器的密钥；不过我们已经在代码里将其与服务器配置一起以静态方式提供。作为一个额外练习，你可以配置服务器对外发布其公钥，并配置客户端在运行时需要时再去拉取服务器的密钥。我们的服务器为
ID token 使用 RS256 签名算法，我们也像第 11 章一样，使用 jsrsasign 库来处理 JOSE 相关功能。

``` javascript
var pubKey = jose.KEYUTIL.getKey(rsaKey);
var tokenParts = body.id_token.split('.');
var payload = JSON.parse(base64url.decode(tokenParts[1]));
if (jose.jws.JWS.verify(body.id_token, pubKey, [rsaKey.alg])) {
```

接下来，我们需要检查其中几个字段，确保它们都合理。同样，我们把每个校验都拆分成各自的嵌套 `if` 语句，只有在所有校验都通过时才接受该
token。首先，我们会确认签发者（issuer）与我们的授权服务器一致，同时也要确保 audience 列表中包含我们的客户端 ID。

``` javascript
if (payload.iss == 'http://localhost:9001/') {
  if ((Array.isArray(payload.aud) && __.contains(payload.aud,
  client.client_id)) ||
      payload.aud == client.client_id) {
```

然后我们会确保签发时间和过期时间戳都合理。

``` javascript
var now = Math.floor(Date.now() / 1000);
if (payload.iat <= now) {
  if (payload.exp >= now) {
```

还有一些额外的测试会用到协议里更高级的用法，比如：如果我们在原始请求中发送过 nonce，就需要把返回的 nonce
值拿来比对；或者为访问令牌（access token）或授权码（code）计算并校验哈希值。这些测试对于使用授权码（authorization
code）授权类型的简单客户端来说并非必需，因此就留作读者练习。

当且仅当以上所有检查全部通过时，我们才能认为拿到了一个有效的 ID 令牌，并将其保存到应用中。实际上，既然令牌已经校验过了，我们也没必要再保存完整的令牌，所以接下来只保存它的
payload 部分，方便之后访问：

``` javascript
id_token = payload;
```

在整个应用中，我们可以将 ID Token 里的 id_token.iss 与 id_token.sub
组合起来，作为当前用户的全局唯一标识。这种做法比使用用户名或邮箱地址更不容易发生冲突，因为发行方（issuer）的 URL 会天然地为
subject 字段中的值划定作用域。拿到 ID Token 后，我们会把用户跳转到一个备用展示页面，用于显示他们已以当前用户身份成功登录。

``` javascript
res.render('userinfo', {userInfo: userInfo, id_token: id_token});
return;
```

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260106150214422.png){ loading=lazy }
  <figcaption>图 13.6：客户端页面显示已登录用户</figcaption>
</figure>

这会生成一个界面，展示 issuer 和 subject，并提供一个按钮，用于拉取当前用户的 UserInfo。最终的处理函数如附录 B 中的清单 15
所示。

### 获取 UserInfo

在我们处理完认证事件之后，往往还想了解更多用户信息，而不仅仅是一个机器可读的唯一标识符。为了获取用户的资料信息（例如姓名、邮箱地址等），我们会使用在
OAuth 2.0 流程中拿到的 access token，调用 IdP 上的 UserInfo 端点。这个 access token 也可能还能用于访问其他资源，不过这里我们将重点放在它与
UserInfo 端点的配合使用上。

我们不会在认证完成后立刻自动下载用户信息，而是让 RP 只在需要时才去调用 UserInfo 端点。在本应用中，我们会把结果保存到
userInfo 对象中，并渲染到网页上。

项目里已经为你准备好了渲染模板，所以我们先从在客户端为 /userinfo 创建一个处理函数开始。

``` javascript
app.get('/userinfo', function(req, res) {

});
```

该调用与其他受 OAuth 2.0 保护的资源访问方式一致。在这个具体场景中，我们会发起一次 HTTP GET 请求，并将访问令牌放在
Authorization 请求头中。

``` javascript
var headers = {
  'Authorization': 'Bearer ' + access_token
};

var resource = request('GET', authServer.userInfoEndpoint,
  {headers: headers}
);
```

UserInfo 端点会返回一个 JSON 对象，我们可以按需保存并进行处理。如果收到成功响应，我们就把用户信息保存下来，并交给渲染模板；否则，就显示一个错误页面。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260106151129318.png){ loading=lazy }
  <figcaption>图 13.7 客户端显示成功登录并获取用户信息</figcaption>
</figure>

``` javascript
if (resource.statusCode >= 200 && resource.statusCode < 300) {
  var body = JSON.parse(resource.getBody());

  userInfo = body;

  res.render('userinfo', {userInfo: userInfo, id_token: id_token});
  return;
} else {
  res.render('error', {error: 'Unable to fetch user information'});
  return;
}
```

这样你就会得到一个页面，大致会像图 13.7 所示的那样。到这里就完成了。你可以尝试授权不同的 scope，看看从端点返回的数据会有什么变化。如果你以前写过
OAuth 2.0 客户端（你在第 3 章就写过了），那这些内容应该都会显得很简单——这也是理所当然的：OpenID Connect 从一开始就是被设计成构建在
OAuth 2.0 之上的东西。

作为额外练习，把客户端的 /userinfo 页面接起来，让它必须在有效的 OpenID Connect
登录后才能访问。也就是说，当有人访问该页面时，客户端本地必须已经存有用户信息，并且需要同时具备一个有效的 ID token
以及一个可用于拉取用户信息的 access token；如果没有，客户端就会自动启动认证协议流程。

## 小结

很多人误以为 OAuth 2.0 是一种认证协议，但现在你已经了解事实真相了。

- OAuth 2.0 不是认证协议，但它可以用来构建认证协议。
- 目前在 Web 上已经有许多基于 OAuth 2.0 构建的认证协议在使用，其中大多数都与特定提供方绑定。
- 认证协议的设计者在 OAuth 2.0 之上常犯许多典型错误；通过谨慎的协议设计，这些错误是可以避免的。
- 只需加入几个关键扩展，OAuth 2.0 授权服务器与受保护资源就能充当身份提供方，而 OAuth 2.0 客户端则可以充当依赖方（relying
  party）。
- OpenID Connect 提供了一个设计严谨的开放标准认证协议，构建在 OAuth 2.0 之上。

既然我们已经看过一个构建在 OAuth 2.0 之上的重要协议，接下来就更深入地看看另外几个用于解决更高级使用场景的协议。

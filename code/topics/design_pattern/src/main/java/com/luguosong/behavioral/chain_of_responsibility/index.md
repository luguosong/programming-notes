# 责任链

## 意图

允许你将请求沿着处理者链进行发送。收到请求后，每个处理者均可对请求进行处理，或将其传递给链上的下个处理者。

## 结构

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409251414491.png){ loading=lazy }
  <figcaption>责任链</figcaption>
</figure>

1. `处理者（Handler）`声明了所有具体处理者的通用接口。该接口通常仅包含单个方法用于请求处理，但有时其还会包含一个设置链上下个处理者的方法。
2. `基础处理者（Base Handler）`
   是一个可选的类，你可以将所有处理者共用的样本代码放置在其中。在通常情况下，该类中定义了一个保存对于下个处理者引用的成员变量。客户端可通过将处理者传递给上个处理者的构造函数或设定方法来创建链。该类还可以实现默认的处理行为：确定下个处理者存在后再将请求传递给它。
3. `具体处理者（Concrete Handlers）`包含处理请求的实际代码。每个处理者接收到请求后，都必须决定是否进行处理，以及是否沿着链传递请求。处理者通常是独立且不可变的，需要通过构造函数一次性地获得所有必要地数据。
4. `客户端（Client）`可根据程序逻辑一次性或者动态地生成链。值得注意的是，请求可发送给链上的任意一个处理者，而非必须是第一个处理者。

## 场景分析

### 需求描述

开发一个在线`订购系统`。你希望对系统访问进行限制，`只允许认证用户`创建订单。此外，`拥有管理权限的用户`也拥有所有订单的完全访问权限。

这些检查需要依次进行。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409251456471.png){ loading=lazy }
  <figcaption>请求必须经过一系列检查后才能由订购系统来处理。</figcaption>
</figure>

### 问题描述

过了一段时间，需求增加：

- 接将原始数据传递给订购系统存在安全隐患。需要新增了额外的验证步骤来清理请求中的数据。
- 无法抵御暴力密码破解方式的攻击。需要添加了一个检查步骤来过滤来自同一 IP 地址的重复错误请求。
- 可以对包含同样数据的重复请求返回缓存中的结果，从而提高系统响应速度。需要新增一个检查步骤，确保只有没有满足条件的缓存结果时请求才能通过并被发送给系统。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409251500680.png){ loading=lazy }
  <figcaption>代码变得越来越多，也越来越混乱。</figcaption>
</figure>

### 解决方案

`责任链模式`会将特定行为转换为被称作`处理者`的独立对象。在上述示例中，每个检查步骤都可被抽取为`仅有单个方法的类`
，并执行检查操作。将这些`处理者`连成一条`链`。`链`上的每个`处理者`都有一个`成员变量`来保存对于`下一处理者的引用`。

`处理者`可以决定不再沿着链传递请求，这可高效地取消所有后续处理步骤。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409251524277.png){ loading=lazy }
  <figcaption>处理者依次排列，组成一条链。</figcaption>
</figure>

### 代码实现

#### 基础处理者

``` java title="Middleware.java"
--8<-- "code/topics/design_pattern/src/main/java/com/luguosong/behavioral/chain_of_responsibility/middleware/Middleware.java"
```

#### 具体处理者

=== "检查请求数量限制"

	``` java title="检查请求数量限制"
	--8<-- "code/topics/design_pattern/src/main/java/com/luguosong/behavioral/chain_of_responsibility/middleware/impl/ThrottlingMiddleware.java"
	```

=== "检查用户登录信息"

	``` java title="检查用户登录信息"
	--8<-- "code/topics/design_pattern/src/main/java/com/luguosong/behavioral/chain_of_responsibility/middleware/impl/UserExistsMiddleware.java"
	```

=== "检查用户角色"

	``` java title="检查用户角色"
	--8<-- "code/topics/design_pattern/src/main/java/com/luguosong/behavioral/chain_of_responsibility/middleware/impl/RoleCheckMiddleware.java"
	```

#### 客户端

=== "客户端代码"

	``` java title="客户端代码"
	--8<-- "code/topics/design_pattern/src/main/java/com/luguosong/behavioral/chain_of_responsibility/Demo.java"
	```

=== "服务模拟"

	``` java title="授权目标"
	--8<-- "code/topics/design_pattern/src/main/java/com/luguosong/behavioral/chain_of_responsibility/server/Server.java"
	```

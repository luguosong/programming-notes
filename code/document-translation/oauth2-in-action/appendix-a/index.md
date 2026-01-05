# 附录 A：我们的代码框架简介

## 我们的代码框架简介

在本书中，我们将使用运行在 [Node.js](https://nodejs.org/zh-cn)（服务端 JavaScript
引擎）上的 [Express.js](https://expressjs.com/) Web 应用框架，以 JavaScript 开发应用。尽管示例本身会用 JavaScript
编写，但其中的所有概念都可以很方便地迁移到其他平台和应用框架。在条件允许的情况下，我们尽量把 JavaScript
的一些特性（例如闭包和函数回调）与您将直接接触的代码部分隔离开来，因为本书的目标并不是把您培养成熟练的 JavaScript
程序员。与此同时，示例中还会使用一些与 OAuth 无关功能的库代码，让您能够把注意力集中在本书的核心目标上：深入理解 OAuth
协议的工作原理。

在真实应用中，通常更适合使用 OAuth 库来处理我们在这里将要手动编写的许多功能。不过在本书中，我们会选择从零开始手写实现，方便你直接上手
OAuth 的核心机制，而不至于被 Node.js
应用的各种细节带偏。本书中的所有代码都已发布在 [GitHub](https://github.com/oauthinaction/oauth-in-action-code/)
以及本书[出版社的网站](https://www.manning.com/books/oauth-2-in-action)上。每个练习都放在单独的目录中，并按章节号和示例编号进行归类整理。

让我们开始吧。首先，在你能运行任何东西之前，你需要先在你的平台上安装 Node.js 和 Node 包管理器（NPM）。具体步骤因系统而异；例如，在使用
MacPorts 的 MacOSX 系统上，可以通过以下命令进行安装：

```shell
> sudo port install node
> sudo port install npm
```

你可以通过分别查看它们的版本号来确认是否已正确安装；执行后会输出类似下面这样的信息：

```shell
> node -v
v14.13.1
> npm -v
6.14.8
```

安装好这些核心库后，我们就可以解压示例代码了。进入 ap-A-ex-0 目录，运行 npm install 命令安装该示例的项目依赖。该操作会下载示例所需的依赖，并将其安装到 node_modules 目录下。npm 会自动列出为满足本项目依赖而安装的所有包，输出内容大致如下：

``` shell
ap-A-ex-0> npm install
underscore@1.8.3 node_modules/underscore

body-parser@1.13.2 node_modules/body-parser
    content-type@1.0.1
    bytes@2.1.0
There's a lot of information that will print to your console here; we're not going to copy everything.

    send@0.13.0 (destroy@1.0.3, statuses@1.2.1, ms@0.7.1, mime@1.3.4, http-errors@1.3.1)
    accepts@1.2.11 (negotiator@0.5.3, mime-types@2.1.3)
    type-is@1.6.5 (media-typer@0.3.0, mime-types@2.1.3)
```

完成这些操作后，你现在应该已经有了一个目录，其中包含本示例所需的全部代码。

!!! note

	npm install 这一步必须为每个练习分别单独执行。

每个练习都包含三个 JavaScript 源文件：client.js、authorizationServer.js 和 protectedResource.js，以及其他用于支撑运行的文件和库。上述每个文件都需要用 node 命令单独启动，我们建议分别在不同的终端窗口中运行，以免日志输出混在一起导致难以分辨。启动顺序无所谓，但为了让大多数示例程序正常工作，它们必须同时保持运行。

例如，运行客户端应用程序后，终端窗口中应输出类似以下内容：

```shell
> node client.js
OAuth Client is listening at http://127.0.0.1:9000
```

授权服务器是这样启动的：

```shell
> node authorizationServer.js
OAuth Authorization Server is listening at http://127.0.0.1:9001
```

受保护的资源是这样启动的：

```shell
> node protectedResource.js
OAuth Protected Resource is listening at http://127.0.0.1:9002
```

我们建议在三个不同的终端窗口中分别运行它们，这样就能实时看到程序运行时的输出。参见图 A.1。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251231110333224.png){ loading=lazy }
  <figcaption>图 A.1 三个终端窗口并行运行各个组件</figcaption>
</figure>

每个组件都配置为在 localhost 上的不同端口运行，并在独立的进程中启动：

- OAuth 客户端应用（client.js）运行在 http://localhost:9000/  
- OAuth 授权服务器应用（authorizationServer.js）运行在 http://localhost:9001/  
- OAuth 受保护资源应用（protectedResource.js）运行在 http://localhost:9002/

所有应用都已配置为提供静态文件服务，例如图片和层叠样式表（CSS）。这些静态资源位于项目的 files 目录中，在任何练习中都不需要编辑。另外，files 目录中还包含 HTML 模板。应用会使用这些模板，根据可变输入生成 HTML 页面。使用模板时，会在应用启动时通过以下代码进行初始化：

```javascript
app.engine('html', cons.underscore);
app.set('view engine', 'html');
app.set('views', 'files');
```

在练习过程中，这些模板不需要修改，但我们会不时回顾它们，用来演示相关功能。我们在所有示例中使用 [Underscore.js](https://underscorejs.org/) 的模板系统，并配合 [Consolidate.js](https://github.com/tj/consolidate.js) 库来创建和管理全部模板。你可以向这些模板传入变量，然后通过响应对象上的 render 调用来渲染输出，例如：

```javascript
res.render('index', {access_token: access_token});
```

第一个示例中的这三个代码文件并不包含任何实际功能，但只要你能分别看到它们的欢迎页，就说明应用已经正常运行，并且依赖也已安装完成。比如，在本机用浏览器访问 OAuth Client 的地址 http://localhost:9000/，应该会看到图 A.2 中展示的页面。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251231112027370.png){ loading=lazy }
  <figcaption>图 A.2 客户端壳层的首页</figcaption>
</figure>

同样，位于 `http://localhost:9001/ `的授权服务器界面会如图 A.3 所示。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251231112111590.png){ loading=lazy }
  <figcaption>图 A.3 授权服务器外壳的首页</figcaption>
</figure>

最后，访问 http://localhost:9002/ 上的受保护资源时，会显示图 A.4 所示的内容（请注意，受保护资源通常不会包含面向用户的界面组件）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251231112154027.png){ loading=lazy }
  <figcaption>图 A.4 受保护资源 Shell 的首页</figcaption>
</figure>

要在应用中添加 HTTP 处理器，我们需要把它们作为路由注册到 Express.js 的应用对象上。对于每个路由，我们需要告诉应用：要监听哪些 HTTP 方法、要匹配哪些 URL 模式，以及当这些条件满足时该调用哪个函数。该函数会接收 request 和 response 两个对象作为参数。比如下面这个示例会监听 /foo 上的 HTTP GET 请求，并在匹配时调用给定的匿名函数。

```javascript
app.get('/foo', function (req, res) {

});
```

在本书的练习中，我们会遵循一个约定：始终把请求对象称为 req，把响应对象称为 res。请求对象包含传入 HTTP 请求的相关信息，包括请求头、URL、查询参数，以及与该请求对应的其他内容。响应对象用于在 HTTP 响应中把信息返回给客户端，包括状态码、响应头、响应体等。

我们会在各个文件的顶部用全局变量来保存大量有状态的信息。在任何像样的 Web 应用里，这些状态都应该绑定到用户的会话（session）上，而不是放在应用本身的全局变量中。原生应用通常也会采用与我们这个框架类似的思路，依赖宿主操作系统提供的能力来完成本地用户会话认证。

在整本书中，你都会使用这个简单框架来构建 OAuth 客户端、受保护资源（Protected Resource）以及授权服务器（Authorization Server）。大多数情况下，每个练习都会把整体框架搭得差不多了，你只需要补齐我们在该练习中讨论的那一小部分与 OAuth 相关的功能即可。

对于每个练习，你都可以在练习目录下的 completed 目录中找到完成版代码。如果你卡住了，我们建议打开这些文件，看看“官方”答案是如何实现的。

最后，尽管练习中的代码符合所有相关标准，我们仍强烈建议你不要把它用于生产系统。

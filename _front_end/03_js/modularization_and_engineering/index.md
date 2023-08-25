---
layout: note
title: 模块化和工程化
nav_order: 60
parent: JavaScript
create_time: 2023/8/3
---

# 文件划分方式模块化

每个文件就是一个独立的模块，使用script标签将模块引入到页面中。

{: .warning-title}
> 存在的问题
>
> - 污染全局作用域，模块中的内容可以被任意访问和修改
> - 命名冲突问题
> - 无法管理模块依赖关系

页面调用：

{% highlight html %}
{% include_relative modularization_with_file/index.html %}
{% endhighlight %}

模块a：

{% highlight js %}
{% include_relative modularization_with_file/module_a.js %}
{% endhighlight %}

模块b:

{% highlight js %}
{% include_relative modularization_with_file/module_b.js %}
{% endhighlight %}

# 命名空间方式模块化

每个模块只暴露一个`全局的对象`，所有的成员都挂载到这个对象下面。

{: .warning-title}
> 存在的问题
>
> - 依然污染全局作用域，模块中的内容可以被任意访问和修改
> - 模块之间的依赖关系也没有得到解决

页面调用：

{% highlight html %}
{% include_relative modularization_with_global_objects/index.html %}
{% endhighlight %}

模块a：

{% highlight js %}
{% include_relative modularization_with_global_objects/module_a.js %}
{% endhighlight %}

模块b:

{% highlight js %}
{% include_relative modularization_with_global_objects/module_b.js %}
{% endhighlight %}

# IIFE方式模块化

使用立即执行函数为函数提供私有空间。将模块中每一个成员都放在一个函数提供的私有作用域当中。对于需要暴露给外部的成员，可以挂载到全局对象上。

页面调用：

{% highlight html %}
{% include_relative modularization_with_iife/index.html %}
{% endhighlight %}

模块a：

{% highlight js %}
{% include_relative modularization_with_iife/module_a.js %}
{% endhighlight %}

模块b:

{% highlight js %}
{% include_relative modularization_with_iife/module_b.js %}
{% endhighlight %}

# AMD模块化规范

`AMD(Asynchronous Module Definition)`是一种异步模块定义规范，用来规定模块的写法。

用于实现`浏览器端`模块化

`Require.js`是一个模块加载器，可以实现异步加载模块。

- 每个模块需要通过`define`函数来定义
    - 参数1：模块名称
    - 参数2：依赖列表
    - 参数3：回调函数
- 每个模块需要通过`require`函数来加载
    - 参数1：依赖列表
    - 参数2：回调函数

# CMD模块化规范

`CMD(Common Module Definition)`是一种通用模块定义规范，用来规定模块的写法。

用于实现`浏览器端`模块化

`Sea.js`是一个模块加载器，可以实现异步加载模块。

# nodejs中的模块化

也就是`commonjs规范`，nodejs中的模块化规范。

- 一个文件就是一个模块
- 每个模块都有自己的作用域
- 通过`module.exports`对象，可以将模块中的成员暴露到外部
- 在模块内部，可以使用`require`方法加载其他模块

{: .note}
> 底层其实就是将js文件包装成了一个函数
>
> 函数中传入了5个参数，分别是：
> - `exports`：用于将模块中的成员暴露到外部,指向`module.exports`
> - `require`：用于加载其他模块
> - `module`：代表当前模块本身
> - `__filename`：当前模块的绝对路径
> - `__dirname`：当前模块所在的目录的绝对路径

{: .note-title}
> exports和module.exports的区别
>
> 在Node.js中，`exports`和`module.exports`都是用于导出模块的对象。它们的区别在于，`exports`是`module.exports`
> 的一个引用，而`module.exports`
> 是真正的导出对象。在模块初始时，它们指向同一块内存区域。如果你想要导出一个对象，你可以直接给`module.exports`
> 赋值，或者给`exports`赋值。但是如果你想要导出一个函数或者类，你必须给`module.exports`赋值。因为当你给`exports`
> 赋值时，它会断开与`module.exports`的引用关系，从而无法导出函数或类。

{: .warning-title}
> 浏览器端使用commonjs规范存在的问题
>
> commonjs规范会在启动时加载所有模块，会导致页面加载速度变慢，用户体验差。
>
> node中不存在这样的问

{: .warning}
> 在2013年5月，Node.js包管理器npm的作者Isaac Z. Schlueter，宣布Node.js已经废弃了CommonJS，Node.js核心开发者应避免使用它

自定义模块1：

{% highlight js %}
{% include_relative modularization_with_commonjs/other1.js %}
{% endhighlight %}

自定义模块2：

{% highlight js %}
{% include_relative modularization_with_commonjs/other2.js %}
{% endhighlight %}

模块调用：

{% highlight js %}
{% include_relative modularization_with_commonjs/main.js %}
{% endhighlight %}

# ES Module规范

`ES Module`是`ES6`中新增的模块化规范，它在语言的层面上实现了模块化。

- 每个js文件都是一个独立的模块
- 导入其它模块的成员使用`import`关键字
- 导出本模块成员使用`export`关键字

{: .note-title}
> node中使用ES Module的前提条件
>
> 1. node版本必须是14.15.1以上
> 2. package.json中必须有`type`字段，且值为`module`

默认导出：

{% highlight js %}
{% include_relative modularization_with_es_module/other1.js %}
{% endhighlight %}

按需导出：

{% highlight js %}
{% include_relative modularization_with_es_module/other2.js %}
{% endhighlight %}

js中放js代码，main.js导入后立即执行：

{% highlight js %}
{% include_relative modularization_with_es_module/other3.js %}
{% endhighlight %}

main.js导入模块，并调用：

{% highlight js %}
{% include_relative modularization_with_es_module/main.js %}
{% endhighlight %}

# webpack

## 概述

`webpack`是一个基于NodeJS的静态模块打包器，可以将多个模块打包成一个文件。

目的：

- 减少文件数量，减少HTTP请求次数，提高页面加载速度
- 减少文件体积，提高页面加载速度
- 便于传输，加快用户访问速度，提高用户体验
- 减轻服务器压力

## 打包文件js

安装webpack和webpack-cli：

```shell
# 开发环境下安装
yarn add webpack webpack-cli -D

# 也可以全局安装
yarn global add webpack webpack-cli
```

配置webpack.config.js文件：

```javascript
const path = require('path');

module.exports = {
    entry: './src/index.js', // 指定打包的入口文件
    output: {
        filename: 'bundle.js', // 指定打包后的文件名
        path: path.resolve(__dirname, 'dist') // 指定打包后的文件所在的目录
    },
    mode: 'development' // 指定打包的模式，development：开发模式，代码不压缩。production：生产模式（默认）,代码会经过高度压缩和混淆
};
```

在package.json中添加打包命令：

```json
{
  "scripts": {
    "build": "webpack --config webpack.config.js"
  }
}
```

执行打包命令：

```shell
yarn build
```

## html-webpack-plugin

将index.html文件拷贝到打包目录，并且自动引入打包后的js文件。

安装html-webpack-plugin：

```shell
# 开发环境下安装
yarn add html-webpack-plugin -D
```

配置webpack.config.js文件：

```javascript
const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = {
    entry: './src/index.js', // 指定打包的入口文件
    output: {
        filename: 'bundle.js', // 指定打包后的文件名
        path: path.resolve(__dirname, 'dist') // 指定打包后的文件所在的目录
    },
    mode: 'development', // 指定打包的模式，development：开发模式，代码不压缩。production：生产模式（默认）,代码会经过高度压缩和混淆
    plugins: [
        new HtmlWebpackPlugin({
            template: './public/index.html' // ⭐指定要拷贝的html文件
        })
    ]
};
```

## css文件打包

webpack默认只认识js和json，如果要打包css，需要

安装css-loader和style-loader：

```shell
# 开发环境下安装
yarn add css-loader style-loader -D
```

编写css：

```css
#content {
    color: red;
}
```

在js中引入css:

```javascript
import "./css/index.css"
```

配置webpack.config.js文件：

```javascript
module.exports = {
    //其它配置
    module: {
        rules: [
            {
                test: /\.css$/, // ⭐指定要打包的文件类型
                //先使用css-loader加载css文件，再使用style-loader将css代码以style标签的形式插入到页面中
                use: ["style-loader", "css-loader"], // ⭐指定使用的loader
            },
        ],
    },
}
```

## 打包时将css独立出来

上面css样式被一起打包到了js文件中，如果想要将css样式独立出来，需要使用`mini-css-extract-plugin`插件。

安装mini-css-extract-plugin：

```shell
# 开发环境下安装
yarn add mini-css-extract-plugin -D
```

配置webpack.config.js文件：

```javascript
const MiniCssExtractPlugin = require("mini-css-extract-plugin");

module.exports = {
    //其它配置
    module: {
        rules: [
            {
                test: /\.css$/, // 指定要打包的文件类型
                //先使用css-loader加载css文件，再使用style-loader将css代码以style标签的形式插入到页面中
                use: [MiniCssExtractPlugin.loader, "css-loader"], // 指定使用的loader
            },
        ],
    },
    plugins: [
        new MiniCssExtractPlugin({
            filename: "css/index.css", // 指定打包后的css文件名
        }),
    ],
}
```

## less文件打包

安装less和less-loader：

```shell
# 开发环境下安装
yarn add less less-loader -D
```

编写less：

```less
#content {
  font-style: italic; // 斜体
}
```

在js中引入less:

```javascript
import "./less/index.less"
```

配置webpack.config.js文件：

```javascript
module.exports = {
    //其它配置
    module: {
        rules: [
            {
                test: /\.less$/, // 指定要打包的文件类型
                //先使用less-loader将less代码转换为css代码，再使用css-loader加载css文件，再使用style-loader将css代码以style标签的形式插入到页面中
                use: [MiniCssExtractPlugin.loader, "css-loader", "less-loader"], // 指定使用的loader
            },
        ],
    },
}
```

## 打包图片

webpack5内部集成了asset模块，可以用来打包图片。无需安装。

配置webpack.config.js文件：

```javascript
module.exports = {
    module: {
        rules: [
            {
                test: /\.(png|jpg|gif)$/, // 指定要打包的文件类型
                type: "asset", // 指定使用的loader
                generator: {
                  filename: "img/[name].[hash:6][ext]", // 指定打包后的文件名
                },
            },
        ],
    },
}
```

## 打包字体图标

配置webpack.config.js文件：

```javascript
module.exports = {
    module: {
        rules: [
            {
                test: /\.(eot|svg|ttf|woff|woff2)$/, // 指定要打包的文件类型
                type: "asset", // 指定使用的loader
                generator: {
                  filename: "font/[name].[hash:6][ext]", // 指定打包后的文件名
                },
            },
        ],
    },
}
```

## 高版本js转低版本

安装babel：

```shell
# 开发环境下安装
yarn add @babel/core @babel/preset-env babel-loader -D
```

配置webpack.config.js文件：

```javascript
module.exports = {
    module: {
        rules: [
            {
                test: /\.js$/, // 指定要打包的文件类型
                execlude: /node_modules/, // 指定不需要打包的文件
                use: {
                    loader: "babel-loader", // 指定使用的loader
                    options: {
                        presets: ["@babel/preset-env"], // 指定要使用的babel插件
                    },
                },
            },
        ],
    },
}
```

## 配置开发服务器

安装webpack-dev-server：

```shell
# 开发环境下安装
yarn add webpack-dev-server -D
```

配置webpack.config.js文件：

```javascript
module.exports = {
    devServer: {
        port: 3000, // 指定端口号
        open: true, // 自动打开浏览器
    },
}
```

在package.json中添加启动命令：

```json
{
  "scripts": {
    "dev": "webpack-dev-server --config webpack.config.js"
  }
}
```

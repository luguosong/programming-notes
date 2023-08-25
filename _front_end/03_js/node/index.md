---
layout: note
title: Nodejs
nav_order: 50
parent: JavaScript
create_time: 2023/8/16
---

# 浏览器和node对比

浏览器结构图：

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202308181450594-%E6%B5%8F%E8%A7%88%E5%99%A8v8%E5%BC%95%E6%93%8E%E7%BB%93%E6%9E%84%E5%9B%BE.png)

node结构图：

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202308181452784-node%E7%BB%93%E6%9E%84%E5%9B%BE.png)

# idea配置

在使用idea开发时，编写代码，比如`global模块`、`__dirname`、`__filename`等，idea会提示找不到这些变量。进行如下配置：

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202308181631869-node-idea%E9%85%8D%E7%BD%AE.png)

# global模块

`global模块`类似于浏览器中的`window对象`，它包含了一些常用的方法和属性。

- 与windows相同的属性和方法
    - `console`
    - `setTimeout`
    - `setInterval`
    - `clearTimeout`
    - `clearInterval`
- 与windows不同的属性和方法
    - `process`：用于获取当前进程信息
    - `Buffer`：用于操作二进制数据
    - `require`：用于加载模块
    - `__dirname`：当前文件所在的目录
    - `__filename`：当前文件的绝对路径

{% highlight js %}
{% include_relative global.js %}
{% endhighlight %}

# fs模块

{: .note-title}
> fs模块
>
> file system
>
> 文件系统,用于操作文件

{% highlight js %}
{% include_relative fs/fs.js %}
{% endhighlight %}

# path模块

{% highlight js %}
{% include_relative path.js %}
{% endhighlight %}

# http模块

{% highlight js %}
{% include_relative http.js %}
{% endhighlight %}

# npm

## 简介

{: .note-title}
> npm
>
> node package manager,node包管理器
>
> 用于管理node包

## 常用命令

- `npm root -g`：查看全局安装的包的安装路径
- `npm list -g --depth 0`：查看全局安装的包
- `npm init`:初始化一个项目，生成package.json文件
  - `-y`：跳过向导，使用默认配置
- `npm install 库名@版本号`:安装所有依赖包
  - `-g`：全局安装
  - `--save`：将依赖包信息保存到package.json文件中的dependencies属性中
  - `--save-dev`：将依赖包信息保存到package.json文件中的devDependencies属性中
- `npm uninstall 库名`:卸载依赖包
  - `-g`：全局卸载

## 更改镜像源

1. 全局安装`nrm`：`npm install nrm -g`
2. 查看可用的镜像源：`nrm ls`
3. 切换镜像源：`nrm use 镜像源名称`

# yarn

与npm类似，用于管理node包

```shell
# 安装yarn
npm install --global yarn

# 全局安装路径
yarn global bin

# 初始化项目
yarn init

# 安装依赖
# -D：开发依赖
yarn add 包名@版本号

# 卸载依赖
yarn remove 包名
```

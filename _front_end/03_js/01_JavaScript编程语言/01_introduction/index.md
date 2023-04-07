---
layout: note
title: 简介
nav_order: 10
parent: JavaScript编程语言
grand_parent: javascript
---

# 什么是 JavaScript？

JavaScript源码被成为`脚本`，需要特殊的准备或编译即可运行

可以在`浏览器`中执行，也可以在`服务器`中执行，甚至任意搭载了`JavaScript 引擎`的设备

# JavaScript引擎

- `V8引擎`：由Google开发，用于Google Chrome浏览器和Node.js运行时环境。它是目前最快的JavaScript引擎之一，以其快速的编译速度和高效的内存管理而著称。
- `SpiderMonkey引擎`：由Mozilla开发，用于Mozilla Firefox浏览器。它是第一个JavaScript引擎，也是开源社区中使用最广泛的引擎之一。
- `JavaScriptCore引擎`：由苹果公司开发，用于Safari浏览器和iOS操作系统。它是WebKit浏览器引擎的一部分。
- `Chakra引擎`：由微软开发，用于Microsoft Edge浏览器和Internet Explorer浏览器。它以其快速的启动时间和优秀的性能而著称。

{: .note-title}
> 引擎工作原理
> 
> 1. 引擎读取脚本
> 2. 引擎将脚本转为机器语言
> 3. 机器代码快速执行

# JavaScript上层语言

- `CoffeeScript` 是 JavaScript 的一种语法糖。它引入了更加简短的语法，使我们可以编写更清晰简洁的代码。通常，Ruby 开发者喜欢它。
- `TypeScript` 专注于添加“严格的数据类型”以简化开发，以更好地支持复杂系统的开发。由微软开发。
- `Flow` 也添加了数据类型，但是以一种不同的方式。由 Facebook 开发。
- `Dart` 是一门独立的语言。它拥有自己的引擎，该引擎可以在非浏览器环境中运行（例如手机应用），它也可以被编译成 JavaScript。由 Google 开发。
- `Brython` 是一个 Python 到 JavaScript 的转译器，让我们可以在不使用 JavaScript 的情况下，以纯 Python 编写应用程序。
- `Kotlin` 是一个现代、简洁且安全的编程语言，编写出的应用程序可以在浏览器和 Node 环境中运行。

# 规范和手册

- ECMA-262 规范 
- [MDN（Mozilla）JavaScript 索引](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference)

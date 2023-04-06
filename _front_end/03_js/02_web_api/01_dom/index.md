---
layout: default
title: DOM
nav_order: 10
parent: WebApi
grand_parent: javascript
---

# 简介

`DOM（文档对象模型）`是JavaScript操作HTML和XML文档的API。通过DOM，JavaScript可以对文档中的任何元素进行访问、操作和修改，例如添加、删除或修改页面的文本、样式、结构和事件处理程序。

`DOM`将文档表示为一个`树形结构`，称为`节点树`，其中每个节点代表文档中的一个元素、属性或文本片段。树的根节点称为`文档节点`，它是整个文档的`父节点`。文档节点下的子节点包括HTML元素节点、文本节点和注释节点等。

JavaScript可以使用DOM API查询和访问文档中的节点，例如`getElementById()`，`getElementsByTagName()`和`getElementsByClassName()`等方法。通过这些方法可以获取元素的属性和内容，并且可以动态地修改元素的属性和内容，从而实现动态的网页效果。

除此之外，JavaScript还可以在DOM节点上添加`事件处理程序`，例如`click`、`mouseover`和`keydown`等事件，以响应用户的交互操作。在事件处理程序中，可以访问事件对象，从而获取有关事件的详细信息，例如点击的元素、按下的键以及鼠标的位置等。

总的来说，DOM是JavaScript的核心API之一，它使JavaScript能够与`HTML和XML文档进行交互`，并且可以让开发者创建丰富的交互式和动态的`Web应用程序`。

# 获取元素

## 方法说明

- `document.getElementById('elementId')`：通过id获取元素。
- `document.getElementsByTagName('tagName')`：通过标签名获取元素。
- `document.getElementsByClassName('className')`：通过类名获取元素。
- `document.getElementsByName('elementName')`：通过名称获取表单元素。
- `document.querySelector('selector')`：通过选择器获取元素（获取匹配到的第一个元素）。
- `document.querySelectorAll('selector')`：通过选择器获取元素（获取匹配到的所有元素）。

## 示例

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
<div id="div1" class="text">hello</div>
<div id="div2" class="text">world</div>
<script>
    // 根据Id获取元素
    let div1 = document.getElementById("div1");
    // 根据标签名获取元素的集合
    let divs1 = document.getElementsByTagName("div");
    // 根据类名获取元素的集合
    let divs2 = document.getElementsByClassName("text");
    //获取符合条件的第一个元素
    let div2 = document.querySelector(".text");
    let div3 = document.querySelector("div");
    let div4 = document.querySelector("#div2");
    //获取符合条件的所有元素对象
    let divs3 = document.querySelectorAll("div");
    //获取body元素
    document.body;
    //获取html元素对象
    document.documentElement
</script>
</body>
</html>
```

# 事件

## DOM事件流

{: .note}
> `addEventListener`方法的第三个参数是一个布尔值，用来指定事件是在`冒泡阶段`还是`捕获阶段`被处理。如果参数是`false`或者`省略`，那么事件将在`冒泡阶段`被处理；如果参数是`true`，那么事件将在`捕获阶段`被处理。

![](https://cdn.jsdelivr.net/gh/guosonglu/images@master/blog-img/20230316233145.png)

## 事件委托

{: .note}
> 事件委托是一种在 JavaScript 中，将事件处理程序附加到多个元素的技术。通过将事件处理程序添加到它们的共同`祖先元素`上，而不是将它们添加到每个具体元素上，当事件从子元素`冒泡到`祖先元素时，`祖先元素`的事件处理程序会检查事件源，并执行相应的操作。

## 事件对象

在JavaScript中，当一个事件发生时，浏览器会创建一个`事件对象`来描述该事件。事件对象包含关于事件的信息，例如`事件类型`、触发事件的元素、鼠标的位置等等。通过事件对象，可以访问事件的相关信息并执行适当的操作。

| 属性/方法      | 描述                                                        |
| -------------- | ----------------------------------------------------------- |
| target         | 返回触发事件的元素                                           |
| type           | 返回事件类型                                                |
| preventDefault | `阻止事件的默认行为`                                          |
| stopPropagation| `阻止事件的冒泡传播`                                          |
| addEventListener| 在事件目标上注册事件监听器，可以指定事件类型、处理程序函数和其他选项 |
| removeEventListener| 从事件目标中删除事件侦听器，必须指定要删除的事件类型和处理程序函数 |
| dispatchEvent  | 从事件目标派发事件                                           |
| event.clientX  | 返回鼠标指针相对于浏览器窗口可视区域的水平坐标              |
| event.clientY  | 返回鼠标指针相对于浏览器窗口可视区域的垂直坐标              |
| event.keyCode  | 返回按下的键的键码值                                         |
| event.target   | 返回触发事件的元素                                           |
| event.preventDefault | 阻止事件的默认行为                                       |
| event.stopPropagation| 阻止事件的冒泡传播                                       |


## 鼠标事件



# 操作元素

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>操作元素</title>
    <style>
        div:hover {
            cursor: pointer;
        }
    </style>
</head>
<body>
<div id="div1"></div>
<div id="div2"></div>
<div id="div3">改变元素属性</div>

<script>
    //修改元素内容
    let div1 = document.querySelector("#div1");
    div1.innerText = "当前时间：" + new Date();  //写
    console.log(div1.innerText) //读

    let div2 = document.querySelector("#div2");
    div2.innerHTML = "当前时间：<strong>" + new Date() + "</strong>";  //写
    console.log(div2.innerHTML) //读


    //设置元素属性
    let div3 = document.querySelector("#div3");
    div3.style.color = "red";
</script>
</body>
</html>
```

# 节点操作

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
<div id="parent">
    <span class="child">h</span>
    <span class="child">e</span>
    <span class="child">l</span>
    <span class="child">l</span>
</div>

<script>
    /*获取父节点*/
    let child = document.querySelector(".child");
    //获取父节点
    let parentNode = child.parentNode;


    /*获取子元素集合*/
    let father = document.querySelector("#parent");
    //获取所有节点，包含文件和元素
    let childNodes1 = father.childNodes;
    //获取所有标签节点
    let childNodes2 = father.children;

    /*获取第一个子节点*/
    //获取第一个节点，包括文本和元素
    let firstChild = father.firstChild;
    //第一个子元素节点（IE9以上支持）
    let firstElementChild = father.firstElementChild;
    //获取第一个子元素节点
    let child1 = father.children[0];

    /*返回最后一个子节点*/
    //获取最后一个节点，包括文件节点和元素
    let lastChild = father.lastChild;
    //最后一个子元素节点（IE9以上支持）
    let lastElementChild = father.lastElementChild;
    //获取最后一个元素节点
    let child2 = father.children[childNodes2.length - 1];


    /*下一个兄弟节点*/
    //下一个节点，包含文本和元素
    let nextSibling = firstElementChild.nextSibling;
    //下一个元素节点（IE9以上支持）
    let nextElementSibling = firstElementChild.nextElementSibling;


    /*上一个兄弟节点*/
    //下一个节点，包含文本和元素
    let previousSibling = lastElementChild.previousSibling;
    //上一个元素节点（IE9以上支持）
    let previousElementSibling = lastElementChild.previousElementSibling;

    /*创建并添加节点*/
    // 在末尾添加
    let span1 = document.createElement("span");
    span1.innerHTML = "o";
    father.appendChild(span1);
    // 在指定元素之前添加
    let span2 = span1.cloneNode();  //克隆节点
    father.insertBefore(span2, father.children[0]);

    /*删除节点*/
    father.removeChild(father.children[0])
</script>
</body>
</html>
```



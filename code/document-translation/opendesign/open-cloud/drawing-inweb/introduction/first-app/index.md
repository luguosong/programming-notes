# 创建你的第一个 DrawingWeb SDK 应用

本指南将带你从零开始，使用 DrawingWeb SDK 创建你的第一个应用。DrawingWeb SDK 是一个强大的 C++
库，通过交叉编译为WebAssembly，可在浏览器中实现图纸的渲染与操作。

## 简介

DrawingWeb SDK 可帮助你构建 Web 应用，用于打开、查看并与多种图纸格式进行交互。本教程将演示如何搭建一个基础应用，实现图纸的加载、显示以及浏览导航。

## 前置条件

- 具备 HTML、CSS 和 JavaScript 的基础知识
- 用于测试的 Web 服务器（或本地开发环境）

## 搭建 HTML 结构

首先，我们来为应用创建 HTML 结构：

``` html
<!DOCTYPE html>
<html lang="en-us">
  <head>
    <meta charset="utf-8" />
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <title>DrawingInWeb</title>
    <style>
      body {
        font-family: arial;
        margin: 0;
        padding: none;
        display: flex;
        flex-direction: column;
        height: 100vh;
        width: 100vw;
      }

      .emscripten {
        padding-right: 0;
        margin-left: auto;
        margin-right: auto;
        display: block;
      }

      div.emscripten {
        text-align: center;
      }

      div.emscripten_border {
        border: 1px solid black;
      }

      .canvas-holder {
        flex-grow: 1;
        overflow: hidden;
      }

      /* 画布绝对不能设置任何边框或内边距，否则鼠标坐标会不准确 */
      canvas.emscripten {
        border: 0px none;
        background-color: black;
        width: 100%;
        height: 100%;
      }
    </style>
  </head>

  <body>
    <div>
      <div class="emscripten_border">
        <input type="file" /><button id="download">Download</button>
      </div>
    </div>
    <div class="canvas-holder">
      <canvas
        class="emscripten"
        id="canvas"
        oncontextmenu="event.preventDefault()"
        tabindex="-1"
      ></canvas>
    </div>

    <!-- 你的 JavaScript 代码将写在这里 -->
    <script>
      // 此处将添加 JavaScript 代码
    </script>

    <!-- 加载 DrawingWeb SDK -->
    <script
      async
      type="text/javascript"
      src="https://cloud.opendesign.com/examples/fps/DrawingWeb.js"
    ></script>
  </body>
</html>

```

该 HTML 结构包含：

- 用于加载绘图的文件输入控件
- 用于保存绘图的下载按钮
- 用于显示绘图的 canvas 画布元素
- 引用云端服务器上的 DrawingWeb SDK

## 实现 JavaScript 逻辑

接下来，我们来添加 JavaScript 代码，用于初始化并使用 DrawingWeb SDK：

``` javascript
var Module = {
  FS: {},
  arguments: [],
  preRun: [],
  ASSETS_FOLDER: '/assets',
  preRun: [
    function () {
      FS.mkdir(Module.ASSETS_FOLDER);
    },
  ],
  postRun: [
    function () {
      // ------------------- 初始化 FS 系统 --------------
      function FileToArrayBuffer(file) {
        return new Promise((resolve) => {
          var reader = new FileReader();
          reader.onload = (ev) => resolve(ev.target.result);
          reader.readAsArrayBuffer(file);
        });
      }
      // -------------------------------------------------------

      // ------------------- 初始化 ODA 库 ----------------
      Module.canvas = document.querySelector('canvas');

      var appCore = new Module.App();
      //-------------

      function OpenFile(name) {
        var time = Date.now();
        app.OpenFile(name);
        Resize();
        app.ZoomExtents();
        app.Update();
      }
      document.getElementById('download').onclick = () => {
        ReadFile(
          Module.ASSETS_FOLDER +
            '/' +
            document.querySelector("input[type='file']").files[0].name
        );
      };
      function ReadFile(name) {
        var buf = FS.readFile(name);
        var blob = new Blob([buf.buffer]);
        var url = window.URL.createObjectURL(blob);
        var a = document.createElement('a');
        a.style.display = 'none';
        a.href = url;
        a.download = name;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        console.log('Downloading start');
      }

      function Resize(ev) {
        Module.canvas.height = Module.canvas.clientHeight;
        Module.canvas.width = Module.canvas.clientWidth;
        app.Resize(Module.canvas.width, Module.canvas.height);
      }

      window.onresize = Resize;

      Module.canvas.onwheel = function (ev) {
        app.Zoom(-ev.deltaY * 0.01, ev.offsetX, ev.offsetY);
        ev.preventDefault();
      };

      Module.canvas.onmousedown = function (ev) {
        if (ev.buttons === 4) {
          app.ZoomExtents();
          ev.preventDefault();
        }
      };

      Module.canvas.onmousemove = function (ev) {
        switch (ev.buttons) {
          case 1:
            app.Dolly(ev.movementX, ev.movementY);
            break;
          case 2:
            app.Orbit(ev.movementX, ev.movementY);
            break;
          default:
            break;
        }
      };

      // ---- 动画帧循环
      function render() {
        requestAnimationFrame(render);
        app.Update();
      }
      render();
      // ---- 结束动画帧循环

      document.querySelector("input[type='file']").onchange = function (ev) {
        const file = ev.target.files[0];
        if (file) {
          const name = file.name;
          FileToArrayBuffer(file)
            .then((arraybuffer) => new Uint8Array(arraybuffer))
            .then((array) => {
              const { exists } = FS.analyzePath(
                Module.ASSETS_FOLDER + '/' + name
              );
              if (!exists)
                Module.FS_createDataFile(
                  Module.ASSETS_FOLDER,
                  name,
                  array,
                  true,
                  true,
                  true
                );
            })
            .then(() => OpenFile(Module.ASSETS_FOLDER + '/' + name))
            .catch((err) => console.error(err));
        }
      };
      // ------------------- 结束初始化 ODA 库 ----------------
    },
  ],

  canvas: (function () {
    var canvas = document.getElementById('canvas');

    // 作为默认的初始行为，当 WebGL 上下文丢失时会弹出一个警告框。为了让你的
	// 应用更加健壮，在正式发布前你可能需要重写这一行为！
	// 参见 http://www.khronos.org/registry/webgl/specs/latest/1.0/#5.15.2
    canvas.addEventListener(
      'webglcontextlost',
      function (e) {
        alert('WebGL context lost. You will need to reload the page.');
        e.preventDefault();
      },
      false
    );

    return canvas;
  })(),
};

```

## 理解代码

### 模块配置

DrawingWeb SDK 使用 Emscripten 框架将 C++ 代码编译为 WebAssembly。Module 对象是一个配置对象，Emscripten 会用它来初始化并配置运行环境：

``` javascript
var Module = {
  FS: {},
  arguments: [],
  preRun: [],
  ASSETS_FOLDER: '/assets',
  // ...
};

```

### 文件系统初始化

在 preRun 数组中，我们搭建了一个虚拟文件系统，用于存储和管理绘图文件：

``` javascript
preRun: [
  function () {
    FS.mkdir(Module.ASSETS_FOLDER);
  },
],

```

### 应用初始化

在 postRun 数组中，我们会在 WebAssembly 模块加载完成后初始化 DrawingWeb 应用：

``` javascript
postRun: [
  function () {
    // ...
    Module.canvas = document.querySelector('canvas');
    var appCore = new Module.App();
    // ...
  }
],

```

### 文件处理

本应用提供用于打开和保存绘图文件的功能：

``` javascript
function OpenFile(name) {
  var time = Date.now();
  app.OpenFile(name);
  Resize();
  app.ZoomExtents();
  app.Update();
}

function ReadFile(name) {
  var buf = FS.readFile(name);
  var blob = new Blob([buf.buffer]);
  var url = window.URL.createObjectURL(blob);
  var a = document.createElement('a');
  a.style.display = 'none';
  a.href = url;
  a.download = name;
  document.body.appendChild(a);
  a.click();
  window.URL.revokeObjectURL(url);
  console.log('Downloading start');
}

```

### 画布交互

应用已配置用于绘图交互的事件处理器：

- 滚轮：放大/缩小
- 鼠标左键（按键 1）：平移（Dolly）
- 鼠标右键（按键 2）：环绕旋转（Orbit）
- 鼠标中键（按键 4）：缩放至全图（Zoom Extents）

``` javascript
Module.canvas.onwheel = function (ev) {
  app.Zoom(-ev.deltaY * 0.01, ev.offsetX, ev.offsetY);
  ev.preventDefault();
};

Module.canvas.onmousedown = function (ev) {
  if (ev.buttons === 4) {
    app.ZoomExtents();
    ev.preventDefault();
  }
};

Module.canvas.onmousemove = function (ev) {
  switch (ev.buttons) {
    case 1:
      app.Dolly(ev.movementX, ev.movementY);
      break;
    case 2:
      app.Orbit(ev.movementX, ev.movementY);
      break;
    default:
      break;
  }
};

```

### 渲染循环

应用程序使用 requestAnimationFrame 持续更新绘制：

``` javascript
function render() {
  requestAnimationFrame(render);
  app.Update();
}
render();

```

## 关键 API 函数

DrawingWeb SDK 提供了多项核心函数，用于与图纸进行交互：

| 函数                        | 描述                     |
|---------------------------|------------------------|
| app.OpenFile(name)        | 从虚拟文件系统中打开图纸文件         |
| app.ZoomExtents()         | 缩放以使整个图纸适配视口           |
| app.Update()              | 刷新图纸显示                 |
| app.Resize(width, height) | 调整图纸视口大小               |
| app.Zoom(factor, x, y)    | 在指定坐标处按给定倍率进行缩放（放大或缩小） |
| app.Dolly(dx, dy)         | 按指定偏移量平移图纸             |
| app.Orbit(dx, dy)         | 围绕图纸旋转相机视角             |

## 运行应用程序

要运行该应用程序：

1. 将 HTML 和 JavaScript 代码保存为一个文件（例如：index.html）
2. 将该文件部署到 Web 服务器上（本地或远程均可）
3. 在 Web 浏览器中打开该页面
4. 使用文件输入控件加载图纸文件
5. 使用鼠标与图纸进行交互

## 支持的文件格式

DrawingWeb SDK 支持多种图纸格式，包括：

- DWG
- DXF
- DGN
- 以及更多

## 结论

本教程演示了如何使用 DrawingWeb SDK 创建一个基础的 Web 应用程序。你可以在此基础上继续扩展更多功能，例如图层管理、实体选择，以及更高级的图纸操作等。








 





  





# HTML5

## H5-布局标签

- `header`: 头部
- `footer`: 底部
- `nav`: 导航
- `article`: 文章
- `section`: 区块
- `aside`: 侧边栏
- `main`: 主要内容,WHATWG没有定义,但是W3C定义了
- `hgroup`: 标题组,W3G将其删除

``` html title="H5-布局标签"
--8<-- "code/front-end/html/html5/h5_layout.html"
```

[示例 :material-cursor-default-click-outline:](h5_layout.html){ .md-button .md-button--primary }

## H5-状态标签

``` html title="H5-状态标签"
--8<-- "code/front-end/html/html5/h5_meter.html"
```

[示例 :material-cursor-default-click-outline:](h5_meter.html){ .md-button .md-button--primary }

## H5-搜索框关键字提示

``` html title="H5-搜索框关键字提示"
--8<-- "code/front-end/html/html5/h5_datalist.html"
```

[示例 :material-cursor-default-click-outline:](h5_datalist.html){ .md-button .md-button--primary }

## H5-详细信息展现元素

``` html title="H5-详细信息展现元素"
--8<-- "code/front-end/html/html5/h5_details.html"
```

[示例 :material-cursor-default-click-outline:](h5_details.html){ .md-button .md-button--primary }

## H5-文本标签

``` html title="H5-文本标签"
--8<-- "code/front-end/html/html5/h5_text.html"
```

[示例 :material-cursor-default-click-outline:](h5_text.html){ .md-button .md-button--primary }

## H5-表单相关

- `placeholder`: 提示文字
- `required`: 必填项
- `autofocus`: 自动聚焦
- `autocomplete`: 自动填充
- `pattern`: 正则表达式

``` html title="H5-表单相关"
--8<-- "code/front-end/html/html5/h5_form.html"
```

[示例 :material-cursor-default-click-outline:](h5_form.html){ .md-button .md-button--primary }

## H5-视频标签

``` html title="H5-视频标签"
--8<-- "code/front-end/html/html5/h5_video.html"
```

[示例 :material-cursor-default-click-outline:](h5_video.html){ .md-button .md-button--primary }

## H5-音频标签

``` html title="H5-音频标签"
--8<-- "code/front-end/html/html5/h5_audio.html"
```

[示例 :material-cursor-default-click-outline:](h5_audio.html){ .md-button .md-button--primary }

## H5兼容性问题

引入[html5shiv.js](https://github.com/aFarkas/html5shiv)

```html
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Your Page Title</title>
  <!--设置IE总是使用最新的文档模式进行渲染-->
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <!--优先使用webkit内核进行渲染-->
  <meta name="renderer" content="webkit">
  <!-- 添加有条件的 IE 版本检查并加载 html5shiv.js -->
  <!--[if lt IE 9]>
    <script src="path/to/html5shiv.js"></script>
  <![endif]-->
</head>
<body>
<!-- Your content goes here -->
</body>
</html>
```

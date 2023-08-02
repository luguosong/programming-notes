---
layout: note
title: HTML
nav_order: 10
create_time: 2023/4/11
---

# 注释

{% highlight html %}
{% include_relative comment.html %}
{% endhighlight %}

[效果](comment.html)

<iframe src="comment.html"></iframe>

# 排版相关标签

{% highlight html %}
{% include_relative typography.html %}
{% endhighlight %}

[效果](typography.html)

<iframe src="typography.html"></iframe>

# 文本标签

{% highlight html %}
{% include_relative text.html %}
{% endhighlight %}

[效果](text.html)

<iframe src="text.html"></iframe>

# 图片标签

{% highlight html %}
{% include_relative image.html %}
{% endhighlight %}

[效果](image.html)

<iframe src="image.html"></iframe>

# 超链接标签

{% highlight html %}
{% include_relative hyperlinks.html %}
{% endhighlight %}

[效果](hyperlinks.html)

<iframe src="hyperlinks.html"></iframe>

# 列表标签

{% highlight html %}
{% include_relative list.html %}
{% endhighlight %}

[效果](list.html)

<iframe src="list.html"></iframe>

# 表格标签

{% highlight html %}
{% include_relative table.html %}
{% endhighlight %}

[效果](table.html)

<iframe src="table.html"></iframe>

# 辅助标签

{% highlight html %}
{% include_relative auxiliary.html %}
{% endhighlight %}

[效果](auxiliary.html)

<iframe src="auxiliary.html"></iframe>

# 表单

用途：收集用户的数据。

{% highlight html %}
{% include_relative form.html %}
{% endhighlight %}

[效果](form.html)

<iframe src="form.html"></iframe>

# 框架标签

{% highlight html %}
{% include_relative frame.html %}
{% endhighlight %}

[效果](frame.html)

<iframe src="frame.html"></iframe>

# 字符实体

{% highlight html %}
{% include_relative entity.html %}
{% endhighlight %}

<iframe src="entity.html"></iframe>

# meta原信息

{% highlight html %}
{% include_relative meta.html %}
{% endhighlight %}

# H5-布局标签

- `header`: 头部
- `footer`: 底部
- `nav`: 导航
- `article`: 文章
- `section`: 区块
- `aside`: 侧边栏
- `main`: 主要内容,WHATWG没有定义,但是W3C定义了
- `hgroup`: 标题组,W3G将其删除

{% highlight html %}
{% include_relative h5_layout.html %}
{% endhighlight %}

<iframe src="h5_layout.html"></iframe>

# H5-状态标签

{% highlight html %}
{% include_relative h5_meter.html %}
{% endhighlight %}

<iframe src="h5_meter.html"></iframe>

# H5-搜索框关键字提示

{% highlight html %}
{% include_relative h5_datalist.html %}
{% endhighlight %}

<iframe src="h5_datalist.html"></iframe>

# H5-详细信息展现元素

{% highlight html %}
{% include_relative h5_details.html %}
{% endhighlight %}

<iframe src="h5_details.html"></iframe>


# H5-文本标签

{% highlight html %}
{% include_relative h5_text.html %}
{% endhighlight %}

<iframe src="h5_text.html"></iframe>

# H5-表单相关

- `placeholder`: 提示文字
- `required`: 必填项
- `autofocus`: 自动聚焦
- `autocomplete`: 自动填充
- `pattern`: 正则表达式

{% highlight html %}
{% include_relative h5_form.html %}
{% endhighlight %}

<iframe src="h5_form.html"></iframe>

# H5-视频标签

{% highlight html %}
{% include_relative h5_video.html %}
{% endhighlight %}

<iframe src="h5_video.html"></iframe>

# H5-音频标签

{% highlight html %}
{% include_relative h5_audio.html %}
{% endhighlight %}

<iframe src="h5_audio.html"></iframe>

# H5兼容性问题

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

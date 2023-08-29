---
layout: note
title: CSS
nav_order: 20
create_time: 2023/7/13
---

# CSS的书写位置

- 内联样式：在标签的style属性中书写
- 内部样式：在head标签中书写
- 外部样式：在外部css文件中书写，使用link标签引入

# 选择器

## 标签选择器

{% highlight html %}
{% include_relative tag_selector.html %}
{% endhighlight %}

<iframe src="tag_selector.html"></iframe>

## 类选择器

{% highlight html %}
{% include_relative class_selector.html %}
{% endhighlight %}

<iframe src="class_selector.html"></iframe>

## id选择器

{% highlight html %}
{% include_relative id_selector.html %}
{% endhighlight %}

<iframe src="id_selector.html"></iframe>

## 通配符选择器

所有标签设置样式

{% highlight html %}
{% include_relative all_selector.html %}
{% endhighlight %}

<iframe src="all_selector.html"></iframe>

## 后代选择器

包含儿子、孙子、重孙子...

{% highlight html %}
{% include_relative descendant_selector.html %}
{% endhighlight %}

<iframe src="descendant_selector.html"></iframe>

## 子代选择器

只包含儿子。

{% highlight html %}
{% include_relative child_selector.html %}
{% endhighlight %}

<iframe src="child_selector.html"></iframe>

## 并集选择器

{% highlight html %}
{% include_relative union_selector.html %}
{% endhighlight %}

<iframe src="union_selector.html"></iframe>

## 交集选择器

{% highlight html %}
{% include_relative intersection_selector.html %}
{% endhighlight %}

<iframe src="intersection_selector.html"></iframe>

## 状态伪类选择器

表示元素状态的选择器

{% highlight html %}
{% include_relative pseudo_class_selector.html %}
{% endhighlight %}

<iframe src="pseudo_class_selector.html"></iframe>

超链接的伪类：

| 选择器      | 作用      |
|----------|---------|
| :link    | 访问前     |
| :visited | 访问后     |
| :hover   | 鼠标悬停    |
| :active  | 点击时（激活） |

## 结构伪类选择器

{% highlight html %}
{% include_relative pseudo_structure_selector.html %}
{% endhighlight %}

<iframe src="pseudo_structure_selector.html"></iframe>

## 伪元素选择器

用来创建虚拟元素，用来摆放装饰性内容。

{% highlight html %}
{% include_relative pseudo_element_selector.html %}
{% endhighlight %}

<iframe src="pseudo_element_selector.html"></iframe>

# 文字属性

| 描述     | 属性              |
|--------|-----------------|
| 文字大小   | font-size       |
| 文字粗细   | font-weight     |
| 文字样式   | font-style      |
| 行高     | line-height     |
| 文字字体   | font-family     |
| 字体复合属性 | font            |
| 文字缩进   | text-indent     |
| 文字对齐   | text-align      |
| 文字修饰线  | text-decoration |
| 文字颜色   | color           |

{% highlight html %}
{% include_relative text_attributes.html %}
{% endhighlight %}

<iframe src="text_attributes.html"></iframe>

# CSS特性

- 继承性：子级标签默认继承父级标签的文字属性样式
    - 应用场景1：一般将字体属性在body标签中设置
- 层叠性：相同的选择器，相同属性后面的CSS属性会覆盖前面的CSS属性。不同的属性会叠加
- 优先级：!important > 行内样式 > id选择器 > 类选择器 > 标签选择器 > 通配符选择器

# 背景

## 概述

| 属性                    | 描述     |
|-----------------------|--------|
| background-color      | 背景颜色   |
| background-image      | 背景图片   |
| background-repeat     | 背景平铺方式 |
| background-position   | 背景图片位置 |
| background-size       | 背景图缩放  |
| background-attachment | 背景图固定  |
| background            | 背景复合属性 |

## 背景颜色

{% highlight html %}
{% include_relative background_color.html %}
{% endhighlight %}

<iframe src="background_color.html"></iframe>

## 背景图片

{% highlight html %}
{% include_relative background_image.html %}
{% endhighlight %}

<iframe src="background_image.html"></iframe>

{: .warning}
> 背景图片默认是平铺的效果

## 背景平铺方式

| 属性值       | 描述     |
|-----------|--------|
| repeat    | 平铺(默认) |
| repeat-x  | 水平平铺   |
| repeat-y  | 垂直平铺   |
| no-repeat | 不平铺    |

{% highlight html %}
{% include_relative background_repeat.html %}
{% endhighlight %}

<iframe src="background_repeat.html"></iframe>

## 背景图位置

{% highlight html %}
{% include_relative background_position.html %}
{% endhighlight %}

<iframe src="background_position.html"></iframe>

## 背景图缩放

| 属性值       | 描述            |
|-----------|---------------|
| contain   | 保持比例缩放,留白     |
| cover     | 保持比例缩放，完全覆盖盒子 |
| 100% 100% | 拉伸            |

{% highlight html %}
{% include_relative background_size.html %}
{% endhighlight %}

<iframe src="background_size.html"></iframe>

## 背景图固定

{% highlight html %}
{% include_relative background_attachment.html %}
{% endhighlight %}

<iframe src="background_attachment.html"></iframe>

## 背景复合属性

background: 背景颜色 背景图片 背景平铺方式 背景图片位置 背景图缩放 背景图固定;

## 精灵图

把多张小图片合并成一张大图片，减少HTTP请求。

下面是一张精灵图：

![](./img/sprites.png)

{% highlight html %}
{% include_relative sprites.html %}
{% endhighlight %}

<iframe src="sprites.html"></iframe>

# 显示模式

- `块级元素`：独占一行，宽度默认是父级宽度，可以设置宽高
- `行内元素`：不独占一行，宽度默认是内容的宽度，不可以设置宽高
- `行内块元素`：不独占一行，宽度默认是内容的宽度，可以设置宽高

显示模式转换：

- `display: block;`：转换为块级元素
- `display: inline;`：转换为行内元素
- `display: inline-block;`：转换为行内块元素

# 盒子模型

## 内边距、边框、外边距

| 属性      | 描述  |
|---------|-----|
| padding | 内边距 |
| border  | 边框  |
| margin  | 外边距 |

{% highlight html %}
{% include_relative box_model.html %}
{% endhighlight %}

<iframe src="box_model.html"></iframe>

{: .warning}
> 盒子的width和height是内容的宽高，不包含内边距、边框、外边距

## 边框样式

| 属性           | 描述     |
|--------------|--------|
| border-style | 边框样式   |
| border-width | 边框宽度   |
| border-color | 边框颜色   |
| border       | 边框复合属性 |

{% highlight html %}
{% include_relative border_style.html %}
{% endhighlight %}

<iframe src="border_style.html"></iframe>

## 左右居中

{: .note}
> `text-align: center; `只能居中行内元素,想对块级元素居中，需要设置宽度，然后`margin: 0 auto`;

{% highlight html %}
{% include_relative margin-center.html %}
{% endhighlight %}

<iframe src="margin-center.html"></iframe>

## 边距多值写法

{% highlight html %}
{% include_relative padding.html %}
{% endhighlight %}

<iframe src="padding.html"></iframe>

## 内减模式

`box-sizing`属性定义了应该如何计算一个元素的总宽度和总高度。

| 属性值         | 描述   |
|-------------|------|
| border-box  | 边框盒子 |
| content-box | 内容盒子 |

{% highlight html %}
{% include_relative box_sizing.html %}
{% endhighlight %}

<iframe src="box_sizing.html"></iframe>

## 元素溢出

| 属性值    | 描述   |
|--------|------|
| hidden | 溢出隐藏 |
| scroll | 溢出滚动 |
| auto   | 溢出自动 |

{% highlight html %}
{% include_relative overflow.html %}
{% endhighlight %}

<iframe src="overflow.html"></iframe>

## 外边距合并

{: .note-title}
> 外边距合并
>
> 两个盒子相邻的外边距会合并为一个外边距，取两个外边距中的最大值

{% highlight html %}
{% include_relative margin.html %}
{% endhighlight %}

<iframe src="margin.html"></iframe>

## 外边距塌陷问题

{: .note-title}
> 外边距塌陷
>
> 父子级标签，子级标签添加上边距，会产生塌陷问题
>
> 塌陷问题会影响父盒子的位置

{% highlight html %}
{% include_relative margin_collapse.html %}
{% endhighlight %}

<iframe src="margin_collapse.html"></iframe>

## 盒子圆角

{% highlight html %}
{% include_relative border_radius.html %}
{% endhighlight %}

<iframe src="border_radius.html"></iframe>

## 盒子阴影

属性值：x轴偏移量 y轴偏移量 模糊半径 扩展半径 颜色 内外阴影

{% highlight html %}
{% include_relative box_shadow.html %}
{% endhighlight %}

<iframe src="box_shadow.html"></iframe>

# 标准流

`标准流`即文档流，是指元素在没有设置浮动和定位的情况下，元素按照其在HTML中的先后位置自上而下布局。

# 浮动

让块级盒子在一行显示。

## 基础

`floats`属性设置为left或right，可以让盒子浮动起来。

浮动后的盒子会脱离标准流，不占据位置。

{% highlight html %}
{% include_relative float.html %}
{% endhighlight %}

<iframe src="float.html"></iframe>

## 清除浮动

问题：浮动不会撑开父盒子，导致父盒子高度为0，影响布局。

解决方案：

- `额外标签法`：在父元素内容的最后添加一个块级元素，设置CSS属性为`clear:both`

{% highlight html %}
{% include_relative clear_float.html %}
{% endhighlight %}

<iframe src="clear_float.html"></iframe>

# flex布局(弹性布局)

现在企业中一般使用`flex布局`替代`浮动布局`。

⭐flex不存在父盒子因为子盒子脱标而塌陷的问题。因此不需要清除浮动。

## 概述

父盒子`display:flex`设置为弹性布局。

{% highlight html %}
{% include_relative flex.html %}
{% endhighlight %}

<iframe src="flex.html"></iframe>

## 主轴对齐方式

`justify-content`属性：

| 属性值           | 描述                                                              |
|---------------|-----------------------------------------------------------------|
| flex-start    | 左对齐                                                             |
| flex-end      | 右对齐                                                             |
| center        | 居中对齐                                                            |
| space-between | 两端对齐                                                            |
| space-around  | 在每行上均匀分配弹性元素。                                                   |
| space-evenly  | 相邻 flex 项之间的间距，主轴起始位置到第一个 flex 项的间距，主轴结束位置到最后一个 flex 项的间距，都完全一样 |

{% highlight html %}
{% include_relative justify_content.html %}
{% endhighlight %}

<iframe src="justify_content.html"></iframe>

## 修改主轴方向

父元素的`flex-direction`属性：

| 属性值    | 描述                 |
|--------|--------------------|
| row    | 默认值，主轴为水平方向，起点在左端。 |
| column | 主轴为垂直方向，起点在上沿。     |

{% highlight html %}
{% include_relative flex_direction.html %}
{% endhighlight %}

<iframe src="flex_direction.html"></iframe>

## 控制盒子伸缩比

flex布局默认情况下，侧轴自动伸缩。主轴宽度取决于内容的宽度。

子元素的`flex`属性表示在主轴方向的伸缩比：

| 属性值 | 描述       |
|-----|----------|
| 1   | 默认值，平均分配 |
| 2   | 伸缩比为2    |
| 3   | 伸缩比为3    |

{% highlight html %}
{% include_relative flex_attribute.html %}
{% endhighlight %}

<iframe src="flex_attribute.html"></iframe>

## 换行

父元素的`flex-wrap`属性：

| 属性值    | 描述      |
|--------|---------|
| nowrap | 默认值，不换行 |
| wrap   | 换行      |

{% highlight html %}
{% include_relative flex_wrap.html %}
{% endhighlight %}

<iframe src="flex_wrap.html"></iframe>

## 侧轴对其方式

侧轴单行显示时父元素的`align-items`属性和子元素的`align-self`属性：

| 属性值        | 描述   |
|------------|------|
| flex-start | 顶部对齐 |
| flex-end   | 底部对齐 |
| center     | 居中对齐 |

{% highlight html %}
{% include_relative align_items.html %}
{% endhighlight %}

<iframe src="align_items.html"></iframe>

侧轴多行显示时父元素的`align-content`属性：

| 属性值           | 描述   |
|---------------|------|
| flex-start    | 顶部对齐 |
| flex-end      | 底部对齐 |
| center        | 居中对齐 |
| space-between | 两端对齐 |
| space-around  | 均匀分布 |
| space-evenly  | 均匀分布 |

{% highlight html %}
{% include_relative align_content.html %}
{% endhighlight %}

<iframe src="align_content.html"></iframe>

# 定位

## 相对定位

相对定位是参照自己原来的位置

{: .note}
相对定位不会脱离标准流，依旧占据原先位置。

{% highlight html %}
{% include_relative relative.html %}
{% endhighlight %}

<iframe src="relative.html"></iframe>

## 绝对定位

相对于父级盒子定位，如果父级盒子没有定位，则相对于body定位。

{: .note}
> 绝对定位会脱离标准流，不占用原先位置。
>
> 绝对定位后的盒子会变为`行内块元素`

`子绝父相`: 一般子级盒子使用绝对定位，父级盒子使用相对定位。

{% highlight html %}
{% include_relative absolute.html %}
{% endhighlight %}

<iframe src="absolute.html"></iframe>

## 水平垂直居中

{% highlight html %}
{% include_relative center.html %}
{% endhighlight %}

<iframe src="center.html"></iframe>

## 固定定位

相对于浏览器窗口定位，不随滚动条滚动。

{: .note}
> 固定定位会脱离标准流，不占用原先位置。
>
> 固定定位后的盒子会变为`行内块元素`

{% highlight html %}
{% include_relative fixed.html %}
{% endhighlight %}

[效果](fixed.html)

## z-index

`z-index`属性设置盒子的层级。

{% highlight html %}
{% include_relative z_index.html %}
{% endhighlight %}

<iframe src="z_index.html"></iframe>

# 其它常用属性

## 行内元素垂直居中

line-height一般用于文字的垂直居中，面对其它行内元素时，使用`vertical-align`属性。

| 属性值      | 描述   |
|----------|------|
| baseline | 默认值  |
| top      | 顶部对齐 |
| middle   | 居中对齐 |
| bottom   | 底部对齐 |

{% highlight html %}
{% include_relative vertical_align.html %}
{% endhighlight %}

<iframe src="vertical_align.html"></iframe>

## 过度效果

`transition`属性设置过度效果。

{% highlight html %}
{% include_relative transition.html %}
{% endhighlight %}

<iframe src="transition.html"></iframe>

## 透明度

`opacity`属性设置透明度。

相比于background-color的透明度，opacity会影响子元素的透明度。

{% highlight html %}
{% include_relative opacity.html %}
{% endhighlight %}

<iframe src="opacity.html"></iframe>

## 鼠标样式

`cursor`属性设置鼠标样式。

{% highlight html %}
{% include_relative cursor.html %}
{% endhighlight %}

<iframe src="cursor.html"></iframe>

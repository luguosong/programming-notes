---
layout: note
title: markdown语法
nav_order: 1000
create_time: 2023/6/26
---

# 一级标题1
## 二级标题1.1
### 三级标题1.1.1
### 三级标题1.1.2
#### 四级标题1.1.2.1
#### 四级标题1.1.2.2
#### 四级标题1.1.2.3
##### 五级标题1.1.2.3.1
##### 五级标题1.1.2.3.2
##### 五级标题1.1.2.3.3
##### 五级标题1.1.2.3.4
###### 六级标题1.1.2.3.4.1
###### 六级标题1.1.2.3.4.2
###### 六级标题1.1.2.3.4.3
###### 六级标题1.1.2.3.4.4
###### 六级标题1.1.2.3.4.5
##### 五级标题1.1.2.3.5
#### 四级标题1.1.2.4
#### 四级标题1.1.2.5
### 三级标题1.1.3
## 二级标题1.2
# 一级标题2
## 二级标题2.1
## 二级标题2.2
## 二级标题2.3

```markdown
# 一级标题1
## 二级标题1.1
### 三级标题1.1.1
### 三级标题1.1.2
#### 四级标题1.1.2.1
#### 四级标题1.1.2.2
#### 四级标题1.1.2.3
##### 五级标题1.1.2.3.1
##### 五级标题1.1.2.3.2
##### 五级标题1.1.2.3.3
##### 五级标题1.1.2.3.4
###### 六级标题1.1.2.3.4.1
###### 六级标题1.1.2.3.4.2
###### 六级标题1.1.2.3.4.3
###### 六级标题1.1.2.3.4.4
###### 六级标题1.1.2.3.4.5
##### 五级标题1.1.2.3.5
#### 四级标题1.1.2.4
#### 四级标题1.1.2.5
### 三级标题1.1.3
## 二级标题1.2
# 一级标题2
## 二级标题2.1
## 二级标题2.2
## 二级标题2.3
```

# 按钮

## 基本按钮样式

[Link button](http://example.com/){: .btn }

[Link button](http://example.com/){: .btn .btn-purple }
[Link button](http://example.com/){: .btn .btn-blue }
[Link button](http://example.com/){: .btn .btn-green }

[Link button](http://example.com/){: .btn .btn-outline }

```markdown
[Link button](http://example.com/){: .btn }

[Link button](http://example.com/){: .btn .btn-purple }
[Link button](http://example.com/){: .btn .btn-blue }
[Link button](http://example.com/){: .btn .btn-green }

[Link button](http://example.com/){: .btn .btn-outline }
```

## 按钮大小

将按钮包裹在一个容器中，使用字体大小的实用类来缩放按钮：

<span class="fs-8">
[Link button](http://example.com/){: .btn }
</span>

<span class="fs-3">
[Tiny ass button](http://example.com/){: .btn }
</span>


```markdown
<span class="fs-8">
[Link button](http://example.com/){: .btn }
</span>

<span class="fs-3">
[Tiny ass button](http://example.com/){: .btn }
</span>
```

## 按钮之间的间距

[Button with space](http://example.com/){: .btn .btn-purple .mr-2 }
[Button](http://example.com/){: .btn .btn-blue }

[Button with more space](http://example.com/){: .btn .btn-green .mr-4 }
[Button](http://example.com/){: .btn .btn-blue }

```markdown
[Button with space](http://example.com/){: .btn .btn-purple .mr-2 }
[Button](http://example.com/){: .btn .btn-blue }

[Button with more space](http://example.com/){: .btn .btn-green .mr-4 }
[Button](http://example.com/){: .btn .btn-blue }
```

# 标签

使用标签是为你的文件的某个部分添加额外标记的一种方式。标签有几种颜色。默认情况下，标签将是蓝色的。

Default label
{: .label }

Blue label
{: .label .label-blue }

Stable
{: .label .label-green }

New release
{: .label .label-purple }

Coming soon
{: .label .label-yellow }

Deprecated
{: .label .label-red }

```markdown
Default label
{: .label }

Blue label
{: .label .label-blue }

Stable
{: .label .label-green }

New release
{: .label .label-purple }

Coming soon
{: .label .label-yellow }

Deprecated
{: .label .label-red }
```

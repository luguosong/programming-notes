# 表单

> 表单就像一张纸质问卷——有填空题（`input`）、选择题（`radio`/`checkbox`）、下拉题（`select`）和问答题（`textarea`）。浏览器把用户填写的数据打包发送给服务器，整个 Web 交互的核心就在于此。

本文你会学到：

- 🎯 `form` 容器的作用与核心属性
- 🔧 各类输入控件（文本、选择、多行文本）的用法
- 📦 用 `fieldset` 对表单进行分组
- 🆕 HTML5 新增控件（`datalist`、`output`、`progress`、`meter`）
- ✅ 浏览器内置表单验证的使用方式

## 📋 form 标签做了什么？

### form 容器

`form` 是所有表单控件的容器，它本身不显示任何内容，但定义了`数据往哪发、怎么发`。

``` html title="form 基本结构"
--8<-- "docs/frontend/html/form/demo/basic-structure.html"
```

<iframe class="html-demo" loading="lazy" src="demo/basic-structure.html" style="height:100px"></iframe>

!!! note "MDN"
    `form` 的三个核心属性：

    | 属性 | 说明 | 示例 |
    |------|------|------|
    | `action` | 提交目标 URL | `action="/api/login"` |
    | `method` | 请求方式：`GET`（查询）或 `POST`（提交） | `method="POST"` |
    | `enctype` | 编码类型，上传文件时需设为 `multipart/form-data` | `enctype="multipart/form-data"` |

**`method` 怎么选？**

- `GET`：数据附在 URL 上（`?name=value`），适合搜索、筛选等`无副作用`的操作
- `POST`：数据放在请求体中，适合登录、注册等`有数据修改`的操作

``` html title="搜索用 GET"
--8<-- "docs/frontend/html/form/demo/search-get.html"
```

<iframe class="html-demo" loading="lazy" src="demo/search-get.html" style="height:120px"></iframe>

``` html title="登录用 POST"
--8<-- "docs/frontend/html/form/demo/login-post.html"
```

<iframe class="html-demo" loading="lazy" src="demo/login-post.html" style="height:150px"></iframe>

## 📝 用户怎么填数据？——基础输入控件

### input 标签与 type 属性

`input` 是最常用的表单控件——一个自闭合标签，通过 `type` 属性决定它的外观和行为。

``` html title="最常用的几种 input"
--8<-- "docs/frontend/html/form/demo/common-input-types.html"
```

<iframe class="html-demo" loading="lazy" src="demo/common-input-types.html" style="height:350px"></iframe>

💡 `type="text"` 是默认值，写不写效果一样，但`显式写出更清晰`。

`type="file"` 有两个特有属性：

``` html title="file 输入的特有属性"
--8<-- "docs/frontend/html/form/demo/file-input-attributes.html"
```

<iframe class="html-demo" loading="lazy" src="demo/file-input-attributes.html" style="height:300px"></iframe>

!!! note "MDN"
    除上述类型外，`input` 还支持以下 `type` 值：`search`、`tel`、`url`、`hidden`、`submit`、`reset`、`button`、`image`、`range`、`color`、`month`、`time`、`week`、`datetime-local`。完整列表见文末速查表。

### input 通用属性

不管 `type` 是什么，以下属性对所有 `input` 都适用：

``` html title="input 通用属性演示"
--8<-- "docs/frontend/html/form/demo/input-common-attributes.html"
```

<iframe class="html-demo" loading="lazy" src="demo/input-common-attributes.html" style="height:100px"></iframe>

| 属性 | 说明 | 示例 |
|------|------|------|
| `name` | 字段名，提交时作为 key | `name="email"` → `email=xxx@xx.com` |
| `placeholder` | 输入框为空时显示的提示文字 | `placeholder="请输入"` |
| `maxlength` | 限制最大输入字符数 | `maxlength="50"` |
| `autocomplete` | 是否允许浏览器自动填充 | `autocomplete="off"` |
| `accesskey` | 快捷键（按 Alt+字母 聚焦） | `accesskey="u"` → `Alt+U` |

⚠️ `没有 name 属性的输入框，数据不会随表单提交。`

### textarea 多行文本

`input` 只能输入单行文本。需要输入多行内容（如评论、备注）时，使用 `textarea`。

``` html title="textarea 多行文本框"
--8<-- "docs/frontend/html/form/demo/textarea-basic.html"
```

<iframe class="html-demo" loading="lazy" src="demo/textarea-basic.html" style="height:200px"></iframe>

💡 `textarea` 的内容写在`开始标签和结束标签之间`，而不是 `value` 属性里：

``` html title="textarea 的默认值"
--8<-- "docs/frontend/html/form/demo/textarea-default-value.html"
```

<iframe class="html-demo" loading="lazy" src="demo/textarea-default-value.html" style="height:200px"></iframe>

## 🔘 怎么让用户做选择？

### select 下拉选择

`select` + `option` 组合出一个下拉选择框——适合选项较多、占用空间又不能太大的场景。

``` html title="select 下拉选择框"
--8<-- "docs/frontend/html/form/demo/select-basic.html"
```

<iframe class="html-demo" loading="lazy" src="demo/select-basic.html" style="height:100px"></iframe>

常用增强属性：

``` html title="select 的实用技巧"
--8<-- "docs/frontend/html/form/demo/select-techniques.html"
```

<iframe class="html-demo" loading="lazy" src="demo/select-techniques.html" style="height:400px"></iframe>

### radio 单选与 checkbox 多选

`radio` 是单选按钮——同组（相同 `name`）中只能选一个。`checkbox` 是复选框——可以同时选多个。

``` html title="radio 单选"
--8<-- "docs/frontend/html/form/demo/radio-single.html"
```

<iframe class="html-demo" loading="lazy" src="demo/radio-single.html" style="height:100px"></iframe>

``` html title="checkbox 多选"
--8<-- "docs/frontend/html/form/demo/checkbox-multiple.html"
```

<iframe class="html-demo" loading="lazy" src="demo/checkbox-multiple.html" style="height:100px"></iframe>

⚠️ `radio` 的分组靠的是 `name` 属性——`name` 相同的 `radio` 互斥：

``` html title="两组独立的 radio"
--8<-- "docs/frontend/html/form/demo/radio-independent-groups.html"
```

<iframe class="html-demo" loading="lazy" src="demo/radio-independent-groups.html" style="height:150px"></iframe>

设置默认选中用 `checked` 属性：

``` html title="默认选中"
--8<-- "docs/frontend/html/form/demo/default-checked.html"
```

<iframe class="html-demo" loading="lazy" src="demo/default-checked.html" style="height:120px"></iframe>

## 🖱️ 点击按钮时发生了什么？

### button 三种类型

`button` 元素有三种 `type`，行为完全不同：

``` html title="button 的三种类型"
--8<-- "docs/frontend/html/form/demo/button-types.html"
```

<iframe class="html-demo" loading="lazy" src="demo/button-types.html" style="height:100px"></iframe>

!!! note "MDN"
    `<button>` 在 `<form>` 内部时，`type` 默认为 `submit`。这意味着如果忘了写 `type`，点击按钮会`意外提交表单`。最佳实践：`始终显式声明 type`。

✅ 始终声明 `type`：

``` html
--8<-- "docs/frontend/html/form/demo/button-always-declare-type.html"
```

<iframe class="html-demo" loading="lazy" src="demo/button-always-declare-type.html" style="height:80px"></iframe>

❌ 忘了 `type` 在 form 中会导致意外提交：

``` html
--8<-- "docs/frontend/html/form/demo/button-missing-type.html"
```

<iframe class="html-demo" loading="lazy" src="demo/button-missing-type.html" style="height:100px"></iframe>

你也可以用 `input` 来创建按钮，但 `button` 更灵活——它支持图标、富文本等内部内容：

``` html title="button vs input 按钮"
--8<-- "docs/frontend/html/form/demo/button-vs-input.html"
```

<iframe class="html-demo" loading="lazy" src="demo/button-vs-input.html" style="height:100px"></iframe>

### label 关联文本

`label` 为表单控件提供可点击的文本标签。它的两个好处：

1. `可访问性`：屏幕阅读器能读出标签，帮助视障用户理解输入框用途
2. `点击体验`：点击标签文本就能聚焦对应的输入框，点选范围更大

**方式一：`for` + `id` 关联**

``` html title="label 方式一：for + id"
--8<-- "docs/frontend/html/form/demo/label-for-id.html"
```

<iframe class="html-demo" loading="lazy" src="demo/label-for-id.html" style="height:80px"></iframe>

**方式二：直接把 `input` 放在 `label` 里面**

``` html title="label 方式二：包含 input"
--8<-- "docs/frontend/html/form/demo/label-wrapping-input.html"
```

<iframe class="html-demo" loading="lazy" src="demo/label-wrapping-input.html" style="height:80px"></iframe>

💡 方式二代码更简洁，但方式一更灵活——`label` 和 `input` 不需要在 DOM 结构上相邻。实际项目中两种都很常见，选择适合当前场景的即可。

## 📦 表单太长怎么分组？

### fieldset 与 legend

当表单字段很多时，用 `fieldset` 把相关的控件分组，再用 `legend` 给每组起个标题。浏览器会自动给 `fieldset` 加上边框。

``` html title="fieldset 分组表单"
--8<-- "docs/frontend/html/form/demo/fieldset-grouping.html"
```

<iframe class="html-demo" loading="lazy" src="demo/fieldset-grouping.html" style="height:400px"></iframe>

!!! note "MDN"
    `fieldset` 还有一个 `disabled` 属性——设置后，组内**所有**表单控件都会被禁用，无需逐个设置。这在表单分步填写、条件显示等场景下很实用。

``` html title="fieldset 禁用整组控件"
--8<-- "docs/frontend/html/form/demo/fieldset-disabled.html"
```

<iframe class="html-demo" loading="lazy" src="demo/fieldset-disabled.html" style="height:200px"></iframe>

## 🆕 HTML5 带来了哪些新控件？

### datalist 输入建议

`datalist` 配合 `input` 提供输入建议列表——用户既可以从列表中选择，也可以自己输入，`灵活性比 select 更高`。

``` html title="datalist 输入建议"
--8<-- "docs/frontend/html/form/demo/datalist.html"
```

<iframe class="html-demo" loading="lazy" src="demo/datalist.html" style="height:100px"></iframe>

💡 用户输入时，浏览器会自动显示匹配的建议。但用户完全可以输入一个不在列表中的值——这就是它和 `select` 的本质区别。

### output / progress / meter

!!! note "MDN"
    HTML5 引入了几个用于展示数据的语义化标签，虽然不属于传统「表单控件」，但常配合表单使用。

**`output`——计算结果输出**

``` html title="output 显示计算结果"
--8<-- "docs/frontend/html/form/demo/output-calculation.html"
```

<iframe class="html-demo" loading="lazy" src="demo/output-calculation.html" style="height:100px"></iframe>

**`progress`——进度条**

``` html title="progress 进度条"
--8<-- "docs/frontend/html/form/demo/progress-bar.html"
```

<iframe class="html-demo" loading="lazy" src="demo/progress-bar.html" style="height:100px"></iframe>

**`meter`——度量/标量值**

``` html title="meter 度量标签"
--8<-- "docs/frontend/html/form/demo/meter-tag.html"
```

<iframe class="html-demo" loading="lazy" src="demo/meter-tag.html" style="height:100px"></iframe>

`progress` 和 `meter` 的区别：

| | `progress` | `meter` |
|------|------|------|
| **语义** | 任务进度（从 0 到完成） | 某个度量值（如磁盘用量、考试成绩） |
| **方向** | 只会从低到高 | 可以高也可以低 |
| **颜色变化** | 统一样式 | 根据 `low`/`high`/`optimum` 自动变色 |

## ✅ 不写 JavaScript 也能验证？——内置表单验证

### 内置验证属性

HTML5 提供了浏览器原生验证——不需要写 JavaScript，直接在标签上加属性就能实现基本的表单校验。

``` html title="常用验证属性"
--8<-- "docs/frontend/html/form/demo/validation-attributes.html"
```

<iframe class="html-demo" loading="lazy" src="demo/validation-attributes.html" style="height:350px"></iframe>

验证属性速查：

| 属性 | 说明 | 示例 |
|------|------|------|
| `required` | 必填字段 | `required` |
| `pattern` | 正则校验 | `pattern="[A-Za-z]{3,}"` |
| `min` / `max` | 数值最小/最大值 | `min="0" max="100"` |
| `minlength` / `maxlength` | 字符串最小/最大长度 | `minlength="6"` |
| `type` | 类型自带验证 | `type="email"`、`type="url"`、`type="number"` |

⚠️ 浏览器原生验证的提示文案是英文的，且样式无法完全自定义。正式项目中通常用 JavaScript 库（如 `vee-validate`、`zod`）实现更灵活的验证。

💡 `novalidate` 属性可以关闭表单的浏览器验证——当你想完全用 JS 控制验证逻辑时使用：

``` html
--8<-- "docs/frontend/html/form/demo/novalidate.html"
```

<iframe class="html-demo" loading="lazy" src="demo/novalidate.html" style="height:150px"></iframe>

## 📊 input 类型速查表

| `type` 值 | 用途 | 渲染效果 |
|-----------|------|----------|
| `text` | 单行文本（默认） | 普通输入框 |
| `password` | 密码输入（内容遮蔽） | `••••••` |
| `email` | 邮箱（自动校验格式） | 普通输入框 + 验证 |
| `number` | 数字（含增减按钮） | 数字输入框 |
| `tel` | 电话号码 | 普通输入框 |
| `url` | 网址（自动校验格式） | 普通输入框 + 验证 |
| `search` | 搜索框（部分浏览器带清除按钮） | 带圆角的输入框 |
| `date` | 日期选择（年-月-日） | 日期选择器 |
| `time` | 时间选择（时:分） | 时间选择器 |
| `month` | 月份选择（年-月） | 月份选择器 |
| `week` | 周数选择（年-第W周） | 周选择器 |
| `datetime-local` | 日期+时间（无时区） | 日期时间选择器 |
| `color` | 颜色选择器 | 色板弹窗 |
| `range` | 滑块 | 可拖动的滑块条 |
| `file` | 文件选择 | "选择文件"按钮 |
| `hidden` | 隐藏字段（用户不可见，数据会提交） | 不渲染 |
| `submit` | 提交按钮 | 普通按钮 |
| `reset` | 重置按钮 | 普通按钮 |
| `button` | 普通按钮（需 JS 绑定事件） | 普通按钮 |
| `image` | 图像提交按钮（以图片形式） | 可点击的图片 |
| `checkbox` | 复选框 | ☑ 方框 |
| `radio` | 单选按钮 | ◉ 圆点 |

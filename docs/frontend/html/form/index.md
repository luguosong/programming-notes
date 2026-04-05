# 表单

> 表单就像一张纸质问卷——有填空题（`input`）、选择题（`radio`/`checkbox`）、下拉题（`select`）和问答题（`textarea`）。浏览器把用户填写的数据打包发送给服务器，整个 Web 交互的核心就在于此。

本文你会学到：

- 🎯 `form` 容器的作用与核心属性
- 🔧 各类输入控件（文本、选择、多行文本）的用法
- 📦 用 `fieldset` 对表单进行分组
- 🆕 HTML5 新增控件（`datalist`、`output`、`progress`、`meter`）
- ✅ 浏览器内置表单验证的使用方式

## 📋 表单概述

### form 容器

`form` 是所有表单控件的容器，它本身不显示任何内容，但定义了`数据往哪发、怎么发`。

``` html title="form 基本结构"
<form action="/submit" method="POST" enctype="multipart/form-data">
  <!-- 表单控件放在这里 -->
</form>
```

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
<form action="/search" method="GET">
  <input type="text" name="q" placeholder="搜索...">
  <button type="submit">搜索</button>
</form>
<!-- 提交后 URL 变为 /search?q=用户输入的内容 -->
```

``` html title="登录用 POST"
<form action="/login" method="POST">
  <input type="text" name="username" placeholder="用户名">
  <input type="password" name="password" placeholder="密码">
  <button type="submit">登录</button>
</form>
```

## 📝 基础输入控件

### input 标签与 type 属性

`input` 是最常用的表单控件——一个自闭合标签，通过 `type` 属性决定它的外观和行为。

``` html title="最常用的几种 input"
<input type="text" name="username" placeholder="请输入用户名">
<input type="password" name="pwd" placeholder="请输入密码">
<input type="email" name="mail" placeholder="请输入邮箱">
<input type="number" name="age" placeholder="年龄" min="0" max="150">
<input type="date" name="birthday">
<input type="file" name="avatar" accept="image/*">
```

💡 `type="text"` 是默认值，写不写效果一样，但`显式写出更清晰`。

`type="file"` 有两个特有属性：

``` html title="file 输入的特有属性"
<!-- multiple：允许选择多个文件 -->
<input type="file" name="files" multiple>

<!-- accept：限制文件类型 -->
<input type="file" name="photo" accept="image/png,image/jpeg">

<!-- 常见 accept 写法 -->
<input type="file" accept=".pdf">              <!-- 只允许 PDF -->
<input type="file" accept="image/*">            <!-- 所有图片 -->
<input type="file" accept="video/*">            <!-- 所有视频 -->
```

!!! note "MDN"
    除上述类型外，`input` 还支持以下 `type` 值：`search`、`tel`、`url`、`hidden`、`submit`、`reset`、`button`、`image`、`range`、`color`、`month`、`time`、`week`、`datetime-local`。完整列表见文末速查表。

### input 通用属性

不管 `type` 是什么，以下属性对所有 `input` 都适用：

``` html title="input 通用属性演示"
<input
  type="text"
  name="username"           <!-- 字段名，提交数据的键 -->
  placeholder="请输入用户名"  <!-- 占位提示文字 -->
  maxlength="20"            <!-- 最大字符数 -->
  autocomplete="on"         <!-- 自动填充（on/off） -->
>
```

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
<textarea
  name="comment"
  rows="5"          <!-- 显示 5 行 -->
  cols="40"         <!-- 显示 40 列宽 -->
  placeholder="请输入评论..."
  maxlength="500"
></textarea>
```

💡 `textarea` 的内容写在`开始标签和结束标签之间`，而不是 `value` 属性里：

``` html title="textarea 的默认值"
<textarea name="bio">这里写默认内容</textarea>

<!-- ❌ 错误：textarea 没有 value 属性 -->
<textarea name="bio" value="默认值"></textarea>
```

## 🔘 选择控件

### select 下拉选择

`select` + `option` 组合出一个下拉选择框——适合选项较多、占用空间又不能太大的场景。

``` html title="select 下拉选择框"
<select name="city">
  <option value="">请选择城市</option>
  <option value="beijing">北京</option>
  <option value="shanghai">上海</option>
  <option value="guangzhou">广州</option>
  <option value="shenzhen">深圳</option>
</select>
```

常用增强属性：

``` html title="select 的实用技巧"
<!-- 默认选中某一项 -->
<option value="shanghai" selected>上海</option>

<!-- 禁用某一项（置灰不可选） -->
<option value="" disabled>请选择城市</option>

<!-- 分组显示（optgroup） -->
<select name="province">
  <optgroup label="华东地区">
    <option value="shanghai">上海</option>
    <option value="jiangsu">江苏</option>
  </optgroup>
  <optgroup label="华南地区">
    <option value="guangdong">广东</option>
    <option value="fujian">福建</option>
  </optgroup>
</select>

<!-- 多选（按住 Ctrl 点击） -->
<select name="hobbies" multiple size="4">
  <option value="reading">阅读</option>
  <option value="coding">编程</option>
  <option value="gaming">游戏</option>
  <option value="music">音乐</option>
</select>
```

### radio 单选与 checkbox 多选

`radio` 是单选按钮——同组（相同 `name`）中只能选一个。`checkbox` 是复选框——可以同时选多个。

``` html title="radio 单选"
<p>性别：</p>
<label><input type="radio" name="gender" value="male"> 男</label>
<label><input type="radio" name="gender" value="female"> 女</label>
<label><input type="radio" name="gender" value="other"> 其他</label>
```

``` html title="checkbox 多选"
<p>爱好：</p>
<label><input type="checkbox" name="hobby" value="reading"> 阅读</label>
<label><input type="checkbox" name="hobby" value="coding"> 编程</label>
<label><input type="checkbox" name="hobby" value="gaming"> 游戏</label>
```

⚠️ `radio` 的分组靠的是 `name` 属性——`name` 相同的 `radio` 互斥：

``` html title="两组独立的 radio"
<p>配送方式：</p>
<label><input type="radio" name="shipping" value="express"> 快递</label>
<label><input type="radio" name="shipping" value="pickup"> 自提</label>

<p>支付方式：</p>
<label><input type="radio" name="payment" value="alipay"> 支付宝</label>
<label><input type="radio" name="payment" value="wechat"> 微信</label>
<!-- shipping 和 payment 是两个独立的组，互不影响 -->
```

设置默认选中用 `checked` 属性：

``` html title="默认选中"
<input type="radio" name="gender" value="male" checked>
<input type="checkbox" name="agree" value="yes" checked>
```

## 🖱️ 按钮与标签

### button 三种类型

`button` 元素有三种 `type`，行为完全不同：

``` html title="button 的三种类型"
<!-- type="submit"：提交表单（默认值） -->
<button type="submit">提交</button>

<!-- type="reset"：重置表单为初始值 -->
<button type="reset">重置</button>

<!-- type="button"：普通按钮，不做任何事，需 JS 绑定逻辑 -->
<button type="button" onclick="handleClick()">点击我</button>
```

!!! note "MDN"
    `<button>` 在 `<form>` 内部时，`type` 默认为 `submit`。这意味着如果忘了写 `type`，点击按钮会`意外提交表单`。最佳实践：`始终显式声明 type`。

✅ 始终声明 `type`：

``` html
<button type="button">取消</button>
```

❌ 忘了 `type` 在 form 中会导致意外提交：

``` html
<!-- 用户点"取消"却触发了表单提交 -->
<form action="/api/order">
  <button>取消</button>
  <button type="submit">确认下单</button>
</form>
```

你也可以用 `input` 来创建按钮，但 `button` 更灵活——它支持图标、富文本等内部内容：

``` html title="button vs input 按钮"
<!-- ✅ button 可以放图标、文字 -->
<button type="submit">
  <img src="send-icon.png" alt="" width="16"> 发送
</button>

<!-- ❌ input 按钮只能放纯文本 -->
<input type="submit" value="发送">
```

### label 关联文本

`label` 为表单控件提供可点击的文本标签。它的两个好处：

1. `可访问性`：屏幕阅读器能读出标签，帮助视障用户理解输入框用途
2. `点击体验`：点击标签文本就能聚焦对应的输入框，点选范围更大

**方式一：`for` + `id` 关联**

``` html title="label 方式一：for + id"
<label for="username">用户名</label>
<input type="text" id="username" name="username">
<!-- 点击"用户名"三个字，输入框会获得焦点 -->
```

**方式二：直接把 `input` 放在 `label` 里面**

``` html title="label 方式二：包含 input"
<label>
  用户名
  <input type="text" name="username">
</label>
<!-- 点击"用户名"文字或输入框本身都能聚焦 -->
```

💡 方式二代码更简洁，但方式一更灵活——`label` 和 `input` 不需要在 DOM 结构上相邻。实际项目中两种都很常见，选择适合当前场景的即可。

## 📦 表单分组

### fieldset 与 legend

当表单字段很多时，用 `fieldset` 把相关的控件分组，再用 `legend` 给每组起个标题。浏览器会自动给 `fieldset` 加上边框。

``` html title="fieldset 分组表单"
<form action="/register" method="POST">
  <!-- 第一组：个人信息 -->
  <fieldset>
    <legend>个人信息</legend>
    <label for="name">姓名</label>
    <input type="text" id="name" name="name" placeholder="请输入姓名">

    <label for="email">邮箱</label>
    <input type="email" id="email" name="email" placeholder="请输入邮箱">
  </fieldset>

  <!-- 第二组：账户设置 -->
  <fieldset>
    <legend>账户设置</legend>
    <label for="password">密码</label>
    <input type="password" id="password" name="password" placeholder="请输入密码">

    <label for="role">角色</label>
    <select id="role" name="role">
      <option value="user">普通用户</option>
      <option value="admin">管理员</option>
    </select>
  </fieldset>

  <button type="submit">注册</button>
</form>
```

!!! note "MDN"
    `fieldset` 还有一个 `disabled` 属性——设置后，组内**所有**表单控件都会被禁用，无需逐个设置。这在表单分步填写、条件显示等场景下很实用。

``` html title="fieldset 禁用整组控件"
<fieldset disabled>
  <legend>高级设置（当前不可编辑）</legend>
  <input type="text" name="api_key" placeholder="API Key">
  <input type="text" name="webhook" placeholder="Webhook URL">
</fieldset>
```

## 🆕 HTML5 新增控件

### datalist 输入建议

`datalist` 配合 `input` 提供输入建议列表——用户既可以从列表中选择，也可以自己输入，`灵活性比 select 更高`。

``` html title="datalist 输入建议"
<input type="text" name="browser" list="browser-list" placeholder="输入或选择浏览器">
<datalist id="browser-list">
  <option value="Chrome">
  <option value="Firefox">
  <option value="Safari">
  <option value="Edge">
</datalist>
```

💡 用户输入时，浏览器会自动显示匹配的建议。但用户完全可以输入一个不在列表中的值——这就是它和 `select` 的本质区别。

### output / progress / meter

!!! note "MDN"
    HTML5 引入了几个用于展示数据的语义化标签，虽然不属于传统「表单控件」，但常配合表单使用。

**`output`——计算结果输出**

``` html title="output 显示计算结果"
<form oninput="result.value = Number(a.value) + Number(b.value)">
  <input type="number" name="a" value="10"> +
  <input type="number" name="b" value="20"> =
  <output name="result" for="a b">30</output>
</form>
```

**`progress`——进度条**

``` html title="progress 进度条"
<!-- 确定性进度（已知百分比） -->
<label>上传进度：<progress value="70" max="100">70%</progress></label>

<!-- 不确定性进度（加载中，不知道多久） -->
<label>加载中：<progress>请稍候...</progress></label>
```

**`meter`——度量/标量值**

``` html title="meter 度量标签"
<!-- 磁盘使用量：low/high/optimum 定义区间 -->
<meter value="0.7" min="0" max="1" low="0.3" high="0.8" optimum="0.5">
  70%
</meter>
```

`progress` 和 `meter` 的区别：

| | `progress` | `meter` |
|------|------|------|
| **语义** | 任务进度（从 0 到完成） | 某个度量值（如磁盘用量、考试成绩） |
| **方向** | 只会从低到高 | 可以高也可以低 |
| **颜色变化** | 统一样式 | 根据 `low`/`high`/`optimum` 自动变色 |

## ✅ 表单验证

### 内置验证属性

HTML5 提供了浏览器原生验证——不需要写 JavaScript，直接在标签上加属性就能实现基本的表单校验。

``` html title="常用验证属性"
<form>
  <!-- required：必填 -->
  <input type="text" name="username" required placeholder="用户名（必填）">

  <!-- minlength / maxlength：长度限制 -->
  <input type="text" name="code" minlength="6" maxlength="6" placeholder="6位验证码">

  <!-- min / max：数值范围 -->
  <input type="number" name="age" min="0" max="150" placeholder="年龄（0-150）">

  <!-- pattern：正则表达式校验 -->
  <input type="text" name="phone" pattern="[0-9]{11}" placeholder="11位手机号">

  <!-- type 本身就是验证：email 类型会检查邮箱格式 -->
  <input type="email" name="email" placeholder="邮箱（自动校验格式）">

  <button type="submit">提交</button>
</form>
```

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
<form novalidate>
  <!-- 浏览器不会进行任何验证，全部由 JS 处理 -->
</form>
```

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

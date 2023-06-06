---
layout: note
title: vue
nav_order: 40
create_time: 2023/6/1
---

# 安装

```shell
#使用Vue官方的项目脚手架工具创建项目
npm init vue@latest
```

```shell
#安装依赖
npm install
```

```shell
#运行
npm run dev
```

# 创建一个应用

## 容器

`index.html`作为容器，是真正整个前端的入口

{% highlight html %}
{% include_relative index.html %}
{% endhighlight %}

## main.js

{% highlight js %}
{% include_relative src/main.js %}
{% endhighlight %}

# 模板语法

- `文本插值`：`Mustache`语法 (即双大括号)
- `v-html`:插入 HTML而不是纯文本
- `v-bind:属性名`:绑定属性
    - 简写  `:属性名`
- 数据绑定支持使用 JavaScript 表达式

# 响应式基础

声明响应式对象或数组：

```js
import { reactive } from "vue"

export default {
  // `setup` 是一个专门用于组合式 API 的特殊钩子函数
  setup() {
    const state = reactive({ count: 0 })

    // 暴露 state 到模板
    return {
      state,
    }
  },
}
```

在单文档组件中可以进行简写：

```vue

<script setup>
import { reactive } from 'vue'

const state = reactive({ count: 0 })

</script>
```

- `reactive()`API的局限性
    - 仅对对象类型有效，对原始类型无效
    - 不可以随意地“替换”一个响应式对象，因为这将导致对初始引用的响应性连接丢失：

```js
// let test = reactive(0) // 这样是不支持的

let state = reactive({ count: 0 })

// 上面的引用 ({ count: 0 }) 将不再被追踪（响应性连接已丢失！）
state = reactive({ count: 1 })
```

Vue 提供了一个 `ref()` 方法来允许我们创建可以使用任何值类型的响应式 ref

# 计算属性

{% raw %}

```vue

<script setup>
import { reactive, computed } from 'vue'

const author = reactive({
  name: 'John Doe',
  books: [
    'Vue 2 - Advanced Guide',
    'Vue 3 - Basic Guide',
    'Vue 4 - The Mystery'
  ]
})

// 一个计算属性 ref
const publishedBooksMessage = computed(() => {
  return author.books.length > 0 ? 'Yes' : 'No'
})
</script>

<template>
  <p>Has published books:</p>
  <span>{{ publishedBooksMessage }}</span>
</template>
```

{% endraw %}

- 可写计算属性

```vue

<script setup>
import { ref, computed } from 'vue'

const firstName = ref('John')
const lastName = ref('Doe')

const fullName = computed({
  // getter
  get() {
    return firstName.value + ' ' + lastName.value
  },
  // setter
  set(newValue) {
    // 注意：我们这里使用的是解构赋值语法
    [firstName.value, lastName.value] = newValue.split(' ')
  }
})
</script>
```

# Class 与 Style 绑定

绑定class

```vue

<script setup>
const isActive = ref(true)
const hasError = ref(false)

const classObject = reactive({
  active: true,
  'text-danger': false
})

const activeClass = ref('active')
const errorClass = ref('text-danger')
</script>

<template>
  <div
    class="static"
    :class="{ active: isActive, 'text-danger': hasError }"
  ></div>

  <!--绑定对象-->
  <div :class="classObject"></div>

  <!--绑定数组-->
  <div :class="[activeClass, errorClass]"></div>
</template>
```

绑定样式：

```vue

<script setup>
const activeColor = ref("red")
const fontSize = ref(30)
</script>
<template>
  <div :style="{ color: activeColor, fontSize: fontSize + 'px' }"></div>
</template>
```

# 条件渲染

```vue

<script setup>
import { ref } from "vue"

const awesome = ref(true)
</script>

<template>
  <button @click="awesome = !awesome">toggle</button>

  <template v-if="awesome">
    <h1>Title</h1>
    <p>Paragraph 1</p>
    <p>Paragraph 2</p>
  </template>
  <h1 v-else>Oh no 😢</h1>

  <!--v-show会在DOM中保留该元素-->
  <h1 v-show="awesome">Hello!</h1>
</template>
```

{: .warning}
> 当 v-if 和 v-for 同时存在于一个元素上的时候，v-if 会首先被执行。

# 列表渲染

{% raw %}

```vue

<script setup>
import { ref } from 'vue'

const parentMessage = ref('Parent')
const items = ref([{ message: 'Foo' }, { message: 'Bar' }])
</script>

<template>
  <!--第二个参数index表示当前项的位置索引。-->
  <li v-for="(item, index) in items">
    {{ parentMessage }} - {{ index }} - {{ item.message }}
  </li>
</template>
```

{% endraw %}

# 事件处理

`v-on`或`@`处理事件

# 表单输入绑定

界面数据修改时，数据会被同步到数据源中

```vue

<template>
  <input v-model="text">
  <input v-model="message" placeholder="edit me" />
  <textarea v-model="message" placeholder="add multiple lines"></textarea>
  <input type="checkbox" id="checkbox" v-model="checked" />

  <input type="radio" id="one" value="One" v-model="picked" />
  <label for="one">One</label>
  <input type="radio" id="two" value="Two" v-model="picked" />
  <label for="two">Two</label>

  <select v-model="selected">
    <option disabled value="">Please select one</option>
    <option>A</option>
    <option>B</option>
    <option>C</option>
  </select>
</template>
```

# 生命周期

```js
<script setup>
  import {onMounted} from 'vue'

  onMounted(() => {
  console.log(`the component is now mounted.`)
})
</script>
```

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20230602105758.png)

# 侦听器

```vue

<script setup>
import { ref, watch } from 'vue'

const question = ref('')
const answer = ref('Questions usually contain a question mark. ;-)')

// 可以直接侦听一个 ref
watch(question, async (newQuestion, oldQuestion) => {
  if (newQuestion.indexOf('?') > -1) {
    answer.value = 'Thinking...'
    try {
      const res = await fetch('https://yesno.wtf/api')
      answer.value = (await res.json()).answer
    } catch (error) {
      answer.value = 'Error! Could not reach the API. ' + error
    }
  }
})
</script>

<template>
  <p>
    Ask a yes/no question:
    <input v-model="question" />
  </p>
  <p>{{ answer }}</p>
</template>
```

# 模板引用

```vue

<script setup>
import { ref, onMounted } from 'vue'

// 声明一个 ref 来存放该元素的引用
// 必须和模板里的 ref 同名
const input = ref(null)

onMounted(() => {
  input.value.focus()
})
</script>

<template>
  <input ref="input" />
</template>
```

# 组件的创建和使用

创建组件：

{% highlight vue %}
{% include_relative src/views/01-defining-and-using/Child.vue %}
{% endhighlight %}

使用组件：

{% highlight vue %}
{% include_relative src/views/01-defining-and-using/Father.vue %}
{% endhighlight %}

# 传递 props

非 `<script setup>`,props需要作为setup第一个参数

```js
export default {
  props: ['title'],
  setup(props) {
    console.log(props.title)
  }
}
```

子组件：

{% highlight vue %}
{% include_relative src/views/02-passing-props/BlogPost.vue %}
{% endhighlight %}

父组件：

{% highlight vue %}
{% include_relative src/views/02-passing-props/Father.vue %}
{% endhighlight %}

# 监听事件

子组件：

{% highlight vue %}
{% include_relative src/views/03-listening-to-events/Child.vue %}
{% endhighlight %}

父组件：

{% highlight vue %}
{% include_relative src/views/03-listening-to-events/Father.vue %}
{% endhighlight %}

# 插槽

子组件：

{% highlight vue %}
{% include_relative src/views/04-slots/Child.vue %}
{% endhighlight %}

父组件：

{% highlight vue %}
{% include_relative src/views/04-slots/Father.vue %}
{% endhighlight %}

# 动态组件



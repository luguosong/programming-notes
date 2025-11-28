# 基本markdown内容

## 警示框

### 自定义标题

!!! note "Phasellus posuere in sem ut cursus"

    Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla et euismod
    nulla. Curabitur feugiat, tortor non consequat finibus, justo purus auctor
    massa, nec semper lorem quam in massa.

```markdown
!!! note "Phasellus posuere in sem ut cursus"

    Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla et euismod
    nulla. Curabitur feugiat, tortor non consequat finibus, justo purus auctor
    massa, nec semper lorem quam in massa.
```

### 嵌套

!!! note "Outer Note"

    Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla et euismod
    nulla. Curabitur feugiat, tortor non consequat finibus, justo purus auctor
    massa, nec semper lorem quam in massa.

    !!! note "Inner Note"

        Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla et euismod
        nulla. Curabitur feugiat, tortor non consequat finibus, justo purus auctor
        massa, nec semper lorem quam in massa.

```markdown
!!! note "Outer Note"

    Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla et euismod
    nulla. Curabitur feugiat, tortor non consequat finibus, justo purus auctor
    massa, nec semper lorem quam in massa.

    !!! note "Inner Note"

        Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla et euismod
        nulla. Curabitur feugiat, tortor non consequat finibus, justo purus auctor
        massa, nec semper lorem quam in massa.
```

### 移除标题

!!! note ""

    Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla et euismod
    nulla. Curabitur feugiat, tortor non consequat finibus, justo purus auctor
    massa, nec semper lorem quam in massa.

```markdown
!!! note ""

    Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla et euismod
    nulla. Curabitur feugiat, tortor non consequat finibus, justo purus auctor
    massa, nec semper lorem quam in massa.
```

### 折叠块

??? note

    Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla et euismod
    nulla. Curabitur feugiat, tortor non consequat finibus, justo purus auctor
    massa, nec semper lorem quam in massa.

```markdown
??? note

    Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla et euismod
    nulla. Curabitur feugiat, tortor non consequat finibus, justo purus auctor
    massa, nec semper lorem quam in massa.
```

在 ??? 标记后添加一个 + 号，可以使该区块默认展开：

???+ note

    Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla et euismod
    nulla. Curabitur feugiat, tortor non consequat finibus, justo purus auctor
    massa, nec semper lorem quam in massa.

```markdown
???+ note

    Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla et euismod
    nulla. Curabitur feugiat, tortor non consequat finibus, justo purus auctor
    massa, nec semper lorem quam in massa.
```
### 内联块

可以作为内联块显示（例如用于侧边栏），通过 inline + end 修饰符将其放置在右侧，或者仅使用 inline 修饰符将其放置在左侧：

!!! info inline end "Lorem ipsum"

    Lorem ipsum dolor sit amet, consectetur
    adipiscing elit. Nulla et euismod nulla.
    Curabitur feugiat, tortor non consequat
    finibus, justo purus auctor massa, nec
    semper lorem quam in massa.

```markdown
!!! info inline end "Lorem ipsum"

    Lorem ipsum dolor sit amet, consectetur
    adipiscing elit. Nulla et euismod nulla.
    Curabitur feugiat, tortor non consequat
    finibus, justo purus auctor massa, nec
    semper lorem quam in massa.
```

!!! info inline "Lorem ipsum"

    Lorem ipsum dolor sit amet, consectetur
    adipiscing elit. Nulla et euismod nulla.
    Curabitur feugiat, tortor non consequat
    finibus, justo purus auctor massa, nec
    semper lorem quam in massa.

```markdown
!!! info inline "Lorem ipsum"

    Lorem ipsum dolor sit amet, consectetur
    adipiscing elit. Nulla et euismod nulla.
    Curabitur feugiat, tortor non consequat
    finibus, justo purus auctor massa, nec
    semper lorem quam in massa.
```

<br>
<br>
<br>

!!! important "重要提示"

	使用内联修饰符的警告框必须在你希望其并排显示的内容块之前声明。如果旁边空间不足以显示警告框，警告框会自动扩展至视口的整个宽度，例如在移动端视口下。

### 支持的类型

`note`
:   !!! note

        这是一段用于排版的占位符文本，旨在展示页面的字体和布局效果。

`abstract`
:   !!! abstract

        这是一段用于排版的占位符文本，旨在展示页面的字体和布局效果。

`info`
:   !!! info

        这是一段用于排版的占位符文本，旨在展示页面的字体和布局效果。

`tip`
:   !!! tip

        这是一段用于排版的占位符文本，旨在展示页面的字体和布局效果。

`success`
:   !!! success

        这是一段用于排版的占位符文本，旨在展示页面的字体和布局效果。

`question`
:   !!! question

        这是一段用于排版的占位符文本，旨在展示页面的字体和布局效果。

`warning`
:   !!! warning

        这是一段用于排版的占位符文本，旨在展示页面的字体和布局效果。

`failure`
:   !!! failure

        这是一段用于排版的占位符文本，旨在展示页面的字体和布局效果。

`danger`
:   !!! danger

        这是一段用于排版的占位符文本，旨在展示页面的字体和布局效果。

`bug`
:   !!! bug

        这是一段用于排版的占位符文本，旨在展示页面的字体和布局效果。

`example`
:   !!! example

        这是一段用于排版的占位符文本，旨在展示页面的字体和布局效果。

`quote`
:   !!! quote

        这是一段用于排版的占位符文本，旨在展示页面的字体和布局效果。


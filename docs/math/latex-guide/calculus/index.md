---
title: 微积分
---

# 微积分

## ➕ 求和与乘积 — `\sum`、`\prod`

- `\sum` 对应希腊大写字母 **Σ**（Sigma），在数学中代表**求和**（summation），下标为起始值，上标为终止值
- `\prod` 对应希腊大写字母 **Π**（Pi），代表**连乘**（product），用法与 `\sum` 对称
- `n!` 表示阶乘（factorial），即 $1 \times 2 \times \cdots \times n$

$$
\sum_{i=1}^{n} i = \frac{n(n+1)}{2}
$$

$$
\prod_{i=1}^{n} i = n!
$$

```latex
$$
\sum_{i=1}^{n} i = \frac{n(n+1)}{2}
$$

$$
\prod_{i=1}^{n} i = n!
$$
```

---

## 🎯 极限 — `\lim`

`\lim` 是 **limit**（极限）的缩写：

- `\lim_{x \to a}` 表示当 $x$ 趋近于 $a$ 时的极限
- `\to` 生成箭头 →，读作 "tends to" 或 "approaches"
- `\infty` 生成无穷符号 ∞（infinity）

$$
\lim_{x \to 0} \frac{\sin x}{x} = 1
$$

$$
\lim_{n \to \infty} \left(1 + \frac{1}{n}\right)^n = e
$$

```latex
$$
\lim_{x \to 0} \frac{\sin x}{x} = 1
$$

$$
\lim_{n \to \infty} \left(1 + \frac{1}{n}\right)^n = e
$$
```

---

## ∫ 积分 — `\int`

`\int` 来源于 **integral**（积分），符号形状源自拉长的字母 S，代表"summation"（求和）的连续形式：

- `\int_a^b` — 定积分，`_a` 为下限，`^b` 为上限
- `\int_{-\infty}^{+\infty}` — 从负无穷到正无穷的广义积分
- `\iint` — 二重积分（double integral），`\i` = double
- `\oint` — 闭合曲线积分（contour integral），圆圈 `o` 表示路径是闭合的
- `\,dx` — 逗号 `\,` 产生一个细小的间距，将微分算子 $dx$ 与被积函数隔开，提升可读性

$$
\int_a^b f(x)\,dx
$$

$$
\int_{-\infty}^{+\infty} e^{-x^2}\,dx = \sqrt{\pi}
$$

$$
\iint_D f(x,y)\,dx\,dy
$$

$$
\oint_C \vec{F} \cdot d\vec{r}
$$

```latex
$$
\int_a^b f(x)\,dx
$$

$$
\int_{-\infty}^{+\infty} e^{-x^2}\,dx = \sqrt{\pi}
$$

$$
\iint_D f(x,y)\,dx\,dy
$$

$$
\oint_C \vec{F} \cdot d\vec{r}
$$
```

---

## 📈 导数 — `f'`、`\frac{dy}{dx}`

导数有多种表示法，各自有不同的历史渊源：

- `f'(x)` — **拉格朗日记法**（ Lagrange notation），用撇号 `'`（prime）表示导数
- `f''(x)` — 二阶导数，两个撇号
- `\frac{dy}{dx}` — **莱布尼茨记法**（Leibniz notation），用微分之比表示导数，直观体现"变化率"
- `\frac{d^2y}{dx^2}` — 二阶导数的莱布尼茨记法
- `\partial` — **偏导数**（partial derivative），源自花体 d（∂），用于多变量函数中对单个变量求导

$$ f'(x) $$

$$ f''(x) $$

$$ \frac{dy}{dx} $$

$$ \frac{d^2y}{dx^2} $$

$$ \frac{\partial z}{\partial x} $$

```latex
$$ f'(x) $$

$$ f''(x) $$

$$ \frac{dy}{dx} $$

$$ \frac{d^2y}{dx^2} $$

$$ \frac{\partial z}{\partial x} $$
```

---

## 📊 泰勒展开 — `\sum` + `\frac{(-1)^k}{(2k)!}`

泰勒展开将函数表示为无穷级数，$\cos x$ 的展开中交替出现正负项（$(-1)^k$），$(2k)!$ 确保展开的是偶数阶项：

$$
\cos x = \sum_{k=0}^{\infty} \frac{(-1)^k}{(2k)!} x^{2k} = 1 - \frac{x^2}{2!} + \frac{x^4}{4!} - \cdots
$$

```latex
$$
\cos x = \sum_{k=0}^{\infty} \frac{(-1)^k}{(2k)!} x^{2k}
= 1 - \frac{x^2}{2!} + \frac{x^4}{4!} - \cdots
$$
```

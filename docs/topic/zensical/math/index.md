# 数学公式

Zensical 使用 `MathJax` 渲染 LaTeX 数学公式，需在 `zensical.toml` 中启用 `pymdownx.arithmatex` 扩展。

## 基础运算

### 行内公式

在文字中嵌入公式，用单个 `$` 包裹：

爱因斯坦质能方程 $E = mc^2$ 揭示了质量与能量的等价关系。

勾股定理：$a^2 + b^2 = c^2$，其中 $c$ 为斜边长度。

```markdown
爱因斯坦质能方程 $E = mc^2$。
勾股定理：$a^2 + b^2 = c^2$。
```

### 块级公式

独占一行的公式，用 `$$` 包裹：

$$
\begin{align}
1 + 2 = 3 \\
3 - 1 = 2 \\
2 \times 3 = 6 \\
6 \div 3 = 2
\end{align}
$$

```latex
$$
\begin{align}
    1 + 2 = 3 \\
    3 - 1 = 2 \\
    2 \times 3 = 6 \\
    6 \div 3 = 2
\end{align}
$$
```

---

## 上下标

$$
\begin{align}
x^2 + y^2 = r^2 \\
H_2O \\
a_{ij}
\end{align}
$$

```latex
$$
\begin{align}
x^2 + y^2 = r^2 \\
H_2O \\
a_{ij}
\end{align}
$$
```

多字符上下标需用 `{}` 包裹：

$$
\begin{align}
e^{i\pi} + 1 = 0 \\
x_{n+1} = x_n - \frac{f(x_n)}{f'(x_n)}
\end{align}
$$

```latex
$$
\begin{align}
e^{i\pi} + 1 = 0 \\
x_{n+1} = x_n - \frac{f(x_n)}{f'(x_n)}
\end{align}
$$
```

---

## 分数

$$
\begin{align}
\frac{a}{b} \\
\frac{x^2 + 1}{2x - 3} \\
\frac{\partial f}{\partial x}
\end{align}
$$

```latex
$$
\begin{align}
\frac{a}{b} \\
\frac{x^2 + 1}{2x - 3} \\
\frac{\partial f}{\partial x}
\end{align}
$$
```

行内分数使用 `\dfrac` 可获得更大的显示尺寸：$\dfrac{1}{2}$

---

## 根号

$$
\begin{align}
\sqrt{x} \\
\sqrt{x^2 + y^2} \\
\sqrt[3]{8} = 2 \\
\sqrt[n]{a}
\end{align}
$$

```latex
$$
\begin{align}
\sqrt{x} \\
\sqrt{x^2 + y^2} \\
\sqrt[3]{8} = 2 \\
\sqrt[n]{a}
\end{align}
$$
```

---

## 求和与乘积

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

## 极限

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

## 积分

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

## 矩阵

### 不带括号

$$
\begin{matrix}
a & b \\
c & d
\end{matrix}
$$

### 方括号矩阵

$$
A = \begin{bmatrix}
1 & 2 & 3 \\
4 & 5 & 6 \\
7 & 8 & 9
\end{bmatrix}
$$

```latex
$$
A = \begin{bmatrix}
1 & 2 & 3 \\
4 & 5 & 6 \\
7 & 8 & 9
\end{bmatrix}
$$
```

### 圆括号矩阵

$$
B = \begin{pmatrix}
a_{11} & a_{12} \\
a_{21} & a_{22}
\end{pmatrix}
$$

### 行列式

$$
\det(A) = \begin{vmatrix}
a & b \\
c & d
\end{vmatrix} = ad - bc
$$

```latex
$$
\det(A) = \begin{vmatrix}
a & b \\
c & d
\end{vmatrix} = ad - bc
$$
```

---

## 对齐环境

使用 `align` 环境对齐多行公式（`&` 为对齐点，`\\` 为换行）：

$$
\begin{align}
f(x) &= (x+1)^2 \\
     &= x^2 + 2x + 1
\end{align}
$$

```latex
$$
\begin{align}
f(x) &= (x+1)^2 \\
     &= x^2 + 2x + 1
\end{align}
$$
```

---

## 分段函数

$$
f(x) = \begin{cases}
x^2, & x \geq 0 \\
-x,  & x < 0
\end{cases}
$$

```latex
$$
f(x) = \begin{cases}
x^2, & x \geq 0 \\
-x,  & x < 0
\end{cases}
$$
```

---

## 希腊字母

| 小写 | 代码 | 大写 | 代码 |
| ---- | ---- | ---- | ---- |
| $\alpha$ | `\alpha` | $\Gamma$ | `\Gamma` |
| $\beta$ | `\beta` | $\Delta$ | `\Delta` |
| $\gamma$ | `\gamma` | $\Theta$ | `\Theta` |
| $\delta$ | `\delta` | $\Lambda$ | `\Lambda` |
| $\epsilon$ | `\epsilon` | $\Pi$ | `\Pi` |
| $\theta$ | `\theta` | $\Sigma$ | `\Sigma` |
| $\lambda$ | `\lambda` | $\Phi$ | `\Phi` |
| $\mu$ | `\mu` | $\Psi$ | `\Psi` |
| $\pi$ | `\pi` | $\Omega$ | `\Omega` |
| $\sigma$ | `\sigma` | | |
| $\phi$ | `\phi` | | |
| $\omega$ | `\omega` | | |

---

## 常用数学符号

### 关系符号

$$
\begin{align}
a \leq b \\
a \geq b \\
a \neq b \\
a \approx b \\
a \equiv b \\
a \propto b
\end{align}
$$

```latex
$$
\begin{align}
a \leq b \\
a \geq b \\
a \neq b \\
a \approx b \\
a \equiv b \\
a \propto b
\end{align}
$$
```

### 集合符号

$$
\begin{align}
A \cup B \\
A \cap B \\
A \subset B \\
x \in A \\
x \notin B \\
\emptyset
\end{align}
$$

```latex
$$
\begin{align}
A \cup B \\
A \cap B \\
A \subset B \\
x \in A \\
x \notin B \\
\emptyset
\end{align}
$$
```

### 逻辑符号

$$
\begin{align}
p \land q \\
p \lor q \\
\lnot p \\
p \Rightarrow q \\
p \Leftrightarrow q \\
\forall x \\
\exists x
\end{align}
$$

```latex
$$
\begin{align}
p \land q \\
p \lor q \\
\lnot p \\
p \Rightarrow q \\
p \Leftrightarrow q \\
\forall x \\
\exists x
\end{align}
$$
```

### 箭头

$$
\begin{align}
\leftarrow \\
\rightarrow \\
\leftrightarrow \\
\Leftarrow \\
\Rightarrow \\
\Leftrightarrow \\
\uparrow \\
\downarrow
\end{align}
$$

```latex
$$
\begin{align}
\leftarrow \\
\rightarrow \\
\leftrightarrow \\
\Leftarrow \\
\Rightarrow \\
\Leftrightarrow \\
\uparrow \\
\downarrow
\end{align}
$$
```

---

## 向量与矩阵记法

$$
\begin{align}
\vec{a} \\
\overrightarrow{AB} \\
\hat{n} \\
\bar{x} \\
\tilde{X} \\
\mathbf{A} \\
\boldsymbol{\omega}
\end{align}
$$

```latex
$$
\begin{align}
\vec{a} \\
\overrightarrow{AB} \\
\hat{n} \\
\bar{x} \\
\tilde{X} \\
\mathbf{A} \\
\boldsymbol{\omega}
\end{align}
$$
```

---

## 括号与大括号自动缩放

使用 `\left` 和 `\right` 让括号自动适应内容高度：

$$
\left( \frac{x^2 + 1}{2} \right)^2
$$

$$
\left[ \sum_{i=1}^{n} a_i \right]
$$

$$
\left\{ x \mid x > 0 \right\}
$$

```latex
$$
\left( \frac{x^2 + 1}{2} \right)^2
$$

$$
\left[ \sum_{i=1}^{n} a_i \right]
$$

$$
\left\{ x \mid x > 0 \right\}
$$
```

---

## 微积分常用

### 导数

$$
\begin{align}
f'(x) \\
f''(x) \\
\frac{dy}{dx} \\
\frac{d^2y}{dx^2} \\
\frac{\partial z}{\partial x}
\end{align}
$$

```latex
$$
\begin{align}
f'(x) \\
f''(x) \\
\frac{dy}{dx} \\
\frac{d^2y}{dx^2} \\
\frac{\partial z}{\partial x}
\end{align}
$$
```

### 泰勒展开

$$
\cos x = \sum_{k=0}^{\infty} \frac{(-1)^k}{(2k)!} x^{2k} = 1 - \frac{x^2}{2!} + \frac{x^4}{4!} - \cdots
$$

```latex
$$
\cos x = \sum_{k=0}^{\infty} \frac{(-1)^k}{(2k)!} x^{2k}
= 1 - \frac{x^2}{2!} + \frac{x^4}{4!} - \cdots
$$
```

---

## 线性代数

### 转置与逆

$$
\begin{align}
A^T \\
A^{-1} \\
(AB)^T = B^T A^T
\end{align}
$$

```latex
$$
\begin{align}
A^T \\
A^{-1} \\
(AB)^T = B^T A^T
\end{align}
$$
```

### 特征值

$$
\begin{align}
Av &= \lambda v \\
\det(A - \lambda I) &= 0
\end{align}
$$

```latex
$$
\begin{align}
Av &= \lambda v \\
\det(A - \lambda I) &= 0
\end{align}
$$
```

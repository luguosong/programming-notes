---
title: 矩阵与线性代数
---

# 矩阵与线性代数

## 🔢 矩阵 — `matrix`、`bmatrix`、`pmatrix`、`vmatrix`

LaTeX 通过不同的环境名称来控制矩阵的**定界符样式**，核心内容（`&` 分隔列、`\\` 换行）完全相同：

| 环境 | 名称由来 | 定界符 |
|------|---------|--------|
| `matrix` | 矩阵（matrix） | 无 |
| `bmatrix` | **b**rackets（方括号） | `[]` |
| `pmatrix` | **p**arentheses（圆括号） | `()` |
| `vmatrix` | **v**ertical bars（竖线） | `\|\|` |

### 不带括号 — `\begin{matrix}`

纯数字阵列，无定界符包裹：

$$
\begin{matrix}
a & b \\
c & d
\end{matrix}
$$

```latex
$$
\begin{matrix}
a & b \\
c & d
\end{matrix}
$$
```

### 方括号矩阵 — `\begin{bmatrix}`

**b** 代表 brackets，是最常见的矩阵表示形式：

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

### 圆括号矩阵 — `\begin{pmatrix}`

**p** 代表 parentheses，常用于表示向量或坐标：

$$
B = \begin{pmatrix}
a_{11} & a_{12} \\
a_{21} & a_{22}
\end{pmatrix}
$$

```latex
$$
B = \begin{pmatrix}
a_{11} & a_{12} \\
a_{21} & a_{22}
\end{pmatrix}
$$
```

### 行列式 — `\begin{vmatrix}`

**v** 代表 vertical bars，用竖线表示行列式（determinant），行列式的值是一个标量而非矩阵：

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

## ⚖️ 对齐环境 — `\begin{align}`

`align` 环境（对齐）用于多行公式推导，核心语法：

- `&` — 标记**对齐点**（通常是 `=` 号的位置），同一列的 `&` 会垂直对齐
- `\\` — 换行符，每行公式末尾必须添加

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

## 📑 分段函数 — `\begin{cases}`

`cases` 环境（情况）用于定义**分段函数**，每个分支用 `&` 分隔条件和表达式：

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

## 🔄 转置与逆 — `A^T`、`A^{-1}`

- `A^T` — **转置**（Transpose），上标 `T` 取自 Transpose，将矩阵的行列互换
- `A^{-1}` — **逆矩阵**（Inverse），上标 `-1` 表示矩阵的乘法逆元，满足 $AA^{-1} = I$
- `(AB)^T = B^T A^T` — 转置的反向律：乘积的转置等于转置的反向乘积

$$ A^T $$

$$ A^{-1} $$

$$ (AB)^T = B^T A^T $$

```latex
$$ A^T $$

$$ A^{-1} $$

$$ (AB)^T = B^T A^T $$
```

---

## ✨ 特征值 — `Av = \lambda v`

特征值（eigenvalue）问题是线性代数的核心：寻找标量 $\lambda$ 和非零向量 $v$，使得矩阵 $A$ 作用于 $v$ 后只产生缩放：

- 第一行是**定义**：$Av = \lambda v$
- 第二行是**特征方程**：$\det(A - \lambda I) = 0$，通过令特征多项式为零来求解 $\lambda$

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

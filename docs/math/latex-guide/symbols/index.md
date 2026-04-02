# 符号参考

## 希腊字母

LaTeX 中希腊字母的命令名就是其英文拼写（或缩写），小写命令全拼，大写命令首字母大写：

| 小写 | 代码 | 大写 | 代码 |
| ---- | ---- | ---- | ---- |
| $\alpha$ | `\alpha` | $\Gamma$ | `\Gamma` |
| $\beta$ | `\beta` | $\Delta$ | `\Delta` |
| $\gamma$ | `\gamma` | $\Theta$ | `\Theta` |
| $\delta$ | `\delta` | $\Lambda$ | `\Lambda` |
| $\epsilon$ | `\epsilon` | $\Pi$ | `\Pi` |
| $\theta$ | `\theta` | $\Sigma$ | `\Sigma` |
| $\lambda$ | `\lambda` | $\Phi$ | `\Phi` |
| $\mu$ | `\mu` | $\Psi` | `\Psi` |
| $\pi$ | `\pi` | $\Omega$ | `\Omega` |
| $\sigma$ | `\sigma` | | |
| $\phi$ | `\phi` | | |
| $\omega$ | `\omega` | | |

---

## 常用数学符号

### 关系符号 — `\leq`、`\geq`、`\neq`

关系符号的命令名多为英文单词的缩写：

- `\leq` — **less or equal**（小于等于），`q` 是 equal 的缩写
- `\geq` — **greater or equal**（大于等于）
- `\neq` — **not equal**（不等于），`\ne` 也可以，`q` 后缀保持一致
- `\approx` — **approximately**（约等于）
- `\equiv` — **equivalent**（恒等/等价）
- `\propto` — **proportional**（正比于）

$$ a \leq b $$

$$ a \geq b $$

$$ a \neq b $$

$$ a \approx b $$

$$ a \equiv b $$

$$ a \propto b $$

```latex
$$ a \leq b $$

$$ a \geq b $$

$$ a \neq b $$

$$ a \approx b $$

$$ a \equiv b $$

$$ a \propto b $$
```

### 集合符号 — `\cup`、`\cap`、`\subset`

- `\cup` — **union**（并集），符号 ∪ 像一个杯子（cup）
- `\cap` — **intersection**（交集），符号 ∩ 像一个倒扣的杯子（cap）
- `\subset` — **subset**（真子集），`sub` = under/below
- `\in` — **in**（属于），元素与集合的关系
- `\notin` — **not in**（不属于）
- `\emptyset` — **empty set**（空集），`\varnothing` 是另一种写法

$$ A \cup B $$

$$ A \cap B $$

$$ A \subset B $$

$$ x \in A $$

$$ x \notin B $$

$$ \emptyset $$

```latex
$$ A \cup B $$

$$ A \cap B $$

$$ A \subset B $$

$$ x \in A $$

$$ x \notin B $$

$$ \emptyset $$
```

### 逻辑符号 — `\land`、`\lor`、`\lnot`

- `\land` — **logical and**（逻辑与），符号 ∧
- `\lor` — **logical or**（逻辑或），符号 ∨
- `\lnot` — **logical not**（逻辑非），符号 ¬
- `\Rightarrow` — 推出（单箭头），命题逻辑中的蕴含
- `\Leftrightarrow` — 当且仅当（双箭头），等价关系
- `\forall` — **for all**（全称量词），倒置的 A（All）
- `\exists` — **exists**（存在量词），倒置的 E（Exists）

$$ p \land q $$

$$ p \lor q $$

$$ \lnot p $$

$$ p \Rightarrow q $$

$$ p \Leftrightarrow q $$

$$ \forall x $$

$$ \exists x $$

```latex
$$ p \land q $$

$$ p \lor q $$

$$ \lnot p $$

$$ p \Rightarrow q $$

$$ p \Leftrightarrow q $$

$$ \forall x $$

$$ \exists x $$
```

### 箭头 — `\rightarrow`、`\Rightarrow`

箭头的命名规则非常直观：

- `\leftarrow` / `\rightarrow` — 向左/向右的单线箭头
- `\Leftarrow` / `\Rightarrow` — 向左/向右的**双线**箭头（首字母大写 = 双线），常用于逻辑蕴含
- `\leftrightarrow` / `\Leftrightarrow` — 双向箭头（等价关系）
- `\uparrow` / `\downarrow` — 向上/向下箭头

$$ \leftarrow $$

$$ \rightarrow $$

$$ \leftrightarrow $$

$$ \Leftarrow $$

$$ \Rightarrow $$

$$ \Leftrightarrow $$

$$ \uparrow $$

$$ \downarrow $$

```latex
$$ \leftarrow $$

$$ \rightarrow $$

$$ \leftrightarrow $$

$$ \Leftarrow $$

$$ \Rightarrow $$

$$ \Leftrightarrow $$

$$ \uparrow $$

$$ \downarrow $$
```

---

## 向量与矩阵记法 — `\vec{}`、`\mathbf{}`

LaTeX 提供多种方式来标记向量、单位向量等特殊含义的量：

- `\vec{a}` — **vector**（向量），在字母上方加箭头
- `\overrightarrow{AB}` — 加长箭头，适合标注从 A 到 B 的向量
- `\hat{n}` — **hat**（帽子），表示单位向量
- `\bar{x}` — **bar**（横线），常表示均值或复共轭
- `\tilde{X}` — **tilde**（波浪线），常表示估计值或等价类
- `\mathbf{A}` — **math boldface**（数学粗体），用粗体表示矩阵或向量
- `\boldsymbol{\omega}` — **bold symbol**（粗体符号），专门用于希腊字母等符号的粗体

$$ \vec{a} $$

$$ \overrightarrow{AB} $$

$$ \hat{n} $$

$$ \bar{x} $$

$$ \tilde{X} $$

$$ \mathbf{A} $$

$$ \boldsymbol{\omega} $$

```latex
$$ \vec{a} $$

$$ \overrightarrow{AB} $$

$$ \hat{n} $$

$$ \bar{x} $$

$$ \tilde{X} $$

$$ \mathbf{A} $$

$$ \boldsymbol{\omega} $$
```

---

## 括号自动缩放 — `\left`、`\right`

普通括号的大小是固定的，当括号内包含分数、求和等高大内容时，需要 `\left` 和 `\right` 让括号**自动匹配内容高度**：

- `\left(` ... `\right)` — 圆括号自动缩放
- `\left[` ... `\right]` — 方括号自动缩放
- `\left\{` ... `\right\}` — 大括号自动缩放（注意大括号需要转义 `\{`）

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

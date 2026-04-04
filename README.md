<h1 align="center">programming-notes</h1>

<p align="center">
  <span>学习编程过程中整理的笔记集合。它涵盖了多个领域的知识</span>
</p>

<p align="center">
  <img alt="GitHub repo size" src="https://img.shields.io/github/repo-size/luguosong/programming-notes">
  <img alt="GitHub Created At" src="https://img.shields.io/github/created-at/luguosong/programming-notes">
  <img alt="GitHub License" src="https://img.shields.io/github/license/luguosong/programming-notes">
</p>

## 本地运行

- 首先安装Python环境。

- 安装zensical

```shell
pip install zensical
```

- 运行

```shell
zensical serve
```

- 编译成静态网页

```shell
zensical build
```

具体操作请查看[zensical文档](https://zensical.org/docs/get-started/)。

## 笔记教学风格

本仓库所有自编笔记（`docs/` 下非翻译类内容）统一采用**深入浅出的教学风格**写作，目标是：**让初学者能读懂、让高手觉得有收获**。

### 写作原则

| 原则 | 说明 |
|------|------|
| 类比优先 | 用生活常识类比技术概念，再给出精确定义。如「JWT 就像一张盖了公章的纸条，任何人都能读，但伪造不了」 |
| 循序渐进 | 先给「够用的版本」，再补充完整细节；先给最简示例，再演进到生产级写法 |
| 口语化且严谨 | 可用「其实」「注意」「换句话说」，但技术术语必须准确 |
| 主动语态 | 「Spring 会自动注入」> 「Bean 会被自动注入」 |
| 术语零门槛 | 遇到专有名词先解释，不假设读者已掌握前置概念 |

### 知识点结构

每个知识点按以下脉络展开：

1. **是什么（What）** — 一句话定义，用最朴素的语言
2. **为什么（Why）** — 解释存在的意义 / 解决了什么问题
3. **怎么用（How）** — 实际代码或操作示例
4. **注意点（Pitfalls）** — 常见误区或边界情况（可选）

### 代码示例

- 每个示例标注语言，关键行加注释
- 提供最小可运行版本，不堆砌无关配置
- 错误示例标注 `// ❌`，正确示例标注 `// ✅`
- 复杂示例前先用自然语言说明「这段代码做了什么」

### 节奏控制

- 每个 H3 知识点控制在 **5 分钟内**可读完
- 每 2-3 个知识点后加小结或对比表格，帮助建立结构感
- 长文开头加「本文你会学到」的要点列表

### 禁止事项

- 不要上来就贴大段代码没有任何解释
- 不要堆砌官方文档原文
- 不要「总之就是这样」式的虎头蛇尾
- 不要假设读者已经知道前置概念

> 翻译类内容（`docs/document-translation/`）忠于原书风格，不适用以上约束。

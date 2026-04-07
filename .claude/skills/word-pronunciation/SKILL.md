---
name: word-pronunciation
description: Use when adding click-to-speak pronunciation buttons to words in documentation pages built with Zensical/MkDocs Material. Triggers on words like pronunciation, 发音, TTS, speech, voice, read aloud, 朗读.
---

# Word Pronunciation Buttons

在 Zensical 文档页面中添加点击即可发音的按钮，基于浏览器原生 Web Speech API，无需外部服务。

## Overview

项目中已实现一套内联发音按钮系统：

- **JS**：`docs/custom/js/pronunciation.js`（事件委托 + `speechSynthesis`）
- **CSS**：`docs/custom/css/pronunciation.css`（按钮样式 + 深色模式适配）
- 已在 `zensical.toml` 中注册，所有页面自动可用

## Markdown 语法

在 Markdown 中插入发音按钮：

```html
<button class="md-pronounce-btn" data-word="hello" title="点击发音"><i class="fa fa-volume-up"></i></button>
```

### 参数说明

| 属性 | 必填 | 说明 | 示例 |
|------|------|------|------|
| `data-word` | 是* | 要朗读的文本 | `"hello"`、`"good morning"` |
| `data-ipa` | 是* | IPA 音标，自动朗读代表单词 | `"æ"` → 读 `cat` |
| `data-lang` | 否 | 语言代码，默认 `en-US` | `"en-GB"`、`"zh-CN"` |
| `data-rate` | 否 | 语速，默认 `0.8`（0.1-2.0） | `"0.6"`（慢）、`"1.0"`（正常） |

> *`data-word` 和 `data-ipa` 二选一，`data-ipa` 优先级更高。

### 已支持的 IPA 音标

| IPA | 代表单词 | IPA | 代表单词 |
|-----|---------|-----|---------|
| `æ` | cat | `ɛ` | bed |
| `ɪ` | sit | `ɒ` | hot |
| `ʌ` | cup | `eɪ` | day |
| `iː` | see | `aɪ` | kite |
| `oʊ` | go | `juː` | cute |
| `ɑː` | father | `ɔː` | door |
| `ʊ` | book | `aʊ` | cow |
| `ɔɪ` | boy | `ʃ` | ship |
| `tʃ` | chip | `θ` | think |
| `ð` | this | `ʒ` | vision |
| `ŋ` | sing | `j` | yes |
| `w` | we | `ɜːr` | bird |
| `ɑːr` | car | `ɔːr` | horse |

### 常见用法

```markdown
<!-- 单词发音 -->
`cat` <button class="md-pronounce-btn" data-word="cat" title="点击发音"><i class="fa fa-volume-up"></i></button>

<!-- 音标发音（自动朗读代表单词） -->
/æ/ <button class="md-pronounce-btn" data-ipa="æ" title="听 /æ/ 发音"><i class="fa fa-volume-up"></i></button>

<!-- 短语发音 -->
`good morning` <button class="md-pronounce-btn" data-word="good morning" title="点击发音"><i class="fa fa-volume-up"></i></button>

<!-- 慢速发音（适合初学者） -->
`through` <button class="md-pronounce-btn" data-word="through" data-rate="0.6" title="点击发音"><i class="fa fa-volume-up"></i></button>

<!-- 英式发音 -->
`water` <button class="md-pronounce-btn" data-word="water" data-lang="en-GB" title="点击发音"><i class="fa fa-volume-up"></i></button>
```

## Quick Reference

| 场景 | 语法 |
|------|------|
| 美式英语（默认） | `data-word="word"` |
| 音标发音 | `data-ipa="æ"`（自动朗读代表单词） |
| 英式英语 | `data-word="word" data-lang="en-GB"` |
| 慢速朗读 | `data-word="word" data-rate="0.6"` |
| 短语/句子 | `data-word="good morning"` |
| 中文发音 | `data-word="你好" data-lang="zh-CN"` |

## Implementation Details

### JS 机制

- 使用 `document$.subscribe()` 兼容 Zensical 的 Instant Navigation
- 事件委托（`document.addEventListener('click', ...)`），无需手动重新绑定
- 点击时自动 `cancel()` 上一次朗读，避免叠加
- 朗读中按钮变色（`.md-pronounce--playing` 类），结束后恢复

### CSS 机制

- 按钮内联显示（20x20px），不破坏行内排版
- FontAwesome 4 图标（`fa-volume-up`）
- 深色模式通过 `[data-md-color-scheme="slate"]` 适配
- hover/active/playing 三态样式

## Common Mistakes

| 错误 | 正确做法 |
|------|---------|
| `data-word` 为空 | 必须指定要朗读的文本 |
| 忘记 `class="md-pronounce-btn"` | 类名是 JS 识别的唯一标识 |
| 放在代码块内 | HTML 在代码块中不会被渲染，放在代码块外 |
| 用 `<a>` 标签 | 必须用 `<button>`，避免链接语义干扰 |

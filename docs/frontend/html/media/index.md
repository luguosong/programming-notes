# 媒体

图片、视频、音频——这些多媒体内容让网页从「文字堆砌」变成「图文并茂」甚至「声画同步」的丰富体验。本节将系统学习 HTML 中所有与媒体相关的标签和最佳实践。

🎯 掌握 `img` 标签的使用与图片格式选型
💡 理解响应式图片的实现方式（`<picture>` + `<source>`）
🔧 学会使用 `video` 和 `audio` 标签嵌入音视频
⚡ 认识 `<track>` 标签为视频添加字幕

## 🖼️ 怎么在页面中展示图片？

网页中到处都是图片——产品照片、用户头像、Logo、Banner……`img` 是 HTML 中最常用的媒体标签之一，但用好它并不简单。

### img 标签

`img`（image）是一个`自闭合标签`，用来在页面中嵌入图片。它有两个最重要的属性：

- `src`（source）：图片的路径，可以是相对路径或绝对 URL
- `alt`（alternative text）：`替代文本`，图片无法显示时的文字描述（`强制要求`）

``` html title="img 基本用法"
--8<-- "docs/frontend/html/media/demo/img-basic.html"
```

<iframe class="html-demo" loading="lazy" src="demo/img-basic.html"></iframe>

⚠️ `` `alt` 属性不能省略 ``。它不仅是无障碍访问（屏幕阅读器会朗读 `alt` 内容）的基础，还在图片加载失败时作为占位文字显示，同时也是搜索引擎理解图片内容的重要依据。

如果图片纯粹是装饰性的、不传达任何信息，可以写空值：

``` html title="装饰性图片的 alt 写法"
--8<-- "docs/frontend/html/media/demo/decorative-img-alt.html"
```

<iframe class="html-demo" loading="lazy" src="demo/decorative-img-alt.html"></iframe>

💡 `img` 是一个「行内元素」，但它的默认表现更像「替换元素」（类似行内块元素），可以设置宽高。

### 图片格式对比

选择合适的图片格式是前端优化的重要环节。不同格式各有特点，适用场景也不同：

| 格式 | 压缩方式 | 透明度 | 动画 | 适用场景 |
| ---- | -------- | ------ | ---- | -------- |
| **JPEG/JPG** | 有损压缩 | ❌ 不支持 | ❌ 不支持 | 摄影照片、色彩丰富的网页图片（非透明背景） |
| **PNG** | 无损压缩 | ✅ 支持 | ❌ 不支持 | Logo、网页图形、需要透明度的图像 |
| **GIF** | 无损压缩 | ✅ 支持 | ✅ 支持 | 简单动画、表情包、低色彩图形（最多 256 色） |
| **WebP** | 有损 / 无损 | ✅ 支持 | ✅ 支持 | 网页优化——可替代 JPEG/PNG/GIF，体积更小 |
| **AVIF** | 有损 / 无损 | ✅ 支持 | ✅ 支持 | 未来网页优化——基于 AV1 编码，压缩率极高 |

📌 `选型建议`：日常开发中，优先使用 `WebP` 格式（体积小、兼容性好），需要透明度且 WebP 不可用时降级为 `PNG`，照片类降级为 `JPEG`。

### 响应式图片

不同设备（手机、平板、桌面）的屏幕宽度和分辨率差异很大，一张图片「适配所有屏幕」是不现实的——手机上加载一张 4K 大图纯属浪费流量。`<picture>` 标签配合 `<source>` 可以让浏览器根据条件选择最合适的图片。

``` html title="picture 响应式图片"
--8<-- "docs/frontend/html/media/demo/picture-responsive.html"
```

<iframe class="html-demo" loading="lazy" src="demo/picture-responsive.html"></iframe>

!!! note "MDN"

    `<picture>` 内部必须包含一个 `<img>` 标签作为兜底。浏览器会从上到下检查每个 `<source>` 的条件，匹配到第一个就使用，都不匹配则显示 `<img>`。

    `<source>` 的关键属性：
    - `media`：媒体查询条件（如 `(max-width: 600px)`）
    - `srcset`：对应的图片路径（可以是逗号分隔的多张图片，配合描述符如 `1x`、`2x` 适配不同 DPI）

💡 `srcset` 还可以直接用在 `<img>` 上，实现更简单的分辨率适配：

``` html title="img 标签的 srcset 简写"
--8<-- "docs/frontend/html/media/demo/img-srcset.html"
```

<iframe class="html-demo" loading="lazy" src="demo/img-srcset.html"></iframe>

### figure 与 figcaption

当图片需要附带说明文字（图注）时，应该使用 `<figure>` + `<figcaption>` 组合——而不是自己用 `<div>` + `<p>` 硬拼。

``` html title="figure + figcaption 图注"
--8<-- "docs/frontend/html/media/demo/figure-figcaption.html"
```

<iframe class="html-demo" loading="lazy" src="demo/figure-figcaption.html"></iframe>

!!! note "MDN"

    `<figure>` 是一个「独立的引用单元」，它表示页面中一段自包含的内容（图片、代码、表格、引用等），通常带有标题。`<figcaption>` 为其提供说明文字，一个 `<figure>` 最多包含一个 `<figcaption>`。

💡 `<figure>` 的好处：语义清晰（屏幕阅读器能正确关联图片和说明文字），CSS 样式也更容易统一管理。

---

## 🎬 怎么嵌入视频？

HTML5 之前，网页播放视频只能依赖 Flash——需要安装插件，安全性差，移动端不支持。`<video>` 标签的出现彻底改变了这一局面，浏览器原生支持视频播放。

### video 标签

``` html title="video 基本用法"
--8<-- "docs/frontend/html/media/demo/video-basic.html"
```

<iframe class="html-demo" loading="lazy" src="demo/video-basic.html"></iframe>

`video` 标签的常用属性：

| 属性 | 说明 |
| ---- | ---- |
| `src` | 视频文件路径 |
| `controls` | 显示播放控件（播放/暂停、进度条、音量等） |
| `width` / `height` | 设置视频显示区域的宽高（单位像素） |
| `autoplay` | 自动播放（注意：大多数浏览器要求同时设置 `muted`） |
| `loop` | 循环播放 |
| `muted` | 静音播放 |
| `poster` | 视频加载前显示的预览图 |

💡 标签之间的文字（如「您的浏览器不支持 video 标签」）是`降级内容`，在不支持 `<video>` 的浏览器中会显示。

⚠️ `不建议同时设置 width 和 height`——这会锁定视频的宽高比。更好的做法是只设置其中一个，让浏览器按原始比例自动计算另一个。如果需要响应式，用 CSS 设置 `width: 100%` 即可。

``` html title="video 自动静音播放"
--8<-- "docs/frontend/html/media/demo/video-autoplay-muted.html"
```

<iframe class="html-demo" loading="lazy" src="demo/video-autoplay-muted.html"></iframe>

### 视频格式兼容

视频编码格式（容器 + 编解码器）的兼容性比图片复杂得多。主流格式及其浏览器支持：

| 格式 | 容器 | 编解码器 | 兼容性 |
| ---- | ---- | -------- | ------ |
| **MP4** | MP4 | H.264 | ✅ 几乎所有浏览器（最广泛） |
| **WebM** | WebM | VP8 / VP9 | ✅ Chrome、Firefox、Edge、Opera |
| **Ogg** | Ogg | Theora | ⚠️ Firefox、Chrome、Opera（Safari 不支持） |

📌 `实际建议`：提供 MP4 格式作为主格式（兼容性最好），同时用 `<source>` 标签提供 WebM 格式作为备选。

``` html title="video 多格式兼容写法"
--8<-- "docs/frontend/html/media/demo/video-multi-format.html"
```

<iframe class="html-demo" loading="lazy" src="demo/video-multi-format.html"></iframe>

⚠️ `务必写 type 属性`。浏览器会根据 `type` 检查自己是否支持该格式，只加载第一个匹配的文件。如果不写 `type`，浏览器会逐个尝试加载每个 `<source>`，白白浪费时间和带宽。

---

## 🔊 怎么嵌入音频？

`<audio>` 标签的用法和 `<video>` 几乎一样，只是少了一些视频专属的属性（如 `poster`、`width`/`height`）。

### audio 标签

``` html title="audio 基本用法"
--8<-- "docs/frontend/html/media/demo/audio-basic.html"
```

<iframe class="html-demo" loading="lazy" src="demo/audio-basic.html"></iframe>

`audio` 标签的属性与 `video` 基本一致：

| 属性 | 说明 |
| ---- | ---- |
| `src` | 音频文件路径 |
| `controls` | 显示播放控件 |
| `autoplay` | 自动播放（同样需要配合 `muted`） |
| `loop` | 循环播放 |
| `muted` | 静音播放 |

💡 没有指定 `controls` 时，`<audio>` 不会显示任何界面——用户看不到也控制不了。所以在大多数场景下，`controls` 属性是必需的。

### 音频格式兼容

| 格式 | 编解码器 | 兼容性 |
| ---- | -------- | ------ |
| **MP3** | MPEG-1 Audio Layer III | ✅ 几乎所有浏览器 |
| **Ogg** | Vorbis | ✅ Chrome、Firefox、Edge、Opera |
| **WAV** | PCM（无损） | ⚠️ Chrome、Firefox、Edge、Safari（但文件体积大） |

``` html title="audio 多格式兼容写法"
--8<-- "docs/frontend/html/media/demo/audio-multi-format.html"
```

<iframe class="html-demo" loading="lazy" src="demo/audio-multi-format.html"></iframe>

⚠️ 同样注意：`type 属性不要省略`，否则浏览器会逐个下载尝试。

📝 **小结**：`<audio>` 和 `<video>` 的使用模式完全一致——用 `<source>` 提供多种格式，写上 `type` 让浏览器智能选择，最后放一段降级文字兜底。

---

## 📝 怎么给视频加字幕？

为视频添加字幕不仅是无障碍访问的要求，也是很多场景下的刚需（比如外语视频、嘈杂环境、静音浏览）。HTML 通过 `<track>` 标签实现这一功能。

### track 标签

`<track>` 作为 `<video>` 或 `<audio>` 的子标签使用，用于加载字幕文件。字幕文件采用 `WebVTT`（Web Video Text Tracks）格式，扩展名为 `.vtt`。

``` html title="video + track 字幕"
--8<-- "docs/frontend/html/media/demo/video-track-subtitles.html"
```

<iframe class="html-demo" loading="lazy" src="demo/video-track-subtitles.html"></iframe>

`track` 标签的属性：

| 属性 | 说明 |
| ---- | ---- |
| `src` | WebVTT 字幕文件路径 |
| `kind` | 轨道类型：`subtitles`（字幕）、`captions`（包含音效描述的字幕）、`descriptions`（视频描述）、`chapters`（章节导航） |
| `srclang` | 语言代码（如 `zh`、`en`、`ja`） |
| `label` | 在播放器字幕菜单中显示的名称 |
| `default` | 设为默认显示的字幕轨道 |

!!! note "MDN"

    WebVTT（.vtt）文件是一个纯文本格式，结构简单：

    ```webvtt title="subtitles/intro-zh.vtt"
    --8<-- "docs/frontend/html/media/demo/subtitles-intro-zh.vtt"
    ```

    - 文件必须以 `WEBVTT` 开头
    - 每条字幕由「时间戳」和「文本内容」组成
    - 时间格式：`时:分:秒.毫秒 --> 时:分:秒.毫秒`

💡 `<track>` 的好处在于：浏览器原生支持字幕切换——用户只需点击播放器上的「CC」按钮（Closed Captions），就能在不同语言的字幕之间切换，无需任何 JavaScript。

📝 **小结**：字幕是视频内容的标配，不是可选项。`<track>` + WebVTT 的组合让字幕实现变得轻量而标准，牢记 `kind`、`srclang`、`label`、`default` 四个属性就够了。

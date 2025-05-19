---
icon: simple/jetbrains
---

# IDEA使用心得

## 常用设置

### 定义实时模板

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409261739726.png){ loading=lazy }
  <figcaption>通过输入少量提示，生成代码块</figcaption>
</figure>

## 主题和插件

### One Dark Theme 主题

[https://github.com/atom/one-dark-syntax](https://github.com/atom/one-dark-syntax)

### codeium 插件

`Codeium`是现代编码的超级力量，是一个基于尖端人工智能技术构建的免费代码加速工具包。目前，Codeium提供超过70种语言的代码补全工具，速度快且建议质量一流。

现代编码工作流中有许多令人厌烦、乏味或令人沮丧的部分，从重复使用样板代码到搜索StackOverflow。最近人工智能的进步使我们能够消除这些部分，使将您的想法转化为代码变得无缝。通过轻松集成到所有JetBrains
IDE中，并且安装过程不到2分钟，您可以专注于成为最优秀的软件开发人员，而不是最优秀的代码猴子。

使用 Codeium，您将获得：

- 无限的单行和多行代码补全，永不限制
- 集成在 IDE 中的聊天功能：无需离开您的 IDE 即可使用 ChatGPT，提供生成文档字符串和解释等便捷建议
- 支持 70 多种编程语言：JavaScript、Python、TypeScript、PHP、Go、Java、C、C++、Rust、Ruby 等
- 通过我们的 Discord 社区提供支持

#### downloading language server异常

找到插件位置，找到`language_server_windows_x64.exe`位置，比如：

```text
C:\Users\10545\AppData\Roaming\JetBrains\IntelliJIdea2024.1\plugins\codeium\4afed79fc3218d4ed6a74b3082a291b8e866ba19\language_server_windows_x64.exe
```

如果下载异常，`language_server_windows_x64.exe`文件会一直处于`language_server_windows_x64.exe.downloading`状态。

删除该文件，手动去github下载：

```text
https://github.com/Exafunction/codeium/releases/tag/language-server-v1.10.11
```

❗注意：地址中版本号要改为与插件一致。

### MyBatisX 插件

MybatisX 插件功能：

- Mapper 和 XML 文件可以相互跳转。
- 支持 Mybatis.xml 和 Mapper.xml 的代码提示。
- Mapper 和 XML 支持类似 JPA 的自动提示功能（参考 MybatisCodeHelperPro）。
- 集成 Mybatis Generator 的图形界面（参考 Free Mybatis 插件）。

### Translation 插件

基于IntelliJ的IDE的翻译插件。

### Rainbow Brackets 插件

彩虹括号核心功能：

- 为各种类型的括号（圆括号、花括号、方括号、尖括号）添加彩虹效果
- 为更多语言的变量添加彩虹效果，并使用颜色生成器配置颜色
- 彩虹化缩进指南
- 作用域高亮
- 为 XML/HTML 的标签名添加彩虹效果
- 为 YAML/JSON 的属性名添加彩虹效果
- 自定义颜色
- 颜色生成器
- 支持 JSX
- 为 Python 关键字和缩进指南添加彩虹效果
- Python 的作用域高亮
- 当前代码块高亮
- 支持代码块列表
- Kotlin 函数字面量括号和箭头彩虹效果

### Grep Console 插件

日志高亮美化插件

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409061558667.png){ loading=lazy }
  <figcaption>配置</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409061559895.png){ loading=lazy }
  <figcaption>效果</figcaption>
</figure>

### .ignore插件

- 文件语法高亮
- 在规则生成器中按名称和内容筛选和选择模板
- 用户自定义模板
- 通过指定的Gitignore文件显示被忽略的文件（右键点击.gitignore文件）
- 在当前选定目录中创建文件
- 基于[GitHub的模板集合][github-gitignore]生成Gitignore规则
- 从弹出菜单中将选定的文件/目录添加到Gitignore规则中
- 为新项目建议创建.gitignore文件
- 条目检查（重复、覆盖、未使用、语法错误、相对条目）并提供快速修复操作
- 支持注释和括号
- 在项目视图中导航到条目
- 从dot-ignore文件中重命名条目
- 关闭已打开的被忽略文件操作
- 用户自定义模板支持导入/导出功能

## 常见问题

### 插件无法下载

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202505191615270.png){ loading=lazy }
  <figcaption>代理方式必须选择手动代理中的SOCKS，如果选择自动检测代理配置或HTTP将无法下载插件</figcaption>
</figure>

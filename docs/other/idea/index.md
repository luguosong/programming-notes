---
icon: simple/jetbrains
---

# IDEA使用心得

## One Dark Theme 主题

[https://github.com/atom/one-dark-syntax](https://github.com/atom/one-dark-syntax)

## codeium 插件

`Codeium`是现代编码的超级力量，是一个基于尖端人工智能技术构建的免费代码加速工具包。目前，Codeium提供超过70种语言的代码补全工具，速度快且建议质量一流。

现代编码工作流中有许多令人厌烦、乏味或令人沮丧的部分，从重复使用样板代码到搜索StackOverflow。最近人工智能的进步使我们能够消除这些部分，使将您的想法转化为代码变得无缝。通过轻松集成到所有JetBrains
IDE中，并且安装过程不到2分钟，您可以专注于成为最优秀的软件开发人员，而不是最优秀的代码猴子。

使用 Codeium，您将获得：

- 无限的单行和多行代码补全，永不限制
- 集成在 IDE 中的聊天功能：无需离开您的 IDE 即可使用 ChatGPT，提供生成文档字符串和解释等便捷建议
- 支持 70 多种编程语言：JavaScript、Python、TypeScript、PHP、Go、Java、C、C++、Rust、Ruby 等
- 通过我们的 Discord 社区提供支持

### downloading language server异常

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

## MyBatisX 插件

MybatisX 插件功能：

- Mapper 和 XML 文件可以相互跳转。
- 支持 Mybatis.xml 和 Mapper.xml 的代码提示。
- Mapper 和 XML 支持类似 JPA 的自动提示功能（参考 MybatisCodeHelperPro）。
- 集成 Mybatis Generator 的图形界面（参考 Free Mybatis 插件）。

# 概述

本文旨在为编写测试的程序员、扩展开发者、引擎开发者，以及构建工具和集成开发环境（IDE）供应商提供全面的参考文档。

## 什么是JUnit5

JUnit 5 与之前的版本不同，它由三个不同子项目中的多个模块组成。

```text
JUnit 5 = JUnit Platform + JUnit Jupiter + JUnit Vintage
```

`JUnit Platform`是一个用于在JVM上启动测试框架的基础工具，同时定义了`TestEngine` API，用于开发运行在该平台上的测试框架。此外，该平台还提供了一个
`命令行启动器`（Console Launcher），可以通过命令行启动平台，以及JUnit平台套件引擎（JUnit Platform Suite
Engine），用于在平台上使用一个或多个测试引擎运行自定义测试套件。主流的集成开发环境（如IntelliJ IDEA、Eclipse、NetBeans和Visual
Studio Code）以及构建工具（如Gradle、Maven和Ant）都对JUnit平台提供了一流的支持。

`JUnit Jupiter`是JUnit 5中用于编写测试和扩展的编程模型与扩展模型的结合。Jupiter子项目提供了一个TestEngine，用于在平台上运行基于Jupiter的测试。

而`JUnit Vintage`则提供了一个TestEngine，用于在平台上运行基于JUnit 3和JUnit 4的测试。需要注意的是，JUnit
Vintage要求类路径或模块路径中至少包含JUnit 4.12或更高版本。

## 支持Java版本

JUnit 5 运行时需要 Java 8 或更高版本。不过，你仍然可以测试使用旧版 JDK 编译的代码。

## 获取帮助

请在 [Stack Overflow](https://stackoverflow.com/questions/tagged/junit5) 上提问有关 JUnit 5
的问题，或在 [Gitter](https://app.gitter.im/#/room/#junit-team_junit5:gitter.im) 上与社区交流。

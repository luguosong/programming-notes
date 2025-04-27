# Maven基础教程

## 简介

Apache Maven，是一个软件（特别是Java软件）`项目管理`及`自动构建`工具，由Apache软件基金会所提供。Maven也可被用于构建和管理各种项目，例如C#，Ruby，Scala和其他语言编写的项目。Maven曾是Jakarta项目的子项目，现为由Apache软件基金会主持的独立Apache项目。

Maven解决了软件构建的两方面问题：

- 一是软件是如何构建的
- 二是软件的依赖关系

## 安装配置

### window安装

!!! 安装前提条件
    
    需要先安装java环境，并配有`JAVA_HOME`环境变量。

    因为Maven采用Java语言编写

- 下载并解压

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405101009751.png){ loading=lazy }
  <figcaption>直接从官网下载压缩包，并解压</figcaption>
</figure>

- 环境变量中配置`MAVEN_HOME`和`Path`。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405101018048.png){ loading=lazy }
  <figcaption>配置MAVEN_HOME</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405101020325.png){ loading=lazy }
  <figcaption>配置Path</figcaption>
</figure>

- cmd调用`mvn`命令,测试是否配置完成

```shell
mvn -v
```

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405101023655.png){ loading=lazy }
  <figcaption>打印类似如上图内容说明安装成功</figcaption>
</figure>

### 配置

- 打开`安装目录/conf/settings.xml`进行配置。
- 修改本地仓库位置。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405101027637.png){ loading=lazy }
  <figcaption>配置本地仓库位置</figcaption>
</figure>

- 配置中央仓库镜像。

[具体配置指南](https://developer.aliyun.com/mvn/guide)

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405101029507.png){ loading=lazy }
  <figcaption>配置中央仓库地址</figcaption>
</figure>

- 设置JDK的编译版本

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405101423726.png){ loading=lazy }
  <figcaption>设置JDK的编译版本</figcaption>
</figure>

### IDEA中配置Maven

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405101427662.png){ loading=lazy }
  <figcaption>IDEA中配置Maven</figcaption>
</figure>

## 项目创建

### 创建JavaSE项目

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405101704894.png){ loading=lazy }
  <figcaption>创建基本的Web项目</figcaption>
</figure>

### 手动创建Java Web项目



### IDEA插件创建Java Web项目

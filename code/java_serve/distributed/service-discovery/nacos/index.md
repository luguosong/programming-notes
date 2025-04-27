# Nacos

## 安装

### Windows安装

- 官方下载二进制包：

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202410151623345.png){ loading=lazy }
  <figcaption>官方下载nacos二进制包</figcaption>
</figure>

- 解压到相应目录，进入bin目录，执行目录启动服务：

```shell
startup.cmd -m standalone
```

浏览器通过 `http://127.0.0.1:8848/nacos` 地址可以访问控制台。

### 带数据库配置

- 在mysql数据库中创建一个名为 `nacos` 的数据库。
- 在nacos安装包`conf目录`下找到`mysql-schema.sql`并执行。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202410151651336.png){ loading=lazy }
  <figcaption>sql脚本</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202410151653248.png){ loading=lazy }
  <figcaption>sql脚本会生成以下表</figcaption>
</figure>

- 打开`conf/application.properties`配置数据库链接参数：

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202410151655025.png){ loading=lazy }
  <figcaption>修改数据库链接配置</figcaption>
</figure>

启动服务：

```shell
startup.cmd -m standalone
```

## 入门案例

先确认使用的`Spring Cloud Alibaba`的版本，再去官网找到对应`Spring Cloud`和`Spring Boot`的版本：

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202410162204234.png){ loading=lazy }
  <figcaption>根据Spring Cloud Alibaba版本，找到Spring Cloud和Spring Boot的版本</figcaption>
</figure>

通过`<dependencyManagement>`进行版本管理：

```xml

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>2023.0.3</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-alibaba-dependencies</artifactId>
            <version>2023.0.1.2</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

引入`spring-cloud-starter-alibaba-nacos-discovery`启动器，完整的`pom.xml`如下：

``` xml title="pom.xml"
--8<-- "code/java_serve/distributed/service-discovery/nacos/nacos-consumer/pom.xml"
```

服务生产者配置文件：

``` yaml title="application.yml"
--8<-- "code/java_serve/distributed/service-discovery/nacos/nacos-producer/src/main/resources/application.yml"
```

服务生产者编写测试服务接口：

``` java title="DemoController.java"
--8<-- "code/java_serve/distributed/service-discovery/nacos/nacos-producer/src/main/java/com/luguosong/nacosproducer/controller/DemoController.java"
```

服务消费者编写测试接口：

``` java title="TestController.java"
--8<-- "code/java_serve/distributed/service-discovery/nacos/nacos-consumer/src/main/java/com/luguosong/nacosconsumer/controller/TestController.java"
```

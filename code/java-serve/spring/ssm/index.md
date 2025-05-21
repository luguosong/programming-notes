# SSM整合

## 全注解开发

### 创建Maven项目并引入依赖

- 将`<packaging>`标记为war包，表示这是一个Web应用
- 在`src/main/`目录下创建`webapp/WEB-INF/`目录，用于存放视图文件。

``` xml title="pom.xml"
--8<-- "code/java-serve/spring/ssm/ssm-annotation/pom.xml"
```

### 数据库参数配置文件

- 在`src/main/resources/`目录下创建`jdbc.properties`文件，内容如下：

``` properties title="jdbc.properties"
--8<-- "code/java-serve/spring/ssm/ssm-annotation/src/main/resources/jdbc.properties"
```

### 配置数据源

``` java title="DataSourceConfig.java"
--8<-- "code/java-serve/spring/ssm/ssm-annotation/src/main/java/com/luguosong/config/DataSourceConfig.java"
```

### Mybatis配置类

``` java title="MyBatisConfig.java"
--8<-- "code/java-serve/spring/ssm/ssm-annotation/src/main/java/com/luguosong/config/MyBatisConfig.java"
```

### 开启事务

1. Spring配置类添加`@EnableTransactionManagement`注解
2. 在`DataSourceConfig.java`中配置事务管理器

### Spring配置类

``` java title="SpringConfig.java"
--8<-- "code/java-serve/spring/ssm/ssm-annotation/src/main/java/com/luguosong/config/SpringConfig.java"
```

### 定义拦截器

``` java title="MyInterceptor.java"
--8<-- "code/java-serve/spring/ssm/ssm-annotation/src/main/java/com/luguosong/interceptor/MyInterceptor.java"
```

### 错误处理视图

``` html title="html"
--8<-- "code/java-serve/spring/ssm/ssm-annotation/src/main/webapp/WEB-INF/thymeleaf/error.html"
```

### Spring MVC配置类

1. 配置视图解析器
2. 配置包扫描
3. 开启注解驱动
4. 开启静态资源访问
5. 配置视图控制器
6. 配置异常处理器
7. 配置拦截器

``` java title="SpringMvcConfig.java"
--8<-- "code/java-serve/spring/ssm/ssm-annotation/src/main/java/com/luguosong/config/SpringMvcConfig.java"
```

### web配置类

1. 指定Spring配置类
2. 指定Spring MVC配置类
3. 配置DispatcherServlet的url-pattern
4. 配置过滤器

``` java title="WebAppInitialize.java"
--8<-- "code/java-serve/spring/ssm/ssm-annotation/src/main/java/com/luguosong/config/WebAppInitialize.java"
```

### 创建实体类

``` java title="User.java"
--8<-- "code/java-serve/spring/ssm/ssm-annotation/src/main/java/com/luguosong/bean/User.java"
```

### 编写dao层

``` java title="UserDao.java"
--8<-- "code/java-serve/spring/ssm/ssm-annotation/src/main/java/com/luguosong/dao/UserDao.java"
```

### 编写service层

``` java title="UserService.java"
--8<-- "code/java-serve/spring/ssm/ssm-annotation/src/main/java/com/luguosong/service/UserService.java"
```

``` java title="UserServiceImpl.java"
--8<-- "code/java-serve/spring/ssm/ssm-annotation/src/main/java/com/luguosong/service/impl/UserServiceImpl.java"
```

### 编写Controller层

``` java title="UserController.java"
--8<-- "code/java-serve/spring/ssm/ssm-annotation/src/main/java/com/luguosong/controller/UserController.java"
```


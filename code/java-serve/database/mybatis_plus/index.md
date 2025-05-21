# Mybatis Plus

## 入门案例

创建项目，导入相关依赖：

``` xml
--8<-- "code/java-serve/database/mybatis_plus/mybatis-plus-hello/pom.xml"
```

application.yml文件配置数据库连接：

``` yaml
--8<-- "code/java-serve/database/mybatis_plus/mybatis-plus-hello/src/main/resources/application.yml"
```

在 Spring Boot 启动类中添加 @MapperScan 注解，扫描 Mapper 文件夹:

``` java
--8<-- "code/java-serve/database/mybatis_plus/mybatis-plus-hello/src/main/java/com/luguosong/mybatisplushello/MybatisPlusHelloApplication.java"
```

编写实体类：

``` java
--8<-- "code/java-serve/database/mybatis_plus/mybatis-plus-hello/src/main/java/com/luguosong/mybatisplushello/entity/User.java"
```

编写 Mapper 接口类:

``` java
--8<-- "code/java-serve/database/mybatis_plus/mybatis-plus-hello/src/main/java/com/luguosong/mybatisplushello/mapper/UserMapper.java"
```

编写测试类：

``` java
--8<-- "code/java-serve/database/mybatis_plus/mybatis-plus-hello/src/test/java/com/luguosong/mybatisplushello/MybatisPlusHelloApplicationTests.java"
```

## 常见注解

### @TableName

默认情况实体类类名驼峰转下划线作为表名，`@TableName`用来自定义表名。

``` java

@TableName("tb_user")
public class User {

	private Long id;

	private String name;
}
```

### @TableId

默认情况下实体类名为id的字段作为主键，`@TableId`用来自定义主键。

``` java

public class User {

	@TableId(value = "id", type = IdType.AUTO)
	private Long id;

	private String name;
}
```

### @TableField

默认情况下实体类成员字段名驼峰转下划线作为表的字段名，`@TableField`用来自定义字段名。

``` java

@TableName("tb_user")
public class User {

	private Long id;

	// 实体类字段名与表字段不一致，可以使用@TableField注解指定表字段名
	@TableField("user_name")
	private String name;

	//当表字段名与mysql关键字冲突，可以使用转义字符处理
	@TableField("`order`")
	private String order;

	// 当实体类字段在表中不存在时
	@TableField(exist = false)
	private String address;
}
```

## 条件构造器

详细条件构造器参考[官网](https://baomidou.com/guides/wrapper/)

``` java title="WrapperTest.java"
--8<-- "code/java-serve/database/mybatis_plus/mybatis-plus-hello/src/test/java/com/luguosong/mybatisplushello/WrapperTest.java"
```

## 自定义SQL

## Service接口

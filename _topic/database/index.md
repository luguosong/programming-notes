---
layout: note
title: 数据库
create_time: 2023/9/7
---

# 历史

- 1995年，瑞典MySQL AB公司开发的`MySQL`数据库诞生。
- 2008年，Sun公司收购MySQL AB公司。
- 2009年，Oracle公司收购Sun公司。MySQL成为Oracle旗下产品。
- 2009年，MySQL AB公司的创始人`Michael Widenius`开发出MySQL的分支——以其最小的女儿玛莉亚（Maria）命名的`MariaDB`
- 2015年，`Mysql5.7`发布
- 2016年，`Mysql8.0`发布。MySQL5.x之后的license分为`免费的社区版`与`收费的标准版、企业版`等。

# 版本

- `Mysql Community Server`：社区版，开源免费
- `Mysql Enterprise Server`：企业版，收费
- `Mysql Cluster`：集群版，开源免费
- `Mysql Cluster CGE`：高级集群版，收费

# 安装Mysql

## 下载

官网下载免费社区版Mysql：

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202309071828340.png)

选择社区版服务：

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202309071841013.png)

## Windows安装

基本就是傻瓜式安装，略过。

⭐安装完后将mysql中的bin目录添加到环境变量中。这样就可以在任意目录下使用mysql命令了。

# 卸载Mysql

## Windows卸载

- 先停止服务

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202309071813117.png)

- 使用控制面板卸载软件
- 删除数据文件存储目录
- 删除环境变量

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202309071821732.png)

# 登录

```shell
# 登录
mysql -u root -P 端口 -h IP号 -p
```

# 默认数据库

- `information_schema`：存储数据库的元数据，如数据库名、表名、列名等。
- `mysql`：系统信息，存储用户信息、字符集等。
- `performance_schema`：存储数据库的性能相关信息。
- `sys`：存储数据库的结构信息。

# 示例数据库脚本

[点击下载](https://cdn.jsdelivr.net/gh/luguosong/images@master/file/sql/demo.sql)

# 默认编码设置

5.x版本1需要将编码设置为`utf8`，8.x版本需要将编码设置为`utf8mb4`。

```sql
-- 查看编码 --
show
variables like 'character%';

-- 查看比较规则,用于排序 --
show
variables like 'collation%';
```

在`my.ini`中设置：

```ini
[mysql]
default-character-set = utf8

[mysqld]
character-set-server = utf8
collation-server = utf8_general_ci
```

# SQL分类

- `DDL`：Data definition language,数据定义语言，用于定义数据库对象：数据库、表、列等。如：create、drop、alter。
- `DQL`：Data query language,数据查询语言，用于查询数据库中表的记录。如：select、describe。
- `DML`：Data manipulation language,数据操作语言，用于对数据库中表的数据进行增删改。如：insert、update、delete。
- `DCL`：Data control language,数据控制语言，用于定义访问权限和安全级别。如：grant、revoke。

# DQL-SELECT查询

## 基本查询

```sql
-- 查询表中所有字段 --
select *
from employees

-- 指定字段 --
select employee_id, last_name, salary
from employees

-- 别名（三种方式） --
select employee_id as id, last_name 名字, salary "员工 收入"
from employees

-- 去重 --
-- 去重一般只查询一个字段 --
select distinct department_id
from employees

-- null处理 --
-- commission_pct存在null值，使用IFNULL函数处理 --
select salary "月收入", salary * (1 + IFNULL(commission_pct, 0)) * 12 "年收入"
from employees

-- 关键字冲突 --
-- 当表名或字段是关键字时，使用着重号``包裹 --
select * from `order`;
```

## 条件查询

```sql
-- 查询last_name为King的员工 --
select *
from employees
where last_name = 'King'
```

## 算数运算符

```sql
-- 加减运算 --
-- 结果为：110, 90, 135.5 --
select 100 + 10, 100 - 10, 100 + 35.5
from dual;

-- 数字和字符串进行运算 --
-- 如果字符串是纯数字，会自动转换为数字类型 --
-- 如果字符串不是纯数字，会转换为0 --
-- 结果为：101, 100 --
select 100 + '1', 100 + 'a'
from dual;

-- 与null进行运算 --
-- 结果为：null --
select 100 + null
from dual;

-- 除法运算 --
-- 结果为：50.0000 , 33.3333 , 33 --
select 100 / 2, 100 / 3, 100 div 3
from dual;
```

## 比较运算符

```sql
-- 等于 --
select *
from employees
where commission_pct = 0.1;

-- 数字和字符串进行比较 --
-- 如果字符串是纯数字，会自动转换为数字类型 --
-- 如果字符串不是纯数字，会转换为0 --
select *
from employees
where commission_pct = '0.1';

-- 与null进行比较 --
-- ❌判断一个值是否为null，不能使用=或!=，而是使用is null或is not null --
select *
from employees
where commission_pct = null;
-- ✅ --
select *
from employees
where commission_pct is null;
-- 判断字段不是null --
select *
from employees
where commission_pct is not null;

-- 最大值和最小值 --
select least(5,10,3), greatest(5,10,3)
from dual;

-- 范围 --
-- between and包含边界值 --
select *
from employees
where salary between 10000 and 20000;

-- 在指定数值内 --
select *
from employees
where salary in (10000, 12000, 17000);

-- 模糊查询 --
-- %表示任意多个字符，_表示一个字符 --
select *
from employees
where last_name like '%ll%';

-- 正则表达式 --
-- ^表示开头，$表示结尾 --
select *
from employees
where last_name regexp '^a.*e$';
```

## 逻辑运算符

```sql
-- 逻辑或 --
select *
from employees
where salary < 10000 or salary > 20000;

-- 逻辑与 --
select *
from employees
where salary > 10000 and salary < 20000;

-- 逻辑非 --
select *
from employees
where not salary > 10000;
```

## 排序

```sql
-- 升序 --
select *
from employees
order by salary;

-- 降序 --
select *
from employees
order by salary desc;
```



# DQL-DESCRIBE查询

```sql
-- 查询表结构 --
describe employees

-- 查询表结构（简写） --
desc employees
```


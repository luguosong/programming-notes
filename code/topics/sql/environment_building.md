# SQL

## 数据库管理系统(DBMS)种类

关系型数据库：

- `Oracle`:第一个商业的关系型数据库。
- `MySQL`: 开放源代码的关系型数据库。由瑞典MySQL AB(创始人Michael Widenius)公司于1995年开发。
- `MariaDB`: 2008年Sun公司收购MySQL，2009年Oracle收购Sun。担心MySQL闭源，因此建立了MySQL的分支项目MariaDB。
- `Microsoft SQL Server`:由微软开发的大型商业关系型数据库。
- `DB2`:由IBM开发的商业关系型数据库。常应用在银行系统中。
- `PostgreSQL`:开源关系型数据库。最符合SQL标准。
- `SyBase`:已经淡出历史舞台，但提供了一个非常专业的数据库建模工具`PowerDesigner`。
- `SQLite`:嵌入式的小型数据库，应用在手机端。不需要安装、不需要配置、不需要启动、关闭或配置数据库实例。
- `informix`:IBM公司出品，取自information和Unix的结合，它是第一个被移植到Linux上的商业数据库。仅运行于unix/linux平台。

键值型数据库：

- `Redis`：使用ANSI C编写的支持网络、基于内存、分布式、可选持久性的键值对存储数据库。

文档型数据库：

- `MongoDB`：一种面向文档的数据库管理系统，用C++等语言撰写而成，介于关系型数据库和非关系型数据库之间，以解决应用程序开发社区中的大量现实问题。

## 环境搭建

### Windows安装配置MySql 8.0.26

[安装包下载地址](https://dev.mysql.com/downloads/mysql/)

- 双击下载的msi文件，打开安装向导。
- 选择`自定义安装`

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405011628249.png){ loading=lazy }
  <figcaption>打开Choosing a Setup Type窗口，选择安装类型，</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405012018783.png){ loading=lazy }
  <figcaption>此时如果直接“Next”（下一步），则产品的安装路径是默认的。如果想要自定义安装目录，则可以选中
对应的产品，然后在下面会出现“Advanced Options”（高级选项）的超链接。</figcaption>
</figure>

<figure markdown="span">
  ![❗目录地址不要包含中文](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405012153168.png){ loading=lazy }
  <figcaption>设置程序和数据目录</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405012300025.png){ loading=lazy }
  <figcaption>执行安装</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405020905845.png){ loading=lazy }
  <figcaption>配置主机类型和端口</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405022116763.png){ loading=lazy }
  <figcaption>设置授权方式</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405022117889.png){ loading=lazy }
  <figcaption>设置root用户密码</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405022127397.png){ loading=lazy }
  <figcaption>设置服务</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405022128484.png){ loading=lazy }
  <figcaption>执行配置</figcaption>
</figure>

### Windows安装Mysql 5.7

与安装`MySql 8.0.26`步骤基本相同

### Windows安装MySql 8.4.0

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405022200866.png){ loading=lazy }
  <figcaption>打开安装程序</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405022204141.png){ loading=lazy }
  <figcaption>同意用户许可协议</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405022206799.png){ loading=lazy }
  <figcaption>选择安装类型</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405022208117.png){ loading=lazy }
  <figcaption>选择安装方式和安装路径</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405022209137.png){ loading=lazy }
  <figcaption>开始安装</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405022211279.png){ loading=lazy }
  <figcaption>完成安装，并开始配置mysql</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405022213581.png){ loading=lazy }
  <figcaption>进入配置页面</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405022219903.png){ loading=lazy }
  <figcaption>如果本电脑上安装过mysql，选择与之前服务并存</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405022222829.png){ loading=lazy }
  <figcaption>配置主机类型和端口</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405022223978.png){ loading=lazy }
  <figcaption>设置root用户密码</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405022225243.png){ loading=lazy }
  <figcaption>设置服务</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405022228661.png){ loading=lazy }
  <figcaption>配置服务器文件权限</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405022229672.png){ loading=lazy }
  <figcaption>创建示例数据库</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405022230545.png){ loading=lazy }
  <figcaption>执行配置</figcaption>
</figure>

### Windows卸载MySql

- 停止服务

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202404292124989.png){ loading=lazy }
  <figcaption>停止mysql服务</figcaption>
</figure>

- 使用`Windows控制面板`卸载软件
- 删除数据文件和配置文件
- 清理环境变量
- 如果是5.x版本，还需要手动清理服务

```text
// 注册表中的ControlSet001,ControlSet002,不一定是001和002,可能是ControlSet005、006之类

HKEY_LOCAL_MACHINE\SYSTEM\ControlSet001\Services\MySQL服务 目录删除
HKEY_LOCAL_MACHINE\SYSTEM\ControlSet002\Services\Eventlog\Application\MySQL服务 目录删除
HKEY_LOCAL_MACHINE\SYSTEM\ControlSet002\Services\MySQL服务 目录删除
HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Services\Eventlog\Application\MySQL服务目录删除
HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Services\MySQL服务删除
```

- 重启电脑

## 登录服务

- 方式一：使用程序提供的登录界面,用户为root

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405022239479.png){ loading=lazy }
</figure>

- 方式二：使用cmd登录

```shell
mysql -u root -h 127.0.0.1 -P 3306 -p
```

## 解决mysql 5.x默认字符集问题

!!! bug "mysql 5.x默认字符集问题"
        
    mysql 5.x默认采用拉丁字符集，创建的表不能使用中文。

    mysql 8.x默认采用utf8编码，不需要配置。

``` shell title="查看默认字符集采用的是拉丁"
mysql> show variables like 'character_%';
+--------------------------+---------------------------------+
| Variable_name            | Value                           |
+--------------------------+---------------------------------+
| character_set_client     | gbk                             |
| character_set_connection | gbk                             |
| character_set_database   | latin1                          |
| character_set_filesystem | binary                          |
| character_set_results    | gbk                             |
| character_set_server     | latin1                          |
| character_set_system     | utf8                            |
| character_sets_dir       | D:\mysql-5.7.44\share\charsets\ |
+--------------------------+---------------------------------+
8 rows in set, 1 warning (0.00 sec)
```

```shell title="查看默认比较规则"
mysql> show variables like 'collation_%';
+----------------------+-----------------+
| Variable_name        | Value             |
+----------------------+-------------------+
| collation_connection | gbk_chinese_ci    |
| collation_database   | latin1_general_ci |
| collation_server     | latin1_general_ci |
+----------------------+-------------------+
3 rows in set, 1 warning (0.00 sec)
```

在my.ini文件中进行配置：

```ini title="my.ini"
[mysql] # 大概在63行左右，在其下添加
default-character-set=utf8


[mysqld] # 大概在76行左右，在其下添加
character-set-server=utf8
collation-server=utf8_general_ci
```

修改完配置，重启服务：

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405030934605.png){ loading=lazy }
  <figcaption>重启服务</figcaption>
</figure>


## SQL语言规则与规范

!!! 基本规则
    - SQL 可以写在一行或者多行。为了提高可读性，各子句分行写，必要时使用缩进
    - 每条命令以 `;` 或 `\g` 或 `\G` 结束
    - 关键字不能被缩写也不能分行

!!! 关于标点符号
    - 必须保证所有的()、单引号、双引号是成对结束的
    - 必须使用英文状态下的半角输入方式
    - `字符串型`和`日期时间`类型的数据可以使用`单引号（' '）`表示
    - 列的`别名`，尽量使用`双引号（" "）`，而且`不建议省略as`

!!! 大小写规范-建议遵守
    - 关键字、函数名、列名(或字段名)、列的别名(字段的别名) 是忽略大小写的。
    - MySQL 在 Windows 环境下是大小写不敏感的
    - MySQL 在 Linux 环境下是大小写敏感的。数据库名、表名、表的别名、变量名是严格区分大小写的
    - ❤️推荐采用统一的书写规范：数据库名、表名、表别名、字段名、字段别名等都小写。SQL 关键字、函数名、绑定变量等都大写

## 注释

```mysql
# 单行注释

-- 单行注释
    
/*
多行注释
多行注释
*/
```

## 导入数据

``` mysql title="使用命令导入数据"
source D:\data\test.sql
```

或者使用图形化界面，如Navicat导入sql文件。

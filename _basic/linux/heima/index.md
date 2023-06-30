---
layout: note
title: 黑马Linux笔记
nav_order: 1
parent: linux
create_time: 2023/6/29
---

# 文件和目录操作

## 目录创建-mkdir

```shell
# mkdir [选项] 目录名
# -p,--parents 递归创建目录
# -v,--verbose 显示创建过程
# -m,--mode=模式值 设置权限

# 递归创建目录
mkdir -p /tmp/a/b/c 
```

## 目录删除-rmdir

```shell
# rmdir [选项] 目录名
# -p,--parents 递归删除空目录
# -v,--verbose 显示删除过程

# 💀注意：删除的目录必须是空目录

# 递归删除空目录
rmdir -p /tmp/a/b/c 

# 删除非空目录，其中r表示递归删除，f表示强制删除
rm -rf /tmp/a/b/c 
```

## 文件创建-touch

```shell
# touch [选项] 文件名
# {开始..结束} 创建多个文件

# 创建文件
touch /tmp/a.txt

# 创建多个文件
touch /tmp/{a.txt,b.txt,c.txt}
touch /tmp/{a..c}.txt
```

## 文件删除-rm

```shell
# rm [选项] 文件名
# -f,--force 忽略不存在的文件，不提示用户
# -r,--recursive 递归删除目录下的文件

# 删除文件
rm /tmp/a.txt

# 递归删除
rm -r /tmp/a

# 强制删除
rm -f /tmp/a
```

## 复制-cp

```shell
# cp [选项] 源文件或目录 目标文件或目录
# -r,--recursive 递归复制目录

# 复制文件
cp /tmp/a.txt /tmp/

# 复制文件，并重命名
cp /tmp/a.txt /tmp/b.txt

# 复制文件夹
cp -r /tmp/a /tmp/
```

## 剪切、重命名-mv

```shell
# mv [选项] 源文件或目录 目标文件或目录

# 剪切文件
mv /tmp/a.txt /tmp/

# 剪切文件夹
mv /tmp/a /tmp/

# 文件重命名(将a.txt重命名为b.txt)
mv /tmp/a.txt /tmp/b.txt
```

# 文件打包压缩

## 打包-tar

```shell
# tar [选项] 目标文件 被打包文件名
# -c,--create 创建打包文件
# -v,--verbose 显示打包过程
# -f,--file=打包文件 指定打包文件
# -u,--update 更新打包文件
# -t,--list 列出打包文件中包含的文件
# -x,--extract 解包打包文件

# 打包多个文件（将a.txt和b.txt打包成test.tar）
tar -cvf /tmp/test.tar /tmp/a.txt /tmp/b.txt

# 更新打包文件（将c.txt添加到test.tar中）
tar -uvf /tmp/test.tar /tmp/c.txt 

# 查看打包文件中包含的文件
tar -tf /tmp/test.tar

# 释放打包文件
tar -xvf /tmp/test.tar
```

## gzip压缩

```shell
# 压缩文件
tar -zcvf /tmp/test.tar.gz /tmp/a.txt /tmp/b.txt

# 解压文件
tar -zxvf /tmp/test.tar.gz

# 解压文件到指定目录
tar -zxvf /tmp/test.tar.gz -C /tmp/
```

## bz2压缩

```shell
# 压缩文件
tar -jcvf /tmp/test.tar.bz2 /tmp/a.txt /tmp/b.txt

# 解压文件
tar -jxvf /tmp/test.tar.bz2

# 解压文件到指定目录
tar -jxvf /tmp/test.tar.bz2 -C /tmp/
```

## xz压缩

```shell
# 压缩文件
tar -Jcvf /tmp/test.tar.xz /tmp/a.txt /tmp/b.txt

# 解压文件
tar -Jxvf /tmp/test.tar.xz

# 解压文件到指定目录
tar -Jxvf /tmp/test.tar.xz -C /tmp/
```

## zip压缩

```shell
# 压缩文件
zip /tmp/test.zip /tmp/a.txt /tmp/b.txt

# 压缩文件夹
zip -r /tmp/test.zip /tmp/a

# 解压文件
unzip /tmp/test.zip

# 解压文件到指定路径
unzip /tmp/test.zip -d /tmp/
```

# 文件查看

## 正序查看文件内容-cat

```shell
# cat [选项] 文件名
# -n,--number 显示行号

# 查看文件内容
cat /tmp/a.txt
```

## 倒序查看文件内容-tac

```shell
# tac [选项] 文件名
# -n,--number 显示行号

# 查看文件内容
tac /tmp/a.txt
```

## 查看文件前几行-head

```shell
# head [选项] 文件名
# -n,--number 显示行数

# 查看文件前10行
head /tmp/a.txt

# 查看文件前5行
head -n 5 /tmp/a.txt
```

## 查看文件后几行-tail

```shell
# tail [选项] 文件名
# -n,--number 显示行数
# -f,--follow 循环读取

# 查看文件后10行
tail /tmp/a.txt

# 查看文件后5行
tail -n 5 /tmp/a.txt

# 动态查看文件内容，比如查看日志输出
tail -f /tmp/a.txt
```

## 分页查看文件内容-more

more命令查看文件时，打开文件时就已经加载到内存中，所以查看文件时，不会出现卡顿的情况。

退出查看后，文件内容仍然会存在于终端上

```shell
# more [选项] 文件名
# -数字 行数 指定每屏显示的行数

# 分页查看文件内容
more /tmp/a.txt
```

- 交互快捷键
    - `d`: 向下翻动半屏
    - `space`: 向下翻动一屏
    - `b`: 向上翻动半屏
    - `enter`: 向下翻动一行
    - `q`: 退出

## 分页查看文件内容-less

less is more，less时more的增强版

less命令查看文件时，打开文件时不会加载到内存中，所以查看文件时，会出现卡顿的情况。

不会在shell中留下查看的内容

```shell
# less [选项] 文件名
# -n,--lines=行数 指定每屏显示的行数

# 分页查看文件内容
less /tmp/a.txt
```

# 文件统计

## 统计文件内容-wc

word count

```shell
# wc [选项] 文件名
# -l,--lines 统计行数
# -w,--words 统计单词数
# -c,--bytes 统计字节数

# 统计文件行数
wc -l /tmp/a.txt

# 统计文件单词数
wc -w /tmp/a.txt

# 统计文件字节数
wc -c /tmp/a.txt
```

## 统计文件大小-du

disk usage

```shell
# du [选项] 文件名
# -h,--human-readable 以人类可读的方式显示
# -s,--summarize 只显示总计

# 统计文件大小
du -h /tmp/a.txt

# 统计文件夹大小
du -h /tmp/a

# 统计文件夹大小，只显示总计,不显示当中详细文件的大小
du -sh /tmp/a
```

# 文件搜索

## 搜索文件-find

```shell
# find [搜索范围] [选项] [搜索条件]
# -name 按照文件名搜索
# -iname 按照文件名搜索，忽略大小写
# -type 按照文件类型搜索,f:普通文件,d:目录,l:软链接

# 在当前目录下搜索文件名为a.txt的文件
find . -name a.txt

# 在当前目录下搜索文件名为a.txt的文件，忽略大小写
find . -iname a.txt

# 在当前目录下搜索文件名为a.txt的文件，忽略大小写，只搜索普通文件
find . -iname a.txt -type f

# 模糊搜索
find . -iname "a*"
```

## 搜索文件内容-grep

```shell
# grep [选项] 搜索内容 文件名
# -n,--line-number 显示行号

# 在当前目录下搜索文件内容为hello的文件
grep hello /tmp/a.txt
```

# 输出重定向

```shell
# > 覆盖,会覆盖原有内容
echo "hello" > /tmp/a.txt

# >> 追加，在原始内容末尾追加
echo "world" >> /tmp/a.txt
```

# 用户和用户组

## 概述

`用户`可以有一个`主组`和多个`附属组`，用户创建时，会自动创建一个和用户名相同的`主组`。

## 用户组位置

用户组会保存到`/etc/group`文件中，每一行代表一个用户组，每一行的格式如下:

```shell
用户组名:密码占位符:用户组id:用户列表
```

## 添加用户组-groupadd

```shell
# groupadd 用户组名
# -g,--gid 指定用户组id,如果不指定，默认从1000开始

# 添加用户组，并指定编号
groupadd -g 1000 test
```

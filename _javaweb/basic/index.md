---
layout: note
title: Web基础
nav_order: 10
create_time: 2023/9/26
---

# 容器

Servlet没有main方法，它们受控于另一个Java应用，这个应用就是Servlet`容器`。

容器提供一下功能：
- 通信支持
- 管理Servlet生命周期管理
- 多线程支持，为每个请求创建一个线程，当Servlet运行完毕，线程销毁
- 声明式实现安全
- JSP支持，把JSP代码翻译为Java（Servlet）代码

容器处理请求的步骤：
1. 用户发起请求
2. 容器接收到请求，判断该请求为Servlet请求，创建`HttpServletRequest`和`HttpServletResponse`对象
3. 容器根据请求地址，找到对应的Servlet，创建或分配线程创建Servlet实例，并将`HttpServletRequest`和`HttpServletResponse`对象传递给Servlet
4. 容器调用Servlet的`service`方法
5. 根据请求类型的不同，service方法调用`doGet`或`doPost`方法
6. Servlet处理请求，将响应内容写入`HttpServletResponse`对象
7. Servlet线程销毁，容器把`HttpServletResponse`对象转为一个HTTP响应，发送给用户
8. 容器删除`HttpServletRequest`和`HttpServletResponse`对象

# Servlet组件

Servlet是Java提供的一门动态web资源开发技术。



# URL映射到Servlet

## 使用XML映射

## 使用注解映射

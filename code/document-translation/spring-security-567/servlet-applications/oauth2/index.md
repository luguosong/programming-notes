# OAuth2

Spring Security 提供了对 OAuth 2.0 的全面支持。本节将介绍如何在基于 Servlet 的应用中集成 OAuth 2.0。

## 概述

Spring Security 对 OAuth 2.0 的支持主要包括两大功能模块：

- OAuth2 资源服务器
- OAuth2 客户端

!!! note

	OAuth2 登录是一个非常强大的 OAuth2 客户端功能，足以在参考文档中单独开设一节进行说明。不过，它并不是一个独立的特性，必须依赖 OAuth2 客户端才能发挥作用。



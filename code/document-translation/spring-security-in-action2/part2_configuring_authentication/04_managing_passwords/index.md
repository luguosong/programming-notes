# 密码管理

本章内容包括：

- 实现并使用 PasswordEncoder
- 利用 Spring Security Crypto 模块提供的工具。

在第3章中，我们讨论了如何在使用 Spring Security 实现的应用程序中管理用户。那么，密码该如何处理呢？密码无疑是认证流程中至关重要的一环。本章将带你了解如何在基于 Spring Security 的应用中管理密码和密钥。我们将探讨 PasswordEncoder 接口，以及 Spring Security Crypto 模块（SSCM）为密码管理提供的相关工具。

## 使用密码编码器

通过第三章的学习，你现在应该已经清楚了解了 UserDetails 接口的作用，以及多种实现方式。正如你在第二章中所学，不同的组件在认证和授权过程中负责管理用户信息。你还了解到，其中一些组件有默认实现，比如 UserDetailsService 和 PasswordEncoder。现在你已经知道可以自定义这些默认实现。接下来，我们将深入探讨这些 Bean 及其实现方式，本节将重点分析 PasswordEncoder。图 4.1 展示了 PasswordEncoder 在认证流程中的位置。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202508221359247.png){ loading=lazy }
  <figcaption>图4.1 Spring Security认证流程。AuthenticationProvider在认证过程中通过PasswordEncoder校验用户密码。</figcaption>
</figure>


# 实现身份认证

本章内容包括：

- 使用自定义 AuthenticationProvider 实现认证逻辑
- 采用 HTTP Basic 和表单登录方式进行身份验证
- 理解并管理 Security-Context 组件

第3章和第4章介绍了在认证流程中参与的一些组件。我们讨论了 UserDetails 以及如何定义原型来描述 Spring Security 中的用户。接着，我们在示例中使用了 UserDetails，展示了 UserDetailsService 和 UserDetailsManager 的接口规范及其实现方式。我们还讨论并演示了这些接口的主流实现。最后，你学习了 PasswordEncoder 如何管理密码及其使用方法，以及 Spring Security 加密模块（SSCM）中的加密器和密钥生成器的用法。



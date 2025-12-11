# Spring Authorization Server 迁移至 Spring Security 7.0

!!! warning

	Spring Authorization Server 已迁移至 Spring Security 7.0。

	1.5.x 分支是 Spring Authorization Server 的最后一个代次版本。请参阅开源版和企业版的支持时间线。

	未来，从 7.0 开始，新的功能将会添加到 Spring Security 中。

自 2022 年 11 月正式发布 1.0 版本以来，Spring Authorization Server 已经走过了不短的路程。它一开始是从 Spring Security 中独立出来的项目，这种模式让它能够快速迭代功能，最终成长为一个功能丰富的 OAuth2 授权服务器构建解决方案。如今，它已经达到了相当成熟和稳定的阶段，我们认为现在是将它并入 Spring Security 7.0 的合适时机。

这次调整给用户带来的最主要好处，是更加顺畅统一的开发体验。无论你使用的是 OAuth2 Client 还是 OAuth2 Authorization Server，再也不需要在不同项目之间来回切换，因为源码、Javadoc 和参考文档都会集中在 Spring Security 中。此外，所有与 OAuth2 相关的 issue 和 pull request 也将统一在 Spring Security 的 GitHub 仓库中进行管理。

我们也预计，这次迁移对用户的影响会非常有限，因为 Maven 坐标中的 groupId 和 artifactId 将保持不变，唯一的变化是版本号。例如，Spring Security 7.0 对应的 Maven 坐标将是：  
`org.springframework.security:spring-security-oauth2-authorization-server:7.0.0`。

同时，现有类的名称和包路径基本都会保持不变，只有极少数包会做轻微的迁移调整，我们有信心这些变更对大家来说只是一次非常直观、容易完成的升级。

在将 Spring Authorization Server 并入 Spring Security 7.0 的过程中，我们和你一样感到兴奋，也期待你能和我们一起迎接这一变化。

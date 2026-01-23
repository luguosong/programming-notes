
# 实现多租户(Multitenancy)

本指南介绍如何定制 Spring Authorization Server，使其在多租户托管配置中支持同一主机下的多个 Issuer。本文旨在展示一种为
Spring Authorization Server 构建支持多租户组件的通用模式，你也可以将该模式应用到其他组件中，以满足自身需求。

## 定义租户标识符

OpenID Connect 1.0 提供方配置端点和 OAuth2 授权服务器元数据端点允许在 issuer 标识符的值中包含路径组件，这实际上使得在同一主机上支持多个
issuer 成为可能。

例如，发起 OpenID Provider 配置请求
`http://localhost:9000/issuer1/.well-known/openid-configuration`，或发起授权服务器元数据请求 `http://localhost:9000/.well-known/oauth-authorization-server/issuer1`，将会返回如下配置元数据：

``` json
{
  "issuer": "http://localhost:9000/issuer1",
  "authorization_endpoint": "http://localhost:9000/issuer1/oauth2/authorize",
  "token_endpoint": "http://localhost:9000/issuer1/oauth2/token",
  "jwks_uri": "http://localhost:9000/issuer1/oauth2/jwks",
  "revocation_endpoint": "http://localhost:9000/issuer1/oauth2/revoke",
  "introspection_endpoint": "http://localhost:9000/issuer1/oauth2/introspect",
  ...
}
```

!!! note

    协议端点的基础 URL 为颁发者标识符（issuer identifier）的值。

本质上，带有路径部分的发行方标识符就代表“租户标识符”。

## 启用多个签发方

默认未启用每个主机使用多个签发者（issuer）的支持。要启用，请添加以下配置：

``` java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;

@Configuration(proxyBeanMethods = false)
public class AuthorizationServerSettingsConfig {

	@Bean
	public AuthorizationServerSettings authorizationServerSettings() {
		return AuthorizationServerSettings.builder()
				//设置为 true 以允许同一主机使用多个签发方。
				.multipleIssuersAllowed(true)
				.build();
	}

}
```

## 创建一个组件注册表

我们先搭建一个简单的注册表，用于管理每个租户对应的具体组件。该注册表包含相应逻辑，可通过 issuer 标识符的值来获取某个类的具体实现。

下面每个委托实现都会使用到以下这个类：

``` java
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContext;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class TenantPerIssuerComponentRegistry {
	private final ConcurrentMap<String, Map<Class<?>, Object>> registry = new ConcurrentHashMap<>();

	//组件注册会隐式启用一份可用的已批准签发者白名单。
	public <T> void register(String tenantId, Class<T> componentClass, T component) {
		Assert.hasText(tenantId, "tenantId cannot be empty");
		Assert.notNull(componentClass, "componentClass cannot be null");
		Assert.notNull(component, "component cannot be null");
		Map<Class<?>, Object> components = this.registry.computeIfAbsent(tenantId, (key) -> new ConcurrentHashMap<>());
		components.put(componentClass, component);
	}

	@Nullable
	public <T> T get(Class<T> componentClass) {
		AuthorizationServerContext context = AuthorizationServerContextHolder.getContext();
		if (context == null || context.getIssuer() == null) {
			return null;
		}
		for (Map.Entry<String, Map<Class<?>, Object>> entry : this.registry.entrySet()) {
			if (context.getIssuer().endsWith(entry.getKey())) {
				return componentClass.cast(entry.getValue().get(componentClass));
			}
		}
		return null;
	}
}
```

!!! note

    该注册表旨在支持在启动时便捷地注册各个组件，以静态方式添加租户；同时也支持在运行时动态新增租户。

## 创建多租户组件

需要具备多租户能力的组件包括：

- RegisteredClientRepository
- OAuth2AuthorizationService
- OAuth2AuthorizationConsentService
- JWKSource<SecurityContext>

对于这些组件中的每一个，都可以提供一个组合（Composite）实现，将调用委托给与“请求的”发行方标识符相对应的具体组件。

让我们一步步演示一个场景：如何定制 Spring Authorization Server，使每个支持多租户的组件都能够支持两个租户。

### 多租户RegisteredClientRepository

下面的示例展示了一个 `RegisteredClientRepository` 的参考实现：它由两个 `JdbcRegisteredClientRepository`
实例组成，并且每个实例都映射到一个对应的发行者（issuer）标识符：

``` java
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.util.Assert;

@Configuration(proxyBeanMethods = false)
public class RegisteredClientRepositoryConfig {

	@Bean
	public RegisteredClientRepository registeredClientRepository(
			@Qualifier("issuer1-data-source") DataSource issuer1DataSource,
			@Qualifier("issuer2-data-source") DataSource issuer2DataSource,
			TenantPerIssuerComponentRegistry componentRegistry) {

		//一个 JdbcRegisteredClientRepository 实例，映射到签发者标识符 issuer1，并使用专用的 DataSource。
		JdbcRegisteredClientRepository issuer1RegisteredClientRepository =
				new JdbcRegisteredClientRepository(new JdbcTemplate(issuer1DataSource));

		issuer1RegisteredClientRepository.save(
				RegisteredClient.withId(UUID.randomUUID().toString())
						.clientId("client-1")
						.clientSecret("{noop}secret")
						.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
						.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
						.scope("scope-1")
						.build()
		);

		//一个 JdbcRegisteredClientRepository 实例，映射到签发者标识符 issuer2，并使用专用的 DataSource。
		JdbcRegisteredClientRepository issuer2RegisteredClientRepository =
				new JdbcRegisteredClientRepository(new JdbcTemplate(issuer2DataSource));

		issuer2RegisteredClientRepository.save(
				RegisteredClient.withId(UUID.randomUUID().toString())
						.clientId("client-2")
						.clientSecret("{noop}secret")
						.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
						.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
						.scope("scope-2")
						.build()
		);

		componentRegistry.register("issuer1", RegisteredClientRepository.class, issuer1RegisteredClientRepository);
		componentRegistry.register("issuer2", RegisteredClientRepository.class, issuer2RegisteredClientRepository);

		return new DelegatingRegisteredClientRepository(componentRegistry);
	}

	//RegisteredClientRepository 的一个组合实现，会将请求委派给与“请求的”签发者标识符相映射的 JdbcRegisteredClientRepository。
	private static class DelegatingRegisteredClientRepository implements RegisteredClientRepository {

		private final TenantPerIssuerComponentRegistry componentRegistry;

		private DelegatingRegisteredClientRepository(TenantPerIssuerComponentRegistry componentRegistry) {
			this.componentRegistry = componentRegistry;
		}

		@Override
		public void save(RegisteredClient registeredClient) {
			getRegisteredClientRepository().save(registeredClient);
		}

		@Override
		public RegisteredClient findById(String id) {
			return getRegisteredClientRepository().findById(id);
		}

		@Override
		public RegisteredClient findByClientId(String clientId) {
			return getRegisteredClientRepository().findByClientId(clientId);
		}

		private RegisteredClientRepository getRegisteredClientRepository() {
			//获取与 AuthorizationServerContext.getIssuer() 指示的“请求的”签发者标识符相映射的 JdbcRegisteredClientRepository。
			RegisteredClientRepository registeredClientRepository =
					this.componentRegistry.get(RegisteredClientRepository.class);
			//如果找不到对应的 JdbcRegisteredClientRepository，则报错，因为该“请求的”签发者标识符不在已批准签发者的允许列表中。
			Assert.state(registeredClientRepository != null,
					"RegisteredClientRepository not found for \"requested\" issuer identifier.");
			return registeredClientRepository;
		}

	}

}
```

!!! warning

    通过 `AuthorizationServerSettings.builder().issuer("http://localhost:9000")` 显式配置颁发者（Issuer）标识符，会强制启用单租户配置。在使用多租户托管配置时，应避免显式配置该 Issuer 标识符。

在前面的示例中，每个 `JdbcRegisteredClientRepository` 实例都配置了一个 `JdbcTemplate` 以及与之关联的 `DataSource`
。这在多租户配置中至关重要，因为首要需求之一就是能够将各个租户的数据相互隔离。

为每个组件实例配置专用的 `DataSource`，可以灵活地将数据隔离在同一个数据库实例中的独立 Schema 里，或者更进一步，将数据完全隔离到不同的数据库实例中。

下面的示例展示了一个包含 2 个 `DataSource` 的 `@Bean` 示例配置（每个租户一个），这些 `DataSource` 将被支持多租户的组件使用：

``` java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

@Configuration(proxyBeanMethods = false)
public class DataSourceConfig {

	@Bean("issuer1-data-source")
	public EmbeddedDatabase issuer1DataSource() {
		return new EmbeddedDatabaseBuilder()
				.setName("issuer1-db") // 使用单独的 H2 数据库实例，名称为 issuer1-db。
				.setType(EmbeddedDatabaseType.H2)
				.setScriptEncoding("UTF-8")
				.addScript("org/springframework/security/oauth2/server/authorization/oauth2-authorization-schema.sql")
				.addScript("org/springframework/security/oauth2/server/authorization/oauth2-authorization-consent-schema.sql")
				.addScript("org/springframework/security/oauth2/server/authorization/client/oauth2-registered-client-schema.sql")
				.build();
	}

	@Bean("issuer2-data-source")
	public EmbeddedDatabase issuer2DataSource() {
		return new EmbeddedDatabaseBuilder()
				.setName("issuer2-db") // 使用单独的 H2 数据库实例，名称为 issuer2-db。
				.setType(EmbeddedDatabaseType.H2)
				.setScriptEncoding("UTF-8")
				.addScript("org/springframework/security/oauth2/server/authorization/oauth2-authorization-schema.sql")
				.addScript("org/springframework/security/oauth2/server/authorization/oauth2-authorization-consent-schema.sql")
				.addScript("org/springframework/security/oauth2/server/authorization/client/oauth2-registered-client-schema.sql")
				.build();
	}

}
```

### 多租户OAuth2AuthorizationService

下面的示例展示了一个 OAuth2AuthorizationService 的参考实现：它由两个 JdbcOAuth2AuthorizationService 实例组成，并将每个实例分别映射到一个
issuer 标识符。

``` java
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.util.Assert;

@Configuration(proxyBeanMethods = false)
public class OAuth2AuthorizationServiceConfig {

	@Bean
	public OAuth2AuthorizationService authorizationService(
			@Qualifier("issuer1-data-source") DataSource issuer1DataSource,
			@Qualifier("issuer2-data-source") DataSource issuer2DataSource,
			TenantPerIssuerComponentRegistry componentRegistry,
			RegisteredClientRepository registeredClientRepository) {

		componentRegistry.register("issuer1", OAuth2AuthorizationService.class,
				//一个 JdbcOAuth2AuthorizationService 实例，映射到发行者标识符 issuer1，并使用专用的 DataSource。
				new JdbcOAuth2AuthorizationService(
						new JdbcTemplate(issuer1DataSource), registeredClientRepository));
		componentRegistry.register("issuer2", OAuth2AuthorizationService.class,
				//一个 JdbcOAuth2AuthorizationService 实例，映射到发行者标识符 issuer2，并使用专用的 DataSource。
				new JdbcOAuth2AuthorizationService(
						new JdbcTemplate(issuer2DataSource), registeredClientRepository));

		return new DelegatingOAuth2AuthorizationService(componentRegistry);
	}

	//一个 OAuth2AuthorizationService 的组合实现，会将请求委派给与“请求的”发行者标识符相映射的 JdbcOAuth2AuthorizationService。
	private static class DelegatingOAuth2AuthorizationService implements OAuth2AuthorizationService {

		private final TenantPerIssuerComponentRegistry componentRegistry;

		private DelegatingOAuth2AuthorizationService(TenantPerIssuerComponentRegistry componentRegistry) {
			this.componentRegistry = componentRegistry;
		}

		@Override
		public void save(OAuth2Authorization authorization) {
			getAuthorizationService().save(authorization);
		}

		@Override
		public void remove(OAuth2Authorization authorization) {
			getAuthorizationService().remove(authorization);
		}

		@Override
		public OAuth2Authorization findById(String id) {
			return getAuthorizationService().findById(id);
		}

		@Override
		public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
			return getAuthorizationService().findByToken(token, tokenType);
		}

		private OAuth2AuthorizationService getAuthorizationService() {
			//通过 AuthorizationServerContext.getIssuer() 获取其指示的“请求的”发行者标识符，并据此取得对应映射的 JdbcOAuth2AuthorizationService。
			OAuth2AuthorizationService authorizationService =
					this.componentRegistry.get(OAuth2AuthorizationService.class);
			//如果找不到对应的 JdbcOAuth2AuthorizationService，则报错，因为该“请求的”发行者标识符不在已批准发行者的白名单中。
			Assert.state(authorizationService != null,
					"OAuth2AuthorizationService not found for \"requested\" issuer identifier.");
			return authorizationService;
		}

	}

}
```

### 多租户OAuth2AuthorizationConsentService

下面的示例展示了一个 OAuth2AuthorizationConsentService 的参考实现：它由两个 JdbcOAuth2AuthorizationConsentService
实例组成，并将每个实例分别映射到对应的 issuer 标识符：

``` java
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.util.Assert;

@Configuration(proxyBeanMethods = false)
public class OAuth2AuthorizationConsentServiceConfig {

	@Bean
	public OAuth2AuthorizationConsentService authorizationConsentService(
			@Qualifier("issuer1-data-source") DataSource issuer1DataSource,
			@Qualifier("issuer2-data-source") DataSource issuer2DataSource,
			TenantPerIssuerComponentRegistry componentRegistry,
			RegisteredClientRepository registeredClientRepository) {

		
		componentRegistry.register("issuer1", OAuth2AuthorizationConsentService.class,
				//一个 JdbcOAuth2AuthorizationConsentService 实例，映射到发行者标识符 issuer1，并使用专用的 DataSource。
				new JdbcOAuth2AuthorizationConsentService(
						new JdbcTemplate(issuer1DataSource), registeredClientRepository));
		componentRegistry.register("issuer2", OAuth2AuthorizationConsentService.class,
				// 一个 JdbcOAuth2AuthorizationConsentService 实例，映射到发行者标识符 issuer2，并使用专用的 DataSource。
				new JdbcOAuth2AuthorizationConsentService(
						new JdbcTemplate(issuer2DataSource), registeredClientRepository));

		return new DelegatingOAuth2AuthorizationConsentService(componentRegistry);
	}

	//一个 OAuth2AuthorizationConsentService 的组合实现，会将请求委派给与“当前请求”发行者标识符相映射的 JdbcOAuth2AuthorizationConsentService。
	private static class DelegatingOAuth2AuthorizationConsentService implements OAuth2AuthorizationConsentService {

		private final TenantPerIssuerComponentRegistry componentRegistry;

		private DelegatingOAuth2AuthorizationConsentService(TenantPerIssuerComponentRegistry componentRegistry) {
			this.componentRegistry = componentRegistry;
		}

		@Override
		public void save(OAuth2AuthorizationConsent authorizationConsent) {
			getAuthorizationConsentService().save(authorizationConsent);
		}

		@Override
		public void remove(OAuth2AuthorizationConsent authorizationConsent) {
			getAuthorizationConsentService().remove(authorizationConsent);
		}

		@Override
		public OAuth2AuthorizationConsent findById(String registeredClientId, String principalName) {
			return getAuthorizationConsentService().findById(registeredClientId, principalName);
		}

		private OAuth2AuthorizationConsentService getAuthorizationConsentService() {
			//根据 AuthorizationServerContext.getIssuer() 指示的“当前请求”发行者标识符，获取对应映射的 JdbcOAuth2AuthorizationConsentService。
			OAuth2AuthorizationConsentService authorizationConsentService =
					this.componentRegistry.get(OAuth2AuthorizationConsentService.class);
			//如果找不到对应的 JdbcOAuth2AuthorizationConsentService，则报错，因为“当前请求”的发行者标识符不在已批准发行者的允许列表（allowlist）中。
			Assert.state(authorizationConsentService != null,
					"OAuth2AuthorizationConsentService not found for \"requested\" issuer identifier.");
			return authorizationConsentService;
		}

	}

}
```

### 多租户JWKSource

最后，下面的示例展示了一个由两个 JWKSet 实例组合而成的 JWKSource<SecurityContext> 示例实现，其中每个实例都映射到一个发行方标识符。

``` java
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.UUID;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

@Configuration(proxyBeanMethods = false)
public class JWKSourceConfig {

	@Bean
	public JWKSource<SecurityContext> jwkSource(TenantPerIssuerComponentRegistry componentRegistry) {
		//一个 JWKSet 实例，映射到签发者标识符 issuer1。
		componentRegistry.register("issuer1", JWKSet.class, new JWKSet(generateRSAJwk()));
		//一个 JWKSet 实例，映射到签发者标识符 issuer2。
		componentRegistry.register("issuer2", JWKSet.class, new JWKSet(generateRSAJwk()));

		return new DelegatingJWKSource(componentRegistry);
	}

	private static RSAKey generateRSAJwk() {
		KeyPair keyPair;
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(2048);
			keyPair = keyPairGenerator.generateKeyPair();
		} catch (Exception ex) {
			throw new IllegalStateException(ex);
		}

		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		return new RSAKey.Builder(publicKey)
				.privateKey(privateKey)
				.keyID(UUID.randomUUID().toString())
				.build();
	}

	//一个 JWKSource<SecurityContext> 的组合实现，它会使用与“请求的”签发者标识符相映射的 JWKSet。
	private static class DelegatingJWKSource implements JWKSource<SecurityContext> {

		private final TenantPerIssuerComponentRegistry componentRegistry;

		private DelegatingJWKSource(TenantPerIssuerComponentRegistry componentRegistry) {
			this.componentRegistry = componentRegistry;
		}

		@Override
		public List<JWK> get(JWKSelector jwkSelector, SecurityContext context) throws KeySourceException {
			return jwkSelector.select(getJwkSet());
		}

		private JWKSet getJwkSet() {
			//获取与“请求的”签发者标识符相映射的 JWKSet，该标识符由 AuthorizationServerContext.getIssuer() 指示。
			JWKSet jwkSet = this.componentRegistry.get(JWKSet.class);
			//如果找不到对应的 JWKSet，则报错，因为“请求的”签发者标识符不在已批准签发者的允许列表中。
			Assert.state(jwkSet != null, "JWKSet not found for \"requested\" issuer identifier.");
			return jwkSet;
		}

	}

}
```

## 动态添加租户

如果租户数量是动态的，并且会在运行时发生变化，那么将每个 DataSource 都定义为一个 @Bean
可能并不可行。在这种情况下，可以在应用启动时和/或运行期间通过其他方式注册 DataSource 及其对应的组件。

下面的示例展示了一个能够动态添加租户的 Spring @Service：

```` java
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;

@Service
public class TenantService {

	private final TenantPerIssuerComponentRegistry componentRegistry;

	public TenantService(TenantPerIssuerComponentRegistry componentRegistry) {
		this.componentRegistry = componentRegistry;
	}

	public void createTenant(String tenantId) {
		EmbeddedDatabase dataSource = createDataSource(tenantId);
		JdbcTemplate jdbcOperations = new JdbcTemplate(dataSource);

		RegisteredClientRepository registeredClientRepository =
				new JdbcRegisteredClientRepository(jdbcOperations);
		this.componentRegistry.register(tenantId, RegisteredClientRepository.class, registeredClientRepository);

		OAuth2AuthorizationService authorizationService =
				new JdbcOAuth2AuthorizationService(jdbcOperations, registeredClientRepository);
		this.componentRegistry.register(tenantId, OAuth2AuthorizationService.class, authorizationService);

		OAuth2AuthorizationConsentService authorizationConsentService =
				new JdbcOAuth2AuthorizationConsentService(jdbcOperations, registeredClientRepository);
		this.componentRegistry.register(tenantId, OAuth2AuthorizationConsentService.class, authorizationConsentService);

		JWKSet jwkSet = new JWKSet(generateRSAJwk());
		this.componentRegistry.register(tenantId, JWKSet.class, jwkSet);
	}

	private EmbeddedDatabase createDataSource(String tenantId) {
		return new EmbeddedDatabaseBuilder()
				.setName(tenantId)
				.setType(EmbeddedDatabaseType.H2)
				.setScriptEncoding("UTF-8")
				.addScript("org/springframework/security/oauth2/server/authorization/oauth2-authorization-schema.sql")
				.addScript("org/springframework/security/oauth2/server/authorization/oauth2-authorization-consent-schema.sql")
				.addScript("org/springframework/security/oauth2/server/authorization/client/oauth2-registered-client-schema.sql")
				.build();
	}

	private static RSAKey generateRSAJwk() {
		KeyPair keyPair;
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(2048);
			keyPair = keyPairGenerator.generateKeyPair();
		} catch (Exception ex) {
			throw new IllegalStateException(ex);
		}

		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		return new RSAKey.Builder(publicKey)
				.privateKey(privateKey)
				.keyID(UUID.randomUUID().toString())
				.build();
	}

}
````

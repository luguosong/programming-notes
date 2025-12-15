# 通过JPA实现核心服务

本指南演示如何使用 JPA 实现 Spring Authorization Server 的核心服务。本指南旨在为你自行实现这些服务提供一个起点，并希望你能根据自身需求进行相应调整与扩展。

## 定义数据模型

本指南为数据模型提供了一个起点，采用尽可能简单的结构与数据类型。为了设计初始 Schema，我们先从梳理核心服务所使用的领域对象入手。

!!! note

    除 token、state、metadata、settings 和 claims 这些字段的值之外，其余所有列我们都采用 JPA 默认的 255 字符列长度。实际使用中，你的字段长度乃至列类型往往都需要按需定制。建议在上线生产环境前充分试验并完成测试。

### 客户端 Schema

RegisteredClient 领域对象包含一些多值字段，以及若干需要存储任意键/值数据的配置字段。下面的清单展示了客户端 Schema。

``` sql
CREATE TABLE client (
    id varchar(255) NOT NULL,
    clientId varchar(255) NOT NULL,
    clientIdIssuedAt timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    clientSecret varchar(255) DEFAULT NULL,
    clientSecretExpiresAt timestamp DEFAULT NULL,
    clientName varchar(255) NOT NULL,
    clientAuthenticationMethods varchar(1000) NOT NULL,
    authorizationGrantTypes varchar(1000) NOT NULL,
    redirectUris varchar(1000) DEFAULT NULL,
    postLogoutRedirectUris varchar(1000) DEFAULT NULL,
    scopes varchar(1000) NOT NULL,
    clientSettings varchar(2000) NOT NULL,
    tokenSettings varchar(2000) NOT NULL,
    PRIMARY KEY (id)
);
```

### 授权方案

OAuth2Authorization 领域对象更为复杂，包含多个多值字段，以及大量长度不定的令牌值、元数据、配置项和声明值。内置的 JDBC
实现采用了扁平化结构，相比规范化更侧重性能，我们在这里也同样沿用这种做法。

!!! note

    一直很难找到一套在所有场景、并且适用于所有数据库厂商的扁平化数据库 Schema。你可能需要根据实际需求对下面的 Schema 进行规范化处理，或做较大幅度的调整。

下面的清单展示了授权架构。

``` sql
CREATE TABLE authorization (
    id varchar(255) NOT NULL,
    registeredClientId varchar(255) NOT NULL,
    principalName varchar(255) NOT NULL,
    authorizationGrantType varchar(255) NOT NULL,
    authorizedScopes varchar(1000) DEFAULT NULL,
    attributes varchar(4000) DEFAULT NULL,
    state varchar(500) DEFAULT NULL,
    authorizationCodeValue varchar(4000) DEFAULT NULL,
    authorizationCodeIssuedAt timestamp DEFAULT NULL,
    authorizationCodeExpiresAt timestamp DEFAULT NULL,
    authorizationCodeMetadata varchar(2000) DEFAULT NULL,
    accessTokenValue varchar(4000) DEFAULT NULL,
    accessTokenIssuedAt timestamp DEFAULT NULL,
    accessTokenExpiresAt timestamp DEFAULT NULL,
    accessTokenMetadata varchar(2000) DEFAULT NULL,
    accessTokenType varchar(255) DEFAULT NULL,
    accessTokenScopes varchar(1000) DEFAULT NULL,
    refreshTokenValue varchar(4000) DEFAULT NULL,
    refreshTokenIssuedAt timestamp DEFAULT NULL,
    refreshTokenExpiresAt timestamp DEFAULT NULL,
    refreshTokenMetadata varchar(2000) DEFAULT NULL,
    oidcIdTokenValue varchar(4000) DEFAULT NULL,
    oidcIdTokenIssuedAt timestamp DEFAULT NULL,
    oidcIdTokenExpiresAt timestamp DEFAULT NULL,
    oidcIdTokenMetadata varchar(2000) DEFAULT NULL,
    oidcIdTokenClaims varchar(2000) DEFAULT NULL,
    userCodeValue varchar(4000) DEFAULT NULL,
    userCodeIssuedAt timestamp DEFAULT NULL,
    userCodeExpiresAt timestamp DEFAULT NULL,
    userCodeMetadata varchar(2000) DEFAULT NULL,
    deviceCodeValue varchar(4000) DEFAULT NULL,
    deviceCodeIssuedAt timestamp DEFAULT NULL,
    deviceCodeExpiresAt timestamp DEFAULT NULL,
    deviceCodeMetadata varchar(2000) DEFAULT NULL,
    PRIMARY KEY (id)
);
```

### 授权同意模式架构

OAuth2AuthorizationConsent 领域对象最容易建模，除了一个复合主键外，仅包含一个多值字段。下面的代码清单展示了
authorizationconsent 的 schema。

``` sql
CREATE TABLE authorizationConsent (
    registeredClientId varchar(255) NOT NULL,
    principalName varchar(255) NOT NULL,
    authorities varchar(1000) NOT NULL,
    PRIMARY KEY (registeredClientId, principalName)
);
```

## 创建JPA实体类

前面的架构示例为我们需要创建的实体结构提供了参考。

!!! note

    以下实体仅做了最基本的注解，只是示例用途。它们支持动态创建数据库 Schema，因此无需手动执行上述 SQL 脚本。

### 客户端实体

下面的代码清单展示了 **Client** 实体，用于持久化从 **RegisteredClient** 领域对象映射而来的信息。

``` java
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "`client`")
public class Client {
	@Id
	private String id;
	private String clientId;
	private Instant clientIdIssuedAt;
	private String clientSecret;
	private Instant clientSecretExpiresAt;
	private String clientName;
	@Column(length = 1000)
	private String clientAuthenticationMethods;
	@Column(length = 1000)
	private String authorizationGrantTypes;
	@Column(length = 1000)
	private String redirectUris;
	@Column(length = 1000)
	private String postLogoutRedirectUris;
	@Column(length = 1000)
	private String scopes;
	@Column(length = 2000)
	private String clientSettings;
	@Column(length = 2000)
	private String tokenSettings;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public Instant getClientIdIssuedAt() {
		return clientIdIssuedAt;
	}

	public void setClientIdIssuedAt(Instant clientIdIssuedAt) {
		this.clientIdIssuedAt = clientIdIssuedAt;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public Instant getClientSecretExpiresAt() {
		return clientSecretExpiresAt;
	}

	public void setClientSecretExpiresAt(Instant clientSecretExpiresAt) {
		this.clientSecretExpiresAt = clientSecretExpiresAt;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getClientAuthenticationMethods() {
		return clientAuthenticationMethods;
	}

	public void setClientAuthenticationMethods(String clientAuthenticationMethods) {
		this.clientAuthenticationMethods = clientAuthenticationMethods;
	}

	public String getAuthorizationGrantTypes() {
		return authorizationGrantTypes;
	}

	public void setAuthorizationGrantTypes(String authorizationGrantTypes) {
		this.authorizationGrantTypes = authorizationGrantTypes;
	}

	public String getRedirectUris() {
		return redirectUris;
	}

	public void setRedirectUris(String redirectUris) {
		this.redirectUris = redirectUris;
	}

	public String getPostLogoutRedirectUris() {
		return this.postLogoutRedirectUris;
	}

	public void setPostLogoutRedirectUris(String postLogoutRedirectUris) {
		this.postLogoutRedirectUris = postLogoutRedirectUris;
	}

	public String getScopes() {
		return scopes;
	}

	public void setScopes(String scopes) {
		this.scopes = scopes;
	}

	public String getClientSettings() {
		return clientSettings;
	}

	public void setClientSettings(String clientSettings) {
		this.clientSettings = clientSettings;
	}

	public String getTokenSettings() {
		return tokenSettings;
	}

	public void setTokenSettings(String tokenSettings) {
		this.tokenSettings = tokenSettings;
	}
}
```

### 授权实体

以下代码清单展示了 Authorization 实体，用于持久化从 OAuth2Authorization 领域对象映射而来的信息。

``` java
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "`authorization`")
public class Authorization {
	@Id
	@Column
	private String id;
	private String registeredClientId;
	private String principalName;
	private String authorizationGrantType;
	@Column(length = 1000)
	private String authorizedScopes;
	@Column(length = 4000)
	private String attributes;
	@Column(length = 500)
	private String state;

	@Column(length = 4000)
	private String authorizationCodeValue;
	private Instant authorizationCodeIssuedAt;
	private Instant authorizationCodeExpiresAt;
	private String authorizationCodeMetadata;

	@Column(length = 4000)
	private String accessTokenValue;
	private Instant accessTokenIssuedAt;
	private Instant accessTokenExpiresAt;
	@Column(length = 2000)
	private String accessTokenMetadata;
	private String accessTokenType;
	@Column(length = 1000)
	private String accessTokenScopes;

	@Column(length = 4000)
	private String refreshTokenValue;
	private Instant refreshTokenIssuedAt;
	private Instant refreshTokenExpiresAt;
	@Column(length = 2000)
	private String refreshTokenMetadata;

	@Column(length = 4000)
	private String oidcIdTokenValue;
	private Instant oidcIdTokenIssuedAt;
	private Instant oidcIdTokenExpiresAt;
	@Column(length = 2000)
	private String oidcIdTokenMetadata;
	@Column(length = 2000)
	private String oidcIdTokenClaims;

	@Column(length = 4000)
	private String userCodeValue;
	private Instant userCodeIssuedAt;
	private Instant userCodeExpiresAt;
	@Column(length = 2000)
	private String userCodeMetadata;

	@Column(length = 4000)
	private String deviceCodeValue;
	private Instant deviceCodeIssuedAt;
	private Instant deviceCodeExpiresAt;
	@Column(length = 2000)
	private String deviceCodeMetadata;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRegisteredClientId() {
		return registeredClientId;
	}

	public void setRegisteredClientId(String registeredClientId) {
		this.registeredClientId = registeredClientId;
	}

	public String getPrincipalName() {
		return principalName;
	}

	public void setPrincipalName(String principalName) {
		this.principalName = principalName;
	}

	public String getAuthorizationGrantType() {
		return authorizationGrantType;
	}

	public void setAuthorizationGrantType(String authorizationGrantType) {
		this.authorizationGrantType = authorizationGrantType;
	}

	public String getAuthorizedScopes() {
		return this.authorizedScopes;
	}

	public void setAuthorizedScopes(String authorizedScopes) {
		this.authorizedScopes = authorizedScopes;
	}

	public String getAttributes() {
		return attributes;
	}

	public void setAttributes(String attributes) {
		this.attributes = attributes;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getAuthorizationCodeValue() {
		return authorizationCodeValue;
	}

	public void setAuthorizationCodeValue(String authorizationCode) {
		this.authorizationCodeValue = authorizationCode;
	}

	public Instant getAuthorizationCodeIssuedAt() {
		return authorizationCodeIssuedAt;
	}

	public void setAuthorizationCodeIssuedAt(Instant authorizationCodeIssuedAt) {
		this.authorizationCodeIssuedAt = authorizationCodeIssuedAt;
	}

	public Instant getAuthorizationCodeExpiresAt() {
		return authorizationCodeExpiresAt;
	}

	public void setAuthorizationCodeExpiresAt(Instant authorizationCodeExpiresAt) {
		this.authorizationCodeExpiresAt = authorizationCodeExpiresAt;
	}

	public String getAuthorizationCodeMetadata() {
		return authorizationCodeMetadata;
	}

	public void setAuthorizationCodeMetadata(String authorizationCodeMetadata) {
		this.authorizationCodeMetadata = authorizationCodeMetadata;
	}

	public String getAccessTokenValue() {
		return accessTokenValue;
	}

	public void setAccessTokenValue(String accessToken) {
		this.accessTokenValue = accessToken;
	}

	public Instant getAccessTokenIssuedAt() {
		return accessTokenIssuedAt;
	}

	public void setAccessTokenIssuedAt(Instant accessTokenIssuedAt) {
		this.accessTokenIssuedAt = accessTokenIssuedAt;
	}

	public Instant getAccessTokenExpiresAt() {
		return accessTokenExpiresAt;
	}

	public void setAccessTokenExpiresAt(Instant accessTokenExpiresAt) {
		this.accessTokenExpiresAt = accessTokenExpiresAt;
	}

	public String getAccessTokenMetadata() {
		return accessTokenMetadata;
	}

	public void setAccessTokenMetadata(String accessTokenMetadata) {
		this.accessTokenMetadata = accessTokenMetadata;
	}

	public String getAccessTokenType() {
		return accessTokenType;
	}

	public void setAccessTokenType(String accessTokenType) {
		this.accessTokenType = accessTokenType;
	}

	public String getAccessTokenScopes() {
		return accessTokenScopes;
	}

	public void setAccessTokenScopes(String accessTokenScopes) {
		this.accessTokenScopes = accessTokenScopes;
	}

	public String getRefreshTokenValue() {
		return refreshTokenValue;
	}

	public void setRefreshTokenValue(String refreshToken) {
		this.refreshTokenValue = refreshToken;
	}

	public Instant getRefreshTokenIssuedAt() {
		return refreshTokenIssuedAt;
	}

	public void setRefreshTokenIssuedAt(Instant refreshTokenIssuedAt) {
		this.refreshTokenIssuedAt = refreshTokenIssuedAt;
	}

	public Instant getRefreshTokenExpiresAt() {
		return refreshTokenExpiresAt;
	}

	public void setRefreshTokenExpiresAt(Instant refreshTokenExpiresAt) {
		this.refreshTokenExpiresAt = refreshTokenExpiresAt;
	}

	public String getRefreshTokenMetadata() {
		return refreshTokenMetadata;
	}

	public void setRefreshTokenMetadata(String refreshTokenMetadata) {
		this.refreshTokenMetadata = refreshTokenMetadata;
	}

	public String getOidcIdTokenValue() {
		return oidcIdTokenValue;
	}

	public void setOidcIdTokenValue(String idToken) {
		this.oidcIdTokenValue = idToken;
	}

	public Instant getOidcIdTokenIssuedAt() {
		return oidcIdTokenIssuedAt;
	}

	public void setOidcIdTokenIssuedAt(Instant idTokenIssuedAt) {
		this.oidcIdTokenIssuedAt = idTokenIssuedAt;
	}

	public Instant getOidcIdTokenExpiresAt() {
		return oidcIdTokenExpiresAt;
	}

	public void setOidcIdTokenExpiresAt(Instant idTokenExpiresAt) {
		this.oidcIdTokenExpiresAt = idTokenExpiresAt;
	}

	public String getOidcIdTokenMetadata() {
		return oidcIdTokenMetadata;
	}

	public void setOidcIdTokenMetadata(String idTokenMetadata) {
		this.oidcIdTokenMetadata = idTokenMetadata;
	}

	public String getOidcIdTokenClaims() {
		return oidcIdTokenClaims;
	}

	public void setOidcIdTokenClaims(String idTokenClaims) {
		this.oidcIdTokenClaims = idTokenClaims;
	}

	public String getUserCodeValue() {
		return this.userCodeValue;
	}

	public void setUserCodeValue(String userCodeValue) {
		this.userCodeValue = userCodeValue;
	}

	public Instant getUserCodeIssuedAt() {
		return this.userCodeIssuedAt;
	}

	public void setUserCodeIssuedAt(Instant userCodeIssuedAt) {
		this.userCodeIssuedAt = userCodeIssuedAt;
	}

	public Instant getUserCodeExpiresAt() {
		return this.userCodeExpiresAt;
	}

	public void setUserCodeExpiresAt(Instant userCodeExpiresAt) {
		this.userCodeExpiresAt = userCodeExpiresAt;
	}

	public String getUserCodeMetadata() {
		return this.userCodeMetadata;
	}

	public void setUserCodeMetadata(String userCodeMetadata) {
		this.userCodeMetadata = userCodeMetadata;
	}

	public String getDeviceCodeValue() {
		return this.deviceCodeValue;
	}

	public void setDeviceCodeValue(String deviceCodeValue) {
		this.deviceCodeValue = deviceCodeValue;
	}

	public Instant getDeviceCodeIssuedAt() {
		return this.deviceCodeIssuedAt;
	}

	public void setDeviceCodeIssuedAt(Instant deviceCodeIssuedAt) {
		this.deviceCodeIssuedAt = deviceCodeIssuedAt;
	}

	public Instant getDeviceCodeExpiresAt() {
		return this.deviceCodeExpiresAt;
	}

	public void setDeviceCodeExpiresAt(Instant deviceCodeExpiresAt) {
		this.deviceCodeExpiresAt = deviceCodeExpiresAt;
	}

	public String getDeviceCodeMetadata() {
		return this.deviceCodeMetadata;
	}

	public void setDeviceCodeMetadata(String deviceCodeMetadata) {
		this.deviceCodeMetadata = deviceCodeMetadata;
	}
}
```

### 授权同意实体

以下代码清单展示了 AuthorizationConsent 实体，用于持久化从 OAuth2AuthorizationConsent 领域对象映射而来的信息。

``` java
import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "`authorizationConsent`")
@IdClass(AuthorizationConsent.AuthorizationConsentId.class)
public class AuthorizationConsent {
	@Id
	private String registeredClientId;
	@Id
	private String principalName;
	@Column(length = 1000)
	private String authorities;

	public String getRegisteredClientId() {
		return registeredClientId;
	}

	public void setRegisteredClientId(String registeredClientId) {
		this.registeredClientId = registeredClientId;
	}

	public String getPrincipalName() {
		return principalName;
	}

	public void setPrincipalName(String principalName) {
		this.principalName = principalName;
	}

	public String getAuthorities() {
		return authorities;
	}

	public void setAuthorities(String authorities) {
		this.authorities = authorities;
	}

	public static class AuthorizationConsentId implements Serializable {
		private String registeredClientId;
		private String principalName;

		public String getRegisteredClientId() {
			return registeredClientId;
		}

		public void setRegisteredClientId(String registeredClientId) {
			this.registeredClientId = registeredClientId;
		}

		public String getPrincipalName() {
			return principalName;
		}

		public void setPrincipalName(String principalName) {
			this.principalName = principalName;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			AuthorizationConsentId that = (AuthorizationConsentId) o;
			return registeredClientId.equals(that.registeredClientId) && principalName.equals(that.principalName);
		}

		@Override
		public int hashCode() {
			return Objects.hash(registeredClientId, principalName);
		}
	}
}
```

## 创建Spring Data仓库

通过仔细梳理各个核心服务的接口，并审阅其 JDBC 实现，我们可以归纳出一组最小化的查询集合，用以支撑每个接口的 JPA 版本实现。

### 客户端仓库

下面的代码清单展示了 `ClientRepository`，它能够通过 `id` 和 `clientId` 字段查找 `Client`。

```java
import java.util.Optional;

import sample.jpa.entity.client.Client;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, String> {
	Optional<Client> findByClientId(String clientId);
}
```

### 授权仓库

下面的代码清单展示了 AuthorizationRepository。它不仅可以通过 id 字段查找 Authorization，也支持按
state、authorizationCodeValue、accessTokenValue、refreshTokenValue、userCodeValue 和 deviceCodeValue
等令牌字段进行查询，并且还允许按多个令牌字段组合查询。

```java
import java.util.Optional;

import sample.jpa.entity.authorization.Authorization;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorizationRepository extends JpaRepository<Authorization, String> {
	Optional<Authorization> findByState(String state);

	Optional<Authorization> findByAuthorizationCodeValue(String authorizationCode);

	Optional<Authorization> findByAccessTokenValue(String accessToken);

	Optional<Authorization> findByRefreshTokenValue(String refreshToken);

	Optional<Authorization> findByOidcIdTokenValue(String idToken);

	Optional<Authorization> findByUserCodeValue(String userCode);

	Optional<Authorization> findByDeviceCodeValue(String deviceCode);

	@Query("select a from Authorization a where a.state = :token" +
			" or a.authorizationCodeValue = :token" +
			" or a.accessTokenValue = :token" +
			" or a.refreshTokenValue = :token" +
			" or a.oidcIdTokenValue = :token" +
			" or a.userCodeValue = :token" +
			" or a.deviceCodeValue = :token"
	)
	Optional<Authorization> findByStateOrAuthorizationCodeValueOrAccessTokenValueOrRefreshTokenValueOrOidcIdTokenValueOrUserCodeValueOrDeviceCodeValue(@Param("token") String token);
}
```

### 授权同意存储库

下面的代码清单展示了 AuthorizationConsentRepository，它可以通过组成复合主键的 registeredClientId 和 principalName 字段查找并删除
AuthorizationConsent。

```java
import java.util.Optional;

import sample.jpa.entity.authorizationconsent.AuthorizationConsent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorizationConsentRepository extends JpaRepository<AuthorizationConsent, AuthorizationConsent.AuthorizationConsentId> {
	Optional<AuthorizationConsent> findByRegisteredClientIdAndPrincipalName(String registeredClientId, String principalName);

	void deleteByRegisteredClientIdAndPrincipalName(String registeredClientId, String principalName);
}
```

## 实现核心服务

有了上述实体和仓储之后，我们就可以开始实现核心服务了。通过审阅这些 JDBC 实现，我们可以梳理出一套最小化的内部工具：用于在枚举与字符串值之间相互转换，以及为
attributes、settings、metadata 和 claims 等字段读写 JSON 数据。

!!! note

    请注意，使用 JDBC 的实现将 JSON 数据写入固定长度的文本列时，已被证明会带来问题。虽然这些示例仍然沿用这种做法，但你可能需要将这些字段拆分到单独的表中，或迁移到支持任意长度数据值的数据存储中。

### 已注册客户端仓库

下面的代码清单展示了 `JpaRegisteredClientRepository`。它使用 `ClientRepository` 来持久化 `Client`，并负责在
`RegisteredClient` 领域对象与其对应的数据模型之间进行映射转换。

``` java
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import sample.jpa.entity.client.Client;
import sample.jpa.repository.client.ClientRepository;

import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Component
public class JpaRegisteredClientRepository implements RegisteredClientRepository {
	private final ClientRepository clientRepository;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public JpaRegisteredClientRepository(ClientRepository clientRepository) {
		Assert.notNull(clientRepository, "clientRepository cannot be null");
		this.clientRepository = clientRepository;

		ClassLoader classLoader = JpaRegisteredClientRepository.class.getClassLoader();
		List<Module> securityModules = SecurityJackson2Modules.getModules(classLoader);
		this.objectMapper.registerModules(securityModules);
		this.objectMapper.registerModule(new OAuth2AuthorizationServerJackson2Module());
	}

	@Override
	public void save(RegisteredClient registeredClient) {
		Assert.notNull(registeredClient, "registeredClient cannot be null");
		this.clientRepository.save(toEntity(registeredClient));
	}

	@Override
	public RegisteredClient findById(String id) {
		Assert.hasText(id, "id cannot be empty");
		return this.clientRepository.findById(id).map(this::toObject).orElse(null);
	}

	@Override
	public RegisteredClient findByClientId(String clientId) {
		Assert.hasText(clientId, "clientId cannot be empty");
		return this.clientRepository.findByClientId(clientId).map(this::toObject).orElse(null);
	}

	private RegisteredClient toObject(Client client) {
		Set<String> clientAuthenticationMethods = StringUtils.commaDelimitedListToSet(
				client.getClientAuthenticationMethods());
		Set<String> authorizationGrantTypes = StringUtils.commaDelimitedListToSet(
				client.getAuthorizationGrantTypes());
		Set<String> redirectUris = StringUtils.commaDelimitedListToSet(
				client.getRedirectUris());
		Set<String> postLogoutRedirectUris = StringUtils.commaDelimitedListToSet(
				client.getPostLogoutRedirectUris());
		Set<String> clientScopes = StringUtils.commaDelimitedListToSet(
				client.getScopes());

		RegisteredClient.Builder builder = RegisteredClient.withId(client.getId())
				.clientId(client.getClientId())
				.clientIdIssuedAt(client.getClientIdIssuedAt())
				.clientSecret(client.getClientSecret())
				.clientSecretExpiresAt(client.getClientSecretExpiresAt())
				.clientName(client.getClientName())
				.clientAuthenticationMethods(authenticationMethods ->
						clientAuthenticationMethods.forEach(authenticationMethod ->
								authenticationMethods.add(resolveClientAuthenticationMethod(authenticationMethod))))
				.authorizationGrantTypes((grantTypes) ->
						authorizationGrantTypes.forEach(grantType ->
								grantTypes.add(resolveAuthorizationGrantType(grantType))))
				.redirectUris((uris) -> uris.addAll(redirectUris))
				.postLogoutRedirectUris((uris) -> uris.addAll(postLogoutRedirectUris))
				.scopes((scopes) -> scopes.addAll(clientScopes));

		Map<String, Object> clientSettingsMap = parseMap(client.getClientSettings());
		builder.clientSettings(ClientSettings.withSettings(clientSettingsMap).build());

		Map<String, Object> tokenSettingsMap = parseMap(client.getTokenSettings());
		builder.tokenSettings(TokenSettings.withSettings(tokenSettingsMap).build());

		return builder.build();
	}

	private Client toEntity(RegisteredClient registeredClient) {
		List<String> clientAuthenticationMethods = new ArrayList<>(registeredClient.getClientAuthenticationMethods().size());
		registeredClient.getClientAuthenticationMethods().forEach(clientAuthenticationMethod ->
				clientAuthenticationMethods.add(clientAuthenticationMethod.getValue()));

		List<String> authorizationGrantTypes = new ArrayList<>(registeredClient.getAuthorizationGrantTypes().size());
		registeredClient.getAuthorizationGrantTypes().forEach(authorizationGrantType ->
				authorizationGrantTypes.add(authorizationGrantType.getValue()));

		Client entity = new Client();
		entity.setId(registeredClient.getId());
		entity.setClientId(registeredClient.getClientId());
		entity.setClientIdIssuedAt(registeredClient.getClientIdIssuedAt());
		entity.setClientSecret(registeredClient.getClientSecret());
		entity.setClientSecretExpiresAt(registeredClient.getClientSecretExpiresAt());
		entity.setClientName(registeredClient.getClientName());
		entity.setClientAuthenticationMethods(StringUtils.collectionToCommaDelimitedString(clientAuthenticationMethods));
		entity.setAuthorizationGrantTypes(StringUtils.collectionToCommaDelimitedString(authorizationGrantTypes));
		entity.setRedirectUris(StringUtils.collectionToCommaDelimitedString(registeredClient.getRedirectUris()));
		entity.setPostLogoutRedirectUris(StringUtils.collectionToCommaDelimitedString(registeredClient.getPostLogoutRedirectUris()));
		entity.setScopes(StringUtils.collectionToCommaDelimitedString(registeredClient.getScopes()));
		entity.setClientSettings(writeMap(registeredClient.getClientSettings().getSettings()));
		entity.setTokenSettings(writeMap(registeredClient.getTokenSettings().getSettings()));

		return entity;
	}

	private Map<String, Object> parseMap(String data) {
		try {
			return this.objectMapper.readValue(data, new TypeReference<Map<String, Object>>() {
			});
		} catch (Exception ex) {
			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
	}

	private String writeMap(Map<String, Object> data) {
		try {
			return this.objectMapper.writeValueAsString(data);
		} catch (Exception ex) {
			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
	}

	private static AuthorizationGrantType resolveAuthorizationGrantType(String authorizationGrantType) {
		if (AuthorizationGrantType.AUTHORIZATION_CODE.getValue().equals(authorizationGrantType)) {
			return AuthorizationGrantType.AUTHORIZATION_CODE;
		} else if (AuthorizationGrantType.CLIENT_CREDENTIALS.getValue().equals(authorizationGrantType)) {
			return AuthorizationGrantType.CLIENT_CREDENTIALS;
		} else if (AuthorizationGrantType.REFRESH_TOKEN.getValue().equals(authorizationGrantType)) {
			return AuthorizationGrantType.REFRESH_TOKEN;
		}
		return new AuthorizationGrantType(authorizationGrantType);              // Custom authorization grant type
	}

	private static ClientAuthenticationMethod resolveClientAuthenticationMethod(String clientAuthenticationMethod) {
		if (ClientAuthenticationMethod.CLIENT_SECRET_BASIC.getValue().equals(clientAuthenticationMethod)) {
			return ClientAuthenticationMethod.CLIENT_SECRET_BASIC;
		} else if (ClientAuthenticationMethod.CLIENT_SECRET_POST.getValue().equals(clientAuthenticationMethod)) {
			return ClientAuthenticationMethod.CLIENT_SECRET_POST;
		} else if (ClientAuthenticationMethod.NONE.getValue().equals(clientAuthenticationMethod)) {
			return ClientAuthenticationMethod.NONE;
		}
		return new ClientAuthenticationMethod(clientAuthenticationMethod);      // Custom client authentication method
	}
}
```

### 授权服务

下面的代码清单展示了 `JpaOAuth2AuthorizationService`：它使用 `AuthorizationRepository` 来持久化 `Authorization`，并负责在
`OAuth2Authorization` 领域对象之间进行映射与转换。

``` java
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import sample.jpa.entity.authorization.Authorization;
import sample.jpa.repository.authorization.AuthorizationRepository;

import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2DeviceCode;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.OAuth2UserCode;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Component
public class JpaOAuth2AuthorizationService implements OAuth2AuthorizationService {
	private final AuthorizationRepository authorizationRepository;
	private final RegisteredClientRepository registeredClientRepository;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public JpaOAuth2AuthorizationService(AuthorizationRepository authorizationRepository, RegisteredClientRepository registeredClientRepository) {
		Assert.notNull(authorizationRepository, "authorizationRepository cannot be null");
		Assert.notNull(registeredClientRepository, "registeredClientRepository cannot be null");
		this.authorizationRepository = authorizationRepository;
		this.registeredClientRepository = registeredClientRepository;

		ClassLoader classLoader = JpaOAuth2AuthorizationService.class.getClassLoader();
		List<Module> securityModules = SecurityJackson2Modules.getModules(classLoader);
		this.objectMapper.registerModules(securityModules);
		this.objectMapper.registerModule(new OAuth2AuthorizationServerJackson2Module());
	}

	@Override
	public void save(OAuth2Authorization authorization) {
		Assert.notNull(authorization, "authorization cannot be null");
		this.authorizationRepository.save(toEntity(authorization));
	}

	@Override
	public void remove(OAuth2Authorization authorization) {
		Assert.notNull(authorization, "authorization cannot be null");
		this.authorizationRepository.deleteById(authorization.getId());
	}

	@Override
	public OAuth2Authorization findById(String id) {
		Assert.hasText(id, "id cannot be empty");
		return this.authorizationRepository.findById(id).map(this::toObject).orElse(null);
	}

	@Override
	public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
		Assert.hasText(token, "token cannot be empty");

		Optional<Authorization> result;
		if (tokenType == null) {
			result = this.authorizationRepository.findByStateOrAuthorizationCodeValueOrAccessTokenValueOrRefreshTokenValueOrOidcIdTokenValueOrUserCodeValueOrDeviceCodeValue(token);
		} else if (OAuth2ParameterNames.STATE.equals(tokenType.getValue())) {
			result = this.authorizationRepository.findByState(token);
		} else if (OAuth2ParameterNames.CODE.equals(tokenType.getValue())) {
			result = this.authorizationRepository.findByAuthorizationCodeValue(token);
		} else if (OAuth2ParameterNames.ACCESS_TOKEN.equals(tokenType.getValue())) {
			result = this.authorizationRepository.findByAccessTokenValue(token);
		} else if (OAuth2ParameterNames.REFRESH_TOKEN.equals(tokenType.getValue())) {
			result = this.authorizationRepository.findByRefreshTokenValue(token);
		} else if (OidcParameterNames.ID_TOKEN.equals(tokenType.getValue())) {
			result = this.authorizationRepository.findByOidcIdTokenValue(token);
		} else if (OAuth2ParameterNames.USER_CODE.equals(tokenType.getValue())) {
			result = this.authorizationRepository.findByUserCodeValue(token);
		} else if (OAuth2ParameterNames.DEVICE_CODE.equals(tokenType.getValue())) {
			result = this.authorizationRepository.findByDeviceCodeValue(token);
		} else {
			result = Optional.empty();
		}

		return result.map(this::toObject).orElse(null);
	}

	private OAuth2Authorization toObject(Authorization entity) {
		RegisteredClient registeredClient = this.registeredClientRepository.findById(entity.getRegisteredClientId());
		if (registeredClient == null) {
			throw new DataRetrievalFailureException(
					"The RegisteredClient with id '" + entity.getRegisteredClientId() + "' was not found in the RegisteredClientRepository.");
		}

		OAuth2Authorization.Builder builder = OAuth2Authorization.withRegisteredClient(registeredClient)
				.id(entity.getId())
				.principalName(entity.getPrincipalName())
				.authorizationGrantType(resolveAuthorizationGrantType(entity.getAuthorizationGrantType()))
				.authorizedScopes(StringUtils.commaDelimitedListToSet(entity.getAuthorizedScopes()))
				.attributes(attributes -> attributes.putAll(parseMap(entity.getAttributes())));
		if (entity.getState() != null) {
			builder.attribute(OAuth2ParameterNames.STATE, entity.getState());
		}

		if (entity.getAuthorizationCodeValue() != null) {
			OAuth2AuthorizationCode authorizationCode = new OAuth2AuthorizationCode(
					entity.getAuthorizationCodeValue(),
					entity.getAuthorizationCodeIssuedAt(),
					entity.getAuthorizationCodeExpiresAt());
			builder.token(authorizationCode, metadata -> metadata.putAll(parseMap(entity.getAuthorizationCodeMetadata())));
		}

		if (entity.getAccessTokenValue() != null) {
			OAuth2AccessToken accessToken = new OAuth2AccessToken(
					OAuth2AccessToken.TokenType.BEARER,
					entity.getAccessTokenValue(),
					entity.getAccessTokenIssuedAt(),
					entity.getAccessTokenExpiresAt(),
					StringUtils.commaDelimitedListToSet(entity.getAccessTokenScopes()));
			builder.token(accessToken, metadata -> metadata.putAll(parseMap(entity.getAccessTokenMetadata())));
		}

		if (entity.getRefreshTokenValue() != null) {
			OAuth2RefreshToken refreshToken = new OAuth2RefreshToken(
					entity.getRefreshTokenValue(),
					entity.getRefreshTokenIssuedAt(),
					entity.getRefreshTokenExpiresAt());
			builder.token(refreshToken, metadata -> metadata.putAll(parseMap(entity.getRefreshTokenMetadata())));
		}

		if (entity.getOidcIdTokenValue() != null) {
			OidcIdToken idToken = new OidcIdToken(
					entity.getOidcIdTokenValue(),
					entity.getOidcIdTokenIssuedAt(),
					entity.getOidcIdTokenExpiresAt(),
					parseMap(entity.getOidcIdTokenClaims()));
			builder.token(idToken, metadata -> metadata.putAll(parseMap(entity.getOidcIdTokenMetadata())));
		}

		if (entity.getUserCodeValue() != null) {
			OAuth2UserCode userCode = new OAuth2UserCode(
					entity.getUserCodeValue(),
					entity.getUserCodeIssuedAt(),
					entity.getUserCodeExpiresAt());
			builder.token(userCode, metadata -> metadata.putAll(parseMap(entity.getUserCodeMetadata())));
		}

		if (entity.getDeviceCodeValue() != null) {
			OAuth2DeviceCode deviceCode = new OAuth2DeviceCode(
					entity.getDeviceCodeValue(),
					entity.getDeviceCodeIssuedAt(),
					entity.getDeviceCodeExpiresAt());
			builder.token(deviceCode, metadata -> metadata.putAll(parseMap(entity.getDeviceCodeMetadata())));
		}

		return builder.build();
	}

	private Authorization toEntity(OAuth2Authorization authorization) {
		Authorization entity = new Authorization();
		entity.setId(authorization.getId());
		entity.setRegisteredClientId(authorization.getRegisteredClientId());
		entity.setPrincipalName(authorization.getPrincipalName());
		entity.setAuthorizationGrantType(authorization.getAuthorizationGrantType().getValue());
		entity.setAuthorizedScopes(StringUtils.collectionToDelimitedString(authorization.getAuthorizedScopes(), ","));
		entity.setAttributes(writeMap(authorization.getAttributes()));
		entity.setState(authorization.getAttribute(OAuth2ParameterNames.STATE));

		OAuth2Authorization.Token<OAuth2AuthorizationCode> authorizationCode =
				authorization.getToken(OAuth2AuthorizationCode.class);
		setTokenValues(
				authorizationCode,
				entity::setAuthorizationCodeValue,
				entity::setAuthorizationCodeIssuedAt,
				entity::setAuthorizationCodeExpiresAt,
				entity::setAuthorizationCodeMetadata
		);

		OAuth2Authorization.Token<OAuth2AccessToken> accessToken =
				authorization.getToken(OAuth2AccessToken.class);
		setTokenValues(
				accessToken,
				entity::setAccessTokenValue,
				entity::setAccessTokenIssuedAt,
				entity::setAccessTokenExpiresAt,
				entity::setAccessTokenMetadata
		);
		if (accessToken != null && accessToken.getToken().getScopes() != null) {
			entity.setAccessTokenScopes(StringUtils.collectionToDelimitedString(accessToken.getToken().getScopes(), ","));
		}

		OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken =
				authorization.getToken(OAuth2RefreshToken.class);
		setTokenValues(
				refreshToken,
				entity::setRefreshTokenValue,
				entity::setRefreshTokenIssuedAt,
				entity::setRefreshTokenExpiresAt,
				entity::setRefreshTokenMetadata
		);

		OAuth2Authorization.Token<OidcIdToken> oidcIdToken =
				authorization.getToken(OidcIdToken.class);
		setTokenValues(
				oidcIdToken,
				entity::setOidcIdTokenValue,
				entity::setOidcIdTokenIssuedAt,
				entity::setOidcIdTokenExpiresAt,
				entity::setOidcIdTokenMetadata
		);
		if (oidcIdToken != null) {
			entity.setOidcIdTokenClaims(writeMap(oidcIdToken.getClaims()));
		}

		OAuth2Authorization.Token<OAuth2UserCode> userCode =
				authorization.getToken(OAuth2UserCode.class);
		setTokenValues(
				userCode,
				entity::setUserCodeValue,
				entity::setUserCodeIssuedAt,
				entity::setUserCodeExpiresAt,
				entity::setUserCodeMetadata
		);

		OAuth2Authorization.Token<OAuth2DeviceCode> deviceCode =
				authorization.getToken(OAuth2DeviceCode.class);
		setTokenValues(
				deviceCode,
				entity::setDeviceCodeValue,
				entity::setDeviceCodeIssuedAt,
				entity::setDeviceCodeExpiresAt,
				entity::setDeviceCodeMetadata
		);

		return entity;
	}

	private void setTokenValues(
			OAuth2Authorization.Token<?> token,
			Consumer<String> tokenValueConsumer,
			Consumer<Instant> issuedAtConsumer,
			Consumer<Instant> expiresAtConsumer,
			Consumer<String> metadataConsumer) {
		if (token != null) {
			OAuth2Token oAuth2Token = token.getToken();
			tokenValueConsumer.accept(oAuth2Token.getTokenValue());
			issuedAtConsumer.accept(oAuth2Token.getIssuedAt());
			expiresAtConsumer.accept(oAuth2Token.getExpiresAt());
			metadataConsumer.accept(writeMap(token.getMetadata()));
		}
	}

	private Map<String, Object> parseMap(String data) {
		try {
			return this.objectMapper.readValue(data, new TypeReference<Map<String, Object>>() {
			});
		} catch (Exception ex) {
			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
	}

	private String writeMap(Map<String, Object> metadata) {
		try {
			return this.objectMapper.writeValueAsString(metadata);
		} catch (Exception ex) {
			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
	}

	private static AuthorizationGrantType resolveAuthorizationGrantType(String authorizationGrantType) {
		if (AuthorizationGrantType.AUTHORIZATION_CODE.getValue().equals(authorizationGrantType)) {
			return AuthorizationGrantType.AUTHORIZATION_CODE;
		} else if (AuthorizationGrantType.CLIENT_CREDENTIALS.getValue().equals(authorizationGrantType)) {
			return AuthorizationGrantType.CLIENT_CREDENTIALS;
		} else if (AuthorizationGrantType.REFRESH_TOKEN.getValue().equals(authorizationGrantType)) {
			return AuthorizationGrantType.REFRESH_TOKEN;
		} else if (AuthorizationGrantType.DEVICE_CODE.getValue().equals(authorizationGrantType)) {
			return AuthorizationGrantType.DEVICE_CODE;
		}
		return new AuthorizationGrantType(authorizationGrantType);              // Custom authorization grant type
	}
}
```

### 授权同意服务

下面的代码清单展示了 `JpaOAuth2AuthorizationConsentService`。它通过 `AuthorizationConsentRepository` 持久化
`AuthorizationConsent`，并在其与 `OAuth2AuthorizationConsent` 领域对象之间进行映射转换。

```java
import java.util.HashSet;
import java.util.Set;

import sample.jpa.entity.authorizationconsent.AuthorizationConsent;
import sample.jpa.repository.authorizationconsent.AuthorizationConsentRepository;

import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Component
public class JpaOAuth2AuthorizationConsentService implements OAuth2AuthorizationConsentService {
	private final AuthorizationConsentRepository authorizationConsentRepository;
	private final RegisteredClientRepository registeredClientRepository;

	public JpaOAuth2AuthorizationConsentService(AuthorizationConsentRepository authorizationConsentRepository, RegisteredClientRepository registeredClientRepository) {
		Assert.notNull(authorizationConsentRepository, "authorizationConsentRepository cannot be null");
		Assert.notNull(registeredClientRepository, "registeredClientRepository cannot be null");
		this.authorizationConsentRepository = authorizationConsentRepository;
		this.registeredClientRepository = registeredClientRepository;
	}

	@Override
	public void save(OAuth2AuthorizationConsent authorizationConsent) {
		Assert.notNull(authorizationConsent, "authorizationConsent cannot be null");
		this.authorizationConsentRepository.save(toEntity(authorizationConsent));
	}

	@Override
	public void remove(OAuth2AuthorizationConsent authorizationConsent) {
		Assert.notNull(authorizationConsent, "authorizationConsent cannot be null");
		this.authorizationConsentRepository.deleteByRegisteredClientIdAndPrincipalName(
				authorizationConsent.getRegisteredClientId(), authorizationConsent.getPrincipalName());
	}

	@Override
	public OAuth2AuthorizationConsent findById(String registeredClientId, String principalName) {
		Assert.hasText(registeredClientId, "registeredClientId cannot be empty");
		Assert.hasText(principalName, "principalName cannot be empty");
		return this.authorizationConsentRepository.findByRegisteredClientIdAndPrincipalName(
				registeredClientId, principalName).map(this::toObject).orElse(null);
	}

	private OAuth2AuthorizationConsent toObject(AuthorizationConsent authorizationConsent) {
		String registeredClientId = authorizationConsent.getRegisteredClientId();
		RegisteredClient registeredClient = this.registeredClientRepository.findById(registeredClientId);
		if (registeredClient == null) {
			throw new DataRetrievalFailureException(
					"The RegisteredClient with id '" + registeredClientId + "' was not found in the RegisteredClientRepository.");
		}

		OAuth2AuthorizationConsent.Builder builder = OAuth2AuthorizationConsent.withId(
				registeredClientId, authorizationConsent.getPrincipalName());
		if (authorizationConsent.getAuthorities() != null) {
			for (String authority : StringUtils.commaDelimitedListToSet(authorizationConsent.getAuthorities())) {
				builder.authority(new SimpleGrantedAuthority(authority));
			}
		}

		return builder.build();
	}

	private AuthorizationConsent toEntity(OAuth2AuthorizationConsent authorizationConsent) {
		AuthorizationConsent entity = new AuthorizationConsent();
		entity.setRegisteredClientId(authorizationConsent.getRegisteredClientId());
		entity.setPrincipalName(authorizationConsent.getPrincipalName());

		Set<String> authorities = new HashSet<>();
		for (GrantedAuthority authority : authorizationConsent.getAuthorities()) {
			authorities.add(authority.getAuthority());
		}
		entity.setAuthorities(StringUtils.collectionToCommaDelimitedString(authorities));

		return entity;
	}
}
```

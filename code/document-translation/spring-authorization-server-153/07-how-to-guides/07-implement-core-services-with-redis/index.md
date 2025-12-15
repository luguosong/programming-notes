
# 用Redis实现核心服务

本指南演示如何结合 Redis 实现 Spring Authorization Server 的核心服务。编写本指南的目的，是为你自行落地这些服务提供一个起步参考，方便你根据自身需求进行相应改造。

!!! tip

    本指南中提供的代码示例位于文档示例目录下的 [redis 子目录](https://github.com/spring-projects/spring-authorization-server/tree/main/docs/src/main/java/sample)中。

## 定义实体模型

以下内容定义了 RegisteredClient、OAuth2Authorization 和 OAuth2AuthorizationConsent 领域类的实体模型表示。

### 已注册客户端实体

下面的代码清单展示了 OAuth2RegisteredClient 实体，它用于持久化从 RegisteredClient 领域类映射而来的信息。

``` java
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithm;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;

@RedisHash("oauth2_registered_client")
public class OAuth2RegisteredClient {

	@Id
	private final String id;

	@Indexed
	private final String clientId;

	private final Instant clientIdIssuedAt;

	private final String clientSecret;

	private final Instant clientSecretExpiresAt;

	private final String clientName;

	private final Set<ClientAuthenticationMethod> clientAuthenticationMethods;

	private final Set<AuthorizationGrantType> authorizationGrantTypes;

	private final Set<String> redirectUris;

	private final Set<String> postLogoutRedirectUris;

	private final Set<String> scopes;

	private final ClientSettings clientSettings;

	private final TokenSettings tokenSettings;

	public OAuth2RegisteredClient(String id, String clientId, Instant clientIdIssuedAt, String clientSecret,
			Instant clientSecretExpiresAt, String clientName,
			Set<ClientAuthenticationMethod> clientAuthenticationMethods,
			Set<AuthorizationGrantType> authorizationGrantTypes, Set<String> redirectUris,
			Set<String> postLogoutRedirectUris, Set<String> scopes, ClientSettings clientSettings,
			TokenSettings tokenSettings) {
		this.id = id;
		this.clientId = clientId;
		this.clientIdIssuedAt = clientIdIssuedAt;
		this.clientSecret = clientSecret;
		this.clientSecretExpiresAt = clientSecretExpiresAt;
		this.clientName = clientName;
		this.clientAuthenticationMethods = clientAuthenticationMethods;
		this.authorizationGrantTypes = authorizationGrantTypes;
		this.redirectUris = redirectUris;
		this.postLogoutRedirectUris = postLogoutRedirectUris;
		this.scopes = scopes;
		this.clientSettings = clientSettings;
		this.tokenSettings = tokenSettings;
	}

	public String getId() {
		return this.id;
	}

	public String getClientId() {
		return this.clientId;
	}

	public Instant getClientIdIssuedAt() {
		return this.clientIdIssuedAt;
	}

	public String getClientSecret() {
		return this.clientSecret;
	}

	public Instant getClientSecretExpiresAt() {
		return this.clientSecretExpiresAt;
	}

	public String getClientName() {
		return this.clientName;
	}

	public Set<ClientAuthenticationMethod> getClientAuthenticationMethods() {
		return this.clientAuthenticationMethods;
	}

	public Set<AuthorizationGrantType> getAuthorizationGrantTypes() {
		return this.authorizationGrantTypes;
	}

	public Set<String> getRedirectUris() {
		return this.redirectUris;
	}

	public Set<String> getPostLogoutRedirectUris() {
		return this.postLogoutRedirectUris;
	}

	public Set<String> getScopes() {
		return this.scopes;
	}

	public ClientSettings getClientSettings() {
		return this.clientSettings;
	}

	public TokenSettings getTokenSettings() {
		return this.tokenSettings;
	}

	public static class ClientSettings {

		private final boolean requireProofKey;

		private final boolean requireAuthorizationConsent;

		private final String jwkSetUrl;

		private final JwsAlgorithm tokenEndpointAuthenticationSigningAlgorithm;

		private final String x509CertificateSubjectDN;

		public ClientSettings(boolean requireProofKey, boolean requireAuthorizationConsent, String jwkSetUrl,
				JwsAlgorithm tokenEndpointAuthenticationSigningAlgorithm, String x509CertificateSubjectDN) {
			this.requireProofKey = requireProofKey;
			this.requireAuthorizationConsent = requireAuthorizationConsent;
			this.jwkSetUrl = jwkSetUrl;
			this.tokenEndpointAuthenticationSigningAlgorithm = tokenEndpointAuthenticationSigningAlgorithm;
			this.x509CertificateSubjectDN = x509CertificateSubjectDN;
		}

		public boolean isRequireProofKey() {
			return this.requireProofKey;
		}

		public boolean isRequireAuthorizationConsent() {
			return this.requireAuthorizationConsent;
		}

		public String getJwkSetUrl() {
			return this.jwkSetUrl;
		}

		public JwsAlgorithm getTokenEndpointAuthenticationSigningAlgorithm() {
			return this.tokenEndpointAuthenticationSigningAlgorithm;
		}

		public String getX509CertificateSubjectDN() {
			return this.x509CertificateSubjectDN;
		}

	}

	public static class TokenSettings {

		private final Duration authorizationCodeTimeToLive;

		private final Duration accessTokenTimeToLive;

		private final OAuth2TokenFormat accessTokenFormat;

		private final Duration deviceCodeTimeToLive;

		private final boolean reuseRefreshTokens;

		private final Duration refreshTokenTimeToLive;

		private final SignatureAlgorithm idTokenSignatureAlgorithm;

		private final boolean x509CertificateBoundAccessTokens;

		public TokenSettings(Duration authorizationCodeTimeToLive, Duration accessTokenTimeToLive,
				OAuth2TokenFormat accessTokenFormat, Duration deviceCodeTimeToLive, boolean reuseRefreshTokens,
				Duration refreshTokenTimeToLive, SignatureAlgorithm idTokenSignatureAlgorithm,
				boolean x509CertificateBoundAccessTokens) {
			this.authorizationCodeTimeToLive = authorizationCodeTimeToLive;
			this.accessTokenTimeToLive = accessTokenTimeToLive;
			this.accessTokenFormat = accessTokenFormat;
			this.deviceCodeTimeToLive = deviceCodeTimeToLive;
			this.reuseRefreshTokens = reuseRefreshTokens;
			this.refreshTokenTimeToLive = refreshTokenTimeToLive;
			this.idTokenSignatureAlgorithm = idTokenSignatureAlgorithm;
			this.x509CertificateBoundAccessTokens = x509CertificateBoundAccessTokens;
		}

		public Duration getAuthorizationCodeTimeToLive() {
			return this.authorizationCodeTimeToLive;
		}

		public Duration getAccessTokenTimeToLive() {
			return this.accessTokenTimeToLive;
		}

		public OAuth2TokenFormat getAccessTokenFormat() {
			return this.accessTokenFormat;
		}

		public Duration getDeviceCodeTimeToLive() {
			return this.deviceCodeTimeToLive;
		}

		public boolean isReuseRefreshTokens() {
			return this.reuseRefreshTokens;
		}

		public Duration getRefreshTokenTimeToLive() {
			return this.refreshTokenTimeToLive;
		}

		public SignatureAlgorithm getIdTokenSignatureAlgorithm() {
			return this.idTokenSignatureAlgorithm;
		}

		public boolean isX509CertificateBoundAccessTokens() {
			return this.x509CertificateBoundAccessTokens;
		}

	}

}
```

### 授权许可基础实体

OAuth2Authorization 领域类的实体模型采用基于授权授予类型（authorization grant type）的类层次结构进行设计。

下面的代码清单展示了 OAuth2AuthorizationGrantAuthorization 基础实体，它定义了各类授权授予类型通用的属性。

``` java
import java.time.Instant;
import java.util.Map;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;

@RedisHash("oauth2_authorization")
public abstract class OAuth2AuthorizationGrantAuthorization {

	@Id
	private final String id;

	private final String registeredClientId;

	private final String principalName;

	private final Set<String> authorizedScopes;

	private final AccessToken accessToken;

	private final RefreshToken refreshToken;

	protected OAuth2AuthorizationGrantAuthorization(String id, String registeredClientId, String principalName,
			Set<String> authorizedScopes, AccessToken accessToken, RefreshToken refreshToken) {
		this.id = id;
		this.registeredClientId = registeredClientId;
		this.principalName = principalName;
		this.authorizedScopes = authorizedScopes;
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
	}

	public String getId() {
		return this.id;
	}

	public String getRegisteredClientId() {
		return this.registeredClientId;
	}

	public String getPrincipalName() {
		return this.principalName;
	}

	public Set<String> getAuthorizedScopes() {
		return this.authorizedScopes;
	}

	public AccessToken getAccessToken() {
		return this.accessToken;
	}

	public RefreshToken getRefreshToken() {
		return this.refreshToken;
	}

	protected abstract static class AbstractToken {

		@Indexed
		private final String tokenValue;

		private final Instant issuedAt;

		private final Instant expiresAt;

		private final boolean invalidated;

		protected AbstractToken(String tokenValue, Instant issuedAt, Instant expiresAt, boolean invalidated) {
			this.tokenValue = tokenValue;
			this.issuedAt = issuedAt;
			this.expiresAt = expiresAt;
			this.invalidated = invalidated;
		}

		public String getTokenValue() {
			return this.tokenValue;
		}

		public Instant getIssuedAt() {
			return this.issuedAt;
		}

		public Instant getExpiresAt() {
			return this.expiresAt;
		}

		public boolean isInvalidated() {
			return this.invalidated;
		}

	}

	public static class ClaimsHolder {

		private final Map<String, Object> claims;

		public ClaimsHolder(Map<String, Object> claims) {
			this.claims = claims;
		}

		public Map<String, Object> getClaims() {
			return this.claims;
		}

	}

	public static class AccessToken extends AbstractToken {

		private final OAuth2AccessToken.TokenType tokenType;

		private final Set<String> scopes;

		private final OAuth2TokenFormat tokenFormat;

		private final ClaimsHolder claims;

		public AccessToken(String tokenValue, Instant issuedAt, Instant expiresAt, boolean invalidated,
				OAuth2AccessToken.TokenType tokenType, Set<String> scopes, OAuth2TokenFormat tokenFormat,
				ClaimsHolder claims) {
			super(tokenValue, issuedAt, expiresAt, invalidated);
			this.tokenType = tokenType;
			this.scopes = scopes;
			this.tokenFormat = tokenFormat;
			this.claims = claims;
		}

		public OAuth2AccessToken.TokenType getTokenType() {
			return this.tokenType;
		}

		public Set<String> getScopes() {
			return this.scopes;
		}

		public OAuth2TokenFormat getTokenFormat() {
			return this.tokenFormat;
		}

		public ClaimsHolder getClaims() {
			return this.claims;
		}

	}

	public static class RefreshToken extends AbstractToken {

		public RefreshToken(String tokenValue, Instant issuedAt, Instant expiresAt, boolean invalidated) {
			super(tokenValue, issuedAt, expiresAt, invalidated);
		}

	}

}
```

### OAuth 2.0授权码授权实体

以下代码清单展示了 OAuth2AuthorizationCodeGrantAuthorization 实体。该实体继承自 OAuth2AuthorizationGrantAuthorization，并针对
OAuth 2.0 的 authorization_code 授权类型定义了额外的属性。

```java
import java.security.Principal;
import java.time.Instant;
import java.util.Set;

import org.springframework.data.redis.core.index.Indexed;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

public class OAuth2AuthorizationCodeGrantAuthorization extends OAuth2AuthorizationGrantAuthorization {

	private final Principal principal;

	private final OAuth2AuthorizationRequest authorizationRequest;

	private final AuthorizationCode authorizationCode;

	@Indexed
	private final String state; // Used to correlate the request during the authorization
	// consent flow

	public OAuth2AuthorizationCodeGrantAuthorization(String id, String registeredClientId, String principalName,
													 Set<String> authorizedScopes, AccessToken accessToken, RefreshToken refreshToken, Principal principal,
													 OAuth2AuthorizationRequest authorizationRequest, AuthorizationCode authorizationCode, String state) {
		super(id, registeredClientId, principalName, authorizedScopes, accessToken, refreshToken);
		this.principal = principal;
		this.authorizationRequest = authorizationRequest;
		this.authorizationCode = authorizationCode;
		this.state = state;
	}

	public Principal getPrincipal() {
		return this.principal;
	}

	public OAuth2AuthorizationRequest getAuthorizationRequest() {
		return this.authorizationRequest;
	}

	public AuthorizationCode getAuthorizationCode() {
		return this.authorizationCode;
	}

	public String getState() {
		return this.state;
	}

	public static class AuthorizationCode extends AbstractToken {

		public AuthorizationCode(String tokenValue, Instant issuedAt, Instant expiresAt, boolean invalidated) {
			super(tokenValue, issuedAt, expiresAt, invalidated);
		}

	}

}
```

### 授权码授权实体（OpenID Connect 1.0）

下面的代码清单展示了 **OidcAuthorizationCodeGrantAuthorization** 实体。它继承自 *
*OAuth2AuthorizationCodeGrantAuthorization**，并为 OpenID Connect 1.0 的 **authorization_code** 授权类型定义了额外的属性。

```java
import java.security.Principal;
import java.time.Instant;
import java.util.Set;

import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

public class OidcAuthorizationCodeGrantAuthorization extends OAuth2AuthorizationCodeGrantAuthorization {

	private final IdToken idToken;

	public OidcAuthorizationCodeGrantAuthorization(String id, String registeredClientId, String principalName,
												   Set<String> authorizedScopes, AccessToken accessToken, RefreshToken refreshToken, Principal principal,
												   OAuth2AuthorizationRequest authorizationRequest, AuthorizationCode authorizationCode, String state,
												   IdToken idToken) {
		super(id, registeredClientId, principalName, authorizedScopes, accessToken, refreshToken, principal,
				authorizationRequest, authorizationCode, state);
		this.idToken = idToken;
	}

	public IdToken getIdToken() {
		return this.idToken;
	}

	public static class IdToken extends AbstractToken {

		private final ClaimsHolder claims;

		public IdToken(String tokenValue, Instant issuedAt, Instant expiresAt, boolean invalidated,
					   ClaimsHolder claims) {
			super(tokenValue, issuedAt, expiresAt, invalidated);
			this.claims = claims;
		}

		public ClaimsHolder getClaims() {
			return this.claims;
		}

	}

}
```

### 客户端凭据授权实体

下面的代码清单展示了适用于 **client_credentials** 授权类型的 **OAuth2ClientCredentialsGrantAuthorization** 实体，它继承自
**OAuth2AuthorizationGrantAuthorization**。

```java
import java.util.Set;

public class OAuth2ClientCredentialsGrantAuthorization extends OAuth2AuthorizationGrantAuthorization {

	public OAuth2ClientCredentialsGrantAuthorization(String id, String registeredClientId, String principalName,
													 Set<String> authorizedScopes, AccessToken accessToken) {
		super(id, registeredClientId, principalName, authorizedScopes, accessToken, null);
	}

}
```

### 设备代码授权实体

下面的代码清单展示了 OAuth2DeviceCodeGrantAuthorization 实体。该实体继承自 OAuth2AuthorizationGrantAuthorization，并为
`urn:ietf:params:oauth:grant-type:device_code` 授权类型定义了额外的属性。

``` java
import java.security.Principal;
import java.time.Instant;
import java.util.Set;

import org.springframework.data.redis.core.index.Indexed;

public class OAuth2DeviceCodeGrantAuthorization extends OAuth2AuthorizationGrantAuthorization {

	private final Principal principal;

	private final DeviceCode deviceCode;

	private final UserCode userCode;

	private final Set<String> requestedScopes;

	@Indexed
	private final String deviceState; // 用于在授权同意流程中关联该请求

	public OAuth2DeviceCodeGrantAuthorization(String id, String registeredClientId, String principalName,
											  Set<String> authorizedScopes, AccessToken accessToken, RefreshToken refreshToken, Principal principal,
											  DeviceCode deviceCode, UserCode userCode, Set<String> requestedScopes, String deviceState) {
		super(id, registeredClientId, principalName, authorizedScopes, accessToken, refreshToken);
		this.principal = principal;
		this.deviceCode = deviceCode;
		this.userCode = userCode;
		this.requestedScopes = requestedScopes;
		this.deviceState = deviceState;
	}

	public Principal getPrincipal() {
		return this.principal;
	}

	public DeviceCode getDeviceCode() {
		return this.deviceCode;
	}

	public UserCode getUserCode() {
		return this.userCode;
	}

	public Set<String> getRequestedScopes() {
		return this.requestedScopes;
	}

	public String getDeviceState() {
		return this.deviceState;
	}

	public static class DeviceCode extends AbstractToken {

		public DeviceCode(String tokenValue, Instant issuedAt, Instant expiresAt, boolean invalidated) {
			super(tokenValue, issuedAt, expiresAt, invalidated);
		}

	}

	public static class UserCode extends AbstractToken {

		public UserCode(String tokenValue, Instant issuedAt, Instant expiresAt, boolean invalidated) {
			super(tokenValue, issuedAt, expiresAt, invalidated);
		}

	}

}
```

### 令牌交换授权实体

下面的代码清单展示了用于 `urn:ietf:params:oauth:grant-type:token-exchange` 授权类型的 OAuth2TokenExchangeGrantAuthorization 实体类，它继承自 OAuth2AuthorizationGrantAuthorization。

```java
import java.util.Set;

public class OAuth2TokenExchangeGrantAuthorization extends OAuth2AuthorizationGrantAuthorization {

	public OAuth2TokenExchangeGrantAuthorization(String id, String registeredClientId, String principalName,
			Set<String> authorizedScopes, AccessToken accessToken) {
		super(id, registeredClientId, principalName, authorizedScopes, accessToken, null);
	}

}
```

### 授权同意实体

下面的代码清单展示了 OAuth2UserConsent 实体，用于持久化从 OAuth2AuthorizationConsent 领域类映射而来的信息。

```java
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;
import org.springframework.security.core.GrantedAuthority;

@RedisHash("oauth2_authorization_consent")
public class OAuth2UserConsent {

	@Id
	private final String id;

	@Indexed
	private final String registeredClientId;

	@Indexed
	private final String principalName;

	private final Set<GrantedAuthority> authorities;

	public OAuth2UserConsent(String id, String registeredClientId, String principalName,
			Set<GrantedAuthority> authorities) {
		this.id = id;
		this.registeredClientId = registeredClientId;
		this.principalName = principalName;
		this.authorities = authorities;
	}

	public String getId() {
		return this.id;
	}

	public String getRegisteredClientId() {
		return this.registeredClientId;
	}

	public String getPrincipalName() {
		return this.principalName;
	}

	public Set<GrantedAuthority> getAuthorities() {
		return this.authorities;
	}

}
```

## 创建Spring Data仓库接口

通过细致梳理各个核心服务的接口，并审阅其 JDBC 实现，我们可以归纳出一组最小化的查询集合，用于支撑这些接口的 Redis 版本实现。

### 已注册客户端存储库

下面的代码清单展示了 OAuth2RegisteredClientRepository，它可以通过 id 和 clientId 字段查找 OAuth2RegisteredClient。

```java
import sample.redis.entity.OAuth2RegisteredClient;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OAuth2RegisteredClientRepository extends CrudRepository<OAuth2RegisteredClient, String> {

	OAuth2RegisteredClient findByClientId(String clientId);

}
```

### 授权授予存储库

下面的代码清单展示了 `OAuth2AuthorizationGrantAuthorizationRepository`。该仓库既可以通过 `id` 字段查找 `OAuth2AuthorizationGrantAuthorization`，也支持按 `state`、`authorizationCode`、`accessToken`、`refreshToken`、`idToken`、`deviceState`、`userCode` 和 `deviceCode` 等值进行查询。

```java
import sample.redis.entity.OAuth2AuthorizationCodeGrantAuthorization;
import sample.redis.entity.OAuth2AuthorizationGrantAuthorization;
import sample.redis.entity.OAuth2DeviceCodeGrantAuthorization;
import sample.redis.entity.OidcAuthorizationCodeGrantAuthorization;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OAuth2AuthorizationGrantAuthorizationRepository
		extends CrudRepository<OAuth2AuthorizationGrantAuthorization, String> {

	<T extends OAuth2AuthorizationCodeGrantAuthorization> T findByState(String state);

	<T extends OAuth2AuthorizationCodeGrantAuthorization> T findByAuthorizationCode_TokenValue(String authorizationCode);

	<T extends OAuth2AuthorizationCodeGrantAuthorization> T findByStateOrAuthorizationCode_TokenValue(String state, String authorizationCode);

	<T extends OAuth2AuthorizationGrantAuthorization> T findByAccessToken_TokenValue(String accessToken);

	<T extends OAuth2AuthorizationGrantAuthorization> T findByRefreshToken_TokenValue(String refreshToken);

	<T extends OAuth2AuthorizationGrantAuthorization> T findByAccessToken_TokenValueOrRefreshToken_TokenValue(String accessToken, String refreshToken);

	<T extends OidcAuthorizationCodeGrantAuthorization> T findByIdToken_TokenValue(String idToken);

	<T extends OAuth2DeviceCodeGrantAuthorization> T findByDeviceState(String deviceState);

	<T extends OAuth2DeviceCodeGrantAuthorization> T findByDeviceCode_TokenValue(String deviceCode);

	<T extends OAuth2DeviceCodeGrantAuthorization> T findByUserCode_TokenValue(String userCode);

	<T extends OAuth2DeviceCodeGrantAuthorization> T findByDeviceStateOrDeviceCode_TokenValueOrUserCode_TokenValue(String deviceState, String deviceCode, String userCode);

}
```

### 授权同意存储库

下面的代码清单展示了 OAuth2UserConsentRepository，它可以通过组成复合主键的 registeredClientId 和 principalName 字段来查找并删除 OAuth2UserConsent。

```java
import sample.redis.entity.OAuth2UserConsent;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OAuth2UserConsentRepository extends CrudRepository<OAuth2UserConsent, String> {

	OAuth2UserConsent findByRegisteredClientIdAndPrincipalName(String registeredClientId, String principalName);

	void deleteByRegisteredClientIdAndPrincipalName(String registeredClientId, String principalName);

}
```

## 实现核心服务

有了上述实体和仓储之后，我们就可以开始实现核心服务了。

!!! tip

    核心服务使用 ModelMapper 工具类，在领域对象（如 RegisteredClient）与实体模型表示（如 OAuth2RegisteredClient）之间进行相互转换。

### 注册客户端仓库

下面的代码清单展示了 `RedisRegisteredClientRepository`：它使用 `OAuth2RegisteredClientRepository` 来持久化 `OAuth2RegisteredClient`，并借助 `ModelMapper` 工具类在 `RegisteredClient` 领域对象之间进行映射与转换。

```java
import sample.redis.entity.OAuth2RegisteredClient;
import sample.redis.repository.OAuth2RegisteredClientRepository;

import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.util.Assert;

public class RedisRegisteredClientRepository implements RegisteredClientRepository {

	private final OAuth2RegisteredClientRepository registeredClientRepository;

	public RedisRegisteredClientRepository(OAuth2RegisteredClientRepository registeredClientRepository) {
		Assert.notNull(registeredClientRepository, "registeredClientRepository cannot be null");
		this.registeredClientRepository = registeredClientRepository;
	}

	@Override
	public void save(RegisteredClient registeredClient) {
		Assert.notNull(registeredClient, "registeredClient cannot be null");
		OAuth2RegisteredClient oauth2RegisteredClient = ModelMapper.convertOAuth2RegisteredClient(registeredClient);
		this.registeredClientRepository.save(oauth2RegisteredClient);
	}

	@Nullable
	@Override
	public RegisteredClient findById(String id) {
		Assert.hasText(id, "id cannot be empty");
		return this.registeredClientRepository.findById(id).map(ModelMapper::convertRegisteredClient).orElse(null);
	}

	@Nullable
	@Override
	public RegisteredClient findByClientId(String clientId) {
		Assert.hasText(clientId, "clientId cannot be empty");
		OAuth2RegisteredClient oauth2RegisteredClient = this.registeredClientRepository.findByClientId(clientId);
		return oauth2RegisteredClient != null ? ModelMapper.convertRegisteredClient(oauth2RegisteredClient) : null;
	}

}
```

### 授权服务

下面的代码清单展示了 `RedisOAuth2AuthorizationService`：它使用 `OAuth2AuthorizationGrantAuthorizationRepository` 来持久化 `OAuth2AuthorizationGrantAuthorization`，并借助 `ModelMapper` 工具类在其与 `OAuth2Authorization` 领域对象之间进行映射转换。

```java
import sample.redis.entity.OAuth2AuthorizationGrantAuthorization;
import sample.redis.repository.OAuth2AuthorizationGrantAuthorizationRepository;

import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.util.Assert;

public class RedisOAuth2AuthorizationService implements OAuth2AuthorizationService {

	private final RegisteredClientRepository registeredClientRepository;

	private final OAuth2AuthorizationGrantAuthorizationRepository authorizationGrantAuthorizationRepository;

	public RedisOAuth2AuthorizationService(RegisteredClientRepository registeredClientRepository,
			OAuth2AuthorizationGrantAuthorizationRepository authorizationGrantAuthorizationRepository) {
		Assert.notNull(registeredClientRepository, "registeredClientRepository cannot be null");
		Assert.notNull(authorizationGrantAuthorizationRepository,
				"authorizationGrantAuthorizationRepository cannot be null");
		this.registeredClientRepository = registeredClientRepository;
		this.authorizationGrantAuthorizationRepository = authorizationGrantAuthorizationRepository;
	}

	@Override
	public void save(OAuth2Authorization authorization) {
		Assert.notNull(authorization, "authorization cannot be null");
		OAuth2AuthorizationGrantAuthorization authorizationGrantAuthorization = ModelMapper
			.convertOAuth2AuthorizationGrantAuthorization(authorization);
		this.authorizationGrantAuthorizationRepository.save(authorizationGrantAuthorization);
	}

	@Override
	public void remove(OAuth2Authorization authorization) {
		Assert.notNull(authorization, "authorization cannot be null");
		this.authorizationGrantAuthorizationRepository.deleteById(authorization.getId());
	}

	@Nullable
	@Override
	public OAuth2Authorization findById(String id) {
		Assert.hasText(id, "id cannot be empty");
		return this.authorizationGrantAuthorizationRepository.findById(id)
			.map(this::toOAuth2Authorization)
			.orElse(null);
	}

	@Nullable
	@Override
	public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
		Assert.hasText(token, "token cannot be empty");
		OAuth2AuthorizationGrantAuthorization authorizationGrantAuthorization = null;
		if (tokenType == null) {
			authorizationGrantAuthorization = this.authorizationGrantAuthorizationRepository
				.findByStateOrAuthorizationCode_TokenValue(token, token);
			if (authorizationGrantAuthorization == null) {
				authorizationGrantAuthorization = this.authorizationGrantAuthorizationRepository
						.findByAccessToken_TokenValueOrRefreshToken_TokenValue(token, token);
			}
			if (authorizationGrantAuthorization == null) {
				authorizationGrantAuthorization = this.authorizationGrantAuthorizationRepository
						.findByIdToken_TokenValue(token);
			}
			if (authorizationGrantAuthorization == null) {
				authorizationGrantAuthorization = this.authorizationGrantAuthorizationRepository
						.findByDeviceStateOrDeviceCode_TokenValueOrUserCode_TokenValue(token, token, token);
			}
		}
		else if (OAuth2ParameterNames.STATE.equals(tokenType.getValue())) {
			authorizationGrantAuthorization = this.authorizationGrantAuthorizationRepository.findByState(token);
			if (authorizationGrantAuthorization == null) {
				authorizationGrantAuthorization = this.authorizationGrantAuthorizationRepository
					.findByDeviceState(token);
			}
		}
		else if (OAuth2ParameterNames.CODE.equals(tokenType.getValue())) {
			authorizationGrantAuthorization = this.authorizationGrantAuthorizationRepository
				.findByAuthorizationCode_TokenValue(token);
		}
		else if (OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)) {
			authorizationGrantAuthorization = this.authorizationGrantAuthorizationRepository
				.findByAccessToken_TokenValue(token);
		}
		else if (OidcParameterNames.ID_TOKEN.equals(tokenType.getValue())) {
			authorizationGrantAuthorization = this.authorizationGrantAuthorizationRepository
				.findByIdToken_TokenValue(token);
		}
		else if (OAuth2TokenType.REFRESH_TOKEN.equals(tokenType)) {
			authorizationGrantAuthorization = this.authorizationGrantAuthorizationRepository
				.findByRefreshToken_TokenValue(token);
		}
		else if (OAuth2ParameterNames.USER_CODE.equals(tokenType.getValue())) {
			authorizationGrantAuthorization = this.authorizationGrantAuthorizationRepository
				.findByUserCode_TokenValue(token);
		}
		else if (OAuth2ParameterNames.DEVICE_CODE.equals(tokenType.getValue())) {
			authorizationGrantAuthorization = this.authorizationGrantAuthorizationRepository
				.findByDeviceCode_TokenValue(token);
		}
		return authorizationGrantAuthorization != null ? toOAuth2Authorization(authorizationGrantAuthorization) : null;
	}

	private OAuth2Authorization toOAuth2Authorization(
			OAuth2AuthorizationGrantAuthorization authorizationGrantAuthorization) {
		RegisteredClient registeredClient = this.registeredClientRepository
			.findById(authorizationGrantAuthorization.getRegisteredClientId());
		OAuth2Authorization.Builder builder = OAuth2Authorization.withRegisteredClient(registeredClient);
		ModelMapper.mapOAuth2AuthorizationGrantAuthorization(authorizationGrantAuthorization, builder);
		return builder.build();
	}

}
```

### 授权同意服务

下面的代码清单展示了 RedisOAuth2AuthorizationConsentService。该服务使用 OAuth2UserConsentRepository 持久化 OAuth2UserConsent，并借助 ModelMapper 工具类在 OAuth2AuthorizationConsent 领域对象与其对应的数据对象之间进行双向映射。

```java
import sample.redis.entity.OAuth2UserConsent;
import sample.redis.repository.OAuth2UserConsentRepository;

import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.util.Assert;

public class RedisOAuth2AuthorizationConsentService implements OAuth2AuthorizationConsentService {

	private final OAuth2UserConsentRepository userConsentRepository;

	public RedisOAuth2AuthorizationConsentService(OAuth2UserConsentRepository userConsentRepository) {
		Assert.notNull(userConsentRepository, "userConsentRepository cannot be null");
		this.userConsentRepository = userConsentRepository;
	}

	@Override
	public void save(OAuth2AuthorizationConsent authorizationConsent) {
		Assert.notNull(authorizationConsent, "authorizationConsent cannot be null");
		OAuth2UserConsent oauth2UserConsent = ModelMapper.convertOAuth2UserConsent(authorizationConsent);
		this.userConsentRepository.save(oauth2UserConsent);
	}

	@Override
	public void remove(OAuth2AuthorizationConsent authorizationConsent) {
		Assert.notNull(authorizationConsent, "authorizationConsent cannot be null");
		this.userConsentRepository.deleteByRegisteredClientIdAndPrincipalName(
				authorizationConsent.getRegisteredClientId(), authorizationConsent.getPrincipalName());
	}

	@Nullable
	@Override
	public OAuth2AuthorizationConsent findById(String registeredClientId, String principalName) {
		Assert.hasText(registeredClientId, "registeredClientId cannot be empty");
		Assert.hasText(principalName, "principalName cannot be empty");
		OAuth2UserConsent oauth2UserConsent = this.userConsentRepository
			.findByRegisteredClientIdAndPrincipalName(registeredClientId, principalName);
		return oauth2UserConsent != null ? ModelMapper.convertOAuth2AuthorizationConsent(oauth2UserConsent) : null;
	}

}
```

## 配置核心服务

下面的示例演示了如何配置核心服务：

``` java
import java.util.Arrays;

import sample.redis.convert.BytesToClaimsHolderConverter;
import sample.redis.convert.BytesToOAuth2AuthorizationRequestConverter;
import sample.redis.convert.BytesToUsernamePasswordAuthenticationTokenConverter;
import sample.redis.convert.ClaimsHolderToBytesConverter;
import sample.redis.convert.OAuth2AuthorizationRequestToBytesConverter;
import sample.redis.convert.UsernamePasswordAuthenticationTokenToBytesConverter;
import sample.redis.repository.OAuth2AuthorizationGrantAuthorizationRepository;
import sample.redis.repository.OAuth2RegisteredClientRepository;
import sample.redis.repository.OAuth2UserConsentRepository;
import sample.redis.service.RedisOAuth2AuthorizationConsentService;
import sample.redis.service.RedisOAuth2AuthorizationService;
import sample.redis.service.RedisRegisteredClientRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.convert.RedisCustomConversions;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

//启用 sample.redis.repository 基包下的 Spring Data Redis Repository。
@EnableRedisRepositories("sample.redis.repository")
@Configuration(proxyBeanMethods = false)
public class RedisConfig {

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		//使用 Jedis 连接器。
		return new JedisConnectionFactory();
	}

	@Bean
	public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<byte[], byte[]> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		return redisTemplate;
	}

	//注册用于在持久化到 Redis 之前执行对象到 Hash 转换的自定义 Converter。
	@Bean
	public RedisCustomConversions redisCustomConversions() {
		return new RedisCustomConversions(Arrays.asList(new UsernamePasswordAuthenticationTokenToBytesConverter(),
				new BytesToUsernamePasswordAuthenticationTokenConverter(),
				new OAuth2AuthorizationRequestToBytesConverter(), new BytesToOAuth2AuthorizationRequestConverter(),
				new ClaimsHolderToBytesConverter(), new BytesToClaimsHolderConverter()));
	}

	@Bean
	public RedisRegisteredClientRepository registeredClientRepository(
			OAuth2RegisteredClientRepository registeredClientRepository) {
		//将 RedisRegisteredClientRepository 注册为已启用的 OAuth2RegisteredClientRepository。
		return new RedisRegisteredClientRepository(registeredClientRepository);
	}

	@Bean
	public RedisOAuth2AuthorizationService authorizationService(RegisteredClientRepository registeredClientRepository,
			OAuth2AuthorizationGrantAuthorizationRepository authorizationGrantAuthorizationRepository) {
		return new RedisOAuth2AuthorizationService(registeredClientRepository,
				//将 RedisOAuth2AuthorizationService 注册为已启用的 OAuth2AuthorizationGrantAuthorizationRepository。
				authorizationGrantAuthorizationRepository);
	}

	@Bean
	public RedisOAuth2AuthorizationConsentService authorizationConsentService(
			OAuth2UserConsentRepository userConsentRepository) {
		//将 RedisOAuth2AuthorizationConsentService 注册为已启用的 OAuth2UserConsentRepository。
		return new RedisOAuth2AuthorizationConsentService(userConsentRepository);
	}

}
```

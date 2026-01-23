# 动态注册客户端

本指南介绍如何在 Spring Authorization Server 中配置 OpenID Connect 动态客户端注册，并通过示例演示如何注册客户端。Spring
Authorization Server 实现了《OpenID Connect Dynamic Client Registration 1.0》规范，支持动态注册并检索 OpenID Connect 客户端。

## 启用动态客户端注册

默认情况下，Spring Authorization Server 已禁用动态客户端注册功能。若要启用，请添加以下配置：

``` java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import static sample.registration.CustomClientMetadataConfig.configureCustomClientMetadataConverters;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
		OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
				OAuth2AuthorizationServerConfigurer.authorizationServer();

		http
			.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
			.with(authorizationServerConfigurer, (authorizationServer) ->
				authorizationServer
					.oidc((oidc) ->
						//	使用默认配置启用 OpenID Connect 1.0 客户端注册端点。 
						oidc.clientRegistrationEndpoint((clientRegistrationEndpoint) ->	
							clientRegistrationEndpoint
								//	可选）自定义默认的 AuthenticationProvider，以支持自定义客户端元数据参数。
								.authenticationProviders(configureCustomClientMetadataConverters())	
						)
					)
			)
			.authorizeHttpRequests((authorize) ->
				authorize
					.anyRequest().authenticated()
			);

		return http.build();
	}

}
```

为了在注册客户端时支持自定义的客户端元数据参数，还需要补充一些额外的实现细节。

下面的示例展示了 Converter 的一个参考实现：它支持自定义客户端元数据参数（logo_uri 和 contacts），并已在
OidcClientRegistrationAuthenticationProvider 与 OidcClientConfigurationAuthenticationProvider 中完成配置。

```java
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.oidc.OidcClientRegistration;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcClientConfigurationAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcClientRegistrationAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.oidc.converter.OidcClientRegistrationRegisteredClientConverter;
import org.springframework.security.oauth2.server.authorization.oidc.converter.RegisteredClientOidcClientRegistrationConverter;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.util.CollectionUtils;

public class CustomClientMetadataConfig {

	//定义一个 Consumer<List<AuthenticationProvider>>，用于自定义默认的 AuthenticationProvider。
	public static Consumer<List<AuthenticationProvider>> configureCustomClientMetadataConverters() {
		//定义在客户端注册时支持的自定义客户端元数据参数。
		List<String> customClientMetadata = List.of("logo_uri", "contacts");

		return (authenticationProviders) -> {
			CustomRegisteredClientConverter registeredClientConverter =
					new CustomRegisteredClientConverter(customClientMetadata);
			CustomClientRegistrationConverter clientRegistrationConverter =
					new CustomClientRegistrationConverter(customClientMetadata);

			authenticationProviders.forEach((authenticationProvider) -> {
				if (authenticationProvider instanceof OidcClientRegistrationAuthenticationProvider provider) {
					//通过 CustomRegisteredClientConverter 配置 OidcClientRegistrationAuthenticationProvider.setRegisteredClientConverter()。
					provider.setRegisteredClientConverter(registeredClientConverter);
					//通过 CustomClientRegistrationConverter 配置 OidcClientRegistrationAuthenticationProvider.setClientRegistrationConverter()。
					provider.setClientRegistrationConverter(clientRegistrationConverter);
				}
				if (authenticationProvider instanceof OidcClientConfigurationAuthenticationProvider provider) {
					//通过 CustomClientRegistrationConverter 配置 OidcClientConfigurationAuthenticationProvider.setClientRegistrationConverter()。
					provider.setClientRegistrationConverter(clientRegistrationConverter);
				}
			});
		};
	}

	private static class CustomRegisteredClientConverter
			implements Converter<OidcClientRegistration, RegisteredClient> {

		private final List<String> customClientMetadata;
		private final OidcClientRegistrationRegisteredClientConverter delegate;

		private CustomRegisteredClientConverter(List<String> customClientMetadata) {
			this.customClientMetadata = customClientMetadata;
			this.delegate = new OidcClientRegistrationRegisteredClientConverter();
		}

		@Override
		public RegisteredClient convert(OidcClientRegistration clientRegistration) {
			RegisteredClient registeredClient = this.delegate.convert(clientRegistration);
			ClientSettings.Builder clientSettingsBuilder = ClientSettings.withSettings(
					registeredClient.getClientSettings().getSettings());
			if (!CollectionUtils.isEmpty(this.customClientMetadata)) {
				clientRegistration.getClaims().forEach((claim, value) -> {
					if (this.customClientMetadata.contains(claim)) {
						clientSettingsBuilder.setting(claim, value);
					}
				});
			}

			return RegisteredClient.from(registeredClient)
					.clientSettings(clientSettingsBuilder.build())
					.build();
		}
	}

	private static class CustomClientRegistrationConverter
			implements Converter<RegisteredClient, OidcClientRegistration> {

		private final List<String> customClientMetadata;
		private final RegisteredClientOidcClientRegistrationConverter delegate;

		private CustomClientRegistrationConverter(List<String> customClientMetadata) {
			this.customClientMetadata = customClientMetadata;
			this.delegate = new RegisteredClientOidcClientRegistrationConverter();
		}

		@Override
		public OidcClientRegistration convert(RegisteredClient registeredClient) {
			OidcClientRegistration clientRegistration = this.delegate.convert(registeredClient);
			Map<String, Object> claims = new HashMap<>(clientRegistration.getClaims());
			if (!CollectionUtils.isEmpty(this.customClientMetadata)) {
				ClientSettings clientSettings = registeredClient.getClientSettings();
				claims.putAll(this.customClientMetadata.stream()
						.filter(metadata -> clientSettings.getSetting(metadata) != null)
						.collect(Collectors.toMap(Function.identity(), clientSettings::getSetting)));
			}

			return OidcClientRegistration.withClaims(claims).build();
		}

	}

}
```

## 配置客户端注册器

现有客户端用于向授权服务器注册新的客户端。该客户端必须配置 `client.create` 作用域，用于注册客户端；另外还可选配
`client.read` 作用域，用于检索客户端信息。下面的代码清单展示了一个示例客户端：

``` java
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

@Configuration
public class ClientConfig {

	@Bean
	public RegisteredClientRepository registeredClientRepository() {
		RegisteredClient registrarClient = RegisteredClient.withId(UUID.randomUUID().toString())
				.clientId("registrar-client")
				.clientSecret("{noop}secret")
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
				// 已配置 client_credentials 授权类型，用于直接获取访问令牌。
				.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
				//已配置 client.create scope，允许客户端注册新的客户端。
				.scope("client.create")
				//已配置 client.read scope，允许客户端获取已注册的客户端信息。
				.scope("client.read")
				.build();

		return new InMemoryRegisteredClientRepository(registrarClient);
	}

}
```

## 获取初始访问令牌

客户端注册请求需要一个“初始”访问令牌。获取该访问令牌的请求必须且只能包含 scope 参数值 `client.create`。

```shell
POST /oauth2/token HTTP/1.1
Authorization: Basic <base64-encoded-credentials>
Content-Type: application/x-www-form-urlencoded

grant_type=client_credentials&scope=client.create
```

!!! warning

    客户端注册请求需要一个仅包含 `client.create` 单一作用域的访问令牌。如果该访问令牌包含其他额外作用域，客户端注册请求将被拒绝。

!!! tip

    要为上述请求生成编码后的凭据，请按 `<clientId>:<clientSecret>` 的格式拼接客户端凭据，并进行 Base64 编码。下面以本指南中的示例演示编码操作：

	```bash
	echo -n "registrar-client:secret" | base64
	```

## 注册客户端

通过上一步获取的访问令牌（access token），客户端现在可以进行动态注册。

!!! note

    “初始”访问令牌只能使用一次。客户端完成注册后，该访问令牌将立即失效。

``` java
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.reactive.function.client.WebClient;

public class ClientRegistrar {
	private final WebClient webClient;

	public ClientRegistrar(WebClient webClient) {
		this.webClient = webClient;
	}

	//客户端注册请求的最简表示。你可以根据《客户端注册请求》的要求添加更多客户端元数据参数。本示例请求包含自定义客户端元数据参数 logo_uri 和 contacts。
	public record ClientRegistrationRequest(
			@JsonProperty("client_name") String clientName,
			@JsonProperty("grant_types") List<String> grantTypes,
			@JsonProperty("redirect_uris") List<String> redirectUris,
			@JsonProperty("logo_uri") String logoUri,
			List<String> contacts,
			String scope) {
	}

	//客户端注册响应的最简表示。你可以根据《客户端注册响应》的要求添加更多客户端元数据参数。本示例响应包含自定义客户端元数据参数 logo_uri 和 contacts。
	public record ClientRegistrationResponse(
			@JsonProperty("registration_access_token") String registrationAccessToken,
			@JsonProperty("registration_client_uri") String registrationClientUri,
			@JsonProperty("client_name") String clientName,
			@JsonProperty("client_id") String clientId,
			@JsonProperty("client_secret") String clientSecret,
			@JsonProperty("grant_types") List<String> grantTypes,
			@JsonProperty("redirect_uris") List<String> redirectUris,
		 	@JsonProperty("logo_uri") String logoUri,
		 	List<String> contacts,
			String scope) {
	}

	//演示客户端注册与客户端读取的示例。
	public void exampleRegistration(String initialAccessToken) {
		//一个示例客户端注册请求对象。
		ClientRegistrationRequest clientRegistrationRequest = new ClientRegistrationRequest(
				"client-1",
				List.of(AuthorizationGrantType.AUTHORIZATION_CODE.getValue()),
				List.of("https://client.example.org/callback", "https://client.example.org/callback2"),
				"https://client.example.org/logo",
				List.of("contact-1", "contact-2"),
				"openid email profile"
		);

		//使用“initial”访问令牌和客户端注册请求对象注册客户端。
		ClientRegistrationResponse clientRegistrationResponse =
				registerClient(initialAccessToken, clientRegistrationRequest);

		//注册成功后，对响应中应被填充的客户端元数据参数进行断言。
		assert (clientRegistrationResponse.clientName().contentEquals("client-1"));
		assert (!Objects.isNull(clientRegistrationResponse.clientSecret()));
		assert (clientRegistrationResponse.scope().contentEquals("openid profile email"));
		assert (clientRegistrationResponse.grantTypes().contains(AuthorizationGrantType.AUTHORIZATION_CODE.getValue()));
		assert (clientRegistrationResponse.redirectUris().contains("https://client.example.org/callback"));
		assert (clientRegistrationResponse.redirectUris().contains("https://client.example.org/callback2"));
		assert (!clientRegistrationResponse.registrationAccessToken().isEmpty());
		assert (!clientRegistrationResponse.registrationClientUri().isEmpty());
		assert (clientRegistrationResponse.logoUri().contentEquals("https://client.example.org/logo"));
		assert (clientRegistrationResponse.contacts().size() == 2);
		assert (clientRegistrationResponse.contacts().contains("contact-1"));
		assert (clientRegistrationResponse.contacts().contains("contact-2"));

		//提取响应参数 registration_access_token 和 registration_client_uri，用于读取新注册的客户端。
		String registrationAccessToken = clientRegistrationResponse.registrationAccessToken();
		String registrationClientUri = clientRegistrationResponse.registrationClientUri();

		//使用 registration_access_token 和 registration_client_uri 读取客户端。
		ClientRegistrationResponse retrievedClient = retrieveClient(registrationAccessToken, registrationClientUri);

		//客户端读取完成后，对响应中应被填充的客户端元数据参数进行断言。
		assert (retrievedClient.clientName().contentEquals("client-1"));
		assert (!Objects.isNull(retrievedClient.clientId()));
		assert (!Objects.isNull(retrievedClient.clientSecret()));
		assert (retrievedClient.scope().contentEquals("openid profile email"));
		assert (retrievedClient.grantTypes().contains(AuthorizationGrantType.AUTHORIZATION_CODE.getValue()));
		assert (retrievedClient.redirectUris().contains("https://client.example.org/callback"));
		assert (retrievedClient.redirectUris().contains("https://client.example.org/callback2"));
		assert (retrievedClient.logoUri().contentEquals("https://client.example.org/logo"));
		assert (retrievedClient.contacts().size() == 2);
		assert (retrievedClient.contacts().contains("contact-1"));
		assert (retrievedClient.contacts().contains("contact-2"));
		assert (Objects.isNull(retrievedClient.registrationAccessToken()));
		assert (!retrievedClient.registrationClientUri().isEmpty());
	}

	//使用 WebClient 的示例客户端注册请求。
	public ClientRegistrationResponse registerClient(String initialAccessToken, ClientRegistrationRequest request) {
		return this.webClient
				.post()
				.uri("/connect/register")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(initialAccessToken))
				.body(Mono.just(request), ClientRegistrationRequest.class)
				.retrieve()
				.bodyToMono(ClientRegistrationResponse.class)
				.block();
	}

	//使用 WebClient 的示例客户端读取请求。
	public ClientRegistrationResponse retrieveClient(String registrationAccessToken, String registrationClientUri) {
		return this.webClient
				.get()
				.uri(registrationClientUri)
				.header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(registrationAccessToken))
				.retrieve()
				.bodyToMono(ClientRegistrationResponse.class)
				.block();
	}

}
```

!!! note

    客户端读取响应应包含与客户端注册响应相同的客户端元数据参数，但不包括 registration_access_token 参数。

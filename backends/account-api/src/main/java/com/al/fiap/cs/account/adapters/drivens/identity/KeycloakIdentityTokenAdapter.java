package com.al.fiap.cs.account.adapters.drivens.identity;

import com.al.fiap.cs.account.adapters.config.AccountProperties;
import com.al.fiap.cs.account.core.domain.exceptions.IdentityProviderException;
import com.al.fiap.cs.account.core.domain.exceptions.InvalidCredentialsException;
import com.al.fiap.cs.account.ports.identity.IdentityTokenPort;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Map;

public class KeycloakIdentityTokenAdapter implements IdentityTokenPort {

	private final RestClient restClient;
	private final AccountProperties properties;

	public KeycloakIdentityTokenAdapter(RestClient.Builder restClientBuilder, AccountProperties properties) {
		this.restClient = restClientBuilder.build();
		this.properties = properties;
	}

	@Override
	@Retryable(
		retryFor = { IdentityProviderException.class },
		maxAttempts = 3,
		backoff = @Backoff(delay = 200, multiplier = 2)
	)
	public TokenPair passwordGrant(String username, String password) {
		MultiValueMap<String, String> form = baseForm();
		form.add("grant_type", "password");
		form.add("username", username);
		form.add("password", password);
		return exchange(form);
	}

	@Override
	@Retryable(
		retryFor = { IdentityProviderException.class },
		maxAttempts = 3,
		backoff = @Backoff(delay = 200, multiplier = 2)
	)
	public TokenPair refreshGrant(String refreshToken) {
		MultiValueMap<String, String> form = baseForm();
		form.add("grant_type", "refresh_token");
		form.add("refresh_token", refreshToken);
		return exchange(form);
	}

	private MultiValueMap<String, String> baseForm() {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("client_id", properties.getKeycloak().getClientId());
		form.add("client_secret", properties.getKeycloak().getClientSecret());
		return form;
	}

	@SuppressWarnings("unchecked")
	private TokenPair exchange(MultiValueMap<String, String> form) {
		try {
			Map<String, Object> body = restClient.post()
				.uri(properties.getKeycloak().tokenUrl())
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(form)
				.retrieve()
				.body(Map.class);
			if (body == null) {
				throw new IdentityProviderException("Empty token response from identity provider");
			}
			return new TokenPair(
				String.valueOf(body.get("access_token")),
				String.valueOf(body.get("refresh_token")),
				String.valueOf(body.getOrDefault("token_type", "Bearer")),
				((Number) body.getOrDefault("expires_in", 300)).longValue()
			);
		}
		catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.BadRequest ex) {
			throw new InvalidCredentialsException("Invalid credentials");
		}
		catch (HttpClientErrorException ex) {
			if (ex.getStatusCode().is4xxClientError()) {
				throw new InvalidCredentialsException("Invalid credentials");
			}
			throw new IdentityProviderException("Identity provider request failed");
		}
		catch (InvalidCredentialsException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new IdentityProviderException("Identity provider unavailable");
		}
	}
}

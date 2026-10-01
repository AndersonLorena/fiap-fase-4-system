package com.al.fiap.cs.dealership.adapters.drivens.buyervalidation;

import com.al.fiap.cs.dealership.adapters.config.DealershipProperties;
import com.al.fiap.cs.dealership.core.domain.exceptions.BuyerValidationUnavailableException;
import com.al.fiap.cs.dealership.ports.buyervalidation.BuyerValidationPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class AccountApiBuyerValidationAdapter implements BuyerValidationPort {

	private final RestClient restClient;
	private final DealershipProperties properties;
	private final AtomicReference<CachedToken> cachedToken = new AtomicReference<>();

	public AccountApiBuyerValidationAdapter(
			RestClient.Builder restClientBuilder,
			DealershipProperties properties) {
		this.restClient = restClientBuilder.build();
		this.properties = properties;
	}

	@Override
	@Retryable(
		retryFor = { BuyerValidationUnavailableException.class },
		maxAttempts = 3,
		backoff = @Backoff(delay = 200, multiplier = 2)
	)
	public Optional<BuyerValidation> validate(Long accountId) {
		String token = serviceAccountToken();
		String url = properties.getAccountApi().getBaseUrl()
			+ "/api/v1/internal/accounts/" + accountId + "/buyer-validation";
		try {
			var response = restClient.get()
				.uri(url)
				.header("Authorization", "Bearer " + token)
				.retrieve()
				.toEntity(Map.class);
			if (response.getStatusCode() == HttpStatus.NO_CONTENT || response.getBody() == null) {
				return Optional.empty();
			}
			Map<String, Object> body = response.getBody();
			return Optional.of(new BuyerValidation(
				toLong(body.get("accountId"), accountId),
				String.valueOf(body.getOrDefault("status", "UNKNOWN")),
				Boolean.TRUE.equals(body.get("eligible")),
				cpf(body.get("cpf"))
			));
		}
		catch (HttpClientErrorException ex) {
			if (ex.getStatusCode() == HttpStatus.NO_CONTENT || ex.getStatusCode() == HttpStatus.NOT_FOUND) {
				return Optional.empty();
			}
			throw new BuyerValidationUnavailableException("Buyer validation failed with status " + ex.getStatusCode());
		}
		catch (BuyerValidationUnavailableException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new BuyerValidationUnavailableException("Buyer validation service unavailable");
		}
	}

	@SuppressWarnings("unchecked")
	private String serviceAccountToken() {
		CachedToken current = cachedToken.get();
		if (current != null && current.expiresAt().isAfter(Instant.now().plusSeconds(30))) {
			return current.token();
		}
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("grant_type", "client_credentials");
		form.add("client_id", properties.getKeycloak().getClientId());
		form.add("client_secret", properties.getKeycloak().getClientSecret());
		try {
			Map<String, Object> body = restClient.post()
				.uri(properties.getKeycloak().tokenUrl())
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(form)
				.retrieve()
				.body(Map.class);
			if (body == null || body.get("access_token") == null) {
				throw new BuyerValidationUnavailableException("Unable to obtain service account token");
			}
			long expiresIn = ((Number) body.getOrDefault("expires_in", 60)).longValue();
			String token = String.valueOf(body.get("access_token"));
			cachedToken.set(new CachedToken(token, Instant.now().plusSeconds(expiresIn)));
			return token;
		}
		catch (BuyerValidationUnavailableException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new BuyerValidationUnavailableException("Unable to obtain service account token");
		}
	}

	private static String cpf(Object value) {
		if (value == null) {
			return null;
		}
		String cpf = String.valueOf(value).trim();
		if (cpf.isEmpty() || "null".equals(cpf)) {
			return null;
		}
		return cpf;
	}

	private static Long toLong(Object value, Long fallback) {
		if (value instanceof Number number) {
			return number.longValue();
		}
		if (value instanceof String str) {
			try {
				return Long.parseLong(str);
			}
			catch (NumberFormatException ignored) {
				return fallback;
			}
		}
		return fallback;
	}

	private record CachedToken(String token, Instant expiresAt) {
	}
}

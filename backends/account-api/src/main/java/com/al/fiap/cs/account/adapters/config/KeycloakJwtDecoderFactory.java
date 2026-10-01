package com.al.fiap.cs.account.adapters.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

final class KeycloakJwtDecoderFactory {

	private static final Logger log = LoggerFactory.getLogger(KeycloakJwtDecoderFactory.class);

	private KeycloakJwtDecoderFactory() {
	}

	static JwtDecoder create(String issuerUri, String jwkSetUri, AccountProperties.Keycloak.Jwks jwks) {
		RestTemplate restTemplate = jwksRestTemplate(jwks);
		int maxAttempts = jwks.getRetryAttempts();
		RuntimeException lastFailure = null;
		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			try {
				NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
					.restOperations(restTemplate)
					.build();
				decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuerUri));
				log.info("JwtDecoder initialized from jwkSetUri={}", jwkSetUri);
				return decoder;
			}
			catch (RuntimeException ex) {
				lastFailure = ex;
				log.warn(
					"JWKS fetch failed on attempt {}/{} for jwkSetUri={}: {}",
					attempt,
					maxAttempts,
					jwkSetUri,
					ex.getMessage()
				);
				if (attempt < maxAttempts) {
					sleep(jwks.getRetryBackoff());
				}
			}
		}
		if (lastFailure == null) {
			throw new IllegalStateException("JWKS fetch failed without a captured exception");
		}
		throw lastFailure;
	}

	private static RestTemplate jwksRestTemplate(AccountProperties.Keycloak.Jwks jwks) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(jwks.getConnectTimeout());
		requestFactory.setReadTimeout(jwks.getReadTimeout());
		return new RestTemplate(requestFactory);
	}

	private static void sleep(Duration backoff) {
		try {
			Thread.sleep(backoff.toMillis());
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while retrying JWKS fetch", ex);
		}
	}
}

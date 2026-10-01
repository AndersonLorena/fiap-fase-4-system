package com.al.fiap.cs.account.adapters.drivens.email;

import com.al.fiap.cs.account.adapters.config.AccountProperties;
import com.al.fiap.cs.account.ports.email.EmailPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.client.RestClient;

import java.util.Map;

public class ResendEmailAdapter implements EmailPort {

	private static final Logger log = LoggerFactory.getLogger(ResendEmailAdapter.class);

	private final RestClient restClient;
	private final AccountProperties properties;

	public ResendEmailAdapter(RestClient.Builder restClientBuilder, AccountProperties properties) {
		this.restClient = restClientBuilder.build();
		this.properties = properties;
	}

	@Override
	@Retryable(maxAttempts = 3, backoff = @Backoff(delay = 200, multiplier = 2))
	public void sendPasswordRecoveryCode(String toEmail, String code, String recoveryUrl) {
		if (properties.getResend().getApiKey() == null || properties.getResend().getApiKey().isBlank()) {
			log.warn("Resend API key is not configured; password recovery email skipped");
			return;
		}
		try {
			restClient.post()
				.uri(properties.getResend().getBaseUrl() + "/emails")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + properties.getResend().getApiKey())
				.body(Map.of(
					"from", properties.getResend().getFrom(),
					"to", java.util.List.of(toEmail),
					"subject", "Password recovery code",
					"text", "Your recovery code is " + code + ". Open " + recoveryUrl + " to continue."
				))
				.retrieve()
				.toBodilessEntity();
		}
		catch (Exception ex) {
			log.error("Failed to send password recovery email");
			throw ex;
		}
	}
}

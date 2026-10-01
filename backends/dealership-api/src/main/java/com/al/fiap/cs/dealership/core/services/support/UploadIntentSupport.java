package com.al.fiap.cs.dealership.core.services.support;

import com.al.fiap.cs.dealership.core.domain.exceptions.InvalidUploadIntentException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;

public class UploadIntentSupport {

	private static final String HMAC_ALGORITHM = "HmacSHA256";

	private final String secret;

	public UploadIntentSupport(String secret) {
		this.secret = Objects.requireNonNull(secret, "secret");
		if (secret.isBlank()) {
			throw new IllegalArgumentException("Upload intent secret must not be blank");
		}
	}

	public String issue(String objectKey, Long carId, Duration ttl) {
		long exp = Instant.now().plus(ttl).getEpochSecond();
		String signature = sign(objectKey, carId, exp);
		return exp + ":" + signature;
	}

	public void verify(String intentToken, String objectKey, Long carId) {
		if (intentToken == null || intentToken.isBlank()) {
			throw new InvalidUploadIntentException("Upload intent token is required");
		}
		int separator = intentToken.indexOf(':');
		if (separator <= 0 || separator == intentToken.length() - 1) {
			throw new InvalidUploadIntentException("Upload intent token is malformed");
		}
		long exp;
		try {
			exp = Long.parseLong(intentToken.substring(0, separator));
		}
		catch (NumberFormatException ex) {
			throw new InvalidUploadIntentException("Upload intent token is malformed");
		}
		if (Instant.ofEpochSecond(exp).isBefore(Instant.now())) {
			throw new InvalidUploadIntentException("Upload intent token expired");
		}
		String expectedSignature = sign(objectKey, carId, exp);
		String providedSignature = intentToken.substring(separator + 1);
		if (!constantTimeEquals(expectedSignature, providedSignature)) {
			throw new InvalidUploadIntentException("Upload intent signature mismatch");
		}
	}

	private String sign(String objectKey, Long carId, long exp) {
		String payload = objectKey + "|" + carId + "|" + exp;
		try {
			Mac mac = Mac.getInstance(HMAC_ALGORITHM);
			mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
			byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
			return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
		}
		catch (Exception ex) {
			throw new InvalidUploadIntentException("Unable to compute upload intent signature");
		}
	}

	private static boolean constantTimeEquals(String a, String b) {
		if (a.length() != b.length()) {
			return false;
		}
		int result = 0;
		for (int i = 0; i < a.length(); i++) {
			result |= a.charAt(i) ^ b.charAt(i);
		}
		return result == 0;
	}
}

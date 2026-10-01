package com.al.fiap.cs.account.core.services.support;

import com.al.fiap.cs.account.core.domain.exceptions.AuthenticationThrottledException;
import com.al.fiap.cs.account.ports.repositories.AuthenticationThrottleRepositoryPort;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;

public class AuthenticationThrottleGuard {

	private final AuthenticationThrottleRepositoryPort throttleRepository;
	private final Duration window;
	private final int maxAttempts;

	public AuthenticationThrottleGuard(
			AuthenticationThrottleRepositoryPort throttleRepository,
			Duration window,
			int maxAttempts) {
		this.throttleRepository = throttleRepository;
		this.window = window;
		this.maxAttempts = maxAttempts;
	}

	public void ensureAllowed(String operation, String subject) {
		Instant now = Instant.now();
		Instant windowStartedAt = now.minus(window);
		int attempts = throttleRepository.findAttempts(operation, hash(subject), windowStartedAt).orElse(0);
		if (attempts >= maxAttempts) {
			throw new AuthenticationThrottledException("Too many attempts");
		}
	}

	public void recordFailure(String operation, String subject) {
		Instant now = Instant.now();
		Instant windowStartedAt = now.minus(window);
		int attempts = throttleRepository.incrementAndGetAttempts(
			operation,
			hash(subject),
			windowStartedAt,
			now
		);
		if (attempts > maxAttempts) {
			throw new AuthenticationThrottledException("Too many attempts");
		}
	}

	public void reset(String operation, String subject) {
		Instant now = Instant.now();
		throttleRepository.reset(operation, hash(subject), now.minus(window));
	}

	/**
	 * Records an attempt for abuse-sensitive flows that always count (e.g. password recovery).
	 */
	public void guard(String operation, String subject) {
		ensureAllowed(operation, subject);
		recordFailure(operation, subject);
	}

	private static String hash(String subject) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashed = digest.digest(subject.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hashed);
		}
		catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 unavailable");
		}
	}
}

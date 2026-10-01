package com.al.fiap.cs.account.core.services.support;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

public final class RecoveryCodeSupport {

	private static final SecureRandom RANDOM = new SecureRandom();

	private RecoveryCodeSupport() {
	}

	public static String generateCode() {
		int value = RANDOM.nextInt(1_000_000);
		return String.format("%06d", value);
	}

	public static String hash(String code) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashed = digest.digest(code.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hashed);
		}
		catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 unavailable");
		}
	}

	public static boolean matches(String code, String codeHash) {
		return hash(code).equals(codeHash);
	}
}

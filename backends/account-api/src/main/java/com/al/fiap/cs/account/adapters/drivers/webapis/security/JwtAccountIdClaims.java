package com.al.fiap.cs.account.adapters.drivers.webapis.security;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;

public final class JwtAccountIdClaims {

	private JwtAccountIdClaims() {
	}

	public static Long parseAccountId(Jwt jwt) {
		return parseAccountIdValue(jwt.getClaim("accountId"));
	}

	static Long parseAccountIdValue(Object claim) {
		if (claim == null) {
			return null;
		}
		if (claim instanceof Number number) {
			return number.longValue();
		}
		if (claim instanceof String text) {
			if (text.isBlank()) {
				return null;
			}
			return Long.valueOf(text.trim());
		}
		if (claim instanceof Collection<?> values) {
			if (values.isEmpty()) {
				return null;
			}
			return parseAccountIdValue(values.iterator().next());
		}
		String text = String.valueOf(claim).trim();
		if (text.isBlank() || "null".equals(text)) {
			return null;
		}
		return Long.valueOf(text);
	}
}

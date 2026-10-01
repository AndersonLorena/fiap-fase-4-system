package com.al.fiap.cs.account.ports.identity;

public interface IdentityTokenPort {

	TokenPair passwordGrant(String username, String password);

	TokenPair refreshGrant(String refreshToken);

	record TokenPair(
			String accessToken,
			String refreshToken,
			String tokenType,
			long expiresIn
	) {
	}
}

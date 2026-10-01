package com.al.fiap.cs.account.core.services;

import com.al.fiap.cs.account.core.domain.exceptions.InvalidCredentialsException;
import com.al.fiap.cs.account.core.services.support.AuthenticationThrottleGuard;
import com.al.fiap.cs.account.ports.identity.IdentityTokenPort;
import com.al.fiap.cs.account.ports.services.RefreshTokenServicePort;
import com.al.fiap.cs.account.ports.services.dtos.request.RefreshTokenRequest;
import com.al.fiap.cs.account.ports.services.dtos.response.TokenResponse;

public class RefreshTokenService implements RefreshTokenServicePort {

	private final IdentityTokenPort identityTokenPort;
	private final AuthenticationThrottleGuard throttleGuard;

	public RefreshTokenService(IdentityTokenPort identityTokenPort, AuthenticationThrottleGuard throttleGuard) {
		this.identityTokenPort = identityTokenPort;
		this.throttleGuard = throttleGuard;
	}

	@Override
	public TokenResponse refresh(RefreshTokenRequest request) {
		throttleGuard.ensureAllowed("REFRESH", request.refreshToken());
		try {
			IdentityTokenPort.TokenPair tokens = identityTokenPort.refreshGrant(request.refreshToken());
			throttleGuard.reset("REFRESH", request.refreshToken());
			return new TokenResponse(
				tokens.accessToken(),
				tokens.refreshToken(),
				tokens.tokenType(),
				tokens.expiresIn()
			);
		}
		catch (InvalidCredentialsException ex) {
			throttleGuard.recordFailure("REFRESH", request.refreshToken());
			throw ex;
		}
	}
}

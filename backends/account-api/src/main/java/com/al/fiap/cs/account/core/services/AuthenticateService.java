package com.al.fiap.cs.account.core.services;

import com.al.fiap.cs.account.core.domain.account.EmailAddress;
import com.al.fiap.cs.account.core.domain.exceptions.InvalidCredentialsException;
import com.al.fiap.cs.account.core.services.support.AuthenticationThrottleGuard;
import com.al.fiap.cs.account.ports.identity.IdentityTokenPort;
import com.al.fiap.cs.account.ports.services.AuthenticateServicePort;
import com.al.fiap.cs.account.ports.services.dtos.request.AuthenticateRequest;
import com.al.fiap.cs.account.ports.services.dtos.response.TokenResponse;

public class AuthenticateService implements AuthenticateServicePort {

	private final IdentityTokenPort identityTokenPort;
	private final AuthenticationThrottleGuard throttleGuard;

	public AuthenticateService(IdentityTokenPort identityTokenPort, AuthenticationThrottleGuard throttleGuard) {
		this.identityTokenPort = identityTokenPort;
		this.throttleGuard = throttleGuard;
	}

	@Override
	public TokenResponse authenticate(AuthenticateRequest request) {
		EmailAddress email = new EmailAddress(request.email());
		throttleGuard.ensureAllowed("LOGIN", email.value());
		try {
			IdentityTokenPort.TokenPair tokens = identityTokenPort.passwordGrant(email.value(), request.password());
			throttleGuard.reset("LOGIN", email.value());
			return new TokenResponse(
				tokens.accessToken(),
				tokens.refreshToken(),
				tokens.tokenType(),
				tokens.expiresIn()
			);
		}
		catch (InvalidCredentialsException ex) {
			throttleGuard.recordFailure("LOGIN", email.value());
			throw ex;
		}
	}
}

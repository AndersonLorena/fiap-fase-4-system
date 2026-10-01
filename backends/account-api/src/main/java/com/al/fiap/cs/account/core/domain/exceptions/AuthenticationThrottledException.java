package com.al.fiap.cs.account.core.domain.exceptions;

public final class AuthenticationThrottledException extends DomainException {
	public AuthenticationThrottledException(String message) {
		super(message);
	}
}

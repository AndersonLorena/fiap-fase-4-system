package com.al.fiap.cs.account.core.domain.exceptions;

public final class AccountAlreadyValidatedException extends DomainException {
	public AccountAlreadyValidatedException(String message) {
		super(message);
	}
}

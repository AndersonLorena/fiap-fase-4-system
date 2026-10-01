package com.al.fiap.cs.account.core.domain.exceptions;

public final class AccountAlreadyExistsException extends DomainException {
	public AccountAlreadyExistsException(String message) {
		super(message);
	}
}

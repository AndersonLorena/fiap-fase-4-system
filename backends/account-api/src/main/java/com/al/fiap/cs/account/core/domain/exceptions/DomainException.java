package com.al.fiap.cs.account.core.domain.exceptions;

public abstract class DomainException extends RuntimeException {

	protected DomainException(String message) {
		super(message);
	}
}

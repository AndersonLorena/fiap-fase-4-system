package com.al.fiap.cs.dealership.core.domain.exceptions;

public final class CarNotFoundException extends DomainException {
	public CarNotFoundException(String message) {
		super(message);
	}
}

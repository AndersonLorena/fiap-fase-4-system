package com.al.fiap.cs.account.core.domain.account;

import com.al.fiap.cs.account.core.domain.exceptions.InvalidPhoneException;

import java.util.Objects;

public record Phone(String value) {

	public Phone {
		Objects.requireNonNull(value, "phone");
		String normalized = value.trim().replaceAll("[^0-9+]", "");
		if (normalized.length() < 10 || normalized.length() > 16) {
			throw new InvalidPhoneException("Phone must contain 10 to 16 digits");
		}
		value = normalized;
	}
}

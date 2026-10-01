package com.al.fiap.cs.account.core.domain.account;

import com.al.fiap.cs.account.core.domain.exceptions.InvalidFullNameException;

import java.util.Objects;

public record FullName(String value) {

	public FullName {
		Objects.requireNonNull(value, "fullName");
		String normalized = value.trim();
		if (normalized.length() < 2 || normalized.length() > 120) {
			throw new InvalidFullNameException("Full name must be between 2 and 120 characters");
		}
		value = normalized;
	}
}

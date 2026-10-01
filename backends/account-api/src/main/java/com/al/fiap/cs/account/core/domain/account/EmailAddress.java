package com.al.fiap.cs.account.core.domain.account;

import com.al.fiap.cs.account.core.domain.exceptions.InvalidEmailException;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public record EmailAddress(String value) {

	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

	public EmailAddress {
		Objects.requireNonNull(value, "email");
		String normalized = value.trim().toLowerCase(Locale.ROOT);
		if (normalized.isBlank() || !EMAIL_PATTERN.matcher(normalized).matches()) {
			throw new InvalidEmailException("Invalid email address");
		}
		value = normalized;
	}
}

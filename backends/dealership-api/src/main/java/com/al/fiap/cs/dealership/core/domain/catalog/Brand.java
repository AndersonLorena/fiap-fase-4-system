package com.al.fiap.cs.dealership.core.domain.catalog;

import com.al.fiap.cs.dealership.core.domain.exceptions.InvalidNameException;
import com.al.fiap.cs.dealership.core.domain.shared.BaseEntity;

import java.time.Instant;
import java.util.Objects;

public final class Brand extends BaseEntity {

	private String name;

	private Brand(Long userId, String name) {
		super(userId);
		this.name = validate(name);
	}

	private Brand(
			Long id,
			Instant createdAt,
			Long createdBy,
			Instant updatedAt,
			Long updatedBy,
			String name) {
		super(id, createdAt, createdBy, updatedAt, updatedBy);
		this.name = validate(name);
	}

	public static Brand create(String name, Long userId) {
		return new Brand(userId, name);
	}

	public static Brand restore(
			Long id,
			Instant createdAt,
			Long createdBy,
			Instant updatedAt,
			Long updatedBy,
			String name) {
		return new Brand(id, createdAt, createdBy, updatedAt, updatedBy, name);
	}

	public void rename(String newName, Long userId) {
		this.name = validate(newName);
		updateAudit(userId);
	}

	public String name() {
		return name;
	}

	private static String validate(String name) {
		Objects.requireNonNull(name, "name");
		String trimmed = name.trim();
		if (trimmed.isEmpty() || trimmed.length() > 80) {
			throw new InvalidNameException("Brand name must be between 1 and 80 characters");
		}
		return trimmed;
	}
}

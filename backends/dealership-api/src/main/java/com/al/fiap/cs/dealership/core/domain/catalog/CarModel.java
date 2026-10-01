package com.al.fiap.cs.dealership.core.domain.catalog;

import com.al.fiap.cs.dealership.core.domain.exceptions.InvalidNameException;
import com.al.fiap.cs.dealership.core.domain.shared.BaseEntity;

import java.time.Instant;
import java.util.Objects;

public final class CarModel extends BaseEntity {

	private final Long brandId;
	private String name;

	private CarModel(Long userId, Long brandId, String name) {
		super(userId);
		this.brandId = Objects.requireNonNull(brandId, "brandId");
		this.name = validate(name);
	}

	private CarModel(
			Long id,
			Instant createdAt,
			Long createdBy,
			Instant updatedAt,
			Long updatedBy,
			Long brandId,
			String name) {
		super(id, createdAt, createdBy, updatedAt, updatedBy);
		this.brandId = Objects.requireNonNull(brandId, "brandId");
		this.name = validate(name);
	}

	public static CarModel create(Long brandId, String name, Long userId) {
		return new CarModel(userId, brandId, name);
	}

	public static CarModel restore(
			Long id,
			Instant createdAt,
			Long createdBy,
			Instant updatedAt,
			Long updatedBy,
			Long brandId,
			String name) {
		return new CarModel(id, createdAt, createdBy, updatedAt, updatedBy, brandId, name);
	}

	public void rename(String newName, Long userId) {
		this.name = validate(newName);
		updateAudit(userId);
	}

	public Long brandId() {
		return brandId;
	}

	public String name() {
		return name;
	}

	private static String validate(String name) {
		Objects.requireNonNull(name, "name");
		String trimmed = name.trim();
		if (trimmed.isEmpty() || trimmed.length() > 80) {
			throw new InvalidNameException("Model name must be between 1 and 80 characters");
		}
		return trimmed;
	}
}

package com.al.fiap.cs.dealership.core.domain.car;

import java.util.Objects;

public record CarPhoto(String objectKey, int sortOrder) {

	public CarPhoto {
		Objects.requireNonNull(objectKey, "objectKey");
		if (objectKey.isBlank()) {
			throw new IllegalArgumentException("objectKey must not be blank");
		}
		if (sortOrder < 0) {
			throw new IllegalArgumentException("sortOrder must be non-negative");
		}
	}
}

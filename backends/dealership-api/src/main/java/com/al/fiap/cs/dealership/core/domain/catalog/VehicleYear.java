package com.al.fiap.cs.dealership.core.domain.catalog;

import com.al.fiap.cs.dealership.core.domain.exceptions.InvalidVehicleYearException;
import com.al.fiap.cs.dealership.core.domain.shared.BaseEntity;

import java.time.Instant;

public final class VehicleYear extends BaseEntity {

	private int year;

	private VehicleYear(Long userId, int year) {
		super(userId);
		this.year = validate(year);
	}

	private VehicleYear(
			Long id,
			Instant createdAt,
			Long createdBy,
			Instant updatedAt,
			Long updatedBy,
			int year) {
		super(id, createdAt, createdBy, updatedAt, updatedBy);
		this.year = validate(year);
	}

	public static VehicleYear create(int year, Long userId) {
		return new VehicleYear(userId, year);
	}

	public static VehicleYear restore(
			Long id,
			Instant createdAt,
			Long createdBy,
			Instant updatedAt,
			Long updatedBy,
			int year) {
		return new VehicleYear(id, createdAt, createdBy, updatedAt, updatedBy, year);
	}

	public void rename(int newYear, Long userId) {
		this.year = validate(newYear);
		updateAudit(userId);
	}

	public int year() {
		return year;
	}

	private static int validate(int year) {
		if (year < 1900 || year > 2100) {
			throw new InvalidVehicleYearException("Vehicle year must be between 1900 and 2100");
		}
		return year;
	}
}

package com.al.fiap.cs.dealership.ports.services.dtos.request;

import com.al.fiap.cs.dealership.core.domain.car.CarStatus;

public record SearchCarsRequest(
		CarStatus status,
		String query,
		int page,
		int size,
		String sortField,
		String sortDirection) {
}

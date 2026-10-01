package com.al.fiap.cs.dealership.ports.services.dtos.response;

import java.util.List;

public record PagedCarsResponse(
		List<CarResponse> content,
		int page,
		int size,
		long totalElements,
		int totalPages
) {
}

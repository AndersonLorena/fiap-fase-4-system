package com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.response;

import java.util.List;

public record PagedCarsWebResponse(
		List<CarWebResponse> content,
		int page,
		int size,
		long totalElements,
		int totalPages
) {
}

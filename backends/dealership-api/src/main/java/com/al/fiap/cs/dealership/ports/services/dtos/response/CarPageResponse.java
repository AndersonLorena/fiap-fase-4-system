package com.al.fiap.cs.dealership.ports.services.dtos.response;

import java.util.List;

public record CarPageResponse(
		List<CarResponse> items,
		long totalElements,
		int page,
		int size) {
}

package com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.response;

import java.util.List;

public record CarPageWebResponse(
		List<CarWebResponse> items,
		long totalElements,
		int page,
		int size) {
}

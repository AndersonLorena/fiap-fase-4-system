package com.al.fiap.cs.dealership.ports.services.dtos.request;

import java.math.BigDecimal;

public record UpdateCarRequest(
		Long carId,
		Long brandId,
		Long modelId,
		Long colorId,
		Long yearId,
		BigDecimal price,
		Long userId) {
}

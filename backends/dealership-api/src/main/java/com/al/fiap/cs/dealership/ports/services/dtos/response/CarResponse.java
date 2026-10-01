package com.al.fiap.cs.dealership.ports.services.dtos.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CarResponse(
		Long carId,
		Long brandId,
		String brandName,
		Long modelId,
		String modelName,
		Long colorId,
		String colorName,
		Long yearId,
		int yearValue,
		BigDecimal price,
		String status,
		Long buyerAccountId,
		String buyerCpf,
		String paymentCode,
		Instant soldAt,
		List<CarPhotoResponse> photos) {
}

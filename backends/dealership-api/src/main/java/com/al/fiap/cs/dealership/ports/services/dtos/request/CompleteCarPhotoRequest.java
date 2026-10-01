package com.al.fiap.cs.dealership.ports.services.dtos.request;

public record CompleteCarPhotoRequest(
		Long carId,
		String objectKey,
		String intentToken,
		int sortOrder,
		Long userId) {
}

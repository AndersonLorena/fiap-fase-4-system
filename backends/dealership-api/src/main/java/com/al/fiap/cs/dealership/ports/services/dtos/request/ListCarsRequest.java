package com.al.fiap.cs.dealership.ports.services.dtos.request;

public record ListCarsRequest(
		String status,
		String query,
		int page,
		int size,
		String sort
) {
}

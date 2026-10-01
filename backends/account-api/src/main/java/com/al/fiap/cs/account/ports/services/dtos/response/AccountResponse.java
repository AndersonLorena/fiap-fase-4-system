package com.al.fiap.cs.account.ports.services.dtos.response;

public record AccountResponse(
		Long accountId,
		String email,
		String fullName,
		String document,
		String phone,
		String status
) {
}

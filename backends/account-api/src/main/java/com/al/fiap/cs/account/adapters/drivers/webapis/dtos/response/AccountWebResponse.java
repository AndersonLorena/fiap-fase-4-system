package com.al.fiap.cs.account.adapters.drivers.webapis.dtos.response;

public record AccountWebResponse(
		Long accountId,
		String email,
		String fullName,
		String document,
		String phone,
		String status
) {
}

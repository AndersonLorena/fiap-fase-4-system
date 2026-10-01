package com.al.fiap.cs.account.ports.services.dtos.request;

import com.al.fiap.cs.account.core.domain.account.AccountType;

public record CreateAccountRequest(
		String email,
		String password,
		String fullName,
		AccountType type,
		boolean requesterIsAdmin
) {
}

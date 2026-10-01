package com.al.fiap.cs.account.core.services.support;

import com.al.fiap.cs.account.core.domain.account.Account;
import com.al.fiap.cs.account.ports.services.dtos.response.AccountResponse;

public final class AccountResponses {

	private AccountResponses() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static AccountResponse from(Account account) {
		return new AccountResponse(
			account.id(),
			account.email().value(),
			account.fullName().value(),
			account.document() == null ? null : account.document().value(),
			account.phone() == null ? null : account.phone().value(),
			account.status().name()
		);
	}
}

package com.al.fiap.cs.account.core.domain.account;

import com.al.fiap.cs.account.core.domain.exceptions.AccountTypeNotAllowedException;

public enum AccountType {
	ADMIN,
	CUSTOMER;

	public static AccountType resolveForCreation(AccountType requested, boolean requesterIsAdmin) {
		AccountType effective = requested == null ? CUSTOMER : requested;
		if (effective == ADMIN && !requesterIsAdmin) {
			throw new AccountTypeNotAllowedException("Only an admin can create admin accounts");
		}
		return effective;
	}

	public String realmRoleName() {
		return name();
	}
}

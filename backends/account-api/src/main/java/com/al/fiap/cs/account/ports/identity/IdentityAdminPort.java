package com.al.fiap.cs.account.ports.identity;

import com.al.fiap.cs.account.core.domain.account.AccountType;

public interface IdentityAdminPort {

	String createUser(CreateIdentityUserCommand command);

	void resetPassword(String keycloakUserId, String newPassword);

	void logoutSessions(String keycloakUserId);

	void deleteUser(String keycloakUserId);

	record CreateIdentityUserCommand(
			String accountId,
			String email,
			String fullName,
			String password,
			AccountType type
	) {
	}
}

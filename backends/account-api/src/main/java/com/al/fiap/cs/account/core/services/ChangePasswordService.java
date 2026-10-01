package com.al.fiap.cs.account.core.services;

import com.al.fiap.cs.account.core.domain.account.Account;
import com.al.fiap.cs.account.core.domain.exceptions.AccountNotFoundException;
import com.al.fiap.cs.account.ports.identity.IdentityAdminPort;
import com.al.fiap.cs.account.ports.identity.IdentityTokenPort;
import com.al.fiap.cs.account.ports.lock.AccountLockPort;
import com.al.fiap.cs.account.ports.repositories.AccountRepositoryPort;
import com.al.fiap.cs.account.ports.services.ChangePasswordServicePort;
import com.al.fiap.cs.account.ports.services.dtos.request.ChangePasswordRequest;

public class ChangePasswordService implements ChangePasswordServicePort {

	private final AccountRepositoryPort accountRepository;
	private final IdentityTokenPort identityTokenPort;
	private final IdentityAdminPort identityAdminPort;
	private final AccountLockPort accountLockPort;

	public ChangePasswordService(
			AccountRepositoryPort accountRepository,
			IdentityTokenPort identityTokenPort,
			IdentityAdminPort identityAdminPort,
			AccountLockPort accountLockPort) {
		this.accountRepository = accountRepository;
		this.identityTokenPort = identityTokenPort;
		this.identityAdminPort = identityAdminPort;
		this.accountLockPort = accountLockPort;
	}

	@Override
	public void changePassword(ChangePasswordRequest request) {
		Account account = accountRepository.findById(request.accountId())
			.orElseThrow(() -> new AccountNotFoundException("Account not found"));
		AccountLockPort.LockHandle lock = accountLockPort.acquire(account.id());
		try {
			identityTokenPort.passwordGrant(account.email().value(), request.currentPassword());
			identityAdminPort.resetPassword(account.keycloakUserId(), request.newPassword());
			identityAdminPort.logoutSessions(account.keycloakUserId());
		}
		finally {
			accountLockPort.release(lock);
		}
	}
}

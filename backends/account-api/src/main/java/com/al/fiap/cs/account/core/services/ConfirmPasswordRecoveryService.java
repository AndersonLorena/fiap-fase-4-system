package com.al.fiap.cs.account.core.services;

import com.al.fiap.cs.account.core.domain.account.Account;
import com.al.fiap.cs.account.core.domain.account.EmailAddress;
import com.al.fiap.cs.account.core.domain.exceptions.InvalidRecoveryCodeException;
import com.al.fiap.cs.account.core.services.support.RecoveryCodeSupport;
import com.al.fiap.cs.account.ports.identity.IdentityAdminPort;
import com.al.fiap.cs.account.ports.lock.AccountLockPort;
import com.al.fiap.cs.account.ports.repositories.AccountRepositoryPort;
import com.al.fiap.cs.account.ports.repositories.PasswordRecoveryTokenRepositoryPort;
import com.al.fiap.cs.account.ports.services.ConfirmPasswordRecoveryServicePort;
import com.al.fiap.cs.account.ports.services.dtos.request.ConfirmPasswordRecoveryRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

public class ConfirmPasswordRecoveryService implements ConfirmPasswordRecoveryServicePort {

	private final AccountRepositoryPort accountRepository;
	private final PasswordRecoveryTokenRepositoryPort recoveryTokenRepository;
	private final IdentityAdminPort identityAdminPort;
	private final AccountLockPort accountLockPort;

	public ConfirmPasswordRecoveryService(
			AccountRepositoryPort accountRepository,
			PasswordRecoveryTokenRepositoryPort recoveryTokenRepository,
			IdentityAdminPort identityAdminPort,
			AccountLockPort accountLockPort) {
		this.accountRepository = accountRepository;
		this.recoveryTokenRepository = recoveryTokenRepository;
		this.identityAdminPort = identityAdminPort;
		this.accountLockPort = accountLockPort;
	}

	@Override
	@Transactional
	public void confirm(ConfirmPasswordRecoveryRequest request) {
		EmailAddress email = new EmailAddress(request.email());
		Account account = accountRepository.findByEmail(email)
			.orElseThrow(() -> new InvalidRecoveryCodeException("Invalid recovery code"));

		AccountLockPort.LockHandle lock = accountLockPort.acquire(account.id());
		try {
			var token = recoveryTokenRepository.findActiveByAccountId(account.id())
				.orElseThrow(() -> new InvalidRecoveryCodeException("Invalid recovery code"));
			if (!RecoveryCodeSupport.matches(request.code(), token.codeHash())) {
				throw new InvalidRecoveryCodeException("Invalid recovery code");
			}
			identityAdminPort.resetPassword(account.keycloakUserId(), request.newPassword());
			identityAdminPort.logoutSessions(account.keycloakUserId());
			recoveryTokenRepository.markUsed(token.id(), Instant.now());
			recoveryTokenRepository.invalidateActiveTokens(account.id());
		}
		finally {
			accountLockPort.release(lock);
		}
	}
}

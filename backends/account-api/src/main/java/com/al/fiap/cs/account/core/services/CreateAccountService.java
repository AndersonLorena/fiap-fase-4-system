package com.al.fiap.cs.account.core.services;

import com.al.fiap.cs.account.core.domain.account.Account;
import com.al.fiap.cs.account.core.domain.account.AccountType;
import com.al.fiap.cs.account.core.domain.account.EmailAddress;
import com.al.fiap.cs.account.core.domain.account.FullName;
import com.al.fiap.cs.account.core.domain.exceptions.AccountAlreadyExistsException;
import com.al.fiap.cs.account.ports.identity.IdentityAdminPort;
import com.al.fiap.cs.account.ports.repositories.AccountRepositoryPort;
import com.al.fiap.cs.account.core.services.support.AccountResponses;
import com.al.fiap.cs.account.ports.services.CreateAccountServicePort;
import com.al.fiap.cs.account.ports.services.dtos.request.CreateAccountRequest;
import com.al.fiap.cs.account.ports.services.dtos.response.AccountResponse;
import org.springframework.transaction.annotation.Transactional;

public class CreateAccountService implements CreateAccountServicePort {

	private final AccountRepositoryPort accountRepository;
	private final IdentityAdminPort identityAdminPort;

	public CreateAccountService(AccountRepositoryPort accountRepository, IdentityAdminPort identityAdminPort) {
		this.accountRepository = accountRepository;
		this.identityAdminPort = identityAdminPort;
	}

	@Override
	@Transactional
	public AccountResponse create(CreateAccountRequest request) {
		AccountType effectiveType = AccountType.resolveForCreation(request.type(), request.requesterIsAdmin());
		EmailAddress email = new EmailAddress(request.email());
		FullName fullName = new FullName(request.fullName());
		if (accountRepository.existsByEmail(email)) {
			throw new AccountAlreadyExistsException("Account email already exists");
		}

		Account account = Account.create(email, fullName);
		String keycloakUserId = null;
		try {
			keycloakUserId = identityAdminPort.createUser(
				new IdentityAdminPort.CreateIdentityUserCommand(
					String.valueOf(account.id()),
					email.value(),
					fullName.value(),
					request.password(),
					effectiveType
				)
			);
			account.bindKeycloakUser(keycloakUserId, Account.SYSTEM_ACTOR_ID);
			accountRepository.save(account);
		}
		catch (RuntimeException ex) {
			if (keycloakUserId != null) {
				identityAdminPort.deleteUser(keycloakUserId);
			}
			throw ex;
		}

		return AccountResponses.from(account);
	}
}

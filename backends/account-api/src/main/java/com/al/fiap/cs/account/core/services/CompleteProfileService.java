package com.al.fiap.cs.account.core.services;

import com.al.fiap.cs.account.core.domain.account.Account;
import com.al.fiap.cs.account.core.domain.account.Document;
import com.al.fiap.cs.account.core.domain.account.Phone;
import com.al.fiap.cs.account.core.domain.exceptions.AccountNotFoundException;
import com.al.fiap.cs.account.core.services.support.AccountResponses;
import com.al.fiap.cs.account.ports.lock.AccountLockPort;
import com.al.fiap.cs.account.ports.repositories.AccountRepositoryPort;
import com.al.fiap.cs.account.ports.services.CompleteProfileServicePort;
import com.al.fiap.cs.account.ports.services.dtos.request.CompleteProfileRequest;
import com.al.fiap.cs.account.ports.services.dtos.response.AccountResponse;
import org.springframework.transaction.annotation.Transactional;

public class CompleteProfileService implements CompleteProfileServicePort {

	private final AccountRepositoryPort accountRepository;
	private final AccountLockPort accountLockPort;

	public CompleteProfileService(AccountRepositoryPort accountRepository, AccountLockPort accountLockPort) {
		this.accountRepository = accountRepository;
		this.accountLockPort = accountLockPort;
	}

	@Override
	@Transactional
	public AccountResponse complete(CompleteProfileRequest request) {
		Account account = accountRepository.findById(request.accountId())
			.orElseThrow(() -> new AccountNotFoundException("Account not found"));
		AccountLockPort.LockHandle lock = accountLockPort.acquire(account.id());
		try {
			account.completeProfile(
				new Document(request.document()),
				new Phone(request.phone()),
				request.accountId()
			);
			accountRepository.save(account);
			return AccountResponses.from(account);
		}
		finally {
			accountLockPort.release(lock);
		}
	}
}

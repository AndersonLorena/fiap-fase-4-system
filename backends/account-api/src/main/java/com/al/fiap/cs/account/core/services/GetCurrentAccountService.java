package com.al.fiap.cs.account.core.services;

import com.al.fiap.cs.account.core.domain.account.Account;
import com.al.fiap.cs.account.core.domain.exceptions.AccountNotFoundException;
import com.al.fiap.cs.account.core.services.support.AccountResponses;
import com.al.fiap.cs.account.ports.repositories.AccountRepositoryPort;
import com.al.fiap.cs.account.ports.services.GetCurrentAccountServicePort;
import com.al.fiap.cs.account.ports.services.dtos.request.GetCurrentAccountRequest;
import com.al.fiap.cs.account.ports.services.dtos.response.AccountResponse;
import org.springframework.transaction.annotation.Transactional;

public class GetCurrentAccountService implements GetCurrentAccountServicePort {

	private final AccountRepositoryPort accountRepository;

	public GetCurrentAccountService(AccountRepositoryPort accountRepository) {
		this.accountRepository = accountRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public AccountResponse get(GetCurrentAccountRequest request) {
		Account account = accountRepository.findById(request.accountId())
			.orElseThrow(() -> new AccountNotFoundException("Account not found"));
		return AccountResponses.from(account);
	}
}

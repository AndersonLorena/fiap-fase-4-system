package com.al.fiap.cs.account.core.services;

import com.al.fiap.cs.account.core.domain.account.Account;
import com.al.fiap.cs.account.core.domain.exceptions.AccountNotFoundException;
import com.al.fiap.cs.account.ports.repositories.AccountRepositoryPort;
import com.al.fiap.cs.account.ports.services.ValidateBuyerServicePort;
import com.al.fiap.cs.account.ports.services.dtos.request.ValidateBuyerRequest;
import com.al.fiap.cs.account.ports.services.dtos.response.BuyerValidationResponse;

public class ValidateBuyerService implements ValidateBuyerServicePort {

	private final AccountRepositoryPort accountRepository;

	public ValidateBuyerService(AccountRepositoryPort accountRepository) {
		this.accountRepository = accountRepository;
	}

	@Override
	public BuyerValidationResponse validate(ValidateBuyerRequest request) {
		Account account = accountRepository.findById(request.accountId())
			.orElseThrow(() -> new AccountNotFoundException("Account not found"));
		return new BuyerValidationResponse(
			account.id(),
			account.status().name(),
			account.isEligibleBuyer(),
			account.document() == null ? null : account.document().value()
		);
	}
}

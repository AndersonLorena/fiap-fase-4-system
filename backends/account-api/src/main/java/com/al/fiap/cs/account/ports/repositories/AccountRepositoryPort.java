package com.al.fiap.cs.account.ports.repositories;

import com.al.fiap.cs.account.core.domain.account.Account;
import com.al.fiap.cs.account.core.domain.account.EmailAddress;

import java.util.Optional;

public interface AccountRepositoryPort {
	Account save(Account account);

	Optional<Account> findById(Long id);

	Optional<Account> findByEmail(EmailAddress email);

	boolean existsByEmail(EmailAddress email);
}

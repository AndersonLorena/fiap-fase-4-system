package com.al.fiap.cs.account.adapters.drivens.repositories;

import com.al.fiap.cs.account.adapters.drivens.repositories.jpa.AccountJpaEntity;
import com.al.fiap.cs.account.adapters.drivens.repositories.jpa.AccountSpringDataRepository;
import com.al.fiap.cs.account.core.domain.account.Account;
import com.al.fiap.cs.account.core.domain.account.AccountStatus;
import com.al.fiap.cs.account.core.domain.account.Document;
import com.al.fiap.cs.account.core.domain.account.EmailAddress;
import com.al.fiap.cs.account.core.domain.account.FullName;
import com.al.fiap.cs.account.core.domain.account.Phone;
import com.al.fiap.cs.account.ports.repositories.AccountRepositoryPort;

import java.util.Optional;

public class AccountRepositoryAdapter implements AccountRepositoryPort {

	private final AccountSpringDataRepository repository;

	public AccountRepositoryAdapter(AccountSpringDataRepository repository) {
		this.repository = repository;
	}

	@Override
	public Account save(Account account) {
		AccountJpaEntity entity = toEntity(account);
		repository.save(entity);
		return account;
	}

	@Override
	public Optional<Account> findById(Long id) {
		return repository.findById(id).map(this::toDomain);
	}

	@Override
	public Optional<Account> findByEmail(EmailAddress email) {
		return repository.findByEmail(email.value()).map(this::toDomain);
	}

	@Override
	public boolean existsByEmail(EmailAddress email) {
		return repository.existsByEmail(email.value());
	}

	private AccountJpaEntity toEntity(Account account) {
		AccountJpaEntity entity = new AccountJpaEntity();
		entity.setId(account.id());
		entity.setEmail(account.email().value());
		entity.setFullName(account.fullName().value());
		entity.setDocument(account.document() == null ? null : account.document().value());
		entity.setPhone(account.phone() == null ? null : account.phone().value());
		entity.setKeycloakUserId(account.keycloakUserId());
		entity.setStatus(account.status().name());
		entity.setCreatedAt(account.createdAt());
		entity.setCreatedBy(account.createdBy());
		entity.setUpdatedAt(account.updatedAt());
		entity.setUpdatedBy(account.updatedBy());
		return entity;
	}

	private Account toDomain(AccountJpaEntity entity) {
		return Account.restore(
			entity.getId(),
			entity.getCreatedAt(),
			entity.getCreatedBy(),
			entity.getUpdatedAt(),
			entity.getUpdatedBy(),
			new EmailAddress(entity.getEmail()),
			new FullName(entity.getFullName()),
			entity.getDocument() == null ? null : new Document(entity.getDocument()),
			entity.getPhone() == null ? null : new Phone(entity.getPhone()),
			entity.getKeycloakUserId(),
			AccountStatus.valueOf(entity.getStatus())
		);
	}
}

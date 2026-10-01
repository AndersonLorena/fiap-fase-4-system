package com.al.fiap.cs.account.adapters.drivens.repositories;

import com.al.fiap.cs.account.adapters.drivens.repositories.jpa.PasswordRecoveryTokenJpaEntity;
import com.al.fiap.cs.account.adapters.drivens.repositories.jpa.PasswordRecoveryTokenSpringDataRepository;
import com.al.fiap.cs.account.core.domain.shared.SnowflakeIdGenerator;
import com.al.fiap.cs.account.ports.repositories.PasswordRecoveryTokenRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

public class PasswordRecoveryTokenRepositoryAdapter implements PasswordRecoveryTokenRepositoryPort {

	private final PasswordRecoveryTokenSpringDataRepository repository;

	public PasswordRecoveryTokenRepositoryAdapter(PasswordRecoveryTokenSpringDataRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional
	public void invalidateActiveTokens(Long accountId) {
		repository.invalidateActiveTokens(accountId, Instant.now());
	}

	@Override
	public void save(PasswordRecoveryTokenRecord token) {
		PasswordRecoveryTokenJpaEntity entity = new PasswordRecoveryTokenJpaEntity();
		Long id = token.id() == null ? SnowflakeIdGenerator.getInstance().nextId() : token.id();
		entity.setId(id);
		entity.setAccountId(token.accountId());
		entity.setCodeHash(token.codeHash());
		entity.setExpiresAt(token.expiresAt());
		entity.setUsedAt(token.usedAt());
		entity.setInvalidatedAt(token.invalidatedAt());
		entity.setCreatedAt(token.createdAt() == null ? Instant.now() : token.createdAt());
		repository.save(entity);
	}

	@Override
	public Optional<PasswordRecoveryTokenRecord> findActiveByAccountId(Long accountId) {
		return repository.findActiveByAccountId(accountId, Instant.now()).map(this::toRecord);
	}

	@Override
	@Transactional
	public void markUsed(Long tokenId, Instant usedAt) {
		repository.findById(tokenId).ifPresent(entity -> {
			entity.setUsedAt(usedAt);
			repository.save(entity);
		});
	}

	private PasswordRecoveryTokenRecord toRecord(PasswordRecoveryTokenJpaEntity entity) {
		return new PasswordRecoveryTokenRecord(
			entity.getId(),
			entity.getAccountId(),
			entity.getCodeHash(),
			entity.getExpiresAt(),
			entity.getUsedAt(),
			entity.getInvalidatedAt(),
			entity.getCreatedAt()
		);
	}
}

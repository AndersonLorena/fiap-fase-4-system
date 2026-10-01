package com.al.fiap.cs.account.ports.repositories;

import java.time.Instant;
import java.util.Optional;

public interface PasswordRecoveryTokenRepositoryPort {

	void invalidateActiveTokens(Long accountId);

	void save(PasswordRecoveryTokenRecord token);

	Optional<PasswordRecoveryTokenRecord> findActiveByAccountId(Long accountId);

	void markUsed(Long tokenId, Instant usedAt);

	record PasswordRecoveryTokenRecord(
			Long id,
			Long accountId,
			String codeHash,
			Instant expiresAt,
			Instant usedAt,
			Instant invalidatedAt,
			Instant createdAt
	) {
	}
}

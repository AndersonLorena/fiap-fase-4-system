package com.al.fiap.cs.account.adapters.drivens.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface PasswordRecoveryTokenSpringDataRepository extends JpaRepository<PasswordRecoveryTokenJpaEntity, Long> {

	@Modifying
	@Query("""
		update PasswordRecoveryTokenJpaEntity t
		set t.invalidatedAt = :now
		where t.accountId = :accountId
		  and t.usedAt is null
		  and t.invalidatedAt is null
		""")
	void invalidateActiveTokens(@Param("accountId") Long accountId, @Param("now") Instant now);

	@Query("""
		select t from PasswordRecoveryTokenJpaEntity t
		where t.accountId = :accountId
		  and t.usedAt is null
		  and t.invalidatedAt is null
		  and t.expiresAt > :now
		order by t.createdAt desc
		""")
	Optional<PasswordRecoveryTokenJpaEntity> findActiveByAccountId(
			@Param("accountId") Long accountId,
			@Param("now") Instant now
	);
}

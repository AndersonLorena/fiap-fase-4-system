package com.al.fiap.cs.account.adapters.drivens.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountSpringDataRepository extends JpaRepository<AccountJpaEntity, Long> {
	Optional<AccountJpaEntity> findByEmail(String email);

	boolean existsByEmail(String email);
}

package com.al.fiap.cs.account.adapters.drivens.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthenticationThrottleSpringDataRepository
		extends JpaRepository<AuthenticationThrottleJpaEntity, AuthenticationThrottleJpaEntity.ThrottleId> {
}

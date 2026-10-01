package com.al.fiap.cs.account.adapters.drivens.repositories;

import com.al.fiap.cs.account.adapters.drivens.repositories.jpa.AuthenticationThrottleJpaEntity;
import com.al.fiap.cs.account.adapters.drivens.repositories.jpa.AuthenticationThrottleSpringDataRepository;
import com.al.fiap.cs.account.ports.repositories.AuthenticationThrottleRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

public class AuthenticationThrottleRepositoryAdapter implements AuthenticationThrottleRepositoryPort {

	private final AuthenticationThrottleSpringDataRepository repository;

	public AuthenticationThrottleRepositoryAdapter(AuthenticationThrottleSpringDataRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Integer> findAttempts(String operation, String subjectHash, Instant windowStartedAt) {
		var id = new AuthenticationThrottleJpaEntity.ThrottleId(operation, subjectHash);
		return repository.findById(id)
			.filter(entity -> !entity.getWindowStartedAt().isBefore(windowStartedAt))
			.map(AuthenticationThrottleJpaEntity::getAttempts);
	}

	@Override
	@Transactional
	public int incrementAndGetAttempts(String operation, String subjectHash, Instant windowStartedAt, Instant now) {
		var id = new AuthenticationThrottleJpaEntity.ThrottleId(operation, subjectHash);
		AuthenticationThrottleJpaEntity entity = repository.findById(id).orElseGet(() -> {
			AuthenticationThrottleJpaEntity created = new AuthenticationThrottleJpaEntity();
			created.setOperation(operation);
			created.setSubjectHash(subjectHash);
			created.setWindowStartedAt(windowStartedAt);
			created.setAttempts(0);
			return created;
		});

		if (entity.getWindowStartedAt().isBefore(windowStartedAt)) {
			entity.setWindowStartedAt(windowStartedAt);
			entity.setAttempts(0);
		}

		entity.setAttempts(entity.getAttempts() + 1);
		repository.save(entity);
		return entity.getAttempts();
	}

	@Override
	@Transactional
	public void reset(String operation, String subjectHash, Instant windowStartedAt) {
		var id = new AuthenticationThrottleJpaEntity.ThrottleId(operation, subjectHash);
		AuthenticationThrottleJpaEntity entity = new AuthenticationThrottleJpaEntity();
		entity.setOperation(operation);
		entity.setSubjectHash(subjectHash);
		entity.setWindowStartedAt(windowStartedAt);
		entity.setAttempts(0);
		repository.save(entity);
	}
}

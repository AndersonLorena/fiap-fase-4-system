package com.al.fiap.cs.account.ports.repositories;

import java.time.Instant;
import java.util.Optional;

public interface AuthenticationThrottleRepositoryPort {

	Optional<Integer> findAttempts(String operation, String subjectHash, Instant windowStartedAt);

	int incrementAndGetAttempts(String operation, String subjectHash, Instant windowStartedAt, Instant now);

	void reset(String operation, String subjectHash, Instant windowStartedAt);
}

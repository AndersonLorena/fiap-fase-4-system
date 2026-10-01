package com.al.fiap.cs.account.adapters.drivens.repositories.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "authentication_throttles")
@IdClass(AuthenticationThrottleJpaEntity.ThrottleId.class)
public class AuthenticationThrottleJpaEntity {

	@Id
	private String operation;

	@Id
	@Column(name = "subject_hash")
	private String subjectHash;

	@Column(name = "window_started_at", nullable = false)
	private Instant windowStartedAt;

	@Column(nullable = false)
	private Integer attempts;

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getSubjectHash() {
		return subjectHash;
	}

	public void setSubjectHash(String subjectHash) {
		this.subjectHash = subjectHash;
	}

	public Instant getWindowStartedAt() {
		return windowStartedAt;
	}

	public void setWindowStartedAt(Instant windowStartedAt) {
		this.windowStartedAt = windowStartedAt;
	}

	public Integer getAttempts() {
		return attempts;
	}

	public void setAttempts(Integer attempts) {
		this.attempts = attempts;
	}

	public static final class ThrottleId implements Serializable {
		private String operation;
		private String subjectHash;

		public ThrottleId() {
		}

		public ThrottleId(String operation, String subjectHash) {
			this.operation = operation;
			this.subjectHash = subjectHash;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof ThrottleId that)) {
				return false;
			}
			return Objects.equals(operation, that.operation)
				&& Objects.equals(subjectHash, that.subjectHash);
		}

		@Override
		public int hashCode() {
			return Objects.hash(operation, subjectHash);
		}
	}
}

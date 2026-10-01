package com.al.fiap.cs.dealership.core.domain.shared;

import java.time.Instant;
import java.util.Objects;

public abstract class BaseEntity {

	private final Long id;
	private final Instant createdAt;
	private final Long createdBy;
	private Instant updatedAt;
	private Long updatedBy;

	protected BaseEntity(Long userId) {
		Objects.requireNonNull(userId, "userId");
		this.id = SnowflakeIdGenerator.getInstance().nextId();
		this.createdAt = Instant.now();
		this.createdBy = userId;
		this.updatedAt = this.createdAt;
		this.updatedBy = userId;
	}

	protected BaseEntity(
			Long id,
			Instant createdAt,
			Long createdBy,
			Instant updatedAt,
			Long updatedBy) {
		this.id = Objects.requireNonNull(id, "id");
		this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
		this.createdBy = Objects.requireNonNull(createdBy, "createdBy");
		this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
		this.updatedBy = Objects.requireNonNull(updatedBy, "updatedBy");
	}

	protected void updateAudit(Long userId) {
		Objects.requireNonNull(userId, "userId");
		this.updatedAt = Instant.now();
		this.updatedBy = userId;
	}

	public Long id() {
		return id;
	}

	public Instant createdAt() {
		return createdAt;
	}

	public Long createdBy() {
		return createdBy;
	}

	public Instant updatedAt() {
		return updatedAt;
	}

	public Long updatedBy() {
		return updatedBy;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		BaseEntity that = (BaseEntity) other;
		return id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}

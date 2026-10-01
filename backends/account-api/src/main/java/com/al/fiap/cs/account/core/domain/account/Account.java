package com.al.fiap.cs.account.core.domain.account;

import com.al.fiap.cs.account.core.domain.exceptions.AccountAlreadyValidatedException;
import com.al.fiap.cs.account.core.domain.shared.BaseEntity;

import java.time.Instant;
import java.util.Objects;

public final class Account extends BaseEntity {

	public static final Long SYSTEM_ACTOR_ID = 0L;

	private final EmailAddress email;
	private FullName fullName;
	private Document document;
	private Phone phone;
	private String keycloakUserId;
	private AccountStatus status;

	private Account(
			Long userId,
			EmailAddress email,
			FullName fullName,
			String keycloakUserId,
			AccountStatus status) {
		super(userId);
		this.email = Objects.requireNonNull(email, "email");
		this.fullName = Objects.requireNonNull(fullName, "fullName");
		this.keycloakUserId = keycloakUserId;
		this.status = Objects.requireNonNull(status, "status");
	}

	private Account(
			Long id,
			Instant createdAt,
			Long createdBy,
			Instant updatedAt,
			Long updatedBy,
			EmailAddress email,
			FullName fullName,
			Document document,
			Phone phone,
			String keycloakUserId,
			AccountStatus status) {
		super(id, createdAt, createdBy, updatedAt, updatedBy);
		this.email = Objects.requireNonNull(email, "email");
		this.fullName = Objects.requireNonNull(fullName, "fullName");
		this.document = document;
		this.phone = phone;
		this.keycloakUserId = keycloakUserId;
		this.status = Objects.requireNonNull(status, "status");
	}

	public static Account create(EmailAddress email, FullName fullName) {
		return new Account(SYSTEM_ACTOR_ID, email, fullName, null, AccountStatus.PENDING);
	}

	public static Account restore(
			Long id,
			Instant createdAt,
			Long createdBy,
			Instant updatedAt,
			Long updatedBy,
			EmailAddress email,
			FullName fullName,
			Document document,
			Phone phone,
			String keycloakUserId,
			AccountStatus status) {
		return new Account(
			id,
			createdAt,
			createdBy,
			updatedAt,
			updatedBy,
			email,
			fullName,
			document,
			phone,
			keycloakUserId,
			status
		);
	}

	public void bindKeycloakUser(String keycloakUserId, Long userId) {
		Objects.requireNonNull(keycloakUserId, "keycloakUserId");
		if (keycloakUserId.isBlank()) {
			throw new IllegalArgumentException("keycloakUserId must not be blank");
		}
		this.keycloakUserId = keycloakUserId;
		updateAudit(userId);
	}

	public void completeProfile(Document document, Phone phone, Long userId) {
		if (status == AccountStatus.VALIDATED) {
			throw new AccountAlreadyValidatedException("Account profile is already validated");
		}
		this.document = Objects.requireNonNull(document, "document");
		this.phone = Objects.requireNonNull(phone, "phone");
		this.status = AccountStatus.VALIDATED;
		updateAudit(userId);
	}

	public boolean isEligibleBuyer() {
		return status == AccountStatus.VALIDATED;
	}

	public EmailAddress email() {
		return email;
	}

	public FullName fullName() {
		return fullName;
	}

	public Document document() {
		return document;
	}

	public Phone phone() {
		return phone;
	}

	public String keycloakUserId() {
		return keycloakUserId;
	}

	public AccountStatus status() {
		return status;
	}
}

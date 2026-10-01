package com.al.fiap.cs.account.core.services;

import com.al.fiap.cs.account.core.domain.account.EmailAddress;
import com.al.fiap.cs.account.core.services.support.AuthenticationThrottleGuard;
import com.al.fiap.cs.account.core.services.support.RecoveryCodeSupport;
import com.al.fiap.cs.account.ports.email.EmailPort;
import com.al.fiap.cs.account.ports.repositories.AccountRepositoryPort;
import com.al.fiap.cs.account.ports.repositories.PasswordRecoveryTokenRepositoryPort;
import com.al.fiap.cs.account.ports.services.RequestPasswordRecoveryServicePort;
import com.al.fiap.cs.account.ports.services.dtos.request.RequestPasswordRecoveryRequest;
import com.al.fiap.cs.account.ports.services.dtos.response.PasswordRecoveryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

public class RequestPasswordRecoveryService implements RequestPasswordRecoveryServicePort {

	private static final Logger log = LoggerFactory.getLogger(RequestPasswordRecoveryService.class);
	private static final String GENERIC_MESSAGE = "If the account exists, a recovery code was sent";

	private final AccountRepositoryPort accountRepository;
	private final PasswordRecoveryTokenRepositoryPort recoveryTokenRepository;
	private final EmailPort emailPort;
	private final AuthenticationThrottleGuard throttleGuard;
	private final Duration passwordRecoveryTtl;
	private final String passwordRecoveryUrl;

	public RequestPasswordRecoveryService(
			AccountRepositoryPort accountRepository,
			PasswordRecoveryTokenRepositoryPort recoveryTokenRepository,
			EmailPort emailPort,
			AuthenticationThrottleGuard throttleGuard,
			Duration passwordRecoveryTtl,
			String passwordRecoveryUrl) {
		this.accountRepository = accountRepository;
		this.recoveryTokenRepository = recoveryTokenRepository;
		this.emailPort = emailPort;
		this.throttleGuard = throttleGuard;
		this.passwordRecoveryTtl = passwordRecoveryTtl;
		this.passwordRecoveryUrl = passwordRecoveryUrl;
	}

	@Override
	@Transactional
	public PasswordRecoveryResponse request(RequestPasswordRecoveryRequest request) {
		EmailAddress email = new EmailAddress(request.email());
		throttleGuard.guard("PASSWORD_RECOVERY", email.value());

		accountRepository.findByEmail(email).ifPresent(account -> {
			String code = RecoveryCodeSupport.generateCode();
			Instant now = Instant.now();
			recoveryTokenRepository.invalidateActiveTokens(account.id());
			recoveryTokenRepository.save(new PasswordRecoveryTokenRepositoryPort.PasswordRecoveryTokenRecord(
				null,
				account.id(),
				RecoveryCodeSupport.hash(code),
				now.plus(passwordRecoveryTtl),
				null,
				null,
				now
			));
			try {
				emailPort.sendPasswordRecoveryCode(
					account.email().value(),
					code,
					passwordRecoveryUrl
				);
			}
			catch (Exception ex) {
				log.error("Password recovery email failed for accountId={}", account.id());
			}
		});

		return new PasswordRecoveryResponse(GENERIC_MESSAGE);
	}
}

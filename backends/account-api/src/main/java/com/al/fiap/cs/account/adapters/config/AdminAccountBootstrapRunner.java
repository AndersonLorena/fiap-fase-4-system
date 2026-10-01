package com.al.fiap.cs.account.adapters.config;

import com.al.fiap.cs.account.core.domain.account.AccountType;
import com.al.fiap.cs.account.core.domain.account.EmailAddress;
import com.al.fiap.cs.account.ports.repositories.AccountRepositoryPort;
import com.al.fiap.cs.account.ports.services.CreateAccountServicePort;
import com.al.fiap.cs.account.ports.services.dtos.request.CreateAccountRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

public class AdminAccountBootstrapRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(AdminAccountBootstrapRunner.class);

	private final AccountProperties properties;
	private final AccountRepositoryPort accountRepository;
	private final CreateAccountServicePort createAccountService;

	public AdminAccountBootstrapRunner(
			AccountProperties properties,
			AccountRepositoryPort accountRepository,
			CreateAccountServicePort createAccountService) {
		this.properties = properties;
		this.accountRepository = accountRepository;
		this.createAccountService = createAccountService;
	}

	@Override
	public void run(ApplicationArguments args) {
		AccountProperties.Bootstrap.Admin admin = properties.getBootstrap().getAdmin();
		if (!admin.isEnabled()) {
			return;
		}
		EmailAddress email = new EmailAddress(admin.getEmail());
		if (accountRepository.existsByEmail(email)) {
			log.info("Product admin account already exists for email={}", admin.getEmail());
			return;
		}
		log.info("Bootstrapping product admin account for email={}", admin.getEmail());
		createAccountService.create(new CreateAccountRequest(
			admin.getEmail(),
			admin.getPassword(),
			admin.getFullName(),
			AccountType.ADMIN,
			true
		));
		log.info("Product admin account bootstrapped successfully");
	}
}

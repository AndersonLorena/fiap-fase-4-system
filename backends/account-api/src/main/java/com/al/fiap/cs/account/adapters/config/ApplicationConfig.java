package com.al.fiap.cs.account.adapters.config;

import com.al.fiap.cs.account.adapters.drivers.webapis.security.AuthenticatedAccountArgumentResolver;
import com.al.fiap.cs.account.adapters.drivens.email.ResendEmailAdapter;
import com.al.fiap.cs.account.adapters.drivens.identity.KeycloakIdentityAdminAdapter;
import com.al.fiap.cs.account.adapters.drivens.identity.KeycloakIdentityTokenAdapter;
import com.al.fiap.cs.account.adapters.drivens.lock.LocalAccountLockAdapter;
import com.al.fiap.cs.account.adapters.drivens.lock.RedisAccountLockAdapter;
import com.al.fiap.cs.account.adapters.drivens.repositories.AccountRepositoryAdapter;
import com.al.fiap.cs.account.adapters.drivens.repositories.AuthenticationThrottleRepositoryAdapter;
import com.al.fiap.cs.account.adapters.drivens.repositories.PasswordRecoveryTokenRepositoryAdapter;
import com.al.fiap.cs.account.adapters.drivens.repositories.jpa.AccountSpringDataRepository;
import com.al.fiap.cs.account.adapters.drivens.repositories.jpa.AuthenticationThrottleSpringDataRepository;
import com.al.fiap.cs.account.adapters.drivens.repositories.jpa.PasswordRecoveryTokenSpringDataRepository;
import com.al.fiap.cs.account.core.domain.shared.SnowflakeIdGenerator;
import com.al.fiap.cs.account.core.services.AuthenticateService;
import com.al.fiap.cs.account.core.services.ChangePasswordService;
import com.al.fiap.cs.account.core.services.CompleteProfileService;
import com.al.fiap.cs.account.core.services.ConfirmPasswordRecoveryService;
import com.al.fiap.cs.account.core.services.CreateAccountService;
import com.al.fiap.cs.account.core.services.GetCurrentAccountService;
import com.al.fiap.cs.account.core.services.RefreshTokenService;
import com.al.fiap.cs.account.core.services.RequestPasswordRecoveryService;
import com.al.fiap.cs.account.core.services.ValidateBuyerService;
import com.al.fiap.cs.account.core.services.support.AuthenticationThrottleGuard;
import com.al.fiap.cs.account.ports.email.EmailPort;
import com.al.fiap.cs.account.ports.identity.IdentityAdminPort;
import com.al.fiap.cs.account.ports.identity.IdentityTokenPort;
import com.al.fiap.cs.account.ports.lock.AccountLockPort;
import com.al.fiap.cs.account.ports.repositories.AccountRepositoryPort;
import com.al.fiap.cs.account.ports.repositories.AuthenticationThrottleRepositoryPort;
import com.al.fiap.cs.account.ports.repositories.PasswordRecoveryTokenRepositoryPort;
import com.al.fiap.cs.account.ports.services.AuthenticateServicePort;
import com.al.fiap.cs.account.ports.services.ChangePasswordServicePort;
import com.al.fiap.cs.account.ports.services.CompleteProfileServicePort;
import com.al.fiap.cs.account.ports.services.ConfirmPasswordRecoveryServicePort;
import com.al.fiap.cs.account.ports.services.CreateAccountServicePort;
import com.al.fiap.cs.account.ports.services.GetCurrentAccountServicePort;
import com.al.fiap.cs.account.ports.services.RefreshTokenServicePort;
import com.al.fiap.cs.account.ports.services.RequestPasswordRecoveryServicePort;
import com.al.fiap.cs.account.ports.services.ValidateBuyerServicePort;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.client.RestClient;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@EnableRetry
@EnableConfigurationProperties(AccountProperties.class)
public class ApplicationConfig implements WebMvcConfigurer {

	private final AccountProperties properties;

	public ApplicationConfig(AccountProperties properties) {
		this.properties = properties;
	}

	@PostConstruct
	void configureSnowflake() {
		SnowflakeIdGenerator.configure(properties.getSnowflake().getMachineId());
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(new AuthenticatedAccountArgumentResolver(properties.getS2s().getAllowedClientIds()));
	}

	@Bean
	RestClient.Builder restClientBuilder() {
		return RestClient.builder();
	}

	@Bean
	AccountRepositoryPort accountRepositoryPort(AccountSpringDataRepository repository) {
		return new AccountRepositoryAdapter(repository);
	}

	@Bean
	PasswordRecoveryTokenRepositoryPort passwordRecoveryTokenRepositoryPort(
			PasswordRecoveryTokenSpringDataRepository repository) {
		return new PasswordRecoveryTokenRepositoryAdapter(repository);
	}

	@Bean
	AuthenticationThrottleRepositoryPort authenticationThrottleRepositoryPort(
			AuthenticationThrottleSpringDataRepository repository) {
		return new AuthenticationThrottleRepositoryAdapter(repository);
	}

	@Bean
	@Profile("!test")
	JwtDecoder jwtDecoder() {
		AccountProperties.Keycloak keycloak = properties.getKeycloak();
		return KeycloakJwtDecoderFactory.create(keycloak.realmBaseUrl(), keycloak.certsUrl(), keycloak.getJwks());
	}

	@Bean
	@Profile("!test")
	IdentityAdminPort identityAdminPort(RestClient.Builder restClientBuilder) {
		return new KeycloakIdentityAdminAdapter(restClientBuilder, properties);
	}

	@Bean
	@Profile("!test")
	IdentityTokenPort identityTokenPort(RestClient.Builder restClientBuilder) {
		return new KeycloakIdentityTokenAdapter(restClientBuilder, properties);
	}

	@Bean
	@Profile("!test")
	EmailPort emailPort(RestClient.Builder restClientBuilder) {
		return new ResendEmailAdapter(restClientBuilder, properties);
	}

	@Bean
	@Profile("!test")
	AccountLockPort redisAccountLockPort(StringRedisTemplate redisTemplate) {
		return new RedisAccountLockAdapter(redisTemplate, properties);
	}

	@Bean
	@Profile("test")
	AccountLockPort localAccountLockPort() {
		return new LocalAccountLockAdapter();
	}

	@Bean
	AuthenticationThrottleGuard authenticationThrottleGuard(
			AuthenticationThrottleRepositoryPort throttleRepository) {
		return new AuthenticationThrottleGuard(
			throttleRepository,
			properties.getThrottle().getWindow(),
			properties.getThrottle().getMaxAttempts()
		);
	}

	@Bean
	CreateAccountServicePort createAccountServicePort(
			AccountRepositoryPort accountRepository,
			IdentityAdminPort identityAdminPort) {
		return new CreateAccountService(accountRepository, identityAdminPort);
	}

	@Bean
	AuthenticateServicePort authenticateServicePort(
			IdentityTokenPort identityTokenPort,
			AuthenticationThrottleGuard throttleGuard) {
		return new AuthenticateService(identityTokenPort, throttleGuard);
	}

	@Bean
	RefreshTokenServicePort refreshTokenServicePort(
			IdentityTokenPort identityTokenPort,
			AuthenticationThrottleGuard throttleGuard) {
		return new RefreshTokenService(identityTokenPort, throttleGuard);
	}

	@Bean
	ChangePasswordServicePort changePasswordServicePort(
			AccountRepositoryPort accountRepository,
			IdentityTokenPort identityTokenPort,
			IdentityAdminPort identityAdminPort,
			AccountLockPort accountLockPort) {
		return new ChangePasswordService(accountRepository, identityTokenPort, identityAdminPort, accountLockPort);
	}

	@Bean
	RequestPasswordRecoveryServicePort requestPasswordRecoveryServicePort(
			AccountRepositoryPort accountRepository,
			PasswordRecoveryTokenRepositoryPort recoveryTokenRepository,
			EmailPort emailPort,
			AuthenticationThrottleGuard throttleGuard) {
		return new RequestPasswordRecoveryService(
			accountRepository,
			recoveryTokenRepository,
			emailPort,
			throttleGuard,
			properties.getPasswordRecoveryTtl(),
			properties.getPasswordRecoveryUrl()
		);
	}

	@Bean
	ConfirmPasswordRecoveryServicePort confirmPasswordRecoveryServicePort(
			AccountRepositoryPort accountRepository,
			PasswordRecoveryTokenRepositoryPort recoveryTokenRepository,
			IdentityAdminPort identityAdminPort,
			AccountLockPort accountLockPort) {
		return new ConfirmPasswordRecoveryService(
			accountRepository,
			recoveryTokenRepository,
			identityAdminPort,
			accountLockPort
		);
	}

	@Bean
	CompleteProfileServicePort completeProfileServicePort(
			AccountRepositoryPort accountRepository,
			AccountLockPort accountLockPort) {
		return new CompleteProfileService(accountRepository, accountLockPort);
	}

	@Bean
	GetCurrentAccountServicePort getCurrentAccountServicePort(AccountRepositoryPort accountRepository) {
		return new GetCurrentAccountService(accountRepository);
	}

	@Bean
	ValidateBuyerServicePort validateBuyerServicePort(AccountRepositoryPort accountRepository) {
		return new ValidateBuyerService(accountRepository);
	}

	@Bean
	ApplicationRunner adminAccountBootstrapRunner(
			AccountRepositoryPort accountRepository,
			CreateAccountServicePort createAccountService) {
		return new AdminAccountBootstrapRunner(properties, accountRepository, createAccountService);
	}
}

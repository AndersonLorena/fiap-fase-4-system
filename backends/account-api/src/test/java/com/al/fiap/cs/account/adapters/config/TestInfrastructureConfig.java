package com.al.fiap.cs.account.adapters.config;

import com.al.fiap.cs.account.core.domain.account.AccountType;
import com.al.fiap.cs.account.core.domain.exceptions.AccountAlreadyExistsException;
import com.al.fiap.cs.account.core.domain.exceptions.InvalidCredentialsException;
import com.al.fiap.cs.account.ports.email.EmailPort;
import com.al.fiap.cs.account.ports.identity.IdentityAdminPort;
import com.al.fiap.cs.account.ports.identity.IdentityTokenPort;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@TestConfiguration
@Profile("test")
public class TestInfrastructureConfig {

	@Bean
	@Primary
	JwtDecoder jwtDecoder(AccountProperties properties) {
		return NimbusJwtDecoder.withSecretKey(secretKey(properties))
			.macAlgorithm(MacAlgorithm.HS256)
			.build();
	}

	@Bean
	JwtEncoder jwtEncoder(AccountProperties properties) {
		return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey(properties)));
	}

	@Bean
	InMemoryIdentityStore inMemoryIdentityStore() {
		return new InMemoryIdentityStore();
	}

	@Bean
	@Primary
	IdentityAdminPort identityAdminPort(InMemoryIdentityStore store) {
		return new InMemoryIdentityAdminPort(store);
	}

	@Bean
	@Primary
	IdentityTokenPort identityTokenPort(InMemoryIdentityStore store) {
		return new InMemoryIdentityTokenPort(store);
	}

	@Bean
	@Primary
	EmailPort emailPort(InMemoryIdentityStore store) {
		return store::captureRecoveryEmail;
	}

	private static SecretKey secretKey(AccountProperties properties) {
		byte[] bytes = properties.getTestJwtSecret().getBytes(StandardCharsets.UTF_8);
		byte[] key = new byte[32];
		System.arraycopy(bytes, 0, key, 0, Math.min(bytes.length, 32));
		return new SecretKeySpec(key, "HmacSHA256");
	}

	public static final class InMemoryIdentityStore {
		private final Map<String, String> passwordsByUserId = new ConcurrentHashMap<>();
		private final Map<String, String> userIdByEmail = new ConcurrentHashMap<>();
		private final Map<String, AccountType> typeByEmail = new ConcurrentHashMap<>();
		private final Map<String, String> refreshToEmail = new ConcurrentHashMap<>();
		private volatile String lastRecoveryCode;
		private volatile String lastRecoveryEmail;
		private volatile AccountType lastCreatedType;

		public String lastRecoveryCode() {
			return lastRecoveryCode;
		}

		public String lastRecoveryEmail() {
			return lastRecoveryEmail;
		}

		public AccountType lastCreatedType() {
			return lastCreatedType;
		}

		public AccountType typeForEmail(String email) {
			return typeByEmail.get(email);
		}

		void captureRecoveryEmail(String toEmail, String code, String recoveryUrl) {
			this.lastRecoveryEmail = toEmail;
			this.lastRecoveryCode = code;
		}
	}

	static final class InMemoryIdentityAdminPort implements IdentityAdminPort {
		private final InMemoryIdentityStore store;

		InMemoryIdentityAdminPort(InMemoryIdentityStore store) {
			this.store = store;
		}

		@Override
		public String createUser(CreateIdentityUserCommand command) {
			if (store.userIdByEmail.containsKey(command.email())) {
				throw new AccountAlreadyExistsException("Account email already exists");
			}
			String userId = UUID.randomUUID().toString();
			store.userIdByEmail.put(command.email(), userId);
			store.passwordsByUserId.put(userId, command.password());
			store.typeByEmail.put(command.email(), command.type());
			store.lastCreatedType = command.type();
			return userId;
		}

		@Override
		public void resetPassword(String keycloakUserId, String newPassword) {
			store.passwordsByUserId.put(keycloakUserId, newPassword);
		}

		@Override
		public void logoutSessions(String keycloakUserId) {
		}

		@Override
		public void deleteUser(String keycloakUserId) {
			store.passwordsByUserId.remove(keycloakUserId);
			store.userIdByEmail.entrySet().removeIf(entry -> {
				if (entry.getValue().equals(keycloakUserId)) {
					store.typeByEmail.remove(entry.getKey());
					return true;
				}
				return false;
			});
		}
	}

	static final class InMemoryIdentityTokenPort implements IdentityTokenPort {
		private final InMemoryIdentityStore store;

		InMemoryIdentityTokenPort(InMemoryIdentityStore store) {
			this.store = store;
		}

		@Override
		public TokenPair passwordGrant(String username, String password) {
			String userId = store.userIdByEmail.get(username);
			if (userId == null || !password.equals(store.passwordsByUserId.get(userId))) {
				throw new InvalidCredentialsException("Invalid credentials");
			}
			String refresh = UUID.randomUUID().toString();
			store.refreshToEmail.put(refresh, username);
			return new TokenPair("access-" + username, refresh, "Bearer", 300);
		}

		@Override
		public TokenPair refreshGrant(String refreshToken) {
			String email = store.refreshToEmail.get(refreshToken);
			if (email == null) {
				throw new InvalidCredentialsException("Invalid credentials");
			}
			String refresh = UUID.randomUUID().toString();
			store.refreshToEmail.put(refresh, email);
			return new TokenPair("access-" + email, refresh, "Bearer", 300);
		}
	}
}

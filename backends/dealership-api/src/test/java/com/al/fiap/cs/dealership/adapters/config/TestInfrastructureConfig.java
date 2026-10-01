package com.al.fiap.cs.dealership.adapters.config;

import com.al.fiap.cs.dealership.ports.buyervalidation.BuyerValidationPort;
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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@TestConfiguration
@Profile("test")
public class TestInfrastructureConfig {

	@Bean
	@Primary
	JwtDecoder jwtDecoder(DealershipProperties properties) {
		return NimbusJwtDecoder.withSecretKey(secretKey(properties))
			.macAlgorithm(MacAlgorithm.HS256)
			.build();
	}

	@Bean
	JwtEncoder jwtEncoder(DealershipProperties properties) {
		return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey(properties)));
	}

	@Bean
	@Primary
	BuyerValidationPort buyerValidationPort(BuyerValidationStub stub) {
		return stub;
	}

	@Bean
	BuyerValidationStub buyerValidationStub() {
		return new BuyerValidationStub();
	}

	private static SecretKey secretKey(DealershipProperties properties) {
		byte[] bytes = properties.getTestJwtSecret().getBytes(StandardCharsets.UTF_8);
		byte[] key = new byte[32];
		System.arraycopy(bytes, 0, key, 0, Math.min(bytes.length, 32));
		return new SecretKeySpec(key, "HmacSHA256");
	}

	public static final class BuyerValidationStub implements BuyerValidationPort {
		private final AtomicBoolean eligible = new AtomicBoolean(true);
		private final AtomicBoolean present = new AtomicBoolean(true);

		public void setEligible(boolean value) {
			eligible.set(value);
		}

		public void setPresent(boolean value) {
			present.set(value);
		}

		@Override
		public Optional<BuyerValidation> validate(Long accountId) {
			if (!present.get()) {
				return Optional.empty();
			}
			return Optional.of(new BuyerValidation(
				accountId,
				eligible.get() ? "VALIDATED" : "PENDING",
				eligible.get(),
				eligible.get() ? "52998224725" : null
			));
		}
	}
}

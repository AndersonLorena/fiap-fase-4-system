package com.al.fiap.cs.dealership.adapters.drivers.webapis;

import com.al.fiap.cs.dealership.adapters.config.TestInfrastructureConfig;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestInfrastructureConfig.class)
abstract class AbstractWebApiIntegrationTest {

	@LocalServerPort
	int port;

	@Autowired
	JwtEncoder jwtEncoder;

	@Autowired
	TestInfrastructureConfig.BuyerValidationStub buyerValidationStub;

	@BeforeEach
	void configureRestAssured() {
		RestAssured.reset();
		RestAssured.baseURI = "http://127.0.0.1";
		RestAssured.port = port;
		buyerValidationStub.setPresent(true);
		buyerValidationStub.setEligible(true);
	}

	protected String bearerForAdmin() {
		return "Bearer " + encodeToken(Map.of(
			"sub", "admin-user",
			"accountId", "1001",
			"azp", "dealership-api",
			"realm_access", Map.of("roles", List.of("ADMIN"))
		));
	}

	protected String bearerForCustomer(Long accountId) {
		return "Bearer " + encodeToken(Map.of(
			"sub", "customer-" + accountId,
			"accountId", String.valueOf(accountId),
			"azp", "dealership-api",
			"realm_access", Map.of("roles", List.of("CUSTOMER"))
		));
	}

	protected String bearerForCustomerWithoutAccountId() {
		return "Bearer " + encodeToken(Map.of(
			"sub", "customer-no-account",
			"azp", "dealership-api",
			"realm_access", Map.of("roles", List.of("CUSTOMER"))
		));
	}

	protected String bearerForPaymentProcessor() {
		return "Bearer " + encodeToken(Map.of(
			"sub", "service-account-payment-processor",
			"azp", "payment-processor",
			"realm_access", Map.of("roles", List.of())
		));
	}

	private String encodeToken(Map<String, Object> claims) {
		Instant now = Instant.now();
		JwtClaimsSet.Builder builder = JwtClaimsSet.builder()
			.issuer("http://localhost/test-issuer")
			.subject(String.valueOf(claims.get("sub")))
			.issuedAt(now)
			.expiresAt(now.plusSeconds(300))
			.audience(List.of("dealership-api"));
		claims.forEach((key, value) -> {
			if (!"sub".equals(key)) {
				builder.claim(key, value);
			}
		});
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		return jwtEncoder.encode(JwtEncoderParameters.from(header, builder.build())).getTokenValue();
	}
}

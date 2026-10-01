package com.al.fiap.cs.account.adapters.drivers.webapis;

import com.al.fiap.cs.account.adapters.config.AccountProperties;
import com.al.fiap.cs.account.adapters.config.TestInfrastructureConfig;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
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

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestInfrastructureConfig.class)
abstract class AbstractWebApiIntegrationTest {

	@LocalServerPort
	int port;

	@Autowired
	JwtEncoder jwtEncoder;

	@Autowired
	AccountProperties accountProperties;

	@Autowired
	TestInfrastructureConfig.InMemoryIdentityStore identityStore;

	@BeforeEach
	void configureRestAssured() {
		RestAssured.reset();
		RestAssured.baseURI = "http://127.0.0.1";
		RestAssured.port = port;
	}

	protected String bearerForAccount(Long accountId) {
		return bearerForAccount(accountId, "CUSTOMER");
	}

	protected String bearerForAccount(Long accountId, String realmRole) {
		return "Bearer " + encodeToken(Map.of(
			"sub", "user-" + accountId,
			"accountId", String.valueOf(accountId),
			"azp", "account-api",
			"realm_access", Map.of("roles", List.of(realmRole))
		));
	}

	protected String bearerForServiceAccount() {
		return "Bearer " + encodeToken(Map.of(
			"sub", "service-account-dealership-api",
			"azp", "dealership-api",
			"preferred_username", "service-account-dealership-api"
		));
	}

	protected Long createAccount(String email, String password, String fullName) {
		Number accountId = given()
			.contentType(ContentType.JSON)
			.body("""
				{
				  "email": "%s",
				  "password": "%s",
				  "fullName": "%s"
				}
				""".formatted(email, password, fullName))
			.when()
			.post("/api/v1/accounts")
			.then()
			.statusCode(201)
			.extract()
			.path("accountId");
		return accountId.longValue();
	}

	private String encodeToken(Map<String, Object> claims) {
		Instant now = Instant.now();
		JwtClaimsSet.Builder builder = JwtClaimsSet.builder()
			.issuer("http://localhost/test-issuer")
			.subject(String.valueOf(claims.get("sub")))
			.issuedAt(now)
			.expiresAt(now.plusSeconds(300))
			.audience(List.of("account-api"));
		claims.forEach((key, value) -> {
			if (!"sub".equals(key)) {
				builder.claim(key, value);
			}
		});
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		return jwtEncoder.encode(JwtEncoderParameters.from(header, builder.build())).getTokenValue();
	}
}

package com.al.fiap.cs.account.adapters.drivers.webapis;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

class AuthWebApiIntegrationTest extends AbstractWebApiIntegrationTest {

	@Test
	void login_returns200_tokens() {
		createAccount("login@example.com", "Str0ng-P@ss", "Login User");

		given()
			.contentType(ContentType.JSON)
			.body("""
				{
				  "email": "login@example.com",
				  "password": "Str0ng-P@ss"
				}
				""")
			.when()
			.post("/api/v1/auth/login")
			.then()
			.statusCode(200)
			.body("accessToken", notNullValue())
			.body("refreshToken", notNullValue())
			.body("tokenType", equalTo("Bearer"))
			.body("expiresIn", equalTo(300));
	}

	@Test
	void login_invalidCredentials_returns401() {
		createAccount("login-fail@example.com", "Str0ng-P@ss", "Login Fail");

		given()
			.contentType(ContentType.JSON)
			.body("""
				{
				  "email": "login-fail@example.com",
				  "password": "wrong-password"
				}
				""")
			.when()
			.post("/api/v1/auth/login")
			.then()
			.statusCode(401);
	}

	@Test
	void refresh_returns200_tokens() {
		createAccount("refresh@example.com", "Str0ng-P@ss", "Refresh User");

		String refreshToken = given()
			.contentType(ContentType.JSON)
			.body("""
				{
				  "email": "refresh@example.com",
				  "password": "Str0ng-P@ss"
				}
				""")
			.when()
			.post("/api/v1/auth/login")
			.then()
			.statusCode(200)
			.extract()
			.path("refreshToken");

		given()
			.contentType(ContentType.JSON)
			.body("""
				{
				  "refreshToken": "%s"
				}
				""".formatted(refreshToken))
			.when()
			.post("/api/v1/auth/refresh")
			.then()
			.statusCode(200)
			.body("accessToken", notNullValue())
			.body("refreshToken", notNullValue());
	}

	@Test
	void refresh_invalidToken_returns401() {
		given()
			.contentType(ContentType.JSON)
			.body("""
				{
				  "refreshToken": "invalid-refresh"
				}
				""")
			.when()
			.post("/api/v1/auth/refresh")
			.then()
			.statusCode(401);
	}
}

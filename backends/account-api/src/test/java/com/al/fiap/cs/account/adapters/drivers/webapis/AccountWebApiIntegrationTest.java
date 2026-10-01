package com.al.fiap.cs.account.adapters.drivers.webapis;

import com.al.fiap.cs.account.core.domain.account.AccountType;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AccountWebApiIntegrationTest extends AbstractWebApiIntegrationTest {

	@Test
	void createAccount_returns201() {
		given()
			.contentType(ContentType.JSON)
			.body("""
				{
				  "email": "buyer1@example.com",
				  "password": "Str0ng-P@ss",
				  "fullName": "Buyer One"
				}
				""")
			.when()
			.post("/api/v1/accounts")
			.then()
			.statusCode(201)
			.contentType(ContentType.JSON)
			.body("accountId", notNullValue())
			.body("email", equalTo("buyer1@example.com"))
			.body("status", equalTo("PENDING"));
		assertEquals(AccountType.CUSTOMER, identityStore.typeForEmail("buyer1@example.com"));
	}

	@Test
	void createAccount_withCustomerType_returns201() {
		given()
			.contentType(ContentType.JSON)
			.body("""
				{
				  "email": "buyer-typed@example.com",
				  "password": "Str0ng-P@ss",
				  "fullName": "Buyer Typed",
				  "type": "CUSTOMER"
				}
				""")
			.when()
			.post("/api/v1/accounts")
			.then()
			.statusCode(201)
			.body("email", equalTo("buyer-typed@example.com"));
		assertEquals(AccountType.CUSTOMER, identityStore.typeForEmail("buyer-typed@example.com"));
	}

	@Test
	void createAccount_adminTypeWithoutAdmin_returns409() {
		given()
			.contentType(ContentType.JSON)
			.body("""
				{
				  "email": "forced-admin@example.com",
				  "password": "Str0ng-P@ss",
				  "fullName": "Forced Admin",
				  "type": "ADMIN"
				}
				""")
			.when()
			.post("/api/v1/accounts")
			.then()
			.statusCode(409);
	}

	@Test
	void createAccount_adminTypeWithCustomerToken_returns409() {
		Long customerId = createAccount("customer-actor@example.com", "Str0ng-P@ss", "Customer Actor");

		given()
			.header("Authorization", bearerForAccount(customerId, "CUSTOMER"))
			.contentType(ContentType.JSON)
			.body("""
				{
				  "email": "forced-admin-2@example.com",
				  "password": "Str0ng-P@ss",
				  "fullName": "Forced Admin Two",
				  "type": "ADMIN"
				}
				""")
			.when()
			.post("/api/v1/accounts")
			.then()
			.statusCode(409);
	}

	@Test
	void createAccount_adminTypeWithAdminToken_returns201() {
		given()
			.header("Authorization", bearerForAccount(1L, "ADMIN"))
			.contentType(ContentType.JSON)
			.body("""
				{
				  "email": "new-admin@example.com",
				  "password": "Str0ng-P@ss",
				  "fullName": "New Admin",
				  "type": "ADMIN"
				}
				""")
			.when()
			.post("/api/v1/accounts")
			.then()
			.statusCode(201)
			.body("email", equalTo("new-admin@example.com"));
		assertEquals(AccountType.ADMIN, identityStore.typeForEmail("new-admin@example.com"));
	}

	@Test
	void createAccount_customerTypeWithAdminToken_returns201() {
		given()
			.header("Authorization", bearerForAccount(1L, "ADMIN"))
			.contentType(ContentType.JSON)
			.body("""
				{
				  "email": "admin-created-customer@example.com",
				  "password": "Str0ng-P@ss",
				  "fullName": "Admin Created Customer",
				  "type": "CUSTOMER"
				}
				""")
			.when()
			.post("/api/v1/accounts")
			.then()
			.statusCode(201)
			.body("email", equalTo("admin-created-customer@example.com"));
		assertEquals(AccountType.CUSTOMER, identityStore.typeForEmail("admin-created-customer@example.com"));
	}

	@Test
	void createAccount_duplicateEmail_returns409() {
		createAccount("dup@example.com", "Str0ng-P@ss", "Dup User");

		given()
			.contentType(ContentType.JSON)
			.body("""
				{
				  "email": "dup@example.com",
				  "password": "Str0ng-P@ss",
				  "fullName": "Dup User"
				}
				""")
			.when()
			.post("/api/v1/accounts")
			.then()
			.statusCode(409);
	}

	@Test
	void createAccount_invalidPayload_returns400() {
		given()
			.contentType(ContentType.JSON)
			.body("""
				{
				  "email": "",
				  "password": "short",
				  "fullName": "A"
				}
				""")
			.when()
			.post("/api/v1/accounts")
			.then()
			.statusCode(400);
	}

	@Test
	void getCurrentAccount_returns200_pendingProfile() {
		Long accountId = createAccount("me@example.com", "Str0ng-P@ss", "Me User");

		given()
			.header("Authorization", bearerForAccount(accountId))
			.when()
			.get("/api/v1/accounts/me")
			.then()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("accountId", equalTo(accountId))
			.body("email", equalTo("me@example.com"))
			.body("fullName", equalTo("Me User"))
			.body("document", nullValue())
			.body("phone", nullValue())
			.body("status", equalTo("PENDING"));
	}

	@Test
	void getCurrentAccount_afterCompleteProfile_returnsValidated() {
		Long accountId = createAccount("me-validated@example.com", "Str0ng-P@ss", "Me Validated");

		given()
			.header("Authorization", bearerForAccount(accountId))
			.contentType(ContentType.JSON)
			.body("""
				{
				  "document": "52998224725",
				  "phone": "11999998888"
				}
				""")
			.when()
			.post("/api/v1/accounts/me/profile")
			.then()
			.statusCode(200);

		given()
			.header("Authorization", bearerForAccount(accountId))
			.when()
			.get("/api/v1/accounts/me")
			.then()
			.statusCode(200)
			.body("accountId", equalTo(accountId))
			.body("email", equalTo("me-validated@example.com"))
			.body("fullName", equalTo("Me Validated"))
			.body("document", equalTo("52998224725"))
			.body("phone", equalTo("11999998888"))
			.body("status", equalTo("VALIDATED"));
	}

	@Test
	void getCurrentAccount_withoutToken_returns401() {
		given()
			.when()
			.get("/api/v1/accounts/me")
			.then()
			.statusCode(401);
	}

	@Test
	void getCurrentAccount_withServiceAccount_returns403() {
		given()
			.header("Authorization", bearerForServiceAccount())
			.when()
			.get("/api/v1/accounts/me")
			.then()
			.statusCode(403);
	}

	@Test
	void getCurrentAccount_missingAccount_returns204() {
		given()
			.header("Authorization", bearerForAccount(999999L))
			.when()
			.get("/api/v1/accounts/me")
			.then()
			.statusCode(204);
	}

	@Test
	void completeProfile_returns200_validated() {
		Long accountId = createAccount("profile@example.com", "Str0ng-P@ss", "Profile User");

		given()
			.header("Authorization", bearerForAccount(accountId))
			.contentType(ContentType.JSON)
			.body("""
				{
				  "document": "52998224725",
				  "phone": "11999998888"
				}
				""")
			.when()
			.post("/api/v1/accounts/me/profile")
			.then()
			.statusCode(200)
			.body("status", equalTo("VALIDATED"))
			.body("fullName", equalTo("Profile User"))
			.body("document", equalTo("52998224725"))
			.body("phone", equalTo("11999998888"));
	}

	@Test
	void completeProfile_invalidCpf_returns400() {
		Long accountId = createAccount("invalid-cpf@example.com", "Str0ng-P@ss", "Invalid Cpf");

		given()
			.header("Authorization", bearerForAccount(accountId))
			.contentType(ContentType.JSON)
			.body("""
				{
				  "document": "12345678901",
				  "phone": "11999998888"
				}
				""")
			.when()
			.post("/api/v1/accounts/me/profile")
			.then()
			.statusCode(400);

		given()
			.header("Authorization", bearerForAccount(accountId))
			.when()
			.get("/api/v1/accounts/me")
			.then()
			.statusCode(200)
			.body("status", equalTo("PENDING"))
			.body("document", nullValue());
	}

	@Test
	void completeProfile_withoutToken_returns401() {
		given()
			.contentType(ContentType.JSON)
			.body("""
				{
				  "document": "52998224725",
				  "phone": "11999998888"
				}
				""")
			.when()
			.post("/api/v1/accounts/me/profile")
			.then()
			.statusCode(401);
	}

	@Test
	void changePassword_returns200() {
		Long accountId = createAccount("pwd@example.com", "Str0ng-P@ss", "Pwd User");

		given()
			.header("Authorization", bearerForAccount(accountId))
			.contentType(ContentType.JSON)
			.body("""
				{
				  "currentPassword": "Str0ng-P@ss",
				  "newPassword": "N3w-Str0ng-P@ss"
				}
				""")
			.when()
			.post("/api/v1/accounts/password")
			.then()
			.statusCode(200);
	}

	@Test
	void passwordRecovery_requestAndConfirm_returns200() {
		createAccount("recover@example.com", "Str0ng-P@ss", "Recover User");

		given()
			.contentType(ContentType.JSON)
			.body("""
				{
				  "email": "recover@example.com"
				}
				""")
			.when()
			.post("/api/v1/accounts/password-recovery")
			.then()
			.statusCode(200)
			.body("message", notNullValue());

		given()
			.contentType(ContentType.JSON)
			.body("""
				{
				  "email": "unknown@example.com"
				}
				""")
			.when()
			.post("/api/v1/accounts/password-recovery")
			.then()
			.statusCode(200);

		String code = identityStore.lastRecoveryCode();

		given()
			.contentType(ContentType.JSON)
			.body("""
				{
				  "email": "recover@example.com",
				  "code": "%s",
				  "newPassword": "N3w-Str0ng-P@ss"
				}
				""".formatted(code))
			.when()
			.post("/api/v1/accounts/password-recovery/confirm")
			.then()
			.statusCode(200);
	}

	@Test
	void passwordRecoveryConfirm_invalidCode_returns400() {
		createAccount("recover2@example.com", "Str0ng-P@ss", "Recover Two");

		given()
			.contentType(ContentType.JSON)
			.body("""
				{
				  "email": "recover2@example.com",
				  "code": "000000",
				  "newPassword": "N3w-Str0ng-P@ss"
				}
				""")
			.when()
			.post("/api/v1/accounts/password-recovery/confirm")
			.then()
			.statusCode(400);
	}

	@Test
	void validateBuyer_eligibleAndIneligible() {
		Long pendingId = createAccount("pending-buyer@example.com", "Str0ng-P@ss", "Pending Buyer");
		Long validatedId = createAccount("validated-buyer@example.com", "Str0ng-P@ss", "Validated Buyer");

		given()
			.header("Authorization", bearerForAccount(validatedId))
			.contentType(ContentType.JSON)
			.body("""
				{
				  "document": "52998224725",
				  "phone": "11999998888"
				}
				""")
			.when()
			.post("/api/v1/accounts/me/profile")
			.then()
			.statusCode(200);

		given()
			.header("Authorization", bearerForServiceAccount())
			.when()
			.get("/api/v1/internal/accounts/{accountId}/buyer-validation", pendingId)
			.then()
			.statusCode(200)
			.body("eligible", equalTo(false))
			.body("status", equalTo("PENDING"))
			.body("cpf", nullValue());

		given()
			.header("Authorization", bearerForServiceAccount())
			.when()
			.get("/api/v1/internal/accounts/{accountId}/buyer-validation", validatedId)
			.then()
			.statusCode(200)
			.body("eligible", equalTo(true))
			.body("status", equalTo("VALIDATED"))
			.body("cpf", equalTo("52998224725"));
	}

	@Test
	void validateBuyer_missingAccount_returns204() {
		given()
			.header("Authorization", bearerForServiceAccount())
			.when()
			.get("/api/v1/internal/accounts/{accountId}/buyer-validation", 999999L)
			.then()
			.statusCode(204);
	}

	@Test
	void validateBuyer_withUserToken_returns403() {
		Long accountId = createAccount("forbid@example.com", "Str0ng-P@ss", "Forbid User");

		given()
			.header("Authorization", bearerForAccount(accountId))
			.when()
			.get("/api/v1/internal/accounts/{accountId}/buyer-validation", accountId)
			.then()
			.statusCode(403);
	}
}

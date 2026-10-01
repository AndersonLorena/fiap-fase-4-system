package com.al.fiap.cs.dealership.adapters.drivers.webapis;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

class BrandWebApiIntegrationTest extends AbstractWebApiIntegrationTest {

	@Test
	void brandCrudLifecycle() {
		Number brandId = given()
			.header("Authorization", bearerForAdmin())
			.contentType(ContentType.JSON)
			.body("""
				{"name":"Toyota"}
				""")
			.when()
			.post("/api/v1/brands")
			.then()
			.statusCode(201)
			.body("name", equalTo("Toyota"))
			.extract()
			.path("brandId");

		given()
			.when()
			.get("/api/v1/brands/{id}", brandId)
			.then()
			.statusCode(200)
			.body("name", equalTo("Toyota"));

		given()
			.when()
			.get("/api/v1/brands")
			.then()
			.statusCode(200)
			.body("name", hasItem("Toyota"));

		given()
			.header("Authorization", bearerForAdmin())
			.contentType(ContentType.JSON)
			.body("""
				{"name":"Toyota Motors"}
				""")
			.when()
			.put("/api/v1/brands/{id}", brandId)
			.then()
			.statusCode(200)
			.body("name", equalTo("Toyota Motors"));

		given()
			.header("Authorization", bearerForAdmin())
			.when()
			.delete("/api/v1/brands/{id}", brandId)
			.then()
			.statusCode(204);

		given()
			.when()
			.get("/api/v1/brands/{id}", brandId)
			.then()
			.statusCode(204);
	}

	@Test
	void createWithoutAuthReturns401() {
		given()
			.contentType(ContentType.JSON)
			.body("""
				{"name":"Honda"}
				""")
			.when()
			.post("/api/v1/brands")
			.then()
			.statusCode(401);
	}

	@Test
	void createWithCustomerReturns403() {
		given()
			.header("Authorization", bearerForCustomer(2001L))
			.contentType(ContentType.JSON)
			.body("""
				{"name":"Honda"}
				""")
			.when()
			.post("/api/v1/brands")
			.then()
			.statusCode(403);
	}

	@Test
	void duplicateNameReturns409() {
		given()
			.header("Authorization", bearerForAdmin())
			.contentType(ContentType.JSON)
			.body("""
				{"name":"Fiat"}
				""")
			.when()
			.post("/api/v1/brands")
			.then()
			.statusCode(201);

		given()
			.header("Authorization", bearerForAdmin())
			.contentType(ContentType.JSON)
			.body("""
				{"name":"Fiat"}
				""")
			.when()
			.post("/api/v1/brands")
			.then()
			.statusCode(409);
	}
}

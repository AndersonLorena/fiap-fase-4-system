package com.al.fiap.cs.dealership.adapters.drivers.webapis;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

class CarModelWebApiIntegrationTest extends AbstractWebApiIntegrationTest {

	@Test
	void modelCrudLifecycle() {
		Number brandId = given()
			.header("Authorization", bearerForAdmin())
			.contentType(ContentType.JSON)
			.body("""
				{"name":"Volkswagen"}
				""")
			.when()
			.post("/api/v1/brands")
			.then()
			.statusCode(201)
			.extract()
			.path("brandId");

		Number modelId = given()
			.header("Authorization", bearerForAdmin())
			.contentType(ContentType.JSON)
			.body("""
				{"brandId": %s, "name":"Gol"}
				""".formatted(brandId))
			.when()
			.post("/api/v1/models")
			.then()
			.statusCode(201)
			.body("name", equalTo("Gol"))
			.extract()
			.path("modelId");

		given()
			.when()
			.get("/api/v1/models?brandId={brandId}", brandId)
			.then()
			.statusCode(200)
			.body("[0].name", equalTo("Gol"));

		given()
			.header("Authorization", bearerForAdmin())
			.contentType(ContentType.JSON)
			.body("""
				{"name":"Gol G6"}
				""")
			.when()
			.put("/api/v1/models/{id}", modelId)
			.then()
			.statusCode(200)
			.body("name", equalTo("Gol G6"));

		given().header("Authorization", bearerForAdmin()).when().delete("/api/v1/models/{id}", modelId).then().statusCode(204);
	}
}

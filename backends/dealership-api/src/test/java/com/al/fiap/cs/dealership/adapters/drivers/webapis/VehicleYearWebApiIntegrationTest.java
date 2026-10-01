package com.al.fiap.cs.dealership.adapters.drivers.webapis;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

class VehicleYearWebApiIntegrationTest extends AbstractWebApiIntegrationTest {

	@Test
	void yearCrudLifecycle() {
		Number yearId = given()
			.header("Authorization", bearerForAdmin())
			.contentType(ContentType.JSON)
			.body("""
				{"year":2024}
				""")
			.when()
			.post("/api/v1/years")
			.then()
			.statusCode(201)
			.body("year", equalTo(2024))
			.extract()
			.path("yearId");

		given().when().get("/api/v1/years/{id}", yearId).then().statusCode(200).body("year", equalTo(2024));

		given()
			.header("Authorization", bearerForAdmin())
			.contentType(ContentType.JSON)
			.body("""
				{"year":2025}
				""")
			.when()
			.put("/api/v1/years/{id}", yearId)
			.then()
			.statusCode(200)
			.body("year", equalTo(2025));

		given().header("Authorization", bearerForAdmin()).when().delete("/api/v1/years/{id}", yearId).then().statusCode(204);
	}
}

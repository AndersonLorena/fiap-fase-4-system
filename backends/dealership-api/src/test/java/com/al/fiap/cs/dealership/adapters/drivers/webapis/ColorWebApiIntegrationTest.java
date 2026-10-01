package com.al.fiap.cs.dealership.adapters.drivers.webapis;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

class ColorWebApiIntegrationTest extends AbstractWebApiIntegrationTest {

	@Test
	void colorCrudLifecycle() {
		Number colorId = given()
			.header("Authorization", bearerForAdmin())
			.contentType(ContentType.JSON)
			.body("""
				{"name":"Red"}
				""")
			.when()
			.post("/api/v1/colors")
			.then()
			.statusCode(201)
			.extract()
			.path("colorId");

		given().when().get("/api/v1/colors/{id}", colorId).then().statusCode(200).body("name", equalTo("Red"));

		given()
			.header("Authorization", bearerForAdmin())
			.contentType(ContentType.JSON)
			.body("""
				{"name":"Crimson"}
				""")
			.when()
			.put("/api/v1/colors/{id}", colorId)
			.then()
			.statusCode(200)
			.body("name", equalTo("Crimson"));

		given().header("Authorization", bearerForAdmin()).when().delete("/api/v1/colors/{id}", colorId).then().statusCode(204);
	}
}

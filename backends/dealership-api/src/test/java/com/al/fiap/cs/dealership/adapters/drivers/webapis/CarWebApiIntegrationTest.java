package com.al.fiap.cs.dealership.adapters.drivers.webapis;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

class CarWebApiIntegrationTest extends AbstractWebApiIntegrationTest {

	@Test
	void carCrudListPurchaseAndPhotos() {
		Catalog catalog = seedCatalog("Chevrolet", "Onix", "Blue", 2022);

		Number cheapId = createCar(catalog, "45000.00");
		Number expensiveId = createCar(catalog, "90000.00");

		given()
			.when()
			.get("/api/v1/cars?status=AVAILABLE&q=Onix&sort=price,asc")
			.then()
			.statusCode(200)
			.body("totalElements", greaterThanOrEqualTo(2))
			.body("content[0].price", equalTo(45000.00f));

		given()
			.when()
			.get("/api/v1/cars?q=Onix")
			.then()
			.statusCode(200)
			.body("totalElements", greaterThanOrEqualTo(1));

		given()
			.header("Authorization", bearerForAdmin())
			.contentType(ContentType.JSON)
			.body("""
				{
				  "brandId": %s,
				  "modelId": %s,
				  "colorId": %s,
				  "yearId": %s,
				  "price": 47000.00
				}
				""".formatted(catalog.brandId, catalog.modelId, catalog.colorId, catalog.yearId))
			.when()
			.put("/api/v1/cars/{id}", cheapId)
			.then()
			.statusCode(200)
			.body("price", equalTo(47000.00f));

		var intent = given()
			.header("Authorization", bearerForAdmin())
			.when()
			.post("/api/v1/cars/{id}/photos/upload-intent", cheapId)
			.then()
			.statusCode(200)
			.body("uploadUrl", notNullValue())
			.body("objectKey", notNullValue())
			.body("intentToken", notNullValue())
			.extract();

		String objectKey = intent.path("objectKey");
		String intentToken = intent.path("intentToken");

		given()
			.header("Authorization", bearerForAdmin())
			.contentType(ContentType.JSON)
			.body("""
				{
				  "objectKey": "%s",
				  "intentToken": "%s",
				  "sortOrder": 0
				}
				""".formatted(objectKey, intentToken))
			.when()
			.post("/api/v1/cars/{id}/photos/complete", cheapId)
			.then()
			.statusCode(200)
			.body("photos[0].objectKey", equalTo(objectKey));

		given()
			.header("Authorization", bearerForCustomer(3001L))
			.when()
			.post("/api/v1/cars/{id}/purchase", cheapId)
			.then()
			.statusCode(200)
			.body("status", equalTo("AWAITING_PAYMENT"))
			.body("buyerAccountId", equalTo(3001))
			.body("buyerCpf", equalTo("52998224725"))
			.body("paymentCode", notNullValue())
			.body("soldAt", nullValue());

		given()
			.header("Authorization", bearerForAdmin())
			.contentType(ContentType.JSON)
			.body("""
				{
				  "brandId": %s,
				  "modelId": %s,
				  "colorId": %s,
				  "yearId": %s,
				  "price": 48000.00
				}
				""".formatted(catalog.brandId, catalog.modelId, catalog.colorId, catalog.yearId))
			.when()
			.put("/api/v1/cars/{id}", cheapId)
			.then()
			.statusCode(409);

		given()
			.header("Authorization", bearerForCustomer(3002L))
			.when()
			.post("/api/v1/cars/{id}/purchase", cheapId)
			.then()
			.statusCode(409);

		given()
			.header("Authorization", bearerForAdmin())
			.when()
			.delete("/api/v1/cars/{id}", cheapId)
			.then()
			.statusCode(409);

		given()
			.header("Authorization", bearerForAdmin())
			.when()
			.delete("/api/v1/cars/{id}", expensiveId)
			.then()
			.statusCode(204);
	}

	@Test
	void purchaseRequiresCustomerRoleAndEligibleBuyer() {
		Catalog catalog = seedCatalog("Ford", "Ka", "White", 2021);
		Number carId = createCar(catalog, "30000.00");

		given()
			.header("Authorization", bearerForAdmin())
			.when()
			.post("/api/v1/cars/{id}/purchase", carId)
			.then()
			.statusCode(403);

		given()
			.header("Authorization", bearerForCustomerWithoutAccountId())
			.when()
			.post("/api/v1/cars/{id}/purchase", carId)
			.then()
			.statusCode(403);

		buyerValidationStub.setEligible(false);
		given()
			.header("Authorization", bearerForCustomer(4001L))
			.when()
			.post("/api/v1/cars/{id}/purchase", carId)
			.then()
			.statusCode(409);
	}

	@Test
	void getUnknownCarReturns204() {
		given()
			.when()
			.get("/api/v1/cars/{id}", 999999999L)
			.then()
			.statusCode(204);
	}

	@Test
	void paymentWebhookConfirmsSaleAndRejectsOtherActors() {
		Catalog catalog = seedCatalog("Renault", "Kwid", "Silver", 2019);
		Number carId = createCar(catalog, "62000.00");

		String paymentCode = given()
			.header("Authorization", bearerForCustomer(5101L))
			.when()
			.post("/api/v1/cars/{id}/purchase", carId)
			.then()
			.statusCode(200)
			.body("status", equalTo("AWAITING_PAYMENT"))
			.extract()
			.path("paymentCode");

		given()
			.when()
			.get("/api/v1/cars?status=AVAILABLE&q=Kwid&sort=price,asc")
			.then()
			.statusCode(200)
			.body("totalElements", equalTo(0));

		given()
			.when()
			.get("/api/v1/cars?status=SOLD&q=Kwid&sort=price,asc")
			.then()
			.statusCode(200)
			.body("totalElements", equalTo(0));

		given()
			.contentType(ContentType.JSON)
			.body("{\"status\":\"PAID\"}")
			.when()
			.post("/api/v1/payments/{paymentCode}", paymentCode)
			.then()
			.statusCode(401);

		given()
			.header("Authorization", bearerForCustomer(5101L))
			.contentType(ContentType.JSON)
			.body("{\"status\":\"PAID\"}")
			.when()
			.post("/api/v1/payments/{paymentCode}", paymentCode)
			.then()
			.statusCode(403);

		given()
			.header("Authorization", bearerForAdmin())
			.contentType(ContentType.JSON)
			.body("{\"status\":\"PAID\"}")
			.when()
			.post("/api/v1/payments/{paymentCode}", paymentCode)
			.then()
			.statusCode(403);

		given()
			.header("Authorization", bearerForPaymentProcessor())
			.contentType(ContentType.JSON)
			.body("{\"status\":\"MAYBE\"}")
			.when()
			.post("/api/v1/payments/{paymentCode}", paymentCode)
			.then()
			.statusCode(400);

		given()
			.header("Authorization", bearerForPaymentProcessor())
			.contentType(ContentType.JSON)
			.body("{\"status\":\"PAID\"}")
			.when()
			.post("/api/v1/payments/{paymentCode}", "unknown-payment-code")
			.then()
			.statusCode(204);

		given()
			.header("Authorization", bearerForPaymentProcessor())
			.contentType(ContentType.JSON)
			.body("{\"status\":\"PAID\"}")
			.when()
			.post("/api/v1/payments/{paymentCode}", paymentCode)
			.then()
			.statusCode(200)
			.body("status", equalTo("SOLD"))
			.body("buyerCpf", equalTo("52998224725"))
			.body("soldAt", notNullValue());

		given()
			.header("Authorization", bearerForPaymentProcessor())
			.contentType(ContentType.JSON)
			.body("{\"status\":\"PAID\"}")
			.when()
			.post("/api/v1/payments/{paymentCode}", paymentCode)
			.then()
			.statusCode(200)
			.body("status", equalTo("SOLD"));

		given()
			.header("Authorization", bearerForPaymentProcessor())
			.contentType(ContentType.JSON)
			.body("{\"status\":\"CANCELLED\"}")
			.when()
			.post("/api/v1/payments/{paymentCode}", paymentCode)
			.then()
			.statusCode(409);

		given()
			.when()
			.get("/api/v1/cars/{id}", carId)
			.then()
			.statusCode(200)
			.body("status", equalTo("SOLD"));

		given()
			.when()
			.get("/api/v1/cars?status=SOLD&q=Kwid&sort=price,asc")
			.then()
			.statusCode(200)
			.body("totalElements", equalTo(1))
			.body("content[0].status", equalTo("SOLD"))
			.body("content[0].price", equalTo(62000.00f));
	}

	@Test
	void paymentWebhookCancellationReturnsCarToSale() {
		Catalog catalog = seedCatalog("Peugeot", "208", "Graphite", 2018);
		Number carId = createCar(catalog, "88000.00");

		String paymentCode = given()
			.header("Authorization", bearerForCustomer(6101L))
			.when()
			.post("/api/v1/cars/{id}/purchase", carId)
			.then()
			.statusCode(200)
			.extract()
			.path("paymentCode");

		given()
			.header("Authorization", bearerForPaymentProcessor())
			.contentType(ContentType.JSON)
			.body("{\"status\":\"CANCELLED\"}")
			.when()
			.post("/api/v1/payments/{paymentCode}", paymentCode)
			.then()
			.statusCode(200)
			.body("status", equalTo("AVAILABLE"))
			.body("buyerAccountId", nullValue())
			.body("buyerCpf", nullValue())
			.body("paymentCode", nullValue())
			.body("soldAt", nullValue());

		given()
			.header("Authorization", bearerForPaymentProcessor())
			.contentType(ContentType.JSON)
			.body("{\"status\":\"PAID\"}")
			.when()
			.post("/api/v1/payments/{paymentCode}", paymentCode)
			.then()
			.statusCode(204);

		given()
			.header("Authorization", bearerForCustomer(6101L))
			.when()
			.post("/api/v1/cars/{id}/purchase", carId)
			.then()
			.statusCode(200)
			.body("status", equalTo("AWAITING_PAYMENT"));
	}

	private Number createCar(Catalog catalog, String price) {
		return given()
			.header("Authorization", bearerForAdmin())
			.contentType(ContentType.JSON)
			.body("""
				{
				  "brandId": %s,
				  "modelId": %s,
				  "colorId": %s,
				  "yearId": %s,
				  "price": %s
				}
				""".formatted(catalog.brandId, catalog.modelId, catalog.colorId, catalog.yearId, price))
			.when()
			.post("/api/v1/cars")
			.then()
			.statusCode(201)
			.extract()
			.path("carId");
	}

	private Catalog seedCatalog(String brand, String model, String color, int year) {
		Number brandId = given()
			.header("Authorization", bearerForAdmin())
			.contentType(ContentType.JSON)
			.body("{\"name\":\"%s\"}".formatted(brand))
			.when()
			.post("/api/v1/brands")
			.then()
			.statusCode(201)
			.extract()
			.path("brandId");

		Number modelId = given()
			.header("Authorization", bearerForAdmin())
			.contentType(ContentType.JSON)
			.body("{\"brandId\": %s, \"name\":\"%s\"}".formatted(brandId, model))
			.when()
			.post("/api/v1/models")
			.then()
			.statusCode(201)
			.extract()
			.path("modelId");

		Number colorId = given()
			.header("Authorization", bearerForAdmin())
			.contentType(ContentType.JSON)
			.body("{\"name\":\"%s\"}".formatted(color))
			.when()
			.post("/api/v1/colors")
			.then()
			.statusCode(201)
			.extract()
			.path("colorId");

		Number yearId = given()
			.header("Authorization", bearerForAdmin())
			.contentType(ContentType.JSON)
			.body("{\"year\": %s}".formatted(year))
			.when()
			.post("/api/v1/years")
			.then()
			.statusCode(201)
			.extract()
			.path("yearId");

		return new Catalog(brandId, modelId, colorId, yearId);
	}

	private record Catalog(Number brandId, Number modelId, Number colorId, Number yearId) {
	}
}

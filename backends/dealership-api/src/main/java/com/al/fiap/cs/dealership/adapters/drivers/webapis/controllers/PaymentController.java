package com.al.fiap.cs.dealership.adapters.drivers.webapis.controllers;

import com.al.fiap.cs.dealership.adapters.config.DealershipProperties;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.request.ApplyPaymentWebRequest;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.response.CarWebResponse;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.mappers.CarWebMapper;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.security.AuthenticatedActor;
import com.al.fiap.cs.dealership.ports.services.CarServicePort;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@Tag(name = "Payments")
@RequestMapping("/api/v1/payments")
public class PaymentController {

	private final CarServicePort carService;
	private final DealershipProperties properties;

	public PaymentController(CarServicePort carService, DealershipProperties properties) {
		this.carService = carService;
		this.properties = properties;
	}

	@PostMapping("/{paymentCode}")
	public ResponseEntity<CarWebResponse> apply(
			AuthenticatedActor actor,
			@PathVariable String paymentCode,
			@Valid @RequestBody ApplyPaymentWebRequest request) {
		if (!isPaymentProcessor(actor)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		return ResponseEntity.ok(CarWebMapper.toWebResponse(
			carService.applyPayment(CarWebMapper.toApplyPaymentRequest(paymentCode, request, actor.actorId()))
		));
	}

	private boolean isPaymentProcessor(AuthenticatedActor actor) {
		if (actor.accountId() != null || actor.clientId() == null) {
			return false;
		}
		return properties.getPayment().getAllowedClientIds().contains(actor.clientId());
	}
}

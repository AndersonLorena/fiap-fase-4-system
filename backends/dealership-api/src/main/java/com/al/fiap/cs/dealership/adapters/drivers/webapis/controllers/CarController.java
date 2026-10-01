package com.al.fiap.cs.dealership.adapters.drivers.webapis.controllers;

import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.request.CompletePhotoUploadWebRequest;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.request.CreateCarWebRequest;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.request.UpdateCarWebRequest;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.response.CarWebResponse;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.response.PagedCarsWebResponse;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.response.PhotoUploadIntentWebResponse;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.mappers.CarWebMapper;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.security.AuthenticatedActor;
import com.al.fiap.cs.dealership.ports.services.CarServicePort;
import com.al.fiap.cs.dealership.ports.services.dtos.request.PurchaseCarRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@Tag(name = "Cars")
@RequestMapping("/api/v1/cars")
public class CarController {

	private final CarServicePort carService;

	public CarController(CarServicePort carService) {
		this.carService = carService;
	}

	@GetMapping
	public ResponseEntity<PagedCarsWebResponse> list(
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String q,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@RequestParam(defaultValue = "price,asc") String sort) {
		return ResponseEntity.ok(CarWebMapper.toWebResponse(
			carService.search(CarWebMapper.toSearchRequest(status, q, page, size, sort))
		));
	}

	@GetMapping("/{id}")
	public ResponseEntity<CarWebResponse> get(@PathVariable Long id) {
		return ResponseEntity.ok(CarWebMapper.toWebResponse(carService.get(id)));
	}

	@PostMapping
	public ResponseEntity<CarWebResponse> create(
			AuthenticatedActor actor,
			@Valid @RequestBody CreateCarWebRequest request) {
		requireAdmin(actor);
		return ResponseEntity.status(HttpStatus.CREATED).body(CarWebMapper.toWebResponse(
			carService.create(CarWebMapper.toCreateRequest(request, actor.actorId()))
		));
	}

	@PutMapping("/{id}")
	public ResponseEntity<CarWebResponse> update(
			AuthenticatedActor actor,
			@PathVariable Long id,
			@Valid @RequestBody UpdateCarWebRequest request) {
		requireAdmin(actor);
		return ResponseEntity.ok(CarWebMapper.toWebResponse(
			carService.update(CarWebMapper.toUpdateRequest(id, request, actor.actorId()))
		));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(AuthenticatedActor actor, @PathVariable Long id) {
		requireAdmin(actor);
		carService.delete(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/purchase")
	public ResponseEntity<CarWebResponse> purchase(AuthenticatedActor actor, @PathVariable Long id) {
		if (!actor.hasRole("ROLE_CUSTOMER") || actor.accountId() == null) {
			throw new IllegalStateException("CUSTOMER role with accountId required");
		}
		return ResponseEntity.ok(CarWebMapper.toWebResponse(
			carService.purchase(new PurchaseCarRequest(id, actor.accountId(), actor.accountId()))
		));
	}

	@PostMapping("/{id}/photos/upload-intent")
	public ResponseEntity<PhotoUploadIntentWebResponse> uploadIntent(
			AuthenticatedActor actor,
			@PathVariable Long id) {
		requireAdmin(actor);
		return ResponseEntity.ok(CarWebMapper.toWebResponse(
			carService.createUploadIntent(CarWebMapper.toUploadIntentRequest(id, actor.actorId()))
		));
	}

	@PostMapping("/{id}/photos/complete")
	public ResponseEntity<CarWebResponse> completePhoto(
			AuthenticatedActor actor,
			@PathVariable Long id,
			@Valid @RequestBody CompletePhotoUploadWebRequest request) {
		requireAdmin(actor);
		return ResponseEntity.ok(CarWebMapper.toWebResponse(
			carService.completePhoto(CarWebMapper.toCompleteRequest(id, request, actor.actorId()))
		));
	}

	private static void requireAdmin(AuthenticatedActor actor) {
		if (!actor.hasRole("ROLE_ADMIN")) {
			throw new IllegalStateException("ADMIN role required");
		}
	}
}

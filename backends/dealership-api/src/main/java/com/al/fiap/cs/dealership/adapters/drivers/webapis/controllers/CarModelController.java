package com.al.fiap.cs.dealership.adapters.drivers.webapis.controllers;

import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.request.CreateCarModelWebRequest;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.request.UpdateCarModelWebRequest;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.response.CarModelWebResponse;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.mappers.CarModelWebMapper;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.security.AuthenticatedActor;
import com.al.fiap.cs.dealership.ports.services.CarModelServicePort;
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

import java.util.List;

@RestController
@Validated
@Tag(name = "Models")
@RequestMapping("/api/v1/models")
public class CarModelController {

	private final CarModelServicePort carModelService;

	public CarModelController(CarModelServicePort carModelService) {
		this.carModelService = carModelService;
	}

	@GetMapping
	public ResponseEntity<List<CarModelWebResponse>> list(@RequestParam(required = false) Long brandId) {
		return ResponseEntity.ok(carModelService.list(brandId).stream().map(CarModelWebMapper::toWebResponse).toList());
	}

	@GetMapping("/{id}")
	public ResponseEntity<CarModelWebResponse> get(@PathVariable Long id) {
		return ResponseEntity.ok(CarModelWebMapper.toWebResponse(carModelService.get(id)));
	}

	@PostMapping
	public ResponseEntity<CarModelWebResponse> create(
			AuthenticatedActor actor,
			@Valid @RequestBody CreateCarModelWebRequest request) {
		requireAdmin(actor);
		return ResponseEntity.status(HttpStatus.CREATED).body(CarModelWebMapper.toWebResponse(
			carModelService.create(CarModelWebMapper.toCreateRequest(request, actor.actorId()))
		));
	}

	@PutMapping("/{id}")
	public ResponseEntity<CarModelWebResponse> update(
			AuthenticatedActor actor,
			@PathVariable Long id,
			@Valid @RequestBody UpdateCarModelWebRequest request) {
		requireAdmin(actor);
		return ResponseEntity.ok(CarModelWebMapper.toWebResponse(
			carModelService.update(CarModelWebMapper.toUpdateRequest(id, request, actor.actorId()))
		));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(AuthenticatedActor actor, @PathVariable Long id) {
		requireAdmin(actor);
		carModelService.delete(id);
		return ResponseEntity.noContent().build();
	}

	private static void requireAdmin(AuthenticatedActor actor) {
		if (!actor.hasRole("ROLE_ADMIN")) {
			throw new IllegalStateException("ADMIN role required");
		}
	}
}

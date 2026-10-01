package com.al.fiap.cs.dealership.adapters.drivers.webapis.controllers;

import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.request.CreateVehicleYearWebRequest;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.request.UpdateVehicleYearWebRequest;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.response.VehicleYearWebResponse;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.mappers.VehicleYearWebMapper;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.security.AuthenticatedActor;
import com.al.fiap.cs.dealership.ports.services.VehicleYearServicePort;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@Tag(name = "Years")
@RequestMapping("/api/v1/years")
public class VehicleYearController {

	private final VehicleYearServicePort vehicleYearService;

	public VehicleYearController(VehicleYearServicePort vehicleYearService) {
		this.vehicleYearService = vehicleYearService;
	}

	@GetMapping
	public ResponseEntity<List<VehicleYearWebResponse>> list() {
		return ResponseEntity.ok(vehicleYearService.list().stream().map(VehicleYearWebMapper::toWebResponse).toList());
	}

	@GetMapping("/{id}")
	public ResponseEntity<VehicleYearWebResponse> get(@PathVariable Long id) {
		return ResponseEntity.ok(VehicleYearWebMapper.toWebResponse(vehicleYearService.get(id)));
	}

	@PostMapping
	public ResponseEntity<VehicleYearWebResponse> create(
			AuthenticatedActor actor,
			@Valid @RequestBody CreateVehicleYearWebRequest request) {
		requireAdmin(actor);
		return ResponseEntity.status(HttpStatus.CREATED).body(VehicleYearWebMapper.toWebResponse(
			vehicleYearService.create(VehicleYearWebMapper.toCreateRequest(request, actor.actorId()))
		));
	}

	@PutMapping("/{id}")
	public ResponseEntity<VehicleYearWebResponse> update(
			AuthenticatedActor actor,
			@PathVariable Long id,
			@Valid @RequestBody UpdateVehicleYearWebRequest request) {
		requireAdmin(actor);
		return ResponseEntity.ok(VehicleYearWebMapper.toWebResponse(
			vehicleYearService.update(VehicleYearWebMapper.toUpdateRequest(id, request, actor.actorId()))
		));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(AuthenticatedActor actor, @PathVariable Long id) {
		requireAdmin(actor);
		vehicleYearService.delete(id);
		return ResponseEntity.noContent().build();
	}

	private static void requireAdmin(AuthenticatedActor actor) {
		if (!actor.hasRole("ROLE_ADMIN")) {
			throw new IllegalStateException("ADMIN role required");
		}
	}
}

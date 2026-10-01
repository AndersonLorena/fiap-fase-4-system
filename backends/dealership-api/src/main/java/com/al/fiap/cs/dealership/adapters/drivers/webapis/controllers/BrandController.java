package com.al.fiap.cs.dealership.adapters.drivers.webapis.controllers;

import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.request.CreateBrandWebRequest;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.request.UpdateBrandWebRequest;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.response.BrandWebResponse;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.mappers.BrandWebMapper;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.security.AuthenticatedActor;
import com.al.fiap.cs.dealership.ports.services.BrandServicePort;
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
@Tag(name = "Brands")
@RequestMapping("/api/v1/brands")
public class BrandController {

	private final BrandServicePort brandService;

	public BrandController(BrandServicePort brandService) {
		this.brandService = brandService;
	}

	@GetMapping
	public ResponseEntity<List<BrandWebResponse>> list() {
		return ResponseEntity.ok(brandService.list().stream().map(BrandWebMapper::toWebResponse).toList());
	}

	@GetMapping("/{id}")
	public ResponseEntity<BrandWebResponse> get(@PathVariable Long id) {
		return ResponseEntity.ok(BrandWebMapper.toWebResponse(brandService.get(id)));
	}

	@PostMapping
	public ResponseEntity<BrandWebResponse> create(
			AuthenticatedActor actor,
			@Valid @RequestBody CreateBrandWebRequest request) {
		requireAdmin(actor);
		BrandWebResponse response = BrandWebMapper.toWebResponse(
			brandService.create(BrandWebMapper.toCreateRequest(request, actor.actorId()))
		);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PutMapping("/{id}")
	public ResponseEntity<BrandWebResponse> update(
			AuthenticatedActor actor,
			@PathVariable Long id,
			@Valid @RequestBody UpdateBrandWebRequest request) {
		requireAdmin(actor);
		return ResponseEntity.ok(BrandWebMapper.toWebResponse(
			brandService.update(BrandWebMapper.toUpdateRequest(id, request, actor.actorId()))
		));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(AuthenticatedActor actor, @PathVariable Long id) {
		requireAdmin(actor);
		brandService.delete(id);
		return ResponseEntity.noContent().build();
	}

	private static void requireAdmin(AuthenticatedActor actor) {
		if (!actor.hasRole("ROLE_ADMIN")) {
			throw new IllegalStateException("ADMIN role required");
		}
	}
}

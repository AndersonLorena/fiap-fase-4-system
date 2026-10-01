package com.al.fiap.cs.dealership.adapters.drivers.webapis.controllers;

import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.request.CreateColorWebRequest;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.request.UpdateColorWebRequest;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.response.ColorWebResponse;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.mappers.ColorWebMapper;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.security.AuthenticatedActor;
import com.al.fiap.cs.dealership.ports.services.ColorServicePort;
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
@Tag(name = "Colors")
@RequestMapping("/api/v1/colors")
public class ColorController {

	private final ColorServicePort colorService;

	public ColorController(ColorServicePort colorService) {
		this.colorService = colorService;
	}

	@GetMapping
	public ResponseEntity<List<ColorWebResponse>> list() {
		return ResponseEntity.ok(colorService.list().stream().map(ColorWebMapper::toWebResponse).toList());
	}

	@GetMapping("/{id}")
	public ResponseEntity<ColorWebResponse> get(@PathVariable Long id) {
		return ResponseEntity.ok(ColorWebMapper.toWebResponse(colorService.get(id)));
	}

	@PostMapping
	public ResponseEntity<ColorWebResponse> create(
			AuthenticatedActor actor,
			@Valid @RequestBody CreateColorWebRequest request) {
		requireAdmin(actor);
		return ResponseEntity.status(HttpStatus.CREATED).body(ColorWebMapper.toWebResponse(
			colorService.create(ColorWebMapper.toCreateRequest(request, actor.actorId()))
		));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ColorWebResponse> update(
			AuthenticatedActor actor,
			@PathVariable Long id,
			@Valid @RequestBody UpdateColorWebRequest request) {
		requireAdmin(actor);
		return ResponseEntity.ok(ColorWebMapper.toWebResponse(
			colorService.update(ColorWebMapper.toUpdateRequest(id, request, actor.actorId()))
		));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(AuthenticatedActor actor, @PathVariable Long id) {
		requireAdmin(actor);
		colorService.delete(id);
		return ResponseEntity.noContent().build();
	}

	private static void requireAdmin(AuthenticatedActor actor) {
		if (!actor.hasRole("ROLE_ADMIN")) {
			throw new IllegalStateException("ADMIN role required");
		}
	}
}

package com.al.fiap.cs.account.adapters.drivers.webapis.controllers;

import com.al.fiap.cs.account.adapters.drivers.webapis.dtos.request.AuthenticateWebRequest;
import com.al.fiap.cs.account.adapters.drivers.webapis.dtos.request.RefreshTokenWebRequest;
import com.al.fiap.cs.account.adapters.drivers.webapis.dtos.response.TokenWebResponse;
import com.al.fiap.cs.account.adapters.drivers.webapis.mappers.AccountWebMapper;
import com.al.fiap.cs.account.ports.services.AuthenticateServicePort;
import com.al.fiap.cs.account.ports.services.RefreshTokenServicePort;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@Tag(name = "Auth")
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthenticateServicePort authenticateService;
	private final RefreshTokenServicePort refreshTokenService;

	public AuthController(
			AuthenticateServicePort authenticateService,
			RefreshTokenServicePort refreshTokenService) {
		this.authenticateService = authenticateService;
		this.refreshTokenService = refreshTokenService;
	}

	@PostMapping("/login")
	public ResponseEntity<TokenWebResponse> login(@Valid @RequestBody AuthenticateWebRequest request) {
		TokenWebResponse response = AccountWebMapper.toWebResponse(
			authenticateService.authenticate(AccountWebMapper.toServiceRequest(request))
		);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/refresh")
	public ResponseEntity<TokenWebResponse> refresh(@Valid @RequestBody RefreshTokenWebRequest request) {
		TokenWebResponse response = AccountWebMapper.toWebResponse(
			refreshTokenService.refresh(AccountWebMapper.toServiceRequest(request))
		);
		return ResponseEntity.ok(response);
	}
}

package com.al.fiap.cs.account.adapters.drivers.webapis.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenWebRequest(
		@NotBlank String refreshToken
) {
}

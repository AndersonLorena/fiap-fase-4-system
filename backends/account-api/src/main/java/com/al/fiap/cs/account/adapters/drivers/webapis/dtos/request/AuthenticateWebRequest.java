package com.al.fiap.cs.account.adapters.drivers.webapis.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthenticateWebRequest(
		@NotBlank @Size(max = 255) String email,
		@NotBlank @Size(min = 8, max = 128) String password
) {
}

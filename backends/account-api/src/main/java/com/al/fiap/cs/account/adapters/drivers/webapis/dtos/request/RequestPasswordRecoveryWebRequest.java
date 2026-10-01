package com.al.fiap.cs.account.adapters.drivers.webapis.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RequestPasswordRecoveryWebRequest(
		@NotBlank @Size(max = 255) String email
) {
}

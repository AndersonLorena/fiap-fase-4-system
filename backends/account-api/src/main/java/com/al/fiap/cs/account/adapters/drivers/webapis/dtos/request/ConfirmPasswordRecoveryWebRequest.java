package com.al.fiap.cs.account.adapters.drivers.webapis.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConfirmPasswordRecoveryWebRequest(
		@NotBlank @Size(max = 255) String email,
		@NotBlank @Size(min = 6, max = 6) String code,
		@NotBlank @Size(min = 8, max = 128) String newPassword
) {
}

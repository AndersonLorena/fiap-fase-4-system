package com.al.fiap.cs.account.adapters.drivers.webapis.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompleteProfileWebRequest(
		@NotBlank @Size(min = 11, max = 20) String document,
		@NotBlank @Size(min = 10, max = 20) String phone
) {
}

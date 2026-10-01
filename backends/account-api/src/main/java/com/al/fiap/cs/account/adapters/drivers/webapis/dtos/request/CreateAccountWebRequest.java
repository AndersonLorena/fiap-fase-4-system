package com.al.fiap.cs.account.adapters.drivers.webapis.dtos.request;

import com.al.fiap.cs.account.core.domain.account.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAccountWebRequest(
		@NotBlank @Size(max = 255) String email,
		@NotBlank @Size(min = 8, max = 128) String password,
		@NotBlank @Size(min = 2, max = 120) String fullName,
		AccountType type
) {
}

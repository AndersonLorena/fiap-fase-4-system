package com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateBrandWebRequest(
		@NotBlank @Size(min = 1, max = 80) String name
) {
}

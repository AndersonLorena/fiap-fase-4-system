package com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateColorWebRequest(
		@NotBlank @Size(min = 1, max = 60) String name
) {
}

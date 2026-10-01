package com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ApplyPaymentWebRequest(
		@NotBlank @Pattern(regexp = "PAID|CANCELLED", message = "status must be PAID or CANCELLED") String status) {
}

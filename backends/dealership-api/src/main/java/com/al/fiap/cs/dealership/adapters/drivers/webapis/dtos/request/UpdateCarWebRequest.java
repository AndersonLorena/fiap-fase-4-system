package com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateCarWebRequest(
		@NotNull Long brandId,
		@NotNull Long modelId,
		@NotNull Long colorId,
		@NotNull Long yearId,
		@NotNull @DecimalMin(value = "0.01", inclusive = true) @Digits(integer = 12, fraction = 2) BigDecimal price
) {
}

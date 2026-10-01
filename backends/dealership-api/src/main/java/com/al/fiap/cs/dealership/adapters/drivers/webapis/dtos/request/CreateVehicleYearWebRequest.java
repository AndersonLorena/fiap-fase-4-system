package com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateVehicleYearWebRequest(
		@NotNull @Min(1900) @Max(2100) Integer year
) {
}

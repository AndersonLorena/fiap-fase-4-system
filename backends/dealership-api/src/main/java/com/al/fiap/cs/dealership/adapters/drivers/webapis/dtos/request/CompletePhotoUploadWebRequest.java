package com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CompletePhotoUploadWebRequest(
		@NotBlank String objectKey,
		@NotBlank String intentToken,
		@NotNull @Min(0) Integer sortOrder
) {
}

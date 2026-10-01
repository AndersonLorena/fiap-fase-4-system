package com.al.fiap.cs.dealership.adapters.drivers.webapis.mappers;

import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.request.CreateBrandWebRequest;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.request.UpdateBrandWebRequest;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.response.BrandWebResponse;
import com.al.fiap.cs.dealership.ports.services.dtos.request.CreateBrandRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.request.UpdateBrandRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.response.BrandResponse;

public final class BrandWebMapper {

	private BrandWebMapper() {
	}

	public static CreateBrandRequest toCreateRequest(CreateBrandWebRequest request, Long actorId) {
		return new CreateBrandRequest(request.name(), actorId);
	}

	public static UpdateBrandRequest toUpdateRequest(Long brandId, UpdateBrandWebRequest request, Long actorId) {
		return new UpdateBrandRequest(brandId, request.name(), actorId);
	}

	public static BrandWebResponse toWebResponse(BrandResponse response) {
		return new BrandWebResponse(response.brandId(), response.name());
	}
}

package com.al.fiap.cs.dealership.adapters.drivers.webapis.mappers;

import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.request.CreateColorWebRequest;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.request.UpdateColorWebRequest;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.response.ColorWebResponse;
import com.al.fiap.cs.dealership.ports.services.dtos.request.CreateColorRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.request.UpdateColorRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.response.ColorResponse;

public final class ColorWebMapper {
	private ColorWebMapper() {}
	public static CreateColorRequest toCreateRequest(CreateColorWebRequest request, Long actorId) {
		return new CreateColorRequest(request.name(), actorId);
	}
	public static UpdateColorRequest toUpdateRequest(Long colorId, UpdateColorWebRequest request, Long actorId) {
		return new UpdateColorRequest(colorId, request.name(), actorId);
	}
	public static ColorWebResponse toWebResponse(ColorResponse response) {
		return new ColorWebResponse(response.colorId(), response.name());
	}
}

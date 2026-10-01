package com.al.fiap.cs.dealership.adapters.drivers.webapis.mappers;

import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.request.CreateCarModelWebRequest;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.request.UpdateCarModelWebRequest;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.response.CarModelWebResponse;
import com.al.fiap.cs.dealership.ports.services.dtos.request.CreateCarModelRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.request.UpdateCarModelRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.response.CarModelResponse;

public final class CarModelWebMapper {
	private CarModelWebMapper() {}
	public static CreateCarModelRequest toCreateRequest(CreateCarModelWebRequest request, Long actorId) {
		return new CreateCarModelRequest(request.brandId(), request.name(), actorId);
	}
	public static UpdateCarModelRequest toUpdateRequest(Long modelId, UpdateCarModelWebRequest request, Long actorId) {
		return new UpdateCarModelRequest(modelId, request.name(), actorId);
	}
	public static CarModelWebResponse toWebResponse(CarModelResponse response) {
		return new CarModelWebResponse(response.modelId(), response.brandId(), response.name());
	}
}

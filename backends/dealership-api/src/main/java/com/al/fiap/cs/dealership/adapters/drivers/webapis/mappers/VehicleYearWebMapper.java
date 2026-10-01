package com.al.fiap.cs.dealership.adapters.drivers.webapis.mappers;

import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.request.CreateVehicleYearWebRequest;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.request.UpdateVehicleYearWebRequest;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.response.VehicleYearWebResponse;
import com.al.fiap.cs.dealership.ports.services.dtos.request.CreateVehicleYearRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.request.UpdateVehicleYearRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.response.VehicleYearResponse;

public final class VehicleYearWebMapper {
	private VehicleYearWebMapper() {}
	public static CreateVehicleYearRequest toCreateRequest(CreateVehicleYearWebRequest request, Long actorId) {
		return new CreateVehicleYearRequest(request.year(), actorId);
	}
	public static UpdateVehicleYearRequest toUpdateRequest(Long yearId, UpdateVehicleYearWebRequest request, Long actorId) {
		return new UpdateVehicleYearRequest(yearId, request.year(), actorId);
	}
	public static VehicleYearWebResponse toWebResponse(VehicleYearResponse response) {
		return new VehicleYearWebResponse(response.yearId(), response.year());
	}
}

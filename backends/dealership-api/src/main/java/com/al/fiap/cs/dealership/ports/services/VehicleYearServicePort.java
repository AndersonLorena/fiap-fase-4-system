package com.al.fiap.cs.dealership.ports.services;

import com.al.fiap.cs.dealership.ports.services.dtos.request.CreateVehicleYearRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.request.UpdateVehicleYearRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.response.VehicleYearResponse;

import java.util.List;

public interface VehicleYearServicePort {

	VehicleYearResponse create(CreateVehicleYearRequest request);

	VehicleYearResponse update(UpdateVehicleYearRequest request);

	VehicleYearResponse get(Long yearId);

	List<VehicleYearResponse> list();

	void delete(Long yearId);
}

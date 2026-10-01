package com.al.fiap.cs.dealership.ports.services;

import com.al.fiap.cs.dealership.ports.services.dtos.request.CreateCarModelRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.request.UpdateCarModelRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.response.CarModelResponse;

import java.util.List;

public interface CarModelServicePort {
	CarModelResponse create(CreateCarModelRequest request);
	CarModelResponse update(UpdateCarModelRequest request);
	CarModelResponse get(Long modelId);
	List<CarModelResponse> list(Long brandId);
	void delete(Long modelId);
}

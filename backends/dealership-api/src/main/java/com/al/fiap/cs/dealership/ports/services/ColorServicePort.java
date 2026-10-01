package com.al.fiap.cs.dealership.ports.services;

import com.al.fiap.cs.dealership.ports.services.dtos.request.CreateColorRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.request.UpdateColorRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.response.ColorResponse;

import java.util.List;

public interface ColorServicePort {

	ColorResponse create(CreateColorRequest request);

	ColorResponse update(UpdateColorRequest request);

	ColorResponse get(Long colorId);

	List<ColorResponse> list();

	void delete(Long colorId);
}

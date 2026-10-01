package com.al.fiap.cs.dealership.ports.services;

import com.al.fiap.cs.dealership.ports.services.dtos.request.CreateBrandRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.request.UpdateBrandRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.response.BrandResponse;

import java.util.List;

public interface BrandServicePort {
	BrandResponse create(CreateBrandRequest request);
	BrandResponse update(UpdateBrandRequest request);
	BrandResponse get(Long brandId);
	List<BrandResponse> list();
	void delete(Long brandId);
}

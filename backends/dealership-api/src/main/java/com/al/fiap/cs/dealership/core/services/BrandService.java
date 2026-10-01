package com.al.fiap.cs.dealership.core.services;

import com.al.fiap.cs.dealership.core.domain.catalog.Brand;
import com.al.fiap.cs.dealership.core.domain.exceptions.BrandAlreadyExistsException;
import com.al.fiap.cs.dealership.core.domain.exceptions.BrandInUseException;
import com.al.fiap.cs.dealership.core.domain.exceptions.BrandNotFoundException;
import com.al.fiap.cs.dealership.ports.repositories.BrandRepositoryPort;
import com.al.fiap.cs.dealership.ports.repositories.CarModelRepositoryPort;
import com.al.fiap.cs.dealership.ports.repositories.CarRepositoryPort;
import com.al.fiap.cs.dealership.ports.services.BrandServicePort;
import com.al.fiap.cs.dealership.ports.services.dtos.request.CreateBrandRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.request.UpdateBrandRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.response.BrandResponse;

import java.util.List;

public class BrandService implements BrandServicePort {

	private final BrandRepositoryPort brandRepository;
	private final CarModelRepositoryPort carModelRepository;
	private final CarRepositoryPort carRepository;

	public BrandService(
			BrandRepositoryPort brandRepository,
			CarModelRepositoryPort carModelRepository,
			CarRepositoryPort carRepository) {
		this.brandRepository = brandRepository;
		this.carModelRepository = carModelRepository;
		this.carRepository = carRepository;
	}

	@Override
	public BrandResponse create(CreateBrandRequest request) {
		if (brandRepository.existsByName(request.name())) {
			throw new BrandAlreadyExistsException("Brand already exists");
		}
		Brand brand = Brand.create(request.name(), request.actorId());
		return toResponse(brandRepository.save(brand));
	}

	@Override
	public BrandResponse update(UpdateBrandRequest request) {
		Brand brand = brandRepository.findById(request.brandId())
			.orElseThrow(() -> new BrandNotFoundException("Brand not found"));
		if (brandRepository.existsByNameAndIdNot(request.name(), request.brandId())) {
			throw new BrandAlreadyExistsException("Brand already exists");
		}
		brand.rename(request.name(), request.actorId());
		return toResponse(brandRepository.save(brand));
	}

	@Override
	public BrandResponse get(Long brandId) {
		return brandRepository.findById(brandId)
			.map(BrandService::toResponse)
			.orElseThrow(() -> new BrandNotFoundException("Brand not found"));
	}

	@Override
	public List<BrandResponse> list() {
		return brandRepository.findAll().stream().map(BrandService::toResponse).toList();
	}

	@Override
	public void delete(Long brandId) {
		brandRepository.findById(brandId)
			.orElseThrow(() -> new BrandNotFoundException("Brand not found"));
		if (carModelRepository.existsByBrandId(brandId) || carRepository.existsByBrandId(brandId)) {
			throw new BrandInUseException("Brand is in use");
		}
		brandRepository.deleteById(brandId);
	}

	private static BrandResponse toResponse(Brand brand) {
		return new BrandResponse(brand.id(), brand.name());
	}
}

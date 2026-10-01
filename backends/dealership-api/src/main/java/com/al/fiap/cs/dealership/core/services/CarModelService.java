package com.al.fiap.cs.dealership.core.services;

import com.al.fiap.cs.dealership.core.domain.catalog.CarModel;
import com.al.fiap.cs.dealership.core.domain.exceptions.BrandNotFoundException;
import com.al.fiap.cs.dealership.core.domain.exceptions.CarModelAlreadyExistsException;
import com.al.fiap.cs.dealership.core.domain.exceptions.CarModelInUseException;
import com.al.fiap.cs.dealership.core.domain.exceptions.CarModelNotFoundException;
import com.al.fiap.cs.dealership.ports.repositories.BrandRepositoryPort;
import com.al.fiap.cs.dealership.ports.repositories.CarModelRepositoryPort;
import com.al.fiap.cs.dealership.ports.repositories.CarRepositoryPort;
import com.al.fiap.cs.dealership.ports.services.CarModelServicePort;
import com.al.fiap.cs.dealership.ports.services.dtos.request.CreateCarModelRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.request.UpdateCarModelRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.response.CarModelResponse;

import java.util.List;

public class CarModelService implements CarModelServicePort {

	private final CarModelRepositoryPort carModelRepository;
	private final BrandRepositoryPort brandRepository;
	private final CarRepositoryPort carRepository;

	public CarModelService(
			CarModelRepositoryPort carModelRepository,
			BrandRepositoryPort brandRepository,
			CarRepositoryPort carRepository) {
		this.carModelRepository = carModelRepository;
		this.brandRepository = brandRepository;
		this.carRepository = carRepository;
	}

	@Override
	public CarModelResponse create(CreateCarModelRequest request) {
		brandRepository.findById(request.brandId())
			.orElseThrow(() -> new BrandNotFoundException("Brand not found"));
		if (carModelRepository.existsByBrandIdAndName(request.brandId(), request.name())) {
			throw new CarModelAlreadyExistsException("Car model already exists");
		}
		CarModel model = CarModel.create(request.brandId(), request.name(), request.actorId());
		return toResponse(carModelRepository.save(model));
	}

	@Override
	public CarModelResponse update(UpdateCarModelRequest request) {
		CarModel model = carModelRepository.findById(request.modelId())
			.orElseThrow(() -> new CarModelNotFoundException("Car model not found"));
		if (carModelRepository.existsByBrandIdAndNameAndIdNot(model.brandId(), request.name(), request.modelId())) {
			throw new CarModelAlreadyExistsException("Car model already exists");
		}
		model.rename(request.name(), request.actorId());
		return toResponse(carModelRepository.save(model));
	}

	@Override
	public CarModelResponse get(Long modelId) {
		return carModelRepository.findById(modelId)
			.map(CarModelService::toResponse)
			.orElseThrow(() -> new CarModelNotFoundException("Car model not found"));
	}

	@Override
	public List<CarModelResponse> list(Long brandId) {
		List<CarModel> models = brandId == null
			? carModelRepository.findAll()
			: carModelRepository.findAllByBrandId(brandId);
		return models.stream().map(CarModelService::toResponse).toList();
	}

	@Override
	public void delete(Long modelId) {
		carModelRepository.findById(modelId)
			.orElseThrow(() -> new CarModelNotFoundException("Car model not found"));
		if (carRepository.existsByModelId(modelId)) {
			throw new CarModelInUseException("Car model is in use");
		}
		carModelRepository.deleteById(modelId);
	}

	private static CarModelResponse toResponse(CarModel model) {
		return new CarModelResponse(model.id(), model.brandId(), model.name());
	}
}

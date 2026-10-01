package com.al.fiap.cs.dealership.adapters.drivens.repositories;

import com.al.fiap.cs.dealership.adapters.drivens.repositories.jpa.CarModelJpaEntity;
import com.al.fiap.cs.dealership.adapters.drivens.repositories.jpa.CarModelSpringDataRepository;
import com.al.fiap.cs.dealership.core.domain.catalog.CarModel;
import com.al.fiap.cs.dealership.ports.repositories.CarModelRepositoryPort;

import java.util.List;
import java.util.Optional;

public class CarModelRepositoryAdapter implements CarModelRepositoryPort {

	private final CarModelSpringDataRepository repository;

	public CarModelRepositoryAdapter(CarModelSpringDataRepository repository) {
		this.repository = repository;
	}

	@Override
	public CarModel save(CarModel model) {
		repository.save(toEntity(model));
		return model;
	}

	@Override
	public Optional<CarModel> findById(Long id) {
		return repository.findById(id).map(CarModelRepositoryAdapter::toDomain);
	}

	@Override
	public boolean existsByBrandIdAndName(Long brandId, String name) {
		return repository.existsByBrandIdAndName(brandId, name);
	}

	@Override
	public boolean existsByBrandIdAndNameAndIdNot(Long brandId, String name, Long id) {
		return repository.existsByBrandIdAndNameAndIdNot(brandId, name, id);
	}

	@Override
	public boolean existsByBrandId(Long brandId) {
		return repository.existsByBrandId(brandId);
	}

	@Override
	public List<CarModel> findAll() {
		return repository.findAll().stream()
			.map(CarModelRepositoryAdapter::toDomain)
			.toList();
	}

	@Override
	public List<CarModel> findAllByBrandId(Long brandId) {
		return repository.findAllByBrandId(brandId).stream()
			.map(CarModelRepositoryAdapter::toDomain)
			.toList();
	}

	@Override
	public void deleteById(Long id) {
		repository.deleteById(id);
	}

	static CarModelJpaEntity toEntity(CarModel model) {
		CarModelJpaEntity entity = new CarModelJpaEntity();
		entity.setId(model.id());
		entity.setBrandId(model.brandId());
		entity.setName(model.name());
		entity.setCreatedAt(model.createdAt());
		entity.setCreatedBy(model.createdBy());
		entity.setUpdatedAt(model.updatedAt());
		entity.setUpdatedBy(model.updatedBy());
		return entity;
	}

	static CarModel toDomain(CarModelJpaEntity entity) {
		return CarModel.restore(
			entity.getId(),
			entity.getCreatedAt(),
			entity.getCreatedBy(),
			entity.getUpdatedAt(),
			entity.getUpdatedBy(),
			entity.getBrandId(),
			entity.getName()
		);
	}
}

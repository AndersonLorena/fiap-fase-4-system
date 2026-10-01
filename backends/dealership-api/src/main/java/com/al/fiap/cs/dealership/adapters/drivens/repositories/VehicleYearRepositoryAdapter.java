package com.al.fiap.cs.dealership.adapters.drivens.repositories;

import com.al.fiap.cs.dealership.adapters.drivens.repositories.jpa.VehicleYearJpaEntity;
import com.al.fiap.cs.dealership.adapters.drivens.repositories.jpa.VehicleYearSpringDataRepository;
import com.al.fiap.cs.dealership.core.domain.catalog.VehicleYear;
import com.al.fiap.cs.dealership.ports.repositories.VehicleYearRepositoryPort;

import java.util.List;
import java.util.Optional;

public class VehicleYearRepositoryAdapter implements VehicleYearRepositoryPort {

	private final VehicleYearSpringDataRepository repository;

	public VehicleYearRepositoryAdapter(VehicleYearSpringDataRepository repository) {
		this.repository = repository;
	}

	@Override
	public VehicleYear save(VehicleYear year) {
		repository.save(toEntity(year));
		return year;
	}

	@Override
	public Optional<VehicleYear> findById(Long id) {
		return repository.findById(id).map(VehicleYearRepositoryAdapter::toDomain);
	}

	@Override
	public boolean existsByYear(int year) {
		return repository.existsByYearValue(year);
	}

	@Override
	public boolean existsByYearAndIdNot(int year, Long id) {
		return repository.existsByYearValueAndIdNot(year, id);
	}

	@Override
	public List<VehicleYear> findAll() {
		return repository.findAll().stream()
			.map(VehicleYearRepositoryAdapter::toDomain)
			.toList();
	}

	@Override
	public void deleteById(Long id) {
		repository.deleteById(id);
	}

	static VehicleYearJpaEntity toEntity(VehicleYear year) {
		VehicleYearJpaEntity entity = new VehicleYearJpaEntity();
		entity.setId(year.id());
		entity.setYearValue(year.year());
		entity.setCreatedAt(year.createdAt());
		entity.setCreatedBy(year.createdBy());
		entity.setUpdatedAt(year.updatedAt());
		entity.setUpdatedBy(year.updatedBy());
		return entity;
	}

	static VehicleYear toDomain(VehicleYearJpaEntity entity) {
		return VehicleYear.restore(
			entity.getId(),
			entity.getCreatedAt(),
			entity.getCreatedBy(),
			entity.getUpdatedAt(),
			entity.getUpdatedBy(),
			entity.getYearValue()
		);
	}
}

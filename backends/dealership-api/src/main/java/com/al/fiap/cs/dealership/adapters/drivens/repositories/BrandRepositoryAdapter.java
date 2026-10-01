package com.al.fiap.cs.dealership.adapters.drivens.repositories;

import com.al.fiap.cs.dealership.adapters.drivens.repositories.jpa.BrandJpaEntity;
import com.al.fiap.cs.dealership.adapters.drivens.repositories.jpa.BrandSpringDataRepository;
import com.al.fiap.cs.dealership.core.domain.catalog.Brand;
import com.al.fiap.cs.dealership.ports.repositories.BrandRepositoryPort;

import java.util.List;
import java.util.Optional;

public class BrandRepositoryAdapter implements BrandRepositoryPort {

	private final BrandSpringDataRepository repository;

	public BrandRepositoryAdapter(BrandSpringDataRepository repository) {
		this.repository = repository;
	}

	@Override
	public Brand save(Brand brand) {
		repository.save(toEntity(brand));
		return brand;
	}

	@Override
	public Optional<Brand> findById(Long id) {
		return repository.findById(id).map(BrandRepositoryAdapter::toDomain);
	}

	@Override
	public boolean existsByName(String name) {
		return repository.existsByName(name);
	}

	@Override
	public boolean existsByNameAndIdNot(String name, Long id) {
		return repository.existsByNameAndIdNot(name, id);
	}

	@Override
	public List<Brand> findAll() {
		return repository.findAll().stream()
			.map(BrandRepositoryAdapter::toDomain)
			.toList();
	}

	@Override
	public void deleteById(Long id) {
		repository.deleteById(id);
	}

	static BrandJpaEntity toEntity(Brand brand) {
		BrandJpaEntity entity = new BrandJpaEntity();
		entity.setId(brand.id());
		entity.setName(brand.name());
		entity.setCreatedAt(brand.createdAt());
		entity.setCreatedBy(brand.createdBy());
		entity.setUpdatedAt(brand.updatedAt());
		entity.setUpdatedBy(brand.updatedBy());
		return entity;
	}

	static Brand toDomain(BrandJpaEntity entity) {
		return Brand.restore(
			entity.getId(),
			entity.getCreatedAt(),
			entity.getCreatedBy(),
			entity.getUpdatedAt(),
			entity.getUpdatedBy(),
			entity.getName()
		);
	}
}

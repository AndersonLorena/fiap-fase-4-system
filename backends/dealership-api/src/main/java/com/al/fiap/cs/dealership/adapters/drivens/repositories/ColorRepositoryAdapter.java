package com.al.fiap.cs.dealership.adapters.drivens.repositories;

import com.al.fiap.cs.dealership.adapters.drivens.repositories.jpa.ColorJpaEntity;
import com.al.fiap.cs.dealership.adapters.drivens.repositories.jpa.ColorSpringDataRepository;
import com.al.fiap.cs.dealership.core.domain.catalog.Color;
import com.al.fiap.cs.dealership.ports.repositories.ColorRepositoryPort;

import java.util.List;
import java.util.Optional;

public class ColorRepositoryAdapter implements ColorRepositoryPort {

	private final ColorSpringDataRepository repository;

	public ColorRepositoryAdapter(ColorSpringDataRepository repository) {
		this.repository = repository;
	}

	@Override
	public Color save(Color color) {
		repository.save(toEntity(color));
		return color;
	}

	@Override
	public Optional<Color> findById(Long id) {
		return repository.findById(id).map(ColorRepositoryAdapter::toDomain);
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
	public List<Color> findAll() {
		return repository.findAll().stream()
			.map(ColorRepositoryAdapter::toDomain)
			.toList();
	}

	@Override
	public void deleteById(Long id) {
		repository.deleteById(id);
	}

	static ColorJpaEntity toEntity(Color color) {
		ColorJpaEntity entity = new ColorJpaEntity();
		entity.setId(color.id());
		entity.setName(color.name());
		entity.setCreatedAt(color.createdAt());
		entity.setCreatedBy(color.createdBy());
		entity.setUpdatedAt(color.updatedAt());
		entity.setUpdatedBy(color.updatedBy());
		return entity;
	}

	static Color toDomain(ColorJpaEntity entity) {
		return Color.restore(
			entity.getId(),
			entity.getCreatedAt(),
			entity.getCreatedBy(),
			entity.getUpdatedAt(),
			entity.getUpdatedBy(),
			entity.getName()
		);
	}
}

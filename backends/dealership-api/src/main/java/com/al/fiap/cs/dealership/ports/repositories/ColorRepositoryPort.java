package com.al.fiap.cs.dealership.ports.repositories;

import com.al.fiap.cs.dealership.core.domain.catalog.Color;

import java.util.List;
import java.util.Optional;

public interface ColorRepositoryPort {

	Color save(Color color);

	Optional<Color> findById(Long id);

	boolean existsByName(String name);

	boolean existsByNameAndIdNot(String name, Long id);

	List<Color> findAll();

	void deleteById(Long id);
}

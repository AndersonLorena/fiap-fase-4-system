package com.al.fiap.cs.dealership.ports.repositories;

import com.al.fiap.cs.dealership.core.domain.catalog.Brand;

import java.util.List;
import java.util.Optional;

public interface BrandRepositoryPort {

	Brand save(Brand brand);

	Optional<Brand> findById(Long id);

	boolean existsByName(String name);

	boolean existsByNameAndIdNot(String name, Long id);

	List<Brand> findAll();

	void deleteById(Long id);
}

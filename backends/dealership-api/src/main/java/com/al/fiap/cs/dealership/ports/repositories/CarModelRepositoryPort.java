package com.al.fiap.cs.dealership.ports.repositories;

import com.al.fiap.cs.dealership.core.domain.catalog.CarModel;

import java.util.List;
import java.util.Optional;

public interface CarModelRepositoryPort {

	CarModel save(CarModel model);

	Optional<CarModel> findById(Long id);

	boolean existsByBrandIdAndName(Long brandId, String name);

	boolean existsByBrandIdAndNameAndIdNot(Long brandId, String name, Long id);

	boolean existsByBrandId(Long brandId);

	List<CarModel> findAll();

	List<CarModel> findAllByBrandId(Long brandId);

	void deleteById(Long id);
}

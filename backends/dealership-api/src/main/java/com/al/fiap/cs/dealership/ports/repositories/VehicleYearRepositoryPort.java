package com.al.fiap.cs.dealership.ports.repositories;

import com.al.fiap.cs.dealership.core.domain.catalog.VehicleYear;

import java.util.List;
import java.util.Optional;

public interface VehicleYearRepositoryPort {

	VehicleYear save(VehicleYear year);

	Optional<VehicleYear> findById(Long id);

	boolean existsByYear(int year);

	boolean existsByYearAndIdNot(int year, Long id);

	List<VehicleYear> findAll();

	void deleteById(Long id);
}

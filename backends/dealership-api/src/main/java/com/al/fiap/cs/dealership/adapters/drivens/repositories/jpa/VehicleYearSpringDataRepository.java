package com.al.fiap.cs.dealership.adapters.drivens.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleYearSpringDataRepository extends JpaRepository<VehicleYearJpaEntity, Long> {

	boolean existsByYearValue(Integer yearValue);

	boolean existsByYearValueAndIdNot(Integer yearValue, Long id);
}

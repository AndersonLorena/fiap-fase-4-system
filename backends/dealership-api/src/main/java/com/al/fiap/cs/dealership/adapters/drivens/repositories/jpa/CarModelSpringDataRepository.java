package com.al.fiap.cs.dealership.adapters.drivens.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarModelSpringDataRepository extends JpaRepository<CarModelJpaEntity, Long> {

	boolean existsByBrandIdAndName(Long brandId, String name);

	boolean existsByBrandIdAndNameAndIdNot(Long brandId, String name, Long id);

	boolean existsByBrandId(Long brandId);

	List<CarModelJpaEntity> findAllByBrandId(Long brandId);
}

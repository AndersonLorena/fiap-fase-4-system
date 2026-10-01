package com.al.fiap.cs.dealership.adapters.drivens.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ColorSpringDataRepository extends JpaRepository<ColorJpaEntity, Long> {

	boolean existsByName(String name);

	boolean existsByNameAndIdNot(String name, Long id);
}

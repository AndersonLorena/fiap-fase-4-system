package com.al.fiap.cs.dealership.adapters.drivens.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandSpringDataRepository extends JpaRepository<BrandJpaEntity, Long> {

	boolean existsByName(String name);

	boolean existsByNameAndIdNot(String name, Long id);
}

package com.al.fiap.cs.dealership.adapters.drivens.repositories.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CarSpringDataRepository extends JpaRepository<CarJpaEntity, Long> {

	boolean existsByBrandId(Long brandId);

	boolean existsByModelId(Long modelId);

	boolean existsByColorId(Long colorId);

	boolean existsByYearId(Long yearId);

	Optional<CarJpaEntity> findByPaymentCode(String paymentCode);

	@Query("""
		SELECT c FROM CarJpaEntity c
		LEFT JOIN BrandJpaEntity b ON b.id = c.brandId
		LEFT JOIN CarModelJpaEntity m ON m.id = c.modelId
		LEFT JOIN ColorJpaEntity col ON col.id = c.colorId
		LEFT JOIN VehicleYearJpaEntity y ON y.id = c.yearId
		WHERE (:status IS NULL OR c.status = :status)
		AND (
		  :query IS NULL OR :query = ''
		  OR LOWER(b.name) LIKE LOWER(CONCAT('%', :query, '%'))
		  OR LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%'))
		  OR LOWER(col.name) LIKE LOWER(CONCAT('%', :query, '%'))
		  OR (:yearFilter IS NOT NULL AND y.yearValue = :yearFilter)
		)
	""")
	Page<CarJpaEntity> search(
			@Param("status") String status,
			@Param("query") String query,
			@Param("yearFilter") Integer yearFilter,
			Pageable pageable);
}

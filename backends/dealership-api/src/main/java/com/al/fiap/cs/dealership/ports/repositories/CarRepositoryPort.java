package com.al.fiap.cs.dealership.ports.repositories;

import com.al.fiap.cs.dealership.core.domain.car.Car;
import com.al.fiap.cs.dealership.core.domain.car.CarStatus;

import java.util.List;
import java.util.Optional;

public interface CarRepositoryPort {

	Car save(Car car);

	Optional<Car> findById(Long id);

	Optional<Car> findByPaymentCode(String paymentCode);

	boolean existsByBrandId(Long brandId);

	boolean existsByModelId(Long modelId);

	boolean existsByColorId(Long colorId);

	boolean existsByYearId(Long yearId);

	CarPage search(CarSearchCriteria criteria);

	void deleteById(Long id);

	record CarSearchCriteria(
			CarStatus status,
			String query,
			int page,
			int size,
			String sortField,
			SortDirection sortDirection
	) {
	}

	enum SortDirection {
		ASC,
		DESC
	}

	record CarPage(List<CarListItem> items, long totalElements, int page, int size) {
	}

	record CarListItem(
			Car car,
			String brandName,
			String modelName,
			String colorName,
			int yearValue
	) {
	}
}

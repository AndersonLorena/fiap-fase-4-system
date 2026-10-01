package com.al.fiap.cs.dealership.adapters.drivens.repositories;

import com.al.fiap.cs.dealership.adapters.drivens.repositories.jpa.BrandJpaEntity;
import com.al.fiap.cs.dealership.adapters.drivens.repositories.jpa.BrandSpringDataRepository;
import com.al.fiap.cs.dealership.adapters.drivens.repositories.jpa.CarJpaEntity;
import com.al.fiap.cs.dealership.adapters.drivens.repositories.jpa.CarModelJpaEntity;
import com.al.fiap.cs.dealership.adapters.drivens.repositories.jpa.CarModelSpringDataRepository;
import com.al.fiap.cs.dealership.adapters.drivens.repositories.jpa.CarPhotoJpaEntity;
import com.al.fiap.cs.dealership.adapters.drivens.repositories.jpa.CarSpringDataRepository;
import com.al.fiap.cs.dealership.adapters.drivens.repositories.jpa.ColorJpaEntity;
import com.al.fiap.cs.dealership.adapters.drivens.repositories.jpa.ColorSpringDataRepository;
import com.al.fiap.cs.dealership.adapters.drivens.repositories.jpa.VehicleYearJpaEntity;
import com.al.fiap.cs.dealership.adapters.drivens.repositories.jpa.VehicleYearSpringDataRepository;
import com.al.fiap.cs.dealership.core.domain.car.Car;
import com.al.fiap.cs.dealership.core.domain.car.CarPhoto;
import com.al.fiap.cs.dealership.core.domain.car.CarStatus;
import com.al.fiap.cs.dealership.core.domain.shared.SnowflakeIdGenerator;
import com.al.fiap.cs.dealership.ports.repositories.CarRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class CarRepositoryAdapter implements CarRepositoryPort {

	private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("price", "createdAt", "updatedAt");

	private final CarSpringDataRepository carRepository;
	private final BrandSpringDataRepository brandRepository;
	private final CarModelSpringDataRepository carModelRepository;
	private final ColorSpringDataRepository colorRepository;
	private final VehicleYearSpringDataRepository vehicleYearRepository;

	public CarRepositoryAdapter(
			CarSpringDataRepository carRepository,
			BrandSpringDataRepository brandRepository,
			CarModelSpringDataRepository carModelRepository,
			ColorSpringDataRepository colorRepository,
			VehicleYearSpringDataRepository vehicleYearRepository) {
		this.carRepository = carRepository;
		this.brandRepository = brandRepository;
		this.carModelRepository = carModelRepository;
		this.colorRepository = colorRepository;
		this.vehicleYearRepository = vehicleYearRepository;
	}

	@Override
	public Car save(Car car) {
		CarJpaEntity entity = carRepository.findById(car.id()).orElseGet(CarJpaEntity::new);
		entity.setId(car.id());
		entity.setBrandId(car.brandId());
		entity.setModelId(car.modelId());
		entity.setColorId(car.colorId());
		entity.setYearId(car.yearId());
		entity.setPrice(car.price());
		entity.setStatus(car.status().name());
		entity.setBuyerAccountId(car.buyerAccountId());
		entity.setBuyerCpf(car.buyerCpf());
		entity.setPaymentCode(car.paymentCode());
		entity.setSoldAt(car.soldAt());
		entity.setCreatedAt(car.createdAt());
		entity.setCreatedBy(car.createdBy());
		entity.setUpdatedAt(car.updatedAt());
		entity.setUpdatedBy(car.updatedBy());
		entity.getPhotos().clear();
		for (CarPhoto photo : car.photos()) {
			CarPhotoJpaEntity photoEntity = new CarPhotoJpaEntity();
			photoEntity.setId(SnowflakeIdGenerator.getInstance().nextId());
			photoEntity.setCar(entity);
			photoEntity.setObjectKey(photo.objectKey());
			photoEntity.setSortOrder(photo.sortOrder());
			entity.getPhotos().add(photoEntity);
		}
		carRepository.save(entity);
		return car;
	}

	@Override
	public Optional<Car> findById(Long id) {
		return carRepository.findById(id).map(CarRepositoryAdapter::toDomain);
	}

	@Override
	public Optional<Car> findByPaymentCode(String paymentCode) {
		return carRepository.findByPaymentCode(paymentCode).map(CarRepositoryAdapter::toDomain);
	}

	@Override
	public boolean existsByBrandId(Long brandId) {
		return carRepository.existsByBrandId(brandId);
	}

	@Override
	public boolean existsByModelId(Long modelId) {
		return carRepository.existsByModelId(modelId);
	}

	@Override
	public boolean existsByColorId(Long colorId) {
		return carRepository.existsByColorId(colorId);
	}

	@Override
	public boolean existsByYearId(Long yearId) {
		return carRepository.existsByYearId(yearId);
	}

	@Override
	public CarPage search(CarSearchCriteria criteria) {
		String sortField = ALLOWED_SORT_FIELDS.contains(criteria.sortField()) ? criteria.sortField() : "price";
		Sort.Direction direction = criteria.sortDirection() == SortDirection.DESC
			? Sort.Direction.DESC
			: Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(criteria.page(), criteria.size(), Sort.by(direction, sortField));
		String status = criteria.status() == null ? null : criteria.status().name();
		Integer yearFilter = parseYear(criteria.query());
		Page<CarJpaEntity> page = carRepository.search(status, criteria.query(), yearFilter, pageable);

		List<CarJpaEntity> content = page.getContent();
		Map<Long, String> brandNames = loadBrandNames(content);
		Map<Long, String> modelNames = loadModelNames(content);
		Map<Long, String> colorNames = loadColorNames(content);
		Map<Long, Integer> yearValues = loadYearValues(content);

		List<CarListItem> items = content.stream()
			.map(entity -> new CarListItem(
				toDomain(entity),
				brandNames.getOrDefault(entity.getBrandId(), ""),
				modelNames.getOrDefault(entity.getModelId(), ""),
				colorNames.getOrDefault(entity.getColorId(), ""),
				yearValues.getOrDefault(entity.getYearId(), 0)
			))
			.toList();
		return new CarPage(items, page.getTotalElements(), page.getNumber(), page.getSize());
	}

	@Override
	public void deleteById(Long id) {
		carRepository.deleteById(id);
	}

	private Map<Long, String> loadBrandNames(List<CarJpaEntity> entities) {
		Map<Long, String> names = new HashMap<>();
		if (entities.isEmpty()) {
			return names;
		}
		List<Long> ids = entities.stream().map(CarJpaEntity::getBrandId).distinct().toList();
		for (BrandJpaEntity brand : brandRepository.findAllById(ids)) {
			names.put(brand.getId(), brand.getName());
		}
		return names;
	}

	private Map<Long, String> loadModelNames(List<CarJpaEntity> entities) {
		Map<Long, String> names = new HashMap<>();
		if (entities.isEmpty()) {
			return names;
		}
		List<Long> ids = entities.stream().map(CarJpaEntity::getModelId).distinct().toList();
		for (CarModelJpaEntity model : carModelRepository.findAllById(ids)) {
			names.put(model.getId(), model.getName());
		}
		return names;
	}

	private Map<Long, String> loadColorNames(List<CarJpaEntity> entities) {
		Map<Long, String> names = new HashMap<>();
		if (entities.isEmpty()) {
			return names;
		}
		List<Long> ids = entities.stream().map(CarJpaEntity::getColorId).distinct().toList();
		for (ColorJpaEntity color : colorRepository.findAllById(ids)) {
			names.put(color.getId(), color.getName());
		}
		return names;
	}

	private Map<Long, Integer> loadYearValues(List<CarJpaEntity> entities) {
		Map<Long, Integer> values = new HashMap<>();
		if (entities.isEmpty()) {
			return values;
		}
		List<Long> ids = entities.stream().map(CarJpaEntity::getYearId).distinct().toList();
		for (VehicleYearJpaEntity year : vehicleYearRepository.findAllById(ids)) {
			values.put(year.getId(), year.getYearValue());
		}
		return values;
	}

	private static Integer parseYear(String query) {
		if (query == null || query.isBlank()) {
			return null;
		}
		try {
			return Integer.parseInt(query.trim());
		}
		catch (NumberFormatException ex) {
			return null;
		}
	}

	static Car toDomain(CarJpaEntity entity) {
		List<CarPhoto> photos = entity.getPhotos().stream()
			.map(photo -> new CarPhoto(photo.getObjectKey(), photo.getSortOrder()))
			.toList();
		return Car.restore(
			entity.getId(),
			entity.getCreatedAt(),
			entity.getCreatedBy(),
			entity.getUpdatedAt(),
			entity.getUpdatedBy(),
			entity.getBrandId(),
			entity.getModelId(),
			entity.getColorId(),
			entity.getYearId(),
			entity.getPrice(),
			CarStatus.valueOf(entity.getStatus()),
			entity.getBuyerAccountId(),
			entity.getBuyerCpf(),
			entity.getPaymentCode(),
			entity.getSoldAt(),
			photos
		);
	}
}

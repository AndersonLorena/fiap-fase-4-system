package com.al.fiap.cs.dealership.core.services;

import com.al.fiap.cs.dealership.core.domain.car.Car;
import com.al.fiap.cs.dealership.core.domain.car.CarPhoto;
import com.al.fiap.cs.dealership.core.domain.car.CarStatus;
import com.al.fiap.cs.dealership.core.domain.catalog.Brand;
import com.al.fiap.cs.dealership.core.domain.catalog.CarModel;
import com.al.fiap.cs.dealership.core.domain.catalog.Color;
import com.al.fiap.cs.dealership.core.domain.catalog.VehicleYear;
import com.al.fiap.cs.dealership.core.domain.exceptions.BrandModelMismatchException;
import com.al.fiap.cs.dealership.core.domain.exceptions.BrandNotFoundException;
import com.al.fiap.cs.dealership.core.domain.exceptions.BuyerNotEligibleException;
import com.al.fiap.cs.dealership.core.domain.exceptions.CarModelNotFoundException;
import com.al.fiap.cs.dealership.core.domain.exceptions.CarNotAvailableException;
import com.al.fiap.cs.dealership.core.domain.exceptions.CarNotFoundException;
import com.al.fiap.cs.dealership.core.domain.exceptions.ColorNotFoundException;
import com.al.fiap.cs.dealership.core.domain.exceptions.PaymentNotFoundException;
import com.al.fiap.cs.dealership.core.domain.exceptions.VehicleYearNotFoundException;
import com.al.fiap.cs.dealership.ports.buyervalidation.BuyerValidationPort;
import com.al.fiap.cs.dealership.ports.lock.CarLockPort;
import com.al.fiap.cs.dealership.ports.repositories.BrandRepositoryPort;
import com.al.fiap.cs.dealership.ports.repositories.CarModelRepositoryPort;
import com.al.fiap.cs.dealership.ports.repositories.CarRepositoryPort;
import com.al.fiap.cs.dealership.ports.repositories.ColorRepositoryPort;
import com.al.fiap.cs.dealership.ports.repositories.VehicleYearRepositoryPort;
import com.al.fiap.cs.dealership.ports.services.CarServicePort;
import com.al.fiap.cs.dealership.ports.services.dtos.request.ApplyPaymentRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.request.CompleteCarPhotoRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.request.CreateCarRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.request.CreateUploadIntentRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.request.PurchaseCarRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.request.SearchCarsRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.request.UpdateCarRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.response.CarPageResponse;
import com.al.fiap.cs.dealership.ports.services.dtos.response.CarPhotoResponse;
import com.al.fiap.cs.dealership.ports.services.dtos.response.CarResponse;
import com.al.fiap.cs.dealership.ports.services.dtos.response.UploadIntentResponse;
import com.al.fiap.cs.dealership.ports.storage.ObjectStoragePort;
import com.al.fiap.cs.dealership.core.services.support.UploadIntentSupport;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class CarService implements CarServicePort {

	private final CarRepositoryPort carRepository;
	private final BrandRepositoryPort brandRepository;
	private final CarModelRepositoryPort carModelRepository;
	private final ColorRepositoryPort colorRepository;
	private final VehicleYearRepositoryPort vehicleYearRepository;
	private final CarLockPort carLockPort;
	private final BuyerValidationPort buyerValidationPort;
	private final ObjectStoragePort objectStoragePort;
	private final UploadIntentSupport uploadIntentSupport;
	private final Duration photoUrlTtl;

	public CarService(
			CarRepositoryPort carRepository,
			BrandRepositoryPort brandRepository,
			CarModelRepositoryPort carModelRepository,
			ColorRepositoryPort colorRepository,
			VehicleYearRepositoryPort vehicleYearRepository,
			CarLockPort carLockPort,
			BuyerValidationPort buyerValidationPort,
			ObjectStoragePort objectStoragePort,
			UploadIntentSupport uploadIntentSupport,
			Duration photoUrlTtl) {
		this.carRepository = carRepository;
		this.brandRepository = brandRepository;
		this.carModelRepository = carModelRepository;
		this.colorRepository = colorRepository;
		this.vehicleYearRepository = vehicleYearRepository;
		this.carLockPort = carLockPort;
		this.buyerValidationPort = buyerValidationPort;
		this.objectStoragePort = objectStoragePort;
		this.uploadIntentSupport = uploadIntentSupport;
		this.photoUrlTtl = photoUrlTtl;
	}

	@Override
	public CarResponse create(CreateCarRequest request) {
		Brand brand = requireBrand(request.brandId());
		CarModel model = requireModelForBrand(request.modelId(), brand.id());
		Color color = requireColor(request.colorId());
		VehicleYear year = requireYear(request.yearId());

		Car car = Car.create(
			brand.id(),
			model.id(),
			color.id(),
			year.id(),
			request.price(),
			request.userId()
		);
		carRepository.save(car);
		return toResponse(car, brand.name(), model.name(), color.name(), year.year());
	}

	@Override
	
	public CarResponse update(UpdateCarRequest request) {
		Car car = carRepository.findById(request.carId())
			.orElseThrow(() -> new CarNotFoundException("Car not found"));
		Brand brand = requireBrand(request.brandId());
		CarModel model = requireModelForBrand(request.modelId(), brand.id());
		Color color = requireColor(request.colorId());
		VehicleYear year = requireYear(request.yearId());
		car.updateDetails(
			brand.id(),
			model.id(),
			color.id(),
			year.id(),
			request.price(),
			request.userId()
		);
		carRepository.save(car);
		return toResponse(car, brand.name(), model.name(), color.name(), year.year());
	}

	@Override
	
	public CarResponse get(Long carId) {
		Car car = carRepository.findById(carId)
			.orElseThrow(() -> new CarNotFoundException("Car not found"));
		Brand brand = requireBrand(car.brandId());
		CarModel model = requireModel(car.modelId());
		Color color = requireColor(car.colorId());
		VehicleYear year = requireYear(car.yearId());
		return toResponse(car, brand.name(), model.name(), color.name(), year.year());
	}

	@Override
	
	public CarPageResponse search(SearchCarsRequest request) {
		CarRepositoryPort.SortDirection direction = "desc".equalsIgnoreCase(request.sortDirection())
			? CarRepositoryPort.SortDirection.DESC
			: CarRepositoryPort.SortDirection.ASC;
		String sortField = request.sortField() == null || request.sortField().isBlank()
			? "price"
			: request.sortField();
		CarRepositoryPort.CarSearchCriteria criteria = new CarRepositoryPort.CarSearchCriteria(
			request.status(),
			request.query(),
			Math.max(0, request.page()),
			request.size() <= 0 ? 20 : request.size(),
			sortField,
			direction
		);
		CarRepositoryPort.CarPage page = carRepository.search(criteria);
		List<CarResponse> items = page.items().stream()
			.map(item -> toResponse(
				item.car(),
				item.brandName(),
				item.modelName(),
				item.colorName(),
				item.yearValue()))
			.toList();
		return new CarPageResponse(items, page.totalElements(), page.page(), page.size());
	}

	@Override
	
	public void delete(Long carId) {
		Car car = carRepository.findById(carId)
			.orElseThrow(() -> new CarNotFoundException("Car not found"));
		car.assertDeletable();
		carRepository.deleteById(car.id());
	}

	@Override
	
	public CarResponse purchase(PurchaseCarRequest request) {
		CarLockPort.LockHandle lock = carLockPort.acquire(request.carId());
		try {
			Car car = carRepository.findById(request.carId())
				.orElseThrow(() -> new CarNotFoundException("Car not found"));
			Optional<BuyerValidationPort.BuyerValidation> validation = buyerValidationPort.validate(request.buyerAccountId());
			if (validation.isEmpty() || !validation.get().eligible() || validation.get().cpf() == null) {
				throw new BuyerNotEligibleException("Buyer account is not eligible");
			}
			String paymentCode = UUID.randomUUID().toString();
			car.awaitPayment(request.buyerAccountId(), validation.get().cpf(), paymentCode, request.userId());
			carRepository.save(car);
			Brand brand = requireBrand(car.brandId());
			CarModel model = requireModel(car.modelId());
			Color color = requireColor(car.colorId());
			VehicleYear year = requireYear(car.yearId());
			return toResponse(car, brand.name(), model.name(), color.name(), year.year());
		}
		finally {
			carLockPort.release(lock);
		}
	}

	@Override
	public CarResponse applyPayment(ApplyPaymentRequest request) {
		Car located = carRepository.findByPaymentCode(request.paymentCode())
			.orElseThrow(() -> new PaymentNotFoundException("Payment not found"));
		CarLockPort.LockHandle lock = carLockPort.acquire(located.id());
		try {
			Car car = carRepository.findById(located.id())
				.orElseThrow(() -> new PaymentNotFoundException("Payment not found"));
			if (!request.paymentCode().equals(car.paymentCode())) {
				throw new PaymentNotFoundException("Payment not found");
			}
			if (request.outcome() == ApplyPaymentRequest.PaymentOutcome.PAID) {
				if (car.confirmPayment(request.userId())) {
					carRepository.save(car);
				}
			}
			else {
				car.cancelPayment(request.userId());
				carRepository.save(car);
			}
			Brand brand = requireBrand(car.brandId());
			CarModel model = requireModel(car.modelId());
			Color color = requireColor(car.colorId());
			VehicleYear year = requireYear(car.yearId());
			return toResponse(car, brand.name(), model.name(), color.name(), year.year());
		}
		finally {
			carLockPort.release(lock);
		}
	}

	@Override
	public UploadIntentResponse createUploadIntent(CreateUploadIntentRequest request) {
		Car car = carRepository.findById(request.carId())
			.orElseThrow(() -> new CarNotFoundException("Car not found"));
		if (car.status() != CarStatus.AVAILABLE) {
			throw new CarNotAvailableException("Cannot upload photos for sold cars");
		}
		String suffix = request.contentType() != null && request.contentType().contains("png") ? ".png" : ".jpg";
		String objectKey = "cars/" + car.id() + "/" + UUID.randomUUID() + suffix;
		Duration ttl = photoUrlTtl;
		ObjectStoragePort.PresignedUpload upload = objectStoragePort.createPresignedPutUrl(objectKey, ttl);
		String intentToken = uploadIntentSupport.issue(objectKey, car.id(), ttl);
		return new UploadIntentResponse(upload.url(), upload.objectKey(), intentToken);
	}

	@Override
	
	public CarResponse completePhoto(CompleteCarPhotoRequest request) {
		Car car = carRepository.findById(request.carId())
			.orElseThrow(() -> new CarNotFoundException("Car not found"));
		uploadIntentSupport.verify(request.intentToken(), request.objectKey(), car.id());
		car.addPhoto(request.objectKey(), request.sortOrder(), request.userId());
		carRepository.save(car);
		Brand brand = requireBrand(car.brandId());
		CarModel model = requireModel(car.modelId());
		Color color = requireColor(car.colorId());
		VehicleYear year = requireYear(car.yearId());
		return toResponse(car, brand.name(), model.name(), color.name(), year.year());
	}

	private Brand requireBrand(Long brandId) {
		return brandRepository.findById(brandId)
			.orElseThrow(() -> new BrandNotFoundException("Brand not found"));
	}

	private CarModel requireModel(Long modelId) {
		return carModelRepository.findById(modelId)
			.orElseThrow(() -> new CarModelNotFoundException("Model not found"));
	}

	private CarModel requireModelForBrand(Long modelId, Long brandId) {
		CarModel model = requireModel(modelId);
		if (!brandId.equals(model.brandId())) {
			throw new BrandModelMismatchException("Model does not belong to brand");
		}
		return model;
	}

	private Color requireColor(Long colorId) {
		return colorRepository.findById(colorId)
			.orElseThrow(() -> new ColorNotFoundException("Color not found"));
	}

	private VehicleYear requireYear(Long yearId) {
		return vehicleYearRepository.findById(yearId)
			.orElseThrow(() -> new VehicleYearNotFoundException("Vehicle year not found"));
	}

	private CarResponse toResponse(
			Car car,
			String brandName,
			String modelName,
			String colorName,
			int yearValue) {
		Duration ttl = photoUrlTtl;
		List<CarPhotoResponse> photos = car.photos().stream()
			.map(photo -> new CarPhotoResponse(
				photo.objectKey(),
				photo.sortOrder(),
				objectStoragePort.createPresignedGetUrl(photo.objectKey(), ttl)))
			.toList();
		return new CarResponse(
			car.id(),
			car.brandId(),
			brandName,
			car.modelId(),
			modelName,
			car.colorId(),
			colorName,
			car.yearId(),
			yearValue,
			car.price(),
			car.status().name().toUpperCase(Locale.ROOT),
			car.buyerAccountId(),
			car.buyerCpf(),
			car.paymentCode(),
			car.soldAt(),
			photos
		);
	}
}

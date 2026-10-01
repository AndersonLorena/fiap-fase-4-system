package com.al.fiap.cs.dealership.core.domain.car;

import com.al.fiap.cs.dealership.core.domain.exceptions.CarNotAvailableException;
import com.al.fiap.cs.dealership.core.domain.exceptions.InvalidPriceException;
import com.al.fiap.cs.dealership.core.domain.shared.BaseEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Car extends BaseEntity {

	private Long brandId;
	private Long modelId;
	private Long colorId;
	private Long yearId;
	private BigDecimal price;
	private CarStatus status;
	private Long buyerAccountId;
	private String buyerCpf;
	private String paymentCode;
	private Instant soldAt;
	private final List<CarPhoto> photos;

	private Car(
			Long userId,
			Long brandId,
			Long modelId,
			Long colorId,
			Long yearId,
			BigDecimal price) {
		super(userId);
		this.brandId = Objects.requireNonNull(brandId, "brandId");
		this.modelId = Objects.requireNonNull(modelId, "modelId");
		this.colorId = Objects.requireNonNull(colorId, "colorId");
		this.yearId = Objects.requireNonNull(yearId, "yearId");
		this.price = validatePrice(price);
		this.status = CarStatus.AVAILABLE;
		this.photos = new ArrayList<>();
	}

	private Car(
			Long id,
			Instant createdAt,
			Long createdBy,
			Instant updatedAt,
			Long updatedBy,
			Long brandId,
			Long modelId,
			Long colorId,
			Long yearId,
			BigDecimal price,
			CarStatus status,
			Long buyerAccountId,
			String buyerCpf,
			String paymentCode,
			Instant soldAt,
			List<CarPhoto> photos) {
		super(id, createdAt, createdBy, updatedAt, updatedBy);
		this.brandId = Objects.requireNonNull(brandId, "brandId");
		this.modelId = Objects.requireNonNull(modelId, "modelId");
		this.colorId = Objects.requireNonNull(colorId, "colorId");
		this.yearId = Objects.requireNonNull(yearId, "yearId");
		this.price = validatePrice(price);
		this.status = Objects.requireNonNull(status, "status");
		this.buyerAccountId = buyerAccountId;
		this.buyerCpf = buyerCpf;
		this.paymentCode = paymentCode;
		this.soldAt = soldAt;
		this.photos = new ArrayList<>(photos == null ? List.of() : photos);
	}

	public static Car create(
			Long brandId,
			Long modelId,
			Long colorId,
			Long yearId,
			BigDecimal price,
			Long userId) {
		return new Car(userId, brandId, modelId, colorId, yearId, price);
	}

	public static Car restore(
			Long id,
			Instant createdAt,
			Long createdBy,
			Instant updatedAt,
			Long updatedBy,
			Long brandId,
			Long modelId,
			Long colorId,
			Long yearId,
			BigDecimal price,
			CarStatus status,
			Long buyerAccountId,
			String buyerCpf,
			String paymentCode,
			Instant soldAt,
			List<CarPhoto> photos) {
		return new Car(
			id,
			createdAt,
			createdBy,
			updatedAt,
			updatedBy,
			brandId,
			modelId,
			colorId,
			yearId,
			price,
			status,
			buyerAccountId,
			buyerCpf,
			paymentCode,
			soldAt,
			photos
		);
	}

	public void updateDetails(
			Long brandId,
			Long modelId,
			Long colorId,
			Long yearId,
			BigDecimal price,
			Long userId) {
		if (status != CarStatus.AVAILABLE) {
			throw new CarNotAvailableException("Car cannot be updated unless it is available");
		}
		this.brandId = Objects.requireNonNull(brandId, "brandId");
		this.modelId = Objects.requireNonNull(modelId, "modelId");
		this.colorId = Objects.requireNonNull(colorId, "colorId");
		this.yearId = Objects.requireNonNull(yearId, "yearId");
		this.price = validatePrice(price);
		updateAudit(userId);
	}

	public void awaitPayment(Long buyerAccountId, String buyerCpf, String paymentCode, Long userId) {
		if (status != CarStatus.AVAILABLE) {
			throw new CarNotAvailableException("Car is not available for purchase");
		}
		this.status = CarStatus.AWAITING_PAYMENT;
		this.buyerAccountId = Objects.requireNonNull(buyerAccountId, "buyerAccountId");
		this.buyerCpf = Objects.requireNonNull(buyerCpf, "buyerCpf");
		this.paymentCode = Objects.requireNonNull(paymentCode, "paymentCode");
		this.soldAt = null;
		updateAudit(userId);
	}

	public boolean confirmPayment(Long userId) {
		if (status == CarStatus.SOLD) {
			return false;
		}
		if (status != CarStatus.AWAITING_PAYMENT) {
			throw new CarNotAvailableException("Car is not awaiting payment");
		}
		this.status = CarStatus.SOLD;
		this.soldAt = Instant.now();
		updateAudit(userId);
		return true;
	}

	public void cancelPayment(Long userId) {
		if (status != CarStatus.AWAITING_PAYMENT) {
			throw new CarNotAvailableException("Payment cannot be cancelled");
		}
		this.status = CarStatus.AVAILABLE;
		this.buyerAccountId = null;
		this.buyerCpf = null;
		this.paymentCode = null;
		this.soldAt = null;
		updateAudit(userId);
	}

	public void assertDeletable() {
		if (status != CarStatus.AVAILABLE) {
			throw new CarNotAvailableException("Car cannot be deleted unless it is available");
		}
	}

	public void addPhoto(String objectKey, int sortOrder, Long userId) {
		if (status != CarStatus.AVAILABLE) {
			throw new CarNotAvailableException("Cannot add photos unless the car is available");
		}
		this.photos.add(new CarPhoto(objectKey, sortOrder));
		updateAudit(userId);
	}

	public void replacePhotos(List<CarPhoto> newPhotos, Long userId) {
		this.photos.clear();
		if (newPhotos != null) {
			this.photos.addAll(newPhotos);
		}
		updateAudit(userId);
	}

	public Long brandId() {
		return brandId;
	}

	public Long modelId() {
		return modelId;
	}

	public Long colorId() {
		return colorId;
	}

	public Long yearId() {
		return yearId;
	}

	public BigDecimal price() {
		return price;
	}

	public CarStatus status() {
		return status;
	}

	public Long buyerAccountId() {
		return buyerAccountId;
	}

	public String buyerCpf() {
		return buyerCpf;
	}

	public String paymentCode() {
		return paymentCode;
	}

	public Instant soldAt() {
		return soldAt;
	}

	public List<CarPhoto> photos() {
		return Collections.unmodifiableList(photos);
	}

	private static BigDecimal validatePrice(BigDecimal price) {
		Objects.requireNonNull(price, "price");
		if (price.signum() <= 0) {
			throw new InvalidPriceException("Price must be greater than zero");
		}
		return price.setScale(2, RoundingMode.HALF_UP);
	}
}

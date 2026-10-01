package com.al.fiap.cs.dealership.adapters.drivers.webapis.mappers;

import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.request.ApplyPaymentWebRequest;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.request.CompletePhotoUploadWebRequest;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.request.CreateCarWebRequest;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.request.UpdateCarWebRequest;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.response.CarPhotoWebResponse;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.response.CarWebResponse;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.response.PagedCarsWebResponse;
import com.al.fiap.cs.dealership.adapters.drivers.webapis.dtos.response.PhotoUploadIntentWebResponse;
import com.al.fiap.cs.dealership.core.domain.car.CarStatus;
import com.al.fiap.cs.dealership.core.domain.exceptions.InvalidCarStatusException;
import com.al.fiap.cs.dealership.ports.services.dtos.request.ApplyPaymentRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.request.CompleteCarPhotoRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.request.CreateCarRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.request.CreateUploadIntentRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.request.SearchCarsRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.request.UpdateCarRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.response.CarPageResponse;
import com.al.fiap.cs.dealership.ports.services.dtos.response.CarResponse;
import com.al.fiap.cs.dealership.ports.services.dtos.response.UploadIntentResponse;

public final class CarWebMapper {

	private CarWebMapper() {
	}

	public static CreateCarRequest toCreateRequest(CreateCarWebRequest request, Long userId) {
		return new CreateCarRequest(
			request.brandId(),
			request.modelId(),
			request.colorId(),
			request.yearId(),
			request.price(),
			userId
		);
	}

	public static UpdateCarRequest toUpdateRequest(Long carId, UpdateCarWebRequest request, Long userId) {
		return new UpdateCarRequest(
			carId,
			request.brandId(),
			request.modelId(),
			request.colorId(),
			request.yearId(),
			request.price(),
			userId
		);
	}

	public static SearchCarsRequest toSearchRequest(String status, String q, int page, int size, String sort) {
		String[] parts = (sort == null || sort.isBlank() ? "price,asc" : sort).split(",", 2);
		String field = parts[0].trim();
		String direction = parts.length > 1 ? parts[1].trim() : "asc";
		CarStatus carStatus = null;
		if (status != null && !status.isBlank()) {
			try {
				carStatus = CarStatus.valueOf(status.trim().toUpperCase());
			}
			catch (IllegalArgumentException ex) {
				throw new InvalidCarStatusException("Invalid car status");
			}
		}
		return new SearchCarsRequest(carStatus, blankToNull(q), page, size, field, direction);
	}

	public static CompleteCarPhotoRequest toCompleteRequest(
			Long carId,
			CompletePhotoUploadWebRequest request,
			Long userId) {
		return new CompleteCarPhotoRequest(
			carId,
			request.objectKey(),
			request.intentToken(),
			request.sortOrder(),
			userId
		);
	}

	public static ApplyPaymentRequest toApplyPaymentRequest(
			String paymentCode,
			ApplyPaymentWebRequest request,
			Long userId) {
		return new ApplyPaymentRequest(
			paymentCode,
			ApplyPaymentRequest.PaymentOutcome.valueOf(request.status()),
			userId
		);
	}

	public static CreateUploadIntentRequest toUploadIntentRequest(Long carId, Long userId) {
		return new CreateUploadIntentRequest(carId, "image/jpeg", userId);
	}

	public static CarWebResponse toWebResponse(CarResponse response) {
		return new CarWebResponse(
			response.carId(),
			response.brandId(),
			response.brandName(),
			response.modelId(),
			response.modelName(),
			response.colorId(),
			response.colorName(),
			response.yearId(),
			response.yearValue(),
			response.price(),
			response.status(),
			response.buyerAccountId(),
			response.buyerCpf(),
			response.paymentCode(),
			response.soldAt(),
			response.photos().stream()
				.map(photo -> new CarPhotoWebResponse(photo.objectKey(), photo.sortOrder(), photo.url()))
				.toList()
		);
	}

	public static PagedCarsWebResponse toWebResponse(CarPageResponse response) {
		int totalPages = response.size() == 0
			? 0
			: (int) Math.ceil((double) response.totalElements() / response.size());
		return new PagedCarsWebResponse(
			response.items().stream().map(CarWebMapper::toWebResponse).toList(),
			response.page(),
			response.size(),
			response.totalElements(),
			totalPages
		);
	}

	public static PhotoUploadIntentWebResponse toWebResponse(UploadIntentResponse response) {
		return new PhotoUploadIntentWebResponse(response.uploadUrl(), response.objectKey(), response.intentToken());
	}

	private static String blankToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}
}

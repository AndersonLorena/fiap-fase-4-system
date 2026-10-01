package com.al.fiap.cs.dealership.ports.services;

import com.al.fiap.cs.dealership.ports.services.dtos.request.ApplyPaymentRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.request.CompleteCarPhotoRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.request.CreateCarRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.request.CreateUploadIntentRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.request.PurchaseCarRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.request.SearchCarsRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.request.UpdateCarRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.response.CarPageResponse;
import com.al.fiap.cs.dealership.ports.services.dtos.response.CarResponse;
import com.al.fiap.cs.dealership.ports.services.dtos.response.UploadIntentResponse;

public interface CarServicePort {

	CarResponse create(CreateCarRequest request);

	CarResponse update(UpdateCarRequest request);

	CarResponse get(Long carId);

	CarPageResponse search(SearchCarsRequest request);

	void delete(Long carId);

	CarResponse purchase(PurchaseCarRequest request);

	CarResponse applyPayment(ApplyPaymentRequest request);

	UploadIntentResponse createUploadIntent(CreateUploadIntentRequest request);

	CarResponse completePhoto(CompleteCarPhotoRequest request);
}

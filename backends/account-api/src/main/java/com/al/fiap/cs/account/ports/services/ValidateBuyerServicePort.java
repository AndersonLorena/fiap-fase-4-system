package com.al.fiap.cs.account.ports.services;

import com.al.fiap.cs.account.ports.services.dtos.request.ValidateBuyerRequest;
import com.al.fiap.cs.account.ports.services.dtos.response.BuyerValidationResponse;

public interface ValidateBuyerServicePort {
	BuyerValidationResponse validate(ValidateBuyerRequest request);
}

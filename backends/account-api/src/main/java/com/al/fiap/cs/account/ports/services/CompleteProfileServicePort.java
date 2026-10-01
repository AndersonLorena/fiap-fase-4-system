package com.al.fiap.cs.account.ports.services;

import com.al.fiap.cs.account.ports.services.dtos.request.CompleteProfileRequest;
import com.al.fiap.cs.account.ports.services.dtos.response.AccountResponse;

public interface CompleteProfileServicePort {
	AccountResponse complete(CompleteProfileRequest request);
}

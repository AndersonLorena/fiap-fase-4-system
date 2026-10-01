package com.al.fiap.cs.account.ports.services;

import com.al.fiap.cs.account.ports.services.dtos.request.GetCurrentAccountRequest;
import com.al.fiap.cs.account.ports.services.dtos.response.AccountResponse;

public interface GetCurrentAccountServicePort {
	AccountResponse get(GetCurrentAccountRequest request);
}

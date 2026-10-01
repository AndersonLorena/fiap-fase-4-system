package com.al.fiap.cs.account.ports.services;

import com.al.fiap.cs.account.ports.services.dtos.request.CreateAccountRequest;
import com.al.fiap.cs.account.ports.services.dtos.response.AccountResponse;

public interface CreateAccountServicePort {
	AccountResponse create(CreateAccountRequest request);
}

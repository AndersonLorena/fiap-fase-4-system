package com.al.fiap.cs.account.ports.services;

import com.al.fiap.cs.account.ports.services.dtos.request.AuthenticateRequest;
import com.al.fiap.cs.account.ports.services.dtos.response.TokenResponse;

public interface AuthenticateServicePort {
	TokenResponse authenticate(AuthenticateRequest request);
}

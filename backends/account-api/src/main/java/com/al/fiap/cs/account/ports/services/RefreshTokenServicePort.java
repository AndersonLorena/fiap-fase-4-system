package com.al.fiap.cs.account.ports.services;

import com.al.fiap.cs.account.ports.services.dtos.request.RefreshTokenRequest;
import com.al.fiap.cs.account.ports.services.dtos.response.TokenResponse;

public interface RefreshTokenServicePort {
	TokenResponse refresh(RefreshTokenRequest request);
}

package com.al.fiap.cs.account.ports.services;

import com.al.fiap.cs.account.ports.services.dtos.request.RequestPasswordRecoveryRequest;
import com.al.fiap.cs.account.ports.services.dtos.response.PasswordRecoveryResponse;

public interface RequestPasswordRecoveryServicePort {
	PasswordRecoveryResponse request(RequestPasswordRecoveryRequest request);
}

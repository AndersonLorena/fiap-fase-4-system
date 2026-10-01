package com.al.fiap.cs.account.ports.services;

import com.al.fiap.cs.account.ports.services.dtos.request.ConfirmPasswordRecoveryRequest;

public interface ConfirmPasswordRecoveryServicePort {
	void confirm(ConfirmPasswordRecoveryRequest request);
}

package com.al.fiap.cs.account.ports.services;

import com.al.fiap.cs.account.ports.services.dtos.request.ChangePasswordRequest;

public interface ChangePasswordServicePort {
	void changePassword(ChangePasswordRequest request);
}

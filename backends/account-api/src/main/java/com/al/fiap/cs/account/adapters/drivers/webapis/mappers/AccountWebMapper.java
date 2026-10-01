package com.al.fiap.cs.account.adapters.drivers.webapis.mappers;

import com.al.fiap.cs.account.adapters.drivers.webapis.dtos.request.AuthenticateWebRequest;
import com.al.fiap.cs.account.adapters.drivers.webapis.dtos.request.ChangePasswordWebRequest;
import com.al.fiap.cs.account.adapters.drivers.webapis.dtos.request.CompleteProfileWebRequest;
import com.al.fiap.cs.account.adapters.drivers.webapis.dtos.request.ConfirmPasswordRecoveryWebRequest;
import com.al.fiap.cs.account.adapters.drivers.webapis.dtos.request.CreateAccountWebRequest;
import com.al.fiap.cs.account.adapters.drivers.webapis.dtos.request.RefreshTokenWebRequest;
import com.al.fiap.cs.account.adapters.drivers.webapis.dtos.request.RequestPasswordRecoveryWebRequest;
import com.al.fiap.cs.account.adapters.drivers.webapis.dtos.response.AccountWebResponse;
import com.al.fiap.cs.account.adapters.drivers.webapis.dtos.response.BuyerValidationWebResponse;
import com.al.fiap.cs.account.adapters.drivers.webapis.dtos.response.PasswordRecoveryWebResponse;
import com.al.fiap.cs.account.adapters.drivers.webapis.dtos.response.TokenWebResponse;
import com.al.fiap.cs.account.ports.services.dtos.request.AuthenticateRequest;
import com.al.fiap.cs.account.ports.services.dtos.request.ChangePasswordRequest;
import com.al.fiap.cs.account.ports.services.dtos.request.CompleteProfileRequest;
import com.al.fiap.cs.account.ports.services.dtos.request.ConfirmPasswordRecoveryRequest;
import com.al.fiap.cs.account.ports.services.dtos.request.CreateAccountRequest;
import com.al.fiap.cs.account.ports.services.dtos.request.RefreshTokenRequest;
import com.al.fiap.cs.account.ports.services.dtos.request.RequestPasswordRecoveryRequest;
import com.al.fiap.cs.account.ports.services.dtos.response.AccountResponse;
import com.al.fiap.cs.account.ports.services.dtos.response.BuyerValidationResponse;
import com.al.fiap.cs.account.ports.services.dtos.response.PasswordRecoveryResponse;
import com.al.fiap.cs.account.ports.services.dtos.response.TokenResponse;

public final class AccountWebMapper {

	private AccountWebMapper() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static CreateAccountRequest toServiceRequest(CreateAccountWebRequest request, boolean requesterIsAdmin) {
		return new CreateAccountRequest(
			request.email(),
			request.password(),
			request.fullName(),
			request.type(),
			requesterIsAdmin
		);
	}

	public static AuthenticateRequest toServiceRequest(AuthenticateWebRequest request) {
		return new AuthenticateRequest(request.email(), request.password());
	}

	public static RefreshTokenRequest toServiceRequest(RefreshTokenWebRequest request) {
		return new RefreshTokenRequest(request.refreshToken());
	}

	public static ChangePasswordRequest toServiceRequest(Long accountId, ChangePasswordWebRequest request) {
		return new ChangePasswordRequest(accountId, request.currentPassword(), request.newPassword());
	}

	public static RequestPasswordRecoveryRequest toServiceRequest(RequestPasswordRecoveryWebRequest request) {
		return new RequestPasswordRecoveryRequest(request.email());
	}

	public static ConfirmPasswordRecoveryRequest toServiceRequest(ConfirmPasswordRecoveryWebRequest request) {
		return new ConfirmPasswordRecoveryRequest(request.email(), request.code(), request.newPassword());
	}

	public static CompleteProfileRequest toServiceRequest(Long accountId, CompleteProfileWebRequest request) {
		return new CompleteProfileRequest(accountId, request.document(), request.phone());
	}

	public static AccountWebResponse toWebResponse(AccountResponse response) {
		return new AccountWebResponse(
			response.accountId(),
			response.email(),
			response.fullName(),
			response.document(),
			response.phone(),
			response.status()
		);
	}

	public static TokenWebResponse toWebResponse(TokenResponse response) {
		return new TokenWebResponse(
			response.accessToken(),
			response.refreshToken(),
			response.tokenType(),
			response.expiresIn()
		);
	}

	public static PasswordRecoveryWebResponse toWebResponse(PasswordRecoveryResponse response) {
		return new PasswordRecoveryWebResponse(response.message());
	}

	public static BuyerValidationWebResponse toWebResponse(BuyerValidationResponse response) {
		return new BuyerValidationWebResponse(
			response.accountId(),
			response.status(),
			response.eligible(),
			response.cpf()
		);
	}
}

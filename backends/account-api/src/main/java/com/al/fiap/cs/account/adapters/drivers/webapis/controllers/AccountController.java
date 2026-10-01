package com.al.fiap.cs.account.adapters.drivers.webapis.controllers;

import com.al.fiap.cs.account.adapters.drivers.webapis.dtos.request.ChangePasswordWebRequest;
import com.al.fiap.cs.account.adapters.drivers.webapis.dtos.request.CompleteProfileWebRequest;
import com.al.fiap.cs.account.adapters.drivers.webapis.dtos.request.ConfirmPasswordRecoveryWebRequest;
import com.al.fiap.cs.account.adapters.drivers.webapis.dtos.request.CreateAccountWebRequest;
import com.al.fiap.cs.account.adapters.drivers.webapis.dtos.request.RequestPasswordRecoveryWebRequest;
import com.al.fiap.cs.account.adapters.drivers.webapis.dtos.response.AccountWebResponse;
import com.al.fiap.cs.account.adapters.drivers.webapis.dtos.response.BuyerValidationWebResponse;
import com.al.fiap.cs.account.adapters.drivers.webapis.dtos.response.PasswordRecoveryWebResponse;
import com.al.fiap.cs.account.adapters.drivers.webapis.mappers.AccountWebMapper;
import com.al.fiap.cs.account.adapters.drivers.webapis.security.AuthenticatedAccount;
import com.al.fiap.cs.account.adapters.drivers.webapis.security.AuthenticatedAccountArgumentResolver;
import com.al.fiap.cs.account.ports.services.ChangePasswordServicePort;
import com.al.fiap.cs.account.ports.services.CompleteProfileServicePort;
import com.al.fiap.cs.account.ports.services.ConfirmPasswordRecoveryServicePort;
import com.al.fiap.cs.account.ports.services.CreateAccountServicePort;
import com.al.fiap.cs.account.ports.services.GetCurrentAccountServicePort;
import com.al.fiap.cs.account.ports.services.RequestPasswordRecoveryServicePort;
import com.al.fiap.cs.account.ports.services.ValidateBuyerServicePort;
import com.al.fiap.cs.account.ports.services.dtos.request.GetCurrentAccountRequest;
import com.al.fiap.cs.account.ports.services.dtos.request.ValidateBuyerRequest;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@Tag(name = "Accounts")
@RequestMapping("/api/v1")
public class AccountController {

	private final CreateAccountServicePort createAccountService;
	private final GetCurrentAccountServicePort getCurrentAccountService;
	private final ChangePasswordServicePort changePasswordService;
	private final RequestPasswordRecoveryServicePort requestPasswordRecoveryService;
	private final ConfirmPasswordRecoveryServicePort confirmPasswordRecoveryService;
	private final CompleteProfileServicePort completeProfileService;
	private final ValidateBuyerServicePort validateBuyerService;

	public AccountController(
			CreateAccountServicePort createAccountService,
			GetCurrentAccountServicePort getCurrentAccountService,
			ChangePasswordServicePort changePasswordService,
			RequestPasswordRecoveryServicePort requestPasswordRecoveryService,
			ConfirmPasswordRecoveryServicePort confirmPasswordRecoveryService,
			CompleteProfileServicePort completeProfileService,
			ValidateBuyerServicePort validateBuyerService) {
		this.createAccountService = createAccountService;
		this.getCurrentAccountService = getCurrentAccountService;
		this.changePasswordService = changePasswordService;
		this.requestPasswordRecoveryService = requestPasswordRecoveryService;
		this.confirmPasswordRecoveryService = confirmPasswordRecoveryService;
		this.completeProfileService = completeProfileService;
		this.validateBuyerService = validateBuyerService;
	}

	@PostMapping("/accounts")
	public ResponseEntity<AccountWebResponse> create(@Valid @RequestBody CreateAccountWebRequest request) {
		AccountWebResponse response = AccountWebMapper.toWebResponse(
			createAccountService.create(
				AccountWebMapper.toServiceRequest(
					request,
					AuthenticatedAccountArgumentResolver.currentRequesterIsAdmin()
				)
			)
		);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/accounts/me")
	public ResponseEntity<AccountWebResponse> getCurrentAccount(AuthenticatedAccount authenticatedAccount) {
		requireUserAccount(authenticatedAccount);
		AccountWebResponse response = AccountWebMapper.toWebResponse(
			getCurrentAccountService.get(new GetCurrentAccountRequest(authenticatedAccount.accountId()))
		);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/accounts/me/profile")
	public ResponseEntity<AccountWebResponse> completeProfile(
			AuthenticatedAccount authenticatedAccount,
			@Valid @RequestBody CompleteProfileWebRequest request) {
		requireUserAccount(authenticatedAccount);
		AccountWebResponse response = AccountWebMapper.toWebResponse(
			completeProfileService.complete(
				AccountWebMapper.toServiceRequest(authenticatedAccount.accountId(), request)
			)
		);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/accounts/password")
	public ResponseEntity<Void> changePassword(
			AuthenticatedAccount authenticatedAccount,
			@Valid @RequestBody ChangePasswordWebRequest request) {
		requireUserAccount(authenticatedAccount);
		changePasswordService.changePassword(
			AccountWebMapper.toServiceRequest(authenticatedAccount.accountId(), request)
		);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/accounts/password-recovery")
	public ResponseEntity<PasswordRecoveryWebResponse> requestRecovery(
			@Valid @RequestBody RequestPasswordRecoveryWebRequest request) {
		PasswordRecoveryWebResponse response = AccountWebMapper.toWebResponse(
			requestPasswordRecoveryService.request(AccountWebMapper.toServiceRequest(request))
		);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/accounts/password-recovery/confirm")
	public ResponseEntity<Void> confirmRecovery(@Valid @RequestBody ConfirmPasswordRecoveryWebRequest request) {
		confirmPasswordRecoveryService.confirm(AccountWebMapper.toServiceRequest(request));
		return ResponseEntity.ok().build();
	}

	@Hidden
	@GetMapping("/internal/accounts/{accountId}/buyer-validation")
	public ResponseEntity<BuyerValidationWebResponse> validateBuyer(
			AuthenticatedAccount authenticatedAccount,
			@PathVariable Long accountId) {
		if (!authenticatedAccount.serviceAccount()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		BuyerValidationWebResponse response = AccountWebMapper.toWebResponse(
			validateBuyerService.validate(new ValidateBuyerRequest(accountId))
		);
		return ResponseEntity.ok(response);
	}

	private static void requireUserAccount(AuthenticatedAccount authenticatedAccount) {
		if (authenticatedAccount.serviceAccount() || authenticatedAccount.accountId() == null) {
			throw new IllegalStateException("User account token is required");
		}
	}
}

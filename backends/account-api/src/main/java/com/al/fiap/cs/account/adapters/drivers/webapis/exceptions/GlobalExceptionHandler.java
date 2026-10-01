package com.al.fiap.cs.account.adapters.drivers.webapis.exceptions;

import com.al.fiap.cs.account.core.domain.exceptions.AccountAlreadyExistsException;
import com.al.fiap.cs.account.core.domain.exceptions.AccountAlreadyValidatedException;
import com.al.fiap.cs.account.core.domain.exceptions.AccountNotFoundException;
import com.al.fiap.cs.account.core.domain.exceptions.AccountTypeNotAllowedException;
import com.al.fiap.cs.account.core.domain.exceptions.AuthenticationThrottledException;
import com.al.fiap.cs.account.core.domain.exceptions.DomainException;
import com.al.fiap.cs.account.core.domain.exceptions.IdentityProviderException;
import com.al.fiap.cs.account.core.domain.exceptions.InvalidCredentialsException;
import com.al.fiap.cs.account.core.domain.exceptions.InvalidRecoveryCodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
		ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
		detail.setTitle("Bad Request");
		FieldError fieldError = ex.getBindingResult().getFieldError();
		detail.setDetail(fieldError == null ? "Invalid request" : fieldError.getDefaultMessage());
		return ResponseEntity.badRequest().body(detail);
	}

	@ExceptionHandler({
		InvalidRecoveryCodeException.class,
		AccountAlreadyValidatedException.class,
		DomainException.class
	})
	public ResponseEntity<ProblemDetail> handleDomain(DomainException ex) {
		HttpStatus status = resolveDomainStatus(ex);
		if (status == HttpStatus.NO_CONTENT) {
			return ResponseEntity.noContent().build();
		}
		ProblemDetail detail = ProblemDetail.forStatus(status);
		detail.setTitle(status.getReasonPhrase());
		detail.setDetail(safeDetail(ex, status));
		return ResponseEntity.status(status).body(detail);
	}

	@ExceptionHandler(IdentityProviderException.class)
	public ResponseEntity<ProblemDetail> handleIdentityProvider(IdentityProviderException ex) {
		ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
		detail.setTitle("Service Unavailable");
		detail.setDetail("Identity provider unavailable");
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(detail);
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ProblemDetail> handleIllegalState(IllegalStateException ex) {
		ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
		detail.setTitle("Forbidden");
		detail.setDetail("Access denied");
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(detail);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex) {
		log.error("Unexpected error", ex);
		ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
		detail.setTitle("Internal Server Error");
		detail.setDetail("Unexpected error");
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(detail);
	}

	private static HttpStatus resolveDomainStatus(DomainException ex) {
		if (ex instanceof AccountAlreadyExistsException || ex instanceof AccountTypeNotAllowedException) {
			return HttpStatus.CONFLICT;
		}
		if (ex instanceof InvalidCredentialsException) {
			return HttpStatus.UNAUTHORIZED;
		}
		if (ex instanceof AuthenticationThrottledException) {
			return HttpStatus.TOO_MANY_REQUESTS;
		}
		if (ex instanceof AccountNotFoundException) {
			return HttpStatus.NO_CONTENT;
		}
		if (ex instanceof InvalidRecoveryCodeException || ex instanceof AccountAlreadyValidatedException) {
			return HttpStatus.BAD_REQUEST;
		}
		return HttpStatus.BAD_REQUEST;
	}

	private static String safeDetail(DomainException ex, HttpStatus status) {
		if (status == HttpStatus.UNAUTHORIZED) {
			return "Unauthorized";
		}
		if (status == HttpStatus.TOO_MANY_REQUESTS) {
			return "Too many attempts";
		}
		if (status == HttpStatus.CONFLICT) {
			return "Conflict";
		}
		return "Request could not be processed";
	}
}

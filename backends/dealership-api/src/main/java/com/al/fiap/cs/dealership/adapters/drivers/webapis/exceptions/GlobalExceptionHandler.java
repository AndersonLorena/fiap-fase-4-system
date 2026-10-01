package com.al.fiap.cs.dealership.adapters.drivers.webapis.exceptions;

import com.al.fiap.cs.dealership.core.domain.exceptions.BrandAlreadyExistsException;
import com.al.fiap.cs.dealership.core.domain.exceptions.BrandInUseException;
import com.al.fiap.cs.dealership.core.domain.exceptions.BrandModelMismatchException;
import com.al.fiap.cs.dealership.core.domain.exceptions.BrandNotFoundException;
import com.al.fiap.cs.dealership.core.domain.exceptions.BuyerNotEligibleException;
import com.al.fiap.cs.dealership.core.domain.exceptions.BuyerValidationUnavailableException;
import com.al.fiap.cs.dealership.core.domain.exceptions.CarLockUnavailableException;
import com.al.fiap.cs.dealership.core.domain.exceptions.CarModelAlreadyExistsException;
import com.al.fiap.cs.dealership.core.domain.exceptions.InvalidCarStatusException;
import com.al.fiap.cs.dealership.core.domain.exceptions.CarModelInUseException;
import com.al.fiap.cs.dealership.core.domain.exceptions.CarModelNotFoundException;
import com.al.fiap.cs.dealership.core.domain.exceptions.CarNotAvailableException;
import com.al.fiap.cs.dealership.core.domain.exceptions.CarNotFoundException;
import com.al.fiap.cs.dealership.core.domain.exceptions.ColorAlreadyExistsException;
import com.al.fiap.cs.dealership.core.domain.exceptions.ColorInUseException;
import com.al.fiap.cs.dealership.core.domain.exceptions.ColorNotFoundException;
import com.al.fiap.cs.dealership.core.domain.exceptions.DomainException;
import com.al.fiap.cs.dealership.core.domain.exceptions.InvalidNameException;
import com.al.fiap.cs.dealership.core.domain.exceptions.InvalidPriceException;
import com.al.fiap.cs.dealership.core.domain.exceptions.InvalidUploadIntentException;
import com.al.fiap.cs.dealership.core.domain.exceptions.InvalidVehicleYearException;
import com.al.fiap.cs.dealership.core.domain.exceptions.PaymentNotFoundException;
import com.al.fiap.cs.dealership.core.domain.exceptions.VehicleYearAlreadyExistsException;
import com.al.fiap.cs.dealership.core.domain.exceptions.VehicleYearInUseException;
import com.al.fiap.cs.dealership.core.domain.exceptions.VehicleYearNotFoundException;
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

	@ExceptionHandler(DomainException.class)
	public ResponseEntity<ProblemDetail> handleDomain(DomainException ex) {
		HttpStatus status = resolveDomainStatus(ex);
		if (status == HttpStatus.NO_CONTENT) {
			return ResponseEntity.noContent().build();
		}
		ProblemDetail detail = ProblemDetail.forStatus(status);
		detail.setTitle(status.getReasonPhrase());
		detail.setDetail(safeDetail(status));
		return ResponseEntity.status(status).body(detail);
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
		if (ex instanceof BrandNotFoundException
			|| ex instanceof CarModelNotFoundException
			|| ex instanceof ColorNotFoundException
			|| ex instanceof VehicleYearNotFoundException
			|| ex instanceof CarNotFoundException
			|| ex instanceof PaymentNotFoundException) {
			return HttpStatus.NO_CONTENT;
		}
		if (ex instanceof BrandAlreadyExistsException
			|| ex instanceof CarModelAlreadyExistsException
			|| ex instanceof ColorAlreadyExistsException
			|| ex instanceof VehicleYearAlreadyExistsException
			|| ex instanceof BrandInUseException
			|| ex instanceof CarModelInUseException
			|| ex instanceof ColorInUseException
			|| ex instanceof VehicleYearInUseException
			|| ex instanceof CarNotAvailableException
			|| ex instanceof BuyerNotEligibleException) {
			return HttpStatus.CONFLICT;
		}
		if (ex instanceof InvalidNameException
			|| ex instanceof InvalidPriceException
			|| ex instanceof InvalidVehicleYearException
			|| ex instanceof InvalidUploadIntentException
			|| ex instanceof InvalidCarStatusException
			|| ex instanceof BrandModelMismatchException) {
			return HttpStatus.BAD_REQUEST;
		}
		if (ex instanceof BuyerValidationUnavailableException
			|| ex instanceof CarLockUnavailableException) {
			return HttpStatus.SERVICE_UNAVAILABLE;
		}
		return HttpStatus.BAD_REQUEST;
	}

	private static String safeDetail(HttpStatus status) {
		if (status == HttpStatus.CONFLICT) {
			return "Conflict";
		}
		if (status == HttpStatus.SERVICE_UNAVAILABLE) {
			return "Service unavailable";
		}
		return "Request could not be processed";
	}
}

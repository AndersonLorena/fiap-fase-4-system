package com.al.fiap.cs.dealership.ports.services.dtos.request;

public record ApplyPaymentRequest(String paymentCode, PaymentOutcome outcome, Long userId) {

	public enum PaymentOutcome {
		PAID,
		CANCELLED
	}
}

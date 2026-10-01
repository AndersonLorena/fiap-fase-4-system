package com.al.fiap.cs.dealership.ports.buyervalidation;

import java.util.Optional;

public interface BuyerValidationPort {

	Optional<BuyerValidation> validate(Long accountId);

	record BuyerValidation(Long accountId, String status, boolean eligible, String cpf) {
	}
}

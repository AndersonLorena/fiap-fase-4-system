package com.al.fiap.cs.account.ports.services.dtos.response;

public record BuyerValidationResponse(Long accountId, String status, boolean eligible, String cpf) {
}

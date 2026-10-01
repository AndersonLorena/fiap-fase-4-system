package com.al.fiap.cs.account.adapters.drivers.webapis.dtos.response;

public record BuyerValidationWebResponse(Long accountId, String status, boolean eligible, String cpf) {
}

package com.al.fiap.cs.account.ports.services.dtos.request;

public record CompleteProfileRequest(Long accountId, String document, String phone) {
}

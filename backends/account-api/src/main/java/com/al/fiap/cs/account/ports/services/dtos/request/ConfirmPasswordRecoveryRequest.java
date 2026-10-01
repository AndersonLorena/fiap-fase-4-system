package com.al.fiap.cs.account.ports.services.dtos.request;

public record ConfirmPasswordRecoveryRequest(String email, String code, String newPassword) {
}

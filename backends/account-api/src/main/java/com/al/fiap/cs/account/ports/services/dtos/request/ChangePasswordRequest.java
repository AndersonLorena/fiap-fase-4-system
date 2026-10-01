package com.al.fiap.cs.account.ports.services.dtos.request;

public record ChangePasswordRequest(Long accountId, String currentPassword, String newPassword) {
}

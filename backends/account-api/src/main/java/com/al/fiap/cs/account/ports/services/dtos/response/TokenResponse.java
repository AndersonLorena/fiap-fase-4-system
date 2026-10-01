package com.al.fiap.cs.account.ports.services.dtos.response;

public record TokenResponse(String accessToken, String refreshToken, String tokenType, long expiresIn) {
}

package com.al.fiap.cs.account.adapters.drivers.webapis.dtos.response;

public record TokenWebResponse(String accessToken, String refreshToken, String tokenType, long expiresIn) {
}

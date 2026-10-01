package com.al.fiap.cs.dealership.ports.services.dtos.request;

public record CreateUploadIntentRequest(Long carId, String contentType, Long userId) {
}

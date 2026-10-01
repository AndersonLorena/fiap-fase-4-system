package com.al.fiap.cs.dealership.ports.services.dtos.request;

public record PurchaseCarRequest(Long carId, Long buyerAccountId, Long userId) {
}

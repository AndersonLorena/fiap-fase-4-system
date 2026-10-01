package com.al.fiap.cs.dealership.ports.services.dtos.request;

public record UpdateCarModelRequest(Long modelId, String name, Long actorId) {
}

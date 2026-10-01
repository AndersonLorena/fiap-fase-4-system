package com.al.fiap.cs.dealership.adapters.drivens.storage;

import com.al.fiap.cs.dealership.adapters.config.DealershipProperties;
import com.al.fiap.cs.dealership.ports.storage.ObjectStoragePort;

import java.time.Duration;

public class LocalObjectStorageAdapter implements ObjectStoragePort {

	private final DealershipProperties properties;

	public LocalObjectStorageAdapter(DealershipProperties properties) {
		this.properties = properties;
	}

	@Override
	public PresignedUpload createPresignedPutUrl(String objectKey, Duration ttl) {
		return new PresignedUpload(baseUrl() + "/" + objectKey + "?token=local-upload", objectKey);
	}

	@Override
	public String createPresignedGetUrl(String objectKey, Duration ttl) {
		return baseUrl() + "/" + objectKey;
	}

	private String baseUrl() {
		String endpoint = properties.getGarage().getPublicEndpoint();
		if (endpoint == null || endpoint.isBlank()) {
			endpoint = "http://localhost:9000";
		}
		String bucket = properties.getGarage().getBucket();
		if (bucket == null || bucket.isBlank()) {
			bucket = "cars";
		}
		return endpoint + "/" + bucket;
	}
}

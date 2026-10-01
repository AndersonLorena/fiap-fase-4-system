package com.al.fiap.cs.dealership.ports.storage;

import java.time.Duration;

public interface ObjectStoragePort {

	PresignedUpload createPresignedPutUrl(String objectKey, Duration ttl);

	String createPresignedGetUrl(String objectKey, Duration ttl);

	record PresignedUpload(String url, String objectKey) {
	}
}

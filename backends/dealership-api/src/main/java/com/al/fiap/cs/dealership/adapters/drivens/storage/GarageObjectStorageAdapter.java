package com.al.fiap.cs.dealership.adapters.drivens.storage;

import com.al.fiap.cs.dealership.adapters.config.DealershipProperties;
import com.al.fiap.cs.dealership.ports.storage.ObjectStoragePort;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.time.Duration;

public class GarageObjectStorageAdapter implements ObjectStoragePort {

	private final DealershipProperties properties;
	private final S3Presigner presigner;

	public GarageObjectStorageAdapter(DealershipProperties properties) {
		this.properties = properties;
		this.presigner = S3Presigner.builder()
			.endpointOverride(URI.create(presignEndpoint(properties.getGarage())))
			.region(Region.of(properties.getGarage().getRegion()))
			.credentialsProvider(StaticCredentialsProvider.create(
				AwsBasicCredentials.create(
					properties.getGarage().getAccessKey(),
					properties.getGarage().getSecretKey()
				)))
			.serviceConfiguration(S3Configuration.builder()
				.pathStyleAccessEnabled(true)
				.build())
			.build();
	}

	private static String presignEndpoint(DealershipProperties.Garage garage) {
		String publicEndpoint = garage.getPublicEndpoint();
		if (publicEndpoint != null && !publicEndpoint.isBlank()) {
			return publicEndpoint;
		}
		return garage.getEndpoint();
	}

	@Override
	public PresignedUpload createPresignedPutUrl(String objectKey, Duration ttl) {
		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
			.bucket(properties.getGarage().getBucket())
			.key(objectKey)
			.build();
		PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
			.signatureDuration(ttl)
			.putObjectRequest(putObjectRequest)
			.build();
		String url = presigner.presignPutObject(presignRequest).url().toString();
		return new PresignedUpload(url, objectKey);
	}

	@Override
	public String createPresignedGetUrl(String objectKey, Duration ttl) {
		GetObjectRequest getObjectRequest = GetObjectRequest.builder()
			.bucket(properties.getGarage().getBucket())
			.key(objectKey)
			.build();
		GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
			.signatureDuration(ttl)
			.getObjectRequest(getObjectRequest)
			.build();
		return presigner.presignGetObject(presignRequest).url().toString();
	}
}

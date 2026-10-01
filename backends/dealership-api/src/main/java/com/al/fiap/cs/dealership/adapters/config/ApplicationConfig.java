package com.al.fiap.cs.dealership.adapters.config;

import com.al.fiap.cs.dealership.adapters.drivers.webapis.security.AuthenticatedActorArgumentResolver;
import com.al.fiap.cs.dealership.adapters.drivens.buyervalidation.AccountApiBuyerValidationAdapter;
import com.al.fiap.cs.dealership.adapters.drivens.lock.LocalCarLockAdapter;
import com.al.fiap.cs.dealership.adapters.drivens.lock.RedisCarLockAdapter;
import com.al.fiap.cs.dealership.adapters.drivens.repositories.BrandRepositoryAdapter;
import com.al.fiap.cs.dealership.adapters.drivens.repositories.CarModelRepositoryAdapter;
import com.al.fiap.cs.dealership.adapters.drivens.repositories.CarRepositoryAdapter;
import com.al.fiap.cs.dealership.adapters.drivens.repositories.ColorRepositoryAdapter;
import com.al.fiap.cs.dealership.adapters.drivens.repositories.VehicleYearRepositoryAdapter;
import com.al.fiap.cs.dealership.adapters.drivens.repositories.jpa.BrandSpringDataRepository;
import com.al.fiap.cs.dealership.adapters.drivens.repositories.jpa.CarModelSpringDataRepository;
import com.al.fiap.cs.dealership.adapters.drivens.repositories.jpa.CarSpringDataRepository;
import com.al.fiap.cs.dealership.adapters.drivens.repositories.jpa.ColorSpringDataRepository;
import com.al.fiap.cs.dealership.adapters.drivens.repositories.jpa.VehicleYearSpringDataRepository;
import com.al.fiap.cs.dealership.adapters.drivens.storage.GarageObjectStorageAdapter;
import com.al.fiap.cs.dealership.adapters.drivens.storage.LocalObjectStorageAdapter;
import com.al.fiap.cs.dealership.core.domain.shared.SnowflakeIdGenerator;
import com.al.fiap.cs.dealership.core.services.BrandService;
import com.al.fiap.cs.dealership.core.services.CarModelService;
import com.al.fiap.cs.dealership.core.services.CarService;
import com.al.fiap.cs.dealership.core.services.ColorService;
import com.al.fiap.cs.dealership.core.services.VehicleYearService;
import com.al.fiap.cs.dealership.core.services.support.UploadIntentSupport;
import com.al.fiap.cs.dealership.ports.buyervalidation.BuyerValidationPort;
import com.al.fiap.cs.dealership.ports.lock.CarLockPort;
import com.al.fiap.cs.dealership.ports.repositories.BrandRepositoryPort;
import com.al.fiap.cs.dealership.ports.repositories.CarModelRepositoryPort;
import com.al.fiap.cs.dealership.ports.repositories.CarRepositoryPort;
import com.al.fiap.cs.dealership.ports.repositories.ColorRepositoryPort;
import com.al.fiap.cs.dealership.ports.repositories.VehicleYearRepositoryPort;
import com.al.fiap.cs.dealership.ports.services.BrandServicePort;
import com.al.fiap.cs.dealership.ports.services.CarModelServicePort;
import com.al.fiap.cs.dealership.ports.services.CarServicePort;
import com.al.fiap.cs.dealership.ports.services.ColorServicePort;
import com.al.fiap.cs.dealership.ports.services.VehicleYearServicePort;
import com.al.fiap.cs.dealership.ports.storage.ObjectStoragePort;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.client.RestClient;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@EnableRetry
@EnableConfigurationProperties(DealershipProperties.class)
public class ApplicationConfig implements WebMvcConfigurer {

	private final DealershipProperties properties;

	public ApplicationConfig(DealershipProperties properties) {
		this.properties = properties;
	}

	@PostConstruct
	void configureSnowflake() {
		SnowflakeIdGenerator.configure(properties.getSnowflake().getMachineId());
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(new AuthenticatedActorArgumentResolver());
	}

	@Bean
	RestClient.Builder restClientBuilder() {
		return RestClient.builder();
	}

	@Bean
	BrandRepositoryPort brandRepositoryPort(BrandSpringDataRepository repository) {
		return new BrandRepositoryAdapter(repository);
	}

	@Bean
	CarModelRepositoryPort carModelRepositoryPort(CarModelSpringDataRepository repository) {
		return new CarModelRepositoryAdapter(repository);
	}

	@Bean
	ColorRepositoryPort colorRepositoryPort(ColorSpringDataRepository repository) {
		return new ColorRepositoryAdapter(repository);
	}

	@Bean
	VehicleYearRepositoryPort vehicleYearRepositoryPort(VehicleYearSpringDataRepository repository) {
		return new VehicleYearRepositoryAdapter(repository);
	}

	@Bean
	CarRepositoryPort carRepositoryPort(
			CarSpringDataRepository carRepository,
			BrandSpringDataRepository brandRepository,
			CarModelSpringDataRepository carModelRepository,
			ColorSpringDataRepository colorRepository,
			VehicleYearSpringDataRepository vehicleYearRepository) {
		return new CarRepositoryAdapter(
			carRepository,
			brandRepository,
			carModelRepository,
			colorRepository,
			vehicleYearRepository
		);
	}

	@Bean
	@Profile("!test")
	JwtDecoder jwtDecoder() {
		DealershipProperties.Keycloak keycloak = properties.getKeycloak();
		return KeycloakJwtDecoderFactory.create(keycloak.realmBaseUrl(), keycloak.certsUrl(), keycloak.getJwks());
	}

	@Bean
	@Profile("!test")
	CarLockPort redisCarLockPort(StringRedisTemplate redisTemplate) {
		return new RedisCarLockAdapter(redisTemplate, properties);
	}

	@Bean
	@Profile("test")
	CarLockPort localCarLockPort() {
		return new LocalCarLockAdapter();
	}

	@Bean
	@Profile("!test")
	BuyerValidationPort accountApiBuyerValidationPort(RestClient.Builder restClientBuilder) {
		return new AccountApiBuyerValidationAdapter(restClientBuilder, properties);
	}

	@Bean
	@Profile("!test")
	ObjectStoragePort garageObjectStoragePort() {
		return new GarageObjectStorageAdapter(properties);
	}

	@Bean
	@Profile("test")
	ObjectStoragePort localObjectStoragePort() {
		return new LocalObjectStorageAdapter(properties);
	}

	@Bean
	UploadIntentSupport uploadIntentSupport() {
		return new UploadIntentSupport(properties.getUploadIntentSecret());
	}

	@Bean
	BrandServicePort brandServicePort(
			BrandRepositoryPort brandRepository,
			CarModelRepositoryPort carModelRepository,
			CarRepositoryPort carRepository) {
		return new BrandService(brandRepository, carModelRepository, carRepository);
	}

	@Bean
	CarModelServicePort carModelServicePort(
			CarModelRepositoryPort carModelRepository,
			BrandRepositoryPort brandRepository,
			CarRepositoryPort carRepository) {
		return new CarModelService(carModelRepository, brandRepository, carRepository);
	}

	@Bean
	ColorServicePort colorServicePort(
			ColorRepositoryPort colorRepository,
			CarRepositoryPort carRepository) {
		return new ColorService(colorRepository, carRepository);
	}

	@Bean
	VehicleYearServicePort vehicleYearServicePort(
			VehicleYearRepositoryPort vehicleYearRepository,
			CarRepositoryPort carRepository) {
		return new VehicleYearService(vehicleYearRepository, carRepository);
	}

	@Bean
	CarServicePort carServicePort(
			CarRepositoryPort carRepository,
			BrandRepositoryPort brandRepository,
			CarModelRepositoryPort carModelRepository,
			ColorRepositoryPort colorRepository,
			VehicleYearRepositoryPort vehicleYearRepository,
			CarLockPort carLockPort,
			BuyerValidationPort buyerValidationPort,
			ObjectStoragePort objectStoragePort,
			UploadIntentSupport uploadIntentSupport) {
		return new CarService(
			carRepository,
			brandRepository,
			carModelRepository,
			colorRepository,
			vehicleYearRepository,
			carLockPort,
			buyerValidationPort,
			objectStoragePort,
			uploadIntentSupport,
			properties.getGarage().getPresignedTtl()
		);
	}

	@Bean
	ApplicationRunner catalogBootstrapRunner(
			BrandRepositoryPort brandRepository,
			BrandServicePort brandService,
			CarModelServicePort carModelService,
			ColorServicePort colorService,
			VehicleYearServicePort vehicleYearService,
			CarServicePort carService) {
		return new CatalogBootstrapRunner(
			properties,
			brandRepository,
			brandService,
			carModelService,
			colorService,
			vehicleYearService,
			carService
		);
	}
}

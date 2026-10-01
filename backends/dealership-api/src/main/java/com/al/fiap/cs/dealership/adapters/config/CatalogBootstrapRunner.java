package com.al.fiap.cs.dealership.adapters.config;

import com.al.fiap.cs.dealership.ports.repositories.BrandRepositoryPort;
import com.al.fiap.cs.dealership.ports.services.BrandServicePort;
import com.al.fiap.cs.dealership.ports.services.CarModelServicePort;
import com.al.fiap.cs.dealership.ports.services.CarServicePort;
import com.al.fiap.cs.dealership.ports.services.ColorServicePort;
import com.al.fiap.cs.dealership.ports.services.VehicleYearServicePort;
import com.al.fiap.cs.dealership.ports.services.dtos.request.CreateBrandRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.request.CreateCarModelRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.request.CreateCarRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.request.CreateColorRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.request.CreateVehicleYearRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.response.BrandResponse;
import com.al.fiap.cs.dealership.ports.services.dtos.response.CarModelResponse;
import com.al.fiap.cs.dealership.ports.services.dtos.response.ColorResponse;
import com.al.fiap.cs.dealership.ports.services.dtos.response.VehicleYearResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CatalogBootstrapRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(CatalogBootstrapRunner.class);

	private static final String IDEMPOTENCY_BRAND = "Volvo";

	private static final List<String> COLORS = List.of(
		"Black", "White", "Silver", "Blue", "Red", "Gray"
	);

	private static final List<Integer> YEARS = List.of(2020, 2021, 2022, 2023, 2024, 2025);

	private static final Map<String, List<String>> BRAND_MODELS;
	private static final Map<String, BigDecimal[]> BRAND_BASE_PRICES;

	static {
		Map<String, List<String>> brandModels = new LinkedHashMap<>();
		brandModels.put("Volvo", List.of("XC40", "XC60", "XC90"));
		brandModels.put("Audi", List.of("A3", "A4", "Q3", "Q5"));
		brandModels.put("BMW", List.of("320i", "X1", "X2"));
		brandModels.put("Toyota", List.of("RAV4", "Corolla", "Camry"));
		BRAND_MODELS = Collections.unmodifiableMap(brandModels);

		Map<String, BigDecimal[]> brandPrices = new LinkedHashMap<>();
		brandPrices.put("Volvo", new BigDecimal[]{new BigDecimal("189900.00"), new BigDecimal("249900.00")});
		brandPrices.put("Audi", new BigDecimal[]{new BigDecimal("169900.00"), new BigDecimal("229900.00")});
		brandPrices.put("BMW", new BigDecimal[]{new BigDecimal("179900.00"), new BigDecimal("239900.00")});
		brandPrices.put("Toyota", new BigDecimal[]{new BigDecimal("139900.00"), new BigDecimal("179900.00")});
		BRAND_BASE_PRICES = Collections.unmodifiableMap(brandPrices);
	}

	private final DealershipProperties properties;
	private final BrandRepositoryPort brandRepository;
	private final BrandServicePort brandService;
	private final CarModelServicePort carModelService;
	private final ColorServicePort colorService;
	private final VehicleYearServicePort vehicleYearService;
	private final CarServicePort carService;

	public CatalogBootstrapRunner(
			DealershipProperties properties,
			BrandRepositoryPort brandRepository,
			BrandServicePort brandService,
			CarModelServicePort carModelService,
			ColorServicePort colorService,
			VehicleYearServicePort vehicleYearService,
			CarServicePort carService) {
		this.properties = properties;
		this.brandRepository = brandRepository;
		this.brandService = brandService;
		this.carModelService = carModelService;
		this.colorService = colorService;
		this.vehicleYearService = vehicleYearService;
		this.carService = carService;
	}

	@Override
	public void run(ApplicationArguments args) {
		DealershipProperties.Bootstrap.Catalog catalog = properties.getBootstrap().getCatalog();
		if (!catalog.isEnabled()) {
			return;
		}
		if (brandRepository.existsByName(IDEMPOTENCY_BRAND)) {
			log.info("Catalog bootstrap skipped; brand already exists name={}", IDEMPOTENCY_BRAND);
			return;
		}

		Long actorId = catalog.getActorId();
		log.info("Bootstrapping dealership catalog with actorId={}", actorId);

		List<ColorResponse> colors = seedColors(actorId);
		List<VehicleYearResponse> years = seedYears(actorId);
		Map<String, BrandResponse> brands = seedBrands(actorId);
		Map<String, List<CarModelResponse>> modelsByBrand = seedModels(brands, actorId);
		int carsCreated = seedCars(brands, modelsByBrand, colors, years, actorId);

		log.info(
			"Catalog bootstrap completed brands={} models={} colors={} years={} cars={}",
			brands.size(),
			modelsByBrand.values().stream().mapToInt(List::size).sum(),
			colors.size(),
			years.size(),
			carsCreated
		);
	}

	private List<ColorResponse> seedColors(Long actorId) {
		return COLORS.stream()
			.map(name -> colorService.create(new CreateColorRequest(name, actorId)))
			.toList();
	}

	private List<VehicleYearResponse> seedYears(Long actorId) {
		return YEARS.stream()
			.map(year -> vehicleYearService.create(new CreateVehicleYearRequest(year, actorId)))
			.toList();
	}

	private Map<String, BrandResponse> seedBrands(Long actorId) {
		Map<String, BrandResponse> brands = new LinkedHashMap<>();
		for (String brandName : BRAND_MODELS.keySet()) {
			brands.put(brandName, brandService.create(new CreateBrandRequest(brandName, actorId)));
		}
		return brands;
	}

	private Map<String, List<CarModelResponse>> seedModels(Map<String, BrandResponse> brands, Long actorId) {
		Map<String, List<CarModelResponse>> modelsByBrand = new LinkedHashMap<>();
		for (Map.Entry<String, List<String>> entry : BRAND_MODELS.entrySet()) {
			BrandResponse brand = brands.get(entry.getKey());
			List<CarModelResponse> models = entry.getValue().stream()
				.map(modelName -> carModelService.create(
					new CreateCarModelRequest(brand.brandId(), modelName, actorId)))
				.toList();
			modelsByBrand.put(entry.getKey(), models);
		}
		return modelsByBrand;
	}

	private int seedCars(
			Map<String, BrandResponse> brands,
			Map<String, List<CarModelResponse>> modelsByBrand,
			List<ColorResponse> colors,
			List<VehicleYearResponse> years,
			Long actorId) {
		int created = 0;
		int variant = 0;
		for (Map.Entry<String, List<CarModelResponse>> entry : modelsByBrand.entrySet()) {
			String brandName = entry.getKey();
			BrandResponse brand = brands.get(brandName);
			BigDecimal[] prices = BRAND_BASE_PRICES.get(brandName);
			for (CarModelResponse model : entry.getValue()) {
				for (int i = 0; i < 2; i++) {
					ColorResponse color = colors.get((variant + i) % colors.size());
					VehicleYearResponse year = years.get((variant + i + 2) % years.size());
					BigDecimal price = prices[i].add(BigDecimal.valueOf(variant * 1000L));
					carService.create(new CreateCarRequest(
						brand.brandId(),
						model.modelId(),
						color.colorId(),
						year.yearId(),
						price,
						actorId
					));
					created++;
				}
				variant++;
			}
		}
		return created;
	}
}

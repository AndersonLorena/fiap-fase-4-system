package com.al.fiap.cs.dealership.core.services;

import com.al.fiap.cs.dealership.core.domain.catalog.VehicleYear;
import com.al.fiap.cs.dealership.core.domain.exceptions.VehicleYearAlreadyExistsException;
import com.al.fiap.cs.dealership.core.domain.exceptions.VehicleYearInUseException;
import com.al.fiap.cs.dealership.core.domain.exceptions.VehicleYearNotFoundException;
import com.al.fiap.cs.dealership.ports.repositories.CarRepositoryPort;
import com.al.fiap.cs.dealership.ports.repositories.VehicleYearRepositoryPort;
import com.al.fiap.cs.dealership.ports.services.VehicleYearServicePort;
import com.al.fiap.cs.dealership.ports.services.dtos.request.CreateVehicleYearRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.request.UpdateVehicleYearRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.response.VehicleYearResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class VehicleYearService implements VehicleYearServicePort {

	private final VehicleYearRepositoryPort vehicleYearRepository;
	private final CarRepositoryPort carRepository;

	public VehicleYearService(
			VehicleYearRepositoryPort vehicleYearRepository,
			CarRepositoryPort carRepository) {
		this.vehicleYearRepository = vehicleYearRepository;
		this.carRepository = carRepository;
	}

	@Override
	@Transactional
	public VehicleYearResponse create(CreateVehicleYearRequest request) {
		VehicleYear year = VehicleYear.create(request.year(), request.userId());
		if (vehicleYearRepository.existsByYear(year.year())) {
			throw new VehicleYearAlreadyExistsException("Vehicle year already exists");
		}
		vehicleYearRepository.save(year);
		return toResponse(year);
	}

	@Override
	@Transactional
	public VehicleYearResponse update(UpdateVehicleYearRequest request) {
		VehicleYear year = vehicleYearRepository.findById(request.yearId())
			.orElseThrow(() -> new VehicleYearNotFoundException("Vehicle year not found"));
		year.rename(request.year(), request.userId());
		if (vehicleYearRepository.existsByYearAndIdNot(year.year(), year.id())) {
			throw new VehicleYearAlreadyExistsException("Vehicle year already exists");
		}
		vehicleYearRepository.save(year);
		return toResponse(year);
	}

	@Override
	@Transactional(readOnly = true)
	public VehicleYearResponse get(Long yearId) {
		VehicleYear year = vehicleYearRepository.findById(yearId)
			.orElseThrow(() -> new VehicleYearNotFoundException("Vehicle year not found"));
		return toResponse(year);
	}

	@Override
	@Transactional(readOnly = true)
	public List<VehicleYearResponse> list() {
		return vehicleYearRepository.findAll().stream().map(VehicleYearService::toResponse).toList();
	}

	@Override
	@Transactional
	public void delete(Long yearId) {
		VehicleYear year = vehicleYearRepository.findById(yearId)
			.orElseThrow(() -> new VehicleYearNotFoundException("Vehicle year not found"));
		if (carRepository.existsByYearId(year.id())) {
			throw new VehicleYearInUseException("Vehicle year is referenced by cars");
		}
		vehicleYearRepository.deleteById(year.id());
	}

	private static VehicleYearResponse toResponse(VehicleYear year) {
		return new VehicleYearResponse(year.id(), year.year());
	}
}

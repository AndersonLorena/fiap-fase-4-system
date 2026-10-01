package com.al.fiap.cs.dealership.core.services;

import com.al.fiap.cs.dealership.core.domain.catalog.Color;
import com.al.fiap.cs.dealership.core.domain.exceptions.ColorAlreadyExistsException;
import com.al.fiap.cs.dealership.core.domain.exceptions.ColorInUseException;
import com.al.fiap.cs.dealership.core.domain.exceptions.ColorNotFoundException;
import com.al.fiap.cs.dealership.ports.repositories.CarRepositoryPort;
import com.al.fiap.cs.dealership.ports.repositories.ColorRepositoryPort;
import com.al.fiap.cs.dealership.ports.services.ColorServicePort;
import com.al.fiap.cs.dealership.ports.services.dtos.request.CreateColorRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.request.UpdateColorRequest;
import com.al.fiap.cs.dealership.ports.services.dtos.response.ColorResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class ColorService implements ColorServicePort {

	private final ColorRepositoryPort colorRepository;
	private final CarRepositoryPort carRepository;

	public ColorService(ColorRepositoryPort colorRepository, CarRepositoryPort carRepository) {
		this.colorRepository = colorRepository;
		this.carRepository = carRepository;
	}

	@Override
	@Transactional
	public ColorResponse create(CreateColorRequest request) {
		Color color = Color.create(request.name(), request.userId());
		if (colorRepository.existsByName(color.name())) {
			throw new ColorAlreadyExistsException("Color name already exists");
		}
		colorRepository.save(color);
		return toResponse(color);
	}

	@Override
	@Transactional
	public ColorResponse update(UpdateColorRequest request) {
		Color color = colorRepository.findById(request.colorId())
			.orElseThrow(() -> new ColorNotFoundException("Color not found"));
		color.rename(request.name(), request.userId());
		if (colorRepository.existsByNameAndIdNot(color.name(), color.id())) {
			throw new ColorAlreadyExistsException("Color name already exists");
		}
		colorRepository.save(color);
		return toResponse(color);
	}

	@Override
	@Transactional(readOnly = true)
	public ColorResponse get(Long colorId) {
		Color color = colorRepository.findById(colorId)
			.orElseThrow(() -> new ColorNotFoundException("Color not found"));
		return toResponse(color);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ColorResponse> list() {
		return colorRepository.findAll().stream().map(ColorService::toResponse).toList();
	}

	@Override
	@Transactional
	public void delete(Long colorId) {
		Color color = colorRepository.findById(colorId)
			.orElseThrow(() -> new ColorNotFoundException("Color not found"));
		if (carRepository.existsByColorId(color.id())) {
			throw new ColorInUseException("Color is referenced by cars");
		}
		colorRepository.deleteById(color.id());
	}

	private static ColorResponse toResponse(Color color) {
		return new ColorResponse(color.id(), color.name());
	}
}

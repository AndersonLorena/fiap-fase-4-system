package com.al.fiap.cs.dealership.adapters.drivens.repositories.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "car_photos")
public class CarPhotoJpaEntity {

	@Id
	private Long id;

	@ManyToOne
	@JoinColumn(name = "car_id", nullable = false)
	private CarJpaEntity car;

	@Column(name = "object_key", nullable = false, length = 512)
	private String objectKey;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public CarJpaEntity getCar() {
		return car;
	}

	public void setCar(CarJpaEntity car) {
		this.car = car;
	}

	public String getObjectKey() {
		return objectKey;
	}

	public void setObjectKey(String objectKey) {
		this.objectKey = objectKey;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}
}

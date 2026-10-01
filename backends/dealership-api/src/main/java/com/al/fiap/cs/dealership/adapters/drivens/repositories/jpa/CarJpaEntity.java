package com.al.fiap.cs.dealership.adapters.drivens.repositories.jpa;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cars")
public class CarJpaEntity {

	@Id
	private Long id;

	@Column(name = "brand_id", nullable = false)
	private Long brandId;

	@Column(name = "model_id", nullable = false)
	private Long modelId;

	@Column(name = "color_id", nullable = false)
	private Long colorId;

	@Column(name = "year_id", nullable = false)
	private Long yearId;

	@Column(nullable = false, precision = 14, scale = 2)
	private BigDecimal price;

	@Column(nullable = false, length = 32)
	private String status;

	@Column(name = "buyer_account_id")
	private Long buyerAccountId;

	@Column(name = "buyer_cpf", length = 11)
	private String buyerCpf;

	@Column(name = "payment_code", length = 64, unique = true)
	private String paymentCode;

	@Column(name = "sold_at")
	private Instant soldAt;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "created_by", nullable = false)
	private Long createdBy;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "updated_by", nullable = false)
	private Long updatedBy;

	@OneToMany(mappedBy = "car", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@OrderBy("sortOrder ASC")
	private List<CarPhotoJpaEntity> photos = new ArrayList<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getBrandId() {
		return brandId;
	}

	public void setBrandId(Long brandId) {
		this.brandId = brandId;
	}

	public Long getModelId() {
		return modelId;
	}

	public void setModelId(Long modelId) {
		this.modelId = modelId;
	}

	public Long getColorId() {
		return colorId;
	}

	public void setColorId(Long colorId) {
		this.colorId = colorId;
	}

	public Long getYearId() {
		return yearId;
	}

	public void setYearId(Long yearId) {
		this.yearId = yearId;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getBuyerAccountId() {
		return buyerAccountId;
	}

	public void setBuyerAccountId(Long buyerAccountId) {
		this.buyerAccountId = buyerAccountId;
	}

	public String getBuyerCpf() {
		return buyerCpf;
	}

	public void setBuyerCpf(String buyerCpf) {
		this.buyerCpf = buyerCpf;
	}

	public String getPaymentCode() {
		return paymentCode;
	}

	public void setPaymentCode(String paymentCode) {
		this.paymentCode = paymentCode;
	}

	public Instant getSoldAt() {
		return soldAt;
	}

	public void setSoldAt(Instant soldAt) {
		this.soldAt = soldAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Long getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Long createdBy) {
		this.createdBy = createdBy;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Long getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(Long updatedBy) {
		this.updatedBy = updatedBy;
	}

	public List<CarPhotoJpaEntity> getPhotos() {
		return photos;
	}

	public void setPhotos(List<CarPhotoJpaEntity> photos) {
		this.photos = photos;
	}
}

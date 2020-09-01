package com.mosip.io.pojo;

public class MosipDeviceServiceRequest {
	private String deviceProviderId;
	private Boolean isActive;
	private String make;
	private String model;
	private String regDeviceSubCode;
	private String regDeviceTypeCode;
	private Integer swBinaryHash;
	private String swCreateDateTime;
	private String swExpiryDateTime;
	private String swVersion;
	public MosipDeviceServiceRequest() {}
	public MosipDeviceServiceRequest(String deviceProviderId, Boolean isActive, String make, String model,
			String regDeviceSubCode, String regDeviceTypeCode, Integer swBinaryHash, String swCreateDateTime,
			String swExpiryDateTime, String swVersion) {
		this.deviceProviderId = deviceProviderId;
		this.isActive = isActive;
		this.make = make;
		this.model = model;
		this.regDeviceSubCode = regDeviceSubCode;
		this.regDeviceTypeCode = regDeviceTypeCode;
		this.swBinaryHash = swBinaryHash;
		this.swCreateDateTime = swCreateDateTime;
		this.swExpiryDateTime = swExpiryDateTime;
		this.swVersion = swVersion;
	}

	@Override
	public String toString() {
		return "{ deviceProviderId=" + deviceProviderId + ", isActive=" + isActive + ", make="
				+ make + ", model=" + model + ", regDeviceSubCode=" + regDeviceSubCode + ", regDeviceTypeCode="
				+ regDeviceTypeCode + ", swBinaryHash=" + swBinaryHash + ", swCreateDateTime=" + swCreateDateTime
				+ ", swExpiryDateTime=" + swExpiryDateTime + ", swVersion=" + swVersion + "}";
	}

	public String getDeviceProviderId() {
	return deviceProviderId;
	}

	public void setDeviceProviderId(String deviceProviderId) {
	this.deviceProviderId = deviceProviderId;
	}

	public Boolean getIsActive() {
	return isActive;
	}

	public void setIsActive(Boolean isActive) {
	this.isActive = isActive;
	}

	public String getMake() {
	return make;
	}

	public void setMake(String make) {
	this.make = make;
	}

	public String getModel() {
	return model;
	}

	public void setModel(String model) {
	this.model = model;
	}

	public String getRegDeviceSubCode() {
	return regDeviceSubCode;
	}

	public void setRegDeviceSubCode(String regDeviceSubCode) {
	this.regDeviceSubCode = regDeviceSubCode;
	}

	public String getRegDeviceTypeCode() {
	return regDeviceTypeCode;
	}

	public void setRegDeviceTypeCode(String regDeviceTypeCode) {
	this.regDeviceTypeCode = regDeviceTypeCode;
	}

	public Integer getSwBinaryHash() {
	return swBinaryHash;
	}

	public void setSwBinaryHash(Integer swBinaryHash) {
	this.swBinaryHash = swBinaryHash;
	}

	public String getSwCreateDateTime() {
	return swCreateDateTime;
	}

	public void setSwCreateDateTime(String swCreateDateTime) {
	this.swCreateDateTime = swCreateDateTime;
	}

	public String getSwExpiryDateTime() {
	return swExpiryDateTime;
	}

	public void setSwExpiryDateTime(String swExpiryDateTime) {
	this.swExpiryDateTime = swExpiryDateTime;
	}

	public String getSwVersion() {
	return swVersion;
	}

	public void setSwVersion(String swVersion) {
	this.swVersion = swVersion;
	}
}

package com.mosip.io.pojo;

public class RegisterDeviceDTO {
	private String serialNo;
	private String deviceProvider;
	private String deviceProviderId;
	private String make;
	private String model;
	private String dateTime;
	private String type;
	private String deviceSubType;
	public RegisterDeviceDTO() {}
	public RegisterDeviceDTO(String serialNo, String deviceProvider, String deviceProviderId, String make, String model,
			String dateTime, String type, String deviceSubType) {
		this.serialNo = serialNo;
		this.deviceProvider = deviceProvider;
		this.deviceProviderId = deviceProviderId;
		this.make = make;
		this.model = model;
		this.dateTime = dateTime;
		this.type = type;
		this.deviceSubType = deviceSubType;
	}

	public String getSerialNo() {
	return serialNo;
	}

	public void setSerialNo(String serialNo) {
	this.serialNo = serialNo;
	}

	public String getDeviceProvider() {
	return deviceProvider;
	}

	public void setDeviceProvider(String deviceProvider) {
	this.deviceProvider = deviceProvider;
	}

	public String getDeviceProviderId() {
	return deviceProviderId;
	}

	public void setDeviceProviderId(String deviceProviderId) {
	this.deviceProviderId = deviceProviderId;
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

	public String getDateTime() {
	return dateTime;
	}

	public void setDateTime(String dateTime) {
	this.dateTime = dateTime;
	}

	public String getType() {
	return type;
	}

	public void setType(String type) {
	this.type = type;
	}

	public String getDeviceSubType() {
	return deviceSubType;
	}

	public void setDeviceSubType(String deviceSubType) {
	this.deviceSubType = deviceSubType;
	}
	@Override
	public String toString() {
		return "{ serialNo=" + serialNo + ", deviceProvider=" + deviceProvider + ", deviceProviderId="
				+ deviceProviderId + ", make=" + make + ", model=" + model + ", dateTime=" + dateTime + ", type=" + type
				+ ", deviceSubType=" + deviceSubType + "}";
	}

}

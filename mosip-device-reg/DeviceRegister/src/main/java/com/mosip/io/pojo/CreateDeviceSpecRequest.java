package com.mosip.io.pojo;

public class CreateDeviceSpecRequest {
	private String brand;
	private String description;
	private String deviceTypeCode;
	private String id;
	private Boolean isActive;
	private String langCode;
	private String minDriverversion;
	private String model;
	private String name;
	public CreateDeviceSpecRequest() {}
	public CreateDeviceSpecRequest(String brand, String description, String deviceTypeCode, String id, Boolean isActive,
			String langCode, String minDriverversion, String model, String name) {
		this.brand = brand;
		this.description = description;
		this.deviceTypeCode = deviceTypeCode;
		this.id = id;
		this.isActive = isActive;
		this.langCode = langCode;
		this.minDriverversion = minDriverversion;
		this.model = model;
		this.name = name;
	}

	public String getBrand() {
	return brand;
	}

	public void setBrand(String brand) {
	this.brand = brand;
	}

	public String getDescription() {
	return description;
	}

	public void setDescription(String description) {
	this.description = description;
	}

	public String getDeviceTypeCode() {
	return deviceTypeCode;
	}

	public void setDeviceTypeCode(String deviceTypeCode) {
	this.deviceTypeCode = deviceTypeCode;
	}

	public String getId() {
	return id;
	}

	public void setId(String id) {
	this.id = id;
	}

	public Boolean getIsActive() {
	return isActive;
	}

	public void setIsActive(Boolean isActive) {
	this.isActive = isActive;
	}

	public String getLangCode() {
	return langCode;
	}

	public void setLangCode(String langCode) {
	this.langCode = langCode;
	}

	public String getMinDriverversion() {
	return minDriverversion;
	}

	public void setMinDriverversion(String minDriverversion) {
	this.minDriverversion = minDriverversion;
	}

	public String getModel() {
	return model;
	}

	public void setModel(String model) {
	this.model = model;
	}

	public String getName() {
	return name;
	}

	public void setName(String name) {
	this.name = name;
	}
	@Override
	public String toString() {
		return "{ brand=" + brand + ", description=" + description + ", deviceTypeCode="
				+ deviceTypeCode + ", id=" + id + ", isActive=" + isActive + ", langCode=" + langCode
				+ ", minDriverversion=" + minDriverversion + ", model=" + model + ", name=" + name + "}";
	}

}

package com.mosip.io.pmp.model;

public class CreateDeviceRequest {
	private String deviceSpecId;
	private String id;
	private String ipAddress;
	private Boolean isActive;
	private String langCode;
	private String macAddress;
	private String name;
	private String regCenterId;
	private String serialNum;
	private String validityDateTime;
	private String zoneCode;
	public CreateDeviceRequest() {}
	public CreateDeviceRequest(String deviceSpecId, String id, String ipAddress, Boolean isActive, String langCode,
			String macAddress, String name, String regCenterId,String serialNum, String validityDateTime, String zoneCode) {
		this.deviceSpecId = deviceSpecId;
		this.id = id;
		this.ipAddress = ipAddress;
		this.isActive = isActive;
		this.langCode = langCode;
		this.macAddress = macAddress;
		this.name = name;
		this.regCenterId=regCenterId;
		this.serialNum = serialNum;
		this.validityDateTime = validityDateTime;
		this.zoneCode = zoneCode;
	}

	
	public String getRegCenterId() {
		return regCenterId;
	}
	public void setRegCenterId(String regCenterId) {
		this.regCenterId = regCenterId;
	}
	public String getDeviceSpecId() {
	return deviceSpecId;
	}

	public void setDeviceSpecId(String deviceSpecId) {
	this.deviceSpecId = deviceSpecId;
	}

	public String getId() {
	return id;
	}

	public void setId(String id) {
	this.id = id;
	}

	public String getIpAddress() {
	return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
	this.ipAddress = ipAddress;
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

	public String getMacAddress() {
	return macAddress;
	}

	public void setMacAddress(String macAddress) {
	this.macAddress = macAddress;
	}

	public String getName() {
	return name;
	}

	public void setName(String name) {
	this.name = name;
	}

	public String getSerialNum() {
	return serialNum;
	}

	public void setSerialNum(String serialNum) {
	this.serialNum = serialNum;
	}

	public String getValidityDateTime() {
	return validityDateTime;
	}

	public void setValidityDateTime(String validityDateTime) {
	this.validityDateTime = validityDateTime;
	}

	public String getZoneCode() {
	return zoneCode;
	}

	public void setZoneCode(String zoneCode) {
	this.zoneCode = zoneCode;
	}
	@Override
	public String toString() {
		return "{ deviceSpecId=" + deviceSpecId + ", id=" + id + ", ipAddress=" + ipAddress
				+ ", isActive=" + isActive + ", langCode=" + langCode + ", macAddress=" + macAddress + ", name=" + name
				+ ", serialNum=" + serialNum + ", validityDateTime=" + validityDateTime + ", zoneCode=" + zoneCode
				+ "}";
	}
}

package com.mosip.io.pojo;

public class DeviceRegistrationCenterMappingRequest {
	private String deviceId;
	private Boolean isActive;
	private String langCode;
	private String regCenterId;
	public DeviceRegistrationCenterMappingRequest() {}
	public DeviceRegistrationCenterMappingRequest(String deviceId, Boolean isActive, String langCode, String regCenterId) {
		this.deviceId = deviceId;
		this.isActive = isActive;
		this.langCode = langCode;
		this.regCenterId = regCenterId;
	}

	public String getDeviceId() {
	return deviceId;
	}

	public void setDeviceId(String deviceId) {
	this.deviceId = deviceId;
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

	public String getRegCenterId() {
	return regCenterId;
	}

	public void setRegCenterId(String regCenterId) {
	this.regCenterId = regCenterId;
	}
	@Override
	public String toString() {
		return "{ deviceId=" + deviceId + ", isActive=" + isActive + ", langCode=" + langCode
				+ ", regCenterId=" + regCenterId + "}";
	}
}

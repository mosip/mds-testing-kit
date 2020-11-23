package com.mosip.io.pmp.model;

public class ValidateHistoryRequest {
	private String deviceCode;
	private String deviceServiceVersion;
	private ValidateHistory_DigitalId digitalId;
	private String purpose;
	private String timeStamp;

	public String getDeviceCode() {
	return deviceCode;
	}

	public void setDeviceCode(String deviceCode) {
	this.deviceCode = deviceCode;
	}

	public String getDeviceServiceVersion() {
	return deviceServiceVersion;
	}

	public void setDeviceServiceVersion(String deviceServiceVersion) {
	this.deviceServiceVersion = deviceServiceVersion;
	}

	public ValidateHistory_DigitalId getDigitalId() {
	return digitalId;
	}

	public void setDigitalId(ValidateHistory_DigitalId digitalId) {
	this.digitalId = digitalId;
	}

	public String getPurpose() {
	return purpose;
	}

	public void setPurpose(String purpose) {
	this.purpose = purpose;
	}

	public String getTimeStamp() {
	return timeStamp;
	}

	public void setTimeStamp(String timeStamp) {
	this.timeStamp = timeStamp;
	}

	@Override
	public String toString() {
		return "{ deviceCode=" + deviceCode + ", deviceServiceVersion=" + deviceServiceVersion
				+ ", digitalId=" + digitalId + ", purpose=" + purpose + ", timeStamp=" + timeStamp + "}";
	}
}

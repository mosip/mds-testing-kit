package com.mosip.io.pojo;

public class RegisterDeviceInfoRequest {
	private String deviceSubId;
	private String certification;
	private String digitalId;
	private String firmware;
	private String deviceExpiry;
	private String timeStamp;
	public RegisterDeviceInfoRequest() {}
	public RegisterDeviceInfoRequest(String deviceSubId, String certification, String digitalId, String firmware,
			String deviceExpiry, String timeStamp) {
		this.deviceSubId = deviceSubId;
		this.certification = certification;
		this.digitalId = digitalId;
		this.firmware = firmware;
		this.deviceExpiry = deviceExpiry;
		this.timeStamp = timeStamp;
	}

	public String getDeviceSubId() {
	return deviceSubId;
	}

	public void setDeviceSubId(String deviceSubId) {
	this.deviceSubId = deviceSubId;
	}

	public String getCertification() {
	return certification;
	}

	public void setCertification(String certification) {
	this.certification = certification;
	}

	public String getDigitalId() {
	return digitalId;
	}

	public void setDigitalId(String digitalId) {
	this.digitalId = digitalId;
	}

	public String getFirmware() {
	return firmware;
	}

	public void setFirmware(String firmware) {
	this.firmware = firmware;
	}

	public String getDeviceExpiry() {
	return deviceExpiry;
	}

	public void setDeviceExpiry(String deviceExpiry) {
	this.deviceExpiry = deviceExpiry;
	}

	public String getTimeStamp() {
	return timeStamp;
	}

	public void setTimeStamp(String timeStamp) {
	this.timeStamp = timeStamp;
	}
	@Override
	public String toString() {
		return "{ deviceSubId=" + deviceSubId + ", certification=" + certification
				+ ", digitalId=" + digitalId + ", firmware=" + firmware + ", deviceExpiry=" + deviceExpiry
				+ ", timeStamp=" + timeStamp + "}";
	}

}

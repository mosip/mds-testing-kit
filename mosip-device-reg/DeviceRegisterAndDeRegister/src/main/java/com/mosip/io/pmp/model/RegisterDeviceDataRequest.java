package com.mosip.io.pmp.model;

public class RegisterDeviceDataRequest {
	private String deviceData;
	public RegisterDeviceDataRequest() {}
	public RegisterDeviceDataRequest(String deviceData) {
		this.deviceData = deviceData;
	}

	public String getDeviceData() {
	return deviceData;
	}

	public void setDeviceData(String deviceData) {
	this.deviceData = deviceData;
	}
	@Override
	public String toString() {
		return "{ deviceData=" + deviceData + "}";
	}

}

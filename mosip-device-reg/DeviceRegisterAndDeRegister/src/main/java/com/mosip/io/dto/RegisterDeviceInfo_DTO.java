package com.mosip.io.dto;

import com.mosip.io.pmp.model.RegisterDeviceInfoRequest;

public class RegisterDeviceInfo_DTO {
	private String deviceId;
	private String purpose;
	//private RegisterDeviceInfoRequest deviceInfo;
	private String deviceInfo;
	private String foundationalTrustProviderId;
	public RegisterDeviceInfo_DTO() {}
	public RegisterDeviceInfo_DTO(String deviceId, String purpose, String deviceInfo,
			String foundationalTrustProviderId) {
		this.deviceId = deviceId;
		this.purpose = purpose;
		this.deviceInfo = deviceInfo;
		this.foundationalTrustProviderId = foundationalTrustProviderId;
	}

	public String getDeviceId() {
	return deviceId;
	}

	public void setDeviceId(String deviceId) {
	this.deviceId = deviceId;
	}

	public String getPurpose() {
	return purpose;
	}

	public void setPurpose(String purpose) {
	this.purpose = purpose;
	}

	public String getDeviceInfo() {
	return deviceInfo;
	}

	public void setDeviceInfo(String deviceInfo) {
	this.deviceInfo = deviceInfo;
	}

	public String getFoundationalTrustProviderId() {
	return foundationalTrustProviderId;
	}

	public void setFoundationalTrustProviderId(String foundationalTrustProviderId) {
	this.foundationalTrustProviderId = foundationalTrustProviderId;
	}
	@Override
	public String toString() {
		return "{ deviceId=" + deviceId + ", purpose=" + purpose + ", deviceInfo=" + deviceInfo
				+ ", foundationalTrustProviderId=" + foundationalTrustProviderId + "}";
	}
}

package com.mosip.io.pmp.model;

public class ValidateHistory_DigitalId {
	private String dateTime;
	private String deviceSubType;
	private String dp;
	private String dpId;
	private String make;
	private String model;
	private String serialNo;
	private String type;
	public ValidateHistory_DigitalId() {}
	public ValidateHistory_DigitalId(String dateTime, String deviceSubType, String dp, String dpId, String make,
			String model, String serialNo, String type) {
		this.dateTime = dateTime;
		this.deviceSubType = deviceSubType;
		this.dp = dp;
		this.dpId = dpId;
		this.make = make;
		this.model = model;
		this.serialNo = serialNo;
		this.type = type;
	}

	public String getDateTime() {
	return dateTime;
	}

	public void setDateTime(String dateTime) {
	this.dateTime = dateTime;
	}

	public String getDeviceSubType() {
	return deviceSubType;
	}

	public void setDeviceSubType(String deviceSubType) {
	this.deviceSubType = deviceSubType;
	}

	public String getDp() {
	return dp;
	}

	public void setDp(String dp) {
	this.dp = dp;
	}

	public String getDpId() {
	return dpId;
	}

	public void setDpId(String dpId) {
	this.dpId = dpId;
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

	public String getSerialNo() {
	return serialNo;
	}

	public void setSerialNo(String serialNo) {
	this.serialNo = serialNo;
	}

	public String getType() {
	return type;
	}

	public void setType(String type) {
	this.type = type;
	}
	@Override
	public String toString() {
		return "{ dateTime=" + dateTime + ", deviceSubType=" + deviceSubType + ", dp=" + dp
				+ ", dpId=" + dpId + ", make=" + make + ", model=" + model + ", serialNo=" + serialNo + ", type=" + type
				+ "}";
	}
}

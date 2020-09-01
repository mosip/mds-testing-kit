package com.mosip.io.pojo;

public class DeviceRegisterRequest {
	@Override
	public String toString() {
		return "{address=" + address + ", certificateAlias=" + certificateAlias
				+ ", contactNumber=" + contactNumber + ", email=" + email + ", isActive=" + isActive + ", vendorName="
				+ vendorName + "}";
	}

	private String address;
	private String certificateAlias;
	private String contactNumber;
	private String email;
	private Boolean isActive;
	private String vendorName;
	public DeviceRegisterRequest() {}

	public String getAddress() {
	return address;
	}

	public void setAddress(String address) {
	this.address = address;
	}

	public String getCertificateAlias() {
	return certificateAlias;
	}

	public void setCertificateAlias(String certificateAlias) {
	this.certificateAlias = certificateAlias;
	}

	public String getContactNumber() {
	return contactNumber;
	}

	public void setContactNumber(String contactNumber) {
	this.contactNumber = contactNumber;
	}

	public String getEmail() {
	return email;
	}

	public void setEmail(String email) {
	this.email = email;
	}

	public Boolean getIsActive() {
	return isActive;
	}

	public void setIsActive(Boolean isActive) {
	this.isActive = isActive;
	}

	public String getVendorName() {
	return vendorName;
	}

	public void setVendorName(String vendorName) {
	this.vendorName = vendorName;
	}

}

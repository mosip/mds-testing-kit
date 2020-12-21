package com.mosip.io.dto;

import com.mosip.io.pmp.model.DeviceRegisterRequest;
import com.mosip.io.pmp.model.Metadata;

public class DeviceRegisterDTO {
	private String id;
	private Metadata metadata=null;
	private DeviceRegisterRequest request;
	private String requesttime;
	private String version;
	public DeviceRegisterDTO(){}

	public String getId() {
	return id;
	}

	@Override
	public String toString() {
		return "{id=" + id + ", metadata=" + metadata + ", request=" + request + ", requesttime="
				+ requesttime + ", version=" + version + "}";
	}

	public void setId(String id) {
	this.id = id;
	}

	public Metadata getMetadata() {
	return metadata;
	}

	public void setMetadata(Metadata metadata) {
	this.metadata = metadata;
	}

	public DeviceRegisterRequest getRequest() {
	return request;
	}

	public void setRequest(DeviceRegisterRequest request) {
	this.request = request;
	}

	public String getRequesttime() {
	return requesttime;
	}

	public void setRequesttime(String requesttime) {
	this.requesttime = requesttime;
	}

	public String getVersion() {
	return version;
	}

	public void setVersion(String version) {
	this.version = version;
	}

}

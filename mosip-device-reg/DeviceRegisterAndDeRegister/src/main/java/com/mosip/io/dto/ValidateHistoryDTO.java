package com.mosip.io.dto;

import com.mosip.io.pmp.model.Metadata;
import com.mosip.io.pmp.model.ValidateHistoryRequest;

public class ValidateHistoryDTO {
	private String id;
	private Metadata metadata;
	private ValidateHistoryRequest request;
	private String requesttime;
	private String version;

	public String getId() {
	return id;
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

	public ValidateHistoryRequest getRequest() {
	return request;
	}

	public void setRequest(ValidateHistoryRequest request) {
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

	@Override
	public String toString() {
		return "{ id=" + id + ", metadata=" + metadata + ", request=" + request + ", requesttime="
				+ requesttime + ", version=" + version + "}";
	}
}

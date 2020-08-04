package com.mosip.io.pojo;

public class CreateDeviceDTO {
	private String id;
	private Metadata metadata=null;
	private CreateDeviceRequest request;
	private String requesttime;
	private String version;
	public CreateDeviceDTO() {}
	public CreateDeviceDTO(String id, Metadata metadata, CreateDeviceRequest request, String requesttime,
			String version) {
		this.id = id;
		this.metadata = metadata;
		this.request = request;
		this.requesttime = requesttime;
		this.version = version;
	}

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

	public CreateDeviceRequest getRequest() {
	return request;
	}

	public void setRequest(CreateDeviceRequest request) {
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

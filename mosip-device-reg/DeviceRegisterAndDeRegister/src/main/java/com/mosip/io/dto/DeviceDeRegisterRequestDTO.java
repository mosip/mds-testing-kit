package com.mosip.io.dto;

import com.mosip.io.pmp.model.DeviceDeRegisterRequest;

public class DeviceDeRegisterRequestDTO {
private String id;
private String version;
private DeviceDeRegisterRequest request= new DeviceDeRegisterRequest();
private String requesttime;
private String metadata;
public DeviceDeRegisterRequestDTO() {}
public DeviceDeRegisterRequestDTO(String id, String version, DeviceDeRegisterRequest request, String requesttime,String metadata) {
	this.id = id;
	this.version = version;
	this.request = request;
	this.requesttime = requesttime;
	this.metadata=metadata;
}
public String getId() {
	return id;
}
public void setId(String id) {
	this.id = id;
}
public String getVersion() {
	return version;
}
public void setVersion(String version) {
	this.version = version;
}
public DeviceDeRegisterRequest getRequest() {
	return request;
}
public void setRequest(DeviceDeRegisterRequest request) {
	this.request = request;
}
public String getRequesttime() {
	return requesttime;
}
public void setRequesttime(String requesttime) {
	this.requesttime = requesttime;
}
public String getMetadata() {
	return metadata;
}
public void setMetadata(String metadata) {
	this.metadata = metadata;
}
}

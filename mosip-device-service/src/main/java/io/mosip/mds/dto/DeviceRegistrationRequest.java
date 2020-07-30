package io.mosip.mds.dto;

import lombok.Data;

@Data
public class DeviceRegistrationRequest {

	private String id;
	private DeviceDataDto request;
	private String requesttime;
	private String version;
}

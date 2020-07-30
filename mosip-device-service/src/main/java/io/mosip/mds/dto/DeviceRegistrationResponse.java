package io.mosip.mds.dto;

import lombok.Data;

@Data
public class DeviceRegistrationResponse {
	
	private String id;
	private String version;
	private String responsetime;
	private String response;
	private RegirtrationError[] error;
}

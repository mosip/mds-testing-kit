package io.mosip.mds.dto;

import lombok.Data;

@Data
public class DeviceDeRegistrationResponse {
	private String id;
	private String version;
	
	//DeRegistrationDetails
	private String response;
	
	private String responsetime;
	private RegirtrationError[] error;
}

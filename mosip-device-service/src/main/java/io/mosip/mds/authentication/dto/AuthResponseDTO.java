package io.mosip.mds.authentication.dto;

import lombok.Data;

@Data
public class AuthResponseDTO {

	private String id;
	private Object metadata;
	private String responsetime;
	private String version;
	private AuthError[] errors;
	private ResponseDTO response;
	
}
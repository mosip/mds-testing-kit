package io.mosip.mds.authentication.dto;

import lombok.Data;

@Data
public class AuthError {

	private String errorCode;
	private String message;
}
package io.mosip.authentication.demo.dto;

import lombok.Data;

@Data
public class AuthError {

	private String errorCode;
	private String message;
}
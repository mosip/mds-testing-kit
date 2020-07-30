package io.mosip.authentication.demo.dto;

import java.util.List;

import lombok.Data;

/**
 * The Class RequestDTO.
 * 
 */
@Data
public class RequestDTO {

	/** variable to hold otp value */
	private String otp;

	/** variable to hold timestamp value */
	private String timestamp;

	/** variable to hold identity value */
	private IdentityDTO demographics;

	/** List of biometric identity info */
	private List<BioIdentityInfoDTO> biometrics;

}
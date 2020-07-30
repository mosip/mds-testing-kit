package io.mosip.authentication.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * The Class IdentityInfoDTO.
 * 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdentityInfoDTO {

	/** Variable to hold language */
	private String language;

	/** Variable to hold value */
	private String value;
}
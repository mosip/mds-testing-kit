package io.mosip.authentication.demo.dto;

import lombok.Data;

/**
 * The Class CryptomanagerRequestDto.
 * 
 */
@Data
public class CryptomanagerRequestDto {
	String applicationId;
	String data;
	String referenceId;
	String salt;
	String timeStamp;
}
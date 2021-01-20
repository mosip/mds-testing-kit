package io.mosip.mds.authentication.dto;

import java.util.Map;

import lombok.Data;

/**
 * The Class EncryptionRequestDto.
 */
@Data
public class EncryptionRequestDto {
	
	private Map<String, Object> identityRequest;

}
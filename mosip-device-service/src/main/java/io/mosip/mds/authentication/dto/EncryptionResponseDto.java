package io.mosip.mds.authentication.dto;

import lombok.Data;

/**
 * The Class EncryptionResponseDto.
 * 
 */
@Data
public class EncryptionResponseDto {
	String encryptedSessionKey;
	String encryptedIdentity;
	String requestHMAC;
}
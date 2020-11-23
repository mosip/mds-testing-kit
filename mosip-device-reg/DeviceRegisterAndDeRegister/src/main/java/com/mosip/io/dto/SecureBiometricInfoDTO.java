package com.mosip.io.dto;

import com.mosip.io.pmp.model.Metadata;
import com.mosip.io.pmp.model.SecureBiometricInfoRequest;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SecureBiometricInfoDTO {
	private String id;
	private Metadata metadata;
	private SecureBiometricInfoRequest request;
	private String requesttime;
	private String version;
}

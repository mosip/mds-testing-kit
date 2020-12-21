package com.mosip.io.pmp.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SecureBiometricInfoRequest {
	private String deviceDetailId;
	private Boolean isItForRegistrationDevice;
	private String swBinaryHash;
	private String swCreateDateTime;
	private String swExpiryDateTime;
	private String swVersion;
}

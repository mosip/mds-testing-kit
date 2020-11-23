package com.mosip.io.dto;

import com.mosip.io.pmp.model.ApproveSecureBiometricInfoRequest;
import com.mosip.io.pmp.model.Metadata;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApproveSecureBiometricInfoDTO {
	private String id;
	private Metadata metadata;
	private ApproveSecureBiometricInfoRequest request;
	private String requesttime;
	private String version;
}

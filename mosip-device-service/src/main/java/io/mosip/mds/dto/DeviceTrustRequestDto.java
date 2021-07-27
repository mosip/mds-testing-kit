package io.mosip.mds.dto;

import lombok.Data;

@Data
public class DeviceTrustRequestDto {

	String certificateData;
	String partnerDomain;
}
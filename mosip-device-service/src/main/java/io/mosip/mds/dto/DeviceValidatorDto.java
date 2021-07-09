package io.mosip.mds.dto;

import lombok.Data;

@Data
public class DeviceValidatorDto {

	String id;
	Object metadata;
	DeviceTrustRequestDto request;
//	DeviceValidatorRequestDto request;
	String requesttime;
	String version;
}

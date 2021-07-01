package io.mosip.mds.dto;

import java.util.List;

import lombok.Data;

@Data
public class DeviceValidatorResponseDto {

	String id;
	Object metadata;
	DeviceValidatorResponse response;
	String responsetime;
	String version;
	List<ErrorDto> errors;
}

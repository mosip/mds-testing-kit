package io.mosip.mds.dto;

import lombok.Data;

@Data
public class RegistrationDeviceInfoDto {

	private String deviceSubId;
	private String certification;
	private String digitalId;
	private String firmware;
	private String deviceExpiry;
	private String timestamp;
}

package io.mosip.mds.dto;

import lombok.Data;

@Data
public class RegistrationResponseDecoded {

	private String status;
	private String digitalId;
	private String deviceCode;
	private String timestamp;
	private String env;
}

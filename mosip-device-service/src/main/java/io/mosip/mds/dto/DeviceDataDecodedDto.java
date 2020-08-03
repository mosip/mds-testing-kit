package io.mosip.mds.dto;

import lombok.Data;

@Data
public class DeviceDataDecodedDto {
	private String deviceId;
	private String purpose;
	private RegistrationDeviceInfoDto deviceInfo;
	private String foundationalTrustProviderId;
}

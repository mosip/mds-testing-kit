package com.mosip.io.pmp.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DeviceDetailRequest {
	private String deviceProviderId;
	private String deviceSubTypeCode;
	private String deviceTypeCode;
	private String id;
	private Boolean isItForRegistrationDevice;
	private String make;
	private String model;
	private String partnerOrganizationName;
}

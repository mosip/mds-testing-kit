package com.mosip.io.pmp.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DeviceDeRegisterRequest {
	private String device;
	private Boolean isItForRegistrationDevice;

}

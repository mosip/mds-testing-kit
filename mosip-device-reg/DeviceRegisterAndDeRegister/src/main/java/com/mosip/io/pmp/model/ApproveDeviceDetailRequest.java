package com.mosip.io.pmp.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApproveDeviceDetailRequest {
	private String approvalStatus;
	private String id;
	private Boolean isItForRegistrationDevice;
}

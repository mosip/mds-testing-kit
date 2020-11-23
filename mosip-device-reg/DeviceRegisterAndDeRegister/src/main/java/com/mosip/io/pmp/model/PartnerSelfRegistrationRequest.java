package com.mosip.io.pmp.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PartnerSelfRegistrationRequest {
	private String address;
	private String contactNumber;
	private String emailId;
	private String organizationName;
	private String partnerType;
	private String policyGroup;
	private String partnerId;
}

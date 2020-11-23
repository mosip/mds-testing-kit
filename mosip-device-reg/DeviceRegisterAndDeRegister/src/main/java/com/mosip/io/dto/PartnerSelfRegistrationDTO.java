package com.mosip.io.dto;

import com.mosip.io.pmp.model.Metadata;
import com.mosip.io.pmp.model.PartnerSelfRegistrationRequest;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PartnerSelfRegistrationDTO {
	private String id;
	private Metadata metadata;
	private PartnerSelfRegistrationRequest request;
	private String requesttime;
	private String version;
}

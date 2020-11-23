package com.mosip.io.dto;

import com.mosip.io.pmp.model.ApproveDeviceDetailRequest;
import com.mosip.io.pmp.model.Metadata;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApproveDeviceDetailDTO {
	private String id;
	private Metadata metadata;
	private ApproveDeviceDetailRequest request;
	private String requesttime;
	private String version;
}

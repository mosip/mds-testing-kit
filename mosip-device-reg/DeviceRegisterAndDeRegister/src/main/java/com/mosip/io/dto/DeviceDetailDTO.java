package com.mosip.io.dto;

import com.mosip.io.pmp.model.DeviceDetailRequest;
import com.mosip.io.pmp.model.Metadata;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DeviceDetailDTO {
	private String id;
	private Metadata metadata;
	private DeviceDetailRequest request;
	private String requesttime;
	private String version;
}

package com.mosip.io.dto;

import com.mosip.io.pmp.model.DefinePolicyGroupRequest;
import com.mosip.io.pmp.model.Metadata;

import lombok.Data;

import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DefinePolicyGroupDTO {
	private String id;
	private Metadata metadata;
	private DefinePolicyGroupRequest request;
	private String requesttime;
	private String version;
}

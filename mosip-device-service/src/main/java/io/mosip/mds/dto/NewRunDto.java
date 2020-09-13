package io.mosip.mds.dto;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class NewRunDto {
	public String email;

	public String runId;
	
	public String runName;
	
	public TestManagerDto targetProfile;
	
	public String[] tests;

}

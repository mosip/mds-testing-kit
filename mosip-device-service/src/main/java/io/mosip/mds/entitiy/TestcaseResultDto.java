package io.mosip.mds.entitiy;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TestcaseResultDto {

	private TestResultKey testResultKey;

	private String description;
	
	private String owner;
	
	private String request;
	
	private String response;
	
	private String validationResults;
	
	public boolean passed;
	
	private String deviceInfo;

	private LocalDateTime executedOn;

	private String currentState;

	public LocalDateTime createdOn;

	public String createdBy;
	
}

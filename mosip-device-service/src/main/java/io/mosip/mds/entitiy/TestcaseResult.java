package io.mosip.mds.entitiy;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name ="testcase_result")
public class TestcaseResult {

	@EmbeddedId
	private TestResultKey testResultKey;

	@Column(name="description")
	private String description;
	
	@Column(name="owner")
	private String owner;
	
	//@Lob
	@Column(name="request")
	private String request;
	
	//@Lob
	@Column(name="response")
	private String response;
	
	//@Lob
	@Column(name="validation_results")
	private String validationResults;
	
	@Column(name="passed")
	public boolean passed;
	
	//@Lob
	@Column(name="device_info")
	private String deviceInfo;

	@Column(name="executed_on")
	private LocalDateTime executedOn;

	@Column(name="current_status")
	private String currentState;

	@Column(name="cr_dtimes")
	public LocalDateTime createdOn;

	@Column(name="cr_by")
	public String createdBy;
	
}

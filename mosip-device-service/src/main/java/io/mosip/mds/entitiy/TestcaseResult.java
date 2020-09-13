package io.mosip.mds.entitiy;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name ="testcase_result")
public class TestcaseResult {

	@EmbeddedId
	public TestResultKey testResultKey;
	
	@Column(name="owner")
	public String owner;
	
	@Lob
	@Column(name="request")
	public String request;
	
	@Lob
	@Column(name="response")
	public String response;
	
	@Lob
	@Column(name="validation_results")
	public String validationResults;
	
	@Column(name="passed")
	public boolean passed;
	
	@Lob
	@Column(name="device_info")
	public String deviceInfo;
	
	
}

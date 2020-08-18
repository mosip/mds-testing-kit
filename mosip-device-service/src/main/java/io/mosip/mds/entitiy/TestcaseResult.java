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
	
	@Column(name="testcase_owner")
	public String testcaseOwner;
	
	@Lob
	@Column(name="testcase_request")
	public String testcaseRequest;
	
	@Lob
	@Column(name="testcase_response")
	public String testcaseResponse;
	
	@Lob
	@Column(name="testcase_result")
	public String validationResults;
	
	@Column(name="testcase_passed")
	public boolean testcasePassed;
	
	@Lob
	@Column(name="device_info")
	public String deviceInfo;
	
	
}

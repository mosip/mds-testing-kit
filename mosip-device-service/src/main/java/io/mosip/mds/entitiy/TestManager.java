package io.mosip.mds.entitiy;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name ="test_manager")
public class TestManager {

	@Id
	@Column(name = "run_id")
	public String runId;

	@Column(name = "mds_spec_version")
	public String mdsSpecVersion;


	public String purpose;

	@Column(name = "biometric_type")
	public String biometricType;

	@Column(name = "device_type")
	public String deviceSubType;

	@ElementCollection(targetClass=String.class)
	public List<String> tests;

	//	private TestRun persistRun(TestRun run)
	//	{
	//		boolean isAllInResponseValidationStage = run.testReport.values().stream()
	//				.allMatch( result -> result.currentState.startsWith("MDS Response Validations"));
	//		if(isAllInResponseValidationStage)
	//			run.runStatus = RunStatus.Done;
	//		return store.saveTestRun(run.user, run);
	//	}
}

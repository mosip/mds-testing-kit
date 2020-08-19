package io.mosip.mds.entitiy;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.TestRun;
import io.mosip.mds.dto.TestRun.RunStatus;
import lombok.Data;

@Entity
@Data
@Table(name ="test_manager")
public class TestManager {

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	Store store;
	
	@Id
	@Column(name = "run_id")
	public String runId;

	@Column(name = "mds_spec_version")
	public String mdsSpecVersion;


	public String process;

	@Column(name = "biometric_type")
	public String biometricType;

	@Column(name = "device_type")
	public String deviceSubType;

	public List<String> tests;

	//private static List<TestExtnDto> allTests = null;


	private TestRun persistRun(TestRun run)
	{
		boolean isAllInResponseValidationStage = run.testReport.values().stream()
				.allMatch( result -> result.currentState.startsWith("MDS Response Validations"));
		if(isAllInResponseValidationStage)
			run.runStatus = RunStatus.Done;
		return store.saveTestRun(run.user, run);
	}

}

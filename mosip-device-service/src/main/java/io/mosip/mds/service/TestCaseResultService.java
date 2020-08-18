package io.mosip.mds.service;

import java.util.List;

import io.mosip.mds.dto.TestManagerDto;
import io.mosip.mds.dto.TestRun;
import io.mosip.mds.dto.postresponse.RunExtnDto;
import io.mosip.mds.entitiy.TestcaseResult;

public interface TestCaseResultService {
	public List<TestcaseResult>  saveTestResult(TestRun run);

	public boolean getRunStatus(String runId);
	

	public RunExtnDto saveTestRun(TestManagerDto testManagerDto);
	
	public List<TestRun> getRuns(String email);
}

package io.mosip.mds.service;

import java.util.List;

import org.springframework.http.HttpEntity;

import io.mosip.mds.dto.TestDefinition;
import io.mosip.mds.dto.TestManagerDto;
import io.mosip.mds.dto.TestManagerGetDto;
import io.mosip.mds.dto.TestReport;
import io.mosip.mds.dto.TestRun;
import io.mosip.mds.dto.TestRunMetadata;
import io.mosip.mds.dto.getresponse.MasterDataResponseDto;


public interface TestManagerService {

	MasterDataResponseDto getMasterData();

	List<TestDefinition> getTests(TestManagerGetDto filter);

	List<TestRunMetadata> getRuns(String email);

	TestRunMetadata createRun(TestManagerDto testManagerDto);

	void saveTestResult(TestRun run);

	TestReport getReport(String runId);

	HttpEntity<byte[]> getPdfReport(String runId, String fileName) throws Exception;

}

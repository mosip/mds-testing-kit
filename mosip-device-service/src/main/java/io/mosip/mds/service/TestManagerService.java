package io.mosip.mds.service;

import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.RequestBody;

import io.mosip.mds.dto.TestManagerDto;
import io.mosip.mds.dto.TestManagerGetDto;
import io.mosip.mds.dto.TestReport;
import io.mosip.mds.dto.TestRun;
import io.mosip.mds.dto.getresponse.MasterDataResponseDto;
import io.mosip.mds.dto.getresponse.TestExtnDto;
import io.mosip.mds.dto.postresponse.RunExtnDto;

public interface TestManagerService {
	
	public RunExtnDto createRun(TestManagerDto testManagerDto);

	public MasterDataResponseDto getMasterData();
	
	public TestExtnDto getTest(@RequestBody TestManagerGetDto testManagerGetDto);

	public List<TestRun> getRuns(String email);

	public TestReport getReport(String runId);

	public HttpEntity<byte[]> getPdfReport(String runId, String fileName) throws Exception;

	public TestExtnDto[] getTests(TestManagerGetDto filter);

}

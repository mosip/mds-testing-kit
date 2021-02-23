package io.mosip.mds.service;

import io.mosip.mds.dto.ComposeRequestDto;
import io.mosip.mds.dto.DeviceInfoResponse;
import io.mosip.mds.dto.DiscoverResponse;
import io.mosip.mds.dto.TestRun;
import io.mosip.mds.dto.ValidateResponseRequestDto;

public interface TestRunnerService {
	
	public TestRun validateResponse(ValidateResponseRequestDto validateRequestDto) throws Exception;

	//public ComposeRequestResponseDto composeRequest(ComposeRequestDto composeRequestDto);

	public TestRun composeRequestForAllTests(ComposeRequestDto composeRequestDto) throws Exception;

	public DiscoverResponse[] decodeDiscoverInfo(String discoverInfo);

	public DeviceInfoResponse[] decodeDeviceInfo(String deviceInfo);
	
	public void downloadReport(String runId, String testId);

	String validateAuthRequest(String runId, String testId);
}

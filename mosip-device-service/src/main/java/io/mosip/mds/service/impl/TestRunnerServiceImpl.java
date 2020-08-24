package io.mosip.mds.service.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.ComposeRequestDto;
import io.mosip.mds.dto.DeviceDto;
import io.mosip.mds.dto.DeviceInfoResponse;
import io.mosip.mds.dto.DiscoverResponse;
import io.mosip.mds.dto.MdsResponse;
import io.mosip.mds.dto.TestResult;
import io.mosip.mds.dto.TestRun;
import io.mosip.mds.dto.TestRun.RunStatus;
import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.dto.getresponse.TestExtnDto;
import io.mosip.mds.dto.postresponse.ComposeRequestResponseDto;
import io.mosip.mds.dto.postresponse.RequestInfoDto;
import io.mosip.mds.dto.postresponse.ValidationResult;
import io.mosip.mds.entitiy.DeviceInfoHelper;
import io.mosip.mds.entitiy.DiscoverHelper;
import io.mosip.mds.entitiy.Store;
import io.mosip.mds.entitiy.Validator;
import io.mosip.mds.service.IMDSRequestBuilder;
import io.mosip.mds.service.IMDSResponseProcessor;
import io.mosip.mds.service.MDS_0_9_2_RequestBuilder;
import io.mosip.mds.service.MDS_0_9_5_RequestBuilder;
import io.mosip.mds.service.MDS_0_9_5_ResponseProcessor;
import io.mosip.mds.service.TestRunnerService;
import io.mosip.mds.util.Intent;
import io.mosip.mds.util.TestServiceUtil;

@Service
public class TestRunnerServiceImpl implements TestRunnerService {

	//	@Override
	//	public ComposeRequestResponseDto composeRequest(ComposeRequestDto composeRequestDto) {
	//		// TODO Auto-generated method stub
	//		//get test information bassed on test id
	//		// create private method discover
	//		
	//		return null;
	//	}

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	Store store;
	
	@Autowired
	TestServiceUtil testServiceUtil;
	
	@Autowired
	DeviceInfoHelper deviceInfoHelper;
	
	@Autowired
	DiscoverHelper discoverHelper;
	
	@Autowired
	IMDSResponseProcessor iMDSResponseProcessor;
	
	@Autowired
	IMDSRequestBuilder iMDSRequestBuilder;
	/**
	 * get information for testId when use that what should go in body
	 * 
	 * @param testId
	 * @param deviceDto
	 * @return
	 * 
	 * 
	 */
	public ComposeRequestResponseDto composeDiscover(String runId, String testId, DeviceDto deviceDto) {
		ComposeRequestResponseDto composeRequestResponseDto=new ComposeRequestResponseDto(runId, testId);
		RequestInfoDto requestInfoDto=new RequestInfoDto();
		requestInfoDto.url = "http://127.0.0.1:<device_service_port>/device";
		requestInfoDto.verb = "MOSIPDISC";
		requestInfoDto.body = "{\n	\"type\": \"type of the device\"\n" + "}";
		composeRequestResponseDto.requestInfoDto = requestInfoDto;

		return composeRequestResponseDto;
	}
	public ComposeRequestResponseDto composeDeviceInfo(String runId, String testId, DeviceDto deviceDto) {
		ComposeRequestResponseDto composeRequestResponseDto=new ComposeRequestResponseDto(runId, testId);
		RequestInfoDto requestInfoDto=new RequestInfoDto();
		requestInfoDto.url = "http://127.0.0.1:<device_service_port>/device";
		requestInfoDto.verb = "MOSIPDINFO";
		requestInfoDto.body = "{\n" + 
				"  \"type\": \"type of the device\"\n" + 
				"}";
		composeRequestResponseDto.requestInfoDto = requestInfoDto;

		return composeRequestResponseDto;
	}
	public ComposeRequestResponseDto composeStream(String runId, String testId, DeviceDto deviceDto) {
		ComposeRequestResponseDto composeRequestResponseDto=new ComposeRequestResponseDto(runId, testId);
		RequestInfoDto requestInfoDto=new RequestInfoDto();
		requestInfoDto.url = "http://127.0.0.1:<device_service_port>/stream";
		requestInfoDto.verb = "STREAM";
		requestInfoDto.body = "{\n" + 
				"  \"deviceId\": \"internal Id\",\n" + 
				"  \"deviceSubId\": \"device sub Id’s\"\n" + 
				"}";
		composeRequestResponseDto.requestInfoDto = requestInfoDto;

		return composeRequestResponseDto;
	}
	public ComposeRequestResponseDto composeCapture(String runId, String testId, DeviceDto deviceDto) {
		ComposeRequestResponseDto composeRequestResponseDto=new ComposeRequestResponseDto(runId, testId);
		RequestInfoDto requestInfoDto=new RequestInfoDto();
		requestInfoDto.url = "http://127.0.0.1:<device_service_port>/capture";
		requestInfoDto.verb = "CAPTURE";
		requestInfoDto.body = "{\n" + 
				"  \"env\":  \"Target environment\",\n" + 
				"  \"purpose\": \"Auth  or Registration\",\n" + 
				"  \"specVersion\": \"Expected version of the MDS spec\",\n" + 
				"  \"timeout\" : <Timeout for capture>,\n" + 
				"  \"captureTime\": <Time of capture request in ISO format including timezone>,\n" + 
				"  \"domainUri\": <URI of the auth server>,\n" + 
				"  \"transactionId\": <Transaction Id for the current capture>,\n" + 
				"  \"bio\": [\n" + 
				"    {\n" + 
				"      \"type\": <type of the biometric data>,\n" + 
				"      \"count\":  <fingerprint/Iris count, in case of face max is set to 1>,\n" + 
				"      \"bioSubType\": [\"Array of subtypes\"],\n" + 
				"      \"requestedScore\": <expected quality score that should match to complete a successful capture>,\n" + 
				"      \"deviceId\": <internal Id>,\n" + 
				"      \"deviceSubId\": <specific device Id>,\n" + 
				"      \"previousHash\": <hash of the previous block>\n" + 
				"    }\n" + 
				"  ],\n" + 
				"  customOpts: {\n" + 
				"    //max of 50 key value pair. This is so that vendor specific parameters can be sent if necessary. The values cannot be hard coded and have to be configured by the apps server and should be modifiable upon need by the applications. Vendors are free to include additional parameters and fine-tuning parameters. None of these values should go undocumented by the vendor. No sensitive data should be available in the customOpts.\n" + 
				"  }\n" + 
				"}";
		composeRequestResponseDto.requestInfoDto = requestInfoDto;

		return composeRequestResponseDto;
	}
	public ComposeRequestResponseDto composeRegistrationCapture(String runId, String testId, DeviceDto deviceDto) {
		ComposeRequestResponseDto composeRequestResponseDto=new ComposeRequestResponseDto(runId, testId);
		RequestInfoDto requestInfoDto=new RequestInfoDto();
		requestInfoDto.url = "http://127.0.0.1:<device_service_port>/capture";
		requestInfoDto.verb = "RCAPTURE";
		requestInfoDto.body = "{\n" + 
				"  \"env\":  \"target environment\",\n" + 
				"  \"specVersion\": \"expected MDS spec version\",\n" + 
				"  \"timeout\": \"timeout for registration capture\",\n" + 
				"  \"captureTime\": \"time of capture request in ISO format including timezone\",\n" + 
				"  \"registrationId\": \"registration Id for the current capture\",\n" + 
				"  \"bio\": [\n" + 
				"    {\n" + 
				"      \"type\": \"type of the biometric data\",\n" + 
				"      \"count\":  \"fingerprint/Iris count, in case of face max is set to 1\",\n" + 
				"      \"exception\": [\"finger or iris to be excluded\"],\n" + 
				"      \"requestedScore\": \"expected quality score that should match to complete a successful capture.\",\n" + 
				"      \"deviceId\": \"internal Id\",\n" + 
				"      \"deviceSubId\": \"specific device Id\",\n" + 
				"      \"previousHash\": \"hash of the previous block\"\n" + 
				"      }\n" + 
				"  ],\n" + 
				"  customOpts: {\n" + 
				"    //max of 50 key value pair. This is so that vendor specific parameters can be sent if necessary. The values cannot be hard coded and have to be configured by the apps server and should be modifiable upon need by the applications. Vendors are free to include additional parameters and fine-tuning parameters. None of these values should go undocumented by the vendor. No sensitive data should be available in the customOpts.\n" + 
				"  }\n" + 
				"}";
		composeRequestResponseDto.requestInfoDto = requestInfoDto;

		return composeRequestResponseDto;
	}

	public void getTest(String testId) {

	}

	//	@Override
	//	public ValidateResponseDto validateResponse(ValidateResponseRequestDto validateRequestDto) {
	//		// TODO Auto-generated method stub
	//		return null;
	//	}
	// moved code from testManager
	//-----------------------------------------------------------------------------------------

	@Override
	public TestRun validateResponse(ValidateResponseRequestDto validateRequestDto) {
		if(!testServiceUtil.getTestRuns().containsKey(validateRequestDto.runId) || !testServiceUtil.getAllTests().containsKey(validateRequestDto.testId))
			return null;
		testServiceUtil.SetupMasterData();
		testServiceUtil.loadTests();
		testServiceUtil.loadRuns();
		TestRun run = testServiceUtil.getTestRuns().get(validateRequestDto.runId);
		TestExtnDto test = testServiceUtil.getAllTests().get(validateRequestDto.testId);
		TestResult testResult = run.testReport.get(test.testId);
		testResult.executedOn = new Date();
		testResult.responseData = validateRequestDto.mdsResponse;

		try {

			Intent intent = getIntent(test.method);
			IMDSResponseProcessor responseProcessor = getResponseProcessor(run.targetProfile.mdsSpecVersion);
			MdsResponse[] mdsDecodedResponse = responseProcessor.getMdsDecodedResponse(intent, testResult.responseData);
			validateRequestDto.setIntent(intent);
			for(MdsResponse mdsResponse:mdsDecodedResponse)
			{
				validateRequestDto.setMdsDecodedResponse(mdsResponse);
				for(Validator v:test.validators)
				{
					ValidationResult vr = v.Validate(validateRequestDto);
					testResult.validationResults.add(vr);

				}
			}

			testResult.renderContent = responseProcessor.getRenderContent(intent, testResult.responseData);
			testResult.currentState = "MDS Response Validations : Completed";

		} catch (Exception ex) {
			ex.printStackTrace();
			testResult.currentState = "MDS Response Validations : Failed";
		}

		run.testReport.put(test.testId, testResult);
		persistRun(run);
		return run;
	}

	private TestRun persistRun(TestRun run)
	{
		boolean isAllInResponseValidationStage = run.testReport.values().stream()
				.allMatch( result -> result.currentState.startsWith("MDS Response Validations"));
		if(isAllInResponseValidationStage)
			run.runStatus = "Done";
		return store.saveTestRun(run.user, run);
	}

	private Intent getIntent(String method) {
		Intent intent = Intent.Discover;

		if(method.equals("deviceinfo"))
		{
			intent = Intent.DeviceInfo;
		}
		else if(method.equals("rcapture"))
		{
			intent = Intent.RegistrationCapture;
		}
		else if(method.equals("capture"))
		{
			intent = Intent.Capture;
		}
		else if(method.equals("stream"))
		{
			intent = Intent.Stream;
		}
		return intent;
	}

	private IMDSResponseProcessor getResponseProcessor(String version)
	{
		if(version.equals("0.9.5"))
			return iMDSResponseProcessor;

		return iMDSResponseProcessor;
	}

	@Override
	public ComposeRequestResponseDto composeRequest(ComposeRequestDto composeRequestDto) {
		// TODO create and use actual request composers		
		return buildRequest(composeRequestDto);
	}

	private ComposeRequestResponseDto buildRequest(ComposeRequestDto requestParams)
	{
		DeviceInfoResponse  response = requestParams.deviceInfo.getDeviceInfo();
		String specVersion = response.specVersion[0];
		IMDSRequestBuilder builder = getRequestBuilder(Arrays.asList(specVersion));
		TestRun run = testServiceUtil.getTestRuns().get(requestParams.runId);
		if(run == null || !run.tests.contains(requestParams.testId))
			return null;
		run.deviceInfo = response; // Overwrites the device info for every call
		persistRun(run);
		TestExtnDto test = testServiceUtil.getAllTests().get(requestParams.testId);
		return builder.buildRequest(run, test, requestParams.deviceInfo, getIntent(test.method));
	}

	private IMDSRequestBuilder getRequestBuilder(List<String> version)
	{
		// Order of comparison is from newer to older. Default is the latest
		//if(version == null || version.size() == 0)
		//	return new MDS_0_9_5_RequestBuilder();	
		if(version.contains("0.9.5"))
			return iMDSRequestBuilder;
		if(version.contains("0.9.2"))
			return new MDS_0_9_2_RequestBuilder();
		return iMDSRequestBuilder;
	}

	@Override
	public TestRun composeRequestForAllTests(ComposeRequestDto composeRequestDto) {
		DeviceInfoResponse  deviceInfo = composeRequestDto.deviceInfo.getDeviceInfo();
		String specVersion = deviceInfo.specVersion[0];
		IMDSRequestBuilder builder = getRequestBuilder(Arrays.asList(specVersion));

		TestRun run = testServiceUtil.getTestRuns().get(composeRequestDto.runId);
		if(deviceInfo != null && run != null && run.tests != null) {
			run.deviceInfo = deviceInfo;
			for(String testId : run.tests) {
				TestExtnDto test = testServiceUtil.getAllTests().get(testId);
				TestResult testResult = new TestResult(run.runId, test.testId, test.testDescription);
				try {
					ComposeRequestResponseDto requestDTO = builder.buildRequest(run, test, composeRequestDto.deviceInfo,
							getIntent(test.method));
					testResult.requestData = mapper.writeValueAsString(requestDTO.requestInfoDto);
					testResult.streamUrl = requestDTO.streamUrl;
					testResult.currentState = "Compose MDS request : Completed";
				} catch (Exception ex) {
					ex.printStackTrace();
					testResult.currentState = "Compose MDS request : Failed";
				}
				run.testReport.put(testId, testResult);
			}

			run.runStatus = "InProgress";
			persistRun(run);
		}
		return run;
	}

	@Override
	public DiscoverResponse[] decodeDiscoverInfo(String discoverInfo) {
		return discoverHelper.decode(discoverInfo);
	}

	@Override
	public DeviceInfoResponse[] decodeDeviceInfo(String deviceInfo) {
		return deviceInfoHelper.decode(deviceInfo);
	}
}

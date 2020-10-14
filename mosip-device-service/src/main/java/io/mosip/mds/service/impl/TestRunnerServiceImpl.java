package io.mosip.mds.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.mds.dto.*;
import io.mosip.mds.dto.postresponse.ComposeRequestResponseDto;
import io.mosip.mds.dto.postresponse.ValidationResult;
import io.mosip.mds.entitiy.*;
import io.mosip.mds.repository.RunIdStatusRepository;
import io.mosip.mds.repository.TestCaseResultRepository;
import io.mosip.mds.service.IMDSRequestBuilder;
import io.mosip.mds.service.IMDSResponseProcessor;
import io.mosip.mds.service.MDS_0_9_2_RequestBuilder;
import io.mosip.mds.service.TestRunnerService;
import io.mosip.mds.util.BioAuthRequestUtil;
import io.mosip.mds.util.Intent;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class TestRunnerServiceImpl implements TestRunnerService {

	private static final Logger logger = LoggerFactory.getLogger(TestRunnerServiceImpl.class);
	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	List<Validator> validators;

	@Autowired
	DeviceInfoHelper deviceInfoHelper;

	@Autowired
	DiscoverHelper discoverHelper;

	@Autowired
	IMDSResponseProcessor iMDSResponseProcessor;
	
	@Autowired
	IMDSRequestBuilder iMDSRequestBuilder;

	@Autowired
	RunIdStatusRepository runIdStatusRepository;

	@Autowired
	TestCaseResultRepository testCaseResultRepository;

	@Autowired
	BioAuthRequestUtil bioAuthRequestUtil;


	@Override
	public TestRun validateResponse(ValidateResponseRequestDto validateRequestDto) throws Exception {
		io.mosip.mds.entitiy.RunStatus runStatus = runIdStatusRepository.findByRunId(validateRequestDto.getRunId());
		if(runStatus == null)
			throw new Exception("Invalid Run Id !");

		List<TestcaseResult> testcaseResults = testCaseResultRepository.findAllByTestResultKeyRunId(validateRequestDto.getRunId());
		if(testcaseResults != null) {
			boolean matched = testcaseResults.stream().anyMatch(testcaseResult ->
					testcaseResult.getTestResultKey().getTestcaseName().equals(validateRequestDto.getTestId()));
			if(!matched)
				throw new Exception("Invalid Test case found !");

			TestRun testRun = getTestRunDetail(runStatus);
			for(TestcaseResult testcaseResult : testcaseResults) {
				TestDefinition testDefinition = Store.getAllTestDefinitions().get(validateRequestDto.getTestId());
				Intent intent = getIntent(testDefinition.getMethod());
				String renderContent = "";

				testcaseResult.setExecutedOn(LocalDateTime.now(ZoneId.of("UTC")));
				testcaseResult.setResponse(validateRequestDto.getMdsResponse());

				//validate MDS response
				if(testcaseResult.getTestResultKey().getTestcaseName().equals(validateRequestDto.getTestId())) {
					List<ValidationResult> validationResults = new ArrayList<>();
					try {
						TestManagerDto targetProfile = mapper.readValue(runStatus.getProfile(), TestManagerDto.class);
						MdsResponse[] mdsDecodedResponses = getResponseProcessor(targetProfile.mdsSpecVersion).getMdsDecodedResponse(intent,
								validateRequestDto.getMdsResponse());
						String deviceInfoString = getRequiredJson(testcaseResult.getDeviceInfo(),"deviceInfo");			
						validateRequestDto.setDeviceInfo((DeviceInfoResponse) (mapper.readValue(deviceInfoString, DeviceInfoResponse.class)));
						String requestString = getRequiredJson(testcaseResult.getRequest(),"body");
						validateRequestDto.setMdsDecodedRequest(requestString);
						validateRequestDto.setIntent(intent);
						validateRequestDto.setTestManagerDto(targetProfile);
						for(MdsResponse mdsResponse : mdsDecodedResponses) {
							validateRequestDto.setMdsDecodedResponse(mdsResponse);
							for(ValidatorDef validatorDef : testDefinition.validatorDefs) {
								Optional<Validator> validator = validators.stream().filter(v ->	v.Name.equals(validatorDef.Name)).findFirst();
								if(validator.isPresent())
									validationResults.add(validator.get().Validate(validateRequestDto));
							}
						}
						testcaseResult.setValidationResults(mapper.writeValueAsString(validationResults));
						
						renderContent = getResponseProcessor(targetProfile.mdsSpecVersion).getRenderContent(intent,
								validateRequestDto.getMdsResponse());

						testcaseResult.setPassed(true); //TODO - need to change column name
						testcaseResult.setCurrentState("MDS Response Validations : Completed");
					} catch (Exception ex) {
						logger.error("Exception validating MDS response", ex);
						testcaseResult.setCurrentState("MDS Response Validations : Failed");
					}
					testCaseResultRepository.save(testcaseResult);
				}
				TestResult testResult = getTestResult(validateRequestDto.getRunId(), testcaseResult, testDefinition);
				testResult.setRenderContent(renderContent);
				testRun.getTests().add(testcaseResult.getTestResultKey().getTestcaseName());
				testRun.getTestReport().put(testcaseResult.getTestResultKey().getTestcaseName(), testResult);
			}
			return testRun;
		}
		throw new Exception("No Test cases found for the provided run !");
	}

	private String getRequiredJson(String jsonString,String key) {
		String resultString = null;
		try {
			JSONObject json = new JSONObject(jsonString);
			resultString = (String) json.get(key).toString();
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return resultString;
	}

	@Override
	public TestRun composeRequestForAllTests(ComposeRequestDto composeRequestDto) throws Exception {
		isDeviceAvailable(composeRequestDto.deviceInfo);

		io.mosip.mds.entitiy.RunStatus runStatus = runIdStatusRepository.findByRunId(composeRequestDto.runId);
		if(runStatus == null)
			throw new Exception("Invalid Run Id !");

		TestRun testRun = getTestRunDetail(runStatus);
		List<TestcaseResult> testcaseResults = testCaseResultRepository.findAllByTestResultKeyRunId(composeRequestDto.runId);

		if(testcaseResults != null) {
			for(TestcaseResult testcaseResult : testcaseResults) {
				ComposeRequestResponseDto requestDTO = null;
				TestDefinition testDefinition = Store.getAllTestDefinitions().get(testcaseResult.getTestResultKey().getTestcaseName());
				if(!testcaseResult.isPassed() || composeRequestDto.forceReset) { //test case is already executed,or if force reset is enabled
					try {
						requestDTO = getRequestBuilder(composeRequestDto).buildRequest(composeRequestDto.runId,
								mapper.readValue(runStatus.getProfile(), TestManagerDto.class),
								testDefinition,
								composeRequestDto.deviceInfo,
								getIntent(testDefinition.getMethod()));
						testcaseResult.setDeviceInfo(mapper.writeValueAsString(composeRequestDto.deviceInfo));
						testcaseResult.setRequest(mapper.writeValueAsString(requestDTO.requestInfoDto));
						testcaseResult.setCurrentState("Compose MDS request : Completed");
					} catch (Exception ex) {
						logger.error("Error composing request", ex);
						testcaseResult.setCurrentState("Compose MDS request : Failed");
					}
					testCaseResultRepository.save(testcaseResult);
				}
				TestResult testResult = getTestResult(composeRequestDto.runId, testcaseResult, testDefinition);
				testResult.streamUrl = requestDTO != null ? requestDTO.getRequestInfoDto().streamUrl : null;
				testRun.getTests().add(testcaseResult.getTestResultKey().getTestcaseName());
				testRun.getTestReport().put(testcaseResult.getTestResultKey().getTestcaseName(), testResult);
			}
		}
		return testRun;
	}

	@Override
	public DiscoverResponse[] decodeDiscoverInfo(String discoverInfo) {
		return discoverHelper.decode(discoverInfo);
	}

	@Override
	public DeviceInfoResponse[] decodeDeviceInfo(String deviceInfo) {
		return deviceInfoHelper.decode(deviceInfo);
	}

	@Override
	public String validateAuthRequest(String runId, String testId) {
		io.mosip.mds.entitiy.RunStatus runStatus = runIdStatusRepository.findByRunId(runId);
		if(runStatus == null)
			return null;

		Optional<TestcaseResult> result = testCaseResultRepository.findByTestResultKey(new TestResultKey(runId, testId));
		if(result.isPresent()) {
			try {
				ValidateResponseRequestDto validateResponseRequestDto = new ValidateResponseRequestDto();
				validateResponseRequestDto.setTestId(testId);
				validateResponseRequestDto.setMdsResponse(result.get().getResponse());
				return bioAuthRequestUtil.authenticateResponse(validateResponseRequestDto);
			} catch (Exception e) {
				logger.error("Error validating auth request", e);
			}
		}
		return null;
	}

	private IMDSRequestBuilder getRequestBuilder(ComposeRequestDto composeRequestDto) {
		DeviceInfoResponse  deviceInfo = composeRequestDto.deviceInfo.getDeviceInfo();
		return getRequestBuilder(Arrays.asList(deviceInfo.specVersion));
	}

	private TestResult getTestResult(String runId, TestcaseResult testcaseResult, TestDefinition testDefinition) {
		TestResult testResult = new TestResult(runId, testcaseResult.getTestResultKey().getTestcaseName(),
				testcaseResult.getDescription());
		testResult.currentState = testcaseResult.getCurrentState();
		testResult.setResponseData(testcaseResult.getResponse());
		testResult.setExecutedOn(testcaseResult.getExecutedOn());
		testResult.setSummary(testcaseResult.getDescription());
		testResult.setRequestData(testcaseResult.getRequest());
		testResult.enableAuthTest = (testDefinition.getMethod().equals("capture")) ? true : false;
		try {
			if(testcaseResult.getValidationResults() != null) {
				List<ValidationResult> validationResults = mapper.readValue(testcaseResult.getValidationResults(),
						new TypeReference<List<ValidationResult>>(){});
				testResult.setValidationResults(validationResults);
			}
		} catch (Exception ex) {
			logger.error("Error parsing validation results for runId : {}", runId, ex);
		}
		return testResult;
	}

	private boolean isTestRunCompleted(String runId) {
		int total = testCaseResultRepository.countByTestResultKeyRunId(runId);
		int completed = testCaseResultRepository.countByTestResultKeyRunIdAndPassed(runId, true);
		return (total == completed);
	}

	private TestRun getTestRunDetail(io.mosip.mds.entitiy.RunStatus runStatus) {
		TestRun run = new TestRun();
		run.setRunId(runStatus.getRunId());
		run.setTests(new ArrayList<String>());
		return run;
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

	private IMDSRequestBuilder getRequestBuilder(List<String> version)
	{
		// Order of comparison is from newer to older. Default is the latest
		if(version.contains("0.9.5"))
			return iMDSRequestBuilder;
		if(version.contains("0.9.2"))
			return new MDS_0_9_2_RequestBuilder();
		return iMDSRequestBuilder;
	}

	private void isDeviceAvailable(DeviceDto deviceDto) throws  Exception {
		logger.info("Validating device status {}", deviceDto.port);
		if(deviceDto.deviceInfo == null)
			throw new Exception("Invalid device details !");

		if(deviceDto.deviceInfo.deviceStatus == null)
			throw new Exception("Invalid device status !");

		if(!deviceDto.deviceInfo.deviceStatus.equals("Ready"))
			throw new Exception("Invalid device status !");
	}
}
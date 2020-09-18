package io.mosip.mds.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.ComposeRequestDto;
import io.mosip.mds.dto.DeviceInfoResponse;
import io.mosip.mds.dto.DiscoverResponse;
import io.mosip.mds.dto.MdsResponse;
import io.mosip.mds.dto.TestDefinition;
import io.mosip.mds.dto.TestManagerDto;
import io.mosip.mds.dto.TestResult;
import io.mosip.mds.dto.TestRun;
import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.dto.ValidatorDef;
import io.mosip.mds.dto.postresponse.ComposeRequestResponseDto;
import io.mosip.mds.entitiy.DeviceInfoHelper;
import io.mosip.mds.entitiy.DiscoverHelper;
import io.mosip.mds.entitiy.Store;
import io.mosip.mds.entitiy.TestcaseResult;
import io.mosip.mds.entitiy.Validator;
import io.mosip.mds.repository.RunIdStatusRepository;
import io.mosip.mds.repository.TestCaseResultRepository;
import io.mosip.mds.service.IMDSRequestBuilder;
import io.mosip.mds.service.IMDSResponseProcessor;
import io.mosip.mds.service.MDS_0_9_2_RequestBuilder;
import io.mosip.mds.service.TestRunnerService;
import io.mosip.mds.util.Intent;

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


	@Override
	public TestRun validateResponse(ValidateResponseRequestDto validateRequestDto) {
		io.mosip.mds.entitiy.RunStatus runStatus = runIdStatusRepository.findByRunId(validateRequestDto.getRunId());
		if(runStatus == null)
			return null;

		List<TestcaseResult> testcaseResults = testCaseResultRepository.findAllByTestResultKeyRunId(validateRequestDto.getRunId());
		if(testcaseResults != null) {
			boolean matched = testcaseResults.stream().anyMatch(testcaseResult ->
			testcaseResult.getTestResultKey().getTestcaseName().equals(validateRequestDto.getTestId()));
			if(!matched)
				return null;

			TestRun testRun = getTestRunDetail(runStatus);
			for(TestcaseResult testcaseResult : testcaseResults) {
				TestResult testResult = new TestResult(runStatus.getRunId(), testcaseResult.getTestResultKey().getTestcaseName(),
						testcaseResult.getDescription());

				//validate MDS response
				if(testcaseResult.getTestResultKey().getTestcaseName().equals(validateRequestDto.getTestId())) {
					testcaseResult.setExecutedOn(System.currentTimeMillis());
					testcaseResult.setResponse(validateRequestDto.getMdsResponse());
					try {
						TestDefinition testDefinition = Store.getAllTestDefinitions().get(validateRequestDto.getTestId());
						TestManagerDto targetProfile = mapper.readValue(runStatus.getProfile(), TestManagerDto.class);
						Intent intent = getIntent(testDefinition.getMethod());
						IMDSResponseProcessor responseProcessor = getResponseProcessor(targetProfile.mdsSpecVersion);
						MdsResponse[] mdsDecodedResponse = responseProcessor.getMdsDecodedResponse(intent, validateRequestDto.getMdsResponse());

						String deviceInfoString = getRequiredJson(testcaseResult.getDeviceInfo(),"deviceInfo");			
						validateRequestDto.setDeviceInfo((DeviceInfoResponse) (mapper.readValue(deviceInfoString, DeviceInfoResponse.class)));
						String requestString = getRequiredJson(testcaseResult.getRequest(),"body");
						validateRequestDto.setMdsDecodedRequest(requestString);
						validateRequestDto.setIntent(intent);
						validateRequestDto.setTestManagerDto(targetProfile);
						
						for(MdsResponse mdsResponse:mdsDecodedResponse)
						{
							validateRequestDto.setMdsDecodedResponse(mdsResponse);						
							for(ValidatorDef validatorDef : testDefinition.validatorDefs) {
								Optional<Validator> validator = validators.stream().filter(v ->	v.Name.equals(validatorDef.Name)).findFirst();
								if(validator.isPresent())
									testResult.validationResults.add(validator.get().Validate(validateRequestDto));
							}
						}
						testResult.renderContent = responseProcessor.getRenderContent(intent, validateRequestDto.getMdsResponse());

						//persist case validation results
						testcaseResult.setValidationResults(mapper.writeValueAsString(testResult.validationResults));
						testcaseResult.setPassed(true); //TODO
						testcaseResult.setCurrentState("MDS Response Validations : Completed");
						testCaseResultRepository.save(testcaseResult);

					} catch (Exception ex) {
						logger.error("Exception validating MDS response", ex);
						testResult.currentState = "MDS Response Validations : Failed";
					}
				}

				testResult.currentState = testcaseResult.getCurrentState();
				testResult.setResponseData(testcaseResult.getResponse());
				testResult.setExecutedOn(new Date(testcaseResult.getExecutedOn()));
				testResult.setSummary(testcaseResult.getDescription());
				testResult.setRequestData(testcaseResult.getRequest());

				testRun.getTests().add(testcaseResult.getTestResultKey().getTestcaseName());
				testRun.getTestReport().put(testcaseResult.getTestResultKey().getTestcaseName(), testResult);
			}
			return testRun;
		}
		return null;
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
	public TestRun composeRequestForAllTests(ComposeRequestDto composeRequestDto) {
		io.mosip.mds.entitiy.RunStatus runStatus = runIdStatusRepository.findByRunId(composeRequestDto.runId);
		if(runStatus == null)
			return null;

		DeviceInfoResponse  deviceInfo = composeRequestDto.deviceInfo.getDeviceInfo();
		String specVersion = deviceInfo.specVersion[0];
		IMDSRequestBuilder builder = getRequestBuilder(Arrays.asList(specVersion));
		TestRun testRun = getTestRunDetail(runStatus);

		List<TestcaseResult> testcaseResults = testCaseResultRepository.findAllByTestResultKeyRunId(composeRequestDto.runId);
		if(testcaseResults != null) {
			for(TestcaseResult testcaseResult : testcaseResults) {
				TestResult testResult = new TestResult(runStatus.getRunId(), testcaseResult.getTestResultKey().getTestcaseName(),
						testcaseResult.getDescription());
				TestDefinition testDefinition = Store.getAllTestDefinitions().get(testcaseResult.getTestResultKey().getTestcaseName());

				try {
					ComposeRequestResponseDto requestDTO = builder.buildRequest(composeRequestDto.runId,
							mapper.readValue(runStatus.getProfile(), TestManagerDto.class),
							testDefinition,
							composeRequestDto.deviceInfo,
							getIntent(testDefinition.getMethod()));

					testResult.requestData = mapper.writeValueAsString(requestDTO.requestInfoDto);
					testResult.streamUrl = requestDTO.streamUrl;
					testResult.currentState = "Compose MDS request : Completed";

					testcaseResult.setDeviceInfo(mapper.writeValueAsString(composeRequestDto.deviceInfo));
					testcaseResult.setRequest(testResult.requestData);
					testcaseResult.setCurrentState("Compose MDS request : Completed");
					testCaseResultRepository.save(testcaseResult);

					testRun.getTests().add(testcaseResult.getTestResultKey().getTestcaseName());
					testRun.getTestReport().put(testcaseResult.getTestResultKey().getTestcaseName(), testResult);

				} catch (Exception ex) {
					logger.error("Error composing request", ex);
					testResult.currentState = "Compose MDS request : Failed";
				}
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
}

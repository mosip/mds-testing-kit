package io.mosip.mds.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.mosip.mds.dto.*;
import io.mosip.mds.entitiy.*;
import io.mosip.mds.repository.RunIdStatusRepository;
import io.mosip.mds.repository.TestCaseResultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.TestRun.RunStatus;
import io.mosip.mds.dto.getresponse.TestExtnDto;
import io.mosip.mds.dto.postresponse.ComposeRequestResponseDto;
import io.mosip.mds.dto.postresponse.RequestInfoDto;
import io.mosip.mds.dto.postresponse.ValidationResult;
import io.mosip.mds.service.IMDSRequestBuilder;
import io.mosip.mds.service.IMDSResponseProcessor;
import io.mosip.mds.service.MDS_0_9_2_RequestBuilder;
import io.mosip.mds.service.MDS_0_9_5_RequestBuilder;
import io.mosip.mds.service.MDS_0_9_5_ResponseProcessor;
import io.mosip.mds.service.TestRunnerService;
import io.mosip.mds.util.Intent;
import io.mosip.mds.util.TestServiceUtil;

import javax.swing.text.html.Option;

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
					testResult.setRequestData(testcaseResult.getRequest());
					testcaseResult.setResponse(validateRequestDto.getMdsResponse());
					try {
						TestDefinition testDefinition = Store.getAllTestDefinitions().get(validateRequestDto.getTestId());
						TestManagerDto targetProfile = mapper.readValue(runStatus.getProfile(), TestManagerDto.class);
						Intent intent = getIntent(testDefinition.getMethod());
						IMDSResponseProcessor responseProcessor = getResponseProcessor(targetProfile.mdsSpecVersion);
						MdsResponse[] mdsDecodedResponse = responseProcessor.getMdsDecodedResponse(intent, validateRequestDto.getMdsResponse());
						validateRequestDto.setIntent(intent);

						List<Validator> optedValidators = validators.stream().filter(validator ->
								validator.Name.equals("")).collect(Collectors.toList());

						for(ValidatorDef validatorDef : testDefinition.validatorDefs) {
							Optional<Validator> validator = validators.stream().filter(v ->	v.Name.equals(validatorDef.Name)).findFirst();
							if(validator.isPresent())
								testResult.validationResults.add(validator.get().Validate(validateRequestDto));
						}

						testResult.setExecutedOn(new Date(testcaseResult.getExecutedOn()));
						testResult.renderContent = responseProcessor.getRenderContent(intent, testResult.responseData);
						testResult.currentState = "MDS Response Validations : Completed";

						//persist case validation results
						testcaseResult.setValidationResults(mapper.writeValueAsString(testResult.validationResults));
						testcaseResult.setPassed(true); //TODO
						testCaseResultRepository.save(testcaseResult);

					} catch (Exception ex) {
						logger.error("Exception validating MDS response", ex);
						testResult.currentState = "MDS Response Validations : Failed";
					}
				}

				testRun.getTests().add(testcaseResult.getTestResultKey().getTestcaseName());
				testRun.getTestReport().put(testcaseResult.getTestResultKey().getTestcaseName(), testResult);
			}
			return testRun;
		}
		return null;
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

package io.mosip.mds.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.TestDefinition;
import io.mosip.mds.dto.TestManagerDto;
import io.mosip.mds.dto.TestManagerGetDto;
import io.mosip.mds.dto.TestReport;
import io.mosip.mds.dto.TestResult;
import io.mosip.mds.dto.TestRun;
import io.mosip.mds.dto.TestRunMetadata;
import io.mosip.mds.dto.getresponse.MasterDataResponseDto;
import io.mosip.mds.entitiy.RunStatus;
import io.mosip.mds.entitiy.Store;
import io.mosip.mds.entitiy.TestResultKey;
import io.mosip.mds.entitiy.TestcaseResult;
import io.mosip.mds.repository.RunIdStatusRepository;
import io.mosip.mds.repository.TestCaseResultRepository;
import io.mosip.mds.service.TestManagerService;

@Service
public class TestManagerServiceImpl implements TestManagerService {

	private static  final Logger logger = LoggerFactory.getLogger(TestManagerServiceImpl.class);
	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private RunIdStatusRepository runIdStatusRepository;

	@Autowired
	public TestCaseResultRepository testCaseResultRepository;

	@Override
	public MasterDataResponseDto getMasterData()
	{
		MasterDataResponseDto masterData = Store.getMasterData();
		return masterData;
	}

	@Override
	public List<TestDefinition> getTests(TestManagerGetDto filter)
	{
		Map<String, TestDefinition> testDefinitions = Store.getAllTestDefinitions();
		List<TestDefinition> results =  testDefinitions.values().stream().filter(test ->
				(isValid(test.processes) && test.processes.contains(filter.process)) &&
						(isValid(test.biometricTypes) && test.biometricTypes.contains(filter.biometricType)) &&
						(isValid(test.deviceSubTypes) && test.deviceSubTypes.contains(filter.deviceSubType)) &&
						( !isValid(test.mdsSpecVersions) || test.mdsSpecVersions.contains(filter.mdsSpecificationVersion ) ))
				.collect(Collectors.toList());

		return results;
	}

	@Override
	public List<TestRunMetadata> getRuns(@NotNull String email)
	{
		List<TestRunMetadata> list = new ArrayList<>();
		List<io.mosip.mds.entitiy.RunStatus> runs = runIdStatusRepository.findAllByRunOwner(email);
		for (io.mosip.mds.entitiy.RunStatus runStatus : runs) {
			TestRunMetadata testRunMetadata = new TestRunMetadata();
			testRunMetadata.setRunId(runStatus.getRunId());
			testRunMetadata.setRunName(runStatus.getRunName());
			int total = testCaseResultRepository.countByTestResultKeyRunId(runStatus.getRunId());
			int completed = testCaseResultRepository.countByTestResultKeyRunIdAndPassed(runStatus.getRunId(), true);
			String status = (completed == total) ? "Completed (%d/%d)" : "InProgress (%d/%d)";
			testRunMetadata.setRunStatus(String.format(status, completed, total));
			testRunMetadata.setCreatedOn(new Date(Long.valueOf(runStatus.getRunId())));
			list.add(testRunMetadata);
		}
		list.sort((run1, run2) -> run1.getCreatedOn().compareTo(run2.getCreatedOn()));
		return list;
	}


	@Override
	public TestRunMetadata createRun(TestManagerDto testManagerDto) {
		//validate data
		List<TestDefinition> definitions = getTests(new TestManagerGetDto(testManagerDto.mdsSpecVersion, testManagerDto.process,
				testManagerDto.biometricType, testManagerDto.deviceSubType));

		definitions = definitions.stream()
				.filter(testDefinition -> testManagerDto.getTests().contains(testDefinition.getTestId()))
				.collect(Collectors.toList());

		if(definitions == null || definitions.isEmpty()) {
			return null;
		}
		//create entry in runStatus
		io.mosip.mds.entitiy.RunStatus runStatus = saveRunStatus(testManagerDto, definitions);

		logger.info("Runstatus saved >>> {}", runStatus);

		//then create initial entries in testcaseresult table
		saveRunCases(testManagerDto, definitions, runStatus);
		return getTestRunMetadata(runStatus);
	}


	//TODO - validation results must be stored in Db during validations itself
	@Override
	public void saveTestResult(TestRun run) {
		for (Map.Entry<String, TestResult> entry : run.getTestReport().entrySet()) {
			TestResultKey testResultKey = new TestResultKey();
			testResultKey.runId = run.runId;
			testResultKey.testcaseName = entry.getKey();
			Optional<TestcaseResult> result = testCaseResultRepository.findById(testResultKey);
			if (result.isPresent()) {
				TestcaseResult testcaseResult = result.get();
				testcaseResult.setRequest(entry.getValue().getRequestData());
				testcaseResult.setResponse(entry.getValue().getResponseData());
				testcaseResult.setValidationResults(entry.getValue().getValidationResults().toString());
				testcaseResult.setDeviceInfo(run.getDeviceInfo().toString());
				testcaseResult.setPassed(true); //TODO - need to check with kiran
				testCaseResultRepository.save(testcaseResult);
			}
		}

		//TODO - Need to update runStatus entity as well
	}

	@Override
	public TestReport getReport(String runId) {
		//TODO - see below for the old implementation
		//TODO - move this to reporting controller
		return null;
	}

	@Override
	public HttpEntity<byte[]> getPdfReport(String runId, String fileName) throws Exception {
		//TODO - see below for the old implementation
		//TODO - move this to reporting controller
		return null;
	}

	private RunStatus saveRunStatus(TestManagerDto testManagerDto, List<TestDefinition> definitions) {
		RunStatus runStatus = new RunStatus();
		runStatus.setRunId("" + System.currentTimeMillis());
		runStatus.setRunName(testManagerDto.getRunName());
		runStatus.setRunOwner(testManagerDto.getEmail());
		runStatus.setStatus("0/"+definitions.size());
		runStatus.setCreatedBy(testManagerDto.getEmail());
		runStatus.setCreatedOn(LocalDateTime.now(ZoneId.of("UTC")));
		try {
			runStatus.setProfile(mapper.writeValueAsString(testManagerDto));
		} catch (JsonProcessingException ex) {
			logger.error("Error converting profile to json", ex);
		}
		return runIdStatusRepository.save(runStatus);
	}

	private void saveRunCases(TestManagerDto testManagerDto, List<TestDefinition> definitions,
							  io.mosip.mds.entitiy.RunStatus runStatus) {
		for(TestDefinition testDefinition : definitions) {
			TestResultKey testResultKey = new TestResultKey();
			testResultKey.setRunId(runStatus.getRunId());
			testResultKey.setTestcaseName(testDefinition.getTestId());

			TestcaseResult testCaseResult = new TestcaseResult();
			testCaseResult.setTestResultKey(testResultKey);
			testCaseResult.setDescription(testDefinition.getTestDescription());
			testCaseResult.setOwner(testManagerDto.getEmail());
			testCaseResult.setPassed(false);
			testCaseResult.setCreatedBy(testManagerDto.getEmail());
			testCaseResult.setCreatedOn(LocalDateTime.now(ZoneId.of("UTC")));
			testCaseResultRepository.save(testCaseResult);
		}
	}

	private TestRunMetadata getTestRunMetadata(RunStatus runStatus) {
		TestRunMetadata testRunMetadata = new TestRunMetadata();
		testRunMetadata.setRunId(runStatus.getRunId());
		testRunMetadata.setRunName(runStatus.getRunName());
		testRunMetadata.setRunStatus(runStatus.getStatus()); //TODO
		testRunMetadata.setCreatedOn(new Date(Long.valueOf(runStatus.getRunId()))); //TODO
		return testRunMetadata;
	}

	private boolean isValid(List<String> value) {
		return (value != null && !value.isEmpty());
	}

}

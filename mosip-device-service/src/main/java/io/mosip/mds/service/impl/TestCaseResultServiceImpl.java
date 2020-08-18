package io.mosip.mds.service.impl;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.NewRunDto;
import io.mosip.mds.dto.TestManagerDto;
import io.mosip.mds.dto.TestResult;
import io.mosip.mds.dto.TestRun;
import io.mosip.mds.dto.getresponse.TestExtnDto;
import io.mosip.mds.dto.postresponse.RunExtnDto;
import io.mosip.mds.entitiy.RunIdStatus;
import io.mosip.mds.entitiy.Store;
import io.mosip.mds.entitiy.TestResultKey;
import io.mosip.mds.entitiy.TestcaseResult;
import io.mosip.mds.repository.RunIdStatusRepository;
import io.mosip.mds.repository.TestCaseResultRepository;
import io.mosip.mds.service.TestCaseResultService;

@Service
public class TestCaseResultServiceImpl implements TestCaseResultService {

	@Autowired
	public TestCaseResultRepository testCaseResultRepository;

	@Autowired
	public RunIdStatusRepository runIdStatusRepository;

	@Transactional
	public List<TestcaseResult> saveTestResult(TestRun run) {
		List<Optional<TestcaseResult>> listOfTests = testCaseResultRepository.findByTestResultKeyRunId(run.runId);
		if (listOfTests.isEmpty()) {
			return null;
		} else {
			HashMap<String, TestResult> testReport = new HashMap<>();
			RunIdStatus runIdStatus = new RunIdStatus();
			testReport = run.testReport;
			List<Boolean> status = new ArrayList<Boolean>();
			for (Map.Entry<String, TestResult> result : testReport.entrySet()) {
				TestResultKey testResultKey = new TestResultKey();
				testResultKey.runId = run.runId;
				testResultKey.testcaseName = result.getKey();
				Optional<TestcaseResult> testCaseResult = testCaseResultRepository.findById(testResultKey);
				testCaseResult.get().testcaseRequest = result.getValue().requestData;
				testCaseResult.get().testcaseResponse = result.getValue().responseData;
				testCaseResult.get().validationResults = result.getValue().validationResults.toString();
				testCaseResult.get().deviceInfo = run.deviceInfo.toString();
				boolean runStatus = false;
				if (result.getValue().validationResults.isEmpty()) {
					runStatus = true;
				}
				status.add(runStatus);
				testCaseResult.get().testcasePassed = runStatus;
				testCaseResultRepository.save(testCaseResult.get());
			}
			runIdStatus.setRunId(run.runId);
			if (status.contains(false))
				runIdStatus.setRunPassed(false);
			else
				runIdStatus.setRunPassed(true);
			runIdStatusRepository.save(runIdStatus);
			return null;
		}

	}

	@Override
	public boolean getRunStatus(String runId) {
		List<RunIdStatus> statusList = runIdStatusRepository.findAll();
		return statusList.get(0).runPassed;

	}

	@Override
	@Transactional
	public RunExtnDto saveTestRun(TestManagerDto runInfo) {
		RunExtnDto runExtnDto = new RunExtnDto();
		RunIdStatus runIdStatus = new RunIdStatus();
		TestExtnDto[] tests = Store.getTestDefinitions();
		HashMap<String, TestExtnDto> allTests = new HashMap<>();
		NewRunDto newRunDto = new NewRunDto();
		if (tests != null) {
			for (TestExtnDto test : tests) {
				allTests.put(test.testId, test);
			}
		}
		Boolean notFound = false;
		for (String testName : runInfo.tests) {

			if (!allTests.containsKey(testName)) {
				notFound = true;
				break;
			}
		}
		if (notFound)
			System.out.println("TestCaseNotFound");
		newRunDto.runId = "" + System.currentTimeMillis();
		newRunDto.runName = runInfo.runName;
		newRunDto.tests = runInfo.tests.toArray(new String[runInfo.tests.size()]);
		if (!runInfo.email.isEmpty())
			newRunDto.email = runInfo.email;
		else
			newRunDto.email = "misc";
		newRunDto.targetProfile = runInfo;
		runIdStatus.setRunId(newRunDto.runId);
		runIdStatus.setRunPassed(false);
		ObjectMapper mapper = new ObjectMapper();
		String targetProfileJson = "";
		try {
			targetProfileJson = mapper.writeValueAsString(newRunDto.targetProfile);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		runIdStatus.setTargetProfile(targetProfileJson);
		runIdStatus.runName = runInfo.runName;
		runIdStatusRepository.save(runIdStatus);
		for (String test : newRunDto.tests) {
			TestcaseResult testCaseResult = new TestcaseResult();
			TestResultKey testResultKey = new TestResultKey();
			testCaseResult.testcaseOwner = newRunDto.email;
			testResultKey.runId = newRunDto.runId;
			testResultKey.testcaseName = test;
			testCaseResult.testcasePassed = false;
			testCaseResult.testResultKey = testResultKey;
			testCaseResultRepository.save(testCaseResult);
			runExtnDto.email = newRunDto.email;
			runExtnDto.runId = newRunDto.runId;
			runExtnDto.runName = newRunDto.runName;
			runExtnDto.runStatus = "Done";
			runExtnDto.tests = newRunDto.tests;
		}
		return runExtnDto;

	}

	@Override
	@Transactional
	public List<TestRun> getRuns(String email) {
		List<TestRun> runs = new ArrayList<>();
		ObjectMapper mapper = new ObjectMapper();
		List<Optional<TestcaseResult>> listOfTests = testCaseResultRepository.findBytestcaseOwner(email);
		String runId = "";
		for (Optional<TestcaseResult> tests : listOfTests) {
			if (tests.get().testResultKey.runId.equals(runId)) {
				continue;
			} else {
				runId = tests.get().testResultKey.runId;
				TestRun testRun = new TestRun();
				testRun.tests = new ArrayList<>();
				testRun.createdOn = new Date(Long.valueOf(tests.get().testResultKey.runId));
				testRun.runId = tests.get().testResultKey.runId;
				testRun.runStatus = testRun.runStatus.Done;
				Optional<RunIdStatus> runIdStatus = runIdStatusRepository.findById(testRun.runId);
				testRun.runName = runIdStatus.get().runName;
				try {
					testRun.targetProfile = mapper.readValue(runIdStatus.get().targetProfile.toString(),
							TestManagerDto.class);
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				testRun.user = email;
				for (Optional<TestcaseResult> testCase : testCaseResultRepository.findByTestResultKeyRunId(runId)) {
					testRun.tests.add(testCase.get().testResultKey.testcaseName);
				}

				runs.add(testRun);
			}
		}

		return runs;
	}

}

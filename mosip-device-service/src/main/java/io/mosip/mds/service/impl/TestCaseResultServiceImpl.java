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

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.mds.constants.MdsLoggerConstant;
import io.mosip.mds.dto.NewRunDto;
import io.mosip.mds.dto.TestManagerDto;
import io.mosip.mds.dto.TestManagerGetDto;
import io.mosip.mds.dto.TestResult;
import io.mosip.mds.dto.TestRun;
import io.mosip.mds.dto.getresponse.TestExtnDto;
import io.mosip.mds.dto.postresponse.RunExtnDto;
import io.mosip.mds.entitiy.Store;
import io.mosip.mds.entitiy.TestManager;
import io.mosip.mds.entitiy.TestResultKey;
import io.mosip.mds.entitiy.TestcaseResult;
import io.mosip.mds.helper.MdsLogger;
import io.mosip.mds.repository.RunIdStatusRepository;
import io.mosip.mds.repository.TestCaseResultRepository;
import io.mosip.mds.service.TestCaseResultService;

@Service
public class TestCaseResultServiceImpl implements TestCaseResultService {
	private static Logger mdsLogger = MdsLogger.getLogger(TestCaseResultService.class);
	@Autowired
	public TestCaseResultRepository testCaseResultRepository;

	@Autowired
	public RunIdStatusRepository runIdStatusRepository;

	@Transactional
	public List<TestcaseResult> saveTestResult(TestRun run) {
		HashMap<String, TestResult> testReport = new HashMap<>();
		io.mosip.mds.entitiy.RunStatus runIdStatus = new io.mosip.mds.entitiy.RunStatus();
		testReport = run.testReport;
		List<Boolean> status = new ArrayList<Boolean>();
		for (Map.Entry<String, TestResult> result : testReport.entrySet()) {
			TestResultKey testResultKey = new TestResultKey();
			testResultKey.runId = run.runId;
			testResultKey.testcaseName = result.getKey();
			Optional<TestcaseResult> testCaseResult = testCaseResultRepository.findById(testResultKey);
			if (testCaseResult.isPresent()) {
				testCaseResult.get().request = result.getValue().requestData;
				testCaseResult.get().response = result.getValue().responseData;
				testCaseResult.get().validationResults = result.getValue().validationResults.toString();
				testCaseResult.get().deviceInfo = run.deviceInfo.toString();
				boolean runStatus = false;
				if (result.getValue().validationResults.isEmpty()) {
					runStatus = true;
				}
				status.add(runStatus);
				testCaseResult.get().passed = runStatus;
				testCaseResultRepository.save(testCaseResult.get());
			}
		}
		runIdStatus.setRunId(run.runId);
		if (status.contains(false))
			runIdStatus.setRunPassed(false);
		else
			runIdStatus.setRunPassed(true);
		runIdStatusRepository.save(runIdStatus);
		return null;

	}

	@Override
	public boolean getRunStatus(String runId) {
		List<io.mosip.mds.entitiy.RunStatus> statusList = runIdStatusRepository.findAll();
		return statusList.get(0).runPassed;

	}

	@Override
	@Transactional
	public RunExtnDto saveTestRun(TestManagerDto runInfo) {
		List<String> testToRun = new ArrayList<>();
		RunExtnDto runExtnDto = new RunExtnDto();
		TestManagerGetDto testManagerGetDto = new TestManagerGetDto();
		io.mosip.mds.entitiy.RunStatus runIdStatus = new io.mosip.mds.entitiy.RunStatus();
		NewRunDto newRunDto = new NewRunDto();
		testManagerGetDto.biometricType = runInfo.biometricType;
		testManagerGetDto.deviceSubType = runInfo.deviceSubType;
		testManagerGetDto.mdsSpecificationVersion = runInfo.mdsSpecVersion;
		testManagerGetDto.process = runInfo.process;
		List<TestExtnDto> testCaseList = TestManager.filterTests(testManagerGetDto);
		for (TestExtnDto testcase : testCaseList) {
			if (runInfo.tests.contains(testcase.testId)) {
				testToRun.add(testcase.testId);
			} else
				
			mdsLogger.info(MdsLoggerConstant.SESSIONID.toString(),testcase.testId,
					MdsLoggerConstant.RUNNAME.toString(), "The given test is invalid for the testType");
		}

		if (testToRun.isEmpty()) {
			mdsLogger.info(MdsLoggerConstant.SESSIONID.toString(),MdsLoggerConstant.TESTID.toString(),
					MdsLoggerConstant.RUNNAME.toString(), "No Valid Tests To Run");
		}
		newRunDto.runId = "" + System.currentTimeMillis();
		newRunDto.runName = runInfo.runName;
		newRunDto.tests = testToRun.toArray(new String[testToRun.size()]);
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
			testCaseResult.owner = newRunDto.email;
			testResultKey.runId = newRunDto.runId;
			testResultKey.testcaseName = test;
			testCaseResult.passed = false;
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
		List<Optional<TestcaseResult>> listOfTests = testCaseResultRepository.findByOwner(email);
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
				Optional<io.mosip.mds.entitiy.RunStatus> runIdStatus = runIdStatusRepository.findById(testRun.runId);
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

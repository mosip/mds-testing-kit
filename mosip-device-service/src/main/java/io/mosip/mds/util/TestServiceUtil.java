package io.mosip.mds.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import io.mosip.mds.dto.TestManagerGetDto;
import io.mosip.mds.dto.TestRun;
import io.mosip.mds.dto.getresponse.BiometricTypeDto;
import io.mosip.mds.dto.getresponse.MasterDataResponseDto;
import io.mosip.mds.dto.getresponse.TestExtnDto;
import io.mosip.mds.entitiy.Store;

@Service
public class TestServiceUtil {
	
	@Autowired
	private Store store;
	
	private Boolean isMasterDataLoaded = false;
	private Boolean areTestsLoaded = false;
	private Boolean areRunsLoaded = false;
	private List<String> processList = new ArrayList<String>();
	private List<BiometricTypeDto> biometricTypes = new ArrayList<BiometricTypeDto>();
	private List<String> mdsSpecVersions = new ArrayList<String>();
	private HashMap<String, TestExtnDto> allTests = new HashMap<>();
	private HashMap<String, TestRun> testRuns = new HashMap<>();

	public void loadRuns()
	{
		if(areRunsLoaded)
			return;
		List<String> users = store.GetUsers();
		for(String user:users)
		{
			List<String> runIds = store.GetRunIds(user);
			for(String runId:runIds)
			{
				TestRun run = store.GetRun(user, runId);
				testRuns.put(runId, run);
			}
		}
		areRunsLoaded = true;
	}


	public void loadTests()
	{
		if(!areTestsLoaded)
		{
			TestExtnDto[] tests = store.getTestDefinitions();
			if(tests != null)
			{
				for(TestExtnDto test:tests)
				{
					allTests.put(test.testId, test);
				}
			}
		}
		areTestsLoaded = true;
	}

	public void SetupMasterData()
	{
		if(!isMasterDataLoaded)
		{
			MasterDataResponseDto masterData = store.getMasterData();
			processList = masterData.process;
			mdsSpecVersions = masterData.mdsSpecificationVersion;
			biometricTypes = masterData.biometricType;
			isMasterDataLoaded = true;
		}
	}


	public  List<TestExtnDto> filterTests(TestManagerGetDto filter)
	{
		List<TestExtnDto> results =  allTests.values().stream().filter(test -> 
		(isValid(test.processes) && test.processes.contains(filter.process)) && 
		(isValid(test.biometricTypes) && test.biometricTypes.contains(filter.biometricType)) &&
		(isValid(test.deviceSubTypes) && test.deviceSubTypes.contains(filter.deviceSubType)) && 
		( !isValid(test.mdsSpecVersions) || test.mdsSpecVersions.contains(filter.mdsSpecificationVersion ) ))
				.collect(Collectors.toList());

		return results;	
	}

	private  boolean isValid(List<String> value) {
		return (value != null && !value.isEmpty());
	}


	public  Boolean getIsMasterDataLoaded() {
		return isMasterDataLoaded;
	}


	public  void setIsMasterDataLoaded(Boolean isMasterDataLoaded) {
		this.isMasterDataLoaded = isMasterDataLoaded;
	}


	public  Boolean getAreTestsLoaded() {
		return areTestsLoaded;
	}


	public  void setAreTestsLoaded(Boolean areTestsLoaded) {
		this.areTestsLoaded = areTestsLoaded;
	}


	public  Boolean getAreRunsLoaded() {
		return areRunsLoaded;
	}


	public  void setAreRunsLoaded(Boolean areRunsLoaded) {
		this.areRunsLoaded = areRunsLoaded;
	}


	public  List<String> getProcessList() {
		return processList;
	}


	public  void setProcessList(List<String> processList) {
		this.processList = processList;
	}


	public  List<BiometricTypeDto> getBiometricTypes() {
		return biometricTypes;
	}


	public  void setBiometricTypes(List<BiometricTypeDto> biometricTypes) {
		this.biometricTypes = biometricTypes;
	}


	public  List<String> getMdsSpecVersions() {
		return mdsSpecVersions;
	}


	public  void setMdsSpecVersions(List<String> mdsSpecVersions) {
		this.mdsSpecVersions = mdsSpecVersions;
	}


	public  HashMap<String, TestExtnDto> getAllTests() {
		return allTests;
	}


	public  void setAllTests(HashMap<String, TestExtnDto> allTests) {
		this.allTests = allTests;
	}


	public  HashMap<String, TestRun> getTestRuns() {
		return testRuns;
	}

	public  void setTestRuns(HashMap<String, TestRun> testRuns) {
		this.testRuns = testRuns;
	}

}

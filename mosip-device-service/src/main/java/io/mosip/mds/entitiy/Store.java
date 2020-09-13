package io.mosip.mds.entitiy;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.stream.FileImageInputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.TestManagerGetDto;
import io.mosip.mds.dto.TestRun;
import io.mosip.mds.dto.ValidatorDef;
import io.mosip.mds.dto.getresponse.MasterDataResponseDto;
import io.mosip.mds.dto.getresponse.TestExtnDto;
import io.mosip.mds.repository.TestCaseResultRepository;
import io.mosip.mds.validator.AuthRequestResponseValidator;
import io.mosip.mds.validator.MandatoryCaptureResponseValidator;
import io.mosip.mds.validator.MandatoryDeviceInfoResponseValidator;
import io.mosip.mds.validator.MandatoryDiscoverResponseValidator;
import io.mosip.mds.validator.MandatoryRCaptureResponseValidator;
import io.mosip.mds.validator.ValidValueCaptureResponseValidator;
import io.mosip.mds.validator.ValidValueDeviceInfoResponseValidator;
import io.mosip.mds.validator.ValidValueDiscoverResponseValidator;
import io.mosip.mds.validator.ValidValueRCaptureResponseValidator;

@Component
public class Store {
	
	public String STORAGE_PATH = null;

	@Autowired
	AuthRequestResponseValidator authRequestResponseValidator;

	@Autowired
	ValidValueDeviceInfoResponseValidator validValueDeviceInfoResponseValidator;

	@Autowired
	MandatoryDeviceInfoResponseValidator mandatoryDeviceInfoResponseValidator;

	@Autowired
	MandatoryCaptureResponseValidator mandatoryCaptureResponseValidator;

	@Autowired
	MandatoryDiscoverResponseValidator mandatoryDiscoverResponseValidator;

	@Autowired
	ValidValueCaptureResponseValidator validValueCaptureResponseValidator;

	@Autowired
	MandatoryRCaptureResponseValidator mandatoryRCaptureResponseValidator;

	@Autowired
	ValidValueDiscoverResponseValidator validValueDiscoverResponseValidator;

	@Autowired
	ValidValueRCaptureResponseValidator validValueRCaptureResponseValidator;

	@Autowired
	ObjectMapper mapper;

	public List<String> GetRunIds(String email)
	{
		List<String> files = new ArrayList<>();
		File f = new File(getStorePath() + "runs/" + email);
		try
		{
			for(File sub:f.listFiles())
			{
				if(sub.isFile())
					files.add(sub.getName());
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return files;
	}

	public List<String> GetUsers()
	{
		List<String> files = new ArrayList<>();
		File f = new File(getStorePath() + "runs");
		try
		{
			for(File sub:f.listFiles())
			{
				if(sub.isDirectory())
					files.add(sub.getName());
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return files;
	}


	public TestRun GetRun(String email, String runId)
	{
		TestRun result = null;
		File runFile = new File(getStorePath() + "runs/" + email + File.separator + runId );
		if(!runFile.exists())
			return null;
		ObjectMapper mapper = new ObjectMapper();
		try
		{
			result = mapper.readValue(new FileInputStream(runFile.getAbsolutePath()), TestRun.class);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return result;
	}

	public TestRun saveTestRun(String email, TestRun run)
	{
		File dir = getOrCreateDirectory(getStorePath() + "runs/" + email);
		File runFile = new File(dir.getAbsolutePath() + File.separator + run.runId);
		ObjectMapper mapper = new ObjectMapper();
		// Constructs a FileWriter given a file name, using the platform's default charset
		try
		{
			mapper.writeValue(runFile, run);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return run;
	}

	public String getStorePath()
	{
		String storePath = STORAGE_PATH == null ? System.getProperty("user.dir") :
			STORAGE_PATH;
		if(!storePath.endsWith(File.separator))
			storePath += File.separator;
		File dataDir = getOrCreateDirectory(storePath + "data/");
		storePath = dataDir.getAbsolutePath();
		if(!storePath.endsWith(File.separator))
			storePath += File.separator;
		return storePath;
	}

	public File getOrCreateDirectory(String path)
	{
		File f = new File(path);
		if(f.isDirectory())
			return f;
		if(f.exists())
			return null;
		if(f.mkdirs())
			return f;
		return null;
	}

	public MasterDataResponseDto getMasterData()
	{
		File masterDataFile = new File(getStorePath() + "config/masterdata.json");
		if(masterDataFile.exists()) {
			try
			{
				return (MasterDataResponseDto)mapper.readValue(new FileImageInputStream(masterDataFile), MasterDataResponseDto.class); 
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
		return null;
	}

	public TestExtnDto[] getTestDefinitions()
	{
		File testDeinitionsFile = new File(getStorePath() + "config/test-definitions.json");
		if(testDeinitionsFile.exists()) {
			try
			{
				TestExtnDto[] testExtnDtos=(TestExtnDto[])mapper.readValue(new FileImageInputStream(testDeinitionsFile), TestExtnDto[].class);
				testExtnDtos=addValidators(testExtnDtos);
				return  testExtnDtos; 
			}
			catch(Exception ex)
			{
				ex.printStackTrace();                
			}
		}
		return null; 
	}

	private TestExtnDto[] addValidators(TestExtnDto[] testExtnDtos) {
		for(TestExtnDto testExtnDto :testExtnDtos) {
			for(ValidatorDef validatorDef:testExtnDto.validatorDefs) {
				//testExtnDto.validators.add(validatorDef.Name.);
				switch(validatorDef.Name) {
				case "MandatoryCaptureResponseValidator":
					testExtnDto.addValidator(mandatoryCaptureResponseValidator);
					break;
				case "MandatoryDeviceInfoResponseValidator":
					testExtnDto.addValidator(mandatoryDeviceInfoResponseValidator);
					break;
				case "MandatoryDiscoverResponseValidator":
					testExtnDto.addValidator(mandatoryDiscoverResponseValidator);
					break;
				case "AlwaysFailValidator":
					testExtnDto.addValidator(new AlwaysFailValidator());
					break;
				case "AlwaysPassValidator":
					testExtnDto.addValidator(new AlwaysPassValidator());
					break;
				case "CoinTossValidator":
					testExtnDto.addValidator(new CoinTossValidator());
					break;
				case "ValidValueCaptureResponseValidator":
					testExtnDto.addValidator(validValueCaptureResponseValidator);
					break;
				case "ValidValueDeviceInfoResponseValidator":
					testExtnDto.addValidator(validValueDeviceInfoResponseValidator);
					break;
				case "ValidValueDiscoverResponseValidator":
					testExtnDto.addValidator(validValueDiscoverResponseValidator);
					break;
				case "ValidValueRCaptureResponseValidator":
					testExtnDto.addValidator(validValueRCaptureResponseValidator);
					break;
				case "MandatoryRCaptureResponseValidator":
					testExtnDto.addValidator(mandatoryRCaptureResponseValidator);
					break;
				case "AuthRequestResponseValidator":
					testExtnDto.addValidator(authRequestResponseValidator);
					break;
				default :
					testExtnDto.addValidator(null);
				}
			}
		}
		return testExtnDtos;
	}
    
	public static List<TestExtnDto> filterTestType(TestManagerGetDto filter,HashMap<String, TestExtnDto> allTests)
	{
		List<TestExtnDto> results =  allTests.values().stream().filter(test -> 
				(isValid(test.processes) && test.processes.contains(filter.process)) && 
				(isValid(test.biometricTypes) && test.biometricTypes.contains(filter.biometricType)) &&
				(isValid(test.deviceSubTypes) && test.deviceSubTypes.contains(filter.deviceSubType)) && 
				( !isValid(test.mdsSpecVersions) || test.mdsSpecVersions.contains(filter.mdsSpecificationVersion ) ))
		.collect(Collectors.toList());
		
		return results;	
	}
	private static boolean isValid(List<String> value) {
		return (value != null && !value.isEmpty());
	}
}
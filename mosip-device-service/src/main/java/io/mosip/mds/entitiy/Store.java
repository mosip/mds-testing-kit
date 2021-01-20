package io.mosip.mds.entitiy;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.imageio.stream.FileImageInputStream;

import com.fasterxml.jackson.core.type.TypeReference;
import io.mosip.mds.dto.TestDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.TestManagerGetDto;
import io.mosip.mds.dto.TestRun;
import io.mosip.mds.dto.ValidatorDef;
import io.mosip.mds.dto.getresponse.MasterDataResponseDto;

@Component
public class Store {

	private static final Logger logger = LoggerFactory.getLogger(Store.class);
	
	public static String STORAGE_PATH = null;
	private static final ObjectMapper mapper = new ObjectMapper();
	private static Map<String, TestDefinition> testDefinitions = null;
	private static MasterDataResponseDto masterDataResponseDto = null;


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
			logger.error("Error reading master data", ex);
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
			logger.error("Error reading master data", ex);
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
			logger.error("Error reading master data", ex);
		}
		return run;
	}

	public static String getStorePath()
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

	public static File getOrCreateDirectory(String path)
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

	/**
	 * returns master data from local cache
	 * @return
	 */
	public static MasterDataResponseDto getMasterData()
	{
		if(masterDataResponseDto != null)
			return masterDataResponseDto;

		File masterDataFile = new File(getStorePath() + "config/masterdata.json");
		if(masterDataFile.exists()) {
			try
			{
				masterDataResponseDto = (MasterDataResponseDto) mapper.readValue(masterDataFile,
						MasterDataResponseDto.class);
			}
			catch(Exception ex)
			{
				logger.error("Error reading master data", ex);
			}
		}
		return masterDataResponseDto;
	}

	/**
	 * returns all the test definitions from local cache
	 * @return
	 */
	public static Map<String, TestDefinition> getAllTestDefinitions()
	{
		if(testDefinitions != null)
			return testDefinitions;

		File file = new File(getStorePath() + "config/test-definitions.json");
		if(file.exists()) {
			try
			{
				testDefinitions = new HashMap<>();
				List<TestDefinition> definitions = (List<TestDefinition>) mapper.readValue(file,
						new TypeReference<List<TestDefinition>>(){});

				for(TestDefinition definition : definitions) {
					testDefinitions.put(definition.testId, definition);
				}
			}
			catch(Exception ex)
			{
				logger.error("Error reading test definitions", ex);
			}
		}
		return testDefinitions;
	}


	private static boolean isValid(List<String> value) {
		return (value != null && !value.isEmpty());
	}
}
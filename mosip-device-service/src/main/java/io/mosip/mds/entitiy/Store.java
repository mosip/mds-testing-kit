package io.mosip.mds.entitiy;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.TestDefinition;
import io.mosip.mds.dto.getresponse.MasterDataResponseDto;

@Component
public class Store {

	private static final Logger logger = LoggerFactory.getLogger(Store.class);
	
	public static String STORAGE_PATH = null;
	private static final ObjectMapper mapper = new ObjectMapper();
	private static Map<String, TestDefinition> testDefinitions = null;
	private static MasterDataResponseDto masterDataResponseDto = null;

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
				testDefinitions = new LinkedHashMap<>();
				List<TestDefinition> definitions = (List<TestDefinition>) mapper.readValue(file,
						new TypeReference<List<TestDefinition>>(){});

				for(TestDefinition definition : definitions) {
					testDefinitions.put(definition.testOrderId, definition);
//					testDefinitions.put(definition.testId, definition);
				}
			}
			catch(Exception ex)
			{
				logger.error("Error reading test definitions", ex);
			}
		}
		return testDefinitions;
	}

}
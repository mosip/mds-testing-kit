package com.mosip.io.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Base {
	private static Logger logger = Logger.getLogger(DataBaseAccess.class.getName());
	public static Map<String,String> queries;
	
	public Base() {
		queries =readProperty("Query");
	}
	
	public static Map<String, String> readProperty(String propertyFileName) {
		Properties prop = new Properties();
		try {
			File propertyFile = new File(System.getProperty("user.dir") + "/config/" + propertyFileName + ".properties");
			prop.load(new FileInputStream(propertyFile));

		} catch (IOException e) {

			logger.info(e.getMessage());
		}

		Map<String, String> mapProp = prop.entrySet().stream()
				.collect(Collectors.toMap(e -> (String) e.getKey(), e -> (String) e.getValue()));

		return mapProp;
	}
}

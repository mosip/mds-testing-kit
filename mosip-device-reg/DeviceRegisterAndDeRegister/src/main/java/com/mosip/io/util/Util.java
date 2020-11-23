package com.mosip.io.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import io.restassured.response.Response;

public class Util {
	private static String auditLogFileName = "mosip_auditLogs" + getCurrentDateAndTime().replace(":", "_") + ".log";
	public static String auditLogFile = System.getProperty("user.dir") + "\\testRun\\logs\\" + auditLogFileName;
	public static Logger auditLog = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	public static String cookies;
	public static Map<String, String> commonDataProp = Util.loadProperty("/commonData.properties");
	public static String type = System.getProperty("type");

	public static Map<String, String> loadProperty(String fileName) {
		Properties prop = new Properties();
		try {
			prop.load(Util.class.getResourceAsStream(fileName));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		Map<String, String> mapProp = prop.entrySet().stream()
				.collect(Collectors.toMap(e -> (String) e.getKey(), e -> (String) e.getValue()));
		return mapProp;
	}

	public static String getCurrentDateAndTimeForAPI() {
		return	javax.xml.bind.DatatypeConverter.printDateTime(
			    Calendar.getInstance(TimeZone.getTimeZone("UTC"))
			);
		//return ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
	}

	public static void setupLogger() {
		LogManager.getLogManager().reset();
		auditLog.setLevel(Level.ALL);

		ConsoleHandler ch = new ConsoleHandler();
		ch.setFormatter(new CustomizedLogFormatter());
		ch.setLevel(Level.ALL);
		auditLog.addHandler(ch);
		try {
			FileHandler fh = new FileHandler(auditLogFile, true);
			fh.setFormatter(new CustomizedLogFormatter());
			fh.setLevel(Level.ALL);
			auditLog.addHandler(fh);
		} catch (IOException e) {
			auditLog.log(Level.SEVERE, "File logger not working", e);
		}
	}

	public static String getCurrentDateAndTime() {
		DateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
		Date date = new Date();
		return format.format(date).toString();
	}

	public static JSONObject readJsonData(String path) {
		String filePath = System.getProperty("user.dir") +"/request/"+ path;
		File fileToRead = new File(filePath);
		InputStream isOfFile = null;
		try {
			isOfFile = new FileInputStream(fileToRead);
		} catch (FileNotFoundException e1) {
			auditLog.info("File Not Found at the given path");
		}
		JSONObject jsonData = null;
		try {
			jsonData = (JSONObject) new JSONParser().parse(new InputStreamReader(isOfFile, "UTF-8"));
		} catch (IOException | ParseException | NullPointerException e) {
			auditLog.info(e.getMessage());
		}
		return jsonData;
	}

	public static Map<String, String> loadDataFromCsv(String dType) {
		String deviceType = dType;
		if (deviceType == null || deviceType.isEmpty())
			throw new RuntimeException("Unable to load csv file with type :" + dType);
		final String CSVFILEPATH = "./dataFolder/deviceData.csv";
		File file = new File(CSVFILEPATH);
		byte[] bytes = null;
		try {
			bytes = FileUtils.readFileToByteArray(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String data = new String(bytes);
		data = StringUtils.replace(data, "\r", "");
		String[] dataArray = data.split("\n");
		if(dataArray==null ||dataArray.length<2)
			throw new RuntimeException("deviceData File cannot be empty");
		String keys = dataArray[0];
		Map<String, Map<String, String>> outerMap = new HashMap<>();
		List<String> keysFromFile = new ArrayList<>();
		String[] keyArr = keys.split(",");

		keysFromFile.addAll(Arrays.asList(keyArr));
		keysFromFile.remove(0);

		for (int d = 1; d < dataArray.length; d++) {
			Map<String, String> mp = new HashMap<>();
			List<String> row = new ArrayList<>();

			String[] rowArr = dataArray[d].split(",");
			if(dType.equalsIgnoreCase("Iris") || dType.equalsIgnoreCase("Finger"))
			rowArr[5]="[1,2,3]";
			row.addAll(Arrays.asList(rowArr));

			String keyForTestCase = row.get(0);
			// now reomving the first column all the data
			row.remove(0);

			for (int i = 0; i < keysFromFile.size(); i++) {
				mp.put(keysFromFile.get(i).trim(), row.get(i).trim());
			}
			outerMap.put(keyForTestCase.toUpperCase(), mp);
		}
		Map<String, String> rowMap = outerMap.get(dType.toUpperCase());
		return rowMap;

	}

	public static void logApiInfo(String requestInJsonForm, String url, Response api_response) {
		auditLog.info("Endpoint : " + url);
		auditLog.info("Request  : " + requestInJsonForm);
		auditLog.info("Response : " + api_response.getBody().jsonPath().prettify());
	}
	
	public static void logInfo(String string) {
		auditLog.info("**    "+string+"    **");
	}
	
	public static boolean isSecdryLangRequired() {
		return commonDataProp.get("secondaryLangRequired") != null
				&& commonDataProp.get("secondaryLangRequired").equalsIgnoreCase("true");
	}
	
	public static String generateRandomString() {
	    int length = 10;
	    boolean useLetters = true;
	    boolean useNumbers = false;
	    String generatedString = RandomStringUtils.random(length, useLetters, useNumbers);
	    return generatedString;
	}

}

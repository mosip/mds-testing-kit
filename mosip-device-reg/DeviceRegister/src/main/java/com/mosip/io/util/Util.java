package com.mosip.io.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Util {
	private static String auditLogFileName = "mosip_auditLogs" + getCurrentDateAndTime().replace(":", "_") + ".log";
	public static String auditLogFile = System.getProperty("user.dir") + "\\testRun\\logs\\" + auditLogFileName;
	public static Logger auditLog = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	public static String  cookies;
	public static Map<String,String> configProp=Util.loadProperty("/config.properties");
	public static Map<String,String> commonDataProp=Util.loadProperty("/commonData.properties");
	public static String type=System.getProperty("type");
	public static Map<String,String> prop=Util.loadProperty("/"+type+".properties");

	public static Map<String, String> loadProperty(String fileName) {
		String deviceType=System.getProperty("type");
		if(deviceType==null || deviceType.isEmpty())
			throw new RuntimeException("Unable to load property file with type :"+type);
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
		return ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
	}

	public static void setupLogger() {
		LogManager.getLogManager().reset();
		auditLog.setLevel(Level.ALL);

		ConsoleHandler ch = new ConsoleHandler();
		ch.setFormatter(new CustomizedLogFormatter());
		ch.setLevel(Level.ALL);
		auditLog.addHandler(ch);
		try {
			FileHandler fh = new FileHandler(auditLogFile, false);
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
		String filePath = System.getProperty("user.dir") + path;
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
	
	public static  String  getDeviceProviderId(){
		return prop.get("deviceProviderId");
		 
	} 

}

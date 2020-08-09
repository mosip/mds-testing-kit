package com.mosip.io;

import static io.restassured.RestAssured.given;

import java.util.Base64;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.mosip.io.db.DataBaseAccess;
import com.mosip.io.pojo.Metadata;
import com.mosip.io.pojo.RegisterDeviceDTO;
import com.mosip.io.pojo.RegisterDeviceDataDTO;
import com.mosip.io.pojo.RegisterDeviceDataRequest;
import com.mosip.io.pojo.RegisterDeviceInfoRequest;
import com.mosip.io.pojo.RegisterDeviceInfo_DTO;
import com.mosip.io.util.ServiceUrl;
import com.mosip.io.util.Util;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class RegisterDevice extends Util{
	
	//6.e finally sending the request (take step 6.d registerDevice())
	public boolean centerRegistrationWithDevice(Map<String,String> prop) {
		boolean isDeviceRegistered=false;
		DataBaseAccess db= new DataBaseAccess();
		if(db.getDbData("select * from master.registered_device_master where  serial_number=" + "'" + prop.get("serialNo") + "'", "masterdata").size()>0) {
			if (db.executeQuery("delete from master.registered_device_master where  serial_number=" + "'" + prop.get("serialNo") + "'","masterdata")){
				auditLog.info("Serial number delete succesfully");
			}
		}			
		String url = ServiceUrl.REGISTERED_DEVICES;
        RestAssured.baseURI = System.getProperty("baseUrl");
        Response api_response =
                given()
                        .cookie("Authorization", Util.cookies)
                        .contentType("application/json")
                        .body(registerDevice(prop))
                        .post(url);
        
        auditLog.info("Endpoint :"+url);
	    auditLog.info("Request  :"+registerDevice(prop));
	    auditLog.info("Response  :"+api_response.getBody().jsonPath().prettify());
	    
	    ReadContext ctx = JsonPath.parse(api_response.getBody().asString());
	    if(ctx.read("$.response")!=null) {
	    	 String registeredDeviceInfo = (String)ctx.read("$.response");
	         auditLog.info("Device Registration center mapped: "+registeredDeviceInfo);
	         isDeviceRegistered=true;
	    }else {
	    	String errorMessage =(String)ctx.read("$.errors[0].message");
	    	auditLog.info(errorMessage);
	    	isDeviceRegistered=false;
	    	//throw new RuntimeException(errorMessage);
	    }
	    return isDeviceRegistered;
       
	}
	
	//6.d. create the final request 
	public RegisterDeviceDataDTO registerDevice(Map<String,String> prop) {
		RegisterDeviceDataDTO dataDTO= new RegisterDeviceDataDTO();
		dataDTO.setId("string");
		dataDTO.setMetadata(new Metadata());
		dataDTO.setRequesttime(Util.getCurrentDateAndTimeForAPI());
		dataDTO.setVersion("version");
		dataDTO.setRequest(createDeviceDataRequest(prop));
		return dataDTO;
	}
	//6.c. create request by the encoded object(step 6.b.2) and pass in deviceData
	private RegisterDeviceDataRequest createDeviceDataRequest(Map<String,String> prop) {
		RegisterDeviceDataRequest  dataRequest= new RegisterDeviceDataRequest();
		dataRequest.setDeviceData(encodeRequest(registerDeviceInfo(prop)));
		return dataRequest;
	}
	//6.b encode the request take the encode string and pass in below request digitalId field
	private RegisterDeviceInfo_DTO registerDeviceInfo(Map<String,String> prop) {
		RegisterDeviceInfo_DTO registerDeviceInfo= new RegisterDeviceInfo_DTO();
		registerDeviceInfo.setDeviceId(prop.get("deviceId"));
		registerDeviceInfo.setPurpose(prop.get("purpose"));
		registerDeviceInfo.setFoundationalTrustProviderId("");
		registerDeviceInfo.setDeviceInfo(createDeviceInfo(prop));
		return registerDeviceInfo;
	}
	//6.1.b encode the request take the encode string and pass in below request digitalId field
	private RegisterDeviceInfoRequest createDeviceInfo(Map<String,String> prop) {
		RegisterDeviceInfoRequest deviceInfo= new RegisterDeviceInfoRequest();
		deviceInfo.setDeviceSubId(prop.get("deviceSubId"));
		deviceInfo.setCertification(prop.get("certification"));
		deviceInfo.setDigitalId(encodeRequest(createRegDeviceDTO(prop)));
		deviceInfo.setFirmware(prop.get("firmware"));
		deviceInfo.setDeviceExpiry("2020-12-16T09:06:38.161Z");
		deviceInfo.setTimeStamp(Util.getCurrentDateAndTimeForAPI());
		return deviceInfo;
	}
	// 6.1.a Register device take request value from info
	private RegisterDeviceDTO createRegDeviceDTO(Map<String,String> prop) {
		RegisterDeviceDTO registerDeviceDTO= new RegisterDeviceDTO();
		registerDeviceDTO.setSerialNo(prop.get("serialNo"));
		registerDeviceDTO.setDeviceProvider(prop.get("deviceProvider"));
		registerDeviceDTO.setDeviceProviderId(prop.get("deviceProviderId"));
		registerDeviceDTO.setMake(prop.get("make"));
		registerDeviceDTO.setModel(prop.get("model"));
		registerDeviceDTO.setDateTime(Util.getCurrentDateAndTimeForAPI());
		registerDeviceDTO.setType(prop.get("type"));
		registerDeviceDTO.setDeviceSubType(prop.get("deviceSubType"));
		return registerDeviceDTO;
	}
	
	private String encodeRequest(Object value) {
		String encodeJsonValue = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			String jsonValue = mapper.writeValueAsString(value);
			encodeJsonValue = Base64.getEncoder().encodeToString(jsonValue.getBytes());
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return encodeJsonValue;
	}
}

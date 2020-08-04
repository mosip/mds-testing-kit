package com.mosip.io;

import static io.restassured.RestAssured.given;

import java.util.Base64;

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
import com.mosip.io.util.Util;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class RegisterDevice extends Util{
	
	//6.e finally sending the request (take step 6.d registerDevice())
	public void centerRegistrationWithDevice() {
		DataBaseAccess db= new DataBaseAccess();
		if(db.getDbData("select * from master.registered_device_master where  serial_number=" + "'" + prop.get("serialNo") + "'", "masterdata").size()>0) {
			if (db.executeQuery("delete from master.registered_device_master where  serial_number=" + "'" + prop.get("serialNo") + "'","masterdata")){
				auditLog.info("Serial number delete succesfully");
			}
		}			
		String url = "/v1/masterdata/registereddevices";
        RestAssured.baseURI = System.getProperty("baseUrl");
        Response api_response =
                given()
                        .cookie("Authorization", Util.cookies)
                        .contentType("application/json")
                        .body(registerDevice())
                        .post(url);
        
        auditLog.info("Endpoint :"+url);
	    auditLog.info("Request  :"+registerDevice());
	    auditLog.info("Response  :"+api_response.getBody().jsonPath().prettify());
	    
	    ReadContext ctx = JsonPath.parse(api_response.getBody().asString());
	    if(ctx.read("$.response")!=null) {
	    	 String registeredDeviceInfo = (String)ctx.read("$.response");
	         auditLog.info("Device Registration center mapped: "+registeredDeviceInfo);
	    }else {
	    	String errorMessage =(String)ctx.read("$.errors[0].message");
	    	throw new RuntimeException(errorMessage);
	    }

       
	}
	
	//6.d. create the final request 
	public RegisterDeviceDataDTO registerDevice() {
		RegisterDeviceDataDTO dataDTO= new RegisterDeviceDataDTO();
		dataDTO.setId("string");
		dataDTO.setMetadata(new Metadata());
		dataDTO.setRequesttime(Util.getCurrentDateAndTimeForAPI());
		dataDTO.setVersion("version");
		dataDTO.setRequest(createDeviceDataRequest());
		return dataDTO;
	}
	//6.c. create request by the encoded object(step 6.b.2) and pass in deviceData
	private RegisterDeviceDataRequest createDeviceDataRequest() {
		RegisterDeviceDataRequest  dataRequest= new RegisterDeviceDataRequest();
		dataRequest.setDeviceData(encodeRequest(registerDeviceInfo()));
		return dataRequest;
	}
	//6.b encode the request take the encode string and pass in below request digitalId field
	private RegisterDeviceInfo_DTO registerDeviceInfo() {
		RegisterDeviceInfo_DTO registerDeviceInfo= new RegisterDeviceInfo_DTO();
		registerDeviceInfo.setDeviceId(prop.get("deviceId"));
		registerDeviceInfo.setPurpose(prop.get("purpose"));
		registerDeviceInfo.setFoundationalTrustProviderId("");
		registerDeviceInfo.setDeviceInfo(createDeviceInfo());
		return registerDeviceInfo;
	}
	//6.1.b encode the request take the encode string and pass in below request digitalId field
	private RegisterDeviceInfoRequest createDeviceInfo() {
		RegisterDeviceInfoRequest deviceInfo= new RegisterDeviceInfoRequest();
		deviceInfo.setDeviceSubId(prop.get("deviceSubId"));
		deviceInfo.setCertification(prop.get("certification"));
		deviceInfo.setDigitalId(encodeRequest(createRegDeviceDTO()));
		deviceInfo.setFirmware(prop.get("firmware"));
		deviceInfo.setDeviceExpiry("2020-12-16T09:06:38.161Z");
		deviceInfo.setTimeStamp(Util.getCurrentDateAndTimeForAPI());
		return deviceInfo;
	}
	// 6.1.a Register device take request value from info
	private RegisterDeviceDTO createRegDeviceDTO() {
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

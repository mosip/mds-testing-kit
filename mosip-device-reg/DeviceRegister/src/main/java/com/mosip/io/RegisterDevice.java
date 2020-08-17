package com.mosip.io;

import static io.restassured.RestAssured.given;

import java.util.Base64;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
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
	public boolean registerDevice(Map<String,String> prop) {
		boolean isDeviceRegistered=false;
		String requestInJsonForm="";
		 ObjectMapper mapper= new ObjectMapper();
		String url = ServiceUrl.REGISTERED_DEVICES;
        RestAssured.baseURI = System.getProperty("baseUrl");
        RegisterDeviceDataDTO dto=createRegisterDevice(prop);
        Response api_response =
                given()
                        .cookie("Authorization", Util.cookies)
                        .contentType("application/json")
                        .body(dto)
                        .post(url);
       
        try {
        	requestInJsonForm=mapper.writeValueAsString(dto);
		} catch (Exception e) {
			e.printStackTrace();
		}
		logApiInfo(requestInJsonForm, System.getProperty("baseUrl")+url, api_response);
		ReadContext ctx = JsonPath.parse(api_response.getBody().asString());
		if (ctx.read("$.response") != null) {
			String registeredDeviceInfo = (String) ctx.read("$.response");
			auditLog.info("Device Registration Center Mapped: " + registeredDeviceInfo);
			isDeviceRegistered = true;
		} else {
			String errorMessage = (String) ctx.read("$.errors[0].message");
			auditLog.info(errorMessage);
			isDeviceRegistered = false;
			// throw new RuntimeException(errorMessage);
		}
		return isDeviceRegistered;
       
	}
	
	//6.d. create the final request 
	private RegisterDeviceDataDTO createRegisterDevice(Map<String,String> prop) {
		RegisterDeviceDataDTO dataDTO = new RegisterDeviceDataDTO();
		dataDTO.setId("string");
		dataDTO.setMetadata(new Metadata());
		dataDTO.setRequesttime(getCurrentDateAndTimeForAPI());
		dataDTO.setVersion("version");
		dataDTO.setRequest(createDeviceDataRequest(prop));
		auditLog.info(convertObjectToJsonString(dataDTO));
		return dataDTO;
	}
	//6.c. create request by the encoded object(step 6.b.2) and pass in deviceData
	private RegisterDeviceDataRequest createDeviceDataRequest(Map<String,String> prop) {
		RegisterDeviceDataRequest dataRequest = new RegisterDeviceDataRequest();
		dataRequest.setDeviceData(encodeRequest(registerDeviceInfo(prop)));
		auditLog.info(convertObjectToJsonString(dataRequest));
		return dataRequest;
	}
	//6.b encode the request take the encode string and pass in below request digitalId field
	private RegisterDeviceInfo_DTO registerDeviceInfo(Map<String,String> prop) {
		RegisterDeviceInfo_DTO registerDeviceInfo = new RegisterDeviceInfo_DTO();
		registerDeviceInfo.setDeviceId(prop.get("deviceId"));
		registerDeviceInfo.setPurpose(prop.get("purpose"));
		registerDeviceInfo.setFoundationalTrustProviderId("");
		registerDeviceInfo.setDeviceInfo(createDeviceInfo(prop));
		auditLog.info(convertObjectToJsonString(registerDeviceInfo));
		return registerDeviceInfo;
	}
	//6.1.b encode the request take the encode string and pass in below request digitalId field
	private RegisterDeviceInfoRequest createDeviceInfo(Map<String,String> prop) {
		RegisterDeviceInfoRequest deviceInfo = new RegisterDeviceInfoRequest();
		deviceInfo.setDeviceSubId(prop.get("deviceSubId"));
		deviceInfo.setCertification(prop.get("certification"));
		deviceInfo.setDigitalId(encodeRequest(createRegDeviceDTO(prop)));
		deviceInfo.setFirmware(prop.get("firmware"));
		deviceInfo.setDeviceExpiry("2020-12-16T09:06:38.161Z");
		deviceInfo.setTimeStamp(getCurrentDateAndTimeForAPI());
		auditLog.info(convertObjectToJsonString(deviceInfo));
		return deviceInfo;
	}
	// 6.1.a Register device take request value from info
	private RegisterDeviceDTO createRegDeviceDTO(Map<String,String> prop) {
		RegisterDeviceDTO registerDeviceDTO = new RegisterDeviceDTO();
		registerDeviceDTO.setSerialNo(prop.get("serialNo"));
		registerDeviceDTO.setDeviceProvider(prop.get("deviceProvider"));
		registerDeviceDTO.setDeviceProviderId(prop.get("deviceProviderId"));
		registerDeviceDTO.setMake(prop.get("make"));
		registerDeviceDTO.setModel(prop.get("model"));
		registerDeviceDTO.setDateTime(getCurrentDateAndTimeForAPI());
		registerDeviceDTO.setType(prop.get("type"));
		registerDeviceDTO.setDeviceSubType(prop.get("deviceSubType"));
		auditLog.info(convertObjectToJsonString(registerDeviceDTO));
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
	private String convertObjectToJsonString(Object object) {
		String jsonString="";
		ObjectMapper mapper = new ObjectMapper();
		try {
			jsonString=mapper.writeValueAsString(object);
		} catch (Exception e) {
		}
		return jsonString;
	}
}

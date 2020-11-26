package com.mosip.io.pmp;

import static io.restassured.RestAssured.given;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.mosip.io.db.DataBaseAccess;
import com.mosip.io.dto.RegisterDeviceDTO;
import com.mosip.io.dto.RegisterDeviceDataDTO;
import com.mosip.io.dto.RegisterDeviceInfo_DTO;
import com.mosip.io.pmp.model.Metadata;
import com.mosip.io.pmp.model.RegisterDeviceDataRequest;
import com.mosip.io.pmp.model.RegisterDeviceInfoRequest;
import com.mosip.io.util.EndPoint;
import com.mosip.io.util.Util;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class RegisterDevice extends Util{
	
	//6.e finally sending the request (take step 6.d registerDevice())
	@SuppressWarnings("unused")
	public  String registerDevice(String deviceId,Map<String,String> prop) {
		String registeredDeviceId=null;
		String sqlQuery="Select code FROM regdevice.registered_device_master where device_id="+"'"+deviceId+"'" +"and serial_number="+"'"+prop.get("serialNo")+"'";
		DataBaseAccess db= new DataBaseAccess();
		List<String> deviceCodeId = db.getDbData(sqlQuery,"regdevice");
		if(!(deviceCodeId.isEmpty()) && deviceCodeId.size()>0) {
			auditLog.info("DeviceDetail  already present in DB");
			registeredDeviceId=deviceCodeId.get(0);
			auditLog.info("Device is already registered with id : "+registeredDeviceId +" and with the serialNo: "+prop.get("serialNo"));
		}else {
		String requestInJsonForm="";
		ObjectMapper mapper= new ObjectMapper();
		String url = EndPoint.REGISTERED_DEVICES;
        RestAssured.baseURI = System.getProperty("baseUrl");
        RegisterDeviceDataDTO dto=createRegisterDevice(deviceId,prop);
        Response api_response =
                given()
                        .cookie("Authorization", cookies)
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
			String payload=registeredDeviceInfo.split("\\.")[1];
			String decodeString=new String(Base64.getDecoder().decode(payload));
			JSONObject jsonObject= new JSONObject(decodeString);
			registeredDeviceId=jsonObject.get("deviceCode").toString();
				/*
				 * db= new DataBaseAccess(); deviceCodeId = db.getDbData(sqlQuery,"regdevice");
				 * if(!(deviceCodeId.isEmpty()) && deviceCodeId.size()>0) {
				 * registeredDeviceId=deviceCodeId.get(0); }
				 */
		} else {
			String errorMessage = (String) ctx.read("$.errors.message");
			auditLog.info(errorMessage);
		}
		}
		return registeredDeviceId;
       
	}
	
	//6.d. create the final request 
	private RegisterDeviceDataDTO createRegisterDevice(String deviceId,Map<String,String> prop) {
		RegisterDeviceDataDTO dataDTO = new RegisterDeviceDataDTO();
		dataDTO.setId("string");
		dataDTO.setMetadata(new Metadata());
		dataDTO.setRequesttime(getCurrentDateAndTimeForAPI());
		dataDTO.setVersion("version");
		dataDTO.setRequest(createDeviceDataRequest(deviceId,prop));
		auditLog.info(convertObjectToJsonString(dataDTO));
		return dataDTO;
	}
	//6.c. create request by the encoded object(step 6.b.2) and pass in deviceData
	private RegisterDeviceDataRequest createDeviceDataRequest(String deviceId,Map<String,String> prop) {
		RegisterDeviceDataRequest dataRequest = new RegisterDeviceDataRequest();
		dataRequest.setDeviceData(encodeRequest(registerDeviceInfo(deviceId,prop)));
		auditLog.info(convertObjectToJsonString(dataRequest));
		return dataRequest;
	}
	//6.b encode the request take the encode string and pass in below request digitalId field
	private RegisterDeviceInfo_DTO registerDeviceInfo(String deviceId,Map<String,String> prop) {
		RegisterDeviceInfo_DTO registerDeviceInfo = new RegisterDeviceInfo_DTO();
		//registerDeviceInfo.setDeviceId(prop.get("deviceId"));
		registerDeviceInfo.setDeviceId(deviceId);
		registerDeviceInfo.setPurpose(prop.get("purpose"));
		registerDeviceInfo.setFoundationalTrustProviderId("");
		registerDeviceInfo.setDeviceInfo(encodeRequest(createDeviceInfo(prop)));
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
		deviceInfo.setDeviceExpiry(commonDataProp.get("validityDateTime"));
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

package com.mosip.io;

import static io.restassured.RestAssured.given;

import java.util.Map;
import org.json.simple.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.mosip.io.db.DataBaseAccess;
import com.mosip.io.pojo.DeviceRegistrationCenterMappingDTO;
import com.mosip.io.util.Util;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class DeviceRegistrationCenterMapping  extends Util{
	Map<String,String> prop=Util.loadProperty("/"+System.getProperty("type")+".properties");
	String regCenterId=commonDataProp.get("regCenterId");
	public String deviceRegCenterMapping(String createdDeviceId) {
		
		//5.a check that device is isAcitive= true in both the language based on deviceId
		 String deviceId =null;
		 CreateDevice createDevice= new CreateDevice();
		 boolean status=createDevice.deviceIsActive(createdDeviceId);
		    if(status) {
		    	auditLog.info(" Device is Active in both the language");
		    }else {
		    	throw new RuntimeException("Device is not active");
		    }
		 
		  //5.b check registration center  is active true in both the language  (bases on centerId)
		if(isRegCenterActiveIn_Prim_Second_language(regCenterId)) {
			DeviceRegistrationCenterMappingDTO deviceRegCentrMapDTO= new DeviceRegistrationCenterMappingDTO();
			JSONObject jsonData = Util.readJsonData("/Request/deviceRegistrationCenterMapping.json");
			auditLog.info("Acutual Reqeust: "+jsonData.toJSONString());
			try {
				ObjectMapper mapper = new ObjectMapper();
				deviceRegCentrMapDTO = mapper.readValue(jsonData.toJSONString(), DeviceRegistrationCenterMappingDTO.class);
				deviceRegCentrMapDTO.getRequest().setDeviceId(createdDeviceId);
				deviceRegCentrMapDTO.getRequest().setRegCenterId(regCenterId);
				deviceRegCentrMapDTO.setRequesttime(Util.getCurrentDateAndTimeForAPI());
				String value=mapper.writeValueAsString(deviceRegCentrMapDTO);
				auditLog.info("Updated Reqeust: "+value);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			
			String url = "/v1/masterdata/registrationcenterdevice";
	        RestAssured.baseURI = System.getProperty("baseUrl");
	        Response api_response =
	                given()
	                        .cookie("Authorization", Util.cookies)
	                        .contentType("application/json")
	                        .body(deviceRegCentrMapDTO)
	                        .post(url);
	        
	        auditLog.info("Endpoint :"+url);
		    auditLog.info("Request  :"+deviceRegCentrMapDTO);
		    auditLog.info("Response  :"+api_response.getBody().jsonPath().prettify());
		    
		    ReadContext ctx = JsonPath.parse(api_response.getBody().asString());
		   
		    if(ctx.read("$.response") != null) {
		    	 deviceId = (String)ctx.read("$.response.deviceId");
		    	 String regCenteId = (String)ctx.read("$.response.regCenterId");
		    	 auditLog.info("Device Registration  center  mapped with deviceId: "+deviceId+" and regcenerId :"+regCenteId);
		    }else {
		    	String errorMessage =(String)ctx.read("$.errors[0].message");
		    	throw new RuntimeException(errorMessage);
		    }
	       	
		}else {
			auditLog.info("RegCenterId: "+regCenterId+" is not activated in Primary and secondory language ");
			throw new RuntimeException("RegCenterId: "+regCenterId+" is not activated in Primary and secondory language ");
		}
		
		return deviceId;
	}
	
	
	private boolean isRegCenterActiveIn_Prim_Second_language(String regCenterId) {
		boolean isRegCenterActive = false;
		DataBaseAccess db= new DataBaseAccess();
		String regCenterQueryEng = "Select * from master.registration_center where id="+"'"+regCenterId+"'"+" and is_active='true' and lang_code='eng'";
		String regCenterQueryAra = "Select * from master.registration_center where id="+"'"+regCenterId+"'"+" and is_active='true' and lang_code='ara'";
		if (db.getDbData(regCenterQueryEng, "masterdata").size()>0 && db.getDbData(regCenterQueryAra, "masterdata").size()>0)
			isRegCenterActive = true;
		return isRegCenterActive;
	}
	 
}

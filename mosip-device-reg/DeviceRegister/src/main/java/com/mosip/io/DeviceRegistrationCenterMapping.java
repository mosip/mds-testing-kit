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
import com.mosip.io.util.ServiceUrl;
import com.mosip.io.util.Util;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class DeviceRegistrationCenterMapping  extends Util{
	
	public String deviceRegCenterMapping(String createdDeviceId,Map<String,String> prop,String primaryLanguage) {
		String regCenterId = prop.get("regCenterId");
		// 5.a check that device is isAcitive= true in both the language based on
		// deviceId
		String deviceId = null;
		String requestInJsonForm = "";
		CreateDevice createDevice = new CreateDevice();
		boolean status = createDevice.deviceIsActive(createdDeviceId,primaryLanguage);
		if (status) {
			auditLog.info("**Device is Active**");
		} else {
			throw new RuntimeException("Device is not active");
		}
		 
		  //5.b check registration center  is active true in both the language  (bases on centerId)
		if(isRegCenterActiveIn_Prim_Second_language(regCenterId,primaryLanguage)) {
			auditLog.info("**Registration Center  is Active**");
			DeviceRegistrationCenterMappingDTO deviceRegCentrMapDTO= new DeviceRegistrationCenterMappingDTO();
			JSONObject jsonData = Util.readJsonData("/Request/deviceRegistrationCenterMapping.json");
			try {
				ObjectMapper mapper = new ObjectMapper();
				deviceRegCentrMapDTO = mapper.readValue(jsonData.toJSONString(), DeviceRegistrationCenterMappingDTO.class);
				deviceRegCentrMapDTO.getRequest().setDeviceId(createdDeviceId);
				deviceRegCentrMapDTO.getRequest().setRegCenterId(regCenterId);
				deviceRegCentrMapDTO.setRequesttime(Util.getCurrentDateAndTimeForAPI());
				requestInJsonForm=mapper.writeValueAsString(deviceRegCentrMapDTO);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			
			String url = ServiceUrl.REGISTRATION_CENTER_DEVICE_MAPPING;
	        RestAssured.baseURI = System.getProperty("baseUrl");
	        Response api_response =
	                given()
	                        .cookie("Authorization", Util.cookies)
	                        .contentType("application/json")
	                        .body(deviceRegCentrMapDTO)
	                        .post(url);
	        logApiInfo(requestInJsonForm, System.getProperty("baseUrl")+url, api_response);
	        
		    ReadContext ctx = JsonPath.parse(api_response.getBody().asString());
		    if(ctx.read("$.response") != null) {
		    	 deviceId = (String)ctx.read("$.response.deviceId");
		    	 String regCenteId = (String)ctx.read("$.response.regCenterId");
		    	 auditLog.info("Device And Center Mapped With DeviceId: "+deviceId+" And RegcenterId :"+regCenteId);
		    }else {
		    	String errorMessage =(String)ctx.read("$.errors[0].message");
		    	auditLog.warning(errorMessage);
		    }
	       	
		}else {
			auditLog.info("RegCenterId: "+regCenterId+" is not activated in Primary or secondory language ");
			throw new RuntimeException("RegCenterId: "+regCenterId+" is not activated in Primary or secondory language ");
		}
		
		return deviceId;
	}
	
	
	private boolean isRegCenterActiveIn_Prim_Second_language(String regCenterId,String primaryLanguage) {
		boolean isRegCenterActive = false;
		DataBaseAccess db= new DataBaseAccess();
		String regCenterQueryEng = "Select * from master.registration_center where id="+"'"+regCenterId+"'"+" and is_active='true' and lang_code="+"'"+primaryLanguage+"'";
		if (isSecdryLangRequired()) {
			String regCenterQueryAra = "Select * from master.registration_center where id="+"'"+regCenterId+"'"+" and is_active='true' and lang_code="+"'"+commonDataProp.get("secondaryLanguage")+"'";
			if (db.getDbData(regCenterQueryEng, "masterdata").size()>0 && db.getData(regCenterQueryAra, "masterdata").size()>0)
				isRegCenterActive = true;
		}
		else if (db.getDbData(regCenterQueryEng, "masterdata").size()>0)
			isRegCenterActive = true;
		return isRegCenterActive;
	}
	 
}

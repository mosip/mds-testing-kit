package com.mosip.io;

import static io.restassured.RestAssured.given;
import org.json.simple.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.mosip.io.db.DataBaseAccess;
import com.mosip.io.pojo.CreateDeviceDTO;
import com.mosip.io.util.Util;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class CreateDevice extends Util{
	
	public String createDevice(String deviceSpecId,String id,String langCode) {
		CreateDeviceDTO createDeviceDTO= new CreateDeviceDTO();
		JSONObject jsonData = Util.readJsonData("/Request/createDevice.json");
		auditLog.info("Acutual Reqeust: "+jsonData.toJSONString());
		try {
			ObjectMapper mapper = new ObjectMapper();
			createDeviceDTO = mapper.readValue(jsonData.toJSONString(), CreateDeviceDTO.class);
			createDeviceDTO.getRequest().setDeviceSpecId(deviceSpecId);
			createDeviceDTO.getRequest().setId(id);
			createDeviceDTO.getRequest().setLangCode(langCode);
			createDeviceDTO.getRequest().setMacAddress(commonDataProp.get("macAddress"));
			createDeviceDTO.getRequest().setName(commonDataProp.get("name"));
			createDeviceDTO.getRequest().setSerialNum(prop.get("serialNo"));
			createDeviceDTO.getRequest().setValidityDateTime(commonDataProp.get("validityDateTime"));
			createDeviceDTO.getRequest().setZoneCode(commonDataProp.get("zoneCode"));
			createDeviceDTO.setRequesttime(Util.getCurrentDateAndTimeForAPI());
			
			String value=mapper.writeValueAsString(createDeviceDTO);
			auditLog.info("Updated Reqeust: "+value);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		String url = "/v1/masterdata/devices";
        RestAssured.baseURI = System.getProperty("baseUrl");
        Response api_response =
                given()
                        .cookie("Authorization", Util.cookies)
                        .contentType("application/json")
                        .body(createDeviceDTO)
                        .post(url);
        
        auditLog.info("Endpoint :"+url);
	    auditLog.info("Request  :"+createDeviceDTO);
	    auditLog.info("Response  :"+api_response.getBody().jsonPath().prettify());
	    
	    ReadContext ctx = JsonPath.parse(api_response.getBody().asString());
	    String deviceId =null;
	    if(ctx.read("$.response") != null) {
	    	deviceId = (String)ctx.read("$.response.id");
	        auditLog.info("Device Created in "+langCode+" with deviceId: "+deviceId);
	    }else {
	    	String errorMessage =(String)ctx.read("$.errors[0].message");
	    	throw new RuntimeException(errorMessage);
	    }
        
		return deviceId;
	}
	
	public String updateDeviceIdWithCode(String deviceId,String deviceSpecId) {
		String deviceIdUpdatedValue=null;
		String deviceIdValue=prop.get("deviceCode");
		DataBaseAccess db= new DataBaseAccess();
			
		if(db.getDbData("select * from master.device_master where  id=" + "'" + deviceIdValue + "'", "masterdata").size()>0) {
			if (db.executeQuery("delete from master.reg_center_device where  device_id=" + "'" + deviceIdValue + "'",
					"masterdata") && db.executeQuery("delete from master.device_master where  id=" + "'" + deviceIdValue + "'",
					"masterdata")) {
				auditLog.info(deviceIdValue + " alredy exit in DB  so, deleting..");
			}
		} 
		if(db.executeQuery("update  master.device_master set id="+"'"+deviceIdValue+"'" +" where id="+"'"+deviceId+"'" +" and dspec_id="+"'"+deviceSpecId+"'", "masterdata")) {
			deviceIdUpdatedValue=deviceIdValue;
			auditLog.info(deviceIdValue + " updated in DB");
		}
		return deviceIdUpdatedValue;
	}
	
	public boolean deviceIsActive(String deviceId) {
		boolean isActive=false;
		DataBaseAccess db= new DataBaseAccess();
		String deviceMasterQueryEng = "Select * from master.device_master where id="+"'"+deviceId+"'"+" and is_active='true' and lang_code='eng'";
		String deviceMasterrQueryAra = "Select * from master.device_master where id="+"'"+deviceId+"'"+" and is_active='true' and lang_code='ara'";
		if (db.getDbData(deviceMasterQueryEng, "masterdata").size()>0 && db.getDbData(deviceMasterrQueryAra, "masterdata").size()>0)
			isActive = true;
		return isActive;
	}
}

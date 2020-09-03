package com.mosip.io;

import static io.restassured.RestAssured.given;

import java.util.Map;

import org.json.simple.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.mosip.io.db.DataBaseAccess;
import com.mosip.io.pojo.CreateDeviceDTO;
import com.mosip.io.util.ServiceUrl;
import com.mosip.io.util.Util;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class CreateDevice extends Util{
	
	public String createDevice(String deviceSpecId,String id,String langCode,Map<String,String> prop) {
		CreateDeviceDTO createDeviceDTO = new CreateDeviceDTO();
		JSONObject jsonData = Util.readJsonData("/Request/createDevice.json");
		String requestInJsonForm = "";
		try {
			ObjectMapper mapper = new ObjectMapper();
			createDeviceDTO = mapper.readValue(jsonData.toJSONString(), CreateDeviceDTO.class);
			createDeviceDTO.getRequest().setDeviceSpecId(deviceSpecId);
			createDeviceDTO.getRequest().setId(id);
			createDeviceDTO.getRequest().setLangCode(langCode);
			createDeviceDTO.getRequest().setMacAddress(commonDataProp.get("macAddress"));
			createDeviceDTO.getRequest().setName(prop.get("name"));
			createDeviceDTO.getRequest().setSerialNum(prop.get("serialNo"));
			createDeviceDTO.getRequest().setValidityDateTime(commonDataProp.get("validityDateTime"));
			createDeviceDTO.getRequest().setZoneCode(prop.get("zoneCode"));
			createDeviceDTO.setRequesttime(getCurrentDateAndTimeForAPI());
			requestInJsonForm = mapper.writeValueAsString(createDeviceDTO);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		String url = ServiceUrl.CREATE_DEVICE;
		RestAssured.baseURI = System.getProperty("baseUrl");
        Response api_response =
                given()
                        .cookie("Authorization", Util.cookies)
                        .contentType("application/json")
                        .body(createDeviceDTO)
                        .post(url);
        
		logApiInfo(requestInJsonForm, System.getProperty("baseUrl")+url, api_response);
		ReadContext ctx = JsonPath.parse(api_response.getBody().asString());
		String deviceId = null;
		if (ctx.read("$.response") != null) {
			deviceId = (String) ctx.read("$.response.id");
			auditLog.info("Device Created In " + langCode + " With DeviceId: " + deviceId);
		} else {
			String errorMessage = (String) ctx.read("$.errors[0].message");
			auditLog.warning(errorMessage);
		}

		return deviceId;
	}

	
	public String updateDeviceIdWithCode(String type,String deviceId,String deviceSpecId,Map<String,String> prop) {
		String deviceIdUpdatedValue=null;
		String deviceIdValue=prop.get("deviceCode");
		DataBaseAccess db= new DataBaseAccess();
			
		if(db.getDbData("select * from master.device_master where  id=" + "'" + deviceIdValue + "'", "masterdata").size()>0) {
			if(type.equalsIgnoreCase("Auth")) {
				if (db.executeQuery("delete from master.device_master where  id=" + "'" + deviceIdValue + "'",
						"masterdata")) {
					auditLog.info(deviceIdValue + " alredy exit in DB  so, deleting..");
				}
			}
			else if (db.executeQuery("delete from master.reg_center_device where  device_id=" + "'" + deviceIdValue + "'",
					"masterdata") && db.executeQuery("delete from master.device_master where  id=" + "'" + deviceIdValue + "'",
					"masterdata")) {
				auditLog.info(deviceIdValue + " alredy exit in DB  so, deleting..");
			}
		} 
		if(db.executeQuery("update  master.device_master set id="+"'"+deviceIdValue+"'" +" where id="+"'"+deviceId+"'" +" and dspec_id="+"'"+deviceSpecId+"'", "masterdata") &&
				db.executeQuery("update  master.device_master_h set id="+"'"+deviceIdValue+"'" +" where id="+"'"+deviceId+"'" +" and dspec_id="+"'"+deviceSpecId+"'", "masterdata")) {
			deviceIdUpdatedValue=deviceIdValue;
			auditLog.info("DeviceIdValue : "+deviceIdValue + " Updated In DB");
		}
		return deviceIdUpdatedValue;
	}
	
	public boolean deviceIsActive(String deviceId,String primaryLanguage) {
		boolean isActive=false;
		DataBaseAccess db= new DataBaseAccess();
		String deviceMasterQueryInEng = "Select * from master.device_master where id="+"'"+deviceId+"'"+" and is_active='true' and lang_code="+"'"+primaryLanguage+"'";
		if (isSecdryLangRequired()) {
			String deviceMasterQueryInAra = "Select * from master.device_master where id="+"'"+deviceId+"'"+" and is_active='true' and lang_code="+"'"+commonDataProp.get("secondaryLanguage")+"'";
			if (db.getDbData(deviceMasterQueryInEng, "masterdata").size()>0 && db.getData(deviceMasterQueryInAra, "masterdata").size()>0)
				isActive = true;
		}
		else if (db.getDbData(deviceMasterQueryInEng, "masterdata").size()>0)
			isActive = true;
		return isActive;
	}
}
